<template>
  <section class="fulfillment-section">
    <h3>{{ t('dealer.fulfillment.productWorkspace') }}</h3>
    <el-table :data="items" border row-key="salesItemId">
      <el-table-column type="expand">
        <template #default="{ row }">
          <div class="item-expand"><strong>{{ t('dealer.fulfillment.configuration') }}</strong><p>{{ row.configurationSummary || '-' }}</p></div>
          <el-table v-if="showBom" :data="bomRows(row.bomSnapshotJson)" border size="small" empty-text="-">
            <el-table-column prop="materialCode" :label="t('dealer.fulfillment.materialCode')" min-width="130" />
            <el-table-column prop="materialNameCn" :label="t('dealer.fulfillment.materialName')" min-width="160" />
            <el-table-column prop="attributeGroupNameCn" :label="t('dealer.fulfillment.materialGroup')" min-width="130" />
            <el-table-column prop="unitCode" :label="t('dealer.fulfillment.unit')" width="90" />
            <el-table-column prop="usageQty" :label="t('dealer.fulfillment.usageQty')" width="110" align="right" />
          </el-table>
        </template>
      </el-table-column>
      <el-table-column prop="lineNo" :label="t('common.index')" width="60" align="center" />
      <el-table-column prop="roomLocation" :label="t('dealer.fulfillment.room')" min-width="130" />
      <el-table-column :label="t('dealer.fulfillment.product')" min-width="190"><template #default="{ row }"><strong>{{ row.saleProductName || '-' }}</strong><small>{{ row.saleProductCode || '-' }}</small></template></el-table-column>
      <el-table-column :label="t('dealer.fulfillment.size')" width="140"><template #default="{ row }">{{ itemSize(row.orderWidthInch, row.orderHeightInch) }}</template></el-table-column>
      <el-table-column prop="quantity" :label="t('dealer.fulfillment.quantity')" width="80" align="center" />
      <el-table-column prop="formulaVersionLabel" :label="t('dealer.fulfillment.formulaVersion')" min-width="150" />
      <el-table-column prop="configurationSummary" :label="t('dealer.fulfillment.configuration')" min-width="240" show-overflow-tooltip />
    </el-table>
  </section>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import type { FulfillmentItem } from '@/api/dealer-fulfillment'
import { itemSize } from '../fulfillmentPresentation'

defineProps<{ items: FulfillmentItem[]; showBom: boolean }>()
const { t } = useI18n()
interface BomRow { materialCode?: string; materialNameCn?: string; attributeGroupNameCn?: string; unitCode?: string; usageQty?: number }
function bomRows(value?: string): BomRow[] {
  if (!value) return []
  try {
    const parsed: unknown = JSON.parse(value)
    return Array.isArray(parsed) ? parsed as BomRow[] : []
  } catch { return [] }
}
</script>

<style scoped>
.fulfillment-section { border: 1px solid #e9edf5; background: #fff; }
.fulfillment-section h3 { margin: 0; padding: 9px 12px; border-bottom: 1px solid #eef0f5; font-size: 14px; }
.item-expand { padding: 0 12px 8px; }
.item-expand p { margin: 5px 0; color: #667085; white-space: pre-wrap; }
strong, small { display: block; }
small { margin-top: 2px; color: #98a2b3; }
</style>
