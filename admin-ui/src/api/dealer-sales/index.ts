import { request, requestPage } from '@/utils/request'
import type { PageQuery } from '@/types/api'

export type DocumentStatus = 'DRAFT' | 'QUOTED' | 'SUBMITTED' | 'CANCELLED' | 'COMPLETED'
export interface SalesItem {
  salesItemId?: string; lineNo?: number; roomLocation?: string; saleProductId?: string
  saleProductCode?: string; saleProductName?: string; formulaVersionLabel?: string
  orderWidthInch?: number; orderHeightInch?: number; quantity?: number
  selectedOptionValues: Record<string, string>; configurationSummary?: string
  calculationStatus?: 'PASS' | 'FAIL'; calculationMessage?: string
  listUnitAmount?: number; listAmount?: number; discountRate?: number; unitAmount?: number
  productAmount?: number; shippingAmount?: number; lineAmount?: number; sortOrder?: number; remark?: string
}
export interface SalesDocument {
  salesDocumentId?: string; tenantId?: string; merchantName?: string; quoteNo?: string; orderNo?: string
  customerId?: string; customerName?: string; companyName?: string; customerEmail?: string; customerPhone?: string
  ownerName?: string; projectName?: string; customerPoNo?: string; validUntil?: string
  recipientName?: string; recipientPhone?: string; shippingAddress?: string; currencyCode?: string
  listAmount?: number; discountAmount?: number; productAmount?: number; shippingAmount?: number; taxAmount?: number; totalAmount?: number
  documentStatus?: DocumentStatus; paymentStatus?: string; productionStatus?: string; shipmentStatus?: string
  paymentMethod?: string; paidAmount?: number; paymentReference?: string; carrierName?: string; trackingNo?: string
  remark?: string; items: SalesItem[]; events?: Array<Record<string, any>>; updateTime?: string
}
export interface SalesQuery extends PageQuery {
  quoteNo?: string; orderNo?: string; customerName?: string; merchantName?: string
  documentStatus?: string; paymentStatus?: string; productionStatus?: string; shipmentStatus?: string
}
export interface CatalogProduct { saleProductId: string; saleProductCode?: string; saleProductName?: string }
export interface CatalogOption { optionCode?: string; optionNameCn?: string; optionNameEn?: string; requiredFlag?: boolean; sortOrder?: number }
export interface CatalogValue { optionCode?: string; valueCode?: string; valueNameCn?: string; valueNameEn?: string; defaultFlag?: boolean }
export interface ProductSetup { saleProduct?: CatalogProduct; options?: CatalogOption[]; optionValues?: CatalogValue[] }

const root = '/dealer/sales-documents'
export const salesApi = {
  list: (params?: SalesQuery) => requestPage<SalesDocument>({ url: `${root}/list`, method: 'get', params }),
  get: (id: string) => request<SalesDocument>({ url: `${root}/${id}`, method: 'get' }),
  history: (customerId: string) => request<SalesDocument[]>({ url: `${root}/customer/${customerId}`, method: 'get' }),
  add: (data: SalesDocument) => request<string>({ url: root, method: 'post', data }),
  update: (data: SalesDocument) => request({ url: root, method: 'put', data }),
  copy: (id: string) => request<string>({ url: `${root}/${id}/copy`, method: 'post' }),
  remove: (id: string) => request({ url: `${root}/${id}`, method: 'delete' }),
  products: () => request<CatalogProduct[]>({ url: `${root}/catalog/options`, method: 'get' }),
  setup: (id: string) => request<ProductSetup>({ url: `${root}/catalog/${id}/setup`, method: 'get' }),
  calculateItem: (data: SalesItem) => request<SalesItem>({ url: `${root}/calculate-item`, method: 'post', data }),
  calculateAll: (id: string) => request<SalesDocument>({ url: `${root}/${id}/calculate`, method: 'put' }),
  quote: (id: string) => request({ url: `${root}/${id}/quote`, method: 'put' }),
  reopen: (id: string) => request({ url: `${root}/${id}/reopen`, method: 'put' }),
  submit: (id: string) => request<string>({ url: `${root}/${id}/submit`, method: 'put' }),
  cancel: (id: string, reason?: string) => request({ url: `${root}/${id}/cancel`, method: 'put', params: { reason } }),
  payment: (id: string, data: any) => request({ url: `${root}/${id}/payment`, method: 'put', data }),
  startProduction: (id: string) => request({ url: `${root}/${id}/production/start`, method: 'put' }),
  completeProduction: (id: string) => request({ url: `${root}/${id}/production/complete`, method: 'put' }),
  ship: (id: string, data: any) => request({ url: `${root}/${id}/ship`, method: 'put', data }),
  deliver: (id: string) => request({ url: `${root}/${id}/deliver`, method: 'put' }),
  email: (id: string, data: any) => request({ url: `${root}/${id}/email`, method: 'post', data }),
  pdf: (id: string, type: string) => request<Blob>({ url: `${root}/${id}/pdf`, method: 'get', params: { type }, responseType: 'blob' }) as unknown as Promise<Blob>,
  pdfUrl: (id: string, type: string) => `${root}/${id}/pdf?type=${type}`
}
