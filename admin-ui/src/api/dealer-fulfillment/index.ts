import { request, requestPage } from '@/utils/request'
import type {
  FulfillmentOrder,
  ProductionOrder,
  ProductionQuery,
  Shipment,
  ShipmentOrder,
  ShipmentPayload,
  ShipmentQuery,
  TrackingEvent,
  TrackingOrder,
  TrackingQuery
} from './types'

export * from './types'

const root = '/dealer/fulfillment'

export const productionApi = {
  list: (params?: ProductionQuery) => requestPage<ProductionOrder>({ url: `${root}/production/list`, method: 'get', params }),
  detail: (id: string | number) => request<FulfillmentOrder>({ url: `${root}/orders/${id}`, method: 'get' }),
  start: (id: string | number) => request({ url: `${root}/orders/${id}/production/start`, method: 'put' }),
  complete: (id: string | number, note?: string) => request({ url: `${root}/orders/${id}/production/complete`, method: 'put', data: { note } }),
  sheet: (id: string | number) => request<Blob>({
    url: `/dealer/sales-documents/${id}/pdf`,
    method: 'get',
    params: { type: 'PRODUCTION' },
    responseType: 'blob'
  }) as unknown as Promise<Blob>
}

export const shipmentApi = {
  list: (params?: ShipmentQuery) => requestPage<ShipmentOrder>({ url: `${root}/shipment/list`, method: 'get', params }),
  orderShipments: (id: string | number) => request<Shipment[]>({ url: `${root}/orders/${id}/shipments`, method: 'get' }),
  detail: (id: string | number) => request<Shipment>({ url: `${root}/shipments/${id}`, method: 'get' }),
  create: (id: string | number, data: ShipmentPayload) => request<Shipment>({ url: `${root}/orders/${id}/shipments`, method: 'post', data }),
  update: (id: string | number, data: ShipmentPayload) => request({ url: `${root}/shipments/${id}`, method: 'put', data }),
  remove: (id: string | number) => request({ url: `${root}/shipments/${id}`, method: 'delete' }),
  dispatch: (id: string | number) => request({ url: `${root}/shipments/${id}/dispatch`, method: 'put' })
}

export const trackingApi = {
  listOrders: (params?: TrackingQuery) => requestPage<TrackingOrder>({ url: '/dealer/sales-documents/list', method: 'get', params }),
  order: (id: string | number) => request<TrackingOrder>({ url: `/dealer/sales-documents/${id}`, method: 'get' }),
  events: (id: string | number) => request<TrackingEvent[]>({ url: `${root}/shipments/${id}/tracking-events`, method: 'get' }),
  sync: (id: string | number) => request<TrackingEvent[]>({ url: `${root}/shipments/${id}/sync-tracking`, method: 'put' }),
  confirmReceipt: (id: string | number) => request({ url: `${root}/shipments/${id}/confirm-receipt`, method: 'put' }),
  overrideReceipt: (id: string | number, reason: string) => request({ url: `${root}/shipments/${id}/override-receipt`, method: 'put', data: { reason } })
}
