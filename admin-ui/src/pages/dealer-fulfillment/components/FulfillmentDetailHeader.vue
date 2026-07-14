<template>
  <header class="detail-header">
    <div class="detail-header__heading">
      <div class="detail-header__title-row">
        <h1>{{ order.orderNo || '-' }}</h1>
        <el-tag :type="order.paymentStatus === 'PAID' ? 'success' : 'warning'" effect="plain" round>
          {{ order.paymentStatus === 'PAID' ? t('pay.status.10') : t('pay.status.0') }}
        </el-tag>
        <el-tag :type="statusType(mode === 'production' ? order.productionStatus : order.shipmentStatus)" effect="plain" round>
          {{ statusText(t, mode === 'production' ? 'production' : 'shipment', mode === 'production' ? order.productionStatus : order.shipmentStatus) }}
        </el-tag>
      </div>
      <div class="detail-header__meta">
        <span>{{ t('dealer.fulfillment.sourceLabel') }}: {{ sourceText(t, order.sourceType) }}</span>
        <span>{{ order.sourceNo || '-' }}</span>
      </div>
    </div>
    <div class="detail-header__actions">
      <el-button :icon="ArrowLeft" @click="emit('back')">{{ t('common.back') }}</el-button>
      <el-button
        v-if="mode === 'production'"
        :icon="Document"
        @click="emit('openSheet')"
        v-hasPermi="['dealer:fulfillment:production:document']"
      >
        {{ t('dealer.fulfillment.productionSheet') }}
      </el-button>
      <el-button
        v-if="canStart"
        type="primary"
        :icon="VideoPlay"
        @click="emit('start')"
        v-hasPermi="['dealer:fulfillment:production:start']"
      >
        {{ t('dealer.fulfillment.startProduction') }}
      </el-button>
      <el-button
        v-if="canComplete"
        type="primary"
        :icon="CircleCheck"
        @click="emit('complete')"
        v-hasPermi="['dealer:fulfillment:production:complete']"
      >
        {{ t('dealer.fulfillment.completeProduction') }}
      </el-button>
      <el-button
        v-if="canCreate"
        type="primary"
        :icon="Plus"
        @click="emit('createPackage')"
        v-hasPermi="['dealer:fulfillment:shipment:add']"
      >
        {{ t('dealer.fulfillment.createPackage') }}
      </el-button>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ArrowLeft, CircleCheck, Document, Plus, VideoPlay } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import type { FulfillmentOrder } from '@/api/dealer-fulfillment'
import { sourceText, statusText, statusType } from '../fulfillmentPresentation'

defineProps<{
  order: FulfillmentOrder
  mode: 'production' | 'shipment' | 'tracking'
  canStart: boolean
  canComplete: boolean
  canCreate: boolean
}>()
const emit = defineEmits<{
  back: []
  openSheet: []
  start: []
  complete: []
  createPackage: []
}>()
const { t } = useI18n()
</script>

<style scoped>
.detail-header,
.detail-header__title-row,
.detail-header__actions {
  display: flex;
  gap: 10px;
}

.detail-header {
  align-items: flex-start;
  justify-content: space-between;
}

.detail-header__heading {
  min-width: 0;
}

.detail-header__title-row {
  flex-wrap: wrap;
  align-items: center;
}

.detail-header__title-row h1 {
  margin: 0;
  color: #1d2129;
  font-size: 20px;
}

.detail-header__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  margin-top: 8px;
  color: #667085;
  font-size: 13px;
}

.detail-header__actions {
  flex-wrap: wrap;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .detail-header {
    flex-direction: column;
  }
}
</style>
