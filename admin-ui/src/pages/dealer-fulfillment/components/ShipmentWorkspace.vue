<template>
  <section class="fulfillment-section">
    <header><h3>{{ t('dealer.fulfillment.packageWorkspace') }}</h3></header>
    <div class="shipment-summary">
      <span>{{ t('dealer.fulfillment.totalQuantity') }} <strong>{{ totalQuantity }}</strong></span>
      <span>{{ t('dealer.fulfillment.dispatchedQuantity') }} <strong>{{ dispatchedQuantity }}</strong></span>
      <span>{{ t('dealer.fulfillment.remainingQuantity') }} <strong>{{ Math.max(0, totalQuantity - dispatchedQuantity) }}</strong></span>
      <span>{{ t('dealer.fulfillment.packageCount') }} <strong>{{ activeShipments.length }}</strong></span>
    </div>
    <el-empty v-if="!shipments.length" :description="t('dealer.fulfillment.noPackages')" />
    <el-collapse v-else accordion>
      <el-collapse-item v-for="shipment in shipments" :key="shipment.shipmentId" :name="shipment.shipmentId">
        <template #title>
          <div class="shipment-title"><strong>{{ shipment.packageNo || shipment.shipmentNo || '-' }}</strong><el-tag :type="statusType(shipment.status)">{{ statusText(t, 'package', shipment.status) }}</el-tag><span>{{ shipment.carrierName || '-' }} · {{ shipment.trackingNo || '-' }}</span><span>{{ shipment.itemQuantity || packageQuantity(shipment) }} {{ t('dealer.fulfillment.pieces') }}</span></div>
        </template>
        <div class="shipment-actions">
          <el-button v-if="canEdit(shipment)" icon="Edit" @click="emit('edit', shipment)" v-hasPermi="[permission.edit]">{{ t('common.edit') }}</el-button>
          <el-button v-if="canEdit(shipment)" type="danger" plain icon="Delete" @click="emit('remove', shipment)" v-hasPermi="[permission.remove]">{{ t('common.delete') }}</el-button>
          <el-button v-if="canDispatch(shipment)" type="primary" icon="Promotion" @click="emit('dispatch', shipment)" v-hasPermi="[permission.dispatch]">{{ t('dealer.fulfillment.confirmDispatch') }}</el-button>
          <el-button v-if="canSync(shipment)" icon="Refresh" @click="emit('sync', shipment)" v-hasPermi="[permission.sync]">{{ t('dealer.fulfillment.syncTracking') }}</el-button>
          <el-button v-if="canReceive(shipment)" type="primary" icon="CircleCheck" @click="emit('receipt', shipment)" v-hasPermi="[permission.receipt]">{{ t('dealer.fulfillment.confirmReceipt') }}</el-button>
          <el-button v-if="canOverride(shipment)" type="warning" plain icon="CircleCheck" @click="emit('override', shipment)" v-hasPermi="[permission.override]">{{ t('dealer.fulfillment.overrideReceipt') }}</el-button>
        </div>
        <el-alert v-if="shipment.trackingErrorMessage" :title="shipment.trackingErrorMessage" type="error" :closable="false" show-icon />
        <el-table :data="shipment.items || []" border size="small">
          <el-table-column prop="lineNo" :label="t('common.index')" width="58" />
          <el-table-column prop="saleProductName" :label="t('dealer.fulfillment.product')" min-width="180" />
          <el-table-column prop="saleProductCode" :label="t('dealer.fulfillment.productCode')" min-width="130" />
          <el-table-column prop="quantity" :label="t('dealer.fulfillment.quantity')" width="90" align="center" />
        </el-table>
        <TrackingTimeline :events="shipment.trackingEvents || []" />
      </el-collapse-item>
    </el-collapse>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type { FulfillmentAudience, Shipment } from '@/api/dealer-fulfillment'
import { statusText, statusType } from '../fulfillmentPresentation'
import TrackingTimeline from './TrackingTimeline.vue'

const props = defineProps<{ shipments: Shipment[]; totalQuantity: number; productionComplete: boolean; audience: FulfillmentAudience }>()
const emit = defineEmits<{ edit: [shipment: Shipment]; remove: [shipment: Shipment]; dispatch: [shipment: Shipment]; sync: [shipment: Shipment]; receipt: [shipment: Shipment]; override: [shipment: Shipment] }>()
const { t } = useI18n()
const permission = computed(() => {
  if (props.audience === 'business') {
    return {
      edit: 'dealer:fulfillment:progress:noop',
      remove: 'dealer:fulfillment:progress:noop',
      dispatch: 'dealer:fulfillment:progress:noop',
      sync: 'dealer:fulfillment:progress:noop',
      receipt: 'dealer:fulfillment:progress:receipt:confirm',
      override: 'dealer:fulfillment:progress:noop'
    }
  }
  if (props.audience === 'platform') {
    return {
      edit: 'dealer:fulfillment:admin:noop',
      remove: 'dealer:fulfillment:admin:noop',
      dispatch: 'dealer:fulfillment:admin:noop',
      sync: 'dealer:fulfillment:admin:tracking:sync',
      receipt: 'dealer:fulfillment:admin:noop',
      override: 'dealer:fulfillment:admin:receipt:override'
    }
  }
  return {
    edit: 'dealer:fulfillment:factory:shipment:edit',
    remove: 'dealer:fulfillment:factory:shipment:remove',
    dispatch: 'dealer:fulfillment:factory:shipment:dispatch',
    sync: 'dealer:fulfillment:factory:tracking:sync',
    receipt: 'dealer:fulfillment:factory:noop',
    override: 'dealer:fulfillment:factory:noop'
  }
})
const activeShipments = computed(() => props.shipments.filter((item) => item.status !== 'CANCELLED'))
const dispatchedQuantity = computed(() => activeShipments.value.filter((item) => item.status !== 'DRAFT').reduce((sum, item) => sum + packageQuantity(item), 0))
const packageQuantity = (shipment: Shipment) => (shipment.items || []).reduce((sum, item) => sum + (item.quantity || 0), 0)
const canEdit = (shipment: Shipment) => props.audience === 'factory' && shipment.status === 'DRAFT'
const canDispatch = (shipment: Shipment) => props.audience === 'factory' && shipment.status === 'DRAFT'
const canSync = (shipment: Shipment) => props.audience !== 'business' && !['DRAFT', 'CANCELLED'].includes(shipment.status || '')
const canReceive = (shipment: Shipment) => props.audience === 'business' && !['DRAFT', 'CANCELLED'].includes(shipment.status || '') && shipment.receiptStatus !== 'CONFIRMED'
const canOverride = (shipment: Shipment) => props.audience === 'platform' && !['DRAFT', 'CANCELLED'].includes(shipment.status || '') && shipment.receiptStatus !== 'CONFIRMED'
</script>

<style scoped>
.fulfillment-section { border: 1px solid #e9edf5; background: #fff; }
.fulfillment-section > header { display: flex; min-height: 42px; align-items: center; justify-content: space-between; padding: 0 12px; border-bottom: 1px solid #eef0f5; }
.fulfillment-section h3 { margin: 0; font-size: 14px; }
.shipment-summary, .shipment-title, .shipment-actions { display: flex; align-items: center; gap: 12px; }
.shipment-summary { padding: 9px 12px; background: #f8fbff; color: #667085; }
.shipment-summary strong { margin-left: 4px; color: #1d2129; }
.shipment-title { width: 100%; min-width: 0; }
.shipment-title > span { color: #667085; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.shipment-actions { justify-content: flex-end; margin-bottom: 8px; }
:deep(.el-collapse-item__content) { padding: 8px 12px 12px; }
</style>
