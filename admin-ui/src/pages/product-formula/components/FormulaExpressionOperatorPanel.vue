<template>
  <section class="expression-editor__operators">
    <h4>{{ title }}</h4>
    <el-button v-for="operator in operators" :key="operator" plain @click="$emit('append', operator)">
      {{ operator }}
    </el-button>
  </section>
</template>

<script setup lang="ts">
defineProps<{
  title: string
  operators: string[]
}>()

defineEmits<{
  append: [value: string]
}>()
</script>
