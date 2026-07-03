<template>
  <div class="app-container role-page">
    <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true" label-width="80px">
      <el-form-item :label="t('role.roleName')" prop="roleName">
        <el-input v-model="queryParams.roleName" :placeholder="t('role.roleNamePlaceholder')" clearable style="width: 240px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item :label="t('role.roleKey')" prop="roleKey">
        <el-input v-model="queryParams.roleKey" :placeholder="t('role.roleKeyPlaceholder')" clearable style="width: 240px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item :label="t('user.status')" prop="status">
        <el-select v-model="queryParams.status" :placeholder="t('role.roleStatusPlaceholder')" clearable style="width: 240px">
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
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['system:role:add']">{{ t('common.add') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()" v-hasPermi="['system:role:edit']">{{ t('common.edit') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['system:role:remove']">{{ t('common.delete') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['system:role:export']">{{ t('common.export') }}</el-button>
      </el-col>
      <span class="selection-count">{{ t('common.selectedCount', { count: ids.length }) }}</span>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="roleList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column :label="t('role.roleName')" prop="roleName" :show-overflow-tooltip="true" width="150" />
      <el-table-column :label="t('role.roleKey')" prop="roleKey" :show-overflow-tooltip="true" width="150" />
      <el-table-column :label="t('role.roleSort')" prop="roleSort" width="100" />
      <el-table-column :label="t('user.status')" align="center" width="100">
        <template #default="{ row }">
          <el-switch v-model="row.status" active-value="1" inactive-value="0" @change="handleStatusChange(row)" />
        </template>
      </el-table-column>
      <el-table-column :label="t('common.createTime')" align="center" prop="createTime" width="170">
        <template #default="{ row }">
          <span>{{ formatUtc(row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column v-if="showOperationColumn" :label="t('common.operate')" align="center" width="150" fixed="right" class-name="small-padding fixed-width">
        <template #default="{ row }">
          <AdminTableActions :actions="roleRowActions(row)" />
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      :total="total"
      @pagination="getList"
    />

    <AdminDrawer
      v-model="open"
      :title="title"
      size="560px"
      append-to-body
      destroy-on-close
      :close-on-click-modal="false"
      :before-close="formCloseGuard.beforeClose"
      @closed="formCloseGuard.handleClosed"
    >
      <el-form ref="roleRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item :label="t('role.roleName')" prop="roleName">
          <el-input v-model="form.roleName" :placeholder="t('role.roleNamePlaceholder')" />
        </el-form-item>
        <el-form-item prop="roleKey">
          <template #label>
            <span>
              <el-tooltip :content="t('role.roleKeyTooltip')" placement="top">
                <el-icon><question-filled /></el-icon>
              </el-tooltip>
              {{ t('role.roleKey') }}
            </span>
          </template>
          <el-input v-model="form.roleKey" :placeholder="t('role.roleKeyPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('role.roleSort')" prop="roleSort">
          <el-input-number v-model="form.roleSort" controls-position="right" :min="0" />
        </el-form-item>
        <el-form-item :label="t('user.status')">
          <el-radio-group v-model="form.status">
            <el-radio v-for="dict in sys_normal_disable" :key="dict.value" :value="dict.value">{{ dict.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="t('role.menuPermission')">
          <div class="permission-tree-block">
            <p class="permission-tree-hint">{{ t('role.menuPermissionHint') }}</p>
            <div class="permission-tree-actions">
              <el-checkbox v-model="menuExpand" @change="handleCheckedTreeExpand($event, 'menu')">{{ t('common.expandCollapse') }}</el-checkbox>
              <el-checkbox v-model="menuNodeAll" @change="handleCheckedTreeNodeAll($event, 'menu')">{{ t('role.selectAllNone') }}</el-checkbox>
              <el-checkbox v-model="form.menuCheckStrictly" @change="handleCheckedTreeConnect($event, 'menu')">{{ t('role.parentChildLink') }}</el-checkbox>
            </div>
          </div>
          <el-tree
            ref="menuRef"
            class="tree-border"
            :data="menuOptions"
            show-checkbox
            node-key="id"
            :check-strictly="!form.menuCheckStrictly"
            :empty-text="t('common.loading')"
            :props="{ label: 'label', children: 'children' }"
          />
        </el-form-item>
        <el-form-item :label="t('user.remark')">
          <el-input v-model="form.remark" type="textarea" :placeholder="t('user.remarkPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">{{ t('common.confirm') }}</el-button>
          <el-button @click="cancel">{{ t('common.cancel') }}</el-button>
        </div>
      </template>
    </AdminDrawer>

    <AdminDrawer
      v-model="openDataScope"
      :title="t('role.assignDataScope')"
      size="560px"
      append-to-body
      destroy-on-close
      :close-on-click-modal="false"
      :before-close="dataScopeCloseGuard.beforeClose"
      @closed="dataScopeCloseGuard.handleClosed"
    >
      <el-form :model="form" label-width="90px">
        <el-form-item :label="t('role.roleName')">
          <el-input v-model="form.roleName" disabled />
        </el-form-item>
        <el-form-item :label="t('role.roleKey')">
          <el-input v-model="form.roleKey" disabled />
        </el-form-item>
        <el-form-item :label="t('role.scopeRange')">
          <el-select v-model="form.dataScope" @change="dataScopeSelectChange">
            <el-option v-for="item in dataScopeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item v-show="form.dataScope === '2'" :label="t('role.dataPermission')">
          <div class="permission-tree-block">
            <p class="permission-tree-hint">{{ t('role.dataPermissionHint') }}</p>
            <div class="permission-tree-actions">
              <el-checkbox v-model="deptExpand" @change="handleCheckedTreeExpand($event, 'dept')">{{ t('common.expandCollapse') }}</el-checkbox>
              <el-checkbox v-model="deptNodeAll" @change="handleCheckedTreeNodeAll($event, 'dept')">{{ t('role.selectAllNone') }}</el-checkbox>
              <el-checkbox v-model="form.deptCheckStrictly" @change="handleCheckedTreeConnect($event, 'dept')">{{ t('role.parentChildLink') }}</el-checkbox>
            </div>
          </div>
          <el-tree
            ref="deptRef"
            class="tree-border"
            :data="deptOptions"
            show-checkbox
            default-expand-all
            node-key="id"
            :check-strictly="!form.deptCheckStrictly"
            :empty-text="t('common.loading')"
            :props="{ label: 'label', children: 'children' }"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitDataScope">{{ t('common.confirm') }}</el-button>
          <el-button @click="cancelDataScope">{{ t('common.cancel') }}</el-button>
        </div>
      </template>
    </AdminDrawer>
  </div>
</template>

<script setup lang="ts" name="RolePage">
import { computed, nextTick, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { addRole, changeRoleStatus, dataScope, delRole, deptTreeSelect, getRole, listRole, updateRole, type Role, type RoleQuery, type TreeOption } from '@/api/system/role'
import { roleMenuTreeselect, treeselect as menuTreeselect } from '@/api/system/menu'
import { download } from '@/utils/request'
import { formatUtc, withUtcDateRange } from '@/utils/datetime'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { useDict } from '@/utils/dict'
import { runUiAction } from '@/utils/action'
import { useFormCloseGuard } from '@/composables/useFormCloseGuard'

type TreeType = 'menu' | 'dept'

const router = useRouter()
const localeStore = useLocaleStore()
const t = (key: string, params?: Record<string, string | number>) => {
  const message = getMessage(key, localeStore.language)
  if (!params) return message
  return Object.entries(params).reduce((text, [name, value]) => text.replaceAll(`{${name}}`, String(value)), message)
}
const { sys_normal_disable } = useDict('sys_normal_disable')

const roleList = ref<Role[]>([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref<Array<number | string>>([])
const total = ref(0)
const dateRange = ref<string[]>([])
const menuOptions = ref<TreeOption[]>([])
const menuExpand = ref(false)
const menuNodeAll = ref(false)
const deptExpand = ref(true)
const deptNodeAll = ref(false)
const deptOptions = ref<TreeOption[]>([])
const openDataScope = ref(false)
const queryRef = ref<FormInstance>()
const roleRef = ref<FormInstance>()
const menuRef = ref()
const deptRef = ref()
const form = ref<Role>({})
const queryParams = reactive<RoleQuery>({
  pageNum: 1,
  pageSize: 10
})

const dataScopeOptions = computed(() => [
  { value: '1', label: t('role.allDataScope') },
  { value: '2', label: t('role.customDataScope') },
  { value: '3', label: t('role.deptDataScope') },
  { value: '4', label: t('role.deptAndChildDataScope') },
  { value: '5', label: t('role.selfDataScope') }
])
const single = computed(() => ids.value.length !== 1)
const multiple = computed(() => ids.value.length === 0)
const showOperationColumn = computed(() => roleList.value.some((row) => roleRowActions(row).some((action) => !action.hidden)))
const title = computed(() => (form.value.roleId ? t('role.editRole') : t('role.addRole')))
const rules = computed<FormRules<Role>>(() => ({
  roleName: [{ required: true, message: t('role.roleNameRequired'), trigger: 'blur' }],
  roleKey: [{ required: true, message: t('role.roleKeyRequired'), trigger: 'blur' }],
  roleSort: [{ required: true, message: t('role.roleSortRequired'), trigger: 'blur' }]
}))
const formCloseGuard = useFormCloseGuard({
  enabled: () => open.value,
  getSnapshot: () => JSON.stringify({
    form: form.value || {},
    menuIds: getCheckedKeys(menuRef.value || {})
  }),
  close: () => {
    open.value = false
  },
  reset,
  t
})
const dataScopeCloseGuard = useFormCloseGuard({
  enabled: () => openDataScope.value,
  getSnapshot: () => JSON.stringify({
    form: form.value || {},
    deptIds: getCheckedKeys(deptRef.value || {})
  }),
  close: () => {
    openDataScope.value = false
  },
  reset,
  t
})

function withDateRange(query: RoleQuery) {
  return withUtcDateRange(query, dateRange.value)
}

async function getList() {
  loading.value = true
  try {
    const response = await listRole(withDateRange(queryParams))
    roleList.value = response.rows || []
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
  dateRange.value = []
  queryRef.value?.resetFields()
  handleQuery()
}

async function handleDelete(row?: Role) {
  const roleIds = row?.roleId || ids.value
  if (!roleIds || (Array.isArray(roleIds) && !roleIds.length)) return
  try {
    await ElMessageBox.confirm(t('role.deleteRoleConfirm', { ids: Array.isArray(roleIds) ? roleIds.join(',') : roleIds }), t('common.prompt'), { type: 'warning' })
    await delRole(roleIds)
    ElMessage.success(t('common.deleteSuccess'))
    await getList()
  } catch {
    // User cancelled or the request interceptor already displayed the backend error.
  }
}

function roleRowActions(row: Role) {
  const hidden = row.roleId === 1
  return [
    { label: t('common.edit'), icon: 'Edit', permission: 'system:role:edit', hidden, primary: true, onClick: () => handleUpdate(row) },
    { label: t('role.dataScope'), icon: 'Connection', permission: 'system:role:edit', hidden, onClick: () => handleDataScope(row) },
    { label: t('role.assignUsers'), icon: 'UserFilled', permission: 'system:role:edit', hidden, onClick: () => handleAuthUser(row) },
    { label: t('common.delete'), icon: 'Delete', type: 'danger' as const, permission: 'system:role:remove', hidden, onClick: () => handleDelete(row) }
  ]
}

function handleExport() {
  download('system/role/export', withDateRange(queryParams), `role_${Date.now()}.xlsx`)
}

function handleSelectionChange(selection: Role[]) {
  ids.value = selection.map((item) => String(item.roleId)).filter(Boolean)
}

async function handleStatusChange(row: Role) {
  const status = row.status || '0'
  const action = status === '1' ? t('legacy.enable') : t('legacy.disable')
  try {
    if (!row.roleId) return
    await ElMessageBox.confirm(t('role.changeStatusConfirm', { action, name: row.roleName || '' }), t('common.prompt'), { type: 'warning' })
    await changeRoleStatus(row.roleId, status)
    ElMessage.success(t('legacy.statusChangeSuccess', { action }))
  } catch {
    row.status = status === '0' ? '1' : '0'
  }
}

function handleAuthUser(row: Role) {
  if (!row.roleId) return
  router.push(`/system/role-auth/user/${row.roleId}`)
}

async function getMenuTreeselect() {
  const response = await menuTreeselect() as { data?: TreeOption[] }
  menuOptions.value = response.data || []
}

function getCheckedKeys(treeRef: { getCheckedKeys?: () => Array<number | string>; getHalfCheckedKeys?: () => Array<number | string> }) {
  const checkedKeys = treeRef.getCheckedKeys?.() || []
  const halfCheckedKeys = treeRef.getHalfCheckedKeys?.() || []
  checkedKeys.unshift(...halfCheckedKeys)
  return checkedKeys
}

function reset() {
  menuRef.value?.setCheckedKeys([])
  menuExpand.value = false
  menuNodeAll.value = false
  deptExpand.value = true
  deptNodeAll.value = false
  form.value = {
    roleName: undefined,
    roleKey: undefined,
    roleSort: 0,
    status: '1',
    menuIds: [],
    deptIds: [],
    menuCheckStrictly: true,
    deptCheckStrictly: true,
    remark: undefined
  }
  roleRef.value?.resetFields()
}

async function handleAdd() {
  reset()
  await runUiAction(async () => {
    await getMenuTreeselect()
    formCloseGuard.markPristine()
    open.value = true
  })
}

async function handleUpdate(row?: Role) {
  reset()
  const roleId = row?.roleId || ids.value[0]
  if (!roleId) return
  await runUiAction(async () => {
    const roleMenu = getRoleMenuTreeselect(roleId)
    const response = await getRole(roleId)
    form.value = {
      ...response.data,
      roleSort: Number(response.data.roleSort)
    }
    open.value = true
    await nextTick()
    const roleMenuResponse = await roleMenu
    const checkedKeys = roleMenuResponse.data?.checkedKeys || []
    checkedKeys.forEach((key: number | string) => {
      menuRef.value?.setChecked(key, true, false)
    })
    formCloseGuard.markPristine()
  })
}

function getRoleMenuTreeselect(roleId: number | string) {
  return roleMenuTreeselect(roleId).then((response) => {
    menuOptions.value = response.data?.menus || []
    return response
  })
}

function getDeptTree(roleId: number | string) {
  return deptTreeSelect(roleId).then((response) => {
    deptOptions.value = response.data?.depts || []
    return response
  })
}

function handleCheckedTreeExpand(value: boolean, type: TreeType) {
  const options = type === 'menu' ? menuOptions.value : deptOptions.value
  const targetRef = type === 'menu' ? menuRef.value : deptRef.value
  options.forEach((item) => {
    const node = targetRef?.store?.nodesMap?.[item.id]
    if (node) node.expanded = value
  })
}

function handleCheckedTreeNodeAll(value: boolean, type: TreeType) {
  if (type === 'menu') {
    menuRef.value?.setCheckedNodes(value ? menuOptions.value : [])
    return
  }
  deptRef.value?.setCheckedNodes(value ? deptOptions.value : [])
}

function handleCheckedTreeConnect(value: boolean, type: TreeType) {
  if (type === 'menu') {
    form.value.menuCheckStrictly = value
    return
  }
  form.value.deptCheckStrictly = value
}

async function submitForm() {
  const valid = await roleRef.value?.validate().catch(() => false)
  if (!valid) return
  form.value.menuIds = getCheckedKeys(menuRef.value || {})
  try {
    if (form.value.roleId) {
      await updateRole(form.value)
      ElMessage.success(t('common.editSuccess'))
    } else {
      await addRole(form.value)
      ElMessage.success(t('common.addSuccess'))
    }
    formCloseGuard.markPristine()
    open.value = false
    await getList()
  } catch {
    // Request interceptor already displays the backend error.
  }
}

function cancel() {
  formCloseGuard.closeWithGuard()
}

function dataScopeSelectChange(value: string) {
  if (value !== '2') deptRef.value?.setCheckedKeys([])
}

async function handleDataScope(row: Role) {
  reset()
  if (!row.roleId) return
  await runUiAction(async () => {
    const roleId = row.roleId as number | string
    const deptTree = getDeptTree(roleId)
    const response = await getRole(roleId)
    form.value = response.data
    openDataScope.value = true
    await nextTick()
    const deptTreeResponse = await deptTree
    deptRef.value?.setCheckedKeys(deptTreeResponse.data?.checkedKeys || [])
    dataScopeCloseGuard.markPristine()
  })
}

async function submitDataScope() {
  if (!form.value.roleId) return
  form.value.deptIds = getCheckedKeys(deptRef.value || {})
  try {
    await dataScope(form.value)
    ElMessage.success(t('common.editSuccess'))
    dataScopeCloseGuard.markPristine()
    openDataScope.value = false
    await getList()
  } catch {
    // Request interceptor already displays the backend error.
  }
}

function cancelDataScope() {
  dataScopeCloseGuard.closeWithGuard()
}

getList()
</script>
