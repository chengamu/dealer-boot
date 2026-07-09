<template>
  <div class="app-container dict-data-page system-table-page">
    <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true" class="system-table-page__search">
      <el-form-item :label="t('legacy.dictType')" prop="dictType">
        <el-select v-model="queryParams.dictType" style="width: 200px">
          <el-option v-for="item in typeOptions" :key="item.dictId" :label="item.dictName" :value="item.dictType" />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('legacy.dictLabel')" prop="dictLabel">
        <el-input
          v-model="queryParams.dictLabel"
          :placeholder="t('legacy.dictLabelPlaceholder')"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item :label="t('user.status')" prop="status">
        <el-select v-model="queryParams.status" :placeholder="t('legacy.dictDataStatusPlaceholder')" clearable style="width: 200px">
          <el-option v-for="dict in sys_normal_disable" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">{{ t('common.search') }}</el-button>
        <el-button icon="Refresh" @click="resetQuery">{{ t('common.reset') }}</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8 system-table-page__toolbar">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['system:dict:add']">
          {{ t('common.add') }}
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()" v-hasPermi="['system:dict:edit']">
          {{ t('common.edit') }}
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['system:dict:remove']">
          {{ t('common.delete') }}
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['system:dict:export']">
          {{ t('common.export') }}
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Close" @click="handleClose">
          {{ t('common.close') }}
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="dataList" border class="system-table-page__table" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column :label="t('legacy.dictLabel')" align="left" prop="dictLabel" :show-overflow-tooltip="true">
        <template #default="{ row }">
          <span v-if="isPlainLabel(row)">{{ row.dictLabel }}</span>
          <el-tag v-else :type="row.listClass === 'primary' ? '' : row.listClass" :class="row.cssClass">{{ row.dictLabel }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="t('legacy.dictValue')" align="left" prop="dictValue" :show-overflow-tooltip="true" />
      <el-table-column :label="t('legacy.dictSort')" align="right" prop="dictSort" />
      <el-table-column :label="t('user.status')" align="center" prop="status">
        <template #default="{ row }">
          <dict-tag :options="sys_normal_disable" :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column :label="t('user.remark')" align="left" prop="remark" :show-overflow-tooltip="true" />
      <el-table-column :label="t('common.createTime')" align="center" prop="createTime" width="180">
        <template #default="{ row }">
          <span>{{ formatUtc(row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="t('common.operate')" align="center" width="150" class-name="small-padding fixed-width">
        <template #default="{ row }">
          <AdminTableActions :actions="[
            { label: t('common.edit'), icon: 'Edit', permission: 'system:dict:edit', onClick: () => handleUpdate(row) },
            { label: t('common.delete'), icon: 'Delete', type: 'danger', permission: 'system:dict:remove', onClick: () => handleDelete(row) }
          ]" />
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      :total="total"
      class="system-table-page__pagination"
      @pagination="getList"
    />

    <AdminDrawer
      v-model="open"
      :title="title"
      size="520px"
      append-to-body
      destroy-on-close
      :close-on-click-modal="false"
      :before-close="formCloseGuard.beforeClose"
      @closed="formCloseGuard.handleClosed"
    >
      <el-form ref="dataRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item :label="t('legacy.dictType')">
          <el-input v-model="form.dictType" disabled />
        </el-form-item>
        <el-form-item :label="t('legacy.dictLabel')" prop="dictLabel">
          <el-input v-model="form.dictLabel" :placeholder="t('legacy.dictLabelPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('legacy.dictValue')" prop="dictValue">
          <el-input v-model="form.dictValue" :placeholder="t('legacy.dictValuePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('legacy.cssClass')" prop="cssClass">
          <el-input v-model="form.cssClass" :placeholder="t('legacy.cssClassPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('legacy.dictSort')" prop="dictSort">
          <el-input-number v-model="form.dictSort" controls-position="right" :min="0" />
        </el-form-item>
        <el-form-item :label="t('legacy.listClass')" prop="listClass">
          <el-select v-model="form.listClass">
            <el-option v-for="item in listClassOptions" :key="item.value" :label="`${item.label}(${item.value})`" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('user.status')" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio v-for="dict in sys_normal_disable" :key="dict.value" :value="dict.value">{{ dict.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="t('user.remark')" prop="remark">
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
  </div>
</template>

<script setup lang="ts" name="DictDataPage">
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { addData, delData, getData, listData, updateData, type DictData, type DictDataQuery } from '@/api/system/dict/data'
import { getType, optionselect, type DictType } from '@/api/system/dict/type'
import { download } from '@/utils/request'
import { formatUtc } from '@/utils/datetime'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { useDict } from '@/utils/dict'
import { useDictStore } from '@/stores/dict'
import { useFormCloseGuard } from '@/composables/useFormCloseGuard'

const route = useRoute()
const router = useRouter()
const localeStore = useLocaleStore()
const t = (key: string, params?: Record<string, string | number>) => {
  const message = getMessage(key, localeStore.language)
  if (!params) return message
  return Object.entries(params).reduce((text, [name, value]) => text.replaceAll(`{${name}}`, String(value)), message)
}
const { sys_normal_disable } = useDict('sys_normal_disable')

const dataList = ref<DictData[]>([])
const typeOptions = ref<DictType[]>([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref<Array<number | string>>([])
const total = ref(0)
const defaultDictType = ref('')
const queryRef = ref<FormInstance>()
const dataRef = ref<FormInstance>()
const form = ref<DictData>({})
const queryParams = reactive<DictDataQuery>({
  pageNum: 1,
  pageSize: 10
})

const listClassOptions = computed(() => [
  { value: 'default', label: t('legacy.tagDefault') },
  { value: 'primary', label: t('legacy.tagPrimary') },
  { value: 'success', label: t('legacy.tagSuccess') },
  { value: 'info', label: t('legacy.tagInfo') },
  { value: 'warning', label: t('legacy.tagWarning') },
  { value: 'danger', label: t('legacy.tagDanger') }
])
const single = computed(() => ids.value.length !== 1)
const multiple = computed(() => ids.value.length === 0)
const title = computed(() => (form.value.dictCode ? t('legacy.editDictData') : t('legacy.addDictData')))
const rules = computed<FormRules<DictData>>(() => ({
  dictLabel: [{ required: true, message: t('legacy.dictLabelRequired'), trigger: 'blur' }],
  dictValue: [{ required: true, message: t('legacy.dictValueRequired'), trigger: 'blur' }],
  dictSort: [{ required: true, message: t('legacy.dictSortRequired'), trigger: 'blur' }]
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

function isPlainLabel(row: DictData) {
  return (!row.listClass || row.listClass === 'default') && !row.cssClass
}

function reset() {
  form.value = {
    dictLabel: undefined,
    dictValue: undefined,
    cssClass: undefined,
    listClass: 'default',
    dictSort: 0,
    status: '1',
    remark: undefined
  }
  dataRef.value?.resetFields()
}

async function getTypes(dictId: string | number) {
  const response = await getType(dictId)
  queryParams.dictType = response.data.dictType
  defaultDictType.value = response.data.dictType || ''
  await getList()
}

async function getTypeList() {
  const response = await optionselect()
  typeOptions.value = response.data || []
}

async function getList() {
  loading.value = true
  try {
    const response = await listData(queryParams)
    dataList.value = response.rows || []
    total.value = response.total || 0
  } finally {
    loading.value = false
  }
}

function cancel() {
  formCloseGuard.closeWithGuard()
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function handleClose() {
  router.push('/system/dict')
}

function resetQuery() {
  queryRef.value?.resetFields()
  queryParams.dictType = defaultDictType.value
  handleQuery()
}

function handleSelectionChange(selection: DictData[]) {
  ids.value = selection.map((item) => String(item.dictCode)).filter(Boolean)
}

function handleAdd() {
  reset()
  form.value.dictType = queryParams.dictType
  formCloseGuard.markPristine()
  open.value = true
}

async function handleUpdate(row?: DictData) {
  reset()
  const dictCode = row?.dictCode || ids.value[0]
  if (!dictCode) return
  try {
    const response = await getData(dictCode)
    form.value = response.data
    formCloseGuard.markPristine()
    open.value = true
  } catch {
    // Request interceptor already displays the backend error.
  }
}

async function submitForm() {
  const valid = await dataRef.value?.validate().catch(() => false)
  if (!valid) return
  try {
    if (form.value.dictCode) {
      await updateData(form.value)
      ElMessage.success(t('common.editSuccess'))
    } else {
      await addData(form.value)
      ElMessage.success(t('common.addSuccess'))
    }
    useDictStore().removeDict(`${localeStore.language}:${queryParams.dictType}`)
    formCloseGuard.markPristine()
    open.value = false
    await getList()
  } catch {
    // Request interceptor already displays the backend error.
  }
}

async function handleDelete(row?: DictData) {
  const dictCodes = row?.dictCode || ids.value
  if (!dictCodes || (Array.isArray(dictCodes) && !dictCodes.length)) return
  try {
    await ElMessageBox.confirm(t('legacy.deleteDictDataConfirm', { ids: Array.isArray(dictCodes) ? dictCodes.join(',') : dictCodes }), t('common.prompt'), {
      type: 'warning'
    })
    await delData(dictCodes)
    useDictStore().removeDict(`${localeStore.language}:${queryParams.dictType}`)
    ElMessage.success(t('common.deleteSuccess'))
    await getList()
  } catch {
    // User cancelled or the request interceptor already displayed the backend error.
  }
}

function handleExport() {
  download('system/dict/data/export', { ...queryParams }, `dict_data_${Date.now()}.xlsx`)
}

getTypes(String(route.params.dictId || ''))
getTypeList()
</script>
