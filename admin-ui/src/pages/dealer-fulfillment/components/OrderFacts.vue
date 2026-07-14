<template>
  <section class="fulfillment-section">
    <h3>{{ t('dealer.fulfillment.orderFacts') }}</h3>
    <dl class="fulfillment-facts">
      <div><dt>{{ t('dealer.fulfillment.sourceLabel') }}</dt><dd>{{ sourceText(t, order.sourceType) }} · {{ order.sourceNo || '-' }}</dd></div>
      <div><dt>{{ t('dealer.fulfillment.merchant') }}</dt><dd>{{ order.merchantName || '-' }}</dd></div>
      <div><dt>{{ t('dealer.fulfillment.customer') }}</dt><dd>{{ order.customerName || '-' }}</dd></div>
      <div><dt>{{ t('dealer.fulfillment.project') }}</dt><dd>{{ order.projectName || '-' }}</dd></div>
      <div><dt>{{ t('dealer.fulfillment.paymentMethod') }}</dt><dd>{{ order.paymentMethod || '-' }}</dd></div>
      <div><dt>{{ t('dealer.fulfillment.paidTime') }}</dt><dd>{{ minute(order.paidTime) }}</dd></div>
      <div><dt>{{ t('dealer.fulfillment.recipient') }}</dt><dd>{{ order.recipientName || '-' }} · {{ order.recipientPhone || '-' }}</dd></div>
      <div class="wide"><dt>{{ t('dealer.fulfillment.shippingAddress') }}</dt><dd>{{ order.shippingAddress || '-' }}</dd></div>
    </dl>
  </section>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import type { FulfillmentOrder } from '@/api/dealer-fulfillment'
import { formatUtc } from '@/utils/datetime'
import { sourceText } from '../fulfillmentPresentation'
defineProps<{ order: FulfillmentOrder }>()
const { t } = useI18n()
const minute = (value?: string) => value ? formatUtc(value, 'YYYY-MM-DD HH:mm') : '-'
</script>

<style scoped>
.fulfillment-section { border: 1px solid #e9edf5; background: #fff; }
.fulfillment-section h3 { margin: 0; padding: 9px 12px; border-bottom: 1px solid #eef0f5; font-size: 14px; }
.fulfillment-facts { display: grid; margin: 0; padding: 10px 12px; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 10px 18px; }
.fulfillment-facts div { min-width: 0; }
.fulfillment-facts .wide { grid-column: span 2; }
.fulfillment-facts dt { margin-bottom: 3px; color: #98a2b3; font-size: 12px; }
.fulfillment-facts dd { margin: 0; color: #344054; font-size: 13px; overflow-wrap: anywhere; }
</style>
