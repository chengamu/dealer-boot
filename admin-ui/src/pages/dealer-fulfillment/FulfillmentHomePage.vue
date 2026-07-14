<template>
  <FulfillmentDetailPage v-if="detailId" :order-id="detailId" :mode="detailMode" :audience="pageMode.audience" @back="closeDetail" />
  <BusinessFulfillmentGrid v-else-if="pageMode.audience === 'business'" :kind="pageMode.kind" @detail="openDetail" />
  <PlatformFulfillmentGrid v-else-if="pageMode.audience === 'platform'" :kind="pageMode.kind" @detail="openDetail" />
  <ProductionQueue v-else-if="pageMode.kind === 'production'" @detail="openDetail" />
  <ShipmentQueue v-else-if="pageMode.kind === 'shipment' || pageMode.kind === 'package'" @detail="openDetail" />
  <TrackingQueue v-else @detail="openDetail" />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import type { FulfillmentAudience } from '@/api/dealer-fulfillment'
import BusinessFulfillmentGrid from './BusinessFulfillmentGrid.vue'
import FulfillmentDetailPage from './FulfillmentDetailPage.vue'
import PlatformFulfillmentGrid from './PlatformFulfillmentGrid.vue'
import ProductionQueue from './ProductionQueue.vue'
import ShipmentQueue from './ShipmentQueue.vue'
import TrackingQueue from './TrackingQueue.vue'
import { resolveFulfillmentPageMode } from './fulfillmentPageModes'

type ViewMode = 'production' | 'shipment' | 'tracking'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const detailId = computed(() => String(route.query.orderId || ''))
const pageMode = computed(() => resolveFulfillmentPageMode(route, userStore.permissions))
const detailMode = computed(() => {
  const value = String(route.query.mode || pageMode.value.kind)
  return value === 'tracking' ? 'tracking' : value === 'shipment' ? 'shipment' : 'production'
})

function openDetail(id: string, mode: ViewMode, audience: FulfillmentAudience) {
  void router.push({ query: { ...route.query, orderId: id, mode, audience } })
}

function closeDetail() {
  const query = { ...route.query }
  delete query.orderId
  delete query.mode
  delete query.audience
  void router.push({ query })
}
</script>
