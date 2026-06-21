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
import { useProductDict } from '@/hooks/useProductDict'
import {
  productBaseAttributeApi,
  productCategoryApi,
  productUnitApi
} from '@/api/product-capability/base'
import { productComponentApi, productComponentItemApi } from '@/api/product-capability/component'
import { fabricSeriesApi } from '@/api/product-capability/fabric'
import { productMaterialApi, productMaterialAttributeApi } from '@/api/product-capability/material'
import type { ProductRecord, ProductUnitVO } from '@/api/product-capability/types'
import ProductEntityGridPage, { type ProductGridConfig } from '@/pages/product-center/components/ProductEntityGridPage.vue'

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const route = useRoute()
const router = useRouter()
const activeTab = ref('category')
const unitList = ref<ProductUnitVO[]>([])
const { options: productDictOptions } = useProductDict(
  'product_unit_type',
  'product_material_type',
  'product_component_type',
  'product_business_type',
  'product_attribute_group',
  'engineering_qty_mode'
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
  { label: t('productCenter.baseAttribute.valueTypeText'), value: 'TEXT' },
  { label: t('productCenter.baseAttribute.valueTypeNumber'), value: 'NUMBER' },
  { label: t('productCenter.baseAttribute.valueTypeBoolean'), value: 'BOOLEAN' },
  { label: t('productCenter.baseAttribute.valueTypeEnum'), value: 'ENUM' }
])

function unitOption(unit: ProductUnitVO) {
  const code = String(unit.unitCode || '')
  const name = localeStore.language === 'zh_CN' ? unit.unitNameCn : unit.unitNameEn || unit.unitNameCn
  return { value: code, label: `${code} ${name || ''}`.trim() }
}

const unitOptions = computed(() => unitList.value.map(unitOption))
const unitTypeOptions = computed(() => productDictOptions.value.product_unit_type || [])
const roundingModeOptions = computed(() => [
  { label: t('productCenter.unit.roundingHalfUp'), value: 'HALF_UP' },
  { label: t('productCenter.unit.roundingUp'), value: 'UP' },
  { label: t('productCenter.unit.roundingDown'), value: 'DOWN' }
])
const qtyModeOptions = computed(() => productDictOptions.value.engineering_qty_mode || [])

function labelOf(row: ProductRecord, codeKey: string, cnKey: string, enKey?: string) {
  const code = String(row[codeKey] || '')
  const name = localeStore.language === 'zh_CN' ? row[cnKey] : row[enKey || cnKey] || row[cnKey]
  return `${code} ${String(name || '')}`.trim()
}

async function loadMaterialOptions(form?: ProductRecord) {
  const response = await productMaterialApi.options?.({
    status: 'ENABLED',
    materialType: form?.materialType as string | undefined,
    pageNum: 1,
    pageSize: 500
  })
  const rows = Array.isArray(response) ? response : response?.data || []
  return rows.map((row) => ({ value: row.materialCode, label: labelOf(row, 'materialCode', 'materialNameCn', 'materialNameEn'), record: row }))
}

async function loadFabricSeriesOptions() {
  const response = await fabricSeriesApi.options?.({ status: 'ENABLED', materialType: 'FABRIC', pageNum: 1, pageSize: 500 })
  const rows = Array.isArray(response) ? response : response?.data || []
  return rows.map((row) => ({ value: row.seriesCode, label: labelOf(row, 'seriesCode', 'seriesNameCn', 'seriesNameEn'), record: row }))
}

async function loadCategoryOptions() {
  const response = await productCategoryApi.options?.({ status: 'ENABLED', pageNum: 1, pageSize: 500 })
  const rows = Array.isArray(response) ? response : response?.data || []
  return rows.map((row) => ({ value: row.categoryId, label: labelOf(row, 'categoryCode', 'categoryNameCn', 'categoryNameEn'), record: row }))
}

async function loadComponentOptions() {
  const response = await productComponentApi.options?.({ status: 'ENABLED', pageNum: 1, pageSize: 500 })
  const rows = Array.isArray(response) ? response : response?.data || []
  return rows.map((row) => ({ value: row.componentCode, label: labelOf(row, 'componentCode', 'componentNameCn', 'componentNameEn'), record: row }))
}

async function loadUnitOptions(form?: ProductRecord) {
  const unitType = form?.unitType as string | undefined
  return unitList.value
    .filter((unit) => !unitType || unit.unitType === unitType)
    .map(unitOption)
}

async function loadBaseAttributeOptions(form?: ProductRecord) {
  const materialType = String(form?.materialType || '')
  if (!materialType) return []
  const response = await productBaseAttributeApi.options?.({
    status: 'ENABLED',
    materialTypes: materialType,
    pageNum: 1,
    pageSize: 500
  })
  const rows = Array.isArray(response) ? response : response?.data || []
  return rows.map((row) => ({
    value: row.attributeCode,
    label: labelOf(row, 'attributeCode', 'attributeNameCn', 'attributeNameEn'),
    record: row
  }))
}

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
    tree: { parentKey: 'parentId', rowKey: 'categoryId', rootValue: 0 },
    fields: [
      { prop: 'categoryCode', labelKey: 'productCenter.category.code', search: true, required: true },
      { prop: 'categoryNameCn', labelKey: 'productCenter.category.name', search: true, required: true },
      { prop: 'categoryNameEn', labelKey: 'productCenter.category.nameEn' },
      { prop: 'parentId', labelKey: 'productCenter.category.parentCategory', type: 'tree-select', optionLoader: loadCategoryOptions, table: false },
      { prop: 'categoryLevel', labelKey: 'productCenter.category.level', type: 'number', form: false, table: false },
      { prop: 'categoryPath', labelKey: 'productCenter.category.path', table: false, readonly: () => true },
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
    superEditPermission: 'product:base:superEdit',
    api: productMaterialApi,
    defaultSort: { prop: 'updateTime', order: 'descending' },
    attachments: { targetType: 'MATERIAL', targetCodeField: 'materialCode', defaultUsageType: 'SPEC' },
    optionLoaders: {
      __unitOptions: async () => unitOptions.value
    },
    rowActions: [
      {
        labelKey: 'productCenter.material.manageAttributes',
        icon: 'List',
        permission: 'product:material-attribute:list',
        handler: (row) => pushTab('/product-master/materials', 'materialAttribute', { materialCode: String(row.materialCode || '') })
      }
    ],
    fields: [
      { prop: 'materialCode', labelKey: 'productCenter.material.code', search: true, required: true, sortable: true, sectionKey: 'basic', sectionLabelKey: 'productCenter.formSection.basic' },
      { prop: 'materialNameCn', labelKey: 'productCenter.material.name', search: true, required: true, sortable: true, sectionKey: 'basic' },
      { prop: 'materialNameEn', labelKey: 'productCenter.material.nameEn', table: false, sectionKey: 'basic' },
      { prop: 'materialType', labelKey: 'productCenter.material.type', type: 'select', options: productDictOptions.value.product_material_type || [], search: true, clearFields: ['attributeList', 'fabricSeriesId', 'fabricSeriesCode', 'fabricSeriesNameCn'], sectionKey: 'basic' },
      {
        prop: 'fabricSeriesCode',
        labelKey: 'productCenter.material.fabricSeries',
        type: 'remote-select',
        optionLoader: loadFabricSeriesOptions,
        fillFields: { fabricSeriesId: 'seriesId', fabricSeriesNameCn: 'seriesNameCn' },
        visible: (form) => form.materialType === 'FABRIC',
        search: true,
        required: true,
        minWidth: 180,
        sectionKey: 'basic'
      },
      { prop: 'fabricSeriesNameCn', labelKey: 'productCenter.material.fabricSeriesName', form: false, minWidth: 180 },
      { prop: 'businessType', labelKey: 'productCenter.material.businessType', type: 'select', options: productDictOptions.value.product_business_type || [], search: true, sectionKey: 'basic' },
      { prop: 'attributeList', labelKey: 'productCenter.material.typeAttributes', type: 'material-attributes', optionLoader: loadBaseAttributeOptions, table: false, formSpan: 2, sectionKey: 'attributes', sectionLabelKey: 'productCenter.formSection.typeAttributes' },
      { prop: 'unitCode', labelKey: 'productCenter.common.unitCode', type: 'select', options: unitOptions.value, sortable: true, sectionKey: 'erp', sectionLabelKey: 'productCenter.formSection.unitsErp' },
      { prop: 'purchaseUnitCode', labelKey: 'productCenter.material.purchaseUnitCode', type: 'select', options: unitOptions.value, table: false, sectionKey: 'erp' },
      { prop: 'inventoryUnitCode', labelKey: 'productCenter.material.inventoryUnitCode', type: 'select', options: unitOptions.value, table: false, sectionKey: 'erp' },
      { prop: 'usageUnitCode', labelKey: 'productCenter.material.usageUnitCode', type: 'select', options: unitOptions.value, sectionKey: 'erp' },
      { prop: 'purchaseEnabled', labelKey: 'productCenter.material.purchaseEnabled', type: 'boolean', table: false, sectionKey: 'erp' },
      { prop: 'inventoryEnabled', labelKey: 'productCenter.material.inventoryEnabled', type: 'boolean', table: false, sectionKey: 'erp' },
      { prop: 'supplierCode', labelKey: 'productCenter.material.supplierCode', table: false, sectionKey: 'supplier', sectionLabelKey: 'productCenter.formSection.supplier' },
      { prop: 'supplierName', labelKey: 'productCenter.material.supplierName', search: true, sortable: true, minWidth: 160, sectionKey: 'supplier' },
      { prop: 'vendorItemNo', labelKey: 'productCenter.material.vendorItemNo', search: true, sortable: true, minWidth: 150, sectionKey: 'supplier' },
      { prop: 'purchaseUnitPrice', labelKey: 'productCenter.material.purchaseUnitPrice', type: 'number', width: 130, sectionKey: 'supplier' },
      { prop: 'costUnitPrice', labelKey: 'productCenter.material.costUnitPrice', type: 'number', width: 130, sectionKey: 'supplier' },
      { prop: 'priceCurrencyCode', labelKey: 'productCenter.material.priceCurrencyCode', table: false, sectionKey: 'supplier' },
      { prop: 'primarySpec', labelKey: 'productCenter.material.primarySpec', sectionKey: 'spec', sectionLabelKey: 'productCenter.formSection.spec' },
      { prop: 'specSummary', labelKey: 'productCenter.material.specSummary', formSpan: 2, sectionKey: 'spec' },
      { prop: 'primaryColor', labelKey: 'productCenter.material.primaryColor', sectionKey: 'spec' },
      { prop: 'primaryWeight', labelKey: 'productCenter.material.primaryWeight', type: 'number', table: false, sectionKey: 'spec' },
      { prop: 'sampleBookNo', labelKey: 'productCenter.material.sampleBookNo', table: false, sectionKey: 'spec' },
      { prop: 'status', labelKey: 'productCenter.common.status', type: 'status', search: true },
      { prop: 'remark', labelKey: 'productCenter.common.remark', type: 'textarea', table: false, formSpan: 2, sectionKey: 'note', sectionLabelKey: 'productCenter.formSection.note' }
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
    closePath: '/product-master/materials',
    defaultSort: { prop: 'sortOrder', order: 'ascending' },
    fields: [
      {
        prop: 'materialCode',
        labelKey: 'productCenter.material.code',
        type: 'remote-select',
        search: true,
        required: true,
        optionLoader: loadMaterialOptions,
        fillFields: { materialNameCn: 'materialNameCn' },
        sortable: true
      },
      { prop: 'attributeCode', labelKey: 'productCenter.materialAttribute.attributeCode', search: true, required: true, sortable: true },
      { prop: 'attributeNameCn', labelKey: 'productCenter.materialAttribute.attributeNameCn', sortable: true },
      { prop: 'valueText', labelKey: 'productCenter.materialAttribute.valueText' },
      { prop: 'valueNumber', labelKey: 'productCenter.materialAttribute.valueNumber', type: 'number' },
      { prop: 'valueBool', labelKey: 'productCenter.materialAttribute.valueBool', type: 'boolean' },
      { prop: 'valueUnitCode', labelKey: 'productCenter.materialAttribute.valueUnitCode', type: 'select', options: unitOptions.value },
      { prop: 'sortOrder', labelKey: 'productCenter.common.sortOrder', type: 'number', sortable: true },
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
    defaultSort: { prop: 'updateTime', order: 'descending' },
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
      { prop: 'componentCode', labelKey: 'productCenter.component.code', search: true, required: true, sortable: true, sectionKey: 'basic', sectionLabelKey: 'productCenter.formSection.basic' },
      { prop: 'componentNameCn', labelKey: 'productCenter.component.name', search: true, required: true, sortable: true, sectionKey: 'basic' },
      { prop: 'componentNameEn', labelKey: 'productCenter.component.nameEn', table: false, sectionKey: 'basic' },
      { prop: 'componentType', labelKey: 'productCenter.component.type', type: 'select', options: productDictOptions.value.product_component_type || [], search: true, sectionKey: 'basic' },
      { prop: 'businessType', labelKey: 'productCenter.material.businessType', type: 'select', options: productDictOptions.value.product_business_type || [], search: true, sectionKey: 'basic' },
      { prop: 'defaultQty', labelKey: 'productCenter.component.defaultQty', type: 'number' },
      { prop: 'qtyMode', labelKey: 'productCenter.component.qtyMode', type: 'select', options: qtyModeOptions.value },
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
    defaultSort: { prop: 'sortOrder', order: 'ascending' },
    fields: [
      {
        prop: 'componentCode',
        labelKey: 'productCenter.component.code',
        type: 'remote-select',
        search: true,
        required: true,
        optionLoader: loadComponentOptions,
        fillFields: { componentNameCn: 'componentNameCn' },
        sortable: true
      },
      {
        prop: 'materialCode',
        labelKey: 'productCenter.componentItem.materialCode',
        type: 'remote-select',
        search: true,
        required: true,
        optionLoader: loadMaterialOptions,
        fillFields: { materialNameCn: 'materialNameCn', unitCode: 'usageUnitCode' },
        sortable: true
      },
      { prop: 'materialNameCn', labelKey: 'productCenter.componentItem.materialNameCn', sortable: true },
      { prop: 'qtyFormula', labelKey: 'productCenter.componentItem.qtyFormula' },
      { prop: 'defaultQty', labelKey: 'productCenter.componentItem.defaultQty', type: 'number' },
      { prop: 'unitCode', labelKey: 'productCenter.componentItem.unitCode', type: 'select', options: unitOptions.value },
      { prop: 'requiredFlag', labelKey: 'productCenter.componentItem.requiredFlag', type: 'boolean' },
      { prop: 'sortOrder', labelKey: 'productCenter.common.sortOrder', type: 'number', sortable: true },
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
    defaultSort: { prop: 'sortOrder', order: 'ascending' },
    fields: [
      { prop: 'attributeGroup', labelKey: 'productCenter.baseAttribute.group', type: 'select', options: productDictOptions.value.product_attribute_group || [], search: true, required: true },
      { prop: 'attributeCode', labelKey: 'productCenter.baseAttribute.code', search: true, required: true, sortable: true },
      { prop: 'attributeNameCn', labelKey: 'productCenter.baseAttribute.name', search: true, required: true, sortable: true },
      { prop: 'attributeNameEn', labelKey: 'productCenter.baseAttribute.nameEn' },
      { prop: 'valueType', labelKey: 'productCenter.baseAttribute.valueType', type: 'select', options: valueTypeOptions.value, required: true },
      { prop: 'unitCode', labelKey: 'productCenter.baseAttribute.unitCode', type: 'select', options: unitOptions.value, visible: (form) => form.valueType === 'NUMBER' },
      { prop: 'materialTypes', labelKey: 'productCenter.baseAttribute.materialTypes', type: 'select', multiple: true, valueMode: 'csv', options: productDictOptions.value.product_material_type || [] },
      { prop: 'extraJson', labelKey: 'productCenter.baseAttribute.extraJson', type: 'textarea', table: false, form: false, formSpan: 2 },
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
    defaultSort: { prop: 'sortOrder', order: 'ascending' },
    fields: [
      { prop: 'unitCode', labelKey: 'productCenter.unit.code', search: true, required: true, sortable: true },
      { prop: 'unitNameCn', labelKey: 'productCenter.unit.name', search: true, required: true, sortable: true },
      { prop: 'unitNameEn', labelKey: 'productCenter.unit.nameEn' },
      { prop: 'unitType', labelKey: 'productCenter.unit.type', type: 'select', options: unitTypeOptions.value, search: true },
      { prop: 'precisionScale', labelKey: 'productCenter.unit.precisionScale', type: 'number' },
      { prop: 'roundingMode', labelKey: 'productCenter.unit.roundingMode', type: 'select', options: roundingModeOptions.value },
      { prop: 'baseUnitCode', labelKey: 'productCenter.unit.baseUnitCode', type: 'remote-select', optionLoader: loadUnitOptions },
      { prop: 'conversionRate', labelKey: 'productCenter.unit.conversionRateHelp', type: 'number' },
      { prop: 'status', labelKey: 'productCenter.common.status', type: 'status', search: true },
      { prop: 'sortOrder', labelKey: 'productCenter.common.sortOrder', type: 'number', sortable: true },
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
