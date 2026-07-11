<template>
  <div class="business-inch-input">
    <el-input-number
      v-model="whole"
      :min="minWhole"
      :max="maxWhole"
      :disabled="disabled"
      :readonly="readonly"
      :controls="false"
      :aria-label="integerAriaLabel"
      @change="emitValue"
    />
    <el-select v-model="numerator" :disabled="disabled || readonly" :aria-label="fractionAriaLabel" @change="emitValue">
      <el-option v-for="option in options" :key="option.value" :label="option.label" :value="option.value" />
    </el-select>
    <span class="business-inch-input__unit">in</span>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { decimalText, fractionOptions, type BusinessNumberValue } from '@/utils/businessNumber'

const props = withDefaults(defineProps<{
  modelValue?: BusinessNumberValue
  denominator?: number
  min?: number
  max?: number
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

const emit = defineEmits<{ 'update:modelValue': [value: number | string | null]; change: [value: number | string | null] }>()
const whole = ref(0)
const numerator = ref(0)
const options = computed(() => fractionOptions(props.denominator))
const minWhole = computed(() => Math.max(0, Math.floor(props.min)))
const maxWhole = computed(() => props.max === undefined ? undefined : Math.floor(props.max))

watch(() => [props.modelValue, props.denominator] as const, syncFromModel, { immediate: true })

function syncFromModel() {
  const text = decimalText(props.modelValue)
  const numeric = text === null ? 0 : Math.max(0, Number(text))
  whole.value = Math.floor(numeric)
  numerator.value = Math.round((numeric - whole.value) * props.denominator)
  if (numerator.value === props.denominator) {
    whole.value += 1
    numerator.value = 0
  }
}

function emitValue() {
  const numeric = whole.value + numerator.value / props.denominator
  const bounded = props.max === undefined ? numeric : Math.min(numeric, props.max)
  const value = typeof props.modelValue === 'string' ? bounded.toFixed(4) : bounded
  emit('update:modelValue', value)
  emit('change', value)
}
</script>

<style scoped>
.business-inch-input { display: grid; grid-template-columns: minmax(96px, 1fr) 104px auto; align-items: center; gap: 8px; width: 100%; }
.business-inch-input :deep(.el-input__inner) { text-align: right; font-variant-numeric: tabular-nums; }
.business-inch-input__unit { color: var(--el-text-color-secondary); }
</style>
