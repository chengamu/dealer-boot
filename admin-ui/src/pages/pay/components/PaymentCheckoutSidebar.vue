<template>
  <aside class="checkout-sidebar">
    <section class="checkout-sidebar__card">
      <h3>{{ t('pay.amountDetail') }} ({{ currency }})</h3>
      <dl class="checkout-sidebar__amounts">
        <div>
          <dt>{{ t('dealer.sales.listAmount') }}</dt>
          <dd>{{ money(order.listAmount, currency) }}</dd>
        </div>
        <div>
          <dt>{{ t('dealer.sales.discountAmount') }}</dt>
          <dd>-{{ money(order.discountAmount, currency) }}</dd>
        </div>
        <div>
          <dt>{{ t('dealer.sales.shippingAmount') }}</dt>
          <dd>{{ money(order.shippingAmount, currency) }}</dd>
        </div>
        <div>
          <dt>{{ t('dealer.sales.taxAmount') }}</dt>
          <dd>{{ money(order.taxAmount, currency) }}</dd>
        </div>
        <div class="checkout-sidebar__total">
          <dt>{{ t('dealer.sales.totalAmount') }}</dt>
          <dd>{{ money(order.totalAmount, currency) }}</dd>
        </div>
      </dl>
    </section>

    <section class="checkout-sidebar__card">
      <h3>{{ t('pay.selectMethod') }}</h3>
      <template v-if="unpaid">
        <div class="checkout-sidebar__methods">
          <button
            v-for="option in methodOptions"
            :key="option.value"
            type="button"
            class="checkout-sidebar__method"
            :class="{ 'is-active': method === option.value }"
            @click="method = option.value"
          >
            {{ option.label }}
          </button>
        </div>
        <div class="checkout-sidebar__panel">
          <PayPalPaymentPanel
            v-if="method === 'paypal'"
            :sales-document-id="payment.salesDocumentId"
            @refresh="forwardRefresh"
          />
          <BankTransferPanel
            v-else-if="method === 'bank'"
            :sales-document-id="payment.salesDocumentId"
            :pay-order-id="payment.payOrder.id"
            :amount="payment.totalAmount"
            :currency="currency"
            @refresh="forwardRefresh"
          />
          <CreditPaymentPanel
            v-else-if="method === 'credit'"
            :sales-document-id="payment.salesDocumentId"
            :amount="payment.totalAmount"
            :currency="currency"
            :account="payment.creditAccount"
            @refresh="forwardRefresh"
          />
          <el-empty v-else :description="t('pay.noMethods')" :image-size="64" />
        </div>
      </template>
      <el-alert v-else :title="t('pay.status.10')" type="success" :closable="false" show-icon />
    </section>

    <section class="checkout-sidebar__card">
      <div class="checkout-sidebar__status-head">
        <h3>{{ t('pay.paymentStatus') }}</h3>
        <el-button text :icon="RefreshRight" :loading="refreshing || polling" @click="forwardRefresh(false)">
          {{ t('common.refresh') }}
        </el-button>
      </div>
      <el-timeline class="checkout-sidebar__timeline">
        <el-timeline-item :timestamp="time(payment.payOrder.createTime)" type="primary">
          {{ t('pay.timeline.created') }}
        </el-timeline-item>
        <el-timeline-item
          v-for="attempt in payment.attempts"
          :key="attempt.id"
          :timestamp="time(attempt.successTime || attempt.bankReviewedTime || attempt.bankSubmittedTime || attempt.createTime)"
          :type="attempt.successTime ? 'success' : 'primary'"
        >
          {{ methodText(t, attempt.channelCode) }}
          <template v-if="attempt.bankTransferStatus">
            · {{ bankStatusText(t, attempt.bankTransferStatus) }}
          </template>
          <template v-else>
            · {{ t('pay.timeline.processing') }}
          </template>
        </el-timeline-item>
        <el-timeline-item v-if="payment.payOrder.status === 10" :timestamp="time(payment.payOrder.successTime)" type="success">
          {{ t('pay.status.10') }}
        </el-timeline-item>
      </el-timeline>
    </section>
  </aside>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { RefreshRight } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import { checkPermi } from '@/utils/permission'
import { formatUtc } from '@/utils/datetime'
import type { SalesDocument } from '@/api/dealer-sales'
import type { SalesPayment } from '@/api/pay'
import { bankStatusText, methodText, money } from '../payPresentation'
import BankTransferPanel from './BankTransferPanel.vue'
import CreditPaymentPanel from './CreditPaymentPanel.vue'
import PayPalPaymentPanel from './PayPalPaymentPanel.vue'

const props = defineProps<{
  order: SalesDocument
  payment: SalesPayment
  refreshing: boolean
  polling: boolean
}>()
const emit = defineEmits<{ refresh: [poll?: boolean] }>()
const { t } = useI18n()
const canPayPal = checkPermi(['pay:order:submit'])
const canBank = checkPermi(['pay:bank:submit'])
const canCredit = checkPermi(['pay:credit:use'])
const method = ref('')

const currency = computed(() => props.order.currencyCode || props.payment.currency || 'USD')
const unpaid = computed(() => props.payment.paymentStatus !== 'PAID' && props.payment.payOrder.status !== 10)
const methodOptions = computed(() => ([
  canPayPal ? { value: 'paypal', label: t('pay.method.paypal') } : undefined,
  canBank ? { value: 'bank', label: t('pay.method.bank') } : undefined,
  canCredit ? { value: 'credit', label: t('pay.method.credit') } : undefined
].filter(Boolean) as Array<{ value: string; label: string }>))

watch(
  () => [unpaid.value, methodOptions.value.map((item) => item.value).join(',')],
  () => {
    if (!unpaid.value) {
      method.value = ''
      return
    }
    if (!methodOptions.value.some((item) => item.value === method.value)) {
      method.value = methodOptions.value[0]?.value || ''
    }
  },
  { immediate: true }
)

function time(value?: string) {
  return value ? formatUtc(value, 'YYYY-MM-DD HH:mm') : '-'
}

function forwardRefresh(poll = false) {
  emit('refresh', poll)
}
</script>

<style scoped>
.checkout-sidebar {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.checkout-sidebar__card {
  border: 1px solid #e9edf5;
  border-radius: 8px;
  background: #fff;
  padding: 16px;
}

.checkout-sidebar__card h3 {
  margin: 0 0 12px;
  color: #1d2129;
  font-size: 16px;
}

.checkout-sidebar__amounts {
  margin: 0;
}

.checkout-sidebar__amounts div {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 0;
  color: #344054;
}

.checkout-sidebar__amounts dt {
  color: #667085;
}

.checkout-sidebar__amounts dd {
  margin: 0;
}

.checkout-sidebar__total {
  margin-top: 6px;
  border-top: 1px solid #eef0f5;
  padding-top: 16px !important;
}

.checkout-sidebar__total dt,
.checkout-sidebar__total dd {
  color: #1677ff;
  font-size: 18px;
  font-weight: 700;
}

.checkout-sidebar__methods {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 0;
  overflow: hidden;
  border: 1px solid #d7e3f7;
  border-radius: 8px;
}

.checkout-sidebar__method {
  height: 40px;
  border: 0;
  border-right: 1px solid #d7e3f7;
  background: #fff;
  color: #344054;
  font-size: 14px;
  cursor: pointer;
}

.checkout-sidebar__method:last-child {
  border-right: 0;
}

.checkout-sidebar__method.is-active {
  background: #eef5ff;
  color: #1677ff;
  box-shadow: inset 0 0 0 1px #1677ff;
}

.checkout-sidebar__panel {
  margin-top: 14px;
}

.checkout-sidebar__status-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.checkout-sidebar__timeline {
  margin-top: 6px;
}

@media (max-width: 560px) {
  .checkout-sidebar__methods {
    grid-template-columns: 1fr;
  }

  .checkout-sidebar__method {
    border-right: 0;
    border-bottom: 1px solid #d7e3f7;
  }

  .checkout-sidebar__method:last-child {
    border-bottom: 0;
  }
}
</style>
