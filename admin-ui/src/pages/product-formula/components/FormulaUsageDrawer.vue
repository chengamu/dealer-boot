<template>
  <AdminDrawer
    :model-value="modelValue"
    :title="t('productCenter.formulaSetup.usageSetting')"
    size="1640px"
    append-to-body
    variant="wide"
    class="formula-usage-drawer"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    :before-close="handleDrawerBeforeClose"
    @update:model-value="$emit('update:modelValue', $event)"
    @closed="handleDrawerClosed"
  >
    <div v-if="usageRow" class="usage-editor">
      <div class="usage-editor__summary">
        <div>
          <span>{{ t('productCenter.formulaSetup.materialType') }}</span>
          <strong>{{ usageRow.materialTypeNameCn || usageRow.materialTypeCode || '-' }}</strong>
        </div>
        <div>
          <span>{{ t('productCenter.formulaSetup.materialCode') }}</span>
          <strong>{{ usageRow.materialCode || '-' }}</strong>
        </div>
        <div class="usage-editor__material">
          <span>{{ t('productCenter.formulaSetup.materialName') }}</span>
          <strong>{{ usageRow.materialNameCn || '-' }}</strong>
        </div>
        <div class="usage-editor__spec">
          <span>{{ t('productCenter.formulaSetup.specModel') }}</span>
          <strong>{{ usageRow.specModelText || '-' }}</strong>
        </div>
        <div>
          <span>{{ t('productCenter.formulaSetup.unit') }}</span>
          <strong>{{ unitDisplay(usageRow.unitCode) }}</strong>
        </div>
      </div>

      <div class="usage-mode-card">
        <div>
          <span class="usage-mode-card__label">{{ t('productCenter.formulaSetup.usageMode') }}</span>
          <p>{{ t('productCenter.formulaSetup.usageModeHint') }}</p>
        </div>
        <el-radio-group class="usage-mode-card__switch" :model-value="usageRow.usageMode || 'FIXED'" @change="handleUsageModeChange">
          <el-radio label="FIXED" border>{{ t('productCenter.formulaSetup.usageFixedValue') }}</el-radio>
          <el-radio label="FORMULA" border>{{ t('productCenter.formulaSetup.usageFormulaRules') }}</el-radio>
        </el-radio-group>
      </div>

      <FormulaUsageFixedPanel v-if="usageRow.usageMode !== 'FORMULA'" :usage-row="usageRow" @change="syncFixedRuleFromRow" />

      <template v-else>
      <div class="usage-editor__toolbar">
        <div>
          <h3>{{ t('productCenter.formulaSetup.conditionalUsageRules') }}</h3>
          <p>{{ t('productCenter.formulaSetup.conditionalUsageRulesHint') }}</p>
        </div>
        <div class="usage-editor__actions">
          <el-button plain :icon="MagicStick" @click="generateFabricRules">{{ t('productCenter.formulaSetup.generateUsageFromFabric') }}</el-button>
          <el-button type="primary" plain :icon="Plus" @click="addConditionalUsageRule">{{ t('productCenter.formulaSetup.addUsageRule') }}</el-button>
          <el-button plain :icon="CopyDocument" :disabled="!selectedRule" @click="copySelectedRule">{{ t('common.copy') }}</el-button>
          <el-button type="danger" plain :icon="Delete" :disabled="!selectedRule" @click="removeSelectedRule">{{ t('common.delete') }}</el-button>
        </div>
      </div>

      <el-table
        :data="currentRules"
        border
        height="430"
        row-key="sortOrder"
        class="usage-rule-table"
        highlight-current-row
        @current-change="selectedRule = $event"
      >
        <el-table-column type="index" :label="t('common.index')" width="54" align="center" />
        <el-table-column :label="t('productCenter.formulaSetup.useCondition')" min-width="300">
          <template #default="{ row }">
            <div class="condition-cell">
              <el-select v-model="row.conditionType" @change="handleConditionTypeChange(row)">
                <el-option value="DEFAULT" :label="t('productCenter.formulaSetup.defaultUsageRule')" />
                <el-option value="OPTION_VALUE" :label="t('productCenter.formulaSetup.conditionOptionValue')" />
                <el-option value="EXPRESSION" :label="t('productCenter.formulaSetup.conditionExpression')" />
              </el-select>
              <div v-if="row.conditionType === 'OPTION_VALUE'" class="condition-cell__selects">
                <el-select v-model="row.conditionOptionCode" clearable filterable @change="handleConditionOptionChange(row)">
                  <el-option v-for="option in options" :key="option.optionCode" :label="optionLabel(option)" :value="option.optionCode" />
                </el-select>
                <el-select v-model="row.conditionValueCode" clearable filterable @change="syncConditionValueName(row)">
                  <el-option v-for="value in optionValuesOf(row.conditionOptionCode)" :key="value.valueCode" :label="valueLabel(value)" :value="value.valueCode" />
                </el-select>
              </div>
              <div v-else-if="row.conditionType === 'EXPRESSION'" class="formula-cell">
                <el-input
                  v-model="row.conditionText"
                  clearable
                  :placeholder="t('productCenter.formulaSetup.conditionExpressionPlaceholder')"
                  @blur="syncExpressionCondition(row)"
                />
                <el-button plain @click="openExpressionEditor(row, 'condition')">{{ t('productCenter.formulaSetup.expressionEditor') }}</el-button>
              </div>
              <el-tag v-else effect="plain" type="info">{{ t('productCenter.formulaSetup.defaultUsageRule') }}</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column :label="t('productCenter.formulaSetup.usageFormula')" min-width="320">
          <template #default="{ row }">
            <div class="formula-cell">
              <el-input
                v-model="row.usageFormulaText"
                clearable
                :placeholder="t('productCenter.formulaSetup.usageFormulaPlaceholder')"
                @blur="syncFormula(row)"
              />
              <el-button plain @click="openExpressionEditor(row, 'usage')">{{ t('productCenter.formulaSetup.expressionEditor') }}</el-button>
            </div>
          </template>
        </el-table-column>
        <el-table-column :label="t('productCenter.formulaSetup.sampleResult')" width="98" align="center">
          <template #default="{ row }">
            <el-tag :type="formulaResult(row).valid ? 'success' : 'warning'" effect="plain">
              {{ formulaResultText(row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('productCenter.formulaSetup.lossRate')" width="96">
          <template #default="{ row }">
            <el-input-number v-model="row.lossRate" :min="0" :precision="2" controls-position="right" />
          </template>
        </el-table-column>
        <el-table-column :label="t('productCenter.formulaSetup.defaultUsageRule')" width="86" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.defaultRuleFlag" @change="handleDefaultRuleChange(row)" />
          </template>
        </el-table-column>
      </el-table>

      <div class="usage-editor__helper">
        <div>
          <h4>{{ t('productCenter.formulaSetup.formulaVariables') }}</h4>
          <div class="usage-editor__chips">
            <el-tag v-for="variable in variableChips" :key="variable.name" effect="plain" @click="insertVariable(variable.label)">
              {{ variable.label }}
            </el-tag>
          </div>
        </div>
        <div class="usage-editor__example">
          <span>{{ t('productCenter.formulaSetup.formulaExample') }}</span>
          <code>订单宽 * 12 - 2.0</code>
        </div>
      </div>
      </template>
    </div>

    <FormulaExpressionEditorDialog
      v-model="expressionEditorOpen"
      v-model:text="expressionEditorText"
      :target="expressionEditorTarget"
      @confirm="confirmExpressionEditor"
    />
    <template #footer>
      <el-button @click="closeDrawerWithGuard">{{ t('common.cancel') }}</el-button>
      <el-button type="primary" @click="confirmAndClose">{{ t('common.confirm') }}</el-button>
    </template>
  </AdminDrawer>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CopyDocument, Delete, MagicStick, Plus } from '@element-plus/icons-vue'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { PRODUCT_STATUS_ENABLED } from '@/constants/productStatus'
import { useUnsavedChangesGuard } from '@/composables/useUnsavedChangesGuard'
import FormulaExpressionEditorDialog from './FormulaExpressionEditorDialog.vue'
import FormulaUsageFixedPanel from './FormulaUsageFixedPanel.vue'
import {
  conditionExpressionForOption,
  conditionKeyForOption,
  defaultConditionKey,
  formulaVariables,
  formatUsageNumber,
  validateConditionExpression,
  validateFormulaExpression
} from '../utils/formulaExpression'
import type {
  ProductFormulaMaterialVO,
  ProductFormulaOptionVO,
  ProductFormulaOptionValueVO,
  ProductFormulaUsageRuleVO,
  ProductOption
} from '@/api/product-capability/types'

const props = defineProps<{
  modelValue: boolean
  usageRow: ProductFormulaMaterialVO | null
  usageRules: ProductFormulaUsageRuleVO[]
  options: ProductFormulaOptionVO[]
  optionValues: ProductFormulaOptionValueVO[]
  unitOptions: ProductOption[]
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const localeStore = useLocaleStore()
const localMessages: Record<string, string> = {
  'productCenter.formulaSetup.usageFixedValue': '固定值',
  'productCenter.formulaSetup.usageFormulaRules': '公式规则',
  'productCenter.formulaSetup.conditionType': '使用条件',
  'productCenter.formulaSetup.conditionExpression': '表达式条件',
  'productCenter.formulaSetup.usageFixedShort': '固定',
  'productCenter.formulaSetup.formulaUsageRuleCount': '公式 {count} 条'
}
const t = (key: string) => {
  const message = getMessage(key, localeStore.language)
  return message === key ? (localMessages[key] || key) : message
}
const selectedRule = ref<ProductFormulaUsageRuleVO | null>(null)
const variableChips = formulaVariables
const expressionEditorOpen = ref(false)
const expressionEditorText = ref('')
const expressionEditorRow = ref<ProductFormulaUsageRuleVO | null>(null)
const expressionEditorTarget = ref<'usage' | 'condition'>('usage')
const usageSnapshot = ref<{
  materialCode?: string
  usageRow: ProductFormulaMaterialVO | null
  rules: ProductFormulaUsageRuleVO[]
} | null>(null)

const currentRules = computed(() => props.usageRules.filter((rule) => rule.materialCode === props.usageRow?.materialCode))
const unsavedChangesGuard = useUnsavedChangesGuard({
  enabled: () => props.modelValue && Boolean(props.usageRow),
  getSnapshot: () => JSON.stringify(createUsageSnapshot()),
  confirmDiscard: confirmDiscardChanges
})

watch(() => props.modelValue, (open) => {
  if (open && props.usageRow) {
    ensureInitialRule()
    selectedRule.value = null
    captureUsageSnapshot()
    window.addEventListener('keydown', handleDrawerShortcut)
  } else {
    window.removeEventListener('keydown', handleDrawerShortcut)
    handleDrawerClosed()
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleDrawerShortcut)
})

function cloneValue<T>(value: T): T {
  return JSON.parse(JSON.stringify(value)) as T
}

function createUsageSnapshot() {
  return {
    materialCode: props.usageRow?.materialCode,
    usageRow: props.usageRow ? cloneValue(props.usageRow) : null,
    rules: cloneValue(currentRules.value)
  }
}

function captureUsageSnapshot() {
  usageSnapshot.value = createUsageSnapshot()
  unsavedChangesGuard.markPristine()
}

function restoreUsageSnapshot() {
  const snapshot = usageSnapshot.value
  if (!snapshot) return
  const materialCode = snapshot.materialCode || props.usageRow?.materialCode
  if (props.usageRow && snapshot.usageRow) {
    const target = props.usageRow as Record<string, unknown>
    Object.keys(target).forEach((key) => delete target[key])
    Object.assign(target, cloneValue(snapshot.usageRow))
  }
  if (!materialCode) return
  for (let index = props.usageRules.length - 1; index >= 0; index--) {
    if (props.usageRules[index].materialCode === materialCode) {
      props.usageRules.splice(index, 1)
    }
  }
  props.usageRules.push(...cloneValue(snapshot.rules))
}

async function confirmDiscardChanges() {
  try {
    await ElMessageBox.confirm(t('common.unsavedChangesConfirm'), t('common.prompt'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
    restoreUsageSnapshot()
    unsavedChangesGuard.markPristine()
    return true
  } catch {
    return false
  }
}

async function closeDrawerWithGuard() {
  await unsavedChangesGuard.closeWithGuard(() => {
    emit('update:modelValue', false)
  })
}

function confirmAndClose() {
  unsavedChangesGuard.markPristine()
  emit('update:modelValue', false)
}

function handleDrawerBeforeClose(done: () => void) {
  unsavedChangesGuard.canClose().then((allowed) => {
    if (allowed) done()
  })
}

function handleDrawerClosed() {
  selectedRule.value = null
  usageSnapshot.value = null
  unsavedChangesGuard.resetPristine()
}

function handleDrawerShortcut(event: KeyboardEvent) {
  if (event.key !== 'Escape' || !props.modelValue) return
  event.preventDefault()
  closeDrawerWithGuard()
}

function ensureInitialRule() {
  if (!props.usageRow) return
  if (currentRules.value.length) return
  if (props.usageRow.usageMode === 'FORMULA') {
    addFormulaRule(true)
  } else {
    ensureFixedRule()
  }
}

function handleUsageModeChange(value: string | number | boolean | undefined) {
  if (!props.usageRow) return
  if (value === 'FORMULA') {
    ensureFormulaMode()
    ensureDefaultRule()
  } else {
    ensureFixedRule()
  }
}

function ensureFixedRule() {
  if (!props.usageRow?.materialCode) return
  removeRulesForCurrent()
  const fixedUsageQty = props.usageRow.fixedUsageQty ?? 1
  props.usageRow.usageMode = 'FIXED'
  props.usageRow.fixedUsageQty = fixedUsageQty
  props.usageRules.push({
    materialCode: props.usageRow.materialCode,
    materialNameCn: props.usageRow.materialNameCn,
    materialId: props.usageRow.materialId,
    formulaMaterialId: props.usageRow.formulaMaterialId,
    ruleName: t('productCenter.formulaSetup.fixedUsageRule'),
    conditionType: 'DEFAULT',
    conditionExpression: 'DEFAULT',
    conditionText: t('productCenter.formulaSetup.defaultUsageRule'),
    conditionKey: defaultConditionKey(),
    usageMode: 'FIXED',
    fixedUsageQty,
    usageFormula: String(fixedUsageQty),
    usageFormulaText: formatUsageNumber(fixedUsageQty),
    calculationUnitCode: props.usageRow.calculationUnitCode || props.usageRow.unitCode,
    lossRate: props.usageRow.lossRate ?? 0,
    defaultRuleFlag: true,
    productionRemark: props.usageRow.productionRemark,
    status: PRODUCT_STATUS_ENABLED,
    sortOrder: 10
  })
}

function syncFixedRuleFromRow() {
  if (!props.usageRow) return
  props.usageRow.usageMode = 'FIXED'
  const rule = currentRules.value.find((item) => item.usageMode === 'FIXED') || currentRules.value[0]
  if (!rule) {
    ensureFixedRule()
    return
  }
  const fixedUsageQty = props.usageRow.fixedUsageQty ?? 0
  rule.usageMode = 'FIXED'
  rule.fixedUsageQty = fixedUsageQty
  rule.usageFormula = String(fixedUsageQty)
  rule.usageFormulaText = formatUsageNumber(fixedUsageQty)
  rule.lossRate = props.usageRow.lossRate ?? 0
  rule.defaultRuleFlag = true
  handleDefaultRuleChange(rule)
}

function ensureFormulaMode() {
  if (!props.usageRow) return
  props.usageRow.usageMode = 'FORMULA'
  currentRules.value.forEach((rule) => {
    if (rule.usageMode === 'FIXED') {
      const fixedUsageQty = rule.fixedUsageQty ?? props.usageRow?.fixedUsageQty ?? 1
      rule.usageFormula = String(fixedUsageQty)
      rule.usageFormulaText = formatUsageNumber(fixedUsageQty)
      rule.fixedUsageQty = undefined
    }
    rule.usageMode = 'FORMULA'
  })
  props.usageRow.fixedUsageQty = undefined
}

function addFormulaRule(defaultRule: boolean, value?: ProductFormulaOptionValueVO) {
  if (!props.usageRow?.materialCode) return
  props.usageRow.usageMode = 'FORMULA'
  const next = currentRules.value.length + 1
  const rule: ProductFormulaUsageRuleVO = {
    materialCode: props.usageRow.materialCode,
    materialNameCn: props.usageRow.materialNameCn,
    materialId: props.usageRow.materialId,
    formulaMaterialId: props.usageRow.formulaMaterialId,
    ruleName: defaultRule ? t('productCenter.formulaSetup.defaultUsageRule') : (value?.valueNameCn || t('productCenter.formulaSetup.conditionalUsageRule')),
    conditionType: defaultRule ? 'DEFAULT' : 'OPTION_VALUE',
    conditionOptionCode: defaultRule ? undefined : value?.optionCode || 'FABRIC',
    conditionOptionNameCn: defaultRule ? undefined : optionName(value?.optionCode || 'FABRIC'),
    conditionValueCode: defaultRule ? undefined : value?.valueCode,
    conditionValueNameCn: defaultRule ? undefined : value?.valueNameCn,
    conditionExpression: defaultRule ? 'DEFAULT' : conditionExpressionForOption(value?.optionCode || 'FABRIC', value?.valueCode),
    conditionText: defaultRule ? t('productCenter.formulaSetup.defaultUsageRule') : conditionText(value?.optionCode || 'FABRIC', value?.valueCode),
    conditionKey: defaultRule ? defaultConditionKey() : conditionKeyForOption(value?.optionCode || 'FABRIC', value?.valueCode),
    usageMode: 'FORMULA',
    fixedUsageQty: undefined,
    usageFormula: defaultRule ? '1' : 'orderWidth',
    usageFormulaText: defaultRule ? '1' : '订单宽',
    calculationUnitCode: props.usageRow.calculationUnitCode || props.usageRow.unitCode,
    lossRate: props.usageRow.lossRate ?? 0,
    defaultRuleFlag: defaultRule,
    productionRemark: props.usageRow.productionRemark,
    status: PRODUCT_STATUS_ENABLED,
    sortOrder: next * 10
  }
  props.usageRules.push(rule)
  if (defaultRule) handleDefaultRuleChange(rule)
  selectedRule.value = rule
}

function addConditionalUsageRule() {
  ensureFormulaMode()
  addFormulaRule(false)
}

function generateFabricRules() {
  ensureFormulaMode()
  ensureDefaultRule()
  const fabricValues = optionValuesOf('FABRIC')
  if (!fabricValues.length) {
    ElMessage.warning(t('productCenter.formulaSetup.fabricOptionMissing'))
    return
  }
  fabricValues.forEach((value) => {
    const key = conditionKeyForOption('FABRIC', value.valueCode)
    const exists = currentRules.value.some((rule) => rule.conditionKey === key)
    if (!exists) addFormulaRule(false, value)
  })
}

function ensureDefaultRule() {
  if (!currentRules.value.some((rule) => rule.defaultRuleFlag)) {
    addFormulaRule(true)
  }
}

function copySelectedRule() {
  if (!selectedRule.value) return
  ensureFormulaMode()
  const copy = { ...selectedRule.value, usageRuleId: undefined, defaultRuleFlag: false, sortOrder: props.usageRules.length * 10 + 10 }
  copy.usageMode = 'FORMULA'
  copy.fixedUsageQty = undefined
  copy.ruleName = `${copy.ruleName || t('productCenter.formulaSetup.conditionalUsageRule')} Copy`
  props.usageRules.push(copy)
  selectedRule.value = copy
}

function removeSelectedRule() {
  if (!selectedRule.value) return
  const index = props.usageRules.indexOf(selectedRule.value)
  if (index >= 0) props.usageRules.splice(index, 1)
  selectedRule.value = null
}

function handleDefaultRuleChange(row: ProductFormulaUsageRuleVO) {
  if (row.defaultRuleFlag) {
    currentRules.value.forEach((rule) => {
      if (rule !== row) rule.defaultRuleFlag = false
    })
    row.conditionType = 'DEFAULT'
    row.conditionOptionCode = undefined
    row.conditionOptionNameCn = undefined
    row.conditionValueCode = undefined
    row.conditionValueNameCn = undefined
    row.conditionExpression = 'DEFAULT'
    row.conditionText = t('productCenter.formulaSetup.defaultUsageRule')
    row.conditionKey = defaultConditionKey()
  } else if (row.conditionType === 'DEFAULT') {
    row.conditionType = 'OPTION_VALUE'
  }
}

function handleConditionTypeChange(row: ProductFormulaUsageRuleVO) {
  row.defaultRuleFlag = row.conditionType === 'DEFAULT'
  if (row.conditionType === 'DEFAULT') {
    handleDefaultRuleChange(row)
    return
  }
  row.conditionExpression = undefined
  row.conditionText = undefined
  row.conditionKey = undefined
}

function handleConditionOptionChange(row: ProductFormulaUsageRuleVO) {
  row.conditionOptionNameCn = optionName(row.conditionOptionCode)
  row.conditionValueCode = undefined
  row.conditionValueNameCn = undefined
  row.conditionExpression = undefined
  row.conditionText = undefined
  row.conditionKey = undefined
}

function syncConditionValueName(row: ProductFormulaUsageRuleVO) {
  const value = optionValuesOf(row.conditionOptionCode).find((item) => item.valueCode === row.conditionValueCode)
  row.conditionValueNameCn = value?.valueNameCn
  row.conditionExpression = conditionExpressionForOption(row.conditionOptionCode, row.conditionValueCode)
  row.conditionText = conditionText(row.conditionOptionCode, row.conditionValueCode)
  row.conditionKey = conditionKeyForOption(row.conditionOptionCode, row.conditionValueCode)
}

function syncExpressionCondition(row: ProductFormulaUsageRuleVO) {
  if (row.conditionType !== 'EXPRESSION') return
  const result = validateConditionExpression(row.conditionText || row.conditionExpression)
  row.conditionExpression = result.expression
  row.conditionKey = result.valid ? `EXPR:${result.expression}` : undefined
}

function syncFormula(row: ProductFormulaUsageRuleVO) {
  const result = validateFormulaExpression(ruleFormulaText(row))
  row.usageFormula = result.expression
  if (row.usageMode === 'FIXED' && result.valid && typeof result.sampleValue === 'number') {
    row.fixedUsageQty = result.sampleValue
  }
}

function formulaResult(row: ProductFormulaUsageRuleVO) {
  return validateFormulaExpression(ruleFormulaText(row))
}

function ruleFormulaText(row: ProductFormulaUsageRuleVO) {
  if (row.usageFormulaText || row.usageFormula) return row.usageFormulaText || row.usageFormula
  if (row.fixedUsageQty !== undefined && row.fixedUsageQty !== null) return String(row.fixedUsageQty)
  return undefined
}

function formulaResultText(row: ProductFormulaUsageRuleVO) {
  const result = formulaResult(row)
  if (!result.valid) return t('productCenter.formulaSetup.formulaInvalid')
  return typeof result.sampleValue === 'number' ? formatUsageNumber(result.sampleValue) : String(result.sampleValue)
}

function insertVariable(label: string) {
  if (!selectedRule.value) return
  selectedRule.value.usageFormulaText = `${selectedRule.value.usageFormulaText || ''}${label}`
}

function openExpressionEditor(row: ProductFormulaUsageRuleVO, target: 'usage' | 'condition') {
  expressionEditorRow.value = row
  expressionEditorTarget.value = target
  expressionEditorText.value = target === 'usage'
    ? ruleFormulaText(row) || ''
    : row.conditionText || row.conditionExpression || ''
  expressionEditorOpen.value = true
}

function confirmExpressionEditor() {
  const row = expressionEditorRow.value
  if (!row) return
  if (expressionEditorTarget.value === 'usage') {
    row.usageFormulaText = expressionEditorText.value
    syncFormula(row)
  } else {
    row.conditionText = expressionEditorText.value
    syncExpressionCondition(row)
  }
  expressionEditorOpen.value = false
}

function optionValuesOf(optionCode?: string) {
  return props.optionValues.filter((value) => value.optionCode === optionCode)
}

function optionName(optionCode?: string) {
  return props.options.find((option) => option.optionCode === optionCode)?.optionNameCn
}

function conditionText(optionCode?: string, valueCode?: string) {
  const option = optionName(optionCode)
  const value = optionValuesOf(optionCode).find((item) => item.valueCode === valueCode)
  return option && value ? `${option} = ${value.valueNameCn || value.valueCode}` : undefined
}

function optionLabel(option: ProductFormulaOptionVO) {
  return `${option.optionCode || ''} ${option.optionNameCn || ''}`.trim()
}

function valueLabel(value: ProductFormulaOptionValueVO) {
  return value.valueNameCn || value.valueCode || '-'
}

function unitDisplay(unitCode?: string) {
  if (!unitCode) return '-'
  return props.unitOptions.find((unit) => unit.value === unitCode)?.label || unitCode
}

function removeRulesForCurrent() {
  if (!props.usageRow?.materialCode) return
  for (let index = props.usageRules.length - 1; index >= 0; index--) {
    if (props.usageRules[index].materialCode === props.usageRow.materialCode) {
      props.usageRules.splice(index, 1)
    }
  }
}
</script>

<style scoped>
.usage-editor {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.usage-editor__summary {
  display: grid;
  grid-template-columns: 120px 120px minmax(300px, 1.2fr) minmax(280px, 1fr) 90px;
  gap: 10px;
  padding: 10px 12px;
  background: #f7faff;
  border: 1px solid #e5ecf6;
  border-radius: 8px;
}

.usage-editor__summary span {
  display: block;
  margin-bottom: 4px;
  color: #6b7280;
  font-size: 12px;
}

.usage-editor__summary strong {
  color: #1f2937;
  font-size: 13px;
  font-weight: 700;
}

.usage-editor__material strong,
.usage-editor__spec strong {
  display: -webkit-box;
  overflow: hidden;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.usage-mode-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 10px 12px;
  background: #fff;
  border: 1px solid #e5ecf6;
  border-radius: 8px;
}

.usage-mode-card__label {
  color: #111827;
  font-size: 15px;
  font-weight: 700;
}

.usage-mode-card p {
  margin: 4px 0 0;
  color: #6b7280;
  font-size: 12px;
}

.usage-mode-card__switch {
  display: flex;
  gap: 8px;
}

.usage-mode-card__switch :deep(.el-radio) {
  height: 34px;
  margin-right: 0;
  padding: 0 14px;
  border-radius: 6px;
  font-weight: 600;
}

.usage-mode-card__switch :deep(.el-radio.is-checked) {
  border-color: #409eff;
  background: #eff6ff;
}

.usage-editor__toolbar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.usage-editor__toolbar h3,
.usage-editor__helper h4 {
  margin: 0 0 4px;
  color: #111827;
  font-size: 15px;
}

.usage-editor__toolbar p {
  margin: 0;
  color: #6b7280;
  font-size: 12px;
}

.usage-editor__actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
}

.usage-editor__actions :deep(.el-button) {
  height: 34px;
  border-radius: 6px;
}

.usage-rule-table {
  border-radius: 8px;
  overflow: hidden;
}

.usage-rule-table :deep(.el-table__header th) {
  background: #f7faff !important;
  color: #1f2937;
  font-weight: 700;
}

.usage-rule-table :deep(.el-input-number),
.usage-rule-table :deep(.el-select) {
  width: 100%;
}

.condition-cell,
.formula-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.condition-cell {
  flex-direction: column;
  align-items: stretch;
}

.condition-cell__selects {
  display: grid;
  grid-template-columns: minmax(110px, 1fr) minmax(130px, 1fr);
  gap: 8px;
}

.formula-cell :deep(.el-input) {
  flex: 1;
}

.formula-cell :deep(.el-button) {
  flex: 0 0 auto;
}

.usage-editor__helper {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  padding: 12px;
  background: #fbfdff;
  border: 1px solid #e5ecf6;
  border-radius: 8px;
}

.usage-editor__chips {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.usage-editor__chips :deep(.el-tag) {
  cursor: pointer;
}

.usage-editor__example {
  min-width: 260px;
  color: #6b7280;
  font-size: 12px;
}

.usage-editor__example code {
  display: block;
  margin-top: 6px;
  padding: 7px 9px;
  color: #1d4ed8;
  background: #eef5ff;
  border-radius: 6px;
}

</style>
