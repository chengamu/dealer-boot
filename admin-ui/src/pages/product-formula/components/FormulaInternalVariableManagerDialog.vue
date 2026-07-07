<template>
  <AdminDialog
    :model-value="modelValue"
    :title="t('productCenter.formulaSetup.internalVariables')"
    width="92vw"
    class="variable-manager-dialog"
    append-to-body
    :close-on-press-escape="false"
    :close-on-click-modal="false"
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <div class="variable-manager">
      <FormulaInternalVariableGrid
        class="variable-manager__list"
        :variables="variables || []"
        :variable-rules="variableRules || []"
        :selected-variable="editingVariable"
        :t="t"
        @add="openVariableEditor()"
        @edit="openVariableEditor"
        @remove="removeVariable"
        @copy="copyVariables"
      />
      <FormulaInternalVariableEditorDialog
        v-model="variableEditorOpen"
        class="variable-manager__editor"
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
      <div v-if="!variableEditorOpen" class="variable-manager__empty">
        {{ t('productCenter.formulaSetup.selectVariableToEdit') }}
      </div>
    </div>
  </AdminDialog>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue'
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

watch(() => props.modelValue, (open) => {
  if (open) addEscapeListeners()
  else removeEscapeListeners()
}, { immediate: true })

onBeforeUnmount(removeEscapeListeners)

function openVariableEditor(row?: ProductFormulaVariableVO) {
  editingVariable.value = row || null
  variableEditorOpen.value = true
}

function stopEscapeEvent(event: KeyboardEvent) {
  if (event.key !== 'Escape') return
  event.preventDefault()
  event.stopPropagation()
  event.stopImmediatePropagation()
}

function addEscapeListeners() {
  window.addEventListener('keydown', stopEscapeEvent, true)
  window.addEventListener('keyup', stopEscapeEvent, true)
  document.addEventListener('keydown', stopEscapeEvent, true)
  document.addEventListener('keyup', stopEscapeEvent, true)
}

function removeEscapeListeners() {
  window.removeEventListener('keydown', stopEscapeEvent, true)
  window.removeEventListener('keyup', stopEscapeEvent, true)
  document.removeEventListener('keydown', stopEscapeEvent, true)
  document.removeEventListener('keyup', stopEscapeEvent, true)
}

async function saveVariable(variable: ProductFormulaVariableVO, rules: ProductFormulaVariableRuleVO[]) {
  if (!props.formulaId) return
  const latest = await productFormulaApi.variables(props.formulaId)
  const latestSetup = latest.data || {}
  const variables = [...(latestSetup.variables?.length ? latestSetup.variables : (props.variables || []))]
  variable.variableKey = variable.variableKey || editingVariable.value?.variableKey || newVariableKey()
  const index = variables.findIndex((row) => sameVariableRef(row, variable) || sameVariableRef(row, editingVariable.value))
  if (index >= 0) variables.splice(index, 1, variable)
  else variables.push(variable)
  const baseRules = latestSetup.variableRules?.length ? latestSetup.variableRules : (props.variableRules || [])
  const variableRules = baseRules
    .filter((rule) => !sameVariableRef(rule, variable) && !sameVariableRef(rule, editingVariable.value))
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
  if (left.variableId && right.variableId) return String(left.variableId) === String(right.variableId)
  return Boolean(left.variableCode && right.variableCode && left.variableCode === right.variableCode)
}

function newVariableKey() {
  return `V_${Date.now().toString(36)}_${Math.random().toString(36).slice(2, 8)}`.toUpperCase()
}
</script>

<style scoped>
.variable-manager {
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr);
  gap: 12px;
  align-items: stretch;
  min-height: min(640px, calc(100vh - 180px));
}

.variable-manager__list {
  min-height: min(640px, calc(100vh - 180px));
}

.variable-manager__editor {
  min-width: 0;
}

.variable-manager__empty {
  display: grid;
  place-items: center;
  min-height: min(640px, calc(100vh - 180px));
  border: 1px dashed #d9e2ef;
  border-radius: 8px;
  color: #8a95a6;
  background: #f8fbff;
}

:global(.variable-manager-dialog.admin-dialog.el-dialog) {
  width: min(1480px, calc(100vw - 56px)) !important;
}

:global(.variable-manager-dialog.admin-dialog .el-dialog__body) {
  padding: 10px 16px 16px;
}
</style>
