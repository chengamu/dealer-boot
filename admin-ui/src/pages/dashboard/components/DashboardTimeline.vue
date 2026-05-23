<template>
  <div class="portal-timeline">
    <div v-for="event in timeline" :key="event.title" class="portal-timeline__item" :style="{ '--dot-color': event.color }">
      <span />
      <div>
        <strong>{{ event.title }}</strong>
        <small>{{ event.by }}</small>
      </div>
      <time>{{ event.time }}</time>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { TimelineItem } from './types'

defineProps<{
  timeline: TimelineItem[]
}>()
</script>
