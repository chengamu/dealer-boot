import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import type { AdminTableAction } from '@/components/AdminTableActions/index.vue'
import { listCustomerOptions, type CustomerProfile } from '@/api/customer/profile'
import { customerQuoteApi, platformCustomerQuoteApi, type CustomerQuote, type CustomerQuoteQuery } from '@/api/customer/quote'
import { listMerchantProfileOptions, type MerchantProfileOption } from '@/api/merchant/profile'
import { download } from '@/utils/request'
import { withUtcDateRange } from '@/utils/datetime'
import { listSalesStoreOptions, type SalesStoreOption } from '@/api/dealer-sales'
import { buildQuoteRowActions } from '@/pages/customer/quote/quoteListPresentation'
import { openQuotePdf } from '@/pages/customer/quote/quoteArtifacts'
import { useStandardGridDateRange } from '@/pages/dealer-sales/components/standard-grid/useStandardGridDateRange'

function openBlob(blob: Blob, print = false) {
  const url = URL.createObjectURL(blob)
  const target = window.open(url, '_blank', 'noopener')
  if (print) {
    target?.addEventListener('load', () => target.print(), { once: true })
  }
  window.setTimeout(() => URL.revokeObjectURL(url), 60_000)
}

function createDefaultQuery(): CustomerQuoteQuery {
  return {
    pageNum: 1,
    pageSize: 10,
    businessOrigin: '',
    customerId: '',
    ownerUserId: '',
    quoteNo: '',
    salesStoreId: '',
    status: '',
    tenantId: ''
  }
}

export function useQuoteListPage(platform: boolean) {
  const { t } = useI18n()
  const router = useRouter()
  const query = reactive<CustomerQuoteQuery>(createDefaultQuery())
  const { dateRange, resetDateRange } = useStandardGridDateRange()
  const rows = ref<CustomerQuote[]>([])
  const total = ref(0)
  const loading = ref(false)
  const showSearch = ref(true)
  const customers = ref<CustomerProfile[]>([])
  const merchantOptions = ref<MerchantProfileOption[]>([])
  const salesStoreOptions = ref<SalesStoreOption[]>([])
  const detailOpen = ref(false)
  const detailLoading = ref(false)
  const detailQuote = ref<CustomerQuote>()
  const emailOpener = ref<(row: CustomerQuote) => void>()
  const convertOpener = ref<(row: CustomerQuote) => void>()

  const statusOptions = computed(() => [
    { value: 'DRAFT', label: t('customer.quote.status.draft') },
    { value: 'CONFIRMED', label: t('customer.quote.status.confirmed') },
    { value: 'VOID', label: t('customer.quote.status.void') }
  ])

  async function loadOptions() {
    if (platform) {
      const [merchantRes, storeRes] = await Promise.all([
        listMerchantProfileOptions(),
        listSalesStoreOptions()
      ])
      merchantOptions.value = merchantRes.data || []
      salesStoreOptions.value = storeRes.data || []
      return
    }
    const customerRes = await listCustomerOptions({ status: 'ENABLED', pageNum: 1, pageSize: 500 })
    customers.value = customerRes.data || []
  }

  async function getList() {
    loading.value = true
    try {
      const params = withUtcDateRange({ ...query }, dateRange.value, 'beginCreateTime', 'endCreateTime')
      const response = platform ? await platformCustomerQuoteApi.list(params) : await customerQuoteApi.list(params)
      rows.value = response.rows || []
      total.value = response.total || 0
    } finally {
      loading.value = false
    }
  }

  function handleQuery() {
    query.pageNum = 1
    void getList()
  }

  function resetQuery() {
    Object.assign(query, createDefaultQuery())
    resetDateRange()
    handleQuery()
  }

  function openWorkbench(row?: CustomerQuote) {
    void router.push({ name: 'CustomerQuoteWorkbench', query: row?.quoteId ? { quoteId: row.quoteId } : {} })
  }

  async function openDetail(row: CustomerQuote) {
    if (!row.quoteId) return
    detailOpen.value = true
    detailLoading.value = true
    try {
      const response = platform ? await platformCustomerQuoteApi.get(row.quoteId) : await customerQuoteApi.get(row.quoteId)
      detailQuote.value = response.data
    } finally {
      detailLoading.value = false
    }
  }

  async function handleDelete(row: CustomerQuote) {
    if (!row.quoteId) return
    await ElMessageBox.confirm(t('common.deleteConfirm'), t('common.prompt'), { type: 'warning' })
    await customerQuoteApi.remove(row.quoteId)
    ElMessage.success(t('common.deleteSuccess'))
    await getList()
  }

  async function handleCopy(row: CustomerQuote) {
    if (!row.quoteId) return
    const response = await customerQuoteApi.copy(row.quoteId)
    await router.push({ name: 'CustomerQuoteWorkbench', query: { quoteId: response.data } })
  }

  async function handleVoid(row: CustomerQuote) {
    if (!row.quoteId) return
    await ElMessageBox.confirm(t('customer.quote.voidHint'), t('common.prompt'), { type: 'warning' })
    await customerQuoteApi.void(row.quoteId)
    ElMessage.success(t('common.operationSuccess'))
    await getList()
  }

  function exportQuote(row: CustomerQuote) {
    if (!row.quoteId) return
    if (platform) {
      download(`/platform-sales/quotes/${row.quoteId}/export`, {}, `quote_${row.quoteNo || Date.now()}.xlsx`)
      return
    }
    download(`customer/quotes/${row.quoteId}/export`, {}, `quote_${row.quoteNo || Date.now()}.xlsx`)
  }

  async function openPdf(row: CustomerQuote, print = false) {
    if (!row.quoteId) return
    if (!platform) {
      await openQuotePdf(row, print)
      return
    }
    openBlob(await platformCustomerQuoteApi.pdf(row.quoteId), print)
  }

  function viewOrder(row: CustomerQuote) {
    if (row.salesDocumentId) void router.push({ name: 'SalesDocumentDetail', params: { id: row.salesDocumentId } })
  }

  function bindBusinessDialogs(openers: { email?: (row: CustomerQuote) => void; convert?: (row: CustomerQuote) => void }) {
    emailOpener.value = openers.email
    convertOpener.value = openers.convert
  }

  function buildActions(row: CustomerQuote): AdminTableAction[] {
    if (!platform) {
      return buildQuoteRowActions(row, t, {
        open: openWorkbench,
        copy: handleCopy,
        export: exportQuote,
        pdf: openPdf,
        email: (target) => emailOpener.value?.(target),
        convert: (target) => convertOpener.value?.(target),
        viewOrder,
        void: handleVoid,
        remove: handleDelete
      }) as AdminTableAction[]
    }
    const confirmed = row.status === 'CONFIRMED'
    return [
      { label: t('common.detail'), icon: 'View', permission: 'platform:sales:quote:query', primary: true, onClick: () => openDetail(row) },
      { label: t('customer.quote.action.export'), icon: 'Download', permission: 'platform:sales:quote:export', hidden: !confirmed, onClick: () => exportQuote(row) },
      { label: t('customer.quote.action.pdf'), icon: 'Document', permission: 'platform:sales:quote:document', hidden: !confirmed, onClick: () => openPdf(row) },
      { label: t('customer.quote.action.print'), icon: 'Printer', permission: 'platform:sales:quote:document', hidden: !confirmed, onClick: () => openPdf(row, true) },
      { label: t('customer.quote.action.viewOrder'), icon: 'Tickets', permission: 'platform:sales:order:query', hidden: !row.salesDocumentId, onClick: () => viewOrder(row) }
    ]
  }

  void Promise.all([loadOptions(), getList()])

  return {
    buildActions,
    bindBusinessDialogs,
    customers,
    dateRange,
    detailLoading,
    detailOpen,
    detailQuote,
    getList,
    handleQuery,
    merchantOptions,
    loading,
    openWorkbench,
    platform,
    query,
    resetQuery,
    rows,
    salesStoreOptions,
    showSearch,
    statusOptions,
    total
  }
}
