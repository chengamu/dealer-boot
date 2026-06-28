<template>
  <div class="admin-dialog__footer" :class="{ 'admin-dialog__footer--actions-only': !status && !$slots.status }">
    <span v-if="status || $slots.status" class="admin-dialog__footer-status">
      <slot name="status">{{ status }}</slot>
    </span>
    <div class="admin-dialog__footer-actions">
      <slot />
    </div>
  </div>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  status?: string
}>(), {
  status: ''
})
</script>
