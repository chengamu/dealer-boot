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
          <el-option :label="t('pay.bank.status.PENDING_REVIEW')" value="PENDING_REVIEW" />
          <el-option :label="t('pay.bank.status.REJECTED')" value="REJECTED" />
          <el-option :label="t('pay.bank.status.SUCCESS')" value="SUCCESS" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="search">{{ t('common.search') }}</el-button>
        <el-button icon="Refresh" @click="reset">{{ t('common.reset') }}</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows" border>
      <el-table-column type="index" :index="indexMethod" :label="t('common.index')" width="58" align="center" />
      <el-table-column prop="payOrderNo" :label="t('pay.orderNo')" min-width="170" show-overflow-tooltip />
      <el-table-column prop="salesOrderNo" :label="t('pay.salesOrderNo')" min-width="170" show-overflow-tooltip />
      <el-table-column prop="subjectName" :label="t('pay.subject')" min-width="160" show-overflow-tooltip />
      <el-table-column prop="payerName" :label="t('pay.bank.payerName')" min-width="140" show-overflow-tooltip />
      <el-table-column prop="referenceNo" :label="t('pay.reference')" min-width="150" show-overflow-tooltip />
      <el-table-column :label="t('pay.amount')" width="130" align="right">
        <template #default="{ row }">{{ minorMoney(row.declaredPrice, row.currency) }}</template>
      </el-table-column>
      <el-table-column :label="t('common.status')" width="120" align="center">
        <template #default="{ row }"><el-tag :type="bankStatusType(row.status)">{{ bankStatusText(t, row.status) }}</el-tag></template>
      </el-table-column>
      <el-table-column :label="t('pay.businessTime')" width="160" align="center">
        <template #default="{ row }">{{ formatMinute(row.submittedTime) }}</template>
      </el-table-column>
      <el-table-column :label="t('common.operate')" width="160" fixed="right" align="center">
        <template #default="{ row }"><AdminTableActions :actions="rowActions(row)" /></template>
      </el-table-column>
    </el-table>

    <Pagination v-show="total > 0" v-model:page="query.pageNum" v-model:limit="query.pageSize" :total="total" @pagination="load" />
    <PaymentDetailDrawer ref="detailRef" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import AdminTableActions, { type AdminTableAction } from '@/components/AdminTableActions/index.vue'
import Pagination from '@/components/Pagination/index.vue'
import { platformBankApi, type BankTransferQuery, type BankTransferRecord } from '@/api/pay'
import { withUtcDateRange } from '@/utils/datetime'
import PaymentDetailDrawer from './PaymentDetailDrawer.vue'
import { currentMonthDateRange, formatMinute, useFinanceSubjectFilter } from '../payGridSupport'
import { bankStatusText, bankStatusType, minorMoney } from '../payPresentation'

const { t } = useI18n()
const loading = ref(false)
const rows = ref<BankTransferRecord[]>([])
const total = ref(0)
const detailRef = ref<InstanceType<typeof PaymentDetailDrawer>>()
const dateRange = ref<string[]>(currentMonthDateRange())
const query = reactive<BankTransferQuery>({ pageNum: 1, pageSize: 20, businessOrigin: '', subjectId: '', keyword: '', status: 'PENDING_REVIEW' })
const { loading: subjectLoading, subjectOptions, load: loadSubjects } = useFinanceSubjectFilter(query)

async function load() {
  loading.value = true
  try {
    const response = await platformBankApi.list(withUtcDateRange({ ...query }, dateRange.value))
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
  Object.assign(query, { pageNum: 1, pageSize: 20, businessOrigin: '', subjectId: '', keyword: '', status: 'PENDING_REVIEW' })
  dateRange.value = currentMonthDateRange()
  void load()
}

function openDetail(row: BankTransferRecord) {
  if (!row.payOrderId) return
  detailRef.value?.open({ payOrderId: row.payOrderId }, 'platform')
}

async function review(row: BankTransferRecord, approved: boolean) {
  if (!row.extensionId) return
  let reason = ''
  if (!approved) {
    const result = await ElMessageBox.prompt(t('pay.bank.rejectReason'), t('pay.bank.reject'))
    reason = result.value
  } else {
    await ElMessageBox.confirm(t('pay.bank.approveConfirm'), t('common.prompt'), { type: 'warning' })
  }
  await platformBankApi.review(row.extensionId, approved, reason)
  ElMessage.success(t(approved ? 'pay.bank.approved' : 'pay.bank.rejected'))
  await load()
}

function rowActions(row: BankTransferRecord): AdminTableAction[] {
  return [
    { label: t('common.detail'), permission: 'platform:finance:payment:query', primary: true, onClick: () => openDetail(row) },
    { label: t('pay.bank.approve'), permission: 'platform:finance:bank:review', hidden: row.status !== 'PENDING_REVIEW', onClick: () => review(row, true) },
    { label: t('pay.bank.reject'), permission: 'platform:finance:bank:review', hidden: row.status !== 'PENDING_REVIEW', type: 'danger', onClick: () => review(row, false) }
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
