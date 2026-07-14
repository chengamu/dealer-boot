<template>
  <section class="checkout-section">
    <header class="checkout-section__header">
      <h3>{{ t('dealer.sales.items') }}</h3>
      <span>({{ order.items?.length || 0 }})</span>
    </header>
    <div class="checkout-items">
      <div class="checkout-items__head">
        <span>{{ t('dealer.sales.room') }}</span>
        <span>{{ t('dealer.sales.product') }}</span>
        <span>{{ t('dealer.sales.size') }}</span>
        <span>{{ t('dealer.sales.quantity') }}</span>
        <span>{{ t('dealer.sales.configuration') }}</span>
        <span>{{ t('dealer.sales.lineAmount') }}</span>
      </div>
      <div
        v-for="item in order.items || []"
        :key="item.salesItemId || item.lineNo"
        class="checkout-items__row-wrap"
      >
        <button class="checkout-items__row" type="button" @click="toggle(item.salesItemId || String(item.lineNo || ''))">
          <span class="checkout-items__room">
            <strong>{{ item.roomLocation || '-' }}</strong>
            <small>{{ item.lineNo || '-' }}</small>
          </span>
          <span class="checkout-items__product">
            <strong>{{ item.saleProductName || '-' }}</strong>
            <small>{{ optionSummary(item.selectedOptionValues) }}</small>
          </span>
          <span>{{ itemSize(item.orderWidthInch, item.orderHeightInch) }}</span>
          <span class="checkout-items__number">{{ item.quantity || 0 }}</span>
          <span class="checkout-items__summary">{{ item.configurationSummary || '-' }}</span>
          <span class="checkout-items__amount">{{ money(item.lineAmount, order.currencyCode) }}</span>
          <el-icon class="checkout-items__arrow" :class="{ 'is-open': isOpen(item.salesItemId || String(item.lineNo || '')) }">
            <ArrowRight />
          </el-icon>
        </button>
        <div v-if="isOpen(item.salesItemId || String(item.lineNo || ''))" class="checkout-items__detail">
          <div class="checkout-items__detail-grid">
            <div>
              <dt>{{ t('dealer.fulfillment.configuration') }}</dt>
              <dd>{{ item.configurationSummary || t('pay.noConfiguration') }}</dd>
            </div>
            <div>
              <dt>{{ t('dealer.fulfillment.formulaVersion') }}</dt>
              <dd>{{ item.formulaVersionLabel || '-' }}</dd>
            </div>
            <div class="is-wide">
              <dt>{{ t('pay.noConfiguration') }}</dt>
              <dd>{{ optionSummary(item.selectedOptionValues) }}</dd>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import type { DecimalValue } from '@/types/api'
import type { SalesDocument } from '@/api/dealer-sales'
import { money } from '../payPresentation'
import { formatInch } from '@/utils/businessNumber'

const props = defineProps<{ order: SalesDocument }>()
const { t } = useI18n()
const expandedKeys = ref<string[]>(props.order.items?.[0]?.salesItemId ? [props.order.items[0].salesItemId] : [])

function toggle(key: string) {
  expandedKeys.value = expandedKeys.value.includes(key)
    ? expandedKeys.value.filter((item) => item !== key)
    : [key]
}

function isOpen(key: string) {
  return expandedKeys.value.includes(key)
}

function optionSummary(values?: Record<string, string>) {
  const entries = Object.entries(values || {})
  return entries.length ? entries.map(([key, value]) => `${key}: ${value}`).join(' / ') : t('pay.noConfiguration')
}

function itemSize(width?: DecimalValue, height?: DecimalValue) {
  return width && height ? `${formatInch(width)} × ${formatInch(height)}` : '-'
}
</script>

<style scoped>
.checkout-section {
  overflow: hidden;
  border: 1px solid #e9edf5;
  border-radius: 8px;
  background: #fff;
}

.checkout-section__header {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 14px 16px;
  border-bottom: 1px solid #eef0f5;
}

.checkout-section__header h3 {
  margin: 0;
  color: #1d2129;
  font-size: 16px;
}

.checkout-section__header span {
  color: #667085;
  font-size: 13px;
}

.checkout-items__head,
.checkout-items__row {
  display: grid;
  grid-template-columns: 1.1fr 1.8fr 1fr 0.7fr 1.8fr 1fr 20px;
  gap: 12px;
  align-items: center;
  width: 100%;
}

.checkout-items__head {
  padding: 12px 16px;
  color: #667085;
  font-size: 12px;
  background: #f8fbff;
}

.checkout-items__row-wrap + .checkout-items__row-wrap {
  border-top: 1px solid #eef0f5;
}

.checkout-items__row {
  padding: 14px 16px;
  border: 0;
  background: transparent;
  text-align: left;
  cursor: pointer;
}

.checkout-items__room strong,
.checkout-items__product strong {
  display: block;
  color: #1d2129;
}

.checkout-items__room small,
.checkout-items__product small {
  display: block;
  margin-top: 4px;
  color: #667085;
  font-size: 12px;
  line-height: 1.5;
}

.checkout-items__number,
.checkout-items__amount {
  color: #1d2129;
  text-align: right;
}

.checkout-items__summary {
  color: #344054;
  line-height: 1.5;
}

.checkout-items__arrow {
  color: #98a2b3;
  transition: transform 0.2s ease;
}

.checkout-items__arrow.is-open {
  transform: rotate(90deg);
}

.checkout-items__detail {
  padding: 0 16px 16px;
  background: #fbfcff;
}

.checkout-items__detail-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  padding: 14px 16px;
  border: 1px solid #e5ebf5;
  border-radius: 8px;
}

.checkout-items__detail-grid .is-wide {
  grid-column: 1 / -1;
}

dt {
  margin-bottom: 6px;
  color: #98a2b3;
  font-size: 12px;
}

dd {
  margin: 0;
  color: #1d2129;
  line-height: 1.6;
  overflow-wrap: anywhere;
}

@media (max-width: 1100px) {
  .checkout-items__head {
    display: none;
  }

  .checkout-items__row {
    grid-template-columns: 1fr;
  }

  .checkout-items__number,
  .checkout-items__amount {
    text-align: left;
  }

  .checkout-items__arrow {
    justify-self: end;
  }

  .checkout-items__detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>
