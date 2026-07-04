import type {
  ProductFormulaOptionMaterialVO,
  ProductFormulaOptionVO,
  ProductFormulaOptionValueVO,
  ProductFormulaRestrictionVO,
  ProductFormulaSetupVO
} from '@/api/product-capability/types'

export type DraftOption = ProductFormulaOptionVO & {
  clientKey?: string
  visibleConditionOptionClientKey?: string
  visibleConditionValueClientKey?: string
}

export type DraftOptionValue = ProductFormulaOptionValueVO & {
  clientKey?: string
  optionClientKey?: string
}

export type DraftOptionMaterial = ProductFormulaOptionMaterialVO & {
  optionClientKey?: string
  valueClientKey?: string
}

type DraftRestriction = ProductFormulaRestrictionVO & {
  targetOptionClientKey?: string
  targetValueClientKey?: string
  conditionOptionClientKey?: string
  conditionValueClientKey?: string
}

const OPTION_PREFIX = 'OPTION_'
const VALUE_PREFIX = 'VALUE_'

export function createDraftClientKey(prefix: string) {
  return `${prefix}_${Date.now().toString(36)}_${Math.random().toString(36).slice(2, 8)}`
}

export function optionClientKey(row?: ProductFormulaOptionVO) {
  return (row as DraftOption | undefined)?.clientKey || ''
}

export function valueClientKey(row?: ProductFormulaOptionValueVO) {
  return (row as DraftOptionValue | undefined)?.clientKey || ''
}

export function valueOwnerClientKey(row?: ProductFormulaOptionValueVO) {
  return (row as DraftOptionValue | undefined)?.optionClientKey || ''
}

export function materialOwnerClientKey(row?: ProductFormulaOptionMaterialVO) {
  return (row as DraftOptionMaterial | undefined)?.optionClientKey || ''
}

export function materialValueClientKey(row?: ProductFormulaOptionMaterialVO) {
  return (row as DraftOptionMaterial | undefined)?.valueClientKey || ''
}

export function normalizeFormulaOptionDraftState(setup: ProductFormulaSetupVO) {
  const options = (setup.options || []) as DraftOption[]
  const values = (setup.optionValues || []) as DraftOptionValue[]
  const materials = (setup.optionMaterials || []) as DraftOptionMaterial[]
  const restrictions = (setup.restrictions || []) as DraftRestriction[]

  const originalOptionKeyByCode = normalizeOptionClientKeys(options)
  normalizeValueClientKeys(values, originalOptionKeyByCode)
  const originalValueKeyByCode = new Map(values.map((row) => [oldValueMapKey(row), row.clientKey || '']))

  normalizeOptionCodes(options)
  normalizeValueCodes(values, new Map(options.map((row) => [row.clientKey || '', row.optionCode || ''])))
  normalizeMaterialReferences(materials, originalOptionKeyByCode, originalValueKeyByCode, options, values)
  normalizeOptionVisibility(options, originalOptionKeyByCode, originalValueKeyByCode, values)
  normalizeRestrictionTargets(restrictions, originalOptionKeyByCode, originalValueKeyByCode, options, values)
}

export function buildFormulaOptionPayload(setup: ProductFormulaSetupVO): ProductFormulaSetupVO {
  normalizeFormulaOptionDraftState(setup)
  return {
    materials: setup.materials,
    options: stripOptions(setup.options),
    optionValues: stripValues(setup.optionValues),
    optionMaterials: stripMaterials(setup.optionMaterials),
    restrictions: stripRestrictions(setup.restrictions),
    usageRules: setup.usageRules,
    variables: setup.variables,
    variableRules: setup.variableRules
  }
}

export function nextOptionCode(options: ProductFormulaOptionVO[]) {
  return nextCode(new Set(options.map((row) => normalizeCode(row.optionCode)).filter(Boolean)), OPTION_PREFIX)
}

export function nextValueCode(values: ProductFormulaOptionValueVO[], optionKey: string) {
  return nextCode(new Set(values
    .filter((row) => valueOwnerClientKey(row) === optionKey)
    .map((row) => normalizeCode(row.valueCode))
    .filter(Boolean)), VALUE_PREFIX)
}

function normalizeOptionClientKeys(options: DraftOption[]) {
  const usedKeys = new Set<string>()
  const firstKeyByCode = new Map<string, string>()
  options.forEach((option, index) => {
    option.clientKey = uniqueKey(option.clientKey || optionIdentity(option, index), usedKeys)
    const code = normalizeCode(option.optionCode)
    if (code && !firstKeyByCode.has(code)) firstKeyByCode.set(code, option.clientKey)
  })
  return firstKeyByCode
}

function normalizeValueClientKeys(values: DraftOptionValue[], optionKeyByCode: Map<string, string>) {
  const usedKeys = new Set<string>()
  values.forEach((value, index) => {
    const optionCode = normalizeCode(value.optionCode)
    value.optionClientKey = value.optionClientKey || optionKeyByCode.get(optionCode) || ''
    value.clientKey = uniqueKey(value.clientKey || valueIdentity(value, index), usedKeys)
  })
}

function normalizeOptionCodes(options: DraftOption[]) {
  const usedCodes = new Set<string>()
  options.forEach((option) => {
    const code = normalizeCode(option.optionCode)
    option.optionCode = code && !usedCodes.has(code) ? code : nextCode(usedCodes, OPTION_PREFIX)
    usedCodes.add(option.optionCode)
  })
}

function normalizeValueCodes(values: DraftOptionValue[], optionCodeByKey: Map<string, string>) {
  const usedByOption = new Map<string, Set<string>>()
  values.forEach((value) => {
    const optionCode = optionCodeByKey.get(value.optionClientKey || '') || normalizeCode(value.optionCode)
    value.optionCode = optionCode
    const used = usedByOption.get(optionCode) || new Set<string>()
    const code = normalizeCode(value.valueCode)
    value.valueCode = code && !used.has(code) ? code : nextCode(used, VALUE_PREFIX)
    used.add(value.valueCode)
    usedByOption.set(optionCode, used)
  })
}

function normalizeMaterialReferences(
  materials: DraftOptionMaterial[],
  originalOptionKeyByCode: Map<string, string>,
  originalValueKeyByCode: Map<string, string>,
  options: DraftOption[],
  values: DraftOptionValue[]
) {
  const optionByKey = new Map(options.map((row) => [row.clientKey || '', row]))
  const valueByKey = new Map(values.map((row) => [row.clientKey || '', row]))
  materials.forEach((material) => {
    const oldOptionCode = normalizeCode(material.optionCode)
    const oldValueCode = normalizeCode(material.valueCode)
    material.optionClientKey = material.optionClientKey || originalOptionKeyByCode.get(oldOptionCode) || ''
    material.valueClientKey = material.valueClientKey || originalValueKeyByCode.get(`${oldOptionCode}:${oldValueCode}`) || ''
    material.optionCode = optionByKey.get(material.optionClientKey)?.optionCode || material.optionCode
    material.valueCode = valueByKey.get(material.valueClientKey)?.valueCode || material.valueCode
  })
}

function normalizeOptionVisibility(
  options: DraftOption[],
  originalOptionKeyByCode: Map<string, string>,
  originalValueKeyByCode: Map<string, string>,
  values: DraftOptionValue[]
) {
  const optionByKey = new Map(options.map((row) => [row.clientKey || '', row]))
  const valueByKey = new Map(values.map((row) => [row.clientKey || '', row]))
  options.forEach((option) => {
    const oldOptionCode = normalizeCode(option.visibleConditionOptionCode)
    const oldValueCode = normalizeCode(option.visibleConditionValueCode)
    option.visibleConditionOptionClientKey = option.visibleConditionOptionClientKey || originalOptionKeyByCode.get(oldOptionCode) || ''
    option.visibleConditionValueClientKey = option.visibleConditionValueClientKey || originalValueKeyByCode.get(`${oldOptionCode}:${oldValueCode}`) || ''
    option.visibleConditionOptionCode = optionByKey.get(option.visibleConditionOptionClientKey)?.optionCode || option.visibleConditionOptionCode
    option.visibleConditionValueCode = valueByKey.get(option.visibleConditionValueClientKey)?.valueCode || option.visibleConditionValueCode
  })
}

function normalizeRestrictionTargets(
  restrictions: DraftRestriction[],
  originalOptionKeyByCode: Map<string, string>,
  originalValueKeyByCode: Map<string, string>,
  options: DraftOption[],
  values: DraftOptionValue[]
) {
  const optionByKey = new Map(options.map((row) => [row.clientKey || '', row]))
  const valueByKey = new Map(values.map((row) => [row.clientKey || '', row]))
  restrictions.forEach((restriction) => {
    normalizeRestrictionPair(restriction, 'target', originalOptionKeyByCode, originalValueKeyByCode, optionByKey, valueByKey)
    normalizeRestrictionPair(restriction, 'condition', originalOptionKeyByCode, originalValueKeyByCode, optionByKey, valueByKey)
  })
}

function normalizeRestrictionPair(
  restriction: DraftRestriction,
  field: 'target' | 'condition',
  originalOptionKeyByCode: Map<string, string>,
  originalValueKeyByCode: Map<string, string>,
  optionByKey: Map<string, DraftOption>,
  valueByKey: Map<string, DraftOptionValue>
) {
  const optionCodeKey = `${field}OptionCode` as 'targetOptionCode' | 'conditionOptionCode'
  const valueCodeKey = `${field}ValueCode` as 'targetValueCode' | 'conditionValueCode'
  const optionClientKey = `${field}OptionClientKey` as 'targetOptionClientKey' | 'conditionOptionClientKey'
  const valueClientKey = `${field}ValueClientKey` as 'targetValueClientKey' | 'conditionValueClientKey'
  const oldOptionCode = normalizeCode(restriction[optionCodeKey])
  const oldValueCode = normalizeCode(restriction[valueCodeKey])
  restriction[optionClientKey] = restriction[optionClientKey] || originalOptionKeyByCode.get(oldOptionCode) || ''
  restriction[valueClientKey] = restriction[valueClientKey] || originalValueKeyByCode.get(`${oldOptionCode}:${oldValueCode}`) || ''
  restriction[optionCodeKey] = optionByKey.get(restriction[optionClientKey] || '')?.optionCode || restriction[optionCodeKey]
  restriction[valueCodeKey] = valueByKey.get(restriction[valueClientKey] || '')?.valueCode || restriction[valueCodeKey]
}

function stripOptions(rows?: ProductFormulaOptionVO[]): ProductFormulaOptionVO[] {
  return (rows || []).map(({ clientKey, visibleConditionOptionClientKey, visibleConditionValueClientKey, ...row } = {} as DraftOption) => row)
}

function stripValues(rows?: ProductFormulaOptionValueVO[]): ProductFormulaOptionValueVO[] {
  return (rows || []).map(({ clientKey, optionClientKey, ...row } = {} as DraftOptionValue) => row)
}

function stripMaterials(rows?: ProductFormulaOptionMaterialVO[]): ProductFormulaOptionMaterialVO[] {
  return (rows || []).map(({ optionClientKey, valueClientKey, ...row } = {} as DraftOptionMaterial) => row)
}

function stripRestrictions(rows?: ProductFormulaRestrictionVO[]): ProductFormulaRestrictionVO[] {
  return (rows || []).map(({
    targetOptionClientKey,
    targetValueClientKey,
    conditionOptionClientKey,
    conditionValueClientKey,
    ...row
  } = {} as DraftRestriction) => row)
}

function optionIdentity(option: ProductFormulaOptionVO, index: number) {
  return option.optionId ? `option-id-${option.optionId}` : `option-draft-${normalizeCode(option.optionCode) || index + 1}`
}

function valueIdentity(value: ProductFormulaOptionValueVO, index: number) {
  return value.optionValueId ? `value-id-${value.optionValueId}` : `value-draft-${normalizeCode(value.optionCode)}-${normalizeCode(value.valueCode) || index + 1}`
}

function oldValueMapKey(value: ProductFormulaOptionValueVO) {
  return `${normalizeCode(value.optionCode)}:${normalizeCode(value.valueCode)}`
}

function uniqueKey(base: string, used: Set<string>) {
  let key = base || createDraftClientKey('draft')
  let index = 2
  while (used.has(key)) {
    key = `${base}-${index}`
    index += 1
  }
  used.add(key)
  return key
}

function nextCode(used: Set<string>, prefix: string) {
  let next = 1
  while (used.has(`${prefix}${next}`)) next += 1
  return `${prefix}${next}`
}

function normalizeCode(value?: string) {
  return String(value || '').trim().toUpperCase()
}
