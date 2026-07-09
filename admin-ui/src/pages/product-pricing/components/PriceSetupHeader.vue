<template>
  <section class="price-setup-header">
    <div class="price-setup-header__main">
      <div class="price-setup-header__topline">
        <span class="price-setup-header__breadcrumb">{{ t('productCenter.pricing.title') }} / {{ t('productCenter.pricing.priceSetting') }}</span>
        <el-select class="price-setup-header__select" :model-value="selectedProductId" filterable @change="$emit('product-change', $event)">
          <el-option v-for="item in products" :key="String(item.saleProductId || '')" :label="productLabel(item)" :value="String(item.saleProductId || '')" />
        </el-select>
      </div>
      <div class="price-setup-header__title">
        <span>{{ product.saleProductName || '-' }}</span>
        <el-tag size="small" type="primary">{{ product.formulaVersionLabel || '-' }}</el-tag>
        <el-tag size="small" :type="enabledStatusTagType(product.status)">{{ enabledStatusText(product.status, t) }}</el-tag>
        <el-tag size="small" :type="priceStatusTagType(product.priceStatus)">{{ priceStatusText(product.priceStatus, t) }}</el-tag>
      </div>
      <div class="price-setup-header__meta">
        <span>{{ t('productCenter.saleProduct.code') }}：{{ product.saleProductCode || '-' }}</span>
        <span>{{ t('productCenter.formula.category') }}：{{ product.categoryNameCn || '-' }}</span>
        <span>{{ t('productCenter.pricing.boundFormula') }}：{{ product.formulaName || '-' }}</span>
        <span>{{ t('productCenter.formula.minWidthInch') }}：{{ formatNumber(product.minWidthInch) }}</span>
        <span>{{ t('productCenter.formula.minHeightInch') }}：{{ formatNumber(product.minHeightInch) }}</span>
        <span>{{ t('productCenter.formula.maxWidthInch') }}：{{ formatNumber(product.maxWidthInch) }}</span>
        <span>{{ t('productCenter.formula.maxHeightInch') }}：{{ formatNumber(product.maxHeightInch) }}</span>
        <span>{{ t('productCenter.formula.updateTime') }}：{{ formatMinute(product.updateTime) }}</span>
      </div>
    </div>
    <div class="price-setup-header__actions">
      <el-button icon="Refresh" :loading="loading" @click="$emit('refresh')">{{ t('productCenter.pricing.refresh') }}</el-button>
      <el-button v-hasPermi="['product:pricing:validate']" icon="CircleCheck" :loading="validating" @click="$emit('validate')">{{ t('productCenter.pricing.validatePrice') }}</el-button>
    </div>
  </section>
</template>

<script setup lang="ts">
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { formatUtc } from '@/utils/datetime'
import type { SaleProductVO } from '@/api/product-pricing/types'
import {
  enabledStatusTagType,
  enabledStatusText,
  priceStatusTagType,
  priceStatusText
} from '../utils/pricingDisplay'

defineProps<{
  product: SaleProductVO
  products: SaleProductVO[]
  selectedProductId: string
  loading?: boolean
  validating?: boolean
  editable?: boolean
}>()

defineEmits<{
  'product-change': [value: string | number]
  refresh: []
  validate: []
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)

function productLabel(item: SaleProductVO) {
  return `${item.saleProductCode || '-'} ${item.saleProductName || ''}`
}

function formatNumber(value?: number | string) {
  const numeric = Number(value)
  return Number.isFinite(numeric) ? numeric.toFixed(2) : '-'
}

function formatMinute(value?: string) {
  return value ? formatUtc(value, 'YYYY-MM-DD HH:mm') : '-'
}
</script>

<style scoped>
.price-setup-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px;
  background: #fff;
  border: 1px solid #e6ebf2;
  border-radius: 8px;
}

.price-setup-header__main {
  min-width: 0;
}

.price-setup-header__topline,
.price-setup-header__title,
.price-setup-header__meta,
.price-setup-header__actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

.price-setup-header__topline {
  gap: 10px;
  margin-bottom: 8px;
}

.price-setup-header__breadcrumb {
  color: #718096;
  font-size: 13px;
}

.price-setup-header__select {
  width: min(420px, 46vw);
}

.price-setup-header__title {
  gap: 8px;
  color: #1f2937;
  font-size: 22px;
  font-weight: 700;
}

.price-setup-header__meta {
  gap: 8px 24px;
  margin-top: 10px;
  color: #4b5563;
  font-size: 13px;
}

.price-setup-header__actions {
  align-items: flex-start;
  justify-content: flex-end;
  gap: 8px;
  min-width: 270px;
}
</style>
