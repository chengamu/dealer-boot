<template>
  <div class="option-overview">
    <div class="option-overview__card option-overview__card--option">
      <span class="option-overview__icon"><Operation /></span>
      <span class="option-overview__label">{{ t('productCenter.formulaSetup.optionCount') }}</span>
      <strong>{{ options.length }}</strong>
    </div>
    <div class="option-overview__card option-overview__card--value">
      <span class="option-overview__icon"><List /></span>
      <span class="option-overview__label">{{ t('productCenter.formulaSetup.optionValueCount') }}</span>
      <strong>{{ allOptionValues.length }}</strong>
    </div>
    <div class="option-overview__card option-overview__card--material">
      <span class="option-overview__icon"><Connection /></span>
      <span class="option-overview__label">{{ t('productCenter.formulaSetup.linkedMaterial') }}</span>
      <strong>{{ allOptionMaterials.length }}</strong>
    </div>
    <div class="option-overview__card option-overview__card--restriction">
      <span class="option-overview__icon"><Lock /></span>
      <span class="option-overview__label">{{ t('productCenter.formulaSetup.restrictionCount') }}</span>
      <strong>{{ restrictions.length }}</strong>
    </div>
    <div class="option-overview__card option-overview__card--exception">
      <span class="option-overview__icon"><WarningFilled /></span>
      <span class="option-overview__label">{{ t('productCenter.formulaSetup.businessExceptionCount') }}</span>
      <strong>{{ businessExceptionCount }}</strong>
    </div>
  </div>

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
        <div class="node-editor">
          <div class="node-editor__header">
            <div>
              <h3>{{ selectedNode?.type === 'value' ? t('productCenter.formulaSetup.valueSetting') : t('productCenter.formulaSetup.optionSetting') }}</h3>
              <p>{{ visibilityText(selectedOption) }}</p>
            </div>
            <div class="node-editor__actions">
              <el-button-group v-if="selectedNode?.type !== 'value'">
                <el-button :icon="ArrowUp" :disabled="currentOptionIndex <= 0" @click="moveSelectedOption('UP')">{{ t('productCenter.formulaSetup.moveUp') }}</el-button>
                <el-button :icon="ArrowDown" :disabled="currentOptionIndex < 0 || currentOptionIndex >= siblingOptions.length - 1" @click="moveSelectedOption('DOWN')">{{ t('productCenter.formulaSetup.moveDown') }}</el-button>
              </el-button-group>
              <el-button
                v-if="selectedNode?.type === 'value' && selectedValue"
                type="primary"
                plain
                :icon="Plus"
                @click="addChildOptionFromValue"
              >
                {{ t('productCenter.formulaSetup.addChildOption') }}
              </el-button>
              <el-button v-if="selectedNode?.type !== 'value'" link type="danger" @click="removeSelectedOption">{{ t('common.delete') }}</el-button>
            </div>
          </div>

          <div class="option-form">
            <label>
              <span>{{ t('productCenter.formulaSetup.optionName') }}</span>
              <el-input v-model="selectedOption.optionNameCn" :placeholder="t('productCenter.formulaSetup.optionNamePlaceholder')" />
            </label>
            <label>
              <span>{{ t('productCenter.formulaSetup.sourceType') }}</span>
              <el-select v-model="selectedOption.sourceType" @change="handleSourceTypeChange">
                <el-option value="MATERIAL_POOL" :label="t('productCenter.formulaSetup.sourceMaterialPool')" />
                <el-option value="PRODUCT_DICT" :label="t('productCenter.formulaSetup.sourceProductDict')" />
                <el-option value="BOOLEAN" :label="t('productCenter.formulaSetup.sourceBoolean')" />
                <el-option value="MANUAL" :label="t('productCenter.formulaSetup.sourceManual')" />
              </el-select>
            </label>
            <label v-if="selectedOption.sourceType === 'MATERIAL_POOL'">
              <span>{{ t('productCenter.formulaSetup.sourceMaterialGroup') }}</span>
              <el-select :model-value="sourceGroupCode(selectedOption)" filterable @change="changeSourceGroup">
                <el-option v-for="group in materialGroupOptions" :key="group.value" :label="group.label" :value="group.value" />
              </el-select>
            </label>
            <label>
              <span>{{ t('productCenter.formulaSetup.selectionMode') }}</span>
              <el-select v-model="selectedOption.selectionMode">
                <el-option value="SINGLE" :label="t('productCenter.formulaSetup.single')" />
                <el-option value="MULTIPLE" :label="t('productCenter.formulaSetup.multiple')" />
                <el-option value="SWITCH" :label="t('productCenter.formulaSetup.switch')" />
              </el-select>
            </label>
            <label>
              <span>{{ t('productCenter.formulaSetup.orderVisibility') }}</span>
              <div class="visibility-summary">
                {{ visibilityText(selectedOption) }}
              </div>
            </label>
            <label class="option-form__switch">
              <span>{{ t('productCenter.formulaSetup.requiredFlag') }}</span>
              <el-switch v-model="selectedOption.requiredFlag" />
            </label>
            <label class="option-form__switch">
              <span>{{ t('productCenter.formulaSetup.businessVisible') }}</span>
              <el-switch v-model="selectedOption.businessVisibleFlag" />
            </label>
          </div>
        </div>

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

  <el-dialog v-model="valueImportOpen" :title="t('productCenter.formulaSetup.importOptionValues')" width="980px" class="formula-option-dialog">
    <div class="formula-option-dialog__toolbar">
      <div>
        <strong>{{ selectedOption?.optionNameCn || selectedOption?.optionCode || '-' }}</strong>
        <span>{{ sourceText(selectedOption || {}) }}</span>
      </div>
      <el-input v-model="valueImportKeyword" :placeholder="t('productCenter.formulaSetup.materialSearchPlaceholder')" clearable />
    </div>
    <el-table :data="importableSourceMaterials" border height="430" @selection-change="importMaterialRows = $event">
      <el-table-column type="selection" width="46" />
      <el-table-column prop="attributeGroupNameCn" :label="t('productCenter.formulaSetup.attributeGroup')" width="120" show-overflow-tooltip />
      <el-table-column prop="materialTypeNameCn" :label="t('productCenter.formulaSetup.materialType')" width="140" show-overflow-tooltip />
      <el-table-column prop="materialCode" :label="t('productCenter.formulaSetup.materialCode')" width="140" show-overflow-tooltip />
      <el-table-column prop="materialNameCn" :label="t('productCenter.formulaSetup.materialName')" min-width="240" show-overflow-tooltip />
      <el-table-column prop="specModelText" :label="t('productCenter.formulaSetup.specModel')" min-width="200" show-overflow-tooltip />
      <el-table-column prop="unitCode" :label="t('productCenter.formulaSetup.unit')" width="90" />
    </el-table>
    <template #footer>
      <span class="formula-option-dialog__count">{{ t('productCenter.formulaSetup.selectedMaterialCount') }}：{{ importMaterialRows.length }}</span>
      <el-button @click="valueImportOpen = false">{{ t('common.cancel') }}</el-button>
      <el-button type="primary" :disabled="!importMaterialRows.length" @click="appendImportedMaterialValues">{{ t('common.confirm') }}</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="valueMaterialOpen" :title="valueMaterialDialogTitle" width="760px" class="formula-option-dialog">
    <div class="formula-option-dialog__toolbar">
      <div>
        <strong>{{ selectedValueName }}</strong>
        <span>{{ t('productCenter.formulaSetup.valueMaterialHint') }}</span>
      </div>
    </div>
    <el-select
      v-model="valueMaterialCodes"
      multiple
      filterable
      clearable
      collapse-tags
      collapse-tags-tooltip
      class="value-material-select"
      :placeholder="t('productCenter.formulaSetup.pickFromMaterialPool')"
    >
      <el-option v-for="material in materials" :key="String(material.materialCode)" :label="materialLabel(material)" :value="material.materialCode" />
    </el-select>
    <template #footer>
      <el-button @click="valueMaterialOpen = false">{{ t('common.cancel') }}</el-button>
      <el-button type="primary" @click="saveValueMaterials">{{ t('common.confirm') }}</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { ElMessage } from 'element-plus'
import { computed, ref, watch } from 'vue'
import { ArrowDown, ArrowUp, Connection, List, Lock, Operation, Plus, WarningFilled } from '@element-plus/icons-vue'
import FormulaOptionTree, { type FormulaOptionTreeViewNode } from './FormulaOptionTree.vue'
import FormulaOptionValueGrid from './FormulaOptionValueGrid.vue'
import FormulaRestrictionBand from './FormulaRestrictionBand.vue'
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

type OptionTreeNode = {
  id: string
  type: 'group' | 'option' | 'value'
  level: number
  parentId?: string
  label?: string
  option?: ProductFormulaOptionVO
  value?: ProductFormulaOptionValueVO
  issue?: boolean
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
const t = (key: string) => getMessage(key, localeStore.language)
const ALL_VALUE_FILTER = '__ALL__'
const selectedValueCode = ref(ALL_VALUE_FILTER)
const selectedNodeId = ref('')
const expandedNodeIds = ref<Set<string>>(new Set())
const valueImportOpen = ref(false)
const valueImportKeyword = ref('')
const importMaterialRows = ref<ProductFormulaMaterialVO[]>([])
const valueMaterialOpen = ref(false)
const valueMaterialCodes = ref<string[]>([])

const sortedOptions = computed(() => [...props.options].sort(sortByOrder))
const selectedOption = computed(() => props.options.find((row) => row.optionCode === props.selectedOptionCode))
const selectedValue = computed(() => props.optionValues.find((row) => row.valueCode === selectedValueCode.value))
const siblingOptions = computed(() => selectedOption.value ? siblingOptionsFor(selectedOption.value) : [])
const currentOptionIndex = computed(() => siblingOptions.value.findIndex((row) => row.optionCode === selectedOption.value?.optionCode))
const selectedNode = computed(() => optionTreeNodes.value.find((node) => node.id === selectedNodeId.value))
const treeNodeMap = computed(() => new Map(optionTreeNodes.value.map((node) => [node.id, node])))
const visibleOptionTreeNodes = computed(() => optionTreeNodes.value.filter((node) => isNodeVisible(node)))
const visibleOptionTreeViewNodes = computed<FormulaOptionTreeViewNode[]>(() => visibleOptionTreeNodes.value.map((node) => ({
  id: node.id,
  type: node.type,
  level: node.level,
  issue: node.issue,
  title: nodeTitle(node),
  meta: nodeMeta(node),
  kindText: nodeKindText(node),
  hasChildren: hasTreeChildren(node),
  expanded: isTreeExpanded(node)
})))
const selectedValueCodes = computed(() => new Set(props.optionValues
  .filter((row) => row.optionCode === props.selectedOptionCode)
  .map((row) => row.valueCode || '')
  .filter(Boolean)))
const materialGroupOptions = computed(() => {
  const groups = new Map<string, string>()
  props.materials.forEach((row) => {
    if (!row.attributeGroupCode) return
    groups.set(row.attributeGroupCode, row.attributeGroupNameCn || row.attributeGroupCode)
  })
  return Array.from(groups.entries()).map(([value, label]) => ({ value, label }))
})
const sourceMaterials = computed(() => {
  const option = selectedOption.value
  if (!option) return []
  const groupCode = sourceGroupCode(option)
  return props.materials.filter((row) => !groupCode || row.attributeGroupCode === groupCode)
})
const importableSourceMaterials = computed(() => sourceMaterials.value.filter((row) => {
  if (selectedValueCodes.value.has(row.materialCode || '')) return false
  const keyword = valueImportKeyword.value.trim().toLowerCase()
  if (!keyword) return true
  return containsText(row.materialCode, keyword)
    || containsText(row.materialNameCn, keyword)
    || containsText(row.specModelText, keyword)
    || containsText(row.materialTypeNameCn, keyword)
}))
const optionTreeNodes = computed<OptionTreeNode[]>(() => {
  const nodes: OptionTreeNode[] = []
  const visited = new Set<string>()
  const roots = sortedOptions.value.filter((option) => option.visibilityMode !== 'CONDITIONAL')

  roots.forEach((option) => appendOptionNode(nodes, visited, option, 0))

  const unclassified = sortedOptions.value.filter((option) => option.visibilityMode === 'CONDITIONAL' && option.optionCode && !visited.has(option.optionCode))
  if (unclassified.length) {
    nodes.push({
      id: 'group:unclassified',
      type: 'group',
      level: 0,
      label: t('productCenter.formulaSetup.unclassifiedOptions'),
      issue: true
    })
    unclassified.forEach((option) => appendOptionNode(nodes, visited, option, 1, true, 'group:unclassified'))
  }

  return nodes
})
const businessExceptionCount = computed(() => {
  const optionIssues = props.options.filter((row) => !row.optionCode || !row.optionNameCn || valuesForOption(row.optionCode).length === 0).length
  const valueIssues = props.allOptionValues.filter((row) => !row.valueCode || !row.valueNameCn).length
  const materialIssues = props.allOptionMaterials.filter((row) => !row.valueCode || !row.materialCode).length
  const restrictionIssues = props.restrictions.filter((row) => !row.targetOptionCode || !row.conditionType || !row.conditionOperator || !row.actionType).length
  const visibilityIssues = props.options.filter((row) => row.visibilityMode === 'CONDITIONAL' && (!row.visibleConditionOptionCode || !row.visibleConditionValueCode)).length
  return optionIssues + valueIssues + materialIssues + restrictionIssues + visibilityIssues
})
const selectedValueName = computed(() => selectedValue.value?.valueNameCn || selectedValue.value?.valueCode || t('productCenter.formulaSetup.selectOptionHint'))
const valueMaterialDialogTitle = computed(() => `${t('productCenter.formulaSetup.valueMaterial')}：${selectedValueName.value}`)

watch(() => props.selectedOptionCode, () => {
  ensureSelectedValue()
  ensureSelectedNode()
  if (selectedNodeId.value) expandNodePath(selectedNodeId.value, true)
})

watch(() => optionTreeNodes.value.map((node) => node.id).join('|'), () => {
  ensureSelectedNode()
}, { immediate: true })

watch(() => props.optionValues.map((row) => String(row.valueCode || '')).join('|'), () => {
  ensureSelectedValue()
}, { immediate: true })

function appendOptionNode(nodes: OptionTreeNode[], visited: Set<string>, option: ProductFormulaOptionVO, level: number, issue = false, parentId?: string) {
  if (!option.optionCode || visited.has(option.optionCode)) return
  visited.add(option.optionCode)
  const nodeId = optionNodeId(option)
  nodes.push({
    id: nodeId,
    type: 'option',
    level,
    parentId,
    option,
    issue
  })

  valuesForOption(option.optionCode).forEach((value) => {
    const valueId = valueNodeId(option, value)
    nodes.push({
      id: valueId,
      type: 'value',
      level: level + 1,
      parentId: nodeId,
      option,
      value
    })
    childOptionsForValue(option.optionCode, value.valueCode).forEach((child) => {
      appendOptionNode(nodes, visited, child, level + 2, false, valueId)
    })
  })
}

function optionNodeId(option: ProductFormulaOptionVO) {
  return `option:${option.optionCode || ''}`
}

function valueNodeId(option: ProductFormulaOptionVO, value: ProductFormulaOptionValueVO) {
  return `value:${option.optionCode || ''}:${value.valueCode || ''}`
}

function sortByOrder<T extends { sortOrder?: number }>(a: T, b: T) {
  return (a.sortOrder || 0) - (b.sortOrder || 0)
}

function childOptionsForValue(optionCode?: string, valueCode?: string) {
  return sortedOptions.value.filter((option) => (
    option.visibilityMode === 'CONDITIONAL'
    && option.visibleConditionOptionCode === optionCode
    && option.visibleConditionValueCode === valueCode
  ))
}

function siblingOptionsFor(option: ProductFormulaOptionVO) {
  if (option.visibilityMode === 'CONDITIONAL') {
    return childOptionsForValue(option.visibleConditionOptionCode, option.visibleConditionValueCode)
  }
  return sortedOptions.value.filter((row) => row.visibilityMode !== 'CONDITIONAL')
}

function nodeKindText(node: OptionTreeNode) {
  if (node.type === 'group') return '!'
  return node.type === 'option' ? t('productCenter.formulaSetup.optionNode') : t('productCenter.formulaSetup.valueNode')
}

function nodeTitle(node: OptionTreeNode) {
  if (node.type === 'group') return node.label || ''
  if (node.type === 'value') return node.value?.valueNameCn || node.value?.valueCode || t('productCenter.formulaSetup.unnamedValue')
  return node.option?.optionNameCn || node.option?.optionCode || t('productCenter.formulaSetup.unnamedOption')
}

function nodeMeta(node: OptionTreeNode) {
  if (node.type === 'group') return t('productCenter.formulaSetup.unclassifiedHint')
  if (node.type === 'value') {
    const count = materialsForValue(node.value).length
    return `${count} ${t('productCenter.formulaSetup.linkedMaterial')}`
  }
  const option = node.option
  if (!option) return ''
  return `${sourceText(option)} · ${valuesForOption(option.optionCode).length} ${t('productCenter.formulaSetup.itemUnit')}`
}

function selectTreeNode(node: OptionTreeNode) {
  if (node.type === 'group') {
    toggleTreeNode(node)
    return
  }
  selectedNodeId.value = node.id
  expandNodePath(node.id, node.type === 'option')
  if (node.option?.optionCode) emit('option-change', node.option)
  if (node.type === 'value') {
    selectedValueCode.value = node.value?.valueCode || ALL_VALUE_FILTER
  } else {
    selectedValueCode.value = valuesForOption(node.option?.optionCode)[0]?.valueCode || ALL_VALUE_FILTER
  }
}

function selectTreeNodeById(nodeId: string) {
  const node = optionTreeNodes.value.find((item) => item.id === nodeId)
  if (node) selectTreeNode(node)
}

function ensureSelectedNode() {
  if (optionTreeNodes.value.some((node) => node.id === selectedNodeId.value)) {
    expandNodePath(selectedNodeId.value, selectedNode.value?.type === 'option')
    return
  }
  const option = selectedOption.value || props.options[0]
  selectedNodeId.value = option ? optionNodeId(option) : ''
  if (selectedNodeId.value) expandNodePath(selectedNodeId.value, true)
}

function ensureSelectedValue() {
  if (props.optionValues.some((row) => row.optionCode === props.selectedOptionCode && row.valueCode === selectedValueCode.value)) return
  selectedValueCode.value = props.optionValues[0]?.valueCode || ALL_VALUE_FILTER
}

function sourceText(option: ProductFormulaOptionVO) {
  if (option.sourceType === 'MATERIAL_POOL') {
    const group = materialGroupOptions.value.find((item) => item.value === sourceGroupCode(option))
    return `${t('productCenter.formulaSetup.sourceMaterialPool')} ${group?.label || ''}`.trim()
  }
  const map: Record<string, string> = {
    PRODUCT_DICT: t('productCenter.formulaSetup.sourceProductDict'),
    BOOLEAN: t('productCenter.formulaSetup.sourceBoolean'),
    MANUAL: t('productCenter.formulaSetup.sourceManual')
  }
  return map[option.sourceType || ''] || t('productCenter.formulaSetup.sourceManual')
}

function visibilityText(option: ProductFormulaOptionVO) {
  if (option.visibilityMode !== 'CONDITIONAL') {
    return t('productCenter.formulaSetup.visibilityAlways')
  }
  const conditionOption = props.options.find((row) => row.optionCode === option.visibleConditionOptionCode)
  const conditionValue = props.allOptionValues.find((row) => row.optionCode === option.visibleConditionOptionCode && row.valueCode === option.visibleConditionValueCode)
  const optionName = option.visibleConditionOptionNameCn || conditionOption?.optionNameCn || option.visibleConditionOptionCode || '-'
  const valueName = option.visibleConditionValueNameCn || conditionValue?.valueNameCn || option.visibleConditionValueCode || '-'
  return t('productCenter.formulaSetup.visibleWhenSummary')
    .replace('{option}', optionName)
    .replace('{value}', valueName)
}

function sourceGroupCode(option?: ProductFormulaOptionVO) {
  return String(option?.sourceScope || '').replace('attributeGroupCode=', '')
}

function changeSourceGroup(value: string) {
  if (!selectedOption.value) return
  selectedOption.value.sourceScope = value ? `attributeGroupCode=${value}` : ''
}

function handleSourceTypeChange() {
  if (!selectedOption.value) return
  if (selectedOption.value.sourceType === 'MATERIAL_POOL' && !selectedOption.value.sourceScope) {
    const firstGroup = materialGroupOptions.value[0]?.value || ''
    selectedOption.value.sourceScope = firstGroup ? `attributeGroupCode=${firstGroup}` : ''
  }
}

function openValueImportDialog() {
  if (!selectedOption.value || selectedOption.value.sourceType !== 'MATERIAL_POOL') return
  valueImportKeyword.value = ''
  importMaterialRows.value = []
  valueImportOpen.value = true
}

function appendImportedMaterialValues() {
  const option = selectedOption.value
  if (!option) return
  const existingValues = new Set(props.allOptionValues.filter((row) => row.optionCode === option.optionCode).map((row) => row.valueCode))
  importMaterialRows.value.forEach((material) => {
    if (!material?.materialCode || existingValues.has(material.materialCode)) return
    props.allOptionValues.push({
      optionCode: option.optionCode,
      valueCode: material.materialCode,
      valueNameCn: material.materialNameCn,
      defaultFlag: false,
      status: 'ENABLED',
      sortOrder: props.allOptionValues.length * 10 + 10
    })
    props.allOptionMaterials.push({
      optionCode: option.optionCode,
      valueCode: material.materialCode,
      formulaMaterialId: material.formulaMaterialId,
      materialId: material.materialId,
      materialCode: material.materialCode,
      materialNameCn: material.materialNameCn,
      requiredFlag: true,
      defaultFlag: true,
      status: 'ENABLED',
      sortOrder: props.allOptionMaterials.length * 10 + 10
    })
    selectedValueCode.value = material.materialCode
  })
  importMaterialRows.value = []
  valueImportOpen.value = false
}

function setDefaultValue(row: ProductFormulaOptionValueVO) {
  props.allOptionValues
    .filter((item) => item.optionCode === row.optionCode)
    .forEach((item) => { item.defaultFlag = item === row })
  if (selectedOption.value) {
    selectedOption.value.defaultValueCode = row.valueCode
    selectedOption.value.defaultValueNameCn = row.valueNameCn
  }
}

function materialsForValue(row?: ProductFormulaOptionValueVO) {
  return props.optionMaterials.filter((item) => item.optionCode === row?.optionCode && item.valueCode === row?.valueCode)
}

function valuesForOption(optionCode?: string) {
  return props.allOptionValues.filter((row) => row.optionCode === optionCode).sort(sortByOrder)
}

function containsText(value: unknown, keyword: string) {
  if (!keyword) return true
  return String(value || '').toLowerCase().includes(keyword)
}

function selectOptionValue(row: ProductFormulaOptionValueVO) {
  selectedValueCode.value = row.valueCode || ALL_VALUE_FILTER
  selectedNodeId.value = valueNodeId(selectedOption.value || {}, row)
}

function removeOptionValue(row: ProductFormulaOptionValueVO) {
  const index = props.optionValues.indexOf(row)
  if (index >= 0) emit('remove-option-value', index)
}

function openValueMaterialDialog(row: ProductFormulaOptionValueVO) {
  if (!row.valueCode) {
    ElMessage.warning(t('productCenter.formulaSetup.selectValueBeforeAddMaterial'))
    return
  }
  selectOptionValue(row)
  valueMaterialCodes.value = materialsForValue(row)
    .map((item) => item.materialCode || '')
    .filter(Boolean)
  valueMaterialOpen.value = true
}

function saveValueMaterials() {
  const option = selectedOption.value
  const value = selectedValue.value
  if (!option?.optionCode || !value?.valueCode) return
  for (let index = props.allOptionMaterials.length - 1; index >= 0; index -= 1) {
    const row = props.allOptionMaterials[index]
    if (row.optionCode === option.optionCode && row.valueCode === value.valueCode) {
      props.allOptionMaterials.splice(index, 1)
    }
  }
  valueMaterialCodes.value.forEach((code) => {
    const material = props.materials.find((row) => row.materialCode === code)
    if (!material?.materialCode) return
    props.allOptionMaterials.push({
      optionCode: option.optionCode,
      valueCode: value.valueCode,
      formulaMaterialId: material.formulaMaterialId,
      materialId: material.materialId,
      materialCode: material.materialCode,
      materialNameCn: material.materialNameCn,
      requiredFlag: true,
      defaultFlag: props.allOptionMaterials.filter((row) => row.optionCode === option.optionCode && row.valueCode === value.valueCode).length === 0,
      status: 'ENABLED',
      sortOrder: props.allOptionMaterials.length * 10 + 10
    })
  })
  valueMaterialOpen.value = false
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

function hasTreeChildren(node: OptionTreeNode) {
  return optionTreeNodes.value.some((item) => item.parentId === node.id)
}

function isTreeExpanded(node: OptionTreeNode) {
  return expandedNodeIds.value.has(node.id)
}

function toggleTreeNode(node: OptionTreeNode) {
  if (!hasTreeChildren(node)) return
  const next = new Set(expandedNodeIds.value)
  if (next.has(node.id)) {
    next.delete(node.id)
  } else {
    next.add(node.id)
  }
  expandedNodeIds.value = next
}

function toggleTreeNodeById(nodeId: string) {
  const node = optionTreeNodes.value.find((item) => item.id === nodeId)
  if (node) toggleTreeNode(node)
}

function isNodeVisible(node: OptionTreeNode) {
  let parentId = node.parentId
  while (parentId) {
    if (!expandedNodeIds.value.has(parentId)) return false
    parentId = treeNodeMap.value.get(parentId)?.parentId
  }
  return true
}

function expandNodePath(nodeId: string, includeNode = false) {
  const next = new Set(expandedNodeIds.value)
  let currentId = includeNode ? nodeId : treeNodeMap.value.get(nodeId)?.parentId
  while (currentId) {
    next.add(currentId)
    currentId = treeNodeMap.value.get(currentId)?.parentId
  }
  expandedNodeIds.value = next
}
</script>

<style scoped>
.option-overview {
  display: grid;
  grid-template-columns: repeat(5, minmax(150px, 1fr));
  gap: 14px;
  margin-bottom: 12px;
  padding: 16px;
  background: #fff;
  border: 1px solid #e6ebf2;
  border-radius: 8px;
}

.option-overview__card {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 72px;
  padding: 12px 16px;
  background: linear-gradient(180deg, #fff, #f8fbff);
  border: 1px solid #e6ebf2;
  border-radius: 8px;
  box-shadow: 0 4px 14px rgb(15 23 42 / 3%);
}

.option-overview__icon {
  display: inline-flex;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  color: #1677ff;
  font-size: 24px;
  background: #f5f9ff;
  border-radius: 10px;
}

.option-overview__label {
  color: #1f2937;
  font-weight: 600;
}

.option-overview__card strong {
  margin-left: auto;
  color: #111827;
  font-size: 24px;
  line-height: 1;
}

.option-overview__card--value .option-overview__icon {
  color: #22c55e;
}

.option-overview__card--material .option-overview__icon {
  color: #8b5cf6;
}

.option-overview__card--restriction .option-overview__icon {
  color: #f59e0b;
}

.option-overview__card--exception .option-overview__icon,
.option-overview__card--exception strong {
  color: #dc2626;
}

.option-workbench {
  display: grid;
  grid-template-columns: minmax(320px, 380px) minmax(0, 1fr);
  align-items: start;
  gap: 12px;
  margin-bottom: 12px;
}

.option-tree-panel,
.option-node-panel,
.setup-section {
  background: #fff;
  border: 1px solid #e6ebf2;
  border-radius: 8px;
}

.option-tree-panel__header,
.node-editor__header,
.node-card__header,
.setup-section__toolbar {
  display: flex;
  justify-content: space-between;
  gap: 14px;
}

.option-tree-panel__header,
.node-editor,
.node-card,
.setup-section {
  padding: 16px;
}

.option-tree-panel__header {
  align-items: flex-start;
  border-bottom: 1px solid #edf1f7;
}

.option-tree-panel__header h3,
.node-editor__header h3,
.node-card__header h4,
.setup-section__toolbar h3 {
  margin: 0 0 4px;
  color: #111827;
  font-size: 16px;
}

.option-tree-panel__header p,
.node-editor__header p,
.node-card__header p,
.setup-section__toolbar p {
  margin: 0;
  color: #6b7280;
  font-size: 13px;
}

.option-tree-panel__actions,
.node-editor__actions,
.node-card__actions,
.setup-section__actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.option-tree-panel__actions {
  flex-wrap: wrap;
  justify-content: flex-end;
}

.option-tree-panel__actions :deep(.el-button) {
  height: 32px;
  padding: 0 12px;
  border-radius: 6px;
}

.option-tree {
  padding: 8px 10px 10px;
}

.option-tree__node {
  position: relative;
  display: flex;
  width: 100%;
  align-items: center;
  gap: 6px;
  min-height: 46px;
  margin-bottom: 2px;
  padding: 5px 8px 5px calc(8px + var(--tree-indent));
  text-align: left;
  background: transparent;
  border: 1px solid transparent;
  border-radius: 6px;
  cursor: pointer;
}

.option-tree__node--value {
  background: transparent;
}

.option-tree__node--active {
  background: #eef6ff;
  border-color: #bfdbfe;
  box-shadow: inset 2px 0 0 #1677ff;
}

.option-tree__node--group {
  color: #b45309;
  background: #fff7ed;
  cursor: default;
}

.option-tree__node--issue {
  border-color: #fed7aa;
}

.option-tree__branch {
  position: absolute;
  top: -3px;
  bottom: -3px;
  left: calc(18px + var(--tree-indent));
  width: 1px;
  background: #dbe5f2;
}

.option-tree__branch::after {
  position: absolute;
  top: 23px;
  left: 0;
  width: 12px;
  height: 1px;
  background: #dbe5f2;
  content: "";
}

.option-tree__node--root > .option-tree__branch {
  display: none;
}

.option-tree__toggle {
  position: relative;
  z-index: 1;
  display: inline-flex;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  color: #64748b;
  background: #fff;
  border: 1px solid #dbe5f2;
  border-radius: 5px;
}

.option-tree__toggle--empty {
  background: transparent;
  border-color: transparent;
}

.option-tree__kind {
  display: inline-flex;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;
  min-width: 32px;
  height: 22px;
  padding: 0 7px;
  color: #1677ff;
  font-size: 12px;
  font-weight: 700;
  background: #eaf3ff;
  border: 1px solid #bfdbfe;
  border-radius: 999px;
}

.option-tree__node--value .option-tree__kind {
  color: #16a34a;
  background: #dcfce7;
  border-color: #bbf7d0;
}

.option-tree__node--group .option-tree__kind {
  color: #ea580c;
  background: #ffedd5;
  border-color: #fed7aa;
}

.option-tree__body {
  display: grid;
  min-width: 0;
  grid-template-columns: minmax(0, 1fr);
  gap: 2px;
}

.option-tree__title {
  overflow: hidden;
  color: #111827;
  font-size: 14px;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.option-tree__meta {
  overflow: hidden;
  color: #6b7280;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.option-node-panel {
  min-width: 0;
  overflow: hidden;
}

.node-editor {
  border-bottom: 1px solid #edf1f7;
}

.node-editor__actions {
  flex-wrap: wrap;
}

.option-form {
  display: grid;
  grid-template-columns: repeat(4, minmax(160px, 1fr));
  gap: 12px;
  margin-top: 14px;
}

.option-form label {
  display: grid;
  gap: 6px;
  color: #4b5563;
  font-size: 12px;
  font-weight: 600;
}

.option-form__switch {
  align-content: end;
  grid-template-columns: auto auto;
  align-items: center;
  justify-content: start;
}

.visibility-summary {
  display: flex;
  align-items: center;
  min-height: 34px;
  padding: 0 10px;
  color: #1f2937;
  background: #f8fbff;
  border: 1px solid #dce6f2;
  border-radius: 6px;
}

.option-edit-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  gap: 12px;
  padding: 12px;
}

.node-card {
  min-width: 0;
  background: #fbfdff;
  border: 1px solid #e6ebf2;
  border-radius: 8px;
}

.node-card__header {
  align-items: flex-start;
  margin-bottom: 12px;
}

.setup-table {
  border-radius: 6px;
  overflow: hidden;
}

.setup-table :deep(.el-table__header th) {
  height: 42px;
  background: #f7faff !important;
  color: #1f2937;
  font-weight: 700;
}

.setup-table :deep(.el-table__row td) {
  height: 50px;
}

.setup-table :deep(.el-input .el-input__wrapper),
.setup-table :deep(.el-select .el-select__wrapper) {
  min-height: 32px;
  border-radius: 6px;
}

.option-value-table :deep(.el-table__row) {
  cursor: pointer;
}

.option-value-table :deep(.option-value-row--active td) {
  background: #eef6ff !important;
}

.linked-materials {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.muted {
  color: #9ca3af;
}

.restriction-section {
  padding: 16px;
}

.formula-option-dialog :deep(.el-dialog) {
  border-radius: 10px;
}

.formula-option-dialog :deep(.el-dialog__header) {
  padding: 18px 22px 12px;
  border-bottom: 1px solid #edf1f7;
}

.formula-option-dialog :deep(.el-dialog__body) {
  padding: 14px 20px;
}

.formula-option-dialog :deep(.el-dialog__footer) {
  padding: 12px 20px 16px;
  border-top: 1px solid #edf1f7;
}

.formula-option-dialog__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
  padding: 10px 12px;
  color: #6b7280;
  background: #f8fbff;
  border: 1px solid #e6ebf2;
  border-radius: 8px;
}

.formula-option-dialog__toolbar > div {
  display: grid;
  gap: 2px;
}

.formula-option-dialog__toolbar strong {
  color: #111827;
}

.formula-option-dialog__toolbar .el-input {
  width: 320px;
}

.formula-option-dialog__count {
  float: left;
  color: #6b7280;
  line-height: 32px;
}

.value-material-select {
  width: 100%;
}

.restriction-sentence {
  display: flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
}

.restriction-sentence__condition,
.restriction-sentence__target {
  width: 150px;
}

.restriction-sentence__operator {
  width: 82px;
}

.restriction-sentence__value {
  width: 140px;
}

.restriction-sentence__action {
  width: 110px;
}

@media (max-width: 1440px) {
  .option-overview {
    grid-template-columns: repeat(3, minmax(150px, 1fr));
  }

  .option-workbench,
  .option-edit-grid {
    grid-template-columns: 1fr;
  }

  .option-tree-panel {
    min-height: auto;
  }

  .option-tree {
    max-height: 320px;
  }
}

@media (max-width: 1180px) {
  .option-overview,
  .option-form {
    grid-template-columns: repeat(2, minmax(150px, 1fr));
  }
}
</style>
