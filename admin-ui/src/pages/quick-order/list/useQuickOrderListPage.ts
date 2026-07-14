import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import type { AdminTableAction } from '@/components/AdminTableActions/index.vue'
import auth from '@/plugins/auth'
import { listMerchantProfileOptions, type MerchantProfileOption } from '@/api/merchant/profile'
import { listSalesStoreOptions, type SalesStoreOption } from '@/api/dealer-sales'
import { platformQuickOrderApi, quickOrderApi, type QuickOrder, type QuickOrderQuery } from '@/api/dealer-sales/quick-order'
import { useStandardGridDateRange } from '@/pages/dealer-sales/components/standard-grid/useStandardGridDateRange'
import { usePermissionStore } from '@/stores/permission'
import { withUtcDateRange } from '@/utils/datetime'
import { quickOrderRouteComponents, resolveRoutePath } from '@/pages/quick-order/quickOrderRoutes'

function createDefaultQuery(): QuickOrderQuery {
  return {
    pageNum: 1,
    pageSize: 10,
    businessOrigin: '',
    customerName: '',
    ownerUserId: '',
    quickOrderNo: '',
    salesStoreId: '',
    status: '',
    tenantId: ''
  }
}

export function useQuickOrderListPage(platform: boolean) {
  const { t } = useI18n()
  const router = useRouter()
  const permissionStore = usePermissionStore()
  const query = reactive<QuickOrderQuery>(createDefaultQuery())
  const { dateRange, resetDateRange } = useStandardGridDateRange()
  const rows = ref<QuickOrder[]>([])
  const total = ref(0)
  const loading = ref(false)
  const showSearch = ref(true)
  const merchantOptions = ref<MerchantProfileOption[]>([])
  const salesStoreOptions = ref<SalesStoreOption[]>([])
  const detailOpen = ref(false)
  const detailLoading = ref(false)
  const detailOrder = ref<QuickOrder>()

  const workbenchPath = computed(() => resolveRoutePath(permissionStore.routers, quickOrderRouteComponents.workbench))

  async function loadOptions() {
    if (!platform) return
    const [merchantRes, storeRes] = await Promise.all([
      listMerchantProfileOptions(),
      listSalesStoreOptions()
    ])
    merchantOptions.value = merchantRes.data || []
    salesStoreOptions.value = storeRes.data || []
  }

  async function getList() {
    loading.value = true
    try {
      const params = withUtcDateRange({ ...query }, dateRange.value, 'beginUpdateTime', 'endUpdateTime')
      const response = platform ? await platformQuickOrderApi.list(params) : await quickOrderApi.list(params)
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

  function openWorkbench(row?: Partial<QuickOrder>) {
    void router.push({ path: workbenchPath.value, query: row?.quickOrderId ? { quickOrderId: row.quickOrderId } : undefined })
  }

  async function openDetail(row: QuickOrder) {
    if (!row.quickOrderId) return
    detailOpen.value = true
    detailLoading.value = true
    try {
      const response = platform ? await platformQuickOrderApi.get(row.quickOrderId) : await quickOrderApi.get(row.quickOrderId)
      detailOrder.value = response.data
    } finally {
      detailLoading.value = false
    }
  }

  async function copyDraft(row: QuickOrder) {
    const quickOrderId = String((await quickOrderApi.copy(String(row.quickOrderId || ''))).data || '')
    ElMessage.success(t('common.operationSuccess'))
    openWorkbench({ quickOrderId })
  }

  async function removeDraft(row: QuickOrder) {
    await ElMessageBox.confirm(t('common.deleteConfirm'), t('common.prompt'), { type: 'warning' })
    await quickOrderApi.remove(String(row.quickOrderId || ''))
    ElMessage.success(t('common.operationSuccess'))
    await getList()
  }

  function viewOrder(row: QuickOrder) {
    if (!row.salesDocumentId) return
    void router.push({ name: 'SalesDocumentDetail', params: { id: row.salesDocumentId } })
  }

  function buildActions(row: QuickOrder): AdminTableAction[] {
    if (platform) {
      return [
        { label: t('common.detail'), icon: 'View', permission: 'platform:sales:quick-order:query', primary: true, onClick: () => openDetail(row) },
        { label: t('customer.quote.action.viewOrder'), icon: 'Tickets', permission: 'platform:sales:order:query', hidden: !row.salesDocumentId, onClick: () => viewOrder(row) }
      ]
    }
    const draft = row.status !== 'ORDERED'
    return [
      { label: t('dealer.quickOrder.continue'), hidden: !draft || !auth.hasPermi('dealer:quick-order:edit'), onClick: () => openWorkbench(row) },
      { label: t('common.copy'), hidden: !auth.hasPermi('dealer:quick-order:add'), onClick: () => copyDraft(row) },
      { label: t('common.delete'), type: 'danger', hidden: !draft || !auth.hasPermi('dealer:quick-order:remove'), onClick: () => removeDraft(row) },
      { label: t('customer.quote.action.viewOrder'), hidden: !row.salesDocumentId || !auth.hasPermi('dealer:sales:query'), onClick: () => viewOrder(row) }
    ]
  }

  void Promise.all([loadOptions(), getList()])

  return {
    buildActions,
    dateRange,
    detailLoading,
    detailOpen,
    detailOrder,
    getList,
    handleQuery,
    loading,
    merchantOptions,
    openWorkbench,
    query,
    resetQuery,
    rows,
    salesStoreOptions,
    showSearch,
    total
  }
}
