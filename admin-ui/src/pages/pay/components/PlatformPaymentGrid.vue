<template>
  <div class="app-container pay-grid">
    <el-form :model="query" inline class="pay-grid__search" @submit.prevent>
      <el-form-item :label="t('pay.businessTime')">
        <el-date-picker v-model="dateRange" type="daterange" value-format="YYYY-MM-DD" />
      </el-form-item>
      <el-form-item :label="t('pay.businessOrigin.label')">
        <el-select v-model="query.businessOrigin" clearable>
          <el-option :label="t('pay.businessOrigin.MERCHANT')" value="MERCHANT" />
          <el-option :label="t('pay.businessOrigin.INTERNAL')" value="INTERNAL" />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('pay.subject')">
        <el-select v-model="query.subjectId" clearable filterable :loading="subjectLoading">
          <el-option v-for="item in subjectOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('pay.keyword')">
        <el-input v-model="query.keyword" clearable />
      </el-form-item>
      <el-form-item :label="t('common.status')">
        <el-select v-model="query.status" clearable>
          <el-option v-for="status in statuses" :key="status" :label="statusText(t, status)" :value="status" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="search">{{ t('common.search') }}</el-button>
        <el-button icon="Refresh" @click="reset">{{ t('common.reset') }}</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows" border @row-dblclick="openDetail">
      <el-table-column type="index" :index="indexMethod" :label="t('common.index')" width="58" align="center" />
      <el-table-column :label="t('pay.orderNo')" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">{{ row.payOrderNo || row.no || '-' }}</template>
      </el-table-column>
      <el-table-column prop="salesOrderNo" :label="t('pay.salesOrderNo')" min-width="170" show-overflow-tooltip />
      <el-table-column :label="t('pay.subject')" min-width="170" show-overflow-tooltip>
        <template #default="{ row }">{{ row.subjectName || '-' }}</template>
      </el-table-column>
      <el-table-column prop="customerName" :label="t('pay.customer')" min-width="150" show-overflow-tooltip />
      <el-table-column :label="t('pay.methodLabel')" width="120" align="center">
        <template #default="{ row }">{{ methodText(t, row.channelCode) }}</template>
      </el-table-column>
      <el-table-column :label="t('pay.orderAmount')" width="130" align="right">
        <template #default="{ row }"><strong>{{ minorMoney(row.price, row.currency) }}</strong></template>
      </el-table-column>
      <el-table-column :label="t('common.status')" width="110" align="center">
        <template #default="{ row }"><el-tag :type="statusType(row.status)">{{ statusText(t, row.status) }}</el-tag></template>
      </el-table-column>
      <el-table-column :label="t('pay.businessTime')" width="160" align="center">
        <template #default="{ row }">{{ formatMinute(row.createTime) }}</template>
      </el-table-column>
      <el-table-column :label="t('pay.successTime')" width="160" align="center">
        <template #default="{ row }">{{ formatMinute(row.successTime) }}</template>
      </el-table-column>
      <el-table-column :label="t('common.operate')" width="160" fixed="right" align="center">
        <template #default="{ row }">
          <AdminTableActions :actions="rowActions(row)" />
        </template>
      </el-table-column>
    </el-table>

    <Pagination v-show="total > 0" v-model:page="query.pageNum" v-model:limit="query.pageSize" :total="total" @pagination="load" />
    <PaymentDetailDrawer ref="detailRef" />
    <PaymentSupplementDialog ref="supplementRef" @saved="load" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import AdminTableActions, { type AdminTableAction } from '@/components/AdminTableActions/index.vue'
import Pagination from '@/components/Pagination/index.vue'
import { platformPaymentApi, type PayOrder, type PayOrderQuery, type PayOrderStatus } from '@/api/pay'
import { withUtcDateRange } from '@/utils/datetime'
import PaymentDetailDrawer from './PaymentDetailDrawer.vue'
import PaymentSupplementDialog from './PaymentSupplementDialog.vue'
import { currentMonthDateRange, formatMinute, useFinanceSubjectFilter } from '../payGridSupport'
import { methodText, minorMoney, statusText, statusType } from '../payPresentation'

const { t } = useI18n()
const loading = ref(false)
const rows = ref<PayOrder[]>([])
const total = ref(0)
const detailRef = ref<InstanceType<typeof PaymentDetailDrawer>>()
const supplementRef = ref<InstanceType<typeof PaymentSupplementDialog>>()
const dateRange = ref<string[]>(currentMonthDateRange())
const statuses: PayOrderStatus[] = [0, 5, 10, 20]
const query = reactive<PayOrderQuery>({
  pageNum: 1,
  pageSize: 20,
  businessOrigin: '',
  subjectId: '',
  keyword: '',
  status: ''
})
const { loading: subjectLoading, subjectOptions, load: loadSubjects } = useFinanceSubjectFilter(query)

async function load() {
  loading.value = true
  try {
    const response = await platformPaymentApi.list(withUtcDateRange({ ...query }, dateRange.value))
    rows.value = response.rows || []
    total.value = response.total || 0
  } finally {
    loading.value = false
  }
}

function search() {
  query.pageNum = 1
  void load()
}

function reset() {
  Object.assign(query, { pageNum: 1, pageSize: 20, businessOrigin: '', subjectId: '', keyword: '', status: '' })
  dateRange.value = currentMonthDateRange()
  void load()
}

function openDetail(row: PayOrder) {
  detailRef.value?.open(row, 'platform')
}

function rowActions(row: PayOrder): AdminTableAction[] {
  return [
    { label: t('common.detail'), permission: 'platform:finance:payment:query', primary: true, onClick: () => openDetail(row) },
    {
      label: t('pay.supplement.action'),
      permission: 'platform:finance:payment:supplement',
      hidden: row.status !== 0,
      onClick: () => supplementRef.value?.open(row, 'platform')
    }
  ]
}

function indexMethod(index: number) {
  return ((query.pageNum || 1) - 1) * (query.pageSize || 20) + index + 1
}

onMounted(async () => {
  await loadSubjects()
  await load()
})
</script>

<style scoped>
.pay-grid {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.pay-grid__search {
  padding: 8px 12px 0;
  border: 1px solid #e9edf5;
  background: #fff;
}

.pay-grid__search :deep(.el-input),
.pay-grid__search :deep(.el-select) {
  width: 160px;
}

.pay-grid__search :deep(.el-date-editor) {
  width: 240px;
}
</style>
