<template>
  <AdminDrawer v-model="visible" :title="t('pay.detail.title')" size="760px">
    <div v-loading="loading" class="payment-detail">
      <el-descriptions :column="2" border>
        <el-descriptions-item :label="t('pay.orderNo')">{{ order?.no || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('pay.salesOrderNo')">{{ order?.salesOrderNo || order?.merchantOrderId || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('pay.merchant')">{{ order?.merchantName || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('pay.customer')">{{ order?.customerName || detail?.customerName || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('pay.methodLabel')">{{ methodText(t, order?.channelCode) }}</el-descriptions-item>
        <el-descriptions-item :label="t('common.status')">
          <el-tag :type="statusType(order?.status)">{{ statusText(t, order?.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item :label="t('pay.orderAmount')">{{ minorMoney(order?.price, order?.currency) }}</el-descriptions-item>
        <el-descriptions-item :label="t('pay.channelOrderNo')">{{ masked(order?.channelOrderNo) }}</el-descriptions-item>
        <el-descriptions-item :label="t('common.createTime')">{{ time(order?.createTime) }}</el-descriptions-item>
        <el-descriptions-item :label="t('pay.successTime')">{{ time(order?.successTime) }}</el-descriptions-item>
      </el-descriptions>

      <h3>{{ t('pay.detail.attempts') }}</h3>
      <el-timeline v-if="detail?.attempts?.length">
        <el-timeline-item v-for="attempt in detail.attempts" :key="attempt.id" :timestamp="time(attempt.createTime || attempt.bankSubmittedTime)" placement="top">
          <div class="payment-detail__attempt">
            <strong>{{ methodText(t, attempt.channelCode) }}</strong>
            <el-tag v-if="attempt.bankTransferStatus" size="small">{{ bankStatus(attempt.bankTransferStatus) }}</el-tag>
            <span>{{ masked(attempt.channelOrderNo || attempt.bankReferenceNo) }}</span>
            <span v-if="attempt.channelErrorMsg" class="payment-detail__error">{{ attempt.channelErrorMsg }}</span>
            <span v-if="attempt.bankRejectReason" class="payment-detail__error">{{ attempt.bankRejectReason }}</span>
          </div>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else :description="t('pay.detail.noAttempts')" :image-size="72" />

      <template v-if="detail?.receivable">
        <h3>{{ t('pay.receivable.title') }}</h3>
        <el-descriptions :column="3" border>
          <el-descriptions-item :label="t('pay.receivable.amount')">{{ money(detail.receivable.receivableAmount, detail.receivable.currency) }}</el-descriptions-item>
          <el-descriptions-item :label="t('pay.receivable.outstanding')">{{ money(detail.receivable.outstandingAmount, detail.receivable.currency) }}</el-descriptions-item>
          <el-descriptions-item :label="t('pay.receivable.dueDate')">{{ detail.receivable.dueDate || '-' }}</el-descriptions-item>
        </el-descriptions>
        <el-button v-if="canRepay && detail.receivable.status !== 'SETTLED'" class="payment-detail__action" @click="repay">{{ t('pay.credit.repay') }}</el-button>
      </template>

      <template v-if="creditAccount">
        <h3>{{ t('pay.credit.account') }}</h3>
        <el-descriptions :column="3" border>
          <el-descriptions-item :label="t('pay.credit.limit')">{{ money(creditAccount.creditLimit, creditAccount.currency) }}</el-descriptions-item>
          <el-descriptions-item :label="t('pay.credit.used')">{{ money(creditAccount.usedCredit, creditAccount.currency) }}</el-descriptions-item>
          <el-descriptions-item :label="t('common.status')">{{ creditAccount.status || '-' }}</el-descriptions-item>
        </el-descriptions>
        <div class="payment-detail__actions">
          <el-button v-if="canAdjust" @click="adjustCredit">{{ t('pay.credit.adjust') }}</el-button>
          <el-button v-if="canFreeze" :type="creditAccount.status === 'FROZEN' ? 'primary' : 'warning'" plain @click="freezeCredit">{{ creditAccount.status === 'FROZEN' ? t('pay.credit.unfreeze') : t('pay.credit.freeze') }}</el-button>
        </div>
      </template>

      <template v-if="showWebhooks && detail?.webhooks?.length">
        <h3>{{ t('pay.detail.webhooks') }}</h3>
        <el-table :data="detail.webhooks" border size="small">
          <el-table-column prop="eventType" :label="t('pay.detail.eventType')" min-width="190" />
          <el-table-column prop="processStatus" :label="t('common.status')" width="110" />
          <el-table-column :label="t('common.createTime')" width="160"><template #default="{ row }">{{ time(row.receivedTime) }}</template></el-table-column>
          <el-table-column prop="errorMessage" :label="t('pay.detail.error')" min-width="180" show-overflow-tooltip />
        </el-table>
      </template>
    </div>
  </AdminDrawer>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import AdminDrawer from '@/components/AdminDrawer/index.vue'
import { payApi, type CreditAccount, type PayOrder, type PayOrderDetail } from '@/api/pay'
import { checkPermi } from '@/utils/permission'
import { formatUtc } from '@/utils/datetime'
import { bankStatusText, methodText, minorMoney, money, statusText, statusType } from '../payPresentation'

const { t } = useI18n()
const visible = ref(false)
const loading = ref(false)
const order = ref<PayOrder>()
const detail = ref<PayOrderDetail>()
const showWebhooks = checkPermi(['pay:reconcile:query'])
const canAdjust = checkPermi(['pay:credit:adjust'])
const canFreeze = checkPermi(['pay:credit:freeze'])
const canRepay = checkPermi(['pay:credit:repay'])
const creditAccount = ref<CreditAccount>()

function time(value?: string) {
  return value ? formatUtc(value, 'YYYY-MM-DD HH:mm') : '-'
}

function masked(value?: string) {
  if (!value) return '-'
  return value.length > 8 ? `${value.slice(0, 4)}...${value.slice(-4)}` : value
}

function bankStatus(status: string) {
  return bankStatusText(t, status)
}

async function adjustCredit() {
  if (!creditAccount.value?.creditAccountId) return
  const amount = await ElMessageBox.prompt(t('pay.credit.adjustAmount'), t('pay.credit.adjust'), { inputPattern: /^-?\d+(\.\d{1,2})?$/, inputErrorMessage: t('pay.credit.amountInvalid') })
  const reason = await ElMessageBox.prompt(t('pay.reason'), t('pay.credit.adjust'), { inputValidator: (value) => Boolean(value?.trim()) || t('common.required') })
  await payApi.adjustCredit(creditAccount.value.creditAccountId, Number(amount.value), reason.value)
  ElMessage.success(t('pay.credit.adjusted'))
  await reloadCredit()
}

async function freezeCredit() {
  if (!creditAccount.value?.creditAccountId) return
  const frozen = creditAccount.value.status !== 'FROZEN'
  const reason = await ElMessageBox.prompt(t('pay.reason'), t(frozen ? 'pay.credit.freeze' : 'pay.credit.unfreeze'), { inputValidator: (value) => Boolean(value?.trim()) || t('common.required') })
  await payApi.freezeCredit(creditAccount.value.creditAccountId, frozen, reason.value)
  ElMessage.success(t(frozen ? 'pay.credit.frozenSuccess' : 'pay.credit.unfrozenSuccess'))
  await reloadCredit()
}

async function repay() {
  if (!detail.value?.receivable?.receivableId) return
  const reason = await ElMessageBox.prompt(t('pay.reason'), t('pay.credit.repay'), { inputValidator: (value) => Boolean(value?.trim()) || t('common.required') })
  await payApi.repayReceivable(detail.value.receivable.receivableId, reason.value)
  ElMessage.success(t('pay.credit.repaid'))
  if (order.value?.id) detail.value = await payApi.orderDetail(order.value.id)
  await reloadCredit()
}

async function reloadCredit() {
  if (!order.value?.salesDocumentId) return
  creditAccount.value = (await payApi.salesPayment(order.value.salesDocumentId)).creditAccount
}

async function open(row: PayOrder) {
  order.value = row
  detail.value = undefined
  creditAccount.value = undefined
  visible.value = true
  if (!row.id) return
  loading.value = true
  try {
    detail.value = await payApi.orderDetail(row.id)
    order.value = { ...row, no: detail.value.payOrderNo, salesDocumentId: detail.value.salesDocumentId, salesOrderNo: detail.value.salesOrderNo,
      merchantName: detail.value.merchantName, customerName: detail.value.customerName, price: detail.value.price, currency: detail.value.currency,
      channelCode: detail.value.channelCode, status: detail.value.status, channelOrderNo: detail.value.channelOrderNo, successTime: detail.value.successTime }
    await reloadCredit()
  } finally {
    loading.value = false
  }
}

defineExpose({ open })
</script>

<style scoped>
.payment-detail h3 { margin: 18px 0 10px; font-size: 14px; }
.payment-detail__attempt { display: flex; align-items: center; gap: 10px; min-height: 28px; }
.payment-detail__error { color: #d92d20; }
.payment-detail__actions { display: flex; gap: 8px; margin-top: 10px; }
.payment-detail__action { margin-top: 10px; }
</style>
