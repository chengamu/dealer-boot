<template>
  <el-alert
    v-if="optionChoices.length === 0"
    :closable="false"
    show-icon
    type="info"
    :title="t('productCenter.formulaSetup.conditionNoOptions')"
  />
  <div v-else class="condition-grid">
    <div v-for="(row, index) in rows" :key="index" class="condition-grid__row">
      <el-select v-if="index > 0" v-model="row.joiner" class="condition-grid__joiner">
        <el-option label="并且" value="AND" />
        <el-option label="或者" value="OR" />
      </el-select>
      <span v-else class="condition-grid__joiner condition-grid__joiner-text">当</span>
      <el-select v-model="row.optionRef" filterable @change="row.valueRef = ''">
        <el-option v-for="option in optionChoices" :key="option.value" :label="option.label" :value="option.value" />
      </el-select>
      <el-select v-model="row.operator" class="condition-grid__operator">
        <el-option v-for="item in optionOperators" :key="item" :label="item" :value="item" />
      </el-select>
      <el-select v-model="row.valueRef" filterable>
        <el-option v-for="option in valueChoices(row.optionRef)" :key="option.value" :label="option.label" :value="option.value" />
      </el-select>
      <el-button v-if="rows.length > 1" text type="danger" @click="removeRow(index)">{{ t('common.delete') }}</el-button>
      <p v-if="row.optionRef && valueChoices(row.optionRef).length === 0" class="condition-grid__hint">
        {{ t('productCenter.formulaSetup.conditionNoOptionValues') }}
      </p>
    </div>
    <el-button plain @click="addRow">{{ t('productCenter.formulaSetup.addConditionRow') }}</el-button>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, watch } from 'vue'
import {
  buildOptionCondition,
  optionOperators,
  optionSelectOptions,
  valueSelectOptions,
  type ConditionBuildResult,
  type OptionConditionRow
} from './formulaConditionEditor'
import type { ProductFormulaOptionVO, ProductFormulaOptionValueVO } from '@/api/product-capability/types'

const props = defineProps<{
  t: (key: string) => string
  options: ProductFormulaOptionVO[]
  optionValues: ProductFormulaOptionValueVO[]
}>()

const emit = defineEmits<{
  change: [value: ConditionBuildResult]
}>()

const rows = reactive<OptionConditionRow[]>([emptyRow()])
const optionChoices = computed(() => optionSelectOptions(props.options))

watch(rows, () => {
  emit('change', buildOptionCondition(rows, props.options, props.optionValues))
}, { deep: true, immediate: true })

function valueChoices(optionRef: string) {
  return valueSelectOptions(optionRef, props.options, props.optionValues)
}

function addRow() {
  rows.push(emptyRow())
}

function removeRow(index: number) {
  rows.splice(index, 1)
}

function emptyRow(): OptionConditionRow {
  return { joiner: 'AND', optionRef: '', operator: '=', valueRef: '' }
}
</script>
