<template>
  <div class="app-container product-center-page">
    <product-entity-grid-page v-if="singleMode && activeConfig" :config="activeConfig" />
    <el-tabs v-else v-model="activeTab" class="product-center-tabs">
      <el-tab-pane v-for="item in configs" :key="item.key" :label="t(item.titleKey)" :name="item.key">
        <product-entity-grid-page v-if="activeTab === item.key" :config="item" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts" name="ProductBaseInfoPage">
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { useDict } from '@/utils/dict'
import { productCategoryApi, productComponentApi, productMaterialApi } from '@/api/product-capability/base'
import ProductEntityGridPage, { type ProductGridConfig } from '@/pages/product-center/components/ProductEntityGridPage.vue'

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const route = useRoute()
const activeTab = ref('category')
const { product_material_type, product_component_type, product_business_type, product_unit } = useDict(
  'product_material_type',
  'product_component_type',
  'product_business_type',
  'product_unit'
)

const routeTabMap: Record<string, string> = {
  categories: 'category',
  materials: 'material',
  components: 'component'
}

const configs = computed<ProductGridConfig[]>(() => [
  {
    key: 'category',
    titleKey: 'productCenter.category.title',
    descriptionKey: 'productCenter.category.description',
    idKey: 'categoryId',
    permissions: { add: 'product:base:add', edit: 'product:base:edit', remove: 'product:base:remove', reference: 'product:base:reference' },
    api: productCategoryApi,
    fields: [
      { prop: 'categoryCode', labelKey: 'productCenter.category.code', search: true, required: true },
      { prop: 'categoryNameCn', labelKey: 'productCenter.category.nameCn', search: true, required: true },
      { prop: 'categoryNameEn', labelKey: 'productCenter.category.nameEn' },
      { prop: 'parentId', labelKey: 'productCenter.category.parentId', type: 'number' },
      { prop: 'categoryLevel', labelKey: 'productCenter.category.level', type: 'number', form: false },
      { prop: 'categoryPath', labelKey: 'productCenter.category.path' },
      { prop: 'status', labelKey: 'productCenter.common.status', type: 'status', search: true },
      { prop: 'sortOrder', labelKey: 'productCenter.common.sortOrder', type: 'number' },
      { prop: 'remark', labelKey: 'productCenter.common.remark', type: 'textarea', table: false }
    ]
  },
  {
    key: 'material',
    titleKey: 'productCenter.material.title',
    descriptionKey: 'productCenter.material.description',
    idKey: 'materialId',
    permissions: { add: 'product:base:add', edit: 'product:base:edit', remove: 'product:base:remove', reference: 'product:base:reference' },
    api: productMaterialApi,
    fields: [
      { prop: 'materialCode', labelKey: 'productCenter.material.code', search: true, required: true },
      { prop: 'materialNameCn', labelKey: 'productCenter.material.nameCn', search: true, required: true },
      { prop: 'materialNameEn', labelKey: 'productCenter.material.nameEn' },
      { prop: 'materialType', labelKey: 'productCenter.material.type', type: 'select', options: product_material_type.value, search: true },
      { prop: 'businessType', labelKey: 'productCenter.material.businessType', type: 'select', options: product_business_type.value },
      { prop: 'unitCode', labelKey: 'productCenter.common.unitCode', type: 'select', options: product_unit.value },
      { prop: 'supplierName', labelKey: 'productCenter.material.supplierName' },
      { prop: 'status', labelKey: 'productCenter.common.status', type: 'status', search: true },
      { prop: 'remark', labelKey: 'productCenter.common.remark', type: 'textarea', table: false }
    ]
  },
  {
    key: 'component',
    titleKey: 'productCenter.component.title',
    descriptionKey: 'productCenter.component.description',
    idKey: 'componentId',
    permissions: { add: 'product:base:add', edit: 'product:base:edit', remove: 'product:base:remove', reference: 'product:base:reference' },
    api: productComponentApi,
    fields: [
      { prop: 'componentCode', labelKey: 'productCenter.component.code', search: true, required: true },
      { prop: 'componentNameCn', labelKey: 'productCenter.component.nameCn', search: true, required: true },
      { prop: 'componentNameEn', labelKey: 'productCenter.component.nameEn' },
      { prop: 'componentType', labelKey: 'productCenter.component.type', type: 'select', options: product_component_type.value, search: true },
      { prop: 'materialCode', labelKey: 'productCenter.material.code', search: true },
      { prop: 'materialNameCn', labelKey: 'productCenter.material.nameCn' },
      { prop: 'defaultQty', labelKey: 'productCenter.component.defaultQty', type: 'number' },
      { prop: 'qtyMode', labelKey: 'productCenter.component.qtyMode' },
      { prop: 'unitCode', labelKey: 'productCenter.common.unitCode', type: 'select', options: product_unit.value },
      { prop: 'status', labelKey: 'productCenter.common.status', type: 'status', search: true },
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

<style scoped lang="scss">
.product-center-page {
  :deep(.el-tabs__content) {
    overflow: visible;
  }
}
</style>
