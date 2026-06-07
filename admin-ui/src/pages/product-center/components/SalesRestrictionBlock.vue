<template>
  <div class="sales-side-panel">
    <div class="sales-side-panel__head">
      <h2>{{ t('productCenter.sales.restrictions') }}</h2>
      <el-tag type="warning" effect="light">{{ t('productCenter.template.warnings') }}</el-tag>
    </div>
    <div class="sales-restriction-block">
      <div><span>{{ t('productCenter.sales.regionLimit') }}</span><strong>-</strong></div>
      <div><span>{{ t('productCenter.sales.channelLimit') }}</span><strong>-</strong></div>
      <div><span>{{ t('productCenter.sales.leadTime') }}</span><strong>-</strong></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'

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
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;

  h2 {
    margin: 0;
    font-size: 16px;
  }
}

.sales-restriction-block {
  display: flex;
  flex-direction: column;
  gap: 10px;

  div {
    display: flex;
    justify-content: space-between;
    gap: 12px;
    padding-bottom: 10px;
    border-bottom: 1px solid #eef2f7;
  }

  span {
    color: #64748b;
    font-size: 12px;
  }

  strong {
    color: #111827;
    font-size: 13px;
  }
}
</style>
