<template>
  <div class="app-container sales-list">
    <el-form :model="query" inline class="search-form">
      <el-form-item :label="t('dealer.sales.quoteNo')"><el-input v-model="query.quoteNo" clearable /></el-form-item>
      <el-form-item :label="t('dealer.sales.orderNo')"><el-input v-model="query.orderNo" clearable /></el-form-item>
      <el-form-item :label="t('dealer.sales.customer')"><el-input v-model="query.customerName" clearable /></el-form-item>
      <el-form-item><el-button type="primary" icon="Search" @click="load">{{ t('common.search') }}</el-button></el-form-item>
    </el-form>
    <el-tabs v-model="query.documentStatus" @tab-change="load">
      <el-tab-pane :label="t('common.all')" name="" />
      <el-tab-pane v-for="status in statuses" :key="status" :label="statusText(status)" :name="status" />
    </el-tabs>
    <div class="table-toolbar">
      <span />
      <div><el-button icon="Refresh" circle @click="load" /></div>
    </div>
    <el-table v-loading="loading" :data="rows" border highlight-current-row>
      <el-table-column type="index" :label="t('common.index')" width="58" align="center" />
      <el-table-column prop="quoteNo" :label="t('dealer.sales.quoteNo')" min-width="160" />
      <el-table-column prop="orderNo" :label="t('dealer.sales.orderNo')" min-width="160"><template #default="{ row }">{{ row.orderNo || '-' }}</template></el-table-column>
      <el-table-column prop="customerName" :label="t('dealer.sales.customer')" min-width="150" />
      <el-table-column prop="merchantName" :label="t('dealer.sales.merchant')" min-width="150" />
      <el-table-column prop="projectName" :label="t('dealer.sales.project')" min-width="160" />
      <el-table-column :label="t('dealer.sales.totalAmount')" width="130" align="right"><template #default="{ row }">{{ money(row.totalAmount, row.currencyCode) }}</template></el-table-column>
      <el-table-column :label="t('common.status')" width="110" align="center"><template #default="{ row }"><el-tag :type="statusType(row.documentStatus)">{{ statusText(row.documentStatus) }}</el-tag></template></el-table-column>
      <el-table-column :label="t('dealer.sales.payment')" width="100" align="center"><template #default="{ row }">{{ paymentStatusText(t, row.paymentStatus) }}</template></el-table-column>
      <el-table-column :label="t('dealer.sales.startProduction')" width="110" align="center"><template #default="{ row }">{{ productionStatusText(t, row.productionStatus) }}</template></el-table-column>
      <el-table-column :label="t('dealer.sales.ship')" width="100" align="center"><template #default="{ row }">{{ shipmentStatusText(t, row.shipmentStatus) }}</template></el-table-column>
      <el-table-column :label="t('common.updateTime')" width="150"><template #default="{ row }">{{ formatUtc(row.updateTime, 'YYYY-MM-DD HH:mm') }}</template></el-table-column>
      <el-table-column :label="t('common.operate')" width="150" fixed="right"><template #default="{ row }">
        <el-button v-hasPermi="['dealer:sales:query']" link type="primary" @click="openDetail(row)">{{ t('common.detail') }}</el-button>
      </template></el-table-column>
    </el-table>
    <pagination v-show="total" :total="total" v-model:page="query.pageNum" v-model:limit="query.pageSize" @pagination="load" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { salesApi, type DocumentStatus, type SalesDocument, type SalesQuery } from '@/api/dealer-sales'
import { formatUtc } from '@/utils/datetime'
import { documentStatusText, paymentStatusText, productionStatusText, shipmentStatusText } from './salesPresentation'
const { t } = useI18n(); const router = useRouter()
const query = reactive<SalesQuery>({ pageNum: 1, pageSize: 10, documentStatus: '' })
const statuses: DocumentStatus[] = ['SUBMITTED', 'COMPLETED', 'CANCELLED']
const rows = ref<SalesDocument[]>([]); const total = ref(0); const loading = ref(false)
async function load() { loading.value = true; try { const res = await salesApi.list(query); rows.value = res.rows || []; total.value = res.total || 0 } finally { loading.value = false } }
function statusText(status?: string) { return documentStatusText(t, status) }
function statusType(status?: string) { return status === 'COMPLETED' ? 'success' : status === 'CANCELLED' ? 'info' : status === 'SUBMITTED' ? 'primary' : 'warning' }
function money(value?: number, currency = 'USD') { return new Intl.NumberFormat('en-US', { style: 'currency', currency: currency || 'USD' }).format(value || 0) }
function openDetail(row: SalesDocument) { void router.push({ name: 'SalesDocumentDetail', params: { id: row.salesDocumentId } }) }
onMounted(load)
</script>

<style scoped>
.sales-list { display: flex; flex-direction: column; gap: 10px; }
.search-form { padding: 12px 14px 0; border: 1px solid #e4e8ef; background: #fff; }
.table-toolbar { display: flex; justify-content: space-between; }
</style>
