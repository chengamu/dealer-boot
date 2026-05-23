<template>
  <div class="portal-users">
    <div v-for="user in users" :key="user.name" class="portal-users__item">
      <span class="portal-avatar" :class="`is-${user.tone}`">{{ user.initials }}</span>
      <div>
        <strong>{{ user.name }}</strong>
        <small>{{ user.location }}</small>
      </div>
      <em>{{ user.role }}</em>
      <time>{{ user.time }}</time>
      <i :class="{ 'is-away': user.away }" />
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ActiveUserItem } from './types'

defineProps<{
  users: ActiveUserItem[]
}>()
</script>
