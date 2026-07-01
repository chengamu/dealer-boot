<template>
  <FormulaOptionOverview
    :option-count="options.length"
    :option-value-count="allOptionValues.length"
    :linked-material-count="allOptionMaterials.length"
    :restriction-count="restrictions.length"
    :exception-count="businessExceptionCount"
  />

  <section class="option-workbench">
    <FormulaOptionTree
      :nodes="visibleOptionTreeViewNodes"
      :total-count="optionTreeNodes.length"
      :selected-node-id="selectedNodeId"
      @add-root-option="$emit('add-option')"
      @select-node="selectTreeNodeById"
      @toggle-node="toggleTreeNodeById"
    />

    <main class="option-node-panel">
      <template v-if="selectedOption">
        <FormulaOptionNodeEditor
          :selected-node="selectedNode"
          :selected-option="selectedOption"
          :selected-value="selectedValue"
          :current-option-index="currentOptionIndex"
          :sibling-options="siblingOptions"
          :material-group-options="materialGroupOptions"
          :t="t"
          :visibility-text="visibilityText"
          :source-group-code="sourceGroupCode"
          @move-option="moveSelectedOption"
          @add-child-option="addChildOptionFromValue"
          @remove-option="removeSelectedOption"
          @source-type-change="handleSourceTypeChange"
          @source-group-change="changeSourceGroup"
        />

        <div class="option-edit-grid">
          <FormulaOptionValueGrid
            :selected-option="selectedOption"
            :selected-option-code="selectedOptionCode"
            :selected-value-code="selectedValueCode"
            :option-values="optionValues"
            :option-materials="optionMaterials"
            @import-values="openValueImportDialog"
            @add-value="$emit('add-option-value')"
            @select-value="selectOptionValue"
            @set-default="setDefaultValue"
            @manage-material="openValueMaterialDialog"
            @remove-value="removeOptionValue"
          />
        </div>
      </template>

      <el-empty v-else :description="t('productCenter.formulaSetup.selectOptionHint')" :image-size="96" />
    </main>
  </section>

  <FormulaRestrictionBand
    :restrictions="restrictions"
    :options="options"
    :all-option-values="allOptionValues"
    @add-restriction="$emit('add-restriction')"
    @remove-restriction="$emit('remove-restriction', $event)"
  />

  <FormulaOptionValueDialogs
    :value-import-open="valueImportOpen"
    :value-import-keyword="valueImportKeyword"
    :import-material-rows="importMaterialRows"
    :importable-source-materials="importableSourceMaterials"
    :value-material-open="valueMaterialOpen"
    :value-material-codes="valueMaterialCodes"
    :selected-option="selectedOption"
    :selected-value-name="selectedValueName"
    :value-material-dialog-title="valueMaterialDialogTitle"
    :materials="materials"
    :material-label="materialLabel"
    :source-text="sourceText"
    :t="t"
    @update:value-import-open="valueImportOpen = $event"
    @update:value-import-keyword="valueImportKeyword = $event"
    @update:import-material-rows="importMaterialRows = $event"
    @update:value-material-open="valueMaterialOpen = $event"
    @update:value-material-codes="valueMaterialCodes = $event"
    @append-imported-material-values="appendImportedMaterialValues"
    @save-value-materials="saveValueMaterials"
  />
</template>

<script setup lang="ts">
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { computed, ref } from 'vue'
import FormulaOptionNodeEditor from './FormulaOptionNodeEditor.vue'
import FormulaOptionOverview from './FormulaOptionOverview.vue'
import FormulaOptionTree from './FormulaOptionTree.vue'
import FormulaOptionValueDialogs from './FormulaOptionValueDialogs.vue'
import FormulaOptionValueGrid from './FormulaOptionValueGrid.vue'
import FormulaRestrictionBand from './FormulaRestrictionBand.vue'
import { useFormulaOptionSource } from './useFormulaOptionSource'
import { useFormulaOptionTree } from './useFormulaOptionTree'
import { useFormulaOptionValueDialogs } from './useFormulaOptionValueDialogs'
import type {
  ProductFormulaMaterialVO,
  ProductFormulaOptionMaterialVO,
  ProductFormulaOptionVO,
  ProductFormulaOptionValueVO,
  ProductFormulaRestrictionVO
} from '@/api/product-capability/types'

type ChildOptionPayload = {
  optionCode?: string
  optionNameCn?: string
  valueCode?: string
  valueNameCn?: string
}

const props = defineProps<{
  options: ProductFormulaOptionVO[]
  optionValues: ProductFormulaOptionValueVO[]
  allOptionValues: ProductFormulaOptionValueVO[]
  optionMaterials: ProductFormulaOptionMaterialVO[]
  allOptionMaterials: ProductFormulaOptionMaterialVO[]
  restrictions: ProductFormulaRestrictionVO[]
  selectedOptionCode: string
  materials: ProductFormulaMaterialVO[]
  materialLabel: (row: ProductFormulaMaterialVO) => string
}>()

const emit = defineEmits<{
  'add-option': [parent?: ChildOptionPayload]
  'option-change': [row?: ProductFormulaOptionVO]
  'move-option': [optionCode: string, direction: 'UP' | 'DOWN']
  'remove-option': [index: number]
  'add-option-value': []
  'remove-option-value': [index: number]
  'add-option-material': [valueCode?: string]
  'sync-option-material': [row: ProductFormulaOptionMaterialVO]
  'remove-option-material': [index: number]
  'add-restriction': []
  'remove-restriction': [index: number]
}>()

const localeStore = useLocaleStore()
const t = (key: string, params?: Record<string, string | number>) => {
  const message = getMessage(key, localeStore.language)
  if (!params) return message
  return Object.entries(params).reduce((text, [name, value]) => text.replaceAll(`{${name}}`, String(value)), message)
}
const {
  selectedValueCode,
  selectedNodeId,
  selectedOption,
  selectedValue,
  selectedNode,
  siblingOptions,
  currentOptionIndex,
  visibleOptionTreeViewNodes,
  optionTreeNodes,
  valuesForOption,
  materialsForValue,
  siblingOptionsFor,
  valueNodeId,
  selectOptionValue,
  selectTreeNodeById,
  toggleTreeNodeById
} = useFormulaOptionTree(props, {
  t,
  sourceText: (option) => sourceText(option),
  onOptionChange: (row) => emit('option-change', row)
})
const {
  materialGroupOptions,
  sourceText,
  visibilityText,
  sourceGroupCode,
  changeSourceGroup,
  handleSourceTypeChange
} = useFormulaOptionSource(props, { t, selectedOption })
const {
  valueImportOpen,
  valueImportKeyword,
  importMaterialRows,
  importableSourceMaterials,
  valueMaterialOpen,
  valueMaterialCodes,
  selectedValueName,
  valueMaterialDialogTitle,
  openValueImportDialog,
  appendImportedMaterialValues,
  openValueMaterialDialog,
  saveValueMaterials
} = useFormulaOptionValueDialogs(props, {
  t,
  selectedOption,
  selectedValue,
  selectedValueCode,
  materialGroupOptions,
  sourceGroupCode,
  selectOptionValue,
  materialsForValue
})
const businessExceptionCount = computed(() => {
  const optionIssues = props.options.filter((row) => !row.optionCode || !row.optionNameCn || valuesForOption(row.optionCode).length === 0).length
  const valueIssues = props.allOptionValues.filter((row) => !row.valueCode || !row.valueNameCn).length
  const materialIssues = props.allOptionMaterials.filter((row) => !row.valueCode || !row.materialCode).length
  const restrictionIssues = props.restrictions.filter((row) => !row.targetOptionCode || !row.conditionType || !row.conditionOperator || !row.actionType).length
  const visibilityIssues = props.options.filter((row) => row.visibilityMode === 'CONDITIONAL' && (!row.visibleConditionOptionCode || !row.visibleConditionValueCode)).length
  return optionIssues + valueIssues + materialIssues + restrictionIssues + visibilityIssues
})
function setDefaultValue(row: ProductFormulaOptionValueVO) {
  props.allOptionValues
    .filter((item) => item.optionCode === row.optionCode)
    .forEach((item) => { item.defaultFlag = item === row })
  if (selectedOption.value) {
    selectedOption.value.defaultValueCode = row.valueCode
    selectedOption.value.defaultValueNameCn = row.valueNameCn
  }
}

function removeOptionValue(row: ProductFormulaOptionValueVO) {
  const index = props.optionValues.indexOf(row)
  if (index >= 0) emit('remove-option-value', index)
}

function addChildOptionFromValue() {
  if (!selectedOption.value || !selectedValue.value) return
  emit('add-option', {
    optionCode: selectedOption.value.optionCode,
    optionNameCn: selectedOption.value.optionNameCn,
    valueCode: selectedValue.value.valueCode,
    valueNameCn: selectedValue.value.valueNameCn
  })
}

function moveSelectedOption(direction: 'UP' | 'DOWN') {
  const option = selectedOption.value
  if (!option?.optionCode) return
  const siblings = siblingOptionsFor(option)
  const currentIndex = siblings.findIndex((row) => row.optionCode === option.optionCode)
  const targetIndex = direction === 'UP' ? currentIndex - 1 : currentIndex + 1
  if (currentIndex < 0 || targetIndex < 0 || targetIndex >= siblings.length) return
  const [row] = siblings.splice(currentIndex, 1)
  siblings.splice(targetIndex, 0, row)
  siblings.forEach((item, index) => { item.sortOrder = (index + 1) * 10 })
}

function removeSelectedOption() {
  const index = props.options.findIndex((row) => row.optionCode === props.selectedOptionCode)
  if (index >= 0) emit('remove-option', index)
}

</script>

<style scoped src="./FormulaBusinessOptionsTab.css"></style>
