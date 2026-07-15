<template>
  <section class="operations-merchant-table">
    <el-table v-loading="loading" :data="rows" highlight-current-row class="operations-merchant-table__grid" @row-click="emit('select', $event)">
      <el-table-column :label="t('dashboard.operations.table.merchant')" min-width="210">
        <template #default="{ row }"><div class="merchant-cell"><strong>{{ row.companyName || row.merchantName || '-' }}</strong><span>{{ address(row) }}</span></div></template>
      </el-table-column>
      <el-table-column :label="t('dashboard.operations.table.contact')" min-width="170">
        <template #default="{ row }"><div class="contact-cell"><span>{{ row.contactName || '-' }}</span><small>{{ row.primaryEmail || '-' }}</small></div></template>
      </el-table-column>
      <el-table-column :label="t('dashboard.operations.table.audit')" width="102" align="center">
        <template #default="{ row }"><el-tag size="small" :type="auditType(row.auditStatus)">{{ auditLabel(row.auditStatus) }}</el-tag></template>
      </el-table-column>
      <el-table-column :label="t('dashboard.operations.table.status')" width="96" align="center">
        <template #default="{ row }"><el-tag v-if="row.status" size="small" :type="row.status === '1' ? 'success' : 'danger'">{{ merchantStatusText(row.status, t) }}</el-tag><span v-else>-</span></template>
      </el-table-column>
      <el-table-column :label="t('dashboard.operations.table.level')" prop="levelName" width="92" align="center"><template #default="{ row }">{{ row.levelName || '-' }}</template></el-table-column>
      <el-table-column :label="t('dashboard.operations.table.discount')" width="92" align="right"><template #default="{ row }">{{ formatDiscountRate(row.discountRate) }}</template></el-table-column>
      <el-table-column :label="t('dashboard.operations.table.orders')" width="76" align="right"><template #default="{ row }">{{ row.orderCount || 0 }}</template></el-table-column>
      <el-table-column :label="t('dashboard.operations.table.auditTime')" width="172" align="center"><template #default="{ row }">{{ formatUtc(row.auditTime || row.createTime) }}</template></el-table-column>
    </el-table>
    <pagination v-show="total > 0" :page="page" :limit="pageSize" :total="total" layout="total, prev, pager, next" class="operations-merchant-table__pagination" @pagination="emit('pagination', $event)" />
  </section>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import type { OperationsMerchant } from '@/api/platform-operations'
import { formatDiscountRate, merchantStatusText } from '@/api/merchant/format'
import { formatUtc } from '@/utils/datetime'

defineProps<{ rows: OperationsMerchant[]; loading: boolean; total: number; page: number; pageSize: number }>()
const emit = defineEmits<{ select: [row: OperationsMerchant]; pagination: [payload: { page: number; limit: number }] }>()
const { t } = useI18n()

function auditType(status?: string) { return status === 'APPROVED' ? 'success' : status === 'REJECTED' ? 'danger' : 'warning' }
function auditLabel(status?: string) { return status === 'APPROVED' ? t('tenant.statusApproved') : status === 'REJECTED' ? t('tenant.statusRejected') : t('tenant.statusPending') }
function address(row: OperationsMerchant) { return [row.country, row.state, row.city, row.addressLine1].filter(Boolean).join(' · ') || '-' }
</script>

<style scoped>
.operations-merchant-table { min-width: 0; overflow: hidden; border: 1px solid #e7ecf4; border-radius: 12px; background: #fff; }
.operations-merchant-table__grid { width: 100%; }
.merchant-cell,.contact-cell { display: grid; min-width: 0; gap: 3px; }
.merchant-cell strong { overflow: hidden; color: #1677ff; font-size: 14px; text-overflow: ellipsis; white-space: nowrap; }
.merchant-cell span,.contact-cell small { overflow: hidden; color: #98a2b3; font-size: 12px; text-overflow: ellipsis; white-space: nowrap; }
.contact-cell span { color: #344054; font-weight: 600; }
.operations-merchant-table__pagination { margin: 0; padding: 12px 14px; border-top: 1px solid #edf0f5; }
</style>
