<template>
  <div class="portal-health">
    <div class="portal-health__chart" aria-hidden="true">
      <span>
        <strong>{{ total }}</strong>
        <small>{{ totalLabel }}</small>
      </span>
    </div>
    <div class="portal-health__legend">
      <div v-for="item in items" :key="item.label">
        <i :style="{ background: item.color }" />
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { HealthItem } from './types'

withDefaults(defineProps<{
  items: HealthItem[]
  total: string
  totalLabel: string
}>(), {
  total: '1,248'
})
</script>
