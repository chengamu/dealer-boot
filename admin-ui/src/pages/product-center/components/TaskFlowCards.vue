<template>
  <el-row :gutter="12" class="task-flow-cards">
    <el-col v-for="item in items" :key="item.key" :xs="12" :sm="8" :lg="4">
      <button class="task-flow-cards__item" :class="`is-${item.tone}`" type="button" @click="$emit('select', item.key)">
        <span class="task-flow-cards__icon">{{ item.icon }}</span>
        <span>
          <small>{{ t(item.labelKey) }}</small>
          <strong>{{ item.value }}</strong>
        </span>
      </button>
    </el-col>
  </el-row>
</template>

<script setup lang="ts">
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'

export interface TaskFlowItem {
  key: string
  labelKey: string
  value: number
  icon: string
  tone: 'blue' | 'slate' | 'green' | 'red' | 'amber'
}

defineProps<{
  items: TaskFlowItem[]
}>()

defineEmits<{
  select: [key: string]
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
</script>

<style scoped lang="scss">
.task-flow-cards {
  width: 100%;
}

.task-flow-cards__item {
  display: flex;
  width: 100%;
  min-height: 94px;
  align-items: center;
  gap: 12px;
  padding: 14px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.04);
  color: #111827;
  cursor: pointer;
  text-align: left;
  transition: border-color 0.18s ease, box-shadow 0.18s ease, transform 0.18s ease;

  &:hover {
    border-color: #93c5fd;
    box-shadow: 0 14px 30px rgba(37, 99, 235, 0.10);
    transform: translateY(-1px);
  }

  small {
    display: block;
    color: #64748b;
    font-size: 12px;
  }

  strong {
    display: block;
    margin-top: 6px;
    font-size: 24px;
    line-height: 1;
  }
}

.task-flow-cards__icon {
  display: grid;
  width: 38px;
  height: 38px;
  flex: none;
  place-items: center;
  border-radius: 8px;
  font-weight: 750;
}

.is-blue .task-flow-cards__icon { background: #eff6ff; color: #2563eb; }
.is-slate .task-flow-cards__icon { background: #f1f5f9; color: #475569; }
.is-green .task-flow-cards__icon { background: #ecfdf5; color: #16a34a; }
.is-red .task-flow-cards__icon { background: #fef2f2; color: #dc2626; }
.is-amber .task-flow-cards__icon { background: #fffbeb; color: #d97706; }
</style>
