<template>
  <div class="sales-document-detail" v-loading="loading">
    <header class="sales-document-detail__topbar">
      <div class="sales-document-detail__title">
        <el-button link icon="ArrowLeft" @click="router.back()" />
        <div>
          <h1>{{ document.orderNo || '-' }}</h1>
          <p>{{ sourceTypeText(t, document.sourceType) }} · {{ document.sourceNo || document.quoteNo || '-' }}</p>
        </div>
        <el-tag :type="document.documentStatus === 'COMPLETED' ? 'success' : document.documentStatus === 'CANCELLED' ? 'info' : 'primary'">
          {{ documentStatusText(t, document.documentStatus) }}
        </el-tag>
      </div>
      <div class="sales-document-detail__actions">
        <el-button v-if="hasSource" @click="viewSource">{{ t('dealer.sales.viewSource') }}</el-button>
        <el-button v-hasPermi="['dealer:sales:document']" @click="togglePreview">{{ previewVisible ? t('dealer.sales.preview.close') : t('dealer.sales.preview.open') }}</el-button>
        <el-button v-hasPermi="['dealer:sales:document']" @click="exportExcel">{{ t('common.export') }}</el-button>
        <el-button v-hasPermi="['dealer:sales:email']" @click="emailVisible = true">{{ t('dealer.sales.email') }}</el-button>
        <el-button v-if="document.documentStatus === 'SUBMITTED'" v-hasPermi="['dealer:sales:cancel']" type="danger" plain @click="cancelOrder">
          {{ t('dealer.sales.cancel') }}
        </el-button>
      </div>
    </header>

    <section class="sales-document-detail__body" :class="{ 'has-preview': previewVisible }">
      <div class="sales-document-detail__main">
        <section class="sales-document-detail__status-strip">
          <el-tag effect="plain">{{ t('dealer.sales.paymentStatus') }}: {{ paymentStatusText(t, document.paymentStatus) }}</el-tag>
          <el-tag effect="plain">{{ t('dealer.sales.productionStatus') }}: {{ productionStatusText(t, document.productionStatus) }}</el-tag>
          <el-tag effect="plain">{{ t('dealer.sales.shipmentStatus') }}: {{ shipmentStatusText(t, document.shipmentStatus) }}</el-tag>
          <span>{{ t('dealer.sales.submittedTime') }}: {{ formatUtc(document.submittedTime, 'YYYY-MM-DD HH:mm') }}</span>
        </section>
        <SalesDocumentFactsCard :document="document" />
        <section class="sales-document-detail__section">
          <h3>{{ t('dealer.sales.items') }}</h3>
          <SalesDocumentItemsTable :items="document.items || []" :currency-code="document.currencyCode" />
        </section>
        <section class="sales-document-detail__summary">
          <div><span>{{ t('dealer.sales.listAmount') }}</span><b>{{ money(document.listAmount) }}</b></div>
          <div><span>{{ t('dealer.sales.discountAmount') }}</span><b>{{ money(document.discountAmount) }}</b></div>
          <div><span>{{ t('dealer.sales.shippingAmount') }}</span><b>{{ money(document.shippingAmount) }}</b></div>
          <div><span>{{ t('dealer.sales.taxAmount') }}</span><b>{{ money(document.taxAmount) }}</b></div>
          <div class="is-total"><span>{{ t('dealer.sales.totalAmount') }}</span><b>{{ money(document.totalAmount) }}</b></div>
        </section>
        <SalesDocumentEventList :events="document.events || []" />
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
import { salesApi, type SalesDocument } from '@/api/dealer-sales'
import { usePermissionStore } from '@/stores/permission'
import { formatUtc } from '@/utils/datetime'
import { download } from '@/utils/request'
import SalesDocumentEventList from './components/SalesDocumentEventList.vue'
import SalesDocumentFactsCard from './components/SalesDocumentFactsCard.vue'
import SalesDocumentItemsTable from './components/SalesDocumentItemsTable.vue'
import SalesDocumentPdfPreview from './components/SalesDocumentPdfPreview.vue'
import { quickOrderRouteComponents, resolveRoutePath } from '@/pages/quick-order/quickOrderRoutes'
import { documentStatusText, paymentStatusText, productionStatusText, shipmentStatusText, sourceTypeText } from './salesPresentation'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const permissionStore = usePermissionStore()
const document = reactive<SalesDocument>({ items: [] })
const loading = ref(false)
const emailVisible = ref(false)
const previewVisible = ref(false)
const pdfUrl = ref('')
const email = reactive({ recipient: '', message: '' })
const id = computed(() => String(route.params.id || ''))
const hasSource = computed(() => Boolean(document.sourceQuoteId || document.sourceQuickOrderId))

async function loadDetail() {
  loading.value = true
  try {
    Object.assign(document, (await salesApi.get(id.value)).data || { items: [] })
    email.recipient = document.customerEmail || ''
  } finally {
    loading.value = false
  }
}

async function togglePreview() {
  if (previewVisible.value) return closePreview()
  const blob = await salesApi.pdf(id.value, 'ORDER')
  closePreview()
  pdfUrl.value = URL.createObjectURL(blob)
  previewVisible.value = true
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
  download(`dealer/sales-documents/${id.value}/export`, {}, `${document.orderNo || document.sourceNo || id.value}.xlsx`)
}

function viewSource() {
  if (document.sourceType === 'QUICK_ORDER' && document.sourceQuickOrderId) {
    const path = resolveRoutePath(permissionStore.routers, quickOrderRouteComponents.workbench)
    void router.push({ path, query: { quickOrderId: document.sourceQuickOrderId } })
    return
  }
  if (document.sourceQuoteId) void router.push({ name: 'CustomerQuoteWorkbench', query: { quoteId: document.sourceQuoteId } })
}

function money(value?: number) {
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: document.currencyCode || 'USD' }).format(value || 0)
}

onBeforeUnmount(closePreview)
void loadDetail()
</script>

<style scoped>
.sales-document-detail { display: flex; min-height: calc(100vh - 92px); flex-direction: column; gap: 12px; padding: 12px; background: #f3f6fa; }
.sales-document-detail__topbar,
.sales-document-detail__title,
.sales-document-detail__actions,
.sales-document-detail__status-strip,
.sales-document-detail__summary { display: flex; align-items: center; gap: 10px; }
.sales-document-detail__topbar { justify-content: space-between; }
.sales-document-detail__title h1 { margin: 0; color: #1d2939; font-size: 24px; }
.sales-document-detail__title p { margin: 4px 0 0; color: #667085; }
.sales-document-detail__actions { flex-wrap: wrap; justify-content: flex-end; }
.sales-document-detail__body { display: grid; grid-template-columns: minmax(0, 1fr); gap: 12px; }
.sales-document-detail__body.has-preview { grid-template-columns: minmax(0, 1.55fr) minmax(420px, 1fr); }
.sales-document-detail__main { display: flex; flex-direction: column; gap: 12px; }
.sales-document-detail__status-strip,
.sales-document-detail__section,
.sales-document-detail__summary { padding: 12px; border: 1px solid #e4eaf2; border-radius: 8px; background: #fff; }
.sales-document-detail__status-strip { flex-wrap: wrap; color: #667085; }
.sales-document-detail__section h3 { margin: 0 0 12px; }
.sales-document-detail__summary { justify-content: flex-end; }
.sales-document-detail__summary div { display: flex; min-width: 160px; align-items: center; justify-content: space-between; color: #667085; }
.sales-document-detail__summary b { color: #1d2939; font-size: 18px; }
.sales-document-detail__summary .is-total b { color: #1677ff; font-size: 22px; }
</style>
