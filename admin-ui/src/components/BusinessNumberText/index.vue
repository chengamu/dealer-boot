<template>
  <span class="business-number-text">{{ displayValue }}<span v-if="unit && displayValue !== emptyText" class="business-number-text__unit">{{ unit }}</span></span>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { formatBusinessNumber, type BusinessNumberMode, type BusinessNumberValue } from '@/utils/businessNumber'

const props = withDefaults(defineProps<{
  value?: BusinessNumberValue
  mode?: BusinessNumberMode
  minFractionDigits?: number
  maxFractionDigits?: number
  unitPrecision?: number
  currencyDigits?: number
  unit?: string
  emptyText?: string
}>(), {
  mode: 'QUANTITY',
  unit: '',
  emptyText: '-'
})

const displayValue = computed(() => formatBusinessNumber(props.value, props.mode, {
  minFractionDigits: props.minFractionDigits,
  maxFractionDigits: props.maxFractionDigits,
  unitPrecision: props.unitPrecision,
  currencyDigits: props.currencyDigits,
  emptyText: props.emptyText
}))
</script>

<style scoped>
.business-number-text { font-variant-numeric: tabular-nums; white-space: nowrap; }
.business-number-text__unit { margin-left: 4px; color: var(--el-text-color-secondary); }
</style>
