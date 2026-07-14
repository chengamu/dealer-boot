<template>
  <section v-if="actions.length" class="sales-quick-actions">
    <button v-for="action in actions" :key="action.key" type="button" class="sales-quick-actions__item" @click="emit('select', action.key)">
      <el-icon><component :is="action.icon" /></el-icon>
      <span class="sales-quick-actions__content"><strong>{{ action.title }}</strong><small>{{ action.description }}</small></span>
      <el-icon><ArrowRight /></el-icon>
    </button>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ArrowRight } from '@element-plus/icons-vue'
import type { DashboardCapabilities } from '@/api/sales-dashboard'
import { buildQuickActions } from '../dashboardPresentation'

const props = defineProps<{ capabilities: DashboardCapabilities }>()
const emit = defineEmits<{ select: [target: string] }>()
const { t } = useI18n()
const actions = computed(() => buildQuickActions(props.capabilities, t))
</script>

<style scoped>
.sales-quick-actions {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  overflow: hidden;
  border: 1px solid #e6edf7;
  border-radius: 8px;
  background: #fff;
}
.sales-quick-actions__item {
  display: grid;
  grid-template-columns: 36px minmax(0, 1fr) 16px;
  align-items: center;
  gap: 14px;
  min-height: 68px;
  padding: 14px 18px;
  border: 0;
  border-right: 1px solid #edf2f9;
  background: transparent;
  color: #1d2129;
  text-align: left;
  cursor: pointer;
}
.sales-quick-actions__item:last-child { border-right: 0; }
.sales-quick-actions__item:hover { background: #f8fbff; }
.sales-quick-actions__item :deep(.el-icon:first-child) { color: #213c89; font-size: 28px; }
.sales-quick-actions__content { min-width: 0; }
.sales-quick-actions__content strong, .sales-quick-actions__content small { display: block; }
.sales-quick-actions__content strong { color: #1f2a5c; font-size: 14px; }
.sales-quick-actions__content small { margin-top: 4px; color: #667085; font-size: 12px; line-height: 1.4; }
@media (max-width: 900px) {
  .sales-quick-actions { grid-template-columns: 1fr; }
  .sales-quick-actions__item { border-right: 0; border-bottom: 1px solid #edf2f9; }
  .sales-quick-actions__item:last-child { border-bottom: 0; }
}
</style>
