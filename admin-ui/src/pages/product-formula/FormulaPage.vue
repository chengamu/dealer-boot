<template>
  <div class="app-container product-formula-page">
    <product-entity-grid-page :config="formulaConfig" />
  </div>
</template>

<script setup lang="ts" name="ProductFormulaPage">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { productCategoryApi } from '@/api/product-capability/base'
import { getProductDictItems } from '@/api/product-capability/product-dict'
import { productFormulaApi } from '@/api/product-formula/formula'
import type { ProductDictOption } from '@/api/product-capability/types'
import ProductEntityGridPage from '@/pages/product-center/components/ProductEntityGridPage.vue'
import type { ProductGridConfig } from '@/pages/product-center/components/productGridTypes'
import {
  FORMULA_STATUS,
  FORMULA_VALIDATION_STATUS,
  PRODUCT_STATUS_ENABLED,
  formulaStatusOptions,
  formulaValidationStatusOptions
} from '@/constants/productStatus'

const localeStore = useLocaleStore()
const router = useRouter()
const t = (key: string) => getMessage(key, localeStore.language)

const formulaStatusOptionList = computed(() => formulaStatusOptions(t))
const validationStatusOptionList = computed(() => formulaValidationStatusOptions(t))
const simulationFormulaStatuses = new Set<string>([FORMULA_STATUS.DRAFT, FORMULA_STATUS.REJECTED, FORMULA_STATUS.EFFECTIVE])

async function loadCategoryOptions() {
  const response = await productCategoryApi.options?.({ status: PRODUCT_STATUS_ENABLED, pageNum: 1, pageSize: 500 })
  const rows = Array.isArray(response) ? response : response?.data || []
  return rows.map((row) => ({ value: row.categoryId, label: String(row.categoryNameCn || row.categoryCode || ''), record: row }))
}

async function loadProductTypeOptions() {
  const response = await getProductDictItems('product_type')
  const rows = response.data || []
  return rows.map((row: ProductDictOption) => ({
    value: row.value,
    label: String(row.labelCn || row.label || row.value || ''),
    record: {
      ...row,
      productTypeNameCn: row.labelCn || row.label
    }
  }))
}

async function runWithConfirm(messageKey: string, action: () => Promise<unknown>) {
  await ElMessageBox.confirm(t(messageKey), t('common.prompt'), { type: 'warning' })
  await action()
  ElMessage.success(t('common.success'))
}

const formulaConfig = computed<ProductGridConfig>(() => ({
  key: 'formula',
  titleKey: 'productCenter.formula.title',
  descriptionKey: 'productCenter.formula.description',
  idKey: 'formulaId',
  singleRowActions: true,
  showDetail: true,
  hideReference: true,
  permissions: {
    add: 'product:formula:add',
    edit: 'product:formula:edit',
    remove: 'product:formula:remove',
    reference: 'product:formula:reference'
  },
  api: productFormulaApi,
  changeLog: { bizModule: 'FORMULA', bizType: 'FORMULA', permission: 'product:formula:reference' },
  defaultSort: { prop: 'updateTime', order: 'descending' },
  defaultRecord: {
    configuredFlag: false,
    materialLineCount: 0,
    minWidthInch: 0,
    minHeightInch: 0,
    latestValidationStatus: FORMULA_VALIDATION_STATUS.NOT_VALIDATED,
    status: FORMULA_STATUS.DRAFT
  },
  rowTone: (row) => row.status === FORMULA_STATUS.REJECTED ? 'danger' : undefined,
  submitFields: [
    'formulaId',
    'formulaCode',
    'formulaName',
    'categoryId',
    'categoryCode',
    'categoryNameCn',
    'productTypeCode',
    'productTypeNameCn',
    'minWidthInch',
    'minHeightInch',
    'maxWidthInch',
    'maxHeightInch',
    'sortOrder',
    'remark'
  ],
  fields: [
    { prop: 'formulaCode', labelKey: 'productCenter.formula.code', search: true, required: true, minWidth: 150, sortable: true },
    { prop: 'formulaName', labelKey: 'productCenter.formula.name', search: true, required: true, minWidth: 220, sortable: true },
    { prop: 'categoryId', labelKey: 'productCenter.formula.category', type: 'remote-select', optionLoader: loadCategoryOptions, fillFields: { categoryCode: 'categoryCode', categoryNameCn: 'categoryNameCn' }, search: true, required: true, table: false },
    { prop: 'categoryNameCn', labelKey: 'productCenter.formula.category', form: false, minWidth: 140 },
    { prop: 'productTypeCode', labelKey: 'productCenter.formula.productType', type: 'remote-select', optionLoader: loadProductTypeOptions, fillFields: { productTypeNameCn: 'productTypeNameCn' }, search: true, required: true, table: false },
    { prop: 'productTypeNameCn', labelKey: 'productCenter.formula.productType', form: false, minWidth: 120 },
    { prop: 'minWidthInch', labelKey: 'productCenter.formula.minWidthInch', type: 'number', required: true, minWidth: 160, sortable: true, precision: 2, step: 0.01 },
    { prop: 'minHeightInch', labelKey: 'productCenter.formula.minHeightInch', type: 'number', required: true, minWidth: 160, sortable: true, precision: 2, step: 0.01 },
    { prop: 'maxWidthInch', labelKey: 'productCenter.formula.maxWidthInch', type: 'number', required: true, minWidth: 160, sortable: true, precision: 2, step: 0.01 },
    { prop: 'maxHeightInch', labelKey: 'productCenter.formula.maxHeightInch', type: 'number', required: true, minWidth: 160, sortable: true, precision: 2, step: 0.01 },
    { prop: 'sizeSummary', labelKey: 'productCenter.formula.sizeSummary', form: false, minWidth: 140 },
    { prop: 'materialLineCount', labelKey: 'productCenter.formula.materialLineCount', type: 'number', form: false, minWidth: 110 },
    { prop: 'latestValidationStatus', labelKey: 'productCenter.formula.validationStatus', type: 'select', options: validationStatusOptionList.value, form: false, minWidth: 120 },
    { prop: 'currentVersionLabel', labelKey: 'productCenter.formula.currentVersion', form: false, minWidth: 120 },
    { prop: 'status', labelKey: 'productCenter.formula.status', type: 'select', options: formulaStatusOptionList.value, search: true, form: false, minWidth: 120 },
    { prop: 'auditBy', labelKey: 'productCenter.formula.auditBy', form: false, minWidth: 120 },
    { prop: 'auditTime', labelKey: 'productCenter.formula.auditTime', type: 'datetime', form: false, minWidth: 160, sortable: true },
    { prop: 'updateTime', labelKey: 'productCenter.formula.updateTime', type: 'datetime', form: false, minWidth: 160, sortable: true },
    { prop: 'updateBy', labelKey: 'productCenter.formula.updateBy', form: false, minWidth: 120 },
    { prop: 'rejectReason', labelKey: 'productCenter.formula.rejectReason', type: 'textarea', table: false, form: false },
    { prop: 'sortOrder', labelKey: 'productCenter.common.sortOrder', type: 'number', table: false },
    { prop: 'remark', labelKey: 'productCenter.common.remark', type: 'textarea', table: false, formSpan: 2 }
  ],
  rowActions: [
    {
      labelKey: 'productCenter.formula.actions.workbench',
      icon: 'Operation',
      type: 'primary',
      permission: 'product:formula:setup',
      primary: true,
      handler: (row) => { void router.push({ name: 'ProductFormulaWorkbench', query: { formulaId: String(row.formulaId || '') } }) }
    },
    {
      labelKey: 'productCenter.formula.actions.simulation',
      icon: 'DataAnalysis',
      type: 'primary',
      permission: 'product:formula:setup',
      visible: (row) => simulationFormulaStatuses.has(String(row.status || '')),
      handler: (row) => { void router.push({ name: 'ProductFormulaSimulation', query: { formulaId: String(row.formulaId || '') } }) }
    },
    {
      labelKey: 'productCenter.formula.actions.submitReview',
      icon: 'Promotion',
      type: 'primary',
      permission: 'product:formula:submitReview',
      visible: (row) => row.status === FORMULA_STATUS.DRAFT || row.status === FORMULA_STATUS.REJECTED,
      handler: (row) => runWithConfirm('productCenter.formula.confirm.submitReview', () => productFormulaApi.submitReview(row.formulaId as string | number))
    },
    {
      labelKey: 'productCenter.formula.actions.stop',
      icon: 'VideoPause',
      type: 'warning',
      permission: 'product:formula:stop',
      visible: (row) => row.status === FORMULA_STATUS.EFFECTIVE,
      handler: (row) => runWithConfirm('productCenter.formula.confirm.stop', () => productFormulaApi.stop(row.formulaId as string | number))
    }
  ]
}))
</script>
