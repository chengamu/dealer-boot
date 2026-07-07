import type { ComposerTranslation } from 'vue-i18n'

export function formatDiscountRate(value?: number | string | null) {
  if (value === null || value === undefined || value === '') return '-'
  const numeric = Number(value)
  return Number.isFinite(numeric) ? numeric.toFixed(2) : '-'
}

export function formatCreditLimit(value?: number | string | null) {
  if (value === null || value === undefined || value === '') return '-'
  const numeric = Number(value)
  return Number.isFinite(numeric) ? numeric.toFixed(2) : '-'
}

export function merchantStatusText(status: string | undefined, t: ComposerTranslation) {
  if (status === 'ENABLED' || status === '1') return t('common.enabled')
  if (status === 'DISABLED' || status === '0') return t('common.disabled')
  return '-'
}
