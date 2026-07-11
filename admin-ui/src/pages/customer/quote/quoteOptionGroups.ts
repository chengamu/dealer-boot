import type { CustomerQuoteCatalogSetup } from '@/api/customer/quote'
import type { ProductFormulaOptionVO, ProductFormulaOptionValueVO } from '@/api/product-capability/types'

export interface QuoteOptionGroup {
  root: ProductFormulaOptionVO
  options: ProductFormulaOptionVO[]
}

export function activeQuoteOptions(setup: CustomerQuoteCatalogSetup, selections: Record<string, string>) {
  return (setup.formulaOptions || [])
    .filter((option) => option.status === 'ENABLED' && option.businessVisibleFlag !== false)
    .filter((option) => option.visibilityMode !== 'CONDITIONAL'
      || splitCodes(selections[option.visibleConditionOptionCode || '']).includes(option.visibleConditionValueCode || ''))
    .sort((left, right) => (left.sortOrder ?? 999999) - (right.sortOrder ?? 999999))
}

export function groupQuoteOptions(options: ProductFormulaOptionVO[]): QuoteOptionGroup[] {
  const optionMap = new Map(options.map((option) => [option.optionCode, option]))
  const groups = new Map<string, QuoteOptionGroup>()
  options.forEach((option) => {
    const root = findRoot(option, optionMap)
    const key = root.optionCode || option.optionCode || ''
    if (!groups.has(key)) groups.set(key, { root, options: [] })
    groups.get(key)?.options.push(option)
  })
  return [...groups.values()]
}

export function valuesForOption(setup: CustomerQuoteCatalogSetup, optionCode?: string): ProductFormulaOptionValueVO[] {
  return (setup.formulaOptionValues || [])
    .filter((value) => value.status === 'ENABLED' && value.optionCode === optionCode)
    .sort((left, right) => (left.sortOrder ?? 999999) - (right.sortOrder ?? 999999))
}

export function applyQuoteDefaults(setup: CustomerQuoteCatalogSetup, selections: Record<string, string>) {
  activeQuoteOptions(setup, selections).forEach((option) => {
    const code = option.optionCode || ''
    if (!code || selections[code]) return
    const value = option.defaultValueCode || valuesForOption(setup, code).find((row) => row.defaultFlag)?.valueCode
    if (value) selections[code] = value
  })
}

function findRoot(option: ProductFormulaOptionVO, map: Map<string | undefined, ProductFormulaOptionVO>) {
  let current = option
  const visited = new Set<string>()
  while (current.visibilityMode === 'CONDITIONAL' && current.visibleConditionOptionCode && !visited.has(current.visibleConditionOptionCode)) {
    visited.add(current.visibleConditionOptionCode)
    current = map.get(current.visibleConditionOptionCode) || current
    if (current.optionCode === option.optionCode) break
  }
  return current
}

function splitCodes(value?: string) {
  return String(value || '').split(',').map((code) => code.trim()).filter(Boolean)
}
