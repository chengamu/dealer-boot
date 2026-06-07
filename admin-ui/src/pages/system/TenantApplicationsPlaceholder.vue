<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true" label-width="90px">
      <el-form-item :label="t('tenant.merchantName')" prop="merchantName">
        <el-input
          v-model="queryParams.merchantName"
          :placeholder="t('tenant.merchantNamePlaceholder')"
          clearable
          style="width: 240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item :label="t('tenant.email')" prop="email">
        <el-input v-model="queryParams.email" :placeholder="t('tenant.emailPlaceholder')" clearable style="width: 240px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item :label="t('tenant.status')" prop="status">
        <el-select v-model="queryParams.status" :placeholder="t('tenant.statusPlaceholder')" clearable filterable style="width: 200px">
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

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="CircleCheck"
          :disabled="auditDisabled"
          @click="handleApprove()"
          v-hasPermi="['system:tenant:application:approve']"
        >
          {{ t('tenant.approve') }}
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="CircleClose"
          :disabled="auditDisabled"
          @click="openReject()"
          v-hasPermi="['system:tenant:application:reject']"
        >
          {{ t('tenant.reject') }}
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="rows" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column :label="t('tenant.merchantName')" align="center" prop="merchantName" min-width="180" :show-overflow-tooltip="true" />
      <el-table-column :label="t('tenant.email')" align="center" prop="email" min-width="220" :show-overflow-tooltip="true" />
      <el-table-column :label="t('tenant.country')" align="center" prop="country" min-width="120" :show-overflow-tooltip="true" />
      <el-table-column :label="t('tenant.status')" align="center" prop="status" width="130">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="t('common.createTime')" align="center" prop="createTime" width="180">
        <template #default="{ row }">{{ formatUtc(row.createTime) }}</template>
      </el-table-column>
      <el-table-column :label="t('common.operate')" align="center" width="230" fixed="right" class-name="small-padding fixed-width">
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
            v-hasPermi="['system:tenant:application:approve']"
          >
            {{ t('tenant.approve') }}
          </el-button>
          <el-button
            v-if="row.status === 'PENDING'"
            link
            type="danger"
            icon="CircleClose"
            @click="openReject(row)"
            v-hasPermi="['system:tenant:application:reject']"
          >
            {{ t('tenant.reject') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />

    <el-drawer v-model="detailOpen" :title="t('tenant.applicationDetail')" size="520px" append-to-body>
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
      <template #footer>
        <div class="tenant-application-page__drawer-footer">
          <el-button @click="detailOpen = false">{{ t('common.close') }}</el-button>
          <el-button
            v-if="detail?.status === 'PENDING'"
            type="success"
            icon="CircleCheck"
            @click="handleApprove(detail)"
            v-hasPermi="['system:tenant:application:approve']"
          >
            {{ t('tenant.approve') }}
          </el-button>
          <el-button
            v-if="detail?.status === 'PENDING'"
            type="danger"
            icon="CircleClose"
            @click="openReject(detail)"
            v-hasPermi="['system:tenant:application:reject']"
          >
            {{ t('tenant.reject') }}
          </el-button>
        </div>
      </template>
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
import { computed, reactive, ref } from 'vue'
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
const selectedRows = ref<MerchantApplication[]>([])
const loading = ref(false)
const detailOpen = ref(false)
const rejectOpen = ref(false)
const rejectReason = ref('')
const rejectingId = ref<number>()
const showSearch = ref(true)
const total = ref(0)
const queryParams = reactive<MerchantApplicationQuery>({ pageNum: 1, pageSize: 10 })
const selectedApplication = computed(() => selectedRows.value[0])
const auditDisabled = computed(() => selectedRows.value.length !== 1 || selectedApplication.value?.status !== 'PENDING')

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
    selectedRows.value = []
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

function handleSelectionChange(selection: MerchantApplication[]) {
  selectedRows.value = selection
}

async function openDetail(applyId?: number) {
  if (!applyId) return
  const response = await getMerchantApplication(applyId)
  detail.value = response.data
  detailOpen.value = true
}

async function handleApprove(row?: MerchantApplication) {
  const target = row || selectedApplication.value
  if (!target?.applyId || target.status !== 'PENDING') return
  try {
    await ElMessageBox.confirm(t('tenant.approveConfirm'), t('common.prompt'), { type: 'warning' })
  } catch {
    return
  }
  await approveMerchantApplication(target.applyId)
  ElMessage.success(t('tenant.approveSuccess'))
  await getList()
}

function openReject(row?: MerchantApplication) {
  const target = row || selectedApplication.value
  if (!target?.applyId || target.status !== 'PENDING') return
  rejectingId.value = target.applyId
  rejectReason.value = t('tenant.rejectDefaultReason')
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
.tenant-application-page__drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
