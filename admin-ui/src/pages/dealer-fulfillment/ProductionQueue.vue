<template>
  <div class="fulfillment-grid">
    <el-form :model="query" inline class="fulfillment-grid__search">
      <el-form-item :label="t('dealer.fulfillment.orderNo')"><el-input v-model="query.orderNo" clearable /></el-form-item>
      <el-form-item :label="t('dealer.fulfillment.sourceNo')"><el-input v-model="query.sourceNo" clearable /></el-form-item>
      <el-form-item :label="t('dealer.fulfillment.merchant')"><el-input v-model="query.merchantName" clearable /></el-form-item>
      <el-form-item :label="t('dealer.fulfillment.customer')"><el-input v-model="query.customerName" clearable /></el-form-item>
      <el-form-item :label="t('dealer.fulfillment.productionStatus')">
        <el-select v-model="query.productionStatus" clearable>
          <el-option v-for="status in productionStatuses" :key="status" :value="status" :label="statusText(t, 'production', status)" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="load">{{ t('common.search') }}</el-button>
        <el-button icon="Refresh" @click="reset">{{ t('common.reset') }}</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="loading" :data="rows" border @row-dblclick="openDetail">
      <el-table-column type="index" :index="indexMethod" :label="t('common.index')" width="58" align="center" />
      <el-table-column prop="orderNo" :label="t('dealer.fulfillment.orderNo')" min-width="170" />
      <el-table-column :label="t('dealer.fulfillment.source')" min-width="190"><template #default="{ row }">{{ sourceText(t, row.sourceType) }} · {{ row.sourceNo || '-' }}</template></el-table-column>
      <el-table-column prop="merchantName" :label="t('dealer.fulfillment.merchant')" min-width="150" />
      <el-table-column prop="customerName" :label="t('dealer.fulfillment.customer')" min-width="150" />
      <el-table-column prop="projectName" :label="t('dealer.fulfillment.project')" min-width="150" show-overflow-tooltip />
      <el-table-column prop="itemCount" :label="t('dealer.fulfillment.itemCount')" width="88" align="center" />
      <el-table-column prop="totalQuantity" :label="t('dealer.fulfillment.totalQuantity')" width="88" align="center" />
      <el-table-column prop="paymentMethod" :label="t('dealer.fulfillment.paymentMethod')" width="120" />
      <el-table-column :label="t('dealer.fulfillment.paidTime')" width="150"><template #default="{ row }">{{ minute(row.paidTime) }}</template></el-table-column>
      <el-table-column :label="t('dealer.fulfillment.productionStatus')" width="120" align="center"><template #default="{ row }"><el-tag :type="statusType(row.productionStatus)">{{ statusText(t, 'production', row.productionStatus) }}</el-tag></template></el-table-column>
      <el-table-column :label="t('dealer.fulfillment.productionStartTime')" width="150"><template #default="{ row }">{{ minute(row.productionStartTime) }}</template></el-table-column>
      <el-table-column :label="t('dealer.fulfillment.productionCompleteTime')" width="150"><template #default="{ row }">{{ minute(row.productionCompleteTime) }}</template></el-table-column>
      <el-table-column :label="t('common.operate')" width="100" fixed="right"><template #default="{ row }"><el-button link type="primary" @click="openDetail(row)">{{ t('common.detail') }}</el-button></template></el-table-column>
    </el-table>
    <pagination v-show="total > 0" v-model:page="query.pageNum" v-model:limit="query.pageSize" :total="total" @pagination="load" />
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { productionApi, type ProductionOrder, type ProductionQuery, type ProductionStatus } from '@/api/dealer-fulfillment'
import { formatUtc } from '@/utils/datetime'
import { sourceText, statusText, statusType } from './fulfillmentPresentation'

const emit = defineEmits<{ detail: [id: string, mode: 'production'] }>()
const { t } = useI18n()
const productionStatuses: ProductionStatus[] = ['PENDING', 'IN_PRODUCTION', 'COMPLETED']
const query = reactive<ProductionQuery>({ pageNum: 1, pageSize: 10, productionStatus: '' })
const rows = ref<ProductionOrder[]>([])
const total = ref(0)
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const response = await productionApi.list(query)
    rows.value = response.rows || []
    total.value = response.total || 0
  } finally { loading.value = false }
}

function reset() {
  Object.assign(query, { orderNo: '', sourceNo: '', merchantName: '', customerName: '', productionStatus: '', pageNum: 1 })
  void load()
}

function openDetail(row: ProductionOrder) { emit('detail', String(row.salesDocumentId), 'production') }
function indexMethod(index: number) { return ((query.pageNum || 1) - 1) * (query.pageSize || 10) + index + 1 }
function minute(value?: string) { return value ? formatUtc(value, 'YYYY-MM-DD HH:mm') : '-' }
void load()
</script>
