<template>
  <div class="sales-side-panel">
    <div class="sales-side-panel__head">
      <h2>{{ t('productCenter.sales.versionHistory') }}</h2>
    </div>
    <div v-if="items.length" class="version-history-timeline">
      <div v-for="item in items" :key="item">
        <i />
        <span>{{ item }}</span>
      </div>
    </div>
    <el-empty v-else :description="t('productCenter.common.empty')" />
  </div>
</template>

<script setup lang="ts">
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'

defineProps<{
  items: string[]
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
</script>

<style scoped lang="scss">
.sales-side-panel {
  margin-bottom: 12px;
  padding: 14px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.04);
}

.sales-side-panel__head {
  margin-bottom: 12px;

  h2 {
    margin: 0;
    font-size: 16px;
  }
}

.version-history-timeline {
  display: flex;
  flex-direction: column;
  gap: 12px;

  div {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  i {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: #2563eb;
  }

  span {
    color: #334155;
    font-size: 13px;
  }
}
</style>
