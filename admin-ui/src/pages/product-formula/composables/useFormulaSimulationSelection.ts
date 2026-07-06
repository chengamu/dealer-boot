import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { productFormulaApi } from '@/api/product-formula/formula'
import { formulaStatusText } from '@/constants/productStatus'
import type { ProductFormulaVO } from '@/api/product-capability/types'

const LAST_FORMULA_STORAGE_KEY = 'productFormula.simulation.lastFormulaId'

export function useFormulaSimulationSelection(t: (key: string) => string) {
  const route = useRoute()
  const router = useRouter()
  const selectedFormulaId = ref('')
  const formulaSelecting = ref(false)
  const formulaRows = ref<ProductFormulaVO[]>([])

  const currentFormulaId = computed(() => selectedFormulaId.value)
  const formulaOptions = computed(() => formulaRows.value.map((row) => ({
    value: String(row.formulaId || ''),
    label: `${row.formulaCode || '-'} ${row.formulaName || '-'}（${formulaStatusText(row.status, t)}）`
  })).filter((item) => item.value))

  async function loadFormulaOptions() {
    formulaSelecting.value = true
    try {
      normalizeInvalidRoute()
      const response = await productFormulaApi.list({ pageNum: 1, pageSize: 500 })
      formulaRows.value = response.rows || []
      const routeId = routeFormulaId()
      if (routeId && formulaRows.value.some((row) => String(row.formulaId) === routeId)) {
        selectFormula(routeId)
        return
      }
      const rememberedId = rememberedFormulaId()
      if (rememberedId && formulaRows.value.some((row) => String(row.formulaId) === rememberedId)) {
        selectedFormulaId.value = rememberedId
        return
      }
      selectedFormulaId.value = formulaOptions.value[0]?.value || ''
    } finally {
      formulaSelecting.value = false
    }
  }

  function handleFormulaChange(value: string) {
    selectFormula(value)
  }

  function selectFormula(value: string) {
    selectedFormulaId.value = value
    if (value) window.sessionStorage.setItem(LAST_FORMULA_STORAGE_KEY, value)
  }

  function routeFormulaId() {
    const rawId = String(route.params.id || route.query.formulaId || '')
    return /^\d+$/.test(rawId) ? rawId : ''
  }

  function normalizeInvalidRoute() {
    const rawId = String(route.params.id || '')
    if (rawId && !/^\d+$/.test(rawId)) {
      router.replace({ name: 'ProductFormulaSimulation' })
    }
  }

  function rememberedFormulaId() {
    return window.sessionStorage.getItem(LAST_FORMULA_STORAGE_KEY) || ''
  }

  return {
    selectedFormulaId,
    currentFormulaId,
    formulaOptions,
    formulaSelecting,
    loadFormulaOptions,
    handleFormulaChange
  }
}
