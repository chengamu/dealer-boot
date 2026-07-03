<template>
  <section class="expression-editor__operators">
    <h4>{{ title }}</h4>
    <el-button v-for="operator in operators" :key="operatorLabel(operator)" plain @click="$emit('append', operatorValue(operator))">
      {{ operatorLabel(operator) }}
    </el-button>
  </section>
</template>

<script setup lang="ts">
export type OperatorOption = string | { label: string; insert: string }

defineProps<{
  title: string
  operators: OperatorOption[]
}>()

defineEmits<{
  append: [value: string]
}>()

function operatorLabel(operator: OperatorOption) {
  return typeof operator === 'string' ? operator : operator.label
}

function operatorValue(operator: OperatorOption) {
  return typeof operator === 'string' ? operator : operator.insert
}
</script>
