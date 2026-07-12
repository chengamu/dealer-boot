<template>
  <aside class="sales-todo-list">
    <header><h2>{{ t('dashboard.sales.todos') }}</h2><el-tag type="warning">{{ todos.length }}</el-tag></header>
    <el-empty v-if="!todos.length" :description="t('dashboard.sales.noTodos')" :image-size="60" />
    <button v-for="todo in todos" v-else :key="`${todo.type}-${todo.sourceId}`" type="button" @click="emit('open', todo)">
      <span class="sales-todo-list__icon"><el-icon><Warning /></el-icon></span>
      <span class="sales-todo-list__content">
        <strong>{{ todo.sourceNo }}</strong>
        <span>{{ reason(todo.reasonCode) }}</span>
        <small>{{ context(todo) }}<template v-if="todo.occurredAt"> · {{ formatUtc(todo.occurredAt, 'MM-DD HH:mm') }}</template></small>
      </span>
      <el-icon><ArrowRight /></el-icon>
    </button>
  </aside>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { ArrowRight, Warning } from '@element-plus/icons-vue'
import type { DashboardTodo } from '@/api/sales-dashboard'
import { formatUtc } from '@/utils/datetime'

defineProps<{ todos: DashboardTodo[] }>()
const emit = defineEmits<{ open: [todo: DashboardTodo] }>()
const { t } = useI18n()

function reason(code: DashboardTodo['reasonCode']) {
  if (code === 'QUOTE_UNCONVERTED') return t('dashboard.sales.todo.quoteUnconverted')
  if (code === 'QUOTE_EXPIRING') return t('dashboard.sales.todo.quoteExpiring')
  if (code === 'PAYMENT_MISSING') return t('dashboard.sales.todo.paymentMissing')
  if (code === 'PAYMENT_PENDING') return t('dashboard.sales.todo.paymentPending')
  if (code === 'PRODUCTION_PENDING') return t('dashboard.sales.todo.productionPending')
  return t('dashboard.sales.todo.shipmentPending')
}

function context(todo: DashboardTodo) {
  return [todo.customerName, todo.projectName].filter(Boolean).join(' · ') || t('dashboard.sales.todo.noContext')
}
</script>

<style scoped>
.sales-todo-list { align-self: start; overflow: hidden; border: 1px solid #e9edf5; border-radius: 7px; background: #fff; }
.sales-todo-list header { display: flex; align-items: center; justify-content: space-between; height: 42px; padding: 0 12px; border-bottom: 1px solid #eef0f5; }
.sales-todo-list h2 { margin: 0; color: #1d2129; font-size: 14px; }
.sales-todo-list > button { display: grid; grid-template-columns: 30px minmax(0, 1fr) 16px; align-items: center; gap: 9px; width: 100%; padding: 11px 12px; border: 0; border-bottom: 1px solid #eef0f5; background: #fff; color: #98a2b3; text-align: left; cursor: pointer; }
.sales-todo-list > button:last-child { border-bottom: 0; }
.sales-todo-list > button:hover { background: #f8fbff; }
.sales-todo-list__icon { display: grid; place-items: center; width: 28px; height: 28px; border-radius: 6px; background: #fff7e8; color: #d97706; }
.sales-todo-list__content { min-width: 0; }
.sales-todo-list__content strong, .sales-todo-list__content span, .sales-todo-list__content small { display: block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.sales-todo-list__content strong { color: #1d2129; font-size: 13px; }
.sales-todo-list__content span { margin-top: 3px; color: #475467; font-size: 12px; }
.sales-todo-list__content small { margin-top: 3px; color: #98a2b3; font-size: 11px; }
</style>
