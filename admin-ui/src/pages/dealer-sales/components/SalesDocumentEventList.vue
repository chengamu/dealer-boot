<template>
  <section class="sales-document-event-list">
    <h3>{{ t('dealer.sales.events') }}</h3>
    <el-timeline>
      <el-timeline-item
        v-for="event in events"
        :key="event.salesEventId"
        :timestamp="formatUtc(event.occurredTime, 'YYYY-MM-DD HH:mm')"
      >
        <strong>{{ eventTypeText(t, event.eventType) }}</strong>
        <span>{{ event.operatorName || '-' }}</span>
        <p v-if="event.eventNote">{{ event.eventNote }}</p>
      </el-timeline-item>
    </el-timeline>
  </section>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import type { SalesDocumentEvent } from '@/api/dealer-sales'
import { formatUtc } from '@/utils/datetime'
import { eventTypeText } from '../salesPresentation'

defineProps<{ events: SalesDocumentEvent[] }>()
const { t } = useI18n()
</script>

<style scoped>
.sales-document-event-list { padding: 12px; border: 1px solid #e4eaf2; border-radius: 8px; background: #fff; }
.sales-document-event-list h3 { margin: 0 0 12px; }
.sales-document-event-list :deep(.el-timeline-item__wrapper p) { margin: 6px 0 0; color: #667085; }
</style>
