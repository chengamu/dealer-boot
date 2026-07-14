<template>
  <section class="process-card">
    <header class="process-card__header">
      <h3>{{ t('pay.detail.attempts') }}</h3>
    </header>
    <div class="process-card__list">
      <div v-for="row in rows" :key="row.key" class="process-card__row" :class="`is-${row.tone}`">
        <span class="process-card__dot" />
        <div class="process-card__content">
          <div class="process-card__summary">
            <strong>{{ row.title }}</strong>
            <el-tag size="small" effect="plain" :type="row.tagType">{{ row.status }}</el-tag>
          </div>
          <p>{{ row.detail }}</p>
          <small>{{ row.time }}</small>
        </div>
      </div>
    </div>
    <el-empty v-if="!rows.length" :description="t('pay.detail.noAttempts')" :image-size="72" />
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type { ReconciliationCaseDetail } from '@/api/pay'
import { formatUtc } from '@/utils/datetime'
import { bankStatusText, methodText } from '../payPresentation'

const props = defineProps<{ detail: ReconciliationCaseDetail }>()
const { t } = useI18n()

const rows = computed(() => {
  const attemptRows = (props.detail.payment?.attempts || []).map((attempt) => ({
    key: `attempt-${attempt.id}`,
    title: methodText(t, attempt.channelCode),
    status: attempt.bankTransferStatus ? bankStatusText(t, attempt.bankTransferStatus) : t('pay.timeline.processing'),
    detail: attempt.channelOrderNo || attempt.bankReferenceNo || '-',
    time: minute(attempt.successTime || attempt.bankReviewedTime || attempt.bankSubmittedTime || attempt.createTime),
    tone: attempt.successTime ? 'success' : 'primary',
    tagType: attempt.successTime ? 'success' : 'warning'
  }))
  const webhookRows = (props.detail.payment?.webhooks || []).map((webhook) => ({
    key: `webhook-${webhook.channelEventId}`,
    title: webhook.eventType || '-',
    status: webhook.processStatus || '-',
    detail: webhook.errorMessage || webhook.channelOrderNo || '-',
    time: minute(webhook.processedTime || webhook.receivedTime),
    tone: webhook.processStatus === 'FAILED' ? 'danger' : 'primary',
    tagType: webhook.processStatus === 'FAILED' ? 'danger' : 'info'
  }))
  const actionRows = (props.detail.actions || []).map((action) => ({
    key: `action-${action.actionId}`,
    title: action.actionType || '-',
    status: action.resultCode || '-',
    detail: action.reason || action.resultMessage || action.operatorName || '-',
    time: minute(action.occurredTime),
    tone: action.resultCode === 'FAILED' ? 'danger' : 'success',
    tagType: action.resultCode === 'FAILED' ? 'danger' : 'success'
  }))
  return [...attemptRows, ...webhookRows, ...actionRows].sort((a, b) => a.time.localeCompare(b.time))
})

function minute(value?: string) {
  return value ? formatUtc(value, 'YYYY-MM-DD HH:mm') : '-'
}
</script>

<style scoped>
.process-card {
  border: 1px solid #e9edf5;
  border-radius: 8px;
  background: #fff;
}

.process-card__header {
  padding: 14px 16px;
  border-bottom: 1px solid #eef0f5;
}

.process-card__header h3 {
  margin: 0;
  font-size: 16px;
}

.process-card__list {
  padding: 8px 16px 16px;
}

.process-card__row {
  position: relative;
  display: flex;
  gap: 12px;
  padding: 12px 0;
}

.process-card__row + .process-card__row {
  border-top: 1px solid #f2f4f7;
}

.process-card__dot {
  width: 12px;
  height: 12px;
  margin-top: 4px;
  border-radius: 50%;
  background: #1677ff;
  flex: 0 0 12px;
}

.process-card__row.is-success .process-card__dot {
  background: #12b76a;
}

.process-card__row.is-danger .process-card__dot {
  background: #f04438;
}

.process-card__summary {
  display: flex;
  align-items: center;
  gap: 8px;
}

.process-card__summary strong {
  color: #1d2129;
}

.process-card__content {
  min-width: 0;
}

.process-card__content p {
  margin: 6px 0 4px;
  color: #667085;
  line-height: 1.5;
  overflow-wrap: anywhere;
}

.process-card__content small {
  color: #98a2b3;
}
</style>
