<template>
  <footer class="quote-totals">
    <span>{{ count }} {{ t('customer.quote.quantity') }}</span>
    <dl><dt>{{ t('customer.quote.amount.product') }}</dt><dd>{{ money(productAmount) }}</dd></dl>
    <dl><dt>{{ t('customer.quote.amount.shipping') }}</dt><dd>{{ money(shippingAmount) }}</dd></dl>
    <dl class="quote-totals__total"><dt>{{ t('customer.quote.amount.total') }}</dt><dd>{{ money(totalAmount) }}</dd></dl>
  </footer>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import type { DecimalValue } from '@/types/api'
import { formatCurrency } from '@/utils/businessNumber'

const props = defineProps<{ count: number; productAmount?: DecimalValue; shippingAmount?: DecimalValue; totalAmount?: DecimalValue; currencyCode?: string }>()
const { t } = useI18n()
function money(value?: DecimalValue) { return formatCurrency(value ?? '0', props.currencyCode || 'USD') }
</script>

<style scoped>
.quote-totals { display: flex; min-height: 66px; align-items: center; justify-content: flex-end; gap: 34px; padding: 10px 20px; border: 1px solid #dfe6ef; border-radius: 7px; background: #fff; }
.quote-totals > span { margin-right: auto; color: #667085; }
.quote-totals dl { margin: 0; min-width: 130px; text-align: right; }
.quote-totals dt { color: #667085; font-size: 12px; }
.quote-totals dd { margin: 4px 0 0; color: #344054; font-size: 18px; font-weight: 650; }
.quote-totals__total { padding-left: 28px; border-left: 1px solid #dfe6ef; }
.quote-totals__total dd { color: #1677ff; font-size: 23px; }
</style>
