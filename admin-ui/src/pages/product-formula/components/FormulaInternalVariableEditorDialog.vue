<template>
  <AdminDialog
    :model-value="modelValue"
    :title="t('productCenter.formulaSetup.variableEditor')"
    width="840px"
    class="variable-editor-dialog"
    append-to-body
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <div class="variable-editor">
      <el-form label-width="78px">
        <div class="variable-editor__base">
          <el-form-item :label="t('productCenter.formulaSetup.variableName')">
            <el-input v-model="draft.variableName" @blur="fillCode" />
          </el-form-item>
          <el-form-item :label="t('productCenter.formulaSetup.variableCode')">
            <el-input v-model="draft.variableCode" />
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
            <el-input
              v-model="row.conditionText"
              :disabled="row.defaultRuleFlag"
              :placeholder="conditionPlaceholder(row)"
            />
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
            <el-input-number v-if="row.valueType !== 'FORMULA'" v-model="row.fixedValue" :precision="2" controls-position="right" />
            <el-input v-else v-model="row.formulaText" :placeholder="t('productCenter.formulaSetup.usageFormulaPlaceholder')" />
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
    <template #footer>
      <AdminDialogFooter>
        <el-button @click="$emit('update:modelValue', false)">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="confirm">{{ t('common.confirm') }}</el-button>
      </AdminDialogFooter>
    </template>
  </AdminDialog>
</template>

<script setup lang="ts">
import { watch, reactive, ref } from 'vue'
import AdminTableActions, { type AdminTableAction } from '@/components/AdminTableActions/index.vue'
import type { ProductFormulaVariableRuleVO, ProductFormulaVariableVO } from '@/api/product-capability/types'

type DraftRule = ProductFormulaVariableRuleVO & { __key: string }

const props = defineProps<{
  modelValue: boolean
  variable?: ProductFormulaVariableVO | null
  rules: ProductFormulaVariableRuleVO[]
  t: (key: string) => string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  save: [variable: ProductFormulaVariableVO, rules: ProductFormulaVariableRuleVO[]]
}>()

const draft = reactive<ProductFormulaVariableVO>({})
const draftRules = ref<DraftRule[]>([])

watch(() => props.modelValue, (open) => {
  if (!open) return
  Object.assign(draft, { ...(props.variable || {}), sortOrder: props.variable?.sortOrder ?? 10 })
  draftRules.value = props.rules.length
    ? props.rules.map((rule, index) => ({ ...rule, __key: String(rule.ruleId || index) }))
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
  return { __key: `${Date.now()}-${Math.random()}`, valueType: 'FIXED', fixedValue: 0, defaultRuleFlag, sortOrder: draftRules.value.length * 10 + 10 }
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

function conditionPlaceholder(row: DraftRule) {
  return row.defaultRuleFlag
    ? props.t('productCenter.formulaSetup.defaultUsageRule')
    : props.t('productCenter.formulaSetup.valueConditionPlaceholder')
}

function fillCode() {
  if (draft.variableCode || !draft.variableName) return
  const normalized = String(draft.variableName).trim().toUpperCase().replace(/[^A-Z0-9]+/g, '_').replace(/^_+|_+$/g, '')
  draft.variableCode = normalized || `VAR_${Date.now()}`
}

function confirm() {
  fillCode()
  if (!draftRules.value.some((rule) => rule.defaultRuleFlag) && draftRules.value[0]) draftRules.value[0].defaultRuleFlag = true
  const variable = { ...draft }
  const rules = draftRules.value.map(({ __key, ...rule }) => ({ ...rule, variableCode: variable.variableCode }))
  emit('save', variable, rules)
}
</script>

<style scoped>
.variable-editor {
  display: grid;
  gap: 14px;
}

.variable-editor__base {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px 18px;
  align-items: start;
}

.variable-editor__base :deep(.el-form-item) {
  margin-bottom: 0;
}

.variable-editor__rule-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.variable-editor__rule-head p {
  margin: 3px 0 0;
  color: var(--el-text-color-secondary);
  font-size: 12px;
  line-height: 1.4;
}

.variable-editor :deep(.el-input-number) {
  width: 100%;
}

.variable-rule-table {
  border-radius: 8px;
  overflow: hidden;
}

.variable-rule-table :deep(.el-table__header th) {
  background: #f7faff !important;
  color: #1f2937;
  font-weight: 700;
}

.variable-rule-table :deep(.el-table__cell) {
  padding: 8px 0;
}

.variable-rule-table :deep(.el-input .el-input__wrapper),
.variable-rule-table :deep(.el-input-number .el-input__wrapper),
.variable-rule-table :deep(.el-select .el-select__wrapper) {
  min-height: 32px;
  border-radius: 6px;
}
</style>
