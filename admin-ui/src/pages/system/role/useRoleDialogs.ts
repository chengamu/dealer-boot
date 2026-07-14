import { computed, nextTick, ref, watch, type Ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { listMenu, roleMenuTreeselect, treeselect as menuTreeselect, type Menu } from '@/api/system/menu'
import { addRole, dataScope, deptTreeSelect, getRole, updateRole, type Role, type TreeOption } from '@/api/system/role'
import { useDict } from '@/utils/dict'
import { runUiAction } from '@/utils/action'
import { useFormCloseGuard } from '@/composables/useFormCloseGuard'
import { buildDefaultHomeOptions, flattenMenus } from './defaultHomeOptions'

type TreeType = 'menu' | 'dept'
type Translate = (key: string, params?: Record<string, string | number>) => string
type EditableRole = Role & { defaultMenuId?: number; defaultMenuName?: string }
type TreeRef = FormInstance & {
  setChecked?: (key: number | string, checked: boolean, deep?: boolean) => void
  setCheckedKeys?: (keys: Array<number | string>) => void
  setCheckedNodes?: (nodes: TreeOption[]) => void
  getCheckedKeys?: () => Array<number | string>
  getHalfCheckedKeys?: () => Array<number | string>
  store?: { nodesMap?: Record<string, { expanded: boolean }> }
}

export function useRoleDialogs(options: {
  getList: () => Promise<void>
  ids: Ref<Array<number | string>>
  t: Translate
}) {
  const { sys_normal_disable } = useDict('sys_normal_disable')
  const open = ref(false)
  const openDataScope = ref(false)
  const menuOptions = ref<TreeOption[]>([])
  const menuCatalog = ref<Menu[]>([])
  const menuExpand = ref(false)
  const menuNodeAll = ref(false)
  const deptExpand = ref(true)
  const deptNodeAll = ref(false)
  const deptOptions = ref<TreeOption[]>([])
  const selectedMenuIds = ref<Array<number | string>>([])
  const roleRef = ref<FormInstance>()
  const menuRef = ref<TreeRef>()
  const deptRef = ref<TreeRef>()
  const form = ref<EditableRole>({})

  const dataScopeOptions = computed(() => [
    { value: '1', label: options.t('role.allDataScope') },
    { value: '2', label: options.t('role.customDataScope') },
    { value: '3', label: options.t('role.deptDataScope') },
    { value: '4', label: options.t('role.deptAndChildDataScope') },
    { value: '5', label: options.t('role.selfDataScope') }
  ])
  const defaultHomeOptions = computed(() => buildDefaultHomeOptions(menuCatalog.value, selectedMenuIds.value))
  const title = computed(() => (form.value.roleId ? options.t('role.editRole') : options.t('role.addRole')))
  const rules = computed<FormRules<Role>>(() => ({
    roleName: [{ required: true, message: options.t('role.roleNameRequired'), trigger: 'blur' }],
    roleKey: [{ required: true, message: options.t('role.roleKeyRequired'), trigger: 'blur' }],
    roleSort: [{ required: true, message: options.t('role.roleSortRequired'), trigger: 'blur' }]
  }))
  const formCloseGuard = useFormCloseGuard({
    enabled: () => open.value,
    getSnapshot: () => JSON.stringify({ form: form.value || {}, menuIds: selectedMenuIds.value }),
    close: () => { open.value = false },
    reset,
    t: options.t
  })
  const dataScopeCloseGuard = useFormCloseGuard({
    enabled: () => openDataScope.value,
    getSnapshot: () => JSON.stringify({ form: form.value || {}, deptIds: getCheckedKeys(deptRef.value) }),
    close: () => { openDataScope.value = false },
    reset,
    t: options.t
  })

  watch(defaultHomeOptions, (items) => {
    const current = Number(form.value.defaultMenuId || 0)
    if (current && !items.some((item) => item.value === current)) form.value.defaultMenuId = undefined
  }, { deep: true })

  function getCheckedKeys(tree?: TreeRef) {
    const checkedKeys = tree?.getCheckedKeys?.() || []
    const halfCheckedKeys = tree?.getHalfCheckedKeys?.() || []
    return [...halfCheckedKeys, ...checkedKeys]
  }

  function syncSelectedMenuIds() {
    selectedMenuIds.value = getCheckedKeys(menuRef.value)
  }

  function reset() {
    menuRef.value?.setCheckedKeys?.([])
    deptRef.value?.setCheckedKeys?.([])
    selectedMenuIds.value = []
    menuExpand.value = false
    menuNodeAll.value = false
    deptExpand.value = true
    deptNodeAll.value = false
    form.value = {
      roleName: undefined,
      roleKey: undefined,
      roleSort: 0,
      status: '1',
      defaultMenuId: undefined,
      menuIds: [],
      deptIds: [],
      menuCheckStrictly: true,
      deptCheckStrictly: true,
      remark: undefined
    }
    roleRef.value?.resetFields()
  }

  async function loadMenuCatalog() {
    const response = await listMenu()
    menuCatalog.value = flattenMenus(response.data || [])
  }

  async function getMenuTree(roleId?: number | string) {
    if (!roleId) {
      const response = await menuTreeselect() as { data?: TreeOption[] }
      menuOptions.value = response.data || []
      return []
    }
    const response = await roleMenuTreeselect(roleId)
    menuOptions.value = response.data?.menus || []
    return response.data?.checkedKeys || []
  }

  function getDeptTree(roleId: number | string) {
    return deptTreeSelect(roleId).then((response) => {
      deptOptions.value = response.data?.depts || []
      return response
    })
  }

  async function handleAdd() {
    reset()
    await runUiAction(async () => {
      await Promise.all([getMenuTree(), loadMenuCatalog()])
      syncSelectedMenuIds()
      open.value = true
      formCloseGuard.markPristine()
    })
  }

  async function handleUpdate(row?: Role) {
    reset()
    const roleId = row?.roleId || options.ids.value[0]
    if (!roleId) return
    await runUiAction(async () => {
      const [checkedKeys, response] = await Promise.all([getMenuTree(roleId), getRole(roleId), loadMenuCatalog()])
      form.value = { ...(response.data as EditableRole), roleSort: Number(response.data.roleSort ?? 0) }
      open.value = true
      await nextTick()
      checkedKeys.forEach((key) => menuRef.value?.setChecked?.(key, true, false))
      syncSelectedMenuIds()
      formCloseGuard.markPristine()
    })
  }

  function handleCheckedTreeExpand(value: boolean, type: TreeType) {
    const targetOptions = type === 'menu' ? menuOptions.value : deptOptions.value
    const targetRef = type === 'menu' ? menuRef.value : deptRef.value
    targetOptions.forEach((item) => {
      const node = targetRef?.store?.nodesMap?.[String(item.id)]
      if (node) node.expanded = value
    })
  }

  function handleCheckedTreeNodeAll(value: boolean, type: TreeType) {
    if (type === 'menu') {
      menuRef.value?.setCheckedNodes?.(value ? menuOptions.value : [])
      syncSelectedMenuIds()
      return
    }
    deptRef.value?.setCheckedNodes?.(value ? deptOptions.value : [])
  }

  function handleCheckedTreeConnect(value: boolean, type: TreeType) {
    if (type === 'menu') form.value.menuCheckStrictly = value
    else form.value.deptCheckStrictly = value
  }

  async function submitForm() {
    const valid = await roleRef.value?.validate().catch(() => false)
    if (!valid) return
    form.value.menuIds = getCheckedKeys(menuRef.value)
    try {
      if (form.value.roleId) {
        await updateRole(form.value)
        ElMessage.success(options.t('common.editSuccess'))
      } else {
        await addRole(form.value)
        ElMessage.success(options.t('common.addSuccess'))
      }
      formCloseGuard.markPristine()
      open.value = false
      await options.getList()
    } catch {
      // Request interceptor already displays the backend error.
    }
  }

  function cancel() {
    formCloseGuard.closeWithGuard()
  }

  function dataScopeSelectChange(value: string) {
    if (value !== '2') deptRef.value?.setCheckedKeys?.([])
  }

  async function handleDataScope(row: Role) {
    reset()
    const roleId = row.roleId
    if (!roleId) return
    await runUiAction(async () => {
      const [response, deptTreeResponse] = await Promise.all([getRole(roleId), getDeptTree(roleId)])
      form.value = response.data as EditableRole
      openDataScope.value = true
      await nextTick()
      deptRef.value?.setCheckedKeys?.(deptTreeResponse.data?.checkedKeys || [])
      dataScopeCloseGuard.markPristine()
    })
  }

  async function submitDataScope() {
    if (!form.value.roleId) return
    form.value.deptIds = getCheckedKeys(deptRef.value)
    try {
      await dataScope(form.value)
      ElMessage.success(options.t('common.editSuccess'))
      dataScopeCloseGuard.markPristine()
      openDataScope.value = false
      await options.getList()
    } catch {
      // Request interceptor already displays the backend error.
    }
  }

  function cancelDataScope() {
    dataScopeCloseGuard.closeWithGuard()
  }

  return {
    sys_normal_disable,
    open,
    openDataScope,
    menuOptions,
    menuExpand,
    menuNodeAll,
    deptExpand,
    deptNodeAll,
    deptOptions,
    roleRef,
    menuRef,
    deptRef,
    form,
    dataScopeOptions,
    defaultHomeOptions,
    title,
    rules,
    formCloseGuard,
    dataScopeCloseGuard,
    handleAdd,
    handleUpdate,
    handleCheckedTreeExpand,
    handleCheckedTreeNodeAll,
    handleCheckedTreeConnect,
    submitForm,
    cancel,
    dataScopeSelectChange,
    handleDataScope,
    submitDataScope,
    cancelDataScope,
    syncSelectedMenuIds
  }
}
