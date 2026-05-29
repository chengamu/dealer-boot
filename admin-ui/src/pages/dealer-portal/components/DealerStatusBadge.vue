<template>
  <component :is="tag" class="dealer-badge" :class="`is-${tone}`">
    <slot />
  </component>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  tag?: 'span' | 'em'
  tone?: string
}>(), {
  tag: 'span',
  tone: 'blue'
})
</script>
