import { ref, type Ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { CustomerQuote } from '@/api/customer/quote'
import { customerQuoteApi } from '@/api/customer/quote'
import type { QuoteWorkbenchItem } from './quoteWorkbenchTypes'
import { applyCalculatedItem, calculateQuoteTotals, quotePayload } from './quoteWorkbenchValues'

export function useQuoteWorkbenchActions(ctx: {
  quote: CustomerQuote
  rows: Ref<QuoteWorkbenchItem[]>
  validateHeader: () => Promise<boolean | undefined>
  reload: (id: string) => Promise<void>
  t: (key: string) => string
}) {
  const saving = ref(false)
  const calculatingId = ref('')

  async function save() {
    if (!await ctx.validateHeader()) return undefined
    saving.value = true
    try {
      const payload = quotePayload(ctx.quote, ctx.rows.value)
      if (ctx.quote.quoteId) await customerQuoteApi.update(payload)
      else ctx.quote.quoteId = String((await customerQuoteApi.add(payload)).data || '')
      if (ctx.quote.quoteId) await ctx.reload(ctx.quote.quoteId)
      ElMessage.success(ctx.t('common.operationSuccess'))
      return ctx.quote.quoteId
    } finally { saving.value = false }
  }

  async function calculateItem(row: QuoteWorkbenchItem) {
    calculatingId.value = row.clientId
    try {
      const response = await customerQuoteApi.calculateItem(row, ctx.quote.quoteLanguage)
      applyCalculatedItem(row, response.data || { selectedOptionValues: {} })
      Object.assign(ctx.quote, calculateQuoteTotals(ctx.rows.value))
      ElMessage.success(ctx.t('customer.quote.calculation.pass'))
    } finally { calculatingId.value = '' }
  }

  async function calculateAll() {
    const id = await save()
    if (!id) return false
    const response = await customerQuoteApi.calculateAll(id)
    await ctx.reload(id)
    const passed = (response.data?.items || []).every((row) => row.calculationStatus === 'PASS')
    ElMessage[passed ? 'success' : 'warning'](ctx.t(passed ? 'customer.quote.calculation.pass' : 'customer.quote.calculation.required'))
    return passed
  }

  async function confirm() {
    if (!await calculateAll() || !ctx.quote.quoteId) return
    await ElMessageBox.confirm(ctx.t('customer.quote.confirmHint'), ctx.t('common.prompt'), { type: 'warning' })
    await customerQuoteApi.confirm(ctx.quote.quoteId)
    await ctx.reload(ctx.quote.quoteId)
    ElMessage.success(ctx.t('common.operationSuccess'))
  }

  async function voidQuote() {
    if (!ctx.quote.quoteId) return
    await ElMessageBox.confirm(ctx.t('customer.quote.action.void'), ctx.t('common.prompt'), { type: 'warning' })
    await customerQuoteApi.void(ctx.quote.quoteId)
    await ctx.reload(ctx.quote.quoteId)
    ElMessage.success(ctx.t('common.operationSuccess'))
  }

  return { saving, calculatingId, save, calculateItem, calculateAll, confirm, voidQuote }
}
