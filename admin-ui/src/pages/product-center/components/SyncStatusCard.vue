<template>
  <div class="sync-status-card">
    <div class="sync-status-card__head">
      <h2>{{ t('productCenter.publish.syncEventStatus') }}</h2>
      <el-tag type="success">{{ t('productCenter.status.enabled') }}</el-tag>
    </div>
    <div class="sync-status-card__grid">
      <div><span>{{ t('productCenter.publish.outbox') }}</span><strong>{{ outboxCount }}</strong></div>
      <div><span>{{ t('productCenter.workbench.retryCount') }}</span><strong>0</strong></div>
      <div><span>{{ t('productCenter.publish.lastErrorMessage') }}</span><strong>-</strong></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import type { ProductRecord } from '@/api/product-capability/types'

const props = defineProps<{
  execution: ProductRecord
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)

const outboxCount = computed(() => (props.execution.outboxes as unknown[] | undefined)?.length || 0)
</script>

<style scoped lang="scss">
.sync-status-card {
  padding: 14px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  background: var(--el-bg-color);
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.04);
}

.sync-status-card__head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;

  h2 {
    margin: 0;
    font-size: 16px;
    font-weight: 650;
  }
}

.sync-status-card__grid {
  display: flex;
  flex-direction: column;
  gap: 10px;

  div {
    display: flex;
    justify-content: space-between;
    gap: 12px;
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
