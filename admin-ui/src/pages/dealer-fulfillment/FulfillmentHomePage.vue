<template>
  <FulfillmentDetailPage v-if="detailId" :order-id="detailId" :mode="detailMode" @back="closeDetail" />
  <div v-else class="app-container fulfillment-home">
    <header class="fulfillment-home__header">
      <div><h2>{{ t('dealer.fulfillment.title') }}</h2><p>{{ t('dealer.fulfillment.commandHint') }}</p></div>
      <el-segmented v-if="availableViews.length > 1" v-model="activeView" :options="availableViews" />
    </header>
    <ProductionQueue v-if="activeView === 'production'" @detail="openDetail" />
    <ShipmentQueue v-else-if="activeView === 'shipment'" @detail="openDetail" />
    <TrackingQueue v-else @detail="openDetail" />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import { checkPermi } from '@/utils/permission'
import FulfillmentDetailPage from './FulfillmentDetailPage.vue'
import ProductionQueue from './ProductionQueue.vue'
import ShipmentQueue from './ShipmentQueue.vue'
import TrackingQueue from './TrackingQueue.vue'

type ViewMode = 'production' | 'shipment' | 'tracking'
const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const detailId = computed(() => String(route.query.orderId || ''))
const detailMode = computed(() => (route.query.mode || activeView.value) as ViewMode)
const availableViews = computed(() => {
  const views: Array<{ label: string; value: ViewMode }> = []
  if (checkPermi(['dealer:fulfillment:production:list'])) views.push({ label: t('dealer.fulfillment.productionQueue'), value: 'production' })
  if (checkPermi(['dealer:fulfillment:shipment:list'])) views.push({ label: t('dealer.fulfillment.shipmentQueue'), value: 'shipment' })
  if (!views.length || checkPermi(['dealer:fulfillment:receipt:confirm'])) views.push({ label: t('dealer.fulfillment.orderTracking'), value: 'tracking' })
  return views
})
const activeView = ref<ViewMode>(availableViews.value[0]?.value || 'tracking')

watch(availableViews, (views) => {
  if (!views.some((view) => view.value === activeView.value)) activeView.value = views[0]?.value || 'tracking'
})

function openDetail(id: string, mode: ViewMode) {
  void router.push({ query: { ...route.query, orderId: id, mode } })
}
function closeDetail() {
  const query = { ...route.query }
  delete query.orderId
  delete query.mode
  void router.push({ query })
}
</script>

<style scoped>
.fulfillment-home { display: flex; flex-direction: column; gap: 8px; background: var(--admin-bg); }
.fulfillment-home__header { display: flex; min-height: 52px; align-items: center; justify-content: space-between; padding: 8px 12px; border: 1px solid #e9edf5; background: #fff; }
.fulfillment-home__header h2 { margin: 0; color: #1d2129; font-size: 18px; }
.fulfillment-home__header p { margin: 3px 0 0; color: #667085; font-size: 12px; }
:deep(.fulfillment-grid) { display: flex; flex-direction: column; gap: 8px; }
:deep(.fulfillment-grid__search) { padding: 8px 12px 0; border: 1px solid #e9edf5; background: #fff; }
:deep(.fulfillment-grid__search .el-input), :deep(.fulfillment-grid__search .el-select) { width: 150px; }
</style>
