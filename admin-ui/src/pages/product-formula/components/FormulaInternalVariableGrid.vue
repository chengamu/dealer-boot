<template>
  <section class="internal-variable-grid">
    <div class="internal-variable-grid__head">
      <h4>{{ t('productCenter.formulaSetup.internalVariables') }}</h4>
      <div v-if="!readonly" class="internal-variable-grid__actions">
        <el-button size="small" plain @click="$emit('copy')">{{ t('productCenter.formulaSetup.copyVariables') }}</el-button>
        <el-button size="small" type="primary" plain @click="$emit('add')">{{ t('common.add') }}</el-button>
      </div>
    </div>
    <el-input
      v-model="keyword"
      class="internal-variable-grid__search"
      clearable
      :placeholder="t('productCenter.formulaSetup.variableSearchPlaceholder')"
    />
    <div class="internal-variable-list">
      <div
        v-for="row in visibleVariables"
        :key="row.variableKey || row.variableCode"
        class="internal-variable-row"
        :class="{ 'is-selected': isSelected(row) }"
        @click="handleRowClick(row)"
        @dblclick="$emit('insert', row)"
      >
        <div class="internal-variable-row__top">
          <strong>{{ row.variableName || '-' }}</strong>
          <span v-if="!readonly" class="internal-variable-row__actions">
            <AdminTableActions :actions="variableActions(row)" />
          </span>
        </div>
        <div class="internal-variable-row__summary">{{ variableSummary(row) }}</div>
      </div>
      <el-empty v-if="!visibleVariables.length" description="-" :image-size="48" />
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { formatUsageNumber } from '../utils/formulaExpression'
import AdminTableActions, { type AdminTableAction } from '@/components/AdminTableActions/index.vue'
import type { ProductFormulaVariableRuleVO, ProductFormulaVariableVO } from '@/api/product-capability/types'

const props = defineProps<{
  variables: ProductFormulaVariableVO[]
  variableRules: ProductFormulaVariableRuleVO[]
  t: (key: string) => string
  readonly?: boolean
  selectedVariable?: ProductFormulaVariableVO | null
}>()

const emit = defineEmits<{
  add: []
  edit: [row: ProductFormulaVariableVO]
  remove: [row: ProductFormulaVariableVO]
  copy: []
  insert: [row: ProductFormulaVariableVO]
}>()

const keyword = ref('')
const visibleVariables = computed(() => {
  const text = keyword.value.trim().toLowerCase()
  return props.variables.filter((row) => {
    if (!text) return true
    return [row.variableCode, row.variableName].some((field) => String(field || '').toLowerCase().includes(text))
  }).slice().sort((left, right) => Number(left.sortOrder || 0) - Number(right.sortOrder || 0))
})

function variableSummary(row: ProductFormulaVariableVO) {
  const rules = props.variableRules.filter((rule) => sameVariableRef(rule, row))
  const defaultRule = rules.find((rule) => rule.defaultRuleFlag) || rules[0]
  if (!defaultRule) return '-'
  const base = defaultRule.valueType === 'FORMULA'
    ? `${row.variableName || row.variableCode} = ${defaultRule.formulaText || defaultRule.formulaExpression || '-'}`
    : `${props.t('productCenter.formulaSetup.defaultUsageRule')} ${formatUsageNumber(defaultRule.fixedValue)}`
  const extraCount = Math.max(0, rules.length - 1)
  return extraCount ? `${base}，${props.t('productCenter.formulaSetup.moreVariableRules').replace('{count}', String(extraCount))}` : base
}

function sameVariableRef(left: ProductFormulaVariableRuleVO, right: ProductFormulaVariableVO) {
  if (left.variableKey && right.variableKey) return left.variableKey === right.variableKey
  if (left.variableId && right.variableId) return String(left.variableId) === String(right.variableId)
  return left.variableCode === right.variableCode
}

function isSelected(row: ProductFormulaVariableVO) {
  const selected = props.selectedVariable
  if (!selected) return false
  if (row.variableKey && selected.variableKey) return row.variableKey === selected.variableKey
  if (row.variableId && selected.variableId) return String(row.variableId) === String(selected.variableId)
  return Boolean(row.variableCode && selected.variableCode && row.variableCode === selected.variableCode)
}

function handleRowClick(row: ProductFormulaVariableVO) {
  if (!props.readonly) emit('edit', row)
}

function variableActions(row: ProductFormulaVariableVO): AdminTableAction[] {
  return [
    { label: props.t('common.delete'), icon: 'Delete', type: 'danger', danger: true, stopPropagation: true, onClick: () => emit('remove', row) }
  ]
}
</script>

<style scoped>
.internal-variable-grid {
  display: grid;
  grid-template-rows: auto auto minmax(0, 1fr);
  gap: 8px;
  min-width: 0;
  height: 100%;
  padding: 10px;
  border: 1px solid #e5ecf6;
  border-radius: 8px;
  background: #fff;
}

.internal-variable-grid__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.internal-variable-grid__actions {
  display: flex;
  gap: 6px;
}

.internal-variable-grid__search :deep(.el-input__wrapper) {
  min-height: 38px;
  border-radius: 8px;
}

.internal-variable-list {
  display: grid;
  gap: 6px;
  align-content: start;
  min-height: 0;
  overflow-y: auto;
}

.internal-variable-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 2px 8px;
  padding: 8px 10px;
  border: 1px solid #e5ecf6;
  border-radius: 8px;
  background: #f8fbff;
  cursor: pointer;
  transition: border-color 0.15s ease, background-color 0.15s ease, box-shadow 0.15s ease;
}

.internal-variable-row:hover,
.internal-variable-row.is-selected {
  border-color: #8bbdff;
  background: #eef6ff;
}

.internal-variable-row.is-selected {
  box-shadow: inset 3px 0 0 #2f7df6;
}

.internal-variable-row__top {
  display: grid;
  grid-column: 1 / -1;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: center;
  gap: 8px;
}

.internal-variable-row__top strong {
  min-width: 0;
  color: #1f2937;
  font-size: 13px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.internal-variable-row__actions {
  display: flex;
  flex: 0 0 auto;
  gap: 8px;
}

.internal-variable-row__actions :deep(.el-button) {
  margin-left: 0;
}

.internal-variable-row__summary {
  grid-column: 1 / -1;
  color: #6b7280;
  font-size: 12px;
  line-height: 1.45;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
