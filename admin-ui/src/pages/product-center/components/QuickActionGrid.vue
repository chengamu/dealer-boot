<template>
  <div class="product-side-panel">
    <div class="product-side-panel__head">
      <h2>{{ t('productCenter.workbench.quickActions') }}</h2>
    </div>
    <div class="quick-action-grid">
      <el-button
        v-for="action in actions"
        :key="action.key"
        :icon="action.icon"
        @click="$emit('run', action.key)"
        v-hasPermi="[action.permission]"
      >
        {{ t(action.labelKey) }}
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'

export interface QuickAction {
  key: string
  labelKey: string
  icon: string
  permission: string
}

defineProps<{
  actions: QuickAction[]
}>()

defineEmits<{
  run: [key: string]
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
</script>

<style scoped lang="scss">
.product-side-panel {
  margin-bottom: 12px;
  padding: 14px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.04);
}

.product-side-panel__head {
  margin-bottom: 12px;

  h2 {
    margin: 0;
    font-size: 16px;
    font-weight: 650;
  }
}

.quick-action-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;

  :deep(.el-button) {
    width: 100%;
    margin: 0;
  }
}
</style>
