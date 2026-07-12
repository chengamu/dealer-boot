<template>
  <div class="tracking-timeline">
    <h4>{{ t('dealer.fulfillment.trackingTimeline') }}</h4>
    <el-empty v-if="!sortedEvents.length" :description="t('dealer.fulfillment.noTrackingEvents')" :image-size="44" />
    <el-timeline v-else>
      <el-timeline-item v-for="event in sortedEvents" :key="event.trackingEventId || `${event.occurredTime}-${event.eventCode}`" :timestamp="minute(event.occurredTime)" placement="top">
        <strong>{{ trackingDescription(event, locale) }}</strong><p>{{ event.location || '-' }} · {{ event.eventStatus || event.eventCode || '-' }}</p>
      </el-timeline-item>
    </el-timeline>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type { TrackingEvent } from '@/api/dealer-fulfillment'
import { formatUtc } from '@/utils/datetime'
import { trackingDescription } from '../fulfillmentPresentation'
const props = defineProps<{ events: TrackingEvent[] }>()
const { t, locale } = useI18n()
const sortedEvents = computed(() => [...props.events].sort((a, b) => String(b.occurredTime || '').localeCompare(String(a.occurredTime || ''))))
const minute = (value?: string) => value ? formatUtc(value, 'YYYY-MM-DD HH:mm') : '-'
</script>

<style scoped>
.tracking-timeline { margin-top: 10px; padding: 8px 12px 0; border: 1px solid #eef0f5; background: #fafbfc; }
.tracking-timeline h4 { margin: 0 0 8px; font-size: 13px; }
.tracking-timeline strong { color: #344054; font-size: 13px; }
.tracking-timeline p { margin: 3px 0 0; color: #667085; font-size: 12px; }
</style>
