<template>
  <div class="quick-order-page" v-loading="loading">
    <header class="quick-order-page__topbar">
      <div class="quick-order-page__title">
        <h1>{{ order.quickOrderNo || t('dealer.quickOrder.workbenchTitle') }}</h1>
        <el-tag :type="order.status === 'ORDERED' ? 'success' : 'warning'">{{ statusText }}</el-tag>
      </div>
      <div class="quick-order-page__actions">
        <el-button v-hasPermi="[savePerm]" :loading="saving" :disabled="readonly" @click="saveDraft">{{ t('dealer.quickOrder.saveDraft') }}</el-button>
        <el-button v-hasPermi="['dealer:quick-order:submit']" type="primary" :disabled="readonly || !rows.length || !currentInputValid" @click="openReview">
          {{ t('dealer.quickOrder.review') }}
        </el-button>
      </div>
    </header>

    <QuickOrderHeaderCard :order="order" :customers="customers" :readonly="readonly" />
    <QuickOrderCurrentItemPanel
      :item="currentItem"
      :sale-products="saleProducts"
      :setup="currentSetup"
      :language="language"
      :currency-code="order.currencyCode"
      :editing="Boolean(editingClientId)"
      :loading-setup="loadingSetup"
      :calculating="calculating"
      :readonly="readonly"
      @dirty="markCurrentDirty"
      @product-change="changeCurrentProduct"
      @calculate="calculateCurrentItem"
      @save-line="saveCurrentLine"
      @reset-line="resetCurrentLine"
      @validity-change="currentInputValid = $event"
    />
    <QuickOrderCartBar
      :rows="rows"
      :expanded="cartExpanded"
      :language="language"
      :currency-code="order.currencyCode"
      :product-amount="order.productAmount"
      :shipping-amount="order.shippingAmount"
      :total-amount="order.totalAmount"
      @toggle="cartExpanded = !cartExpanded"
      @review="openReview"
      @edit="editRow"
      @remove="removeRow"
      @clear="clearRows"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import auth from '@/plugins/auth'
import { listCustomerOptions, type CustomerProfile } from '@/api/customer/profile'
import { quickOrderApi, quickOrderCatalogApi, type QuickOrder } from '@/api/dealer-sales/quick-order'
import type { CustomerQuoteCatalogSetup } from '@/api/customer/quote'
import type { SaleProductVO } from '@/api/product-pricing/types'
import { usePermissionStore } from '@/stores/permission'
import QuickOrderCartBar from './components/QuickOrderCartBar.vue'
import QuickOrderCurrentItemPanel from './components/QuickOrderCurrentItemPanel.vue'
import QuickOrderHeaderCard from './components/QuickOrderHeaderCard.vue'
import {
  emptyQuickOrder,
  emptyQuickOrderItem,
  localeQuoteLanguage,
  normalizeQuickOrder,
  normalizeQuickOrderItem,
  syncQuickOrderTotals,
  toQuickOrderPayload,
  type QuickOrderWorkbenchItem
} from './quickOrderShared'
import { quickOrderRouteComponents, resolveRoutePath } from './quickOrderRoutes'

const { t, locale } = useI18n()
const route = useRoute()
const router = useRouter()
const permissionStore = usePermissionStore()
const order = reactive<QuickOrder>(emptyQuickOrder())
const currentItem = reactive<QuickOrderWorkbenchItem>(emptyQuickOrderItem())
const rows = ref<QuickOrderWorkbenchItem[]>([])
const customers = ref<CustomerProfile[]>([])
const saleProducts = ref<SaleProductVO[]>([])
const setups = reactive<Record<string, CustomerQuoteCatalogSetup | undefined>>({})
const loading = ref(false)
const saving = ref(false)
const loadingSetup = ref(false)
const calculating = ref(false)
const cartExpanded = ref(true)
const editingClientId = ref('')
const currentInputValid = ref(true)
const language = computed(() => localeQuoteLanguage(locale.value))
const readonly = computed(() => order.status === 'ORDERED' || !auth.hasPermi(savePerm.value))
const savePerm = computed(() => order.quickOrderId ? 'dealer:quick-order:edit' : 'dealer:quick-order:add')
const statusText = computed(() => order.status === 'ORDERED' ? t('dealer.quickOrder.status.ORDERED') : t('dealer.quickOrder.status.DRAFT'))
const currentSetup = computed(() => setups[String(currentItem.saleProductId || '')])

async function loadPage() {
  loading.value = true
  try {
    const [customerRes, productRes] = await Promise.all([
      listCustomerOptions({ status: 'ENABLED', pageNum: 1, pageSize: 500 }),
      quickOrderCatalogApi.products({ status: 'ENABLED', priceStatus: 'READY', pageNum: 1, pageSize: 500 })
    ])
    customers.value = customerRes.data || []
    saleProducts.value = productRes.data || []
    const quickOrderId = String(route.query.quickOrderId || '')
    if (quickOrderId) await loadDraft(quickOrderId)
  } finally {
    loading.value = false
  }
}

async function loadDraft(id: string) {
  const response = await quickOrderApi.get(id)
  Object.assign(order, normalizeQuickOrder(response.data || emptyQuickOrder()))
  rows.value = (response.data?.items || []).map(normalizeQuickOrderItem)
  syncQuickOrderTotals(order, rows.value)
  resetCurrentLine()
}

async function saveDraft() {
  saving.value = true
  try {
    syncQuickOrderTotals(order, rows.value)
    const payload = toQuickOrderPayload(order, rows.value)
    if (order.quickOrderId) await quickOrderApi.update(payload)
    else order.quickOrderId = String((await quickOrderApi.add(payload)).data || '')
    if (order.quickOrderId) await router.replace({ path: route.path, query: { quickOrderId: order.quickOrderId } })
    ElMessage.success(t('common.operationSuccess'))
  } finally {
    saving.value = false
  }
}

async function ensureSetup(productId: string) {
  if (!productId || setups[productId]) return
  loadingSetup.value = true
  try {
    setups[productId] = (await quickOrderCatalogApi.setup(productId)).data || {}
  } finally {
    loadingSetup.value = false
  }
}

function markCurrentDirty() {
  currentItem.calculationStatus = 'PENDING'
  currentItem.calculationMessage = ''
  currentItem.productAmount = undefined
  currentItem.shippingAmount = undefined
  currentItem.lineAmount = undefined
}

async function changeCurrentProduct() {
  const saleProduct = saleProducts.value.find((item) => String(item.saleProductId || '') === currentItem.saleProductId)
  Object.assign(currentItem, {
    selectedOptionValues: {},
    formulaId: String(saleProduct?.formulaId || ''),
    formulaVersionId: String(saleProduct?.formulaVersionId || ''),
    formulaVersionLabel: saleProduct?.formulaVersionLabel || '',
    saleProductName: saleProduct?.saleProductName || ''
  })
  markCurrentDirty()
  await ensureSetup(String(currentItem.saleProductId || ''))
}

async function calculateCurrentItem() {
  if (!ensureCurrentInputValid()) return
  if (!order.quickOrderId) await saveDraft()
  if (!order.quickOrderId) return
  calculating.value = true
  try {
    Object.assign(currentItem, normalizeQuickOrderItem((await quickOrderApi.calculateItem(order.quickOrderId, currentItem)).data || currentItem), {
      clientId: currentItem.clientId
    })
    ElMessage.success(t('dealer.quickOrder.calculation.pass'))
  } finally {
    calculating.value = false
  }
}

function saveCurrentLine() {
  if (!ensureCurrentInputValid()) return
  if (currentItem.calculationStatus !== 'PASS') return
  const next = normalizeQuickOrderItem(currentItem)
  if (editingClientId.value) {
    rows.value = rows.value.map((row) => row.clientId === editingClientId.value ? { ...next, clientId: editingClientId.value } : row)
  } else {
    rows.value = [...rows.value, next]
  }
  syncQuickOrderTotals(order, rows.value)
  resetCurrentLine()
}

function resetCurrentLine() {
  editingClientId.value = ''
  Object.assign(currentItem, emptyQuickOrderItem())
}

function editRow(row: QuickOrderWorkbenchItem) {
  editingClientId.value = row.clientId
  Object.assign(currentItem, normalizeQuickOrderItem(row), { clientId: row.clientId })
}

function removeRow(row: QuickOrderWorkbenchItem) {
  rows.value = rows.value.filter((item) => item.clientId !== row.clientId)
  syncQuickOrderTotals(order, rows.value)
  if (editingClientId.value === row.clientId) resetCurrentLine()
}

async function clearRows() {
  await ElMessageBox.confirm(t('dealer.quickOrder.cart.clearConfirm'), t('common.prompt'), { type: 'warning' })
  rows.value = []
  syncQuickOrderTotals(order, rows.value)
  resetCurrentLine()
}

async function openReview() {
  if (!ensureCurrentInputValid()) return
  await saveDraft()
  if (!order.quickOrderId) return
  const path = resolveRoutePath(permissionStore.routers, quickOrderRouteComponents.review)
  await router.push({ path, query: { quickOrderId: order.quickOrderId } })
}

function ensureCurrentInputValid() {
  if (currentInputValid.value) return true
  ElMessage.warning(t('product.numeric.inputInvalid'))
  return false
}

void loadPage()
</script>

<style scoped>
.quick-order-page { display: flex; min-height: calc(100vh - 92px); flex-direction: column; gap: 12px; padding: 12px; background: var(--admin-bg); }
.quick-order-page__topbar,
.quick-order-page__title,
.quick-order-page__actions { display: flex; align-items: center; gap: 10px; }
.quick-order-page__topbar { justify-content: space-between; }
.quick-order-page__title h1 { margin: 0; color: #1d2939; font-size: 24px; }
.quick-order-page__actions { justify-content: flex-end; }
</style>
