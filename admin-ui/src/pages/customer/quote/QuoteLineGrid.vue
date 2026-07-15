<template>
  <section class="quote-line-grid">
    <header class="quote-line-grid__toolbar">
      <div v-if="!readonly">
        <el-button type="primary" plain icon="Plus" @click="emit('add')">{{ t('customer.quote.addLine') }}</el-button>
        <el-button icon="CopyDocument" :disabled="!selectedRows.length" @click="emit('duplicate-many', selectedRows)">{{ t('customer.quote.action.copySelected') }}</el-button>
        <el-button icon="DocumentCopy" @click="pasteRef?.open()">{{ t('customer.quote.action.batchPaste') }}</el-button>
        <el-button type="danger" plain icon="Delete" :disabled="!selectedRows.length" @click="emit('remove-many', selectedRows)">{{ t('customer.quote.action.deleteSelected') }}</el-button>
      </div>
      <span>{{ rows.length }} {{ t('customer.quote.quantity') }}</span>
    </header>
    <vxe-table
      ref="tableRef"
      :data="rows"
      border
      stripe
      show-overflow
      keep-source
      :row-config="{ isCurrent: true, keyField: 'clientId' }"
      :cell-config="{ height: 80 }"
      :expand-config="{ accordion: true }"
      @checkbox-change="syncSelection"
      @checkbox-all="syncSelection"
      @toggle-row-expand="handleExpand"
    >
      <vxe-column v-if="!readonly" type="checkbox" width="44" fixed="left" />
      <vxe-column type="expand" width="44" fixed="left">
        <template #content="{ row }">
          <QuoteLineEditor
            :row="row"
            :setup="setups[String(row.saleProductId || '')]"
            :language="language"
            :loading="loadingProductId === row.saleProductId"
            :calculating="calculatingId === row.clientId"
            :readonly="readonly"
            @dirty="markDirty(row)"
            @calculate="emit('calculate', row)"
          />
        </template>
      </vxe-column>
      <vxe-column type="seq" :title="t('common.index')" width="58" align="center" fixed="left" />
      <vxe-column field="roomLocation" :title="t('customer.quote.room')" min-width="130">
        <template #default="{ row }">
          <span v-if="readonly">{{ row.roomLocation || '-' }}</span>
          <el-input v-else v-model="row.roomLocation" @change="markDirty(row)" />
        </template>
      </vxe-column>
      <vxe-column field="saleProductId" :title="t('customer.quote.product')" min-width="190">
        <template #default="{ row }">
          <div v-if="readonly" class="quote-line-grid__product">
            <strong>{{ row.saleProductName || '-' }}</strong>
            <span>{{ row.formulaVersionLabel || '-' }}</span>
          </div>
          <el-select v-else v-model="row.saleProductId" filterable @change="changeProduct(row)">
            <el-option v-for="product in saleProducts" :key="product.saleProductId" :label="product.saleProductName" :value="String(product.saleProductId)" />
          </el-select>
        </template>
      </vxe-column>
      <vxe-column :title="t('dealer.sales.size')" width="208" align="right">
        <template #default="{ row }">
          <div class="quote-line-grid__size">
            <label><span>W</span><BusinessVxeInchCell v-model="row.orderWidthInch" :readonly="readonly" min="0.125" @change="markDirty(row)" @validity-change="updateValidity(row, 'width', $event)" /></label>
            <label><span>H</span><BusinessVxeInchCell v-model="row.orderHeightInch" :readonly="readonly" min="0.125" @change="markDirty(row)" @validity-change="updateValidity(row, 'height', $event)" /></label>
          </div>
        </template>
      </vxe-column>
      <vxe-column field="quantity" :title="t('customer.quote.quantity')" width="82" align="center">
        <template #default="{ row }"><BusinessVxeNumberCell v-model="row.quantity" mode="COUNT" integer-value :readonly="readonly" :min="1" :allow-zero="false" @change="markDirty(row)" @validity-change="updateValidity(row, 'quantity', $event)" /></template>
      </vxe-column>
      <vxe-column :title="t('customer.quote.configuration')" min-width="220" show-overflow>
        <template #default="{ row }">
          <div class="quote-line-grid__summary">
            <strong>{{ summary(row) || '-' }}</strong>
            <span v-if="row.remark">{{ row.remark }}</span>
          </div>
        </template>
      </vxe-column>
      <vxe-column :title="t('customer.quote.amount.line')" width="154" align="right">
        <template #default="{ row }">
          <div class="quote-line-grid__result">
            <el-tag :type="calculationType(row.calculationStatus)">{{ calculationText(row.calculationStatus) }}</el-tag>
            <strong>{{ money(row.lineAmount) }}</strong>
            <span>{{ money(row.unitAmount) }} · {{ money(row.shippingAmount) }}</span>
          </div>
        </template>
      </vxe-column>
      <vxe-column v-if="!readonly" :title="t('common.operate')" width="88" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="danger" icon="Delete" @click="emit('remove', row)" />
        </template>
      </vxe-column>
    </vxe-table>
    <QuoteBatchPasteDialog ref="pasteRef" @confirm="emit('paste', $event)" />
  </section>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import type { DecimalValue } from '@/types/api'
import type { QuoteCalculationStatus, QuoteLanguage } from '@/api/customer/quote'
import { formatCurrency } from '@/utils/businessNumber'
import type { SaleProductVO } from '@/api/product-pricing/types'
import type { VxeTableInstance } from 'vxe-table'
import QuoteLineEditor from './QuoteLineEditor.vue'
import QuoteBatchPasteDialog from './QuoteBatchPasteDialog.vue'
import type { QuotePastedRow, QuoteSetupMap, QuoteWorkbenchItem } from './quoteWorkbenchTypes'

const props = defineProps<{
  rows: QuoteWorkbenchItem[]
  saleProducts: SaleProductVO[]
  setups: QuoteSetupMap
  language: QuoteLanguage
  currencyCode?: string
  loadingProductId?: string
  calculatingId?: string
  readonly?: boolean
}>()
const emit = defineEmits<{
  add: []; duplicate: [row: QuoteWorkbenchItem]; remove: [row: QuoteWorkbenchItem]
  'duplicate-many': [rows: QuoteWorkbenchItem[]]; 'remove-many': [rows: QuoteWorkbenchItem[]]; paste: [rows: QuotePastedRow[]]
  loadProduct: [row: QuoteWorkbenchItem]; calculate: [row: QuoteWorkbenchItem]; dirty: [row: QuoteWorkbenchItem]
  'validity-change': [valid: boolean]
}>()
const { t } = useI18n()
const tableRef = ref<VxeTableInstance<QuoteWorkbenchItem>>()
const pasteRef = ref<InstanceType<typeof QuoteBatchPasteDialog>>()
const selectedRows = ref<QuoteWorkbenchItem[]>([])
const invalidCells = ref(new Set<string>())

watch(() => props.rows.map((row) => row.clientId), (clientIds) => {
  const activeIds = new Set(clientIds)
  const next = new Set([...invalidCells.value].filter((key) => activeIds.has(key.split(':')[0])))
  updateInvalidCells(next)
})

function changeProduct(row: QuoteWorkbenchItem) { row.selectedOptionValues = {}; markDirty(row); emit('loadProduct', row) }
function markDirty(row: QuoteWorkbenchItem) { row.calculationStatus = 'PENDING'; row.calculationMessage = ''; emit('dirty', row) }
function updateValidity(row: QuoteWorkbenchItem, field: string, valid: boolean) {
  const next = new Set(invalidCells.value)
  const key = `${row.clientId}:${field}`
  if (valid) next.delete(key)
  else { next.add(key); markDirty(row) }
  updateInvalidCells(next)
}
function updateInvalidCells(next: Set<string>) {
  invalidCells.value = next
  emit('validity-change', next.size === 0)
}
function handleExpand({ row, expanded }: { row: QuoteWorkbenchItem; expanded: boolean }) { if (expanded) emit('loadProduct', row) }
function syncSelection() { selectedRows.value = tableRef.value?.getCheckboxRecords() || [] }
function summary(row: QuoteWorkbenchItem) { return props.language === 'EN_US' ? row.selectedOptionsSummaryEn : row.selectedOptionsSummaryCn }
function money(value?: DecimalValue) { return formatCurrency(value, props.currencyCode || 'USD') }
function calculationText(status?: QuoteCalculationStatus) {
  return status === 'PASS' ? t('customer.quote.calculation.pass') : status === 'FAIL' ? t('customer.quote.calculation.fail') : t('customer.quote.calculation.pending')
}
function calculationType(status?: QuoteCalculationStatus) { return status === 'PASS' ? 'success' : status === 'FAIL' ? 'danger' : 'warning' }
function expand(row: QuoteWorkbenchItem) { void tableRef.value?.setRowExpand(row, true) }
function validate() { return invalidCells.value.size === 0 }
defineExpose({ expand, validate })
</script>

<style scoped>
.quote-line-grid { overflow: hidden; border: 1px solid #dfe6ef; border-radius: 7px; background: #fff; }
.quote-line-grid__toolbar { display: flex; min-height: 52px; align-items: center; justify-content: space-between; padding: 8px 12px; border-bottom: 1px solid #e5eaf1; }
.quote-line-grid__toolbar span { color: #667085; font-size: 13px; }
.quote-line-grid__product,
.quote-line-grid__summary {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 4px;
}
.quote-line-grid__product strong,
.quote-line-grid__summary strong {
  color: #1d2939;
  font-weight: 600;
}
.quote-line-grid__product span,
.quote-line-grid__summary span {
  color: #667085;
  font-size: 12px;
  line-height: 18px;
}
.quote-line-grid__size { display: grid; gap: 5px; }
.quote-line-grid__size label { display: grid; grid-template-columns: 14px minmax(0, 1fr); align-items: center; gap: 5px; }
.quote-line-grid__size label > span,
.quote-line-grid__result span { color: #667085; font-size: 12px; }
.quote-line-grid__result { display: flex; min-width: 0; flex-direction: column; align-items: flex-end; gap: 3px; }
.quote-line-grid__result :deep(.el-tag) { max-width: 100%; }
.quote-line-grid :deep(.vxe-table--render-default .vxe-body--column) { color: #344054; }
.quote-line-grid :deep(.el-select), .quote-line-grid :deep(.el-input-number) { width: 100%; }
.quote-line-grid :deep(.el-input__wrapper), .quote-line-grid :deep(.el-select__wrapper) { min-height: 32px; box-shadow: none; }
.quote-line-grid :deep(.row--current) { background: #eef6ff; }
</style>
