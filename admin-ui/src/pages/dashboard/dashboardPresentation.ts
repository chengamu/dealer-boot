import { Box, Document, DocumentAdd, Money, ShoppingCart, UserFilled } from '@element-plus/icons-vue'
import type { Component } from 'vue'
import type {
  DashboardAttentionGroup as ApiAttentionGroup,
  DashboardAttentionItem,
  DashboardCapabilities,
  RecentOrder,
  RecentQuote,
  SalesDashboard
} from '@/api/sales-dashboard'
import { formatCurrency } from '@/utils/businessNumber'

type Translate = (key: string, params?: Record<string, string | number>) => string

export interface DashboardMetricCard {
  key: string
  label: string
  value: string
  caption: string
  target: string
  icon: Component
}

export interface DashboardQuickAction {
  key: string
  title: string
  description: string
  icon: Component
}

export type DashboardAttentionKey = 'quoteExpiring' | 'paymentPending' | 'shipmentPending' | 'productionPending' | 'quoteUnconverted'

export interface DashboardAttentionGroup {
  key: DashboardAttentionKey
  title: string
  target: string
  count: number
  accent: string
  items: DashboardAttentionItem[]
}

export function buildMetricCards(dashboard: SalesDashboard, t: Translate): DashboardMetricCard[] {
  const cards: DashboardMetricCard[] = []
  const { summary } = dashboard

  if (dashboard.capabilities.quote) {
    cards.push({
      key: 'activeQuotes',
      label: t('dashboard.sales.summary.activeQuotes'),
      value: String(summary.activeQuoteCount),
      caption: t('dashboard.sales.summary.activeQuotesCaption', {
        confirmed: summary.expiringQuoteCount,
        draft: summary.activeQuoteCount
      }),
      target: 'quote',
      icon: Document
    })
  }

  if (dashboard.capabilities.production) {
    cards.push({
      key: 'inProduction',
      label: t('dashboard.sales.summary.inProduction'),
      value: String(summary.activeProductionCount),
      caption: t('dashboard.sales.summary.productionCaption', {
        pending: summary.pendingProductionCount,
        active: summary.inProductionCount
      }),
      target: 'production',
      icon: Box
    })
  }

  if (dashboard.capabilities.payment) {
    cards.push({
      key: 'monthSales',
      label: t('dashboard.sales.summary.monthSales'),
      value: money(summary.paidAmount, summary.currencyCode),
      caption: t('dashboard.sales.summary.monthSalesCaption', {
        count: summary.paidOrderCount
      }),
      target: 'payment',
      icon: Money
    })
    cards.push({
      key: 'pendingAmount',
      label: t('dashboard.sales.summary.pendingAmount'),
      value: money(summary.pendingPaymentAmount, summary.currencyCode),
      caption: t('dashboard.sales.summary.pendingAmountCaption', {
        count: summary.pendingPaymentOrderCount
      }),
      target: 'payment',
      icon: ShoppingCart
    })
  }

  if (cards.length < 4 && dashboard.capabilities.order) {
    cards.push({
      key: 'activeOrders',
      label: t('dashboard.sales.summary.activeOrders'),
      value: String(summary.pendingPaymentOrderCount),
      caption: t('dashboard.sales.summary.activeOrdersCaption', {
        count: summary.pendingPaymentOrderCount
      }),
      target: 'order',
      icon: ShoppingCart
    })
  }

  if (cards.length < 4 && dashboard.capabilities.shipment) {
    cards.push({
      key: 'pendingShipment',
      label: t('dashboard.sales.summary.pendingShipment'),
      value: String(groupCount(dashboard.attentionGroups, 'SHIPMENT_PENDING')),
      caption: t('dashboard.sales.summary.pendingShipmentCaption', {
        count: groupCount(dashboard.attentionGroups, 'SHIPMENT_PENDING')
      }),
      target: 'shipment',
      icon: Box
    })
  }

  return cards.slice(0, 4)
}

export function buildQuickActions(capabilities: DashboardCapabilities, t: Translate): DashboardQuickAction[] {
  const actions: DashboardQuickAction[] = []
  if (capabilities.createQuote) {
    actions.push({
      key: 'quote',
      title: t('dashboard.sales.actions.quote.title'),
      description: t('dashboard.sales.actions.quote.description'),
      icon: DocumentAdd
    })
  }
  if (capabilities.quickOrder) {
    actions.push({
      key: 'quickOrder',
      title: t('dashboard.sales.actions.quickOrder.title'),
      description: t('dashboard.sales.actions.quickOrder.description'),
      icon: ShoppingCart
    })
  }
  if (capabilities.createCustomer) {
    actions.push({
      key: 'customer',
      title: t('dashboard.sales.actions.customer.title'),
      description: t('dashboard.sales.actions.customer.description'),
      icon: UserFilled
    })
  }
  return actions
}

export function buildAttentionGroups(dashboard: SalesDashboard, t: Translate): DashboardAttentionGroup[] {
  return dashboard.attentionGroups.map((group) => ({
    key: attentionKey(group.reasonCode),
    title: attentionTitle(group.reasonCode, t),
    target: group.target.module.toLowerCase(),
    count: group.totalCount,
    accent: attentionAccent(group.reasonCode),
    items: group.items
  }))
}

function groupCount(groups: ApiAttentionGroup[], reason: ApiAttentionGroup['reasonCode']) {
  return groups.find((group) => group.reasonCode === reason)?.totalCount ?? 0
}

function attentionKey(reason: ApiAttentionGroup['reasonCode']): DashboardAttentionKey {
  const keys: Record<ApiAttentionGroup['reasonCode'], DashboardAttentionKey> = {
    QUOTE_EXPIRING: 'quoteExpiring',
    PAYMENT_PENDING: 'paymentPending',
    SHIPMENT_PENDING: 'shipmentPending',
    PRODUCTION_PENDING: 'productionPending',
    QUOTE_UNCONVERTED: 'quoteUnconverted'
  }
  return keys[reason]
}

function attentionAccent(reason: ApiAttentionGroup['reasonCode']) {
  if (reason === 'QUOTE_UNCONVERTED') return '#16a34a'
  if (reason === 'PRODUCTION_PENDING' || reason === 'SHIPMENT_PENDING') return '#1677ff'
  return '#ff8a1f'
}

function attentionTitle(reason: ApiAttentionGroup['reasonCode'], t: Translate) {
  if (reason === 'QUOTE_EXPIRING') return t('dashboard.sales.attention.quoteExpiring')
  if (reason === 'PAYMENT_PENDING') return t('dashboard.sales.attention.paymentPending')
  if (reason === 'SHIPMENT_PENDING') return t('dashboard.sales.attention.shipmentPending')
  if (reason === 'PRODUCTION_PENDING') return t('dashboard.sales.attention.productionPending')
  return t('dashboard.sales.attention.quoteUnconverted')
}

export function formatDashboardMoney(value?: string | null, currency = 'USD') {
  return formatCurrency(value, currency, 2, formatCurrency('0', currency))
}

export function quoteStatusText(status: RecentQuote['status'], t: Translate) {
  if (status === 'CONFIRMED') return t('dashboard.sales.status.quoteConfirmed')
  if (status === 'VOID') return t('dashboard.sales.status.quoteVoid')
  return t('dashboard.sales.status.quoteDraft')
}

export function quoteStatusTone(status: RecentQuote['status']) {
  return status === 'CONFIRMED' ? 'success' : status === 'VOID' ? 'info' : 'warning'
}

export function quoteConversionText(quote: RecentQuote, t: Translate) {
  return quote.salesDocumentId ? t('dashboard.sales.table.converted') : t('dashboard.sales.table.unconverted')
}

export function sourceTypeText(sourceType: RecentOrder['sourceType'], t: Translate) {
  return sourceType === 'QUICK_ORDER' ? t('dashboard.sales.source.quickOrder') : t('dashboard.sales.source.quote')
}

export function orderStatusText(status: RecentOrder['documentStatus'], t: Translate) {
  if (status === 'COMPLETED') return t('dashboard.sales.status.completed')
  if (status === 'CANCELLED') return t('dashboard.sales.status.cancelled')
  return t('dashboard.sales.status.submitted')
}

export function orderStatusTone(status: RecentOrder['documentStatus']) {
  if (status === 'COMPLETED') return 'success'
  if (status === 'CANCELLED') return 'danger'
  return 'primary'
}

export function paymentStatusText(status: RecentOrder['paymentStatus'], t: Translate) {
  return status === 'PAID' ? t('dashboard.sales.status.paid') : t('dashboard.sales.status.unpaid')
}

export function paymentStatusTone(status: RecentOrder['paymentStatus']) {
  return status === 'PAID' ? 'success' : 'warning'
}

export function productionStatusText(status: RecentOrder['productionStatus'], t: Translate) {
  if (status === 'COMPLETED') return t('dashboard.sales.status.productionCompleted')
  if (status === 'IN_PRODUCTION') return t('dashboard.sales.status.inProduction')
  return t('dashboard.sales.status.productionPending')
}

export function productionStatusTone(status: RecentOrder['productionStatus']) {
  if (status === 'COMPLETED') return 'success'
  if (status === 'IN_PRODUCTION') return 'primary'
  return 'info'
}

export function shipmentStatusText(status: RecentOrder['shipmentStatus'], t: Translate) {
  if (status === 'DELIVERED') return t('dashboard.sales.status.delivered')
  if (status === 'SHIPPED') return t('dashboard.sales.status.shipped')
  if (status === 'PARTIALLY_SHIPPED') return t('dashboard.sales.status.partiallyShipped')
  return t('dashboard.sales.status.unshipped')
}

export function shipmentStatusTone(status: RecentOrder['shipmentStatus']) {
  if (status === 'DELIVERED' || status === 'SHIPPED') return 'success'
  if (status === 'PARTIALLY_SHIPPED') return 'warning'
  return 'info'
}

function money(value?: string | null, currency = 'USD') {
  return formatDashboardMoney(value, currency)
}
