<template>
  <div class="app-container tenant-application-page merchant-table-page">
    <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true" label-width="90px" class="merchant-table-page__search">
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

    <el-row :gutter="10" class="mb8 merchant-table-page__toolbar">
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

    <el-table v-loading="loading" :data="rows" border class="merchant-table-page__table" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column :label="t('tenant.merchantName')" prop="merchantName" min-width="180" :show-overflow-tooltip="true" />
      <el-table-column :label="t('tenant.email')" prop="email" min-width="220" :show-overflow-tooltip="true" />
      <el-table-column :label="t('tenant.country')" prop="country" min-width="120" :show-overflow-tooltip="true" />
      <el-table-column :label="t('tenant.status')" align="center" prop="status" width="130">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="t('common.createTime')" align="center" prop="createTime" width="180">
        <template #default="{ row }">{{ formatUtc(row.createTime) }}</template>
      </el-table-column>
      <el-table-column :label="t('common.operate')" align="center" width="150" fixed="right" class-name="small-padding fixed-width">
        <template #default="{ row }">
          <AdminTableActions :actions="[
            { label: t('common.detail'), icon: 'View', permission: 'system:tenant:application:query', primary: true, onClick: () => openDetail(row.applyId) },
            { label: t('tenant.approve'), icon: 'CircleCheck', type: 'success', permission: 'system:tenant:application:approve', hidden: row.status !== 'PENDING', onClick: () => handleApprove(row) },
            { label: t('tenant.reject'), icon: 'CircleClose', type: 'danger', permission: 'system:tenant:application:reject', hidden: row.status !== 'PENDING', onClick: () => openReject(row) }
          ]" />
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" class="merchant-table-page__pagination" @pagination="getList" />

    <AdminDrawer v-model="detailOpen" :title="t('tenant.applicationDetail')" size="520px" variant="detail" append-to-body>
      <div v-if="detail" class="admin-detail">
        <section class="admin-detail__section">
          <h3 class="admin-detail__section-title">{{ t('tenant.applicationDetail') }}</h3>
          <dl class="admin-detail__grid">
            <div class="admin-detail__item"><dt>{{ t('tenant.merchantName') }}</dt><dd>{{ detail.merchantName || '-' }}</dd></div>
            <div class="admin-detail__item"><dt>{{ t('tenant.companyName') }}</dt><dd>{{ detail.companyName || '-' }}</dd></div>
            <div class="admin-detail__item"><dt>{{ t('tenant.contactName') }}</dt><dd>{{ detail.contactName || '-' }}</dd></div>
            <div class="admin-detail__item"><dt>{{ t('tenant.email') }}</dt><dd>{{ detail.email || '-' }}</dd></div>
            <div class="admin-detail__item"><dt>{{ t('tenant.officePhone') }}</dt><dd>{{ detail.officePhone || '-' }}</dd></div>
            <div class="admin-detail__item"><dt>{{ t('tenant.mobilePhone') }}</dt><dd>{{ detail.mobilePhone || '-' }}</dd></div>
            <div class="admin-detail__item"><dt>{{ t('tenant.country') }}</dt><dd>{{ detail.country || '-' }}</dd></div>
            <div class="admin-detail__item"><dt>{{ t('tenant.status') }}</dt><dd>{{ statusText(detail.status) }}</dd></div>
            <div class="admin-detail__item admin-detail__item--full"><dt>{{ t('tenant.address') }}</dt><dd>{{ formatAddress(detail) }}</dd></div>
            <div class="admin-detail__item"><dt>{{ t('tenant.auditBy') }}</dt><dd>{{ detail.auditBy || '-' }}</dd></div>
            <div class="admin-detail__item"><dt>{{ t('tenant.auditTime') }}</dt><dd>{{ formatUtc(detail.auditTime) }}</dd></div>
            <div class="admin-detail__item admin-detail__item--full"><dt>{{ t('tenant.remark') }}</dt><dd class="admin-detail__value--long">{{ detail.remark || '-' }}</dd></div>
            <div class="admin-detail__item admin-detail__item--full"><dt>{{ t('tenant.rejectReason') }}</dt><dd class="admin-detail__value--long">{{ detail.rejectReason || '-' }}</dd></div>
          </dl>
        </section>
      </div>
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
    </AdminDrawer>

    <AdminDialog v-model="rejectOpen" :title="t('tenant.reject')" width="460px" class="admin-reason-dialog" append-to-body :close-on-click-modal="false">
      <div class="admin-reason-dialog__body">
        <el-input v-model="rejectReason" type="textarea" :rows="4" :placeholder="t('tenant.rejectReasonPlaceholder')" />
      </div>
      <template #footer>
        <AdminDialogFooter>
          <el-button @click="rejectOpen = false">{{ t('common.cancel') }}</el-button>
          <el-button type="primary" @click="handleReject">{{ t('common.confirm') }}</el-button>
        </AdminDialogFooter>
      </template>
    </AdminDialog>
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
