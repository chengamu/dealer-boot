<template>
  <div class="app-container sales-dashboard-page">
    <header class="sales-dashboard-page__header">
      <div>
        <h1>{{ t('dashboard.sales.title') }}</h1>
        <p v-if="dashboard">{{ scopeText }} · {{ dashboard.scopeLabel }} · {{ t('dashboard.sales.period', { from: dashboard.fromDate, to: dashboard.toDate }) }}</p>
      </div>
      <div v-if="dashboard" class="sales-dashboard-page__as-of">
        <span>{{ t('dashboard.sales.dataAsOf') }} {{ formatUtc(dashboard.dataAsOf, 'YYYY-MM-DD HH:mm') }}</span>
        <el-tooltip :content="t('dashboard.sales.refresh')"><el-button :icon="Refresh" circle :loading="loading" @click="load" /></el-tooltip>
      </div>
    </header>

    <el-skeleton v-if="loading && !dashboard" :rows="8" animated />
    <el-result v-else-if="error" icon="error" :title="t('dashboard.sales.loadFailed')">
      <template #extra><el-button type="primary" @click="load">{{ t('dashboard.sales.retry') }}</el-button></template>
    </el-result>
    <template v-else-if="dashboard">
      <SalesSummaryGrid :dashboard="dashboard" @select="openArea" />
      <SalesQuickActions :capabilities="dashboard.capabilities" @select="openAction" />
      <div class="sales-dashboard-page__content">
        <SalesRecentDocuments
          :dashboard="dashboard"
          @open-quote="openQuote"
          @open-order="openOrder"
          @open-list="openArea"
        />
        <SalesTodoList :todos="dashboard.todos" @open="openTodo" />
      </div>
    </template>
  </div>
</template>

<script setup lang="ts" name="DashboardPage">
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { Refresh } from '@element-plus/icons-vue'
import { salesDashboardApi, type DashboardTodo, type RecentOrder, type RecentQuote, type SalesDashboard } from '@/api/sales-dashboard'
import { usePermissionStore } from '@/stores/permission'
import { formatUtc } from '@/utils/datetime'
import { resolveRoutePath } from '@/pages/quick-order/quickOrderRoutes'
import SalesQuickActions from './components/SalesQuickActions.vue'
import SalesRecentDocuments from './components/SalesRecentDocuments.vue'
import SalesSummaryGrid from './components/SalesSummaryGrid.vue'
import SalesTodoList from './components/SalesTodoList.vue'

const { t } = useI18n()
const router = useRouter()
const permissionStore = usePermissionStore()
const dashboard = ref<SalesDashboard>()
const loading = ref(false)
const error = ref(false)

const scopeText = computed(() => {
  const type = dashboard.value?.scopeType
  if (type === 'SELF') return t('dashboard.sales.scope.self')
  if (type === 'TENANT') return t('dashboard.sales.scope.tenant')
  if (type === 'PLATFORM_FINANCE') return t('dashboard.sales.scope.finance')
  if (type === 'FULFILLMENT') return t('dashboard.sales.scope.fulfillment')
  return t('dashboard.sales.scope.authorized')
})

async function load() {
  loading.value = true
  error.value = false
  try { dashboard.value = (await salesDashboardApi.get()).data }
  catch { error.value = true }
  finally { loading.value = false }
}

function route(component: string, fallback: string, query?: Record<string, string>) {
  void router.push({ path: resolveRoutePath(permissionStore.routers, component, fallback), query })
}

function openArea(target: string) {
  if (target === 'quote') return route('customer/quotes', '/sales/quotes')
  if (target === 'payment') return route('pay/order/list', '/sales/payments')
  if (target === 'production' || target === 'shipment') return route('dealer-fulfillment/index', '/sales/fulfillment')
  route('dealer-sales/list', '/sales/salesDocuments')
}

function openAction(target: string) {
  if (target === 'quote') return void router.push({ name: 'CustomerQuoteWorkbench' })
  if (target === 'customer') return route('customer/profile', '/customer/customers', { action: 'add' })
  route('dealer-quick-order/workbench', '/sales/quickOrders')
}

function openQuote(quote: RecentQuote) { void router.push({ name: 'CustomerQuoteWorkbench', query: { quoteId: quote.id } }) }
function openOrder(order: RecentOrder) { void router.push({ name: 'SalesDocumentDetail', params: { id: order.id } }) }
function openTodo(todo: DashboardTodo) {
  if (todo.type === 'QUOTE') return openQuote({ id: todo.sourceId, quoteNo: todo.sourceNo, status: 'CONFIRMED' })
  if (todo.reasonCode === 'PRODUCTION_PENDING' || todo.reasonCode === 'SHIPMENT_PENDING') return openArea('production')
  if (todo.reasonCode === 'PAYMENT_MISSING' || todo.reasonCode === 'PAYMENT_PENDING') return openArea('payment')
  void router.push({ name: 'SalesDocumentDetail', params: { id: todo.sourceId } })
}

void load()
</script>

<style scoped>
.sales-dashboard-page { display: grid; gap: 10px; padding: 10px; background: var(--admin-bg); color: #1d2129; }
.sales-dashboard-page__header { display: flex; align-items: center; justify-content: space-between; min-height: 48px; padding: 0 2px; }
.sales-dashboard-page__header h1 { margin: 0; font-size: 18px; letter-spacing: 0; }
.sales-dashboard-page__header p, .sales-dashboard-page__as-of { margin: 4px 0 0; color: #667085; font-size: 12px; }
.sales-dashboard-page__as-of { display: flex; align-items: center; gap: 8px; margin: 0; }
.sales-dashboard-page__content { display: grid; grid-template-columns: minmax(0, 1fr) minmax(280px, 340px); gap: 10px; align-items: start; }
@media (max-width: 1100px) { .sales-dashboard-page__content { grid-template-columns: 1fr; } }
</style>
