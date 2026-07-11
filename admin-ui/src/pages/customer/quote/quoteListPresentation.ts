import type { CustomerQuote, QuoteStatus } from '@/api/customer/quote'

type Translate = (key: string) => string

export function quoteMoney(value?: number, currency = 'USD') {
  return value == null ? '-'
    : new Intl.NumberFormat('en-US', { style: 'currency', currency: currency || 'USD' }).format(value)
}

export function quoteStatusText(status: QuoteStatus | undefined, options: Array<{ value: string; label: string }>) {
  return options.find((item) => item.value === status)?.label || status || '-'
}

export function quoteStatusType(status?: QuoteStatus) {
  return status === 'CONFIRMED' ? 'success' : status === 'VOID' ? 'info' : 'warning'
}

export function buildQuoteRowActions(
  row: CustomerQuote,
  t: Translate,
  open: (row: CustomerQuote) => void,
  exportQuote: (row: CustomerQuote) => void,
  remove: (row: CustomerQuote) => Promise<void>
) {
  return [
    { label: t('common.detail'), icon: 'View', permission: 'customer:quote:query', onClick: () => open(row) },
    { label: t('common.edit'), icon: 'Edit', permission: 'customer:quote:edit', hidden: row.status !== 'DRAFT', onClick: () => open(row) },
    { label: t('customer.quote.action.export'), icon: 'Download', permission: 'customer:quote:export', hidden: row.status !== 'CONFIRMED', onClick: () => exportQuote(row) },
    { label: t('common.delete'), icon: 'Delete', type: 'danger', permission: 'customer:quote:remove', hidden: row.status !== 'DRAFT', onClick: () => remove(row) }
  ]
}
