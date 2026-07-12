import type { ComposerTranslation } from 'vue-i18n'
import { formatMoney, formatRatioAsPercent } from '@/utils/businessNumber'

export function formatDiscountRate(value?: number | string | null) {
  if (value === null || value === undefined || value === '') return '-'
  return formatRatioAsPercent(value, 4)
}

export function formatCreditLimit(value?: number | string | null) {
  if (value === null || value === undefined || value === '') return '-'
  return formatMoney(value)
}

export function merchantStatusText(status: string | undefined, t: ComposerTranslation) {
  if (status === 'ENABLED' || status === '1') return t('common.enabled')
  if (status === 'DISABLED' || status === '0') return t('common.disabled')
  return '-'
}
