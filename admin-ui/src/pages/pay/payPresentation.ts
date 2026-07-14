import type {
  BankTransferStatus,
  BusinessOrigin,
  CreditAccountStatus,
  PayOrderStatus,
  ReceivableStatus,
  ReconciliationSeverity,
  ReconciliationStatus
} from '@/api/pay'
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

export function businessOriginText(t: Translate, origin?: BusinessOrigin) {
  if (origin === 'INTERNAL') return t('pay.businessOrigin.INTERNAL')
  if (origin === 'MERCHANT') return t('pay.businessOrigin.MERCHANT')
  return origin || '-'
}

export function creditAccountStatusText(t: Translate, status?: CreditAccountStatus) {
  if (!status) return '-'
  const keys: Record<string, string> = {
    ACTIVE: 'pay.credit.status.ACTIVE',
    FROZEN: 'pay.credit.status.FROZEN'
  }
  return keys[status] ? t(keys[status]) : status
}

export function creditTransactionTypeText(t: Translate, type?: string) {
  if (!type) return '-'
  const keys: Record<string, string> = {
    OCCUPY: 'pay.credit.transactionType.OCCUPY',
    REPAY: 'pay.credit.transactionType.REPAY',
    RELEASE: 'pay.credit.transactionType.RELEASE',
    ADJUST: 'pay.credit.transactionType.ADJUST',
    FREEZE: 'pay.credit.transactionType.FREEZE',
    UNFREEZE: 'pay.credit.transactionType.UNFREEZE'
  }
  return keys[type] ? t(keys[type]) : type
}

export function receivableStatusText(t: Translate, status?: ReceivableStatus) {
  const keys: Record<string, string> = {
    OPEN: 'pay.receivable.status.OPEN',
    PARTIAL: 'pay.receivable.status.PARTIAL',
    SETTLED: 'pay.receivable.status.SETTLED',
    OVERDUE: 'pay.receivable.status.OVERDUE'
  }
  if (!status) return '-'
  return t(keys[status] || 'pay.receivable.status.OPEN')
}

export function reconciliationSeverityText(t: Translate, severity?: ReconciliationSeverity) {
  const keys: Record<string, string> = {
    CRITICAL: 'pay.reconciliation.severity.CRITICAL',
    WARNING: 'pay.reconciliation.severity.WARNING'
  }
  if (!severity) return '-'
  return t(keys[severity] || 'pay.reconciliation.severity.WARNING')
}

export function reconciliationStatusText(t: Translate, status?: ReconciliationStatus) {
  const keys: Record<string, string> = {
    OPEN: 'pay.reconciliation.status.OPEN',
    RESOLVED: 'pay.reconciliation.status.RESOLVED',
    IGNORED: 'pay.reconciliation.status.IGNORED'
  }
  if (!status) return '-'
  return t(keys[status] || 'pay.reconciliation.status.OPEN')
}

export function reconciliationSeverityType(severity?: ReconciliationSeverity) {
  if (severity === 'CRITICAL') return 'danger'
  if (severity === 'WARNING') return 'warning'
  return 'info'
}

export function bankStatusType(status?: BankTransferStatus) {
  if (status === 'SUCCESS') return 'success'
  if (status === 'REJECTED' || status === 'CLOSED') return 'danger'
  return 'warning'
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
