<template>
  <el-table
    v-loading="loading"
    :data="rows"
    border
    class="system-table-page__table"
    @row-dblclick="handleRowDblclick"
  >
    <el-table-column type="index" :label="t('common.index')" width="70" align="center" :index="indexMethod" />
    <el-table-column :label="t('salesStore.code')" prop="storeCode" min-width="140" show-overflow-tooltip />
    <el-table-column :label="t('salesStore.name')" prop="storeName" min-width="180" show-overflow-tooltip />
    <el-table-column :label="t('salesStore.dept')" prop="deptName" min-width="170" show-overflow-tooltip />
    <el-table-column :label="t('salesStore.contact')" min-width="170" show-overflow-tooltip>
      <template #default="{ row }">
        <div class="sales-store-grid__dual">
          <span>{{ row.contactName || '-' }}</span>
          <small>{{ row.contactPhone || '-' }}</small>
        </div>
      </template>
    </el-table-column>
    <el-table-column :label="t('salesStore.address')" min-width="240" show-overflow-tooltip>
      <template #default="{ row }">{{ row.address || '-' }}</template>
    </el-table-column>
    <el-table-column :label="t('salesStore.currency')" prop="currencyCode" width="100" align="center" />
    <el-table-column :label="t('salesStore.creditLimit')" width="140" align="right">
      <template #default="{ row }">{{ formatMoney(row.creditLimit, 2) }}</template>
    </el-table-column>
    <el-table-column :label="t('salesStore.paymentTermDays')" width="110" align="right">
      <template #default="{ row }">{{ row.paymentTermDays ?? 0 }}</template>
    </el-table-column>
    <el-table-column :label="t('salesStore.status')" width="100" align="center">
      <template #default="{ row }">
        <dict-tag :options="statusOptions" :value="row.status" />
      </template>
    </el-table-column>
    <el-table-column :label="t('salesStore.updatedInfo')" width="180" align="center">
      <template #default="{ row }">
        <div class="sales-store-grid__dual">
          <span>{{ formatUtc(row.updateTime, 'YYYY-MM-DD HH:mm') }}</span>
          <small>{{ row.updateBy || '-' }}</small>
        </div>
      </template>
    </el-table-column>
    <el-table-column v-if="showOperationColumn" :label="t('common.operate')" width="160" fixed="right" align="center">
      <template #default="{ row }">
        <AdminTableActions :actions="buildActions(row)" />
      </template>
    </el-table-column>
  </el-table>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import type { AdminTableAction } from '@/components/AdminTableActions/index.vue'
import type { SalesStore } from '@/api/system/sales-store'
import type { DictOption } from '@/utils/dict'
import { formatUtc } from '@/utils/datetime'
import { formatMoney } from '@/utils/businessNumber'

const props = defineProps<{
  rows: SalesStore[]
  loading: boolean
  pageNum: number
  pageSize: number
  statusOptions: DictOption[]
  showOperationColumn: boolean
  buildActions: (row: SalesStore) => AdminTableAction[]
}>()

const emit = defineEmits<{
  (event: 'row-dblclick', row: SalesStore): void
}>()

const { t } = useI18n()

function indexMethod(index: number) {
  return (Math.max(props.pageNum, 1) - 1) * Math.max(props.pageSize, 1) + index + 1
}

function handleRowDblclick(row: SalesStore) {
  emit('row-dblclick', row)
}
</script>

<style scoped>
.sales-store-grid__dual {
  display: flex;
  flex-direction: column;
  gap: 2px;
  line-height: 1.3;
}

.sales-store-grid__dual small {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
</style>
