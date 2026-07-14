<template>
  <div class="production-workspace">
    <section class="workspace-card">
      <header class="workspace-card__header">
        <h3>{{ t('dealer.fulfillment.productWorkspace') }}</h3>
      </header>
      <el-table :data="order.items || []" border row-key="salesItemId">
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="production-workspace__expand">
              <div>
                <dt>{{ t('dealer.fulfillment.configuration') }}</dt>
                <dd>{{ row.configurationSummary || '-' }}</dd>
              </div>
              <div>
                <dt>{{ t('dealer.fulfillment.formulaVersion') }}</dt>
                <dd>{{ row.formulaVersionLabel || '-' }}</dd>
              </div>
              <div class="is-wide">
                <dt>{{ t('pay.noConfiguration') }}</dt>
                <dd>{{ optionSummary(row.selectedOptionValues) }}</dd>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="roomLocation" :label="t('dealer.fulfillment.room')" min-width="140" />
        <el-table-column :label="t('dealer.fulfillment.product')" min-width="200">
          <template #default="{ row }">
            <strong>{{ row.saleProductName || '-' }}</strong>
            <small>{{ row.saleProductCode || '-' }}</small>
          </template>
        </el-table-column>
        <el-table-column :label="t('dealer.fulfillment.size')" width="150">
          <template #default="{ row }">{{ itemSize(row.orderWidthInch, row.orderHeightInch) }}</template>
        </el-table-column>
        <el-table-column prop="quantity" :label="t('dealer.fulfillment.quantity')" width="86" align="center" />
        <el-table-column prop="formulaVersionLabel" :label="t('dealer.fulfillment.formulaVersion')" min-width="150" />
        <el-table-column prop="configurationSummary" :label="t('dealer.fulfillment.configuration')" min-width="220" show-overflow-tooltip />
      </el-table>
    </section>

    <aside class="production-workspace__side">
      <section class="workspace-card">
        <header class="workspace-card__header">
          <h3>{{ t('dealer.fulfillment.productionStatus') }}</h3>
        </header>
        <div class="production-workspace__facts">
          <div><dt>{{ t('common.status') }}</dt><dd>{{ statusText(t, 'production', order.productionStatus) }}</dd></div>
          <div><dt>{{ t('dealer.fulfillment.paidTime') }}</dt><dd>{{ minute(order.paidTime) }}</dd></div>
          <div><dt>{{ t('dealer.fulfillment.itemCount') }}</dt><dd>{{ order.itemCount || 0 }}</dd></div>
          <div><dt>{{ t('dealer.fulfillment.totalQuantity') }}</dt><dd>{{ order.totalQuantity || 0 }}</dd></div>
        </div>
        <el-alert
          :title="statusMessage"
          :type="statusTone"
          :closable="false"
          show-icon
        />
        <div class="production-workspace__actions">
          <el-button @click="emit('openSheet')" v-hasPermi="['dealer:fulfillment:production:document']">
            {{ t('dealer.fulfillment.productionSheet') }}
          </el-button>
          <el-button
            v-if="canStart"
            type="primary"
            @click="emit('start')"
            v-hasPermi="['dealer:fulfillment:production:start']"
          >
            {{ t('dealer.fulfillment.startProduction') }}
          </el-button>
          <el-button
            v-if="canComplete"
            type="primary"
            @click="emit('complete')"
            v-hasPermi="['dealer:fulfillment:production:complete']"
          >
            {{ t('dealer.fulfillment.completeProduction') }}
          </el-button>
        </div>
      </section>
    </aside>

    <section class="workspace-card production-workspace__bom">
      <header class="workspace-card__header">
        <h3>{{ t('dealer.fulfillment.formulaVersion') }}</h3>
      </header>
      <el-table :data="bomRows" border>
        <el-table-column prop="materialCode" :label="t('dealer.fulfillment.materialCode')" min-width="140" />
        <el-table-column prop="materialNameCn" :label="t('dealer.fulfillment.materialName')" min-width="180" />
        <el-table-column prop="attributeGroupNameCn" :label="t('dealer.fulfillment.materialGroup')" min-width="140" />
        <el-table-column prop="unitCode" :label="t('dealer.fulfillment.unit')" width="90" />
        <el-table-column prop="usageQty" :label="t('dealer.fulfillment.usageQty')" width="120" align="right" />
      </el-table>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type { DecimalValue } from '@/types/api'
import type { FulfillmentItem, FulfillmentOrder } from '@/api/dealer-fulfillment'
import { formatUtc } from '@/utils/datetime'
import { formatInch } from '@/utils/businessNumber'
import { statusText } from '../fulfillmentPresentation'

interface BomRow {
  materialCode?: string
  materialNameCn?: string
  attributeGroupNameCn?: string
  unitCode?: string
  usageQty?: number
}

const props = defineProps<{ order: FulfillmentOrder; canStart: boolean; canComplete: boolean }>()
const emit = defineEmits<{ openSheet: []; start: []; complete: [] }>()
const { t } = useI18n()

const bomRows = computed(() => {
  const bucket = new Map<string, BomRow>()
  ;(props.order.items || []).forEach((item) => parseBom(item).forEach((row) => {
    const key = row.materialCode || row.materialNameCn || JSON.stringify(row)
    const current = bucket.get(key) || { ...row, usageQty: 0 }
    current.usageQty = Number(current.usageQty || 0) + Number(row.usageQty || 0)
    bucket.set(key, current)
  }))
  return Array.from(bucket.values())
})
const statusMessage = computed(() => {
  if (props.canComplete) return t('dealer.fulfillment.completeProduction')
  if (props.canStart) return t('dealer.fulfillment.startProduction')
  if (props.order.productionStatus === 'COMPLETED') return t('dealer.fulfillment.production.COMPLETED')
  if (missingBom.value) return t('dealer.fulfillment.productionSnapshotIncomplete')
  return t('dealer.fulfillment.production.notReady')
})
const statusTone = computed(() => {
  if (props.order.productionStatus === 'COMPLETED') return 'success'
  if (props.canStart || props.canComplete) return 'info'
  return missingBom.value ? 'warning' : 'info'
})
const missingBom = computed(() => (props.order.items || []).some((item) => !parseBom(item).length))

function parseBom(item: FulfillmentItem) {
  if (!item.bomSnapshotJson) return [] as BomRow[]
  try {
    const parsed: unknown = JSON.parse(item.bomSnapshotJson)
    return Array.isArray(parsed) ? parsed as BomRow[] : []
  } catch {
    return []
  }
}

function optionSummary(values?: Record<string, string>) {
  const entries = Object.entries(values || {})
  return entries.length ? entries.map(([key, value]) => `${key}: ${value}`).join(' / ') : t('pay.noConfiguration')
}

function itemSize(width?: DecimalValue, height?: DecimalValue) {
  return width && height ? `${formatInch(width)} × ${formatInch(height)}` : '-'
}

function minute(value?: string) {
  return value ? formatUtc(value, 'YYYY-MM-DD HH:mm') : '-'
}
</script>

<style scoped>
.production-workspace {
  display: grid;
  grid-template-columns: minmax(0, 1.7fr) minmax(320px, 0.8fr);
  gap: 12px;
}

.workspace-card {
  overflow: hidden;
  border: 1px solid #e9edf5;
  border-radius: 8px;
  background: #fff;
}

.workspace-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid #eef0f5;
}

.workspace-card__header h3 {
  margin: 0;
  font-size: 16px;
}

.production-workspace__expand {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  padding: 8px 12px;
}

.production-workspace__expand .is-wide {
  grid-column: 1 / -1;
}

.production-workspace__expand dt,
.production-workspace__facts dt {
  margin-bottom: 6px;
  color: #98a2b3;
  font-size: 12px;
}

.production-workspace__expand dd,
.production-workspace__facts dd {
  margin: 0;
  color: #1d2129;
  line-height: 1.5;
}

.production-workspace__side {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.production-workspace__facts {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  padding: 16px;
}

.production-workspace__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 16px;
}

.production-workspace__bom {
  grid-column: 1 / -1;
}

small {
  display: block;
  margin-top: 4px;
  color: #98a2b3;
}

strong {
  display: block;
}

@media (max-width: 1200px) {
  .production-workspace {
    grid-template-columns: 1fr;
  }

  .production-workspace__expand,
  .production-workspace__facts {
    grid-template-columns: 1fr;
  }
}
</style>
