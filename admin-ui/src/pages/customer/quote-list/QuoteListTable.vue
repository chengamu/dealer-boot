<template>
  <el-table v-loading="loading" :data="rows" border>
    <el-table-column type="index" :label="t('common.index')" width="70" align="center" />
    <el-table-column v-if="platform" :label="t('dealer.sales.businessOrigin.label')" width="110" align="center">
      <template #default="{ row }">{{ businessOriginText(t, row.businessOrigin) }}</template>
    </el-table-column>
    <el-table-column :label="t('customer.quote.no')" prop="quoteNo" width="180" show-overflow-tooltip />
    <el-table-column :label="t('customer.quote.customer')" min-width="180" show-overflow-tooltip>
      <template #default="{ row }">{{ row.companyName || row.customerName || '-' }}</template>
    </el-table-column>
    <el-table-column v-if="platform" :label="t('dealer.sales.merchant')" min-width="160" show-overflow-tooltip>
      <template #default="{ row }">{{ resolveMerchantName(row.tenantId, merchantOptions) }}</template>
    </el-table-column>
    <el-table-column v-if="platform" :label="t('dealer.sales.salesStore')" min-width="160" show-overflow-tooltip>
      <template #default="{ row }">{{ resolveSalesStoreName(row.salesStoreId, salesStoreOptions) }}</template>
    </el-table-column>
    <el-table-column :label="t('customer.quote.project')" prop="projectName" min-width="180" show-overflow-tooltip />
    <el-table-column :label="t('common.status')" width="110" align="center">
      <template #default="{ row }">
        <el-tag :type="quoteStatusType(row.status)">{{ quoteStatusText(row.status, statusOptions) }}</el-tag>
      </template>
    </el-table-column>
    <el-table-column :label="t('customer.quote.quantity')" prop="itemCount" width="90" align="center" />
    <el-table-column :label="t('customer.quote.amount.product')" width="125" align="right">
      <template #default="{ row }">{{ quoteMoney(row.productAmount, row.currencyCode) }}</template>
    </el-table-column>
    <el-table-column :label="t('customer.quote.amount.shipping')" width="115" align="right">
      <template #default="{ row }">{{ quoteMoney(row.shippingAmount, row.currencyCode) }}</template>
    </el-table-column>
    <el-table-column :label="t('customer.quote.amount.total')" width="130" align="right">
      <template #default="{ row }"><strong>{{ quoteMoney(row.totalAmount, row.currencyCode) }}</strong></template>
    </el-table-column>
    <el-table-column :label="t('customer.quote.conversion.title')" width="150" align="center">
      <template #default="{ row }">
        <el-tag :type="row.salesDocumentId ? 'success' : 'info'">
          {{ row.salesDocumentId ? `${t('customer.quote.conversion.converted')} · ${row.orderNo}` : t('customer.quote.conversion.pending') }}
        </el-tag>
      </template>
    </el-table-column>
    <el-table-column :label="t('customer.quote.owner')" prop="ownerName" width="130" show-overflow-tooltip />
    <el-table-column :label="t('customer.quote.createdTime')" width="170" align="center">
      <template #default="{ row }">{{ formatUtc(row.createTime, 'YYYY-MM-DD HH:mm') }}</template>
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
import type { CustomerQuote } from '@/api/customer/quote'
import type { MerchantProfileOption } from '@/api/merchant/profile'
import { businessOriginText } from '@/pages/dealer-sales/salesPresentation'
import { resolveMerchantName, resolveSalesStoreName } from '@/pages/dealer-sales/components/standard-grid/platformOptionLookup'
import { formatUtc } from '@/utils/datetime'
import { quoteMoney, quoteStatusText, quoteStatusType } from '@/pages/customer/quote/quoteListPresentation'

defineProps<{
  platform: boolean
  loading: boolean
  merchantOptions: MerchantProfileOption[]
  rows: CustomerQuote[]
  salesStoreOptions: SalesStoreOption[]
  statusOptions: Array<{ value: string; label: string }>
  buildActions: (row: CustomerQuote) => AdminTableAction[]
}>()

const { t } = useI18n()
</script>
