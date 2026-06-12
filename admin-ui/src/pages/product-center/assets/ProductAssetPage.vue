<template>
  <div class="app-container product-center-page">
    <product-entity-grid-page v-if="singleMode && activeConfig" :config="activeConfig" />
    <el-tabs v-else v-model="activeTab">
      <el-tab-pane v-for="item in configs" :key="item.key" :label="t(item.titleKey)" :name="item.key">
        <product-entity-grid-page v-if="activeTab === item.key" :config="item" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts" name="ProductAssetPage">
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { productMediaAssetApi, productMediaBindingApi } from '@/api/product-capability/asset'
import ProductEntityGridPage, { type ProductGridConfig } from '@/pages/product-center/components/ProductEntityGridPage.vue'

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const route = useRoute()
const activeTab = ref('asset')

const routeTabMap: Record<string, string> = {
  'media-assets': 'asset',
  'media-bindings': 'binding'
}

const assetPermissions = { add: 'product:asset:upload', edit: 'product:asset:upload', remove: 'product:asset:upload', reference: 'product:asset:reference' }
const bindingPermissions = { add: 'product:asset:bind', edit: 'product:asset:bind', remove: 'product:asset:bind', reference: 'product:asset:reference' }

const configs = computed<ProductGridConfig[]>(() => [
  {
    key: 'asset',
    titleKey: 'productCenter.asset.title',
    descriptionKey: 'productCenter.asset.description',
    idKey: 'assetId',
    permissions: assetPermissions,
    api: productMediaAssetApi,
    fields: [
      { prop: 'assetCode', labelKey: 'productCenter.asset.code', search: true, required: true },
      { prop: 'assetNameCn', labelKey: 'productCenter.asset.name', search: true, required: true },
      { prop: 'assetNameEn', labelKey: 'productCenter.asset.nameEn' },
      { prop: 'assetType', labelKey: 'productCenter.asset.type', search: true },
      { prop: 'usageType', labelKey: 'productCenter.asset.usageType', search: true },
      { prop: 'languageCode', labelKey: 'productCenter.asset.languageCode' },
      { prop: 'visibility', labelKey: 'productCenter.asset.visibility' },
      { prop: 'ossId', labelKey: 'productCenter.asset.ossId', type: 'number' },
      { prop: 'url', labelKey: 'productCenter.asset.url', type: 'url', formSpan: 2 },
      { prop: 'versionNo', labelKey: 'productCenter.asset.versionNo', type: 'number' },
      { prop: 'status', labelKey: 'productCenter.common.status', type: 'status', search: true },
      { prop: 'remark', labelKey: 'productCenter.common.remark', type: 'textarea', table: false, formSpan: 2 }
    ]
  },
  {
    key: 'binding',
    titleKey: 'productCenter.binding.title',
    descriptionKey: 'productCenter.binding.description',
    idKey: 'bindingId',
    permissions: bindingPermissions,
    api: productMediaBindingApi,
    fields: [
      { prop: 'assetId', labelKey: 'productCenter.asset.id', type: 'number', required: true },
      { prop: 'assetCode', labelKey: 'productCenter.asset.code', search: true, required: true },
      { prop: 'targetType', labelKey: 'productCenter.binding.targetType', search: true, required: true },
      { prop: 'targetId', labelKey: 'productCenter.binding.targetId', type: 'number' },
      { prop: 'targetCode', labelKey: 'productCenter.binding.targetCode', search: true, required: true },
      { prop: 'usageType', labelKey: 'productCenter.asset.usageType' },
      { prop: 'visibility', labelKey: 'productCenter.asset.visibility' },
      { prop: 'languageCode', labelKey: 'productCenter.asset.languageCode' },
      { prop: 'requiredForPublish', labelKey: 'productCenter.binding.requiredForPublish' },
      { prop: 'sortOrder', labelKey: 'productCenter.common.sortOrder', type: 'number' },
      { prop: 'status', labelKey: 'productCenter.common.status', type: 'status', search: true },
      { prop: 'remark', labelKey: 'productCenter.common.remark', type: 'textarea', table: false, formSpan: 2 }
    ]
  }
])

const routeTab = computed(() => routeTabMap[String(route.path).split('/').pop() || ''])
const singleMode = computed(() => Boolean(routeTab.value))
const activeConfig = computed(() => configs.value.find((item) => item.key === activeTab.value))

watch(routeTab, (tab) => {
  if (tab) activeTab.value = tab
}, { immediate: true })
</script>
