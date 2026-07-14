import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { useI18n } from 'vue-i18n'
import {
  SALES_STORE_STATUS,
  changeSalesStoreStatus,
  createSalesStore,
  getSalesStore,
  listSalesStoreDeptOptions,
  listSalesStores,
  updateSalesStore,
  type SalesStore,
  type SalesStoreDeptOption,
  type SalesStoreQuery
} from '@/api/system/sales-store'
import { useFormCloseGuard } from '@/composables/useFormCloseGuard'
import { useDict } from '@/utils/dict'
import { checkPermi } from '@/utils/permission'
import {
  buildSalesStorePayload,
  createEmptySalesStoreForm,
  isEnabledSalesStore,
  normalizeSalesStoreDeptOptions
} from './salesStorePageSupport'

interface SalesStoreFormExpose {
  validate: () => Promise<boolean>
}

export function useSalesStorePage() {
  const { t } = useI18n()
  const { sys_normal_disable } = useDict('sys_normal_disable')
  const queryRef = ref<FormInstance>()
  const formRef = ref<SalesStoreFormExpose>()
  const loading = ref(false)
  const rows = ref<SalesStore[]>([])
  const total = ref(0)
  const showSearch = ref(true)
  const deptOptions = ref<SalesStoreDeptOption[]>([])
  const editorOpen = ref(false)
  const editorMode = ref<'add' | 'edit'>('add')
  const editorForm = ref<SalesStore>(createEmptySalesStoreForm())
  const pendingStatusStoreId = ref<number>()
  const disableCheckOpen = ref(false)
  const disableCheckRow = ref<SalesStore>()
  const queryParams = reactive<SalesStoreQuery>({
    pageNum: 1,
    pageSize: 10,
    storeCode: '',
    storeName: '',
    deptId: undefined,
    status: ''
  })

  const editorTitle = computed(() => t(editorMode.value === 'edit' ? 'salesStore.editTitle' : 'salesStore.addTitle'))
  const showOperationColumn = computed(() => checkPermi(['system:sales-store:edit']) || checkPermi(['system:sales-store:status']))
  const canOpenEditorByRow = computed(() => checkPermi(['system:sales-store:edit']))
  const editorCloseGuard = useFormCloseGuard({
    enabled: () => editorOpen.value,
    getSnapshot: () => JSON.stringify(buildSalesStorePayload(editorForm.value)),
    close: () => {
      editorOpen.value = false
    },
    reset: resetEditorState,
    t
  })

  async function getList() {
    loading.value = true
    try {
      const response = await listSalesStores(queryParams)
      rows.value = response.rows || []
      total.value = response.total || 0
    } finally {
      loading.value = false
    }
  }

  async function loadDeptOptions(currentDeptId?: number, currentDeptName?: string) {
    const options = await listSalesStoreDeptOptions()
    deptOptions.value = normalizeSalesStoreDeptOptions(options || [], currentDeptId, currentDeptName)
  }

  function handleQuery() {
    queryParams.pageNum = 1
    void getList()
  }

  function resetQuery() {
    queryRef.value?.resetFields()
    queryParams.pageNum = 1
    queryParams.deptId = undefined
    queryParams.status = ''
    void getList()
  }

  function resetEditorState() {
    editorMode.value = 'add'
    editorForm.value = createEmptySalesStoreForm()
  }

  async function openAdd() {
    resetEditorState()
    await loadDeptOptions()
    editorCloseGuard.markPristine()
    editorOpen.value = true
  }

  async function openEdit(row: SalesStore) {
    if (!row.salesStoreId) return
    const [detail] = await Promise.all([
      getSalesStore(row.salesStoreId),
      loadDeptOptions(row.deptId, row.deptName)
    ])
    editorMode.value = 'edit'
    editorForm.value = {
      ...createEmptySalesStoreForm(),
      ...detail,
      currencyCode: detail.currencyCode || 'USD',
      status: detail.status || row.status || SALES_STORE_STATUS.ENABLED,
      deptName: row.deptName || detail.deptName || ''
    }
    editorCloseGuard.markPristine()
    editorOpen.value = true
  }

  async function submitEditor() {
    const valid = await formRef.value?.validate()
    if (!valid) return
    const payload = buildSalesStorePayload(editorForm.value)
    if (editorMode.value === 'edit' && payload.salesStoreId) {
      await updateSalesStore(payload)
      ElMessage.success(t('common.editSuccess'))
    } else {
      await createSalesStore(payload)
      ElMessage.success(t('common.addSuccess'))
    }
    editorCloseGuard.markPristine()
    editorOpen.value = false
    await Promise.all([getList(), loadDeptOptions()])
  }

  function buildRowActions(row: SalesStore) {
    const enabled = isEnabledSalesStore(row.status)
    return [
      {
        label: t('common.edit'),
        icon: 'Edit',
        permission: 'system:sales-store:edit',
        primary: true,
        onClick: () => openEdit(row)
      },
      {
        label: t(enabled ? 'common.disable' : 'common.enable'),
        icon: 'Switch',
        permission: 'system:sales-store:status',
        loading: pendingStatusStoreId.value === row.salesStoreId,
        onClick: () => enabled ? openDisableCheck(row) : enableStore(row)
      }
    ]
  }

  async function enableStore(row: SalesStore) {
    if (!row.salesStoreId) return
    try {
      await ElMessageBox.confirm(
        t('salesStore.enableConfirm', { name: row.storeName || row.storeCode || '' }),
        t('common.prompt'),
        { type: 'warning' }
      )
      pendingStatusStoreId.value = row.salesStoreId
      await changeSalesStoreStatus(row.salesStoreId, SALES_STORE_STATUS.ENABLED)
      ElMessage.success(t('salesStore.enableSuccess'))
      await Promise.all([getList(), loadDeptOptions()])
    } catch {
      // User cancelled or the request interceptor already displayed the backend error.
    } finally {
      pendingStatusStoreId.value = undefined
    }
  }

  async function openDisableCheck(row: SalesStore) {
    if (!row.salesStoreId) return
    disableCheckRow.value = row
    disableCheckOpen.value = true
  }

  async function confirmDisable() {
    if (!disableCheckRow.value?.salesStoreId) return
    pendingStatusStoreId.value = disableCheckRow.value.salesStoreId
    try {
      await changeSalesStoreStatus(disableCheckRow.value.salesStoreId, SALES_STORE_STATUS.DISABLED)
      ElMessage.success(t('salesStore.disableSuccess'))
      disableCheckOpen.value = false
      await Promise.all([getList(), loadDeptOptions()])
    } finally {
      pendingStatusStoreId.value = undefined
    }
  }

  function handleDisableCheckClosed() {
    disableCheckRow.value = undefined
  }

  function closeEditor() {
    void editorCloseGuard.closeWithGuard()
  }

  function handleEditorClosed() {
    editorCloseGuard.handleClosed()
  }

  function handleRowDblclick(row: SalesStore) {
    if (!canOpenEditorByRow.value) return
    void openEdit(row)
  }

  function isDeptOptionDisabled(option: SalesStoreDeptOption) {
    return option.linked && option.deptId !== editorForm.value.deptId
  }

  void Promise.all([getList(), loadDeptOptions()])

  return {
    t,
    sys_normal_disable,
    queryRef,
    formRef,
    rows,
    total,
    loading,
    showSearch,
    deptOptions,
    queryParams,
    editorOpen,
    editorMode,
    editorForm,
    editorTitle,
    editorCloseGuard,
    disableCheckOpen,
    disableCheckRow,
    pendingStatusStoreId,
    showOperationColumn,
    getList,
    handleQuery,
    resetQuery,
    openAdd,
    openEdit,
    submitEditor,
    buildRowActions,
    confirmDisable,
    closeEditor,
    handleEditorClosed,
    handleDisableCheckClosed,
    handleRowDblclick,
    isDeptOptionDisabled
  }
}
