<template>
  <div class="condition-cell">
    <el-select :model-value="conditionMode(row)" @change="handleConditionModeChange(row, $event)">
      <el-option value="DEFAULT" :label="t('productCenter.formulaSetup.defaultUsageRule')" />
      <el-option value="OPTION_VALUE" :label="t('productCenter.formulaSetup.conditionBusinessOption')" />
      <el-option value="ORDER_VARIABLE" :label="t('productCenter.formulaSetup.conditionOrderVariable')" />
      <el-option value="MATERIAL_FIELD" :label="t('productCenter.formulaSetup.conditionSelectedMaterialField')" />
      <el-option value="EXPRESSION" :label="t('productCenter.formulaSetup.conditionAdvancedExpression')" />
    </el-select>
    <div v-if="conditionMode(row) === 'OPTION_VALUE'" class="condition-cell__selects">
      <el-select v-model="row.conditionOptionCode" clearable filterable @change="handleConditionOptionChange(row)">
        <el-option v-for="option in options" :key="option.optionCode" :label="optionLabel(option)" :value="option.optionCode" />
      </el-select>
      <el-select v-model="row.conditionValueCode" clearable filterable @change="syncConditionValueName(row)">
        <el-option v-for="value in optionValuesOf(row.conditionOptionCode)" :key="value.valueCode" :label="valueLabel(value)" :value="value.valueCode" />
      </el-select>
    </div>
    <div v-else-if="conditionMode(row) === 'ORDER_VARIABLE'" class="condition-cell__selects condition-cell__selects--three">
      <el-select :model-value="conditionDraft(row, 'orderVariable')" @change="setConditionDraft(row, 'orderVariable', $event)">
        <el-option v-for="item in orderConditionFields" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-select :model-value="conditionDraft(row, 'operator')" @change="setConditionDraft(row, 'operator', $event)">
        <el-option v-for="item in conditionOperators" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-input
        :model-value="conditionDraft(row, 'compareValue')"
        clearable
        :placeholder="t('productCenter.formulaSetup.conditionValue')"
        @update:model-value="setConditionDraft(row, 'compareValue', $event)"
      />
    </div>
    <div v-else-if="conditionMode(row) === 'MATERIAL_FIELD'" class="condition-cell__selects condition-cell__selects--material">
      <el-select :model-value="conditionDraft(row, 'materialOptionCode')" filterable @change="setConditionDraft(row, 'materialOptionCode', $event)">
        <el-option v-for="option in options" :key="option.optionCode" :label="optionLabel(option)" :value="option.optionCode" />
      </el-select>
      <el-select :model-value="conditionDraft(row, 'materialField')" @change="setConditionDraft(row, 'materialField', $event)">
        <el-option v-for="item in selectedMaterialFields" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-input
        :model-value="conditionDraft(row, 'compareValue')"
        clearable
        :placeholder="t('productCenter.formulaSetup.conditionValue')"
        @update:model-value="setConditionDraft(row, 'compareValue', $event)"
      />
    </div>
    <div v-else-if="conditionMode(row) === 'EXPRESSION'" class="formula-cell">
      <el-input
        v-model="row.conditionText"
        clearable
        :placeholder="t('productCenter.formulaSetup.conditionExpressionPlaceholder')"
        @blur="syncExpressionCondition(row)"
      />
      <el-button plain @click="$emit('open-expression', row)">{{ t('productCenter.formulaSetup.conditionExpressionEditor') }}</el-button>
    </div>
    <el-tag v-else effect="plain" type="info">{{ t('productCenter.formulaSetup.defaultUsageRule') }}</el-tag>
  </div>
</template>

<script setup lang="ts">
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import {
  conditionExpressionForOption,
  conditionKeyForOption,
  defaultConditionKey,
  validateConditionExpression
} from '../utils/formulaExpression'
import type {
  ProductFormulaOptionVO,
  ProductFormulaOptionValueVO,
  ProductFormulaUsageRuleVO
} from '@/api/product-capability/types'

const props = defineProps<{
  row: ProductFormulaUsageRuleVO
  options: ProductFormulaOptionVO[]
  optionValues: ProductFormulaOptionValueVO[]
}>()

const emit = defineEmits<{
  'default-change': [row: ProductFormulaUsageRuleVO]
  'open-expression': [row: ProductFormulaUsageRuleVO]
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
type ConditionMode = 'DEFAULT' | 'OPTION_VALUE' | 'ORDER_VARIABLE' | 'MATERIAL_FIELD' | 'EXPRESSION'
type ConditionDraftKey = 'orderVariable' | 'operator' | 'compareValue' | 'materialOptionCode' | 'materialField'

const conditionOperators = [
  { value: '==', label: '=' },
  { value: '!=', label: '!=' },
  { value: '>', label: '>' },
  { value: '>=', label: '>=' },
  { value: '<', label: '<' },
  { value: '<=', label: '<=' }
]

const orderConditionFields = [
  { value: 'orderWidth', label: t('productCenter.formulaSetup.templateOrderWidth') },
  { value: 'orderLength', label: t('productCenter.formulaSetup.templateOrderLength') },
  { value: 'store', label: t('productCenter.formulaSetup.store') }
]

const selectedMaterialFields = [
  { value: 'materialCode', label: t('productCenter.formulaSetup.materialCode') },
  { value: 'materialTypeCode', label: t('productCenter.formulaSetup.materialTypeCode') },
  { value: 'attributeGroupCode', label: t('productCenter.formulaSetup.attributeGroupCode') },
  { value: 'fabricSeriesCode', label: t('productCenter.formulaSetup.fabricSeriesCode') }
]

function conditionMode(row: ProductFormulaUsageRuleVO): ConditionMode {
  const draftMode = (row as Record<string, unknown>)._conditionMode
  if (typeof draftMode === 'string') return draftMode as ConditionMode
  if (row.defaultRuleFlag || row.conditionType === 'DEFAULT') return 'DEFAULT'
  if (row.conditionType === 'OPTION_VALUE') return 'OPTION_VALUE'
  if (String(row.conditionKey || '').startsWith('ORDER:')) return 'ORDER_VARIABLE'
  if (String(row.conditionKey || '').startsWith('MATERIAL_FIELD:')) return 'MATERIAL_FIELD'
  return 'EXPRESSION'
}

function handleConditionModeChange(row: ProductFormulaUsageRuleVO, value: string | number | boolean | undefined) {
  const mode = String(value || 'DEFAULT') as ConditionMode
  ;(row as Record<string, unknown>)._conditionMode = mode
  row.defaultRuleFlag = mode === 'DEFAULT'
  if (mode === 'DEFAULT') {
    row.conditionType = 'DEFAULT'
    emit('default-change', row)
    return
  }
  row.conditionType = mode === 'OPTION_VALUE' ? 'OPTION_VALUE' : 'EXPRESSION'
  row.conditionExpression = undefined
  row.conditionText = undefined
  row.conditionKey = undefined
  row.conditionOptionCode = undefined
  row.conditionOptionNameCn = undefined
  row.conditionValueCode = undefined
  row.conditionValueNameCn = undefined
  if (mode === 'ORDER_VARIABLE') {
    setConditionDraft(row, 'orderVariable', 'orderWidth')
    setConditionDraft(row, 'operator', '>')
    return
  }
  if (mode === 'MATERIAL_FIELD') {
    setConditionDraft(row, 'materialOptionCode', preferredMaterialOptionCode())
    setConditionDraft(row, 'materialField', 'materialTypeCode')
  }
}

function handleConditionOptionChange(row: ProductFormulaUsageRuleVO) {
  ;(row as Record<string, unknown>)._conditionMode = 'OPTION_VALUE'
  row.conditionOptionNameCn = optionName(row.conditionOptionCode)
  row.conditionValueCode = undefined
  row.conditionValueNameCn = undefined
  row.conditionExpression = undefined
  row.conditionText = undefined
  row.conditionKey = undefined
}

function syncConditionValueName(row: ProductFormulaUsageRuleVO) {
  ;(row as Record<string, unknown>)._conditionMode = 'OPTION_VALUE'
  const value = optionValuesOf(row.conditionOptionCode).find((item) => item.valueCode === row.conditionValueCode)
  row.conditionValueNameCn = value?.valueNameCn
  row.conditionExpression = conditionExpressionForOption(row.conditionOptionCode, row.conditionValueCode)
  row.conditionText = conditionText(row.conditionOptionCode, row.conditionValueCode)
  row.conditionKey = conditionKeyForOption(row.conditionOptionCode, row.conditionValueCode)
}

function syncExpressionCondition(row: ProductFormulaUsageRuleVO) {
  if (row.conditionType !== 'EXPRESSION') return
  ;(row as Record<string, unknown>)._conditionMode = 'EXPRESSION'
  const result = validateConditionExpression(row.conditionText || row.conditionExpression)
  row.conditionExpression = result.expression
  row.conditionKey = result.valid ? `EXPR:${result.expression}` : undefined
}

function conditionDraft(row: ProductFormulaUsageRuleVO, key: ConditionDraftKey) {
  const draftValue = (row as Record<string, unknown>)[`_${key}`]
  if (draftValue !== undefined && draftValue !== null && draftValue !== '') return String(draftValue)
  const keyParts = String(row.conditionKey || '').split(':')
  if (keyParts[0] === 'ORDER') {
    if (key === 'orderVariable') return keyParts[1] || defaultConditionDraftValue(key)
    if (key === 'operator') return keyParts[2] || defaultConditionDraftValue(key)
    if (key === 'compareValue') return keyParts.slice(3).join(':')
  }
  if (keyParts[0] === 'MATERIAL_FIELD') {
    if (key === 'materialOptionCode') return keyParts[1] || defaultConditionDraftValue(key)
    if (key === 'materialField') return keyParts[2] || defaultConditionDraftValue(key)
    if (key === 'compareValue') return keyParts.slice(3).join(':')
  }
  return String(defaultConditionDraftValue(key))
}

function setConditionDraft(row: ProductFormulaUsageRuleVO, key: ConditionDraftKey, value: string | number | boolean | undefined) {
  ;(row as Record<string, unknown>)[`_${key}`] = value === undefined ? '' : String(value)
  if (conditionMode(row) === 'ORDER_VARIABLE') syncOrderCondition(row)
  if (conditionMode(row) === 'MATERIAL_FIELD') syncSelectedMaterialFieldCondition(row)
}

function defaultConditionDraftValue(key: ConditionDraftKey) {
  if (key === 'orderVariable') return 'orderWidth'
  if (key === 'operator') return '>'
  if (key === 'materialOptionCode') return preferredMaterialOptionCode()
  if (key === 'materialField') return 'materialTypeCode'
  return ''
}

function preferredMaterialOptionCode() {
  return props.options.find((option) => option.optionCode === 'FABRIC')?.optionCode || props.options[0]?.optionCode || ''
}

function syncOrderCondition(row: ProductFormulaUsageRuleVO) {
  const variable = conditionDraft(row, 'orderVariable')
  const operator = conditionDraft(row, 'operator')
  const compareValue = conditionDraft(row, 'compareValue')
  row.conditionType = 'EXPRESSION'
  if (!variable || !operator || !compareValue) {
    row.conditionExpression = undefined
    row.conditionText = undefined
    row.conditionKey = undefined
    return
  }
  const valueExpression = variable === 'store' ? quoted(compareValue) : compareValue
  row.conditionExpression = `${variable} ${operator} ${valueExpression}`
  row.conditionText = `${orderConditionLabel(variable)} ${operatorLabel(operator)} ${compareValue}`
  row.conditionKey = `ORDER:${variable}:${operator}:${compareValue}`
}

function syncSelectedMaterialFieldCondition(row: ProductFormulaUsageRuleVO) {
  const optionCode = conditionDraft(row, 'materialOptionCode')
  const field = conditionDraft(row, 'materialField')
  const compareValue = conditionDraft(row, 'compareValue')
  row.conditionType = 'EXPRESSION'
  if (!optionCode || !field || !compareValue) {
    row.conditionExpression = undefined
    row.conditionText = undefined
    row.conditionKey = undefined
    return
  }
  const variable = `option_${optionCode}_${field}`
  row.conditionExpression = `${variable} == ${quoted(compareValue)}`
  row.conditionText = `${optionName(optionCode) || optionCode}.${selectedMaterialFieldLabel(field)} = ${compareValue}`
  row.conditionKey = `MATERIAL_FIELD:${optionCode}:${field}:${compareValue}`
}

function orderConditionLabel(value: string) {
  return orderConditionFields.find((item) => item.value === value)?.label || value
}

function selectedMaterialFieldLabel(value: string) {
  return selectedMaterialFields.find((item) => item.value === value)?.label || value
}

function operatorLabel(value: string) {
  return conditionOperators.find((item) => item.value === value)?.label || value
}

function quoted(value: string) {
  return `"${String(value).replace(/"/g, '\\"')}"`
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
</script>

<style scoped>
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

.condition-cell__selects--three,
.condition-cell__selects--material {
  grid-template-columns: minmax(120px, 1fr) minmax(100px, .75fr) minmax(130px, 1fr);
}

.formula-cell :deep(.el-input) {
  flex: 1;
}

.formula-cell :deep(.el-button) {
  flex: 0 0 auto;
}
</style>
