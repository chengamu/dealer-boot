<template>
  <div class="product-work-panel">
    <div class="product-work-panel__head">
      <h2>{{ t('productCenter.workbench.progress') }}</h2>
      <el-space wrap>
        <el-button size="small" icon="Operation" @click="$emit('batchAction')">{{ t('productCenter.workbench.batchAction') }}</el-button>
        <el-button size="small" icon="Download" @click="$emit('export')">{{ t('common.export') }}</el-button>
      </el-space>
    </div>
    <el-table v-loading="loading" :data="rows" class="product-progress-table" @row-click="$emit('openRow', $event)">
      <el-table-column :label="t('productCenter.model.code')" prop="modelCode" min-width="120" />
      <el-table-column :label="t('productCenter.model.nameCn')" prop="modelName" min-width="160" />
      <el-table-column :label="t('productCenter.category.title')" prop="categoryName" min-width="130" />
      <el-table-column :label="t('productCenter.workbench.templateStatus')" prop="templateStatus" width="120">
        <template #default="{ row }"><el-tag effect="light" :type="statusTone(row.templateStatus)">{{ statusText(row.templateStatus) }}</el-tag></template>
      </el-table-column>
      <el-table-column :label="t('productCenter.workbench.priceStatus')" prop="priceStatus" width="120">
        <template #default="{ row }"><el-tag effect="light" :type="statusTone(row.priceStatus)">{{ statusText(row.priceStatus) }}</el-tag></template>
      </el-table-column>
      <el-table-column :label="t('productCenter.workbench.assetStatus')" prop="assetStatus" width="120">
        <template #default="{ row }"><el-tag effect="light" :type="statusTone(row.assetStatus)">{{ statusText(row.assetStatus) }}</el-tag></template>
      </el-table-column>
      <el-table-column :label="t('productCenter.workbench.updatedTime')" width="170">
        <template #default="{ row }">{{ formatUtc(row.updatedTime) }}</template>
      </el-table-column>
      <el-table-column :label="t('common.operate')" width="120" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" icon="Right" @click.stop="$emit('openRow', row)">
            {{ t('productCenter.workbench.nextStep') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { formatUtc } from '@/utils/datetime'
import type { WorkbenchProgress } from '@/api/product-capability/types'

defineProps<{
  rows: WorkbenchProgress[]
  loading: boolean
  statusTone: (status?: string) => 'success' | 'warning' | 'danger' | 'info'
}>()

defineEmits<{
  batchAction: []
  export: []
  openRow: [row: WorkbenchProgress]
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)

function statusText(status?: string) {
  const value = String(status || '').toUpperCase()
  if (!value) return '-'
  const keyMap: Record<string, string> = {
    READY: 'productCenter.workbench.status.ready',
    MISSING: 'productCenter.workbench.status.missing',
    PARTIAL: 'productCenter.workbench.status.partial',
    PENDING: 'productCenter.workbench.status.pending',
    BLOCKER: 'productCenter.workbench.status.blocker',
    WARNING: 'productCenter.workbench.status.warning'
  }
  return keyMap[value] ? t(keyMap[value]) : status || '-'
}
</script>

<style scoped lang="scss">
.product-work-panel {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.04);
}

.product-work-panel__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 14px 0;
  margin-bottom: 12px;

  h2 {
    margin: 0;
    color: #111827;
    font-size: 16px;
    font-weight: 650;
  }
}

.product-progress-table {
  cursor: pointer;
}
</style>
