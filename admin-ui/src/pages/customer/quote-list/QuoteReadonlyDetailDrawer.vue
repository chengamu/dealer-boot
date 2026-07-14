<template>
  <AdminDrawer v-model="visible" :title="t('customer.quote.detailTitle')" size="960px" variant="detail">
    <div v-loading="loading" class="quote-detail-drawer">
      <el-descriptions v-if="quote" :column="3" border>
        <el-descriptions-item :label="t('customer.quote.no')">{{ quote.quoteNo || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('customer.quote.customer')">{{ quote.companyName || quote.customerName || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('common.status')">
          <el-tag :type="quoteStatusType(quote.status)">{{ quoteStatusText(quote.status, statusOptions) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item :label="t('customer.quote.project')">{{ quote.projectName || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('customer.quote.owner')">{{ quote.ownerName || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('customer.quote.createdTime')">{{ formatUtc(quote.createTime, 'YYYY-MM-DD HH:mm') }}</el-descriptions-item>
      </el-descriptions>

      <el-table v-if="quote?.items?.length" :data="quote.items" border class="quote-detail-drawer__table">
        <el-table-column prop="roomLocation" :label="t('customer.quote.room')" min-width="120" />
        <el-table-column prop="saleProductName" :label="t('customer.quote.product')" min-width="180" />
        <el-table-column :label="t('dealer.sales.size')" width="150">
          <template #default="{ row }">{{ size(row) }}</template>
        </el-table-column>
        <el-table-column prop="quantity" :label="t('customer.quote.quantity')" width="90" align="center" />
        <el-table-column :label="t('customer.quote.configuration')" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">{{ summary(row) }}</template>
        </el-table-column>
        <el-table-column :label="t('customer.quote.amount.product')" width="130" align="right">
          <template #default="{ row }">{{ quoteMoney(row.productAmount, quote?.currencyCode) }}</template>
        </el-table-column>
        <el-table-column :label="t('customer.quote.amount.shipping')" width="130" align="right">
          <template #default="{ row }">{{ quoteMoney(row.shippingAmount, quote?.currencyCode) }}</template>
        </el-table-column>
        <el-table-column :label="t('customer.quote.amount.line')" width="130" align="right">
          <template #default="{ row }"><strong>{{ quoteMoney(row.lineAmount, quote?.currencyCode) }}</strong></template>
        </el-table-column>
      </el-table>
    </div>
  </AdminDrawer>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { formatUtc } from '@/utils/datetime'
import { formatInch } from '@/utils/businessNumber'
import { quoteMoney, quoteStatusText, quoteStatusType } from '@/pages/customer/quote/quoteListPresentation'
import type { CustomerQuote, CustomerQuoteItem } from '@/api/customer/quote'

const props = defineProps<{
  modelValue: boolean
  loading: boolean
  quote?: CustomerQuote
  statusOptions: Array<{ value: string; label: string }>
}>()

const emit = defineEmits<{
  (event: 'update:modelValue', value: boolean): void
}>()

const { t, locale } = useI18n()

const visible = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value)
})

function size(row: CustomerQuoteItem) {
  return `${formatInch(row.orderWidthInch)} × ${formatInch(row.orderHeightInch)}`
}

function summary(row: CustomerQuoteItem) {
  return locale.value.toLowerCase().startsWith('zh')
    ? row.selectedOptionsSummaryCn || '-'
    : row.selectedOptionsSummaryEn || row.selectedOptionsSummaryCn || '-'
}
</script>

<style scoped>
.quote-detail-drawer {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.quote-detail-drawer__table {
  width: 100%;
}
</style>
