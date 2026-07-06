<template>
  <AdminDialog
    :model-value="modelValue"
    :title="t('productCenter.formulaSetup.internalVariables')"
    width="960px"
    class="variable-manager-dialog"
    append-to-body
    :close-on-press-escape="false"
    :close-on-click-modal="false"
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <FormulaInternalVariableGrid
      :variables="variables || []"
      :variable-rules="variableRules || []"
      :t="t"
      @add="openVariableEditor()"
      @edit="openVariableEditor"
      @remove="removeVariable"
      @copy="copyVariables"
    />
    <FormulaInternalVariableEditorDialog
      v-model="variableEditorOpen"
      :variable="editingVariable"
      :rules="editingVariableRules"
      :variables="variables || []"
      :materials="materials || []"
      :options="options || []"
      :option-values="optionValues || []"
      :option-materials="optionMaterials || []"
      :t="t"
      @save="saveVariable"
    />
  </AdminDialog>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { productFormulaApi } from '@/api/product-formula/formula'
import FormulaInternalVariableEditorDialog from './FormulaInternalVariableEditorDialog.vue'
import FormulaInternalVariableGrid from './FormulaInternalVariableGrid.vue'
import { normalizeDisplayExpression } from './formulaExpressionDisplay'
import type {
  ProductFormulaMaterialVO,
  ProductFormulaOptionMaterialVO,
  ProductFormulaOptionVO,
  ProductFormulaOptionValueVO,
  ProductFormulaSetupVO,
  ProductFormulaVariableRuleVO,
  ProductFormulaVariableVO
} from '@/api/product-capability/types'

const props = defineProps<{
  modelValue: boolean
  formulaId?: string | number
  variables?: ProductFormulaVariableVO[]
  variableRules?: ProductFormulaVariableRuleVO[]
  materials?: ProductFormulaMaterialVO[]
  options?: ProductFormulaOptionVO[]
  optionValues?: ProductFormulaOptionValueVO[]
  optionMaterials?: ProductFormulaOptionMaterialVO[]
  t: (key: string) => string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'variables-saved': [value: ProductFormulaSetupVO]
}>()

const variableEditorOpen = ref(false)
const editingVariable = ref<ProductFormulaVariableVO | null>(null)
const editingVariableRules = computed(() => (props.variableRules || []).filter((rule) => sameVariableRef(rule, editingVariable.value)))

function openVariableEditor(row?: ProductFormulaVariableVO) {
  editingVariable.value = row || null
  variableEditorOpen.value = true
}

async function saveVariable(variable: ProductFormulaVariableVO, rules: ProductFormulaVariableRuleVO[]) {
  if (!props.formulaId) return
  const variables = [...(props.variables || [])]
  variable.variableKey = variable.variableKey || editingVariable.value?.variableKey || newVariableKey()
  const oldCode = editingVariable.value?.variableCode
  const oldKey = editingVariable.value?.variableKey
  const index = variables.findIndex((row) => row.variableKey === variable.variableKey || row.variableId === variable.variableId)
  if (index >= 0) variables.splice(index, 1, variable)
  else variables.push(variable)
  const variableRules = (props.variableRules || [])
    .filter((rule) => rule.variableKey !== variable.variableKey && rule.variableKey !== oldKey && rule.variableCode !== oldCode)
    .concat(rules.map((rule) => normalizeVariableRule(rule, variable, variables)))
  await productFormulaApi.saveVariables(props.formulaId, { variables, variableRules })
  const response = await productFormulaApi.variables(props.formulaId)
  emit('variables-saved', response.data || { variables, variableRules })
  variableEditorOpen.value = false
  ElMessage.success(props.t('common.success'))
}

async function removeVariable(row: ProductFormulaVariableVO) {
  if (!props.formulaId) return
  await ElMessageBox.confirm(props.t('productCenter.formulaSetup.variableRemoveConfirm'), props.t('common.prompt'), {
    confirmButtonText: props.t('common.confirm'),
    cancelButtonText: props.t('common.cancel'),
    type: 'warning'
  })
  const variables = (props.variables || []).filter((item) => !sameVariableRef(item, row))
  const variableRules = (props.variableRules || []).filter((rule) => !sameVariableRef(rule, row))
  await productFormulaApi.saveVariables(props.formulaId, { variables, variableRules })
  const response = await productFormulaApi.variables(props.formulaId)
  emit('variables-saved', response.data || { variables, variableRules })
}

async function copyVariables() {
  if (!props.formulaId) return
  const { value } = await ElMessageBox.prompt(props.t('productCenter.formulaSetup.copyVariablesPlaceholder'), props.t('productCenter.formulaSetup.copyVariables'), {
    confirmButtonText: props.t('common.confirm'),
    cancelButtonText: props.t('common.cancel')
  })
  const sourceFormulaCode = String(value || '').trim()
  if (!sourceFormulaCode) return
  await productFormulaApi.copyVariables(props.formulaId, sourceFormulaCode)
  const response = await productFormulaApi.variables(props.formulaId)
  emit('variables-saved', response.data || {})
}

function normalizeVariableRule(rule: ProductFormulaVariableRuleVO, variable: ProductFormulaVariableVO, variables: ProductFormulaVariableVO[]) {
  const formulaText = rule.formulaText || rule.formulaExpression
  const conditionText = rule.defaultRuleFlag ? undefined : (rule.conditionText || rule.conditionExpression)
  return {
    ...rule,
    variableKey: variable.variableKey,
    variableCode: variable.variableCode,
    formulaExpression: formulaText ? normalizeDisplayExpression(formulaText, props.options, props.optionValues, props.materials, variables) : undefined,
    conditionExpression: conditionText ? normalizeDisplayExpression(conditionText, props.options, props.optionValues, props.materials, variables) : undefined,
    conditionText,
    formulaText
  }
}

function sameVariableRef(left?: { variableKey?: string; variableCode?: string; variableId?: number }, right?: { variableKey?: string; variableCode?: string; variableId?: number } | null) {
  if (!left || !right) return false
  if (left.variableKey && right.variableKey) return left.variableKey === right.variableKey
  if (left.variableId && right.variableId) return left.variableId === right.variableId
  return Boolean(left.variableCode && right.variableCode && left.variableCode === right.variableCode)
}

function newVariableKey() {
  return `V_${Date.now().toString(36)}_${Math.random().toString(36).slice(2, 8)}`.toUpperCase()
}
</script>

<style scoped>
.variable-manager-dialog :deep(.internal-variable-grid) {
  min-height: 520px;
}
</style>
