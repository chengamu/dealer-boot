import type { ProductFormulaMaterialVO } from '@/api/product-capability/types'

export interface FormulaReviewPriceSnapshotRow {
  materialCode?: string
  unitPrice?: number
  salesPrice?: number
}

export const FORMULA_REVIEW_GROUP_ORDER = ['FABRIC', 'ALUMINUM', 'SYSTEM', 'ACCESSORY', 'PART_PACK', 'PACKAGING']

export function formulaReviewGroupKey(row: ProductFormulaMaterialVO) {
  return row.attributeGroupCode || row.attributeGroupNameCn || '-'
}

export function formulaReviewGroupRank(code?: string) {
  const index = FORMULA_REVIEW_GROUP_ORDER.indexOf(code || '')
  return index >= 0 ? index : FORMULA_REVIEW_GROUP_ORDER.length + 1
}

export function formatFormulaReviewMoney(value?: number) {
  return value == null ? '-' : Number(value).toFixed(2)
}

export function formatFormulaReviewNumber(value?: number) {
  return value == null ? '-' : Number(value).toFixed(2)
}

export function formatFormulaReviewPercent(value?: number) {
  if (value == null) return '-'
  const numberValue = Math.abs(value) <= 1 ? value * 100 : value
  return `${numberValue.toFixed(2)}%`
}
