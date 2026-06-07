<template>
  <div class="product-side-panel">
    <div class="product-side-panel__head">
      <h2>{{ t('productCenter.workbench.syncEvents') }}</h2>
      <el-button link type="primary" @click="$emit('viewMore')">{{ t('productCenter.workbench.viewMore') }}</el-button>
    </div>
    <div v-loading="loading" class="recent-sync-events">
      <button
        v-for="row in rows.slice(0, 5)"
        :key="`${row.eventType}-${row.targetCode}-${row.updatedTime}`"
        class="recent-sync-events__item"
        type="button"
        @click="$emit('openEvent', row)"
      >
        <i :class="statusTone(row.status)" />
        <span>
          <strong>{{ row.eventType || '-' }}</strong>
          <small>{{ row.targetCode || '-' }} · {{ formatUtc(row.updatedTime) }}</small>
        </span>
      </button>
      <el-empty v-if="!loading && rows.length === 0" :description="t('productCenter.workbench.noSyncEvents')" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { formatUtc } from '@/utils/datetime'
import type { WorkbenchSyncEvent } from '@/api/product-capability/types'

defineProps<{
  rows: WorkbenchSyncEvent[]
  loading: boolean
  statusTone: (status?: string) => 'success' | 'warning' | 'danger' | 'info'
}>()

defineEmits<{
  openEvent: [row: WorkbenchSyncEvent]
  viewMore: []
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
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;

  h2 {
    margin: 0;
    font-size: 16px;
    font-weight: 650;
  }
}

.recent-sync-events {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.recent-sync-events__item {
  display: flex;
  width: 100%;
  align-items: center;
  gap: 10px;
  padding: 0;
  border: 0;
  background: transparent;
  cursor: pointer;
  text-align: left;

  i {
    width: 8px;
    height: 8px;
    flex: none;
    border-radius: 50%;
    background: #64748b;
  }

  i.success { background: #16a34a; }
  i.warning { background: #d97706; }
  i.danger { background: #dc2626; }

  strong,
  small {
    display: block;
  }

  strong {
    color: #111827;
    font-size: 13px;
  }

  small {
    margin-top: 3px;
    color: #64748b;
    font-size: 12px;
  }
}
</style>
