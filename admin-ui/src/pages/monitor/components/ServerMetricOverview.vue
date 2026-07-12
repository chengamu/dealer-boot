<template>
  <el-card class="server-panel">
    <template #header>
      <div class="panel-header">
        <div class="panel-title"><Monitor /><span>{{ t('serverResource.overview') }}</span></div>
        <div class="panel-actions"><slot name="actions" /></div>
      </div>
    </template>
    <div class="metric-grid">
      <section v-for="metric in metrics" :key="metric.key" class="metric-item">
        <div class="metric-name">{{ metric.label }}</div>
        <el-progress
          type="circle"
          :percentage="clampPercent(metric.percentage)"
          :width="112"
          :stroke-width="8"
          :color="usageColor(metric.percentage)"
        >
          <span class="metric-value">{{ formatPercent(metric.percentage) }}</span>
        </el-progress>
        <div class="metric-detail">{{ metric.detail }}</div>
      </section>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Monitor } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import type { ServerResource } from '@/api/monitor/server'
import { clampPercent, formatBytes, formatPercent, usageColor } from '../serverResourceFormat'

const props = defineProps<{ resource: ServerResource }>()
const { t } = useI18n()

const metrics = computed(() => [
  {
    key: 'cpu',
    label: t('serverResource.cpuUsage'),
    percentage: props.resource.cpu.usagePercent,
    detail: t('serverResource.cpuCores', { count: props.resource.cpu.logicalCores })
  },
  {
    key: 'memory',
    label: t('serverResource.physicalMemory'),
    percentage: props.resource.memory.usagePercent,
    detail: `${formatBytes(props.resource.memory.usedBytes)} / ${formatBytes(props.resource.memory.totalBytes)}`
  },
  {
    key: 'jvm',
    label: t('serverResource.jvmHeap'),
    percentage: props.resource.jvm.usagePercent,
    detail: `${formatBytes(props.resource.jvm.usedBytes)} / ${formatBytes(props.resource.jvm.maxBytes)}`
  },
  {
    key: 'storage',
    label: t('serverResource.diskUsage'),
    percentage: props.resource.storage.usagePercent,
    detail: `${formatBytes(props.resource.storage.usedBytes)} / ${formatBytes(props.resource.storage.totalBytes)}`
  }
])
</script>

<style scoped>
.server-panel { border-color: #eef0f5; border-radius: 8px; box-shadow: none; }
.server-panel :deep(.el-card__header) { min-height: 40px; padding: 10px 12px; border-bottom-color: #eef0f5; }
.server-panel :deep(.el-card__body) { padding: 0; }
.panel-header { display: flex; min-width: 0; align-items: center; justify-content: space-between; gap: 12px; }
.panel-title { display: flex; align-items: center; gap: 6px; color: #1d2129; font-size: 14px; font-weight: 600; }
.panel-title svg { width: 16px; color: #1677ff; }
.panel-actions { display: flex; min-width: 0; align-items: center; gap: 8px; }
.metric-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); }
.metric-item { display: flex; min-width: 0; padding: 20px 12px 18px; align-items: center; flex-direction: column; border-right: 1px solid #eef0f5; }
.metric-item:last-child { border-right: 0; }
.metric-name { margin-bottom: 12px; color: #667085; font-size: 13px; font-weight: 600; }
.metric-value { color: #1d2129; font-size: 20px; font-weight: 600; font-variant-numeric: tabular-nums; }
.metric-detail { max-width: 100%; margin-top: 10px; overflow: hidden; color: #667085; font-size: 12px; text-overflow: ellipsis; white-space: nowrap; }
@media (max-width: 900px) {
  .metric-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .metric-item:nth-child(2) { border-right: 0; }
  .metric-item:nth-child(-n + 2) { border-bottom: 1px solid #eef0f5; }
}
@media (max-width: 520px) {
  .panel-header { gap: 8px; }
  .metric-grid { grid-template-columns: 1fr; }
  .metric-item { border-right: 0; border-bottom: 1px solid #eef0f5; }
  .metric-item:last-child { border-bottom: 0; }
}
</style>
