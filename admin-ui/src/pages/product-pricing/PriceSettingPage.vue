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
        :fabric-rule-count="priceFabrics.length"
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
        :rows="priceFabrics"
        :rules="fabricRules"
        :editable="editable"
        @generate="generateFabricPrices"
        @batch="batchOpen = true"
        @open-rules="openFabricRules"
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
      :fabrics="priceFabrics"
      :options="setup.formulaOptions || []"
      :option-values="setup.formulaOptionValues || []"
      @save="applyBatchFabricPrice"
    />
    <FabricPriceRuleDrawer
      v-model="ruleDrawerOpen"
      :fabric="activeFabric"
      :rules="activeFabricRules"
      :editable="editable"
      :options="setup.formulaOptions || []"
      :option-values="setup.formulaOptionValues || []"
      @save="saveFabricRules"
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
  PriceFabricVO,
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
import FabricPriceRuleDrawer from './components/FabricPriceRuleDrawer.vue'
import ShippingFormulaPanel from './components/ShippingFormulaPanel.vue'
import PriceIssuePanel from './components/PriceIssuePanel.vue'
import { fabricPriceFormulaForUnitPrice } from './utils/pricingDisplay'

const route = useRoute()
const router = useRouter()
const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)

const products = ref<SaleProductVO[]>([])
const selectedProductId = ref(toIdString(route.query.saleProductId))
const setup = ref<PriceSetupVO>({})
const priceFabrics = ref<PriceFabricVO[]>([])
const fabricRules = ref<FabricPriceRule[]>([])
const feeRows = ref<ExtraFeeRule[]>([])
const issues = ref<PriceValidationIssue[]>([])
const loading = ref(false)
const saving = ref(false)
const validating = ref(false)
const batchOpen = ref(false)
const ruleDrawerOpen = ref(false)
const activeFabric = ref<PriceFabricVO>()

const currentProduct = computed(() => setup.value.saleProduct || products.value.find((item) => toIdString(item.saleProductId) === selectedProductId.value) || {})
const editable = computed(() => currentProduct.value.status !== PRODUCT_STATUS_ENABLED)
const activeFabricRules = computed(() => fabricRules.value.filter(row => row.priceFabricId === activeFabric.value?.priceFabricId))

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
    priceFabrics.value = (setup.value.priceFabrics || []).map((row) => ({ ...row }))
    fabricRules.value = (setup.value.fabricRules || []).map((row) => ({ ...row }))
    feeRows.value = (setup.value.feeRules || []).map((row) => ({ ...row }))
    issues.value = setup.value.issues || []
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
  await ElMessageBox.confirm(
    t(overwrite ? 'productCenter.pricing.confirmOverwriteGenerate' : 'productCenter.pricing.confirmSyncGenerate'),
    t('common.prompt'),
    { type: overwrite ? 'warning' : 'info' }
  )
  await productPriceApi.generateFabricPrices(selectedProductId.value, overwrite)
  ElMessage.success(t('common.success'))
  await reload()
}

function openFabricRules(row: PriceFabricVO) {
  activeFabric.value = row
  ruleDrawerOpen.value = true
}

async function applyBatchFabricPrice(payload: { priceFabricIds: number[], conditionExpression: string, unitPrice: number, priceFormula: string, blankOnly: boolean }) {
  if (!selectedProductId.value) return
  for (const priceFabricId of payload.priceFabricIds) {
    const existing = fabricRules.value.filter(row => row.priceFabricId === priceFabricId)
    if (payload.blankOnly && existing.length) continue
    const defaultRule = buildFabricRule(priceFabricId, payload)
    const nextRows = existing.filter((row) => {
      if (defaultRule.defaultRuleFlag) return !row.defaultRuleFlag
      return row.conditionKey !== defaultRule.conditionKey
    })
    await productPriceApi.saveFabricRules(selectedProductId.value, priceFabricId, nextRows.concat(defaultRule))
  }
  ElMessage.success(t('common.success'))
  await reload()
}

function updateShippingRules(rows: ExtraFeeRule[]) {
  feeRows.value = rows
}

async function saveAll() {
  saving.value = true
  try {
    await saveFeeRules(false)
    ElMessage.success(t('common.success'))
    await reload()
  } finally {
    saving.value = false
  }
}

async function saveFabricRules(priceFabricId?: number, rows?: FabricPriceRule[], refresh = true) {
  if (!selectedProductId.value) return
  if (!priceFabricId || !rows) return
  await productPriceApi.saveFabricRules(selectedProductId.value, priceFabricId, rows)
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

function buildFabricRule(priceFabricId: number, payload: { conditionExpression: string, unitPrice: number, priceFormula: string }): FabricPriceRule {
  const expression = payload.conditionExpression?.trim()
  const defaultRule = !expression
  return {
    priceFabricId,
    conditionType: defaultRule ? 'DEFAULT' : 'EXPRESSION',
    conditionExpression: defaultRule ? 'DEFAULT' : expression,
    conditionText: defaultRule ? t('productCenter.pricing.defaultRule') : expression,
    conditionKey: defaultRule ? 'DEFAULT' : expression,
    priceMode: 'FORMULA',
    unitPrice: payload.unitPrice,
    priceFormula: payload.priceFormula || fabricPriceFormulaForUnitPrice(payload.unitPrice),
    defaultRuleFlag: defaultRule,
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
