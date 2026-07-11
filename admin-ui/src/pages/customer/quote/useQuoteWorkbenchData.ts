import { reactive, ref } from 'vue'
import { customerQuoteApi, customerQuoteCatalogApi, type CustomerQuote } from '@/api/customer/quote'
import { listCustomerOptions, listCustomerOwnerOptions, type CustomerOwnerOption, type CustomerProfile } from '@/api/customer/profile'
import type { SaleProductVO } from '@/api/product-pricing/types'
import { applyQuoteDefaults } from './quoteOptionGroups'
import type { QuoteSetupMap, QuoteWorkbenchItem } from './quoteWorkbenchTypes'
import { normalizeLoadedItem, normalizeLoadedQuote } from './quoteWorkbenchValues'
import { useQuoteLines } from './useQuoteLines'

export function useQuoteWorkbenchData() {
  const future = new Date(Date.now() + 14 * 86400000).toISOString().slice(0, 10)
  const quote = reactive<CustomerQuote>({ quoteLanguage: 'EN_US', validUntil: future, items: [] })
  const rows = ref<QuoteWorkbenchItem[]>([])
  const customers = ref<CustomerProfile[]>([])
  const owners = ref<CustomerOwnerOption[]>([])
  const saleProducts = ref<SaleProductVO[]>([])
  const setups = reactive<QuoteSetupMap>({})
  const loading = ref(false)
  const loadingProductId = ref('')
  const lineActions = useQuoteLines(rows)

  async function loadDependencies() {
    const [customerRes, ownerRes, productRes] = await Promise.all([
      listCustomerOptions({ status: 'ENABLED', pageNum: 1, pageSize: 500 }),
      listCustomerOwnerOptions(),
      customerQuoteCatalogApi.products({ status: 'ENABLED', priceStatus: 'READY', pageNum: 1, pageSize: 500 })
    ])
    customers.value = customerRes.data || []
    owners.value = ownerRes.data || []
    saleProducts.value = productRes.data || []
  }

  async function loadQuote(id?: string) {
    if (!id) return
    loading.value = true
    try {
      const response = await customerQuoteApi.get(id)
      const loaded = response.data || ({} as CustomerQuote)
      Object.assign(quote, normalizeLoadedQuote(loaded))
      rows.value = (loaded.items || []).map(normalizeLoadedItem)
    } finally { loading.value = false }
  }

  async function loadSetup(row: QuoteWorkbenchItem) {
    const id = String(row.saleProductId || '')
    if (!id || setups[id]) return
    loadingProductId.value = id
    try {
      const response = await customerQuoteCatalogApi.setup(id)
      setups[id] = response.data || {}
      applyQuoteDefaults(setups[id] || {}, row.selectedOptionValues)
    } finally { loadingProductId.value = '' }
  }

  return { quote, rows, customers, owners, saleProducts, setups, loading, loadingProductId, loadDependencies, loadQuote, loadSetup, ...lineActions }
}
