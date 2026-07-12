import type { ComposerTranslation } from 'vue-i18n'
import type { PackageStatus, ProductionStatus, ReceiptStatus, ShipmentStatus } from '@/api/dealer-fulfillment'

type Status = ProductionStatus | ShipmentStatus | PackageStatus | ReceiptStatus | string | undefined

const statusKeys: Record<string, string> = {
  'production.PENDING': 'dealer.fulfillment.production.PENDING',
  'production.IN_PRODUCTION': 'dealer.fulfillment.production.IN_PRODUCTION',
  'production.COMPLETED': 'dealer.fulfillment.production.COMPLETED',
  'shipment.UNSHIPPED': 'dealer.fulfillment.shipment.UNSHIPPED',
  'shipment.PARTIALLY_SHIPPED': 'dealer.fulfillment.shipment.PARTIALLY_SHIPPED',
  'shipment.SHIPPED': 'dealer.fulfillment.shipment.SHIPPED',
  'shipment.DELIVERED': 'dealer.fulfillment.shipment.DELIVERED',
  'package.DRAFT': 'dealer.fulfillment.package.DRAFT',
  'package.SHIPPED': 'dealer.fulfillment.package.SHIPPED',
  'package.IN_TRANSIT': 'dealer.fulfillment.package.IN_TRANSIT',
  'package.DELIVERED': 'dealer.fulfillment.package.DELIVERED',
  'package.EXCEPTION': 'dealer.fulfillment.package.EXCEPTION',
  'package.CANCELLED': 'dealer.fulfillment.package.CANCELLED',
  'receipt.PENDING': 'dealer.fulfillment.receipt.PENDING',
  'receipt.CONFIRMED': 'dealer.fulfillment.receipt.CONFIRMED'
}

const sourceKeys: Record<string, string> = {
  QUOTE: 'dealer.fulfillment.source.QUOTE',
  QUICK_ORDER: 'dealer.fulfillment.source.QUICK_ORDER'
}

export function statusText(t: ComposerTranslation, group: string, status?: Status) {
  return status ? t(statusKeys[`${group}.${status}`] || 'dealer.fulfillment.statusUnknown') : '-'
}

export function statusType(status?: Status) {
  if (status === 'COMPLETED' || status === 'DELIVERED' || status === 'CONFIRMED' || status === 'SHIPPED') return 'success'
  if (status === 'IN_PRODUCTION' || status === 'IN_TRANSIT' || status === 'PARTIALLY_SHIPPED') return 'primary'
  if (status === 'EXCEPTION') return 'danger'
  if (status === 'CANCELLED') return 'info'
  return 'warning'
}

export function sourceText(t: ComposerTranslation, source?: string) {
  return source ? t(sourceKeys[source] || 'dealer.fulfillment.sourceUnknown') : '-'
}

import { formatInch } from '@/utils/businessNumber'
import type { DecimalValue } from '@/types/api'

export function itemSize(width?: DecimalValue, height?: DecimalValue) {
  return width && height ? `${formatInch(width)} × ${formatInch(height)}` : '-'
}

export function trackingDescription(event: { descriptionCn?: string; descriptionEn?: string; descriptionOriginal?: string }, locale: string) {
  return (locale.startsWith('zh') ? event.descriptionCn : event.descriptionEn) || event.descriptionOriginal || '-'
}
