<template>
  <el-table v-loading="loading" :data="rows" border>
    <el-table-column type="index" :label="t('common.index')" width="70" align="center" />
    <el-table-column v-if="platform" :label="t('dealer.sales.businessOrigin.label')" width="110" align="center">
      <template #default="{ row }">{{ businessOriginText(t, row.businessOrigin) }}</template>
    </el-table-column>
    <el-table-column :label="t('dealer.sales.source')" min-width="210" show-overflow-tooltip>
      <template #default="{ row }">{{ sourceTypeText(t, row.sourceType) }} · {{ row.sourceNo || row.quoteNo || '-' }}</template>
    </el-table-column>
    <el-table-column prop="orderNo" :label="t('dealer.sales.orderNo')" width="170" show-overflow-tooltip />
    <el-table-column prop="customerName" :label="t('dealer.sales.customer')" min-width="160" show-overflow-tooltip />
    <el-table-column v-if="platform" :label="t('dealer.sales.merchant')" min-width="160" show-overflow-tooltip>
      <template #default="{ row }">{{ row.merchantName || resolveMerchantName(row.tenantId, merchantOptions) }}</template>
    </el-table-column>
    <el-table-column v-if="platform" :label="t('dealer.sales.salesStore')" min-width="160" show-overflow-tooltip>
      <template #default="{ row }">{{ resolveSalesStoreName(row.salesStoreId, salesStoreOptions) }}</template>
    </el-table-column>
    <el-table-column :label="t('dealer.sales.itemCount')" width="90" align="center">
      <template #default="{ row }">{{ row.itemCount || row.items?.length || 0 }}</template>
    </el-table-column>
    <el-table-column :label="t('dealer.sales.totalAmount')" width="140" align="right">
      <template #default="{ row }"><strong>{{ money(row.totalAmount, row.currencyCode) }}</strong></template>
    </el-table-column>
    <el-table-column :label="t('common.status')" width="110" align="center">
      <template #default="{ row }"><el-tag :type="statusType(row.documentStatus)">{{ documentStatusText(t, row.documentStatus) }}</el-tag></template>
    </el-table-column>
    <el-table-column :label="t('dealer.sales.paymentStatus')" width="110" align="center">
      <template #default="{ row }">{{ paymentStatusText(t, row.paymentStatus) }}</template>
    </el-table-column>
    <el-table-column :label="t('dealer.sales.productionStatus')" width="110" align="center">
      <template #default="{ row }">{{ productionStatusText(t, row.productionStatus) }}</template>
    </el-table-column>
    <el-table-column :label="t('dealer.sales.shipmentStatus')" width="120" align="center">
      <template #default="{ row }">{{ shipmentStatusText(t, row.shipmentStatus) }}</template>
    </el-table-column>
    <el-table-column :label="t('dealer.sales.submittedTime')" width="170" align="center">
      <template #default="{ row }">{{ formatUtc(row.submittedTime, 'YYYY-MM-DD HH:mm') }}</template>
    </el-table-column>
    <el-table-column :label="t('common.operate')" width="120" fixed="right" align="center">
      <template #default="{ row }">
        <AdminTableActions :actions="buildActions(row)" />
      </template>
    </el-table-column>
  </el-table>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import type { AdminTableAction } from '@/components/AdminTableActions/index.vue'
import type { SalesDocument, SalesStoreOption } from '@/api/dealer-sales'
import type { MerchantProfileOption } from '@/api/merchant/profile'
import { resolveMerchantName, resolveSalesStoreName } from '@/pages/dealer-sales/components/standard-grid/platformOptionLookup'
import { formatCurrency } from '@/utils/businessNumber'
import { formatUtc } from '@/utils/datetime'
import { businessOriginText, documentStatusText, paymentStatusText, productionStatusText, shipmentStatusText, sourceTypeText } from '@/pages/dealer-sales/salesPresentation'

defineProps<{
  platform: boolean
  loading: boolean
  merchantOptions: MerchantProfileOption[]
  rows: SalesDocument[]
  salesStoreOptions: SalesStoreOption[]
  buildActions: (row: SalesDocument) => AdminTableAction[]
}>()

const { t } = useI18n()

function money(value?: string | null, currency = 'USD') {
  return formatCurrency(value, currency || 'USD')
}

function statusType(status?: string) {
  if (status === 'COMPLETED') return 'success'
  if (status === 'CANCELLED') return 'info'
  return 'primary'
}
</script>
