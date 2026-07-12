<template>
  <section class="credit-management">
    <el-tabs v-model="tab" @tab-change="reload">
      <el-tab-pane :label="t('pay.credit.accounts')" name="accounts" />
      <el-tab-pane :label="t('pay.credit.receivables')" name="receivables" />
    </el-tabs>
    <el-form :model="query" inline>
      <el-form-item :label="t('pay.merchant')"><el-input v-model="query.merchantName" clearable /></el-form-item>
      <el-form-item :label="t('common.status')"><el-input v-model="query.status" clearable /></el-form-item>
      <el-form-item><el-button type="primary" @click="search">{{ t('common.search') }}</el-button></el-form-item>
    </el-form>

    <el-table v-if="tab === 'accounts'" v-loading="loading" :data="accounts" border>
      <el-table-column prop="merchantName" :label="t('pay.merchant')" min-width="170" />
      <el-table-column :label="t('pay.credit.limit')" width="140" align="right"><template #default="{ row }">{{ money(row.creditLimit, row.currency) }}</template></el-table-column>
      <el-table-column :label="t('pay.credit.used')" width="140" align="right"><template #default="{ row }">{{ money(row.usedCredit, row.currency) }}</template></el-table-column>
      <el-table-column :label="t('pay.credit.available')" width="140" align="right"><template #default="{ row }">{{ money(row.availableCredit, row.currency) }}</template></el-table-column>
      <el-table-column prop="status" :label="t('common.status')" width="110" />
      <el-table-column :label="t('common.updateTime')" width="160"><template #default="{ row }">{{ time(row.updateTime) }}</template></el-table-column>
      <el-table-column :label="t('common.operate')" width="210" fixed="right">
        <template #default="{ row }">
          <el-button v-hasPermi="['pay:credit:query']" link type="primary" @click="openTransactions(row)">{{ t('pay.credit.transactions') }}</el-button>
          <el-button v-hasPermi="['pay:credit:adjust']" link type="primary" @click="adjust(row)">{{ t('pay.credit.adjust') }}</el-button>
          <el-button v-hasPermi="['pay:credit:freeze']" link :type="row.status === 'FROZEN' ? 'success' : 'danger'" @click="freeze(row)">{{ t(row.status === 'FROZEN' ? 'pay.credit.unfreeze' : 'pay.credit.freeze') }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-table v-else v-loading="loading" :data="receivables" border>
      <el-table-column prop="merchantName" :label="t('pay.merchant')" min-width="160" />
      <el-table-column prop="salesOrderNo" :label="t('pay.salesOrderNo')" min-width="170" />
      <el-table-column prop="payOrderNo" :label="t('pay.orderNo')" min-width="170" />
      <el-table-column :label="t('pay.receivable.amount')" width="130" align="right"><template #default="{ row }">{{ money(row.receivableAmount, row.currency) }}</template></el-table-column>
      <el-table-column :label="t('pay.receivable.outstanding')" width="130" align="right"><template #default="{ row }">{{ money(row.outstandingAmount, row.currency) }}</template></el-table-column>
      <el-table-column prop="dueDate" :label="t('pay.receivable.dueDate')" width="120" />
      <el-table-column prop="status" :label="t('common.status')" width="110" />
      <el-table-column :label="t('common.operate')" width="120"><template #default="{ row }"><el-button v-if="row.status !== 'SETTLED'" v-hasPermi="['pay:credit:repay']" link type="primary" @click="repay(row)">{{ t('pay.credit.repay') }}</el-button></template></el-table-column>
    </el-table>
    <Pagination v-show="total > 0" v-model:page="query.pageNum" v-model:limit="query.pageSize" :total="total" @pagination="load" />

    <el-drawer v-model="transactionVisible" :title="t('pay.credit.transactions')" size="760px">
      <el-table v-loading="transactionLoading" :data="transactions" border>
        <el-table-column prop="transactionNo" :label="t('pay.credit.transactionNo')" min-width="180" />
        <el-table-column prop="transactionType" :label="t('pay.credit.transactionType')" width="130" />
        <el-table-column prop="businessNo" :label="t('pay.credit.businessNo')" min-width="150" />
        <el-table-column :label="t('pay.amount')" width="120" align="right"><template #default="{ row }">{{ money(row.amount, row.currency) }}</template></el-table-column>
        <el-table-column :label="t('common.updateTime')" width="160"><template #default="{ row }">{{ time(row.occurredTime) }}</template></el-table-column>
      </el-table>
    </el-drawer>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import Pagination from '@/components/Pagination/index.vue'
import { payApi, type CreditAccount, type CreditTransaction, type Receivable } from '@/api/pay'
import { formatUtc } from '@/utils/datetime'
import { money } from '../payPresentation'
import { canonicalDecimal } from '@/utils/businessNumber'

const { t } = useI18n()
const tab = ref('accounts')
const loading = ref(false)
const accounts = ref<CreditAccount[]>([])
const receivables = ref<Receivable[]>([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 20, merchantName: '', status: '' })
const transactionVisible = ref(false)
const transactionLoading = ref(false)
const transactions = ref<CreditTransaction[]>([])

function time(value?: string) { return value ? formatUtc(value, 'YYYY-MM-DD HH:mm') : '-' }
async function load() {
  loading.value = true
  try {
    const response = tab.value === 'accounts' ? await payApi.creditAccounts(query) : await payApi.receivables(query)
    if (tab.value === 'accounts') accounts.value = response.rows || []
    else receivables.value = response.rows || []
    total.value = response.total || 0
  } finally { loading.value = false }
}
function search() { query.pageNum = 1; void load() }
function reload() { Object.assign(query, { pageNum: 1, merchantName: '', status: '' }); void load() }
async function openTransactions(row: CreditAccount) {
  if (!row.creditAccountId) return
  transactionVisible.value = true; transactionLoading.value = true
  try { transactions.value = (await payApi.creditTransactions({ creditAccountId: row.creditAccountId, pageNum: 1, pageSize: 100 })).rows || [] }
  finally { transactionLoading.value = false }
}
async function adjust(row: CreditAccount) {
  if (!row.creditAccountId) return
  const amount = await ElMessageBox.prompt(t('pay.credit.adjustAmount'), t('pay.credit.adjust'), { inputPattern: /^-?\d+(\.\d{1,2})?$/, inputErrorMessage: t('pay.credit.amountInvalid') })
  const reason = await ElMessageBox.prompt(t('pay.reason'), t('pay.credit.adjust'), { inputValidator: value => Boolean(value?.trim()) || t('common.required') })
  await payApi.adjustCredit(row.creditAccountId, canonicalDecimal(amount.value), reason.value); ElMessage.success(t('pay.credit.adjusted')); await load()
}
async function freeze(row: CreditAccount) {
  if (!row.creditAccountId) return
  const frozen = row.status !== 'FROZEN'
  const reason = await ElMessageBox.prompt(t('pay.reason'), t(frozen ? 'pay.credit.freeze' : 'pay.credit.unfreeze'), { inputValidator: value => Boolean(value?.trim()) || t('common.required') })
  await payApi.freezeCredit(row.creditAccountId, frozen, reason.value); await load()
}
async function repay(row: Receivable) {
  if (!row.receivableId) return
  const reason = await ElMessageBox.prompt(t('pay.reason'), t('pay.credit.repay'), { inputValidator: value => Boolean(value?.trim()) || t('common.required') })
  await payApi.repayReceivable(row.receivableId, reason.value); ElMessage.success(t('pay.credit.repaid')); await load()
}
onMounted(load)
</script>

<style scoped>
.credit-management { padding: 0 10px 10px; background: #fff; }
.credit-management :deep(.el-form .el-input) { width: 180px; }
</style>
