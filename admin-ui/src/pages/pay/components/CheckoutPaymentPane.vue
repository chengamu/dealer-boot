<template>
  <aside class="checkout-payment">
    <h3>{{ t('pay.amountDetail') }} ({{ order.currencyCode || 'USD' }})</h3>
    <dl class="checkout-payment__amounts">
      <div><dt>{{ t('dealer.sales.listAmount') }}</dt><dd>{{ money(order.listAmount, currency) }}</dd></div>
      <div><dt>{{ t('dealer.sales.discountAmount') }}</dt><dd>-{{ money(order.discountAmount, currency) }}</dd></div>
      <div><dt>{{ t('dealer.sales.shippingAmount') }}</dt><dd>{{ money(order.shippingAmount, currency) }}</dd></div>
      <div><dt>{{ t('dealer.sales.taxAmount') }}</dt><dd>{{ money(order.taxAmount, currency) }}</dd></div>
      <div class="checkout-payment__total"><dt>{{ t('dealer.sales.totalAmount') }}</dt><dd>{{ money(order.totalAmount, currency) }}</dd></div>
    </dl>

    <template v-if="payment.paymentStatus !== 'PAID' && payment.payOrder.status !== 10">
      <h3>{{ t('pay.selectMethod') }}</h3>
      <el-radio-group v-model="method" class="checkout-payment__methods">
        <el-radio-button v-if="canPayPal" value="paypal">{{ t('pay.method.paypal') }}</el-radio-button>
        <el-radio-button v-if="canBank" value="bank">{{ t('pay.method.bank') }}</el-radio-button>
        <el-radio-button v-if="canCredit" value="credit">{{ t('pay.method.credit') }}</el-radio-button>
      </el-radio-group>
      <PayPalPaymentPanel v-if="method === 'paypal'" :sales-document-id="payment.salesDocumentId" @refresh="forwardRefresh" />
      <BankTransferPanel v-else-if="method === 'bank'" :sales-document-id="payment.salesDocumentId" :pay-order-id="payment.payOrder.id" :amount="payment.totalAmount" :currency="currency" @refresh="forwardRefresh" />
      <CreditPaymentPanel v-else-if="method === 'credit'" :sales-document-id="payment.salesDocumentId" :amount="payment.totalAmount" :currency="currency" :account="payment.creditAccount" @refresh="forwardRefresh" />
      <el-empty v-else :description="t('pay.noMethods')" :image-size="64" />
    </template>
    <el-result v-else icon="success" :title="t('pay.status.10')" :sub-title="time(payment.payOrder.successTime)" />

    <section class="checkout-payment__status">
      <div class="checkout-payment__status-title">
        <h3>{{ t('pay.paymentStatus') }}</h3>
        <el-button text icon="Refresh" :loading="refreshing" @click="forwardRefresh(false)">{{ t('common.refresh') }}</el-button>
      </div>
      <el-timeline>
        <el-timeline-item :timestamp="time(payment.payOrder.createTime)" type="primary">{{ t('pay.timeline.created') }}</el-timeline-item>
        <el-timeline-item v-for="attempt in payment.attempts" :key="attempt.id" :timestamp="time(attempt.createTime)">
          {{ methodText(t, attempt.channelCode) }} · {{ attempt.bankTransferStatus ? bankStatusText(t, attempt.bankTransferStatus) : t('pay.timeline.processing') }}
        </el-timeline-item>
        <el-timeline-item v-if="payment.payOrder.status === 10" :timestamp="time(payment.payOrder.successTime)" type="success">{{ t('pay.status.10') }}</el-timeline-item>
      </el-timeline>
    </section>
  </aside>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import type { SalesDocument } from '@/api/dealer-sales'
import type { SalesPayment } from '@/api/pay'
import { checkPermi } from '@/utils/permission'
import { formatUtc } from '@/utils/datetime'
import { bankStatusText, methodText, money } from '../payPresentation'
import PayPalPaymentPanel from './PayPalPaymentPanel.vue'
import BankTransferPanel from './BankTransferPanel.vue'
import CreditPaymentPanel from './CreditPaymentPanel.vue'

const props = defineProps<{ order: SalesDocument; payment: SalesPayment; refreshing: boolean }>()
const emit = defineEmits<{ refresh: [poll?: boolean] }>()
const { t } = useI18n()
const canPayPal = checkPermi(['pay:order:submit'])
const canBank = checkPermi(['pay:bank:submit'])
const canCredit = checkPermi(['pay:credit:use'])
const method = ref(canPayPal ? 'paypal' : canBank ? 'bank' : canCredit ? 'credit' : '')
const currency = computed(() => props.order.currencyCode || props.payment.currency || 'USD')

function time(value?: string) { return value ? formatUtc(value, 'YYYY-MM-DD HH:mm') : '-' }
function forwardRefresh(poll = false) { emit('refresh', poll) }
</script>

<style scoped>
.checkout-payment { padding: 14px; border: 1px solid #e9edf5; background: #fff; }
.checkout-payment h3 { margin: 0 0 10px; font-size: 14px; }
.checkout-payment__amounts { margin: 0 0 14px; }
.checkout-payment__amounts div { display: flex; justify-content: space-between; padding: 6px 0; }
.checkout-payment__amounts dt { color: #667085; }
.checkout-payment__amounts dd { margin: 0; }
.checkout-payment__total { margin-top: 6px; padding-top: 12px !important; border-top: 1px solid #e9edf5; font-size: 18px; font-weight: 700; color: #1677ff; }
.checkout-payment__methods { display: grid; grid-template-columns: repeat(3, 1fr); margin-bottom: 12px; }
.checkout-payment__methods :deep(.el-radio-button__inner) { width: 100%; }
.checkout-payment__status { margin-top: 16px; padding-top: 12px; border-top: 1px solid #e9edf5; }
.checkout-payment__status-title { display: flex; justify-content: space-between; align-items: center; }
</style>
