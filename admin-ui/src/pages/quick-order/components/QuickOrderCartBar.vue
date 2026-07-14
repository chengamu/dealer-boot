<template>
  <section class="quick-order-cart-bar">
    <header class="quick-order-cart-bar__header">
      <div class="quick-order-cart-bar__heading">
        <strong>{{ t('dealer.quickOrder.cart.title', { count: rows.length }) }}</strong>
        <span>{{ t('dealer.quickOrder.totalAmount') }} {{ money(totalAmount) }}</span>
      </div>
      <div class="quick-order-cart-bar__header-actions">
        <el-button text type="danger" :disabled="!rows.length" @click="emit('clear')">{{ t('dealer.quickOrder.cart.clear') }}</el-button>
        <el-button :disabled="!rows.length" @click="emit('toggle')">
          {{ expanded ? t('dealer.quickOrder.cart.collapse') : t('dealer.quickOrder.cart.expand') }}
        </el-button>
      </div>
    </header>
    <div v-if="expanded" class="quick-order-cart-bar__panel">
      <el-empty v-if="!rows.length" :description="t('dealer.quickOrder.cart.empty')" :image-size="48" />
      <div v-for="row in rows" :key="row.clientId" class="quick-order-cart-bar__row">
        <div class="quick-order-cart-bar__row-text">
          <strong>{{ row.roomLocation || t('dealer.quickOrder.roomEmpty') }}</strong>
          <span>{{ row.saleProductName || '-' }} · {{ quickOrderSize(row) }} · {{ t('dealer.quickOrder.quantityShort') }} {{ row.quantity || 0 }}</span>
          <span>{{ quickOrderSummary(row, language) || '-' }}</span>
        </div>
        <div class="quick-order-cart-bar__row-actions">
          <b>{{ money(row.lineAmount) }}</b>
          <el-button link type="primary" @click="emit('edit', row)">{{ t('common.edit') }}</el-button>
          <el-button link type="danger" @click="emit('remove', row)">{{ t('common.delete') }}</el-button>
        </div>
      </div>
    </div>
    <footer class="quick-order-cart-bar__summary">
      <dl><dt>{{ t('dealer.quickOrder.productAmount') }}</dt><dd>{{ money(productAmount) }}</dd></dl>
      <dl><dt>{{ t('dealer.quickOrder.shippingAmount') }}</dt><dd>{{ money(shippingAmount) }}</dd></dl>
      <dl class="is-total"><dt>{{ t('dealer.quickOrder.totalAmount') }}</dt><dd>{{ money(totalAmount) }}</dd></dl>
      <el-button type="primary" :disabled="!rows.length" @click="emit('review')">{{ t('dealer.quickOrder.reviewAction') }}</el-button>
    </footer>
  </section>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import type { QuoteLanguage } from '@/api/customer/quote'
import type { DecimalValue } from '@/types/api'
import { formatCurrency } from '@/utils/businessNumber'
import { quickOrderSize, quickOrderSummary, type QuickOrderWorkbenchItem } from '../quickOrderShared'

const props = defineProps<{
  rows: QuickOrderWorkbenchItem[]
  expanded: boolean
  language: QuoteLanguage
  currencyCode?: string
  productAmount?: DecimalValue
  shippingAmount?: DecimalValue
  totalAmount?: DecimalValue
}>()

const emit = defineEmits<{
  toggle: []
  review: []
  clear: []
  edit: [row: QuickOrderWorkbenchItem]
  remove: [row: QuickOrderWorkbenchItem]
}>()

const { t } = useI18n()

function money(value?: DecimalValue) { return formatCurrency(value ?? '0', props.currencyCode || 'USD') }
</script>

<style scoped>
.quick-order-cart-bar {
  display: flex;
  flex-direction: column;
  border: 1px solid #dbe5f0;
  border-radius: 10px;
  background: #fff;
}
.quick-order-cart-bar__header,
.quick-order-cart-bar__row,
.quick-order-cart-bar__summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.quick-order-cart-bar__header {
  padding: 14px 16px;
  border-bottom: 1px solid #edf1f6;
}
.quick-order-cart-bar__heading {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 4px;
}
.quick-order-cart-bar__heading strong { color: #1d2939; font-size: 16px; }
.quick-order-cart-bar__heading span { color: #1677ff; font-size: 13px; font-weight: 600; }
.quick-order-cart-bar__header-actions { display: flex; align-items: center; gap: 8px; }
.quick-order-cart-bar__panel { padding: 8px 14px; }
.quick-order-cart-bar__row { padding: 10px 0; border-bottom: 1px solid #edf1f6; }
.quick-order-cart-bar__row:last-of-type { border-bottom: 0; }
.quick-order-cart-bar__row-text { display: flex; min-width: 0; flex: 1; flex-direction: column; gap: 4px; }
.quick-order-cart-bar__row-text strong { color: #1d2939; }
.quick-order-cart-bar__row-text span { color: #667085; font-size: 13px; }
.quick-order-cart-bar__row-actions { display: flex; align-items: center; gap: 10px; }
.quick-order-cart-bar__row-actions b { min-width: 96px; color: #1d2939; text-align: right; }
.quick-order-cart-bar__summary {
  padding: 14px 16px;
  border-top: 1px solid #edf1f6;
}
.quick-order-cart-bar__summary dl { margin: 0; min-width: 112px; }
.quick-order-cart-bar__summary dt { color: #667085; font-size: 12px; }
.quick-order-cart-bar__summary dd { margin: 6px 0 0; color: #1d2939; font-size: 18px; font-weight: 600; }
.quick-order-cart-bar__summary .is-total dd { color: #1677ff; font-size: 22px; }
@media (max-width: 1320px) {
  .quick-order-cart-bar__summary { flex-wrap: wrap; }
}
</style>
