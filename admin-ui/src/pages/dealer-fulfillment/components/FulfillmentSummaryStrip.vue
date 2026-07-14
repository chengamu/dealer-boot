<template>
  <section class="summary-strip">
    <div>
      <dt>{{ t('dealer.fulfillment.merchant') }}</dt>
      <dd>{{ order.merchantName || '-' }}</dd>
    </div>
    <div>
      <dt>{{ t('dealer.fulfillment.customer') }}</dt>
      <dd>{{ order.customerName || '-' }}</dd>
    </div>
    <div>
      <dt>{{ t('dealer.fulfillment.itemCount') }}</dt>
      <dd>{{ order.itemCount || 0 }}</dd>
    </div>
    <div>
      <dt>{{ t('dealer.fulfillment.totalQuantity') }}</dt>
      <dd>{{ order.totalQuantity || 0 }}</dd>
    </div>
    <div>
      <dt>{{ t('dealer.fulfillment.paidTime') }}</dt>
      <dd>{{ minute(order.paidTime) }}</dd>
    </div>
    <div>
      <dt>{{ mode === 'production' ? t('dealer.fulfillment.productionStartTime') : t('dealer.fulfillment.latestLogistics') }}</dt>
      <dd>{{ mode === 'production' ? minute(order.productionStartTime) : latestLogistics }}</dd>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type { FulfillmentOrder } from '@/api/dealer-fulfillment'
import { formatUtc } from '@/utils/datetime'

const props = defineProps<{ order: FulfillmentOrder; mode: 'production' | 'shipment' | 'tracking' }>()
const { t } = useI18n()

const latestLogistics = computed(() => {
  const shipment = (props.order.shipments || []).find((item) => item.trackingNo || item.carrierName)
  return shipment ? `${shipment.carrierName || '-'} · ${shipment.trackingNo || '-'}` : '-'
})

function minute(value?: string) {
  return value ? formatUtc(value, 'YYYY-MM-DD HH:mm') : '-'
}
</script>

<style scoped>
.summary-strip {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  border: 1px solid #e9edf5;
  border-radius: 8px;
  background: #fff;
}

.summary-strip > div {
  min-width: 0;
  padding: 14px 16px;
  border-right: 1px solid #eef0f5;
}

.summary-strip > div:last-child {
  border-right: 0;
}

dt {
  margin-bottom: 6px;
  color: #98a2b3;
  font-size: 12px;
}

dd {
  margin: 0;
  color: #1d2129;
  line-height: 1.5;
  overflow-wrap: anywhere;
}

@media (max-width: 1200px) {
  .summary-strip {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .summary-strip > div:nth-child(3n) {
    border-right: 0;
  }
}

@media (max-width: 768px) {
  .summary-strip {
    grid-template-columns: 1fr;
  }

  .summary-strip > div {
    border-right: 0;
    border-bottom: 1px solid #eef0f5;
  }

  .summary-strip > div:last-child {
    border-bottom: 0;
  }
}
</style>
