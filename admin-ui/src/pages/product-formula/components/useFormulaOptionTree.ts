import { computed, ref, watch } from 'vue'
import type { ProductFormulaOptionMaterialVO, ProductFormulaOptionVO, ProductFormulaOptionValueVO } from '@/api/product-capability/types'
import {
  materialValueClientKey,
  optionClientKey,
  valueClientKey,
  valueOwnerClientKey,
  type DraftOption
} from '../utils/formulaOptionDraftIdentity'
import type { FormulaOptionTreeViewNode } from './FormulaOptionTree.vue'

export type OptionTreeNode = {
  id: string
  type: 'group' | 'option' | 'value'
  level: number
  parentId?: string
  label?: string
  option?: ProductFormulaOptionVO
  value?: ProductFormulaOptionValueVO
  issue?: boolean
}

type OptionTreeProps = {
  options: ProductFormulaOptionVO[]
  optionValues: ProductFormulaOptionValueVO[]
  allOptionValues: ProductFormulaOptionValueVO[]
  optionMaterials: ProductFormulaOptionMaterialVO[]
  selectedOptionCode: string
}

type UseOptionTreeOptions = {
  t: (key: string, params?: Record<string, string | number>) => string
  sourceText: (option: ProductFormulaOptionVO) => string
  onOptionChange: (row?: ProductFormulaOptionVO) => void
}

const ALL_VALUE_FILTER = '__ALL__'

export function useFormulaOptionTree(props: OptionTreeProps, options: UseOptionTreeOptions) {
  const selectedValueCode = ref(ALL_VALUE_FILTER)
  const selectedNodeId = ref('')
  const expandedNodeIds = ref<Set<string>>(new Set())
  const sortedOptions = computed(() => [...props.options].sort(sortByOrder))
  const selectedOption = computed(() => props.options.find((row) => sameOptionKey(row, props.selectedOptionCode)))
  const selectedValue = computed(() => props.optionValues.find((row) => sameValueKey(row, selectedValueCode.value)))
  const siblingOptions = computed(() => selectedOption.value ? siblingOptionsFor(selectedOption.value) : [])
  const currentOptionIndex = computed(() => siblingOptions.value.findIndex((row) => optionClientKey(row) === optionClientKey(selectedOption.value)))
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
  const optionTreeNodes = computed<OptionTreeNode[]>(() => {
    const nodes: OptionTreeNode[] = []
    const visited = new Set<string>()
    const roots = sortedOptions.value.filter((option) => option.visibilityMode !== 'CONDITIONAL')

    roots.forEach((option) => appendOptionNode(nodes, visited, option, 0))

    const unclassified = sortedOptions.value.filter((option) => option.visibilityMode === 'CONDITIONAL' && optionClientKey(option) && !visited.has(optionClientKey(option)))
    if (unclassified.length) {
      nodes.push({
        id: 'group:unclassified',
        type: 'group',
        level: 0,
        label: options.t('productCenter.formulaSetup.unclassifiedOptions'),
        issue: true
      })
      unclassified.forEach((option) => appendOptionNode(nodes, visited, option, 1, true, 'group:unclassified'))
    }

    return nodes
  })

  watch(() => props.selectedOptionCode, () => {
    ensureSelectedValue()
    ensureSelectedNode(true)
  })

  watch(() => optionTreeNodes.value.map((node) => node.id).join('|'), () => {
    ensureSelectedNode()
  }, { immediate: true })

  watch(() => props.optionValues.map((row) => String(row.valueCode || '')).join('|'), () => {
    ensureSelectedValue()
  }, { immediate: true })

  function appendOptionNode(nodes: OptionTreeNode[], visited: Set<string>, option: ProductFormulaOptionVO, level: number, issue = false, parentId?: string) {
    const optionKey = optionClientKey(option)
    if (!optionKey || visited.has(optionKey)) return
    visited.add(optionKey)
    const nodeId = optionNodeId(option)
    nodes.push({ id: nodeId, type: 'option', level, parentId, option, issue })
    valuesForOption(option).forEach((value) => {
      const valueId = valueNodeId(option, value)
      nodes.push({ id: valueId, type: 'value', level: level + 1, parentId: nodeId, option, value })
      childOptionsForValue(option, value).forEach((child) => {
        appendOptionNode(nodes, visited, child, level + 2, false, valueId)
      })
    })
  }

  function optionNodeId(option: ProductFormulaOptionVO) {
    return `option:${optionClientKey(option) || option.optionCode || ''}`
  }

  function valueNodeId(option: ProductFormulaOptionVO, value: ProductFormulaOptionValueVO) {
    return `value:${optionClientKey(option) || option.optionCode || ''}:${valueClientKey(value) || value.valueCode || ''}`
  }

  function childOptionsForValue(parentOption?: ProductFormulaOptionVO, value?: ProductFormulaOptionValueVO) {
    const optionKey = optionClientKey(parentOption)
    const optionCode = parentOption?.optionCode
    const valueKey = valueClientKey(value)
    const valueCode = value?.valueCode
    return sortedOptions.value.filter((row) => (
      row.visibilityMode === 'CONDITIONAL'
      && ((row as DraftOption).visibleConditionOptionClientKey
        ? (row as DraftOption).visibleConditionOptionClientKey === optionKey
        : row.visibleConditionOptionCode === optionCode)
      && ((row as DraftOption).visibleConditionValueClientKey
        ? (row as DraftOption).visibleConditionValueClientKey === valueKey
        : row.visibleConditionValueCode === valueCode)
    ))
  }

  function siblingOptionsFor(option: ProductFormulaOptionVO) {
    if (option.visibilityMode === 'CONDITIONAL') {
      return sortedOptions.value.filter((row) => (
        row.visibilityMode === 'CONDITIONAL'
        && ((row as DraftOption).visibleConditionOptionClientKey || row.visibleConditionOptionCode) === ((option as DraftOption).visibleConditionOptionClientKey || option.visibleConditionOptionCode)
        && ((row as DraftOption).visibleConditionValueClientKey || row.visibleConditionValueCode) === ((option as DraftOption).visibleConditionValueClientKey || option.visibleConditionValueCode)
      ))
    }
    return sortedOptions.value.filter((row) => row.visibilityMode !== 'CONDITIONAL')
  }

  function nodeKindText(node: OptionTreeNode) {
    if (node.type === 'group') return '!'
    return node.type === 'option' ? options.t('productCenter.formulaSetup.optionNode') : options.t('productCenter.formulaSetup.valueNode')
  }

  function nodeTitle(node: OptionTreeNode) {
    if (node.type === 'group') return node.label || ''
    if (node.type === 'value') return node.value?.valueNameCn || node.value?.valueCode || options.t('productCenter.formulaSetup.unnamedValue')
    return node.option?.optionNameCn || node.option?.optionCode || options.t('productCenter.formulaSetup.unnamedOption')
  }

  function nodeMeta(node: OptionTreeNode) {
    if (node.type === 'group') return options.t('productCenter.formulaSetup.unclassifiedHint')
    if (node.type === 'value') {
      return `${materialsForValue(node.value).length} ${options.t('productCenter.formulaSetup.linkedMaterial')}`
    }
    return node.option ? `${options.sourceText(node.option)} · ${valuesForOption(node.option).length} ${options.t('productCenter.formulaSetup.itemUnit')}` : ''
  }

  function selectTreeNode(node: OptionTreeNode) {
    if (node.type === 'group') {
      toggleTreeNode(node)
      return
    }
    selectedNodeId.value = node.id
    expandNodePath(node.id, node.type === 'option')
    if (node.option?.optionCode) options.onOptionChange(node.option)
    selectedValueCode.value = node.type === 'value'
      ? valueClientKey(node.value) || node.value?.valueCode || ALL_VALUE_FILTER
      : valueClientKey(valuesForOption(node.option)[0]) || valuesForOption(node.option)[0]?.valueCode || ALL_VALUE_FILTER
  }

  function selectTreeNodeById(nodeId: string) {
    const node = optionTreeNodes.value.find((item) => item.id === nodeId)
    if (node) selectTreeNode(node)
  }

  function ensureSelectedNode(reveal = false) {
    if (optionTreeNodes.value.some((node) => node.id === selectedNodeId.value)) {
      if (reveal) expandNodePath(selectedNodeId.value, selectedNode.value?.type === 'option')
      return
    }
    const option = selectedOption.value || props.options[0]
    selectedNodeId.value = option ? optionNodeId(option) : ''
    if (reveal && selectedNodeId.value) expandNodePath(selectedNodeId.value, true)
  }

  function ensureSelectedValue() {
    const option = selectedOption.value
    if (props.optionValues.some((row) => sameValueKey(row, selectedValueCode.value) && (!option || valueBelongsToOption(row, option)))) return
    const first = option ? valuesForOption(option)[0] : props.optionValues[0]
    selectedValueCode.value = valueClientKey(first) || first?.valueCode || ALL_VALUE_FILTER
  }

  function materialsForValue(row?: ProductFormulaOptionValueVO) {
    return props.optionMaterials.filter((item) => {
      const materialKey = materialValueClientKey(item)
      return materialKey ? materialKey === valueClientKey(row) : item.optionCode === row?.optionCode && item.valueCode === row?.valueCode
    })
  }

  function valuesForOption(option?: ProductFormulaOptionVO | string) {
    const optionKey = typeof option === 'string' ? option : optionClientKey(option)
    const optionCode = typeof option === 'string' ? option : option?.optionCode
    return props.allOptionValues.filter((row) => (
      valueOwnerClientKey(row) ? valueOwnerClientKey(row) === optionKey : row.optionCode === optionCode
    )).sort(sortByOrder)
  }

  function selectOptionValue(row: ProductFormulaOptionValueVO) {
    selectedValueCode.value = valueClientKey(row) || row.valueCode || ALL_VALUE_FILTER
    selectedNodeId.value = valueNodeId(selectedOption.value || {}, row)
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
    next.has(node.id) ? next.delete(node.id) : next.add(node.id)
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

  return {
    ALL_VALUE_FILTER,
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
  }
}

function sameOptionKey(row: ProductFormulaOptionVO, key?: string) {
  return optionClientKey(row) === key || (!optionClientKey(row) && row.optionCode === key)
}

function sameValueKey(row: ProductFormulaOptionValueVO, key?: string) {
  return valueClientKey(row) === key || (!valueClientKey(row) && row.valueCode === key)
}

function valueBelongsToOption(row: ProductFormulaOptionValueVO, option: ProductFormulaOptionVO) {
  return valueOwnerClientKey(row) ? valueOwnerClientKey(row) === optionClientKey(option) : row.optionCode === option.optionCode
}

function sortByOrder<T extends { sortOrder?: number }>(a: T, b: T) {
  return (a.sortOrder || 0) - (b.sortOrder || 0)
}
