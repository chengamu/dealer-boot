<template>
  <aside class="sales-todo-list">
    <header><h2>{{ t('dashboard.sales.todos') }}</h2></header>
    <template v-if="groups.length">
      <section
        v-for="group in groups"
        :key="group.key"
        class="sales-todo-list__group"
        :style="{ '--group-accent': group.accent }"
      >
        <div class="sales-todo-list__group-header">
          <div class="sales-todo-list__group-title">
            <strong>{{ group.title }}</strong>
            <em>{{ group.count }}</em>
          </div>
          <el-button link type="primary" @click="emit('open-list', group.target)">{{ t('dashboard.sales.attention.viewAll') }}</el-button>
        </div>
        <p v-if="!group.items.length" class="sales-todo-list__empty">{{ t('dashboard.sales.attention.empty') }}</p>
        <button v-for="todo in group.items" v-else :key="`${group.key}-${todo.type}-${todo.sourceId}`" type="button" @click="emit('open', todo, group.key)">
          <span class="sales-todo-list__content">
            <strong>{{ todo.sourceNo }}</strong>
            <span>{{ context(todo) }}</span>
          </span>
          <span class="sales-todo-list__meta">{{ meta(group.key, todo) }}</span>
        </button>
      </section>
    </template>
    <div v-else class="sales-todo-list__empty-state">
      <img :src="salesAttentionEmpty" alt="" />
      <strong>{{ t('dashboard.sales.noTodosTitle') }}</strong>
      <p>{{ t('dashboard.sales.noTodos') }}</p>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type { DashboardAttentionItem, SalesDashboard } from '@/api/sales-dashboard'
import salesAttentionEmpty from '@/assets/illustrations/sales-attention-empty.png'
import { formatUtc } from '@/utils/datetime'
import { buildAttentionGroups } from '../dashboardPresentation'

const props = defineProps<{ dashboard: SalesDashboard }>()
const emit = defineEmits<{
  open: [todo: DashboardAttentionItem, reason: string]
  'open-list': [target: string]
}>()
const { t } = useI18n()
const groups = computed(() => buildAttentionGroups(props.dashboard, t))

function context(todo: DashboardAttentionItem) {
  return [todo.customerName, todo.projectName].filter(Boolean).join(' / ') || t('dashboard.sales.todo.noContext')
}

function meta(groupKey: string, todo: DashboardAttentionItem) {
  if (!todo.occurredAt) return '-'
  return groupKey === 'quoteExpiring'
    ? formatUtc(todo.occurredAt, 'YYYY-MM-DD')
    : formatUtc(todo.occurredAt, 'MM-DD HH:mm')
}
</script>

<style scoped>
.sales-todo-list { display: flex; min-height: 100%; overflow: hidden; flex-direction: column; border: 1px solid var(--sales-border, #e6edf7); border-radius: 12px; background: #fff; box-shadow: var(--sales-shadow, 0 4px 16px rgb(33 83 197 / 5%)); }
.sales-todo-list header { height: 52px; padding: 0 18px; border-bottom: 1px solid #eef2f8; }
.sales-todo-list h2 { margin: 0; color: #1f2a44; font-size: 16px; line-height: 52px; }
.sales-todo-list__group { padding: 12px 12px 0; }
.sales-todo-list__group:last-child { padding-bottom: 12px; }
.sales-todo-list__group-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 28px;
  padding-left: 10px;
  border-left: 3px solid var(--group-accent);
}
.sales-todo-list__group-title { display: flex; align-items: center; gap: 8px; min-width: 0; }
.sales-todo-list__group-title strong { color: #1f2a5c; font-size: 14px; }
.sales-todo-list__group-title em {
  min-width: 22px;
  padding: 0 6px;
  border-radius: 10px;
  background: color-mix(in srgb, var(--group-accent) 14%, white);
  color: var(--group-accent);
  font-style: normal;
  font-size: 12px;
  line-height: 20px;
  text-align: center;
}
.sales-todo-list__empty {
  margin: 8px 0 0;
  padding: 14px 12px;
  border: 1px solid #eef2f8;
  border-radius: 8px;
  color: #98a2b3;
  font-size: 12px;
}
.sales-todo-list__group > button {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 8px;
  width: 100%;
  margin-top: 8px;
  padding: 12px;
  border: 1px solid #eef2f8;
  border-radius: 8px;
  background: #fff;
  text-align: left;
  cursor: pointer;
}
.sales-todo-list__group > button:hover { background: #f8fbff; border-color: #dce8fb; }
.sales-todo-list__group > button:focus-visible { outline: 2px solid #1677ff; outline-offset: 2px; }
.sales-todo-list__content { min-width: 0; }
.sales-todo-list__content strong, .sales-todo-list__content span { display: block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.sales-todo-list__content strong { color: #1f2a5c; font-size: 13px; }
.sales-todo-list__content span { margin-top: 6px; color: #475467; font-size: 12px; }
.sales-todo-list__meta { color: #667085; font-size: 12px; line-height: 18px; white-space: nowrap; }
.sales-todo-list__empty-state { display: flex; flex: 1; min-height: 340px; align-items: center; justify-content: center; flex-direction: column; padding: 24px; text-align: center; }
.sales-todo-list__empty-state img { display: block; width: 240px; max-width: 70%; height: auto; }
.sales-todo-list__empty-state strong { margin-top: 12px; color: #667085; font-size: 14px; }
.sales-todo-list__empty-state p { margin: 8px 0 0; color: #98a2b3; font-size: 13px; }
</style>
