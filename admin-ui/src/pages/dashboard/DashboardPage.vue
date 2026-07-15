<template>
  <div class="app-container sales-dashboard-page">
    <header class="sales-dashboard-page__toolbar">
      <span v-if="dashboard" class="sales-dashboard-page__as-of">
        {{ t('dashboard.sales.dataAsOf') }} {{ formatUtc(dashboard.dataAsOf, 'YYYY-MM-DD HH:mm') }}
      </span>
      <el-button class="sales-dashboard-page__refresh" plain :icon="Refresh" :loading="loading" @click="load">
        {{ t('dashboard.sales.refresh') }}
      </el-button>
    </header>

    <el-skeleton v-if="loading && !dashboard" :rows="10" animated />
    <el-result v-else-if="blocked" icon="warning" :title="t('errorCode.403')" />
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
        <SalesTodoList :dashboard="dashboard" @open="openTodo" @open-list="openArea" />
      </div>
    </template>
  </div>
</template>

<script setup lang="ts" name="DashboardPage">
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { Refresh } from '@element-plus/icons-vue'
import { getSalesDashboard, type DashboardAttentionItem, type RecentOrder, type RecentQuote, type SalesDashboard } from '@/api/sales-dashboard'
import { usePermissionStore } from '@/stores/permission'
import { useUserStore } from '@/stores/user'
import { formatUtc } from '@/utils/datetime'
import { resolveRoutePath } from '@/pages/quick-order/quickOrderRoutes'
import { resolveDashboardAccess } from './dashboardAccess'
import SalesQuickActions from './components/SalesQuickActions.vue'
import SalesRecentDocuments from './components/SalesRecentDocuments.vue'
import SalesSummaryGrid from './components/SalesSummaryGrid.vue'
import SalesTodoList from './components/SalesTodoList.vue'

const { t } = useI18n()
const router = useRouter()
const permissionStore = usePermissionStore()
const userStore = useUserStore()
const dashboard = ref<SalesDashboard>()
const loading = ref(false)
const error = ref(false)
const blocked = ref(false)

async function load() {
  const access = resolveDashboardAccess(userStore.user, userStore.roles, userStore.permissions)
  if (access.mode === 'redirectPayment') return void openArea('payment')
  if (access.mode === 'redirectFulfillment') return void openArea('production')

  loading.value = true
  error.value = false
  blocked.value = false
  try {
    dashboard.value = (await getSalesDashboard(access.audience)).data
  } catch {
    error.value = true
    dashboard.value = undefined
  } finally {
    loading.value = false
  }
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
function openTodo(todo: DashboardAttentionItem, reason: string) {
  if (todo.type === 'QUOTE') return void router.push({ name: 'CustomerQuoteWorkbench', query: { quoteId: todo.sourceId } })
  if (reason === 'productionPending' || reason === 'shipmentPending') return openArea('production')
  if (reason === 'paymentPending') return openArea('payment')
  void router.push({ name: 'SalesDocumentDetail', params: { id: todo.sourceId } })
}

onMounted(() => { void load() })
</script>

<style scoped>
.sales-dashboard-page {
  --sales-border: #e4eaf3;
  --sales-shadow: 0 4px 16px rgb(33 83 197 / 5%);
  display: grid;
  gap: 16px;
  padding: 16px;
  background: var(--admin-bg);
  color: #1d2129;
}
.sales-dashboard-page__toolbar { display: flex; align-items: center; justify-content: flex-end; gap: 10px; min-height: 32px; }
.sales-dashboard-page__as-of { color: #667085; font-size: 12px; line-height: 1; }
.sales-dashboard-page__refresh { min-width: 98px; height: 36px; border-color: #d8e2f0; color: #344054; }
.sales-dashboard-page__content { display: grid; grid-template-columns: minmax(0, 2fr) minmax(340px, 0.95fr); gap: 16px; align-items: stretch; }
@media (max-width: 1100px) { .sales-dashboard-page__content { grid-template-columns: 1fr; } }
@media (max-width: 720px) {
  .sales-dashboard-page__toolbar { justify-content: space-between; }
  .sales-dashboard-page__as-of { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
}
</style>
