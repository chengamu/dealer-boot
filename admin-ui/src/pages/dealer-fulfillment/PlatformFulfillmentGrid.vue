<template>
  <div class="app-container fulfillment-grid">
    <el-form :model="query" inline class="fulfillment-grid__search" @submit.prevent>
      <el-form-item :label="t('dealer.fulfillment.businessTime')">
        <el-date-picker v-model="dateRange" type="daterange" value-format="YYYY-MM-DD" />
      </el-form-item>
      <el-form-item :label="t('dealer.fulfillment.businessOrigin.label')">
        <el-select v-model="query.businessOrigin" clearable>
          <el-option :label="t('dealer.fulfillment.businessOrigin.MERCHANT')" value="MERCHANT" />
          <el-option :label="t('dealer.fulfillment.businessOrigin.INTERNAL')" value="INTERNAL" />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('dealer.fulfillment.subject')">
        <el-select v-model="subjectValue" clearable filterable :loading="subjectLoading">
          <el-option v-for="item in subjectOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('dealer.fulfillment.orderNo')"><el-input v-model="query.orderNo" clearable /></el-form-item>
      <el-form-item><el-button type="primary" icon="Search" @click="search">{{ t('common.search') }}</el-button><el-button icon="Refresh" @click="reset">{{ t('common.reset') }}</el-button></el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows" border @row-dblclick="openDetail">
      <el-table-column type="index" :index="indexMethod" :label="t('common.index')" width="58" align="center" />
      <el-table-column prop="orderNo" :label="t('dealer.fulfillment.orderNo')" min-width="170" />
      <el-table-column :label="t('dealer.fulfillment.subject')" min-width="170" show-overflow-tooltip>
        <template #default="{ row }">{{ row.merchantName || row.customerName || '-' }}</template>
      </el-table-column>
      <el-table-column v-if="kind !== 'shipment'" :label="t('dealer.fulfillment.productionStatus')" width="120" align="center">
        <template #default="{ row }"><el-tag :type="statusType(row.productionStatus)">{{ statusText(t, 'production', row.productionStatus) }}</el-tag></template>
      </el-table-column>
      <el-table-column v-if="kind !== 'production'" :label="t('dealer.fulfillment.shipmentStatus')" width="130" align="center">
        <template #default="{ row }"><el-tag :type="statusType(row.shipmentStatus)">{{ statusText(t, 'shipment', row.shipmentStatus) }}</el-tag></template>
      </el-table-column>
      <el-table-column v-if="kind === 'package' || kind === 'tracking'" prop="packageCount" :label="t('dealer.fulfillment.packageCount')" width="90" align="center" />
      <el-table-column v-if="kind !== 'production'" :label="t('dealer.fulfillment.latestLogistics')" min-width="220" show-overflow-tooltip>
        <template #default="{ row }">{{ row.carrierName || '-' }} · {{ row.trackingNo || '-' }}</template>
      </el-table-column>
      <el-table-column :label="timeLabel" width="160" align="center"><template #default="{ row }">{{ formatTime(row) }}</template></el-table-column>
      <el-table-column :label="t('common.operate')" width="100" fixed="right" align="center">
        <template #default="{ row }"><el-button link type="primary" @click="openDetail(row)">{{ t('common.detail') }}</el-button></template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" v-model:page="query.pageNum" v-model:limit="query.pageSize" :total="total" @pagination="load" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { platformFulfillmentApi, type FulfillmentGridKind, type ShipmentOrder, type ShipmentQuery } from '@/api/dealer-fulfillment'
import { withUtcDateRange } from '@/utils/datetime'
import { currentMonthDateRange, formatMinute, useFulfillmentSubjectFilter } from './fulfillmentGridSupport'
import { statusText, statusType } from './fulfillmentPresentation'

const props = defineProps<{ kind: FulfillmentGridKind }>()
const emit = defineEmits<{ detail: [id: string, mode: 'production' | 'shipment' | 'tracking', audience: 'platform'] }>()
const { t } = useI18n()
const loading = ref(false)
const rows = ref<ShipmentOrder[]>([])
const total = ref(0)
const dateRange = ref<string[]>(currentMonthDateRange())
const query = reactive<ShipmentQuery>({ pageNum: 1, pageSize: 20, businessOrigin: '', tenantId: '', salesStoreId: '', orderNo: '' })
const { loading: subjectLoading, subjectOptions, load: loadSubjects } = useFulfillmentSubjectFilter(query)

const subjectValue = computed({
  get: () => query.businessOrigin === 'INTERNAL' ? query.salesStoreId || '' : query.tenantId || '',
  set: (value: string) => {
    if (query.businessOrigin === 'INTERNAL') query.salesStoreId = value
    else query.tenantId = value
  }
})
const timeLabel = computed(() => props.kind === 'production' ? t('dealer.fulfillment.paidTime') : t('dealer.fulfillment.firstShippedTime'))

async function load() {
  loading.value = true
  try {
    const response = await platformFulfillmentApi.list(withUtcDateRange({ ...query }, dateRange.value))
    rows.value = response.rows || []
    total.value = response.total || 0
  } finally {
    loading.value = false
  }
}

function search() {
  query.pageNum = 1
  void load()
}

function reset() {
  Object.assign(query, { pageNum: 1, pageSize: 20, businessOrigin: '', tenantId: '', salesStoreId: '', orderNo: '' })
  dateRange.value = currentMonthDateRange()
  void load()
}

function openDetail(row: ShipmentOrder) {
  const mode = props.kind === 'production' ? 'production' : props.kind === 'tracking' ? 'tracking' : 'shipment'
  emit('detail', String(row.salesDocumentId), mode, 'platform')
}

function formatTime(row: ShipmentOrder) {
  if (props.kind === 'production') return formatMinute(row.paidTime)
  return formatMinute(row.shippedTime)
}

function indexMethod(index: number) {
  return ((query.pageNum || 1) - 1) * (query.pageSize || 20) + index + 1
}

onMounted(async () => {
  await loadSubjects()
  await load()
})
</script>

<style scoped>
.fulfillment-grid {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.fulfillment-grid__search {
  padding: 8px 12px 0;
  border: 1px solid #e9edf5;
  background: #fff;
}

.fulfillment-grid__search :deep(.el-input),
.fulfillment-grid__search :deep(.el-select) {
  width: 160px;
}

.fulfillment-grid__search :deep(.el-date-editor) {
  width: 240px;
}
</style>
