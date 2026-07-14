<template>
  <div class="app-container pay-grid">
    <el-form v-if="mode === 'receivable'" :model="query" inline class="pay-grid__search" @submit.prevent>
      <el-form-item :label="t('pay.salesOrderNo')">
        <el-input v-model="query.salesOrderNo" clearable />
      </el-form-item>
      <el-form-item :label="t('pay.orderNo')">
        <el-input v-model="query.payOrderNo" clearable />
      </el-form-item>
      <el-form-item :label="t('common.status')">
        <el-input v-model="query.status" clearable />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="search">{{ t('common.search') }}</el-button>
        <el-button icon="Refresh" @click="reset">{{ t('common.reset') }}</el-button>
      </el-form-item>
    </el-form>

    <el-table v-if="mode === 'credit'" v-loading="loading" :data="account ? [account] : []" border>
      <el-table-column prop="subjectName" :label="t('pay.subject')" min-width="150" show-overflow-tooltip />
      <el-table-column prop="merchantName" :label="t('pay.merchant')" min-width="150" show-overflow-tooltip />
      <el-table-column :label="t('pay.credit.limit')" width="140" align="right">
        <template #default="{ row }">{{ money(row.creditLimit, row.currency) }}</template>
      </el-table-column>
      <el-table-column :label="t('pay.credit.used')" width="140" align="right">
        <template #default="{ row }">{{ money(row.usedCredit, row.currency) }}</template>
      </el-table-column>
      <el-table-column :label="t('pay.credit.available')" width="140" align="right">
        <template #default="{ row }">{{ money(row.availableCredit, row.currency) }}</template>
      </el-table-column>
      <el-table-column :label="t('common.status')" width="110" align="center">
        <template #default="{ row }">{{ creditAccountStatusText(t, row.status) }}</template>
      </el-table-column>
      <el-table-column :label="t('common.updateTime')" width="160" align="center">
        <template #default="{ row }">{{ formatMinute(row.updateTime) }}</template>
      </el-table-column>
      <el-table-column :label="t('common.operate')" width="120" fixed="right" align="center">
        <template #default="{ row }">
          <el-button v-if="row.creditAccountId" link type="primary" @click="openTransactions(row)">
            {{ t('pay.credit.transactions') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-table v-else v-loading="loading" :data="receivables" border>
      <el-table-column type="index" :index="indexMethod" :label="t('common.index')" width="58" align="center" />
      <el-table-column prop="salesOrderNo" :label="t('pay.salesOrderNo')" min-width="160" show-overflow-tooltip />
      <el-table-column prop="payOrderNo" :label="t('pay.orderNo')" min-width="160" show-overflow-tooltip />
      <el-table-column :label="t('pay.receivable.amount')" width="140" align="right">
        <template #default="{ row }">{{ money(row.receivableAmount, row.currency) }}</template>
      </el-table-column>
      <el-table-column :label="t('pay.receivable.outstanding')" width="140" align="right">
        <template #default="{ row }">{{ money(row.outstandingAmount, row.currency) }}</template>
      </el-table-column>
      <el-table-column :label="t('pay.receivable.dueDate')" width="120" align="center">
        <template #default="{ row }">{{ row.dueDate || '-' }}</template>
      </el-table-column>
      <el-table-column :label="t('common.status')" width="110" align="center">
        <template #default="{ row }">{{ receivableStatusText(t, row.status) }}</template>
      </el-table-column>
      <el-table-column :label="t('pay.businessTime')" width="160" align="center">
        <template #default="{ row }">{{ formatMinute(row.formedTime) }}</template>
      </el-table-column>
    </el-table>

    <Pagination v-if="mode === 'receivable'" v-show="total > 0" v-model:page="pageNum" v-model:limit="pageSize" :total="total" @pagination="load" />
    <el-drawer v-model="transactionVisible" :title="t('pay.credit.transactions')" size="760px">
      <el-table v-loading="transactionLoading" :data="transactions" border>
        <el-table-column prop="transactionNo" :label="t('pay.credit.transactionNo')" min-width="180" />
        <el-table-column :label="t('pay.credit.transactionType')" width="140">
          <template #default="{ row }">{{ creditTransactionTypeText(t, row.transactionType) }}</template>
        </el-table-column>
        <el-table-column prop="businessNo" :label="t('pay.credit.businessNo')" min-width="160" />
        <el-table-column :label="t('pay.amount')" width="120" align="right">
          <template #default="{ row }">{{ money(row.amount, row.currency) }}</template>
        </el-table-column>
        <el-table-column :label="t('common.updateTime')" width="160" align="center">
          <template #default="{ row }">{{ formatMinute(row.occurredTime) }}</template>
        </el-table-column>
      </el-table>
      <Pagination
        v-show="transactionTotal > 0"
        v-model:page="transactionPageNum"
        v-model:limit="transactionPageSize"
        :total="transactionTotal"
        @pagination="loadTransactions"
      />
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import Pagination from '@/components/Pagination/index.vue'
import { businessPaymentApi, type CreditAccount, type CreditTransaction, type Receivable } from '@/api/pay'
import { formatMinute } from '../payGridSupport'
import { creditAccountStatusText, creditTransactionTypeText, money, receivableStatusText } from '../payPresentation'

const props = defineProps<{ mode: 'credit' | 'receivable' }>()
const { t } = useI18n()
const loading = ref(false)
const account = ref<CreditAccount | null>(null)
const receivables = ref<Receivable[]>([])
const transactions = ref<CreditTransaction[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)
const query = reactive({ salesOrderNo: '', payOrderNo: '', status: '' })
const transactionVisible = ref(false)
const transactionLoading = ref(false)
const transactionTotal = ref(0)
const transactionPageNum = ref(1)
const transactionPageSize = ref(20)

async function load() {
  loading.value = true
  try {
    if (props.mode === 'credit') {
      account.value = await businessPaymentApi.creditAccount()
      return
    }
    const response = await businessPaymentApi.receivables({ ...query, pageNum: pageNum.value, pageSize: pageSize.value })
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
  Object.assign(query, { salesOrderNo: '', payOrderNo: '', status: '' })
  pageNum.value = 1
  pageSize.value = 20
  void load()
}

async function loadTransactions() {
  transactionLoading.value = true
  try {
    const response = await businessPaymentApi.creditTransactions({
      pageNum: transactionPageNum.value,
      pageSize: transactionPageSize.value
    })
    transactions.value = response.rows || []
    transactionTotal.value = response.total || 0
  } finally {
    transactionLoading.value = false
  }
}

function openTransactions(row: CreditAccount) {
  if (!row.creditAccountId) return
  transactionVisible.value = true
  transactionPageNum.value = 1
  void loadTransactions()
}

function indexMethod(index: number) {
  return (pageNum.value - 1) * pageSize.value + index + 1
}

onMounted(load)
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

.pay-grid__search :deep(.el-input) {
  width: 180px;
}
</style>
