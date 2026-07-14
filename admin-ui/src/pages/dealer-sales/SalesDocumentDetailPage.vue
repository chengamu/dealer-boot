<template>
  <div class="sales-document-detail" v-loading="loading">
    <header class="sales-document-detail__topbar">
      <div class="sales-document-detail__title">
        <div>
          <h1>{{ `${t('dealer.sales.list')} ${document.orderNo || '-'}` }}</h1>
          <p>{{ sourceTypeText(t, document.sourceType) }} · {{ document.sourceNo || document.quoteNo || '-' }}</p>
        </div>
      </div>
      <div class="sales-document-detail__badges">
        <el-tag :type="document.documentStatus === 'COMPLETED' ? 'success' : document.documentStatus === 'CANCELLED' ? 'info' : 'primary'">
          {{ documentStatusText(t, document.documentStatus) }}
        </el-tag>
        <el-tag effect="plain">{{ paymentStatusText(t, document.paymentStatus) }}</el-tag>
        <el-tag effect="plain">{{ productionStatusText(t, document.productionStatus) }}</el-tag>
        <el-tag effect="plain">{{ shipmentStatusText(t, document.shipmentStatus) }}</el-tag>
      </div>
      <div class="sales-document-detail__actions">
        <el-button icon="ArrowLeft" @click="backToList">{{ t('common.back') }}</el-button>
        <el-button v-if="showSource" @click="viewSource">{{ t('dealer.sales.viewSource') }}</el-button>
        <el-button v-if="canDocument" @click="togglePreview">{{ previewVisible ? t('dealer.sales.preview.close') : t('dealer.sales.preview.open') }}</el-button>
        <el-button v-if="canDocument" @click="exportExcel">{{ t('common.export') }}</el-button>
        <el-button v-if="canEmail" @click="emailVisible = true">{{ t('dealer.sales.email') }}</el-button>
        <el-button v-if="canCancel" type="danger" plain @click="cancelOrder">
          {{ t('dealer.sales.cancel') }}
        </el-button>
        <el-button v-if="canCheckout" type="primary" @click="openCheckout">{{ t('dealer.sales.openPayment') }}</el-button>
      </div>
    </header>

    <SalesDocumentProgressTracker :document="document" />

    <section class="sales-document-detail__body" :class="{ 'has-preview': previewVisible }">
      <div class="sales-document-detail__main">
        <SalesDocumentFactsCard :document="document" />
        <section class="sales-document-detail__section">
          <h3>{{ t('dealer.sales.items') }}</h3>
          <SalesDocumentItemsTable :items="document.items || []" :currency-code="document.currencyCode" />
        </section>
        <section class="sales-document-detail__summary">
          <div><span>{{ t('dealer.sales.productAmount') }}</span><b>{{ money(document.productAmount) }}</b></div>
          <div><span>{{ t('dealer.sales.shippingAmount') }}</span><b>{{ money(document.shippingAmount) }}</b></div>
          <div><span>{{ t('dealer.sales.taxAmount') }}</span><b>{{ money(document.taxAmount) }}</b></div>
          <div class="is-total"><span>{{ t('dealer.sales.totalAmount') }}</span><b>{{ money(document.totalAmount) }}</b></div>
        </section>
        <section class="sales-document-detail__trail">
          <article class="sales-document-detail__source-card">
            <span>{{ t('dealer.sales.source') }}</span>
            <strong>{{ sourceTypeText(t, document.sourceType) }} · {{ document.sourceNo || document.quoteNo || '-' }}</strong>
            <p v-if="document.remark">{{ document.remark }}</p>
          </article>
          <SalesDocumentEventList :events="document.events || []" />
        </section>
      </div>
      <SalesDocumentPdfPreview
        v-if="previewVisible"
        class="sales-document-detail__preview"
        :src="pdfUrl"
        @close="closePreview"
        @print="printPreview"
        @new-window="openPreviewInNewWindow"
      />
    </section>

    <el-dialog v-model="emailVisible" :title="t('dealer.sales.email')" width="500px">
      <el-form label-width="90px">
        <el-form-item :label="t('dealer.sales.recipientEmail')"><el-input v-model="email.recipient" /></el-form-item>
        <el-form-item :label="t('dealer.sales.message')"><el-input v-model="email.message" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="emailVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="sendEmail">{{ t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import { platformSalesApi, salesApi, type SalesDocument } from '@/api/dealer-sales'
import { usePermissionStore } from '@/stores/permission'
import { formatUtc } from '@/utils/datetime'
import { formatCurrency } from '@/utils/businessNumber'
import { download } from '@/utils/request'
import auth from '@/plugins/auth'
import SalesDocumentEventList from './components/SalesDocumentEventList.vue'
import SalesDocumentFactsCard from './components/SalesDocumentFactsCard.vue'
import SalesDocumentItemsTable from './components/SalesDocumentItemsTable.vue'
import SalesDocumentPdfPreview from './components/SalesDocumentPdfPreview.vue'
import SalesDocumentProgressTracker from './components/SalesDocumentProgressTracker.vue'
import { quickOrderRouteComponents, resolveRoutePath } from '@/pages/quick-order/quickOrderRoutes'
import { documentStatusText, paymentStatusText, productionStatusText, shipmentStatusText, sourceTypeText } from './salesPresentation'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const permissionStore = usePermissionStore()
const platform = auth.hasPermi('platform:sales:order:list')
const document = reactive<SalesDocument>({ items: [] })
const loading = ref(false)
const emailVisible = ref(false)
const previewVisible = ref(false)
const pdfUrl = ref('')
const email = reactive({ recipient: '', message: '' })
const id = computed(() => String(route.params.id || ''))
const hasSource = computed(() => Boolean(document.sourceQuoteId || document.sourceQuickOrderId))
const showSource = computed(() => !platform && hasSource.value)
const canDocument = computed(() => platform ? auth.hasPermi('platform:sales:order:document') : auth.hasPermi('dealer:sales:document'))
const canEmail = computed(() => !platform && auth.hasPermi('dealer:sales:email'))
const canCancel = computed(() => !platform && document.documentStatus === 'SUBMITTED' && auth.hasPermi('dealer:sales:cancel'))
const canCheckout = computed(() => !platform && document.paymentStatus === 'UNPAID' && auth.hasPermi('pay:order:submit'))
const previewInitialized = ref(false)

async function loadDetail() {
  loading.value = true
  try {
    const response = platform ? await platformSalesApi.get(id.value) : await salesApi.get(id.value)
    Object.assign(document, response.data || { items: [] })
    email.recipient = document.customerEmail || ''
    if (canDocument.value && (!previewInitialized.value || previewVisible.value)) {
      previewInitialized.value = true
      await openPreview(true)
    }
  } finally {
    loading.value = false
  }
}

async function openPreview(force = false) {
  if (!id.value) return
  if (pdfUrl.value && !force) {
    previewVisible.value = true
    return
  }
  const blob = platform ? await platformSalesApi.pdf(id.value) : await salesApi.pdf(id.value, 'ORDER')
  closePreview()
  pdfUrl.value = URL.createObjectURL(blob)
  previewVisible.value = true
}

async function togglePreview() {
  if (previewVisible.value) return closePreview()
  await openPreview()
}

function closePreview() {
  previewVisible.value = false
  if (pdfUrl.value) URL.revokeObjectURL(pdfUrl.value)
  pdfUrl.value = ''
}

function openPreviewInNewWindow() {
  if (pdfUrl.value) window.open(pdfUrl.value, '_blank', 'noopener')
}

function printPreview() {
  if (!pdfUrl.value) return
  const target = window.open(pdfUrl.value, '_blank', 'noopener')
  target?.addEventListener('load', () => target.print(), { once: true })
}

async function cancelOrder() {
  const result = await ElMessageBox.prompt(t('dealer.sales.cancelReason'), t('dealer.sales.cancel'), { inputType: 'textarea' })
  await salesApi.cancel(id.value, result.value)
  ElMessage.success(t('common.operationSuccess'))
  await loadDetail()
}

async function sendEmail() {
  await salesApi.email(id.value, email)
  emailVisible.value = false
  ElMessage.success(t('common.operationSuccess'))
}

function exportExcel() {
  download(
    platform ? `/platform-sales/orders/${id.value}/export` : `dealer/sales-documents/${id.value}/export`,
    {},
    `${document.orderNo || document.sourceNo || id.value}.xlsx`
  )
}

function viewSource() {
  if (document.sourceType === 'QUICK_ORDER' && document.sourceQuickOrderId) {
    const path = resolveRoutePath(permissionStore.routers, quickOrderRouteComponents.workbench)
    void router.push({ path, query: { quickOrderId: document.sourceQuickOrderId } })
    return
  }
  if (document.sourceQuoteId) void router.push({ name: 'CustomerQuoteWorkbench', query: { quoteId: document.sourceQuoteId } })
}
function backToList() {
  if (window.history.length > 1) router.back()
  else void router.push('/sales/salesDocuments')
}
function openCheckout() {
  const path = resolveRoutePath(permissionStore.routers, 'pay/order/list', '/sales/payments')
  void router.push({ path, query: { salesDocumentId: id.value } })
}

function money(value?: import('@/types/api').DecimalValue) {
  return formatCurrency(value ?? '0', document.currencyCode || 'USD')
}

onBeforeUnmount(closePreview)
void loadDetail()
</script>

<style scoped>
.sales-document-detail { display: flex; min-height: calc(100vh - 92px); flex-direction: column; gap: 12px; padding: 12px; background: var(--admin-bg); }
.sales-document-detail__topbar,
.sales-document-detail__title,
.sales-document-detail__badges,
.sales-document-detail__actions,
.sales-document-detail__summary { display: flex; align-items: center; gap: 10px; }
.sales-document-detail__topbar { display: grid; grid-template-columns: minmax(0, 1fr) auto auto; align-items: center; gap: 12px; }
.sales-document-detail__title h1 { margin: 0; color: #1d2939; font-size: 24px; }
.sales-document-detail__title p { margin: 4px 0 0; color: #667085; }
.sales-document-detail__actions { flex-wrap: wrap; justify-content: flex-end; }
.sales-document-detail__body { display: grid; grid-template-columns: minmax(0, 1fr); gap: 12px; }
.sales-document-detail__body.has-preview { grid-template-columns: minmax(0, 1.55fr) minmax(420px, 1fr); }
.sales-document-detail__main { display: flex; flex-direction: column; gap: 12px; }
.sales-document-detail__section,
.sales-document-detail__summary { padding: 12px; border: 1px solid #e4eaf2; border-radius: 8px; background: #fff; }
.sales-document-detail__section h3 { margin: 0 0 12px; }
.sales-document-detail__summary { justify-content: flex-end; }
.sales-document-detail__summary div { display: flex; min-width: 160px; align-items: center; justify-content: space-between; color: #667085; }
.sales-document-detail__summary b { color: #1d2939; font-size: 18px; }
.sales-document-detail__summary .is-total b { color: #1677ff; font-size: 22px; }
.sales-document-detail__trail {
  display: grid;
  grid-template-columns: minmax(220px, 0.8fr) minmax(0, 1fr);
  gap: 12px;
}
.sales-document-detail__source-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
  border: 1px solid #e4eaf2;
  border-radius: 8px;
  background: #fff;
}
.sales-document-detail__source-card span { color: #667085; font-size: 12px; font-weight: 600; }
.sales-document-detail__source-card strong { color: #1d2939; }
.sales-document-detail__source-card p { margin: 0; color: #667085; line-height: 20px; }
@media (max-width: 1500px) { .sales-document-detail__topbar { grid-template-columns: minmax(0, 1fr) auto; } }
@media (max-width: 1320px) {
  .sales-document-detail__body.has-preview,
  .sales-document-detail__trail { grid-template-columns: minmax(0, 1fr); }
}
</style>
