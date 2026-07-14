import { request } from '@/utils/request'
import type { DecimalValue } from '@/types/api'

export interface DashboardCapabilities {
  quote: boolean
  order: boolean
  payment: boolean
  production: boolean
  shipment: boolean
  createQuote: boolean
  createCustomer: boolean
  quickOrder: boolean
}

export interface DashboardTargetFilters {
  id?: string
  status?: string
  paymentStatus?: string
  productionStatuses: string[]
  shipmentStatuses: string[]
  unconverted?: boolean
  dateFrom?: string
  dateTo?: string
  tenantId?: string
  businessOrigin?: string
  salesStoreId?: string
}

export interface DashboardTarget {
  module: 'QUOTE' | 'ORDER' | 'PAYMENT' | 'PRODUCTION' | 'SHIPMENT'
  filters: DashboardTargetFilters
}

export interface DashboardSummary {
  activeQuoteCount: number
  expiringQuoteCount: number
  activeProductionCount: number
  pendingProductionCount: number
  inProductionCount: number
  paidOrderCount: number
  paidAmount: DecimalValue
  pendingPaymentOrderCount: number
  pendingPaymentAmount: DecimalValue
  currencyCode: string
  activeQuoteTarget: DashboardTarget
  productionTarget: DashboardTarget
  paidTarget: DashboardTarget
  pendingPaymentTarget: DashboardTarget
}

export interface RecentQuote {
  id: string
  quoteNo: string
  customerName?: string
  projectName?: string
  status: 'DRAFT' | 'CONFIRMED' | 'VOID'
  validUntil?: string
  totalAmount?: DecimalValue
  currencyCode?: string
  salesDocumentId?: string
  updatedAt?: string
  businessOrigin?: string
  tenantId?: string
  salesStoreId?: string
  target: DashboardTarget
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
  totalAmount?: DecimalValue
  currencyCode?: string
  submittedAt?: string
  businessOrigin?: string
  tenantId?: string
  salesStoreId?: string
  target: DashboardTarget
}

export interface DashboardAttentionItem {
  type: 'QUOTE' | 'ORDER'
  sourceId: string
  sourceNo: string
  customerName?: string
  projectName?: string
  occurredAt?: string
}

export interface DashboardAttentionGroup {
  reasonCode: 'QUOTE_UNCONVERTED' | 'QUOTE_EXPIRING' | 'PAYMENT_PENDING' | 'PRODUCTION_PENDING' | 'SHIPMENT_PENDING'
  totalCount: number
  target: DashboardTarget
  items: DashboardAttentionItem[]
}

export interface SalesDashboard {
  viewType: string
  scopeLabel: string
  salesStoreId?: string
  salesStoreName?: string
  periodStart: string
  periodEnd: string
  dataAsOf: string
  capabilities: DashboardCapabilities
  summary: DashboardSummary
  recentQuotes: RecentQuote[]
  recentOrders: RecentOrder[]
  attentionGroups: DashboardAttentionGroup[]
}

export type SalesDashboardAudience = 'business' | 'platformSales'

export const businessSalesDashboardApi = {
  get: () => request<SalesDashboard>({ url: '/sales/dashboard', method: 'get' })
}

export const platformSalesDashboardApi = {
  get: () => request<SalesDashboard>({ url: '/platform-sales/dashboard', method: 'get' })
}

export function getSalesDashboard(audience: SalesDashboardAudience = 'business') {
  return audience === 'platformSales' ? platformSalesDashboardApi.get() : businessSalesDashboardApi.get()
}

export const salesDashboardApi = businessSalesDashboardApi
