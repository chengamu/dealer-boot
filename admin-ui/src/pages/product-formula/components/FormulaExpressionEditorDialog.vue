<template>
  <AdminDialog
    :model-value="modelValue"
    :title="editorTitle"
    width="1280px"
    class="formula-expression-dialog"
    append-to-body
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <div class="expression-editor">
      <template v-if="isFormulaTarget">
        <div class="expression-editor__formula-layout">
          <div class="expression-editor__formula-main">
            <FormulaExpressionComposer
              v-model="editorText"
              :title="t('productCenter.formulaSetup.expressionContent')"
              :placeholder="editorPlaceholder"
              :clear-text="t('common.clear')"
            />
            <FormulaExpressionOperatorPanel
              :title="t('productCenter.formulaSetup.operator')"
              :operators="editorOperators"
              @append="appendExpressionText"
            />
            <FormulaExpressionValidationPanel
              :title="t('productCenter.formulaSetup.validationResult')"
              :status-text="editorResultText"
              :normalized-title="t('productCenter.formulaSetup.normalizedExpression')"
              :expression="editorResult.expression || ''"
              :valid="editorResult.valid"
            />
            <section class="expression-editor__tools">
              <div v-for="group in variableGroups" :key="group.title" class="expression-editor__tool-card">
                <h4>{{ group.title }}</h4>
                <el-button v-for="variable in group.variables" :key="variable.name" plain @click="appendExpressionText(variable.insert)">
                  {{ variable.label }}
                </el-button>
              </div>
            </section>
          </div>
          <FormulaInternalVariableGrid
            :variables="variables || []"
            :variable-rules="variableRules || []"
            :t="t"
            @add="openVariableEditor()"
            @edit="openVariableEditor"
            @remove="removeVariable"
            @copy="copyVariables"
            @insert="insertVariable"
          />
        </div>
      </template>

      <template v-else>
        <FormulaExpressionComposer
          v-model="editorText"
          :title="t('productCenter.formulaSetup.expressionContent')"
          :placeholder="editorPlaceholder"
          :clear-text="t('common.clear')"
        />

        <div class="expression-editor__condition-area">
          <FormulaExpressionOperatorPanel
            :title="t('productCenter.formulaSetup.operator')"
            :operators="editorOperators"
            @append="appendExpressionText"
          />

          <FormulaExpressionConditionBuilder
            class="expression-editor__builder"
            :has-text="Boolean(editorText)"
            :materials="materials"
            :options="options"
            :option-values="optionValues"
            @insert="appendConditionClause"
          />
        </div>

        <FormulaExpressionValidationPanel
          :title="t('productCenter.formulaSetup.validationResult')"
          :status-text="editorResultText"
          :normalized-title="t('productCenter.formulaSetup.normalizedExpression')"
          :expression="editorResult.expression || ''"
          :valid="editorResult.valid"
        />
      </template>
    </div>
    <FormulaInternalVariableEditorDialog
      v-model="variableEditorOpen"
      :variable="editingVariable"
      :rules="editingVariableRules"
      :t="t"
      @save="saveVariable"
    />
    <template #footer>
      <AdminDialogFooter>
        <el-button @click="$emit('update:modelValue', false)">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :disabled="!editorResult.valid" @click="$emit('confirm')">{{ t('common.confirm') }}</el-button>
      </AdminDialogFooter>
    </template>
  </AdminDialog>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { productFormulaApi } from '@/api/product-formula/formula'
import FormulaExpressionComposer from './FormulaExpressionComposer.vue'
import FormulaExpressionConditionBuilder from './FormulaExpressionConditionBuilder.vue'
import FormulaInternalVariableEditorDialog from './FormulaInternalVariableEditorDialog.vue'
import FormulaInternalVariableGrid from './FormulaInternalVariableGrid.vue'
import FormulaExpressionOperatorPanel from './FormulaExpressionOperatorPanel.vue'
import FormulaExpressionValidationPanel from './FormulaExpressionValidationPanel.vue'
import {
  formulaVariables,
  formatUsageNumber,
  validateConditionExpression,
  validateFormulaExpression
} from '../utils/formulaExpression'
import { normalizeDisplayExpression } from './formulaExpressionDisplay'
import type {
  ProductFormulaMaterialVO,
  ProductFormulaOptionVO,
  ProductFormulaOptionValueVO,
  ProductFormulaSetupVO,
  ProductFormulaVariableRuleVO,
  ProductFormulaVariableVO
} from '@/api/product-capability/types'

type ExpressionTarget = 'length' | 'width' | 'height' | 'weight' | 'usage' | 'condition'
type InsertVariable = { label: string; name: string; insert: string }

const props = defineProps<{
  modelValue: boolean
  formulaId?: string | number
  target: ExpressionTarget
  text: string
  materials?: ProductFormulaMaterialVO[]
  options?: ProductFormulaOptionVO[]
  optionValues?: ProductFormulaOptionValueVO[]
  variables?: ProductFormulaVariableVO[]
  variableRules?: ProductFormulaVariableRuleVO[]
  enableConditionArithmeticOperators?: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'update:text': [value: string]
  'variables-saved': [value: ProductFormulaSetupVO]
  confirm: []
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)

const editorText = computed({ get: () => props.text, set: (value: string) => emit('update:text', value) })
const isFormulaTarget = computed(() => props.target !== 'condition')
const editorTitle = computed(() => isFormulaTarget.value ? t('productCenter.formulaSetup.formulaSelector') : t('productCenter.formulaSetup.conditionExpressionEditor'))
const editorPlaceholder = computed(() => isFormulaTarget.value ? t('productCenter.formulaSetup.usageFormulaPlaceholder') : t('productCenter.formulaSetup.conditionExpressionPlaceholder'))
const editorOperators = computed(() => isFormulaTarget.value
  ? ['+', '-', '*', '/', '(', ')', { label: '四舍五入(x, 2)', insert: '四舍五入()' }, { label: '向上取整(x, 2)', insert: '向上取整()' }, { label: '向下取整(x, 2)', insert: '向下取整()' }]
  : conditionOperators())
const normalizedEditorText = computed(() => normalizeDisplayExpression(props.text, props.options, props.optionValues, props.materials, props.variables))
const editorResult = computed(() => isFormulaTarget.value ? validateFormulaExpression(normalizedEditorText.value) : validateConditionExpression(normalizedEditorText.value))
const editorResultText = computed(() => {
  const result = editorResult.value
  if (!result.valid) return expressionValidationMessage(result.message)
  if (isFormulaTarget.value) return `${t('productCenter.formulaSetup.sampleResult')}：${typeof result.sampleValue === 'number' ? formatUsageNumber(result.sampleValue) : result.sampleValue}`
  return t('productCenter.formulaSetup.expressionValid')
})
const variableGroups = computed(() => isFormulaTarget.value ? formulaVariableGroups() : [])
const variableEditorOpen = ref(false)
const editingVariable = ref<ProductFormulaVariableVO | null>(null)
const editingVariableRules = computed(() => props.variableRules?.filter((rule) => rule.variableCode === editingVariable.value?.variableCode) || [])

function appendExpressionText(value: string) {
  editorText.value = `${editorText.value || ''}${editorText.value ? ' ' : ''}${value}`
}

function appendConditionClause(clause: string, joiner: string) {
  editorText.value = `${editorText.value.trim()}${editorText.value.trim() ? ` ${joiner} ` : ''}${clause}`.trim()
}

function conditionOperators() {
  const operators: Array<string | { label: string; insert: string }> = ['=', '!=', '>', '>=', '<', '<=', '并且', '或者', '(', ')']
  return props.enableConditionArithmeticOperators ? ['+', '-', '*', '/', ...operators] : operators
}

function insertVariable(row: ProductFormulaVariableVO) {
  if (row.variableName) appendExpressionText(row.variableName)
}

function openVariableEditor(row?: ProductFormulaVariableVO) {
  editingVariable.value = row || null
  variableEditorOpen.value = true
}

async function saveVariable(variable: ProductFormulaVariableVO, rules: ProductFormulaVariableRuleVO[]) {
  if (!props.formulaId) return
  const variables = [...(props.variables || [])]
  const oldCode = editingVariable.value?.variableCode
  const index = variables.findIndex((row) => row.variableCode === variable.variableCode || row.variableId === variable.variableId)
  if (index >= 0) variables.splice(index, 1, variable)
  else variables.push(variable)
  const variableRules = (props.variableRules || []).filter((rule) => rule.variableCode !== variable.variableCode && rule.variableCode !== oldCode)
    .concat(rules.map((rule) => normalizeVariableRule(rule, variables)))
  await productFormulaApi.saveVariables(props.formulaId, { variables, variableRules })
  const response = await productFormulaApi.variables(props.formulaId)
  emit('variables-saved', response.data || { variables, variableRules })
  variableEditorOpen.value = false
  ElMessage.success(t('common.success'))
}

async function removeVariable(row: ProductFormulaVariableVO) {
  if (!props.formulaId) return
  await ElMessageBox.confirm(t('productCenter.formulaSetup.variableRemoveConfirm'), t('common.prompt'), {
    confirmButtonText: t('common.confirm'),
    cancelButtonText: t('common.cancel'),
    type: 'warning'
  })
  const variables = (props.variables || []).filter((item) => item.variableCode !== row.variableCode)
  const variableRules = (props.variableRules || []).filter((rule) => rule.variableCode !== row.variableCode)
  await productFormulaApi.saveVariables(props.formulaId, { variables, variableRules })
  const response = await productFormulaApi.variables(props.formulaId)
  emit('variables-saved', response.data || { variables, variableRules })
}

async function copyVariables() {
  if (!props.formulaId) return
  const { value } = await ElMessageBox.prompt(t('productCenter.formulaSetup.copyVariablesPlaceholder'), t('productCenter.formulaSetup.copyVariables'), {
    confirmButtonText: t('common.confirm'),
    cancelButtonText: t('common.cancel')
  })
  const sourceFormulaCode = String(value || '').trim()
  if (!sourceFormulaCode) return
  await productFormulaApi.copyVariables(props.formulaId, sourceFormulaCode)
  const response = await productFormulaApi.variables(props.formulaId)
  emit('variables-saved', response.data || {})
}

function normalizeVariableRule(rule: ProductFormulaVariableRuleVO, variables: ProductFormulaVariableVO[]) {
  const formulaText = rule.formulaText || rule.formulaExpression
  const conditionText = rule.defaultRuleFlag ? undefined : (rule.conditionText || rule.conditionExpression)
  return {
    ...rule,
    formulaExpression: formulaText ? normalizeDisplayExpression(formulaText, props.options, props.optionValues, props.materials, variables) : undefined,
    conditionExpression: conditionText ? normalizeDisplayExpression(conditionText, props.options, props.optionValues, props.materials, variables) : undefined,
    conditionText,
    formulaText
  }
}

function formulaVariableGroups() {
  const names = new Set(['orderWidthIn', 'orderLengthIn', 'orderWidthCm', 'orderLengthCm', 'orderAreaM2'])
  return [{ title: t('productCenter.formulaSetup.orderVariables'), variables: formulaVariables.filter((item) => names.has(item.name)).map(toInsertVariable) }]
}

function toInsertVariable(variable: { label: string; name: string }): InsertVariable {
  return { label: variable.label, name: variable.name, insert: variable.label }
}

function expressionValidationMessage(message?: string) {
  const map: Record<string, string> = {
    empty: 'productCenter.formulaSetup.expressionEmpty',
    invalidResult: 'productCenter.formulaSetup.expressionInvalidResult',
    conditionMustBeBoolean: 'productCenter.formulaSetup.conditionMustBeBoolean'
  }
  return t(map[message || ''] || 'productCenter.formulaSetup.formulaInvalid')
}
</script>

<style>
@import './FormulaExpressionEditorDialog.css';
</style>
