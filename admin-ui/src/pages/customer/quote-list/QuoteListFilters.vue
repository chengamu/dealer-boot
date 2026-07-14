<template>
  <el-form-item :label="t('customer.quote.createdTime')">
    <el-date-picker
      :model-value="dateRange"
      type="daterange"
      value-format="YYYY-MM-DD"
      style="width: 240px"
      @update:model-value="$emit('update:dateRange', $event || [])"
    />
  </el-form-item>
  <el-form-item :label="t('customer.quote.no')">
    <el-input v-model="query.quoteNo" clearable style="width: 160px" @keyup.enter="$emit('query')" />
  </el-form-item>
  <template v-if="platform">
    <el-form-item :label="t('dealer.sales.businessOrigin.label')">
      <el-select v-model="query.businessOrigin" clearable style="width: 120px">
        <el-option :label="t('dealer.sales.businessOrigin.MERCHANT')" value="MERCHANT" />
        <el-option :label="t('dealer.sales.businessOrigin.INTERNAL')" value="INTERNAL" />
      </el-select>
    </el-form-item>
    <el-form-item :label="t('dealer.sales.merchant')">
      <el-select v-model="query.tenantId" clearable filterable style="width: 180px">
        <el-option
          v-for="item in merchantOptions"
          :key="item.merchantId"
          :label="item.merchantName || '-'"
          :value="String(item.tenantId || '')"
        />
      </el-select>
    </el-form-item>
    <el-form-item :label="t('dealer.sales.salesStore')">
      <el-select v-model="query.salesStoreId" clearable filterable style="width: 160px">
        <el-option
          v-for="item in salesStoreOptions"
          :key="item.salesStoreId"
          :label="item.storeName || item.storeCode || '-'"
          :value="String(item.salesStoreId || '')"
        />
      </el-select>
    </el-form-item>
  </template>
  <template v-else>
    <el-form-item :label="t('customer.quote.customer')">
      <el-select v-model="query.customerId" clearable filterable style="width: 180px">
        <el-option
          v-for="item in customers"
          :key="item.customerId"
          :label="item.companyName || item.customerName || '-'"
          :value="String(item.customerId || '')"
        />
      </el-select>
    </el-form-item>
  </template>
  <el-form-item :label="t('common.status')">
    <el-select v-model="query.status" clearable style="width: 120px">
      <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
    </el-select>
  </el-form-item>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import type { CustomerProfile } from '@/api/customer/profile'
import type { CustomerQuoteQuery } from '@/api/customer/quote'
import type { MerchantProfileOption } from '@/api/merchant/profile'
import type { SalesStoreOption } from '@/api/dealer-sales'

defineProps<{
  platform: boolean
  query: CustomerQuoteQuery
  dateRange: string[]
  customers: CustomerProfile[]
  merchantOptions: MerchantProfileOption[]
  salesStoreOptions: SalesStoreOption[]
  statusOptions: Array<{ value: string; label: string }>
}>()

defineEmits<{
  (event: 'query'): void
  (event: 'update:dateRange', value: string[]): void
}>()

const { t } = useI18n()
</script>
