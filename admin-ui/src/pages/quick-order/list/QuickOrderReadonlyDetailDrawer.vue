<template>
  <AdminDrawer v-model="visible" :title="t('common.detail')" size="960px" variant="detail">
    <div v-loading="loading" class="quick-order-detail-drawer">
      <el-descriptions v-if="order" :column="3" border>
        <el-descriptions-item :label="t('dealer.quickOrder.no')">{{ order.quickOrderNo || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('dealer.quickOrder.customerLabel')">{{ order.companyName || order.customerName || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('common.status')">
          <el-tag :type="order.status === 'ORDERED' ? 'success' : 'warning'">
            {{ order.status === 'ORDERED' ? t('dealer.quickOrder.status.ORDERED') : t('dealer.quickOrder.status.DRAFT') }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item :label="t('dealer.quickOrder.recipient')">{{ order.recipientName || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('dealer.quickOrder.phone')">{{ order.recipientPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('dealer.quickOrder.updatedTime')">{{ formatUtc(order.updateTime, 'YYYY-MM-DD HH:mm') }}</el-descriptions-item>
      </el-descriptions>

      <QuickOrderLineTable :rows="rows" :language="language" :currency-code="order?.currencyCode" />
    </div>
  </AdminDrawer>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { formatUtc } from '@/utils/datetime'
import type { QuickOrder } from '@/api/dealer-sales/quick-order'
import QuickOrderLineTable from '@/pages/quick-order/components/QuickOrderLineTable.vue'
import { localeQuoteLanguage, normalizeQuickOrderItem, type QuickOrderWorkbenchItem } from '@/pages/quick-order/quickOrderShared'

const props = defineProps<{
  modelValue: boolean
  loading: boolean
  order?: QuickOrder
}>()

const emit = defineEmits<{
  (event: 'update:modelValue', value: boolean): void
}>()

const { t, locale } = useI18n()

const visible = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value)
})

const language = computed(() => localeQuoteLanguage(locale.value))
const rows = computed<QuickOrderWorkbenchItem[]>(() => (props.order?.items || []).map(normalizeQuickOrderItem))
</script>

<style scoped>
.quick-order-detail-drawer {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
</style>
