import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { changeRoleStatus, delRole, listRole, type Role, type RoleQuery } from '@/api/system/role'
import { download } from '@/utils/request'
import { formatUtc, withUtcDateRange } from '@/utils/datetime'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { useRoleDialogs } from './useRoleDialogs'

export function useRolePage() {
  const router = useRouter()
  const localeStore = useLocaleStore()
  const t = (key: string, params?: Record<string, string | number>) => {
    const message = getMessage(key, localeStore.language)
    if (!params) return message
    return Object.entries(params).reduce((text, [name, value]) => text.replaceAll(`{${name}}`, String(value)), message)
  }

  const roleList = ref<Role[]>([])
  const loading = ref(true)
  const showSearch = ref(true)
  const ids = ref<Array<number | string>>([])
  const total = ref(0)
  const dateRange = ref<string[]>([])
  const queryRef = ref<FormInstance>()
  const queryParams = reactive<RoleQuery>({ pageNum: 1, pageSize: 10 })
  const dialogs = useRoleDialogs({ ids, getList, t })

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
    void getList()
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
      { label: t('common.edit'), icon: 'Edit', permission: 'system:role:edit', hidden, primary: true, onClick: () => dialogs.handleUpdate(row) },
      { label: t('role.dataScope'), icon: 'Connection', permission: 'system:role:edit', hidden, onClick: () => dialogs.handleDataScope(row) },
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
    void router.push(`/system/role-auth/user/${row.roleId}`)
  }

  void getList()

  return {
    t,
    roleList,
    loading,
    showSearch,
    ids,
    total,
    dateRange,
    queryRef,
    queryParams,
    formatUtc,
    getList,
    handleQuery,
    resetQuery,
    handleDelete,
    roleRowActions,
    handleExport,
    handleSelectionChange,
    handleStatusChange,
    ...dialogs,
    single: computed(() => ids.value.length !== 1),
    multiple: computed(() => ids.value.length === 0),
    showOperationColumn: computed(() => roleList.value.some((row) => roleRowActions(row).some((action) => !action.hidden)))
  }
}
