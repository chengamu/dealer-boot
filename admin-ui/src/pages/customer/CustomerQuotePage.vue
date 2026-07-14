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
      <QuoteListFilters
        :platform="platform"
        :query="page.query"
        :date-range="page.dateRange.value"
        :customers="page.customers.value"
        :merchant-options="page.merchantOptions.value"
        :sales-store-options="page.salesStoreOptions.value"
        :status-options="page.statusOptions.value"
        @query="page.handleQuery"
        @update:date-range="page.dateRange.value = $event"
      />
    </template>
    <template #toolbar>
      <el-button v-if="!platform" type="primary" icon="Plus" v-hasPermi="['customer:quote:add']" @click="page.openWorkbench()">
        {{ t('common.add') }}
      </el-button>
    </template>

    <QuoteListTable
      :platform="platform"
      :loading="page.loading.value"
      :merchant-options="page.merchantOptions.value"
      :rows="page.rows.value"
      :sales-store-options="page.salesStoreOptions.value"
      :status-options="page.statusOptions.value"
      :build-actions="page.buildActions"
    />
  </StandardGridPageShell>

  <QuoteReadonlyDetailDrawer
    v-model="page.detailOpen.value"
    :loading="page.detailLoading.value"
    :quote="page.detailQuote.value"
    :status-options="page.statusOptions.value"
  />
  <QuoteEmailDialog ref="emailRef" />
  <QuoteConvertDialog ref="convertRef" @converted="handleConverted" />
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import type { CustomerQuote } from '@/api/customer/quote'
import auth from '@/plugins/auth'
import StandardGridPageShell from '@/pages/dealer-sales/components/standard-grid/StandardGridPageShell.vue'
import QuoteEmailDialog from './quote/QuoteEmailDialog.vue'
import QuoteConvertDialog from './quote/QuoteConvertDialog.vue'
import QuoteListFilters from './quote-list/QuoteListFilters.vue'
import QuoteListTable from './quote-list/QuoteListTable.vue'
import QuoteReadonlyDetailDrawer from './quote-list/QuoteReadonlyDetailDrawer.vue'
import { useQuoteListPage } from './quote-list/useQuoteListPage'

const { t } = useI18n()
const router = useRouter()
const platform = auth.hasPermi('platform:sales:quote:list')
const emailRef = ref<InstanceType<typeof QuoteEmailDialog>>()
const convertRef = ref<InstanceType<typeof QuoteConvertDialog>>()
const page = useQuoteListPage(platform)

page.bindBusinessDialogs({
  email: (row: CustomerQuote) => emailRef.value?.open(row),
  convert: (row: CustomerQuote) => convertRef.value?.open(row)
})

function handleConverted(result: { salesDocumentId: string }) {
  void router.push({ name: 'SalesDocumentDetail', params: { id: result.salesDocumentId } })
}
</script>
