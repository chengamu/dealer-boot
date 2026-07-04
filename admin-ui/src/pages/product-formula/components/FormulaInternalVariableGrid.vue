<template>
  <section class="internal-variable-grid">
    <div class="internal-variable-grid__head">
      <h4>{{ t('productCenter.formulaSetup.internalVariables') }}</h4>
      <div class="internal-variable-grid__actions">
        <el-button size="small" plain @click="$emit('copy')">{{ t('productCenter.formulaSetup.copyVariables') }}</el-button>
        <el-button size="small" type="primary" plain @click="$emit('add')">{{ t('common.add') }}</el-button>
      </div>
    </div>
    <el-input
      v-model="keyword"
      clearable
      size="small"
      :placeholder="t('productCenter.formulaSetup.variableSearchPlaceholder')"
    />
    <div class="internal-variable-list">
      <div
        v-for="row in visibleVariables"
        :key="row.variableKey || row.variableCode"
        class="internal-variable-row"
        @dblclick="$emit('insert', row)"
      >
        <div class="internal-variable-row__top">
          <strong>{{ row.variableName || '-' }}</strong>
          <span class="internal-variable-row__actions">
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
  return left.variableCode === right.variableCode
}

function variableActions(row: ProductFormulaVariableVO): AdminTableAction[] {
  return [
    { label: props.t('common.edit'), icon: 'Edit', primary: true, stopPropagation: true, onClick: () => emit('edit', row) },
    { label: props.t('common.delete'), icon: 'Delete', type: 'danger', danger: true, stopPropagation: true, onClick: () => emit('remove', row) }
  ]
}
</script>

<style scoped>
.internal-variable-grid {
  display: grid;
  grid-template-rows: auto auto auto;
  gap: 8px;
  min-width: 0;
  height: 100%;
  padding: 8px 10px;
  border: 1px solid #e5ecf6;
  border-radius: 8px;
  background: #f8fbff;
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

.internal-variable-list {
  display: grid;
  gap: 6px;
}

.internal-variable-row {
  display: grid;
  gap: 4px;
  padding: 9px 10px;
  border: 1px solid #e5ecf6;
  border-radius: 8px;
  background: #fff;
  cursor: default;
}

.internal-variable-row__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
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
  color: #6b7280;
  font-size: 12px;
  line-height: 1.45;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
