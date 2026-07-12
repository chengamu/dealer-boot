<template>
  <div class="app-container quick-order-list">
    <el-form :model="query" inline class="quick-order-list__search">
      <el-form-item :label="t('dealer.quickOrder.no')"><el-input v-model="query.quickOrderNo" clearable /></el-form-item>
      <el-form-item :label="t('dealer.quickOrder.customer')"><el-input v-model="query.customerName" clearable /></el-form-item>
      <el-form-item :label="t('common.status')">
        <el-select v-model="query.status" clearable>
          <el-option :label="t('dealer.quickOrder.status.DRAFT')" value="DRAFT" />
          <el-option :label="t('dealer.quickOrder.status.ORDERED')" value="ORDERED" />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('common.updateTime')"><el-date-picker v-model="dateRange" type="daterange" value-format="YYYY-MM-DD" /></el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">{{ t('common.search') }}</el-button>
        <el-button @click="reset">{{ t('common.reset') }}</el-button>
      </el-form-item>
    </el-form>

    <div class="quick-order-list__toolbar">
      <el-button v-hasPermi="['dealer:quick-order:add']" type="primary" @click="openWorkbench()">{{ t('dealer.quickOrder.create') }}</el-button>
    </div>

    <el-table v-loading="loading" :data="rows" border>
      <el-table-column type="index" :label="t('common.index')" width="58" align="center" />
      <el-table-column prop="quickOrderNo" :label="t('dealer.quickOrder.no')" min-width="170" />
      <el-table-column prop="customerName" :label="t('dealer.quickOrder.customer')" min-width="180" />
      <el-table-column :label="t('dealer.quickOrder.itemCount')" width="110" align="center">
        <template #default="{ row }">{{ row.itemCount || row.items?.length || 0 }}</template>
      </el-table-column>
      <el-table-column :label="t('dealer.quickOrder.totalQuantity')" width="110" align="center">
        <template #default="{ row }">{{ row.totalQuantity || 0 }}</template>
      </el-table-column>
      <el-table-column :label="t('dealer.quickOrder.productAmount')" width="140" align="right">
        <template #default="{ row }">{{ money(row.productAmount) }}</template>
      </el-table-column>
      <el-table-column :label="t('dealer.quickOrder.shippingAmount')" width="140" align="right">
        <template #default="{ row }">{{ money(row.shippingAmount) }}</template>
      </el-table-column>
      <el-table-column :label="t('dealer.quickOrder.totalAmount')" width="150" align="right">
        <template #default="{ row }"><strong>{{ money(row.totalAmount) }}</strong></template>
      </el-table-column>
      <el-table-column :label="t('common.status')" width="110" align="center">
        <template #default="{ row }"><el-tag :type="row.status === 'ORDERED' ? 'success' : 'warning'">{{ row.status === 'ORDERED' ? t('dealer.quickOrder.status.ORDERED') : t('dealer.quickOrder.status.DRAFT') }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="orderNo" :label="t('dealer.quickOrder.orderNo')" min-width="150">
        <template #default="{ row }">{{ row.orderNo || '-' }}</template>
      </el-table-column>
      <el-table-column :label="t('common.updateTime')" width="170">
        <template #default="{ row }">{{ formatUtc(row.updateTime, 'YYYY-MM-DD HH:mm') }}</template>
      </el-table-column>
      <el-table-column :label="t('common.operate')" width="220" fixed="right">
        <template #default="{ row }">
          <AdminTableActions :actions="rowActions(row)" />
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total" v-model:page="query.pageNum" v-model:limit="query.pageSize" :total="total" @pagination="load" />
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { type AdminTableAction } from '@/components/AdminTableActions/index.vue'
import auth from '@/plugins/auth'
import { quickOrderApi, type QuickOrder, type QuickOrderQuery } from '@/api/dealer-sales/quick-order'
import { usePermissionStore } from '@/stores/permission'
import { formatUtc, withUtcDateRange } from '@/utils/datetime'
import { quickOrderRouteComponents, resolveRoutePath } from './quickOrderRoutes'

const { t } = useI18n()
const router = useRouter()
const permissionStore = usePermissionStore()
const query = reactive<QuickOrderQuery>({ pageNum: 1, pageSize: 10, status: '' })
const dateRange = ref<string[]>([])
const rows = ref<QuickOrder[]>([])
const total = ref(0)
const loading = ref(false)
const workbenchPath = computed(() => resolveRoutePath(permissionStore.routers, quickOrderRouteComponents.workbench))

async function load() {
  loading.value = true
  try {
    const response = await quickOrderApi.list(withUtcDateRange(query, dateRange.value, 'beginUpdateTime', 'endUpdateTime'))
    rows.value = response.rows || []
    total.value = response.total || 0
  } finally {
    loading.value = false
  }
}

function reset() {
  Object.assign(query, { quickOrderNo: '', customerName: '', status: '', pageNum: 1, pageSize: 10 })
  dateRange.value = []
  void load()
}

function openWorkbench(row?: Partial<QuickOrder>) {
  void router.push({ path: workbenchPath.value, query: row?.quickOrderId ? { quickOrderId: row.quickOrderId } : undefined })
}

async function copyDraft(row: QuickOrder) {
  const quickOrderId = String((await quickOrderApi.copy(String(row.quickOrderId || ''))).data || '')
  ElMessage.success(t('common.operationSuccess'))
  openWorkbench({ quickOrderId })
}

async function removeDraft(row: QuickOrder) {
  await ElMessageBox.confirm(t('common.deleteConfirm'), t('common.prompt'), { type: 'warning' })
  await quickOrderApi.remove(String(row.quickOrderId || ''))
  ElMessage.success(t('common.operationSuccess'))
  await load()
}

function viewOrder(row: QuickOrder) {
  if (!row.salesDocumentId) return
  void router.push({ name: 'SalesDocumentDetail', params: { id: row.salesDocumentId } })
}

function money(value?: number) {
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(value || 0)
}

function rowActions(row: QuickOrder): AdminTableAction[] {
  const draft = row.status !== 'ORDERED'
  return [
    { label: t('dealer.quickOrder.continue'), hidden: !draft || !auth.hasPermi('dealer:quick-order:edit'), onClick: () => openWorkbench(row) },
    { label: t('common.copy'), hidden: !auth.hasPermi('dealer:quick-order:add'), onClick: () => copyDraft(row) },
    { label: t('common.delete'), type: 'danger', hidden: !draft || !auth.hasPermi('dealer:quick-order:remove'), onClick: () => removeDraft(row) },
    { label: t('customer.quote.action.viewOrder'), hidden: !row.salesDocumentId || !auth.hasPermi('dealer:sales:query'), onClick: () => viewOrder(row) }
  ]
}

void load()
</script>

<style scoped>
.quick-order-list { display: flex; flex-direction: column; gap: 8px; }
.quick-order-list__search,
.quick-order-list__toolbar { padding: 8px 12px; border: 1px solid #e3e9f2; background: #fff; }
.quick-order-list__toolbar { display: flex; justify-content: flex-start; }
</style>
