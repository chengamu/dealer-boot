import type { CustomerQuote, CustomerQuoteItem } from '@/api/customer/quote'
import type { QuoteWorkbenchItem } from './quoteWorkbenchTypes'
import { sumDecimals } from '@/utils/businessNumber'
import { toQuoteDecimal, toQuoteId } from './quoteValueNormalization'

export function normalizeLoadedQuote(quote: CustomerQuote): CustomerQuote {
  return {
    ...quote,
    quoteId: toQuoteId(quote.quoteId),
    customerId: toQuoteId(quote.customerId),
    ownerUserId: toQuoteId(quote.ownerUserId),
    productAmount: toQuoteDecimal(quote.productAmount),
    shippingAmount: toQuoteDecimal(quote.shippingAmount),
    totalAmount: toQuoteDecimal(quote.totalAmount)
  }
}

export function normalizeLoadedItem(row: CustomerQuoteItem): QuoteWorkbenchItem {
  return {
    ...row,
    quoteItemId: toQuoteId(row.quoteItemId),
    quoteId: toQuoteId(row.quoteId),
    saleProductId: toQuoteId(row.saleProductId),
    formulaId: toQuoteId(row.formulaId),
    formulaVersionId: toQuoteId(row.formulaVersionId),
    clientId: crypto.randomUUID(),
    orderWidthInch: toQuoteDecimal(row.orderWidthInch),
    orderHeightInch: toQuoteDecimal(row.orderHeightInch),
    quantity: Number(row.quantity) || 1,
    unitAmount: toQuoteDecimal(row.unitAmount),
    productAmount: toQuoteDecimal(row.productAmount),
    unitShippingAmount: toQuoteDecimal(row.unitShippingAmount),
    shippingAmount: toQuoteDecimal(row.shippingAmount),
    lineAmount: toQuoteDecimal(row.lineAmount),
    selectedOptionValues: { ...(row.selectedOptionValues || {}) }
  }
}

export function applyCalculatedItem(row: QuoteWorkbenchItem, calculated: CustomerQuoteItem) {
  const { selectedOptionValues: _selectedOptionValues, ...fields } = calculated
  Object.assign(row, fields, {
    unitAmount: toQuoteDecimal(fields.unitAmount),
    productAmount: toQuoteDecimal(fields.productAmount),
    unitShippingAmount: toQuoteDecimal(fields.unitShippingAmount),
    shippingAmount: toQuoteDecimal(fields.shippingAmount),
    lineAmount: toQuoteDecimal(fields.lineAmount)
  })
}

export function calculateQuoteTotals(rows: QuoteWorkbenchItem[]) {
  const productAmount = sumDecimals(rows.map((row) => row.productAmount))
  const shippingAmount = sumDecimals(rows.map((row) => row.shippingAmount))
  return { productAmount, shippingAmount, totalAmount: sumDecimals([productAmount, shippingAmount]) }
}

export function quotePayload(quote: CustomerQuote, rows: QuoteWorkbenchItem[]): CustomerQuote {
  return {
    ...quote,
    items: rows.map(({ clientId: _clientId, ...row }) => ({
      ...row,
      selectedOptionValues: { ...row.selectedOptionValues }
    }))
  }
}
