<template>
  <el-row :gutter="12" class="publish-status-cards">
    <el-col v-for="item in items" :key="item.key" :xs="12" :md="6">
      <div class="publish-status-cards__item" :class="`is-${item.tone}`">
        <span>{{ t(item.labelKey) }}</span>
        <strong>{{ item.value }}</strong>
        <p>{{ t(item.hintKey) }}</p>
      </div>
    </el-col>
  </el-row>
</template>

<script setup lang="ts">
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'

export interface PublishStatusCard {
  key: string
  labelKey: string
  hintKey: string
  value: unknown
  tone: 'success' | 'warning' | 'danger' | 'info'
}

defineProps<{
  items: PublishStatusCard[]
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
</script>

<style scoped lang="scss">
.publish-status-cards__item {
  min-height: 104px;
  padding: 14px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  background: var(--el-bg-color);
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.04);

  span,
  p {
    color: #64748b;
    font-size: 12px;
  }

  strong {
    display: block;
    margin: 10px 0 6px;
    font-size: 30px;
    line-height: 1;
  }

  p {
    margin: 0;
  }

  &.is-success strong { color: #16a34a; }
  &.is-warning strong { color: #d97706; }
  &.is-danger strong { color: #dc2626; }
  &.is-info strong { color: #475569; }
}
</style>
