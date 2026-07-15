<template>
  <section v-if="actions.length" class="sales-quick-actions">
    <header><h2>{{ t('dashboard.sales.quickActions') }}</h2></header>
    <div class="sales-quick-actions__grid">
      <button
        v-for="action in actions"
        :key="action.key"
        type="button"
        :data-testid="`sales-quick-action-${action.key}`"
        :class="['sales-quick-actions__item', `sales-quick-actions__item--${action.key}`]"
        @click="emit('select', action.key)"
      >
        <span class="sales-quick-actions__icon"><el-icon><component :is="action.icon" /></el-icon></span>
        <span class="sales-quick-actions__content"><strong>{{ action.title }}</strong><small>{{ action.description }}</small></span>
        <el-icon class="sales-quick-actions__arrow"><ArrowRight /></el-icon>
      </button>
    </div>
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
  overflow: hidden;
  padding: 18px 20px 20px;
  border: 1px solid var(--sales-border, #e6edf7);
  border-radius: 12px;
  background: #fff;
  box-shadow: var(--sales-shadow, 0 4px 16px rgb(33 83 197 / 5%));
}
.sales-quick-actions header { margin-bottom: 14px; }
.sales-quick-actions h2 { margin: 0; color: #1f2a44; font-size: 16px; line-height: 24px; }
.sales-quick-actions__grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 18px; }
.sales-quick-actions__item {
  display: grid;
  grid-template-columns: 48px minmax(0, 1fr) 18px;
  align-items: center;
  gap: 16px;
  min-height: 90px;
  padding: 16px 18px;
  border: 1px solid #e4eaf3;
  border-radius: 10px;
  background: #fff;
  color: #1d2129;
  text-align: left;
  cursor: pointer;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}
.sales-quick-actions__item:hover { border-color: #bfd6ff; box-shadow: 0 6px 16px rgb(33 83 197 / 8%); }
.sales-quick-actions__item:focus-visible { outline: 2px solid #1677ff; outline-offset: 2px; }
.sales-quick-actions__icon { display: grid; place-items: center; width: 48px; height: 48px; border-radius: 10px; background: #1677ff; color: #fff; }
.sales-quick-actions__item--quickOrder .sales-quick-actions__icon { background: #0ea5a8; }
.sales-quick-actions__item--customer .sales-quick-actions__icon { background: #7048c8; }
.sales-quick-actions__icon :deep(.el-icon) { font-size: 26px; }
.sales-quick-actions__content { min-width: 0; }
.sales-quick-actions__content strong, .sales-quick-actions__content small { display: block; }
.sales-quick-actions__content strong { color: #1f2a44; font-size: 15px; }
.sales-quick-actions__content small { margin-top: 5px; color: #667085; font-size: 13px; line-height: 1.4; }
.sales-quick-actions__arrow { color: #667085; }
@media (max-width: 900px) {
  .sales-quick-actions__grid { grid-template-columns: 1fr; }
}
</style>
