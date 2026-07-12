import type { CustomerQuoteCatalogSetup } from '@/api/customer/quote'
import type { SaleProductQuery, SaleProductVO } from '@/api/product-pricing/types'
import type { PageQuery } from '@/types/api'
import { request, requestPage } from '@/utils/request'

export type QuickOrderStatus = 'DRAFT' | 'ORDERED'
export type QuickOrderCalculationStatus = 'PENDING' | 'PASS' | 'FAIL'

export interface QuickOrderItem {
  quickOrderItemId?: string
  quickOrderId?: string
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
  calculationStatus?: QuickOrderCalculationStatus
  calculationMessage?: string
  listUnitAmount?: number
  listAmount?: number
  discountRate?: number
  discountAmount?: number
  unitAmount?: number
  productAmount?: number
  unitShippingAmount?: number
  shippingAmount?: number
  lineAmount?: number
  sortOrder?: number
  remark?: string
}

export interface QuickOrder {
  quickOrderId?: string
  quickOrderNo?: string
  customerId?: string
  customerName?: string
  companyName?: string
  customerPoNo?: string
  recipientName?: string
  recipientPhone?: string
  shippingAddress?: string
  currencyCode?: string
  productAmount?: number
  shippingAmount?: number
  totalAmount?: number
  status?: QuickOrderStatus
  salesDocumentId?: string
  orderNo?: string
  itemCount?: number
  totalQuantity?: number
  remark?: string
  createBy?: string
  createTime?: string
  updateBy?: string
  updateTime?: string
  items: QuickOrderItem[]
}

export interface QuickOrderSubmitResult {
  salesDocumentId: string
  orderNo: string
  totalAmount: number
}

export interface QuickOrderSubmitRequest {
  expectedTotalAmount: number
}

export interface QuickOrderQuery extends PageQuery {
  quickOrderNo?: string
  customerName?: string
  status?: QuickOrderStatus | ''
  createBy?: string
  beginUpdateTime?: string
  endUpdateTime?: string
}

const root = '/dealer/quick-orders'

export const quickOrderApi = {
  list: (params?: QuickOrderQuery) => requestPage<QuickOrder>({ url: `${root}/list`, method: 'get', params }),
  get: (id: string | number) => request<QuickOrder>({ url: `${root}/${id}`, method: 'get' }),
  add: (data: QuickOrder) => request<string>({ url: root, method: 'post', data }),
  update: (data: QuickOrder) => request({ url: root, method: 'put', data }),
  remove: (ids: Array<string | number> | string | number) => request({ url: `${root}/${ids}`, method: 'delete' }),
  copy: (id: string | number) => request<string>({ url: `${root}/${id}/copy`, method: 'post' }),
  calculateItem: (id: string | number, data: QuickOrderItem) => request<QuickOrderItem>({
    url: `${root}/${id}/calculate-item`,
    method: 'put',
    data
  }),
  calculate: (id: string | number) => request<QuickOrder>({ url: `${root}/${id}/calculate`, method: 'put' }),
  submit: (id: string | number, data: QuickOrderSubmitRequest) => request<QuickOrderSubmitResult>({
    url: `${root}/${id}/submit`,
    method: 'post',
    data
  })
}

export const quickOrderCatalogApi = {
  products: (query?: SaleProductQuery) => request<SaleProductVO[]>({
    url: `${root}/catalog/products`,
    method: 'get',
    params: query
  }),
  setup: (saleProductId: string | number) => request<CustomerQuoteCatalogSetup>({
    url: `${root}/catalog/products/${saleProductId}/setup`,
    method: 'get'
  })
}
