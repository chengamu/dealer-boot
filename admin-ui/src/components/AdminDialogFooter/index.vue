<template>
  <div class="admin-dialog__footer" :class="footerClass">
    <span v-if="status || $slots.status" class="admin-dialog__footer-status">
      <i class="admin-dialog__footer-dot" aria-hidden="true" />
      <slot name="status">{{ status }}</slot>
    </span>
    <div class="admin-dialog__footer-actions">
      <slot />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, useSlots } from 'vue'

const props = withDefaults(defineProps<{
  status?: string
  tone?: 'neutral' | 'info' | 'success' | 'warning' | 'danger'
}>(), {
  status: '',
  tone: 'neutral'
})
const slots = useSlots()

const footerClass = computed(() => [
  `admin-dialog__footer--${props.tone}`,
  { 'admin-dialog__footer--actions-only': !props.status && !slots.status }
])
</script>
