<template>
  <footer class="quote-totals">
    <div class="quote-totals__remark">
      <span>{{ t('customer.quote.remark') }}</span>
      <el-input
        v-if="!readonly"
        :model-value="remark"
        type="textarea"
        :rows="2"
        maxlength="500"
        show-word-limit
        :placeholder="t('customer.quote.remark')"
        @update:model-value="emit('update:remark', String($event || ''))"
      />
      <p v-else>{{ remark || '-' }}</p>
    </div>
    <div class="quote-totals__meta">
      <span>{{ count }} {{ t('customer.quote.quantity') }}</span>
      <dl><dt>{{ t('customer.quote.amount.product') }}</dt><dd>{{ money(productAmount) }}</dd></dl>
      <dl><dt>{{ t('customer.quote.amount.shipping') }}</dt><dd>{{ money(shippingAmount) }}</dd></dl>
      <dl class="quote-totals__total"><dt>{{ t('customer.quote.amount.total') }}</dt><dd>{{ money(totalAmount) }}</dd></dl>
    </div>
  </footer>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import type { DecimalValue } from '@/types/api'
import { formatCurrency } from '@/utils/businessNumber'

const props = defineProps<{
  count: number
  remark?: string
  readonly?: boolean
  productAmount?: DecimalValue
  shippingAmount?: DecimalValue
  totalAmount?: DecimalValue
  currencyCode?: string
}>()
const emit = defineEmits<{ 'update:remark': [value: string] }>()
const { t } = useI18n()
function money(value?: DecimalValue) { return formatCurrency(value ?? '0', props.currencyCode || 'USD') }
</script>

<style scoped>
.quote-totals {
  display: grid;
  grid-template-columns: minmax(320px, 1.35fr) minmax(420px, 1fr);
  gap: 20px;
  padding: 14px 18px;
  border: 1px solid #dfe6ef;
  border-radius: 8px;
  background: #fff;
}
.quote-totals__remark {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 10px;
}
.quote-totals__remark span {
  color: #667085;
  font-size: 12px;
  font-weight: 600;
}
.quote-totals__remark p {
  min-height: 64px;
  margin: 0;
  padding: 12px 14px;
  border: 1px solid #dfe6ef;
  border-radius: 8px;
  color: #1d2939;
  background: #f8fafc;
}
.quote-totals__meta {
  display: flex;
  align-items: stretch;
  justify-content: flex-end;
  gap: 22px;
}
.quote-totals__meta > span {
  margin-right: auto;
  align-self: center;
  color: #667085;
}
.quote-totals dl { margin: 0; min-width: 130px; text-align: right; }
.quote-totals dt { color: #667085; font-size: 12px; }
.quote-totals dd { margin: 4px 0 0; color: #344054; font-size: 18px; font-weight: 650; }
.quote-totals__total { padding-left: 28px; border-left: 1px solid #dfe6ef; }
.quote-totals__total dd { color: #1677ff; font-size: 23px; }
@media (max-width: 1200px) {
  .quote-totals { grid-template-columns: minmax(0, 1fr); }
  .quote-totals__meta { flex-wrap: wrap; }
  .quote-totals__meta > span { width: 100%; }
}
</style>
