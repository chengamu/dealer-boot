<template>
  <el-input
    :model-value="inputText"
    :disabled="disabled"
    :readonly="readonly"
    :placeholder="placeholder"
    :aria-invalid="invalid"
    inputmode="decimal"
    class="business-number-input"
    :class="{ 'is-invalid': invalid }"
    @focus="handleFocus"
    @blur="handleBlur"
    @input="handleInput"
    @change="handleChange"
  >
    <template v-if="unit" #append>{{ unit }}</template>
  </el-input>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import {
  canonicalDecimal,
  formatBusinessNumber,
  resolveNumericContract,
  validateNumeric,
  type BusinessNumberMode,
  type BusinessNumberValue
} from '@/utils/businessNumber'
import { useBusinessInputFormValidation } from '@/components/useBusinessInputFormValidation'

const props = withDefaults(defineProps<{
  modelValue?: BusinessNumberValue
  mode?: BusinessNumberMode
  minFractionDigits?: number
  maxFractionDigits?: number
  unitPrecision?: number
  currencyDigits?: number
  min?: number | string
  max?: number | string
  allowNegative?: boolean
  allowZero?: boolean
  disabled?: boolean
  readonly?: boolean
  placeholder?: string
  unit?: string
}>(), {
  mode: 'QUANTITY',
  allowNegative: false,
  allowZero: true,
  disabled: false,
  readonly: false,
  placeholder: '',
  unit: ''
})

const emit = defineEmits<{
  'update:modelValue': [value: string | null]
  change: [value: BusinessNumberValue]
  blur: []
  validityChange: [valid: boolean]
}>()

const focused = ref(false)
const draft = ref(editableText(props.modelValue))
const contract = computed(() => resolveNumericContract(props.mode, {
  minFractionDigits: props.minFractionDigits,
  maxFractionDigits: props.maxFractionDigits,
  unitPrecision: props.unitPrecision,
  currencyDigits: props.currencyDigits,
  min: props.min,
  max: props.max,
  allowNegative: props.allowNegative,
  allowZero: props.allowZero
}))
const invalid = ref(!validateDraft())
const inputText = computed(() => focused.value || invalid.value
  ? draft.value
  : formatBusinessNumber(props.modelValue, props.mode, {
      minFractionDigits: props.minFractionDigits,
      maxFractionDigits: props.maxFractionDigits,
      unitPrecision: props.unitPrecision,
      currencyDigits: props.currencyDigits,
      min: props.min,
      max: props.max,
      allowNegative: props.allowNegative,
      allowZero: props.allowZero,
      emptyText: ''
    }))

watch([() => props.modelValue, contract], ([value]) => {
  if (!focused.value) draft.value = editableText(value)
  setInvalid(!validateDraft())
})

useBusinessInputFormValidation(validateDraft)
onMounted(() => emit('validityChange', validateDraft()))

function handleFocus() {
  focused.value = true
  if (!invalid.value) draft.value = editableText(props.modelValue)
}

function handleInput(value: string) {
  draft.value = value.replace(/,/g, '')
  const result = validateNumeric(draft.value, contract.value)
  setInvalid(!result.valid)
  if (result.valid) emit('update:modelValue', result.value)
}

function handleBlur() {
  const result = validateNumeric(draft.value, contract.value)
  setInvalid(!result.valid)
  if (result.valid) {
    draft.value = result.value ?? ''
    emit('update:modelValue', result.value)
  }
  focused.value = false
  emit('blur')
}

function handleChange() {
  const result = validateNumeric(draft.value, contract.value)
  if (result.valid) emit('change', result.value)
}

function setInvalid(value: boolean) {
  if (invalid.value === value) return
  invalid.value = value
  emit('validityChange', !value)
}

function editableText(value: BusinessNumberValue) {
  return canonicalDecimal(value) ?? (value == null ? '' : String(value))
}

function validateDraft() {
  return validateNumeric(draft.value, contract.value).valid
}

defineExpose({ validate: () => validateNumeric(draft.value, contract.value) })
</script>

<style scoped>
.business-number-input :deep(.el-input__inner) { text-align: right; font-variant-numeric: tabular-nums; }
.business-number-input.is-invalid :deep(.el-input__wrapper) { box-shadow: 0 0 0 1px var(--el-color-danger) inset; }
</style>
