<template>
  <div class="quote-workbench" v-loading="loading">
    <header class="quote-workbench__topbar">
      <div class="quote-workbench__title">
        <h1>{{ pageTitle }}</h1>
        <el-tag :type="statusType">{{ statusText }}</el-tag>
      </div>
      <div class="quote-workbench__actions">
        <el-button icon="ArrowLeft" @click="back">{{ t('common.back') }}</el-button>
        <el-button v-if="draft && canSave" icon="DocumentChecked" :loading="saving" @click="save">{{ t('customer.quote.action.save') }}</el-button>
        <el-button v-if="draft && canEdit" icon="Calculator" @click="calculateAll">{{ t('customer.quote.action.calculateAll') }}</el-button>
        <el-button v-if="draft && canEdit" type="primary" icon="CircleCheck" @click="confirm">{{ t('customer.quote.action.confirm') }}</el-button>
        <el-button v-if="quote.status === 'CONFIRMED' && canCopy" icon="CopyDocument" @click="copyDraft">{{ t('customer.quote.action.copy') }}</el-button>
        <el-button v-if="quote.status === 'CONFIRMED'" v-hasPermi="['customer:quote:export']" icon="Download" @click="exportQuote">{{ t('customer.quote.action.export') }}</el-button>
        <el-button v-if="quote.status === 'CONFIRMED' && canDocument" icon="Document" @click="togglePreview">{{ previewVisible ? t('dealer.sales.preview.close') : t('dealer.sales.preview.open') }}</el-button>
        <el-button v-if="quote.status === 'CONFIRMED' && canDocument" icon="Printer" @click="printPreview">{{ t('customer.quote.action.print') }}</el-button>
        <el-button v-if="quote.status === 'CONFIRMED'" v-hasPermi="['customer:quote:email']" icon="Message" @click="emailRef?.open(quote)">{{ t('customer.quote.action.email') }}</el-button>
        <el-button v-if="quote.status === 'CONFIRMED' && !quote.salesDocumentId" v-hasPermi="['customer:quote:convert']" type="primary" icon="Promotion" @click="convertRef?.open(quote)">{{ t('customer.quote.action.convert') }}</el-button>
        <el-button v-if="quote.status === 'CONFIRMED' && quote.salesDocumentId" v-hasPermi="['dealer:sales:query']" icon="Tickets" @click="viewOrder">{{ t('customer.quote.action.viewOrder') }}</el-button>
        <el-button v-if="quote.status === 'CONFIRMED' && !quote.salesDocumentId" v-hasPermi="['customer:quote:edit']" type="danger" plain icon="CircleClose" @click="voidQuote">{{ t('customer.quote.action.void') }}</el-button>
      </div>
    </header>

    <section class="quote-workbench__body" :class="{ 'has-preview': previewVisible }">
      <div class="quote-workbench__main">
        <QuoteHeaderForm ref="headerRef" :quote="quote" :customers="customers" :owners="owners" :readonly="!editable" />
        <QuoteLineGrid
          ref="lineGridRef"
          :rows="rows"
          :sale-products="saleProducts"
          :setups="setups"
          :language="quote.quoteLanguage"
          :currency-code="quote.currencyCode"
          :loading-product-id="loadingProductId"
          :calculating-id="calculatingId"
          :readonly="!editable"
          @add="addAndExpand"
          @remove="removeLine"
          @duplicate-many="duplicateManyAndExpand"
          @remove-many="removeLines"
          @paste="pasteLines"
          @load-product="loadSetup"
          @calculate="calculateItem"
        />
        <QuoteTotalsBar
          :count="rows.length"
          :remark="quote.remark"
          :readonly="!editable"
          :product-amount="quote.productAmount"
          :shipping-amount="quote.shippingAmount"
          :total-amount="quote.totalAmount"
          :currency-code="quote.currencyCode"
          @update:remark="quote.remark = $event"
        />
      </div>
      <QuotePdfPreviewCard
        v-if="previewVisible"
        class="quote-workbench__preview"
        :src="pdfUrl"
        @close="closePreview"
        @print="printPreview"
        @new-window="openPreviewInNewWindow"
      />
    </section>
    <QuoteEmailDialog ref="emailRef" />
    <QuoteConvertDialog ref="convertRef" @converted="handleConverted" />
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import { customerQuoteApi } from '@/api/customer/quote'
import QuoteHeaderForm from './quote/QuoteHeaderForm.vue'
import QuoteLineGrid from './quote/QuoteLineGrid.vue'
import QuoteTotalsBar from './quote/QuoteTotalsBar.vue'
import QuoteEmailDialog from './quote/QuoteEmailDialog.vue'
import QuoteConvertDialog from './quote/QuoteConvertDialog.vue'
import QuotePdfPreviewCard from './quote/QuotePdfPreviewCard.vue'
import type { QuotePastedRow } from './quote/quoteWorkbenchTypes'
import type { QuoteWorkbenchItem } from './quote/quoteWorkbenchTypes'
import { useQuoteWorkbenchData } from './quote/useQuoteWorkbenchData'
import { useQuoteWorkbenchActions } from './quote/useQuoteWorkbenchActions'
import { download } from '@/utils/request'
import { ElMessage } from 'element-plus'
import auth from '@/plugins/auth'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const headerRef = ref<InstanceType<typeof QuoteHeaderForm>>()
const lineGridRef = ref<InstanceType<typeof QuoteLineGrid>>()
const emailRef = ref<InstanceType<typeof QuoteEmailDialog>>()
const convertRef = ref<InstanceType<typeof QuoteConvertDialog>>()
const data = useQuoteWorkbenchData()
const { quote, rows, customers, owners, saleProducts, setups, loading, loadingProductId, loadDependencies, loadQuote, loadSetup, addLine, duplicateLines, removeLine, removeLines, addLines } = data
const draft = computed(() => !quote.status || quote.status === 'DRAFT')
const canEdit = auth.hasPermi('customer:quote:edit')
const canCopy = auth.hasPermi('customer:quote:add')
const canDocument = auth.hasPermi('customer:quote:document')
const canSave = computed(() => quote.quoteId ? canEdit : auth.hasPermi('customer:quote:add'))
const editable = computed(() => draft.value && canSave.value)
const pageHeading = computed(() => quote.status === 'CONFIRMED' ? t('customer.quote.detailTitle') : t('customer.quote.workbenchTitle'))
const pageTitle = computed(() => quote.quoteNo ? `${pageHeading.value} · ${quote.quoteNo}` : pageHeading.value)
const statusText = computed(() => quote.status === 'CONFIRMED'
  ? t('customer.quote.status.confirmed') : quote.status === 'VOID' ? t('customer.quote.status.void') : t('customer.quote.status.draft'))
const statusType = computed(() => quote.status === 'CONFIRMED' ? 'success' : quote.status === 'VOID' ? 'info' : 'warning')
const previewVisible = ref(false)
const pdfUrl = ref('')

async function reload(id: string) {
  if (String(route.query.quoteId || '') !== id) await router.replace({ name: 'CustomerQuoteWorkbench', query: { quoteId: id } })
  await loadQuote(id)
  if (quote.status === 'CONFIRMED' && canDocument) void loadPreview(true)
}
const actions = useQuoteWorkbenchActions({
  quote,
  rows,
  validateHeader: () => headerRef.value?.validate() || Promise.resolve(false),
  validateLines: () => lineGridRef.value?.validate() ?? true,
  reload,
  t
})
const { saving, calculatingId, save, calculateItem, calculateAll, confirm, voidQuote } = actions

async function addAndExpand() { const row = addLine(); await nextTick(); lineGridRef.value?.expand(row) }
async function duplicateManyAndExpand(selected: QuoteWorkbenchItem[]) { const copies = duplicateLines(selected); await nextTick(); if (copies[0]) lineGridRef.value?.expand(copies[0]) }
async function pasteLines(values: QuotePastedRow[]) { const added = addLines(values); await nextTick(); if (added[0]) lineGridRef.value?.expand(added[0]) }
function exportQuote() {
  if (!quote.quoteId) return
  download(`customer/quotes/${quote.quoteId}/export`, {}, `quote_${quote.quoteNo || Date.now()}.xlsx`)
}
async function copyDraft() {
  if (!quote.quoteId) return
  const response = await customerQuoteApi.copy(quote.quoteId)
  ElMessage.success(t('common.operationSuccess'))
  await router.push({ name: 'CustomerQuoteWorkbench', query: { quoteId: response.data } })
}
function back() {
  if (window.history.length > 1) router.back()
  else void router.push('/sales/quotes')
}
function viewOrder() {
  if (quote.salesDocumentId) void router.push({ name: 'SalesDocumentDetail', params: { id: quote.salesDocumentId } })
}
function handleConverted(result: { salesDocumentId: string }) {
  void router.push({ name: 'SalesDocumentDetail', params: { id: result.salesDocumentId } })
}
async function loadPreview(force = false) {
  if (!quote.quoteId) return
  if (pdfUrl.value && !force) {
    previewVisible.value = true
    return
  }
  closePreview()
  const blob = await customerQuoteApi.pdf(quote.quoteId)
  pdfUrl.value = URL.createObjectURL(blob)
  previewVisible.value = true
}
async function togglePreview() {
  if (previewVisible.value) {
    closePreview()
    return
  }
  await loadPreview()
}
function closePreview() {
  previewVisible.value = false
  if (pdfUrl.value) URL.revokeObjectURL(pdfUrl.value)
  pdfUrl.value = ''
}
function openPreviewInNewWindow() {
  if (pdfUrl.value) window.open(pdfUrl.value, '_blank', 'noopener')
}
async function printPreview() {
  if (!previewVisible.value || !pdfUrl.value) await loadPreview()
  if (!pdfUrl.value) return
  const target = window.open(pdfUrl.value, '_blank', 'noopener')
  target?.addEventListener('load', () => target.print(), { once: true })
}

Promise.all([loadDependencies(), loadQuote(String(route.query.quoteId || '') || undefined)]).then(() => {
  if (!rows.value.length && editable.value) void addAndExpand()
  else if (quote.status === 'CONFIRMED' && canDocument) void loadPreview()
})
onBeforeUnmount(closePreview)
</script>

<style scoped>
.quote-workbench { display: flex; min-height: calc(100vh - 92px); flex-direction: column; gap: 12px; padding: 14px 18px 18px; background: var(--admin-bg); }
.quote-workbench__body { display: grid; grid-template-columns: minmax(0, 1fr); gap: 12px; }
.quote-workbench__body.has-preview { grid-template-columns: minmax(0, 1.55fr) minmax(420px, 1fr); }
.quote-workbench__main { display: flex; flex-direction: column; gap: 12px; min-width: 0; }
.quote-workbench__topbar { display: flex; min-height: 44px; align-items: center; justify-content: space-between; gap: 18px; }
.quote-workbench__title, .quote-workbench__actions { display: flex; align-items: center; gap: 10px; }
.quote-workbench__title h1 { margin: 0; color: #1d2939; font-size: 20px; font-weight: 650; letter-spacing: 0; }
.quote-workbench__actions { flex-wrap: wrap; justify-content: flex-end; }
@media (max-width: 1320px) { .quote-workbench__body.has-preview { grid-template-columns: minmax(0, 1fr); } }
</style>
