<template>
  <div class="fulfillment-grid">
    <el-form :model="query" inline class="fulfillment-grid__search">
      <el-form-item :label="t('dealer.fulfillment.orderNo')"><el-input v-model="query.orderNo" clearable /></el-form-item>
      <el-form-item :label="t('dealer.fulfillment.customer')"><el-input v-model="query.customerName" clearable /></el-form-item>
      <el-form-item :label="t('dealer.fulfillment.project')"><el-input v-model="query.projectName" clearable /></el-form-item>
      <el-form-item :label="t('dealer.fulfillment.productionStatus')"><el-select v-model="query.productionStatus" clearable><el-option v-for="status in productionStatuses" :key="status" :value="status" :label="statusText(t, 'production', status)" /></el-select></el-form-item>
      <el-form-item :label="t('dealer.fulfillment.shipmentStatus')"><el-select v-model="query.shipmentStatus" clearable><el-option v-for="status in shipmentStatuses" :key="status" :value="status" :label="statusText(t, 'shipment', status)" /></el-select></el-form-item>
      <el-form-item><el-button type="primary" icon="Search" @click="load">{{ t('common.search') }}</el-button><el-button icon="Refresh" @click="reset">{{ t('common.reset') }}</el-button></el-form-item>
    </el-form>
    <el-table v-loading="loading" :data="rows" border @row-dblclick="openDetail">
      <el-table-column type="index" :index="indexMethod" :label="t('common.index')" width="58" align="center" />
      <el-table-column prop="orderNo" :label="t('dealer.fulfillment.orderNo')" min-width="180" />
      <el-table-column prop="projectName" :label="t('dealer.fulfillment.project')" min-width="180" show-overflow-tooltip />
      <el-table-column prop="customerName" :label="t('dealer.fulfillment.customer')" min-width="160" />
      <el-table-column :label="t('dealer.fulfillment.productionStatus')" width="130" align="center"><template #default="{ row }"><el-tag :type="statusType(row.productionStatus)">{{ statusText(t, 'production', row.productionStatus) }}</el-tag></template></el-table-column>
      <el-table-column :label="t('dealer.fulfillment.shipmentStatus')" width="140" align="center"><template #default="{ row }"><el-tag :type="statusType(row.shipmentStatus)">{{ statusText(t, 'shipment', row.shipmentStatus) }}</el-tag></template></el-table-column>
      <el-table-column :label="t('dealer.fulfillment.receiptProgress')" width="110" align="center"><template #default="{ row }">{{ row.receivedPackageCount || 0 }} / {{ row.packageCount || 0 }}</template></el-table-column>
      <el-table-column prop="latestTrackingStatus" :label="t('dealer.fulfillment.latestTrackingStatus')" min-width="220" show-overflow-tooltip />
      <el-table-column :label="t('dealer.fulfillment.latestTrackingTime')" width="160"><template #default="{ row }">{{ minute(row.latestTrackingTime) }}</template></el-table-column>
      <el-table-column :label="t('dealer.fulfillment.submittedTime')" width="160"><template #default="{ row }">{{ minute(row.submittedTime) }}</template></el-table-column>
      <el-table-column :label="t('common.operate')" width="100" fixed="right"><template #default="{ row }"><el-button link type="primary" @click="openDetail(row)">{{ t('dealer.fulfillment.track') }}</el-button></template></el-table-column>
    </el-table>
    <pagination v-show="total > 0" v-model:page="query.pageNum" v-model:limit="query.pageSize" :total="total" @pagination="load" />
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { trackingApi, type ProductionStatus, type ShipmentStatus, type TrackingOrder, type TrackingQuery } from '@/api/dealer-fulfillment'
import { formatUtc } from '@/utils/datetime'
import { statusText, statusType } from './fulfillmentPresentation'
import { enrichTrackingOrders } from './trackingOrderEnrichment'

const emit = defineEmits<{ detail: [id: string, mode: 'tracking'] }>()
const { t } = useI18n()
const productionStatuses: ProductionStatus[] = ['PENDING', 'IN_PRODUCTION', 'COMPLETED']
const shipmentStatuses: ShipmentStatus[] = ['UNSHIPPED', 'PARTIALLY_SHIPPED', 'SHIPPED', 'DELIVERED']
const query = reactive<TrackingQuery>({ pageNum: 1, pageSize: 10, productionStatus: '', shipmentStatus: '' })
const rows = ref<TrackingOrder[]>([])
const total = ref(0)
const loading = ref(false)

async function load() {
  loading.value = true
  try { const response = await trackingApi.listOrders(query); rows.value = await enrichTrackingOrders(response.rows || []); total.value = response.total || 0 }
  finally { loading.value = false }
}
function reset() { Object.assign(query, { orderNo: '', customerName: '', projectName: '', productionStatus: '', shipmentStatus: '', pageNum: 1 }); void load() }
function openDetail(row: TrackingOrder) { emit('detail', String(row.salesDocumentId), 'tracking') }
function indexMethod(index: number) { return ((query.pageNum || 1) - 1) * (query.pageSize || 10) + index + 1 }
function minute(value?: string) { return value ? formatUtc(value, 'YYYY-MM-DD HH:mm') : '-' }
void load()
</script>
