<template>
  <el-table :data="rows" border class="quick-order-line-table">
    <el-table-column type="expand" width="44">
      <template #default="{ row }">
        <div class="quick-order-line-table__config">
          <div v-for="entry in entries(row.selectedOptionValues)" :key="entry.key" class="quick-order-line-table__config-item">
            <span>{{ entry.key }}</span>
            <b>{{ entry.value }}</b>
          </div>
        </div>
      </template>
    </el-table-column>
    <el-table-column prop="roomLocation" :label="t('dealer.quickOrder.room')" min-width="120" />
    <el-table-column prop="saleProductName" :label="t('dealer.quickOrder.product')" min-width="180" />
    <el-table-column :label="t('dealer.quickOrder.size')" min-width="150">
      <template #default="{ row }">{{ quickOrderSize(row) }}</template>
    </el-table-column>
    <el-table-column prop="quantity" :label="t('dealer.quickOrder.quantity')" width="90" align="center" />
    <el-table-column :label="t('dealer.quickOrder.configuration')" min-width="220" show-overflow-tooltip>
      <template #default="{ row }">{{ quickOrderSummary(row, language) || '-' }}</template>
    </el-table-column>
    <el-table-column :label="t('dealer.quickOrder.productAmount')" width="130" align="right">
      <template #default="{ row }">{{ money(row.productAmount) }}</template>
    </el-table-column>
    <el-table-column :label="t('dealer.quickOrder.shippingAmount')" width="130" align="right">
      <template #default="{ row }">{{ money(row.shippingAmount) }}</template>
    </el-table-column>
    <el-table-column :label="t('dealer.quickOrder.lineAmount')" width="130" align="right">
      <template #default="{ row }"><strong>{{ money(row.lineAmount) }}</strong></template>
    </el-table-column>
  </el-table>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import type { QuoteLanguage } from '@/api/customer/quote'
import { quickOrderSize, quickOrderSummary, type QuickOrderWorkbenchItem } from '../quickOrderShared'
import { formatCurrency } from '@/utils/businessNumber'

const props = defineProps<{
  rows: QuickOrderWorkbenchItem[]
  language: QuoteLanguage
  currencyCode?: string
}>()

const { t } = useI18n()

function entries(values?: Record<string, string>) {
  return Object.entries(values || {}).map(([key, value]) => ({ key, value }))
}

function money(value?: string | null) {
  return formatCurrency(value, props.currencyCode || 'USD')
}
</script>

<style scoped>
.quick-order-line-table__config { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 10px 14px; padding: 4px 2px; }
.quick-order-line-table__config-item { display: flex; flex-direction: column; gap: 4px; padding: 10px 12px; border: 1px solid #e7edf4; border-radius: 8px; background: #fafcff; }
.quick-order-line-table__config-item span { color: #667085; font-size: 12px; }
.quick-order-line-table__config-item b { color: #1d2939; }
</style>
