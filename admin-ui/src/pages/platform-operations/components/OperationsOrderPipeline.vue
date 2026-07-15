<template>
  <section class="operations-order-pipeline">
    <header><div><h2>{{ t('dashboard.operations.orderSummary') }}</h2><span>{{ t('dashboard.operations.lastThirtyDays') }}</span></div><el-button link type="primary" @click="emit('openOrders')">{{ t('dashboard.operations.viewOrders') }}</el-button></header>
    <div class="operations-order-pipeline__body"><div v-for="(item, index) in stages" :key="item.key" class="pipeline-stage" :class="`is-${item.tone}`"><span class="pipeline-stage__icon"><el-icon><component :is="item.icon" /></el-icon></span><div><small>{{ item.label }}</small><strong>{{ item.value }}{{ t('dashboard.operations.ordersUnit') }}</strong></div><el-icon v-if="index < stages.length - 1" class="pipeline-stage__arrow"><ArrowRight /></el-icon></div><div class="pipeline-amount"><small>{{ t('dashboard.operations.orderAmount') }}</small><strong>{{ amountText }}</strong></div></div>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ArrowRight, Box, CircleCheck, Document, Money, Van } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import type { OperationsSummary } from '@/api/platform-operations'
import { formatCurrency } from '@/utils/businessNumber'

const props = defineProps<{ summary?: OperationsSummary }>()
const emit = defineEmits<{ openOrders: [] }>()
const { t } = useI18n()
const stages = computed(() => [
  { key: 'submitted', tone: 'submitted', icon: Document, label: t('dashboard.operations.submittedOrders'), value: props.summary?.orderLifecycle?.submittedCount || 0 },
  { key: 'unpaid', tone: 'unpaid', icon: Money, label: t('dashboard.operations.unpaidOrders'), value: props.summary?.orderLifecycle?.unpaidCount || 0 },
  { key: 'production', tone: 'production', icon: Box, label: t('dashboard.operations.inProductionOrders'), value: props.summary?.orderLifecycle?.productionCount || 0 },
  { key: 'shipped', tone: 'shipped', icon: Van, label: t('dashboard.operations.shippedOrders'), value: props.summary?.orderLifecycle?.shippedCount || 0 },
  { key: 'completed', tone: 'completed', icon: CircleCheck, label: t('dashboard.operations.completedOrders'), value: props.summary?.orderLifecycle?.completedCount || 0 }
])
const amountText = computed(() => Object.entries(props.summary?.currencyAmounts || {}).map(([code, amount]) => formatCurrency(amount, code)).join(' · ') || '-')
</script>

<style scoped>
.operations-order-pipeline { border: 1px solid #e7ecf4; border-radius: 12px; background: #fff; }.operations-order-pipeline header { display: flex; align-items: center; justify-content: space-between; padding: 15px 18px 8px; }.operations-order-pipeline h2 { display: inline; margin: 0; color: #182230; font-size: 16px; }.operations-order-pipeline header span { margin-left: 8px; color: #98a2b3; font-size: 12px; }.operations-order-pipeline__body { display: grid; grid-template-columns: repeat(5, minmax(0, 1fr)) minmax(140px, .8fr); align-items: center; gap: 12px; padding: 10px 18px 18px; }.pipeline-stage { position: relative; display: flex; min-width: 0; align-items: center; gap: 9px; }.pipeline-stage__icon { display: grid; width: 38px; height: 38px; flex: 0 0 38px; place-items: center; border-radius: 10px; font-size: 21px; }.pipeline-stage small,.pipeline-amount small { display: block; color: #667085; font-size: 12px; }.pipeline-stage strong { display: block; margin-top: 3px; color: #182230; font-size: 16px; }.pipeline-stage__arrow { position: absolute; right: -12px; color: #98a2b3; }.pipeline-amount { padding-left: 14px; border-left: 1px solid #edf0f5; text-align: right; }.pipeline-amount strong { display: block; margin-top: 4px; color: #182230; font-size: 18px; }.is-submitted .pipeline-stage__icon { background: #eff8ff; color: #2e90fa; }.is-unpaid .pipeline-stage__icon { background: #fff7ed; color: #f79009; }.is-production .pipeline-stage__icon { background: #ecfdf3; color: #12b76a; }.is-shipped .pipeline-stage__icon { background: #f4f3ff; color: #7f56d9; }.is-completed .pipeline-stage__icon { background: #ecfdf3; color: #039855; }
@media (max-width: 1200px) { .operations-order-pipeline__body { grid-template-columns: repeat(3, minmax(0, 1fr)); }.pipeline-stage__arrow { display: none; }.pipeline-amount { grid-column: span 3; padding-top: 10px; padding-left: 0; border-top: 1px solid #edf0f5; border-left: 0; text-align: left; } }
@media (max-width: 620px) { .operations-order-pipeline__body { grid-template-columns: 1fr; }.pipeline-amount { grid-column: auto; } }
</style>
