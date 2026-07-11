import { PRODUCT_STATUS_DISABLED, PRODUCT_STATUS_ENABLED } from '@/constants/productStatus'
import type { ProductFormulaOptionVO, ProductFormulaOptionValueVO } from '@/api/product-capability/types'
import { formatUnitPrice } from '@/utils/businessNumber'

export const PRICE_STATUS = {
  NOT_READY: 'NOT_READY',
  READY: 'READY',
  WARNING: 'WARNING'
} as const

type TranslateFn = (key: string) => string

export function enabledStatusOptions(t: TranslateFn) {
  return [
    { label: t('productCenter.status.enabled'), value: PRODUCT_STATUS_ENABLED },
    { label: t('productCenter.status.disabled'), value: PRODUCT_STATUS_DISABLED }
  ]
}

export function priceStatusOptions(t: TranslateFn) {
  return [
    { label: t('productCenter.pricing.priceStatusNotReady'), value: PRICE_STATUS.NOT_READY },
    { label: t('productCenter.pricing.priceStatusReady'), value: PRICE_STATUS.READY },
    { label: t('productCenter.pricing.priceStatusWarning'), value: PRICE_STATUS.WARNING }
  ]
}

export function priceStatusText(status: string | undefined, t: TranslateFn) {
  if (status === PRICE_STATUS.READY) return t('productCenter.pricing.priceStatusReady')
  if (status === PRICE_STATUS.WARNING) return t('productCenter.pricing.priceStatusWarning')
  return t('productCenter.pricing.priceStatusNotReady')
}

export function priceStatusTagType(status?: string) {
  if (status === PRICE_STATUS.READY) return 'success'
  if (status === PRICE_STATUS.WARNING) return 'warning'
  return 'info'
}

export function enabledStatusText(status: string | undefined, t: TranslateFn) {
  return status === PRODUCT_STATUS_ENABLED ? t('productCenter.status.enabled') : t('productCenter.status.disabled')
}

export function enabledStatusTagType(status?: string) {
  return status === PRODUCT_STATUS_ENABLED ? 'success' : 'info'
}

export function money(value?: number | string) {
  return formatUnitPrice(value, '0.00')
}

export const DEFAULT_MATERIAL_PRICE_FORMULA = 'unitPrice * usageQty'
export const DEFAULT_FABRIC_AREA_FORMULA = 'MAX(drop * 2.54 * width * 2.54 / 10000, 1)'

export function materialPriceFormulaForUnitPrice(unitPrice?: number | string, attributeGroupCode?: string) {
  return attributeGroupCode === 'FABRIC'
    ? `${money(unitPrice)} * ${DEFAULT_FABRIC_AREA_FORMULA}`
    : `${money(unitPrice)} * usageQty`
}

export function displayMaterialPriceFormula(formula?: string, unitPrice?: number | string) {
  return String(formula || DEFAULT_MATERIAL_PRICE_FORMULA).replace(/\bunitPrice\b/g, money(unitPrice))
}

export function displayPriceExpression(
  expression: string | undefined,
  t: TranslateFn,
  options: ProductFormulaOptionVO[] = [],
  optionValues: ProductFormulaOptionValueVO[] = []
) {
  let text = String(expression || '').trim()
  if (!text) return '-'
  text = replaceOptionClauses(text, options, optionValues)
  const variableLabels: Record<string, string> = {
    width: t('productCenter.pricing.variableWidth'),
    drop: t('productCenter.pricing.variableDrop'),
    widthCm: t('productCenter.pricing.variableWidthCm'),
    dropCm: t('productCenter.pricing.variableDropCm'),
    areaM2: t('productCenter.pricing.variableAreaM2'),
    areaSqft: t('productCenter.pricing.variableAreaSqft'),
    usageQty: t('productCenter.pricing.variableUsageQty')
  }
  Object.entries(variableLabels).forEach(([key, label]) => {
    text = text.replace(new RegExp(`\\b${key}\\b`, 'g'), label)
  })
  return text
    .replace(/\s*&&\s*/g, ` ${t('productCenter.pricing.and')} `)
    .replace(/\s*\|\|\s*/g, ` ${t('productCenter.pricing.or')} `)
    .replace(/\s*==\s*/g, ' = ')
    .replace(/\s*!=\s*/g, ' ≠ ')
}

function replaceOptionClauses(
  expression: string,
  options: ProductFormulaOptionVO[],
  optionValues: ProductFormulaOptionValueVO[]
) {
  return expression.replace(/option_([A-Za-z0-9_]+)\s*(==|!=)\s*(['"])(.*?)\3/g, (_match, optionToken, operator, _quote, valueToken) => {
    const option = options.find((item) => optionMatchesToken(item, optionToken))
    const value = optionValues.find((item) => valueMatchesToken(item, option, valueToken))
    const optionName = option?.optionNameCn || option?.optionNameEn || optionToken
    const valueName = value?.valueNameCn || value?.valueNameEn || valueToken
    return `${optionName} ${operator === '!=' ? '!=' : '=='} "${valueName}"`
  })
}

function optionMatchesToken(option: ProductFormulaOptionVO, token: string) {
  return option.optionRefKey === token || option.optionCode === token
}

function valueMatchesToken(value: ProductFormulaOptionValueVO, option: ProductFormulaOptionVO | undefined, token: string) {
  const sameValue = value.valueRefKey === token || value.valueCode === token
  if (!sameValue || !option) return sameValue
  if (value.optionRefKey && option.optionRefKey) return value.optionRefKey === option.optionRefKey
  return value.optionCode === option.optionCode
}
