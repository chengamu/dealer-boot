<template>
  <div class="usage-editor__toolbar">
    <div>
      <h3>{{ t('productCenter.formulaSetup.conditionalUsageRules') }}</h3>
      <p>{{ t('productCenter.formulaSetup.conditionalUsageRulesHint') }}</p>
    </div>
    <div class="usage-editor__actions">
      <el-button plain :icon="MagicStick" @click="$emit('generate-fabric-rules')">{{ t('productCenter.formulaSetup.generateUsageFromFabric') }}</el-button>
      <el-button type="primary" plain :icon="Plus" @click="$emit('add-rule')">{{ t('productCenter.formulaSetup.addUsageRule') }}</el-button>
      <el-button plain :icon="CopyDocument" :disabled="!selectedRule" @click="$emit('copy-rule')">{{ t('common.copy') }}</el-button>
      <el-button type="danger" plain :icon="Delete" :disabled="!selectedRule" @click="$emit('remove-rule')">{{ t('common.delete') }}</el-button>
    </div>
  </div>

  <el-table
    :data="rules"
    border
    height="430"
    row-key="sortOrder"
    class="usage-rule-table"
    highlight-current-row
    @current-change="$emit('update:selectedRule', $event)"
  >
    <el-table-column type="index" :label="t('common.index')" width="54" align="center" />
    <el-table-column :label="t('productCenter.formulaSetup.useCondition')" min-width="430">
      <template #default="{ row }">
        <FormulaUsageConditionEditor
          :row="row"
          :options="options"
          :option-values="optionValues"
          @default-change="$emit('default-rule-change', $event)"
          @open-expression="$emit('open-expression', $event, 'condition')"
        />
      </template>
    </el-table-column>
    <el-table-column
      v-for="field in formulaFields"
      :key="field.valueKey"
      :label="t(field.labelKey)"
      min-width="180"
    >
      <template #default="{ row }">
        <div class="formula-cell formula-cell--compact">
          <el-input
            v-model="row[field.textKey]"
            clearable
            :placeholder="t('productCenter.formulaSetup.formulaOptionalPlaceholder')"
            @blur="$emit('formula-blur', row, field)"
          />
          <el-button plain @click="$emit('open-expression', row, field.target)">{{ t('productCenter.formulaSetup.formulaSelectorShort') }}</el-button>
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
        <el-switch v-model="row.defaultRuleFlag" @change="$emit('default-rule-change', row)" />
      </template>
    </el-table-column>
  </el-table>

  <div class="usage-editor__helper">
    <div>
      <h4>{{ t('productCenter.formulaSetup.formulaVariables') }}</h4>
      <div class="usage-editor__chips">
        <el-tag v-for="variable in variableChips" :key="variable.name" effect="plain" @click="$emit('insert-variable', variable.label)">
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

<script setup lang="ts">
import { CopyDocument, Delete, MagicStick, Plus } from '@element-plus/icons-vue'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import FormulaUsageConditionEditor from './FormulaUsageConditionEditor.vue'
import { formulaVariables, formatUsageNumber, validateFormulaExpression } from '../utils/formulaExpression'
import type { FormulaField, ExpressionTarget } from './formulaUsageTypes'
import type {
  ProductFormulaOptionVO,
  ProductFormulaOptionValueVO,
  ProductFormulaUsageRuleVO
} from '@/api/product-capability/types'

const props = defineProps<{
  rules: ProductFormulaUsageRuleVO[]
  selectedRule: ProductFormulaUsageRuleVO | null
  options: ProductFormulaOptionVO[]
  optionValues: ProductFormulaOptionValueVO[]
  formulaFields: FormulaField[]
}>()

defineEmits<{
  'update:selectedRule': [row: ProductFormulaUsageRuleVO | null]
  'generate-fabric-rules': []
  'add-rule': []
  'copy-rule': []
  'remove-rule': []
  'default-rule-change': [row: ProductFormulaUsageRuleVO]
  'formula-blur': [row: ProductFormulaUsageRuleVO, field: FormulaField]
  'open-expression': [row: ProductFormulaUsageRuleVO, target: ExpressionTarget]
  'insert-variable': [label: string]
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const variableChips = formulaVariables

function formulaResult(row: ProductFormulaUsageRuleVO) {
  const candidates = props.formulaFields
    .map((field) => ruleFormulaText(row, field))
    .filter(Boolean)
  return candidates.length
    ? validateFormulaExpression(candidates[0])
    : { valid: false, expression: '', message: 'empty' }
}

function formulaResultText(row: ProductFormulaUsageRuleVO) {
  const result = formulaResult(row)
  if (!result.valid) return t('productCenter.formulaSetup.formulaInvalid')
  return typeof result.sampleValue === 'number' ? formatUsageNumber(result.sampleValue) : String(result.sampleValue)
}

function ruleFormulaText(row: ProductFormulaUsageRuleVO, field: FormulaField) {
  const textValue = row[field.textKey]
  const formulaValue = row[field.valueKey]
  if (typeof textValue === 'string' || typeof formulaValue === 'string') return (textValue || formulaValue) as string
  if (field.target === 'usage' && row.fixedUsageQty !== undefined && row.fixedUsageQty !== null) return String(row.fixedUsageQty)
  return undefined
}
</script>

<style scoped>
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

.formula-cell {
  display: flex;
  align-items: center;
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
