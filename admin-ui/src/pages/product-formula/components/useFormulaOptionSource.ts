import { computed, type Ref } from 'vue'
import { ElMessage } from 'element-plus'
import type {
  ProductFormulaMaterialVO,
  ProductFormulaOptionMaterialVO,
  ProductFormulaOptionVO,
  ProductFormulaOptionValueVO,
  ProductFormulaRestrictionVO,
  ProductFormulaUsageRuleVO,
  ProductFormulaVariableRuleVO
} from '@/api/product-capability/types'
import {
  createDraftClientKey,
  optionClientKey,
  valueClientKey,
  type DraftOption,
  type DraftOptionMaterial,
  type DraftOptionValue
} from '../utils/formulaOptionDraftIdentity'

type OptionSourceProps = {
  options: ProductFormulaOptionVO[]
  allOptionValues: ProductFormulaOptionValueVO[]
  allOptionMaterials: ProductFormulaOptionMaterialVO[]
  restrictions: ProductFormulaRestrictionVO[]
  usageRules: ProductFormulaUsageRuleVO[]
  variableRules: ProductFormulaVariableRuleVO[]
  materials: ProductFormulaMaterialVO[]
}

type UseOptionSourceOptions = {
  t: (key: string, params?: Record<string, string | number>) => string
  selectedOption: Ref<ProductFormulaOptionVO | undefined>
}

export function useFormulaOptionSource(props: OptionSourceProps, options: UseOptionSourceOptions) {
  const materialGroupOptions = computed(() => {
    const groups = new Map<string, string>()
    props.materials.forEach((row) => {
      if (!row.attributeGroupCode) return
      groups.set(row.attributeGroupCode, row.attributeGroupNameCn || row.attributeGroupCode)
    })
    return Array.from(groups.entries()).map(([value, label]) => ({ value, label }))
  })

  function sourceText(option: ProductFormulaOptionVO) {
    if (option.sourceType === 'MATERIAL_POOL') {
      const group = materialGroupOptions.value.find((item) => item.value === sourceGroupCode(option))
      return `${options.t('productCenter.formulaSetup.sourceMaterialPool')} ${group?.label || ''}`.trim()
    }
    const map: Record<string, string> = {
      PRODUCT_DICT: options.t('productCenter.formulaSetup.sourceProductDict'),
      BOOLEAN: options.t('productCenter.formulaSetup.sourceBoolean'),
      MANUAL: options.t('productCenter.formulaSetup.sourceManual')
    }
    return map[option.sourceType || ''] || options.t('productCenter.formulaSetup.sourceManual')
  }

  function visibilityText(option: ProductFormulaOptionVO) {
    if (option.visibilityMode !== 'CONDITIONAL') {
      return options.t('productCenter.formulaSetup.visibilityAlways')
    }
    const conditionOption = props.options.find((row) => row.optionCode === option.visibleConditionOptionCode)
    const conditionValue = props.allOptionValues.find((row) => row.optionCode === option.visibleConditionOptionCode && row.valueCode === option.visibleConditionValueCode)
    const optionName = option.visibleConditionOptionNameCn || conditionOption?.optionNameCn || option.visibleConditionOptionCode || '-'
    const valueName = option.visibleConditionValueNameCn || conditionValue?.valueNameCn || option.visibleConditionValueCode || '-'
    return options.t('productCenter.formulaSetup.visibleWhenSummary')
      .replace('{option}', optionName)
      .replace('{value}', valueName)
  }

  function sourceGroupCode(option?: ProductFormulaOptionVO) {
    return String(option?.sourceScope || '').replace('attributeGroupCode=', '')
  }

  function changeSourceGroup(value: string) {
    if (!options.selectedOption.value) return
    options.selectedOption.value.sourceScope = value ? `attributeGroupCode=${value}` : ''
  }

  function handleSourceTypeChange(nextSourceType?: string) {
    const option = options.selectedOption.value
    if (!option) return
    const sourceType = nextSourceType || option.sourceType || 'MANUAL'
    if (sourceType === 'BOOLEAN' && option.sourceType !== 'BOOLEAN' && hasBooleanSourceBlockingReferences(option)) {
      ElMessage.warning(options.t('productCenter.formulaSetup.sourceTypeChangeReferencedDenied'))
      return
    }
    option.sourceType = sourceType
    if (option.sourceType === 'BOOLEAN') {
      normalizeBooleanOption(option)
      return
    }
    if (option.sourceType === 'MATERIAL_POOL' && !option.sourceScope) {
      const firstGroup = materialGroupOptions.value[0]?.value || ''
      option.sourceScope = firstGroup ? `attributeGroupCode=${firstGroup}` : ''
    }
    if (option.sourceType !== 'MATERIAL_POOL') {
      option.displayMode = 'SELECT'
      return
    }
    option.displayMode ||= 'SELECT'
  }

  function normalizeBooleanOption(option: ProductFormulaOptionVO) {
    option.sourceScope = ''
    option.selectionMode = 'SWITCH'
    option.displayMode = 'SELECT'
    option.defaultValueCode = String(option.defaultValueCode || '').toUpperCase() === 'TRUE' ? 'TRUE' : 'FALSE'
    option.defaultValueNameCn = option.defaultValueCode.toLowerCase()
    const optionKey = optionClientKey(option)
    const booleanRows = [
      booleanValueRow(option, optionKey, 'FALSE', 10, option.defaultValueCode !== 'TRUE'),
      booleanValueRow(option, optionKey, 'TRUE', 20, option.defaultValueCode === 'TRUE')
    ]
    props.allOptionValues.splice(
      0,
      props.allOptionValues.length,
      ...props.allOptionValues.filter((row) => !valueBelongsToOption(row, option, optionKey)),
      ...booleanRows
    )
  }

  function booleanValueRow(option: ProductFormulaOptionVO, optionKey: string, valueCode: 'TRUE' | 'FALSE', sortOrder: number, defaultFlag: boolean) {
    const existing = props.allOptionValues.find((row) => valueBelongsToOption(row, option, optionKey) && row.valueCode === valueCode) as DraftOptionValue | undefined
    const row = existing || ({ clientKey: createDraftClientKey('value') } as DraftOptionValue)
    row.optionClientKey = optionKey
    row.optionCode = option.optionCode
    row.valueCode = valueCode
    row.valueNameCn = valueCode.toLowerCase()
    row.valueNameEn = valueCode.toLowerCase()
    row.defaultFlag = defaultFlag
    row.status ||= 'ENABLED'
    row.sortOrder = sortOrder
    return row as ProductFormulaOptionValueVO
  }

  function valueBelongsToOption(row: ProductFormulaOptionValueVO, option: ProductFormulaOptionVO, optionKey: string) {
    return (row as DraftOptionValue).optionClientKey
      ? (row as DraftOptionValue).optionClientKey === optionKey
      : row.optionCode === option.optionCode
  }

  function hasBooleanSourceBlockingReferences(option: ProductFormulaOptionVO) {
    const optionKey = optionClientKey(option)
    const optionCode = option.optionCode
    return props.allOptionMaterials.some((row) => sameOptionRef(row, optionKey, optionCode))
      || props.options.some((row) => sameOptionVisibilityRef(row, optionKey, optionCode))
      || props.restrictions.some((row) => (
        row.targetOptionCode === optionCode
        || row.conditionOptionCode === optionCode
        || expressionReferencesOption(row.conditionExpression || row.conditionText, option)
      ))
      || props.usageRules.some((row) => row.conditionOptionCode === optionCode || expressionReferencesOption(row.conditionExpression || row.conditionText, option))
      || props.variableRules.some((row) => expressionReferencesOption(row.conditionExpression || row.conditionText, option))
      || optionValuesFor(option, optionKey).some((value) => isValueReferenced(value))
  }

  function isValueReferenced(value: ProductFormulaOptionValueVO) {
    const valueKey = valueClientKey(value)
    return props.options.some((option) => (
      sameOptionValueVisibilityRef(option, value)
      || ((option as DraftOption).visibleConditionValueClientKey && (option as DraftOption).visibleConditionValueClientKey === valueKey)
    ))
      || props.restrictions.some((row) => (
        (row.targetOptionCode === value.optionCode && row.targetValueCode === value.valueCode)
        || (row.conditionOptionCode === value.optionCode && row.conditionValueCode === value.valueCode)
      ) || expressionReferencesOptionValue(row.conditionExpression || row.conditionText, value))
      || props.usageRules.some((row) => (
        row.conditionOptionCode === value.optionCode && row.conditionValueCode === value.valueCode
      ) || expressionReferencesOptionValue(row.conditionExpression || row.conditionText, value))
      || props.variableRules.some((row) => expressionReferencesOptionValue(row.conditionExpression || row.conditionText, value))
  }

  function optionValuesFor(option: ProductFormulaOptionVO, optionKey: string) {
    return props.allOptionValues.filter((row) => valueBelongsToOption(row, option, optionKey))
  }

  function sameOptionRef(row: ProductFormulaOptionMaterialVO, optionKey: string, optionCode?: string) {
    const draft = row as DraftOptionMaterial
    return draft.optionClientKey ? draft.optionClientKey === optionKey : row.optionCode === optionCode
  }

  function sameOptionVisibilityRef(row: ProductFormulaOptionVO, optionKey: string, optionCode?: string) {
    const draft = row as DraftOption
    return draft.visibleConditionOptionClientKey ? draft.visibleConditionOptionClientKey === optionKey : row.visibleConditionOptionCode === optionCode
  }

  function sameOptionValueVisibilityRef(option: ProductFormulaOptionVO, value: ProductFormulaOptionValueVO) {
    const draft = option as DraftOption
    const valueKey = valueClientKey(value)
    if (draft.visibleConditionValueClientKey) return draft.visibleConditionValueClientKey === valueKey
    return option.visibleConditionOptionCode === value.optionCode && option.visibleConditionValueCode === value.valueCode
  }

  function expressionReferencesOption(expression: unknown, option: ProductFormulaOptionVO) {
    const text = textOf(expression)
    if (!text) return false
    return [optionVariableName(option.optionCode), option.optionCode, option.optionNameCn, option.optionNameEn].some((value) => value && text.includes(value))
  }

  function expressionReferencesOptionValue(expression: unknown, value: ProductFormulaOptionValueVO) {
    const option = props.options.find((row) => row.optionCode === value.optionCode)
    if (!option || !expressionReferencesOption(expression, option)) return false
    const text = textOf(expression)
    return [value.valueCode, value.valueNameCn, value.valueNameEn].some((item) => item && text.includes(item))
  }

  function optionVariableName(optionCode?: string) {
    return optionCode === 'FABRIC' ? 'fabric' : `option_${optionCode || ''}`
  }

  function textOf(value: unknown) {
    return value === undefined || value === null ? '' : String(value)
  }

  return {
    materialGroupOptions,
    sourceText,
    visibilityText,
    sourceGroupCode,
    changeSourceGroup,
    handleSourceTypeChange
  }
}
