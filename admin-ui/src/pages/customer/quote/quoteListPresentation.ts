import type { CustomerQuote, QuoteStatus } from '@/api/customer/quote'

type Translate = (key: string) => string
interface QuoteActions {
  open: (row: CustomerQuote) => void
  copy: (row: CustomerQuote) => Promise<void>
  export: (row: CustomerQuote) => void
  pdf: (row: CustomerQuote, print?: boolean) => Promise<void>
  email: (row: CustomerQuote) => void
  convert: (row: CustomerQuote) => void
  viewOrder: (row: CustomerQuote) => void
  void: (row: CustomerQuote) => Promise<void>
  remove: (row: CustomerQuote) => Promise<void>
}

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
  actions: QuoteActions
) {
  const confirmed = row.status === 'CONFIRMED'
  const converted = Boolean(row.salesDocumentId)
  return [
    { label: row.status === 'DRAFT' ? t('common.edit') : t('common.detail'), icon: row.status === 'DRAFT' ? 'Edit' : 'View',
      permission: row.status === 'DRAFT' ? 'customer:quote:edit' : 'customer:quote:query', primary: true, onClick: () => actions.open(row) },
    { label: t('customer.quote.action.copy'), icon: 'CopyDocument', permission: 'customer:quote:add', onClick: () => actions.copy(row) },
    { label: t('customer.quote.action.export'), icon: 'Download', permission: 'customer:quote:export', hidden: !confirmed, onClick: () => actions.export(row) },
    { label: t('customer.quote.action.pdf'), icon: 'Document', permission: 'customer:quote:document', hidden: !confirmed, onClick: () => actions.pdf(row) },
    { label: t('customer.quote.action.print'), icon: 'Printer', permission: 'customer:quote:document', hidden: !confirmed, onClick: () => actions.pdf(row, true) },
    { label: t('customer.quote.action.email'), icon: 'Message', permission: 'customer:quote:email', hidden: !confirmed, onClick: () => actions.email(row) },
    { label: t('customer.quote.action.convert'), icon: 'Promotion', permission: 'customer:quote:convert', hidden: !confirmed || converted, onClick: () => actions.convert(row) },
    { label: t('customer.quote.action.viewOrder'), icon: 'Tickets', permission: 'dealer:sales:query', hidden: !converted, onClick: () => actions.viewOrder(row) },
    { label: t('customer.quote.action.void'), icon: 'CircleClose', type: 'danger', permission: 'customer:quote:edit', hidden: !confirmed || converted, onClick: () => actions.void(row) },
    { label: t('common.delete'), icon: 'Delete', type: 'danger', permission: 'customer:quote:remove', hidden: row.status !== 'DRAFT', onClick: () => actions.remove(row) }
  ]
}
