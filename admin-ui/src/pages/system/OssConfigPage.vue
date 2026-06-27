<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true">
      <el-form-item :label="t('legacy.configKey')" prop="configKey">
        <el-input v-model="queryParams.configKey" :placeholder="t('legacy.configKeyPlaceholder')" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item :label="t('legacy.bucketName')" prop="bucketName">
        <el-input v-model="queryParams.bucketName" :placeholder="t('legacy.bucketNamePlaceholder')" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item :label="t('legacy.defaultStatus')" prop="status">
        <el-select v-model="queryParams.status" :placeholder="t('legacy.defaultStatusPlaceholder')" clearable style="width: 200px">
          <el-option v-for="item in defaultStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">{{ t('common.search') }}</el-button>
        <el-button icon="Refresh" @click="resetQuery">{{ t('common.reset') }}</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['system:oss:add']">{{ t('common.add') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()" v-hasPermi="['system:oss:edit']">{{ t('common.edit') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['system:oss:remove']">{{ t('common.delete') }}</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="ossConfigList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column v-if="columns[0].visible" :label="t('legacy.configKey')" align="center" prop="configKey" />
      <el-table-column v-if="columns[1].visible" :label="t('legacy.endpoint')" align="center" prop="endpoint" width="200" />
      <el-table-column v-if="columns[2].visible" :label="t('legacy.domain')" align="center" prop="domain" width="200" />
      <el-table-column v-if="columns[3].visible" :label="t('legacy.bucketName')" align="center" prop="bucketName" />
      <el-table-column v-if="columns[4].visible" :label="t('legacy.prefix')" align="center" prop="prefix" />
      <el-table-column v-if="columns[5].visible" :label="t('legacy.region')" align="center" prop="region" />
      <el-table-column v-if="columns[6].visible" :label="t('legacy.accessPolicy')" align="center" prop="accessPolicy">
        <template #default="{ row }">
          <el-tag v-if="row.accessPolicy === '0'" type="warning">private</el-tag>
          <el-tag v-if="row.accessPolicy === '1'" type="success">public</el-tag>
          <el-tag v-if="row.accessPolicy === '2'" type="info">custom</el-tag>
        </template>
      </el-table-column>
      <el-table-column v-if="columns[7].visible" :label="t('legacy.defaultStatus')" align="center" prop="status">
        <template #default="{ row }">
          <el-switch v-model="row.status" active-value="1" inactive-value="0" @change="handleStatusChange(row)" />
        </template>
      </el-table-column>
      <el-table-column :label="t('common.operate')" align="center" width="150" class-name="small-padding fixed-width">
        <template #default="{ row }">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(row)" v-hasPermi="['system:oss:edit']">{{ t('common.edit') }}</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(row)" v-hasPermi="['system:oss:remove']">{{ t('common.delete') }}</el-button>
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

    <el-drawer v-model="open" :title="title" size="760px" append-to-body destroy-on-close @closed="reset">
      <el-form ref="ossConfigRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item :label="t('legacy.configKey')" prop="configKey">
          <el-input v-model="form.configKey" :placeholder="t('legacy.configKeyPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('legacy.endpoint')" prop="endpoint">
          <el-input v-model="form.endpoint" :placeholder="t('legacy.endpointPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('legacy.domain')" prop="domain">
          <el-input v-model="form.domain" :placeholder="t('legacy.domainPlaceholder')" />
        </el-form-item>
        <el-form-item label="accessKey" prop="accessKey">
          <el-input v-model="form.accessKey" placeholder="accessKey" />
        </el-form-item>
        <el-form-item label="secretKey" prop="secretKey">
          <el-input v-model="form.secretKey" placeholder="secretKey" show-password />
        </el-form-item>
        <el-form-item :label="t('legacy.bucketName')" prop="bucketName">
          <el-input v-model="form.bucketName" :placeholder="t('legacy.bucketNamePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('legacy.prefix')" prop="prefix">
          <el-input v-model="form.prefix" :placeholder="t('legacy.prefixPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('legacy.isHttps')">
          <el-radio-group v-model="form.isHttps">
            <el-radio v-for="dict in sys_yes_no" :key="dict.value" :value="dict.value">{{ dict.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="t('legacy.accessPolicy')" prop="accessPolicy">
          <el-radio-group v-model="form.accessPolicy">
            <el-radio value="0">private</el-radio>
            <el-radio value="1">public</el-radio>
            <el-radio value="2">custom</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="t('legacy.region')" prop="region">
          <el-input v-model="form.region" :placeholder="t('legacy.regionPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('user.remark')" prop="remark">
          <el-input v-model="form.remark" type="textarea" :placeholder="t('user.remarkPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button :loading="buttonLoading" type="primary" @click="submitForm">{{ t('common.confirm') }}</el-button>
          <el-button @click="cancel">{{ t('common.cancel') }}</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts" name="OssConfigPage">
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  addOssConfig,
  changeOssConfigStatus,
  delOssConfig,
  getOssConfig,
  listOssConfig,
  updateOssConfig,
  type OssConfig,
  type OssConfigQuery
} from '@/api/system/ossConfig'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { useDict } from '@/utils/dict'

const localeStore = useLocaleStore()
const t = (key: string, params?: Record<string, string | number>) => {
  const message = getMessage(key, localeStore.language)
  if (!params) return message
  return Object.entries(params).reduce((text, [name, value]) => text.replaceAll(`{${name}}`, String(value)), message)
}
const { sys_yes_no } = useDict('sys_yes_no')

const ossConfigList = ref<OssConfig[]>([])
const open = ref(false)
const buttonLoading = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref<Array<number | string>>([])
const total = ref(0)
const queryRef = ref<FormInstance>()
const ossConfigRef = ref<FormInstance>()
const form = ref<OssConfig>({})
const queryParams = reactive<OssConfigQuery>({
  pageNum: 1,
  pageSize: 10
})
const columns = ref([
  { key: 0, visible: true },
  { key: 1, visible: true },
  { key: 2, visible: true },
  { key: 3, visible: true },
  { key: 4, visible: true },
  { key: 5, visible: true },
  { key: 6, visible: true },
  { key: 7, visible: true }
])

const defaultStatusOptions = computed(() => [
  { value: '1', label: t('common.yes') },
  { value: '0', label: t('common.no') }
])
const single = computed(() => ids.value.length !== 1)
const multiple = computed(() => ids.value.length === 0)
const title = computed(() => (form.value.ossConfigId ? t('legacy.editOssConfig') : t('legacy.addOssConfig')))
const rules = computed<FormRules<OssConfig>>(() => ({
  configKey: [{ required: true, message: t('legacy.configKeyRequired'), trigger: 'blur' }],
  accessKey: [
    { required: true, message: t('legacy.accessKeyRequired'), trigger: 'blur' },
    { min: 2, max: 200, message: t('legacy.accessKeyLength'), trigger: 'blur' }
  ],
  secretKey: [
    { required: true, message: t('legacy.secretKeyRequired'), trigger: 'blur' },
    { min: 2, max: 100, message: t('legacy.secretKeyLength'), trigger: 'blur' }
  ],
  bucketName: [
    { required: true, message: t('legacy.bucketNameRequired'), trigger: 'blur' },
    { min: 2, max: 100, message: t('legacy.bucketNameLength'), trigger: 'blur' }
  ],
  endpoint: [
    { required: true, message: t('legacy.endpointRequired'), trigger: 'blur' },
    { min: 2, max: 100, message: t('legacy.endpointLength'), trigger: 'blur' }
  ],
  accessPolicy: [{ required: true, message: t('legacy.accessPolicyRequired'), trigger: 'blur' }]
}))

function reset() {
  form.value = {
    configKey: undefined,
    accessKey: undefined,
    secretKey: undefined,
    bucketName: undefined,
    prefix: undefined,
    endpoint: undefined,
    domain: undefined,
    isHttps: 'N',
    accessPolicy: '1',
    region: undefined,
    status: '1',
    remark: undefined
  }
  ossConfigRef.value?.resetFields()
}

async function getList() {
  loading.value = true
  try {
    const response = await listOssConfig(queryParams)
    ossConfigList.value = response.rows || []
    total.value = response.total || 0
  } finally {
    loading.value = false
  }
}

function cancel() {
  open.value = false
  reset()
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  queryRef.value?.resetFields()
  handleQuery()
}

function handleSelectionChange(selection: OssConfig[]) {
  ids.value = selection.map((item) => String(item.ossConfigId)).filter(Boolean)
}

function handleAdd() {
  reset()
  open.value = true
}

async function handleUpdate(row?: OssConfig) {
  reset()
  const ossConfigId = row?.ossConfigId || ids.value[0]
  if (!ossConfigId) return
  loading.value = true
  try {
    const response = await getOssConfig(ossConfigId)
    form.value = response.data
    open.value = true
  } finally {
    loading.value = false
  }
}

async function submitForm() {
  const valid = await ossConfigRef.value?.validate().catch(() => false)
  if (!valid) return
  buttonLoading.value = true
  try {
    if (form.value.ossConfigId) {
      await updateOssConfig(form.value)
      ElMessage.success(t('common.editSuccess'))
    } else {
      await addOssConfig(form.value)
      ElMessage.success(t('common.addSuccess'))
    }
    open.value = false
    reset()
    await getList()
  } catch {
    // Request interceptor already displays the backend error.
  } finally {
    buttonLoading.value = false
  }
}

async function handleStatusChange(row: OssConfig) {
  const status = row.status || '0'
  const action = status === '1' ? t('legacy.enable') : t('legacy.disable')
  try {
    await ElMessageBox.confirm(t('legacy.changeOssConfigStatusConfirm', { action, name: row.configKey || '' }), t('common.prompt'), { type: 'warning' })
    if (!row.ossConfigId) return
    await changeOssConfigStatus(row.ossConfigId, status, row.configKey)
    ElMessage.success(t('legacy.statusChangeSuccess', { action }))
    await getList()
  } catch {
    row.status = status === '0' ? '1' : '0'
  }
}

async function handleDelete(row?: OssConfig) {
  const ossConfigIds = row?.ossConfigId || ids.value
  if (!ossConfigIds || (Array.isArray(ossConfigIds) && !ossConfigIds.length)) return
  loading.value = true
  try {
    await ElMessageBox.confirm(t('legacy.deleteOssConfigConfirm', { ids: Array.isArray(ossConfigIds) ? ossConfigIds.join(',') : ossConfigIds }), t('common.prompt'), {
      type: 'warning'
    })
    await delOssConfig(ossConfigIds)
    ElMessage.success(t('common.deleteSuccess'))
    await getList()
  } catch {
    // User cancelled or the request interceptor already displayed the backend error.
  } finally {
    loading.value = false
  }
}

getList()
</script>
