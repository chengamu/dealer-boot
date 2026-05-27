<template>
  <div class="app-container merchant-audit-page">
    <el-form ref="queryRef" :model="queryParams" :inline="true" class="toolbar-form">
      <el-form-item :label="t('tenant.merchantName')" prop="merchantName">
        <el-input v-model="queryParams.merchantName" :placeholder="t('tenant.merchantNamePlaceholder')" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item :label="t('tenant.email')" prop="email">
        <el-input v-model="queryParams.email" :placeholder="t('tenant.emailPlaceholder')" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item :label="t('tenant.status')" prop="status">
        <el-select v-model="queryParams.status" :placeholder="t('tenant.statusPlaceholder')" clearable>
          <el-option :label="t('tenant.statusPending')" value="PENDING" />
          <el-option :label="t('tenant.statusApproved')" value="APPROVED" />
          <el-option :label="t('tenant.statusRejected')" value="REJECTED" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">{{ t('common.search') }}</el-button>
        <el-button icon="Refresh" @click="resetQuery">{{ t('common.reset') }}</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows">
      <el-table-column :label="t('tenant.merchantName')" prop="merchantName" min-width="180" />
      <el-table-column :label="t('tenant.email')" prop="email" min-width="220" />
      <el-table-column :label="t('tenant.country')" prop="country" min-width="120" />
      <el-table-column :label="t('tenant.status')" prop="status" width="130">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="t('common.createTime')" prop="createTime" width="180">
        <template #default="{ row }">{{ formatUtc(row.createTime) }}</template>
      </el-table-column>
      <el-table-column :label="t('common.operate')" width="230" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" icon="View" @click="openDetail(row.applyId)" v-hasPermi="['system:tenant:application:query']">
            {{ t('common.detail') }}
          </el-button>
          <el-button
            v-if="row.status === 'PENDING'"
            link
            type="success"
            icon="CircleCheck"
            @click="handleApprove(row)"
            v-hasPermi="['system:tenant:application:audit']"
          >
            {{ t('tenant.approve') }}
          </el-button>
          <el-button
            v-if="row.status === 'PENDING'"
            link
            type="danger"
            icon="CircleClose"
            @click="openReject(row)"
            v-hasPermi="['system:tenant:application:audit']"
          >
            {{ t('tenant.reject') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />

    <el-drawer v-model="detailOpen" :title="t('tenant.applicationDetail')" size="520px">
      <el-descriptions v-if="detail" :column="1" border>
        <el-descriptions-item :label="t('tenant.merchantName')">{{ detail.merchantName }}</el-descriptions-item>
        <el-descriptions-item :label="t('tenant.companyName')">{{ detail.companyName || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('tenant.contactName')">{{ detail.contactName || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('tenant.email')">{{ detail.email }}</el-descriptions-item>
        <el-descriptions-item :label="t('tenant.officePhone')">{{ detail.officePhone || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('tenant.mobilePhone')">{{ detail.mobilePhone || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('tenant.country')">{{ detail.country }}</el-descriptions-item>
        <el-descriptions-item :label="t('tenant.address')">{{ formatAddress(detail) }}</el-descriptions-item>
        <el-descriptions-item :label="t('tenant.status')">{{ statusText(detail.status) }}</el-descriptions-item>
        <el-descriptions-item :label="t('tenant.auditBy')">{{ detail.auditBy || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('tenant.auditTime')">{{ formatUtc(detail.auditTime) }}</el-descriptions-item>
        <el-descriptions-item :label="t('tenant.remark')">{{ detail.remark || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('tenant.rejectReason')">{{ detail.rejectReason || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-drawer>

    <el-dialog v-model="rejectOpen" :title="t('tenant.reject')" width="460px" append-to-body>
      <el-input v-model="rejectReason" type="textarea" :rows="4" :placeholder="t('tenant.rejectReasonPlaceholder')" />
      <template #footer>
        <el-button @click="rejectOpen = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleReject">{{ t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { useI18n } from 'vue-i18n'
import {
  approveMerchantApplication,
  getMerchantApplication,
  listMerchantApplications,
  rejectMerchantApplication,
  type MerchantApplication,
  type MerchantApplicationQuery
} from '@/api/merchant/application'
import { formatUtc } from '@/utils/datetime'

const { t } = useI18n()
const queryRef = ref<FormInstance>()
const rows = ref<MerchantApplication[]>([])
const detail = ref<MerchantApplication>()
const loading = ref(false)
const detailOpen = ref(false)
const rejectOpen = ref(false)
const rejectReason = ref('')
const rejectingId = ref<number>()
const total = ref(0)
const queryParams = reactive<MerchantApplicationQuery>({ pageNum: 1, pageSize: 10 })

function statusText(status?: string) {
  if (status === 'APPROVED') return t('tenant.statusApproved')
  if (status === 'REJECTED') return t('tenant.statusRejected')
  return t('tenant.statusPending')
}

function statusType(status?: string) {
  if (status === 'APPROVED') return 'success'
  if (status === 'REJECTED') return 'danger'
  return 'warning'
}

function formatAddress(row: MerchantApplication) {
  return [row.addressLine1, row.addressLine2, row.city, row.state, row.postalCode].filter(Boolean).join(', ') || '-'
}

async function getList() {
  loading.value = true
  try {
    const response = await listMerchantApplications(queryParams)
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

async function openDetail(applyId?: number) {
  if (!applyId) return
  const response = await getMerchantApplication(applyId)
  detail.value = response.data
  detailOpen.value = true
}

async function handleApprove(row: MerchantApplication) {
  if (!row.applyId) return
  try {
    await ElMessageBox.confirm(t('tenant.approveConfirm'), t('common.prompt'), { type: 'warning' })
  } catch {
    return
  }
  await approveMerchantApplication(row.applyId)
  ElMessage.success(t('tenant.approveSuccess'))
  await getList()
}

function openReject(row: MerchantApplication) {
  rejectingId.value = row.applyId
  rejectReason.value = ''
  rejectOpen.value = true
}

async function handleReject() {
  if (!rejectingId.value) return
  await rejectMerchantApplication(rejectingId.value, rejectReason.value)
  ElMessage.success(t('tenant.rejectSuccess'))
  rejectOpen.value = false
  await getList()
}

getList()
</script>

<style scoped>
.merchant-audit-page {
  background: #fff;
}

.toolbar-form {
  padding: 4px 0 12px;
}
</style>
