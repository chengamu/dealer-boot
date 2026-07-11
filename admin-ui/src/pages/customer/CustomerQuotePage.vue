<template>
  <div class="app-container merchant-table-page">
    <el-form v-show="showSearch" ref="queryRef" :model="query" :inline="true" class="merchant-table-page__search">
      <el-form-item :label="t('customer.quote.no')" prop="quoteNo"><el-input v-model="query.quoteNo" clearable @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item :label="t('customer.quote.project')" prop="projectName"><el-input v-model="query.projectName" clearable @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item :label="t('customer.quote.customer')" prop="customerId">
        <el-select v-model="query.customerId" clearable filterable style="width: 190px">
          <el-option v-for="item in customers" :key="item.customerId" :label="item.companyName || item.customerName" :value="String(item.customerId)" />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('common.status')" prop="status">
        <el-select v-model="query.status" clearable style="width: 140px">
          <el-option v-for="item in statuses" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">{{ t('common.search') }}</el-button>
        <el-button icon="Refresh" @click="resetQuery">{{ t('common.reset') }}</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8 merchant-table-page__toolbar">
      <el-button type="primary" icon="Plus" v-hasPermi="['customer:quote:add']" @click="openWorkbench()">{{ t('common.add') }}</el-button>
      <el-button plain icon="Edit" :disabled="selected?.status !== 'DRAFT'" v-hasPermi="['customer:quote:edit']" @click="openWorkbench(selected)">{{ t('common.edit') }}</el-button>
      <el-button type="danger" plain icon="Delete" :disabled="selected?.status !== 'DRAFT'" v-hasPermi="['customer:quote:remove']" @click="handleDelete(selected)">{{ t('common.delete') }}</el-button>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="rows" border highlight-current-row class="merchant-table-page__table" @current-change="selected = $event">
      <el-table-column type="index" :label="t('common.index')" width="70" align="center" />
      <el-table-column :label="t('customer.quote.no')" prop="quoteNo" width="190" show-overflow-tooltip />
      <el-table-column :label="t('customer.quote.customer')" min-width="170" show-overflow-tooltip>
        <template #default="{ row }">{{ row.companyName || row.customerName }}</template>
      </el-table-column>
      <el-table-column :label="t('customer.quote.project')" prop="projectName" min-width="190" show-overflow-tooltip />
      <el-table-column :label="t('customer.quote.language')" width="100" align="center">
        <template #default="{ row }">{{ row.quoteLanguage === 'EN_US' ? t('customer.quote.languageOption.en') : t('customer.quote.languageOption.zh') }}</template>
      </el-table-column>
      <el-table-column :label="t('common.status')" width="110" align="center">
        <template #default="{ row }"><el-tag :type="quoteStatusType(row.status)">{{ quoteStatusText(row.status, statuses) }}</el-tag></template>
      </el-table-column>
      <el-table-column :label="t('customer.quote.validUntil')" prop="validUntil" width="120" align="center" />
      <el-table-column :label="t('customer.quote.quantity')" prop="itemCount" width="90" align="center" />
      <el-table-column :label="t('customer.quote.amount.product')" width="125" align="right">
        <template #default="{ row }">{{ quoteMoney(row.productAmount, row.currencyCode) }}</template>
      </el-table-column>
      <el-table-column :label="t('customer.quote.amount.shipping')" width="115" align="right">
        <template #default="{ row }">{{ quoteMoney(row.shippingAmount, row.currencyCode) }}</template>
      </el-table-column>
      <el-table-column :label="t('customer.quote.amount.total')" width="130" align="right">
        <template #default="{ row }">{{ quoteMoney(row.totalAmount, row.currencyCode) }}</template>
      </el-table-column>
      <el-table-column :label="t('customer.quote.conversion.title')" min-width="150" align="center">
        <template #default="{ row }">
          <el-tag :type="row.salesDocumentId ? 'success' : 'info'">{{ row.salesDocumentId ? `${t('customer.quote.conversion.converted')} · ${row.orderNo}` : t('customer.quote.conversion.pending') }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="t('customer.quote.owner')" prop="ownerName" min-width="120" />
      <el-table-column :label="t('common.updateTime')" width="150" align="center">
        <template #default="{ row }">{{ formatUtc(row.updateTime || row.createTime, 'YYYY-MM-DD HH:mm') }}</template>
      </el-table-column>
      <el-table-column :label="t('common.operate')" width="190" fixed="right" align="center">
        <template #default="{ row }">
          <AdminTableActions :actions="buildQuoteRowActions(row, t, quoteActions)" />
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" v-model:page="query.pageNum" v-model:limit="query.pageSize" :total="total" class="merchant-table-page__pagination" @pagination="getList" />
    <QuoteEmailDialog ref="emailRef" />
    <QuoteConvertDialog ref="convertRef" @converted="handleConverted" />
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { customerQuoteApi, type CustomerQuote, type CustomerQuoteQuery } from '@/api/customer/quote'
import { listCustomerOptions, type CustomerProfile } from '@/api/customer/profile'
import { formatUtc } from '@/utils/datetime'
import { download } from '@/utils/request'
import { buildQuoteRowActions, quoteMoney, quoteStatusText, quoteStatusType } from './quote/quoteListPresentation'
import QuoteEmailDialog from './quote/QuoteEmailDialog.vue'
import QuoteConvertDialog from './quote/QuoteConvertDialog.vue'
import { openQuotePdf } from './quote/quoteArtifacts'

const { t } = useI18n()
const router = useRouter()
const rows = ref<CustomerQuote[]>([])
const customers = ref<CustomerProfile[]>([])
const selected = ref<CustomerQuote>()
const loading = ref(false)
const showSearch = ref(true)
const total = ref(0)
const queryRef = ref<FormInstance>()
const emailRef = ref<InstanceType<typeof QuoteEmailDialog>>()
const convertRef = ref<InstanceType<typeof QuoteConvertDialog>>()
const query = reactive<CustomerQuoteQuery>({ pageNum: 1, pageSize: 10 })
const statuses = computed(() => [
  { value: 'DRAFT', label: t('customer.quote.status.draft') },
  { value: 'CONFIRMED', label: t('customer.quote.status.confirmed') },
  { value: 'VOID', label: t('customer.quote.status.void') }
])

async function getList() {
  loading.value = true
  try {
    const response = await customerQuoteApi.list(query)
    rows.value = response.rows || []
    total.value = response.total || 0
    selected.value = undefined
  } finally { loading.value = false }
}
function handleQuery() { query.pageNum = 1; void getList() }
function resetQuery() { queryRef.value?.resetFields(); handleQuery() }
function openWorkbench(row?: CustomerQuote) {
  void router.push({ name: 'CustomerQuoteWorkbench', query: row?.quoteId ? { quoteId: row.quoteId } : {} })
}
async function handleDelete(row?: CustomerQuote) {
  if (!row?.quoteId) return
  await ElMessageBox.confirm(t('common.deleteConfirm'), t('common.prompt'), { type: 'warning' })
  await customerQuoteApi.remove(row.quoteId)
  ElMessage.success(t('common.deleteSuccess'))
  await getList()
}
function handleExport(row: CustomerQuote) {
  if (!row.quoteId) return
  download(`customer/quotes/${row.quoteId}/export`, {}, `quote_${row.quoteNo || Date.now()}.xlsx`)
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
function viewOrder(row: CustomerQuote) {
  if (row.salesDocumentId) void router.push({ name: 'SalesDocumentDetail', params: { id: row.salesDocumentId } })
}
function handleConverted(result: { salesDocumentId: string }) {
  void router.push({ name: 'SalesDocumentDetail', params: { id: result.salesDocumentId } })
}
const quoteActions = {
  open: openWorkbench, copy: handleCopy, export: handleExport, pdf: openQuotePdf,
  email: (row: CustomerQuote) => emailRef.value?.open(row),
  convert: (row: CustomerQuote) => convertRef.value?.open(row), viewOrder, void: handleVoid, remove: handleDelete
}
Promise.all([getList(), listCustomerOptions({ status: 'ENABLED', pageNum: 1, pageSize: 500 }).then((res) => { customers.value = res.data || [] })])
</script>
