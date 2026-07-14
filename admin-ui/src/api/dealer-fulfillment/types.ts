import type { DecimalValue, PageQuery } from '@/types/api'

export type ProductionStatus = 'PENDING' | 'IN_PRODUCTION' | 'COMPLETED'
export type ShipmentStatus = 'UNSHIPPED' | 'PARTIALLY_SHIPPED' | 'SHIPPED' | 'DELIVERED'
export type PackageStatus = 'DRAFT' | 'DISPATCHED' | 'SHIPPED' | 'IN_TRANSIT' | 'DELIVERED' | 'EXCEPTION' | 'CANCELLED'
export type ReceiptStatus = 'PENDING' | 'CONFIRMED'
export type FulfillmentAudience = 'business' | 'platform' | 'factory'
export type FulfillmentGridKind = 'production' | 'shipment' | 'package' | 'tracking'
export type BusinessOrigin = 'MERCHANT' | 'INTERNAL' | string

export interface FulfillmentEvent {
  salesEventId?: string
  eventType?: string
  operatorName?: string
  eventNote?: string
  occurredTime?: string
}

export interface FulfillmentItem {
  salesItemId: string
  lineNo?: number
  roomLocation?: string
  saleProductCode?: string
  saleProductName?: string
  formulaVersionLabel?: string
  orderWidthInch?: DecimalValue
  orderHeightInch?: DecimalValue
  quantity?: number
  configurationSummary?: string
  bomSnapshotJson?: string
}

export interface TrackingEvent {
  trackingEventId?: string
  shipmentId?: string
  eventCode?: string
  eventStatus?: string
  descriptionOriginal?: string
  descriptionCn?: string
  descriptionEn?: string
  location?: string
  occurredTime?: string
  source?: string
}

export interface TrackingSummary {
  shipmentId?: string
  capability?: string
  trackingStatus?: string
  lastTrackingTime?: string
  latestEvent?: TrackingEvent
}

export interface ShipmentItem {
  shipmentItemId?: string
  shipmentId?: string
  salesDocumentId?: string
  salesItemId: string
  lineNo?: number
  saleProductCode?: string
  saleProductName?: string
  quantity: number
  remark?: string
}

export interface Shipment {
  shipmentId?: string
  salesDocumentId?: string
  shipmentNo?: string
  packageNo?: string
  carrierCode?: string
  carrierName?: string
  trackingNo?: string
  status?: PackageStatus
  trackingStatus?: string
  itemQuantity?: number
  weight?: DecimalValue
  weightUnit?: string
  length?: DecimalValue
  width?: DecimalValue
  height?: DecimalValue
  dimensionUnit?: string
  shippedTime?: string
  receiptStatus?: ReceiptStatus
  receivedTime?: string
  lastTrackingTime?: string
  trackingErrorCode?: string
  trackingErrorMessage?: string
  remark?: string
  items?: ShipmentItem[]
  trackingEvents?: TrackingEvent[]
}

export interface ProductionOrder {
  salesDocumentId: string
  tenantId?: string
  businessOrigin?: BusinessOrigin
  orderNo?: string
  sourceType?: string
  sourceNo?: string
  merchantName?: string
  customerName?: string
  projectName?: string
  paymentMethod?: string
  paidTime?: string
  productionStatus?: ProductionStatus
  productionStartTime?: string
  productionCompleteTime?: string
  itemCount?: number
  totalQuantity?: number
}

export interface ShipmentOrder extends ProductionOrder {
  shipmentStatus?: ShipmentStatus
  dispatchedQuantity?: number
  packageCount?: number
  shippedTime?: string
  carrierName?: string
  trackingNo?: string
}

export interface FulfillmentOrder extends ProductionOrder {
  documentStatus?: string
  paymentStatus?: string
  shipmentStatus?: ShipmentStatus
  recipientName?: string
  recipientPhone?: string
  shippingAddress?: string
  deliveredTime?: string
  items?: FulfillmentItem[]
  shipments?: Shipment[]
  events?: FulfillmentEvent[]
}

export interface ProductionQuery extends PageQuery {
  beginTime?: string
  endTime?: string
  businessOrigin?: BusinessOrigin | ''
  tenantId?: string
  salesStoreId?: string
  orderNo?: string
  sourceNo?: string
  merchantName?: string
  customerName?: string
  productionStatus?: ProductionStatus | ''
  paymentMethod?: string
}

export interface ShipmentQuery extends PageQuery {
  beginTime?: string
  endTime?: string
  businessOrigin?: BusinessOrigin | ''
  tenantId?: string
  salesStoreId?: string
  orderNo?: string
  merchantName?: string
  customerName?: string
  productionStatus?: ProductionStatus | ''
  shipmentStatus?: ShipmentStatus | ''
  carrierName?: string
  trackingNo?: string
}

export interface TrackingOrder extends ShipmentOrder {
  documentStatus?: string
  customerName?: string
  projectName?: string
  submittedTime?: string
  productionStatus?: ProductionStatus
  shipmentStatus?: ShipmentStatus
  receivedPackageCount?: number
  latestTrackingStatus?: string
  latestTrackingTime?: string
}

export interface TrackingQuery extends PageQuery {
  beginTime?: string
  endTime?: string
  businessOrigin?: BusinessOrigin | ''
  tenantId?: string
  salesStoreId?: string
  orderNo?: string
  customerName?: string
  projectName?: string
  productionStatus?: ProductionStatus | ''
  shipmentStatus?: ShipmentStatus | ''
}

export interface ShipmentPayload {
  packageNo?: string
  carrierCode?: string
  carrierName?: string
  trackingNo?: string
  weight?: DecimalValue
  weightUnit?: string
  length?: DecimalValue
  width?: DecimalValue
  height?: DecimalValue
  dimensionUnit?: string
  remark?: string
  items: Array<{ salesItemId: string; quantity: number; remark?: string }>
}
