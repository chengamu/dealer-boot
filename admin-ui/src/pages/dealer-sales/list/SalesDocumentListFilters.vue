<template>
  <el-form-item :label="t('dealer.sales.submittedTime')">
    <el-date-picker :model-value="dateRange" type="daterange" value-format="YYYY-MM-DD" style="width: 240px" @update:model-value="$emit('update:dateRange', $event || [])" />
  </el-form-item>
  <el-form-item v-if="platform" :label="t('dealer.sales.businessOrigin.label')">
    <el-select v-model="query.businessOrigin" clearable style="width: 120px">
      <el-option :label="t('dealer.sales.businessOrigin.MERCHANT')" value="MERCHANT" />
      <el-option :label="t('dealer.sales.businessOrigin.INTERNAL')" value="INTERNAL" />
    </el-select>
  </el-form-item>
  <el-form-item v-if="platform" :label="t('dealer.sales.merchant')">
    <el-select v-model="query.tenantId" clearable filterable style="width: 180px">
      <el-option
        v-for="item in merchantOptions"
        :key="item.merchantId"
        :label="item.merchantName || '-'"
        :value="String(item.tenantId || '')"
      />
    </el-select>
  </el-form-item>
  <el-form-item v-if="platform" :label="t('dealer.sales.salesStore')">
    <el-select v-model="query.salesStoreId" clearable filterable style="width: 160px">
      <el-option
        v-for="item in salesStoreOptions"
        :key="item.salesStoreId"
        :label="item.storeName || item.storeCode || '-'"
        :value="String(item.salesStoreId || '')"
      />
    </el-select>
  </el-form-item>
  <el-form-item :label="t('dealer.sales.orderNo')">
    <el-input v-model="query.orderNo" clearable style="width: 160px" @keyup.enter="$emit('query')" />
  </el-form-item>
  <el-form-item :label="t('dealer.sales.sourceTypeLabel')">
    <el-select v-model="query.sourceType" clearable style="width: 130px">
      <el-option :label="t('dealer.sales.sourceType.QUOTE')" value="QUOTE" />
      <el-option :label="t('dealer.sales.sourceType.QUICK_ORDER')" value="QUICK_ORDER" />
    </el-select>
  </el-form-item>
  <el-form-item :label="t('common.status')">
    <el-select v-model="query.documentStatus" clearable style="width: 120px">
      <el-option :label="t('dealer.sales.status.SUBMITTED')" value="SUBMITTED" />
      <el-option :label="t('dealer.sales.status.CANCELLED')" value="CANCELLED" />
      <el-option :label="t('dealer.sales.status.COMPLETED')" value="COMPLETED" />
    </el-select>
  </el-form-item>
  <el-form-item v-if="!platform" :label="t('dealer.sales.customer')">
    <el-input v-model="query.customerName" clearable style="width: 170px" @keyup.enter="$emit('query')" />
  </el-form-item>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import type { MerchantProfileOption } from '@/api/merchant/profile'
import type { SalesQuery, SalesStoreOption } from '@/api/dealer-sales'

defineProps<{
  platform: boolean
  query: SalesQuery
  dateRange: string[]
  merchantOptions: MerchantProfileOption[]
  salesStoreOptions: SalesStoreOption[]
}>()

defineEmits<{
  (event: 'query'): void
  (event: 'update:dateRange', value: string[]): void
}>()

const { t } = useI18n()
</script>
