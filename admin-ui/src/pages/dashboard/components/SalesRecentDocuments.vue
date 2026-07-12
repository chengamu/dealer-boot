<template>
  <div class="sales-recent-documents">
    <section v-if="dashboard.capabilities.quote" class="sales-dashboard-panel">
      <header><h2>{{ t('dashboard.sales.recentQuotes') }}</h2><el-button link type="primary" @click="emit('open-list', 'quote')">{{ t('common.more') }}</el-button></header>
      <el-table :data="dashboard.recentQuotes" border size="small" :empty-text="t('dashboard.sales.noData')">
        <el-table-column prop="quoteNo" :label="t('dashboard.sales.quoteNo')" min-width="145" />
        <el-table-column :label="t('dashboard.sales.customerProject')" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ documentName(row.customerName, row.projectName) }}</template>
        </el-table-column>
        <el-table-column :label="t('common.status')" width="105" align="center">
          <template #default="{ row }"><el-tag :type="quoteTone(row.status)">{{ quoteStatus(row.status) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="validUntil" :label="t('dashboard.sales.validUntil')" width="112" align="center" />
        <el-table-column :label="t('dashboard.sales.amount')" width="125" align="right">
          <template #default="{ row }">{{ money(row.totalAmount, row.currencyCode) }}</template>
        </el-table-column>
        <el-table-column v-if="dashboard.capabilities.quoteDetail" :label="t('common.operate')" width="78" align="center">
          <template #default="{ row }"><el-button link type="primary" @click="emit('open-quote', row)">{{ t('common.detail') }}</el-button></template>
        </el-table-column>
      </el-table>
    </section>

    <section v-if="dashboard.capabilities.order" class="sales-dashboard-panel">
      <header><h2>{{ t('dashboard.sales.recentOrders') }}</h2><el-button link type="primary" @click="emit('open-list', 'order')">{{ t('common.more') }}</el-button></header>
      <el-table :data="dashboard.recentOrders" border size="small" :empty-text="t('dashboard.sales.noData')">
        <el-table-column prop="orderNo" :label="t('dashboard.sales.orderNo')" min-width="145" />
        <el-table-column :label="t('dashboard.sales.sourceLabel')" width="100" align="center">
          <template #default="{ row }">{{ sourceType(row.sourceType) }}</template>
        </el-table-column>
        <el-table-column :label="t('dashboard.sales.customerProject')" min-width="165" show-overflow-tooltip>
          <template #default="{ row }">{{ documentName(row.customerName, row.projectName) }}</template>
        </el-table-column>
        <el-table-column :label="t('dashboard.sales.orderFacts')" min-width="250">
          <template #default="{ row }">
            <span class="sales-order-facts">{{ orderStatus(row.documentStatus) }} · {{ paymentStatus(row.paymentStatus) }} · {{ productionStatus(row.productionStatus) }} · {{ shipmentStatus(row.shipmentStatus) }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="t('dashboard.sales.amount')" width="125" align="right">
          <template #default="{ row }">{{ money(row.totalAmount, row.currencyCode) }}</template>
        </el-table-column>
        <el-table-column v-if="dashboard.capabilities.orderDetail" :label="t('common.operate')" width="78" align="center">
          <template #default="{ row }"><el-button link type="primary" @click="emit('open-order', row)">{{ t('common.detail') }}</el-button></template>
        </el-table-column>
      </el-table>
    </section>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import type { RecentOrder, RecentQuote, SalesDashboard } from '@/api/sales-dashboard'
import { formatCurrency } from '@/utils/businessNumber'

defineProps<{ dashboard: SalesDashboard }>()
const emit = defineEmits<{
  'open-quote': [quote: RecentQuote]
  'open-order': [order: RecentOrder]
  'open-list': [target: 'quote' | 'order']
}>()
const { t } = useI18n()

function documentName(customer?: string, project?: string) { return [customer, project].filter(Boolean).join(' · ') || '-' }
function money(value?: string | null, currency = 'USD') { return formatCurrency(value, currency) }
function quoteTone(status: string) { return status === 'CONFIRMED' ? 'success' : status === 'VOID' ? 'info' : 'warning' }
function quoteStatus(status: string) {
  if (status === 'CONFIRMED') return t('dashboard.sales.status.quoteConfirmed')
  if (status === 'VOID') return t('dashboard.sales.status.quoteVoid')
  return t('dashboard.sales.status.quoteDraft')
}
function sourceType(value: string) { return value === 'QUICK_ORDER' ? t('dashboard.sales.source.quickOrder') : t('dashboard.sales.source.quote') }
function orderStatus(value: string) {
  if (value === 'COMPLETED') return t('dashboard.sales.status.completed')
  if (value === 'CANCELLED') return t('dashboard.sales.status.cancelled')
  return t('dashboard.sales.status.submitted')
}
function paymentStatus(value: string) { return value === 'PAID' ? t('dashboard.sales.status.paid') : t('dashboard.sales.status.unpaid') }
function productionStatus(value: string) {
  if (value === 'COMPLETED') return t('dashboard.sales.status.productionCompleted')
  if (value === 'IN_PRODUCTION') return t('dashboard.sales.status.inProduction')
  return t('dashboard.sales.status.productionPending')
}
function shipmentStatus(value: string) {
  if (value === 'DELIVERED') return t('dashboard.sales.status.delivered')
  if (value === 'SHIPPED') return t('dashboard.sales.status.shipped')
  if (value === 'PARTIALLY_SHIPPED') return t('dashboard.sales.status.partiallyShipped')
  return t('dashboard.sales.status.unshipped')
}
</script>

<style scoped>
.sales-recent-documents { display: grid; gap: 10px; min-width: 0; }
.sales-dashboard-panel { overflow: hidden; border: 1px solid #e9edf5; border-radius: 7px; background: #fff; }
.sales-dashboard-panel header { display: flex; align-items: center; justify-content: space-between; height: 42px; padding: 0 12px; border-bottom: 1px solid #eef0f5; }
.sales-dashboard-panel h2 { margin: 0; color: #1d2129; font-size: 14px; }
.sales-order-facts { color: #475467; font-size: 12px; white-space: nowrap; }
:deep(.el-table) { --el-table-header-bg-color: #f7f9fc; }
</style>
