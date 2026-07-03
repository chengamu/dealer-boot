<template>
  <div class="app-container ai-audit-page">
    <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true">
      <el-form-item :label="t('ai.settings.user')" prop="userId">
        <el-input v-model="queryUserLabel" readonly clearable style="width: 220px" @clear="clearQueryUser">
          <template #append>
            <el-button icon="Search" @click="queryUserSelectorRef?.show()" />
          </template>
        </el-input>
      </el-form-item>
      <el-form-item :label="t('ai.settings.actionType')" prop="actionType">
        <el-input v-model="queryParams.actionType" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item :label="t('ai.settings.toolCode')" prop="toolCode">
        <el-input v-model="queryParams.toolCode" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item :label="t('ai.settings.riskLevel')" prop="riskLevel">
        <el-input v-model="queryParams.riskLevel" clearable style="width: 140px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item :label="t('ai.settings.approvalStatus')" prop="approvalStatus">
        <el-input v-model="queryParams.approvalStatus" clearable style="width: 150px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">{{ t('common.search') }}</el-button>
        <el-button icon="Refresh" @click="resetQuery">{{ t('common.reset') }}</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="dataList" border>
      <el-table-column :label="t('ai.settings.createdTime')" prop="createdTime" width="180">
        <template #default="{ row }">{{ formatUtc(row.createdTime) || '-' }}</template>
      </el-table-column>
      <el-table-column :label="t('ai.settings.user')" min-width="160" show-overflow-tooltip>
        <template #default="{ row }">{{ userLabel(row) }}</template>
      </el-table-column>
      <el-table-column :label="t('ai.settings.actionType')" prop="actionType" min-width="130" />
      <el-table-column :label="t('ai.settings.toolCode')" prop="toolCode" min-width="180" show-overflow-tooltip />
      <el-table-column :label="t('ai.settings.businessTarget')" prop="businessTarget" min-width="180" show-overflow-tooltip />
      <el-table-column :label="t('ai.settings.riskLevel')" prop="riskLevel" width="110" />
      <el-table-column :label="t('ai.settings.approvalStatus')" prop="approvalStatus" width="130" />
      <el-table-column :label="t('common.status')" prop="status" width="110" />
    </el-table>

    <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    <AiUserSelectorDialog ref="queryUserSelectorRef" :multiple="false" :title="t('ai.settings.selectUser')" @confirm="selectQueryUser" />
  </div>
</template>

<script setup lang="ts" name="AiAuditPage">
import { onMounted, reactive, ref } from 'vue'
import { listAiAuditSummaries, type AiAuditQuery, type AiAuditSummary } from '@/api/ai-admin'
import type { SysUser } from '@/api/system/user'
import { formatUtc } from '@/utils/datetime'
import { useAiI18n } from './useAiI18n'
import AiUserSelectorDialog from './components/AiUserSelectorDialog.vue'

const t = useAiI18n()
const loading = ref(false)
const showSearch = ref(true)
const total = ref(0)
const dataList = ref<AiAuditSummary[]>([])
const queryUserLabel = ref('')
const queryRef = ref()
const queryUserSelectorRef = ref<InstanceType<typeof AiUserSelectorDialog>>()
const queryParams = reactive<AiAuditQuery>({
  pageNum: 1,
  pageSize: 10
})

function userLabel(row: AiAuditSummary) {
  const name = row.nickName || row.userName
  return name ? `${name}${row.userName && row.userName !== name ? ` / ${row.userName}` : ''}` : '-'
}

async function getList() {
  loading.value = true
  try {
    const res = await listAiAuditSummaries(queryParams)
    dataList.value = res.rows
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  queryParams.pageNum = 1
  void getList()
}

function resetQuery() {
  queryRef.value?.resetFields()
  clearQueryUser()
  handleQuery()
}

function clearQueryUser() {
  queryUserLabel.value = ''
  queryParams.userId = undefined
  queryParams.tenantId = undefined
}

function selectQueryUser(users: SysUser[]) {
  const user = users[0]
  if (!user) return
  queryParams.userId = Number(user.userId)
  queryParams.tenantId = user.tenantId ? Number(user.tenantId) : undefined
  queryUserLabel.value = user.nickName || user.userName || ''
}

onMounted(() => {
  void getList()
})
</script>
