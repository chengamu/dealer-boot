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

<script setup lang="ts" name="ProductModelPage">
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { useDict } from '@/utils/dict'
import { productModelApi, salesVariantApi } from '@/api/product-capability/model'
import ProductEntityGridPage, { type ProductGridConfig } from '@/pages/product-center/components/ProductEntityGridPage.vue'

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const route = useRoute()
const activeTab = ref('model')
const { product_unit } = useDict('product_unit')

const routeTabMap: Record<string, string> = {
  models: 'model',
  'sales-variants': 'variant'
}

const configs = computed<ProductGridConfig[]>(() => [
  {
    key: 'model',
    titleKey: 'productCenter.model.title',
    descriptionKey: 'productCenter.model.description',
    idKey: 'modelId',
    permissions: { add: 'product:model:add', edit: 'product:model:edit', remove: 'product:model:remove', reference: 'product:model:list' },
    api: productModelApi,
    fields: [
      { prop: 'modelCode', labelKey: 'productCenter.model.code', search: true, required: true },
      { prop: 'modelNameCn', labelKey: 'productCenter.model.nameCn', search: true, required: true },
      { prop: 'modelNameEn', labelKey: 'productCenter.model.nameEn' },
      { prop: 'categoryCode', labelKey: 'productCenter.category.code', search: true },
      { prop: 'categoryNameCn', labelKey: 'productCenter.category.nameCn' },
      { prop: 'productFamily', labelKey: 'productCenter.model.family', search: true },
      { prop: 'structureType', labelKey: 'productCenter.model.structureType' },
      { prop: 'salesMode', labelKey: 'productCenter.model.salesMode' },
      { prop: 'productNature', labelKey: 'productCenter.model.nature' },
      { prop: 'unitCode', labelKey: 'productCenter.common.unitCode', type: 'select', options: product_unit.value },
      { prop: 'publishStatus', labelKey: 'productCenter.model.publishStatus', type: 'status' },
      { prop: 'status', labelKey: 'productCenter.common.status', type: 'status', search: true },
      { prop: 'remark', labelKey: 'productCenter.common.remark', type: 'textarea', table: false }
    ]
  },
  {
    key: 'variant',
    titleKey: 'productCenter.variant.title',
    descriptionKey: 'productCenter.variant.description',
    idKey: 'variantId',
    permissions: { add: 'product:model:add', edit: 'product:model:edit', remove: 'product:model:remove', reference: 'product:model:list' },
    api: salesVariantApi,
    fields: [
      { prop: 'modelId', labelKey: 'productCenter.model.id', type: 'number', search: true, required: true },
      { prop: 'variantCode', labelKey: 'productCenter.variant.code', search: true, required: true },
      { prop: 'variantNameCn', labelKey: 'productCenter.variant.nameCn', search: true, required: true },
      { prop: 'variantNameEn', labelKey: 'productCenter.variant.nameEn' },
      { prop: 'marketCode', labelKey: 'productCenter.variant.marketCode', search: true },
      { prop: 'controlMethod', labelKey: 'productCenter.variant.controlMethod' },
      { prop: 'grade', labelKey: 'productCenter.variant.grade' },
      { prop: 'packageType', labelKey: 'productCenter.variant.packageType' },
      { prop: 'status', labelKey: 'productCenter.common.status', type: 'status', search: true },
      { prop: 'sortOrder', labelKey: 'productCenter.common.sortOrder', type: 'number' },
      { prop: 'remark', labelKey: 'productCenter.common.remark', type: 'textarea', table: false }
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
