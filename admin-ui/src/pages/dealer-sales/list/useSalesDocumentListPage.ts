import { reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import type { AdminTableAction } from '@/components/AdminTableActions/index.vue'
import { listMerchantProfileOptions, type MerchantProfileOption } from '@/api/merchant/profile'
import { listSalesStoreOptions, platformSalesApi, salesApi, type SalesDocument, type SalesQuery, type SalesStoreOption } from '@/api/dealer-sales'
import { withUtcDateRange } from '@/utils/datetime'
import { useStandardGridDateRange } from '@/pages/dealer-sales/components/standard-grid/useStandardGridDateRange'

function createDefaultQuery(): SalesQuery {
  return {
    pageNum: 1,
    pageSize: 10,
    businessOrigin: '',
    customerName: '',
    documentStatus: '',
    merchantName: '',
    orderNo: '',
    ownerUserId: '',
    paymentStatus: '',
    productionStatus: '',
    salesStoreId: '',
    shipmentStatus: '',
    sourceNo: '',
    sourceType: '',
    tenantId: ''
  }
}

export function useSalesDocumentListPage(platform: boolean) {
  const { t } = useI18n()
  const router = useRouter()
  const query = reactive<SalesQuery>(createDefaultQuery())
  const { dateRange, resetDateRange } = useStandardGridDateRange()
  const rows = ref<SalesDocument[]>([])
  const total = ref(0)
  const loading = ref(false)
  const showSearch = ref(true)
  const merchantOptions = ref<MerchantProfileOption[]>([])
  const salesStoreOptions = ref<SalesStoreOption[]>([])

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
      const params = withUtcDateRange({ ...query }, dateRange.value, 'beginSubmittedTime', 'endSubmittedTime')
      const response = platform ? await platformSalesApi.list(params) : await salesApi.list(params)
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

  function openDetail(row: SalesDocument) {
    if (!row.salesDocumentId) return
    void router.push({ name: 'SalesDocumentDetail', params: { id: row.salesDocumentId } })
  }

  function buildActions(row: SalesDocument): AdminTableAction[] {
    return [
      {
        label: t('common.detail'),
        icon: 'View',
        permission: platform ? 'platform:sales:order:query' : 'dealer:sales:query',
        primary: true,
        onClick: () => openDetail(row)
      }
    ]
  }

  void Promise.all([loadOptions(), getList()])

  return {
    buildActions,
    dateRange,
    getList,
    handleQuery,
    loading,
    merchantOptions,
    query,
    resetQuery,
    rows,
    salesStoreOptions,
    showSearch,
    total
  }
}
