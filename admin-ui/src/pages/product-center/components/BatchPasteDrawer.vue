<template>
  <el-drawer :model-value="modelValue" :title="title" size="680px" append-to-body destroy-on-close @update:model-value="$emit('update:modelValue', $event)">
    <el-alert :title="tip" type="info" show-icon :closable="false" />
    <el-input :model-value="text" type="textarea" :rows="8" class="batch-paste-drawer__input" @update:model-value="$emit('update:text', $event)" />
    <el-table :data="rows" max-height="320">
      <el-table-column v-for="column in columns" :key="column.prop" :label="column.label" :prop="column.prop" :min-width="column.minWidth" :width="column.width" />
    </el-table>
    <template #footer>
      <div class="batch-paste-drawer__footer">
        <el-button @click="$emit('update:modelValue', false)">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="$emit('parse')">{{ t('productCenter.template.parse') }}</el-button>
      </div>
    </template>
  </el-drawer>
</template>

<script setup lang="ts">
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import type { ProductRecord } from '@/api/product-capability/types'

defineProps<{
  modelValue: boolean
  title: string
  tip: string
  text: string
  rows: ProductRecord[]
  columns: Array<{ prop: string; label: string; minWidth?: number | string; width?: number | string }>
}>()

defineEmits<{
  'update:modelValue': [value: boolean]
  'update:text': [value: string]
  parse: []
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
</script>

<style scoped lang="scss">
.batch-paste-drawer__input {
  margin: 12px 0;
}

.batch-paste-drawer__footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
