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
      <SalesDocumentListFilters
        :platform="platform"
        :query="page.query"
        :date-range="page.dateRange.value"
        :merchant-options="page.merchantOptions.value"
        :sales-store-options="page.salesStoreOptions.value"
        @query="page.handleQuery"
        @update:date-range="page.dateRange.value = $event"
      />
    </template>

    <SalesDocumentListTable
      :platform="platform"
      :loading="page.loading.value"
      :merchant-options="page.merchantOptions.value"
      :rows="page.rows.value"
      :sales-store-options="page.salesStoreOptions.value"
      :build-actions="page.buildActions"
    />
  </StandardGridPageShell>
</template>

<script setup lang="ts">
import auth from '@/plugins/auth'
import StandardGridPageShell from './components/standard-grid/StandardGridPageShell.vue'
import SalesDocumentListFilters from './list/SalesDocumentListFilters.vue'
import SalesDocumentListTable from './list/SalesDocumentListTable.vue'
import { useSalesDocumentListPage } from './list/useSalesDocumentListPage'

const platform = auth.hasPermi('platform:sales:order:list')
const page = useSalesDocumentListPage(platform)
</script>
