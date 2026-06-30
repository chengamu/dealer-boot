import type { ProductFormulaUsageRuleVO } from '@/api/product-capability/types'

export type FormulaTarget = 'length' | 'width' | 'height' | 'weight' | 'usage'
export type ExpressionTarget = FormulaTarget | 'condition'

export type FormulaField = {
  target: FormulaTarget
  labelKey: string
  valueKey: keyof ProductFormulaUsageRuleVO
  textKey: keyof ProductFormulaUsageRuleVO
}
