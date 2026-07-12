export type Translate = (key: string) => string

export function documentStatusText(t: Translate, status?: string) {
  switch (status) {
    case 'SUBMITTED': return t('dealer.sales.status.SUBMITTED')
    case 'CANCELLED': return t('dealer.sales.status.CANCELLED')
    case 'COMPLETED': return t('dealer.sales.status.COMPLETED')
    default: return '-'
  }
}

export function sourceTypeText(t: Translate, sourceType?: string) {
  return sourceType === 'QUICK_ORDER' ? t('dealer.sales.sourceType.QUICK_ORDER') : t('dealer.sales.sourceType.QUOTE')
}

export function paymentStatusText(t: Translate, status?: string) {
  return status === 'PAID' ? t('dealer.sales.payment.PAID') : t('dealer.sales.payment.UNPAID')
}
export function productionStatusText(t: Translate, status?: string) {
  if (status === 'IN_PRODUCTION') return t('dealer.sales.production.IN_PRODUCTION')
  if (status === 'COMPLETED') return t('dealer.sales.production.COMPLETED')
  return t('dealer.sales.production.PENDING')
}
export function shipmentStatusText(t: Translate, status?: string) {
  if (status === 'PARTIALLY_SHIPPED') return t('dealer.sales.shipment.PARTIALLY_SHIPPED')
  if (status === 'SHIPPED') return t('dealer.sales.shipment.SHIPPED')
  if (status === 'DELIVERED') return t('dealer.sales.shipment.DELIVERED')
  return t('dealer.sales.shipment.UNSHIPPED')
}

export function eventTypeText(t: Translate, eventType?: string) {
  switch (eventType) {
    case 'ORDER_CREATED_FROM_QUOTE': return t('dealer.sales.event.ORDER_CREATED_FROM_QUOTE')
    case 'ORDER_CREATED_FROM_QUICK_ORDER': return t('dealer.sales.event.ORDER_CREATED_FROM_QUICK_ORDER')
    case 'CANCEL': return t('dealer.sales.event.CANCEL')
    case 'PDF_GENERATED': return t('dealer.sales.event.PDF_GENERATED')
    case 'EXCEL_EXPORTED': return t('dealer.sales.event.EXCEL_EXPORTED')
    case 'EMAIL_SENT': return t('dealer.sales.event.EMAIL_SENT')
    case 'EMAIL_FAILED': return t('dealer.sales.event.EMAIL_FAILED')
    default: return eventType || '-'
  }
}
