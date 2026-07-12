import type { ProductFormulaMaterialVO, ProductFormulaUsageRuleVO } from '@/api/product-capability/types'
import { PRODUCT_STATUS_DISABLED } from '@/constants/productStatus'
import { formatUsageNumber } from './formulaExpression'
import { canonicalDecimal, formatQuantity, formatRate, formatUnitPrice } from '@/utils/businessNumber'

export interface FormulaReviewPriceSnapshotRow {
  materialCode?: string
  unitPrice?: string | number | null
  salesPrice?: string | number | null
}

export const FORMULA_REVIEW_GROUP_ORDER = ['FABRIC', 'ALUMINUM', 'SYSTEM', 'ACCESSORY', 'PART_PACK', 'PACKAGING']

export function formulaReviewGroupKey(row: ProductFormulaMaterialVO) {
  return row.attributeGroupCode || row.attributeGroupNameCn || '-'
}

export function formulaReviewGroupRank(code?: string) {
  const index = FORMULA_REVIEW_GROUP_ORDER.indexOf(code || '')
  return index >= 0 ? index : FORMULA_REVIEW_GROUP_ORDER.length + 1
}

export function formatFormulaReviewMoney(value?: string | number | null) {
  return formatUnitPrice(value)
}

export function formatFormulaReviewNumber(value?: string | number | null) {
  return formatQuantity(value)
}

export function formatFormulaReviewPercent(value?: string | number | null) {
  if (value == null) return '-'
  return `${formatRate(value)}%`
}

export function formulaReviewUsageSummaryLines(row: ProductFormulaMaterialVO, usageRules: ProductFormulaUsageRuleVO[], t: (key: string) => string) {
  return formulaReviewUsageSummary(row, usageRules, t).split('\n').map((line) => line.trim()).filter(Boolean)
}

export function formulaReviewUsageSummary(row: ProductFormulaMaterialVO, usageRules: ProductFormulaUsageRuleVO[], t: (key: string) => string) {
  const activeRules = usageRulesFor(row, usageRules).filter((rule) => rule.status !== PRODUCT_STATUS_DISABLED)
  if (activeRules.length > 0) {
    const formulaRules = activeRules.filter((rule) => rule.usageMode === 'FORMULA')
    const fixedRule = activeRules.find((rule) => rule.usageMode === 'FIXED')
    if (formulaRules.length > 0) return formulaRules.slice().sort(compareFormulaUsageRuleSort).map((rule) => usageRuleSummary(rule, t)).join('\n')
    if (fixedRule) return `${textOf(t('productCenter.formulaSetup.usageFixedShort'))} ${formatUsageNumber(fixedRule.fixedUsageQty)}`
  }
  if (row.usageMode === 'FIXED' && row.fixedUsageQty !== undefined && row.fixedUsageQty !== null) {
    return `${textOf(t('productCenter.formulaSetup.usageFixedShort'))} ${formatUsageNumber(row.fixedUsageQty)}`
  }
  if (row.usageMode === 'FORMULA' && hasAnyUsageFormula(row)) return usageRuleSummary(row, t)
  return textOf(t('productCenter.formulaSetup.usageNotSet'))
}

export function formulaReviewIsUsageConditionLine(line: string) {
  return line.endsWith('：') || line.endsWith(':')
}

function usageRulesFor(row: ProductFormulaMaterialVO, usageRules: ProductFormulaUsageRuleVO[]) {
  return usageRules.filter((rule) => rule.formulaMaterialId === row.formulaMaterialId || rule.materialCode === row.materialCode)
}

function usageRuleSummary(rule: ProductFormulaUsageRuleVO | ProductFormulaMaterialVO, t: (key: string) => string) {
  const condition = usageConditionText(rule, t)
  const formulas = usageFormulaParts(rule, t)
  return formulas.length ? `${condition}：\n${formulas.join('\n')}` : condition
}

function usageConditionText(rule: ProductFormulaUsageRuleVO | ProductFormulaMaterialVO, t: (key: string) => string) {
  if ('defaultRuleFlag' in rule && rule.defaultRuleFlag) return textOf(t('productCenter.formulaSetup.defaultUsageRule'))
  if ('conditionText' in rule && rule.conditionText) return textOf(rule.conditionText)
  if ('conditionOptionNameCn' in rule && (rule.conditionOptionNameCn || rule.conditionOptionCode)) {
    return `${rule.conditionOptionNameCn || rule.conditionOptionCode} = ${rule.conditionValueNameCn || rule.conditionValueCode || '-'}`
  }
  if ('conditionExpression' in rule && rule.conditionExpression) return textOf(rule.conditionExpression)
  return textOf(t('productCenter.formulaSetup.defaultUsageRule'))
}

function usageFormulaParts(rule: ProductFormulaUsageRuleVO | ProductFormulaMaterialVO, t: (key: string) => string) {
  const parts: string[] = []
  appendUsageFormulaPart(parts, textOf(t('productCenter.formulaSetup.lengthFormula')), summaryFormulaValue(rule.lengthFormulaText || rule.lengthFormula))
  appendUsageFormulaPart(parts, textOf(t('productCenter.formulaSetup.widthFormula')), summaryFormulaValue(rule.widthFormulaText || rule.widthFormula))
  appendUsageFormulaPart(parts, textOf(t('productCenter.formulaSetup.heightFormula')), summaryFormulaValue(rule.heightFormulaText || rule.heightFormula))
  appendUsageFormulaPart(parts, textOf(t('productCenter.formulaSetup.weightFormula')), summaryFormulaValue(rule.weightFormulaText || rule.weightFormula))
  const quantityFormula = summaryFormulaValue(rule.usageFormulaText || rule.usageFormula)
  if (quantityFormula && !(parts.length > 0 && isDefaultQuantityFormula(quantityFormula))) {
    appendUsageFormulaPart(parts, textOf(t('productCenter.formulaSetup.quantityFormula')), quantityFormula)
  }
  if (!parts.length && rule.fixedUsageQty !== undefined && rule.fixedUsageQty !== null) {
    parts.push(`${textOf(t('productCenter.formulaSetup.usageFixedShort'))} ${formatUsageNumber(rule.fixedUsageQty)}`)
  }
  return parts
}

function hasAnyUsageFormula(row: ProductFormulaUsageRuleVO | ProductFormulaMaterialVO) {
  return Boolean(row.lengthFormula || row.widthFormula || row.heightFormula || row.weightFormula || row.usageFormula)
}

function appendUsageFormulaPart(parts: string[], label: string, value?: string) {
  if (value) parts.push(`${label} ${value}`)
}

function summaryFormulaValue(value: unknown) {
  const text = textOf(value).trim()
  return canonicalDecimal(text) !== null ? formatQuantity(text) : text
}

function isDefaultQuantityFormula(value: string) {
  return canonicalDecimal(value) === '1'
}

function compareFormulaUsageRuleSort(left: ProductFormulaUsageRuleVO, right: ProductFormulaUsageRuleVO) {
  return Number(left.sortOrder || 0) - Number(right.sortOrder || 0)
}

function textOf(value: unknown) {
  return value === undefined || value === null ? '' : String(value)
}
