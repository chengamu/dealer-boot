<template>
  <div class="sales-recent-documents">
    <section v-if="dashboard.capabilities.quote" class="sales-dashboard-panel">
      <header>
        <h2>{{ t('dashboard.sales.recentQuotes') }}</h2>
        <el-button link type="primary" @click="emit('open-list', 'quote')">{{ t('dashboard.sales.viewAllQuotes') }}</el-button>
      </header>
      <el-table :data="dashboard.recentQuotes" border size="small" :empty-text="t('dashboard.sales.noData')">
        <el-table-column prop="quoteNo" :label="t('dashboard.sales.quoteNo')" min-width="145" />
        <el-table-column :label="t('dashboard.sales.customerProject')" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="sales-document-name">
              <span>{{ row.customerName || '-' }}</span>
              <span v-if="row.projectName">{{ row.projectName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column :label="t('dashboard.sales.table.quoteStatus')" width="100" align="center">
          <template #default="{ row }"><el-tag size="small" effect="plain" :type="quoteStatusTone(row.status)">{{ quoteStatusText(row.status, t) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="validUntil" :label="t('dashboard.sales.validUntil')" width="118" align="center">
          <template #default="{ row }">{{ row.validUntil || '-' }}</template>
        </el-table-column>
        <el-table-column :label="t('dashboard.sales.table.quoteAmount')" width="130" align="right">
          <template #default="{ row }">{{ formatDashboardMoney(row.totalAmount, row.currencyCode) }}</template>
        </el-table-column>
        <el-table-column :label="t('dashboard.sales.table.conversionStatus')" width="118" align="center">
          <template #default="{ row }">
            <el-tag size="small" effect="plain" :type="row.salesDocumentId ? 'success' : 'info'">
              {{ quoteConversionText(row, t) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('common.operate')" width="78" align="center">
          <template #default="{ row }"><el-button link type="primary" @click="emit('open-quote', row)">{{ t('common.detail') }}</el-button></template>
        </el-table-column>
        <template #empty><el-empty :image="salesOrdersEmpty" :image-size="200" :description="t('dashboard.sales.noData')" /></template>
      </el-table>
    </section>

    <section v-if="dashboard.capabilities.order" class="sales-dashboard-panel">
      <header>
        <h2>{{ t('dashboard.sales.recentOrders') }}</h2>
        <el-button link type="primary" @click="emit('open-list', 'order')">{{ t('dashboard.sales.viewAllOrders') }}</el-button>
      </header>
      <el-table :data="dashboard.recentOrders" border size="small" :empty-text="t('dashboard.sales.noData')">
        <el-table-column prop="orderNo" :label="t('dashboard.sales.orderNo')" min-width="145" />
        <el-table-column :label="t('dashboard.sales.sourceLabel')" width="100" align="center">
          <template #default="{ row }">{{ sourceTypeText(row.sourceType, t) }}</template>
        </el-table-column>
        <el-table-column :label="t('dashboard.sales.customerProject')" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="sales-document-name">
              <span>{{ row.customerName || '-' }}</span>
              <span v-if="row.projectName">{{ row.projectName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column :label="t('dashboard.sales.table.orderStatus')" width="96" align="center">
          <template #default="{ row }"><el-tag size="small" effect="plain" :type="orderStatusTone(row.documentStatus)">{{ orderStatusText(row.documentStatus, t) }}</el-tag></template>
        </el-table-column>
        <el-table-column :label="t('dashboard.sales.table.paymentStatus')" width="92" align="center">
          <template #default="{ row }"><el-tag size="small" effect="plain" :type="paymentStatusTone(row.paymentStatus)">{{ paymentStatusText(row.paymentStatus, t) }}</el-tag></template>
        </el-table-column>
        <el-table-column :label="t('dashboard.sales.table.productionStatus')" width="98" align="center">
          <template #default="{ row }"><el-tag size="small" effect="plain" :type="productionStatusTone(row.productionStatus)">{{ productionStatusText(row.productionStatus, t) }}</el-tag></template>
        </el-table-column>
        <el-table-column :label="t('dashboard.sales.table.shipmentStatus')" width="98" align="center">
          <template #default="{ row }"><el-tag size="small" effect="plain" :type="shipmentStatusTone(row.shipmentStatus)">{{ shipmentStatusText(row.shipmentStatus, t) }}</el-tag></template>
        </el-table-column>
        <el-table-column :label="t('dashboard.sales.table.orderAmount')" width="130" align="right">
          <template #default="{ row }">{{ formatDashboardMoney(row.totalAmount, row.currencyCode) }}</template>
        </el-table-column>
        <el-table-column :label="t('common.operate')" width="78" align="center">
          <template #default="{ row }"><el-button link type="primary" @click="emit('open-order', row)">{{ t('common.detail') }}</el-button></template>
        </el-table-column>
        <template #empty><el-empty :image="salesOrdersEmpty" :image-size="200" :description="t('dashboard.sales.noData')" /></template>
      </el-table>
    </section>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import type { RecentOrder, RecentQuote, SalesDashboard } from '@/api/sales-dashboard'
import salesOrdersEmpty from '@/assets/illustrations/sales-orders-empty.png'
import {
  formatDashboardMoney,
  orderStatusText,
  orderStatusTone,
  paymentStatusText,
  paymentStatusTone,
  productionStatusText,
  productionStatusTone,
  quoteConversionText,
  quoteStatusText,
  quoteStatusTone,
  shipmentStatusText,
  shipmentStatusTone,
  sourceTypeText
} from '../dashboardPresentation'

defineProps<{ dashboard: SalesDashboard }>()
const emit = defineEmits<{
  'open-quote': [quote: RecentQuote]
  'open-order': [order: RecentOrder]
  'open-list': [target: 'quote' | 'order']
}>()
const { t } = useI18n()
</script>

<style scoped>
.sales-recent-documents { display: grid; gap: 16px; min-width: 0; }
.sales-dashboard-panel { overflow: hidden; border: 1px solid var(--sales-border, #e6edf7); border-radius: 12px; background: #fff; box-shadow: var(--sales-shadow, 0 4px 16px rgb(33 83 197 / 5%)); }
.sales-dashboard-panel header { display: flex; align-items: center; justify-content: space-between; height: 52px; padding: 0 18px; border-bottom: 1px solid #eef2f8; }
.sales-dashboard-panel h2 { margin: 0; color: #1f2a44; font-size: 16px; }
.sales-document-name { display: grid; gap: 2px; line-height: 1.35; }
.sales-document-name span:last-child { color: #667085; }
:deep(.el-table) { --el-table-header-bg-color: #f7f9fc; }
:deep(.el-table th.el-table__cell) { height: 46px; color: #475467; font-weight: 600; }
:deep(.el-table td.el-table__cell) { padding-top: 12px; padding-bottom: 12px; }
:deep(.el-tag) { border-radius: 4px; }
:deep(.el-empty) { min-height: 196px; padding: 22px 0 26px; }
:deep(.el-empty__description) { margin-top: 8px; }
</style>
