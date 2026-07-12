<template>
  <el-card class="disk-card">
    <template #header><div class="panel-title"><FolderOpened /><span>{{ t('serverResource.diskDetails') }}</span></div></template>
    <el-table :data="disks" border style="width: 100%">
      <el-table-column prop="mount" :label="t('serverResource.mount')" min-width="130" />
      <el-table-column prop="name" :label="t('serverResource.diskName')" min-width="150" show-overflow-tooltip />
      <el-table-column prop="type" :label="t('serverResource.fileSystem')" min-width="110" />
      <el-table-column :label="t('serverResource.total')" min-width="110" align="right">
        <template #default="{ row }">{{ formatBytes(row.totalBytes) }}</template>
      </el-table-column>
      <el-table-column :label="t('serverResource.used')" min-width="110" align="right">
        <template #default="{ row }">{{ formatBytes(row.usedBytes) }}</template>
      </el-table-column>
      <el-table-column :label="t('serverResource.available')" min-width="110" align="right">
        <template #default="{ row }">{{ formatBytes(row.availableBytes) }}</template>
      </el-table-column>
      <el-table-column :label="t('serverResource.usage')" min-width="180">
        <template #default="{ row }">
          <div class="usage-cell">
            <el-progress :percentage="clampPercent(row.usagePercent)" :stroke-width="7" :show-text="false" :color="usageColor(row.usagePercent)" />
            <span>{{ formatPercent(row.usagePercent) }}</span>
          </div>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup lang="ts">
import { FolderOpened } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import type { ServerDisk } from '@/api/monitor/server'
import { clampPercent, formatBytes, formatPercent, usageColor } from '../serverResourceFormat'

defineProps<{ disks: ServerDisk[] }>()
const { t } = useI18n()
</script>

<style scoped>
.disk-card { border-color: #eef0f5; border-radius: 8px; box-shadow: none; }
.disk-card :deep(.el-card__header) { min-height: 40px; padding: 10px 12px; border-bottom-color: #eef0f5; }
.disk-card :deep(.el-card__body) { padding: 12px; }
.disk-card :deep(.el-table__header th) { height: 40px; background: #f7f9fc; color: #667085; font-weight: 600; }
.disk-card :deep(.el-table__row td) { height: 44px; color: #344054; }
.panel-title { display: flex; align-items: center; gap: 6px; color: #1d2129; font-size: 14px; font-weight: 600; }
.panel-title svg { width: 16px; color: #1677ff; }
.usage-cell { display: grid; grid-template-columns: minmax(80px, 1fr) 52px; gap: 10px; align-items: center; }
.usage-cell span { text-align: right; font-variant-numeric: tabular-nums; }
</style>
