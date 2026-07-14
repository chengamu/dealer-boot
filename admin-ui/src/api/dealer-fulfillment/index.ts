import { request, requestPage } from '@/utils/request'
import type {
  FulfillmentAudience,
  FulfillmentOrder,
  ProductionOrder,
  ProductionQuery,
  Shipment,
  ShipmentOrder,
  ShipmentPayload,
  ShipmentQuery,
  TrackingEvent,
  TrackingOrder,
  TrackingSummary,
  TrackingQuery
} from './types'

export * from './types'

const businessRoot = '/dealer/fulfillment/progress'
const platformRoot = '/dealer/fulfillment/admin'
const factoryProductionRoot = '/dealer/fulfillment/factory/production'
const factoryShipmentRoot = '/dealer/fulfillment/factory/shipment'
const factoryTrackingRoot = '/dealer/fulfillment/factory/tracking'

export const businessFulfillmentApi = {
  list: (params?: ShipmentQuery) => requestPage<ShipmentOrder>({ url: `${businessRoot}/list`, method: 'get', params }),
  detail: (id: string | number) => request<FulfillmentOrder>({ url: `${businessRoot}/orders/${id}`, method: 'get' }),
  orderShipments: (id: string | number) => request<Shipment[]>({ url: `${businessRoot}/orders/${id}/shipments`, method: 'get' }),
  events: (id: string | number) => request<TrackingEvent[]>({ url: `${businessRoot}/shipments/${id}/tracking-events`, method: 'get' }),
  summaries: (shipmentIds: Array<string | number>) => request<TrackingSummary[]>({ url: `${businessRoot}/tracking-summaries`, method: 'get', params: { shipmentIds } }),
  confirmReceipt: (id: string | number) => request({ url: `${businessRoot}/shipments/${id}/confirm-receipt`, method: 'put' })
}

export const platformFulfillmentApi = {
  list: (params?: ShipmentQuery) => requestPage<ShipmentOrder>({ url: `${platformRoot}/list`, method: 'get', params }),
  detail: (id: string | number) => request<FulfillmentOrder>({ url: `${platformRoot}/orders/${id}`, method: 'get' }),
  events: (id: string | number) => request<TrackingEvent[]>({ url: `${platformRoot}/shipments/${id}/tracking-events`, method: 'get' }),
  summaries: (shipmentIds: Array<string | number>) => request<TrackingSummary[]>({ url: `${platformRoot}/tracking-summaries`, method: 'get', params: { shipmentIds } }),
  sync: (id: string | number) => request<TrackingEvent[]>({ url: `${platformRoot}/shipments/${id}/sync-tracking`, method: 'put' }),
  overrideReceipt: (id: string | number, reason: string) => request({ url: `${platformRoot}/shipments/${id}/override-receipt`, method: 'put', data: { reason } })
}

export const factoryProductionApi = {
  list: (params?: ProductionQuery) => requestPage<ProductionOrder>({ url: `${factoryProductionRoot}/list`, method: 'get', params }),
  detail: (id: string | number) => request<FulfillmentOrder>({ url: `${factoryProductionRoot}/orders/${id}`, method: 'get' }),
  start: (id: string | number) => request({ url: `${factoryProductionRoot}/orders/${id}/start`, method: 'put' }),
  complete: (id: string | number, note?: string) => request({ url: `${factoryProductionRoot}/orders/${id}/complete`, method: 'put', data: { note } }),
  sheet: (id: string | number) => request<Blob>({
    url: `/dealer/sales-documents/${id}/pdf`,
    method: 'get',
    params: { type: 'PRODUCTION' },
    responseType: 'blob'
  }) as unknown as Promise<Blob>
}

export const factoryShipmentApi = {
  list: (params?: ShipmentQuery) => requestPage<ShipmentOrder>({ url: `${factoryShipmentRoot}/list`, method: 'get', params }),
  detail: (id: string | number) => request<FulfillmentOrder>({ url: `${factoryShipmentRoot}/orders/${id}`, method: 'get' }),
  create: (id: string | number, data: ShipmentPayload) => request<Shipment>({ url: `${factoryShipmentRoot}/orders/${id}/shipments`, method: 'post', data }),
  update: (id: string | number, data: ShipmentPayload) => request({ url: `${factoryShipmentRoot}/shipments/${id}`, method: 'put', data }),
  remove: (id: string | number) => request({ url: `${factoryShipmentRoot}/shipments/${id}`, method: 'delete' }),
  dispatch: (id: string | number) => request({ url: `${factoryShipmentRoot}/shipments/${id}/dispatch`, method: 'put' })
}

export const factoryTrackingApi = {
  events: (id: string | number) => request<TrackingEvent[]>({ url: `${factoryTrackingRoot}/shipments/${id}/events`, method: 'get' }),
  summaries: (shipmentIds: Array<string | number>) => request<TrackingSummary[]>({ url: `${factoryTrackingRoot}/summaries`, method: 'get', params: { shipmentIds } }),
  sync: (id: string | number) => request<TrackingEvent[]>({ url: `${factoryTrackingRoot}/shipments/${id}/sync`, method: 'put' })
}

export const productionApi = factoryProductionApi
export const shipmentApi = factoryShipmentApi
export const trackingApi = factoryTrackingApi

export function isFactoryAudience(audience: FulfillmentAudience) {
  return audience === 'factory'
}
