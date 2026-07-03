<template>
  <div class="admin-empty-state" :class="`admin-empty-state--${tone}`">
    <div class="admin-empty-state__mark" aria-hidden="true">
      <el-icon v-if="icon">
        <component :is="icon" />
      </el-icon>
      <span v-else />
    </div>
    <div class="admin-empty-state__content">
      <h3 v-if="title">{{ title }}</h3>
      <p v-if="description">{{ description }}</p>
    </div>
    <el-button v-if="actionLabel" type="primary" plain @click="emit('action')">
      {{ actionLabel }}
    </el-button>
    <slot />
  </div>
</template>

<script setup lang="ts">
import type { Component } from 'vue'

withDefaults(defineProps<{
  title?: string
  description?: string
  actionLabel?: string
  icon?: string | Component
  tone?: 'neutral' | 'info'
}>(), {
  title: '',
  description: '',
  actionLabel: '',
  icon: '',
  tone: 'neutral'
})

const emit = defineEmits<{
  action: []
}>()
</script>
