import { materialAttributeVariableName, normalizeDisplayExpression, optionVariableName } from './formulaExpressionDisplay'
import { validateConditionExpression } from '../utils/formulaExpression'
import {
  materialOwnerClientKey,
  optionClientKey,
  valueClientKey,
  valueOwnerClientKey
} from '../utils/formulaOptionDraftIdentity'
import type {
  ProductFormulaMaterialVO,
  ProductFormulaOptionMaterialVO,
  ProductFormulaOptionVO,
  ProductFormulaOptionValueVO,
  ProductMaterialAttributeVO
} from '@/api/product-capability/types'

export type ConditionBuildResult = {
  text: string
  expression: string
  valid: boolean
  message?: string
}

export type OptionConditionRow = {
  joiner: 'AND' | 'OR'
  optionRef: string
  operator: '=' | '!='
  valueRef: string
}

export type MaterialAttributeSourceRow = {
  optionRef: string
  attributeCode: string
}

export type SelectOption = {
  value: string
  label: string
}

export const compareOperators = ['>', '>=', '<', '<=', '=', '!=']
export const optionOperators = ['=', '!=']

export function buildOrderCondition(field: SelectOption | undefined, operator: string, value: string): ConditionBuildResult {
  const numericValue = String(value || '').trim()
  const text = field && numericValue ? `${field.label} ${operator} ${numericValue}` : ''
  return validateBuildResult(text, field && numericValue ? `${field.value} ${toExpressionOperator(operator)} ${numericValue}` : '')
}

export function buildOptionCondition(rows: OptionConditionRow[], options: ProductFormulaOptionVO[], values: ProductFormulaOptionValueVO[]): ConditionBuildResult {
  const validRows = rows
    .map((row) => ({ row, option: optionByRef(row.optionRef, options), value: valueByRef(row.valueRef, values) }))
    .filter((item) => item.option?.optionCode && item.value?.valueCode && item.row.operator)
  const text = validRows.map((row, index) => {
    const prefix = index === 0 ? '' : ` ${joinerText(row.row.joiner)} `
    return `${prefix}${optionLabel(row.option?.optionCode, options)} ${row.row.operator} ${valueLabel(row.value?.valueCode, values)}`
  }).join('')
  const expression = validRows.map((row, index) => {
    const prefix = index === 0 ? '' : ` ${row.row.joiner === 'AND' ? '&&' : '||'} `
    return `${prefix}${optionVariableName(row.option?.optionCode)} ${toExpressionOperator(row.row.operator)} ${quoted(row.value?.valueCode || '')}`
  }).join('')
  return validateBuildResult(text, expression)
}

export function buildMaterialAttributeCondition(
  sources: MaterialAttributeSourceRow[],
  operator: string,
  value: string,
  options: ProductFormulaOptionVO[],
  attributes: ProductMaterialAttributeVO[]
): ConditionBuildResult {
  const validSources = sources
    .map((row) => ({ row, option: optionByRef(row.optionRef, options) }))
    .filter((item) => item.option?.optionCode && item.row.attributeCode)
  const numericValue = String(value || '').trim()
  const textLeft = validSources.map(({ row, option }) => `${optionLabel(option?.optionCode, options)}.${attributeLabel(row.attributeCode, attributes)}`).join(' + ')
  const expressionLeft = validSources.map(({ row, option }) => materialAttributeVariableName(option?.optionCode, row.attributeCode)).join(' + ')
  const text = textLeft && numericValue ? `${textLeft} ${operator} ${numericValue}` : ''
  const expression = expressionLeft && numericValue ? `${expressionLeft} ${toExpressionOperator(operator)} ${numericValue}` : ''
  return validateBuildResult(text, expression)
}

export function buildAdvancedCondition(text: string, options: ProductFormulaOptionVO[], values: ProductFormulaOptionValueVO[], materials: ProductFormulaMaterialVO[]): ConditionBuildResult {
  const displayText = String(text || '').trim()
  return validateBuildResult(displayText, normalizeDisplayExpression(displayText, options, values, materials))
}

export function optionSelectOptions(options: ProductFormulaOptionVO[]): SelectOption[] {
  return options.map((option) => ({
    value: optionRef(option),
    label: option.optionNameCn || option.optionNameEn || option.optionCode || '-'
  })).filter((option) => option.value)
}

export function valueSelectOptions(optionRefValue: string, options: ProductFormulaOptionVO[], values: ProductFormulaOptionValueVO[]): SelectOption[] {
  const option = optionByRef(optionRefValue, options)
  const ownerKey = optionClientKey(option)
  const optionCode = option?.optionCode
  return values
    .filter((value) => {
      const valueOwnerKey = valueOwnerClientKey(value)
      return valueOwnerKey ? valueOwnerKey === ownerKey : value.optionCode === optionCode
    })
    .sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
    .map((value) => ({ value: valueRef(value), label: value.valueNameCn || value.valueNameEn || value.valueCode || '-' }))
    .filter((option) => option.value)
}

export function materialAttributeOptions(optionRefValue: string, options: ProductFormulaOptionVO[], optionMaterials: ProductFormulaOptionMaterialVO[], materials: ProductFormulaMaterialVO[]): SelectOption[] {
  const option = optionByRef(optionRefValue, options)
  const ownerKey = optionClientKey(option)
  const optionCode = option?.optionCode || optionRefValue
  const materialCodes = new Set(optionMaterials
    .filter((row) => {
      if (!optionRefValue) return true
      const rowOwnerKey = materialOwnerClientKey(row)
      return rowOwnerKey ? rowOwnerKey === ownerKey : row.optionCode === optionCode
    })
    .map((row) => row.materialCode)
    .filter(Boolean))
  const attributes = materials
    .filter((material) => material.materialCode && materialCodes.has(material.materialCode))
    .flatMap((material) => material.attributeList || [])
  const map = new Map<string, SelectOption>()
  attributes.forEach((attribute) => {
    if (!attribute.attributeCode || map.has(attribute.attributeCode)) return
    map.set(attribute.attributeCode, {
      value: attribute.attributeCode,
      label: attribute.attributeNameCn || attribute.attributeNameEn || attribute.attributeCode
    })
  })
  return Array.from(map.values())
}

export function allMaterialAttributes(optionMaterials: ProductFormulaOptionMaterialVO[], materials: ProductFormulaMaterialVO[]) {
  return materialAttributeOptions('', [], optionMaterials.map((row) => ({ ...row, optionCode: '' })), materials)
}

function validateBuildResult(text: string, expression: string): ConditionBuildResult {
  const result = validateConditionExpression(expression)
  return { text, expression: result.expression || expression, valid: result.valid, message: result.message }
}

function optionLabel(optionCode: string | undefined, options: ProductFormulaOptionVO[]) {
  const option = options.find((row) => row.optionCode === optionCode)
  return option?.optionNameCn || option?.optionNameEn || optionCode
}

function valueLabel(valueCode: string | undefined, values: ProductFormulaOptionValueVO[]) {
  const value = values.find((row) => row.valueCode === valueCode)
  return value?.valueNameCn || value?.valueNameEn || valueCode
}

function optionByRef(ref: string | undefined, options: ProductFormulaOptionVO[]) {
  return options.find((option) => optionRef(option) === ref || option.optionCode === ref)
}

function valueByRef(ref: string | undefined, values: ProductFormulaOptionValueVO[]) {
  return values.find((value) => valueRef(value) === ref || value.valueCode === ref)
}

function optionRef(option?: ProductFormulaOptionVO) {
  return optionClientKey(option) || String(option?.optionCode || '')
}

function valueRef(value?: ProductFormulaOptionValueVO) {
  return valueClientKey(value) || String(value?.valueCode || '')
}

function attributeLabel(attributeCode: string, attributes: ProductMaterialAttributeVO[]) {
  const attribute = attributes.find((row) => row.attributeCode === attributeCode)
  return attribute?.attributeNameCn || attribute?.attributeNameEn || attributeCode
}

function toExpressionOperator(operator: string) {
  return operator === '=' ? '==' : operator
}

function quoted(value: string) {
  return `"${String(value || '').replace(/"/g, '\\"')}"`
}

function joinerText(joiner: 'AND' | 'OR') {
  return joiner === 'AND' ? '并且' : '或者'
}
