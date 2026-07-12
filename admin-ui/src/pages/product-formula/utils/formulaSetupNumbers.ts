import type {
  ProductFormulaMaterialVO,
  ProductFormulaUsageRuleVO,
  ProductFormulaVariableRuleVO
} from '@/api/product-capability/types'
import { canonicalDecimal } from '@/utils/businessNumber'

export function normalizeFormulaMaterials(rows: ProductFormulaMaterialVO[] = []) {
  return rows.map((row) => normalizeFields(row, ['fixedUsageQty', 'lossRate'], ['lineNo', 'sortOrder']))
}

export function normalizeFormulaUsageRules(rows: ProductFormulaUsageRuleVO[] = []) {
  return rows.map((row) => normalizeFields(row, ['fixedUsageQty', 'minUsageQty', 'maxUsageQty', 'lossRate'], ['sortOrder']))
}

export function normalizeFormulaVariableRules(rows: ProductFormulaVariableRuleVO[] = []) {
  return rows.map((row) => normalizeFields(row, ['fixedValue'], ['sortOrder']))
}

function normalizeFields<T extends object>(row: T, decimalKeys: string[], integerKeys: string[]) {
  const next: Record<string, unknown> = { ...(row as Record<string, unknown>) }
  decimalKeys.forEach((key) => {
    const value = next[key]
    if (value === '' || value === undefined) {
      next[key] = undefined
      return
    }
    if (value === null) return
    next[key] = canonicalDecimal(value as string | number) ?? value
  })
  integerKeys.forEach((key) => {
    const value = Number(next[key])
    if (Number.isInteger(value)) next[key] = value
  })
  return next as T
}
