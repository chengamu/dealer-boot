<template>
  <div class="app-container price-setting-page">
    <PriceSetupHeader
      v-if="currentProduct.saleProductId"
      :product="currentProduct"
      :products="products"
      :selected-product-id="selectedProductId"
      :loading="loading"
      :saving="saving"
      :validating="validating"
      :editable="editable"
      @product-change="selectProduct"
      @refresh="reload"
      @validate="validatePrice"
      @save="saveAll"
    />
    <el-empty v-else :description="t('productCenter.pricing.noSaleProduct')" />
    <template v-if="currentProduct.saleProductId">
      <PriceSetupOverview
        :fabric-rule-count="fabricRows.length"
        :fee-rule-count="feeRows.length"
        :issue-count="issues.length"
        :material-group-counts="setup.materialGroupCounts"
      />
      <el-alert
        v-if="!editable"
        :title="t('productCenter.pricing.enabledEditDenied')"
        type="warning"
        show-icon
        :closable="false"
      />
      <FabricPriceTable
        :rows="fabricRows"
        :formula="fabricFormula"
        :editable="editable"
        @generate="generateFabricPrices"
        @batch="batchOpen = true"
        @row-change="updateFabricRule"
        @formula-change="updateFabricFormula"
      />
      <div class="price-setting-page__lower">
        <ShippingFormulaPanel
          :rows="feeRows"
          :editable="editable"
          @change="updateShippingRules"
        />
        <PriceIssuePanel :issues="issues" />
      </div>
    </template>
    <FabricPriceBatchDialog
      v-model="batchOpen"
      :fabrics="fabricCandidates"
      :columns="fabricColumns"
      :formula="fabricFormula"
      @save="applyBatchFabricPrice"
    />
  </div>
</template>

<script setup lang="ts" name="PriceSettingPage">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import type {
  ExtraFeeRule,
  FabricPriceRule,
  PriceFabricCandidate,
  PriceOptionCombination,
  PriceSetupVO,
  PriceValidationIssue,
  SaleProductVO
} from '@/api/product-pricing/types'
import { productPriceApi, saleProductApi } from '@/api/product-pricing/pricing'
import { PRODUCT_STATUS_ENABLED } from '@/constants/productStatus'
import PriceSetupHeader from './components/PriceSetupHeader.vue'
import PriceSetupOverview from './components/PriceSetupOverview.vue'
import FabricPriceTable from './components/FabricPriceTable.vue'
import FabricPriceBatchDialog from './components/FabricPriceBatchDialog.vue'
import ShippingFormulaPanel from './components/ShippingFormulaPanel.vue'
import PriceIssuePanel from './components/PriceIssuePanel.vue'
import { DEFAULT_FABRIC_PRICE_FORMULA } from './utils/pricingDisplay'

const route = useRoute()
const router = useRouter()
const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)

const products = ref<SaleProductVO[]>([])
const selectedProductId = ref(toIdString(route.query.saleProductId))
const setup = ref<PriceSetupVO>({})
const fabricRows = ref<FabricPriceRule[]>([])
const feeRows = ref<ExtraFeeRule[]>([])
const issues = ref<PriceValidationIssue[]>([])
const loading = ref(false)
const saving = ref(false)
const validating = ref(false)
const batchOpen = ref(false)
const fabricFormula = ref(DEFAULT_FABRIC_PRICE_FORMULA)

const currentProduct = computed(() => setup.value.saleProduct || products.value.find((item) => toIdString(item.saleProductId) === selectedProductId.value) || {})
const editable = computed(() => currentProduct.value.status !== PRODUCT_STATUS_ENABLED)
const fabricCandidates = computed(() => setup.value.fabricCandidates || [])
const fabricColumns = computed(() => setup.value.fabricPriceColumns?.length
  ? setup.value.fabricPriceColumns
  : [{ optionCombinationKey: 'DEFAULT', optionCombinationName: t('productCenter.pricing.defaultCombination') }])

onMounted(async () => {
  await loadProducts()
  if (!selectedProductId.value && products.value[0]?.saleProductId) selectedProductId.value = toIdString(products.value[0].saleProductId)
  await reload()
})

async function loadProducts() {
  const response = await saleProductApi.options({ pageNum: 1, pageSize: 500 })
  products.value = Array.isArray(response) ? response : response.data || []
}

async function reload() {
  if (!selectedProductId.value) return
  loading.value = true
  try {
    const response = await productPriceApi.setup(selectedProductId.value)
    setup.value = response.data || {}
    fabricRows.value = (setup.value.fabricRules || []).map((row) => ({ ...row }))
    feeRows.value = (setup.value.feeRules || []).map((row) => ({ ...row }))
    issues.value = setup.value.issues || []
    fabricFormula.value = fabricRows.value.find(row => row.areaFormula)?.areaFormula || DEFAULT_FABRIC_PRICE_FORMULA
  } finally {
    loading.value = false
  }
}

async function selectProduct(value: string | number) {
  selectedProductId.value = toIdString(value)
  await router.replace({ path: route.path, query: { saleProductId: selectedProductId.value } })
  await reload()
}

async function generateFabricPrices(overwrite: boolean) {
  if (!selectedProductId.value) return
  if (overwrite) {
    await ElMessageBox.confirm(t('productCenter.pricing.confirmOverwriteGenerate'), t('common.prompt'), { type: 'warning' })
  }
  await productPriceApi.generateFabricPrices(selectedProductId.value, overwrite)
  ElMessage.success(t('common.success'))
  await reload()
}

function updateFabricRule(row: FabricPriceRule) {
  fabricRows.value = upsertFabricRule(fabricRows.value, row)
}

function updateFabricFormula(value: string) {
  fabricFormula.value = value
  fabricRows.value = fabricRows.value.map(row => ({ ...row, areaFormula: value }))
}

async function applyBatchFabricPrice(payload: { materialCodes: string[], combinationKeys: string[], unitPrice: number, formula: string, blankOnly: boolean }) {
  fabricFormula.value = payload.formula || fabricFormula.value
  let next = fabricRows.value
  for (const material of fabricCandidates.value.filter(item => payload.materialCodes.includes(String(item.materialCode || '')))) {
    for (const column of fabricColumns.value.filter(item => payload.combinationKeys.includes(item.optionCombinationKey || 'DEFAULT'))) {
      const existing = findFabricRule(next, material, column)
      if (payload.blankOnly && Number(existing?.basePrice || 0) > 0) continue
      next = upsertFabricRule(next, buildFabricRule(material, column, payload.unitPrice, fabricFormula.value))
    }
  }
  fabricRows.value = next
  await saveFabricRules()
}

function updateShippingRules(rows: ExtraFeeRule[]) {
  feeRows.value = rows
}

async function saveAll() {
  saving.value = true
  try {
    await Promise.all([saveFabricRules(false), saveFeeRules(false)])
    ElMessage.success(t('common.success'))
    await reload()
  } finally {
    saving.value = false
  }
}

async function saveFabricRules(refresh = true) {
  if (!selectedProductId.value) return
  await productPriceApi.saveFabricRules(selectedProductId.value, fabricRows.value)
  if (refresh) {
    ElMessage.success(t('common.success'))
    await reload()
  }
}

async function saveFeeRules(refresh = true) {
  if (!selectedProductId.value) return
  await productPriceApi.saveExtraFeeRules(selectedProductId.value, feeRows.value)
  if (refresh) {
    ElMessage.success(t('common.success'))
    await reload()
  }
}

async function validatePrice() {
  if (!selectedProductId.value) return
  validating.value = true
  try {
    const response = await productPriceApi.validate(selectedProductId.value)
    issues.value = response.data || []
    ElMessage.success(t('productCenter.pricing.validateDone'))
    await reload()
  } finally {
    validating.value = false
  }
}

function upsertFabricRule(rows: FabricPriceRule[], row: FabricPriceRule) {
  const index = rows.findIndex(item => item.materialCode === row.materialCode
    && (item.optionCombinationKey || 'DEFAULT') === (row.optionCombinationKey || 'DEFAULT'))
  if (index < 0) return rows.concat(row)
  return rows.map((item, itemIndex) => itemIndex === index ? { ...item, ...row } : item)
}

function findFabricRule(rows: FabricPriceRule[], material: PriceFabricCandidate, column: PriceOptionCombination) {
  return rows.find(item => item.materialCode === material.materialCode
    && (item.optionCombinationKey || 'DEFAULT') === (column.optionCombinationKey || 'DEFAULT'))
}

function buildFabricRule(material: PriceFabricCandidate, column: PriceOptionCombination, basePrice: number, formula: string): FabricPriceRule {
  return {
    ...findFabricRule(fabricRows.value, material, column),
    materialId: material.materialId,
    materialCode: material.materialCode,
    materialNameCn: material.materialNameCn,
    unitCode: material.unitCode,
    optionCombinationKey: column.optionCombinationKey || 'DEFAULT',
    optionCombinationName: column.optionCombinationName || t('productCenter.pricing.defaultCombination'),
    priceMode: 'FORMULA',
    basePrice,
    areaFormula: formula || DEFAULT_FABRIC_PRICE_FORMULA,
    minBillArea: 1,
    lossRate: 0,
    status: 'ENABLED'
  }
}

function toIdString(value: unknown) {
  const raw = Array.isArray(value) ? value[0] : value
  return raw === undefined || raw === null ? '' : String(raw)
}
</script>

<style scoped>
.price-setting-page {
  display: grid;
  gap: 12px;
  background: #f3f6fb;
}

.price-setting-page__lower {
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(360px, 0.65fr);
  gap: 12px;
}

@media (max-width: 1280px) {
  .price-setting-page__lower {
    grid-template-columns: 1fr;
  }
}
</style>
