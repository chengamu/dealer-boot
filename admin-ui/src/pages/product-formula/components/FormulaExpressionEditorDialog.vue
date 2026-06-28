<template>
  <AdminDialog :model-value="modelValue" :title="editorTitle" width="1040px" class="formula-expression-dialog" append-to-body @update:model-value="$emit('update:modelValue', $event)">
    <div class="expression-editor">
      <div class="expression-editor__main">
        <section class="expression-editor__composer">
          <div class="expression-editor__section-head">
            <div>
              <h4>{{ t('productCenter.formulaSetup.expressionContent') }}</h4>
              <p>{{ editorPlaceholder }}</p>
            </div>
            <el-tag effect="plain" :type="editorResult.valid ? 'success' : 'warning'">
              {{ editorResult.valid ? t('productCenter.formulaSetup.expressionValid') : t('productCenter.formulaSetup.expressionInvalid') }}
            </el-tag>
          </div>
          <el-input
            v-model="editorText"
            type="textarea"
            :rows="7"
            resize="none"
            :placeholder="editorPlaceholder"
          />
          <div v-if="target === 'usage'" class="expression-editor__templates">
            <el-button v-for="template in formulaTemplates" :key="template.value" plain @click="useExpressionTemplate(template.value)">
              {{ template.label }}
            </el-button>
          </div>
          <div v-else class="expression-editor__builder">
            <el-select v-model="conditionBuilder.variable" filterable>
              <el-option v-for="variable in conditionBuilderVariables" :key="variable.name" :label="variable.label" :value="variable.name" />
            </el-select>
            <el-select v-model="conditionBuilder.operator">
              <el-option v-for="operator in conditionBuilderOperators" :key="operator" :label="operator" :value="operator" />
            </el-select>
            <el-input v-model="conditionBuilder.value" :placeholder="t('productCenter.formulaSetup.conditionValue')" />
            <el-button type="primary" plain @click="useConditionBuilder">{{ t('productCenter.formulaSetup.useCondition') }}</el-button>
          </div>
        </section>

        <aside class="expression-editor__result" :class="{ 'is-error': !editorResult.valid }">
          <h4>{{ t('productCenter.formulaSetup.validationResult') }}</h4>
          <strong>{{ editorResultText }}</strong>
          <dl>
            <div>
              <dt>{{ t('productCenter.formulaSetup.normalizedExpression') }}</dt>
              <dd>{{ editorResult.expression || '-' }}</dd>
            </div>
            <div>
              <dt>{{ t('productCenter.formulaSetup.sampleContext') }}</dt>
              <dd>{{ sampleText }}</dd>
            </div>
          </dl>
        </aside>
      </div>

      <div class="expression-editor__tools">
        <section v-for="group in editorVariableGroups" :key="group.title" class="expression-editor__tool-card">
          <h4>{{ group.title }}</h4>
          <div class="expression-editor__chips">
            <el-button v-for="variable in group.variables" :key="variable.name" plain @click="appendExpressionText(variable.label)">
              {{ variable.label }}
            </el-button>
          </div>
        </section>
        <section class="expression-editor__tool-card">
          <h4>{{ t('productCenter.formulaSetup.operator') }}</h4>
          <div class="expression-editor__ops">
            <el-button v-for="operator in editorOperators" :key="operator" plain @click="appendExpressionText(operator)">
              {{ operator }}
            </el-button>
          </div>
        </section>
      </div>
    </div>
    <template #footer>
      <AdminDialogFooter>
        <el-button @click="$emit('update:modelValue', false)">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :disabled="!editorResult.valid" @click="$emit('confirm')">{{ t('common.confirm') }}</el-button>
      </AdminDialogFooter>
    </template>
  </AdminDialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import {
  formulaVariables,
  formatUsageNumber,
  validateConditionExpression,
  validateFormulaExpression
} from '../utils/formulaExpression'

type ExpressionTarget = 'usage' | 'condition'

const props = defineProps<{
  modelValue: boolean
  target: ExpressionTarget
  text: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'update:text': [value: string]
  confirm: []
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const conditionBuilder = ref({
  variable: 'orderWidth',
  operator: '>',
  value: '12'
})

const editorText = computed({
  get: () => props.text,
  set: (value: string) => emit('update:text', value)
})
const editorTitle = computed(() => props.target === 'usage'
  ? t('productCenter.formulaSetup.usageFormulaEditor')
  : t('productCenter.formulaSetup.conditionExpressionEditor'))
const editorPlaceholder = computed(() => props.target === 'usage'
  ? t('productCenter.formulaSetup.usageFormulaPlaceholder')
  : t('productCenter.formulaSetup.conditionExpressionPlaceholder'))
const editorVariables = computed(() => props.target === 'usage'
  ? formulaVariables.filter((variable) => ['orderWidth', 'orderHeight', 'orderArea'].includes(variable.name))
  : formulaVariables)
const conditionBuilderVariables = computed(() => formulaVariables.filter((variable) => ['orderWidth', 'orderHeight', 'orderArea', 'fabric', 'productType', 'optionValue'].includes(variable.name)))
const editorOperators = computed(() => props.target === 'usage'
  ? ['+', '-', '*', '/', '(', ')']
  : ['=', '!=', '>', '>=', '<', '<=', '并且', '或者', '(', ')'])
const conditionBuilderOperators = ['=', '!=', '>', '>=', '<', '<=']
const formulaTemplates = computed(() => [
  { label: t('productCenter.formulaSetup.templateOrderWidth'), value: '订单宽' },
  { label: t('productCenter.formulaSetup.templateOrderArea'), value: '订单面积' },
  { label: t('productCenter.formulaSetup.templateWidthDeduct'), value: '订单宽 * 12 - 2.0' }
])
const editorVariableGroups = computed(() => {
  const orderVariables = editorVariables.value.filter((variable) => ['orderWidth', 'orderHeight', 'orderArea'].includes(variable.name))
  const businessVariables = editorVariables.value.filter((variable) => !['orderWidth', 'orderHeight', 'orderArea'].includes(variable.name))
  return [
    { title: t('productCenter.formulaSetup.orderVariables'), variables: orderVariables },
    ...(businessVariables.length ? [{ title: t('productCenter.formulaSetup.businessVariables'), variables: businessVariables }] : [])
  ]
})
const editorResult = computed(() => props.target === 'usage'
  ? validateFormulaExpression(props.text)
  : validateConditionExpression(props.text))
const editorResultText = computed(() => {
  const result = editorResult.value
  if (!result.valid) return expressionValidationMessage(result.message)
  if (props.target === 'usage') {
    return `${t('productCenter.formulaSetup.sampleResult')}：${typeof result.sampleValue === 'number' ? formatUsageNumber(result.sampleValue) : result.sampleValue}`
  }
  return `${t('productCenter.formulaSetup.sampleResult')}：${result.sampleValue ? t('common.yes') : t('common.no')}`
})
const sampleText = computed(() => props.target === 'usage'
  ? t('productCenter.formulaSetup.formulaSampleContext')
  : t('productCenter.formulaSetup.conditionSampleContext'))

watch(() => props.modelValue, (open) => {
  if (open) resetConditionBuilder()
})

function appendExpressionText(value: string) {
  editorText.value = `${editorText.value || ''}${editorText.value ? ' ' : ''}${value}`
}

function useExpressionTemplate(value: string) {
  editorText.value = value
}

function resetConditionBuilder() {
  conditionBuilder.value = {
    variable: 'orderWidth',
    operator: '>',
    value: '12'
  }
}

function useConditionBuilder() {
  const variable = conditionBuilderVariables.value.find((item) => item.name === conditionBuilder.value.variable)
  const label = variable?.label || conditionBuilder.value.variable
  const rawValue = String(conditionBuilder.value.value || '').trim()
  const value = needsQuotedConditionValue(conditionBuilder.value.variable, rawValue)
    ? `"${rawValue.replace(/"/g, '\\"')}"`
    : rawValue
  editorText.value = `${label} ${conditionBuilder.value.operator} ${value}`
}

function needsQuotedConditionValue(variable: string, value: string) {
  if (!value) return false
  if (value.startsWith('"') || value.startsWith("'")) return false
  if (['fabric', 'productType', 'optionValue'].includes(variable)) return true
  return Number.isNaN(Number(value))
}

function expressionValidationMessage(message?: string) {
  if (!message) return t('productCenter.formulaSetup.formulaInvalid')
  const messageKeyMap: Record<string, string> = {
    empty: 'productCenter.formulaSetup.expressionEmpty',
    invalidResult: 'productCenter.formulaSetup.expressionInvalidResult',
    conditionMustBeBoolean: 'productCenter.formulaSetup.conditionMustBeBoolean'
  }
  const key = messageKeyMap[message]
  return key ? t(key) : t('productCenter.formulaSetup.formulaInvalid')
}
</script>

<style scoped>
.expression-editor {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.expression-editor__main {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 12px;
}

.expression-editor__composer,
.expression-editor__result,
.expression-editor__tool-card {
  border: 1px solid #e5ecf6;
  border-radius: 8px;
  background: #fff;
  padding: 12px;
}

.expression-editor__section-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.expression-editor__section-head h4,
.expression-editor__tool-card h4,
.expression-editor__result h4 {
  margin: 0;
  font-size: 15px;
}

.expression-editor__section-head p {
  margin: 4px 0 0;
  color: #8a95a6;
  font-size: 12px;
}

.expression-editor__templates,
.expression-editor__builder {
  display: flex;
  gap: 8px;
  margin-top: 10px;
  flex-wrap: wrap;
}

.expression-editor__builder {
  align-items: center;
}

.expression-editor__tools {
  display: grid;
  grid-template-columns: 1fr 260px;
  gap: 12px;
}

.expression-editor__chips,
.expression-editor__ops {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.expression-editor__chips :deep(.el-button),
.expression-editor__ops :deep(.el-button) {
  margin-left: 0;
}

.expression-editor__result {
  background: #f8fbff;
}

.expression-editor__result strong {
  display: block;
  color: #0f766e;
  font-size: 16px;
  margin-bottom: 12px;
}

.expression-editor__result dl {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin: 0;
}

.expression-editor__result dt {
  color: #8a95a6;
  font-size: 12px;
  margin-bottom: 4px;
}

.expression-editor__result dd {
  margin: 0;
  color: #1f2937;
  word-break: break-all;
}

.expression-editor__result.is-error strong {
  color: #dc2626;
}
</style>
