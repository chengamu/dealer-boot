<template>
  <AdminDrawer v-model="open" :title="title" size="720px" variant="detail" append-to-body>
    <el-table v-if="rows.length" v-loading="loading" :data="rows" border class="formula-review-record">
      <el-table-column type="index" :label="t('common.index')" width="64" align="center" />
      <el-table-column :label="t('productCenter.changeLog.action')" prop="actionName" width="116" show-overflow-tooltip />
      <el-table-column :label="t('productCenter.changeLog.operator')" prop="operatorName" width="128" show-overflow-tooltip />
      <el-table-column :label="t('productCenter.changeLog.operateTime')" width="152" align="center">
        <template #default="{ row }">{{ formatFormulaReviewMinute(row.operateTime) }}</template>
      </el-table-column>
      <el-table-column :label="t('productCenter.changeLog.diff')" min-width="280">
        <template #default="{ row }">
          <pre class="formula-review-record__diff">{{ formatChangeDiff(row.diffJson) }}</pre>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-else v-loading="loading" :description="t('productCenter.changeLog.empty')" />
  </AdminDrawer>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import type { ProductChangeLogVO } from '@/api/product-capability/types'
import { formatFormulaReviewMinute } from '../utils/formulaReviewDisplay'

const props = defineProps<{
  modelValue: boolean
  rows: ProductChangeLogVO[]
  loading: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const title = computed(() => t('productCenter.formulaReview.reviewRecord'))
const open = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

function displayValue(value: unknown) {
  if (Array.isArray(value)) return value.length ? value.join(', ') : '-'
  if (value && typeof value === 'object') return JSON.stringify(value)
  return String(value ?? '-')
}

function formatChangeDiff(value: unknown) {
  if (!value) return '-'
  try {
    const parsed = typeof value === 'string' ? JSON.parse(value) : value
    if (!parsed || typeof parsed !== 'object' || Array.isArray(parsed)) return displayValue(parsed)
    const lines = Object.entries(parsed as Record<string, { before?: unknown; after?: unknown }>).map(([field, item]) => {
      const beforeText = displayValue(item?.before)
      const afterText = displayValue(item?.after)
      return beforeText === afterText ? '' : `${field}: ${beforeText} -> ${afterText}`
    }).filter(Boolean)
    return lines.length ? lines.join('\n') : '-'
  } catch {
    return String(value)
  }
}
</script>

<style scoped>
.formula-review-record__diff {
  max-height: 220px;
  margin: 0;
  overflow: auto;
  white-space: pre-wrap;
}
</style>
