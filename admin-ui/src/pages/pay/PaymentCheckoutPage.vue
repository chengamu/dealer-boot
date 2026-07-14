<template>
  <div v-loading="loading" class="payment-checkout-page">
    <header class="payment-checkout-page__header">
      <div class="payment-checkout-page__heading">
        <div class="payment-checkout-page__title-row">
          <h1>{{ t('pay.checkout.title', { no: order?.orderNo || '-' }) }}</h1>
          <el-tag
            v-if="order?.documentStatus"
            type="success"
            effect="plain"
            round
          >
            {{ documentStatusText(order.documentStatus) }}
          </el-tag>
          <el-tag
            v-if="payment"
            :type="paymentTagType"
            effect="plain"
            round
          >
            {{ paymentStatusText }}
          </el-tag>
        </div>
        <div v-if="order" class="payment-checkout-page__meta">
          <span>{{ t('common.createTime') }} {{ time(order.createTime) }}</span>
          <span>{{ t('pay.source') }}: {{ order.sourceNo || order.quoteNo || '-' }}</span>
        </div>
      </div>
      <el-button :icon="ArrowLeft" @click="emit('back')">
        {{ t('pay.backToList') }}
      </el-button>
    </header>

    <template v-if="order && payment">
      <div class="payment-checkout-page__layout">
        <div class="payment-checkout-page__main">
          <PaymentCheckoutSummaryCard :order="order" />
          <PaymentCheckoutItemsCard :order="order" />
          <PaymentCheckoutDeliveryCard :order="order" />
        </div>
        <PaymentCheckoutSidebar
          :order="order"
          :payment="payment"
          :refreshing="refreshing"
          :polling="polling"
          @refresh="refresh"
        />
      </div>
    </template>
    <el-empty v-else-if="!loading" :description="t('pay.checkout.notFound')" />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ArrowLeft } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import { salesApi, type SalesDocument } from '@/api/dealer-sales'
import { payApi, type SalesPayment } from '@/api/pay'
import { formatUtc } from '@/utils/datetime'
import PaymentCheckoutDeliveryCard from './components/PaymentCheckoutDeliveryCard.vue'
import PaymentCheckoutItemsCard from './components/PaymentCheckoutItemsCard.vue'
import PaymentCheckoutSidebar from './components/PaymentCheckoutSidebar.vue'
import PaymentCheckoutSummaryCard from './components/PaymentCheckoutSummaryCard.vue'
import { usePaymentPolling } from './composables/usePaymentPolling'

const props = defineProps<{ salesDocumentId: string }>()
const emit = defineEmits<{ back: [] }>()
const { t } = useI18n()
const loading = ref(false)
const refreshing = ref(false)
const order = ref<SalesDocument>()
const payment = ref<SalesPayment>()
const { polling, start, stop } = usePaymentPolling(() => refresh(false))

const paymentTagType = computed(() => (
  payment.value?.paymentStatus === 'PAID' || payment.value?.payOrder.status === 10 ? 'success' : 'warning'
))
const paymentStatusText = computed(() => (
  payment.value?.paymentStatus === 'PAID' || payment.value?.payOrder.status === 10
    ? t('pay.status.10')
    : t('pay.status.0')
))

function time(value?: string) {
  return value ? formatUtc(value, 'YYYY-MM-DD HH:mm') : '-'
}

function documentStatusText(status?: string) {
  if (status === 'SUBMITTED') return t('dealer.sales.status.SUBMITTED')
  if (status === 'CANCELLED') return t('dealer.sales.status.CANCELLED')
  if (status === 'COMPLETED') return t('dealer.sales.status.COMPLETED')
  return '-'
}

async function refresh(poll = false) {
  if (!props.salesDocumentId) return
  refreshing.value = true
  try {
    payment.value = await payApi.salesPayment(props.salesDocumentId)
    const paid = payment.value.paymentStatus === 'PAID' || payment.value.payOrder.status === 10 || payment.value.payOrder.status === 20
    if (paid) stop()
    else if (poll) start()
  } finally {
    refreshing.value = false
  }
}

async function load() {
  if (!props.salesDocumentId) return
  loading.value = true
  stop()
  try {
    const [salesResponse, paymentResponse] = await Promise.all([
      salesApi.get(props.salesDocumentId),
      payApi.salesPayment(props.salesDocumentId)
    ])
    order.value = salesResponse.data
    payment.value = paymentResponse
    if (payment.value.payOrder.status === 5) start()
  } finally {
    loading.value = false
  }
}

watch(() => props.salesDocumentId, () => { void load() }, { immediate: true })
</script>

<style scoped>
.payment-checkout-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 10px;
  background: var(--admin-bg);
}

.payment-checkout-page__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.payment-checkout-page__heading {
  min-width: 0;
}

.payment-checkout-page__title-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
}

.payment-checkout-page__title-row h1 {
  margin: 0;
  color: #1d2129;
  font-size: 20px;
  line-height: 1.3;
}

.payment-checkout-page__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 18px;
  margin-top: 8px;
  color: #667085;
  font-size: 13px;
}

.payment-checkout-page__layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 460px;
  gap: 12px;
  align-items: start;
}

.payment-checkout-page__main {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 0;
}

@media (max-width: 1280px) {
  .payment-checkout-page__layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .payment-checkout-page__header {
    flex-direction: column;
  }

  .payment-checkout-page__meta {
    gap: 8px 14px;
  }
}
</style>
