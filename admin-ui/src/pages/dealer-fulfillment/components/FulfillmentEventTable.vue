<template>
  <section class="event-table-card">
    <header class="event-table-card__header">
      <h3>{{ t('dealer.fulfillment.operationRecords') }}</h3>
    </header>
    <el-table :data="events" border>
      <el-table-column :label="t('common.createTime')" width="170" align="center">
        <template #default="{ row }">{{ minute(row.occurredTime) }}</template>
      </el-table-column>
      <el-table-column :label="t('common.detail')" min-width="220">
        <template #default="{ row }">
          <strong>{{ eventLabel(row.eventType) }}</strong>
          <small v-if="row.operatorName">{{ row.operatorName }}</small>
        </template>
      </el-table-column>
      <el-table-column prop="eventNote" :label="t('common.remark')" min-width="220" show-overflow-tooltip />
    </el-table>
    <el-empty v-if="!events.length" :description="t('dealer.fulfillment.noOperationRecords')" :image-size="64" />
  </section>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import type { FulfillmentEvent } from '@/api/dealer-fulfillment'
import { formatUtc } from '@/utils/datetime'

defineProps<{ events: FulfillmentEvent[] }>()
const { t } = useI18n()

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

function minute(value?: string) {
  return value ? formatUtc(value, 'YYYY-MM-DD HH:mm') : '-'
}
</script>

<style scoped>
.event-table-card {
  overflow: hidden;
  border: 1px solid #e9edf5;
  border-radius: 8px;
  background: #fff;
}

.event-table-card__header {
  padding: 14px 16px;
  border-bottom: 1px solid #eef0f5;
}

.event-table-card__header h3 {
  margin: 0;
  font-size: 16px;
}

strong,
small {
  display: block;
}

small {
  margin-top: 4px;
  color: #98a2b3;
}
</style>
