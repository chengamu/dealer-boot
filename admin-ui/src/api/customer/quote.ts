import { request, requestPage } from '@/utils/request'
import type { DecimalValue, PageQuery } from '@/types/api'
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
  orderWidthInch?: DecimalValue
  orderHeightInch?: DecimalValue
  quantity?: number
  selectedOptionValues: Record<string, string>
  selectedOptionsSummaryCn?: string
  selectedOptionsSummaryEn?: string
  calculationStatus?: QuoteCalculationStatus
  calculationMessage?: string
  unitAmount?: DecimalValue
  productAmount?: DecimalValue
  unitShippingAmount?: DecimalValue
  shippingTemplateId?: string
  shippingAmount?: DecimalValue
  lineAmount?: DecimalValue
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
  productAmount?: DecimalValue
  shippingAmount?: DecimalValue
  totalAmount?: DecimalValue
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
  listAmount?: DecimalValue
  discountRate?: DecimalValue
  discountAmount?: DecimalValue
  productAmount?: DecimalValue
  shippingAmount?: DecimalValue
  lineAmount?: DecimalValue
}

export interface QuoteOrderPreview {
  quoteId?: string
  quoteNo?: string
  customerName?: string
  projectName?: string
  merchantLevelCode?: string
  merchantLevelName?: string
  currencyCode?: string
  customerQuoteAmount?: DecimalValue
  listAmount?: DecimalValue
  discountAmount?: DecimalValue
  productAmount?: DecimalValue
  shippingAmount?: DecimalValue
  totalAmount?: DecimalValue
  items?: QuoteOrderLinePreview[]
}

export interface QuoteConvertOrderRequest {
  recipientName: string
  recipientPhone: string
  shippingAddress: string
  customerPoNo?: string
  remark?: string
  expectedTotalAmount: string
}

export interface QuoteOrderResult {
  salesDocumentId: string
  orderNo: string
  totalAmount: string
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
