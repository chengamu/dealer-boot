<template>
  <el-drawer
    :model-value="modelValue"
    :title="t('productCenter.formulaSetup.usageSetting')"
    size="1280px"
    append-to-body
    class="formula-usage-drawer"
    @update:model-value="$emit('update:modelValue', $event)"
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
        <div>
          <span>{{ t('productCenter.formulaSetup.unit') }}</span>
          <strong>{{ usageRow.unitCode || '-' }}</strong>
        </div>
      </div>

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
        <el-table-column :label="t('productCenter.formulaSetup.conditionType')" width="130">
          <template #default="{ row }">
            <el-select v-model="row.conditionType" @change="handleConditionTypeChange(row)">
              <el-option value="DEFAULT" :label="t('productCenter.formulaSetup.defaultUsageRule')" />
              <el-option value="OPTION_VALUE" :label="t('productCenter.formulaSetup.conditionOptionValue')" />
              <el-option value="EXPRESSION" :label="t('productCenter.formulaSetup.conditionExpression')" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column :label="t('productCenter.formulaSetup.conditionOption')" width="150">
          <template #default="{ row }">
            <el-select v-model="row.conditionOptionCode" clearable filterable :disabled="row.conditionType !== 'OPTION_VALUE'" @change="handleConditionOptionChange(row)">
              <el-option v-for="option in options" :key="option.optionCode" :label="optionLabel(option)" :value="option.optionCode" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column :label="t('productCenter.formulaSetup.conditionValue')" width="180">
          <template #default="{ row }">
            <el-select v-model="row.conditionValueCode" clearable filterable :disabled="row.conditionType !== 'OPTION_VALUE'" @change="syncConditionValueName(row)">
              <el-option v-for="value in optionValuesOf(row.conditionOptionCode)" :key="value.valueCode" :label="valueLabel(value)" :value="value.valueCode" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column :label="t('productCenter.formulaSetup.conditionExpression')" min-width="210">
          <template #default="{ row }">
            <el-input
              v-model="row.conditionText"
              :disabled="row.conditionType !== 'EXPRESSION'"
              clearable
              :placeholder="t('productCenter.formulaSetup.conditionExpressionPlaceholder')"
              @blur="syncExpressionCondition(row)"
            />
          </template>
        </el-table-column>
        <el-table-column :label="t('productCenter.formulaSetup.usageFormula')" min-width="230">
          <template #default="{ row }">
            <el-input
              v-model="row.usageFormulaText"
              clearable
              :placeholder="t('productCenter.formulaSetup.usageFormulaPlaceholder')"
              @blur="syncFormula(row)"
            />
          </template>
        </el-table-column>
        <el-table-column :label="t('productCenter.formulaSetup.sampleResult')" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="formulaResult(row).valid ? 'success' : 'warning'" effect="plain">
              {{ formulaResultText(row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('productCenter.formulaSetup.lossRate')" width="112">
          <template #default="{ row }">
            <el-input-number v-model="row.lossRate" :min="0" :precision="2" controls-position="right" />
          </template>
        </el-table-column>
        <el-table-column :label="t('productCenter.formulaSetup.defaultUsageRule')" width="96" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.defaultRuleFlag" @change="handleDefaultRuleChange(row)" />
          </template>
        </el-table-column>
        <el-table-column :label="t('productCenter.common.status')" width="96" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.status" active-value="ENABLED" inactive-value="DISABLED" />
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
    </div>
    <template #footer>
      <el-button @click="$emit('update:modelValue', false)">{{ t('common.close') }}</el-button>
    </template>
  </el-drawer>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { CopyDocument, Delete, MagicStick, Plus } from '@element-plus/icons-vue'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
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

defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const selectedRule = ref<ProductFormulaUsageRuleVO | null>(null)
const variableChips = formulaVariables

const currentRules = computed(() => props.usageRules.filter((rule) => rule.materialCode === props.usageRow?.materialCode))

watch(() => props.modelValue, (open) => {
  if (open && props.usageRow) {
    ensureInitialRule()
  }
})

function ensureInitialRule() {
  if (!props.usageRow) return
  if (currentRules.value.length) return
  if (props.usageRow.usageMode === 'FORMULA') {
    addFormulaRule(true)
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
    status: 'ENABLED',
    sortOrder: 10
  })
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
    status: 'ENABLED',
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
  return `${value.valueCode || ''} ${value.valueNameCn || ''}`.trim()
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
  grid-template-columns: 150px 170px minmax(240px, 1fr) 100px;
  gap: 10px;
  padding: 12px;
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

.usage-editor__material strong {
  display: -webkit-box;
  overflow: hidden;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
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
