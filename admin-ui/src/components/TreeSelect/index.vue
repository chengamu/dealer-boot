<template>
  <div class="el-tree-select">
    <el-select
      style="width: 100%"
      v-model="valueId"
      ref="treeSelect"
      :filterable="true"
      :clearable="true"
      @clear="clearHandle"
      :filter-method="selectFilterData"
      :placeholder="placeholder"
    >
      <el-option :value="valueId" :label="valueTitle">
        <el-tree
          id="tree-option"
          ref="selectTree"
          :accordion="accordion"
          :data="options"
          :props="objMap"
          :node-key="objMap.value"
          :expand-on-click-node="false"
          :default-expanded-keys="defaultExpandedKey"
          :filter-node-method="filterNode"
          @node-click="handleNodeClick"
        ></el-tree>
      </el-option>
    </el-select>
  </div>
</template>

<script setup lang="ts">
import type { PropType } from 'vue'

type TreeValue = string | number

type TreeOption = {
  [key: string]: unknown
  children?: TreeOption[]
}

type TreeObjMap = {
  value: string
  label: string
  children: string
}

type SelectTreeRef = {
  getNode: (key: TreeValue) => { data: TreeOption } | undefined
  setCurrentKey: (key: TreeValue) => void
  filter: (value: string) => void
}

type TreeSelectRef = {
  blur: () => void
}

const props = defineProps({
  /* 配置项 */
  objMap: {
    type: Object as PropType<TreeObjMap>,
    default: () => {
      return {
        value: 'id', // ID字段名
        label: 'label', // 显示名称
        children: 'children' // 子级字段名
      }
    }
  },
  /* 自动收起 */
  accordion: {
    type: Boolean,
    default: () => {
      return false
    }
  },
  /**当前双向数据绑定的值 */
  value: {
    type: [String, Number],
    default: ''
  },
  /**当前的数据 */
  options: {
    type: Array as PropType<TreeOption[]>,
    default: () => []
  },
  /**输入框内部的文字 */
  placeholder: {
    type: String,
    default: ''
  }
})

const emit = defineEmits<{
  (event: 'update:value', value: TreeValue | ''): void
}>();

const selectTree = ref<SelectTreeRef | null>(null);
const treeSelect = ref<TreeSelectRef | null>(null);

const valueId = computed({
  get: () => props.value,
  set: (val: TreeValue | '') => {
    emit('update:value', val)
  }
});
const valueTitle = ref('');
const defaultExpandedKey = ref<TreeValue[]>([]);

function initHandle() {
  nextTick(() => {
    const selectedValue = valueId.value;
    if(selectedValue !== null && typeof (selectedValue) !== 'undefined') {
      const node = selectTree.value?.getNode(selectedValue)
      if (node) {
        valueTitle.value = String(node.data[props.objMap.label] || '')
        selectTree.value?.setCurrentKey(selectedValue) // 设置默认选中
        defaultExpandedKey.value = [selectedValue] // 设置默认展开
      }
    } else {
      clearHandle()
    }
  })
}
function handleNodeClick(node: TreeOption) {
  valueTitle.value = String(node[props.objMap.label] || '')
  valueId.value = node[props.objMap.value] as TreeValue;
  defaultExpandedKey.value = [];
  treeSelect.value?.blur()
  selectFilterData('')
}
function selectFilterData(val: string) {
  selectTree.value?.filter(val)
}
function filterNode(value: string, data: TreeOption) {
  if (!value) return true
  return String(data[props.objMap.label] || '').indexOf(value) !== -1
}
function clearHandle() {
  valueTitle.value = ''
  valueId.value = ''
  defaultExpandedKey.value = [];
  clearSelected()
}
function clearSelected() {
  const allNode = document.querySelectorAll('#tree-option .el-tree-node')
  allNode.forEach((element) => element.classList.remove('is-current'))
}

onMounted(() => {
  initHandle()
})

watch(valueId, () => {
  initHandle();
})
</script>

<style lang='scss' scoped>
@use "sass:color";
@use "@/assets/styles/variables.module.scss" as *;
.el-scrollbar .el-scrollbar__view .el-select-dropdown__item {
  padding: 0;
  background-color: #fff;
  height: auto;
}

.el-select-dropdown__item.selected {
  font-weight: normal;
}

ul li .el-tree .el-tree-node__content {
  height: auto;
  padding: 0 20px;
  box-sizing: border-box;
}

:deep(.el-tree-node__content:hover),
:deep(.el-tree-node__content:active),
:deep(.is-current > div:first-child),
:deep(.el-tree-node__content:focus) {
  background-color: color.mix(#fff, $color-primary, 90%);
  color: $color-primary;
}
</style>
