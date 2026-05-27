<template>
  <div class="app-container merchant-profile-page">
    <el-form ref="queryRef" :model="queryParams" :inline="true" class="toolbar-form">
      <el-form-item :label="t('merchantProfile.merchantName')" prop="merchantName">
        <el-input v-model="queryParams.merchantName" :placeholder="t('merchantProfile.merchantNamePlaceholder')" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item :label="t('merchantProfile.primaryEmail')" prop="primaryEmail">
        <el-input v-model="queryParams.primaryEmail" :placeholder="t('merchantProfile.primaryEmailPlaceholder')" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">{{ t('common.search') }}</el-button>
        <el-button icon="Refresh" @click="resetQuery">{{ t('common.reset') }}</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows">
      <el-table-column :label="t('merchantProfile.merchantName')" prop="merchantName" min-width="180" />
      <el-table-column :label="t('merchantProfile.companyName')" prop="companyName" min-width="180" />
      <el-table-column :label="t('merchantProfile.primaryEmail')" prop="primaryEmail" min-width="220" />
      <el-table-column :label="t('merchantProfile.country')" prop="country" width="120" />
      <el-table-column :label="t('merchantProfile.status')" prop="status" width="110">
        <template #default="{ row }">
          <el-tag :type="row.status === '1' ? 'success' : 'info'">{{ row.status === '1' ? t('common.normal') : t('common.disabled') }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="t('common.createTime')" width="180">
        <template #default="{ row }">{{ formatUtc(row.createTime) }}</template>
      </el-table-column>
      <el-table-column :label="t('common.operate')" width="120" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" icon="View" @click="openDetail(row.merchantId)" v-hasPermi="['system:merchant:profile:query']">
            {{ t('common.detail') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />

    <el-drawer v-model="detailOpen" :title="t('merchantProfile.detailTitle')" size="520px">
      <MerchantProfileDescriptions v-if="detail" :profile="detail" />
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import type { FormInstance } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { getMerchantProfile, listMerchantProfiles, type MerchantProfile, type MerchantProfileQuery } from '@/api/merchant/profile'
import { formatUtc } from '@/utils/datetime'
import MerchantProfileDescriptions from '@/pages/merchant/MerchantProfileDescriptions.vue'

const { t } = useI18n()
const queryRef = ref<FormInstance>()
const rows = ref<MerchantProfile[]>([])
const detail = ref<MerchantProfile>()
const loading = ref(false)
const detailOpen = ref(false)
const total = ref(0)
const queryParams = reactive<MerchantProfileQuery>({ pageNum: 1, pageSize: 10 })

async function getList() {
  loading.value = true
  try {
    const response = await listMerchantProfiles(queryParams)
    rows.value = response.rows || []
    total.value = response.total || 0
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
  handleQuery()
}

async function openDetail(merchantId?: number) {
  if (!merchantId) return
  const response = await getMerchantProfile(merchantId)
  detail.value = response.data
  detailOpen.value = true
}

getList()
</script>

<style scoped>
.toolbar-form {
  padding: 4px 0 12px;
}
</style>
