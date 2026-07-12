<template>
  <div class="fulfillment-grid">
    <el-form :model="query" inline class="fulfillment-grid__search">
      <el-form-item :label="t('dealer.fulfillment.orderNo')"><el-input v-model="query.orderNo" clearable /></el-form-item>
      <el-form-item :label="t('dealer.fulfillment.merchant')"><el-input v-model="query.merchantName" clearable /></el-form-item>
      <el-form-item :label="t('dealer.fulfillment.customer')"><el-input v-model="query.customerName" clearable /></el-form-item>
      <el-form-item :label="t('dealer.fulfillment.shipmentStatus')"><el-select v-model="query.shipmentStatus" clearable><el-option v-for="status in shipmentStatuses" :key="status" :value="status" :label="statusText(t, 'shipment', status)" /></el-select></el-form-item>
      <el-form-item :label="t('dealer.fulfillment.carrier')"><el-input v-model="query.carrierName" clearable /></el-form-item>
      <el-form-item :label="t('dealer.fulfillment.trackingNo')"><el-input v-model="query.trackingNo" clearable /></el-form-item>
      <el-form-item><el-button type="primary" icon="Search" @click="load">{{ t('common.search') }}</el-button><el-button icon="Refresh" @click="reset">{{ t('common.reset') }}</el-button></el-form-item>
    </el-form>
    <el-table v-loading="loading" :data="rows" border @row-dblclick="openDetail">
      <el-table-column type="index" :index="indexMethod" :label="t('common.index')" width="58" align="center" />
      <el-table-column prop="orderNo" :label="t('dealer.fulfillment.orderNo')" min-width="170" />
      <el-table-column prop="merchantName" :label="t('dealer.fulfillment.merchant')" min-width="150" />
      <el-table-column prop="customerName" :label="t('dealer.fulfillment.customer')" min-width="150" />
      <el-table-column prop="projectName" :label="t('dealer.fulfillment.project')" min-width="150" show-overflow-tooltip />
      <el-table-column :label="t('dealer.fulfillment.quantityProgress')" width="110" align="center"><template #default="{ row }">{{ row.dispatchedQuantity || 0 }} / {{ row.totalQuantity || 0 }}</template></el-table-column>
      <el-table-column prop="packageCount" :label="t('dealer.fulfillment.packageCount')" width="90" align="center" />
      <el-table-column :label="t('dealer.fulfillment.shipmentStatus')" width="130" align="center"><template #default="{ row }"><el-tag :type="statusType(row.shipmentStatus)">{{ statusText(t, 'shipment', row.shipmentStatus) }}</el-tag></template></el-table-column>
      <el-table-column :label="t('dealer.fulfillment.latestLogistics')" min-width="220"><template #default="{ row }">{{ row.carrierName || '-' }} · {{ row.trackingNo || '-' }}</template></el-table-column>
      <el-table-column :label="t('dealer.fulfillment.productionCompleteTime')" width="150"><template #default="{ row }">{{ minute(row.productionCompleteTime) }}</template></el-table-column>
      <el-table-column :label="t('dealer.fulfillment.firstShippedTime')" width="150"><template #default="{ row }">{{ minute(row.shippedTime) }}</template></el-table-column>
      <el-table-column :label="t('common.operate')" width="100" fixed="right"><template #default="{ row }"><el-button link type="primary" @click="openDetail(row)">{{ t('common.detail') }}</el-button></template></el-table-column>
    </el-table>
    <pagination v-show="total > 0" v-model:page="query.pageNum" v-model:limit="query.pageSize" :total="total" @pagination="load" />
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { shipmentApi, type ShipmentOrder, type ShipmentQuery, type ShipmentStatus } from '@/api/dealer-fulfillment'
import { formatUtc } from '@/utils/datetime'
import { statusText, statusType } from './fulfillmentPresentation'

const emit = defineEmits<{ detail: [id: string, mode: 'shipment'] }>()
const { t } = useI18n()
const shipmentStatuses: ShipmentStatus[] = ['UNSHIPPED', 'PARTIALLY_SHIPPED', 'SHIPPED', 'DELIVERED']
const query = reactive<ShipmentQuery>({ pageNum: 1, pageSize: 10, shipmentStatus: '' })
const rows = ref<ShipmentOrder[]>([])
const total = ref(0)
const loading = ref(false)

async function load() {
  loading.value = true
  try { const response = await shipmentApi.list(query); rows.value = response.rows || []; total.value = response.total || 0 }
  finally { loading.value = false }
}
function reset() { Object.assign(query, { orderNo: '', merchantName: '', customerName: '', shipmentStatus: '', carrierName: '', trackingNo: '', pageNum: 1 }); void load() }
function openDetail(row: ShipmentOrder) { emit('detail', String(row.salesDocumentId), 'shipment') }
function indexMethod(index: number) { return ((query.pageNum || 1) - 1) * (query.pageSize || 10) + index + 1 }
function minute(value?: string) { return value ? formatUtc(value, 'YYYY-MM-DD HH:mm') : '-' }
void load()
</script>
