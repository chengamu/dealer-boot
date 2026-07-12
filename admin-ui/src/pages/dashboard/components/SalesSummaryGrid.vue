<template>
  <section v-if="cards.length" class="sales-summary-grid">
    <button v-for="card in cards" :key="card.key" type="button" class="sales-summary-card" @click="emit('select', card.target)">
      <span class="sales-summary-card__icon"><el-icon><component :is="card.icon" /></el-icon></span>
      <span class="sales-summary-card__body">
        <span>{{ card.label }}</span>
        <strong>{{ card.value }}</strong>
        <small>{{ card.caption }}</small>
      </span>
      <el-icon class="sales-summary-card__arrow"><ArrowRight /></el-icon>
    </button>
  </section>
</template>

<script setup lang="ts">
import { computed, type Component } from 'vue'
import { useI18n } from 'vue-i18n'
import { ArrowRight, Box, Document, Money, ShoppingCart, Van } from '@element-plus/icons-vue'
import type { SalesDashboard } from '@/api/sales-dashboard'

const props = defineProps<{ dashboard: SalesDashboard }>()
const emit = defineEmits<{ select: [target: string] }>()
const { t } = useI18n()

interface SummaryCard {
  key: string
  label: string
  value: string
  caption: string
  target: string
  icon: Component
}

const cards = computed<SummaryCard[]>(() => {
  const result: SummaryCard[] = []
  const quote = props.dashboard.quoteSummary
  const order = props.dashboard.orderSummary
  const payment = props.dashboard.paymentSummary
  const fulfillment = props.dashboard.fulfillmentSummary
  if (quote) result.push(card('activeQuotes', quote.activeCount, 'quote', Document))
  if (fulfillment && props.dashboard.capabilities.production) {
    result.push(card('inProduction', fulfillment.inProductionCount, 'production', Box))
  }
  if (payment) {
    result.push(card('monthSales', money(payment.paidThisMonthAmount, payment.currencyCode), 'payment', Money))
    result.push(card('pendingAmount', money(payment.pendingAmount, payment.currencyCode), 'payment', ShoppingCart))
  }
  if (order) {
    result.push(card('activeOrders', order.activeCount, 'order', ShoppingCart))
    result.push(card('periodOrders', order.periodSubmittedCount, 'order', ShoppingCart))
    result.push(card('pendingPayment', order.pendingPaymentCount, 'order', Money))
  }
  if (fulfillment && props.dashboard.capabilities.production) {
    result.push(card('pendingProduction', fulfillment.pendingProductionCount, 'production', Box))
  }
  if (fulfillment && props.dashboard.capabilities.shipment) {
    result.push(card('pendingShipment', fulfillment.pendingShipmentCount, 'shipment', Van))
    result.push(card('shipped', fulfillment.shippedCount, 'shipment', Van))
  }
  return result.slice(0, 4)
})

function card(key: string, value: number | string, target: string, icon: Component): SummaryCard {
  return { key, value: String(value), target, icon, label: summaryLabel(key), caption: t('dashboard.sales.summary.view') }
}

function summaryLabel(key: string) {
  if (key === 'activeQuotes') return t('dashboard.sales.summary.activeQuotes')
  if (key === 'inProduction') return t('dashboard.sales.summary.inProduction')
  if (key === 'monthSales') return t('dashboard.sales.summary.monthSales')
  if (key === 'pendingAmount') return t('dashboard.sales.summary.pendingAmount')
  if (key === 'activeOrders') return t('dashboard.sales.summary.activeOrders')
  if (key === 'periodOrders') return t('dashboard.sales.summary.periodOrders')
  if (key === 'pendingPayment') return t('dashboard.sales.summary.pendingPayment')
  if (key === 'pendingProduction') return t('dashboard.sales.summary.pendingProduction')
  if (key === 'pendingShipment') return t('dashboard.sales.summary.pendingShipment')
  return t('dashboard.sales.summary.shipped')
}

function money(value = 0, currency = 'USD') {
  return new Intl.NumberFormat(undefined, { style: 'currency', currency }).format(value)
}
</script>

<style scoped>
.sales-summary-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 10px; }
.sales-summary-card { position: relative; display: grid; grid-template-columns: 42px minmax(0, 1fr) 18px; align-items: center; min-height: 104px; padding: 14px; border: 1px solid #e9edf5; border-radius: 7px; background: #fff; color: #1d2129; text-align: left; cursor: pointer; }
.sales-summary-card:hover { border-color: #b8d2ff; background: #f8fbff; }
.sales-summary-card__icon { display: grid; place-items: center; width: 36px; height: 36px; border-radius: 6px; background: #eef5ff; color: #1677ff; font-size: 19px; }
.sales-summary-card__body { min-width: 0; }
.sales-summary-card__body > span, .sales-summary-card__body > small { display: block; color: #667085; font-size: 12px; }
.sales-summary-card__body > strong { display: block; margin: 5px 0; overflow: hidden; color: #1d2129; font-size: 23px; line-height: 1.1; text-overflow: ellipsis; white-space: nowrap; }
.sales-summary-card__arrow { color: #98a2b3; }
@media (max-width: 1200px) { .sales-summary-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
</style>
