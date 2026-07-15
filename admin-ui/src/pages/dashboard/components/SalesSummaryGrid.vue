<template>
  <section v-if="cards.length" class="sales-summary-grid">
    <button
      v-for="card in cards"
      :key="card.key"
      type="button"
      :class="['sales-summary-card', `sales-summary-card--${card.key}`]"
      @click="emit('select', card.target)"
    >
      <span class="sales-summary-card__icon">
        <el-icon><component :is="card.icon" /></el-icon>
      </span>
      <span class="sales-summary-card__content">
        <span class="sales-summary-card__label">{{ card.label }}</span>
        <strong>{{ card.value }}</strong>
        <small>{{ card.caption }}</small>
      </span>
    </button>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type { SalesDashboard } from '@/api/sales-dashboard'
import { buildMetricCards } from '../dashboardPresentation'

const props = defineProps<{ dashboard: SalesDashboard }>()
const emit = defineEmits<{ select: [target: string] }>()
const { t } = useI18n()
const cards = computed(() => buildMetricCards(props.dashboard, t))
</script>

<style scoped>
.sales-summary-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 16px; }
.sales-summary-card {
  display: grid;
  grid-template-columns: 64px minmax(0, 1fr);
  align-items: center;
  gap: 18px;
  min-height: 142px;
  padding: 22px 24px;
  border: 1px solid var(--sales-border, #e6edf7);
  border-radius: 12px;
  background: #fff;
  color: #1d2129;
  text-align: left;
  cursor: pointer;
  box-shadow: var(--sales-shadow, 0 4px 16px rgb(33 83 197 / 5%));
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}
.sales-summary-card:hover { border-color: #bfd6ff; box-shadow: 0 10px 24px rgb(33 83 197 / 10%); transform: translateY(-1px); }
.sales-summary-card:focus-visible { outline: 2px solid #1677ff; outline-offset: 2px; }
.sales-summary-card__icon {
  display: grid;
  place-items: center;
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: #edf5ff;
  color: #1677ff;
  font-size: 32px;
}
.sales-summary-card--monthSales .sales-summary-card__icon { background: #edf9f1; color: #16a34a; }
.sales-summary-card--pendingAmount .sales-summary-card__icon { background: #fff5ea; color: #f97316; }
.sales-summary-card__content { min-width: 0; }
.sales-summary-card__label, .sales-summary-card__content > small {
  display: block;
  color: #667085;
  font-size: 13px;
  line-height: 1.5;
}
.sales-summary-card__label { color: #475467; font-size: 14px; font-weight: 600; }
.sales-summary-card__content > strong {
  display: block;
  margin: 6px 0 5px;
  overflow: hidden;
  color: #101828;
  font-size: 28px;
  line-height: 1.2;
  text-overflow: ellipsis;
  white-space: nowrap;
}
@media (max-width: 1200px) { .sales-summary-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 720px) {
  .sales-summary-grid { grid-template-columns: 1fr; }
  .sales-summary-card { min-height: 124px; padding: 18px 20px; }
}
</style>
