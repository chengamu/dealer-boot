<template>
  <div v-loading="loading" class="fulfillment-detail-page">
    <FulfillmentDetailHeader
      :order="order"
      :mode="mode"
      :can-start="canStart"
      :can-complete="canComplete"
      :can-create="canCreate"
      @back="emit('back')"
      @open-sheet="actions.openProductionSheet"
      @start="actions.startProduction"
      @complete="actions.completeProduction"
      @create-package="actions.createPackage"
    />
    <FulfillmentStageRail :order="order" />
    <FulfillmentSummaryStrip :order="order" :mode="mode" />

    <FulfillmentProductionWorkspace
      v-if="mode === 'production'"
      :order="order"
      :can-start="canStart"
      :can-complete="canComplete"
      @open-sheet="actions.openProductionSheet"
      @start="actions.startProduction"
      @complete="actions.completeProduction"
    />
    <FulfillmentShipmentWorkspace
      v-else
      :order="order"
      :mode="mode"
      :can-create="canCreate"
      @create-package="actions.createPackage"
      @edit="actions.editPackage"
      @remove="actions.removePackage"
      @dispatch="actions.dispatchPackage"
      @sync="actions.syncTracking"
      @receipt="actions.confirmReceipt"
      @override="actions.overrideReceipt"
    />

    <FulfillmentEventTable :events="order.events || []" />
    <ShipmentEditorDialog
      v-model="actions.editorVisible"
      :items="order.items || []"
      :shipments="order.shipments || []"
      :shipment="actions.editingShipment"
      @save="actions.savePackage"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  businessFulfillmentApi,
  platformFulfillmentApi,
  productionApi,
  shipmentApi,
  type FulfillmentAudience,
  type FulfillmentOrder
} from '@/api/dealer-fulfillment'
import ShipmentEditorDialog from './components/ShipmentEditorDialog.vue'
import FulfillmentDetailHeader from './components/FulfillmentDetailHeader.vue'
import FulfillmentEventTable from './components/FulfillmentEventTable.vue'
import FulfillmentProductionWorkspace from './components/FulfillmentProductionWorkspace.vue'
import FulfillmentShipmentWorkspace from './components/FulfillmentShipmentWorkspace.vue'
import FulfillmentStageRail from './components/FulfillmentStageRail.vue'
import FulfillmentSummaryStrip from './components/FulfillmentSummaryStrip.vue'
import { useFulfillmentActions } from './useFulfillmentActions'

const props = defineProps<{ orderId: string; mode: 'production' | 'shipment' | 'tracking'; audience: FulfillmentAudience }>()
const emit = defineEmits<{ back: [] }>()
const { t } = useI18n()
const loading = ref(false)
const order = reactive<FulfillmentOrder>({ salesDocumentId: props.orderId, items: [], shipments: [], events: [] })

const canStart = computed(() => props.audience === 'factory' && props.mode === 'production' && order.paymentStatus === 'PAID' && order.productionStatus === 'PENDING')
const canComplete = computed(() => props.audience === 'factory' && props.mode === 'production' && order.productionStatus === 'IN_PRODUCTION')
const allocatedQuantity = computed(() => (order.shipments || [])
  .filter((item) => item.status !== 'CANCELLED')
  .flatMap((item) => item.items || [])
  .reduce((sum, item) => sum + (item.quantity || 0), 0))
const canCreate = computed(() => props.audience === 'factory' && props.mode === 'shipment' && order.productionStatus === 'COMPLETED' && allocatedQuantity.value < (order.totalQuantity || 0))

async function load() {
  loading.value = true
  try {
    if (props.audience === 'business') {
      const [detail, shipments] = await Promise.all([
        businessFulfillmentApi.detail(props.orderId),
        businessFulfillmentApi.orderShipments(props.orderId)
      ])
      Object.assign(order, detail.data || {}, { shipments: shipments?.data || [] })
    } else if (props.audience === 'platform') {
      Object.assign(order, (await platformFulfillmentApi.detail(props.orderId)).data || {})
    } else {
      const api = props.mode === 'production' ? productionApi : shipmentApi
      Object.assign(order, (await api.detail(props.orderId)).data || {})
    }
  } finally {
    loading.value = false
  }
}

const actions = reactive(useFulfillmentActions(order, load, t, props.audience))
watch(() => [props.orderId, props.mode, props.audience], () => { void load() }, { immediate: true })
</script>

<style scoped>
.fulfillment-detail-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 10px;
  background: var(--admin-bg);
}
</style>
