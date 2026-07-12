<template>
  <span v-if="readonly" class="business-vxe-inch-cell__text">{{ formatInch(modelValue, denominator) }}</span>
  <BusinessInchInput
    v-else
    :model-value="modelValue"
    :denominator="denominator"
    :min="min"
    :max="max"
    class="business-vxe-inch-cell"
    @update:model-value="$emit('update:modelValue', $event)"
    @change="$emit('change')"
    @validity-change="$emit('validityChange', $event)"
  />
</template>

<script setup lang="ts">
import { formatInch, type BusinessNumberValue } from '@/utils/businessNumber'

withDefaults(defineProps<{
  modelValue?: BusinessNumberValue
  denominator?: number
  min?: string | number
  max?: string | number
  readonly?: boolean
}>(), { denominator: 8, readonly: false })

defineEmits<{
  'update:modelValue': [value: string | null]
  change: []
  validityChange: [valid: boolean]
}>()
</script>

<style scoped>
.business-vxe-inch-cell { grid-template-columns: minmax(52px, 1fr) 66px 18px; gap: 4px; }
.business-vxe-inch-cell :deep(.el-input__wrapper),
.business-vxe-inch-cell :deep(.el-select__wrapper) { min-height: 32px; padding: 0 8px; box-shadow: none; }
.business-vxe-inch-cell__text { font-variant-numeric: tabular-nums; white-space: nowrap; }
</style>
