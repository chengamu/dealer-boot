<template>
  <div class="product-workbench-trend">
    <div class="product-workbench-trend__head">
      <div>
        <span>{{ t('productCenter.workbench.publishTrend') }}</span>
        <strong>{{ pendingSyncCount }}</strong>
      </div>
      <el-button text type="primary" icon="Right" @click="$emit('openSyncLog')">{{ t('common.detail') }}</el-button>
    </div>

    <div ref="chartRef" class="product-workbench-trend__chart" />

    <div class="product-workbench-trend__metrics">
      <button type="button" @click="$emit('openGaps', 'WARNING')">
        <span>{{ t('productCenter.workbench.dataGaps') }}</span>
        <strong>{{ warningCount }}</strong>
        <small>{{ t('productCenter.workbench.dataGapsHint') }}</small>
      </button>
      <button type="button" @click="$emit('openGaps', 'BLOCKER')">
        <span>{{ t('productCenter.workbench.priceGaps') }}</span>
        <strong>{{ blockerCount }}</strong>
        <small>{{ t('productCenter.workbench.priceGapsHint') }}</small>
      </button>
      <button type="button" @click="$emit('openSyncLog')">
        <span>{{ t('productCenter.workbench.publishTrend') }}</span>
        <strong>{{ pendingSyncCount }}</strong>
        <small>{{ t('productCenter.workbench.publishTrendHint') }}</small>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent } from 'echarts/components'
import { init, use, type EChartsType } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import type { WorkbenchSummary } from '@/api/product-capability/types'

use([TooltipComponent, GridComponent, BarChart, CanvasRenderer])

const props = defineProps<{
  summary: WorkbenchSummary
}>()

defineEmits<{
  openGaps: [severity: 'BLOCKER' | 'WARNING']
  openSyncLog: []
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)

const chartRef = ref<HTMLElement>()
let chart: EChartsType | undefined

const warningCount = computed(() => Number(props.summary.warningCount || 0))
const blockerCount = computed(() => Number(props.summary.blockerCount || 0))
const pendingSyncCount = computed(() => Number(props.summary.pendingSyncCount || 0))

function renderChart() {
  if (!chartRef.value) return
  chart = chart || init(chartRef.value)
  chart.setOption({
    color: ['#f59e0b', '#ef4444', '#2563eb'],
    grid: {
      left: 8,
      right: 8,
      top: 16,
      bottom: 0,
      containLabel: true
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    xAxis: {
      type: 'category',
      axisTick: { show: false },
      axisLine: { show: false },
      axisLabel: { color: '#64748b' },
      data: [
        t('productCenter.workbench.dataGaps'),
        t('productCenter.workbench.priceGaps'),
        t('productCenter.workbench.publishTrend')
      ]
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      splitLine: { lineStyle: { color: '#eef2f7' } },
      axisLabel: { color: '#94a3b8' }
    },
    series: [
      {
        type: 'bar',
        barWidth: 28,
        itemStyle: { borderRadius: [6, 6, 0, 0] },
        data: [
          warningCount.value,
          blockerCount.value,
          pendingSyncCount.value
        ]
      }
    ]
  })
}

function resizeChart() {
  chart?.resize()
}

watch(
  () => [props.summary.warningCount, props.summary.blockerCount, props.summary.pendingSyncCount, localeStore.language],
  async () => {
    await nextTick()
    renderChart()
  }
)

onMounted(async () => {
  await nextTick()
  renderChart()
  window.addEventListener('resize', resizeChart)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeChart)
  chart?.dispose()
})
</script>

<style scoped lang="scss">
.product-workbench-trend {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.04);
}

.product-workbench-trend__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 14px 0;

  span {
    display: block;
    color: #64748b;
    font-size: 12px;
  }

  strong {
    display: block;
    margin-top: 6px;
    color: #111827;
    font-size: 26px;
    line-height: 1;
  }
}

.product-workbench-trend__chart {
  height: 180px;
  margin: 4px 8px 0;
}

.product-workbench-trend__metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  border-top: 1px solid #eef2f7;

  button {
    min-width: 0;
    padding: 12px;
    border: 0;
    border-right: 1px solid #eef2f7;
    background: transparent;
    color: #111827;
    cursor: pointer;
    text-align: left;

    &:last-child {
      border-right: 0;
    }

    &:hover {
      background: #f8fafc;
    }
  }

  span,
  small {
    display: block;
    overflow: hidden;
    color: #64748b;
    font-size: 12px;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  strong {
    display: block;
    margin: 6px 0 4px;
    font-size: 22px;
    line-height: 1;
  }
}

@media (max-width: 768px) {
  .product-workbench-trend__metrics {
    grid-template-columns: 1fr;

    button {
      border-right: 0;
      border-bottom: 1px solid #eef2f7;

      &:last-child {
        border-bottom: 0;
      }
    }
  }
}
</style>
