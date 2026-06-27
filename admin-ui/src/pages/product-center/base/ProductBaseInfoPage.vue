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
import { useRoute } from 'vue-router'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { useProductDict } from '@/hooks/useProductDict'
import {
  productBaseAttributeApi,
  productCategoryApi,
  productManufacturerApi,
  productMaterialTypeApi,
  productMaterialTypeGroupApi,
  productUnitApi
} from '@/api/product-capability/base'
import { productMaterialApi, productMaterialAttributeApi } from '@/api/product-capability/material'
import type { ProductRecord, ProductUnitVO } from '@/api/product-capability/types'
import ProductEntityGridPage from '@/pages/product-center/components/ProductEntityGridPage.vue'
import type { ProductGridConfig } from '@/pages/product-center/components/productGridTypes'
import { PRODUCT_STATUS_DISABLED, PRODUCT_STATUS_ENABLED } from '@/constants/productStatus'
import { localizedRecordLabel } from '@/utils/productLabels'

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const route = useRoute()
const activeTab = ref('category')
const unitList = ref<ProductUnitVO[]>([])
const { options: productDictOptions } = useProductDict(
  'product_unit_type',
  'product_business_type'
)

const routeTabMap: Record<string, string> = {
  categories: 'category',
  materials: 'material',
  'base-attributes': 'baseAttribute',
  'material-types': 'materialType',
  manufacturers: 'manufacturer',
  units: 'unit',
  'material-attributes': 'materialAttribute'
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
const materialStatusOptions = computed(() => [
  { label: t('productCenter.material.auditStatusDraft'), value: PRODUCT_STATUS_DISABLED },
  { label: t('productCenter.material.auditStatusAudited'), value: PRODUCT_STATUS_ENABLED }
])

function labelOf(row: ProductRecord, codeKey: string, cnKey: string, enKey?: string) {
  return localizedRecordLabel(row, localeStore.language, codeKey, cnKey, enKey)
}

async function loadMaterialOptions(form?: ProductRecord) {
  const response = await productMaterialApi.options?.({
    status: PRODUCT_STATUS_ENABLED,
    materialTypeCode: form?.materialTypeCode as string | undefined,
    pageNum: 1,
    pageSize: 500
  })
  const rows = Array.isArray(response) ? response : response?.data || []
  return rows.map((row) => ({ value: row.materialCode, label: labelOf(row, 'materialCode', 'materialNameCn', 'materialNameEn'), record: row }))
}

async function loadCategoryOptions() {
  const response = await productCategoryApi.options?.({ status: PRODUCT_STATUS_ENABLED, pageNum: 1, pageSize: 500 })
  const rows = Array.isArray(response) ? response : response?.data || []
  return rows.map((row) => ({ value: row.categoryId, label: labelOf(row, 'categoryCode', 'categoryNameCn', 'categoryNameEn'), record: row }))
}

async function loadBaseAttributeOptions(form?: ProductRecord) {
  const attributeGroupCode = String(form?.attributeGroupCode || '')
  if (!attributeGroupCode) return []
  const response = await productBaseAttributeApi.options?.({
    status: PRODUCT_STATUS_ENABLED,
    attributeGroupCode,
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

async function loadMaterialTypeOptions() {
  const response = await productMaterialTypeApi.options?.({ status: PRODUCT_STATUS_ENABLED, pageNum: 1, pageSize: 500 })
  const rows = Array.isArray(response) ? response : response?.data || []
  return rows.map((row) => ({
    value: row.materialTypeCode,
    label: String((localeStore.language === 'zh_CN' ? row.materialTypeNameCn : row.materialTypeNameEn || row.materialTypeNameCn) || row.materialTypeCode || ''),
    record: row
  }))
}

async function loadMaterialTypeGroupOptions() {
  const response = await productMaterialTypeGroupApi.options?.({ status: PRODUCT_STATUS_ENABLED, pageNum: 1, pageSize: 500 })
  const rows = Array.isArray(response) ? response : response?.data || []
  return rows.map((row) => ({ value: row.groupCode, label: labelOf(row, 'groupCode', 'groupNameCn', 'groupNameEn'), record: row }))
}

async function loadManufacturerOptions() {
  const response = await productManufacturerApi.options?.({ status: PRODUCT_STATUS_ENABLED, pageNum: 1, pageSize: 500 })
  const rows = Array.isArray(response) ? response : response?.data || []
  return rows.map((row) => ({
    value: row.manufacturerId,
    label: String(row.manufacturerName || row.manufacturerCode || ''),
    record: row
  }))
}

async function loadUnits() {
  const response = await productUnitApi.options?.({ status: PRODUCT_STATUS_ENABLED })
  unitList.value = Array.isArray(response) ? response : response?.data || []
}

onMounted(() => {
  loadUnits()
})

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
      { prop: 'categoryPath', labelKey: 'productCenter.category.path', form: false, table: false },
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
    singleRowActions: true,
    defaultRecord: { status: PRODUCT_STATUS_DISABLED },
    api: productMaterialApi,
    defaultSort: { prop: 'updateTime', order: 'descending' },
    attachments: { targetType: 'MATERIAL', targetCodeField: 'materialCode', defaultUsageType: 'SPEC' },
    changeLog: { bizModule: 'BASE_INFO', bizType: 'MATERIAL', permission: 'product:base:reference' },
    rowActions: [
      {
        labelKey: 'productCenter.material.audit',
        icon: 'CircleCheck',
        type: 'success',
        permission: 'product:base:edit',
        visible: (row) => row.status !== PRODUCT_STATUS_ENABLED,
        handler: async (row) => {
          await productMaterialApi.changeStatus?.(row.materialId as string | number, PRODUCT_STATUS_ENABLED)
        }
      },
      {
        labelKey: 'productCenter.material.unaudit',
        icon: 'RefreshLeft',
        type: 'warning',
        permission: 'product:base:edit',
        visible: (row) => row.status === PRODUCT_STATUS_ENABLED,
        handler: async (row) => {
          await productMaterialApi.changeStatus?.(row.materialId as string | number, PRODUCT_STATUS_DISABLED)
        }
      }
    ],
    optionLoaders: {
      __unitOptions: async () => unitOptions.value
    },
    fields: [
      { prop: 'materialCode', labelKey: 'productCenter.material.code', search: true, required: true, sortable: true, sectionKey: 'basic', sectionLabelKey: 'productCenter.formSection.basic' },
      { prop: 'materialNameCn', labelKey: 'productCenter.material.name', search: true, required: true, sortable: true, sectionKey: 'basic' },
      { prop: 'materialNameEn', labelKey: 'productCenter.material.nameEn', table: false, sectionKey: 'basic' },
      { prop: 'materialTypeCode', labelKey: 'productCenter.material.type', type: 'remote-select', optionLoader: loadMaterialTypeOptions, fillFields: { materialTypeId: 'materialTypeId', materialTypeNameCn: 'materialTypeNameCn', attributeGroupId: 'attributeGroupId', attributeGroupCode: 'attributeGroupCode', attributeGroupNameCn: 'attributeGroupNameCn', materialType: 'materialTypeCode' }, search: true, required: true, clearFields: ['attributeList'], table: false, sectionKey: 'basic' },
      { prop: 'materialTypeNameCn', labelKey: 'productCenter.material.type', form: false, minWidth: 150 },
      { prop: 'attributeGroupNameCn', labelKey: 'productCenter.material.attributeGroup', form: false, table: false },
      { prop: 'model', labelKey: 'productCenter.material.model', sortable: true, minWidth: 140, sectionKey: 'spec', sectionLabelKey: 'productCenter.formSection.spec' },
      { prop: 'spec', labelKey: 'productCenter.material.spec', search: true, required: true, sortable: true, minWidth: 180, sectionKey: 'spec' },
      { prop: 'specModelText', labelKey: 'productCenter.material.specModelText', form: false, table: false },
      { prop: 'colorName', labelKey: 'productCenter.material.colorName', table: false, sectionKey: 'spec' },
      { prop: 'weightValue', labelKey: 'productCenter.material.weightValue', type: 'number', table: false, sectionKey: 'spec' },
      { prop: 'unitCode', labelKey: 'productCenter.common.unitCode', type: 'select', options: unitOptions.value, required: true, sortable: true, minWidth: 120, sectionKey: 'unitPrice', sectionLabelKey: 'productCenter.formSection.unitPrice' },
      { prop: 'secondaryUnitCode', labelKey: 'productCenter.material.secondaryUnitCode', type: 'select', options: unitOptions.value, minWidth: 120, sectionKey: 'unitPrice' },
      { prop: 'unitPrice', labelKey: 'productCenter.material.unitPrice', type: 'number', minWidth: 120, sectionKey: 'unitPrice' },
      { prop: 'salesPrice', labelKey: 'productCenter.material.salesPrice', type: 'number', minWidth: 120, sectionKey: 'unitPrice' },
      { prop: 'attributeList', labelKey: 'productCenter.material.typeAttributes', type: 'material-attributes', optionLoader: loadBaseAttributeOptions, table: false, formSpan: 2, sectionKey: 'attributes', sectionLabelKey: 'productCenter.formSection.typeAttributes' },
      { prop: 'status', labelKey: 'productCenter.material.auditStatus', type: 'select', options: materialStatusOptions.value, search: true, form: false, minWidth: 120 },
      { prop: 'sortOrder', labelKey: 'productCenter.common.sortOrder', type: 'number', sortable: true, table: false, sectionKey: 'manage', sectionLabelKey: 'productCenter.formSection.manage' },
      { prop: 'remark', labelKey: 'productCenter.common.remark', type: 'textarea', table: false, formSpan: 2, sectionKey: 'manage' },
      { prop: 'manufacturerId', labelKey: 'productCenter.material.manufacturerName', type: 'remote-select', optionLoader: loadManufacturerOptions, fillFields: { manufacturerCode: 'manufacturerCode', manufacturerName: 'manufacturerName' }, table: false, sectionKey: 'manufacturer', sectionLabelKey: 'productCenter.formSection.manufacturer' },
      { prop: 'manufacturerName', labelKey: 'productCenter.material.manufacturerName', form: false, sortable: true, minWidth: 160 },
      { prop: 'auditBy', labelKey: 'productCenter.material.auditBy', form: false, minWidth: 120 },
      { prop: 'auditTime', labelKey: 'productCenter.material.auditDate', type: 'date', form: false, minWidth: 120 },
      { prop: 'createBy', labelKey: 'productCenter.material.createBy', form: false, minWidth: 120 },
      { prop: 'createTime', labelKey: 'productCenter.material.createDate', type: 'date', form: false, minWidth: 120 },
      { prop: 'manufacturerCode', labelKey: 'productCenter.material.manufacturerCode', table: false, form: false, sectionKey: 'manufacturer' },
      { prop: 'manufacturerItemNo', labelKey: 'productCenter.material.manufacturerItemNo', table: false, sectionKey: 'manufacturer' }
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
    singleRowActions: true,
    readonly: true,
    hideReference: true,
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
    key: 'materialType',
    titleKey: 'productCenter.materialType.title',
    descriptionKey: 'productCenter.materialType.description',
    idKey: 'materialTypeId',
    permissions: {
      add: 'product:material-type:add',
      edit: 'product:material-type:edit',
      remove: 'product:material-type:remove',
      reference: 'product:material-type:reference'
    },
    api: productMaterialTypeApi,
    singleRowActions: true,
    defaultSort: { prop: 'sortOrder', order: 'ascending' },
    fields: [
      { prop: 'attributeGroupCode', labelKey: 'productCenter.materialType.group', type: 'remote-select', optionLoader: loadMaterialTypeGroupOptions, fillFields: { attributeGroupId: 'groupId', attributeGroupNameCn: 'groupNameCn' }, search: true, required: true },
      { prop: 'attributeGroupNameCn', labelKey: 'productCenter.materialType.groupName', form: false },
      { prop: 'materialTypeCode', labelKey: 'productCenter.materialType.code', search: true, required: true, sortable: true },
      { prop: 'materialTypeNameCn', labelKey: 'productCenter.materialType.name', search: true, required: true, sortable: true },
      { prop: 'materialTypeNameEn', labelKey: 'productCenter.materialType.nameEn' },
      { prop: 'systemFlag', labelKey: 'productCenter.common.systemFlag', type: 'boolean' },
      { prop: 'editableFlag', labelKey: 'productCenter.common.editableFlag', type: 'boolean' },
      { prop: 'sortOrder', labelKey: 'productCenter.common.sortOrder', type: 'number', sortable: true },
      { prop: 'status', labelKey: 'productCenter.common.status', type: 'status', search: true },
      { prop: 'remark', labelKey: 'productCenter.common.remark', type: 'textarea', table: false, formSpan: 2 }
    ]
  },
  {
    key: 'manufacturer',
    titleKey: 'productCenter.manufacturer.title',
    descriptionKey: 'productCenter.manufacturer.description',
    idKey: 'manufacturerId',
    permissions: {
      add: 'product:manufacturer:add',
      edit: 'product:manufacturer:edit',
      remove: 'product:manufacturer:remove',
      reference: 'product:manufacturer:reference'
    },
    api: productManufacturerApi,
    singleRowActions: true,
    defaultSort: { prop: 'sortOrder', order: 'ascending' },
    fields: [
      { prop: 'manufacturerCode', labelKey: 'productCenter.manufacturer.code', search: true, required: true, sortable: true, width: 140 },
      { prop: 'manufacturerName', labelKey: 'productCenter.manufacturer.name', search: true, required: true, sortable: true, minWidth: 180 },
      { prop: 'manufacturerShortName', labelKey: 'productCenter.manufacturer.shortName', minWidth: 140 },
      { prop: 'manufacturerFlag', labelKey: 'productCenter.manufacturer.manufacturerFlag', type: 'boolean', search: true, width: 120 },
      { prop: 'supplierFlag', labelKey: 'productCenter.manufacturer.supplierFlag', type: 'boolean', search: true, width: 120 },
      { prop: 'contactName', labelKey: 'productCenter.manufacturer.contactName', minWidth: 140 },
      { prop: 'contactPhone', labelKey: 'productCenter.manufacturer.contactPhone', minWidth: 150 },
      { prop: 'address', labelKey: 'productCenter.manufacturer.address', type: 'textarea', minWidth: 220 },
      { prop: 'status', labelKey: 'productCenter.common.status', type: 'status', search: true },
      { prop: 'sortOrder', labelKey: 'productCenter.common.sortOrder', type: 'number', sortable: true },
      { prop: 'remark', labelKey: 'productCenter.common.remark', type: 'textarea', table: false, formSpan: 2 }
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
    singleRowActions: true,
    defaultSort: { prop: 'sortOrder', order: 'ascending' },
    fields: [
      { prop: 'attributeGroupCode', labelKey: 'productCenter.baseAttribute.group', type: 'remote-select', optionLoader: loadMaterialTypeGroupOptions, fillFields: { attributeGroupNameCn: 'groupNameCn' }, search: true, required: true, table: false },
      { prop: 'attributeGroupNameCn', labelKey: 'productCenter.baseAttribute.groupName', form: false },
      { prop: 'attributeCode', labelKey: 'productCenter.baseAttribute.code', search: true, required: true, sortable: true },
      { prop: 'attributeNameCn', labelKey: 'productCenter.baseAttribute.name', search: true, required: true, sortable: true },
      { prop: 'attributeNameEn', labelKey: 'productCenter.baseAttribute.nameEn' },
      { prop: 'valueType', labelKey: 'productCenter.baseAttribute.valueType', type: 'select', options: valueTypeOptions.value, required: true },
      { prop: 'unitCode', labelKey: 'productCenter.baseAttribute.unitCode', type: 'select', options: unitOptions.value, visible: (form) => form.valueType === 'NUMBER' },
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
    singleRowActions: true,
    api: productUnitApi,
    defaultSort: { prop: 'sortOrder', order: 'ascending' },
    fields: [
      { prop: 'unitCode', labelKey: 'productCenter.unit.code', search: true, required: true, sortable: true },
      { prop: 'unitNameCn', labelKey: 'productCenter.unit.name', search: true, required: true, sortable: true },
      { prop: 'unitNameEn', labelKey: 'productCenter.unit.nameEn' },
      { prop: 'unitType', labelKey: 'productCenter.unit.type', type: 'select', options: unitTypeOptions.value, search: true },
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
