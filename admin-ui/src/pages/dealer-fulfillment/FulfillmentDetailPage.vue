<template>
  <div v-loading="loading" class="fulfillment-detail">
    <header class="fulfillment-detail__topbar">
      <div class="fulfillment-detail__heading"><el-button link :icon="ArrowLeft" :aria-label="t('common.back')" @click="emit('back')" /><div><h2>{{ order.orderNo || '-' }}</h2><p>{{ sourceText(t, order.sourceType) }} · {{ order.sourceNo || '-' }}</p></div><el-tag :type="statusType(order.productionStatus)">{{ statusText(t, 'production', order.productionStatus) }}</el-tag><el-tag :type="statusType(order.shipmentStatus)">{{ statusText(t, 'shipment', order.shipmentStatus) }}</el-tag></div>
      <div class="fulfillment-detail__actions">
        <el-button v-if="mode !== 'tracking'" icon="Document" @click="actions.openProductionSheet" v-hasPermi="['dealer:fulfillment:production:document']">{{ t('dealer.fulfillment.productionSheet') }}</el-button>
        <el-button v-if="canStart" type="primary" icon="VideoPlay" @click="actions.startProduction" v-hasPermi="['dealer:fulfillment:production:start']">{{ t('dealer.fulfillment.startProduction') }}</el-button>
        <el-button v-if="canComplete" type="primary" icon="CircleCheck" @click="actions.completeProduction" v-hasPermi="['dealer:fulfillment:production:complete']">{{ t('dealer.fulfillment.completeProduction') }}</el-button>
        <el-button v-if="canCreate" type="primary" icon="Plus" @click="actions.createPackage" v-hasPermi="['dealer:fulfillment:shipment:add']">{{ t('dealer.fulfillment.createPackage') }}</el-button>
      </div>
    </header>
    <FulfillmentTimeline :order="order" />
    <OrderFacts :order="order" />
    <FulfillmentItems :items="order.items || []" :show-bom="mode !== 'tracking'" />
    <ShipmentWorkspace
      v-if="mode !== 'production' || order.productionStatus === 'COMPLETED'"
      :shipments="order.shipments || []" :total-quantity="order.totalQuantity || 0" :production-complete="order.productionStatus === 'COMPLETED'"
      @edit="actions.editPackage" @remove="actions.removePackage" @dispatch="actions.dispatchPackage"
      @sync="actions.syncTracking" @receipt="actions.confirmReceipt" @override="actions.overrideReceipt"
    />
    <FulfillmentEvents v-if="mode !== 'tracking'" :events="order.events || []" />
    <ShipmentEditorDialog v-model="actions.editorVisible" :items="order.items || []" :shipments="order.shipments || []" :shipment="actions.editingShipment" @save="actions.savePackage" />
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { ArrowLeft } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import { productionApi, shipmentApi, trackingApi, type FulfillmentOrder } from '@/api/dealer-fulfillment'
import FulfillmentEvents from './components/FulfillmentEvents.vue'
import FulfillmentItems from './components/FulfillmentItems.vue'
import FulfillmentTimeline from './components/FulfillmentTimeline.vue'
import OrderFacts from './components/OrderFacts.vue'
import ShipmentEditorDialog from './components/ShipmentEditorDialog.vue'
import ShipmentWorkspace from './components/ShipmentWorkspace.vue'
import { sourceText, statusText, statusType } from './fulfillmentPresentation'
import { useFulfillmentActions } from './useFulfillmentActions'

const props = defineProps<{ orderId: string; mode: 'production' | 'shipment' | 'tracking' }>()
const emit = defineEmits<{ back: [] }>()
const { t } = useI18n()
const loading = ref(false)
const order = reactive<FulfillmentOrder>({ salesDocumentId: props.orderId, items: [], shipments: [], events: [] })
const canStart = computed(() => props.mode === 'production' && order.paymentStatus === 'PAID' && order.productionStatus === 'PENDING')
const canComplete = computed(() => props.mode === 'production' && order.productionStatus === 'IN_PRODUCTION')
const allocatedQuantity = computed(() => (order.shipments || []).filter((item) => item.status !== 'CANCELLED')
  .flatMap((item) => item.items || []).reduce((sum, item) => sum + (item.quantity || 0), 0))
const canCreate = computed(() => props.mode === 'shipment' && order.productionStatus === 'COMPLETED' && allocatedQuantity.value < (order.totalQuantity || 0))

async function load() {
  loading.value = true
  try {
    if (props.mode === 'tracking') {
      const [detail, shipments] = await Promise.all([trackingApi.order(props.orderId), shipmentApi.orderShipments(props.orderId)])
      Object.assign(order, detail.data || {}, { shipments: shipments.data || [] })
    } else {
      Object.assign(order, (await productionApi.detail(props.orderId)).data || {})
    }
  } finally { loading.value = false }
}

const actions = reactive(useFulfillmentActions(order, load, t))
void load()
</script>

<style scoped>
.fulfillment-detail { display: flex; min-height: calc(100vh - 92px); flex-direction: column; gap: 8px; padding: 10px; background: #f3f6fa; }
.fulfillment-detail__topbar, .fulfillment-detail__heading, .fulfillment-detail__actions { display: flex; align-items: center; gap: 8px; }
.fulfillment-detail__topbar { min-height: 48px; justify-content: space-between; }
.fulfillment-detail__heading h2 { margin: 0; color: #1d2129; font-size: 20px; }
.fulfillment-detail__heading p { margin: 2px 0 0; color: #667085; font-size: 12px; }
</style>
