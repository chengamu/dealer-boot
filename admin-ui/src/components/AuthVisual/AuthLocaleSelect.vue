<template>
  <el-select :model-value="modelValue" class="auth-locale-select" size="large" @change="onChange">
    <template #prefix><span class="locale-globe">◎</span></template>
    <el-option label="English" value="en_US" />
    <el-option label="中文" value="zh_CN" />
  </el-select>
</template>

<script setup lang="ts">
import type { AppLocale } from '@/i18n'

defineProps<{
  modelValue: AppLocale
}>()

const emit = defineEmits<{
  'update:modelValue': [value: AppLocale]
  change: [value: AppLocale]
}>()

function onChange(value: AppLocale) {
  emit('update:modelValue', value)
  emit('change', value)
}
</script>

<style scoped lang="scss">
.auth-locale-select {
  width: 150px;
}

.locale-globe {
  display: inline-grid;
  width: 22px;
  height: 22px;
  place-items: center;
  color: #075cff;
  font-size: 19px;
  font-weight: 700;
  line-height: 1;
}

.auth-locale-select :deep(.el-select__wrapper) {
  height: 44px;
  padding: 0 14px;
  border: 1px solid #d6e2f0;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.82);
  box-shadow: none;
}

.auth-locale-select :deep(.el-select__selected-item) {
  color: #06184a;
  font-weight: 700;
}
</style>
