import type { CustomerQuote, CustomerQuoteItem } from '@/api/customer/quote'
import type { QuoteWorkbenchItem } from './quoteWorkbenchTypes'
import { toQuoteId, toQuoteNumber } from './quoteValueNormalization'

export function normalizeLoadedQuote(quote: CustomerQuote): CustomerQuote {
  return {
    ...quote,
    quoteId: toQuoteId(quote.quoteId),
    customerId: toQuoteId(quote.customerId),
    ownerUserId: toQuoteId(quote.ownerUserId),
    productAmount: toQuoteNumber(quote.productAmount),
    shippingAmount: toQuoteNumber(quote.shippingAmount),
    totalAmount: toQuoteNumber(quote.totalAmount)
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
    orderWidthInch: toQuoteNumber(row.orderWidthInch),
    orderHeightInch: toQuoteNumber(row.orderHeightInch),
    quantity: toQuoteNumber(row.quantity) || 1,
    unitAmount: toQuoteNumber(row.unitAmount),
    productAmount: toQuoteNumber(row.productAmount),
    unitShippingAmount: toQuoteNumber(row.unitShippingAmount),
    shippingAmount: toQuoteNumber(row.shippingAmount),
    lineAmount: toQuoteNumber(row.lineAmount),
    selectedOptionValues: { ...(row.selectedOptionValues || {}) }
  }
}

export function applyCalculatedItem(row: QuoteWorkbenchItem, calculated: CustomerQuoteItem) {
  const { selectedOptionValues: _selectedOptionValues, ...fields } = calculated
  Object.assign(row, fields, {
    unitAmount: toQuoteNumber(fields.unitAmount),
    productAmount: toQuoteNumber(fields.productAmount),
    unitShippingAmount: toQuoteNumber(fields.unitShippingAmount),
    shippingAmount: toQuoteNumber(fields.shippingAmount),
    lineAmount: toQuoteNumber(fields.lineAmount)
  })
}

export function calculateQuoteTotals(rows: QuoteWorkbenchItem[]) {
  const productAmount = rows.reduce((total, row) => total + (toQuoteNumber(row.productAmount) || 0), 0)
  const shippingAmount = rows.reduce((total, row) => total + (toQuoteNumber(row.shippingAmount) || 0), 0)
  return { productAmount, shippingAmount, totalAmount: productAmount + shippingAmount }
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
