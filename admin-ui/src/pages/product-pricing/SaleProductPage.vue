<template>
  <div class="app-container sale-product-page">
    <product-entity-grid-page :config="saleProductConfig" />
  </div>
</template>

<script setup lang="ts" name="SaleProductPage">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import ProductEntityGridPage from '@/pages/product-center/components/ProductEntityGridPage.vue'
import type { ProductGridConfig } from '@/pages/product-center/components/productGridTypes'
import type { ProductRecord } from '@/api/product-capability/types'
import { formatInchRange } from '@/utils/businessNumber'
import { productFormulaApi } from '@/api/product-formula/formula'
import { saleProductApi } from '@/api/product-pricing/pricing'
import { FORMULA_STATUS, PRODUCT_STATUS_DISABLED } from '@/constants/productStatus'
import {
  enabledStatusOptions,
  priceStatusOptions
} from './utils/pricingDisplay'

const router = useRouter()
const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)

function sizeSummary(_value: unknown, row: ProductRecord) {
  return formatInchRange(row.minWidthInch as number | string, row.maxWidthInch as number | string,
    row.minHeightInch as number | string, row.maxHeightInch as number | string)
}

const statusOptions = computed(() => enabledStatusOptions(t))
const priceOptions = computed(() => priceStatusOptions(t))
const saleProductGridApi = saleProductApi as unknown as ProductGridConfig['api']

async function loadFormulaOptions() {
  const response = await productFormulaApi.list({ status: FORMULA_STATUS.EFFECTIVE, pageNum: 1, pageSize: 500 })
  return (response.rows || []).map((row) => ({
    value: row.formulaId,
    label: compactLabel(row.formulaCode, row.formulaName, row.currentVersionLabel),
    record: row
  }))
}

function compactLabel(...parts: Array<string | number | undefined>) {
  return parts.filter((item) => item !== undefined && item !== '').join(' ')
}

function fillFormulaDefaults(_value: unknown, form: ProductRecord) {
  if (!form.saleProductName && form.formulaName) form.saleProductName = form.formulaName
}

const saleProductConfig = computed<ProductGridConfig>(() => ({
  key: 'sale-product',
  titleKey: 'productCenter.saleProduct.title',
  descriptionKey: 'productCenter.saleProduct.description',
  idKey: 'saleProductId',
  singleRowActions: true,
  showDetail: true,
  permissions: {
    add: 'product:sale-product:add',
    edit: 'product:sale-product:edit',
    remove: 'product:sale-product:remove',
    reference: 'product:sale-product:reference'
  },
  api: saleProductGridApi,
  hideReference: false,
  defaultSort: { prop: 'updateTime', order: 'descending' },
  defaultRecord: {
    status: PRODUCT_STATUS_DISABLED,
    sortOrder: 0
  },
  submitFields: [
    'saleProductId',
    'saleProductCode',
    'saleProductName',
    'formulaId',
    'sortOrder',
    'remark'
  ],
  rowTone: (row) => row.priceStatus === 'WARNING' ? 'warning' : undefined,
  fields: [
    { prop: 'saleProductCode', labelKey: 'productCenter.saleProduct.code', search: true, required: true, minWidth: 160, sortable: true },
    { prop: 'saleProductName', labelKey: 'productCenter.saleProduct.name', search: true, required: true, minWidth: 220, sortable: true },
    {
      prop: 'formulaId',
      labelKey: 'productCenter.saleProduct.formula',
      type: 'remote-select',
      optionLoader: loadFormulaOptions,
      fillFields: {
        formulaCode: 'formulaCode',
        formulaName: 'formulaName',
        formulaVersionId: 'currentVersionId',
        formulaVersionNo: 'currentVersionNo',
        formulaVersionLabel: 'currentVersionLabel',
        categoryId: 'categoryId',
        categoryCode: 'categoryCode',
        categoryNameCn: 'categoryNameCn',
        productTypeCode: 'productTypeCode',
        productTypeNameCn: 'productTypeNameCn',
        minWidthInch: 'minWidthInch',
        minHeightInch: 'minHeightInch',
        maxWidthInch: 'maxWidthInch',
        maxHeightInch: 'maxHeightInch',
        sizeSummary: 'sizeSummary'
      },
      onChange: fillFormulaDefaults,
      search: true,
      required: true,
      table: false
    },
    { prop: 'categoryNameCn', labelKey: 'productCenter.formula.category', form: false, minWidth: 150 },
    { prop: 'productTypeNameCn', labelKey: 'productCenter.formula.productType', form: false, minWidth: 130 },
    { prop: 'formulaName', labelKey: 'productCenter.saleProduct.formula', form: false, minWidth: 180 },
    { prop: 'formulaVersionLabel', labelKey: 'productCenter.saleProduct.formulaVersion', form: false, minWidth: 120 },
    { prop: 'minWidthInch', labelKey: 'productCenter.formula.minWidthInch', type: 'inch', form: false, minWidth: 150 },
    { prop: 'minHeightInch', labelKey: 'productCenter.formula.minHeightInch', type: 'inch', form: false, minWidth: 150 },
    { prop: 'maxWidthInch', labelKey: 'productCenter.formula.maxWidthInch', type: 'inch', form: false, minWidth: 150 },
    { prop: 'maxHeightInch', labelKey: 'productCenter.formula.maxHeightInch', type: 'inch', form: false, minWidth: 150 },
    { prop: 'sizeSummary', labelKey: 'productCenter.formula.sizeSummary', form: false, minWidth: 210, formatter: sizeSummary },
    { prop: 'priceStatus', labelKey: 'productCenter.saleProduct.priceStatus', type: 'select', options: priceOptions.value, search: true, form: false, minWidth: 130 },
    { prop: 'status', labelKey: 'productCenter.common.status', type: 'status', options: statusOptions.value, search: true, form: false, minWidth: 120 },
    { prop: 'updateTime', labelKey: 'productCenter.formula.updateTime', type: 'datetime', form: false, minWidth: 160, sortable: true },
    { prop: 'updateBy', labelKey: 'productCenter.formula.updateBy', form: false, minWidth: 120 },
    { prop: 'sortOrder', labelKey: 'productCenter.common.sortOrder', type: 'number', table: false },
    { prop: 'remark', labelKey: 'productCenter.common.remark', type: 'textarea', table: false, formSpan: 2 }
  ],
  rowActions: [
    {
      labelKey: 'productCenter.pricing.priceSetting',
      icon: 'Money',
      type: 'primary',
      primary: true,
      permission: 'product:pricing:query',
      handler: (row) => {
        void router.push({ path: '/product-formula/price-settings', query: { saleProductId: String(row.saleProductId || '') } })
      }
    }
  ]
}))
</script>
