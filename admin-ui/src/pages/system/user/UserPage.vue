<template>
  <div class="app-container user-page">
    <el-row :gutter="12" class="user-page__layout">
      <el-col :span="3" :xs="24" class="user-page__dept">
        <div class="head-container user-page__dept-search">
          <el-input
            v-model="deptName"
            :placeholder="t('user.deptNamePlaceholder')"
            clearable
            prefix-icon="Search"
          />
        </div>
        <div class="head-container user-page__dept-tree">
          <el-tree
            ref="deptTreeRef"
            :data="deptOptions"
            :props="{ label: 'label', children: 'children' }"
            :expand-on-click-node="false"
            :filter-node-method="filterNode"
            node-key="id"
            highlight-current
            default-expand-all
            @node-click="handleNodeClick"
          >
            <template #default="{ node }">
              <span class="user-page__dept-card" :class="`is-${getDeptTone(node.level)}`">
                <span class="user-page__dept-card-icon">
                  <el-icon><Briefcase /></el-icon>
                </span>
                <span class="user-page__dept-card-main">
                  <strong>{{ node.label }}</strong>
                  <small>{{ getDeptSubtitle(node.level) }}</small>
                </span>
              </span>
            </template>
          </el-tree>
        </div>
      </el-col>

      <el-col :span="21" :xs="24" class="user-page__content system-table-page">
        <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true" label-width="96px" class="system-table-page__search">
          <el-form-item :label="t('user.userName')" prop="userName">
            <el-input v-model="queryParams.userName" :placeholder="t('user.userNamePlaceholder')" clearable style="width: 240px" @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item :label="t('user.status')" prop="status" data-agent-field="status">
            <el-select v-model="queryParams.status" class="user-page__status-select" :placeholder="t('user.statusPlaceholder')" clearable>
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

        <el-row :gutter="10" class="mb8 system-table-page__toolbar">
          <el-col :span="1.5">
            <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['system:user:add']">{{ t('common.add') }}</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()" v-hasPermi="['system:user:edit']">{{ t('common.edit') }}</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['system:user:remove']">{{ t('common.delete') }}</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="info" plain icon="Upload" @click="handleImport" v-hasPermi="['system:user:import']">{{ t('common.import') }}</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['system:user:export']">{{ t('common.export') }}</el-button>
          </el-col>
          <span class="selection-count">{{ t('common.selectedCount', { count: ids.length }) }}</span>
          <right-toolbar v-model:showSearch="showSearch" :columns="columns" @queryTable="getList" />
        </el-row>

        <el-table v-loading="loading" :data="userList" border class="user-rich-table system-table-page__table" @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column v-if="columns[0].visible" :label="t('user.userName')" min-width="240">
            <template #default="{ row }">
              <div class="user-rich-cell">
                <span class="user-rich-avatar" :class="`is-${getAvatarTone(row)}`">{{ getInitials(row) }}</span>
                <span class="user-rich-main">
                  <strong>{{ getDisplayName(row) }}</strong>
                  <small>{{ getUserSubtitle(row) }}</small>
                </span>
              </div>
            </template>
          </el-table-column>
          <el-table-column v-if="columns[1].visible" :label="t('user.deptName')" align="center" prop="deptName" min-width="150" :show-overflow-tooltip="true">
            <template #default="{ row }">
              <span class="user-org-pill">{{ row.deptName || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column v-if="columns[2].visible" :label="t('user.phonenumber')" align="center" prop="phonenumber" width="142" />
          <el-table-column v-if="columns[3].visible" :label="t('user.status')" align="center" width="126">
            <template #default="{ row }">
              <button type="button" class="user-status-pill" :class="`is-${getStatusTone(row)}`" @click="toggleUserStatus(row)">
                <i />
                <span>{{ getStatusLabel(row) }}</span>
              </button>
            </template>
          </el-table-column>
          <el-table-column v-if="columns[4].visible" :label="t('common.createTime')" align="center" prop="createTime" width="170">
            <template #default="{ row }">
              <span>{{ formatUtc(row.createTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column v-if="showOperationColumn" :label="t('common.operate')" align="center" width="150" fixed="right" class-name="small-padding fixed-width">
            <template #default="{ row }">
              <AdminTableActions :actions="userRowActions(row)" />
            </template>
          </el-table-column>
        </el-table>

        <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" class="system-table-page__pagination" @pagination="getList" />
      </el-col>
    </el-row>

    <AdminDrawer
      v-model="open"
      :title="title"
      size="720px"
      class="user-edit-dialog"
      append-to-body
      destroy-on-close
      :close-on-click-modal="false"
      :before-close="formCloseGuard.beforeClose"
      @closed="formCloseGuard.handleClosed"
    >
      <el-form ref="userRef" :model="form" :rules="rules" label-width="112px" class="user-dialog">
        <div class="user-dialog__intro">
          <span>{{ form.userId === undefined ? t('common.add') : t('common.edit') }}</span>
          <p>{{ t('user.dialogIntro') }}</p>
        </div>

        <section class="user-dialog__section">
          <div class="user-dialog__section-title">
            <strong>{{ t('user.dialogBasicInfo') }}</strong>
            <em>{{ t('user.dialogBasicDesc') }}</em>
          </div>
          <el-row :gutter="16">
            <el-col :span="12" :xs="24">
              <el-form-item :label="t('user.nickName')" prop="nickName">
                <el-input v-model="form.nickName" :placeholder="t('user.nickNamePlaceholder')" maxlength="30" />
              </el-form-item>
            </el-col>
            <el-col :span="12" :xs="24">
              <el-form-item :label="t('user.deptId')" prop="deptId">
                <el-tree-select
                  v-model="form.deptId"
                  :data="deptOptions"
                  :props="{ value: 'id', label: 'label', children: 'children' }"
                  value-key="id"
                  :placeholder="t('user.deptIdPlaceholder')"
                  check-strictly
                />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="16">
            <el-col :span="12" :xs="24">
              <el-form-item :label="t('user.phonenumber')" prop="phonenumber">
                <el-input v-model="form.phonenumber" :placeholder="t('user.phonenumberPlaceholder')" maxlength="11" />
              </el-form-item>
            </el-col>
            <el-col :span="12" :xs="24">
              <el-form-item :label="t('user.email')" prop="email">
                <el-input v-model="form.email" :placeholder="t('user.emailPlaceholder')" maxlength="50" />
              </el-form-item>
            </el-col>
          </el-row>
        </section>

        <section v-if="form.userId === undefined" class="user-dialog__section">
          <div class="user-dialog__section-title">
            <strong>{{ t('user.dialogAccountSecurity') }}</strong>
            <em>{{ t('user.dialogAccountDesc') }}</em>
          </div>
          <el-row :gutter="16">
            <el-col :span="12" :xs="24">
              <el-form-item :label="t('user.userName')" prop="userName">
                <el-input v-model="form.userName" :placeholder="t('user.userNamePlaceholder')" maxlength="30" />
              </el-form-item>
            </el-col>
            <el-col :span="12" :xs="24">
              <el-form-item :label="t('user.password')" prop="password">
                <el-input v-model="form.password" :placeholder="t('user.passwordPlaceholder')" type="password" maxlength="20" show-password />
              </el-form-item>
            </el-col>
          </el-row>
        </section>

        <section class="user-dialog__section">
          <div class="user-dialog__section-title">
            <strong>{{ t('user.dialogPermissionConfig') }}</strong>
            <em>{{ t('user.dialogPermissionDesc') }}</em>
          </div>
          <el-row :gutter="16">
            <el-col :span="12" :xs="24">
              <el-form-item :label="t('user.sex')">
                <el-select v-model="form.sex" :placeholder="t('common.selectPlaceholder')">
                  <el-option v-for="dict in sys_user_sex" :key="dict.value" :label="getUserSexLabel(dict)" :value="dict.value" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12" :xs="24">
              <el-form-item :label="t('user.status')">
                <el-radio-group v-model="form.status">
                  <el-radio v-for="dict in sys_normal_disable" :key="dict.value" :value="dict.value">{{ dict.label }}</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="16">
            <el-col :span="12" :xs="24">
              <el-form-item :label="t('user.post')">
                <el-select v-model="form.postIds" multiple :placeholder="t('common.selectPlaceholder')">
                  <el-option v-for="item in postOptions" :key="item.postId" :label="item.postName" :value="item.postId" :disabled="item.status === '0'" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12" :xs="24">
              <el-form-item :label="t('user.role')">
                <el-select v-model="form.roleIds" multiple :placeholder="t('common.selectPlaceholder')">
                  <el-option v-for="item in roleOptions" :key="item.roleId" :label="item.roleName" :value="item.roleId" :disabled="item.status === '0'" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </section>

        <section class="user-dialog__section">
          <div class="user-dialog__section-title">
            <strong>{{ t('user.dialogRemarkInfo') }}</strong>
            <em>{{ t('user.dialogRemarkDesc') }}</em>
          </div>
          <el-form-item :label="t('user.remark')">
            <el-input v-model="form.remark" type="textarea" :placeholder="t('user.remarkPlaceholder')" />
          </el-form-item>
        </section>
      </el-form>
      <template #footer>
        <div class="dialog-footer user-dialog__footer">
          <el-button type="primary" :loading="submitLoading" @click="submitForm">{{ t('common.confirm') }}</el-button>
          <el-button @click="cancel">{{ t('common.cancel') }}</el-button>
        </div>
      </template>
    </AdminDrawer>

    <AdminDialog v-model="upload.open" :title="upload.title" width="480px" class="admin-upload-dialog" append-to-body destroy-on-close @closed="resetUpload">
      <el-upload
        ref="uploadRef"
        :limit="1"
        accept=".xlsx, .xls"
        :action="`${upload.url}?updateSupport=${upload.updateSupport}`"
        :http-request="uploadUserImport"
        :disabled="upload.isUploading"
        :on-progress="handleFileUploadProgress"
        :on-success="handleFileSuccess"
        :auto-upload="false"
        drag
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">{{ t('user.dragFileHere') }}<em>{{ t('user.clickUpload') }}</em></div>
        <template #tip>
          <div class="el-upload__tip text-center">
            <div class="el-upload__tip">
              <el-checkbox v-model="upload.updateSupport" :true-value="1" :false-value="0" />{{ t('user.updateExistingUser') }}
            </div>
            <span>{{ t('user.importFileTip') }}</span>
            <el-link type="primary" :underline="false" style="font-size: 12px; vertical-align: baseline" @click="importTemplate">{{ t('user.downloadTemplate') }}</el-link>
          </div>
        </template>
      </el-upload>
      <template #footer>
        <AdminDialogFooter>
          <el-button type="primary" :loading="upload.isUploading" @click="submitFileForm">{{ t('common.confirm') }}</el-button>
          <el-button @click="upload.open = false">{{ t('common.cancel') }}</el-button>
        </AdminDialogFooter>
      </template>
    </AdminDialog>
  </div>
</template>

<script setup lang="ts" name="UserPage">
import { computed, h, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules, type UploadInstance, type UploadProgressEvent, type UploadRawFile, type UploadRequestOptions } from 'element-plus'
import { Briefcase } from '@element-plus/icons-vue'
import { addUser, changeUserStatus, delUser, deptTreeSelect, getInitPassword, getUser, listUser, resetUserPwd, updateUser, type SysUser, type TreeOption, type UserOptionPost, type UserOptionRole, type UserQuery } from '@/api/system/user'
import { download, request } from '@/utils/request'
import { formatUtc, withUtcDateRange } from '@/utils/datetime'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { useDict } from '@/utils/dict'
import { runUiAction } from '@/utils/action'
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

const router = useRouter()
const localeStore = useLocaleStore()
const t = (key: string, params?: Record<string, string | number>) => {
  const message = getMessage(key, localeStore.language)
  if (!params) return message
  return Object.entries(params).reduce((text, [name, value]) => text.replaceAll(`{${name}}`, String(value)), message)
}
const { sys_normal_disable, sys_user_sex } = useDict('sys_normal_disable', 'sys_user_sex')

const columnLabelKeys = ['user.userName', 'user.deptName', 'user.phonenumber', 'user.status', 'common.createTime']
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
const total = ref(0)
const dateRange = ref<string[]>([])
const deptName = ref('')
const deptOptions = ref<TreeOption[]>([])
const postOptions = ref<UserOptionPost[]>([])
const roleOptions = ref<UserOptionRole[]>([])
const form = ref<SysUser>({})
const queryRef = ref<FormInstance>()
const userRef = ref<FormInstance>()
const uploadRef = ref<UploadInstance>()
const deptTreeRef = ref()
const queryParams = reactive<UserQuery>({
  pageNum: 1,
  pageSize: 10
})

const upload = reactive({
  open: false,
  title: '',
  isUploading: false,
  updateSupport: 0,
  url: '/system/user/importData'
})

const single = computed(() => ids.value.length !== 1)
const multiple = computed(() => ids.value.length === 0)
const showOperationColumn = computed(() => userList.value.some((row) => userRowActions(row).some((action) => !action.hidden)))
const title = computed(() => (form.value.userId ? t('user.editTitle') : t('user.addTitle')))
const userSexLabelKeys: Record<string, string> = {
  '0': 'user.sexMale',
  '1': 'user.sexFemale',
  '2': 'user.sexUnknown'
}
const rules = computed<FormRules<SysUser>>(() => ({
  userName: [
    { required: true, message: t('user.userNameRequired'), trigger: 'blur' },
    { min: 2, max: 20, message: t('user.userNameLength'), trigger: 'blur' }
  ],
  nickName: [{ required: true, message: t('user.nickNameRequired'), trigger: 'blur' }],
  password: [
    { required: true, message: t('user.passwordRequired'), trigger: 'blur' },
    { min: 5, max: 20, message: t('user.passwordLength'), trigger: 'blur' }
  ],
  email: [
    { required: true, message: t('user.emailRequired'), trigger: 'blur' },
    { type: 'email', message: t('user.emailInvalid'), trigger: ['blur', 'change'] }
  ],
  phonenumber: [{ pattern: /^1[3-9][0-9]\d{8}$/, message: t('user.phonenumberInvalid'), trigger: 'blur' }]
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

function getDisplayName(row: SysUser) {
  return row.nickName || row.userName || '-'
}

function getUserSubtitle(row: SysUser) {
  return row.email || row.userName || row.phonenumber || '-'
}

function getInitials(row: SysUser) {
  const source = getDisplayName(row).trim()
  if (!source || source === '-') return 'U'
  const words = source.split(/\s+/).filter(Boolean)
  if (words.length > 1) return `${words[0][0]}${words[1][0]}`.toUpperCase()
  return source.slice(0, 2).toUpperCase()
}

function getAvatarTone(row: SysUser) {
  const tones = ['blue', 'teal', 'purple', 'orange', 'green']
  const seed = String(row.userId || row.userName || row.nickName || '').split('').reduce((sum, char) => sum + char.charCodeAt(0), 0)
  return tones[seed % tones.length]
}

function getStatusLabel(row: SysUser) {
  return sys_normal_disable.value.find((item) => String(item.value) === String(row.status))?.label || row.status || '-'
}

function getStatusTone(row: SysUser) {
  return String(row.status) === '1' ? 'active' : 'inactive'
}

function userRowActions(row: SysUser) {
  const hidden = row.userId === 1
  return [
    { label: t('common.edit'), icon: 'Edit', permission: 'system:user:edit', hidden, primary: true, onClick: () => handleUpdate(row) },
    { label: t('user.resetPassword'), icon: 'Key', permission: 'system:user:resetPwd', hidden, onClick: () => handleResetPwd(row) },
    { label: t('user.assignRole'), icon: 'UserFilled', permission: 'system:user:edit', hidden, onClick: () => handleAuthRole(row) },
    { label: t('common.delete'), icon: 'Delete', type: 'danger' as const, permission: 'system:user:remove', hidden, onClick: () => handleDelete(row) }
  ]
}

function getDeptTone(level: number) {
  const tones = ['blue', 'green', 'purple', 'orange', 'teal']
  return tones[(Math.max(level, 1) - 1) % tones.length]
}

function getDeptSubtitle(level: number) {
  return level === 1 ? t('user.deptCardRootDesc') : t('user.deptCardChildDesc')
}

function toggleUserStatus(row: SysUser) {
  row.status = String(row.status) === '1' ? '0' : '1'
  handleStatusChange(row)
}

function withDateRange(query: UserQuery) {
  return withUtcDateRange(query, dateRange.value)
}

function reset() {
  form.value = {
    userId: undefined,
    deptId: undefined,
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

async function getDeptTree() {
  const response = await deptTreeSelect()
  deptOptions.value = response.data || []
}

async function getList() {
  loading.value = true
  try {
    const response = await listUser(withDateRange(queryParams))
    userList.value = response.rows || []
    total.value = response.total || 0
  } finally {
    loading.value = false
  }
}

function filterNode(value: string, data: TreeOption) {
  if (!value) return true
  return data.label.includes(value)
}

watch(deptName, (value) => {
  deptTreeRef.value?.filter(value)
})

function handleNodeClick(data: TreeOption) {
  queryParams.deptId = data.id
  handleQuery()
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  dateRange.value = []
  queryRef.value?.resetFields()
  queryParams.deptId = undefined
  deptTreeRef.value?.setCurrentKey(null)
  handleQuery()
}

function handleSelectionChange(selection: SysUser[]) {
  ids.value = selection.map((item) => String(item.userId)).filter(Boolean)
}

async function handleAdd() {
  reset()
  await runUiAction(async () => {
    const response = await getUser()
    postOptions.value = response.data.posts || []
    roleOptions.value = response.data.roles || []
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
  await runUiAction(async () => {
    const response = await getUser(userId)
    form.value = response.data.user || {}
    postOptions.value = response.data.posts || []
    roleOptions.value = response.data.roles || []
    form.value.postIds = response.data.postIds || []
    form.value.roleIds = response.data.roleIds || []
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
    if (form.value.userId !== undefined) {
      await updateUser(form.value)
      ElMessage.success(t('common.editSuccess'))
    } else {
      await addUser(form.value)
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
    await delUser(userIds)
    ElMessage.success(t('common.deleteSuccess'))
    await getList()
  } catch {
    // User cancelled or the request interceptor already displayed the backend error.
  }
}

function handleExport() {
  download('system/user/export', { ...withDateRange(queryParams) }, `user_${Date.now()}.xlsx`)
}

async function handleStatusChange(row: SysUser) {
  if (!row.userId || !row.status) return
  const action = row.status === '1' ? t('user.enable') : t('user.disable')
  try {
    await ElMessageBox.confirm(t('user.statusConfirm', { action, name: row.userName || '' }), t('common.prompt'), { type: 'warning' })
    await changeUserStatus(row.userId, row.status)
    ElMessage.success(t('user.statusSuccess', { action }))
  } catch {
    row.status = row.status === '0' ? '1' : '0'
  }
}

async function handleResetPwd(row: SysUser) {
  if (!row.userId) return
  try {
    const result = await ElMessageBox.prompt(t('user.resetPasswordPrompt', { name: row.userName || '' }), t('common.prompt'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      customClass: 'admin-message-box--prompt',
      closeOnClickModal: false,
      inputPattern: /^.{5,20}$/,
      inputErrorMessage: t('user.passwordLength')
    }).catch(() => undefined)
    if (!result?.value) return
    await resetUserPwd(row.userId, result.value)
    ElMessage.success(t('user.resetPasswordSuccess', { password: result.value }))
  } catch {
    // Request interceptor already displays the backend error.
  }
}

function handleAuthRole(row: SysUser) {
  if (row.userId) router.push(`/system/user-auth/role/${row.userId}`)
}

function handleImport() {
  upload.title = t('user.importTitle')
  upload.open = true
}

function resetUpload() {
  upload.isUploading = false
  upload.updateSupport = 0
  uploadRef.value?.clearFiles()
}

function importTemplate() {
  download('system/user/importTemplate', {}, `user_template_${Date.now()}.xlsx`)
}

function handleFileUploadProgress(_event: UploadProgressEvent) {
  upload.isUploading = true
}

async function uploadUserImport(options: UploadRequestOptions) {
  upload.isUploading = true
  const formData = new FormData()
  formData.append(options.filename, options.file)
  try {
    return await request<{ msg?: string }>({
      url: `${upload.url}?updateSupport=${upload.updateSupport}`,
      method: 'post',
      data: formData
    })
  } finally {
    upload.isUploading = false
  }
}

function handleFileSuccess(response: { msg?: string }, file: UploadRawFile) {
  upload.open = false
  upload.isUploading = false
  uploadRef.value?.handleRemove(file)
  ElMessageBox.alert(
    h('div', { class: 'admin-message-result' }, response.msg || ''),
    t('user.importResult'),
    { customClass: 'admin-message-box--result' }
  )
  getList()
}

function submitFileForm() {
  uploadRef.value?.submit()
}

getDeptTree()
getList()
</script>

<style scoped>
.user-rich-table :deep(.el-table__row) {
  --el-table-row-hover-bg-color: #f8fbff;
}

.user-rich-table :deep(td.el-table__cell) {
  height: 64px;
}

.user-rich-cell {
  display: inline-flex;
  align-items: center;
  min-width: 0;
  gap: 12px;
}

.user-rich-avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  flex: 0 0 38px;
  border-radius: 50%;
  color: #ffffff;
  font-size: 13px;
  font-weight: 700;
  box-shadow: 0 6px 14px rgba(15, 35, 80, 0.14);
}

.user-rich-avatar.is-blue {
  background: linear-gradient(135deg, #7dd3fc, #1677ff);
}

.user-rich-avatar.is-teal {
  background: linear-gradient(135deg, #5eead4, #14b8a6);
}

.user-rich-avatar.is-purple {
  background: linear-gradient(135deg, #c4b5fd, #7c3aed);
}

.user-rich-avatar.is-orange {
  background: linear-gradient(135deg, #fdba74, #f97316);
}

.user-rich-avatar.is-green {
  background: linear-gradient(135deg, #86efac, #22c55e);
}

.user-rich-main {
  display: grid;
  min-width: 0;
  text-align: left;
}

.user-rich-main strong {
  overflow: hidden;
  color: #07143d;
  font-size: 14px;
  font-weight: 700;
  line-height: 1.35;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-rich-main small {
  overflow: hidden;
  color: #6b7895;
  font-size: 12px;
  font-weight: 500;
  line-height: 1.45;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-org-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  max-width: 100%;
  min-height: 26px;
  padding: 4px 10px;
  border-radius: 7px;
  background: #eef6ff;
  color: #1e4f9a;
  font-size: 12px;
  font-weight: 600;
}

.user-status-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  min-width: 76px;
  height: 28px;
  padding: 0 10px;
  border: 0;
  border-radius: 7px;
  cursor: pointer;
  font: inherit;
  font-size: 12px;
  font-weight: 700;
}

.user-status-pill i {
  width: 7px;
  height: 7px;
  border-radius: 50%;
}

.user-status-pill.is-active {
  background: #e8f8ee;
  color: #159947;
}

.user-status-pill.is-active i {
  background: #22c55e;
}

.user-status-pill.is-inactive {
  background: #fff1f1;
  color: #e5484d;
}

.user-status-pill.is-inactive i {
  background: #ff4d4f;
}

.user-page__dept-card {
  display: grid;
  grid-template-columns: 28px minmax(0, 1fr);
  align-items: center;
  width: 100%;
  gap: 8px;
}

.user-page__dept-card-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 6px;
  background: #eaf2ff;
  color: #1677ff;
}

.user-page__dept-card-icon .el-icon {
  font-size: 14px;
}

.user-page__dept-card-main {
  display: grid;
  min-width: 0;
  text-align: left;
}

.user-page__dept-card-main strong {
  overflow: hidden;
  color: #07143d;
  font-size: 13px;
  font-weight: 700;
  line-height: 1.35;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-page__dept-card-main small {
  overflow: hidden;
  margin-top: 1px;
  color: #6b7895;
  font-size: 10px;
  font-weight: 500;
  line-height: 1.35;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-page__dept-card.is-green .user-page__dept-card-icon {
  background: #e7f9eb;
  color: #16b345;
}

.user-page__dept-card.is-purple .user-page__dept-card-icon {
  background: #f1eaff;
  color: #7c3aed;
}

.user-page__dept-card.is-orange .user-page__dept-card-icon {
  background: #fff0df;
  color: #f97316;
}

.user-page__dept-card.is-teal .user-page__dept-card-icon {
  background: #dff9f7;
  color: #13b8b5;
}
</style>
