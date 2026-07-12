<template>
  <section class="quick-order-cart-bar">
    <div class="quick-order-cart-bar__summary">
      <div class="quick-order-cart-bar__main">
        <strong>{{ t('dealer.quickOrder.cart.title', { count: rows.length }) }}</strong>
        <span>{{ t('dealer.quickOrder.productAmount') }} {{ money(productAmount) }}</span>
        <span>{{ t('dealer.quickOrder.shippingAmount') }} {{ money(shippingAmount) }}</span>
        <span class="is-total">{{ t('dealer.quickOrder.totalAmount') }} {{ money(totalAmount) }}</span>
      </div>
      <div class="quick-order-cart-bar__actions">
        <el-button :disabled="!rows.length" @click="emit('toggle')">
          {{ expanded ? t('dealer.quickOrder.cart.collapse') : t('dealer.quickOrder.cart.expand') }}
        </el-button>
        <el-button type="primary" :disabled="!rows.length" @click="emit('review')">{{ t('dealer.quickOrder.review') }}</el-button>
      </div>
    </div>
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
      <div v-if="rows.length" class="quick-order-cart-bar__footer">
        <el-button text type="danger" @click="emit('clear')">{{ t('dealer.quickOrder.cart.clear') }}</el-button>
      </div>
    </div>
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
.quick-order-cart-bar { position: sticky; bottom: 12px; z-index: 3; display: flex; flex-direction: column; gap: 10px; }
.quick-order-cart-bar__summary,
.quick-order-cart-bar__panel { border: 1px solid #dbe5f0; border-radius: 10px; background: #fff; }
.quick-order-cart-bar__summary,
.quick-order-cart-bar__row,
.quick-order-cart-bar__footer { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.quick-order-cart-bar__summary { padding: 14px 16px; }
.quick-order-cart-bar__main { display: flex; flex-wrap: wrap; align-items: center; gap: 18px; color: #667085; }
.quick-order-cart-bar__main strong { color: #1d2939; font-size: 16px; }
.quick-order-cart-bar__main .is-total { color: #1677ff; font-weight: 600; }
.quick-order-cart-bar__panel { padding: 8px 14px; }
.quick-order-cart-bar__row { padding: 10px 0; border-bottom: 1px solid #edf1f6; }
.quick-order-cart-bar__row:last-of-type { border-bottom: 0; }
.quick-order-cart-bar__row-text { display: flex; min-width: 0; flex: 1; flex-direction: column; gap: 4px; }
.quick-order-cart-bar__row-text strong { color: #1d2939; }
.quick-order-cart-bar__row-text span { color: #667085; font-size: 13px; }
.quick-order-cart-bar__row-actions { display: flex; align-items: center; gap: 10px; }
.quick-order-cart-bar__row-actions b { min-width: 96px; color: #1d2939; text-align: right; }
.quick-order-cart-bar__footer { padding-top: 8px; }
</style>
