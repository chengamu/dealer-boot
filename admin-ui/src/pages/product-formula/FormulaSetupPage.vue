<template>
  <div class="app-container formula-setup-page">
    <FormulaSetupHeader
      :formula="formula"
      :draft-version-label="draftVersionLabel"
      :saving="saving"
      :validating="validating"
      :format-number="formatNumber"
      :format-minute="formatMinute"
      :status-text="statusText"
      :validation-text="validationText"
      :status-tag-type="statusTagType"
      :validation-tag-type="validationTagType"
      :active-tab="activeTab"
      @back="goBack"
      @validate="validateSetup"
      @save="saveSetup"
      @submit-review="submitReview"
      @tab-change="switchSection"
    />

    <template v-if="activeTab === 'content'">
      <FormulaSetupSummary
        :material-group-cards="materialGroupCards"
        :material-count="setup.materials.length"
        :unset-usage-count="unsetUsageCount"
        :option-count="setup.options.length"
        :rule-count="setup.optionMaterials.length"
        :exception-count="setup.restrictions.length"
      />

      <FormulaMaterialPoolTab
        :loading="loading"
        :materials="setup.materials"
        :usage-summary="usageSummary"
        :usage-unset="isUsageUnset"
        :unit-label="unitLabel"
        :material-count="setup.materials.length"
        :unset-usage-count="unsetUsageCount"
        :exception-count="setup.restrictions.length"
        @open-picker="materialPickerOpen = true"
        @open-usage="openUsage"
        @remove-material="removeMaterial"
        @remove-materials="removeMaterials"
      />
    </template>

    <FormulaBusinessOptionsTab
      v-else
      :options="setup.options"
      :option-values="selectedValues"
      :all-option-values="setup.optionValues"
      :option-materials="selectedOptionMaterials"
      :all-option-materials="setup.optionMaterials"
      :restrictions="setup.restrictions"
      :selected-option-code="selectedOptionCode"
      :materials="setup.materials"
      :material-label="materialLabel"
      @add-option="addOptionRow"
      @option-change="handleOptionChange"
      @move-option="moveOption"
      @remove-option="removeOption"
      @add-option-value="addOptionValueRow"
      @remove-option-value="removeSelectedValue"
      @add-option-material="addOptionMaterialRow"
      @sync-option-material="syncOptionMaterial"
      @remove-option-material="removeSelectedOptionMaterial"
      @add-restriction="addRestrictionRow"
      @remove-restriction="removeRow(setup.restrictions, $event)"
    />

    <FormulaMaterialPickerDialog
      v-model="materialPickerOpen"
      :material-rows="materialRows"
      :group-options="groupOptions"
      :material-type-rows="materialTypeRows"
      :unit-label="unitLabel"
      @confirm="appendSelectedMaterials"
    />

    <FormulaUsageDrawer
      v-model="usageDrawerOpen"
      :usage-row="usageRow"
      :usage-rules="setup.usageRules"
      :options="setup.options"
      :option-values="setup.optionValues"
      :unit-options="unitOptions"
    />
  </div>
</template>

<script setup lang="ts" name="ProductFormulaSetupPage">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { productMaterialApi } from '@/api/product-capability/material'
import { productMaterialTypeApi, productMaterialTypeGroupApi, productUnitApi } from '@/api/product-capability/base'
import { productFormulaApi } from '@/api/product-formula/formula'
import FormulaBusinessOptionsTab from './components/FormulaBusinessOptionsTab.vue'
import FormulaMaterialPoolTab from './components/FormulaMaterialPoolTab.vue'
import FormulaMaterialPickerDialog from './components/FormulaMaterialPickerDialog.vue'
import FormulaSetupHeader from './components/FormulaSetupHeader.vue'
import FormulaSetupSummary from './components/FormulaSetupSummary.vue'
import FormulaUsageDrawer from './components/FormulaUsageDrawer.vue'
import { formatUsageNumber } from './utils/formulaExpression'
import {
  PRODUCT_STATUS_DISABLED,
  PRODUCT_STATUS_ENABLED,
  formulaStatusTagType,
  formulaStatusText,
  formulaValidationStatusText,
  formulaValidationTagType
} from '@/constants/productStatus'
import { localizedRecordLabel } from '@/utils/productLabels'
import type {
  ProductFormulaMaterialVO,
  ProductFormulaOptionMaterialVO,
  ProductFormulaOptionVO,
  ProductFormulaOptionValueVO,
  ProductFormulaRestrictionVO,
  ProductFormulaSetupVO,
  ProductFormulaUsageRuleVO,
  ProductFormulaVO,
  ProductMaterialTypeGroupVO,
  ProductMaterialTypeVO,
  ProductMaterialVO,
  ProductOption,
  ProductRecord,
  ProductUnitVO
} from '@/api/product-capability/types'

interface SetupState {
  materials: ProductFormulaMaterialVO[]
  options: ProductFormulaOptionVO[]
  optionValues: ProductFormulaOptionValueVO[]
  optionMaterials: ProductFormulaOptionMaterialVO[]
  restrictions: ProductFormulaRestrictionVO[]
  usageRules: ProductFormulaUsageRuleVO[]
}

const route = useRoute()
const router = useRouter()
const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const props = defineProps<{
  setupSection?: 'content' | 'options'
}>()

const formulaId = computed(() => String(route.params.id || ''))
function routeSetupSection() {
  if (props.setupSection) return props.setupSection
  return route.meta.setupSection === 'options' ? 'options' : 'content'
}

const activeTab = ref<'content' | 'options'>(routeSetupSection())
const loading = ref(false)
const saving = ref(false)
const validating = ref(false)
const materialPickerOpen = ref(false)
const usageDrawerOpen = ref(false)
const usageRow = ref<ProductFormulaMaterialVO | null>(null)
const selectedOptionCode = ref('')
const materialRows = ref<ProductMaterialVO[]>([])
const groupRows = ref<ProductMaterialTypeGroupVO[]>([])
const materialTypeRows = ref<ProductMaterialTypeVO[]>([])
const unitRows = ref<ProductUnitVO[]>([])

const setup = reactive<SetupState>({
  materials: [],
  options: [],
  optionValues: [],
  optionMaterials: [],
  restrictions: [],
  usageRules: []
})
const formula = ref<ProductFormulaVO>({})

const draftVersionLabel = computed(() => `V${formula.value.draftVersionNo || 1} ${t('productCenter.formula.status.draft')}`)
const groupOptions = computed<ProductOption[]>(() => groupRows.value.map((row) => ({ value: row.groupCode || '', label: labelOf(row, 'groupCode', 'groupNameCn', 'groupNameEn') })).filter((item) => item.value))
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
const unsetUsageCount = computed(() => setup.materials.filter((row) => isUsageUnset(row)).length)
const selectedValues = computed(() => setup.optionValues.filter((row) => row.optionCode === selectedOptionCode.value))
const selectedOptionMaterials = computed(() => setup.optionMaterials.filter((row) => row.optionCode === selectedOptionCode.value))

onMounted(async () => {
  await Promise.all([loadBaseOptions(), loadSetup()])
})

watch([() => route.meta.setupSection, () => props.setupSection, formulaId], async () => {
  activeTab.value = routeSetupSection()
  await loadSetup()
})

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

async function loadSetup() {
  if (!formulaId.value) return
  loading.value = true
  try {
    const response = activeTab.value === 'options'
      ? await productFormulaApi.getFormulaOptions(formulaId.value)
      : await productFormulaApi.materials(formulaId.value)
    formula.value = response.data?.formula || {}
    setup.materials = response.data?.materials || []
    setup.options = (response.data?.options || []).map((row) => ({
      ...row,
      visibilityMode: row.visibilityMode || 'ALWAYS'
    }))
    setup.optionValues = response.data?.optionValues || []
    setup.optionMaterials = response.data?.optionMaterials || []
    setup.restrictions = response.data?.restrictions || []
    setup.usageRules = response.data?.usageRules || []
    selectedOptionCode.value = setup.options[0]?.optionCode || ''
  } finally {
    loading.value = false
  }
}

async function saveSetup() {
  saving.value = true
  try {
    if (activeTab.value === 'options') {
      await productFormulaApi.saveOptions(formulaId.value, buildPayload())
    } else {
      await productFormulaApi.saveMaterials(formulaId.value, buildPayload())
    }
    ElMessage.success(t('common.success'))
    await loadSetup()
  } finally {
    saving.value = false
  }
}

async function validateSetup() {
  validating.value = true
  try {
    if (activeTab.value === 'options') {
      await productFormulaApi.validateOptions(formulaId.value)
    } else {
      await productFormulaApi.validateMaterials(formulaId.value)
    }
    ElMessage.success(t('productCenter.formula.validation.pass'))
    await loadSetup()
  } finally {
    validating.value = false
  }
}

async function submitReview() {
  await ElMessageBox.confirm(t('productCenter.formula.confirm.submitReview'), t('common.prompt'), { type: 'warning' })
  await productFormulaApi.submitReview(formulaId.value)
  ElMessage.success(t('common.success'))
  await loadSetup()
}

async function switchSection(tab: 'content' | 'options') {
  const target = tab === 'options' ? 'options' : 'materials'
  await router.push(`/product-formula/formulas/${formulaId.value}/${target}`)
}

function buildPayload(): ProductFormulaSetupVO {
  return {
    materials: setup.materials,
    options: setup.options,
    optionValues: setup.optionValues,
    optionMaterials: setup.optionMaterials,
    restrictions: setup.restrictions,
    usageRules: setup.usageRules
  }
}

function appendSelectedMaterials(selectedMaterials: ProductMaterialVO[]) {
  const existing = new Set(setup.materials.map((row) => row.materialCode))
  selectedMaterials.forEach((material) => {
    if (!material.materialCode || existing.has(material.materialCode)) return
    setup.materials.push(materialToFormulaMaterial(material))
    existing.add(material.materialCode)
  })
  materialPickerOpen.value = false
}

function materialToFormulaMaterial(material: ProductMaterialVO): ProductFormulaMaterialVO {
  return {
    lineNo: setup.materials.length + 1,
    materialId: material.materialId,
    materialCode: material.materialCode,
    materialNameCn: material.materialNameCn,
    specModelText: material.specModelText,
    attributeGroupId: material.attributeGroupId,
    attributeGroupCode: material.attributeGroupCode,
    attributeGroupNameCn: material.attributeGroupNameCn,
    materialTypeId: material.materialTypeId,
    materialTypeCode: material.materialTypeCode,
    materialTypeNameCn: material.materialTypeNameCn,
    unitCode: material.unitCode,
    calculationUnitCode: material.unitCode,
    usageMode: 'FIXED',
    fixedUsageQty: 1,
    lossRate: 0,
    defaultFlag: false,
    requiredFlag: true,
    status: PRODUCT_STATUS_ENABLED,
    sortOrder: (setup.materials.length + 1) * 10
  }
}

function addOptionRow(parent?: {
  optionCode?: string
  optionNameCn?: string
  valueCode?: string
  valueNameCn?: string
}) {
  const next = setup.options.length + 1
  const code = `OPTION_${next}`
  setup.options.push({
    optionCode: code,
    optionNameCn: parent ? `${parent.valueNameCn || parent.valueCode || ''}${t('productCenter.formulaSetup.childOption')}` : `${t('productCenter.formulaSetup.optionName')} ${next}`,
    sourceType: 'MANUAL',
    selectionMode: 'SINGLE',
    requiredFlag: true,
    businessVisibleFlag: true,
    visibilityMode: parent ? 'CONDITIONAL' : 'ALWAYS',
    visibleConditionOptionCode: parent?.optionCode || '',
    visibleConditionOptionNameCn: parent?.optionNameCn || '',
    visibleConditionValueCode: parent?.valueCode || '',
    visibleConditionValueNameCn: parent?.valueNameCn || '',
    status: PRODUCT_STATUS_ENABLED,
    sortOrder: next * 10
  })
  selectedOptionCode.value = code
}

function addOptionValueRow() {
  if (!selectedOptionCode.value) return
  const next = selectedValues.value.length + 1
  setup.optionValues.push({
    optionCode: selectedOptionCode.value,
    valueCode: `VALUE_${next}`,
    valueNameCn: '',
    defaultFlag: false,
    status: PRODUCT_STATUS_ENABLED,
    sortOrder: setup.optionValues.length * 10 + 10
  })
}

function addOptionMaterialRow(valueCode?: string) {
  if (!selectedOptionCode.value) return
  setup.optionMaterials.push({
    optionCode: selectedOptionCode.value,
    valueCode: valueCode || selectedValues.value[0]?.valueCode,
    requiredFlag: true,
    defaultFlag: true,
    status: PRODUCT_STATUS_ENABLED,
    sortOrder: setup.optionMaterials.length * 10 + 10
  })
}

function addRestrictionRow() {
  setup.restrictions.push({
    restrictionName: '',
    targetOptionCode: selectedOptionCode.value || setup.options[0]?.optionCode,
    conditionType: 'WIDTH',
    conditionOperator: 'GT',
    actionType: 'DISABLE',
    status: PRODUCT_STATUS_ENABLED,
    sortOrder: setup.restrictions.length * 10 + 10
  })
}

function removeOption(index: number) {
  const optionCode = setup.options[index]?.optionCode
  removeRow(setup.options, index)
  setup.optionValues = setup.optionValues.filter((row) => row.optionCode !== optionCode)
  setup.optionMaterials = setup.optionMaterials.filter((row) => row.optionCode !== optionCode)
  setup.options.forEach((row) => {
    if (row.visibleConditionOptionCode === optionCode) clearVisibilityCondition(row)
  })
  if (selectedOptionCode.value === optionCode) {
    selectedOptionCode.value = setup.options[0]?.optionCode || ''
  }
}

function removeSelectedValue(index: number) {
  const row = selectedValues.value[index]
  if (!row) return
  const realIndex = setup.optionValues.indexOf(row)
  removeRow(setup.optionValues, realIndex)
  setup.optionMaterials = setup.optionMaterials.filter((item) => !(item.optionCode === row.optionCode && item.valueCode === row.valueCode))
  setup.options.forEach((option) => {
    if (option.visibleConditionOptionCode === row.optionCode && option.visibleConditionValueCode === row.valueCode) {
      clearVisibilityCondition(option)
    }
  })
}

function removeSelectedOptionMaterial(index: number) {
  const row = selectedOptionMaterials.value[index]
  if (!row) return
  removeRow(setup.optionMaterials, setup.optionMaterials.indexOf(row))
}

function removeMaterial(index: number) {
  const material = setup.materials[index]
  if (isMaterialReferenced(material?.materialCode)) {
    ElMessage.warning(t('productCenter.formulaSetup.materialReferencedRemoveDenied'))
    return
  }
  removeRow(setup.materials, index)
}

function removeRow<T>(rows: T[], index: number) {
  if (index >= 0) rows.splice(index, 1)
}

function clearVisibilityCondition(row: ProductFormulaOptionVO) {
  row.visibilityMode = 'ALWAYS'
  row.visibleConditionOptionCode = ''
  row.visibleConditionOptionNameCn = ''
  row.visibleConditionValueCode = ''
  row.visibleConditionValueNameCn = ''
}

function removeMaterials(rows: ProductFormulaMaterialVO[]) {
  const codes = new Set(rows.map((row) => row.materialCode).filter(Boolean))
  if ([...codes].some((code) => isMaterialReferenced(code))) {
    ElMessage.warning(t('productCenter.formulaSetup.materialReferencedRemoveDenied'))
    return
  }
  setup.materials = setup.materials.filter((row) => !codes.has(row.materialCode))
}

function isMaterialReferenced(materialCode?: string) {
  if (!materialCode) return false
  return setup.usageRules.some((row) => row.materialCode === materialCode)
    || setup.optionMaterials.some((row) => row.materialCode === materialCode)
}

function handleOptionChange(row?: ProductFormulaOptionVO) {
  if (row?.optionCode) selectedOptionCode.value = row.optionCode
}

function moveOption(optionCode: string, direction: 'UP' | 'DOWN') {
  const currentIndex = setup.options.findIndex((row) => row.optionCode === optionCode)
  const targetIndex = direction === 'UP' ? currentIndex - 1 : currentIndex + 1
  if (currentIndex < 0 || targetIndex < 0 || targetIndex >= setup.options.length) return
  const [row] = setup.options.splice(currentIndex, 1)
  setup.options.splice(targetIndex, 0, row)
  setup.options.forEach((item, index) => { item.sortOrder = (index + 1) * 10 })
}

function openUsage(row: ProductFormulaMaterialVO) {
  usageRow.value = row
  usageDrawerOpen.value = true
}

function syncOptionMaterial(row: ProductFormulaOptionMaterialVO) {
  const material = setup.materials.find((item) => item.materialCode === row.materialCode)
  row.formulaMaterialId = material?.formulaMaterialId
  row.materialId = material?.materialId
  row.materialNameCn = material?.materialNameCn
}

function usageSummary(row: ProductFormulaMaterialVO) {
  const activeRules = usageRulesFor(row).filter((rule) => rule.status !== PRODUCT_STATUS_DISABLED)
  if (activeRules.length > 0) {
    const hasDefault = activeRules.some((rule) => rule.defaultRuleFlag)
    const formulaRules = activeRules.filter((rule) => rule.usageMode === 'FORMULA')
    const fixedRule = activeRules.find((rule) => rule.usageMode === 'FIXED')
    if (formulaRules.length > 0) {
      const fabricRuleCount = formulaRules.filter((rule) => rule.conditionOptionCode === 'FABRIC' || String(rule.conditionKey || '').includes('FABRIC')).length
      const label = fabricRuleCount > 0
        ? t('productCenter.formulaSetup.fabricUsageRuleCount').replace('{count}', String(formulaRules.length))
        : t('productCenter.formulaSetup.formulaUsageRuleCount').replace('{count}', String(formulaRules.length))
      return hasDefault ? label : `${label} / ${t('productCenter.formulaSetup.defaultRuleMissing')}`
    }
    if (fixedRule) {
      return `${t('productCenter.formulaSetup.usageFixedShort')} ${formatUsageNumber(fixedRule.fixedUsageQty)}`
    }
  }
  if (row.usageMode === 'FIXED' && row.fixedUsageQty !== undefined && row.fixedUsageQty !== null) {
    return `${t('productCenter.formulaSetup.usageFixedShort')} ${formatUsageNumber(row.fixedUsageQty)}`
  }
  if (row.usageMode === 'FORMULA' && row.usageFormula) {
    return t('productCenter.formulaSetup.formulaUsageRuleCount').replace('{count}', '1')
  }
  return t('productCenter.formulaSetup.usageNotSet')
}

function isUsageUnset(row: ProductFormulaMaterialVO) {
  const activeRules = usageRulesFor(row).filter((rule) => rule.status !== PRODUCT_STATUS_DISABLED)
  if (activeRules.length > 0) {
    const formulaRules = activeRules.filter((rule) => rule.usageMode === 'FORMULA')
    if (formulaRules.length > 0) return !formulaRules.some((rule) => rule.defaultRuleFlag)
    return activeRules.some((rule) => rule.usageMode === 'FIXED' && (rule.fixedUsageQty === undefined || rule.fixedUsageQty === null))
  }
  if (row.usageMode === 'FIXED') return row.fixedUsageQty === undefined || row.fixedUsageQty === null
  if (row.usageMode === 'FORMULA') return !row.usageFormula
  return true
}

function usageRulesFor(row: ProductFormulaMaterialVO) {
  return setup.usageRules.filter((rule) => rule.materialCode === row.materialCode)
}

function statusText(status?: string) {
  return formulaStatusText(status, t)
}

function validationText(status?: string) {
  return formulaValidationStatusText(status, t)
}

function statusTagType(status?: string) {
  return formulaStatusTagType(status)
}

function validationTagType(status?: string) {
  return formulaValidationTagType(status)
}

function materialLabel(row: ProductMaterialVO | ProductFormulaMaterialVO) {
  return `${row.materialCode || ''} ${row.materialNameCn || ''}`.trim()
}

function unitLabel(unitCode?: string) {
  if (!unitCode) return '-'
  const unit = unitRows.value.find((row) => row.unitCode === unitCode)
  return unit ? labelOf(unit, 'unitCode', 'unitNameCn', 'unitNameEn') : unitCode
}

function labelOf(row: ProductRecord, codeKey: string, cnKey: string, enKey?: string) {
  return localizedRecordLabel(row, localeStore.language, codeKey, cnKey, enKey)
}

function responseRows<T>(response: { data?: T[] } | T[] | undefined): T[] {
  return Array.isArray(response) ? response : response?.data || []
}

function formatNumber(value?: number | string) {
  const numericValue = Number(value)
  return Number.isFinite(numericValue) ? numericValue.toFixed(2) : '-'
}

function formatMinute(value?: string) {
  return value ? value.replace('T', ' ').slice(0, 16) : '-'
}

function goBack() {
  router.push('/product-formula/formulas')
}
</script>

<style scoped>
.formula-setup-page {
  background: #f5f7fb;
}
</style>
