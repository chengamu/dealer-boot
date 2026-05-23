<template>
  <article class="portal-panel" :class="panelClass">
    <header class="portal-panel__header">
      <h2>
        {{ title }}
        <small v-if="subtitle">{{ subtitle }}</small>
      </h2>
      <button v-if="actionLabel" type="button" @click="emit('action')">
        {{ actionLabel }}
        <el-icon v-if="showArrow"><ArrowRight /></el-icon>
      </button>
    </header>
    <slot />
  </article>
</template>

<script setup lang="ts">
import { ArrowRight } from '@element-plus/icons-vue'

withDefaults(defineProps<{
  title: string
  subtitle?: string
  actionLabel?: string
  panelClass?: string
  showArrow?: boolean
}>(), {
  subtitle: '',
  actionLabel: '',
  panelClass: '',
  showArrow: true
})

const emit = defineEmits<{
  action: []
}>()
</script>
