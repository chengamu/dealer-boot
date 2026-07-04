import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { productMaterialApi } from '@/api/product-capability/material'
import { productMaterialTypeApi, productMaterialTypeGroupApi, productUnitApi } from '@/api/product-capability/base'
import { productFormulaApi } from '@/api/product-formula/formula'
import useUserStore from '@/stores/user'
import { FORMULA_STATUS, PRODUCT_STATUS_ENABLED, formulaStatusText } from '@/constants/productStatus'
import { localizedRecordLabel } from '@/utils/productLabels'
import { useFormulaSetupDraftCache } from './useFormulaSetupDraftCache'
import { buildFormulaOptionPayload, normalizeFormulaOptionDraftState, optionClientKey } from '../utils/formulaOptionDraftIdentity'
import { normalizeFormulaMaterials, normalizeFormulaUsageRules, normalizeFormulaVariableRules } from '../utils/formulaSetupNumbers'
import type {
  ProductFormulaMaterialVO,
  ProductFormulaOptionMaterialVO,
  ProductFormulaOptionVO,
  ProductFormulaOptionValueVO,
  ProductFormulaRestrictionVO,
  ProductFormulaSetupVO,
  ProductFormulaUsageRuleVO,
  ProductFormulaVariableRuleVO,
  ProductFormulaVariableVO,
  ProductFormulaVO,
  ProductMaterialTypeGroupVO,
  ProductMaterialTypeVO,
  ProductMaterialVO,
  ProductOption,
  ProductRecord,
  ProductUnitVO
} from '@/api/product-capability/types'

export interface SetupState {
  materials: ProductFormulaMaterialVO[]
  options: ProductFormulaOptionVO[]
  optionValues: ProductFormulaOptionValueVO[]
  optionMaterials: ProductFormulaOptionMaterialVO[]
  restrictions: ProductFormulaRestrictionVO[]
  usageRules: ProductFormulaUsageRuleVO[]
  variables: ProductFormulaVariableVO[]
  variableRules: ProductFormulaVariableRuleVO[]
}

const LAST_FORMULA_STORAGE_KEY = 'productFormula.setup.lastFormulaId'

export function useFormulaSetupCore(props: { setupSection?: 'content' | 'options' }, t: (key: string) => string, language: () => string) {
  const route = useRoute()
  const userStore = useUserStore()
  const selectedFormulaId = ref('')
  const currentFormulaId = computed(() => selectedFormulaId.value)
  const activeTab = ref<'content' | 'options'>(routeSetupSection())
  const loading = ref(false)
  const saving = ref(false)
  const validating = ref(false)
  const formulaSelecting = ref(false)
  const materialPickerOpen = ref(false)
  const usageDrawerOpen = ref(false)
  const usageRow = ref<ProductFormulaMaterialVO | null>(null)
  const usageRows = ref<ProductFormulaMaterialVO[]>([])
  const selectedOptionCode = ref('')
  const materialRows = ref<ProductMaterialVO[]>([])
  const groupRows = ref<ProductMaterialTypeGroupVO[]>([])
  const materialTypeRows = ref<ProductMaterialTypeVO[]>([])
  const unitRows = ref<ProductUnitVO[]>([])
  const editableFormulas = ref<ProductFormulaVO[]>([])
  const setup = reactive<SetupState>({ materials: [], options: [], optionValues: [], optionMaterials: [], restrictions: [], usageRules: [], variables: [], variableRules: [] })
  const formula = ref<ProductFormulaVO>({})
  let setupLoadSeq = 0
  const draftCache = useFormulaSetupDraftCache({
    activeTab,
    currentFormulaId,
    formula,
    setup,
    tenantId: () => String(userStore.user?.tenantId || 'default'),
    userId: () => String(userStore.id || userStore.user?.userId || 'anonymous'),
    t,
    normalize: () => normalizeFormulaOptionDraftState(setup)
  })

  const draftVersionLabel = computed(() => `V${formula.value.draftVersionNo || 1} ${t('productCenter.formula.status.draft')}`)
  const editableFormulaOptions = computed(() => editableFormulas.value.map((row) => ({
    value: String(row.formulaId || ''),
    label: compactFormulaLabel(row)
  })).filter((item) => item.value))
  const groupOptions = computed<ProductOption[]>(() => groupRows.value.map((row) => ({ value: row.groupCode || '', label: labelOf(row, 'groupCode', 'groupNameCn', 'groupNameEn') })).filter((item) => item.value))
  const groupSortMap = computed<Record<string, number>>(() => {
    const result: Record<string, number> = {}
    groupRows.value.forEach((row, index) => {
      if (row.groupCode) result[row.groupCode] = index + 1
    })
    return result
  })
  const unitOptions = computed<ProductOption[]>(() => unitRows.value.map((row) => ({ value: row.unitCode || '', label: labelOf(row, 'unitCode', 'unitNameCn', 'unitNameEn') })).filter((item) => item.value))
  const materialGroupCards = computed(() => groupRows.value
    .filter((group) => group.status === PRODUCT_STATUS_ENABLED && group.formulaSummaryVisibleFlag !== false)
    .map((group) => ({
      code: group.groupCode || '',
      name: group.groupNameCn || group.groupCode || '-',
      nameEn: group.groupNameEn || group.groupCode || '',
      shortName: String(group.groupNameCn || group.groupCode || '-').slice(0, 1),
      count: setup.materials.filter((row) => row.attributeGroupCode === group.groupCode).length
    })))

  onMounted(async () => {
    await Promise.all([loadBaseOptions(), loadEditableFormulas()])
    await loadSetup()
  })
  watch([() => route.meta.setupSection, () => props.setupSection], async () => {
    activeTab.value = routeSetupSection()
    await loadSetup()
  })
  watch(currentFormulaId, async () => { await loadSetup() })

  async function loadBaseOptions() {
    const [groupResponse, typeResponse, materialResponse, unitResponse] = await Promise.all([
      productMaterialTypeGroupApi.options?.({ status: PRODUCT_STATUS_ENABLED, pageNum: 1, pageSize: 500 }),
      productMaterialTypeApi.options?.({ status: PRODUCT_STATUS_ENABLED, pageNum: 1, pageSize: 500 }),
      productMaterialApi.options?.({ status: PRODUCT_STATUS_ENABLED, pageNum: 1, pageSize: 1000 }),
      productUnitApi.options?.({ status: PRODUCT_STATUS_ENABLED, pageNum: 1, pageSize: 500 })
    ])
    groupRows.value = responseRows(groupResponse)
    materialTypeRows.value = responseRows(typeResponse)
    materialRows.value = responseRows(materialResponse)
    unitRows.value = responseRows(unitResponse)
  }

  async function loadEditableFormulas() {
    formulaSelecting.value = true
    try {
      const [draftResponse, rejectedResponse] = await Promise.all([
        productFormulaApi.list({ status: FORMULA_STATUS.DRAFT, pageNum: 1, pageSize: 500 }),
        productFormulaApi.list({ status: FORMULA_STATUS.REJECTED, pageNum: 1, pageSize: 500 })
      ])
      const formulaMap = new Map<string, ProductFormulaVO>()
      ;[...(draftResponse.rows || []), ...(rejectedResponse.rows || [])].forEach((row) => {
        if (row.formulaId) formulaMap.set(String(row.formulaId), row)
      })
      editableFormulas.value = [...formulaMap.values()]
      const routeId = routeFormulaId()
      if (routeId && formulaMap.has(routeId)) {
        selectedFormulaId.value = routeId
        rememberFormulaId(routeId)
        return
      }
      const rememberedId = rememberedFormulaId()
      if (!selectedFormulaId.value && rememberedId && formulaMap.has(rememberedId)) {
        selectedFormulaId.value = rememberedId
        return
      }
      if (!selectedFormulaId.value || !formulaMap.has(selectedFormulaId.value)) {
        selectedFormulaId.value = editableFormulaOptions.value[0]?.value || ''
      }
    } finally {
      formulaSelecting.value = false
    }
  }

  async function loadSetup() {
    const loadSeq = ++setupLoadSeq
    const formulaId = currentFormulaId.value
    const setupSection = activeTab.value
    await draftCache.beforeSetupReload()
    if (!formulaId) {
      resetSetup()
      return
    }
    loading.value = true
    try {
      const response = setupSection === 'options'
        ? await productFormulaApi.getFormulaOptions(formulaId)
        : await productFormulaApi.materials(formulaId)
      if (loadSeq !== setupLoadSeq || currentFormulaId.value !== formulaId || activeTab.value !== setupSection) return
      formula.value = response.data?.formula || {}
      setup.materials = normalizeFormulaMaterials(response.data?.materials || [])
      setup.options = (response.data?.options || []).map((row) => ({ ...row, visibilityMode: row.visibilityMode || 'ALWAYS' }))
      setup.optionValues = response.data?.optionValues || []
      setup.optionMaterials = response.data?.optionMaterials || []
      setup.restrictions = response.data?.restrictions || []
      setup.usageRules = normalizeFormulaUsageRules(response.data?.usageRules || [])
      setup.variables = response.data?.variables || []
      setup.variableRules = normalizeFormulaVariableRules(response.data?.variableRules || [])
      normalizeFormulaOptionDraftState(setup)
      selectedOptionCode.value = optionClientKey(setup.options[0]) || setup.options[0]?.optionCode || ''
      await draftCache.afterSetupLoaded()
      selectedOptionCode.value = optionClientKey(setup.options.find((row) => optionClientKey(row) === selectedOptionCode.value) || setup.options[0]) || setup.options[0]?.optionCode || ''
    } finally {
      if (loadSeq === setupLoadSeq) loading.value = false
    }
  }

  async function saveSetup() {
    if (!currentFormulaId.value) {
      ElMessage.warning(t('productCenter.formulaSetup.selectEditableFormulaHint'))
      return
    }
    saving.value = true
    try {
      const payload = buildPayload()
      if (activeTab.value === 'options') await productFormulaApi.saveOptions(currentFormulaId.value, payload)
      else await productFormulaApi.saveMaterials(currentFormulaId.value, payload)
      await draftCache.clearCurrentDraft()
      ElMessage.success(t('common.success'))
      await loadSetup()
    } finally {
      saving.value = false
    }
  }

  async function validateSetup() {
    if (!currentFormulaId.value) {
      ElMessage.warning(t('productCenter.formulaSetup.selectEditableFormulaHint'))
      return
    }
    if (draftCache.hasPendingDraft.value) {
      ElMessage.warning(t('productCenter.formulaSetup.saveBeforeValidate'))
      return
    }
    validating.value = true
    try {
      if (activeTab.value === 'options') await productFormulaApi.validateOptions(currentFormulaId.value)
      else await productFormulaApi.validateMaterials(currentFormulaId.value)
      ElMessage.success(t('productCenter.formula.validation.pass'))
      await loadSetup()
    } finally {
      validating.value = false
    }
  }

  function buildPayload(): ProductFormulaSetupVO {
    const payload = { materials: normalizeFormulaMaterials(setup.materials), options: setup.options, optionValues: setup.optionValues, optionMaterials: setup.optionMaterials, restrictions: setup.restrictions, usageRules: normalizeFormulaUsageRules(setup.usageRules).map(withoutUsageRuleLossRate), variables: setup.variables, variableRules: normalizeFormulaVariableRules(setup.variableRules) }
    return activeTab.value === 'options' ? buildFormulaOptionPayload(payload) : payload
  }

  function withoutUsageRuleLossRate(row: ProductFormulaUsageRuleVO): ProductFormulaUsageRuleVO {
    const next = { ...row }
    delete next.lossRate
    return next
  }

  function handleFormulaChange(value: string) {
    selectedFormulaId.value = value
    rememberFormulaId(value)
  }

  function handleVariablesSaved(value: ProductFormulaSetupVO) {
    setup.variables = value.variables || []
    setup.variableRules = normalizeFormulaVariableRules(value.variableRules || [])
  }

  function resetSetup() {
    formula.value = {}
    setup.materials = []
    setup.options = []
    setup.optionValues = []
    setup.optionMaterials = []
    setup.restrictions = []
    setup.usageRules = []
    setup.variables = []
    setup.variableRules = []
    selectedOptionCode.value = ''
  }

  function routeFormulaId() {
    const rawId = String(route.params.id || '')
    return /^\d+$/.test(rawId) ? rawId : ''
  }

  function routeSetupSection() {
    if (props.setupSection) return props.setupSection
    return route.meta.setupSection === 'options' ? 'options' : 'content'
  }

  function compactFormulaLabel(row: ProductFormulaVO) {
    return `${row.formulaCode || '-'} ${row.formulaName || '-'}（${formulaStatusText(row.status, t)}）`
  }

  function labelOf(row: ProductRecord, codeKey: string, cnKey: string, enKey?: string) {
    return localizedRecordLabel(row, language(), codeKey, cnKey, enKey)
  }

  function responseRows<T>(response: { data?: T[] } | T[] | undefined): T[] {
    return Array.isArray(response) ? response : response?.data || []
  }

  function rememberedFormulaId() {
    return window.sessionStorage.getItem(LAST_FORMULA_STORAGE_KEY) || ''
  }

  function rememberFormulaId(value: string) {
    if (value) window.sessionStorage.setItem(LAST_FORMULA_STORAGE_KEY, value)
  }

  return {
    activeTab, loading, saving, validating, formulaSelecting, materialPickerOpen, usageDrawerOpen, usageRow, usageRows,
    selectedFormulaId, currentFormulaId, selectedOptionCode, materialRows, groupRows, materialTypeRows, unitRows, setup, formula,
    draftVersionLabel, editableFormulaOptions, groupOptions, groupSortMap, unitOptions, materialGroupCards, draftCacheStatus: draftCache.cacheStatus,
    saveSetup, validateSetup, handleFormulaChange, handleVariablesSaved
  }
}
