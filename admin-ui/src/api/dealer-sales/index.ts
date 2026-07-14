import { request, requestPage } from '@/utils/request'
import type { DecimalValue, PageQuery } from '@/types/api'

export type DocumentStatus = 'SUBMITTED' | 'CANCELLED' | 'COMPLETED'
export type SalesSourceType = 'QUOTE' | 'QUICK_ORDER'
export type SalesBusinessOrigin = 'MERCHANT' | 'INTERNAL'
export type SalesPaymentStatus = 'UNPAID' | 'PAID'
export type SalesProductionStatus = 'PENDING' | 'IN_PRODUCTION' | 'COMPLETED'
export type SalesShipmentStatus = 'UNSHIPPED' | 'PARTIALLY_SHIPPED' | 'SHIPPED' | 'DELIVERED'

export interface SalesDocumentEvent {
  salesEventId?: string
  eventType?: string
  fromStatus?: string
  toStatus?: string
  operatorId?: string
  operatorName?: string
  eventNote?: string
  occurredTime?: string
}

export interface SalesItem {
  salesItemId?: string
  salesDocumentId?: string
  sourceQuoteItemId?: string
  sourceQuickOrderItemId?: string
  lineNo?: number
  roomLocation?: string
  saleProductId?: string
  saleProductCode?: string
  saleProductName?: string
  formulaVersionLabel?: string
  orderWidthInch?: DecimalValue
  orderHeightInch?: DecimalValue
  quantity?: number
  selectedOptionValues: Record<string, string>
  configurationSummary?: string
  calculationStatus?: 'PASS' | 'FAIL'
  calculationMessage?: string
  listUnitAmount?: DecimalValue
  listAmount?: DecimalValue
  discountRate?: DecimalValue
  unitAmount?: DecimalValue
  productAmount?: DecimalValue
  shippingAmount?: DecimalValue
  lineAmount?: DecimalValue
  sortOrder?: number
  remark?: string
}

export interface SalesDocument {
  salesDocumentId?: string
  tenantId?: string
  businessOrigin?: SalesBusinessOrigin
  salesStoreId?: string
  deptId?: string
  merchantId?: string
  merchantName?: string
  sourceType?: SalesSourceType
  sourceQuoteId?: string
  sourceQuickOrderId?: string
  sourceNo?: string
  quoteNo?: string
  orderNo?: string
  customerId?: string
  customerName?: string
  companyName?: string
  customerEmail?: string
  customerPhone?: string
  ownerUserId?: string
  ownerName?: string
  projectName?: string
  customerPoNo?: string
  validUntil?: string
  recipientName?: string
  recipientPhone?: string
  shippingAddress?: string
  currencyCode?: string
  listAmount?: DecimalValue
  discountRate?: DecimalValue
  discountAmount?: DecimalValue
  productAmount?: DecimalValue
  shippingAmount?: DecimalValue
  taxAmount?: DecimalValue
  totalAmount?: DecimalValue
  documentStatus?: DocumentStatus
  paymentStatus?: SalesPaymentStatus
  productionStatus?: SalesProductionStatus
  shipmentStatus?: SalesShipmentStatus
  submittedTime?: string
  paidTime?: string
  productionStartTime?: string
  productionCompleteTime?: string
  shippedTime?: string
  deliveredTime?: string
  remark?: string
  createBy?: string
  createTime?: string
  updateBy?: string
  updateTime?: string
  itemCount?: number
  items: SalesItem[]
  events?: SalesDocumentEvent[]
}

export interface SalesQuery extends PageQuery {
  tenantId?: string
  businessOrigin?: SalesBusinessOrigin | ''
  salesStoreId?: string
  ownerUserId?: string
  orderNo?: string
  sourceNo?: string
  sourceType?: SalesSourceType | ''
  customerName?: string
  merchantName?: string
  documentStatus?: DocumentStatus | ''
  paymentStatus?: SalesPaymentStatus | ''
  productionStatus?: SalesProductionStatus | ''
  shipmentStatus?: SalesShipmentStatus | ''
  beginSubmittedTime?: string
  endSubmittedTime?: string
}

export interface SalesDocumentEmailRequest {
  recipient: string
  message?: string
}

export interface SalesStoreOption {
  salesStoreId?: number | string
  tenantId?: number | string
  storeCode?: string
  storeName?: string
  deptId?: number | string
  deptName?: string
}

const root = '/dealer/sales-documents'
export const salesApi = {
  list: (params?: SalesQuery) => requestPage<SalesDocument>({ url: `${root}/list`, method: 'get', params }),
  get: (id: string | number) => request<SalesDocument>({ url: `${root}/${id}`, method: 'get' }),
  history: (customerId: string | number) => request<SalesDocument[]>({ url: `${root}/customer/${customerId}`, method: 'get' }),
  cancel: (id: string | number, reason?: string) => request({ url: `${root}/${id}/cancel`, method: 'put', params: { reason } }),
  email: (id: string | number, data: SalesDocumentEmailRequest) => request({ url: `${root}/${id}/email`, method: 'post', data }),
  pdf: (id: string | number, type: 'ORDER' | 'PRODUCTION') => request<Blob>({
    url: `${root}/${id}/pdf`,
    method: 'get',
    params: { type },
    responseType: 'blob'
  }) as unknown as Promise<Blob>
}

export const platformSalesApi = {
  list: (params?: SalesQuery) => requestPage<SalesDocument>({ url: '/platform-sales/orders/list', method: 'get', params }),
  get: (id: string | number) => request<SalesDocument>({ url: `/platform-sales/orders/${id}`, method: 'get' }),
  pdf: (id: string | number) => request<Blob>({
    url: `/platform-sales/orders/${id}/pdf`,
    method: 'get',
    responseType: 'blob'
  }) as unknown as Promise<Blob>,
  export: (id: string | number) => request<Blob>({
    url: `/platform-sales/orders/${id}/export`,
    method: 'post',
    responseType: 'blob'
  }) as unknown as Promise<Blob>
}

export function listSalesStoreOptions() {
  return request<SalesStoreOption[]>({ url: '/system/sales-store/options', method: 'get' })
}
