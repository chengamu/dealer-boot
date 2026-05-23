<template>
  <div class="portal-alerts">
    <div v-for="alert in alerts" :key="alert.title" class="portal-alerts__item" :class="`is-${alert.tone}`">
      <span><component :is="alert.icon" /></span>
      <div>
        <strong>{{ alert.title }}</strong>
        <small>{{ alert.description }}</small>
      </div>
      <time>{{ alert.time }}</time>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { AlertItem } from './types'

defineProps<{
  alerts: AlertItem[]
}>()
</script>
