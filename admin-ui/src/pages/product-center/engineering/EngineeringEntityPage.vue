<template>
  <div class="app-container product-center-page">
    <product-entity-grid-page v-if="activeConfig" :config="activeConfig" />
  </div>
</template>

<script setup lang="ts" name="EngineeringEntityPage">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { useProductDict } from '@/hooks/useProductDict'
import { productCategoryApi, productUnitApi } from '@/api/product-capability/base'
import { productComponentApi } from '@/api/product-capability/component'
import { engineeringApi, engineeringPlanApi } from '@/api/product-capability/engineering'
import { productMaterialApi } from '@/api/product-capability/material'
import { fabricSeriesApi } from '@/api/product-capability/fabric'
import { productMediaAssetApi } from '@/api/product-capability/asset'
import type { ProductRecord } from '@/api/product-capability/types'
import ProductEntityGridPage, { type ProductGridConfig } from '@/pages/product-center/components/ProductEntityGridPage.vue'

const route = useRoute()
const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const { options: productDictOptions } = useProductDict(
  'engineering_item_type',
  'engineering_scope_type',
  'engineering_rule_source',
  'engineering_rule_type',
  'engineering_rule_operator',
  'engineering_rule_action',
  'engineering_output_type',
  'engineering_qty_mode',
  'engineering_severity',
  'product_material_type',
  'product_component_type'
)

const yesNoOptions = computed(() => [
  { label: t('common.yes'), value: '1' },
  { label: t('common.no'), value: '0' }
])
const includeOptions = computed(() => [
  { label: t('productCenter.engineering.includeFlag'), value: 'INCLUDE' },
  { label: t('productCenter.engineering.excludeFlag'), value: 'EXCLUDE' }
])
const bizStatusOptions = computed(() => [
  { label: t('productCenter.common.draft'), value: 'DRAFT' },
  { label: t('productCenter.common.published'), value: 'PUBLISHED' }
])
const scopeTypeOptions = computed(() => productDictOptions.value.engineering_scope_type || [])
const qtyModeOptions = computed(() => productDictOptions.value.engineering_qty_mode || [])

function labelOf(row: ProductRecord, codeKey: string, cnKey: string, enKey?: string) {
  const code = String(row[codeKey] || '')
  const name = localeStore.language === 'zh_CN' ? row[cnKey] : row[enKey || cnKey] || row[cnKey]
  return `${code} ${String(name || '')}`.trim()
}

function optionRecord(row: ProductRecord, codeKey: string, cnKey: string, enKey?: string) {
  return {
    ...row,
    __code: row[codeKey],
    __nameCn: row[cnKey],
    __nameEn: row[enKey || cnKey]
  }
}

async function loadPlanOptions() {
  const response = await engineeringPlanApi.options({ status: 'ENABLED', pageNum: 1, pageSize: 500 })
  const rows = Array.isArray(response) ? response : response?.data || []
  return rows.map((row) => ({ value: row.planId, label: labelOf(row, 'planCode', 'planNameCn', 'planNameEn'), record: optionRecord(row, 'planCode', 'planNameCn', 'planNameEn') }))
}

async function loadVersionOptions() {
  const response = await engineeringApi.versions.list({ pageNum: 1, pageSize: 500 })
  const rows = response.rows || []
  return rows.map((row) => ({ value: row.versionId, label: `${row.planCode || ''} / ${row.versionNo || ''} ${row.versionName || ''}`.trim(), record: optionRecord(row, 'versionNo', 'versionName') }))
}

async function loadItemOptions(form?: ProductRecord) {
  const response = await engineeringApi.items.list({ versionId: form?.versionId, pageNum: 1, pageSize: 500 })
  const rows = response.rows || []
  return rows.map((row) => ({ value: row.itemId, label: labelOf(row, 'itemCode', 'itemNameCn', 'itemNameEn'), record: optionRecord(row, 'itemCode', 'itemNameCn', 'itemNameEn') }))
}

async function loadItemCodeOptions(form?: ProductRecord) {
  const response = await engineeringApi.items.list({ versionId: form?.versionId, pageNum: 1, pageSize: 500 })
  const rows = response.rows || []
  return rows.map((row) => ({ value: row.itemCode, label: labelOf(row, 'itemCode', 'itemNameCn', 'itemNameEn'), record: optionRecord(row, 'itemCode', 'itemNameCn', 'itemNameEn') }))
}

async function loadScopeCodeOptions(form?: ProductRecord) {
  const response = await engineeringApi.scopes.list({ versionId: form?.versionId, pageNum: 1, pageSize: 500 })
  const rows = response.rows || []
  return rows.map((row) => ({ value: row.scopeCode, label: labelOf(row, 'scopeCode', 'scopeNameCn', 'scopeNameEn'), record: optionRecord(row, 'scopeCode', 'scopeNameCn', 'scopeNameEn') }))
}

async function loadCategoryOptions() {
  const response = await productCategoryApi.options?.({ status: 'ENABLED', pageNum: 1, pageSize: 500 })
  const rows = Array.isArray(response) ? response : response?.data || []
  return rows.map((row) => ({ value: row.categoryCode, label: labelOf(row, 'categoryCode', 'categoryNameCn', 'categoryNameEn'), record: optionRecord(row, 'categoryCode', 'categoryNameCn', 'categoryNameEn') }))
}

async function loadSeriesOptions() {
  const response = await fabricSeriesApi.options?.({ status: 'ENABLED', pageNum: 1, pageSize: 500 })
  const rows = Array.isArray(response) ? response : response?.data || []
  return rows.map((row) => ({ value: row.seriesCode, label: labelOf(row, 'seriesCode', 'seriesNameCn', 'seriesNameEn'), record: optionRecord(row, 'seriesCode', 'seriesNameCn', 'seriesNameEn') }))
}

async function loadMaterialOptions() {
  const response = await productMaterialApi.options?.({ status: 'ENABLED', pageNum: 1, pageSize: 500 })
  const rows = Array.isArray(response) ? response : response?.data || []
  return rows.map((row) => ({ value: row.materialCode, label: labelOf(row, 'materialCode', 'materialNameCn', 'materialNameEn'), record: optionRecord(row, 'materialCode', 'materialNameCn', 'materialNameEn') }))
}

async function loadComponentOptions() {
  const response = await productComponentApi.options?.({ status: 'ENABLED', pageNum: 1, pageSize: 500 })
  const rows = Array.isArray(response) ? response : response?.data || []
  return rows.map((row) => ({ value: row.componentCode, label: labelOf(row, 'componentCode', 'componentNameCn', 'componentNameEn'), record: optionRecord(row, 'componentCode', 'componentNameCn', 'componentNameEn') }))
}

async function loadMediaOptions() {
  const response = await productMediaAssetApi.options({ status: 'ENABLED', pageNum: 1, pageSize: 500 })
  const rows = Array.isArray(response) ? response : response?.data || []
  return rows.map((row) => ({ value: row.assetCode, label: labelOf(row, 'assetCode', 'assetNameCn', 'assetNameEn'), record: optionRecord(row, 'assetCode', 'assetNameCn', 'assetNameEn') }))
}

async function loadUnitOptions() {
  const response = await productUnitApi.options?.({ status: 'ENABLED', pageNum: 1, pageSize: 500 })
  const rows = Array.isArray(response) ? response : response?.data || []
  return rows.map((row) => ({ value: row.unitCode, label: labelOf(row, 'unitCode', 'unitNameCn', 'unitNameEn'), record: row }))
}

function scopeObjectLoader(form: ProductRecord) {
  const type = String(form.scopeType || form.sourceType || form.outputType || '')
  if (type === 'MATERIAL_TYPE') return Promise.resolve(productMaterialTypeOptions())
  if (type === 'COMPONENT_TYPE') return Promise.resolve(componentTypeOptions())
  if (type === 'FABRIC_SERIES') return loadSeriesOptions()
  if (type === 'MATERIAL_CODE' || type === 'MATERIAL') return loadMaterialOptions()
  if (type === 'COMPONENT_CODE' || type === 'COMPONENT') return loadComponentOptions()
  if (type === 'MEDIA_CODE' || type === 'MEDIA' || type === 'MEDIA_TYPE') return loadMediaOptions()
  return Promise.resolve([])
}

function productMaterialTypeOptions() {
  return productDictOptions.value.product_material_type || []
}

function componentTypeOptions() {
  return productDictOptions.value.product_component_type || []
}

function sharedEngineeringOptionLoaders() {
  return {
    __engineeringItems: loadItemCodeOptions,
    __engineeringScopes: loadScopeCodeOptions,
    __mediaAssets: loadMediaOptions,
    __engineeringItemTypes: async () => productDictOptions.value.engineering_item_type || [],
    __engineeringOperators: async () => productDictOptions.value.engineering_rule_operator || [],
    __engineeringActions: async () => productDictOptions.value.engineering_rule_action || []
  }
}

const configs = computed<Record<string, ProductGridConfig>>(() => ({
  plan: {
    key: 'plan',
    titleKey: 'productCenter.engineering.planTitle',
    descriptionKey: 'productCenter.engineering.planDescription',
    idKey: 'planId',
    permissions: engineeringPermissions(),
    api: engineeringPlanApi,
    fields: [
      { prop: 'planCode', labelKey: 'productCenter.engineering.planCode', search: true, required: true, sectionKey: 'basic', sectionLabelKey: 'productCenter.formSection.basic' },
      { prop: 'planNameCn', labelKey: 'productCenter.engineering.planNameCn', search: true, required: true, sectionKey: 'basic' },
      { prop: 'planNameEn', labelKey: 'productCenter.engineering.planNameEn', table: false, sectionKey: 'basic' },
      { prop: 'categoryCode', labelKey: 'productCenter.category.code', type: 'remote-select', optionLoader: loadCategoryOptions, fillFields: { categoryNameCn: 'categoryNameCn', categoryNameEn: 'categoryNameEn', categoryId: 'categoryId' }, search: true, sectionKey: 'series', sectionLabelKey: 'productCenter.engineering.seriesScope' },
      { prop: 'categoryNameCn', labelKey: 'productCenter.category.name', table: false, form: false },
      { prop: 'seriesCode', labelKey: 'productCenter.fabricSeries.code', type: 'remote-select', optionLoader: loadSeriesOptions, fillFields: { seriesNameCn: 'seriesNameCn', seriesNameEn: 'seriesNameEn', seriesId: 'seriesId' }, search: true, required: true, sectionKey: 'series' },
      { prop: 'seriesNameCn', labelKey: 'productCenter.fabricSeries.name', required: true, form: false },
      { prop: 'currentVersionId', labelKey: 'productCenter.engineering.currentVersion', type: 'remote-select', optionLoader: loadVersionOptions, fillFields: { currentVersionNo: 'versionNo' }, table: false, sectionKey: 'version', sectionLabelKey: 'productCenter.engineering.activeVersion' },
      { prop: 'currentVersionNo', labelKey: 'productCenter.engineering.versionNo' },
      { prop: 'bizStatus', labelKey: 'productCenter.common.bizStatus', type: 'select', options: bizStatusOptions.value, search: true, sectionKey: 'version' },
      { prop: 'status', labelKey: 'productCenter.common.status', type: 'status', search: true },
      { prop: 'sortOrder', labelKey: 'productCenter.common.sortOrder', type: 'number', table: false, sectionKey: 'version' },
      { prop: 'remark', labelKey: 'productCenter.common.remark', type: 'textarea', table: false, formSpan: 2, sectionKey: 'note', sectionLabelKey: 'productCenter.formSection.note' }
    ]
  },
  version: {
    key: 'version',
    titleKey: 'productCenter.engineering.versionTitle',
    descriptionKey: 'productCenter.engineering.versionDescription',
    idKey: 'versionId',
    permissions: engineeringPermissions(),
    api: engineeringApi.versions,
    fields: [
      { prop: 'versionId', labelKey: 'productCenter.engineering.versionId', search: true, form: false, table: false },
      { prop: 'planId', labelKey: 'productCenter.engineering.planTitle', type: 'remote-select', optionLoader: loadPlanOptions, fillFields: { planCode: 'planCode' }, search: true, required: true, sectionKey: 'basic', sectionLabelKey: 'productCenter.formSection.basic' },
      { prop: 'planCode', labelKey: 'productCenter.engineering.planCode', search: true, required: true, form: false },
      { prop: 'versionNo', labelKey: 'productCenter.engineering.versionNo', search: true, required: true, sectionKey: 'basic' },
      { prop: 'versionName', labelKey: 'productCenter.engineering.versionName', required: true, sectionKey: 'basic' },
      { prop: 'bizStatus', labelKey: 'productCenter.common.bizStatus', type: 'select', options: bizStatusOptions.value, search: true },
      { prop: 'status', labelKey: 'productCenter.common.status', type: 'status', search: true },
      { prop: 'ruleSchemaVersion', labelKey: 'productCenter.engineering.ruleSchemaVersion', table: false },
      { prop: 'configJson', labelKey: 'productCenter.engineering.configJson', type: 'textarea', table: false, form: false, formSpan: 2 },
      { prop: 'remark', labelKey: 'productCenter.common.remark', type: 'textarea', table: false, formSpan: 2 }
    ]
  },
  item: {
    key: 'item',
    titleKey: 'productCenter.engineering.itemTitle',
    descriptionKey: 'productCenter.engineering.itemDescription',
    idKey: 'itemId',
    permissions: engineeringPermissions(),
    api: engineeringApi.items,
    fields: [
      { prop: 'versionId', labelKey: 'productCenter.engineering.versionTitle', type: 'remote-select', optionLoader: loadVersionOptions, fillFields: { planId: 'planId', planCode: 'planCode' }, search: true, required: true, sectionKey: 'basic', sectionLabelKey: 'productCenter.formSection.basic' },
      { prop: 'planId', labelKey: 'productCenter.engineering.planId', type: 'number', required: true, form: false, table: false },
      { prop: 'itemCode', labelKey: 'productCenter.engineering.itemCode', search: true, required: true, sectionKey: 'basic' },
      { prop: 'itemNameCn', labelKey: 'productCenter.engineering.itemNameCn', search: true, required: true, sectionKey: 'basic' },
      { prop: 'itemNameEn', labelKey: 'productCenter.engineering.itemNameEn', table: false, sectionKey: 'basic' },
      { prop: 'itemType', labelKey: 'productCenter.engineering.itemType', type: 'select', options: productDictOptions.value.engineering_item_type || [], search: true },
      { prop: 'sourceType', labelKey: 'productCenter.engineering.sourceType', type: 'select', options: productDictOptions.value.engineering_rule_source || [], search: true, clearFields: ['defaultSourceCode', 'defaultSourceNameCn'] },
      { prop: 'requiredFlag', labelKey: 'productCenter.engineering.requiredFlag', type: 'select', options: yesNoOptions.value },
      { prop: 'multiSelectFlag', labelKey: 'productCenter.engineering.multiSelectFlag', type: 'select', options: yesNoOptions.value },
      { prop: 'customerSelectable', labelKey: 'productCenter.engineering.customerSelectable', type: 'select', options: yesNoOptions.value },
      { prop: 'defaultSourceCode', labelKey: 'productCenter.engineering.defaultSourceCode', type: 'remote-select', optionLoader: scopeObjectLoader, clearFields: ['defaultSourceNameCn'] },
      { prop: 'status', labelKey: 'productCenter.common.status', type: 'status', search: true },
      { prop: 'sortOrder', labelKey: 'productCenter.common.sortOrder', type: 'number' },
      { prop: 'extraJson', labelKey: 'productCenter.engineering.extraJson', type: 'textarea', table: false, form: false, formSpan: 2 },
      { prop: 'remark', labelKey: 'productCenter.common.remark', type: 'textarea', table: false, formSpan: 2 }
    ]
  },
  scope: {
    key: 'scope',
    titleKey: 'productCenter.engineering.scopeTitle',
    descriptionKey: 'productCenter.engineering.scopeDescription',
    idKey: 'scopeId',
    permissions: engineeringPermissions(),
    api: engineeringApi.scopes,
    fields: [
      { prop: 'versionId', labelKey: 'productCenter.engineering.versionTitle', type: 'remote-select', optionLoader: loadVersionOptions, search: true, required: true, sectionKey: 'basic', sectionLabelKey: 'productCenter.formSection.basic' },
      { prop: 'itemId', labelKey: 'productCenter.engineering.itemTitle', type: 'remote-select', optionLoader: loadItemOptions, fillFields: { itemCode: 'itemCode' }, required: true, sectionKey: 'basic' },
      { prop: 'itemCode', labelKey: 'productCenter.engineering.itemCode', search: true, required: true, form: false },
      { prop: 'scopeType', labelKey: 'productCenter.engineering.scopeType', type: 'select', options: scopeTypeOptions.value, search: true, required: true, clearFields: ['scopeCode', 'scopeNameCn', 'scopeNameEn'], sectionKey: 'scope', sectionLabelKey: 'productCenter.engineering.scopeObject' },
      { prop: 'scopeCode', labelKey: 'productCenter.engineering.scopeCode', type: 'remote-select', optionLoader: scopeObjectLoader, fillFields: { scopeNameCn: '__nameCn', scopeNameEn: '__nameEn' }, search: true, required: true, sectionKey: 'scope' },
      { prop: 'scopeNameCn', labelKey: 'productCenter.engineering.scopeNameCn', required: true },
      { prop: 'scopeNameEn', labelKey: 'productCenter.engineering.scopeNameEn' },
      { prop: 'includeFlag', labelKey: 'productCenter.engineering.includeFlag', type: 'select', options: includeOptions.value },
      { prop: 'conditionJson', labelKey: 'productCenter.engineering.conditionJson', type: 'rule-condition', table: false, formSpan: 2 },
      { prop: 'status', labelKey: 'productCenter.common.status', type: 'status', search: true },
      { prop: 'sortOrder', labelKey: 'productCenter.common.sortOrder', type: 'number' },
      { prop: 'remark', labelKey: 'productCenter.common.remark', type: 'textarea', table: false, formSpan: 2 }
    ]
  },
  rule: {
    key: 'rule',
    titleKey: 'productCenter.engineering.ruleTitle',
    descriptionKey: 'productCenter.engineering.ruleDescription',
    idKey: 'ruleId',
    permissions: engineeringPermissions(),
    api: engineeringApi.rules,
    optionLoaders: sharedEngineeringOptionLoaders(),
    fields: [
      { prop: 'versionId', labelKey: 'productCenter.engineering.versionTitle', type: 'remote-select', optionLoader: loadVersionOptions, search: true, required: true, sectionKey: 'basic', sectionLabelKey: 'productCenter.formSection.basic' },
      { prop: 'ruleCode', labelKey: 'productCenter.engineering.ruleCode', search: true, required: true },
      { prop: 'ruleNameCn', labelKey: 'productCenter.engineering.ruleNameCn', search: true, required: true },
      { prop: 'ruleNameEn', labelKey: 'productCenter.engineering.ruleNameEn', table: false },
      { prop: 'ruleType', labelKey: 'productCenter.engineering.ruleType', type: 'select', options: productDictOptions.value.engineering_rule_type || [], search: true },
      { prop: 'severity', labelKey: 'productCenter.engineering.severity', type: 'select', options: productDictOptions.value.engineering_severity || [], search: true },
      { prop: 'conditionJson', labelKey: 'productCenter.engineering.conditionEditor', type: 'rule-condition', table: false, formSpan: 2, required: true, sectionKey: 'condition', sectionLabelKey: 'productCenter.engineering.conditionSection' },
      { prop: 'actionJson', labelKey: 'productCenter.engineering.actionEditor', type: 'rule-action', table: false, formSpan: 2, required: true, sectionKey: 'action', sectionLabelKey: 'productCenter.engineering.actionSection' },
      { prop: 'messageCn', labelKey: 'productCenter.engineering.messageCn', formSpan: 2 },
      { prop: 'messageEn', labelKey: 'productCenter.engineering.messageEn', formSpan: 2 },
      { prop: 'status', labelKey: 'productCenter.common.status', type: 'status', search: true },
      { prop: 'sortOrder', labelKey: 'productCenter.common.sortOrder', type: 'number' },
      { prop: 'remark', labelKey: 'productCenter.common.remark', type: 'textarea', table: false, formSpan: 2 }
    ]
  },
  'output-rule': {
    key: 'output-rule',
    titleKey: 'productCenter.engineering.outputRuleTitle',
    descriptionKey: 'productCenter.engineering.outputRuleDescription',
    idKey: 'outputRuleId',
    permissions: engineeringPermissions(),
    api: engineeringApi.outputRules,
    optionLoaders: sharedEngineeringOptionLoaders(),
    fields: [
      { prop: 'versionId', labelKey: 'productCenter.engineering.versionTitle', type: 'remote-select', optionLoader: loadVersionOptions, search: true, required: true, sectionKey: 'basic', sectionLabelKey: 'productCenter.formSection.basic' },
      { prop: 'ruleCode', labelKey: 'productCenter.engineering.ruleCode', search: true, required: true },
      { prop: 'ruleNameCn', labelKey: 'productCenter.engineering.ruleNameCn', required: true },
      { prop: 'ruleNameEn', labelKey: 'productCenter.engineering.ruleNameEn', table: false },
      { prop: 'conditionJson', labelKey: 'productCenter.engineering.conditionEditor', type: 'rule-condition', table: false, formSpan: 2, sectionKey: 'condition', sectionLabelKey: 'productCenter.engineering.conditionSection' },
      { prop: 'outputType', labelKey: 'productCenter.engineering.outputType', type: 'select', options: productDictOptions.value.engineering_output_type || [], search: true, required: true, clearFields: ['outputCode', 'outputNameCn', 'outputNameEn', 'unitCode'] },
      { prop: 'outputCode', labelKey: 'productCenter.engineering.outputCode', type: 'remote-select', optionLoader: scopeObjectLoader, fillFields: { outputNameCn: '__nameCn', outputNameEn: '__nameEn', unitCode: 'unitCode' }, search: true, required: true },
      { prop: 'outputNameCn', labelKey: 'productCenter.engineering.outputNameCn', required: true },
      { prop: 'outputNameEn', labelKey: 'productCenter.engineering.outputNameEn', table: false },
      { prop: 'defaultQty', labelKey: 'productCenter.engineering.defaultQty', type: 'number' },
      { prop: 'unitCode', labelKey: 'productCenter.common.unitCode', type: 'remote-select', optionLoader: loadUnitOptions },
      { prop: 'reasonCn', labelKey: 'productCenter.engineering.reasonCn', formSpan: 2 },
      { prop: 'reasonEn', labelKey: 'productCenter.engineering.reasonEn', formSpan: 2 },
      { prop: 'status', labelKey: 'productCenter.common.status', type: 'status', search: true },
      { prop: 'sortOrder', labelKey: 'productCenter.common.sortOrder', type: 'number' },
      { prop: 'remark', labelKey: 'productCenter.common.remark', type: 'textarea', table: false, formSpan: 2 }
    ]
  },
  'standard-sku': {
    key: 'standard-sku',
    titleKey: 'productCenter.engineering.standardSkuTitle',
    descriptionKey: 'productCenter.engineering.standardSkuDescription',
    idKey: 'skuEngineeringId',
    permissions: engineeringPermissions(),
    api: engineeringApi.standardSkus,
    optionLoaders: sharedEngineeringOptionLoaders(),
    fields: [
      { prop: 'versionId', labelKey: 'productCenter.engineering.versionTitle', type: 'remote-select', optionLoader: loadVersionOptions, search: true, required: true, sectionKey: 'basic', sectionLabelKey: 'productCenter.formSection.basic' },
      { prop: 'standardSkuCode', labelKey: 'productCenter.engineering.standardSkuCode', search: true, required: true },
      { prop: 'standardSkuNameCn', labelKey: 'productCenter.engineering.standardSkuNameCn', search: true, required: true },
      { prop: 'standardSkuNameEn', labelKey: 'productCenter.engineering.standardSkuNameEn', table: false },
      { prop: 'fixedItemsJson', labelKey: 'productCenter.engineering.fixedItemsEditor', type: 'fixed-items', table: false, formSpan: 2, required: true, sectionKey: 'fixed', sectionLabelKey: 'productCenter.engineering.fixedItemsSection' },
      { prop: 'status', labelKey: 'productCenter.common.status', type: 'status', search: true },
      { prop: 'remark', labelKey: 'productCenter.common.remark', type: 'textarea', table: false, formSpan: 2 }
    ]
  },
  'check-case': {
    key: 'check-case',
    titleKey: 'productCenter.engineering.checkCaseTitle',
    descriptionKey: 'productCenter.engineering.checkCaseDescription',
    idKey: 'checkCaseId',
    permissions: engineeringPermissions(),
    api: engineeringApi.checkCases,
    optionLoaders: sharedEngineeringOptionLoaders(),
    fields: [
      { prop: 'versionId', labelKey: 'productCenter.engineering.versionTitle', type: 'remote-select', optionLoader: loadVersionOptions, search: true, required: true, sectionKey: 'basic', sectionLabelKey: 'productCenter.formSection.basic' },
      { prop: 'caseCode', labelKey: 'productCenter.engineering.caseCode', search: true, required: true },
      { prop: 'caseNameCn', labelKey: 'productCenter.engineering.caseNameCn', search: true, required: true },
      { prop: 'caseNameEn', labelKey: 'productCenter.engineering.caseNameEn', table: false },
      { prop: 'inputJson', labelKey: 'productCenter.engineering.inputEditor', type: 'case-input', table: false, formSpan: 2, required: true },
      { prop: 'expectedJson', labelKey: 'productCenter.engineering.expectedEditor', type: 'expected-result', table: false, formSpan: 2 },
      { prop: 'status', labelKey: 'productCenter.common.status', type: 'status', search: true },
      { prop: 'sortOrder', labelKey: 'productCenter.common.sortOrder', type: 'number' },
      { prop: 'remark', labelKey: 'productCenter.common.remark', type: 'textarea', table: false, formSpan: 2 }
    ]
  }
}))

const routeKey = computed(() => String(route.path.split('/').pop() || 'plan'))
const activeConfig = computed(() => {
  const config = configs.value[routeKey.value]
  if (!config) return config
  const versionId = route.query.versionId
  return typeof versionId === 'string' && ['item', 'scope', 'rule', 'output-rule', 'standard-sku', 'check-case'].includes(routeKey.value)
    ? { ...config, initialQuery: { versionId } }
    : config
})

function engineeringPermissions() {
  return {
    add: 'product:engineering:add',
    edit: 'product:engineering:edit',
    remove: 'product:engineering:remove',
    reference: 'product:engineering:list'
  }
}
</script>
