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
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { useDict } from '@/utils/dict'
import {
  productBaseAttributeApi,
  productCategoryApi,
  productUnitApi
} from '@/api/product-capability/base'
import { productComponentApi, productComponentItemApi } from '@/api/product-capability/component'
import { productMaterialApi, productMaterialAttributeApi } from '@/api/product-capability/material'
import type { ProductUnitVO } from '@/api/product-capability/types'
import ProductEntityGridPage, { type ProductGridConfig } from '@/pages/product-center/components/ProductEntityGridPage.vue'

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const route = useRoute()
const router = useRouter()
const activeTab = ref('category')
const unitList = ref<ProductUnitVO[]>([])
const { product_material_type, product_component_type, product_business_type } = useDict(
  'product_material_type',
  'product_component_type',
  'product_business_type'
)

const routeTabMap: Record<string, string> = {
  categories: 'category',
  materials: 'material',
  components: 'component',
  'base-attributes': 'baseAttribute',
  units: 'unit',
  'material-attributes': 'materialAttribute',
  'component-items': 'componentItem'
}

const valueTypeOptions = computed(() => [
  { label: t('productCenter.baseAttribute.valueTypeText'), value: 'text' },
  { label: t('productCenter.baseAttribute.valueTypeNumber'), value: 'number' },
  { label: t('productCenter.baseAttribute.valueTypeBoolean'), value: 'boolean' },
  { label: t('productCenter.baseAttribute.valueTypeEnum'), value: 'enum' }
])

function unitOption(unit: ProductUnitVO) {
  const code = String(unit.unitCode || '')
  const name = localeStore.language === 'zh_CN' ? unit.unitNameCn : unit.unitNameEn || unit.unitNameCn
  return { value: code, label: `${code} ${name || ''}`.trim() }
}

const unitOptions = computed(() => unitList.value.map(unitOption))

async function loadUnits() {
  const response = await productUnitApi.options?.({ status: 'ENABLED' })
  unitList.value = Array.isArray(response) ? response : response?.data || []
}

onMounted(() => {
  loadUnits()
})

function pushTab(path: string, tab: string, query: Record<string, string>) {
  return router.push({ path, query: { ...query, tab } }).then(() => undefined)
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
      { prop: 'categoryNameCn', labelKey: 'productCenter.category.name', search: true, required: true },
      { prop: 'categoryNameEn', labelKey: 'productCenter.category.nameEn' },
      { prop: 'parentId', labelKey: 'productCenter.category.parentId', type: 'number' },
      { prop: 'categoryLevel', labelKey: 'productCenter.category.level', type: 'number', form: false },
      { prop: 'categoryPath', labelKey: 'productCenter.category.path' },
      { prop: 'status', labelKey: 'productCenter.common.status', type: 'status', search: true },
      { prop: 'sortOrder', labelKey: 'productCenter.common.sortOrder', type: 'number' },
      { prop: 'remark', labelKey: 'productCenter.common.remark', type: 'textarea', table: false, formSpan: 2 }
    ]
  },
  {
    key: 'material',
    titleKey: 'productCenter.material.title',
    descriptionKey: 'productCenter.material.description',
    idKey: 'materialId',
    permissions: { add: 'product:base:add', edit: 'product:base:edit', remove: 'product:base:remove', reference: 'product:base:reference' },
    api: productMaterialApi,
    attachments: { targetType: 'MATERIAL', targetCodeField: 'materialCode', defaultUsageType: 'SPEC' },
    rowActions: [
      {
        labelKey: 'productCenter.material.manageAttributes',
        icon: 'List',
        permission: 'product:material-attribute:list',
        handler: (row) => pushTab('/product-master/materials', 'materialAttribute', { materialCode: String(row.materialCode || '') })
      }
    ],
    fields: [
      { prop: 'materialCode', labelKey: 'productCenter.material.code', search: true, required: true },
      { prop: 'materialNameCn', labelKey: 'productCenter.material.name', search: true, required: true },
      { prop: 'materialNameEn', labelKey: 'productCenter.material.nameEn' },
      { prop: 'materialType', labelKey: 'productCenter.material.type', type: 'select', options: product_material_type.value, search: true },
      { prop: 'businessType', labelKey: 'productCenter.material.businessType', type: 'select', options: product_business_type.value, search: true },
      { prop: 'unitCode', labelKey: 'productCenter.common.unitCode', type: 'select', options: unitOptions.value },
      { prop: 'supplierCode', labelKey: 'productCenter.material.supplierCode' },
      { prop: 'supplierName', labelKey: 'productCenter.material.supplierName' },
      { prop: 'sampleBookNo', labelKey: 'productCenter.material.sampleBookNo' },
      { prop: 'vendorItemNo', labelKey: 'productCenter.material.vendorItemNo' },
      { prop: 'status', labelKey: 'productCenter.common.status', type: 'status', search: true },
      { prop: 'remark', labelKey: 'productCenter.common.remark', type: 'textarea', table: false, formSpan: 2 }
    ]
  },
  {
    key: 'materialAttribute',
    titleKey: 'productCenter.materialAttribute.title',
    descriptionKey: 'productCenter.materialAttribute.description',
    idKey: 'attributeValueId',
    permissions: {
      add: 'product:material-attribute:add',
      edit: 'product:material-attribute:edit',
      remove: 'product:material-attribute:remove',
      reference: 'product:material-attribute:reference'
    },
    api: productMaterialAttributeApi,
    fields: [
      { prop: 'materialCode', labelKey: 'productCenter.material.code', search: true, required: true },
      { prop: 'attributeCode', labelKey: 'productCenter.materialAttribute.attributeCode', search: true, required: true },
      { prop: 'attributeNameCn', labelKey: 'productCenter.materialAttribute.attributeNameCn' },
      { prop: 'valueText', labelKey: 'productCenter.materialAttribute.valueText' },
      { prop: 'valueNumber', labelKey: 'productCenter.materialAttribute.valueNumber', type: 'number' },
      { prop: 'valueBool', labelKey: 'productCenter.materialAttribute.valueBool', type: 'boolean' },
      { prop: 'valueUnitCode', labelKey: 'productCenter.materialAttribute.valueUnitCode', type: 'select', options: unitOptions.value },
      { prop: 'sortOrder', labelKey: 'productCenter.common.sortOrder', type: 'number' },
      { prop: 'status', labelKey: 'productCenter.common.status', type: 'status', search: true }
    ]
  },
  {
    key: 'component',
    titleKey: 'productCenter.component.title',
    descriptionKey: 'productCenter.component.description',
    idKey: 'componentId',
    permissions: { add: 'product:base:add', edit: 'product:base:edit', remove: 'product:base:remove', reference: 'product:base:reference' },
    api: productComponentApi,
    attachments: { targetType: 'COMPONENT', targetCodeField: 'componentCode', defaultUsageType: 'INSTALL_GUIDE' },
    rowActions: [
      {
        labelKey: 'productCenter.component.manageItems',
        icon: 'List',
        permission: 'product:component-item:list',
        handler: (row) => pushTab('/product-master/components', 'componentItem', { componentCode: String(row.componentCode || '') })
      }
    ],
    fields: [
      { prop: 'componentCode', labelKey: 'productCenter.component.code', search: true, required: true },
      { prop: 'componentNameCn', labelKey: 'productCenter.component.name', search: true, required: true },
      { prop: 'componentNameEn', labelKey: 'productCenter.component.nameEn' },
      { prop: 'componentType', labelKey: 'productCenter.component.type', type: 'select', options: product_component_type.value, search: true },
      { prop: 'businessType', labelKey: 'productCenter.material.businessType', type: 'select', options: product_business_type.value, search: true },
      { prop: 'defaultQty', labelKey: 'productCenter.component.defaultQty', type: 'number' },
      { prop: 'qtyMode', labelKey: 'productCenter.component.qtyMode' },
      { prop: 'unitCode', labelKey: 'productCenter.common.unitCode', type: 'select', options: unitOptions.value },
      { prop: 'status', labelKey: 'productCenter.common.status', type: 'status', search: true },
      { prop: 'remark', labelKey: 'productCenter.common.remark', type: 'textarea', table: false, formSpan: 2 }
    ]
  },
  {
    key: 'componentItem',
    titleKey: 'productCenter.componentItem.title',
    descriptionKey: 'productCenter.componentItem.description',
    idKey: 'componentItemId',
    permissions: {
      add: 'product:component-item:add',
      edit: 'product:component-item:edit',
      remove: 'product:component-item:remove',
      reference: 'product:component-item:reference'
    },
    api: productComponentItemApi,
    fields: [
      { prop: 'componentCode', labelKey: 'productCenter.component.code', search: true, required: true },
      { prop: 'materialCode', labelKey: 'productCenter.componentItem.materialCode', search: true, required: true },
      { prop: 'materialNameCn', labelKey: 'productCenter.componentItem.materialNameCn' },
      { prop: 'qtyFormula', labelKey: 'productCenter.componentItem.qtyFormula' },
      { prop: 'defaultQty', labelKey: 'productCenter.componentItem.defaultQty', type: 'number' },
      { prop: 'unitCode', labelKey: 'productCenter.componentItem.unitCode', type: 'select', options: unitOptions.value },
      { prop: 'requiredFlag', labelKey: 'productCenter.componentItem.requiredFlag', type: 'boolean' },
      { prop: 'sortOrder', labelKey: 'productCenter.common.sortOrder', type: 'number' },
      { prop: 'status', labelKey: 'productCenter.common.status', type: 'status', search: true }
    ]
  },
  {
    key: 'baseAttribute',
    titleKey: 'productCenter.baseAttribute.title',
    descriptionKey: 'productCenter.baseAttribute.description',
    idKey: 'attributeId',
    permissions: {
      add: 'product:base-attribute:add',
      edit: 'product:base-attribute:edit',
      remove: 'product:base-attribute:remove',
      reference: 'product:base-attribute:reference'
    },
    api: productBaseAttributeApi,
    fields: [
      { prop: 'attributeGroup', labelKey: 'productCenter.baseAttribute.group', search: true, required: true },
      { prop: 'attributeCode', labelKey: 'productCenter.baseAttribute.code', search: true, required: true },
      { prop: 'attributeNameCn', labelKey: 'productCenter.baseAttribute.name', search: true, required: true },
      { prop: 'attributeNameEn', labelKey: 'productCenter.baseAttribute.nameEn' },
      { prop: 'valueType', labelKey: 'productCenter.baseAttribute.valueType', type: 'select', options: valueTypeOptions.value },
      { prop: 'unitCode', labelKey: 'productCenter.baseAttribute.unitCode', type: 'select', options: unitOptions.value },
      { prop: 'materialTypes', labelKey: 'productCenter.baseAttribute.materialTypes' },
      { prop: 'extraJson', labelKey: 'productCenter.baseAttribute.extraJson', type: 'textarea', table: false, formSpan: 2 },
      { prop: 'status', labelKey: 'productCenter.common.status', type: 'status', search: true },
      { prop: 'remark', labelKey: 'productCenter.common.remark', type: 'textarea', table: false, formSpan: 2 }
    ]
  },
  {
    key: 'unit',
    titleKey: 'productCenter.unit.title',
    descriptionKey: 'productCenter.unit.description',
    idKey: 'unitId',
    permissions: { add: 'product:unit:add', edit: 'product:unit:edit', remove: 'product:unit:remove', reference: 'product:unit:reference' },
    api: productUnitApi,
    fields: [
      { prop: 'unitCode', labelKey: 'productCenter.unit.code', search: true, required: true },
      { prop: 'unitNameCn', labelKey: 'productCenter.unit.name', search: true, required: true },
      { prop: 'unitNameEn', labelKey: 'productCenter.unit.nameEn' },
      { prop: 'unitType', labelKey: 'productCenter.unit.type', search: true },
      { prop: 'precisionScale', labelKey: 'productCenter.unit.precisionScale', type: 'number' },
      { prop: 'roundingMode', labelKey: 'productCenter.unit.roundingMode' },
      { prop: 'baseUnitCode', labelKey: 'productCenter.unit.baseUnitCode' },
      { prop: 'conversionRate', labelKey: 'productCenter.unit.conversionRate', type: 'number' },
      { prop: 'status', labelKey: 'productCenter.common.status', type: 'status', search: true },
      { prop: 'sortOrder', labelKey: 'productCenter.common.sortOrder', type: 'number' },
      { prop: 'remark', labelKey: 'productCenter.common.remark', type: 'textarea', table: false, formSpan: 2 }
    ]
  }
])

const routeTab = computed(() => {
  const queryTab = typeof route.query.tab === 'string' ? route.query.tab : ''
  if (queryTab && configs.value.some((item) => item.key === queryTab)) return queryTab
  return routeTabMap[String(route.path).split('/').pop() || '']
})

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
