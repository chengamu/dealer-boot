<template>
  <div class="app-container product-formula-page">
    <product-entity-grid-page :config="formulaConfig" />
  </div>
</template>

<script setup lang="ts" name="ProductFormulaPage">
import { computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { productCategoryApi } from '@/api/product-capability/base'
import { getProductDictItems } from '@/api/product-capability/product-dict'
import { productFormulaApi } from '@/api/product-formula/formula'
import type { ProductDictOption, ProductRecord } from '@/api/product-capability/types'
import ProductEntityGridPage, { type ProductGridConfig } from '@/pages/product-center/components/ProductEntityGridPage.vue'

const localeStore = useLocaleStore()
const router = useRouter()
const t = (key: string) => getMessage(key, localeStore.language)

const formulaStatusOptions = computed(() => [
  { label: t('productCenter.formula.status.draft'), value: 'DRAFT' },
  { label: t('productCenter.formula.status.pendingReview'), value: 'PENDING_REVIEW' },
  { label: t('productCenter.formula.status.rejected'), value: 'REJECTED' },
  { label: t('productCenter.formula.status.effective'), value: 'EFFECTIVE' },
  { label: t('productCenter.formula.status.stopped'), value: 'STOPPED' }
])

const validationStatusOptions = computed(() => [
  { label: t('productCenter.formula.validation.notValidated'), value: 'NOT_VALIDATED' },
  { label: t('productCenter.formula.validation.pass'), value: 'PASS' },
  { label: t('productCenter.formula.validation.fail'), value: 'FAIL' }
])

function labelOf(row: ProductRecord, codeKey: string, cnKey: string, enKey?: string) {
  const code = String(row[codeKey] || '')
  const name = localeStore.language === 'zh_CN' ? row[cnKey] : row[enKey || cnKey] || row[cnKey]
  return `${code} ${String(name || '')}`.trim()
}

async function loadCategoryOptions() {
  const response = await productCategoryApi.options?.({ status: 'ENABLED', pageNum: 1, pageSize: 500 })
  const rows = Array.isArray(response) ? response : response?.data || []
  return rows.map((row) => ({ value: row.categoryId, label: labelOf(row, 'categoryCode', 'categoryNameCn', 'categoryNameEn'), record: row }))
}

async function loadProductTypeOptions() {
  const response = await getProductDictItems('product_type')
  const rows = response.data || []
  return rows.map((row: ProductDictOption) => ({
    value: row.value,
    label: `${row.value} ${localeStore.language === 'zh_CN' ? row.labelCn || row.label : row.labelEn || row.labelCn || row.label}`.trim(),
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

async function rejectFormula(row: ProductRecord) {
  const id = row.formulaId as string | number | undefined
  if (!id) return
  const result = await ElMessageBox.prompt(t('productCenter.formula.rejectPrompt'), t('productCenter.formula.actions.reject'), {
    confirmButtonText: t('common.confirm'),
    cancelButtonText: t('common.cancel'),
    inputType: 'textarea',
    inputValidator: (value) => Boolean(value && value.trim()),
    inputErrorMessage: t('productCenter.formula.rejectReasonRequired')
  })
  await productFormulaApi.reject(id, result.value.trim())
  ElMessage.success(t('common.success'))
}

async function openSetup(row: ProductRecord) {
  const id = row.formulaId as string | number | undefined
  if (!id) return
  await router.push(`/product-formula/formulas/${id}/setup`)
}

const formulaConfig = computed<ProductGridConfig>(() => ({
  key: 'formula',
  titleKey: 'productCenter.formula.title',
  descriptionKey: 'productCenter.formula.description',
  idKey: 'formulaId',
  singleRowActions: true,
  showDetail: true,
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
    latestValidationStatus: 'NOT_VALIDATED',
    status: 'DRAFT'
  },
  submitFields: [
    'formulaId',
    'formulaCode',
    'formulaName',
    'categoryId',
    'categoryCode',
    'categoryNameCn',
    'productTypeCode',
    'productTypeNameCn',
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
    { prop: 'maxWidthInch', labelKey: 'productCenter.formula.maxWidthInch', type: 'number', required: true, minWidth: 160, sortable: true, precision: 2, step: 0.01 },
    { prop: 'maxHeightInch', labelKey: 'productCenter.formula.maxHeightInch', type: 'number', required: true, minWidth: 160, sortable: true, precision: 2, step: 0.01 },
    { prop: 'sizeSummary', labelKey: 'productCenter.formula.sizeSummary', form: false, minWidth: 140 },
    { prop: 'materialLineCount', labelKey: 'productCenter.formula.materialLineCount', type: 'number', form: false, minWidth: 110 },
    { prop: 'latestValidationStatus', labelKey: 'productCenter.formula.validationStatus', type: 'select', options: validationStatusOptions.value, form: false, minWidth: 120 },
    { prop: 'currentVersionLabel', labelKey: 'productCenter.formula.currentVersion', form: false, minWidth: 120 },
    { prop: 'status', labelKey: 'productCenter.formula.status', type: 'select', options: formulaStatusOptions.value, search: true, form: false, minWidth: 120 },
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
      labelKey: 'productCenter.formula.actions.setup',
      icon: 'Setting',
      type: 'primary',
      permission: 'product:formula:setup',
      visible: (row) => row.status === 'DRAFT' || row.status === 'REJECTED',
      handler: openSetup
    },
    {
      labelKey: 'productCenter.formula.actions.submitReview',
      icon: 'Promotion',
      type: 'primary',
      permission: 'product:formula:submitReview',
      visible: (row) => row.status === 'DRAFT' || row.status === 'REJECTED',
      handler: (row) => runWithConfirm('productCenter.formula.confirm.submitReview', () => productFormulaApi.submitReview(row.formulaId as string | number))
    },
    {
      labelKey: 'productCenter.formula.actions.approve',
      icon: 'CircleCheck',
      type: 'success',
      permission: 'product:formula:approve',
      visible: (row) => row.status === 'PENDING_REVIEW',
      handler: (row) => runWithConfirm('productCenter.formula.confirm.approve', () => productFormulaApi.approve(row.formulaId as string | number))
    },
    {
      labelKey: 'productCenter.formula.actions.reject',
      icon: 'CircleClose',
      type: 'warning',
      permission: 'product:formula:reject',
      visible: (row) => row.status === 'PENDING_REVIEW',
      handler: rejectFormula
    },
    {
      labelKey: 'productCenter.formula.actions.stop',
      icon: 'VideoPause',
      type: 'warning',
      permission: 'product:formula:stop',
      visible: (row) => row.status === 'EFFECTIVE',
      handler: (row) => runWithConfirm('productCenter.formula.confirm.stop', () => productFormulaApi.stop(row.formulaId as string | number))
    }
  ]
}))
</script>
