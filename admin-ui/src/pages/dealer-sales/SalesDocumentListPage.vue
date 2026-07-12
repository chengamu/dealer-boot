<template>
  <div class="app-container sales-document-list">
    <el-form :model="query" inline class="sales-document-list__search">
      <el-form-item :label="t('dealer.sales.orderNo')"><el-input v-model="query.orderNo" clearable /></el-form-item>
      <el-form-item :label="t('dealer.sales.sourceNo')"><el-input v-model="query.sourceNo" clearable /></el-form-item>
      <el-form-item :label="t('dealer.sales.sourceType')">
        <el-select v-model="query.sourceType" clearable>
          <el-option :label="t('dealer.sales.sourceType.QUOTE')" value="QUOTE" />
          <el-option :label="t('dealer.sales.sourceType.QUICK_ORDER')" value="QUICK_ORDER" />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('dealer.sales.customer')"><el-input v-model="query.customerName" clearable /></el-form-item>
      <el-form-item :label="t('dealer.sales.merchant')"><el-input v-model="query.merchantName" clearable /></el-form-item>
      <el-form-item :label="t('dealer.sales.submittedTime')">
        <el-date-picker v-model="dateRange" type="daterange" value-format="YYYY-MM-DD" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">{{ t('common.search') }}</el-button>
        <el-button @click="reset">{{ t('common.reset') }}</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows" border>
      <el-table-column type="index" :label="t('common.index')" width="58" align="center" />
      <el-table-column :label="t('dealer.sales.source')" min-width="210">
        <template #default="{ row }">{{ sourceTypeText(t, row.sourceType) }} · {{ row.sourceNo || row.quoteNo || '-' }}</template>
      </el-table-column>
      <el-table-column prop="orderNo" :label="t('dealer.sales.orderNo')" min-width="170" />
      <el-table-column prop="customerName" :label="t('dealer.sales.customer')" min-width="160" />
      <el-table-column prop="merchantName" :label="t('dealer.sales.merchant')" min-width="160" />
      <el-table-column :label="t('dealer.sales.itemCount')" width="90" align="center">
        <template #default="{ row }">{{ row.itemCount || row.items?.length || 0 }}</template>
      </el-table-column>
      <el-table-column :label="t('dealer.sales.totalAmount')" width="140" align="right">
        <template #default="{ row }"><strong>{{ money(row.totalAmount, row.currencyCode) }}</strong></template>
      </el-table-column>
      <el-table-column :label="t('common.status')" width="110" align="center">
        <template #default="{ row }"><el-tag :type="statusType(row.documentStatus)">{{ documentStatusText(t, row.documentStatus) }}</el-tag></template>
      </el-table-column>
      <el-table-column :label="t('dealer.sales.paymentStatus')" width="110" align="center">
        <template #default="{ row }">{{ paymentStatusText(t, row.paymentStatus) }}</template>
      </el-table-column>
      <el-table-column :label="t('dealer.sales.productionStatus')" width="110" align="center">
        <template #default="{ row }">{{ productionStatusText(t, row.productionStatus) }}</template>
      </el-table-column>
      <el-table-column :label="t('dealer.sales.shipmentStatus')" width="120" align="center">
        <template #default="{ row }">{{ shipmentStatusText(t, row.shipmentStatus) }}</template>
      </el-table-column>
      <el-table-column :label="t('dealer.sales.submittedTime')" width="170">
        <template #default="{ row }">{{ formatUtc(row.submittedTime, 'YYYY-MM-DD HH:mm') }}</template>
      </el-table-column>
      <el-table-column :label="t('common.updateTime')" width="170">
        <template #default="{ row }">{{ formatUtc(row.updateTime, 'YYYY-MM-DD HH:mm') }}</template>
      </el-table-column>
      <el-table-column :label="t('common.operate')" width="120" fixed="right">
        <template #default="{ row }">
          <el-button v-hasPermi="['dealer:sales:query']" link type="primary" @click="openDetail(row)">{{ t('common.detail') }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total" v-model:page="query.pageNum" v-model:limit="query.pageSize" :total="total" @pagination="load" />
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { salesApi, type DocumentStatus, type SalesDocument, type SalesQuery } from '@/api/dealer-sales'
import { formatUtc, withUtcDateRange } from '@/utils/datetime'
import { formatCurrency } from '@/utils/businessNumber'
import { documentStatusText, paymentStatusText, productionStatusText, shipmentStatusText, sourceTypeText } from './salesPresentation'

const { t } = useI18n()
const router = useRouter()
const query = reactive<SalesQuery>({ pageNum: 1, pageSize: 10, sourceType: '', documentStatus: '', paymentStatus: '', productionStatus: '', shipmentStatus: '' })
const dateRange = ref<string[]>([])
const rows = ref<SalesDocument[]>([])
const total = ref(0)
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const response = await salesApi.list(withUtcDateRange(query, dateRange.value, 'beginSubmittedTime', 'endSubmittedTime'))
    rows.value = response.rows || []
    total.value = response.total || 0
  } finally {
    loading.value = false
  }
}

function reset() {
  Object.assign(query, { orderNo: '', sourceNo: '', sourceType: '', customerName: '', merchantName: '', pageNum: 1, pageSize: 10 })
  dateRange.value = []
  void load()
}

function openDetail(row: SalesDocument) {
  void router.push({ name: 'SalesDocumentDetail', params: { id: row.salesDocumentId } })
}

function money(value?: string | null, currency = 'USD') {
  return formatCurrency(value, currency || 'USD', 2, formatCurrency('0', currency || 'USD'))
}

function statusType(status?: DocumentStatus) {
  if (status === 'COMPLETED') return 'success'
  if (status === 'CANCELLED') return 'info'
  return 'primary'
}

void load()
</script>

<style scoped>
.sales-document-list { display: flex; flex-direction: column; gap: 8px; }
.sales-document-list__search { padding: 8px 12px; border: 1px solid #e4eaf2; background: #fff; }
</style>
