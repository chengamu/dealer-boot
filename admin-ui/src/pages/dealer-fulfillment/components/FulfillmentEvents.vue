<template>
  <section class="fulfillment-events">
    <h3>{{ t('dealer.fulfillment.operationRecords') }}</h3>
    <el-timeline>
      <el-timeline-item v-for="event in events" :key="event.salesEventId" :timestamp="minute(event.occurredTime)" placement="top">
        <strong>{{ eventLabel(event.eventType) }}</strong>
        <p>{{ event.operatorName || '-' }}<template v-if="event.eventNote"> · {{ event.eventNote }}</template></p>
      </el-timeline-item>
    </el-timeline>
    <el-empty v-if="!events.length" :description="t('dealer.fulfillment.noOperationRecords')" :image-size="44" />
  </section>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import type { FulfillmentEvent } from '@/api/dealer-fulfillment'
import { formatUtc } from '@/utils/datetime'
defineProps<{ events: FulfillmentEvent[] }>()
const { t } = useI18n()
const minute = (value?: string) => value ? formatUtc(value, 'YYYY-MM-DD HH:mm') : '-'
const eventKeys: Record<string, string> = {
  PRODUCTION_STARTED: 'dealer.fulfillment.event.PRODUCTION_STARTED',
  PRODUCTION_COMPLETED: 'dealer.fulfillment.event.PRODUCTION_COMPLETED',
  SHIPMENT_DRAFT_CREATED: 'dealer.fulfillment.event.SHIPMENT_DRAFT_CREATED',
  SHIPMENT_DRAFT_UPDATED: 'dealer.fulfillment.event.SHIPMENT_DRAFT_UPDATED',
  SHIPMENT_DRAFT_DELETED: 'dealer.fulfillment.event.SHIPMENT_DRAFT_DELETED',
  SHIPMENT_DISPATCHED: 'dealer.fulfillment.event.SHIPMENT_DISPATCHED',
  PACKAGE_RECEIVED: 'dealer.fulfillment.event.PACKAGE_RECEIVED',
  PACKAGE_RECEIPT_OVERRIDDEN: 'dealer.fulfillment.event.PACKAGE_RECEIPT_OVERRIDDEN',
  ORDER_COMPLETED: 'dealer.fulfillment.event.ORDER_COMPLETED',
  ORDER_CREATED_FROM_QUOTE: 'dealer.sales.event.ORDER_CREATED_FROM_QUOTE',
  ORDER_CREATED_FROM_QUICK_ORDER: 'dealer.sales.event.ORDER_CREATED_FROM_QUICK_ORDER',
  CANCEL: 'dealer.sales.event.CANCEL',
  PDF_GENERATED: 'dealer.sales.event.PDF_GENERATED',
  EXCEL_EXPORTED: 'dealer.sales.event.EXCEL_EXPORTED',
  EMAIL_SENT: 'dealer.sales.event.EMAIL_SENT',
  EMAIL_FAILED: 'dealer.sales.event.EMAIL_FAILED'
}
function eventLabel(type?: string) {
  if (!type) return '-'
  return t(eventKeys[type] || 'dealer.fulfillment.event.other')
}
</script>

<style scoped>
.fulfillment-events { padding: 10px 12px 0; border: 1px solid #e9edf5; background: #fff; }
.fulfillment-events h3 { margin: 0 0 10px; font-size: 14px; }
.fulfillment-events strong { font-size: 13px; }
.fulfillment-events p { margin: 3px 0 0; color: #667085; font-size: 12px; }
</style>
