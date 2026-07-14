<template>
  <el-table :data="items" border>
    <el-table-column type="expand" width="44">
      <template #default="{ row }">
        <div class="sales-document-items-table__config">
          <div v-for="entry in entries(row.selectedOptionValues)" :key="entry.key" class="sales-document-items-table__config-item">
            <span>{{ entry.key }}</span>
            <b>{{ entry.value }}</b>
          </div>
        </div>
      </template>
    </el-table-column>
    <el-table-column prop="roomLocation" :label="t('dealer.sales.room')" min-width="120" />
    <el-table-column :label="t('dealer.sales.product')" min-width="210">
      <template #default="{ row }">
        <div class="sales-document-items-table__product">
          <strong>{{ row.saleProductName || '-' }}</strong>
          <span>{{ row.configurationSummary || '-' }}</span>
        </div>
      </template>
    </el-table-column>
    <el-table-column :label="t('dealer.sales.size')" width="150"><template #default="{ row }">{{ size(row) }}</template></el-table-column>
    <el-table-column prop="quantity" :label="t('dealer.sales.quantity')" width="90" align="center" />
    <el-table-column :label="t('dealer.sales.configuration')" min-width="220" show-overflow-tooltip>
      <template #default="{ row }">{{ row.configurationSummary || '-' }}</template>
    </el-table-column>
    <el-table-column :label="t('dealer.sales.unitAmount')" width="130" align="right">
      <template #default="{ row }">{{ money(row.unitAmount || row.productAmount) }}</template>
      </el-table-column>
    <el-table-column :label="t('dealer.sales.shippingAmount')" width="130" align="right">
      <template #default="{ row }">{{ money(row.shippingAmount) }}</template>
    </el-table-column>
    <el-table-column :label="t('dealer.sales.lineAmount')" width="130" align="right">
      <template #default="{ row }"><strong>{{ money(row.lineAmount) }}</strong></template>
    </el-table-column>
  </el-table>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import type { SalesItem } from '@/api/dealer-sales'
import { formatCurrency, formatInch } from '@/utils/businessNumber'

const props = defineProps<{ items: SalesItem[]; currencyCode?: string }>()
const { t } = useI18n()

function entries(values?: Record<string, string>) {
  return Object.entries(values || {}).map(([key, value]) => ({ key, value }))
}

function size(row: SalesItem) {
  return `${formatInch(row.orderWidthInch)} × ${formatInch(row.orderHeightInch)}`
}

function money(value?: string | null) {
  return formatCurrency(value, props.currencyCode || 'USD')
}
</script>

<style scoped>
.sales-document-items-table__config { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 10px 14px; padding: 4px 2px; }
.sales-document-items-table__config-item { display: flex; flex-direction: column; gap: 4px; padding: 10px 12px; border: 1px solid #e7edf4; border-radius: 8px; background: #fafcff; }
.sales-document-items-table__config-item span { color: #667085; font-size: 12px; }
.sales-document-items-table__config-item b { color: #1d2939; }
.sales-document-items-table__product {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 4px;
}
.sales-document-items-table__product strong { color: #1d2939; }
.sales-document-items-table__product span {
  color: #667085;
  font-size: 12px;
  line-height: 18px;
}
</style>
