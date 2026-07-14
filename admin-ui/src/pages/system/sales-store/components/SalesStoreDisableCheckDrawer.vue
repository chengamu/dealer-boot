<template>
  <AdminDrawer
    :model-value="modelValue"
    :title="t('salesStore.disableCheckTitle')"
    size="520px"
    variant="detail"
    append-to-body
    @update:model-value="emit('update:modelValue', $event)"
    @closed="emit('closed')"
  >
    <el-alert :title="t('salesStore.disableAllowed')" type="info" show-icon :closable="false" />
    <el-descriptions :column="1" border class="sales-store-disable__descriptions">
      <el-descriptions-item :label="t('salesStore.name')">{{ row?.storeName || '-' }}</el-descriptions-item>
      <el-descriptions-item :label="t('salesStore.code')">{{ row?.storeCode || '-' }}</el-descriptions-item>
      <el-descriptions-item :label="t('salesStore.customerCount')">{{ row?.customerCount ?? 0 }}</el-descriptions-item>
      <el-descriptions-item :label="t('salesStore.unfinishedOrderCount')">{{ row?.unfinishedOrderCount ?? 0 }}</el-descriptions-item>
    </el-descriptions>
    <template #footer>
      <div class="sales-store-disable__footer">
        <el-button @click="emit('cancel')">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="emit('confirm')">{{ t('common.confirm') }}</el-button>
      </div>
    </template>
  </AdminDrawer>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import type { SalesStore } from '@/api/system/sales-store'

defineProps<{
  modelValue: boolean
  row?: SalesStore
}>()

const emit = defineEmits<{
  (event: 'update:modelValue', value: boolean): void
  (event: 'cancel'): void
  (event: 'confirm'): void
  (event: 'closed'): void
}>()

const { t } = useI18n()
</script>

<style scoped>
.sales-store-disable__descriptions {
  margin-top: 12px;
}

.sales-store-disable__footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
