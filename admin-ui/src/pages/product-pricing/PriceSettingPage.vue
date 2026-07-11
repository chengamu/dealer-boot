<template>
  <div class="app-container price-setting-page">
    <PriceSetupHeader
      v-if="currentProduct.saleProductId"
      :product="currentProduct"
      :products="products"
      :selected-product-id="selectedProductId"
      :loading="loading"
      :validating="validating"
      :editable="editable"
      @product-change="selectProduct"
      @refresh="reload"
      @validate="validatePrice"
    />
    <el-empty v-else :description="t('productCenter.pricing.noSaleProduct')" />
    <template v-if="currentProduct.saleProductId">
      <PriceSetupOverview
        :material-rule-count="priceMaterials.length"
        :issue-count="issues.length"
        :material-group-counts="setup.materialGroupCounts"
        :formula-materials="setup.formulaMaterials || []"
      />
      <el-alert
        v-if="!editable"
        :title="t('productCenter.pricing.enabledEditDenied')"
        type="warning"
        show-icon
        :closable="false"
      />
      <MaterialPriceTable
        :rows="priceMaterials"
        :rules="materialRules"
        :editable="editable"
        :options="setup.formulaOptions || []"
        :option-values="setup.formulaOptionValues || []"
        @generate="generateMaterialPrices"
        @batch="openBatchPrice"
        @open-rules="openMaterialRules"
      />
      <PriceIssuePanel :issues="issues" />
    </template>
    <MaterialPriceBatchDrawer
      v-model="batchOpen"
      :materials="batchMaterials"
      :options="setup.formulaOptions || []"
      :option-values="setup.formulaOptionValues || []"
      @save="applyBatchMaterialPrice"
    />
    <MaterialPriceRuleDrawer
      v-model="ruleDrawerOpen"
      :material="activeMaterial"
      :rules="activeMaterialRules"
      :editable="editable"
      :options="setup.formulaOptions || []"
      :option-values="setup.formulaOptionValues || []"
      @save="saveMaterialRules"
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
  MaterialPriceRule,
  PriceMaterialVO,
  PriceSetupVO,
  PriceValidationIssue,
  SaleProductVO
} from '@/api/product-pricing/types'
import { productPriceApi, saleProductApi } from '@/api/product-pricing/pricing'
import { PRODUCT_STATUS_ENABLED } from '@/constants/productStatus'
import PriceSetupHeader from './components/PriceSetupHeader.vue'
import PriceSetupOverview from './components/PriceSetupOverview.vue'
import MaterialPriceTable from './components/MaterialPriceTable.vue'
import MaterialPriceBatchDrawer from './components/MaterialPriceBatchDrawer.vue'
import MaterialPriceRuleDrawer from './components/MaterialPriceRuleDrawer.vue'
import PriceIssuePanel from './components/PriceIssuePanel.vue'

const route = useRoute()
const router = useRouter()
const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)

const products = ref<SaleProductVO[]>([])
const selectedProductId = ref(toIdString(route.query.saleProductId))
const setup = ref<PriceSetupVO>({})
const priceMaterials = ref<PriceMaterialVO[]>([])
const materialRules = ref<MaterialPriceRule[]>([])
const issues = ref<PriceValidationIssue[]>([])
const loading = ref(false)
const validating = ref(false)
const batchOpen = ref(false)
const ruleDrawerOpen = ref(false)
const activeMaterial = ref<PriceMaterialVO>()
const batchMaterials = ref<PriceMaterialVO[]>([])

const currentProduct = computed(() => setup.value.saleProduct || products.value.find((item) => toIdString(item.saleProductId) === selectedProductId.value) || {})
const editable = computed(() => currentProduct.value.status !== PRODUCT_STATUS_ENABLED)
const activeMaterialRules = computed(() => materialRules.value.filter(row => String(row.priceMaterialId || '') === String(activeMaterial.value?.priceMaterialId || '')))

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
    priceMaterials.value = (setup.value.priceMaterials || []).map((row) => ({ ...row }))
    materialRules.value = (setup.value.materialRules || []).map((row) => ({ ...row }))
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

async function generateMaterialPrices(overwrite: boolean) {
  if (!selectedProductId.value) return
  await ElMessageBox.confirm(
    t(overwrite ? 'productCenter.pricing.confirmOverwriteGenerate' : 'productCenter.pricing.confirmSyncGenerate'),
    t('common.prompt'),
    { type: overwrite ? 'warning' : 'info' }
  )
  await productPriceApi.generateMaterialPrices(selectedProductId.value, overwrite)
  ElMessage.success(t('common.success'))
  await reload()
}

function openMaterialRules(row: PriceMaterialVO) {
  activeMaterial.value = row
  ruleDrawerOpen.value = true
}

function openBatchPrice(rows: PriceMaterialVO[]) {
  const groups = new Set(rows.map((row) => row.attributeGroupCode || 'UNCLASSIFIED'))
  if (groups.size > 1) {
    ElMessage.warning(t('productCenter.pricing.batchSameGroupRequired'))
    return
  }
  batchMaterials.value = rows
  batchOpen.value = true
}

async function applyBatchMaterialPrice(payload: { priceMaterialIds: string[], rules: MaterialPriceRule[] }) {
  if (!selectedProductId.value) return
  await productPriceApi.saveMaterialRulesBatch(selectedProductId.value, payload)
  batchOpen.value = false
  ElMessage.success(t('common.success'))
  await reload()
}

async function saveMaterialRules(priceMaterialId?: string, rows?: MaterialPriceRule[], refresh = true) {
  if (!selectedProductId.value) return
  if (!priceMaterialId || !rows) return
  await productPriceApi.saveMaterialRules(selectedProductId.value, priceMaterialId, rows)
  ruleDrawerOpen.value = false
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

</style>
