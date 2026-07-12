<template>
  <section class="checkout-review">
    <el-descriptions :column="3" border>
      <el-descriptions-item :label="t('pay.salesOrderNo')">{{ order.orderNo || '-' }}</el-descriptions-item>
      <el-descriptions-item :label="t('pay.customer')">{{ order.customerName || '-' }}</el-descriptions-item>
      <el-descriptions-item :label="t('pay.merchant')">{{ order.merchantName || '-' }}</el-descriptions-item>
      <el-descriptions-item :label="t('dealer.sales.project')">{{ order.projectName || '-' }}</el-descriptions-item>
      <el-descriptions-item :label="t('dealer.sales.customerPo')">{{ order.customerPoNo || '-' }}</el-descriptions-item>
      <el-descriptions-item :label="t('dealer.sales.recipient')">{{ order.recipientName || '-' }} · {{ order.recipientPhone || '-' }}</el-descriptions-item>
      <el-descriptions-item :label="t('dealer.sales.address')" :span="3">{{ order.shippingAddress || '-' }}</el-descriptions-item>
    </el-descriptions>

    <h3>{{ t('dealer.sales.items') }}</h3>
    <el-table :data="order.items || []" border row-key="salesItemId">
      <el-table-column type="expand">
        <template #default="{ row }"><div class="checkout-review__options">{{ optionSummary(row.selectedOptionValues) }}</div></template>
      </el-table-column>
      <el-table-column prop="roomLocation" :label="t('dealer.sales.room')" min-width="120" />
      <el-table-column prop="saleProductName" :label="t('dealer.sales.product')" min-width="180" show-overflow-tooltip />
      <el-table-column :label="t('dealer.sales.size')" width="140"><template #default="{ row }">{{ row.orderWidthInch }} × {{ row.orderHeightInch }}</template></el-table-column>
      <el-table-column prop="quantity" :label="t('dealer.sales.quantity')" width="76" align="right" />
      <el-table-column prop="configurationSummary" :label="t('dealer.sales.configuration')" min-width="220" show-overflow-tooltip />
      <el-table-column :label="t('dealer.sales.lineAmount')" width="130" align="right"><template #default="{ row }">{{ money(row.lineAmount, order.currencyCode) }}</template></el-table-column>
    </el-table>

    <h3>{{ t('pay.delivery') }}</h3>
    <div class="checkout-review__delivery">
      <span>{{ order.shippingAddress || '-' }}</span><span>{{ order.recipientName || '-' }}</span><span>{{ order.recipientPhone || '-' }}</span>
    </div>
  </section>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import type { SalesDocument } from '@/api/dealer-sales'
import { money } from '../payPresentation'

defineProps<{ order: SalesDocument }>()
const { t } = useI18n()

function optionSummary(values?: Record<string, string>) {
  const entries = Object.entries(values || {})
  return entries.length ? entries.map(([key, value]) => `${key}: ${value}`).join(' · ') : t('pay.noConfiguration')
}
</script>

<style scoped>
.checkout-review h3 { margin: 14px 0 8px; font-size: 14px; }
.checkout-review__options { padding: 2px 40px; color: #667085; line-height: 1.7; }
.checkout-review__delivery { display: grid; grid-template-columns: 1fr 180px 160px; gap: 12px; padding: 12px; border: 1px solid #e9edf5; }
@media (max-width: 900px) { .checkout-review__delivery { grid-template-columns: 1fr; } }
</style>
