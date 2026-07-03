<template>
  <div class="admin-skeleton-table" :style="{ '--admin-skeleton-columns': columns }">
    <div class="admin-skeleton-table__toolbar">
      <span />
      <span />
      <span />
    </div>
    <div class="admin-skeleton-table__header">
      <span v-for="column in columns" :key="`header-${column}`" />
    </div>
    <div v-for="row in rows" :key="`row-${row}`" class="admin-skeleton-table__row">
      <span v-for="column in columns" :key="`${row}-${column}`" />
    </div>
  </div>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  rows?: number
  columns?: number
}>(), {
  rows: 5,
  columns: 4
})
</script>
