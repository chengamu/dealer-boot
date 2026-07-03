<template>
  <section class="admin-section" :class="[`admin-section--${variant}`, { 'admin-section--compact': compact }]">
    <header v-if="title || subtitle || $slots.actions" class="admin-section__header">
      <div>
        <h3 v-if="title" class="admin-section__title">{{ title }}</h3>
        <p v-if="subtitle" class="admin-section__subtitle">{{ subtitle }}</p>
      </div>
      <div v-if="$slots.actions" class="admin-section__actions">
        <slot name="actions" />
      </div>
    </header>
    <div class="admin-section__body">
      <slot />
    </div>
  </section>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  title?: string
  subtitle?: string
  variant?: 'plain' | 'panel'
  compact?: boolean
}>(), {
  title: '',
  subtitle: '',
  variant: 'panel',
  compact: false
})
</script>
