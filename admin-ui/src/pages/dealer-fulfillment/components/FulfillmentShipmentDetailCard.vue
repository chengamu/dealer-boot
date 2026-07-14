<template>
  <section class="workspace-card">
    <header class="workspace-card__header">
      <h3>{{ t('dealer.fulfillment.editPackage') }}</h3>
    </header>
    <template v-if="shipment">
      <div class="shipment-detail__facts">
        <div><dt>{{ t('dealer.fulfillment.packageNo') }}</dt><dd>{{ shipment.packageNo || shipment.shipmentNo || '-' }}</dd></div>
        <div><dt>{{ t('dealer.fulfillment.carrier') }}</dt><dd>{{ shipment.carrierName || '-' }}</dd></div>
        <div><dt>{{ t('dealer.fulfillment.trackingNo') }}</dt><dd>{{ shipment.trackingNo || '-' }}</dd></div>
        <div><dt>{{ t('common.status') }}</dt><dd>{{ statusText(t, 'package', shipment.status) }}</dd></div>
        <div class="is-wide"><dt>{{ t('common.remark') }}</dt><dd>{{ shipment.remark || '-' }}</dd></div>
      </div>
      <div class="shipment-detail__checks">
        <el-tag effect="plain" :type="productionComplete ? 'success' : 'warning'">
          {{ t(productionComplete ? 'dealer.fulfillment.production.COMPLETED' : 'dealer.fulfillment.productionNotComplete') }}
        </el-tag>
        <el-tag effect="plain" :type="shipment.trackingNo ? 'success' : 'warning'">
          {{ shipment.trackingNo || t('dealer.fulfillment.trackingNo') }}
        </el-tag>
      </div>
      <div class="shipment-detail__actions">
        <el-button v-if="shipment.status === 'DRAFT'" @click="emit('edit', shipment)" v-hasPermi="['dealer:fulfillment:shipment:edit']">
          {{ t('common.edit') }}
        </el-button>
        <el-button v-if="shipment.status === 'DRAFT'" @click="emit('remove', shipment)" v-hasPermi="['dealer:fulfillment:shipment:remove']">
          {{ t('common.delete') }}
        </el-button>
        <el-button v-if="shipment.status === 'DRAFT'" type="primary" @click="emit('dispatch', shipment)" v-hasPermi="['dealer:fulfillment:shipment:dispatch']">
          {{ t('dealer.fulfillment.confirmDispatch') }}
        </el-button>
        <el-button v-if="canSync(shipment)" @click="emit('sync', shipment)" v-hasPermi="['dealer:fulfillment:tracking:sync']">
          {{ t('dealer.fulfillment.syncTracking') }}
        </el-button>
        <el-button v-if="canReceive(shipment)" @click="emit('receipt', shipment)" v-hasPermi="['dealer:fulfillment:receipt:confirm']">
          {{ t('dealer.fulfillment.confirmReceipt') }}
        </el-button>
        <el-button v-if="canReceive(shipment)" @click="emit('override', shipment)" v-hasPermi="['dealer:fulfillment:receipt:override']">
          {{ t('dealer.fulfillment.overrideReceipt') }}
        </el-button>
      </div>
      <el-table :data="shipment.items || []" border size="small">
        <el-table-column prop="lineNo" :label="t('common.index')" width="58" align="center" />
        <el-table-column prop="saleProductName" :label="t('dealer.fulfillment.product')" min-width="170" />
        <el-table-column prop="quantity" :label="t('dealer.fulfillment.quantity')" width="86" align="center" />
      </el-table>
      <div class="shipment-detail__timeline">
        <div v-for="event in shipment.trackingEvents || []" :key="event.trackingEventId" class="shipment-detail__timeline-row">
          <strong>{{ trackingDescription(event, locale) }}</strong>
          <span>{{ event.location || '-' }}</span>
          <small>{{ minute(event.occurredTime) }}</small>
        </div>
        <el-empty v-if="!(shipment.trackingEvents || []).length" :description="t('dealer.fulfillment.noTrackingEvents')" :image-size="56" />
      </div>
    </template>
    <el-empty v-else :description="t('dealer.fulfillment.noPackages')" :image-size="72" />
  </section>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import type { Shipment } from '@/api/dealer-fulfillment'
import { formatUtc } from '@/utils/datetime'
import { statusText, trackingDescription } from '../fulfillmentPresentation'

defineProps<{ shipment?: Shipment; productionComplete: boolean }>()
const emit = defineEmits<{
  edit: [shipment: Shipment]
  remove: [shipment: Shipment]
  dispatch: [shipment: Shipment]
  sync: [shipment: Shipment]
  receipt: [shipment: Shipment]
  override: [shipment: Shipment]
}>()
const { t, locale } = useI18n()

function canSync(shipment: Shipment) {
  return !['DRAFT', 'CANCELLED'].includes(shipment.status || '')
}

function canReceive(shipment: Shipment) {
  return canSync(shipment) && shipment.receiptStatus !== 'CONFIRMED'
}

function minute(value?: string) {
  return value ? formatUtc(value, 'YYYY-MM-DD HH:mm') : '-'
}
</script>

<style scoped>
.workspace-card {
  overflow: hidden;
  border: 1px solid #e9edf5;
  border-radius: 8px;
  background: #fff;
}

.workspace-card__header {
  padding: 14px 16px;
  border-bottom: 1px solid #eef0f5;
}

.workspace-card__header h3 {
  margin: 0;
  font-size: 16px;
}

.shipment-detail__facts {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  padding: 16px;
}

.shipment-detail__facts .is-wide {
  grid-column: 1 / -1;
}

.shipment-detail__facts dt {
  margin-bottom: 6px;
  color: #98a2b3;
  font-size: 12px;
}

.shipment-detail__facts dd {
  margin: 0;
  color: #1d2129;
  line-height: 1.5;
}

.shipment-detail__checks,
.shipment-detail__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 0 16px 16px;
}

.shipment-detail__timeline {
  padding: 12px 16px 16px;
}

.shipment-detail__timeline-row + .shipment-detail__timeline-row {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #eef0f5;
}

.shipment-detail__timeline-row strong,
.shipment-detail__timeline-row span,
.shipment-detail__timeline-row small {
  display: block;
}

.shipment-detail__timeline-row span {
  margin-top: 4px;
  color: #667085;
}

.shipment-detail__timeline-row small {
  margin-top: 4px;
  color: #98a2b3;
}

@media (max-width: 768px) {
  .shipment-detail__facts {
    grid-template-columns: 1fr;
  }
}
</style>
