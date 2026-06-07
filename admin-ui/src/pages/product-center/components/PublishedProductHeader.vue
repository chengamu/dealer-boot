<template>
  <section class="published-product-header">
    <div class="published-product-header__media">
      <span>{{ t('productCenter.sales.publishedPackage') }}</span>
    </div>
    <div class="published-product-header__summary">
      <el-tag type="success" effect="light">{{ packageInfo.packageStatus || t('productCenter.status.published') }}</el-tag>
      <h2>{{ packageInfo.packageCode || t('productCenter.sales.packageList') }}</h2>
      <div class="published-product-header__meta">
        <span>{{ t('productCenter.publish.packageCode') }}：{{ packageInfo.packageCode || '-' }}</span>
        <span>{{ t('productCenter.template.versionNo') }}：{{ packageInfo.templateVersionNo || '-' }}</span>
        <span>{{ t('productCenter.publish.publishedTime') }}：{{ formatUtc(packageInfo.publishedTime as string | undefined) }}</span>
        <span>{{ t('productCenter.publish.syncStatus') }}：{{ t('productCenter.status.enabled') }}</span>
      </div>
    </div>
    <quick-quote-panel @quote="$emit('quote')" />
  </section>
</template>

<script setup lang="ts">
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { formatUtc } from '@/utils/datetime'
import type { ProductRecord } from '@/api/product-capability/types'
import QuickQuotePanel from '@/pages/product-center/components/QuickQuotePanel.vue'

defineProps<{
  packageInfo: ProductRecord
}>()

defineEmits<{
  quote: []
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
</script>

<style scoped lang="scss">
.published-product-header {
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr) 300px;
  gap: 20px;
  padding: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.04);
}

.published-product-header__media {
  display: grid;
  min-height: 160px;
  place-items: center;
  border-radius: 8px;
  background:
    linear-gradient(135deg, rgba(37, 99, 235, 0.12), rgba(15, 23, 42, 0.02)),
    repeating-linear-gradient(90deg, #f8fafc 0 16px, #eef2ff 16px 18px);

  span {
    color: #2563eb;
    font-size: 13px;
    font-weight: 650;
  }
}

.published-product-header__summary h2 {
  margin: 10px 0 14px;
  font-size: 24px;
}

.published-product-header__meta {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px 18px;

  span {
    color: #475569;
    font-size: 13px;
  }
}

@media (max-width: 1180px) {
  .published-product-header {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .published-product-header__meta {
    grid-template-columns: 1fr;
  }
}
</style>
