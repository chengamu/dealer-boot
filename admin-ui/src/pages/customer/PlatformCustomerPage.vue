<template>
  <div class="app-container merchant-table-page">
    <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true" class="merchant-table-page__search">
      <el-form-item :label="t('merchantProfile.merchantName')" prop="merchantId">
        <el-select v-model="queryParams.merchantId" clearable filterable style="width: 190px" @change="handleMerchantChange">
          <el-option v-for="item in merchantOptions" :key="item.merchantId" :label="merchantOptionLabel(item)" :value="item.merchantId" />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('customer.owner')" prop="ownerUserId">
        <el-select v-model="queryParams.ownerUserId" clearable filterable :disabled="!selectedMerchantTenantId" style="width: 120px">
          <el-option v-for="item in ownerOptions" :key="item.userId" :label="item.nickName || item.userName" :value="item.userId" />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('customer.name')" prop="customerName"><el-input v-model="queryParams.customerName" clearable @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item :label="t('customer.email')" prop="email"><el-input v-model="queryParams.email" clearable @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item :label="t('common.status')" prop="status">
        <el-select v-model="queryParams.status" clearable style="width: 100px">
          <el-option :label="t('common.enabled')" value="ENABLED" />
          <el-option :label="t('common.disabled')" value="DISABLED" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">{{ t('common.search') }}</el-button>
        <el-button icon="Refresh" @click="resetQuery">{{ t('common.reset') }}</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8 merchant-table-page__toolbar">
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="rows" border class="merchant-table-page__table">
      <el-table-column type="index" :label="t('common.index')" width="70" align="center" />
      <el-table-column :label="t('customer.ownerSubject')" min-width="160" :show-overflow-tooltip="true">
        <template #default="{ row }">{{ ownerSubjectText(row) }}</template>
      </el-table-column>
      <el-table-column :label="t('customer.name')" prop="customerName" min-width="150" :show-overflow-tooltip="true" />
      <el-table-column :label="t('customer.company')" prop="companyName" min-width="160" :show-overflow-tooltip="true" />
      <el-table-column :label="t('customer.email')" prop="email" min-width="220" :show-overflow-tooltip="true" />
      <el-table-column :label="t('customer.owner')" prop="ownerName" min-width="130" />
      <el-table-column :label="t('common.status')" width="110" align="center">
        <template #default="{ row }"><el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'">{{ row.status === 'ENABLED' ? t('common.enabled') : t('common.disabled') }}</el-tag></template>
      </el-table-column>
      <el-table-column :label="t('common.updateTime')" width="170" align="center">
        <template #default="{ row }">{{ formatUtc(row.updateTime || row.createTime, 'YYYY-MM-DD HH:mm') }}</template>
      </el-table-column>
      <el-table-column :label="t('common.operate')" width="100" fixed="right" align="center">
        <template #default="{ row }">
          <AdminTableActions :actions="[
            { label: t('common.detail'), icon: 'View', permission: 'platform:customer:query', onClick: () => openDetail(row) }
          ]" />
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" class="merchant-table-page__pagination" @pagination="getList" />

    <AdminDrawer v-model="detailOpen" :title="t('customer.detailTitle')" size="560px" variant="detail">
      <el-descriptions v-if="detail" :column="1" border>
        <el-descriptions-item :label="t('customer.ownerSubject')">{{ ownerSubjectText(detail) }}</el-descriptions-item>
        <el-descriptions-item :label="t('customer.name')">{{ detail.customerName }}</el-descriptions-item>
        <el-descriptions-item :label="t('customer.company')">{{ detail.companyName || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('customer.email')">{{ detail.email }}</el-descriptions-item>
        <el-descriptions-item :label="t('customer.phone')">{{ detail.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('customer.owner')">{{ detail.ownerName || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('customer.address')">{{ addressText(detail) }}</el-descriptions-item>
      </el-descriptions>
    </AdminDrawer>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { type FormInstance } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { getPlatformCustomer, listPlatformCustomerOwnerOptions, listPlatformCustomers, type CustomerOwnerOption, type CustomerProfile, type CustomerProfileQuery } from '@/api/customer/profile'
import { listMerchantProfiles, type MerchantProfile } from '@/api/merchant/profile'
import { formatUtc } from '@/utils/datetime'

const { t } = useI18n()
const rows = ref<CustomerProfile[]>([])
const merchantOptions = ref<MerchantProfile[]>([])
const ownerOptions = ref<CustomerOwnerOption[]>([])
const detail = ref<CustomerProfile>()
const loading = ref(false)
const detailOpen = ref(false)
const showSearch = ref(true)
const total = ref(0)
const queryRef = ref<FormInstance>()
const queryParams = reactive<CustomerProfileQuery>({ pageNum: 1, pageSize: 10 })
const selectedMerchant = computed(() => merchantOptions.value.find((item) => String(item.merchantId || '') === String(queryParams.merchantId || '')))
const selectedMerchantTenantId = computed(() => selectedMerchant.value?.tenantId)

async function loadMerchants() {
  const res = await listMerchantProfiles({ pageNum: 1, pageSize: 500 })
  merchantOptions.value = res.rows || []
}
async function loadOwners() {
  const tenantId = selectedMerchantTenantId.value
  if (!tenantId) {
    ownerOptions.value = []
    return
  }
  const res = await listPlatformCustomerOwnerOptions(tenantId)
  ownerOptions.value = res.data || []
}
async function getList() {
  loading.value = true
  try {
    syncMerchantTenant()
    const res = await listPlatformCustomers(queryParams)
    rows.value = res.rows || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}
function handleQuery() {
  queryParams.pageNum = 1
  getList()
}
function resetQuery() {
  queryRef.value?.resetFields()
  queryParams.tenantId = undefined
  ownerOptions.value = []
  handleQuery()
}
function handleMerchantChange() {
  syncMerchantTenant()
  queryParams.ownerUserId = undefined
  loadOwners()
}
async function openDetail(row: CustomerProfile) {
  if (!row.customerId) return
  const res = await getPlatformCustomer(row.customerId)
  detail.value = res.data
  detailOpen.value = true
}
function addressText(row: CustomerProfile) {
  return [row.addressLine1, row.addressLine2, row.city, row.state, row.postalCode, row.country].filter(Boolean).join(', ') || '-'
}
function merchantOptionLabel(item: MerchantProfile) {
  return item.merchantName || item.companyName || '-'
}
function ownerSubjectText(row: CustomerProfile) {
  return row.merchantName || t('customer.platformOwned')
}
function syncMerchantTenant() {
  queryParams.tenantId = selectedMerchantTenantId.value
}
loadMerchants()
getList()
</script>
