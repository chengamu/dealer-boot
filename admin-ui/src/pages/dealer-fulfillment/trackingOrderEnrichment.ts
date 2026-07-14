import { businessFulfillmentApi, type Shipment, type TrackingOrder } from '@/api/dealer-fulfillment'

export async function enrichTrackingOrders(rows: TrackingOrder[]) {
  return Promise.all(rows.map(async (row) => {
    const response = await businessFulfillmentApi.orderShipments(row.salesDocumentId)
    return enrich(row, response.data || [])
  }))
}

function enrich(row: TrackingOrder, shipments: Shipment[]): TrackingOrder {
  const active = shipments.filter((item) => item.status !== 'CANCELLED')
  const latest = active.flatMap((item) => item.trackingEvents || [])
    .sort((a, b) => String(b.occurredTime || '').localeCompare(String(a.occurredTime || '')))[0]
  return {
    ...row,
    packageCount: active.length,
    receivedPackageCount: active.filter((item) => item.receiptStatus === 'CONFIRMED').length,
    latestTrackingStatus: latest?.descriptionOriginal || latest?.eventStatus || latest?.eventCode,
    latestTrackingTime: latest?.occurredTime
  }
}
