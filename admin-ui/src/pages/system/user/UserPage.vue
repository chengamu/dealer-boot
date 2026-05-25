<template>
  <div class="app-container user-page">
    <el-row :gutter="20" class="user-page__layout">
      <el-col :span="4" :xs="24" class="user-page__dept">
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
              <span class="user-page__dept-node" :class="{ 'is-root': node.level === 1 }">
                <el-icon v-if="node.level === 1" class="user-page__dept-node-icon"><Briefcase /></el-icon>
                <span v-else class="user-page__dept-node-dot" />
                <span class="user-page__dept-node-label">{{ node.label }}</span>
              </span>
            </template>
          </el-tree>
        </div>
      </el-col>

      <el-col :span="20" :xs="24" class="user-page__content">
        <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true" label-width="96px">
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

        <el-row :gutter="10" class="mb8">
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
          <right-toolbar v-model:showSearch="showSearch" :columns="columns" @queryTable="getList" />
        </el-row>

        <el-table v-loading="loading" :data="userList" @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column v-if="columns[0].visible" :label="t('user.userName')" align="center" prop="userName" min-width="120" :show-overflow-tooltip="true" />
          <el-table-column v-if="columns[1].visible" :label="t('user.nickName')" align="center" prop="nickName" min-width="120" :show-overflow-tooltip="true" />
          <el-table-column v-if="columns[2].visible" :label="t('user.deptName')" align="center" prop="dept.deptName" min-width="126" :show-overflow-tooltip="true" />
          <el-table-column v-if="columns[3].visible" :label="t('user.phonenumber')" align="center" prop="phonenumber" width="132" />
          <el-table-column v-if="columns[4].visible" :label="t('user.status')" align="center" width="96">
            <template #default="{ row }">
              <el-switch v-model="row.status" active-value="1" inactive-value="0" @change="handleStatusChange(row)" />
            </template>
          </el-table-column>
          <el-table-column v-if="columns[5].visible" :label="t('common.createTime')" align="center" prop="createTime" width="170">
            <template #default="{ row }">
              <span>{{ formatUtc(row.createTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column :label="t('common.operate')" align="center" width="172" fixed="right" class-name="small-padding fixed-width">
            <template #default="{ row }">
              <el-tooltip :content="t('common.edit')" placement="top" v-if="row.userId !== 1">
                <el-button link type="primary" icon="Edit" :aria-label="t('user.editUser')" :title="t('user.editUser')" @click="handleUpdate(row)" v-hasPermi="['system:user:edit']" />
              </el-tooltip>
              <el-tooltip :content="t('common.delete')" placement="top" v-if="row.userId !== 1">
                <el-button link type="primary" icon="Delete" :aria-label="t('user.deleteUser')" :title="t('user.deleteUser')" @click="handleDelete(row)" v-hasPermi="['system:user:remove']" />
              </el-tooltip>
              <el-tooltip :content="t('user.resetPassword')" placement="top" v-if="row.userId !== 1">
                <el-button link type="primary" icon="Key" :aria-label="t('user.resetUserPassword')" :title="t('user.resetUserPassword')" @click="handleResetPwd(row)" v-hasPermi="['system:user:resetPwd']" />
              </el-tooltip>
              <el-tooltip :content="t('user.assignRole')" placement="top" v-if="row.userId !== 1">
                <el-button link type="primary" icon="CircleCheck" :aria-label="t('user.assignUserRole')" :title="t('user.assignUserRole')" @click="handleAuthRole(row)" v-hasPermi="['system:user:edit']" />
              </el-tooltip>
            </template>
          </el-table-column>
        </el-table>

        <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
      </el-col>
    </el-row>

    <el-dialog v-model="open" :title="title" width="720px" class="user-edit-dialog" append-to-body destroy-on-close @closed="reset">
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
          <el-button type="primary" @click="submitForm">{{ t('common.confirm') }}</el-button>
          <el-button @click="cancel">{{ t('common.cancel') }}</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="upload.open" :title="upload.title" width="420px" append-to-body destroy-on-close @closed="resetUpload">
      <el-upload
        ref="uploadRef"
        :limit="1"
        accept=".xlsx, .xls"
        :headers="upload.headers"
        :action="`${upload.url}?updateSupport=${upload.updateSupport}`"
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
        <div class="dialog-footer">
          <el-button type="primary" @click="submitFileForm">{{ t('common.confirm') }}</el-button>
          <el-button @click="upload.open = false">{{ t('common.cancel') }}</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="UserPage">
import { computed, h, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules, type UploadInstance, type UploadProgressEvent, type UploadRawFile } from 'element-plus'
import { Briefcase } from '@element-plus/icons-vue'
import { getToken } from '@/utils/auth'
import { getConfigKey } from '@/api/system/config'
import { addUser, changeUserStatus, delUser, deptTreeSelect, getUser, listUser, resetUserPwd, updateUser, type SysUser, type TreeOption, type UserOptionPost, type UserOptionRole, type UserQuery } from '@/api/system/user'
import { download } from '@/utils/request'
import { formatUtc, withUtcDateRange } from '@/utils/datetime'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { useDict } from '@/utils/dict'
import { runUiAction } from '@/utils/action'
import { getApiBaseUrl } from '@/utils/config'

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

const columnLabelKeys = ['user.userName', 'user.nickName', 'user.deptName', 'user.phonenumber', 'user.status', 'common.createTime']
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
const showSearch = ref(true)
const ids = ref<Array<number | string>>([])
const total = ref(0)
const dateRange = ref<string[]>([])
const deptName = ref('')
const deptOptions = ref<TreeOption[]>([])
const initPassword = ref('')
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
  headers: { Authorization: `Bearer ${getToken()}` },
  url: `${getApiBaseUrl()}/system/user/importData`
})

const single = computed(() => ids.value.length !== 1)
const multiple = computed(() => ids.value.length === 0)
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
  phonenumber: [{ pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/, message: t('user.phonenumberInvalid'), trigger: 'blur' }]
}))

function getUserSexLabel(dict: DictOption) {
  return dict.value && userSexLabelKeys[dict.value] ? t(userSexLabelKeys[dict.value]) : dict.label || ''
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
    form.value.password = initPassword.value
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
    open.value = true
  })
}

function cancel() {
  open.value = false
  reset()
}

async function submitForm() {
  const valid = await userRef.value?.validate().catch(() => false)
  if (!valid) return
  try {
    if (form.value.userId !== undefined) {
      await updateUser(form.value)
      ElMessage.success(t('common.editSuccess'))
    } else {
      await addUser(form.value)
      ElMessage.success(t('common.addSuccess'))
    }
    open.value = false
    reset()
    await getList()
  } catch {
    // Request interceptor already displays the backend error.
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

function handleFileSuccess(response: { msg?: string }, file: UploadRawFile) {
  upload.open = false
  upload.isUploading = false
  uploadRef.value?.handleRemove(file)
  ElMessageBox.alert(
    h('div', { style: 'overflow: auto; overflow-x: hidden; max-height: 70vh; padding: 10px 20px 0; white-space: pre-wrap;' }, response.msg || ''),
    t('user.importResult')
  )
  getList()
}

function submitFileForm() {
  uploadRef.value?.submit()
}

getDeptTree()
getList()
getConfigKey('sys.user.initPassword').then((response) => {
  initPassword.value = response.msg || ''
})
</script>
