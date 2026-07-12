<template>
  <div class="app-container formula-workbench-page" v-loading="loading">
    <FormulaWorkbenchHeader
      :formula="formula"
      :loading="actionLoading"
      :t="t"
      @back="router.push('/product-formula/formulas')"
      @copy="copyDialogOpen = true"
      @validate="validateFormula"
      @submit="submitReview"
      @start-revision="startRevision"
      @withdraw="withdrawFormula"
    />

    <FormulaWorkbenchSteps
      :formula="formula"
      :pending-version="pendingVersion"
      :t="t"
      @open="openStep"
    />

    <FormulaVersionHistoryPanel
      :versions="versions"
      :loading="versionLoading"
      :t="t"
      @refresh="loadVersions"
      @view="viewSnapshot"
    />

    <FormulaCopyDialog
      v-model="copyDialogOpen"
      :source="formula"
      :loading="actionLoading"
      :t="t"
      @submit="copyFormula"
    />

    <FormulaVersionSnapshotDrawer
      v-model="snapshotOpen"
      :version="snapshotVersion"
      :unit-rows="unitRows"
      :t="t"
    />
  </div>
</template>

<script setup lang="ts" name="ProductFormulaWorkbenchPage">
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { productUnitApi } from '@/api/product-capability/base'
import { productFormulaApi } from '@/api/product-formula/formula'
import type { ProductFormulaVO, ProductFormulaVersionVO, ProductUnitVO } from '@/api/product-capability/types'
import { FORMULA_STATUS, PRODUCT_STATUS_ENABLED } from '@/constants/productStatus'
import FormulaCopyDialog from './components/FormulaCopyDialog.vue'
import FormulaVersionHistoryPanel from './components/FormulaVersionHistoryPanel.vue'
import FormulaVersionSnapshotDrawer from './components/FormulaVersionSnapshotDrawer.vue'
import FormulaWorkbenchHeader from './components/FormulaWorkbenchHeader.vue'
import FormulaWorkbenchSteps, { type FormulaWorkbenchStepKey } from './components/FormulaWorkbenchSteps.vue'

const route = useRoute()
const router = useRouter()
const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)

const formula = ref<ProductFormulaVO>({})
const versions = ref<ProductFormulaVersionVO[]>([])
const loading = ref(false)
const versionLoading = ref(false)
const actionLoading = ref(false)
const copyDialogOpen = ref(false)
const snapshotOpen = ref(false)
const snapshotVersion = ref<ProductFormulaVersionVO>()
const unitRows = ref<ProductUnitVO[]>([])

const formulaId = computed(() => String(route.query.formulaId || formula.value.formulaId || ''))
const pendingVersion = computed(() => versions.value.find((item) => item.versionStatus === FORMULA_STATUS.PENDING_REVIEW))

async function loadFormula() {
  if (!formulaId.value) return
  loading.value = true
  try {
    const response = await productFormulaApi.get(formulaId.value)
    formula.value = response.data || {}
  } finally {
    loading.value = false
  }
}

async function loadVersions() {
  if (!formulaId.value) return
  versionLoading.value = true
  try {
    const response = await productFormulaApi.versionHistory(formulaId.value)
    versions.value = response.data || []
  } finally {
    versionLoading.value = false
  }
}

async function loadUnits() {
  const response = await productUnitApi.options?.({ status: PRODUCT_STATUS_ENABLED, pageNum: 1, pageSize: 500 })
  unitRows.value = Array.isArray(response) ? response : response?.data || []
}

async function refreshAll() {
  await Promise.all([loadFormula(), loadVersions(), loadUnits()])
}

async function withAction(messageKey: string, action: () => Promise<unknown>) {
  await ElMessageBox.confirm(t(messageKey), t('common.prompt'), { type: 'warning' })
  actionLoading.value = true
  try {
    await action()
    ElMessage.success(t('common.success'))
    await refreshAll()
  } finally {
    actionLoading.value = false
  }
}

function openStep(step: FormulaWorkbenchStepKey) {
  if (step === 'materials') {
    void router.push({ name: 'ProductFormulaMaterials', query: { formulaId: formulaId.value } })
  } else if (step === 'options') {
    void router.push({ name: 'ProductFormulaOptions', query: { formulaId: formulaId.value } })
  } else if (step === 'simulation') {
    void router.push({ name: 'ProductFormulaSimulation', query: { formulaId: formulaId.value } })
  } else if (pendingVersion.value?.versionId) {
    void router.push({ name: 'ProductFormulaReviewDetail', params: { reviewId: String(pendingVersion.value.versionId) } })
  }
}

async function validateFormula() {
  await withAction('productCenter.formula.confirm.validate', () => productFormulaApi.validate(formulaId.value))
}

async function submitReview() {
  await withAction('productCenter.formula.confirm.submitReview', () => productFormulaApi.submitReview(formulaId.value))
}

async function startRevision() {
  await withAction('productCenter.formula.confirm.startRevision', () => productFormulaApi.startRevision(formulaId.value))
}

async function withdrawFormula() {
  await withAction('productCenter.formula.confirm.withdraw', () => productFormulaApi.withdraw(formulaId.value))
}

async function copyFormula(payload: Partial<ProductFormulaVO>) {
  actionLoading.value = true
  try {
    const response = await productFormulaApi.copyFormula(formulaId.value, payload)
    copyDialogOpen.value = false
    ElMessage.success(t('common.success'))
    const nextId = response.data?.formulaId
    if (nextId) {
      await router.replace({ name: 'ProductFormulaWorkbench', query: { formulaId: String(nextId) } })
      await refreshAll()
    }
  } finally {
    actionLoading.value = false
  }
}

function viewSnapshot(version: ProductFormulaVersionVO) {
  snapshotVersion.value = version
  snapshotOpen.value = true
}

onMounted(refreshAll)
</script>

<style scoped>
.formula-workbench-page {
  display: grid;
  gap: 12px;
  background: var(--admin-bg);
}
</style>
