<template>
  <div class="product-side-panel">
    <div class="product-side-panel__head">
      <h2>{{ t('productCenter.workbench.priority') }}</h2>
      <el-button link type="primary" @click="$emit('viewMore')">{{ t('productCenter.workbench.viewMore') }}</el-button>
    </div>
    <div v-loading="loading" class="priority-queue">
      <button v-for="row in rows" :key="`${row.targetCode}-${row.severity}`" class="priority-queue__item" type="button" @click="$emit('openTask', row)">
        <el-tag size="small" :type="statusTone(row.severity)">{{ row.severity || '-' }}</el-tag>
        <span>
          <strong>{{ row.targetCode || '-' }}</strong>
          <small>{{ row.ownerName || t('productCenter.workbench.unassigned') }}</small>
        </span>
      </button>
      <el-empty v-if="!loading && rows.length === 0" :description="t('productCenter.workbench.emptyPriority')" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import type { WorkbenchPriority } from '@/api/product-capability/types'

defineProps<{
  rows: WorkbenchPriority[]
  loading: boolean
  statusTone: (status?: string) => 'success' | 'warning' | 'danger' | 'info'
}>()

defineEmits<{
  openTask: [row: WorkbenchPriority]
  viewMore: []
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
</script>

<style scoped lang="scss">
.product-side-panel {
  margin-bottom: 12px;
  padding: 14px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.04);
}

.product-side-panel__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;

  h2 {
    margin: 0;
    font-size: 16px;
    font-weight: 650;
  }
}

.priority-queue {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.priority-queue__item {
  display: flex;
  width: 100%;
  align-items: center;
  gap: 10px;
  padding: 10px;
  border: 1px solid #edf2f7;
  border-radius: 8px;
  background: #f8fafc;
  cursor: pointer;
  text-align: left;

  strong,
  small {
    display: block;
  }

  strong {
    color: #111827;
    font-size: 13px;
  }

  small {
    margin-top: 3px;
    color: #64748b;
    font-size: 12px;
  }
}
</style>
