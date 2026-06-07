<template>
  <div class="app-container sales-readonly">
    <section class="sales-readonly__header">
      <div>
        <p>{{ t('productCenter.menu.title') }}</p>
        <h1>{{ t('productCenter.sales.title') }}</h1>
        <span>{{ t('productCenter.sales.workbenchHint') }}</span>
      </div>
      <el-tag type="success">{{ t('productCenter.workbench.readOnly') }}</el-tag>
    </section>

    <published-product-header :package-info="firstPackage" @quote="goQuotePreview" />

    <readonly-ability-cards :items="summaryCards" />

    <el-row :gutter="12">
      <el-col :xs="24" :xl="16">
        <product-entity-grid-page :config="packageConfig" />
      </el-col>
      <el-col :xs="24" :xl="8">
        <sales-restriction-block />
        <version-history-timeline :items="versionSteps" />
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts" name="SalesReadOnlyPage">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { salesViewPackageApi } from '@/api/product-capability/publish'
import type { ProductRecord } from '@/api/product-capability/types'
import ProductEntityGridPage, { type ProductGridConfig } from '@/pages/product-center/components/ProductEntityGridPage.vue'
import PublishedProductHeader from '@/pages/product-center/components/PublishedProductHeader.vue'
import ReadonlyAbilityCards from '@/pages/product-center/components/ReadonlyAbilityCards.vue'
import SalesRestrictionBlock from '@/pages/product-center/components/SalesRestrictionBlock.vue'
import VersionHistoryTimeline from '@/pages/product-center/components/VersionHistoryTimeline.vue'

const localeStore = useLocaleStore()
const router = useRouter()
const t = (key: string) => getMessage(key, localeStore.language)
const packages = ref<ProductRecord[]>([])
const firstPackage = computed(() => packages.value[0] || {})

const summaryCards = computed(() => [
  { key: 'config', titleKey: 'productCenter.sales.configSummary', hintKey: 'productCenter.sales.configSummaryHint', type: 'success' as const, status: 'OK' },
  { key: 'price', titleKey: 'productCenter.sales.priceStatus', hintKey: 'productCenter.sales.priceStatusHint', type: 'success' as const, status: 'OK' },
  { key: 'media', titleKey: 'productCenter.sales.customerMedia', hintKey: 'productCenter.sales.customerMediaHint', type: 'success' as const, status: 'READY' },
  { key: 'bom', titleKey: 'productCenter.sales.bomSummary', hintKey: 'productCenter.sales.bomSummaryHint', type: 'success' as const, status: 'OK' }
])
const versionSteps = computed(() => packages.value.slice(0, 5).map((item) => `${item.packageCode || '-'} / ${item.templateVersionNo || '-'}`))

const packageConfig = computed<ProductGridConfig>(() => ({
  key: 'salesPublishedPackage',
  titleKey: 'productCenter.sales.packageList',
  descriptionKey: 'productCenter.sales.description',
  idKey: 'packageId',
  readonly: true,
  hideReference: true,
  showDetail: true,
  permissions: {
    add: 'product:sales-view:disabled',
    edit: 'product:sales-view:disabled',
    remove: 'product:sales-view:disabled',
    reference: 'product:sales-view:list'
  },
  api: salesViewPackageApi,
  fields: [
    { prop: 'packageCode', labelKey: 'productCenter.publish.packageCode', search: true },
    { prop: 'productModelCode', labelKey: 'productCenter.model.code', search: true },
    { prop: 'salesVariantCode', labelKey: 'productCenter.variant.code', search: true },
    { prop: 'templateVersionNo', labelKey: 'productCenter.template.versionNo' },
    { prop: 'pricePlanCode', labelKey: 'productCenter.price.planCode', search: true },
    { prop: 'packageStatus', labelKey: 'productCenter.publish.packageStatus', search: true },
    { prop: 'packageHash', labelKey: 'productCenter.publish.packageHash' },
    { prop: 'effectiveFrom', labelKey: 'productCenter.publish.effectiveFrom', type: 'datetime' },
    { prop: 'effectiveTo', labelKey: 'productCenter.publish.effectiveTo', type: 'datetime' },
    { prop: 'publishedTime', labelKey: 'productCenter.publish.publishedTime', type: 'datetime' }
  ]
}))

function goQuotePreview() {
  router.push({
    path: '/product-config/quote-preview',
    query: {
      pricePlanVersionId: String(firstPackage.value.pricePlanVersionId || ''),
      pricePlanCode: String(firstPackage.value.pricePlanCode || '')
    }
  })
}

onMounted(async () => {
  const response = await salesViewPackageApi.list({ pageNum: 1, pageSize: 5 })
  packages.value = response.rows || []
})
</script>

<style scoped lang="scss">
.sales-readonly {
  display: flex;
  flex-direction: column;
  gap: 12px;
  color: #111827;
}

.sales-readonly__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.04);
}

.sales-readonly__header p {
  margin: 0 0 4px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.sales-readonly__header h1 {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
}

.sales-readonly__header span {
  display: block;
  margin-top: 8px;
  color: #64748b;
  font-size: 13px;
}

@media (max-width: 768px) {
  .sales-readonly__header {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
