<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true">
      <el-form-item :label="t('legacy.fileName')" prop="fileName">
        <el-input v-model="queryParams.fileName" :placeholder="t('legacy.fileNamePlaceholder')" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item :label="t('legacy.originalName')" prop="originalName">
        <el-input v-model="queryParams.originalName" :placeholder="t('legacy.originalNamePlaceholder')" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item :label="t('legacy.fileSuffix')" prop="fileSuffix">
        <el-input v-model="queryParams.fileSuffix" :placeholder="t('legacy.fileSuffixPlaceholder')" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item :label="t('common.createTime')">
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
      <el-form-item :label="t('legacy.createBy')" prop="createBy">
        <el-input v-model="queryParams.createBy" :placeholder="t('legacy.createByPlaceholder')" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item :label="t('legacy.provider')" prop="service">
        <el-input v-model="queryParams.service" :placeholder="t('legacy.providerPlaceholder')" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">{{ t('common.search') }}</el-button>
        <el-button icon="Refresh" @click="resetQuery">{{ t('common.reset') }}</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleFile" v-hasPermi="['system:oss:upload']">
          {{ t('legacy.uploadFile') }}
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleImage" v-hasPermi="['system:oss:upload']">
          {{ t('legacy.uploadImage') }}
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['system:oss:remove']">
          {{ t('common.delete') }}
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button :type="previewListResource ? 'danger' : 'warning'" plain @click="handlePreviewListResource(!previewListResource)" v-hasPermi="['system:oss:edit']">
          {{ previewListResource ? t('legacy.disablePreviewList') : t('legacy.enablePreviewList') }}
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="Operation" @click="handleOssConfig" v-hasPermi="['system:oss:list']">
          {{ t('legacy.ossConfigManage') }}
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table
      v-if="showTable"
      v-loading="loading"
      :data="ossList"
      :header-cell-class-name="handleHeaderClass"
      @header-click="handleHeaderClick"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column :label="t('legacy.ossId')" align="center" prop="ossId" v-if="false" />
      <el-table-column :label="t('legacy.fileName')" align="center" prop="fileName" />
      <el-table-column :label="t('legacy.originalName')" align="center" prop="originalName" />
      <el-table-column :label="t('legacy.fileSuffix')" align="center" prop="fileSuffix" />
      <el-table-column :label="t('legacy.fileDisplay')" align="center" prop="url">
        <template #default="{ row }">
          <ImagePreview v-if="previewListResource && checkFileSuffix(row.fileSuffix)" :width="100" :height="100" :src="row.url" />
          <span v-else>{{ row.url }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="t('common.createTime')" align="center" prop="createTime" width="180" sortable="custom">
        <template #default="{ row }">
          <span>{{ formatUtc(row.createTime, 'YYYY-MM-DD') }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="t('legacy.createBy')" align="center" prop="createBy" />
      <el-table-column :label="t('legacy.provider')" align="center" prop="service" sortable="custom" />
      <el-table-column :label="t('common.operate')" align="center" class-name="small-padding fixed-width">
        <template #default="{ row }">
          <el-button link type="primary" icon="Download" @click="handleDownload(row)" v-hasPermi="['system:oss:download']">
            {{ t('legacy.download') }}
          </el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(row)" v-hasPermi="['system:oss:remove']">
            {{ t('common.delete') }}
          </el-button>
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

    <el-dialog v-model="open" :title="title" width="500px" append-to-body destroy-on-close @closed="reset">
      <el-form ref="ossRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item :label="t('legacy.fileName')" prop="file">
          <FileUpload v-if="uploadType === 0" v-model="form.file" />
          <ImageUpload v-if="uploadType === 1" v-model="form.file" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button :loading="buttonLoading" type="primary" @click="submitForm">{{ t('common.confirm') }}</el-button>
          <el-button @click="cancel">{{ t('common.cancel') }}</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="OssPage">
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { delOss, listOss, type OssFile, type OssQuery } from '@/api/system/oss'
import { getConfigKey, updateConfigByKey } from '@/api/system/config'
import { formatUtc } from '@/utils/datetime'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import ImagePreview from '@/components/ImagePreview/index.vue'
import FileUpload from '@/components/FileUpload/index.vue'
import ImageUpload from '@/components/ImageUpload/index.vue'
import rawDownload from '@/plugins/download'

interface UploadForm {
  file?: string | unknown[] | Record<string, unknown>
}

interface TableColumn {
  property?: string
  sortable?: string | boolean
  multiOrder?: string
  order?: string
}

const downloadPlugin = rawDownload as { oss: (ossId?: number | string) => void }
const router = useRouter()
const localeStore = useLocaleStore()
const t = (key: string, params?: Record<string, string | number>) => {
  const message = getMessage(key, localeStore.language)
  if (!params) return message
  return Object.entries(params).reduce((text, [name, value]) => text.replaceAll(`{${name}}`, String(value)), message)
}

const ossList = ref<OssFile[]>([])
const open = ref(false)
const showTable = ref(true)
const buttonLoading = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref<Array<number | string>>([])
const total = ref(0)
const uploadType = ref(0)
const previewListResource = ref(true)
const dateRange = ref<string[]>([])
const queryRef = ref<FormInstance>()
const ossRef = ref<FormInstance>()
const form = ref<UploadForm>({})
const queryParams = reactive<OssQuery>({
  pageNum: 1,
  pageSize: 10,
  orderByColumn: 'createTime',
  isAsc: 'ascending'
})

const multiple = computed(() => ids.value.length === 0)
const title = computed(() => (uploadType.value === 1 ? t('legacy.uploadImage') : t('legacy.uploadFile')))
const rules = computed<FormRules<UploadForm>>(() => ({
  file: [{ required: true, message: t('legacy.fileRequired'), trigger: 'blur' }]
}))

function withDateRange(query: OssQuery) {
  const params = { ...query, params: {} as Record<string, string | undefined> }
  if (dateRange.value?.length === 2) {
    params.params.beginCreateTime = dateRange.value[0]
    params.params.endCreateTime = dateRange.value[1]
  }
  return params
}

async function getList() {
  loading.value = true
  try {
    const config = await getConfigKey('sys.oss.previewListResource') as { msg?: string }
    previewListResource.value = config.msg === undefined ? true : config.msg === 'true'
    const response = await listOss(withDateRange(queryParams))
    ossList.value = response.rows || []
    total.value = response.total || 0
    showTable.value = true
  } finally {
    loading.value = false
  }
}

function checkFileSuffix(fileSuffix?: string) {
  return ['png', 'jpg', 'jpeg'].some((type) => fileSuffix?.toLowerCase().includes(type))
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = {
    file: undefined
  }
  ossRef.value?.resetFields()
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  showTable.value = false
  dateRange.value = []
  queryRef.value?.resetFields()
  queryParams.orderByColumn = 'createTime'
  queryParams.isAsc = 'ascending'
  handleQuery()
}

function handleSelectionChange(selection: OssFile[]) {
  ids.value = selection.map((item) => String(item.ossId)).filter(Boolean)
}

function handleHeaderClass({ column }: { column: TableColumn }) {
  column.order = column.multiOrder
  return ''
}

function handleHeaderClick(column: TableColumn) {
  if (column.sortable !== 'custom' || !column.property) return
  switch (column.multiOrder) {
    case 'descending':
      column.multiOrder = 'ascending'
      break
    case 'ascending':
      column.multiOrder = ''
      break
    default:
      column.multiOrder = 'descending'
      break
  }
  handleOrderChange(column.property, column.multiOrder)
}

function handleOrderChange(prop: string, order?: string) {
  const orderByArr = queryParams.orderByColumn ? queryParams.orderByColumn.split(',') : []
  const isAscArr = queryParams.isAsc ? queryParams.isAsc.split(',') : []
  const propIndex = orderByArr.indexOf(prop)
  if (propIndex !== -1) {
    if (order) {
      isAscArr[propIndex] = order
    } else {
      isAscArr.splice(propIndex, 1)
      orderByArr.splice(propIndex, 1)
    }
  } else if (order) {
    orderByArr.push(prop)
    isAscArr.push(order)
  }
  queryParams.orderByColumn = orderByArr.join(',')
  queryParams.isAsc = isAscArr.join(',')
  getList()
}

function handleOssConfig() {
  router.push('/system/oss-config/index')
}

function handleFile() {
  reset()
  uploadType.value = 0
  open.value = true
}

function handleImage() {
  reset()
  uploadType.value = 1
  open.value = true
}

async function submitForm() {
  const valid = await ossRef.value?.validate().catch(() => false)
  if (!valid) return
  try {
    open.value = false
    reset()
    await getList()
  } catch {
    // Request interceptor already displays the backend error.
  }
}

function handleDownload(row: OssFile) {
  downloadPlugin.oss(row.ossId)
}

async function handlePreviewListResource(value: boolean) {
  const action = value ? t('legacy.enable') : t('legacy.disable')
  try {
    await ElMessageBox.confirm(t('legacy.previewListConfirm', { action }), t('common.prompt'), { type: 'warning' })
    await updateConfigByKey('sys.oss.previewListResource', String(value))
    ElMessage.success(t('legacy.previewListSuccess', { action }))
    await getList()
  } catch {
    // User cancelled or the request interceptor already displayed the backend error.
  }
}

async function handleDelete(row?: OssFile) {
  const ossIds = row?.ossId || ids.value
  if (!ossIds || (Array.isArray(ossIds) && !ossIds.length)) return
  loading.value = true
  try {
    await ElMessageBox.confirm(t('legacy.deleteOssConfirm', { ids: Array.isArray(ossIds) ? ossIds.join(',') : ossIds }), t('common.prompt'), {
      type: 'warning'
    })
    await delOss(ossIds)
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
