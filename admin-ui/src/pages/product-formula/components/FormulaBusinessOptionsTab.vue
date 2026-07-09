<template>
  <FormulaOptionOverview
    :option-count="options.length"
    :option-value-count="allOptionValues.length"
    :linked-material-count="allOptionMaterials.length"
    :restriction-count="restrictions.length"
    :exception-count="businessExceptionCount"
  />

  <FormulaRestrictionBand
    :restrictions="restrictions"
    :options="conditionOptions"
    :all-option-values="allOptionValues"
    :option-materials="allOptionMaterials"
    :materials="materials"
    @add-restriction="$emit('add-restriction')"
    @remove-restriction="$emit('remove-restriction', $event)"
  />

  <section class="option-workbench">
    <FormulaOptionTree
      ref="optionTreeRef"
      :nodes="visibleOptionTreeViewNodes"
      :total-count="optionTreeNodes.length"
      :selected-node-id="selectedNodeId"
      :can-add-child-option="canAddChildOption"
      :can-remove-option="canRemoveSelectedOption"
      :can-move-up="canMoveSelectedOptionUp"
      :can-move-down="canMoveSelectedOptionDown"
      @add-root-option="addRootOption"
      @add-child-option="addChildOptionFromValue"
      @remove-selection="removeSelectedTreeNode"
      @move-selection="moveSelectedTreeNode"
      @select-node="selectTreeNodeById"
      @toggle-node="toggleTreeNodeById"
    />

    <main class="option-node-panel">
      <template v-if="selectedOption">
        <FormulaOptionNodeEditor
          ref="optionEditorRef"
          :selected-option="selectedOption"
          :material-group-options="materialGroupOptions"
          :t="t"
          :source-group-code="sourceGroupCode"
          @source-type-change="handleSourceTypeChange"
          @source-group-change="changeSourceGroup"
        />

        <div class="option-edit-grid">
          <FormulaOptionValueGrid
            ref="optionValueGridRef"
            :selected-option="selectedOption"
            :selected-option-code="selectedOptionCode"
            :selected-value-code="selectedValueCode"
            :option-values="optionValues"
            :option-materials="optionMaterials"
            @import-values="openValueImportDialog"
            @add-value="addOptionValue"
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
import { ElMessage } from 'element-plus'
import { computed, nextTick, ref } from 'vue'
import FormulaOptionNodeEditor from './FormulaOptionNodeEditor.vue'
import FormulaOptionOverview from './FormulaOptionOverview.vue'
import FormulaOptionTree from './FormulaOptionTree.vue'
import FormulaOptionValueDialogs from './FormulaOptionValueDialogs.vue'
import FormulaOptionValueGrid from './FormulaOptionValueGrid.vue'
import FormulaRestrictionBand from './FormulaRestrictionBand.vue'
import { useFormulaOptionSource } from './useFormulaOptionSource'
import { useFormulaOptionTree } from './useFormulaOptionTree'
import { useFormulaOptionValueDialogs } from './useFormulaOptionValueDialogs'
import { optionValueLiteral, optionVariableName } from './formulaExpressionDisplay'
import {
  optionClientKey,
  valueClientKey,
  valueOwnerClientKey,
  type DraftOption
} from '../utils/formulaOptionDraftIdentity'
import type {
  ProductFormulaMaterialVO,
  ProductFormulaOptionMaterialVO,
  ProductFormulaOptionVO,
  ProductFormulaOptionValueVO,
  ProductFormulaRestrictionVO,
  ProductFormulaUsageRuleVO,
  ProductFormulaVariableRuleVO
} from '@/api/product-capability/types'

type ChildOptionPayload = {
  optionClientKey?: string
  optionCode?: string
  optionNameCn?: string
  valueClientKey?: string
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
  usageRules: ProductFormulaUsageRuleVO[]
  variableRules: ProductFormulaVariableRuleVO[]
  selectedOptionCode: string
  materials: ProductFormulaMaterialVO[]
  materialLabel: (row: ProductFormulaMaterialVO) => string
}>()

const emit = defineEmits<{
  'add-option': [parent?: ChildOptionPayload]
  'option-change': [row?: ProductFormulaOptionVO]
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
const optionTreeRef = ref<{ scrollNodeIntoView: (nodeId: string) => Promise<void> }>()
const optionEditorRef = ref<{ focusOptionName: () => Promise<void> }>()
const optionValueGridRef = ref<{ focusValueName: (row?: ProductFormulaOptionValueVO) => Promise<void> }>()
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
  const optionIssues = props.options.filter((row) => !row.optionCode || !row.optionNameCn || valuesForOption(row).length === 0).length
  const valueIssues = props.allOptionValues.filter((row) => !row.valueCode || !row.valueNameCn).length
  const materialIssues = props.allOptionValues.filter((row) => !row.valueCode || (valueRequiresLinkedMaterial(row) && !valueHasChildOption(row) && materialsForValue(row).length === 0)).length
    + props.allOptionMaterials.filter((row) => !row.valueCode || !row.materialCode).length
  const restrictionIssues = props.restrictions.filter((row) => !row.conditionExpression || !row.actionType).length
  const visibilityIssues = props.options.filter((row) => row.visibilityMode === 'CONDITIONAL' && (!row.visibleConditionOptionCode || !row.visibleConditionValueCode)).length
  return optionIssues + valueIssues + materialIssues + restrictionIssues + visibilityIssues
})
const conditionOptions = computed(() => {
  const visibleOptionIds = new Set(visibleOptionTreeViewNodes.value.filter((node) => node.type === 'option').map((node) => node.id))
  return optionTreeNodes.value
    .filter((node) => node.type === 'option' && node.option && visibleOptionIds.has(node.id))
    .map((node) => node.option!)
})

function valueHasChildOption(row: ProductFormulaOptionValueVO) {
  return props.options.some((option) => (
    option.visibilityMode === 'CONDITIONAL'
    && ((option as DraftOption).visibleConditionOptionClientKey
      ? (option as DraftOption).visibleConditionOptionClientKey === valueOwnerClientKey(row)
      : option.visibleConditionOptionCode === row.optionCode)
    && ((option as DraftOption).visibleConditionValueClientKey
      ? (option as DraftOption).visibleConditionValueClientKey === valueClientKey(row)
      : option.visibleConditionValueCode === row.valueCode)
  ))
}

function valueRequiresLinkedMaterial(row: ProductFormulaOptionValueVO) {
  const ownerOption = props.options.find((option) => (
    valueOwnerClientKey(row) ? optionClientKey(option) === valueOwnerClientKey(row) : option.optionCode === row.optionCode
  ))
  return ownerOption?.sourceType === 'MATERIAL_POOL'
}

const canAddChildOption = computed(() => selectedNode.value?.type === 'value' && Boolean(selectedValue.value))
const canRemoveSelectedOption = computed(() => selectedNode.value?.type === 'option' || selectedNode.value?.type === 'value')
const canMoveSelectedOptionUp = computed(() => {
  if (selectedNode.value?.type === 'option') return currentOptionIndex.value > 0
  if (selectedNode.value?.type === 'value') return currentValueIndex.value > 0
  return false
})
const canMoveSelectedOptionDown = computed(() => (
  selectedNode.value?.type === 'option'
    ? currentOptionIndex.value >= 0 && currentOptionIndex.value < siblingOptions.value.length - 1
    : currentValueIndex.value >= 0 && currentValueIndex.value < siblingValues.value.length - 1
))
function setDefaultValue(row: ProductFormulaOptionValueVO) {
  props.allOptionValues
    .filter((item) => (valueOwnerClientKey(item) ? valueOwnerClientKey(item) === valueOwnerClientKey(row) : item.optionCode === row.optionCode))
    .forEach((item) => { item.defaultFlag = item === row })
  if (selectedOption.value) {
    selectedOption.value.defaultValueCode = row.valueCode
    selectedOption.value.defaultValueNameCn = row.valueNameCn
  }
}

function removeOptionValue(row: ProductFormulaOptionValueVO) {
  if (optionValueReferenced(row)) {
    ElMessage.warning(t('productCenter.formulaSetup.optionReferencedRemoveDenied'))
    return
  }
  const index = props.optionValues.indexOf(row)
  if (index >= 0) emit('remove-option-value', index)
}

function addRootOption() {
  emit('add-option')
  void focusSelectedOption()
}

function addChildOptionFromValue() {
  if (!selectedOption.value || !selectedValue.value) return
  emit('add-option', {
    optionClientKey: optionClientKey(selectedOption.value),
    optionCode: selectedOption.value.optionCode,
    optionNameCn: selectedOption.value.optionNameCn,
    valueClientKey: valueClientKey(selectedValue.value),
    valueCode: selectedValue.value.valueCode,
    valueNameCn: selectedValue.value.valueNameCn
  })
  void focusSelectedOption()
}

function addOptionValue() {
  const previousCount = props.optionValues.length
  emit('add-option-value')
  void focusAddedOptionValue(previousCount)
}

async function focusSelectedOption() {
  await nextTick()
  if (selectedNodeId.value) await optionTreeRef.value?.scrollNodeIntoView(selectedNodeId.value)
  await optionEditorRef.value?.focusOptionName()
}

async function focusAddedOptionValue(previousCount: number) {
  await nextTick()
  const row = props.optionValues[previousCount] || props.optionValues[props.optionValues.length - 1]
  if (!row) return
  selectOptionValue(row)
  await nextTick()
  selectTreeNodeById(selectedNodeId.value)
  await nextTick()
  if (selectedNodeId.value) await optionTreeRef.value?.scrollNodeIntoView(selectedNodeId.value)
  await optionValueGridRef.value?.focusValueName(row)
}

function optionHasChildOption(row?: ProductFormulaOptionVO) {
  if (!row?.optionCode && !optionClientKey(row)) return false
  const optionKey = optionClientKey(row)
  const optionCode = row?.optionCode
  return props.options.some((option) => (
    (option as DraftOption).visibleConditionOptionClientKey
      ? (option as DraftOption).visibleConditionOptionClientKey === optionKey
      : option.visibleConditionOptionCode === optionCode
  ))
}

function optionReferenced(row?: ProductFormulaOptionVO) {
  if (!row?.optionCode) return false
  const optionCode = row.optionCode
  return optionHasChildOption(row)
    || props.restrictions.some((restriction) => restriction.targetOptionCode === optionCode || expressionReferencesOption(restriction.conditionExpression || restriction.conditionText, row))
}

function optionValueReferenced(row?: ProductFormulaOptionValueVO) {
  if (!row?.optionCode || !row.valueCode) return false
  return valueHasChildOption(row)
    || props.options.some((option) => option.optionCode === row.optionCode && option.defaultValueCode === row.valueCode)
    || props.restrictions.some((restriction) => {
      const targetMatched = restriction.targetOptionCode === row.optionCode && restriction.targetValueCode === row.valueCode
      const expressionMatched = expressionReferencesOptionValue(restriction.conditionExpression || restriction.conditionText, row)
      return targetMatched || expressionMatched
    })
}

function expressionReferencesOption(expression: unknown, option: ProductFormulaOptionVO) {
  const text = String(expression || '')
  if (!text) return false
  return [
    optionVariableName(option),
    optionVariableName(option.optionCode),
    option.optionRefKey,
    option.optionCode,
    option.optionNameCn,
    option.optionNameEn
  ].some((value) => value && text.includes(value))
}

function expressionReferencesOptionValue(expression: unknown, row: ProductFormulaOptionValueVO) {
  const option = props.options.find((item) => optionClientKey(item) === valueOwnerClientKey(row) || item.optionCode === row.optionCode)
  if (!option || !expressionReferencesOption(expression, option)) return false
  const text = String(expression || '')
  return [
    optionValueLiteral(row),
    row.valueRefKey,
    row.valueCode,
    row.valueNameCn,
    row.valueNameEn
  ].some((value) => value && text.includes(value))
}

const siblingValues = computed(() => selectedOption.value ? valuesForOption(selectedOption.value) : [])
const currentValueIndex = computed(() => siblingValues.value.findIndex((row) => valueClientKey(row) === valueClientKey(selectedValue.value)))

function moveSelectedTreeNode(direction: 'UP' | 'DOWN') {
  if (selectedNode.value?.type === 'value') {
    moveSelectedValue(direction)
    return
  }
  moveSelectedOption(direction)
}

function moveSelectedOption(direction: 'UP' | 'DOWN') {
  const option = selectedOption.value
  if (selectedNode.value?.type !== 'option' || !option?.optionCode) return
  const siblings = siblingOptionsFor(option)
  const currentIndex = siblings.findIndex((row) => optionClientKey(row) === optionClientKey(option))
  const targetIndex = direction === 'UP' ? currentIndex - 1 : currentIndex + 1
  if (currentIndex < 0 || targetIndex < 0 || targetIndex >= siblings.length) return
  const [row] = siblings.splice(currentIndex, 1)
  siblings.splice(targetIndex, 0, row)
  siblings.forEach((item, index) => { item.sortOrder = (index + 1) * 10 })
}

function moveSelectedValue(direction: 'UP' | 'DOWN') {
  const value = selectedValue.value
  if (!value?.optionCode || !value.valueCode) return
  const siblings = valuesForOption(valueOwnerClientKey(value) || value.optionCode)
  const currentIndex = siblings.findIndex((row) => valueClientKey(row) === valueClientKey(value))
  const targetIndex = direction === 'UP' ? currentIndex - 1 : currentIndex + 1
  if (currentIndex < 0 || targetIndex < 0 || targetIndex >= siblings.length) return
  const [row] = siblings.splice(currentIndex, 1)
  siblings.splice(targetIndex, 0, row)
  siblings.forEach((item, index) => { item.sortOrder = (index + 1) * 10 })
}

function removeSelectedTreeNode() {
  if (selectedNode.value?.type === 'value' && selectedValue.value) {
    removeOptionValue(selectedValue.value)
    return
  }
  if (selectedNode.value?.type === 'option') {
    if (optionReferenced(selectedOption.value)) {
      ElMessage.warning(t('productCenter.formulaSetup.optionReferencedRemoveDenied'))
      return
    }
    const index = props.options.findIndex((row) => optionClientKey(row) === props.selectedOptionCode || row.optionCode === props.selectedOptionCode)
    if (index >= 0) emit('remove-option', index)
  }
}

</script>

<style scoped src="./FormulaBusinessOptionsTab.css"></style>
