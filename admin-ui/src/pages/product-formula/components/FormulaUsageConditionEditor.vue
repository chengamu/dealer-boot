<template>
  <div class="condition-cell">
    <el-select :model-value="conditionMode(row)" @change="handleConditionModeChange">
      <el-option value="DEFAULT" :label="t('productCenter.formulaSetup.defaultUsageRule')" />
      <el-option value="EXPRESSION" :label="t('productCenter.formulaSetup.conditionalUsageRule')" />
    </el-select>
    <el-tag v-if="conditionMode(row) === 'DEFAULT'" effect="plain" type="info">
      {{ t('productCenter.formulaSetup.defaultUsageRule') }}
    </el-tag>
    <div v-else class="condition-cell__expression">
      <span :title="conditionSummary">{{ conditionSummary }}</span>
      <el-button plain @click="openExpressionEditor">
        {{ t('productCenter.formulaSetup.conditionExpressionEditor') }}
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { defaultConditionKey, validateConditionExpression } from '../utils/formulaExpression'
import type { ProductFormulaUsageRuleVO } from '@/api/product-capability/types'

const props = defineProps<{
  row: ProductFormulaUsageRuleVO
}>()

const emit = defineEmits<{
  'default-change': [row: ProductFormulaUsageRuleVO]
  'open-expression': [row: ProductFormulaUsageRuleVO]
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)

function conditionMode(row: ProductFormulaUsageRuleVO) {
  return row.defaultRuleFlag || row.conditionType === 'DEFAULT' ? 'DEFAULT' : 'EXPRESSION'
}

function handleConditionModeChange(value: string | number | boolean | undefined) {
  const mode = String(value || 'DEFAULT')
  if (mode === 'DEFAULT') {
    props.row.defaultRuleFlag = true
    props.row.conditionType = 'DEFAULT'
    props.row.conditionExpression = 'DEFAULT'
    props.row.conditionText = t('productCenter.formulaSetup.defaultUsageRule')
    props.row.conditionKey = defaultConditionKey()
    clearOptionConditionFields()
    emit('default-change', props.row)
    return
  }
  props.row.defaultRuleFlag = false
  props.row.conditionType = 'EXPRESSION'
  if (props.row.conditionExpression === 'DEFAULT') {
    props.row.conditionExpression = undefined
    props.row.conditionText = undefined
    props.row.conditionKey = undefined
  }
  clearOptionConditionFields()
}

function openExpressionEditor() {
  props.row.defaultRuleFlag = false
  props.row.conditionType = 'EXPRESSION'
  emit('open-expression', props.row)
}

function clearOptionConditionFields() {
  props.row.conditionOptionCode = undefined
  props.row.conditionOptionNameCn = undefined
  props.row.conditionValueCode = undefined
  props.row.conditionValueNameCn = undefined
}

const conditionSummary = computed(() => {
  const text = props.row.conditionText || props.row.conditionExpression
  if (!text) return t('productCenter.formulaSetup.conditionExpressionPlaceholder')
  const validationText = props.row.conditionExpression && props.row.conditionExpression !== 'DEFAULT'
    ? props.row.conditionExpression
    : text
  const result = validateConditionExpression(validationText)
  return result.valid ? text : t('productCenter.formulaSetup.expressionInvalid')
})
</script>

<style scoped>
.condition-cell {
  display: grid;
  grid-template-columns: 132px minmax(0, 1fr);
  align-items: center;
  gap: 8px;
}

.condition-cell__expression {
  display: flex;
  align-items: center;
  min-width: 0;
  gap: 8px;
}

.condition-cell__expression span {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  color: #334155;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.condition-cell__expression :deep(.el-button) {
  flex: 0 0 auto;
}
</style>
