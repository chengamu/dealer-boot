<template>
  <div class="app-container product-center-page">
    <product-entity-grid-page :key="activeConfig.key" :config="activeConfig" />
  </div>
</template>

<script setup lang="ts" name="ProductDictPage">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { productDictItemApi, productDictTypeApi } from '@/api/product-capability/product-dict'
import ProductEntityGridPage, { type ProductGridConfig } from '@/pages/product-center/components/ProductEntityGridPage.vue'

const route = useRoute()
const router = useRouter()
const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)

const businessDomainOptions = computed(() => [
  { label: t('productCenter.productDict.domainBase'), value: 'BASE' },
  { label: t('productCenter.productDict.domainEngineering'), value: 'ENGINEERING' },
  { label: t('productCenter.productDict.domainConfig'), value: 'CONFIG' }
])

const dictTypeCode = computed(() => String(route.query.dictTypeCode || ''))
const isItemMode = computed(() => Boolean(dictTypeCode.value))

function openDictItems(row: Record<string, unknown>) {
  const code = String(row.dictTypeCode || '')
  if (!code) return
  return router.push({ path: '/product-master/product-dicts', query: { dictTypeCode: code } }).then(() => undefined)
}

function backToTypes() {
  return router.push({ path: '/product-master/product-dicts' }).then(() => undefined)
}

const typeConfig = computed<ProductGridConfig>(() => ({
  key: 'productDictType',
  titleKey: 'productCenter.productDict.typeTitle',
  descriptionKey: 'productCenter.productDict.typeDescription',
  idKey: 'dictTypeId',
  permissions: { add: 'product:dict:add', edit: 'product:dict:edit', remove: 'product:dict:remove', reference: 'product:dict:reference' },
  api: productDictTypeApi,
  defaultRecord: { businessDomain: 'BASE', status: 'ENABLED', delFlag: '0', systemFlag: false, editableFlag: true, sortOrder: 0 },
  showDetail: true,
  rowActions: [
    {
      labelKey: 'productCenter.productDict.manageItems',
      icon: 'List',
      permission: 'product:dict:list',
      handler: openDictItems
    }
  ],
  fields: [
    { prop: 'dictTypeCode', labelKey: 'productCenter.productDict.typeCode', search: true, required: true, minWidth: 190, sectionKey: 'basic', sectionLabelKey: 'productCenter.formSection.basic' },
    { prop: 'dictTypeNameCn', labelKey: 'productCenter.productDict.typeNameCn', search: true, required: true, minWidth: 180, sectionKey: 'basic' },
    { prop: 'dictTypeNameEn', labelKey: 'productCenter.productDict.typeNameEn', minWidth: 200, sectionKey: 'basic' },
    { prop: 'businessDomain', labelKey: 'productCenter.productDict.businessDomain', type: 'select', options: businessDomainOptions.value, search: true, minWidth: 140, sectionKey: 'basic' },
    { prop: 'systemFlag', labelKey: 'productCenter.productDict.systemFlag', type: 'boolean', table: false, sectionKey: 'control', sectionLabelKey: 'productCenter.formSection.control' },
    { prop: 'editableFlag', labelKey: 'productCenter.productDict.editableFlag', type: 'boolean', table: false, sectionKey: 'control' },
    { prop: 'sortOrder', labelKey: 'productCenter.common.sortOrder', type: 'number', width: 110, sectionKey: 'control' },
    { prop: 'status', labelKey: 'productCenter.common.status', type: 'status', search: true },
    { prop: 'remark', labelKey: 'productCenter.common.remark', type: 'textarea', table: false, formSpan: 2, sectionKey: 'note', sectionLabelKey: 'productCenter.formSection.note' }
  ]
}))

const itemConfig = computed<ProductGridConfig>(() => ({
  key: `productDictItem:${dictTypeCode.value}`,
  titleKey: 'productCenter.productDict.itemTitle',
  descriptionKey: 'productCenter.productDict.itemDescription',
  idKey: 'dictItemId',
  permissions: { add: 'product:dict:add', edit: 'product:dict:edit', remove: 'product:dict:remove', reference: 'product:dict:reference' },
  api: productDictItemApi,
  initialQuery: { dictTypeCode: dictTypeCode.value },
  defaultRecord: { dictTypeCode: dictTypeCode.value, status: 'ENABLED', delFlag: '0', systemFlag: false, editableFlag: true, sortOrder: 0 },
  showDetail: true,
  toolbarActions: [
    {
      labelKey: 'productCenter.productDict.backToTypes',
      icon: 'Back',
      permission: 'product:dict:list',
      handler: backToTypes
    }
  ],
  fields: [
    { prop: 'dictTypeCode', labelKey: 'productCenter.productDict.typeCode', required: true, table: false, sectionKey: 'basic', sectionLabelKey: 'productCenter.formSection.basic' },
    { prop: 'dictItemValue', labelKey: 'productCenter.productDict.itemValue', search: true, required: true, minWidth: 170, sectionKey: 'basic' },
    { prop: 'dictItemLabelCn', labelKey: 'productCenter.productDict.itemLabelCn', search: true, required: true, minWidth: 180, sectionKey: 'basic' },
    { prop: 'dictItemLabelEn', labelKey: 'productCenter.productDict.itemLabelEn', minWidth: 200, sectionKey: 'basic' },
    { prop: 'parentValue', labelKey: 'productCenter.productDict.parentValue', table: false, sectionKey: 'basic' },
    { prop: 'systemFlag', labelKey: 'productCenter.productDict.systemFlag', type: 'boolean', table: false, sectionKey: 'control', sectionLabelKey: 'productCenter.formSection.control' },
    { prop: 'editableFlag', labelKey: 'productCenter.productDict.editableFlag', type: 'boolean', table: false, sectionKey: 'control' },
    { prop: 'sortOrder', labelKey: 'productCenter.common.sortOrder', type: 'number', width: 110, sectionKey: 'control' },
    { prop: 'status', labelKey: 'productCenter.common.status', type: 'status', search: true },
    { prop: 'remark', labelKey: 'productCenter.common.remark', type: 'textarea', table: false, formSpan: 2, sectionKey: 'note', sectionLabelKey: 'productCenter.formSection.note' }
  ]
}))

const activeConfig = computed(() => (isItemMode.value ? itemConfig.value : typeConfig.value))
</script>
