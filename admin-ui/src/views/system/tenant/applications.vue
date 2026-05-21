<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item :label="t('tenant.merchantName')" prop="merchantName">
        <el-input v-model="queryParams.merchantName" clearable style="width: 220px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item :label="t('tenant.email')" prop="email">
        <el-input v-model="queryParams.email" clearable style="width: 220px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item :label="t('tenant.status')" prop="status">
        <el-select v-model="queryParams.status" clearable style="width: 160px">
          <el-option label="PENDING" value="PENDING" />
          <el-option label="APPROVED" value="APPROVED" />
          <el-option label="REJECTED" value="REJECTED" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">{{ t('common.search') }}</el-button>
        <el-button icon="Refresh" @click="resetQuery">{{ t('common.reset') }}</el-button>
      </el-form-item>
    </el-form>

    <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />

    <el-table v-loading="loading" :data="applicationList">
      <el-table-column :label="t('tenant.merchantName')" prop="merchantName" min-width="180" />
      <el-table-column :label="t('tenant.email')" prop="email" min-width="220" />
      <el-table-column :label="t('tenant.country')" prop="country" width="140" />
      <el-table-column :label="t('tenant.status')" prop="status" width="120" />
      <el-table-column :label="t('common.createTime')" prop="createTime" width="180" />
      <el-table-column :label="t('common.operate')" align="center" width="180" fixed="right">
        <template #default="scope">
          <el-button link type="primary" :disabled="scope.row.status !== 'PENDING'" @click="handleApprove(scope.row)">
            {{ t('tenant.approve') }}
          </el-button>
          <el-button link type="danger" :disabled="scope.row.status !== 'PENDING'" @click="handleReject(scope.row)">
            {{ t('tenant.reject') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <el-dialog v-model="rejectOpen" :title="t('tenant.reject')" width="460px" append-to-body>
      <el-input v-model="rejectForm.rejectReason" type="textarea" :rows="4" :placeholder="t('tenant.rejectReason')" />
      <template #footer>
        <el-button @click="rejectOpen = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="submitReject">{{ t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ElMessage, ElMessageBox } from 'element-plus'
import { approveTenantApplication, listTenantApplications, rejectTenantApplication } from '@/api/system/tenant'
import { useLocale } from '@/locales'

const { proxy } = getCurrentInstance()
const { t } = useLocale()

const loading = ref(false)
const showSearch = ref(true)
const total = ref(0)
const applicationList = ref([])
const rejectOpen = ref(false)
const rejectForm = ref({ applyId: null, rejectReason: '' })

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  merchantName: '',
  email: '',
  status: ''
})

function getList() {
  loading.value = true
  listTenantApplications(queryParams).then(res => {
    applicationList.value = res.rows || []
    total.value = res.total || 0
  }).finally(() => {
    loading.value = false
  })
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm('queryRef')
  handleQuery()
}

function handleApprove(row) {
  ElMessageBox.confirm(t('tenant.approveConfirm'), t('common.prompt'), { type: 'warning' }).then(() => {
    return approveTenantApplication(row.applyId)
  }).then(res => {
    ElMessage.success(`${t('tenant.approveSuccess')}: ${res.data}`)
    getList()
  })
}

function handleReject(row) {
  rejectForm.value = { applyId: row.applyId, rejectReason: '' }
  rejectOpen.value = true
}

function submitReject() {
  rejectTenantApplication(rejectForm.value.applyId, rejectForm.value).then(() => {
    ElMessage.success(t('tenant.rejectSuccess'))
    rejectOpen.value = false
    getList()
  })
}

getList()
</script>
