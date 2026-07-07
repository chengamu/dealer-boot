<template>
  <div class="app-container merchant-user-page merchant-table-page">
    <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true" label-width="96px" class="merchant-table-page__search">
      <el-form-item v-if="isPlatformMode" :label="t('merchantProfile.merchantName')" prop="tenantId">
        <el-select v-model="selectedTenantId" clearable filterable :placeholder="t('merchantUser.allMerchants')" style="width: 240px" @change="handleMerchantChange">
          <el-option v-for="item in merchantOptions" :key="item.tenantId" :label="merchantOptionLabel(item)" :value="item.tenantId" />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('user.userName')" prop="userName">
        <el-input v-model="queryParams.userName" :placeholder="t('user.userNamePlaceholder')" clearable style="width: 240px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item :label="t('user.phonenumber')" prop="phonenumber">
        <el-input v-model="queryParams.phonenumber" :placeholder="t('user.phonenumberPlaceholder')" clearable style="width: 240px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item :label="t('user.status')" prop="status">
        <el-select v-model="queryParams.status" :placeholder="t('user.statusPlaceholder')" clearable style="width: 240px">
          <el-option v-for="dict in sys_normal_disable" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('common.createTime')" style="width: 330px">
        <el-date-picker
          v-model="dateRange"
          value-format="YYYY-MM-DD HH:mm:ss"
          type="daterange"
          range-separator="-"
          :start-placeholder="t('common.startDate')"
          :end-placeholder="t('common.endDate')"
          :default-time="[new Date(2000, 1, 1, 0, 0, 0), new Date(2000, 1, 1, 23, 59, 59)]"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">{{ t('common.search') }}</el-button>
        <el-button icon="Refresh" @click="resetQuery">{{ t('common.reset') }}</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8 merchant-table-page__toolbar">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" :disabled="requiresMerchantSelection" @click="handleAdd" v-hasPermi="['merchant:user:add']">{{ t('common.add') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()" v-hasPermi="['merchant:user:edit']">{{ t('common.edit') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['merchant:user:remove']">{{ t('common.delete') }}</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" :columns="columns" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="userList" border class="merchant-table-page__table" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column v-if="columns[0].visible" :label="t('user.userName')" min-width="220">
        <template #default="{ row }">
          <div class="merchant-user-cell">
            <span class="merchant-user-avatar">{{ getInitials(row) }}</span>
            <span>
              <strong>{{ row.nickName || row.userName || '-' }}</strong>
              <small>{{ row.email || row.userName || '-' }}</small>
            </span>
          </div>
        </template>
      </el-table-column>
      <el-table-column v-if="columns[1].visible" :label="t('user.phonenumber')" prop="phonenumber" width="142" :show-overflow-tooltip="true" />
      <el-table-column v-if="columns[2].visible" :label="t('user.status')" align="center" width="126">
        <template #default="{ row }">
          <el-switch
            v-model="row.status"
            active-value="1"
            inactive-value="0"
            :active-text="getStatusLabel(row)"
            inline-prompt
            @change="handleStatusChange(row)"
          />
        </template>
      </el-table-column>
      <el-table-column v-if="columns[3].visible" :label="t('common.createTime')" align="center" prop="createTime" width="170">
        <template #default="{ row }">
          <span>{{ formatUtc(row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="t('common.operate')" align="center" width="150" fixed="right">
        <template #default="{ row }">
          <AdminTableActions :actions="[
            { label: t('common.edit'), icon: 'Edit', permission: 'merchant:user:edit', onClick: () => handleUpdate(row) },
            { label: t('common.delete'), icon: 'Delete', type: 'danger', permission: 'merchant:user:remove', onClick: () => handleDelete(row) }
          ]" />
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" class="merchant-table-page__pagination" @pagination="getList" />

    <AdminDrawer
      v-model="open"
      :title="title"
      size="640px"
      append-to-body
      destroy-on-close
      :close-on-click-modal="false"
      :before-close="formCloseGuard.beforeClose"
      @closed="formCloseGuard.handleClosed"
    >
      <el-form ref="userRef" :model="form" :rules="rules" label-width="112px">
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item :label="t('user.nickName')" prop="nickName">
              <el-input v-model="form.nickName" :placeholder="t('user.nickNamePlaceholder')" maxlength="30" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row v-if="form.userId === undefined" :gutter="16">
          <el-col :span="24">
            <el-form-item :label="t('user.userName')" prop="userName">
              <el-input v-model="form.userName" :placeholder="t('user.userNamePlaceholder')" maxlength="100" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="t('user.password')" prop="password">
              <el-input v-model="form.password" :placeholder="t('user.passwordPlaceholder')" type="password" maxlength="20" show-password />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item :label="t('user.phonenumber')" prop="phonenumber">
              <el-input v-model="form.phonenumber" :placeholder="t('user.phonenumberPlaceholder')" maxlength="20" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="t('user.email')" prop="email">
              <el-input v-model="form.email" :placeholder="t('user.emailPlaceholder')" maxlength="50" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item :label="t('user.sex')">
              <el-select v-model="form.sex" :placeholder="t('common.selectPlaceholder')">
                <el-option v-for="dict in sys_user_sex" :key="dict.value" :label="getUserSexLabel(dict)" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="t('user.status')">
              <el-radio-group v-model="form.status">
                <el-radio v-for="dict in sys_normal_disable" :key="dict.value" :value="dict.value">{{ dict.label }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item :label="t('user.remark')">
          <el-input v-model="form.remark" type="textarea" :placeholder="t('user.remarkPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :loading="submitLoading" @click="submitForm">{{ t('common.confirm') }}</el-button>
          <el-button @click="cancel">{{ t('common.cancel') }}</el-button>
        </div>
      </template>
    </AdminDrawer>
  </div>
</template>

<script setup lang="ts" name="MerchantUserPage">
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { addMerchantUser, changeMerchantUserStatus, delMerchantUser, getMerchantUser, listMerchantUser, updateMerchantUser, type MerchantUserQuery } from '@/api/merchant/user'
import { listMerchantProfiles, type MerchantProfile } from '@/api/merchant/profile'
import type { SysUser } from '@/api/system/user'
import { formatUtc, withUtcDateRange } from '@/utils/datetime'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { useDict } from '@/utils/dict'
import { runUiAction } from '@/utils/action'
import { getInitPassword } from '@/api/system/user'
import { useFormCloseGuard } from '@/composables/useFormCloseGuard'

interface DictOption {
  label?: string
  value?: string
}

interface ColumnOption {
  key: number
  label: string
  visible: boolean
}

const props = withDefaults(
  defineProps<{
    platformMode?: boolean
  }>(),
  {
    platformMode: false
  }
)
const localeStore = useLocaleStore()
const t = (key: string, params?: Record<string, string | number>) => {
  const message = getMessage(key, localeStore.language)
  if (!params) return message
  return Object.entries(params).reduce((text, [name, value]) => text.replaceAll(`{${name}}`, String(value)), message)
}
const { sys_normal_disable, sys_user_sex } = useDict('sys_normal_disable', 'sys_user_sex')

const columnLabelKeys = ['user.userName', 'user.phonenumber', 'user.status', 'common.createTime']
const columns = ref<ColumnOption[]>(columnLabelKeys.map((key, index) => ({ key: index, label: t(key), visible: true })))
watch(
  () => localeStore.language,
  () => {
    columns.value.forEach((column, index) => {
      column.label = t(columnLabelKeys[index])
    })
  }
)

const userList = ref<SysUser[]>([])
const open = ref(false)
const loading = ref(true)
const submitLoading = ref(false)
const showSearch = ref(true)
const ids = ref<Array<number | string>>([])
const selectionRows = ref<SysUser[]>([])
const total = ref(0)
const dateRange = ref<string[]>([])
const merchantOptions = ref<MerchantProfile[]>([])
const selectedTenantId = ref<number | string>()
const form = ref<SysUser>({})
const queryRef = ref<FormInstance>()
const userRef = ref<FormInstance>()
const queryParams = reactive<MerchantUserQuery>({
  pageNum: 1,
  pageSize: 10
})

const single = computed(() => ids.value.length !== 1)
const multiple = computed(() => ids.value.length === 0)
const title = computed(() => (form.value.userId ? t('user.editTitle') : t('user.addTitle')))
const isPlatformMode = computed(() => props.platformMode)
const requiresMerchantSelection = computed(() => isPlatformMode.value && !selectedTenantId.value)
const userSexLabelKeys: Record<string, string> = {
  '0': 'user.sexMale',
  '1': 'user.sexFemale',
  '2': 'user.sexUnknown'
}
const rules = computed<FormRules<SysUser>>(() => ({
  userName: [
    { required: true, message: t('user.userNameRequired'), trigger: 'blur' },
    { min: 2, max: 100, message: t('validation.user.username.max', { max: 100 }), trigger: 'blur' }
  ],
  nickName: [{ required: true, message: t('user.nickNameRequired'), trigger: 'blur' }],
  password: [
    { required: true, message: t('user.passwordRequired'), trigger: 'blur' },
    { min: 5, max: 20, message: t('user.passwordLength'), trigger: 'blur' }
  ],
  email: [{ type: 'email', message: t('user.emailInvalid'), trigger: ['blur', 'change'] }]
}))
const formCloseGuard = useFormCloseGuard({
  enabled: () => open.value,
  getSnapshot: () => JSON.stringify(form.value || {}),
  close: () => {
    open.value = false
  },
  reset,
  t
})

function getUserSexLabel(dict: DictOption) {
  return dict.value && userSexLabelKeys[dict.value] ? t(userSexLabelKeys[dict.value]) : dict.label || ''
}

function merchantOptionLabel(item: MerchantProfile) {
  return item.merchantName || item.companyName || '-'
}

function getInitials(row: SysUser) {
  const source = (row.nickName || row.userName || 'U').trim()
  return source.slice(0, 2).toUpperCase()
}

function getStatusLabel(row: SysUser) {
  return sys_normal_disable.value.find((item) => String(item.value) === String(row.status))?.label || row.status || '-'
}

function withDateRange(query: MerchantUserQuery) {
  return withUtcDateRange(query, dateRange.value)
}

function reset() {
  form.value = {
    userId: undefined,
    userName: undefined,
    nickName: undefined,
    password: undefined,
    phonenumber: undefined,
    email: undefined,
    sex: undefined,
    status: '1',
    remark: undefined,
    postIds: [],
    roleIds: []
  }
  userRef.value?.resetFields()
}

async function getList() {
  loading.value = true
  try {
    queryParams.tenantId = isPlatformMode.value ? selectedTenantId.value || undefined : undefined
    const response = await listMerchantUser(withDateRange(queryParams))
    userList.value = response.rows || []
    total.value = response.total || 0
  } finally {
    loading.value = false
  }
}

function handleMerchantChange() {
  queryParams.pageNum = 1
  getList()
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  dateRange.value = []
  if (isPlatformMode.value) {
    selectedTenantId.value = undefined
  }
  queryRef.value?.resetFields()
  handleQuery()
}

function handleSelectionChange(selection: SysUser[]) {
  selectionRows.value = selection
  ids.value = selection.map((item) => String(item.userId)).filter(Boolean)
}

async function handleAdd() {
  if (requiresMerchantSelection.value) return
  reset()
  await runUiAction(async () => {
    const response = await getMerchantUser(undefined, selectedTenantId.value)
    form.value.roleIds = response.data.roleIds || []
    const initPasswordResponse = await getInitPassword()
    form.value.password = initPasswordResponse.data || initPasswordResponse.msg || ''
    formCloseGuard.markPristine()
    open.value = true
  })
}

async function handleUpdate(row?: SysUser) {
  reset()
  const userId = row?.userId || ids.value[0]
  if (!userId) return
  const targetTenantId = isPlatformMode.value ? row?.tenantId || selectionRows.value[0]?.tenantId : undefined
  await runUiAction(async () => {
    const response = await getMerchantUser(userId, targetTenantId)
    form.value = response.data.user || {}
    if (targetTenantId) {
      form.value.tenantId = Number(targetTenantId)
    }
    form.value.roleIds = response.data.roleIds || []
    form.value.postIds = []
    formCloseGuard.markPristine()
    open.value = true
  })
}

function cancel() {
  formCloseGuard.closeWithGuard()
}

async function submitForm() {
  if (submitLoading.value) return
  const valid = await userRef.value?.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (isPlatformMode.value) {
      form.value.tenantId = selectedTenantId.value ? Number(selectedTenantId.value) : form.value.tenantId
    }
    if (form.value.userId !== undefined) {
      await updateMerchantUser(form.value)
      ElMessage.success(t('common.editSuccess'))
    } else {
      await addMerchantUser(form.value)
      ElMessage.success(t('common.addSuccess'))
    }
    formCloseGuard.markPristine()
    open.value = false
    await getList()
  } catch {
    // Request interceptor already displays the backend error.
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row?: SysUser) {
  const userIds = row?.userId || ids.value
  if (!userIds || (Array.isArray(userIds) && !userIds.length)) return
  try {
    await ElMessageBox.confirm(t('user.deleteConfirm', { ids: Array.isArray(userIds) ? userIds.join(',') : userIds }), t('common.prompt'), {
      type: 'warning'
    })
    if (isPlatformMode.value && !row && selectionRows.value.length) {
      const rowsByTenant = selectionRows.value.reduce<Record<string, Array<number | string>>>((groups, item) => {
        const key = String(item.tenantId || '')
        if (!key || !item.userId) return groups
        groups[key] ||= []
        groups[key].push(item.userId)
        return groups
      }, {})
      await Promise.all(Object.entries(rowsByTenant).map(([tenantId, rowUserIds]) => delMerchantUser(rowUserIds, tenantId)))
    } else {
      await delMerchantUser(userIds, isPlatformMode.value ? row?.tenantId : undefined)
    }
    ElMessage.success(t('common.deleteSuccess'))
    await getList()
  } catch {
    // User cancelled or the request interceptor already displayed the backend error.
  }
}

async function handleStatusChange(row: SysUser) {
  if (!row.userId || !row.status) return
  try {
    await changeMerchantUserStatus(row.userId, row.status, isPlatformMode.value ? row.tenantId : undefined)
    ElMessage.success(t('common.editSuccess'))
  } catch {
    row.status = row.status === '0' ? '1' : '0'
  }
}

async function initPage() {
  if (isPlatformMode.value) {
    const response = await listMerchantProfiles({ pageNum: 1, pageSize: 200 })
    merchantOptions.value = response.rows || []
  }
  await getList()
}

initPage()
</script>

<style scoped>
.merchant-user-cell {
  display: inline-flex;
  align-items: center;
  min-width: 0;
  gap: 8px;
}

.merchant-user-cell small {
  display: block;
  margin-top: 3px;
  color: var(--el-text-color-secondary);
}

.merchant-user-avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  flex: 0 0 30px;
  border-radius: 50%;
  background: #1677ff;
  color: #fff;
  font-size: 12px;
  font-weight: 700;
}

.mr4 {
  margin-right: 4px;
}
</style>
