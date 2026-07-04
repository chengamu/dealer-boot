import type {
  ProductFormulaMaterialVO,
  ProductFormulaUsageRuleVO,
  ProductFormulaVariableRuleVO
} from '@/api/product-capability/types'

export function normalizeFormulaMaterials(rows: ProductFormulaMaterialVO[] = []) {
  return rows.map((row) => normalizeNumericFields(row, ['fixedUsageQty', 'lineNo', 'lossRate', 'sortOrder']))
}

export function normalizeFormulaUsageRules(rows: ProductFormulaUsageRuleVO[] = []) {
  return rows.map((row) => normalizeNumericFields(row, ['fixedUsageQty', 'sortOrder']))
}

export function normalizeFormulaVariableRules(rows: ProductFormulaVariableRuleVO[] = []) {
  return rows.map((row) => normalizeNumericFields(row, ['fixedValue', 'sortOrder']))
}

function normalizeNumericFields<T extends object>(row: T, keys: string[]) {
  const next: Record<string, unknown> = { ...(row as Record<string, unknown>) }
  keys.forEach((key) => {
    const value = next[key]
    if (value === '' || value === undefined) {
      next[key] = undefined
      return
    }
    if (value === null) return
    const numeric = Number(value)
    if (Number.isFinite(numeric)) next[key] = numeric
  })
  return next as T
}
