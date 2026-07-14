<template>
  <div class="shipment-workspace">
    <div class="shipment-workspace__main">
      <section class="workspace-card">
        <header class="workspace-card__header">
          <h3>{{ t('dealer.fulfillment.packageAllocation') }}</h3>
        </header>
        <el-table :data="allocationRows" border>
          <el-table-column prop="roomLocation" :label="t('dealer.fulfillment.room')" min-width="140" />
          <el-table-column prop="saleProductName" :label="t('dealer.fulfillment.product')" min-width="190" />
          <el-table-column prop="quantity" :label="t('dealer.fulfillment.orderQuantity')" width="96" align="center" />
          <el-table-column :label="t('dealer.fulfillment.allocatedQuantity')" width="110" align="center">
            <template #default="{ row }">{{ packedQuantity(row.salesItemId) }}</template>
          </el-table-column>
          <el-table-column :label="t('dealer.fulfillment.remainingQuantity')" width="110" align="center">
            <template #default="{ row }">{{ Math.max(0, (row.quantity || 0) - packedQuantity(row.salesItemId)) }}</template>
          </el-table-column>
          <el-table-column :label="t('dealer.fulfillment.thisPackageQuantity')" width="130" align="center">
            <template #default="{ row }">{{ activeShipmentQuantity(row.salesItemId) }}</template>
          </el-table-column>
        </el-table>
      </section>

      <section class="workspace-card">
        <header class="workspace-card__header">
          <h3>{{ t('dealer.fulfillment.packageWorkspace') }}</h3>
          <el-button v-if="canCreate" type="primary" @click="emit('createPackage')" v-hasPermi="['dealer:fulfillment:shipment:add']">
            {{ t('dealer.fulfillment.createPackage') }}
          </el-button>
        </header>
        <div class="shipment-list">
          <button
            v-for="shipment in order.shipments || []"
            :key="shipment.shipmentId"
            type="button"
            class="shipment-list__row"
            :class="{ 'is-active': shipment.shipmentId === activeShipmentId }"
            @click="activeShipmentId = shipment.shipmentId"
          >
            <div class="shipment-list__title">
              <strong>{{ shipment.packageNo || shipment.shipmentNo || '-' }}</strong>
              <el-tag size="small" effect="plain" :type="statusType(shipment.status)">
                {{ statusText(t, 'package', shipment.status) }}
              </el-tag>
            </div>
            <span>{{ shipment.carrierName || '-' }} · {{ shipment.trackingNo || '-' }}</span>
            <small>{{ minute(shipment.shippedTime || shipment.lastTrackingTime || shipment.receivedTime) }}</small>
          </button>
        </div>
      </section>
    </div>

    <aside class="shipment-workspace__side">
      <FulfillmentShipmentDetailCard
        :shipment="activeShipment"
        :production-complete="order.productionStatus === 'COMPLETED'"
        @edit="emit('edit', $event)"
        @remove="emit('remove', $event)"
        @dispatch="emit('dispatch', $event)"
        @sync="emit('sync', $event)"
        @receipt="emit('receipt', $event)"
        @override="emit('override', $event)"
      />
    </aside>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import type { FulfillmentOrder, Shipment } from '@/api/dealer-fulfillment'
import { formatUtc } from '@/utils/datetime'
import { statusText, statusType } from '../fulfillmentPresentation'
import FulfillmentShipmentDetailCard from './FulfillmentShipmentDetailCard.vue'

const props = defineProps<{
  order: FulfillmentOrder
  mode: 'shipment' | 'tracking'
  canCreate: boolean
}>()
const emit = defineEmits<{
  createPackage: []
  edit: [shipment: Shipment]
  remove: [shipment: Shipment]
  dispatch: [shipment: Shipment]
  sync: [shipment: Shipment]
  receipt: [shipment: Shipment]
  override: [shipment: Shipment]
}>()
const { t } = useI18n()
const activeShipmentId = ref<string>()
const activeShipment = computed(() => (props.order.shipments || []).find((item) => item.shipmentId === activeShipmentId.value))
const allocationRows = computed(() => props.order.items || [])

watch(
  () => props.order.shipments,
  (shipments) => {
    const next = (shipments || []).find((item) => item.status === 'DRAFT') || (shipments || [])[0]
    activeShipmentId.value = next?.shipmentId
  },
  { immediate: true, deep: true }
)

function packedQuantity(salesItemId: string) {
  return (props.order.shipments || [])
    .filter((shipment) => shipment.status !== 'CANCELLED')
    .flatMap((shipment) => shipment.items || [])
    .filter((item) => item.salesItemId === salesItemId)
    .reduce((sum, item) => sum + (item.quantity || 0), 0)
}

function activeShipmentQuantity(salesItemId: string) {
  return (activeShipment.value?.items || [])
    .filter((item) => item.salesItemId === salesItemId)
    .reduce((sum, item) => sum + (item.quantity || 0), 0)
}

function minute(value?: string) {
  return value ? formatUtc(value, 'YYYY-MM-DD HH:mm') : '-'
}
</script>

<style scoped>
.shipment-workspace {
  display: grid;
  grid-template-columns: minmax(0, 1.55fr) minmax(360px, 0.9fr);
  gap: 12px;
}

.shipment-workspace__main,
.shipment-workspace__side {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.workspace-card {
  overflow: hidden;
  border: 1px solid #e9edf5;
  border-radius: 8px;
  background: #fff;
}

.workspace-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid #eef0f5;
}

.workspace-card__header h3 {
  margin: 0;
  font-size: 16px;
}

.shipment-list {
  display: flex;
  flex-direction: column;
}

.shipment-list__row {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 14px 16px;
  border: 0;
  border-top: 1px solid #eef0f5;
  background: #fff;
  text-align: left;
  cursor: pointer;
}

.shipment-list__row:first-child {
  border-top: 0;
}

.shipment-list__row.is-active {
  background: #eef5ff;
}

.shipment-list__title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.shipment-list__row span,
.shipment-list__row small {
  color: #667085;
}

.shipment-workspace__facts {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  padding: 16px;
}

@media (max-width: 1200px) {
  .shipment-workspace {
    grid-template-columns: 1fr;
  }
}
</style>
