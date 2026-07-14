<template>
  <StandardGridPageShell
    v-model:showSearch="page.showSearch.value"
    :page="Number(page.query.pageNum || 1)"
    :limit="Number(page.query.pageSize || 10)"
    :total="page.total.value"
    @query="page.handleQuery"
    @reset="page.resetQuery"
    @pagination="page.getList"
    @update:page="page.query.pageNum = $event"
    @update:limit="page.query.pageSize = $event"
  >
    <template #filters>
      <QuickOrderListFilters
        :platform="platform"
        :query="page.query"
        :date-range="page.dateRange.value"
        :merchant-options="page.merchantOptions.value"
        :sales-store-options="page.salesStoreOptions.value"
        @query="page.handleQuery"
        @update:date-range="page.dateRange.value = $event"
      />
    </template>
    <template #toolbar>
      <el-button v-if="!platform" v-hasPermi="['dealer:quick-order:add']" type="primary" @click="page.openWorkbench()">
        {{ t('dealer.quickOrder.create') }}
      </el-button>
    </template>

    <QuickOrderListTable
      :platform="platform"
      :loading="page.loading.value"
      :merchant-options="page.merchantOptions.value"
      :rows="page.rows.value"
      :sales-store-options="page.salesStoreOptions.value"
      :build-actions="page.buildActions"
    />
  </StandardGridPageShell>

  <QuickOrderReadonlyDetailDrawer v-model="page.detailOpen.value" :loading="page.detailLoading.value" :order="page.detailOrder.value" />
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import auth from '@/plugins/auth'
import StandardGridPageShell from '@/pages/dealer-sales/components/standard-grid/StandardGridPageShell.vue'
import QuickOrderListFilters from './list/QuickOrderListFilters.vue'
import QuickOrderListTable from './list/QuickOrderListTable.vue'
import QuickOrderReadonlyDetailDrawer from './list/QuickOrderReadonlyDetailDrawer.vue'
import { useQuickOrderListPage } from './list/useQuickOrderListPage'

const { t } = useI18n()
const platform = auth.hasPermi('platform:sales:quick-order:list')
const page = useQuickOrderListPage(platform)
</script>
