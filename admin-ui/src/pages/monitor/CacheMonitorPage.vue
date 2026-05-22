<template>
  <div class="app-container cache-monitor-page">
    <el-row :gutter="16">
      <el-col :span="24" class="card-box">
        <el-card v-loading="loading">
          <template #header>
            <Monitor class="card-icon" />
            <span>{{ t('cache.basicInfo') }}</span>
          </template>
          <el-descriptions :column="4" border>
            <el-descriptions-item v-for="item in infoItems" :key="item.label" :label="item.label">
              {{ item.value }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="24" :md="12" class="card-box">
        <el-card v-loading="loading">
          <template #header>
            <PieIcon class="card-icon" />
            <span>{{ t('cache.commandStats') }}</span>
          </template>
          <div ref="commandStatsRef" class="chart-panel" />
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="24" :md="12" class="card-box">
        <el-card v-loading="loading">
          <template #header>
            <Odometer class="card-icon" />
            <span>{{ t('cache.memoryInfo') }}</span>
          </template>
          <div ref="usedMemoryRef" class="chart-panel" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts" name="CacheMonitorPage">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { Monitor, Odometer, PieChart as PieIcon } from '@element-plus/icons-vue'
import { GaugeChart, PieChart } from 'echarts/charts'
import { TooltipComponent } from 'echarts/components'
import { init, use, type EChartsType } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { getCache, type CacheMonitor } from '@/api/monitor/cache'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { runUiAction } from '@/utils/action'

use([TooltipComponent, PieChart, GaugeChart, CanvasRenderer])

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)

const loading = ref(false)
const cache = ref<CacheMonitor>({
  info: {},
  dbSize: 0,
  commandStats: []
})
const commandStatsRef = ref<HTMLElement>()
const usedMemoryRef = ref<HTMLElement>()
let commandStatsChart: EChartsType | undefined
let usedMemoryChart: EChartsType | undefined

const displayValue = (value: unknown) => (value === undefined || value === null || value === '' ? '-' : String(value))

const infoItems = computed(() => {
  const info = cache.value.info || {}
  return [
    { label: t('cache.redisVersion'), value: displayValue(info.redis_version) },
    { label: t('cache.runMode'), value: info.redis_mode === 'standalone' ? t('cache.standalone') : displayValue(info.redis_mode || t('cache.cluster')) },
    { label: t('cache.port'), value: displayValue(info.tcp_port) },
    { label: t('cache.clientCount'), value: displayValue(info.connected_clients) },
    { label: t('cache.uptimeDays'), value: displayValue(info.uptime_in_days) },
    { label: t('cache.usedMemory'), value: displayValue(info.used_memory_human) },
    { label: t('cache.usedCpu'), value: Number(info.used_cpu_user_children || 0).toFixed(2) },
    { label: t('cache.maxMemory'), value: displayValue(info.maxmemory_human) },
    { label: t('cache.aofEnabled'), value: String(info.aof_enabled) === '0' ? t('common.no') : t('common.yes') },
    { label: t('cache.rdbStatus'), value: displayValue(info.rdb_last_bgsave_status) },
    { label: t('cache.keyCount'), value: displayValue(cache.value.dbSize) },
    {
      label: t('cache.networkInOut'),
      value: `${displayValue(info.instantaneous_input_kbps)}kps/${displayValue(info.instantaneous_output_kbps)}kps`
    }
  ]
})

function renderCharts() {
  if (!commandStatsRef.value || !usedMemoryRef.value) return
  commandStatsChart?.dispose()
  usedMemoryChart?.dispose()

  commandStatsChart = init(commandStatsRef.value)
  commandStatsChart.setOption({
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b} : {c} ({d}%)'
    },
    series: [
      {
        name: t('cache.command'),
        type: 'pie',
        roseType: 'radius',
        radius: [15, 95],
        center: ['50%', '42%'],
        data: cache.value.commandStats || [],
        animationEasing: 'cubicInOut',
        animationDuration: 1000
      }
    ]
  })

  const usedMemory = cache.value.info?.used_memory_human || '-'
  usedMemoryChart = init(usedMemoryRef.value)
  usedMemoryChart.setOption({
    tooltip: {
      formatter: `{b} <br/>{a} : ${usedMemory}`
    },
    series: [
      {
        name: t('cache.peak'),
        type: 'gauge',
        min: 0,
        max: 1000,
        detail: {
          formatter: usedMemory
        },
        data: [
          {
            value: parseFloat(usedMemory) || 0,
            name: t('cache.memoryUsage')
          }
        ]
      }
    ]
  })
}

function resizeCharts() {
  commandStatsChart?.resize()
  usedMemoryChart?.resize()
}

async function getList() {
  loading.value = true
  try {
    await runUiAction(async () => {
      cache.value = await getCache()
      await nextTick()
      renderCharts()
    })
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  getList()
  window.addEventListener('resize', resizeCharts)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  commandStatsChart?.dispose()
  usedMemoryChart?.dispose()
})
</script>

<style scoped>
.cache-monitor-page .card-box {
  margin-bottom: 16px;
}

.card-icon {
  width: 1em;
  height: 1em;
  margin-right: 6px;
  vertical-align: middle;
}

.chart-panel {
  height: 420px;
}
</style>
