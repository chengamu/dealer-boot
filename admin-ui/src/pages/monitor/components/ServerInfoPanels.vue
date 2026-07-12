<template>
  <div class="info-grid">
    <el-card class="info-card">
      <template #header><div class="panel-title"><Platform /><span>{{ t('serverResource.systemInfo') }}</span></div></template>
      <el-descriptions :column="1" border>
        <el-descriptions-item :label="t('serverResource.hostName')">{{ resource.system.hostName }}</el-descriptions-item>
        <el-descriptions-item :label="t('serverResource.operatingSystem')">{{ resource.system.osName }} {{ resource.system.osVersion }}</el-descriptions-item>
        <el-descriptions-item :label="t('serverResource.architecture')">{{ resource.system.architecture }}</el-descriptions-item>
        <el-descriptions-item :label="t('serverResource.systemUptime')">{{ systemUptime }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card class="info-card">
      <template #header><div class="panel-title"><DataBoard /><span>{{ t('serverResource.jvmInfo') }}</span></div></template>
      <el-descriptions :column="1" border>
        <el-descriptions-item :label="t('serverResource.vmName')">{{ resource.jvm.vmName }}</el-descriptions-item>
        <el-descriptions-item :label="t('serverResource.javaVersion')">{{ resource.jvm.javaVersion }}</el-descriptions-item>
        <el-descriptions-item :label="t('serverResource.startTime')">{{ formatUtc(resource.jvm.startTime, 'YYYY-MM-DD HH:mm') }}</el-descriptions-item>
        <el-descriptions-item :label="t('serverResource.jvmUptime')">{{ jvmUptime }}</el-descriptions-item>
        <el-descriptions-item :label="t('serverResource.threadCount')">{{ resource.jvm.threadCount }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { DataBoard, Platform } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import type { ServerResource } from '@/api/monitor/server'
import { formatUtc } from '@/utils/datetime'
import { formatDuration } from '../serverResourceFormat'

const props = defineProps<{ resource: ServerResource }>()
const { t } = useI18n()
const durationLabels = computed(() => ({
  day: t('serverResource.dayShort'),
  hour: t('serverResource.hourShort'),
  minute: t('serverResource.minuteShort')
}))
const systemUptime = computed(() => formatDuration(props.resource.system.uptimeSeconds, durationLabels.value))
const jvmUptime = computed(() => formatDuration(props.resource.jvm.uptimeSeconds, durationLabels.value))
</script>

<style scoped>
.info-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 10px; }
.info-card { border-color: #eef0f5; border-radius: 8px; box-shadow: none; }
.info-card :deep(.el-card__header) { min-height: 40px; padding: 10px 12px; border-bottom-color: #eef0f5; }
.info-card :deep(.el-card__body) { padding: 12px; }
.info-card :deep(.el-descriptions__label) { width: 132px; color: #667085; font-weight: 500; }
.info-card :deep(.el-descriptions__content) { color: #1d2129; }
.panel-title { display: flex; align-items: center; gap: 6px; color: #1d2129; font-size: 14px; font-weight: 600; }
.panel-title svg { width: 16px; color: #1677ff; }
@media (max-width: 900px) { .info-grid { grid-template-columns: 1fr; } }
</style>
