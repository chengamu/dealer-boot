<template>
  <article
    class="portal-stat-card"
    :class="`is-${item.tone}`"
    role="button"
    tabindex="0"
    @click="emit('select', item)"
    @keydown.enter.prevent="emit('select', item)"
    @keydown.space.prevent="emit('select', item)"
  >
    <div class="portal-stat-card__icon">
      <component :is="item.icon" />
    </div>
    <div class="portal-stat-card__content">
      <span>{{ item.title }}</span>
      <strong>{{ item.value }}</strong>
      <div v-if="item.trend || item.caption" class="portal-stat-card__meta">
        <em v-if="item.trend">{{ item.trend }}</em>
        <small v-if="item.caption">{{ item.caption }}</small>
      </div>
    </div>
    <svg v-if="item.sparkline" class="portal-stat-card__sparkline" viewBox="0 0 120 38" aria-hidden="true">
      <polyline :points="item.sparkline" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round" />
    </svg>
    <div v-else-if="item.progress" class="portal-stat-card__progress">
      <span>{{ item.progressLabel }}</span>
      <i><b :style="{ width: item.progress }" /></i>
    </div>
  </article>
</template>

<script setup lang="ts">
import type { StatCardItem } from './types'

defineProps<{
  item: StatCardItem
}>()

const emit = defineEmits<{
  select: [item: StatCardItem]
}>()
</script>
