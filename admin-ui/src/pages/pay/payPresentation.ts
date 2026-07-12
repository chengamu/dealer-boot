import type { PayOrderStatus } from '@/api/pay'
import type { DecimalValue } from '@/types/api'
import { formatCurrency, minorUnitsToDecimal } from '@/utils/businessNumber'

type Translate = (key: string, params?: Record<string, unknown>) => string

export function minorMoney(value?: string | number, currency = 'USD') {
  return formatCurrency(minorUnitsToDecimal(value ?? '0', 2) ?? '0', currency)
}

export function money(value?: DecimalValue | number, currency = 'USD') {
  return formatCurrency(value ?? '0', currency)
}

export function statusText(t: Translate, status?: PayOrderStatus) {
  const keys: Record<number, string> = {
    0: 'pay.status.0', 5: 'pay.status.5', 10: 'pay.status.10', 20: 'pay.status.20'
  }
  return t(keys[status ?? 0] || 'pay.status.0')
}

export function bankStatusText(t: Translate, status?: string) {
  const keys: Record<string, string> = {
    DRAFT: 'pay.bank.status.DRAFT', PENDING_REVIEW: 'pay.bank.status.PENDING_REVIEW',
    REJECTED: 'pay.bank.status.REJECTED', SUCCESS: 'pay.bank.status.SUCCESS', CLOSED: 'pay.bank.status.CLOSED'
  }
  return status ? t(keys[status] || 'pay.bank.status.DRAFT') : '-'
}

export function statusType(status?: PayOrderStatus) {
  if (status === 10) return 'success'
  if (status === 20) return 'info'
  return 'warning'
}

export function methodText(t: Translate, method?: string) {
  if (!method) return '-'
  const normalized = method.toLowerCase()
  if (normalized.includes('paypal')) return t('pay.method.paypal')
  if (normalized.includes('bank')) return t('pay.method.bank')
  if (normalized.includes('credit')) return t('pay.method.credit')
  return method
}
