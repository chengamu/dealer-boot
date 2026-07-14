<template>
  <el-table v-loading="loading" :data="rows" border>
    <el-table-column type="index" :label="t('common.index')" width="70" align="center" />
    <el-table-column v-if="platform" :label="t('dealer.sales.businessOrigin.label')" width="110" align="center">
      <template #default="{ row }">{{ businessOriginText(t, row.businessOrigin) }}</template>
    </el-table-column>
    <el-table-column prop="quickOrderNo" :label="t('dealer.quickOrder.no')" width="170" show-overflow-tooltip />
    <el-table-column prop="customerName" :label="t('dealer.quickOrder.customerLabel')" min-width="180" show-overflow-tooltip />
    <el-table-column v-if="platform" :label="t('dealer.sales.merchant')" min-width="160" show-overflow-tooltip>
      <template #default="{ row }">{{ resolveMerchantName(row.tenantId, merchantOptions) }}</template>
    </el-table-column>
    <el-table-column v-if="platform" :label="t('dealer.sales.salesStore')" min-width="160" show-overflow-tooltip>
      <template #default="{ row }">{{ resolveSalesStoreName(row.salesStoreId, salesStoreOptions) }}</template>
    </el-table-column>
    <el-table-column prop="ownerName" :label="t('dealer.sales.owner')" width="130" show-overflow-tooltip />
    <el-table-column :label="t('dealer.quickOrder.itemCount')" width="100" align="center">
      <template #default="{ row }">{{ row.itemCount || row.itemTypeCount || row.items?.length || 0 }}</template>
    </el-table-column>
    <el-table-column :label="t('dealer.quickOrder.totalQuantity')" width="100" align="center">
      <template #default="{ row }">{{ row.totalQuantity || 0 }}</template>
    </el-table-column>
    <el-table-column :label="t('dealer.quickOrder.productAmount')" width="140" align="right">
      <template #default="{ row }">{{ money(row.productAmount, row.currencyCode) }}</template>
    </el-table-column>
    <el-table-column :label="t('dealer.quickOrder.shippingAmount')" width="130" align="right">
      <template #default="{ row }">{{ money(row.shippingAmount, row.currencyCode) }}</template>
    </el-table-column>
    <el-table-column :label="t('dealer.quickOrder.totalAmount')" width="140" align="right">
      <template #default="{ row }"><strong>{{ money(row.totalAmount, row.currencyCode) }}</strong></template>
    </el-table-column>
    <el-table-column :label="t('common.status')" width="110" align="center">
      <template #default="{ row }">
        <el-tag :type="row.status === 'ORDERED' ? 'success' : 'warning'">
          {{ row.status === 'ORDERED' ? t('dealer.quickOrder.status.ORDERED') : t('dealer.quickOrder.status.DRAFT') }}
        </el-tag>
      </template>
    </el-table-column>
    <el-table-column prop="orderNo" :label="t('dealer.quickOrder.orderNo')" width="150" show-overflow-tooltip>
      <template #default="{ row }">{{ row.orderNo || '-' }}</template>
    </el-table-column>
    <el-table-column :label="t('dealer.quickOrder.updatedTime')" width="170" align="center">
      <template #default="{ row }">{{ formatUtc(row.updateTime, 'YYYY-MM-DD HH:mm') }}</template>
    </el-table-column>
    <el-table-column :label="t('common.operate')" width="220" fixed="right" align="center">
      <template #default="{ row }">
        <AdminTableActions :actions="buildActions(row)" />
      </template>
    </el-table-column>
  </el-table>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import type { AdminTableAction } from '@/components/AdminTableActions/index.vue'
import type { SalesStoreOption } from '@/api/dealer-sales'
import type { QuickOrder } from '@/api/dealer-sales/quick-order'
import type { MerchantProfileOption } from '@/api/merchant/profile'
import { resolveMerchantName, resolveSalesStoreName } from '@/pages/dealer-sales/components/standard-grid/platformOptionLookup'
import { businessOriginText } from '@/pages/dealer-sales/salesPresentation'
import { formatUtc } from '@/utils/datetime'
import { formatCurrency } from '@/utils/businessNumber'

defineProps<{
  platform: boolean
  loading: boolean
  merchantOptions: MerchantProfileOption[]
  rows: QuickOrder[]
  salesStoreOptions: SalesStoreOption[]
  buildActions: (row: QuickOrder) => AdminTableAction[]
}>()

const { t } = useI18n()

function money(value?: string | null, currency = 'USD') {
  return formatCurrency(value, currency || 'USD')
}
</script>
