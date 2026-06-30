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
      :formula-options="editableFormulaOptions"
      :selected-formula-id="selectedFormulaId"
      :formula-selecting="formulaSelecting"
      :active-section="activeTab"
      :can-operate="Boolean(currentFormulaId)"
      @formula-change="handleFormulaChange"
      @validate="validateSetup"
      @save="saveSetup"
    />

    <el-empty
      v-if="!currentFormulaId"
      class="formula-setup-page__empty"
      :description="t('productCenter.formulaSetup.selectEditableFormulaHint')"
      :image-size="120"
    />

    <template v-else-if="activeTab === 'content'">
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
        :group-sort-map="groupSortMap"
        :material-group-cards="materialGroupCards"
        :material-count="setup.materials.length"
        :unset-usage-count="unsetUsageCount"
        :exception-count="setup.restrictions.length"
        @open-picker="materialPickerOpen = true"
        @open-usage="openUsage"
        @open-batch-usage="openBatchUsage"
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
      :usage-rows="usageRows"
      :usage-rules="setup.usageRules"
      :options="setup.options"
      :option-values="setup.optionValues"
      :unit-options="unitOptions"
      @select-usage-row="selectUsageRow"
    />
  </div>
</template>

<script setup lang="ts" name="ProductFormulaSetupPage">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
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
  FORMULA_STATUS,
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
const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const props = defineProps<{
  setupSection?: 'content' | 'options'
}>()

const routeFormulaId = computed(() => {
  const rawId = String(route.params.id || '')
  return /^\d+$/.test(rawId) ? rawId : ''
})
const selectedFormulaId = ref('')
const currentFormulaId = computed(() => selectedFormulaId.value)
function routeSetupSection() {
  if (props.setupSection) return props.setupSection
  return route.meta.setupSection === 'options' ? 'options' : 'content'
}

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
const editableFormulaOptions = computed(() => editableFormulas.value.map((row) => ({
  value: String(row.formulaId || ''),
  label: compactFormulaLabel(row)
})).filter((item) => item.value))
const groupOptions = computed<ProductOption[]>(() => groupRows.value.map((row) => ({ value: row.groupCode || '', label: labelOf(row, 'groupCode', 'groupNameCn', 'groupNameEn') })).filter((item) => item.value))
const groupSortMap = computed<Record<string, number>>(() => {
  const result: Record<string, number> = {}
  groupRows.value.forEach((row, index) => {
    if (!row.groupCode) return
    result[row.groupCode] = index + 1
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
const unsetUsageCount = computed(() => setup.materials.filter((row) => isUsageUnset(row)).length)
const selectedValues = computed(() => setup.optionValues.filter((row) => row.optionCode === selectedOptionCode.value))
const selectedOptionMaterials = computed(() => setup.optionMaterials.filter((row) => row.optionCode === selectedOptionCode.value))

onMounted(async () => {
  await Promise.all([loadBaseOptions(), loadEditableFormulas()])
  await loadSetup()
})

watch([() => route.meta.setupSection, () => props.setupSection], async () => {
  activeTab.value = routeSetupSection()
  await loadSetup()
})

watch(currentFormulaId, async () => {
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
    const routeId = routeFormulaId.value
    if (routeId && formulaMap.has(routeId)) {
      selectedFormulaId.value = routeId
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
  if (!currentFormulaId.value) {
    resetSetup()
    return
  }
  loading.value = true
  try {
    const response = activeTab.value === 'options'
      ? await productFormulaApi.getFormulaOptions(currentFormulaId.value)
      : await productFormulaApi.materials(currentFormulaId.value)
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
  if (!currentFormulaId.value) {
    ElMessage.warning(t('productCenter.formulaSetup.selectEditableFormulaHint'))
    return
  }
  saving.value = true
  try {
    if (activeTab.value === 'options') {
      await productFormulaApi.saveOptions(currentFormulaId.value, buildPayload())
    } else {
      await productFormulaApi.saveMaterials(currentFormulaId.value, buildPayload())
    }
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
  validating.value = true
  try {
    if (activeTab.value === 'options') {
      await productFormulaApi.validateOptions(currentFormulaId.value)
    } else {
      await productFormulaApi.validateMaterials(currentFormulaId.value)
    }
    ElMessage.success(t('productCenter.formula.validation.pass'))
    await loadSetup()
  } finally {
    validating.value = false
  }
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

function handleFormulaChange(value: string) {
  selectedFormulaId.value = value
}

function resetSetup() {
  formula.value = {}
  setup.materials = []
  setup.options = []
  setup.optionValues = []
  setup.optionMaterials = []
  setup.restrictions = []
  setup.usageRules = []
  selectedOptionCode.value = ''
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
    sortOrder: nextMaterialSortOrder(material)
  }
}

function nextMaterialSortOrder(material: ProductMaterialVO) {
  const groupRank = Math.max(1, groupSortMap.value[material.attributeGroupCode || ''] || groupRows.value.length + 1)
  const base = groupRank * 100
  const used = new Set(setup.materials
    .filter((row) => row.attributeGroupCode === material.attributeGroupCode)
    .map((row) => Number(row.sortOrder || 0))
    .filter((value) => value >= base && value < base + 100))
  let next = base + used.size + 1
  while (used.has(next)) next += 1
  return next
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
  usageRows.value = [row]
  usageDrawerOpen.value = true
}

function openBatchUsage(rows: ProductFormulaMaterialVO[]) {
  usageRows.value = rows
  usageRow.value = rows[0] || null
  usageDrawerOpen.value = true
}

function selectUsageRow(row: ProductFormulaMaterialVO) {
  usageRow.value = row
}

function syncOptionMaterial(row: ProductFormulaOptionMaterialVO) {
  const material = setup.materials.find((item) => item.materialCode === row.materialCode)
  row.formulaMaterialId = material?.formulaMaterialId
  row.materialId = material?.materialId
  row.materialNameCn = material?.materialNameCn
}

function usageSummary(row: ProductFormulaMaterialVO): string {
  const activeRules = usageRulesFor(row).filter((rule) => rule.status !== PRODUCT_STATUS_DISABLED)
  if (activeRules.length > 0) {
    const formulaRules = activeRules.filter((rule) => rule.usageMode === 'FORMULA')
    const fixedRule = activeRules.find((rule) => rule.usageMode === 'FIXED')
    if (formulaRules.length > 0) {
      const lines = formulaRules
        .slice()
        .sort((left, right) => Number(left.sortOrder || 0) - Number(right.sortOrder || 0))
        .map(usageRuleSummary)
      return lines.join('\n')
    }
    if (fixedRule) {
      return `${textOf(t('productCenter.formulaSetup.usageFixedShort'))} ${formatUsageNumber(fixedRule.fixedUsageQty)}`
    }
  }
  if (row.usageMode === 'FIXED' && row.fixedUsageQty !== undefined && row.fixedUsageQty !== null) {
    return `${textOf(t('productCenter.formulaSetup.usageFixedShort'))} ${formatUsageNumber(row.fixedUsageQty)}`
  }
  if (row.usageMode === 'FORMULA' && hasAnyUsageFormula(row)) {
    return usageRuleSummary(row)
  }
  return textOf(t('productCenter.formulaSetup.usageNotSet'))
}

function usageRuleSummary(rule: ProductFormulaUsageRuleVO | ProductFormulaMaterialVO): string {
  const condition = usageConditionText(rule)
  const formulas = usageFormulaParts(rule)
  return formulas.length ? `${condition}：\n${formulas.join('\n')}` : condition
}

function usageConditionText(rule: ProductFormulaUsageRuleVO | ProductFormulaMaterialVO): string {
  if ('defaultRuleFlag' in rule && rule.defaultRuleFlag) return textOf(t('productCenter.formulaSetup.defaultUsageRule'))
  if ('conditionText' in rule && rule.conditionText) return textOf(rule.conditionText)
  if ('conditionOptionNameCn' in rule && (rule.conditionOptionNameCn || rule.conditionOptionCode)) {
    const optionName = rule.conditionOptionNameCn || rule.conditionOptionCode
    const valueName = rule.conditionValueNameCn || rule.conditionValueCode || '-'
    return `${optionName} = ${valueName}`
  }
  if ('conditionExpression' in rule && rule.conditionExpression) return textOf(rule.conditionExpression)
  return textOf(t('productCenter.formulaSetup.defaultUsageRule'))
}

function usageFormulaParts(rule: ProductFormulaUsageRuleVO | ProductFormulaMaterialVO): string[] {
  const parts: string[] = []
  appendUsageFormulaPart(parts, textOf(t('productCenter.formulaSetup.lengthFormula')), summaryFormulaValue(rule.lengthFormulaText || rule.lengthFormula))
  appendUsageFormulaPart(parts, textOf(t('productCenter.formulaSetup.widthFormula')), summaryFormulaValue(rule.widthFormulaText || rule.widthFormula))
  appendUsageFormulaPart(parts, textOf(t('productCenter.formulaSetup.heightFormula')), summaryFormulaValue(rule.heightFormulaText || rule.heightFormula))
  appendUsageFormulaPart(parts, textOf(t('productCenter.formulaSetup.weightFormula')), summaryFormulaValue(rule.weightFormulaText || rule.weightFormula))
  const quantityFormula = summaryFormulaValue(rule.usageFormulaText || rule.usageFormula)
  if (quantityFormula && !(parts.length > 0 && isDefaultQuantityFormula(quantityFormula))) {
    appendUsageFormulaPart(parts, textOf(t('productCenter.formulaSetup.quantityFormula')), quantityFormula)
  }
  if (!parts.length && rule.fixedUsageQty !== undefined && rule.fixedUsageQty !== null) {
    parts.push(`${textOf(t('productCenter.formulaSetup.usageFixedShort'))} ${formatUsageNumber(rule.fixedUsageQty)}`)
  }
  return parts
}

function appendUsageFormulaPart(parts: string[], label: string, value?: string) {
  if (!value) return
  parts.push(`${label} ${value}`)
}

function summaryFormulaValue(value: unknown): string {
  const text = textOf(value).trim()
  if (!text) return ''
  const numeric = Number(text)
  return Number.isFinite(numeric) && /^-?\d+(\.\d+)?$/.test(text) ? numeric.toFixed(2) : text
}

function isDefaultQuantityFormula(value: string) {
  return Number(value) === 1 && /^1(\.0+)?$/.test(value)
}

function textOf(value: unknown): string {
  return value === undefined || value === null ? '' : String(value)
}

function isUsageUnset(row: ProductFormulaMaterialVO) {
  const activeRules = usageRulesFor(row).filter((rule) => rule.status !== PRODUCT_STATUS_DISABLED)
  if (activeRules.length > 0) {
    const formulaRules = activeRules.filter((rule) => rule.usageMode === 'FORMULA')
    if (formulaRules.length > 0) return !formulaRules.some((rule) => rule.defaultRuleFlag)
    return activeRules.some((rule) => rule.usageMode === 'FIXED' && (rule.fixedUsageQty === undefined || rule.fixedUsageQty === null))
  }
  if (row.usageMode === 'FIXED') return row.fixedUsageQty === undefined || row.fixedUsageQty === null
  if (row.usageMode === 'FORMULA') return !hasAnyUsageFormula(row)
  return true
}

function usageRulesFor(row: ProductFormulaMaterialVO) {
  return setup.usageRules.filter((rule) => rule.materialCode === row.materialCode)
}

function hasAnyUsageFormula(row: ProductFormulaUsageRuleVO | ProductFormulaMaterialVO) {
  return Boolean(
    row.lengthFormula ||
    row.widthFormula ||
    row.heightFormula ||
    row.weightFormula ||
    row.usageFormula
  )
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

function compactFormulaLabel(row: ProductFormulaVO) {
  const code = row.formulaCode || '-'
  const name = row.formulaName || '-'
  const status = statusText(row.status)
  return `${code} ${name}（${status}）`
}

function materialLabel(row: ProductMaterialVO | ProductFormulaMaterialVO) {
  return `${row.materialCode || ''} ${row.materialNameCn || ''}`.trim()
}

function unitLabel(unitCode?: string) {
  if (!unitCode) return '-'
  const unit = unitRows.value.find((row) => row.unitCode === unitCode)
  if (!unit) return unitCode
  if (localeStore.language === 'zh_CN') return unit.unitNameCn || unit.unitCode || unitCode
  return unit.unitNameEn || unit.unitNameCn || unit.unitCode || unitCode
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
</script>

<style scoped>
.formula-setup-page {
  background: #f5f7fb;
}

.formula-setup-page__empty {
  min-height: 360px;
  margin-top: 16px;
  background: #fff;
  border: 1px solid #e6ebf2;
  border-radius: 8px;
}
</style>
