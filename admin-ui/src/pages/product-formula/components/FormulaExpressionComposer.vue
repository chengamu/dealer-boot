<template>
  <section class="expression-editor__composer">
    <div class="expression-editor__head">
      <h4>
        {{ title }}
        <span>{{ placeholder }}</span>
      </h4>
      <el-button text type="primary" :disabled="!modelValue" @click="$emit('update:modelValue', '')">
        {{ clearText }}
      </el-button>
    </div>
    <el-input
      :model-value="modelValue"
      type="textarea"
      :rows="2"
      resize="none"
      :placeholder="placeholder"
      @update:model-value="$emit('update:modelValue', String($event))"
    />
  </section>
</template>

<script setup lang="ts">
defineProps<{
  modelValue: string
  title: string
  placeholder: string
  clearText: string
}>()

defineEmits<{
  'update:modelValue': [value: string]
}>()
</script>
