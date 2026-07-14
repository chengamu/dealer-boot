<template>
  <section class="sales-document-progress-tracker">
    <article
      v-for="step in steps"
      :key="step.key"
      class="sales-document-progress-tracker__step"
      :class="`is-${step.state}`"
    >
      <div class="sales-document-progress-tracker__dot" />
      <div class="sales-document-progress-tracker__body">
        <strong>{{ step.label }}</strong>
        <span>{{ step.time ? formatUtc(step.time, 'YYYY-MM-DD HH:mm') : '-' }}</span>
      </div>
    </article>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type { SalesDocument } from '@/api/dealer-sales'
import { formatUtc } from '@/utils/datetime'
import { paymentStatusText, productionStatusText, shipmentStatusText } from '../salesPresentation'

const props = defineProps<{ document: SalesDocument }>()
const { t } = useI18n()

const steps = computed(() => {
  const paymentDone = props.document.paymentStatus === 'PAID'
  const productionDone = props.document.productionStatus === 'COMPLETED'
  const productionWorking = props.document.productionStatus === 'IN_PRODUCTION'
  const shipped = props.document.shipmentStatus === 'SHIPPED' || props.document.shipmentStatus === 'PARTIALLY_SHIPPED'
  const delivered = props.document.shipmentStatus === 'DELIVERED' || props.document.documentStatus === 'COMPLETED'
  return [
    {
      key: 'submitted',
      label: t('dealer.sales.status.SUBMITTED'),
      time: props.document.submittedTime,
      state: 'done'
    },
    {
      key: 'payment',
      label: paymentStatusText(t, props.document.paymentStatus),
      time: props.document.paidTime,
      state: paymentDone ? 'done' : 'current'
    },
    {
      key: 'production',
      label: productionStatusText(t, props.document.productionStatus),
      time: props.document.productionCompleteTime || props.document.productionStartTime,
      state: productionDone ? 'done' : productionWorking ? 'current' : paymentDone ? 'current' : 'pending'
    },
    {
      key: 'shipment',
      label: shipmentStatusText(t, props.document.shipmentStatus),
      time: props.document.shippedTime,
      state: delivered ? 'done' : shipped ? 'current' : productionDone ? 'current' : 'pending'
    },
    {
      key: 'delivery',
      label: delivered ? t('dealer.sales.shipment.DELIVERED') : t('dealer.sales.progress.completed'),
      time: props.document.deliveredTime,
      state: delivered ? 'done' : shipped ? 'current' : 'pending'
    }
  ]
})
</script>

<style scoped>
.sales-document-progress-tracker {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 0;
  padding: 12px 14px;
  border: 1px solid #e4eaf2;
  border-radius: 8px;
  background: #fff;
}
.sales-document-progress-tracker__step {
  position: relative;
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 4px 10px;
}
.sales-document-progress-tracker__step::after {
  content: '';
  position: absolute;
  top: 14px;
  left: calc(100% - 8px);
  width: calc(100% - 4px);
  border-top: 1px dashed #d6deea;
}
.sales-document-progress-tracker__step:last-child::after { display: none; }
.sales-document-progress-tracker__dot {
  position: relative;
  z-index: 1;
  width: 18px;
  height: 18px;
  flex: 0 0 18px;
  border: 2px solid #d0d7e2;
  border-radius: 999px;
  background: #fff;
}
.sales-document-progress-tracker__body {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 4px;
}
.sales-document-progress-tracker__body strong { color: #1d2939; font-size: 14px; }
.sales-document-progress-tracker__body span { color: #667085; font-size: 12px; }
.sales-document-progress-tracker__step.is-done .sales-document-progress-tracker__dot {
  border-color: #16a34a;
  background: #16a34a;
}
.sales-document-progress-tracker__step.is-current .sales-document-progress-tracker__dot {
  border-color: #1677ff;
  background: #eef5ff;
}
@media (max-width: 1320px) {
  .sales-document-progress-tracker { grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 8px 0; }
  .sales-document-progress-tracker__step::after { display: none; }
}
</style>
