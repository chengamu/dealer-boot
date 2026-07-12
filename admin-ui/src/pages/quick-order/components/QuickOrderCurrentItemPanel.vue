<template>
  <section class="quick-order-current-item">
    <header class="quick-order-current-item__header">
      <h3>{{ title }}</h3>
      <span>{{ item.formulaVersionLabel || t('dealer.quickOrder.formulaVersionEmpty') }}</span>
    </header>
    <div class="quick-order-current-item__sheet">
      <el-form-item :label="t('dealer.quickOrder.product')">
        <el-select v-model="item.saleProductId" filterable :disabled="readonly" @change="emit('product-change')">
          <el-option
            v-for="product in saleProducts"
            :key="product.saleProductId"
            :label="product.saleProductName"
            :value="String(product.saleProductId || '')"
          />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('dealer.quickOrder.room')"><el-input v-model="item.roomLocation" :disabled="readonly" @change="emit('dirty')" /></el-form-item>
      <el-form-item :label="t('dealer.quickOrder.width')">
        <BusinessInchInput v-model="item.orderWidthInch" :disabled="readonly" @change="emit('dirty')" />
      </el-form-item>
      <el-form-item :label="t('dealer.quickOrder.height')">
        <BusinessInchInput v-model="item.orderHeightInch" :disabled="readonly" @change="emit('dirty')" />
      </el-form-item>
      <el-form-item :label="t('dealer.quickOrder.quantity')">
        <el-input-number v-model="item.quantity" :min="1" :precision="0" :disabled="readonly" @change="emit('dirty')" />
      </el-form-item>
    </div>
    <div class="quick-order-current-item__options" v-loading="loadingSetup">
      <QuickOrderOptionGroups :setup="setup" :selected-values="item.selectedOptionValues" :language="language" :readonly="readonly" @change="emit('dirty')" />
    </div>
    <footer class="quick-order-current-item__footer">
      <div class="quick-order-current-item__checks">
        <el-tag :type="item.calculationStatus === 'PASS' ? 'success' : 'warning'">{{ statusText }}</el-tag>
        <span>{{ t('dealer.quickOrder.productAmount') }} <b>{{ money(item.productAmount) }}</b></span>
        <span>{{ t('dealer.quickOrder.shippingAmount') }} <b>{{ money(item.shippingAmount) }}</b></span>
        <span class="is-total">{{ t('dealer.quickOrder.lineAmount') }} <b>{{ money(item.lineAmount) }}</b></span>
      </div>
      <div class="quick-order-current-item__actions">
        <el-button :disabled="readonly || !item.saleProductId" :loading="calculating" @click="emit('calculate')">
          {{ t('dealer.quickOrder.calculate') }}
        </el-button>
        <el-button type="primary" :disabled="readonly || item.calculationStatus !== 'PASS'" @click="emit('save-line')">
          {{ editing ? t('dealer.quickOrder.updateLine') : t('dealer.quickOrder.addLine') }}
        </el-button>
        <el-button :disabled="readonly" @click="emit('reset-line')">{{ t('common.reset') }}</el-button>
      </div>
    </footer>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type { CustomerQuoteCatalogSetup, QuoteLanguage } from '@/api/customer/quote'
import type { SaleProductVO } from '@/api/product-pricing/types'
import type { QuickOrderWorkbenchItem } from '../quickOrderShared'
import QuickOrderOptionGroups from './QuickOrderOptionGroups.vue'

const props = defineProps<{
  item: QuickOrderWorkbenchItem
  saleProducts: SaleProductVO[]
  setup?: CustomerQuoteCatalogSetup
  language: QuoteLanguage
  currencyCode?: string
  editing?: boolean
  loadingSetup?: boolean
  calculating?: boolean
  readonly?: boolean
}>()

const emit = defineEmits<{
  dirty: []
  calculate: []
  'save-line': []
  'reset-line': []
  'product-change': []
}>()

const { t } = useI18n()
const title = computed(() => props.editing ? t('dealer.quickOrder.currentLine.edit') : t('dealer.quickOrder.currentLine.add'))
const statusText = computed(() => {
  if (props.item.calculationStatus === 'PASS') return t('dealer.quickOrder.calculation.pass')
  if (props.item.calculationStatus === 'FAIL') return props.item.calculationMessage || t('dealer.quickOrder.calculation.fail')
  return t('dealer.quickOrder.calculation.pending')
})

function money(value?: number) {
  return value == null
    ? '-'
    : new Intl.NumberFormat('en-US', { style: 'currency', currency: props.currencyCode || 'USD' }).format(value)
}
</script>

<style scoped>
.quick-order-current-item { padding: 12px 14px 14px; border: 1px solid #e3e9f2; border-radius: 8px; background: #fff; }
.quick-order-current-item__header,
.quick-order-current-item__footer,
.quick-order-current-item__checks,
.quick-order-current-item__actions { display: flex; align-items: center; gap: 12px; }
.quick-order-current-item__header { justify-content: space-between; margin-bottom: 10px; }
.quick-order-current-item__header h3 { margin: 0; font-size: 16px; }
.quick-order-current-item__header span { color: #667085; font-size: 13px; }
.quick-order-current-item__sheet { display: grid; grid-template-columns: 1.4fr 1fr 1fr 1fr 0.8fr; gap: 8px 12px; }
.quick-order-current-item__options { margin-top: 10px; }
.quick-order-current-item__footer { justify-content: space-between; margin-top: 12px; }
.quick-order-current-item__checks { flex-wrap: wrap; color: #667085; font-size: 13px; }
.quick-order-current-item__checks b { color: #1d2939; font-size: 16px; }
.quick-order-current-item__checks .is-total b { color: #1677ff; }
.quick-order-current-item__actions { justify-content: flex-end; }
.quick-order-current-item :deep(.el-form-item) { margin-bottom: 0; }
.quick-order-current-item :deep(.el-input-number),
.quick-order-current-item :deep(.el-select) { width: 100%; }
@media (max-width: 1280px) {
  .quick-order-current-item__sheet { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .quick-order-current-item__footer { align-items: flex-start; flex-direction: column; }
}
</style>
