<template>
  <svg viewBox="0 0 120 36" aria-hidden="true">
    <polyline :points="points" />
  </svg>
</template>

<script setup lang="ts">
defineProps<{
  points: string
}>()
</script>
