<template>
  <aside class="option-tree-panel">
    <div class="option-tree-panel__header">
      <div>
        <h3>{{ t('productCenter.formulaSetup.optionTree') }}</h3>
        <p>{{ t('productCenter.formulaSetup.optionTreeHint') }}</p>
      </div>
      <div class="option-tree-panel__actions">
        <el-button type="primary" plain :icon="Plus" @click="$emit('addRootOption')">
          {{ t('productCenter.formulaSetup.addRootOption') }}
        </el-button>
      </div>
    </div>

    <div class="option-tree" role="tree">
      <button
        v-for="node in nodes"
        :key="node.id"
        type="button"
        class="option-tree__node"
        :class="{
          'option-tree__node--active': node.id === selectedNodeId,
          'option-tree__node--group': node.type === 'group',
          'option-tree__node--root': node.level === 0,
          'option-tree__node--value': node.type === 'value',
          'option-tree__node--issue': node.issue
        }"
        :style="{ '--tree-indent': `${node.level * 18}px` }"
        :title="node.title"
        @click="$emit('selectNode', node.id)"
      >
        <span class="option-tree__branch" />
        <span class="option-tree__toggle" :class="{ 'option-tree__toggle--empty': !node.hasChildren }" @click.stop="$emit('toggleNode', node.id)">
          <el-icon v-if="node.hasChildren">
            <CaretBottom v-if="node.expanded" />
            <CaretRight v-else />
          </el-icon>
        </span>
        <span class="option-tree__kind">{{ node.kindText }}</span>
        <span class="option-tree__body">
          <span class="option-tree__title">{{ node.title }}</span>
          <span class="option-tree__meta">{{ node.meta }}</span>
        </span>
      </button>
      <el-empty v-if="!totalCount" :description="t('productCenter.formulaSetup.selectOptionHint')" :image-size="80" />
    </div>
  </aside>
</template>

<script setup lang="ts">
import { CaretBottom, CaretRight, Plus } from '@element-plus/icons-vue'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'

export type FormulaOptionTreeViewNode = {
  id: string
  type: 'group' | 'option' | 'value'
  level: number
  issue?: boolean
  title: string
  meta: string
  kindText: string
  hasChildren: boolean
  expanded: boolean
}

defineProps<{
  nodes: FormulaOptionTreeViewNode[]
  totalCount: number
  selectedNodeId: string
}>()

defineEmits<{
  addRootOption: []
  selectNode: [nodeId: string]
  toggleNode: [nodeId: string]
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
</script>

<style scoped>
.option-tree-panel {
  background: #fff;
  border: 1px solid #e6ebf2;
  border-radius: 8px;
}

.option-tree-panel__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
  padding: 16px;
  border-bottom: 1px solid #edf1f7;
}

.option-tree-panel__header h3 {
  margin: 0 0 4px;
  color: #111827;
  font-size: 16px;
}

.option-tree-panel__header p {
  margin: 0;
  color: #6b7280;
  font-size: 13px;
}

.option-tree-panel__actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
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
  gap: 8px;
  min-height: 46px;
  padding: 6px 10px 6px calc(8px + var(--tree-indent));
  color: #1f2937;
  background: transparent;
  border: 1px solid transparent;
  border-radius: 8px;
  cursor: pointer;
  text-align: left;
}

.option-tree__node:hover {
  background: #f8fbff;
  border-color: #dbe8ff;
}

.option-tree__node--active {
  background: #edf5ff;
  border-color: #8bbcff;
  box-shadow: inset 3px 0 0 #1677ff;
}

.option-tree__node--issue {
  background: #fff8eb;
}

.option-tree__branch {
  position: absolute;
  left: calc(15px + var(--tree-indent));
  top: -8px;
  width: 14px;
  height: 26px;
  border-bottom: 1px solid #d7e2f1;
  border-left: 1px solid #d7e2f1;
  pointer-events: none;
}

.option-tree__node--root .option-tree__branch {
  display: none;
}

.option-tree__toggle {
  z-index: 1;
  display: inline-flex;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  color: #5f6f86;
  background: #fff;
  border: 1px solid #dbe5f2;
  border-radius: 6px;
}

.option-tree__toggle--empty {
  visibility: hidden;
}

.option-tree__kind {
  display: inline-flex;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;
  min-width: 52px;
  height: 24px;
  padding: 0 8px;
  color: #1677ff;
  font-size: 12px;
  font-weight: 700;
  background: #edf5ff;
  border: 1px solid #bfdaff;
  border-radius: 999px;
}

.option-tree__node--value .option-tree__kind {
  color: #16a34a;
  background: #ecfdf3;
  border-color: #bbf7d0;
}

.option-tree__node--issue .option-tree__kind {
  color: #d97706;
  background: #fff7ed;
  border-color: #fed7aa;
}

.option-tree__body {
  display: grid;
  min-width: 0;
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
  color: #8a94a6;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
