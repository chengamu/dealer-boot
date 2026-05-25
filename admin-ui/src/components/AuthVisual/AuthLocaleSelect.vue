<template>
  <el-select :model-value="modelValue" class="auth-locale-select" size="large" @change="onChange">
    <template #prefix><span class="locale-globe">◎</span></template>
    <el-option
      v-for="item in languageOptions"
      :key="item.value"
      :label="item.label"
      :value="item.value"
    />
  </el-select>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { localeOptions, type AppLocale } from '@/i18n'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'

defineProps<{
  modelValue: AppLocale
}>()

const emit = defineEmits<{
  'update:modelValue': [value: AppLocale]
  change: [value: AppLocale]
}>()

const localeStore = useLocaleStore()
const languageOptions = computed(() => localeOptions.map(item => ({
  ...item,
  label: getMessage(item.labelKey, localeStore.language)
})))

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
