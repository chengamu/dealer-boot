<template>
  <section class="operations-summary-cards" :aria-label="t('dashboard.operations.merchantOverview')">
    <button v-for="card in cards" :key="card.key" type="button" class="operations-summary-card" :class="`is-${card.tone}`" @click="emit('filter', card.key)">
      <span class="operations-summary-card__icon"><el-icon><component :is="card.icon" /></el-icon></span>
      <span class="operations-summary-card__content"><small>{{ card.label }}</small><strong>{{ card.value }}</strong><em>{{ card.caption }}</em></span>
      <el-icon class="operations-summary-card__arrow"><ArrowRight /></el-icon>
    </button>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ArrowRight, CircleClose, DocumentChecked, Medal, Shop } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import type { OperationsSummary } from '@/api/platform-operations'

const props = defineProps<{ summary?: OperationsSummary }>()
const emit = defineEmits<{ filter: [key: 'pending' | 'enabled' | 'disabled' | 'vip'] }>()
const { t } = useI18n()

const cards = computed(() => [
  { key: 'pending' as const, tone: 'pending', icon: DocumentChecked, label: t('dashboard.operations.pendingApplications'), value: props.summary?.pendingApplicationCount || 0, caption: t('dashboard.operations.pendingCaption') },
  { key: 'enabled' as const, tone: 'enabled', icon: Shop, label: t('dashboard.operations.enabledMerchants'), value: props.summary?.enabledMerchantCount || 0, caption: t('dashboard.operations.enabledCaption') },
  { key: 'disabled' as const, tone: 'disabled', icon: CircleClose, label: t('dashboard.operations.disabledMerchants'), value: props.summary?.disabledMerchantCount || 0, caption: t('dashboard.operations.disabledCaption') },
  { key: 'vip' as const, tone: 'vip', icon: Medal, label: t('dashboard.operations.vipMerchants'), value: props.summary?.vipMerchantCount || 0, caption: t('dashboard.operations.vipCaption') }
])
</script>

<style scoped>
.operations-summary-cards { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 12px; }
.operations-summary-card { display: flex; min-width: 0; min-height: 108px; align-items: center; gap: 14px; padding: 16px; border: 1px solid #e7ecf4; border-radius: 12px; background: #fff; color: #182230; text-align: left; cursor: pointer; transition: border-color .18s, box-shadow .18s, transform .18s; }
.operations-summary-card:hover { border-color: #b7d3ff; box-shadow: 0 8px 20px rgb(31 78 151 / 9%); transform: translateY(-1px); }
.operations-summary-card__icon { display: grid; width: 54px; height: 54px; flex: 0 0 54px; place-items: center; border-radius: 14px; font-size: 30px; }
.operations-summary-card__content { display: grid; min-width: 0; gap: 3px; }
.operations-summary-card small { overflow: hidden; color: #344054; font-size: 14px; font-weight: 600; text-overflow: ellipsis; white-space: nowrap; }
.operations-summary-card strong { color: #101828; font-size: 28px; line-height: 32px; }
.operations-summary-card em { color: #98a2b3; font-size: 12px; font-style: normal; }
.operations-summary-card__arrow { margin-left: auto; color: #98a2b3; font-size: 18px; }
.is-pending .operations-summary-card__icon { background: #fff7ed; color: #f79009; }
.is-enabled .operations-summary-card__icon { background: #ecfdf3; color: #12b76a; }
.is-disabled .operations-summary-card__icon { background: #fff1f3; color: #f04438; }
.is-vip .operations-summary-card__icon { background: #f4f3ff; color: #7f56d9; }
@media (max-width: 1180px) { .operations-summary-cards { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 620px) { .operations-summary-cards { grid-template-columns: 1fr; } }
</style>
