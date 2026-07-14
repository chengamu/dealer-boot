<template>
  <section class="stage-rail">
    <ol class="stage-rail__steps">
      <li v-for="step in steps" :key="step.key" :class="{ done: step.done, active: step.active }">
        <span class="stage-rail__dot">
          <el-icon v-if="step.done"><Check /></el-icon>
        </span>
        <div>
          <strong>{{ step.label }}</strong>
          <small>{{ step.time }}</small>
        </div>
      </li>
    </ol>
    <div class="stage-rail__facts">
      <span v-if="order.productionCompleteTime">{{ t('dealer.fulfillment.productionCompleteTime') }}: {{ minute(order.productionCompleteTime) }}</span>
      <span v-if="firstShippedTime">{{ t('dealer.fulfillment.firstShippedTime') }}: {{ minute(firstShippedTime) }}</span>
      <span v-if="latestShippedTime">{{ t('dealer.fulfillment.latestTrackingTime') }}: {{ minute(latestShippedTime) }}</span>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Check } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import type { FulfillmentOrder } from '@/api/dealer-fulfillment'
import { formatUtc } from '@/utils/datetime'

const props = defineProps<{ order: FulfillmentOrder }>()
const { t } = useI18n()

const firstShippedTime = computed(() => {
  const values = (props.order.shipments || []).map((item) => item.shippedTime).filter(Boolean) as string[]
  return values.sort()[0]
})
const latestShippedTime = computed(() => {
  const values = (props.order.shipments || []).map((item) => item.shippedTime).filter(Boolean) as string[]
  return values.sort().at(-1)
})
const steps = computed(() => {
  const productionStarted = ['IN_PRODUCTION', 'COMPLETED'].includes(props.order.productionStatus || '')
  const productionDone = props.order.productionStatus === 'COMPLETED'
  const shipped = ['PARTIALLY_SHIPPED', 'SHIPPED', 'DELIVERED'].includes(props.order.shipmentStatus || '')
  const transit = (props.order.shipments || []).some((item) => item.status === 'IN_TRANSIT' || item.status === 'SHIPPED')
  const received = props.order.shipmentStatus === 'DELIVERED'
  return [
    { key: 'paid', label: t('dealer.fulfillment.step.paid'), done: props.order.paymentStatus === 'PAID', active: props.order.paymentStatus !== 'PAID', time: minute(props.order.paidTime) },
    { key: 'production', label: t('dealer.fulfillment.step.production'), done: productionDone, active: productionStarted && !productionDone, time: minute(props.order.productionStartTime || props.order.productionCompleteTime) },
    { key: 'shipment', label: t('dealer.fulfillment.step.shipment'), done: shipped, active: productionDone && !shipped, time: minute(firstShippedTime.value) },
    { key: 'transit', label: t('dealer.fulfillment.step.transit'), done: received, active: transit && !received, time: minute(latestShippedTime.value) },
    { key: 'received', label: t('dealer.fulfillment.step.received'), done: received, active: received, time: minute(props.order.deliveredTime) }
  ]
})

function minute(value?: string) {
  return value ? formatUtc(value, 'YYYY-MM-DD HH:mm') : '-'
}
</script>

<style scoped>
.stage-rail {
  border: 1px solid #e9edf5;
  border-radius: 8px;
  background: #fff;
}

.stage-rail__steps {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 16px;
  margin: 0;
  padding: 14px 18px;
  list-style: none;
}

.stage-rail__steps li {
  position: relative;
  display: flex;
  align-items: center;
  gap: 10px;
  color: #98a2b3;
}

.stage-rail__steps li:not(:last-child)::after {
  position: absolute;
  top: 12px;
  right: 10px;
  left: 34px;
  height: 1px;
  background: #d9e0ea;
  content: '';
}

.stage-rail__dot {
  z-index: 1;
  display: grid;
  width: 24px;
  height: 24px;
  flex: 0 0 24px;
  place-items: center;
  border: 2px solid #d9e0ea;
  border-radius: 50%;
  background: #fff;
}

.stage-rail__steps li.done,
.stage-rail__steps li.active {
  color: #1677ff;
}

.stage-rail__steps li.done .stage-rail__dot {
  border-color: #12b76a;
  background: #12b76a;
  color: #fff;
}

.stage-rail__steps li.active .stage-rail__dot {
  border-color: #1677ff;
}

.stage-rail__steps strong,
.stage-rail__steps small {
  display: block;
}

.stage-rail__steps small {
  margin-top: 3px;
  color: #98a2b3;
  font-size: 11px;
}

.stage-rail__facts {
  display: flex;
  flex-wrap: wrap;
  gap: 18px;
  padding: 0 18px 14px;
  color: #667085;
  font-size: 12px;
}

@media (max-width: 1100px) {
  .stage-rail__steps {
    grid-template-columns: 1fr;
  }

  .stage-rail__steps li:not(:last-child)::after {
    display: none;
  }
}
</style>
