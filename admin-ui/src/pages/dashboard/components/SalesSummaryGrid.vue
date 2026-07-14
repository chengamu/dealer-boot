<template>
  <section v-if="cards.length" class="sales-summary-grid">
    <button v-for="card in cards" :key="card.key" type="button" class="sales-summary-card" @click="emit('select', card.target)">
      <span class="sales-summary-card__icon">
        <el-icon><component :is="card.icon" /></el-icon>
      </span>
      <span class="sales-summary-card__content">
        <span class="sales-summary-card__label">{{ card.label }}</span>
        <strong>{{ card.value }}</strong>
        <small>{{ card.caption }}</small>
      </span>
      <el-icon class="sales-summary-card__arrow"><ArrowRight /></el-icon>
    </button>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ArrowRight } from '@element-plus/icons-vue'
import type { SalesDashboard } from '@/api/sales-dashboard'
import { buildMetricCards } from '../dashboardPresentation'

const props = defineProps<{ dashboard: SalesDashboard }>()
const emit = defineEmits<{ select: [target: string] }>()
const { t } = useI18n()
const cards = computed(() => buildMetricCards(props.dashboard, t))
</script>

<style scoped>
.sales-summary-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 10px; }
.sales-summary-card {
  display: grid;
  grid-template-columns: 44px minmax(0, 1fr) 18px;
  align-items: center;
  min-height: 124px;
  padding: 18px 20px;
  border: 1px solid #e6edf7;
  border-radius: 8px;
  background: #fff;
  color: #1d2129;
  text-align: left;
  cursor: pointer;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}
.sales-summary-card:hover { border-color: #bfd6ff; box-shadow: 0 8px 20px rgb(33 83 197 / 6%); }
.sales-summary-card__icon {
  display: grid;
  place-items: center;
  width: 40px;
  height: 40px;
  color: #1677ff;
  font-size: 30px;
}
.sales-summary-card__content { min-width: 0; padding-left: 10px; }
.sales-summary-card__label, .sales-summary-card__content > small {
  display: block;
  color: #667085;
  font-size: 12px;
  line-height: 1.5;
}
.sales-summary-card__content > strong {
  display: block;
  margin: 8px 0 6px;
  overflow: hidden;
  color: #1f2a5c;
  font-size: 20px;
  line-height: 1.2;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.sales-summary-card__arrow { color: #7f8db0; }
@media (max-width: 1200px) { .sales-summary-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 720px) { .sales-summary-grid { grid-template-columns: 1fr; } }
</style>
