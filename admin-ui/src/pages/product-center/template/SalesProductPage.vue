<template>
  <div class="app-container product-center-page">
    <product-entity-grid-page :config="salesProductConfig" />
  </div>
</template>

<script setup lang="ts" name="SalesProductPage">
import { computed, onMounted, ref } from 'vue'
import { productCategoryApi, productUnitApi } from '@/api/product-capability/base'
import { configTemplateVersionApi, salesProductApi } from '@/api/product-capability/config'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { useProductDict } from '@/hooks/useProductDict'
import ProductEntityGridPage, { type ProductGridConfig } from '@/pages/product-center/components/ProductEntityGridPage.vue'
import type { ConfigTemplateVersionVO, ProductCategoryVO, ProductOption, ProductRecord, ProductUnitVO } from '@/api/product-capability/types'

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const { options: productDictOptions } = useProductDict('product_business_type')

const categories = ref<ProductCategoryVO[]>([])
const templateVersions = ref<ConfigTemplateVersionVO[]>([])
const dimensionUnits = ref<ProductUnitVO[]>([])

const salesProductPermissions = {
  add: 'product:sales-product:add',
  edit: 'product:sales-product:edit',
  remove: 'product:sales-product:remove',
  reference: 'product:sales-product:reference'
}

const productTypeOptions = computed<ProductOption[]>(() => productDictOptions.value.product_business_type || [])
const salesModeOptions = computed<ProductOption[]>(() => [
  { label: t('productCenter.salesProduct.salesModeConfigurable'), value: 'CONFIGURABLE' },
  { label: t('productCenter.salesProduct.salesModeStandard'), value: 'STANDARD' }
])
const bizStatusOptions = computed<ProductOption[]>(() => [
  { label: t('productCenter.bizStatus.draft'), value: 'DRAFT' },
  { label: t('productCenter.bizStatus.ready'), value: 'READY' },
  { label: t('productCenter.bizStatus.published'), value: 'PUBLISHED' },
  { label: t('productCenter.bizStatus.archived'), value: 'ARCHIVED' }
])
const categoryOptions = computed<ProductOption[]>(() =>
  categories.value.map((item) => ({
    label: `${item.categoryCode || ''} ${item.categoryNameCn || item.categoryNameEn || ''}`.trim(),
    value: item.categoryId || 0
  }))
)
const templateVersionOptions = computed<ProductOption[]>(() =>
  templateVersions.value.map((item) => ({
    label: `${item.templateCode || ''} / ${item.versionNo || ''}`.trim(),
    value: item.templateVersionId || 0
  }))
)
const dimensionUnitOptions = computed<ProductOption[]>(() =>
  dimensionUnits.value.map((item) => ({
    label: `${item.unitCode || ''} ${item.unitNameCn || item.unitNameEn || ''}`.trim(),
    value: item.unitCode || ''
  }))
)

function applyCategory(value: unknown, form: ProductRecord) {
  const category = categories.value.find((item) => item.categoryId === value)
  form.categoryCode = category?.categoryCode
  form.categoryNameCn = category?.categoryNameCn
  form.categoryNameEn = category?.categoryNameEn
}

function applyTemplateVersion(value: unknown, form: ProductRecord) {
  const version = templateVersions.value.find((item) => item.templateVersionId === value)
  form.templateId = version?.templateId
  form.templateCode = version?.templateCode
  form.templateVersionNo = version?.versionNo
}

const salesProductConfig = computed<ProductGridConfig>(() => ({
  key: 'salesProduct',
  titleKey: 'productCenter.salesProduct.title',
  descriptionKey: 'productCenter.salesProduct.description',
  idKey: 'salesProductId',
  permissions: salesProductPermissions,
  api: salesProductApi,
  defaultRecord: {
    salesMode: 'CONFIGURABLE',
    dimensionUnit: 'CM',
    bizStatus: 'DRAFT'
  },
  fields: [
    { prop: 'salesProductCode', labelKey: 'productCenter.salesProduct.code', search: true, required: true },
    { prop: 'salesProductNameCn', labelKey: 'productCenter.salesProduct.nameCn', search: true, required: true },
    { prop: 'salesProductNameEn', labelKey: 'productCenter.salesProduct.nameEn' },
    { prop: 'categoryId', labelKey: 'productCenter.salesProduct.category', type: 'select', search: true, table: false, required: true, options: categoryOptions.value, onChange: applyCategory },
    { prop: 'categoryCode', labelKey: 'productCenter.salesProduct.categoryCode', form: false },
    { prop: 'categoryNameCn', labelKey: 'productCenter.salesProduct.categoryNameCn', form: false },
    { prop: 'categoryNameEn', labelKey: 'productCenter.salesProduct.categoryNameEn', form: false, table: false },
    { prop: 'productType', labelKey: 'productCenter.salesProduct.productType', type: 'select', search: true, required: true, options: productTypeOptions.value },
    { prop: 'salesMode', labelKey: 'productCenter.salesProduct.salesMode', type: 'select', search: true, required: true, options: salesModeOptions.value },
    { prop: 'templateVersionId', labelKey: 'productCenter.template.currentTemplateVersion', type: 'select', table: false, options: templateVersionOptions.value, onChange: applyTemplateVersion },
    { prop: 'templateCode', labelKey: 'productCenter.template.code', form: false },
    { prop: 'templateVersionNo', labelKey: 'productCenter.template.versionNo', form: false },
    { prop: 'defaultWidth', labelKey: 'productCenter.salesProduct.defaultWidth', type: 'number' },
    { prop: 'defaultHeight', labelKey: 'productCenter.salesProduct.defaultHeight', type: 'number' },
    { prop: 'dimensionUnit', labelKey: 'productCenter.salesProduct.dimensionUnit', type: 'select', options: dimensionUnitOptions.value },
    { prop: 'bizStatus', labelKey: 'productCenter.template.bizStatus', type: 'select', options: bizStatusOptions.value },
    { prop: 'status', labelKey: 'productCenter.common.status', type: 'status', search: true },
    { prop: 'sortOrder', labelKey: 'productCenter.common.sortOrder', type: 'number' },
    { prop: 'remark', labelKey: 'productCenter.common.remark', type: 'textarea', table: false, formSpan: 2 }
  ]
}))

onMounted(async () => {
  const [categoryResponse, unitResponse, versionResponse] = await Promise.all([
    productCategoryApi.options?.({ pageNum: 1, pageSize: 200, status: 'ENABLED' }),
    productUnitApi.options?.({ pageNum: 1, pageSize: 100, unitType: 'LENGTH', status: 'ENABLED' }),
    configTemplateVersionApi.options?.({ pageNum: 1, pageSize: 200 })
  ])
  categories.value = Array.isArray(categoryResponse) ? categoryResponse : categoryResponse?.data || []
  dimensionUnits.value = Array.isArray(unitResponse) ? unitResponse : unitResponse?.data || []
  templateVersions.value = Array.isArray(versionResponse) ? versionResponse : versionResponse?.data || []
})
</script>
