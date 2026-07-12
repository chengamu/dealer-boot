<template>
  <section v-if="modelValue" class="variable-editor-panel">
    <div class="variable-editor-panel__head">
      <strong>{{ t('productCenter.formulaSetup.variableEditor') }}</strong>
    </div>
    <div class="variable-editor">
      <el-form label-width="78px">
        <div class="variable-editor__base">
          <el-form-item :label="t('productCenter.formulaSetup.variableName')">
            <el-input v-model="draft.variableName" />
          </el-form-item>
          <el-form-item :label="t('productCenter.formulaSetup.variableCode')">
            <el-input v-model="draft.variableCode" disabled />
          </el-form-item>
          <el-form-item :label="t('productCenter.common.sortOrder')">
            <el-input-number v-model="draft.sortOrder" :min="0" controls-position="right" />
          </el-form-item>
          <el-form-item :label="t('productCenter.common.remark')">
            <el-input v-model="draft.remark" />
          </el-form-item>
        </div>
      </el-form>

      <div class="variable-editor__rule-head">
        <div>
          <strong>{{ t('productCenter.formulaSetup.variableRules') }}</strong>
          <p>{{ t('productCenter.formulaSetup.variableRulesHint') }}</p>
        </div>
        <el-button size="small" type="primary" plain @click="addRule">{{ t('common.add') }}</el-button>
      </div>
      <el-table :data="draftRules" border size="small" row-key="__key" class="variable-rule-table">
        <el-table-column :label="t('productCenter.formulaSetup.valueCondition')" min-width="180">
          <template #default="{ row }">
            <div class="variable-editor__condition-cell">
              <el-input
                v-model="row.conditionText"
                :disabled="row.defaultRuleFlag"
                :placeholder="conditionPlaceholder(row)"
              />
              <el-button v-if="!row.defaultRuleFlag" plain @click="openConditionEditor(row)">
                {{ t('productCenter.formulaSetup.conditionExpressionEditor') }}
              </el-button>
            </div>
          </template>
        </el-table-column>
        <el-table-column :label="t('productCenter.formulaSetup.valueType')" width="112">
          <template #default="{ row }">
            <el-select v-model="row.valueType">
              <el-option value="FIXED" :label="t('productCenter.formulaSetup.fixedValue')" />
              <el-option value="FORMULA" :label="t('productCenter.formulaSetup.formulaValue')" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column :label="t('productCenter.formulaSetup.valueSummary')" min-width="210">
          <template #default="{ row }">
            <BusinessNumberInput v-if="row.valueType !== 'FORMULA'" v-model="row.fixedValue" mode="QUANTITY" :unit-precision="6" :allow-negative="true" />
            <div v-else class="variable-editor__formula-cell">
              <el-input v-model="row.formulaText" :placeholder="t('productCenter.formulaSetup.usageFormulaPlaceholder')" />
              <el-button plain @click="openFormulaEditor(row)">{{ t('productCenter.formulaSetup.formulaSelectorShort') }}</el-button>
            </div>
          </template>
        </el-table-column>
        <el-table-column :label="t('productCenter.formulaSetup.defaultUsageRule')" width="84" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.defaultRuleFlag" @change="handleDefaultChange(row)" />
          </template>
        </el-table-column>
        <el-table-column :label="t('common.operate')" width="96" align="center" class-name="small-padding fixed-width">
          <template #default="{ $index }">
            <AdminTableActions :actions="ruleActions($index)" />
          </template>
        </el-table-column>
      </el-table>
    </div>
    <FormulaVariableFormulaEditorDialog
      v-model="formulaEditorOpen"
      v-model:text="formulaEditorText"
      :variables="variables"
      :current-variable="draft"
      :t="t"
      @confirm="confirmFormulaEditor"
    />
    <FormulaExpressionEditorDialog
      v-model="conditionEditorOpen"
      v-model:text="conditionEditorText"
      target="condition"
      :materials="materials || []"
      :options="options || []"
      :option-values="optionValues || []"
      :option-materials="optionMaterials || []"
      :variables="variables"
      @confirm="confirmConditionEditor"
    />
    <div class="variable-editor-panel__footer">
      <el-button @click="$emit('update:modelValue', false)">{{ t('common.cancel') }}</el-button>
      <el-button type="primary" @click="confirm">{{ t('common.confirm') }}</el-button>
    </div>
  </section>
</template>

<script setup lang="ts">
import { watch, reactive, ref } from 'vue'
import AdminTableActions, { type AdminTableAction } from '@/components/AdminTableActions/index.vue'
import FormulaExpressionEditorDialog from './FormulaExpressionEditorDialog.vue'
import FormulaVariableFormulaEditorDialog from './FormulaVariableFormulaEditorDialog.vue'
import { validateConditionExpression } from '../utils/formulaExpression'
import { normalizeDisplayExpression } from './formulaExpressionDisplay'
import type {
  ProductFormulaMaterialVO,
  ProductFormulaOptionMaterialVO,
  ProductFormulaOptionVO,
  ProductFormulaOptionValueVO,
  ProductFormulaVariableRuleVO,
  ProductFormulaVariableVO
} from '@/api/product-capability/types'

type DraftRule = ProductFormulaVariableRuleVO & { __key: string }

const props = defineProps<{
  modelValue: boolean
  variable?: ProductFormulaVariableVO | null
  rules: ProductFormulaVariableRuleVO[]
  variables: ProductFormulaVariableVO[]
  materials?: ProductFormulaMaterialVO[]
  options?: ProductFormulaOptionVO[]
  optionValues?: ProductFormulaOptionValueVO[]
  optionMaterials?: ProductFormulaOptionMaterialVO[]
  t: (key: string) => string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  save: [variable: ProductFormulaVariableVO, rules: ProductFormulaVariableRuleVO[]]
}>()

const draft = reactive<ProductFormulaVariableVO>({})
const draftRules = ref<DraftRule[]>([])
const formulaEditorOpen = ref(false)
const formulaEditorText = ref('')
const formulaEditorRule = ref<DraftRule>()
const conditionEditorOpen = ref(false)
const conditionEditorText = ref('')
const conditionEditorRule = ref<DraftRule>()
watch(() => [props.modelValue, props.variable] as const, ([open]) => {
  if (!open) return
  Object.keys(draft).forEach((key) => delete (draft as Record<string, unknown>)[key])
  Object.assign(draft, { ...(props.variable || {}), sortOrder: props.variable?.sortOrder ?? 10 })
  draft.variableCode = draft.variableCode || newVariableCode()
  draftRules.value = props.rules.length
    ? props.rules.map((rule, index) => ({ ...rule, formulaText: rule.formulaText || rule.formulaExpression, __key: String(rule.ruleId || index) }))
    : [emptyRule(true)]
})

function addRule() {
  draftRules.value.push(emptyRule(false))
}

function removeRule(index: number) {
  draftRules.value.splice(index, 1)
}

function ruleActions(index: number): AdminTableAction[] {
  return [
    {
      label: props.t('common.delete'),
      icon: 'Delete',
      type: 'danger',
      danger: true,
      disabled: draftRules.value.length <= 1,
      onClick: () => removeRule(index)
    }
  ]
}

function emptyRule(defaultRuleFlag: boolean): DraftRule {
  return { __key: `${Date.now()}-${Math.random()}`, valueType: 'FIXED', fixedValue: '0', defaultRuleFlag, sortOrder: draftRules.value.length * 10 + 10 }
}

function handleDefaultChange(row: DraftRule) {
  if (!row.defaultRuleFlag) {
    if (!draftRules.value.some((item) => item.defaultRuleFlag)) row.defaultRuleFlag = true
    return
  }
  row.conditionText = undefined
  row.conditionExpression = undefined
  draftRules.value.forEach((item) => {
    if (item !== row) item.defaultRuleFlag = false
  })
}

function openFormulaEditor(row: DraftRule) {
  formulaEditorRule.value = row
  formulaEditorText.value = row.formulaText || row.formulaExpression || ''
  formulaEditorOpen.value = true
}

function confirmFormulaEditor() {
  if (formulaEditorRule.value) formulaEditorRule.value.formulaText = formulaEditorText.value
  formulaEditorOpen.value = false
}

function openConditionEditor(row: DraftRule) {
  conditionEditorRule.value = row
  conditionEditorText.value = row.conditionText || row.conditionExpression || ''
  conditionEditorOpen.value = true
}

function confirmConditionEditor() {
  const row = conditionEditorRule.value
  if (!row) return
  row.conditionText = conditionEditorText.value
  row.conditionExpression = normalizeDisplayExpression(conditionEditorText.value, props.options || [], props.optionValues || [], props.materials || [], props.variables)
  if (!validateConditionExpression(row.conditionExpression).valid) row.conditionExpression = undefined
  conditionEditorOpen.value = false
}

function conditionPlaceholder(row: DraftRule) {
  return row.defaultRuleFlag
    ? props.t('productCenter.formulaSetup.defaultUsageRule')
    : props.t('productCenter.formulaSetup.valueConditionPlaceholder')
}

function confirm() {
  draft.variableCode = draft.variableCode || newVariableCode()
  draft.variableKey = draft.variableKey || `V_${Date.now().toString(36)}_${Math.random().toString(36).slice(2, 8)}`.toUpperCase()
  if (!draftRules.value.some((rule) => rule.defaultRuleFlag) && draftRules.value[0]) draftRules.value[0].defaultRuleFlag = true
  const variable = { ...draft }
  const rules = draftRules.value.map(({ __key, ...rule }) => ({ ...rule, variableKey: variable.variableKey, variableCode: variable.variableCode }))
  emit('save', variable, rules)
}

function newVariableCode() {
  return `VAR_${Date.now().toString(36)}_${Math.random().toString(36).slice(2, 6)}`.toUpperCase()
}
</script>

<style scoped src="./FormulaInternalVariableEditorDialog.css"></style>
