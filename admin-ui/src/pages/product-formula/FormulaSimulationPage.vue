<template>
  <div class="app-container formula-simulation-page">
    <FormulaSimulationHeader
      :formula="formula"
      :status="simulationStatus"
      :message="result.message"
      :running="running"
      :validating="validating"
      :formula-options="formulaOptions"
      :selected-formula-id="selectedFormulaId"
      :formula-selecting="formulaSelecting"
      :can-operate="Boolean(currentFormulaId)"
      :validation-text="validationText"
      :message-text="messageText"
      @back="router.back()"
      @run="run"
      @validate="validate"
      @formula-change="handleFormulaChange"
    />

    <FormulaSimulationOrderSheet
      :form="form"
      :options="visibleOptions"
      :option-values="setup.optionValues || []"
      :option-materials="enabledOptionMaterials"
      :selected-option-values="selectedOptionValues"
      :quantity="quantity"
      :room="room"
      :hidden-count="hiddenOptionCount"
      :show-validation="showValidation"
      :disabled-option-values="disabledOptionValues"
      :restriction-messages="restrictionMessages"
      @update:quantity="quantity = $event"
      @update:room="room = $event"
    />

    <FormulaSimulationResultPanel
      v-loading="loading"
      :result="result"
      :formula="formula"
      :quantity="quantity"
      :selected-option-values="selectedOptionValues"
      :visible-options="visibleOptions"
      :option-materials="enabledOptionMaterials"
      :unit-label="unitLabel"
      :value-label="optionValueLabel"
      :quantity-format="quantityText"
      :money-format="moneyText"
      :validation-text="validationText"
      :message-text="messageText"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { productFormulaApi } from '@/api/product-formula/formula'
import { productUnitApi } from '@/api/product-capability/base'
import FormulaSimulationHeader from './components/FormulaSimulationHeader.vue'
import FormulaSimulationOrderSheet from './components/FormulaSimulationOrderSheet.vue'
import FormulaSimulationResultPanel from './components/FormulaSimulationResultPanel.vue'
import { useFormulaSimulationSelection } from './composables/useFormulaSimulationSelection'
import { useFormulaSimulationRestrictions } from './composables/useFormulaSimulationRestrictions'
import type { ProductFormulaOptionVO, ProductFormulaOptionValueVO, ProductFormulaSetupVO, ProductFormulaSimulationBO, ProductFormulaSimulationVO, ProductFormulaVO, ProductUnitVO } from '@/api/product-capability/types'
import { FORMULA_VALIDATION_STATUS, PRODUCT_STATUS_ENABLED, formulaValidationStatusText } from '@/constants/productStatus'

const router = useRouter()
const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const {
  selectedFormulaId,
  currentFormulaId,
  formulaOptions,
  formulaSelecting,
  loadFormulaOptions,
  handleFormulaChange
} = useFormulaSimulationSelection(t)

const loading = ref(false)
const running = ref(false)
const validating = ref(false)
const formula = ref<ProductFormulaVO>({})
const result = ref<ProductFormulaSimulationVO>({})
const setup = ref<ProductFormulaSetupVO>({})
const unitRows = ref<ProductUnitVO[]>([])
const quantity = ref(1)
const room = ref('')
const showValidation = ref(false)
const selectedOptionValues = reactive<Record<string, string>>({})
const form = reactive<ProductFormulaSimulationBO>({
  orderWidth: undefined,
  orderHeight: undefined,
  selectedOptionValues
})

const orderedOptions = computed(() => sortRows((setup.value.options || []).filter((option) => option.status === PRODUCT_STATUS_ENABLED)))
const activeOptions = computed(() => orderedOptions.value.filter((option) => isOptionConditionActive(option)))
const visibleOptions = computed(() => activeOptions.value.filter((option) => option.businessVisibleFlag !== false))
const enabledOptionMaterials = computed(() => sortRows((setup.value.optionMaterials || []).filter((row) => row.status === PRODUCT_STATUS_ENABLED)))
const hiddenOptionCount = computed(() => Math.max(0, orderedOptions.value.length - visibleOptions.value.length))
const simulationStatus = computed(() => result.value.status || formula.value.simulationValidationStatus || FORMULA_VALIDATION_STATUS.NOT_VALIDATED)
const { disabledOptionValues, restrictionMessages, hasBlockingRestriction, blockingRestrictionMessages } = useFormulaSimulationRestrictions({
  form,
  formula,
  options: computed(() => setup.value.options || []),
  optionValues: computed(() => setup.value.optionValues || []),
  restrictions: computed(() => setup.value.restrictions || []),
  materials: computed(() => setup.value.materials || []),
  optionMaterials: enabledOptionMaterials,
  selectedOptionValues
})

watch(activeOptions, () => {
  applyActiveDefaults()
  clearHiddenSelections()
}, { deep: true })

watch(currentFormulaId, async () => { await load() })

function validationText(status?: string) {
  return formulaValidationStatusText(status, t)
}

function messageText(message?: string) {
  return message ? t(message) : '-'
}

function quantityText(value?: number) {
  return value == null ? '-' : Number(value).toFixed(2)
}

function moneyText(value?: number) {
  return value == null ? '-' : Number(value).toFixed(2)
}

function unitLabel(unitCode?: string) {
  const row = unitRows.value.find((unit) => unit.unitCode === unitCode)
  if (!row) return unitCode || '-'
  return String(localeStore.language === 'zh_CN' ? row.unitNameCn : row.unitNameEn || row.unitNameCn || unitCode || '-')
}

async function load() {
  const formulaId = currentFormulaId.value
  if (!formulaId) {
    resetSimulation()
    return
  }
  loading.value = true
  try {
    const [formulaResponse, simulationResponse, setupResponse, unitResponse] = await Promise.all([
      productFormulaApi.get(formulaId),
      productFormulaApi.simulation(formulaId),
      productFormulaApi.getFormulaOptions(formulaId),
      productUnitApi.options?.({ status: PRODUCT_STATUS_ENABLED, pageNum: 1, pageSize: 500 })
    ])
    formula.value = formulaResponse.data || {}
    result.value = simulationResponse.data || {}
    setup.value = setupResponse.data || {}
    unitRows.value = responseRows(unitResponse)
    quantity.value = result.value.orderQuantity || quantity.value
    room.value = result.value.room || room.value
    Object.keys(selectedOptionValues).forEach((optionCode) => delete selectedOptionValues[optionCode])
    Object.assign(selectedOptionValues, result.value.selectedOptionValues || {})
    applyActiveDefaults()
    clearHiddenSelections()
  } finally {
    loading.value = false
  }
}

function resetSimulation() {
  formula.value = {}
  result.value = {}
  setup.value = {}
  quantity.value = 1
  room.value = ''
  showValidation.value = false
  form.orderWidth = undefined
  form.orderHeight = undefined
  Object.keys(selectedOptionValues).forEach((optionCode) => delete selectedOptionValues[optionCode])
}

function optionValuesOf(optionCode?: string) {
  return sortRows((setup.value.optionValues || [])
    .filter((value: ProductFormulaOptionValueVO) => value.status === PRODUCT_STATUS_ENABLED && value.optionCode === optionCode))
}

function optionValueLabel(optionCode?: string, valueCode?: string) {
  if (!optionCode || !valueCode) return valueCode || '-'
  const values = optionValuesOf(optionCode)
  const labels = splitCodes(valueCode).map((code) => {
    const value = values.find((row) => row.valueCode === code)
    return value?.valueNameCn || value?.valueNameEn || value?.valueCode || code
  })
  return labels.join('、') || valueCode
}

function responseRows<T>(response: { data?: T[] } | T[] | undefined): T[] {
  return Array.isArray(response) ? response : response?.data || []
}

function isOptionConditionActive(option: ProductFormulaOptionVO) {
  if (option.visibilityMode !== 'CONDITIONAL') return true
  const conditionOptionCode = option.visibleConditionOptionCode
  return Boolean(conditionOptionCode) && splitCodes(selectedOptionValues[conditionOptionCode as string]).includes(String(option.visibleConditionValueCode || ''))
}

function applyActiveDefaults() {
  activeOptions.value.forEach((option) => {
    const optionCode = option.optionCode
    if (!optionCode || selectedOptionValues[optionCode]) return
    const values = optionValuesOf(optionCode)
    const defaultValue = option.defaultValueCode || values.find((row) => row.defaultFlag)?.valueCode
    if (defaultValue) selectedOptionValues[optionCode] = defaultValue
  })
}

function clearHiddenSelections() {
  const visibleCodes = new Set(activeOptions.value.map((option) => option.optionCode).filter(Boolean))
  Object.keys(selectedOptionValues).forEach((optionCode) => {
    if (!visibleCodes.has(optionCode)) delete selectedOptionValues[optionCode]
  })
}

function sortRows<T extends { sortOrder?: number }>(rows: T[]) {
  return [...rows].sort((left, right) => (left.sortOrder ?? 999999) - (right.sortOrder ?? 999999))
}

function splitCodes(value?: string) {
  return String(value || '').split(',').map((code) => code.trim()).filter(Boolean)
}

function runPayload(): ProductFormulaSimulationBO {
  return {
    orderWidth: form.orderWidth,
    orderHeight: form.orderHeight,
    orderQuantity: quantity.value,
    room: room.value,
    selectedOptionValues: { ...selectedOptionValues }
  }
}

async function run() {
  const formulaId = currentFormulaId.value
  if (!formulaId) {
    ElMessage.warning(t('productCenter.formulaSimulation.selectFormulaHint'))
    return
  }
  if (!validateBeforeSubmit()) return
  running.value = true
  try {
    const response = await productFormulaApi.runSimulation(formulaId, runPayload())
    result.value = response.data || {}
    formula.value.simulationValidationStatus = result.value.status
  } finally {
    running.value = false
  }
}

async function validate() {
  const formulaId = currentFormulaId.value
  if (!formulaId) {
    ElMessage.warning(t('productCenter.formulaSimulation.selectFormulaHint'))
    return
  }
  if (!validateBeforeSubmit()) return
  validating.value = true
  try {
    await productFormulaApi.validateSimulation(formulaId, runPayload())
    ElMessage.success(t('productCenter.formula.validation.pass'))
    await load()
  } finally {
    validating.value = false
  }
}

function validateBeforeSubmit() {
  showValidation.value = true
  if (!form.orderWidth || !form.orderHeight) {
    ElMessage.warning(t('productCenter.formulaSimulation.sizeRequired'))
    return false
  }
  const missingOption = visibleOptions.value.find((option) => option.requiredFlag && !selectedOptionValues[option.optionCode || ''])
  if (missingOption) {
    ElMessage.warning(t('productCenter.formulaSimulation.requiredMissing'))
    return false
  }
  if (hasBlockingRestriction.value) {
    const message = blockingRestrictionMessages.value[0] || 'product.formula.simulationRestrictionHit'
    result.value = { ...runPayload(), formulaId: Number(currentFormulaId.value), status: 'FAIL', message }
    ElMessage.warning(messageText(message))
    return false
  }
  return true
}

onMounted(async () => { await loadFormulaOptions() })
</script>
