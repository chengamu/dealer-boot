import type { QuoteLanguage } from '@/api/customer/quote'
import type { QuickOrder, QuickOrderItem } from '@/api/dealer-sales/quick-order'
import { canonicalDecimal, formatInch, sumDecimals } from '@/utils/businessNumber'

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
    productAmount: '0',
    shippingAmount: '0',
    totalAmount: '0',
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
    totalAmount: toDecimal(order.totalAmount),
    productAmount: toDecimal(order.productAmount),
    shippingAmount: toDecimal(order.shippingAmount)
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
    orderWidthInch: toDecimal(item.orderWidthInch),
    orderHeightInch: toDecimal(item.orderHeightInch),
    quantity: Math.max(1, Number(item.quantity) || 1),
    listUnitAmount: toDecimal(item.listUnitAmount),
    listAmount: toDecimal(item.listAmount),
    discountRate: toDecimal(item.discountRate),
    discountAmount: toDecimal(item.discountAmount),
    unitAmount: toDecimal(item.unitAmount),
    productAmount: toDecimal(item.productAmount),
    unitShippingAmount: toDecimal(item.unitShippingAmount),
    shippingAmount: toDecimal(item.shippingAmount),
    lineAmount: toDecimal(item.lineAmount),
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
  const productAmount = sumDecimals(rows.map((row) => row.productAmount))
  const shippingAmount = sumDecimals(rows.map((row) => row.shippingAmount))
  const totalAmount = sumDecimals([productAmount, shippingAmount])
  const itemCount = rows.length
  const totalQuantity = rows.reduce((sum, row) => sum + Math.max(0, Number(row.quantity) || 0), 0)
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

function toDecimal(value?: string | number | null) {
  return canonicalDecimal(value) ?? undefined
}
