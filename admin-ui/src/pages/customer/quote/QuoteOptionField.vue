<template>
  <div class="quote-option-field">
    <label>{{ optionLabel }}<em v-if="option.requiredFlag">*</em></label>
    <el-switch v-if="switchOption" :model-value="modelValue === switchCodes.active" :disabled="readonly" @update:model-value="updateSwitch" />
    <el-select v-else :model-value="selectValue" :disabled="readonly" filterable clearable :multiple="multiple" @change="updateSelect">
      <el-option v-for="value in values" :key="value.valueCode" :label="valueLabel(value)" :value="value.valueCode" />
    </el-select>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { QuoteLanguage } from '@/api/customer/quote'
import type { ProductFormulaOptionVO, ProductFormulaOptionValueVO } from '@/api/product-capability/types'

const props = defineProps<{
  option: ProductFormulaOptionVO
  values: ProductFormulaOptionValueVO[]
  modelValue?: string
  language: QuoteLanguage
  readonly?: boolean
}>()
const emit = defineEmits<{ 'update:modelValue': [value: string] }>()
const multiple = computed(() => props.option.selectionMode === 'MULTIPLE')
const switchOption = computed(() => props.option.sourceType === 'BOOLEAN' || props.option.selectionMode === 'SWITCH')
const switchCodes = computed(() => {
  const active = props.option.defaultValueCode || props.values.find((value) => value.defaultFlag)?.valueCode || props.values[1]?.valueCode || props.values[0]?.valueCode || ''
  return { active, inactive: props.values.find((value) => value.valueCode !== active)?.valueCode || '' }
})
const selectValue = computed(() => multiple.value ? splitCodes(props.modelValue) : props.modelValue)
const optionLabel = computed(() => props.language === 'EN_US'
  ? props.option.optionNameEn || `[${props.option.optionCode || '-'}]`
  : props.option.optionNameCn || props.option.optionCode || '-')

function valueLabel(value: ProductFormulaOptionValueVO) {
  return props.language === 'EN_US' ? value.valueNameEn || `[${value.valueCode || '-'}]` : value.valueNameCn || value.valueCode || '-'
}
function updateSelect(value: string | string[]) { emit('update:modelValue', Array.isArray(value) ? value.join(',') : String(value || '')) }
function updateSwitch(checked: string | number | boolean) { emit('update:modelValue', checked ? switchCodes.value.active : switchCodes.value.inactive) }
function splitCodes(value?: string) { return String(value || '').split(',').map((code) => code.trim()).filter(Boolean) }
</script>

<style scoped>
.quote-option-field { min-width: 0; }
.quote-option-field label { display: flex; gap: 3px; margin-bottom: 5px; color: #344054; font-size: 13px; font-weight: 600; }
.quote-option-field label em { color: #ef4444; font-style: normal; }
.quote-option-field :deep(.el-select) { width: 100%; }
.quote-option-field :deep(.el-select__wrapper) { min-height: 34px; }
</style>
