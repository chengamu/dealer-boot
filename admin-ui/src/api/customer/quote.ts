import { request, requestPage } from '@/utils/request'
import type { PageQuery } from '@/types/api'
import type { SaleProductQuery, SaleProductVO } from '@/api/product-pricing/types'
import type { ProductFormulaOptionVO, ProductFormulaOptionValueVO } from '@/api/product-capability/types'

export type QuoteLanguage = 'ZH_CN' | 'EN_US'
export type QuoteStatus = 'DRAFT' | 'CONFIRMED' | 'VOID'
export type QuoteCalculationStatus = 'PENDING' | 'PASS' | 'FAIL'

export interface CustomerQuoteItem {
  quoteItemId?: string
  quoteId?: string
  lineNo?: number
  roomLocation?: string
  saleProductId?: string
  saleProductCode?: string
  saleProductName?: string
  formulaId?: string
  formulaVersionId?: string
  formulaVersionLabel?: string
  categoryId?: string
  categoryCode?: string
  categoryNameCn?: string
  productTypeCode?: string
  productTypeNameCn?: string
  orderWidthInch?: number
  orderHeightInch?: number
  quantity?: number
  selectedOptionValues: Record<string, string>
  selectedOptionsSummaryCn?: string
  selectedOptionsSummaryEn?: string
  calculationStatus?: QuoteCalculationStatus
  calculationMessage?: string
  unitAmount?: number
  productAmount?: number
  unitShippingAmount?: number
  shippingTemplateId?: string
  shippingAmount?: number
  lineAmount?: number
  sortOrder?: number
  remark?: string
}

export interface CustomerQuote {
  quoteId?: string
  quoteNo?: string
  customerId?: string
  customerName?: string
  companyName?: string
  customerEmail?: string
  customerPhone?: string
  customerPoNo?: string
  recipientName?: string
  recipientPhone?: string
  shippingAddress?: string
  projectName?: string
  quoteLanguage: QuoteLanguage
  validUntil?: string
  ownerUserId?: string
  ownerName?: string
  currencyCode?: string
  status?: QuoteStatus
  productAmount?: number
  shippingAmount?: number
  totalAmount?: number
  confirmedBy?: string
  confirmedTime?: string
  salesDocumentId?: string
  orderNo?: string
  convertedBy?: string
  convertedTime?: string
  itemCount?: number
  remark?: string
  createBy?: string
  createTime?: string
  updateBy?: string
  updateTime?: string
  items: CustomerQuoteItem[]
}

export interface QuoteOrderLinePreview {
  quoteItemId?: string
  lineNo?: number
  roomLocation?: string
  saleProductName?: string
  quantity?: number
  listAmount?: number
  discountRate?: number
  discountAmount?: number
  productAmount?: number
  shippingAmount?: number
  lineAmount?: number
}

export interface QuoteOrderPreview {
  quoteId?: string
  quoteNo?: string
  customerName?: string
  projectName?: string
  merchantLevelCode?: string
  merchantLevelName?: string
  currencyCode?: string
  customerQuoteAmount?: number
  listAmount?: number
  discountAmount?: number
  productAmount?: number
  shippingAmount?: number
  totalAmount?: number
  items?: QuoteOrderLinePreview[]
}

export interface QuoteConvertOrderRequest {
  recipientName: string
  recipientPhone: string
  shippingAddress: string
  customerPoNo?: string
  remark?: string
  expectedTotalAmount: number
}

export interface QuoteOrderResult {
  salesDocumentId: string
  orderNo: string
  totalAmount: number
}

export interface QuoteEmailRequest {
  recipient: string
  message?: string
}

export interface CustomerQuoteQuery extends PageQuery {
  quoteNo?: string
  customerId?: string
  projectName?: string
  status?: QuoteStatus | ''
}

export interface CustomerQuoteCatalogSetup {
  formulaOptions?: ProductFormulaOptionVO[]
  formulaOptionValues?: ProductFormulaOptionValueVO[]
}

export const customerQuoteApi = {
  list: (query?: CustomerQuoteQuery) => requestPage<CustomerQuote>({ url: '/customer/quotes/list', method: 'get', params: query }),
  get: (id: string | number) => request<CustomerQuote>({ url: `/customer/quotes/${id}`, method: 'get' }),
  add: (data: CustomerQuote) => request<CustomerQuote>({ url: '/customer/quotes', method: 'post', data }),
  update: (data: CustomerQuote) => request<CustomerQuote>({ url: '/customer/quotes', method: 'put', data }),
  remove: (ids: Array<string | number> | string | number) => request({ url: `/customer/quotes/${ids}`, method: 'delete' }),
  calculateItem: (data: CustomerQuoteItem, quoteLanguage: QuoteLanguage) => request<CustomerQuoteItem>({
    url: '/customer/quotes/calculate-item', method: 'post', params: { quoteLanguage }, data
  }),
  confirm: (id: string | number) => request({ url: `/customer/quotes/${id}/confirm`, method: 'put' }),
  void: (id: string | number) => request({ url: `/customer/quotes/${id}/void`, method: 'put' }),
  copy: (id: string | number) => request<string>({ url: `/customer/quotes/${id}/copy`, method: 'post' }),
  orderPreview: (id: string | number) => request<QuoteOrderPreview>({ url: `/customer/quotes/${id}/order-preview`, method: 'post' }),
  convertOrder: (id: string | number, data: QuoteConvertOrderRequest) => request<QuoteOrderResult>({
    url: `/customer/quotes/${id}/convert-order`, method: 'post', data
  }),
  email: (id: string | number, data: QuoteEmailRequest) => request({ url: `/customer/quotes/${id}/email`, method: 'post', data }),
  pdf: (id: string | number) => request<Blob>({
    url: `/customer/quotes/${id}/pdf`, method: 'get', responseType: 'blob'
  }) as unknown as Promise<Blob>
}

export const customerQuoteCatalogApi = {
  products: (query?: SaleProductQuery) => request<SaleProductVO[]>({
    url: '/customer/quotes/catalog/products', method: 'get', params: query
  }),
  setup: (saleProductId: string | number) => request<CustomerQuoteCatalogSetup>({
    url: `/customer/quotes/catalog/products/${saleProductId}/setup`, method: 'get'
  })
}
