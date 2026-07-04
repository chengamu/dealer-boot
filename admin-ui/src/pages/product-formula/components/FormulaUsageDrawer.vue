<template>
  <AdminDrawer
    :model-value="modelValue"
    :title="t('productCenter.formulaSetup.usageSetting')"
    size="96vw"
    append-to-body
    variant="wide"
    class="formula-usage-drawer"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    :before-close="handleDrawerBeforeClose"
    @update:model-value="$emit('update:modelValue', $event)"
    @closed="handleDrawerClosed"
  >
    <div v-if="usageRow" class="usage-editor">
      <FormulaUsageMaterialSummary
        v-model:collapsed="summaryCollapsed"
        :usage-row="usageRow"
        :usage-rows="currentUsageRows"
        :unit-options="unitOptions"
        @select-usage-row="$emit('select-usage-row', $event)"
      />

      <div class="usage-mode-card">
        <div>
          <span class="usage-mode-card__label">{{ t('productCenter.formulaSetup.usageMode') }}</span>
          <p>{{ t('productCenter.formulaSetup.usageModeHint') }}</p>
        </div>
        <el-select
          class="usage-mode-card__select"
          :model-value="usageRow.usageMode || 'FIXED'"
          @change="handleUsageModeChange"
        >
          <el-option value="FIXED" :label="t('productCenter.formulaSetup.usageFixedValue')" />
          <el-option value="FORMULA" :label="t('productCenter.formulaSetup.usageFormulaRules')" />
        </el-select>
      </div>

      <FormulaUsageFixedPanel v-if="usageRow.usageMode !== 'FORMULA'" :usage-row="usageRow" @change="syncFixedRuleFromRow" />

      <FormulaUsageRuleGrid
        v-else
        v-model:selected-rule="selectedRule"
        :rules="currentRules"
        :formula-fields="formulaFields"
        :materials="materials || []"
        :options="options"
        :option-values="optionValues"
        :variables="variables || []"
        @generate-fabric-rules="generateFabricRules"
        @add-rule="addConditionalUsageRule"
        @copy-rule="copySelectedRule"
        @remove-rule="removeSelectedRule"
        @default-rule-change="handleDefaultRuleChange"
        @formula-blur="syncFormula"
        @open-expression="openExpressionEditor"
      />
    </div>

    <FormulaExpressionEditorDialog
      v-model="expressionEditorOpen"
      v-model:text="expressionEditorText"
      :target="expressionEditorTarget"
      :formula-id="formulaId"
      :materials="materials || []"
      :options="options"
      :option-values="optionValues"
      :option-materials="optionMaterials"
      :variables="variables || []"
      :variable-rules="variableRules || []"
      @variables-saved="$emit('variables-saved', $event)"
      @confirm="confirmExpressionEditor"
    />
    <template #footer>
      <el-button @click="closeDrawerWithGuard">{{ t('common.cancel') }}</el-button>
      <el-button type="primary" @click="confirmAndClose">{{ t('common.confirm') }}</el-button>
    </template>
  </AdminDrawer>
</template>

<script setup lang="ts">
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import FormulaExpressionEditorDialog from './FormulaExpressionEditorDialog.vue'
import FormulaUsageFixedPanel from './FormulaUsageFixedPanel.vue'
import FormulaUsageMaterialSummary from './FormulaUsageMaterialSummary.vue'
import FormulaUsageRuleGrid from './FormulaUsageRuleGrid.vue'
import { useFormulaUsageEditor } from './useFormulaUsageEditor'
import type {
  ProductFormulaMaterialVO,
  ProductFormulaOptionMaterialVO,
  ProductFormulaOptionVO,
  ProductFormulaOptionValueVO,
  ProductFormulaVariableRuleVO,
  ProductFormulaVariableVO,
  ProductFormulaUsageRuleVO,
  ProductOption
} from '@/api/product-capability/types'

const props = defineProps<{
  modelValue: boolean
  formulaId?: string | number
  usageRow: ProductFormulaMaterialVO | null
  usageRows?: ProductFormulaMaterialVO[]
  materials?: ProductFormulaMaterialVO[]
  variables?: ProductFormulaVariableVO[]
  variableRules?: ProductFormulaVariableRuleVO[]
  usageRules: ProductFormulaUsageRuleVO[]
  options: ProductFormulaOptionVO[]
  optionValues: ProductFormulaOptionValueVO[]
  optionMaterials?: ProductFormulaOptionMaterialVO[]
  unitOptions: ProductOption[]
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'select-usage-row': [row: ProductFormulaMaterialVO]
  'variables-saved': [setup: { variables?: ProductFormulaVariableVO[]; variableRules?: ProductFormulaVariableRuleVO[] }]
}>()

const localeStore = useLocaleStore()
const t = (key: string) => {
  const message = getMessage(key, localeStore.language)
  return message
}

const {
  selectedRule,
  summaryCollapsed,
  expressionEditorOpen,
  expressionEditorText,
  expressionEditorTarget,
  formulaFields,
  currentUsageRows,
  currentRules,
  closeDrawerWithGuard,
  confirmAndClose,
  handleDrawerBeforeClose,
  handleDrawerClosed,
  handleUsageModeChange,
  syncFixedRuleFromRow,
  generateFabricRules,
  addConditionalUsageRule,
  copySelectedRule,
  removeSelectedRule,
  handleDefaultRuleChange,
  syncFormula,
  openExpressionEditor,
  confirmExpressionEditor
} = useFormulaUsageEditor(props, () => {
  emit('update:modelValue', false)
})
</script>

<style scoped>
.usage-editor {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.usage-mode-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 10px 12px;
  background: #fff;
  border: 1px solid #e5ecf6;
  border-radius: 8px;
}

.usage-mode-card__label {
  color: #111827;
  font-size: 15px;
  font-weight: 700;
}

.usage-mode-card p {
  margin: 4px 0 0;
  color: #6b7280;
  font-size: 12px;
}

.usage-mode-card__select {
  width: 180px;
  flex: 0 0 auto;
}

.usage-mode-card__select :deep(.el-select__wrapper) {
  min-height: 34px;
  border-radius: 6px;
}

</style>
