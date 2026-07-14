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
          <el-option :label="t('pay.reconciliation.status.OPEN')" value="OPEN" />
          <el-option :label="t('pay.reconciliation.status.RESOLVED')" value="RESOLVED" />
          <el-option :label="t('pay.reconciliation.status.IGNORED')" value="IGNORED" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="search">{{ t('common.search') }}</el-button>
        <el-button icon="Refresh" @click="reset">{{ t('common.reset') }}</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows" border>
      <el-table-column type="index" :index="indexMethod" :label="t('common.index')" width="58" align="center" />
      <el-table-column prop="caseNo" :label="t('pay.reconciliation.caseNo')" min-width="170" show-overflow-tooltip />
      <el-table-column prop="anomalyType" :label="t('pay.reconciliation.anomalyType')" min-width="180" show-overflow-tooltip />
      <el-table-column :label="t('pay.reconciliation.severity.label')" width="110" align="center">
        <template #default="{ row }"><el-tag :type="reconciliationSeverityType(row.severity)">{{ reconciliationSeverityText(t, row.severity) }}</el-tag></template>
      </el-table-column>
      <el-table-column :label="t('common.status')" width="110" align="center">
        <template #default="{ row }">{{ reconciliationStatusText(t, row.status) }}</template>
      </el-table-column>
      <el-table-column prop="diagnosisMessage" :label="t('pay.reconciliation.diagnosis')" min-width="240" show-overflow-tooltip />
      <el-table-column :label="t('pay.businessTime')" width="160" align="center"><template #default="{ row }">{{ formatMinute(row.detectedTime) }}</template></el-table-column>
      <el-table-column :label="t('common.operate')" width="180" fixed="right" align="center">
        <template #default="{ row }"><AdminTableActions :actions="rowActions(row)" /></template>
      </el-table-column>
    </el-table>

    <Pagination v-show="total > 0" v-model:page="query.pageNum" v-model:limit="query.pageSize" :total="total" @pagination="load" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import AdminTableActions, { type AdminTableAction } from '@/components/AdminTableActions/index.vue'
import Pagination from '@/components/Pagination/index.vue'
import { platformReconciliationApi, type ReconciliationCase, type ReconciliationQuery } from '@/api/pay'
import { withUtcDateRange } from '@/utils/datetime'
import { currentMonthDateRange, formatMinute, useFinanceSubjectFilter } from '../payGridSupport'
import { reconciliationSeverityText, reconciliationSeverityType, reconciliationStatusText } from '../payPresentation'

const { t } = useI18n()
const emit = defineEmits<{ detail: [caseId: string] }>()
const loading = ref(false)
const rows = ref<ReconciliationCase[]>([])
const total = ref(0)
const dateRange = ref<string[]>(currentMonthDateRange())
const query = reactive<ReconciliationQuery>({ pageNum: 1, pageSize: 20, businessOrigin: '', subjectId: '', keyword: '', status: 'OPEN' })
const { loading: subjectLoading, subjectOptions, load: loadSubjects } = useFinanceSubjectFilter(query)

async function load() {
  loading.value = true
  try {
    const response = await platformReconciliationApi.list(withUtcDateRange({ ...query }, dateRange.value))
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
  Object.assign(query, { pageNum: 1, pageSize: 20, businessOrigin: '', subjectId: '', keyword: '', status: 'OPEN' })
  dateRange.value = currentMonthDateRange()
  void load()
}

async function handleReasonAction(row: ReconciliationCase, action: 'rescan' | 'reconcileChannel' | 'repairOrder' | 'ignore') {
  if (!row.caseId) return
  const titles = {
    rescan: t('pay.reconciliation.rescan'),
    reconcileChannel: t('pay.reconciliation.reconcileChannel'),
    repairOrder: t('pay.reconciliation.repairOrder'),
    ignore: t('pay.reconciliation.ignore')
  } as const
  const result = await ElMessageBox.prompt(t('pay.reason'), titles[action])
  await platformReconciliationApi[action](row.caseId, result.value)
  ElMessage.success(t('common.operationSuccess'))
  await load()
}

function rowActions(row: ReconciliationCase): AdminTableAction[] {
  return [
    {
      label: t('common.detail'),
      permission: 'platform:finance:reconciliation:query',
      primary: true,
      onClick: () => {
        if (row.caseId) emit('detail', row.caseId)
      }
    },
    { label: t('pay.reconciliation.rescan'), permission: 'platform:finance:reconciliation:rescan', hidden: row.status !== 'OPEN', onClick: () => handleReasonAction(row, 'rescan') },
    { label: t('pay.reconciliation.reconcileChannel'), permission: 'platform:finance:reconciliation:channel', hidden: row.status !== 'OPEN', onClick: () => handleReasonAction(row, 'reconcileChannel') },
    { label: t('pay.reconciliation.repairOrder'), permission: 'platform:finance:reconciliation:repair', hidden: row.status !== 'OPEN', onClick: () => handleReasonAction(row, 'repairOrder') },
    { label: t('pay.reconciliation.ignore'), permission: 'platform:finance:reconciliation:ignore', hidden: row.status !== 'OPEN', onClick: () => handleReasonAction(row, 'ignore') }
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
