<template>
  <div class="app-container formula-setup-page">
    <FormulaSetupHeader
      :formula="formula"
      :draft-version-label="draftVersionLabel"
      :saving="saving"
      :validating="validating"
      :format-number="formatNumber"
      :format-minute="formatMinute"
      :status-text="statusText"
      :validation-text="validationText"
      :status-tag-type="statusTagType"
      :validation-tag-type="validationTagType"
      :formula-options="editableFormulaOptions"
      :selected-formula-id="selectedFormulaId"
      :formula-selecting="formulaSelecting"
      :active-section="activeTab"
      :can-operate="Boolean(currentFormulaId)"
      :draft-cache-status="draftCacheStatus"
      @formula-change="handleFormulaChange"
      @validate="validateSetup"
      @save="saveSetup"
    />

    <el-empty
      v-if="!currentFormulaId"
      class="formula-setup-page__empty"
      :description="t('productCenter.formulaSetup.selectEditableFormulaHint')"
      :image-size="120"
    />

    <template v-else-if="activeTab === 'content'">
      <FormulaSetupSummary
        :material-group-cards="materialGroupCards"
        :material-count="setup.materials.length"
        :unset-usage-count="unsetUsageCount"
        :option-count="setup.options.length"
        :rule-count="setup.optionMaterials.length"
        :exception-count="setup.restrictions.length"
      />

      <FormulaMaterialPoolTab
        :loading="loading"
        :materials="setup.materials"
        :usage-summary="usageSummary"
        :usage-unset="isUsageUnset"
        :unit-label="unitLabel"
        :group-sort-map="groupSortMap"
        :material-group-cards="materialGroupCards"
        :material-count="setup.materials.length"
        :unset-usage-count="unsetUsageCount"
        :exception-count="setup.restrictions.length"
        @open-picker="materialPickerOpen = true"
        @open-usage="openUsage"
        @open-batch-usage="openBatchUsage"
        @remove-material="removeMaterial"
        @remove-materials="removeMaterials"
      />
    </template>

    <FormulaBusinessOptionsTab
      v-else
      :options="setup.options"
      :option-values="selectedValues"
      :all-option-values="setup.optionValues"
      :option-materials="selectedOptionMaterials"
      :all-option-materials="setup.optionMaterials"
      :restrictions="setup.restrictions"
      :usage-rules="setup.usageRules"
      :variable-rules="setup.variableRules"
      :selected-option-code="selectedOptionCode"
      :materials="setup.materials"
      :material-label="materialLabel"
      @add-option="addOptionRow"
      @option-change="handleOptionChange"
      @remove-option="removeOption"
      @add-option-value="addOptionValueRow"
      @remove-option-value="removeSelectedValue"
      @add-option-material="addOptionMaterialRow"
      @sync-option-material="syncOptionMaterial"
      @remove-option-material="removeSelectedOptionMaterial"
      @add-restriction="addRestrictionRow"
      @remove-restriction="removeRow(setup.restrictions, $event)"
    />

    <FormulaMaterialPickerDialog
      v-model="materialPickerOpen"
      :material-rows="materialRows"
      :group-options="groupOptions"
      :material-type-rows="materialTypeRows"
      :unit-label="unitLabel"
      @confirm="appendSelectedMaterials"
    />

    <FormulaUsageDrawer
      v-model="usageDrawerOpen"
      :usage-row="usageRow"
      :usage-rows="usageRows"
      :materials="setup.materials"
      :formula-id="currentFormulaId"
      :variables="setup.variables"
      :variable-rules="setup.variableRules"
      :usage-rules="setup.usageRules"
      :options="setup.options"
      :option-values="setup.optionValues"
      :option-materials="setup.optionMaterials"
      :unit-options="unitOptions"
      @select-usage-row="selectUsageRow"
      @variables-saved="handleVariablesSaved"
    />
  </div>
</template>

<script setup lang="ts" name="ProductFormulaSetupPage">
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import FormulaBusinessOptionsTab from './components/FormulaBusinessOptionsTab.vue'
import FormulaMaterialPoolTab from './components/FormulaMaterialPoolTab.vue'
import FormulaMaterialPickerDialog from './components/FormulaMaterialPickerDialog.vue'
import FormulaSetupHeader from './components/FormulaSetupHeader.vue'
import FormulaSetupSummary from './components/FormulaSetupSummary.vue'
import FormulaUsageDrawer from './components/FormulaUsageDrawer.vue'
import { useFormulaSetupCore } from './composables/useFormulaSetupCore'
import { useFormulaSetupOperations } from './composables/useFormulaSetupOperations'

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const props = defineProps<{
  setupSection?: 'content' | 'options'
}>()

const {
  activeTab,
  loading,
  saving,
  validating,
  formulaSelecting,
  materialPickerOpen,
  usageDrawerOpen,
  usageRow,
  usageRows,
  selectedFormulaId,
  currentFormulaId,
  selectedOptionCode,
  materialRows,
  groupRows,
  materialTypeRows,
  unitRows,
  setup,
  formula,
  draftVersionLabel,
  editableFormulaOptions,
  groupOptions,
  groupSortMap,
  unitOptions,
  materialGroupCards,
  draftCacheStatus,
  saveSetup,
  validateSetup,
  handleFormulaChange,
  handleVariablesSaved
} = useFormulaSetupCore(props, t, () => localeStore.language)

const {
  selectedValues,
  selectedOptionMaterials,
  unsetUsageCount,
  appendSelectedMaterials,
  addOptionRow,
  addOptionValueRow,
  addOptionMaterialRow,
  addRestrictionRow,
  removeOption,
  removeSelectedValue,
  removeSelectedOptionMaterial,
  removeMaterial,
  removeMaterials,
  removeRow,
  handleOptionChange,
  openUsage,
  openBatchUsage,
  selectUsageRow,
  syncOptionMaterial,
  usageSummary,
  isUsageUnset,
  statusText,
  validationText,
  statusTagType,
  validationTagType,
  materialLabel,
  unitLabel,
  formatNumber,
  formatMinute
} = useFormulaSetupOperations({
  setup,
  formula,
  selectedOptionCode,
  usageRow,
  usageRows,
  usageDrawerOpen,
  materialPickerOpen,
  groupRows,
  groupSortMap,
  unitRows,
  language: () => localeStore.language,
  t
})
</script>

<style scoped>
.formula-setup-page {
  background: var(--admin-bg);
}

.formula-setup-page__empty {
  min-height: 360px;
  margin-top: 16px;
  background: #fff;
  border: 1px solid #e6ebf2;
  border-radius: 8px;
}
</style>
