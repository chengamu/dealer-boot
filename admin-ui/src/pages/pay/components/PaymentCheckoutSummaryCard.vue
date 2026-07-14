<template>
  <section class="checkout-card">
    <div class="checkout-card__grid">
      <div class="checkout-card__cell">
        <dt>{{ t('pay.customer') }}</dt>
        <dd>{{ order.customerName || '-' }}</dd>
      </div>
      <div class="checkout-card__cell">
        <dt>{{ t('pay.merchant') }}</dt>
        <dd>{{ order.merchantName || '-' }}</dd>
      </div>
      <div class="checkout-card__cell">
        <dt>{{ t('dealer.sales.address') }}</dt>
        <dd>{{ order.shippingAddress || '-' }}</dd>
      </div>
      <div class="checkout-card__cell">
        <dt>{{ t('dealer.sales.customerPo') }}</dt>
        <dd>{{ order.customerPoNo || '-' }}</dd>
      </div>
      <div class="checkout-card__cell">
        <dt>{{ t('dealer.sales.project') }}</dt>
        <dd>{{ order.projectName || '-' }}</dd>
      </div>
      <div class="checkout-card__cell">
        <dt>{{ t('dealer.sales.recipient') }}</dt>
        <dd>{{ order.recipientName || '-' }}</dd>
      </div>
      <div class="checkout-card__cell">
        <dt>{{ t('dealer.sales.phone') }}</dt>
        <dd>{{ order.recipientPhone || '-' }}</dd>
      </div>
      <div class="checkout-card__cell">
        <dt>{{ t('common.status') }}</dt>
        <dd>{{ order.paymentStatus === 'PAID' ? t('pay.status.10') : t('pay.status.0') }}</dd>
      </div>
      <div class="checkout-card__cell">
        <dt>{{ t('pay.salesOrderNo') }}</dt>
        <dd>{{ order.orderNo || '-' }}</dd>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import type { SalesDocument } from '@/api/dealer-sales'

defineProps<{ order: SalesDocument }>()
const { t } = useI18n()
</script>

<style scoped>
.checkout-card {
  border: 1px solid #e9edf5;
  border-radius: 8px;
  background: #fff;
}

.checkout-card__grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.checkout-card__cell {
  min-width: 0;
  padding: 16px 18px 14px;
  border-right: 1px solid #eef0f5;
  border-bottom: 1px solid #eef0f5;
}

.checkout-card__cell:nth-child(3n) {
  border-right: 0;
}

.checkout-card__cell:nth-last-child(-n + 3) {
  border-bottom: 0;
}

dt {
  margin-bottom: 8px;
  color: #98a2b3;
  font-size: 12px;
}

dd {
  margin: 0;
  color: #1d2129;
  font-size: 14px;
  line-height: 1.5;
  overflow-wrap: anywhere;
}

@media (max-width: 900px) {
  .checkout-card__grid {
    grid-template-columns: 1fr;
  }

  .checkout-card__cell,
  .checkout-card__cell:nth-child(3n) {
    border-right: 0;
    border-bottom: 1px solid #eef0f5;
  }

  .checkout-card__cell:last-child {
    border-bottom: 0;
  }
}
</style>
