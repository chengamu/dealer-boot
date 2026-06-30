<template>
  <div class="usage-editor__materials">
    <div class="usage-editor__materials-head">
      <div>
        <span class="usage-editor__materials-kicker">{{ t('productCenter.formulaSetup.selectedMaterialCount') }}</span>
        <strong>{{ t('common.selectedCount').replace('{count}', String(usageRows.length)) }}</strong>
      </div>
      <el-button
        plain
        size="small"
        :icon="collapsed ? ArrowDown : ArrowUp"
        @click="$emit('update:collapsed', !collapsed)"
      >
        {{ collapsed ? t('legacy.expand') : t('legacy.collapse') }}
      </el-button>
    </div>

    <div v-if="collapsed" class="usage-editor__current">
      <span>{{ usageRow.materialCode || '-' }}</span>
      <strong>{{ usageRow.materialNameCn || '-' }}</strong>
      <em>{{ usageRow.specModelText || '-' }}</em>
      <span>{{ unitDisplay(usageRow.unitCode) }}</span>
    </div>

    <div v-else class="usage-editor__material-list">
      <button
        v-for="row in usageRows"
        :key="row.materialCode || row.materialId || row.lineNo"
        type="button"
        class="usage-editor__material-row"
        :class="{ 'is-active': row.materialCode === usageRow.materialCode }"
        @click="$emit('select-usage-row', row)"
      >
        <span class="usage-editor__material-type">{{ row.materialTypeNameCn || row.materialTypeCode || '-' }}</span>
        <span class="usage-editor__material-code">{{ row.materialCode || '-' }}</span>
        <strong>{{ row.materialNameCn || '-' }}</strong>
        <em>{{ row.specModelText || '-' }}</em>
        <span>{{ unitDisplay(row.unitCode) }}</span>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ArrowDown, ArrowUp } from '@element-plus/icons-vue'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import type { ProductFormulaMaterialVO, ProductOption } from '@/api/product-capability/types'

const props = defineProps<{
  usageRow: ProductFormulaMaterialVO
  usageRows: ProductFormulaMaterialVO[]
  unitOptions: ProductOption[]
  collapsed: boolean
}>()

defineEmits<{
  'update:collapsed': [value: boolean]
  'select-usage-row': [row: ProductFormulaMaterialVO]
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)

function unitDisplay(unitCode?: string) {
  if (!unitCode) return '-'
  return props.unitOptions.find((unit) => unit.value === unitCode)?.label || unitCode
}
</script>

<style scoped>
.usage-editor__materials {
  padding: 10px 12px;
  background: #f7faff;
  border: 1px solid #e5ecf6;
  border-radius: 8px;
}

.usage-editor__materials-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}

.usage-editor__materials-head strong {
  color: #111827;
  font-size: 14px;
}

.usage-editor__materials-kicker {
  display: block;
  margin-bottom: 2px;
  color: #6b7280;
  font-size: 12px;
}

.usage-editor__material-list {
  display: grid;
  gap: 6px;
  max-height: 180px;
  overflow: auto;
}

.usage-editor__material-row {
  display: grid;
  grid-template-columns: 110px 110px minmax(220px, 1.2fr) minmax(220px, 1fr) 80px;
  gap: 8px;
  align-items: center;
  width: 100%;
  padding: 8px 10px;
  color: #374151;
  text-align: left;
  cursor: pointer;
  background: #fff;
  border: 1px solid #e5ecf6;
  border-radius: 6px;
}

.usage-editor__material-row.is-active {
  background: #ecf5ff;
  border-color: #409eff;
}

.usage-editor__material-row strong,
.usage-editor__material-row em,
.usage-editor__current strong,
.usage-editor__current em {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.usage-editor__material-row em,
.usage-editor__current em {
  color: #6b7280;
  font-style: normal;
}

.usage-editor__material-type {
  color: #2563eb;
  font-weight: 700;
}

.usage-editor__material-code {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", monospace;
}

.usage-editor__current {
  display: grid;
  grid-template-columns: 120px minmax(240px, 1fr) minmax(240px, 1fr) 80px;
  gap: 8px;
  align-items: center;
  padding: 8px 10px;
  background: #fff;
  border: 1px solid #e5ecf6;
  border-radius: 6px;
}
</style>
