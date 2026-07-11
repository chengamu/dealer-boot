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
    @change="$emit('change', modelValue)"
  >
    <template v-if="unit" #append>{{ unit }}</template>
  </el-input>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { decimalText, formatBusinessNumber, type BusinessNumberMode, type BusinessNumberValue } from '@/utils/businessNumber'

const props = withDefaults(defineProps<{
  modelValue?: BusinessNumberValue
  mode?: BusinessNumberMode
  minFractionDigits?: number
  maxFractionDigits?: number
  unitPrecision?: number
  currencyDigits?: number
  min?: number
  max?: number
  allowNegative?: boolean
  disabled?: boolean
  readonly?: boolean
  placeholder?: string
  unit?: string
}>(), {
  mode: 'QUANTITY',
  allowNegative: false,
  disabled: false,
  readonly: false,
  placeholder: '',
  unit: ''
})

const emit = defineEmits<{
  'update:modelValue': [value: number | string | null]
  change: [value: BusinessNumberValue]
  blur: []
}>()

const focused = ref(false)
const invalid = ref(false)
const draft = ref(editableText(props.modelValue))
const maxDigits = computed(() => props.mode === 'COUNT'
  ? 0
  : props.maxFractionDigits ?? (props.mode === 'UNIT_PRICE' ? 4 : props.mode === 'MONEY' ? props.currencyDigits ?? 2 : props.unitPrecision ?? 6))
const inputText = computed(() => focused.value
  ? draft.value
  : formatBusinessNumber(props.modelValue, props.mode, {
      minFractionDigits: props.minFractionDigits,
      maxFractionDigits: props.maxFractionDigits,
      unitPrecision: props.unitPrecision,
      currencyDigits: props.currencyDigits,
      emptyText: ''
    }))

watch(() => props.modelValue, (value) => {
  if (!focused.value) draft.value = editableText(value)
})

function handleFocus() {
  focused.value = true
  draft.value = editableText(props.modelValue)
}

function handleInput(value: string) {
  draft.value = sanitize(value)
  const parsed = validValue(draft.value)
  invalid.value = parsed === undefined
  if (parsed !== undefined) emit('update:modelValue', parsed)
}

function handleBlur() {
  const parsed = validValue(draft.value)
  if (parsed === undefined) draft.value = editableText(props.modelValue)
  else emit('update:modelValue', parsed)
  invalid.value = false
  focused.value = false
  emit('blur')
}

function sanitize(value: string) {
  let next = value.replace(/[^\d.-]/g, '').replace(/(?!^)-/g, '')
  if (!props.allowNegative) next = next.replace(/-/g, '')
  const dotIndex = next.indexOf('.')
  if (dotIndex >= 0) next = `${next.slice(0, dotIndex + 1)}${next.slice(dotIndex + 1).replace(/\./g, '').slice(0, maxDigits.value)}`
  return next
}

function validValue(value: string): number | string | null | undefined {
  if (value === '') return null
  if (value === '-' || value === '.' || value === '-.') return undefined
  const normalized = decimalText(value)
  if (normalized === null) return undefined
  const numeric = Number(normalized)
  if (!Number.isFinite(numeric) || (props.min !== undefined && numeric < props.min) || (props.max !== undefined && numeric > props.max)) return undefined
  if (props.mode === 'COUNT' && !Number.isInteger(numeric)) return undefined
  return typeof props.modelValue === 'string' ? normalized : numeric
}

function editableText(value: BusinessNumberValue) {
  return decimalText(value) ?? ''
}
</script>

<style scoped>
.business-number-input :deep(.el-input__inner) { text-align: right; font-variant-numeric: tabular-nums; }
.business-number-input.is-invalid :deep(.el-input__wrapper) { box-shadow: 0 0 0 1px var(--el-color-danger) inset; }
</style>
