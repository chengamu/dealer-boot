<template>
  <div class="business-inch-input">
    <el-input
      :model-value="wholeDraft"
      :disabled="disabled"
      :readonly="readonly"
      inputmode="decimal"
      :class="{ 'is-invalid': invalid }"
      :aria-label="integerAriaLabel"
      :aria-invalid="invalid"
      @input="handleWholeInput"
      @blur="handleBlur"
    />
    <el-select v-model="numerator" clearable :disabled="disabled || readonly" :aria-label="fractionAriaLabel" @change="emitValue">
      <el-option v-for="option in options" :key="option.value" :label="option.label" :value="option.value" />
    </el-select>
    <span class="business-inch-input__unit">in</span>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import {
  canonicalDecimal,
  compareDecimals,
  decimalToInchParts,
  fractionOptions,
  inchPartsToDecimal,
  isSupportedDenominator,
  type BusinessNumberValue
} from '@/utils/businessNumber'
import { useBusinessInputFormValidation } from '@/components/useBusinessInputFormValidation'

const props = withDefaults(defineProps<{
  modelValue?: BusinessNumberValue
  denominator?: number
  min?: number | string
  max?: number | string
  disabled?: boolean
  readonly?: boolean
  integerAriaLabel?: string
  fractionAriaLabel?: string
}>(), {
  denominator: 8,
  min: 0,
  disabled: false,
  readonly: false,
  integerAriaLabel: 'Whole inches',
  fractionAriaLabel: 'Fractional inches'
})

const emit = defineEmits<{
  'update:modelValue': [value: string | null]
  change: [value: string | null]
  validityChange: [valid: boolean]
}>()
const wholeDraft = ref('')
const numerator = ref<number | null>(null)
const invalid = ref(false)
const options = computed(() => fractionOptions(props.denominator))

watch(() => [props.modelValue, props.denominator, props.min, props.max] as const, syncFromModel, { immediate: true })
useBusinessInputFormValidation(validateDraft)
onMounted(() => emit('validityChange', validateDraft()))

function syncFromModel() {
  const text = canonicalDecimal(props.modelValue)
  if (text === null) {
    wholeDraft.value = ''
    numerator.value = null
    setInvalid(false)
    return
  }
  const parts = decimalToInchParts(text, props.denominator)
  wholeDraft.value = parts?.whole ?? text
  numerator.value = parts?.numerator ?? null
  setInvalid(!parts || !withinBounds(text))
}

function emitValue() {
  if (wholeDraft.value === '' && numerator.value === null) {
    setInvalid(false)
    emit('update:modelValue', null)
    emit('change', null)
    return
  }
  const value = inchPartsToDecimal(wholeDraft.value, numerator.value ?? 0, props.denominator)
  const valid = value !== null && withinBounds(value)
  setInvalid(!valid)
  if (!valid) return
  emit('update:modelValue', value)
  emit('change', value)
}

function handleWholeInput(value: string) {
  wholeDraft.value = value.trim()
  if (wholeDraft.value === '') numerator.value = null
  const pastedParts = decimalToInchParts(wholeDraft.value, props.denominator)
  if (pastedParts && wholeDraft.value.includes('.')) {
    wholeDraft.value = pastedParts.whole
    numerator.value = pastedParts.numerator
  }
  emitValue()
}

function handleBlur() {
  setInvalid(!validateDraft())
}

function validateDraft() {
  if (wholeDraft.value === '' && numerator.value === null) return true
  const value = inchPartsToDecimal(wholeDraft.value, numerator.value ?? 0, props.denominator)
  return value !== null && withinBounds(value)
}

function withinBounds(value: string) {
  return (props.min === undefined || (compareDecimals(value, props.min) ?? -1) >= 0)
    && (props.max === undefined || (compareDecimals(value, props.max) ?? 1) <= 0)
}

function setInvalid(value: boolean) {
  if (invalid.value === value) return
  invalid.value = value
  emit('validityChange', !value)
}

if (!isSupportedDenominator(props.denominator)) throw new Error(`Unsupported inch denominator: ${props.denominator}`)

defineExpose({ validate: validateDraft })
</script>

<style scoped>
.business-inch-input { display: grid; grid-template-columns: minmax(96px, 1fr) 104px auto; align-items: center; gap: 8px; width: 100%; }
.business-inch-input :deep(.el-input__inner) { text-align: right; font-variant-numeric: tabular-nums; }
.business-inch-input :deep(.el-input.is-invalid .el-input__wrapper) { box-shadow: 0 0 0 1px var(--el-color-danger) inset; }
.business-inch-input__unit { color: var(--el-text-color-secondary); }
</style>
