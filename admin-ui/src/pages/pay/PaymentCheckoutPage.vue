<template>
  <div v-loading="loading" class="payment-checkout">
    <header class="payment-checkout__header">
      <div>
        <h2>{{ t('pay.checkout.title', { no: order?.orderNo || '-' }) }}</h2>
        <div v-if="order" class="payment-checkout__meta">
          <el-tag type="success" effect="plain">{{ t('dealer.sales.status.SUBMITTED') }}</el-tag>
          <el-tag :type="payment?.paymentStatus === 'PAID' ? 'success' : 'warning'" effect="plain">{{ payment?.paymentStatus === 'PAID' ? t('pay.status.10') : t('pay.status.0') }}</el-tag>
          <span>{{ t('common.createTime') }} {{ time(order.createTime) }}</span>
          <span>{{ t('pay.source') }} {{ order.sourceNo || order.quoteNo || '-' }}</span>
        </div>
      </div>
      <el-button icon="ArrowLeft" @click="emit('back')">{{ t('pay.backToList') }}</el-button>
    </header>

    <div v-if="order && payment" class="payment-checkout__layout">
      <CheckoutOrderReview :order="order" />
      <CheckoutPaymentPane :order="order" :payment="payment" :refreshing="refreshing" @refresh="refresh" />
    </div>
    <el-empty v-else-if="!loading" :description="t('pay.checkout.notFound')" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { salesApi, type SalesDocument } from '@/api/dealer-sales'
import { payApi, type SalesPayment } from '@/api/pay'
import { formatUtc } from '@/utils/datetime'
import CheckoutOrderReview from './components/CheckoutOrderReview.vue'
import CheckoutPaymentPane from './components/CheckoutPaymentPane.vue'
import { usePaymentPolling } from './composables/usePaymentPolling'

const props = defineProps<{ salesDocumentId: string }>()
const emit = defineEmits<{ back: [] }>()
const { t } = useI18n()
const loading = ref(false)
const refreshing = ref(false)
const order = ref<SalesDocument>()
const payment = ref<SalesPayment>()
const { start, stop } = usePaymentPolling(() => refresh(false))

function time(value?: string) { return value ? formatUtc(value, 'YYYY-MM-DD HH:mm') : '-' }

async function refresh(poll = false) {
  refreshing.value = true
  try {
    payment.value = await payApi.salesPayment(props.salesDocumentId)
    if (payment.value.paymentStatus === 'PAID' || payment.value.payOrder.status === 10 || payment.value.payOrder.status === 20) stop()
    else if (poll) start()
  } finally { refreshing.value = false }
}

onMounted(async () => {
  loading.value = true
  try {
    const [salesResponse, paymentResponse] = await Promise.all([salesApi.get(props.salesDocumentId), payApi.salesPayment(props.salesDocumentId)])
    order.value = salesResponse.data
    payment.value = paymentResponse
    if (payment.value.payOrder.status === 5) start()
  } finally { loading.value = false }
})
</script>

<style scoped>
.payment-checkout { padding: 10px; }
.payment-checkout__header { display: flex; justify-content: space-between; gap: 16px; margin-bottom: 10px; }
.payment-checkout__header h2 { margin: 0 0 8px; font-size: 20px; }
.payment-checkout__meta { display: flex; flex-wrap: wrap; align-items: center; gap: 8px; color: #667085; font-size: 13px; }
.payment-checkout__layout { display: grid; grid-template-columns: minmax(0, 1fr) 410px; gap: 12px; align-items: start; }
@media (max-width: 1100px) { .payment-checkout__layout { grid-template-columns: 1fr; } }
</style>
