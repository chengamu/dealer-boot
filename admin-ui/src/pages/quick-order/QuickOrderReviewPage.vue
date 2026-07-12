<template>
  <div class="quick-order-review" v-loading="loading">
    <header class="quick-order-review__topbar">
      <div>
        <h1>{{ order.quickOrderNo || t('dealer.quickOrder.reviewTitle') }}</h1>
        <p>{{ t('dealer.quickOrder.reviewHint') }}</p>
      </div>
      <div class="quick-order-review__actions">
        <el-button @click="backToWorkbench">{{ t('dealer.quickOrder.backToWorkbench') }}</el-button>
        <el-button v-hasPermi="['dealer:quick-order:edit']" :loading="saving" :disabled="readonly" @click="saveDraft">{{ t('dealer.quickOrder.saveDraft') }}</el-button>
        <el-button v-hasPermi="['dealer:quick-order:submit']" type="primary" :loading="submitting" :disabled="readonly || !rows.length" @click="submitOrder">
          {{ t('dealer.quickOrder.submit') }}
        </el-button>
      </div>
    </header>

    <section class="quick-order-review__facts">
      <el-descriptions :column="4" border>
        <el-descriptions-item :label="t('dealer.quickOrder.customer')">{{ order.customerName || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('dealer.quickOrder.recipient')">{{ order.recipientName || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('dealer.quickOrder.phone')">{{ order.recipientPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('dealer.quickOrder.customerPo')">{{ order.customerPoNo || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('dealer.quickOrder.address')" :span="2">{{ order.shippingAddress || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('dealer.quickOrder.remark')" :span="2">{{ order.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </section>

    <section class="quick-order-review__table">
      <QuickOrderLineTable :rows="rows" :language="language" :currency-code="order.currencyCode" />
    </section>

    <section class="quick-order-review__totals">
      <div><span>{{ t('dealer.quickOrder.productAmount') }}</span><b>{{ money(order.productAmount) }}</b></div>
      <div><span>{{ t('dealer.quickOrder.shippingAmount') }}</span><b>{{ money(order.shippingAmount) }}</b></div>
      <div class="is-total"><span>{{ t('dealer.quickOrder.totalAmount') }}</span><b>{{ money(order.totalAmount) }}</b></div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import { quickOrderApi, type QuickOrder } from '@/api/dealer-sales/quick-order'
import { usePermissionStore } from '@/stores/permission'
import QuickOrderLineTable from './components/QuickOrderLineTable.vue'
import { localeQuoteLanguage, normalizeQuickOrder, normalizeQuickOrderItem, syncQuickOrderTotals, toQuickOrderPayload, type QuickOrderWorkbenchItem } from './quickOrderShared'
import { quickOrderRouteComponents, resolveRoutePath } from './quickOrderRoutes'
import type { DecimalValue } from '@/types/api'
import { formatCurrency } from '@/utils/businessNumber'

const { t, locale } = useI18n()
const route = useRoute()
const router = useRouter()
const permissionStore = usePermissionStore()
const order = reactive<QuickOrder>({ items: [] })
const rows = ref<QuickOrderWorkbenchItem[]>([])
const loading = ref(false)
const saving = ref(false)
const submitting = ref(false)
const language = computed(() => localeQuoteLanguage(locale.value))
const readonly = computed(() => order.status === 'ORDERED')
const quickOrderId = computed(() => String(route.query.quickOrderId || ''))

async function loadDraft() {
  if (!quickOrderId.value) return
  loading.value = true
  try {
    const response = await quickOrderApi.get(quickOrderId.value)
    Object.assign(order, normalizeQuickOrder(response.data || { items: [] }))
    rows.value = (response.data?.items || []).map(normalizeQuickOrderItem)
    syncQuickOrderTotals(order, rows.value)
  } finally {
    loading.value = false
  }
}

async function saveDraft() {
  if (!quickOrderId.value) return
  saving.value = true
  try {
    await quickOrderApi.update(toQuickOrderPayload(order, rows.value))
    ElMessage.success(t('common.operationSuccess'))
  } finally {
    saving.value = false
  }
}

function backToWorkbench() {
  const path = resolveRoutePath(permissionStore.routers, quickOrderRouteComponents.workbench)
  void router.push({ path, query: { quickOrderId: quickOrderId.value } })
}

async function submitOrder() {
  if (!quickOrderId.value) return
  submitting.value = true
  try {
    const calculated = await quickOrderApi.calculate(quickOrderId.value)
    Object.assign(order, normalizeQuickOrder(calculated.data || order))
    rows.value = (calculated.data?.items || []).map(normalizeQuickOrderItem)
    syncQuickOrderTotals(order, rows.value)
    const result = await quickOrderApi.submit(quickOrderId.value, { expectedTotalAmount: order.totalAmount || '0' })
    ElMessage.success(t('common.operationSuccess'))
    await router.push({ name: 'SalesDocumentDetail', params: { id: result.data.salesDocumentId } })
  } finally {
    submitting.value = false
  }
}

function money(value?: DecimalValue) { return formatCurrency(value ?? '0', order.currencyCode || 'USD') }

void loadDraft()
</script>

<style scoped>
.quick-order-review { display: flex; min-height: calc(100vh - 92px); flex-direction: column; gap: 12px; padding: 12px; background: var(--admin-bg); }
.quick-order-review__topbar,
.quick-order-review__actions,
.quick-order-review__totals { display: flex; align-items: center; gap: 12px; }
.quick-order-review__topbar { justify-content: space-between; }
.quick-order-review__topbar h1 { margin: 0; color: #1d2939; font-size: 24px; }
.quick-order-review__topbar p { margin: 6px 0 0; color: #667085; }
.quick-order-review__facts,
.quick-order-review__table,
.quick-order-review__totals { padding: 12px 14px; border: 1px solid #e3e9f2; border-radius: 8px; background: #fff; }
.quick-order-review__totals { justify-content: flex-end; }
.quick-order-review__totals div { display: flex; min-width: 180px; align-items: center; justify-content: space-between; color: #667085; }
.quick-order-review__totals b { color: #1d2939; font-size: 18px; }
.quick-order-review__totals .is-total b { color: #1677ff; font-size: 22px; }
</style>
