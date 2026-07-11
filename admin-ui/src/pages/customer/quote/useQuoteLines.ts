import type { Ref } from 'vue'
import type { QuoteWorkbenchItem } from './quoteWorkbenchTypes'

export function useQuoteLines(rows: Ref<QuoteWorkbenchItem[]>) {
  function addLine() {
    const row: QuoteWorkbenchItem = {
      clientId: crypto.randomUUID(), quantity: 1, selectedOptionValues: {}, calculationStatus: 'PENDING', sortOrder: (rows.value.length + 1) * 10
    }
    rows.value.push(row)
    return row
  }

  function duplicateLine(source: QuoteWorkbenchItem) {
    const row: QuoteWorkbenchItem = {
      ...source, quoteItemId: undefined, clientId: crypto.randomUUID(), lineNo: undefined,
      selectedOptionValues: { ...source.selectedOptionValues }, calculationStatus: 'PENDING', calculationMessage: undefined,
      unitAmount: undefined, productAmount: undefined, unitShippingAmount: undefined, shippingAmount: undefined, lineAmount: undefined
    }
    rows.value.push(row)
    return row
  }

  function duplicateLines(source: QuoteWorkbenchItem[]) {
    return source.map(duplicateLine)
  }

  function removeLine(row: QuoteWorkbenchItem) {
    rows.value = rows.value.filter((item) => item.clientId !== row.clientId)
  }

  function removeLines(selected: QuoteWorkbenchItem[]) {
    const ids = new Set(selected.map((item) => item.clientId))
    rows.value = rows.value.filter((item) => !ids.has(item.clientId))
  }

  function addLines(values: Array<Pick<QuoteWorkbenchItem, 'roomLocation' | 'orderWidthInch' | 'orderHeightInch' | 'quantity'>>) {
    return values.map((value) => Object.assign(addLine(), value))
  }

  return { addLine, duplicateLine, duplicateLines, removeLine, removeLines, addLines }
}
