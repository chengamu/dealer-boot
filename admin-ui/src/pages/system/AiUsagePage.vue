<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true">
      <el-form-item :label="t('ai.settings.user')" prop="userId">
        <el-input v-model="queryUserLabel" readonly clearable style="width: 220px" @clear="clearQueryUser">
          <template #append>
            <el-button icon="Search" @click="queryUserSelectorRef?.show()" />
          </template>
        </el-input>
      </el-form-item>
      <el-form-item :label="t('ai.settings.channel')" prop="provider">
        <el-select v-model="queryParams.provider" clearable filterable style="width: 200px">
          <el-option v-for="item in providers" :key="item.providerCode" :label="providerLabel(item)" :value="item.providerCode || ''" />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('ai.settings.modelName')" prop="model">
        <el-input v-model="queryParams.model" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item :label="t('common.status')" prop="status">
        <el-input v-model="queryParams.status" clearable style="width: 140px" @keyup.enter="handleQuery" />
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
      <el-table-column :label="t('ai.settings.channel')" prop="provider" min-width="130" show-overflow-tooltip />
      <el-table-column :label="t('ai.settings.modelName')" prop="model" min-width="180" show-overflow-tooltip />
      <el-table-column :label="t('ai.settings.inputTokens')" prop="inputTokens" width="120" />
      <el-table-column :label="t('ai.settings.outputTokens')" prop="outputTokens" width="120" />
      <el-table-column :label="t('ai.settings.costAmount')" prop="costAmount" width="120" />
      <el-table-column :label="t('ai.settings.latencyMs')" prop="latencyMs" width="120" />
      <el-table-column :label="t('common.status')" prop="status" width="110" />
    </el-table>

    <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    <AiUserSelectorDialog ref="queryUserSelectorRef" :multiple="false" :title="t('ai.settings.selectUser')" @confirm="selectQueryUser" />
  </div>
</template>

<script setup lang="ts" name="AiUsagePage">
import { onMounted, reactive, ref } from 'vue'
import { listAiProviderOptions, listAiUsageLedgers, type AiProviderConfig, type AiUsageLedger, type AiUsageQuery } from '@/api/ai-admin'
import type { SysUser } from '@/api/system/user'
import { formatUtc } from '@/utils/datetime'
import { useAiI18n } from './useAiI18n'
import AiUserSelectorDialog from './components/AiUserSelectorDialog.vue'

const t = useAiI18n()
const loading = ref(false)
const showSearch = ref(true)
const total = ref(0)
const dataList = ref<AiUsageLedger[]>([])
const providers = ref<AiProviderConfig[]>([])
const queryUserLabel = ref('')
const queryRef = ref()
const queryUserSelectorRef = ref<InstanceType<typeof AiUserSelectorDialog>>()
const queryParams = reactive<AiUsageQuery>({
  pageNum: 1,
  pageSize: 10
})

function providerLabel(provider: AiProviderConfig) {
  return `${provider.providerName || provider.providerCode} / ${provider.providerCode}`
}

function userLabel(row: AiUsageLedger) {
  const name = row.nickName || row.userName
  return name ? `${name}${row.userName && row.userName !== name ? ` / ${row.userName}` : ''}` : '-'
}

async function getProviderOptions() {
  providers.value = await listAiProviderOptions()
}

async function getList() {
  loading.value = true
  try {
    const res = await listAiUsageLedgers(queryParams)
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

onMounted(async () => {
  await getProviderOptions()
  await getList()
})
</script>
