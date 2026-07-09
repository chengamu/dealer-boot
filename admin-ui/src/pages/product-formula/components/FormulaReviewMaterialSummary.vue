<template>
  <div class="review-material-summary">
    <div class="material-group-strip">
      <div v-for="group in groupCards" :key="group.key" class="material-group-card">
        <span>{{ group.name }}</span>
        <strong>{{ group.count }}</strong>
      </div>
    </div>

    <div class="review-material-table-shell">
      <el-table
        v-loading="loading"
        :data="rows"
        :span-method="spanMethod"
        border
        class="review-material-table"
      >
        <el-table-column type="index" :label="t('common.index')" width="56" align="center" />
        <el-table-column prop="materialCode" :label="t('productCenter.formulaSetup.materialCode')" width="120" show-overflow-tooltip />
        <el-table-column prop="attributeGroupNameCn" :label="t('productCenter.formulaSetup.attributeGroup')" width="92" align="center">
          <template #default="{ row }">
            <div class="group-cell">
              <strong>{{ row.attributeGroupNameCn || row.attributeGroupCode || '-' }}</strong>
              <span>{{ groupCount(row) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="materialTypeNameCn" :label="t('productCenter.formulaSetup.materialType')" width="118" />
        <el-table-column prop="materialNameCn" :label="t('productCenter.formulaSetup.materialName')" min-width="220" />
        <el-table-column prop="specModelText" :label="t('productCenter.formulaSetup.specModel')" min-width="210" />
        <el-table-column :label="t('productCenter.formulaSetup.unit')" width="74" align="center">
          <template #default="{ row }">{{ unitLabel(row.unitCode) }}</template>
        </el-table-column>
        <el-table-column :label="t('productCenter.formulaSetup.usageSummary')" min-width="360">
          <template #default="{ row }">
            <div class="usage-summary-lines">
              <div
                v-for="(line, index) in usageSummaryLines(row)"
                :key="`${row.materialCode || row.lineNo || ''}-${index}`"
                class="usage-summary-line"
                :class="{ 'usage-summary-line--condition': isUsageConditionLine(line) }"
              >
                {{ line }}
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column :label="t('productCenter.formulaSetup.lossRate')" width="92" align="right">
          <template #default="{ row }">{{ formatFormulaReviewPercent(row.lossRate) }}</template>
        </el-table-column>
        <el-table-column :label="t('productCenter.formulaSimulation.unitPrice')" width="96" align="right">
          <template #default="{ row }">{{ formatFormulaReviewMoney(priceSnapshot(row.materialCode)?.unitPrice) }}</template>
        </el-table-column>
        <el-table-column :label="t('productCenter.formulaSimulation.salesPrice')" width="96" align="right">
          <template #default="{ row }">{{ formatFormulaReviewMoney(priceSnapshot(row.materialCode)?.salesPrice) }}</template>
        </el-table-column>
        <el-table-column prop="productionRemark" :label="t('productCenter.formulaSetup.productionRemark')" min-width="180" />
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { TableColumnCtx } from 'element-plus'
import type {
  ProductFormulaMaterialVO,
  ProductFormulaUsageRuleVO,
  ProductUnitVO
} from '@/api/product-capability/types'
import {
  formatFormulaReviewMoney,
  formatFormulaReviewPercent,
  formulaReviewIsUsageConditionLine,
  formulaReviewGroupKey,
  formulaReviewGroupRank,
  formulaReviewUsageSummaryLines,
  type FormulaReviewPriceSnapshotRow
} from '../utils/formulaReviewMaterialSummary'

interface TableSpanParams {
  rowIndex: number
  column: TableColumnCtx<ProductFormulaMaterialVO>
}

const props = defineProps<{
  loading?: boolean
  materials: ProductFormulaMaterialVO[]
  usageRules: ProductFormulaUsageRuleVO[]
  priceSnapshotRows: FormulaReviewPriceSnapshotRow[]
  unitRows: ProductUnitVO[]
  t: (key: string) => string
}>()

const rows = computed(() => [...props.materials].sort((a, b) => {
  const groupDiff = formulaReviewGroupRank(a.attributeGroupCode) - formulaReviewGroupRank(b.attributeGroupCode)
  if (groupDiff !== 0) return groupDiff
  const keyDiff = formulaReviewGroupKey(a).localeCompare(formulaReviewGroupKey(b))
  if (keyDiff !== 0) return keyDiff
  const sortDiff = Number(a.sortOrder || 0) - Number(b.sortOrder || 0)
  if (sortDiff !== 0) return sortDiff
  return String(a.materialCode || '').localeCompare(String(b.materialCode || ''))
}))

const groupCards = computed(() => {
  const map = new Map<string, { key: string; name: string; count: number; rank: number }>()
  rows.value.forEach((row) => {
    const key = row.attributeGroupCode || row.attributeGroupNameCn || '-'
    const item = map.get(key) || {
      key,
      name: row.attributeGroupNameCn || row.attributeGroupCode || '-',
      count: 0,
      rank: formulaReviewGroupRank(row.attributeGroupCode)
    }
    item.count += 1
    map.set(key, item)
  })
  return [...map.values()].sort((a, b) => a.rank - b.rank || a.name.localeCompare(b.name))
})

const groupSpans = computed(() => {
  const spans: number[] = []
  let index = 0
  while (index < rows.value.length) {
    const key = formulaReviewGroupKey(rows.value[index])
    const count = rows.value.filter((row) => formulaReviewGroupKey(row) === key).length
    spans[index] = count
    for (let offset = 1; offset < count; offset += 1) spans[index + offset] = 0
    index += count
  }
  return spans
})

function spanMethod({ rowIndex, column }: TableSpanParams) {
  if (column.property !== 'attributeGroupNameCn') return [1, 1]
  const rowspan = groupSpans.value[rowIndex] || 0
  return rowspan > 0 ? [rowspan, 1] : [0, 0]
}

function groupCount(row: ProductFormulaMaterialVO) {
  const count = groupCards.value.find((group) => group.key === formulaReviewGroupKey(row))?.count || 0
  return count ? `${count}` : ''
}

function priceSnapshot(materialCode?: string) {
  return props.priceSnapshotRows.find((row) => row.materialCode === materialCode)
}

function unitLabel(unitCode?: string) {
  if (!unitCode) return '-'
  return props.unitRows.find((unit) => unit.unitCode === unitCode)?.unitNameCn || unitCode
}

function usageSummaryLines(row: ProductFormulaMaterialVO) {
  return formulaReviewUsageSummaryLines(row, props.usageRules, props.t)
}

function isUsageConditionLine(line: string) {
  return formulaReviewIsUsageConditionLine(line)
}
</script>

<style scoped>
.review-material-summary {
  display: grid;
  gap: 10px;
  min-width: 0;
}

.material-group-strip {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.material-group-card {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  min-width: 104px;
  height: 34px;
  padding: 0 12px;
  border: 1px solid #e9edf5;
  border-radius: 6px;
  background: #fff;
  color: #475467;
  font-size: 13px;
}

.material-group-card strong {
  color: #1d2129;
  font-size: 16px;
}

.review-material-table-shell {
  width: 100%;
  overflow-x: auto;
}

.review-material-table {
  min-width: 1380px;
}

.review-material-table :deep(.el-table__cell) {
  vertical-align: top;
}

.review-material-table :deep(.cell) {
  white-space: normal;
  word-break: break-word;
  line-height: 1.35;
}

.group-cell {
  display: grid;
  gap: 4px;
  justify-items: center;
  color: #344054;
}

.group-cell span {
  color: #98a2b3;
  font-size: 12px;
}

.usage-summary-lines {
  display: grid;
  gap: 4px;
  padding: 2px 0;
  line-height: 1.45;
  white-space: normal;
}

.usage-summary-line {
  color: #344054;
  word-break: break-word;
}

.usage-summary-line--condition {
  display: inline-flex;
  width: fit-content;
  max-width: 100%;
  padding: 1px 6px;
  border-radius: 5px;
  background: #eef5ff;
  color: #1677ff;
  font-weight: 600;
}
</style>
