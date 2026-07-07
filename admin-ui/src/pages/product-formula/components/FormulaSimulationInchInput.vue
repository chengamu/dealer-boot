<template>
  <div class="simulation-inch-input">
    <el-input
      :model-value="wholeInput"
      type="number"
      min="0"
      inputmode="numeric"
      @update:model-value="updateWhole"
    />
    <el-select :model-value="fractionValue" @change="updateFraction(String($event || ''))">
      <el-option v-for="item in fractionOptions" :key="item.value" :label="item.label" :value="item.value" />
    </el-select>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  modelValue?: number
}>()

const emit = defineEmits<{
  'update:modelValue': [value?: number]
}>()

const fractionOptions = [
  { value: '', label: '-' },
  { value: '0.125', label: '1/8' },
  { value: '0.25', label: '1/4' },
  { value: '0.375', label: '3/8' },
  { value: '0.5', label: '1/2' },
  { value: '0.625', label: '5/8' },
  { value: '0.75', label: '3/4' },
  { value: '0.875', label: '7/8' }
]

const wholeInch = computed(() => props.modelValue == null ? undefined : Math.floor(Number(props.modelValue)))
const fractionValue = computed(() => {
  if (props.modelValue == null || wholeInch.value == null) return ''
  const fraction = Number(props.modelValue) - wholeInch.value
  const match = fractionOptions.slice(1).find((item) => Math.abs(Number(item.value) - fraction) < 0.001)
  return match?.value || ''
})
const wholeInput = computed(() => wholeInch.value == null ? '' : String(wholeInch.value))

function updateWhole(value: string | number) {
  const whole = value === '' ? undefined : Math.floor(Math.max(0, Number(value)))
  emitValue(Number.isFinite(whole) ? whole : undefined, fractionValue.value)
}

function updateFraction(value: string) {
  emitValue(wholeInch.value, value)
}

function emitValue(whole: number | undefined, fraction: string) {
  if (whole == null && !fraction) {
    emit('update:modelValue', undefined)
    return
  }
  emit('update:modelValue', Number((Math.max(0, whole ?? 0) + Number(fraction || 0)).toFixed(3)))
}
</script>

<style scoped>
.simulation-inch-input {
  display: grid;
  grid-template-columns: 1fr 90px;
  gap: 8px;
}

.simulation-inch-input :deep(.el-input-number),
.simulation-inch-input :deep(.el-select) {
  width: 100%;
}

.simulation-inch-input :deep(.el-input__inner) {
  text-align: center;
}
</style>
