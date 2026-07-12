import type { QuoteLanguage } from '@/api/customer/quote'
import type { QuickOrder, QuickOrderItem } from '@/api/dealer-sales/quick-order'
import { formatInch } from '@/utils/businessNumber'

export interface QuickOrderWorkbenchItem extends QuickOrderItem {
  clientId: string
}

export function emptyQuickOrder(): QuickOrder {
  return {
    customerId: '',
    customerName: '',
    recipientName: '',
    recipientPhone: '',
    shippingAddress: '',
    customerPoNo: '',
    currencyCode: 'USD',
    productAmount: 0,
    shippingAmount: 0,
    totalAmount: 0,
    items: []
  }
}

export function emptyQuickOrderItem(): QuickOrderWorkbenchItem {
  return {
    clientId: crypto.randomUUID(),
    roomLocation: '',
    saleProductId: '',
    quantity: 1,
    selectedOptionValues: {},
    calculationStatus: 'PENDING'
  }
}

export function normalizeQuickOrder(order: QuickOrder): QuickOrder {
  return {
    ...emptyQuickOrder(),
    ...order,
    customerId: stringify(order.customerId),
    salesDocumentId: stringify(order.salesDocumentId),
    totalAmount: toNumber(order.totalAmount),
    productAmount: toNumber(order.productAmount),
    shippingAmount: toNumber(order.shippingAmount)
  }
}

export function normalizeQuickOrderItem(item: QuickOrderItem): QuickOrderWorkbenchItem {
  return {
    ...emptyQuickOrderItem(),
    ...item,
    clientId: crypto.randomUUID(),
    quickOrderItemId: stringify(item.quickOrderItemId),
    quickOrderId: stringify(item.quickOrderId),
    saleProductId: stringify(item.saleProductId),
    formulaId: stringify(item.formulaId),
    formulaVersionId: stringify(item.formulaVersionId),
    orderWidthInch: toNumber(item.orderWidthInch),
    orderHeightInch: toNumber(item.orderHeightInch),
    quantity: Math.max(1, toNumber(item.quantity) || 1),
    listUnitAmount: toNumber(item.listUnitAmount),
    listAmount: toNumber(item.listAmount),
    discountRate: toNumber(item.discountRate),
    discountAmount: toNumber(item.discountAmount),
    unitAmount: toNumber(item.unitAmount),
    productAmount: toNumber(item.productAmount),
    unitShippingAmount: toNumber(item.unitShippingAmount),
    shippingAmount: toNumber(item.shippingAmount),
    lineAmount: toNumber(item.lineAmount),
    selectedOptionValues: { ...(item.selectedOptionValues || {}) }
  }
}

export function toQuickOrderPayload(order: QuickOrder, rows: QuickOrderWorkbenchItem[]): QuickOrder {
  return {
    ...order,
    customerId: order.customerId || undefined,
    items: rows.map(({ clientId: _clientId, ...row }) => ({
      ...row,
      selectedOptionValues: { ...row.selectedOptionValues }
    }))
  }
}

export function syncQuickOrderTotals(order: QuickOrder, rows: QuickOrderWorkbenchItem[]) {
  const productAmount = rows.reduce((sum, row) => sum + (toNumber(row.productAmount) || 0), 0)
  const shippingAmount = rows.reduce((sum, row) => sum + (toNumber(row.shippingAmount) || 0), 0)
  const totalAmount = productAmount + shippingAmount
  const itemCount = rows.length
  const totalQuantity = rows.reduce((sum, row) => sum + Math.max(0, toNumber(row.quantity) || 0), 0)
  Object.assign(order, { productAmount, shippingAmount, totalAmount, itemCount, totalQuantity })
}

export function quickOrderSummary(item: QuickOrderItem, language: QuoteLanguage) {
  return language === 'EN_US' ? item.selectedOptionsSummaryEn : item.selectedOptionsSummaryCn
}

export function quickOrderSize(item: QuickOrderItem) {
  return `${formatInch(item.orderWidthInch)} × ${formatInch(item.orderHeightInch)}`
}

export function localeQuoteLanguage(locale: string): QuoteLanguage {
  return locale.toLowerCase().startsWith('zh') ? 'ZH_CN' : 'EN_US'
}

function stringify(value?: string | number | null) {
  return value === null || value === undefined || value === '' ? '' : String(value)
}

function toNumber(value?: string | number | null) {
  if (value === null || value === undefined || value === '') return undefined
  const next = Number(value)
  return Number.isFinite(next) ? next : undefined
}
