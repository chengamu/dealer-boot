<template>
  <ol class="fulfillment-timeline">
    <li v-for="step in steps" :key="step.key" :class="{ active: step.active, done: step.done }">
      <span class="fulfillment-timeline__dot"><el-icon v-if="step.done"><Check /></el-icon></span>
      <div><strong>{{ step.label }}</strong><small>{{ step.time }}</small></div>
    </li>
  </ol>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Check } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import type { FulfillmentOrder } from '@/api/dealer-fulfillment'
import { formatUtc } from '@/utils/datetime'

const props = defineProps<{ order: FulfillmentOrder }>()
const { t } = useI18n()
const minute = (value?: string) => value ? formatUtc(value, 'YYYY-MM-DD HH:mm') : ''
const steps = computed(() => {
  const productionStarted = ['IN_PRODUCTION', 'COMPLETED'].includes(props.order.productionStatus || '')
  const productionDone = props.order.productionStatus === 'COMPLETED'
  const shipped = ['PARTIALLY_SHIPPED', 'SHIPPED', 'DELIVERED'].includes(props.order.shipmentStatus || '')
  const received = props.order.shipmentStatus === 'DELIVERED'
  return [
    { key: 'paid', label: t('dealer.fulfillment.step.paid'), done: props.order.paymentStatus === 'PAID', active: !productionStarted, time: minute(props.order.paidTime) },
    { key: 'production', label: t('dealer.fulfillment.step.production'), done: productionDone, active: productionStarted && !productionDone, time: minute(props.order.productionCompleteTime || props.order.productionStartTime) },
    { key: 'shipment', label: t('dealer.fulfillment.step.shipment'), done: shipped, active: productionDone && !shipped, time: '' },
    { key: 'transit', label: t('dealer.fulfillment.step.transit'), done: received, active: shipped && !received, time: '' },
    { key: 'received', label: t('dealer.fulfillment.step.received'), done: received, active: received, time: minute(props.order.deliveredTime) }
  ]
})
</script>

<style scoped>
.fulfillment-timeline { display: grid; margin: 0; padding: 12px 18px; border: 1px solid #e9edf5; background: #fff; grid-template-columns: repeat(5, 1fr); list-style: none; }
.fulfillment-timeline li { position: relative; display: flex; align-items: center; gap: 8px; color: #98a2b3; }
.fulfillment-timeline li:not(:last-child)::after { position: absolute; top: 12px; right: 10px; left: 34px; height: 1px; background: #d9e0ea; content: ''; }
.fulfillment-timeline__dot { z-index: 1; display: grid; width: 24px; height: 24px; flex: 0 0 24px; place-items: center; border: 2px solid #d9e0ea; border-radius: 50%; background: #fff; }
.fulfillment-timeline .done, .fulfillment-timeline .active { color: #1677ff; }
.fulfillment-timeline .done .fulfillment-timeline__dot { border-color: #1677ff; background: #1677ff; color: #fff; }
.fulfillment-timeline .active .fulfillment-timeline__dot { border-color: #1677ff; }
.fulfillment-timeline strong, .fulfillment-timeline small { display: block; font-size: 13px; }
.fulfillment-timeline small { min-height: 16px; margin-top: 2px; color: #98a2b3; font-size: 11px; }
</style>
