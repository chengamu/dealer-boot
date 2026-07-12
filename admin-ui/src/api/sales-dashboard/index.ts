import { request } from '@/utils/request'

export type DashboardScopeType = 'SELF' | 'TENANT' | 'PLATFORM_ALL' | 'PLATFORM_AUTHORIZED' | 'PLATFORM_FINANCE' | 'FULFILLMENT'

export interface DashboardCapabilities {
  quote: boolean
  quoteDetail: boolean
  order: boolean
  orderDetail: boolean
  payment: boolean
  fulfillment: boolean
  production: boolean
  shipment: boolean
  createQuote: boolean
  createCustomer: boolean
  quickOrder: boolean
}

export interface QuoteSummary {
  activeCount: number
  draftCount: number
  confirmedUnconvertedCount: number
}

export interface OrderSummary {
  activeCount: number
  periodSubmittedCount: number
  pendingPaymentCount: number
}

export interface PaymentSummary {
  pendingCount: number
  pendingAmount: number
  paidThisMonthCount: number
  paidThisMonthAmount: number
  currencyCode: string
}

export interface FulfillmentSummary {
  pendingProductionCount: number
  inProductionCount: number
  pendingShipmentCount: number
  shippedCount: number
  completedCount: number
}

export interface RecentQuote {
  id: string
  quoteNo: string
  customerName?: string
  projectName?: string
  status: 'DRAFT' | 'CONFIRMED' | 'VOID'
  validUntil?: string
  totalAmount?: number
  currencyCode?: string
  salesDocumentId?: string
  updatedAt?: string
}

export interface RecentOrder {
  id: string
  orderNo: string
  sourceType: 'QUOTE' | 'QUICK_ORDER'
  customerName?: string
  projectName?: string
  documentStatus: 'SUBMITTED' | 'CANCELLED' | 'COMPLETED'
  paymentStatus: 'UNPAID' | 'PAID'
  productionStatus: 'PENDING' | 'IN_PRODUCTION' | 'COMPLETED'
  shipmentStatus: 'UNSHIPPED' | 'PARTIALLY_SHIPPED' | 'SHIPPED' | 'DELIVERED'
  totalAmount?: number
  currencyCode?: string
  submittedAt?: string
}

export interface DashboardTodo {
  type: 'QUOTE' | 'ORDER'
  sourceId: string
  sourceNo: string
  customerName?: string
  projectName?: string
  reasonCode: 'QUOTE_UNCONVERTED' | 'QUOTE_EXPIRING' | 'PAYMENT_MISSING' | 'PAYMENT_PENDING' | 'PRODUCTION_PENDING' | 'SHIPMENT_PENDING'
  occurredAt?: string
}

export interface SalesDashboard {
  scopeType: DashboardScopeType
  scopeLabel: string
  dataAsOf: string
  fromDate: string
  toDate: string
  capabilities: DashboardCapabilities
  quoteSummary?: QuoteSummary
  orderSummary?: OrderSummary
  paymentSummary?: PaymentSummary
  fulfillmentSummary?: FulfillmentSummary
  recentQuotes: RecentQuote[]
  recentOrders: RecentOrder[]
  todos: DashboardTodo[]
}

export const salesDashboardApi = {
  get: () => request<SalesDashboard>({ url: '/sales/dashboard', method: 'get' })
}
