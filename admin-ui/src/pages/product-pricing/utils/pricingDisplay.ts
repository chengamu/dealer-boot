import { PRODUCT_STATUS_DISABLED, PRODUCT_STATUS_ENABLED } from '@/constants/productStatus'

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
  const numeric = Number(value)
  return Number.isFinite(numeric) ? numeric.toFixed(2) : '0.00'
}

export const DEFAULT_FABRIC_PRICE_FORMULA = 'unitPrice * MAX(drop * 2.54 * width * 2.54 / 10000, 1)'
