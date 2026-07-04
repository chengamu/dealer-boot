<template>
  <aside class="expression-editor__result" :class="{ 'is-error': !valid }">
    <div class="expression-editor__result-head">
      <h4>{{ title }}</h4>
      <el-tag effect="plain" :type="valid ? 'success' : 'danger'">{{ statusText }}</el-tag>
    </div>
    <dl class="expression-editor__result-expression">
      <dt>{{ normalizedTitle }}</dt>
      <dd>{{ expression || '-' }}</dd>
    </dl>
  </aside>
</template>

<script setup lang="ts">
defineProps<{
  title: string
  statusText: string
  normalizedTitle: string
  expression: string
  valid: boolean
}>()
</script>
