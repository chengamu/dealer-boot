<template>
  <section class="detail-card">
    <div class="detail-card__grid">
      <div>
        <dt>{{ t('pay.merchant') }}</dt>
        <dd>{{ detail.payment?.merchantName || '-' }}</dd>
      </div>
      <div>
        <dt>{{ t('pay.customer') }}</dt>
        <dd>{{ detail.payment?.customerName || '-' }}</dd>
      </div>
      <div>
        <dt>{{ t('pay.methodLabel') }}</dt>
        <dd>{{ methodText(t, detail.payment?.channelCode) }}</dd>
      </div>
      <div>
        <dt>{{ t('pay.currency') }}</dt>
        <dd>{{ detail.payment?.currency || '-' }}</dd>
      </div>
      <div>
        <dt>{{ t('pay.amount') }}</dt>
        <dd>{{ minorMoney(detail.payment?.price, detail.payment?.currency) }}</dd>
      </div>
      <div>
        <dt>{{ t('pay.successTime') }}</dt>
        <dd>{{ time(detail.payment?.successTime) }}</dd>
      </div>
    </div>
    <div v-if="snapshotRows.length" class="detail-card__snapshots">
      <div v-for="row in snapshotRows" :key="row.key" class="detail-card__snapshot-row">
        <strong>{{ row.key }}</strong>
        <span>{{ row.expected || '-' }}</span>
        <span>{{ row.actual || '-' }}</span>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type { ReconciliationCaseDetail } from '@/api/pay'
import { formatUtc } from '@/utils/datetime'
import { methodText, minorMoney } from '../payPresentation'

const props = defineProps<{ detail: ReconciliationCaseDetail }>()
const { t } = useI18n()

const snapshotRows = computed(() => {
  const expected = parseJson(props.detail.reconciliationCase?.expectedSnapshotJson)
  const actual = parseJson(props.detail.reconciliationCase?.actualSnapshotJson)
  return Array.from(new Set([...Object.keys(expected), ...Object.keys(actual)])).map((key) => ({
    key,
    expected: stringify(expected[key]),
    actual: stringify(actual[key])
  }))
})

function parseJson(value?: string) {
  if (!value) return {} as Record<string, unknown>
  try {
    const parsed: unknown = JSON.parse(value)
    return typeof parsed === 'object' && parsed ? parsed as Record<string, unknown> : {}
  } catch {
    return {}
  }
}

function stringify(value: unknown) {
  if (value == null || value === '') return ''
  return typeof value === 'object' ? JSON.stringify(value) : String(value)
}

function time(value?: string) {
  return value ? formatUtc(value, 'YYYY-MM-DD HH:mm') : '-'
}
</script>

<style scoped>
.detail-card {
  border: 1px solid #e9edf5;
  border-radius: 8px;
  background: #fff;
}

.detail-card__grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
}

.detail-card__grid > div {
  min-width: 0;
  padding: 16px;
  border-right: 1px solid #eef0f5;
}

.detail-card__grid > div:last-child {
  border-right: 0;
}

dt {
  margin-bottom: 6px;
  color: #98a2b3;
  font-size: 12px;
}

dd {
  margin: 0;
  color: #1d2129;
  line-height: 1.5;
  overflow-wrap: anywhere;
}

.detail-card__snapshots {
  border-top: 1px solid #eef0f5;
}

.detail-card__snapshot-row {
  display: grid;
  grid-template-columns: minmax(120px, 0.8fr) minmax(0, 1fr) minmax(0, 1fr);
  gap: 12px;
  padding: 12px 16px;
  border-top: 1px solid #f2f4f7;
}

.detail-card__snapshot-row:first-child {
  border-top: 0;
}

.detail-card__snapshot-row strong {
  color: #344054;
  overflow-wrap: anywhere;
}

.detail-card__snapshot-row span {
  color: #667085;
  overflow-wrap: anywhere;
}

@media (max-width: 1200px) {
  .detail-card__grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .detail-card__grid > div:nth-child(3n) {
    border-right: 0;
  }
}

@media (max-width: 768px) {
  .detail-card__grid {
    grid-template-columns: 1fr;
  }

  .detail-card__grid > div {
    border-right: 0;
    border-bottom: 1px solid #eef0f5;
  }

  .detail-card__grid > div:last-child {
    border-bottom: 0;
  }

  .detail-card__snapshot-row {
    grid-template-columns: 1fr;
  }
}
</style>
