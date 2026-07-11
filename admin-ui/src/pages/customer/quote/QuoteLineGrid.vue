<template>
  <section class="quote-line-grid">
    <header class="quote-line-grid__toolbar">
      <div v-if="!readonly">
        <el-button type="primary" plain icon="Plus" @click="emit('add')">{{ t('customer.quote.addLine') }}</el-button>
        <el-button icon="CopyDocument" :disabled="!currentRow" @click="currentRow && emit('duplicate', currentRow)">{{ t('customer.quote.action.copyLine') }}</el-button>
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
      :cell-config="{ height: 44 }"
      :expand-config="{ accordion: true }"
      :max-height="560"
      @current-change="currentRow = $event.row"
      @toggle-row-expand="handleExpand"
    >
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
        <template #default="{ row }"><el-input v-model="row.roomLocation" :disabled="readonly" @change="markDirty(row)" /></template>
      </vxe-column>
      <vxe-column field="saleProductId" :title="t('customer.quote.product')" min-width="190">
        <template #default="{ row }">
          <el-select v-model="row.saleProductId" :disabled="readonly" filterable @change="changeProduct(row)">
            <el-option v-for="product in saleProducts" :key="product.saleProductId" :label="product.saleProductName" :value="String(product.saleProductId)" />
          </el-select>
        </template>
      </vxe-column>
      <vxe-column field="orderWidthInch" :title="t('customer.quote.width')" width="125" align="right">
        <template #default="{ row }"><el-input-number v-model="row.orderWidthInch" :disabled="readonly" :min="0.01" :precision="2" :controls="false" @change="markDirty(row)" /></template>
      </vxe-column>
      <vxe-column field="orderHeightInch" :title="t('customer.quote.height')" width="125" align="right">
        <template #default="{ row }"><el-input-number v-model="row.orderHeightInch" :disabled="readonly" :min="0.01" :precision="2" :controls="false" @change="markDirty(row)" /></template>
      </vxe-column>
      <vxe-column field="quantity" :title="t('customer.quote.quantity')" width="82" align="center">
        <template #default="{ row }"><el-input-number v-model="row.quantity" :disabled="readonly" :min="1" :precision="0" :controls="false" @change="markDirty(row)" /></template>
      </vxe-column>
      <vxe-column :title="t('customer.quote.configuration')" min-width="220" show-overflow>
        <template #default="{ row }">{{ summary(row) || '-' }}</template>
      </vxe-column>
      <vxe-column :title="t('common.status')" width="105" align="center">
        <template #default="{ row }"><el-tag :type="calculationType(row.calculationStatus)">{{ calculationText(row.calculationStatus) }}</el-tag></template>
      </vxe-column>
      <vxe-column :title="t('customer.quote.amount.unit')" width="105" align="right">
        <template #default="{ row }">{{ money(row.unitAmount) }}</template>
      </vxe-column>
      <vxe-column :title="t('customer.quote.amount.shipping')" width="105" align="right">
        <template #default="{ row }">{{ money(row.shippingAmount) }}</template>
      </vxe-column>
      <vxe-column :title="t('customer.quote.amount.line')" width="115" align="right">
        <template #default="{ row }"><strong>{{ money(row.lineAmount) }}</strong></template>
      </vxe-column>
      <vxe-column v-if="!readonly" :title="t('common.operate')" width="88" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="danger" icon="Delete" @click="emit('remove', row)" />
        </template>
      </vxe-column>
    </vxe-table>
  </section>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import type { QuoteCalculationStatus, QuoteLanguage } from '@/api/customer/quote'
import type { SaleProductVO } from '@/api/product-pricing/types'
import type { VxeTableInstance } from 'vxe-table'
import QuoteLineEditor from './QuoteLineEditor.vue'
import type { QuoteSetupMap, QuoteWorkbenchItem } from './quoteWorkbenchTypes'

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
  loadProduct: [row: QuoteWorkbenchItem]; calculate: [row: QuoteWorkbenchItem]; dirty: [row: QuoteWorkbenchItem]
}>()
const { t } = useI18n()
const tableRef = ref<VxeTableInstance<QuoteWorkbenchItem>>()
const currentRow = ref<QuoteWorkbenchItem>()

function changeProduct(row: QuoteWorkbenchItem) { row.selectedOptionValues = {}; markDirty(row); emit('loadProduct', row) }
function markDirty(row: QuoteWorkbenchItem) { row.calculationStatus = 'PENDING'; row.calculationMessage = ''; emit('dirty', row) }
function handleExpand({ row, expanded }: { row: QuoteWorkbenchItem; expanded: boolean }) { if (expanded) emit('loadProduct', row) }
function summary(row: QuoteWorkbenchItem) { return props.language === 'EN_US' ? row.selectedOptionsSummaryEn : row.selectedOptionsSummaryCn }
function money(value?: number) { return value == null ? '-' : new Intl.NumberFormat('en-US', { style: 'currency', currency: props.currencyCode || 'USD' }).format(value) }
function calculationText(status?: QuoteCalculationStatus) {
  return status === 'PASS' ? t('customer.quote.calculation.pass') : status === 'FAIL' ? t('customer.quote.calculation.fail') : t('customer.quote.calculation.pending')
}
function calculationType(status?: QuoteCalculationStatus) { return status === 'PASS' ? 'success' : status === 'FAIL' ? 'danger' : 'warning' }
function expand(row: QuoteWorkbenchItem) { void tableRef.value?.setRowExpand(row, true) }
defineExpose({ expand })
</script>

<style scoped>
.quote-line-grid { overflow: hidden; border: 1px solid #dfe6ef; border-radius: 7px; background: #fff; }
.quote-line-grid__toolbar { display: flex; min-height: 52px; align-items: center; justify-content: space-between; padding: 8px 12px; border-bottom: 1px solid #e5eaf1; }
.quote-line-grid__toolbar span { color: #667085; font-size: 13px; }
.quote-line-grid :deep(.vxe-table--render-default .vxe-body--column) { color: #344054; }
.quote-line-grid :deep(.el-select), .quote-line-grid :deep(.el-input-number) { width: 100%; }
.quote-line-grid :deep(.el-input__wrapper), .quote-line-grid :deep(.el-select__wrapper) { min-height: 32px; box-shadow: none; }
.quote-line-grid :deep(.row--current) { background: #eef6ff; }
</style>
