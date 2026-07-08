<template>
  <div class="formula-editor">
    <div class="formula-editor__label">{{ label }}</div>
    <el-input
      :model-value="modelValue"
      :rows="rows"
      type="textarea"
      resize="none"
      :disabled="disabled"
      :placeholder="placeholder || t('productCenter.pricing.formulaPlaceholder')"
      @update:model-value="$emit('update:modelValue', $event)"
    />
    <div class="formula-editor__chips">
      <el-button v-for="item in variables" :key="item.value" size="small" :disabled="disabled" @click="append(item.value)">
        {{ item.label }}
      </el-button>
      <el-button v-for="item in functions" :key="item" size="small" plain :disabled="disabled" @click="append(item + '(')">
        {{ item }}
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'

const props = withDefaults(defineProps<{
  modelValue?: string
  label: string
  placeholder?: string
  rows?: number
  disabled?: boolean
}>(), {
  modelValue: '',
  rows: 2,
  disabled: false
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)

const variables = [
  { label: 'width', value: 'width' },
  { label: 'drop', value: 'drop' },
  { label: 'widthCm', value: 'width * 2.54' },
  { label: 'dropCm', value: 'drop * 2.54' }
]
const functions = ['MAX', 'MIN', 'ROUND', 'CEIL', 'FLOOR']

function append(value: string) {
  const next = props.modelValue ? `${props.modelValue} ${value}` : value
  emit('update:modelValue', next)
}
</script>

<style scoped>
.formula-editor {
  display: grid;
  gap: 8px;
}

.formula-editor__label {
  color: #344054;
  font-size: 13px;
  font-weight: 600;
}

.formula-editor__chips {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
</style>
