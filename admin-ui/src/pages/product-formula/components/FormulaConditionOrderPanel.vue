<template>
  <div class="condition-panel condition-panel--order">
    <el-select v-model="field" filterable>
      <el-option v-for="option in fields" :key="option.value" :label="option.label" :value="option.value" />
    </el-select>
    <el-select v-model="operator" class="condition-panel__operator">
      <el-option v-for="item in compareOperators" :key="item" :label="item" :value="item" />
    </el-select>
    <el-input v-model="value" :placeholder="t('productCenter.formulaSetup.conditionValue')" />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { buildOrderCondition, compareOperators, type ConditionBuildResult, type SelectOption } from './formulaConditionEditor'

const props = defineProps<{
  t: (key: string) => string
}>()

const emit = defineEmits<{
  change: [value: ConditionBuildResult]
}>()

const fields = computed<SelectOption[]>(() => [
  { value: 'orderWidthIn', label: props.t('productCenter.formulaSetup.orderWidthIn') },
  { value: 'orderLengthIn', label: props.t('productCenter.formulaSetup.orderLengthIn') },
  { value: 'orderWidthCm', label: props.t('productCenter.formulaSetup.orderWidthCm') },
  { value: 'orderLengthCm', label: props.t('productCenter.formulaSetup.orderLengthCm') },
  { value: 'orderAreaM2', label: props.t('productCenter.formulaSetup.orderAreaM2') }
])
const field = ref('orderWidthIn')
const operator = ref('>')
const value = ref('')
const selectedField = computed(() => fields.value.find((item) => item.value === field.value))

watch([field, operator, value, fields], () => {
  emit('change', buildOrderCondition(selectedField.value, operator.value, value.value))
}, { immediate: true })
</script>
