<template>
  <el-select
    class="condition-builder__joiner"
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <el-option value="并且" :label="t('productCenter.formulaSetup.conditionAnd')" />
    <el-option value="或者" :label="t('productCenter.formulaSetup.conditionOr')" />
  </el-select>
</template>

<script setup lang="ts">
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'

defineProps<{ modelValue: string }>()
defineEmits<{ 'update:modelValue': [value: string] }>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
</script>
