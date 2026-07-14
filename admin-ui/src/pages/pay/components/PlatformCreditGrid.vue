<template>
  <div class="app-container pay-grid">
    <el-form :model="query" inline class="pay-grid__search" @submit.prevent>
      <el-form-item :label="t('pay.businessOrigin.label')">
        <el-select v-model="query.businessOrigin" clearable>
          <el-option :label="t('pay.businessOrigin.MERCHANT')" value="MERCHANT" />
          <el-option :label="t('pay.businessOrigin.INTERNAL')" value="INTERNAL" />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('pay.subject')">
        <el-select v-if="mode === 'credit'" v-model="query.subjectId" clearable filterable :loading="subjectLoading">
          <el-option v-for="item in subjectOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-select v-else v-model="receivableQuery.subjectId" clearable filterable :loading="subjectLoading">
          <el-option v-for="item in subjectOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item :label="mode === 'credit' ? t('pay.merchant') : t('pay.keyword')">
        <el-input v-model="keyword" clearable />
      </el-form-item>
      <el-form-item :label="t('common.status')">
        <el-input v-if="mode === 'credit'" v-model="query.status" clearable />
        <el-input v-else v-model="receivableQuery.status" clearable />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="search">{{ t('common.search') }}</el-button>
        <el-button icon="Refresh" @click="reset">{{ t('common.reset') }}</el-button>
      </el-form-item>
    </el-form>

    <el-table v-if="mode === 'credit'" v-loading="loading" :data="accounts" border>
      <el-table-column type="index" :index="indexMethod" :label="t('common.index')" width="58" align="center" />
      <el-table-column prop="subjectName" :label="t('pay.subject')" min-width="150" show-overflow-tooltip />
      <el-table-column prop="merchantName" :label="t('pay.merchant')" min-width="150" show-overflow-tooltip />
      <el-table-column :label="t('pay.credit.limit')" width="130" align="right"><template #default="{ row }">{{ money(row.creditLimit, row.currency) }}</template></el-table-column>
      <el-table-column :label="t('pay.credit.used')" width="130" align="right"><template #default="{ row }">{{ money(row.usedCredit, row.currency) }}</template></el-table-column>
      <el-table-column :label="t('pay.credit.available')" width="130" align="right"><template #default="{ row }">{{ money(row.availableCredit, row.currency) }}</template></el-table-column>
      <el-table-column prop="status" :label="t('common.status')" width="110" align="center" />
      <el-table-column :label="t('common.updateTime')" width="160" align="center"><template #default="{ row }">{{ formatMinute(row.updateTime) }}</template></el-table-column>
      <el-table-column :label="t('common.operate')" width="190" fixed="right" align="center">
        <template #default="{ row }"><AdminTableActions :actions="creditActions(row)" /></template>
      </el-table-column>
    </el-table>

    <el-table v-else v-loading="loading" :data="receivables" border>
      <el-table-column type="index" :index="indexMethod" :label="t('common.index')" width="58" align="center" />
      <el-table-column prop="subjectName" :label="t('pay.subject')" min-width="150" show-overflow-tooltip />
      <el-table-column prop="merchantName" :label="t('pay.merchant')" min-width="150" show-overflow-tooltip />
      <el-table-column prop="salesOrderNo" :label="t('pay.salesOrderNo')" min-width="160" show-overflow-tooltip />
      <el-table-column prop="payOrderNo" :label="t('pay.orderNo')" min-width="160" show-overflow-tooltip />
      <el-table-column :label="t('pay.receivable.amount')" width="130" align="right"><template #default="{ row }">{{ money(row.receivableAmount, row.currency) }}</template></el-table-column>
      <el-table-column :label="t('pay.receivable.outstanding')" width="130" align="right"><template #default="{ row }">{{ money(row.outstandingAmount, row.currency) }}</template></el-table-column>
      <el-table-column :label="t('pay.receivable.dueDate')" width="120" align="center"><template #default="{ row }">{{ row.dueDate || '-' }}</template></el-table-column>
      <el-table-column :label="t('common.status')" width="110" align="center"><template #default="{ row }">{{ receivableStatusText(t, row.status) }}</template></el-table-column>
      <el-table-column :label="t('pay.businessTime')" width="160" align="center"><template #default="{ row }">{{ formatMinute(row.formedTime) }}</template></el-table-column>
      <el-table-column :label="t('common.operate')" width="120" fixed="right" align="center">
        <template #default="{ row }"><AdminTableActions :actions="receivableActions(row)" /></template>
      </el-table-column>
    </el-table>

    <Pagination v-show="total > 0" v-model:page="pageNum" v-model:limit="pageSize" :total="total" @pagination="load" />
    <el-drawer v-model="transactionVisible" :title="t('pay.credit.transactions')" size="760px">
      <el-table v-loading="transactionLoading" :data="transactions" border>
        <el-table-column prop="transactionNo" :label="t('pay.credit.transactionNo')" min-width="180" />
        <el-table-column prop="transactionType" :label="t('pay.credit.transactionType')" width="140" />
        <el-table-column prop="businessNo" :label="t('pay.credit.businessNo')" min-width="160" />
        <el-table-column :label="t('pay.amount')" width="120" align="right"><template #default="{ row }">{{ money(row.amount, row.currency) }}</template></el-table-column>
        <el-table-column :label="t('common.updateTime')" width="160" align="center"><template #default="{ row }">{{ formatMinute(row.occurredTime) }}</template></el-table-column>
      </el-table>
    </el-drawer>
    <CreditReceivableRepayDialog ref="repayRef" @saved="load" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import AdminTableActions, { type AdminTableAction } from '@/components/AdminTableActions/index.vue'
import Pagination from '@/components/Pagination/index.vue'
import { platformCreditApi, platformReceivableApi, type CreditAccount, type CreditTransaction, type Receivable } from '@/api/pay'
import CreditReceivableRepayDialog from './CreditReceivableRepayDialog.vue'
import { formatMinute, useFinanceSubjectFilter } from '../payGridSupport'
import { money, receivableStatusText } from '../payPresentation'

const props = defineProps<{ mode: 'credit' | 'receivable' }>()
const { t } = useI18n()
const loading = ref(false)
const total = ref(0)
const accounts = ref<CreditAccount[]>([])
const receivables = ref<Receivable[]>([])
const transactions = ref<CreditTransaction[]>([])
const pageNum = ref(1)
const pageSize = ref(20)
const query = reactive({ businessOrigin: '', subjectId: '', merchantName: '', status: '' })
const receivableQuery = reactive({ businessOrigin: '', subjectId: '', merchantName: '', status: '', salesOrderNo: '', payOrderNo: '' })
const { loading: subjectLoading, subjectOptions, load: loadSubjects } = useFinanceSubjectFilter(props.mode === 'credit' ? query : receivableQuery)
const transactionVisible = ref(false)
const transactionLoading = ref(false)
const repayRef = ref<InstanceType<typeof CreditReceivableRepayDialog>>()

const keyword = computed({
  get: () => (props.mode === 'credit' ? query.merchantName : receivableQuery.salesOrderNo || receivableQuery.payOrderNo || receivableQuery.merchantName),
  set: (value: string) => {
    if (props.mode === 'credit') query.merchantName = value
    else Object.assign(receivableQuery, { merchantName: value, salesOrderNo: value, payOrderNo: value })
  }
})

async function load() {
  loading.value = true
  try {
    if (props.mode === 'credit') {
      const response = await platformCreditApi.list({ ...query, pageNum: pageNum.value, pageSize: pageSize.value })
      accounts.value = response.rows || []
      total.value = response.total || 0
      return
    }
    const response = await platformReceivableApi.list({ ...receivableQuery, pageNum: pageNum.value, pageSize: pageSize.value })
    receivables.value = response.rows || []
    total.value = response.total || 0
  } finally {
    loading.value = false
  }
}

function search() {
  pageNum.value = 1
  void load()
}

function reset() {
  pageNum.value = 1
  pageSize.value = 20
  Object.assign(query, { businessOrigin: '', subjectId: '', merchantName: '', status: '' })
  Object.assign(receivableQuery, { businessOrigin: '', subjectId: '', merchantName: '', status: '', salesOrderNo: '', payOrderNo: '' })
  void load()
}

async function openTransactions(row: CreditAccount) {
  if (!row.creditAccountId) return
  transactionVisible.value = true
  transactionLoading.value = true
  try {
    const response = await platformCreditApi.transactions(row.creditAccountId, { pageNum: 1, pageSize: 100 })
    transactions.value = response.rows || []
  } finally {
    transactionLoading.value = false
  }
}

async function adjust(row: CreditAccount) {
  if (!row.creditAccountId) return
  const amount = await ElMessageBox.prompt(t('pay.credit.adjustAmount'), t('pay.credit.adjust'))
  const reason = await ElMessageBox.prompt(t('pay.reason'), t('pay.credit.adjust'))
  await platformCreditApi.adjust(row.creditAccountId, amount.value, reason.value)
  ElMessage.success(t('pay.credit.adjusted'))
  await load()
}

async function freeze(row: CreditAccount) {
  if (!row.creditAccountId) return
  const reason = await ElMessageBox.prompt(t('pay.reason'), t(row.status === 'FROZEN' ? 'pay.credit.unfreeze' : 'pay.credit.freeze'))
  if (row.status === 'FROZEN') await platformCreditApi.unfreeze(row.creditAccountId, reason.value)
  else await platformCreditApi.freeze(row.creditAccountId, reason.value)
  ElMessage.success(t(row.status === 'FROZEN' ? 'pay.credit.unfrozenSuccess' : 'pay.credit.frozenSuccess'))
  await load()
}

function creditActions(row: CreditAccount): AdminTableAction[] {
  return [
    { label: t('pay.credit.transactions'), permission: 'platform:finance:credit:query', primary: true, onClick: () => openTransactions(row) },
    { label: t('pay.credit.adjust'), permission: 'platform:finance:credit:adjust', onClick: () => adjust(row) },
    { label: row.status === 'FROZEN' ? t('pay.credit.unfreeze') : t('pay.credit.freeze'), permission: 'platform:finance:credit:freeze', onClick: () => freeze(row) }
  ]
}

function receivableActions(row: Receivable): AdminTableAction[] {
  return [
    {
      label: t('pay.receivable.repayAction'),
      permission: 'platform:finance:receivable:repay',
      primary: true,
      hidden: row.status === 'SETTLED',
      onClick: () => repayRef.value?.open(row)
    }
  ]
}

function indexMethod(index: number) {
  return (pageNum.value - 1) * pageSize.value + index + 1
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
</style>
