<template>
  <AdminDialog
    :model-value="modelValue"
    :title="t('productCenter.formulaSetup.formulaSelector')"
    width="1120px"
    class="variable-formula-dialog"
    append-to-body
    :close-on-press-escape="false"
    :close-on-click-modal="false"
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <div class="variable-formula-editor">
      <div class="variable-formula-editor__main">
        <FormulaExpressionComposer
          v-model="editorText"
          :title="t('productCenter.formulaSetup.expressionContent')"
          :placeholder="t('productCenter.formulaSetup.usageFormulaPlaceholder')"
          :clear-text="t('common.clear')"
        />
        <FormulaExpressionOperatorPanel
          :title="t('productCenter.formulaSetup.operator')"
          :operators="operators"
          @append="appendText"
        />
        <FormulaExpressionValidationPanel
          :title="t('productCenter.formulaSetup.validationResult')"
          :status-text="resultText"
          :normalized-title="t('productCenter.formulaSetup.normalizedExpression')"
          :expression="result.expression || ''"
          :valid="result.valid"
        />
        <section class="variable-formula-editor__orders">
          <h4>{{ t('productCenter.formulaSetup.orderVariables') }}</h4>
          <el-button v-for="variable in orderVariables" :key="variable.name" plain @click="appendText(variable.label)">
            {{ variable.label }}
          </el-button>
        </section>
      </div>
      <section class="variable-formula-editor__variables">
        <h4>{{ t('productCenter.formulaSetup.internalVariables') }}</h4>
        <el-input v-model="keyword" clearable :placeholder="t('productCenter.formulaSetup.variableSearchPlaceholder')" />
        <div class="variable-formula-editor__list">
          <button
            v-for="variable in visibleVariables"
            :key="variable.variableKey || variable.variableCode"
            class="variable-formula-editor__item"
            type="button"
            @dblclick="appendText(variable.variableName || variable.variableCode || '')"
          >
            <strong>{{ variable.variableName || variable.variableCode }}</strong>
            <span>{{ variable.variableCode }}</span>
          </button>
        </div>
      </section>
    </div>
    <template #footer>
      <AdminDialogFooter>
        <el-button @click="$emit('update:modelValue', false)">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :disabled="!result.valid" @click="$emit('confirm')">{{ t('common.confirm') }}</el-button>
      </AdminDialogFooter>
    </template>
  </AdminDialog>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import {
  formulaVariables,
  formatUsageNumber,
  validateFormulaExpression
} from '../utils/formulaExpression'
import { normalizeDisplayExpression } from './formulaExpressionDisplay'
import FormulaExpressionComposer from './FormulaExpressionComposer.vue'
import FormulaExpressionOperatorPanel from './FormulaExpressionOperatorPanel.vue'
import FormulaExpressionValidationPanel from './FormulaExpressionValidationPanel.vue'
import type { ProductFormulaVariableVO } from '@/api/product-capability/types'

const props = defineProps<{
  modelValue: boolean
  text: string
  variables: ProductFormulaVariableVO[]
  currentVariable?: ProductFormulaVariableVO
  t: (key: string) => string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'update:text': [value: string]
  confirm: []
}>()

const keyword = ref('')
const operators = ['+', '-', '*', '/', '(', ')', { label: '四舍五入(x, 2)', insert: '四舍五入()' }, { label: '向上取整(x, 2)', insert: '向上取整()' }, { label: '向下取整(x, 2)', insert: '向下取整()' }]
const editorText = computed({ get: () => props.text, set: (value: string) => emit('update:text', value) })
const orderNames = new Set(['orderWidthIn', 'orderLengthIn', 'orderWidthCm', 'orderLengthCm', 'orderAreaM2'])
const orderVariables = computed(() => formulaVariables.filter((item) => orderNames.has(item.name)))
const availableVariables = computed(() => props.variables.filter((variable) => !sameVariable(variable, props.currentVariable)))
const normalizedText = computed(() => normalizeDisplayExpression(props.text, [], [], [], availableVariables.value))
const result = computed(() => validateFormulaExpression(normalizedText.value))
const resultText = computed(() => {
  if (!result.value.valid) return props.t('productCenter.formulaSetup.formulaInvalid')
  return `${props.t('productCenter.formulaSetup.sampleResult')}：${formatUsageNumber(typeof result.value.sampleValue === 'boolean' ? undefined : result.value.sampleValue)}`
})
const visibleVariables = computed(() => {
  const search = keyword.value.trim().toLowerCase()
  return availableVariables.value
    .filter((variable) => {
      if (!search) return true
      return `${variable.variableName || ''} ${variable.variableCode || ''}`.toLowerCase().includes(search)
    })
})

function appendText(value: string) {
  if (!value) return
  editorText.value = `${editorText.value || ''}${editorText.value ? ' ' : ''}${value}`
}

function sameVariable(left?: ProductFormulaVariableVO, right?: ProductFormulaVariableVO) {
  if (!left || !right) return false
  if (left.variableKey && right.variableKey) return left.variableKey === right.variableKey
  if (left.variableId && right.variableId) return left.variableId === right.variableId
  return Boolean(left.variableCode && right.variableCode && left.variableCode === right.variableCode)
}
</script>

<style scoped>
.variable-formula-editor {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 340px;
  gap: 10px;
}

.variable-formula-editor__main {
  display: grid;
  gap: 8px;
  min-width: 0;
}

.variable-formula-editor :deep(.expression-editor__composer),
.variable-formula-editor :deep(.expression-editor__operators),
.variable-formula-editor :deep(.expression-editor__result) {
  min-width: 0;
  border: 1px solid #e5ecf6;
  border-radius: 8px;
  background: #fff;
  padding: 8px 12px;
}

.variable-formula-editor :deep(.expression-editor__head),
.variable-formula-editor :deep(.expression-editor__result-head) {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 6px;
}

.variable-formula-editor :deep(.expression-editor__composer .el-textarea__inner) {
  min-height: 118px !important;
  line-height: 1.6;
}

.variable-formula-editor :deep(.expression-editor__operators) {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  background: #f8fbff;
}

.variable-formula-editor :deep(.expression-editor__operators h4) {
  width: 100%;
}

.variable-formula-editor :deep(.expression-editor__operators .el-button),
.variable-formula-editor__orders .el-button {
  margin-left: 0;
}

.variable-formula-editor :deep(.expression-editor__result-expression) {
  margin: 0;
}

.variable-formula-editor :deep(.expression-editor__result-expression dt) {
  display: none;
}

.variable-formula-editor :deep(.expression-editor__result-expression dd) {
  margin: 0;
  min-height: 32px;
  padding: 7px 10px;
  border: 1px solid #e5ecf6;
  border-radius: 6px;
  background: #fff;
  color: #334155;
}

.variable-formula-editor__orders,
.variable-formula-editor__variables {
  border: 1px solid #e5ecf6;
  border-radius: 8px;
  background: #fff;
  padding: 10px 12px;
}

.variable-formula-editor__orders {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.variable-formula-editor h4 {
  margin: 0;
  color: #1f2937;
  font-size: 14px;
  white-space: nowrap;
}

.variable-formula-editor__variables {
  display: grid;
  gap: 8px;
  align-content: start;
}

.variable-formula-editor__list {
  display: grid;
  gap: 8px;
  max-height: 330px;
  overflow-y: auto;
}

.variable-formula-editor__item {
  display: grid;
  gap: 4px;
  width: 100%;
  padding: 10px;
  cursor: pointer;
  text-align: left;
  border: 1px solid #e5ecf6;
  border-radius: 8px;
  background: #f8fbff;
}

.variable-formula-editor__item span {
  color: #8a95a6;
  font-size: 12px;
}
</style>
