<template>
  <div class="app-container ai-table-page">
    <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true" class="ai-table-page__search">
      <el-form-item :label="t('ai.settings.user')" prop="userId">
        <el-input v-model="queryUserLabel" readonly clearable style="width: 220px" @clear="clearQueryUser">
          <template #append>
            <el-button icon="Search" @click="queryUserSelectorRef?.show()" />
          </template>
        </el-input>
      </el-form-item>
      <el-form-item :label="t('common.status')" prop="status">
        <el-select v-model="queryParams.status" clearable style="width: 140px">
          <el-option :label="t('common.normal')" value="1" />
          <el-option :label="t('common.disabled')" value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">{{ t('common.search') }}</el-button>
        <el-button icon="Refresh" @click="resetQuery">{{ t('common.reset') }}</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8 ai-table-page__toolbar">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="openAddUserSelector" v-hasPermi="['ai:quota:add']">
          {{ t('ai.settings.addUserQuota') }}
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="dataList" border class="ai-table-page__table">
      <el-table-column type="index" :label="t('common.index')" width="70" align="center" />
      <el-table-column :label="t('ai.settings.user')" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">{{ userLabel(row) }}</template>
      </el-table-column>
      <el-table-column :label="t('ai.settings.dailyRequestLimit')" prop="dailyRequestLimit" min-width="150" align="center" />
      <el-table-column :label="t('ai.settings.dailyTokenLimit')" prop="dailyTokenLimit" min-width="150" align="center" />
      <el-table-column :label="t('ai.settings.dailyCostLimit')" prop="dailyCostLimit" min-width="150" align="center">
        <template #default="{ row }">{{ formatAmount(row.dailyCostLimit) }}</template>
      </el-table-column>
      <el-table-column :label="t('common.status')" prop="status" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === '1' ? 'success' : 'info'">{{ row.status === '1' ? t('common.normal') : t('common.disabled') }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="t('common.updateTime')" prop="updateTime" width="180" align="center">
        <template #default="{ row }">{{ formatUtc(row.updateTime || row.createTime) || '-' }}</template>
      </el-table-column>
      <el-table-column :label="t('common.operate')" width="120" align="center" fixed="right">
        <template #default="{ row }">
          <AdminTableActions :actions="quotaActions(row)" />
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" class="ai-table-page__pagination" @pagination="getList" />

    <AdminDrawer v-model="drawerOpen" :title="quotaForm.quotaId ? t('ai.settings.editQuota') : t('ai.settings.addUserQuota')" size="620px" append-to-body destroy-on-close>
      <el-form ref="quotaRef" :model="quotaForm" :rules="rules" label-width="148px">
        <el-form-item :label="t('ai.settings.user')">
          <div class="ai-selected-users">
            <el-tag v-for="user in selectedUsers" :key="user.userId" class="ai-selected-users__tag">
              {{ displayUser(user) }}
            </el-tag>
          </div>
        </el-form-item>
        <el-form-item :label="t('ai.settings.dailyRequestLimit')" prop="dailyRequestLimit">
          <el-input-number v-model="quotaForm.dailyRequestLimit" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item :label="t('ai.settings.dailyTokenLimit')" prop="dailyTokenLimit">
          <el-input-number v-model="quotaForm.dailyTokenLimit" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item :label="t('ai.settings.dailyCostLimit')" prop="dailyCostLimit">
          <el-input-number v-model="quotaForm.dailyCostLimit" :min="0" :precision="2" :step="0.01" controls-position="right" />
        </el-form-item>
        <el-form-item :label="t('common.status')" prop="status">
          <el-radio-group v-model="quotaForm.status">
            <el-radio value="1">{{ t('common.normal') }}</el-radio>
            <el-radio value="0">{{ t('common.disabled') }}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="admin-drawer__footer-actions">
          <el-button @click="drawerOpen = false">{{ t('common.cancel') }}</el-button>
          <el-button type="primary" @click="handleSaveQuota">{{ t('common.save') }}</el-button>
        </div>
      </template>
    </AdminDrawer>

    <AiUserSelectorDialog ref="addUserSelectorRef" :title="t('ai.settings.selectQuotaUsers')" :exclude-user-keys="quotaUserKeys" @confirm="openAddQuotaDrawer" />
    <AiUserSelectorDialog ref="queryUserSelectorRef" :multiple="false" :title="t('ai.settings.selectUser')" @confirm="selectQueryUser" />
  </div>
</template>

<script setup lang="ts" name="AiQuotaPage">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import AdminDrawer from '@/components/AdminDrawer/index.vue'
import AdminTableActions, { type AdminTableAction } from '@/components/AdminTableActions/index.vue'
import { addAiUserQuota, listAiUserQuotas, updateAiUserQuota, type AiTenantUserQuery, type AiUserQuota } from '@/api/ai-admin'
import type { SysUser } from '@/api/system/user'
import { formatUtc } from '@/utils/datetime'
import { useAiI18n } from './useAiI18n'
import AiUserSelectorDialog from './components/AiUserSelectorDialog.vue'

const t = useAiI18n()
const loading = ref(false)
const showSearch = ref(true)
const total = ref(0)
const dataList = ref<AiUserQuota[]>([])
const selectedUsers = ref<SysUser[]>([])
const queryUserLabel = ref('')
const queryRef = ref<FormInstance>()
const quotaRef = ref<FormInstance>()
const drawerOpen = ref(false)
const addUserSelectorRef = ref<InstanceType<typeof AiUserSelectorDialog>>()
const queryUserSelectorRef = ref<InstanceType<typeof AiUserSelectorDialog>>()
const quotaUserKeys = ref<string[]>([])

const DEFAULT_DAILY_REQUEST_LIMIT = 500
const DEFAULT_DAILY_TOKEN_LIMIT = 10000000
const DEFAULT_DAILY_COST_LIMIT = 5

const queryParams = reactive<AiTenantUserQuery>({
  pageNum: 1,
  pageSize: 10
})
const quotaForm = reactive<AiUserQuota>(emptyQuota())

const rules = computed<FormRules>(() => ({}))

function emptyQuota(): AiUserQuota {
  return {
    dailyRequestLimit: DEFAULT_DAILY_REQUEST_LIMIT,
    dailyTokenLimit: DEFAULT_DAILY_TOKEN_LIMIT,
    dailyCostLimit: DEFAULT_DAILY_COST_LIMIT,
    status: '1'
  }
}

function userLabel(row: AiUserQuota) {
  const name = row.nickName || row.userName
  return name ? `${name}${row.userName && row.userName !== name ? ` / ${row.userName}` : ''}` : '-'
}

function displayUser(user: SysUser | AiUserQuota) {
  const name = user.nickName || user.userName
  return name ? `${name}${user.userName && user.userName !== name ? ` / ${user.userName}` : ''}` : '-'
}

function formatAmount(value?: number) {
  return value == null ? '-' : Number(value).toFixed(2)
}

async function getList() {
  loading.value = true
  try {
    const res = await listAiUserQuotas(queryParams)
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
  queryUserLabel.value = displayUser(user)
}

async function openAddUserSelector() {
  await refreshQuotaUserKeys()
  addUserSelectorRef.value?.show()
}

async function refreshQuotaUserKeys() {
  const res = await listAiUserQuotas({
    pageNum: 1,
    pageSize: 10000
  })
  quotaUserKeys.value = (res.rows || []).map((quota) => `${quota.tenantId || ''}:${quota.userId || ''}`)
}

function openAddQuotaDrawer(users: SysUser[]) {
  selectedUsers.value = users
  Object.assign(quotaForm, emptyQuota(), {
    quotaId: undefined,
    tenantId: users[0]?.tenantId,
    userId: users[0]?.userId
  })
  drawerOpen.value = true
}

function openEditQuotaDrawer(row: AiUserQuota) {
  selectedUsers.value = [{
    tenantId: row.tenantId,
    userId: row.userId,
    userName: row.userName,
    nickName: row.nickName
  }]
  Object.assign(quotaForm, emptyQuota(), row)
  drawerOpen.value = true
}

function quotaActions(row: AiUserQuota): AdminTableAction[] {
  return [
    {
      label: t('common.edit'),
      icon: 'Edit',
      primary: true,
      permission: 'ai:quota:edit',
      onClick: () => openEditQuotaDrawer(row)
    }
  ]
}

async function handleSaveQuota() {
  const valid = await quotaRef.value?.validate().catch(() => false)
  if (!valid) return
  if (quotaForm.quotaId) {
    await updateAiUserQuota(quotaForm)
  } else {
    for (const user of selectedUsers.value) {
      await addAiUserQuota({
        ...quotaForm,
        tenantId: user.tenantId ? Number(user.tenantId) : quotaForm.tenantId,
        userId: user.userId ? Number(user.userId) : quotaForm.userId
      })
    }
  }
  ElMessage.success(t('common.success'))
  drawerOpen.value = false
  await getList()
}

onMounted(() => {
  void getList()
})
</script>

<style scoped lang="scss">
@use './styles/ai-table-page';

.ai-selected-users {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  min-height: 32px;
  align-items: center;
}

.ai-selected-users__tag {
  max-width: 220px;
}
</style>
