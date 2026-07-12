<template>
  <BusinessNumberText
    v-if="readonly"
    :value="modelValue"
    :mode="mode"
    :max-fraction-digits="maxFractionDigits"
    :unit-precision="unitPrecision"
    :currency-digits="currencyDigits"
    :unit="unit"
  />
  <BusinessNumberInput
    v-else
    :model-value="modelValue"
    :mode="mode"
    :max-fraction-digits="maxFractionDigits"
    :unit-precision="unitPrecision"
    :currency-digits="currencyDigits"
    :min="min"
    :max="max"
    :allow-zero="allowZero"
    :unit="unit"
    class="business-vxe-number-cell"
    @update:model-value="emitValue"
    @change="$emit('change')"
    @validity-change="$emit('validityChange', $event)"
  />
</template>

<script setup lang="ts">
import type { BusinessNumberMode, BusinessNumberValue } from '@/utils/businessNumber'

const props = withDefaults(defineProps<{
  modelValue?: BusinessNumberValue
  mode?: BusinessNumberMode
  maxFractionDigits?: number
  unitPrecision?: number
  currencyDigits?: number
  min?: string | number
  max?: string | number
  allowZero?: boolean
  readonly?: boolean
  integerValue?: boolean
  unit?: string
}>(), {
  mode: 'QUANTITY',
  allowZero: true,
  readonly: false,
  integerValue: false,
  unit: ''
})

const emit = defineEmits<{
  'update:modelValue': [value: string | number | null]
  change: []
  validityChange: [valid: boolean]
}>()

function emitValue(value: string | null) {
  emit('update:modelValue', props.integerValue && value !== null ? Number(value) : value)
}
</script>

<style scoped>
.business-vxe-number-cell { width: 100%; }
.business-vxe-number-cell :deep(.el-input__wrapper) { min-height: 32px; box-shadow: none; }
</style>
