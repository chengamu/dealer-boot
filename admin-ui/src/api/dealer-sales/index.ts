import { request, requestPage } from '@/utils/request'
import type { PageQuery } from '@/types/api'

export type DocumentStatus = 'SUBMITTED' | 'CANCELLED' | 'COMPLETED'
export type SalesSourceType = 'QUOTE' | 'QUICK_ORDER'
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
  orderWidthInch?: number
  orderHeightInch?: number
  quantity?: number
  selectedOptionValues: Record<string, string>
  configurationSummary?: string
  calculationStatus?: 'PASS' | 'FAIL'
  calculationMessage?: string
  listUnitAmount?: number
  listAmount?: number
  discountRate?: number
  unitAmount?: number
  productAmount?: number
  shippingAmount?: number
  lineAmount?: number
  sortOrder?: number
  remark?: string
}

export interface SalesDocument {
  salesDocumentId?: string
  tenantId?: string
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
  listAmount?: number
  discountRate?: number
  discountAmount?: number
  productAmount?: number
  shippingAmount?: number
  taxAmount?: number
  totalAmount?: number
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
