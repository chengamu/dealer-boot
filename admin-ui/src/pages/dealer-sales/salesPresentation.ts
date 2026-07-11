export type Translate = (key: string) => string
export function documentStatusText(t: Translate, status?: string) {
  switch (status) {
    case 'SUBMITTED': return t('dealer.sales.status.SUBMITTED')
    case 'CANCELLED': return t('dealer.sales.status.CANCELLED')
    case 'COMPLETED': return t('dealer.sales.status.COMPLETED')
    default: return '-'
  }
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
  if (status === 'SHIPPED') return t('dealer.sales.shipment.SHIPPED')
  if (status === 'DELIVERED') return t('dealer.sales.shipment.DELIVERED')
  return t('dealer.sales.shipment.UNSHIPPED')
}
