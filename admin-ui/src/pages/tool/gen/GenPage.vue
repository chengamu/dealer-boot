<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true">
      <el-form-item :label="t('gen.dataName')" prop="dataName">
        <el-input v-model="queryParams.dataName" :placeholder="t('gen.dataNamePlaceholder')" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item :label="t('gen.tableName')" prop="tableName">
        <el-input v-model="queryParams.tableName" :placeholder="t('gen.tableNamePlaceholder')" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item :label="t('gen.tableComment')" prop="tableComment">
        <el-input v-model="queryParams.tableComment" :placeholder="t('gen.tableCommentPlaceholder')" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item :label="t('common.createTime')" style="width: 308px">
        <el-date-picker v-model="dateRange" value-format="YYYY-MM-DD" type="daterange" range-separator="-" :start-placeholder="t('common.startDate')" :end-placeholder="t('common.endDate')" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">{{ t('common.search') }}</el-button>
        <el-button icon="Refresh" @click="resetQuery">{{ t('common.reset') }}</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Download" @click="handleGenTable()" v-hasPermi="['tool:gen:code']">{{ t('gen.generate') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="Upload" @click="openImportTable" v-hasPermi="['tool:gen:import']">{{ t('common.import') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleEditTable()" v-hasPermi="['tool:gen:edit']">{{ t('common.edit') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['tool:gen:remove']">{{ t('common.delete') }}</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="tableList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" align="center" width="55" />
      <el-table-column :label="t('common.index')" type="index" width="60" align="center">
        <template #default="{ $index }">
          <span>{{ ((queryParams.pageNum || 1) - 1) * (queryParams.pageSize || 10) + $index + 1 }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="t('gen.tableName')" align="center" prop="tableName" :show-overflow-tooltip="true" />
      <el-table-column :label="t('gen.tableComment')" align="center" prop="tableComment" :show-overflow-tooltip="true" />
      <el-table-column :label="t('gen.className')" align="center" prop="className" :show-overflow-tooltip="true" />
      <el-table-column :label="t('common.createTime')" align="center" prop="createTime" width="170">
        <template #default="{ row }">{{ formatUtc(row.createTime) }}</template>
      </el-table-column>
      <el-table-column :label="t('common.updateTime')" align="center" prop="updateTime" width="170">
        <template #default="{ row }">{{ formatUtc(row.updateTime) }}</template>
      </el-table-column>
      <el-table-column :label="t('common.operate')" align="center" width="330" class-name="small-padding fixed-width">
        <template #default="{ row }">
          <el-tooltip :content="t('common.preview')" placement="top">
            <el-button link type="primary" icon="View" :aria-label="t('common.preview')" :title="t('common.preview')" @click="handlePreview(row)" v-hasPermi="['tool:gen:preview']" />
          </el-tooltip>
          <el-tooltip :content="t('common.edit')" placement="top">
            <el-button link type="primary" icon="Edit" :aria-label="t('common.edit')" :title="t('common.edit')" @click="handleEditTable(row)" v-hasPermi="['tool:gen:edit']" />
          </el-tooltip>
          <el-tooltip :content="t('common.delete')" placement="top">
            <el-button link type="primary" icon="Delete" :aria-label="t('common.delete')" :title="t('common.delete')" @click="handleDelete(row)" v-hasPermi="['tool:gen:remove']" />
          </el-tooltip>
          <el-tooltip :content="t('common.sync')" placement="top">
            <el-button link type="primary" icon="Refresh" :aria-label="t('common.sync')" :title="t('common.sync')" @click="handleSynchDb(row)" v-hasPermi="['tool:gen:edit']" />
          </el-tooltip>
          <el-tooltip :content="t('gen.generateCode')" placement="top">
            <el-button link type="primary" icon="Download" :aria-label="t('gen.generateCode')" :title="t('gen.generateCode')" @click="handleGenTable(row)" v-hasPermi="['tool:gen:code']" />
          </el-tooltip>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />

    <el-dialog v-model="preview.open" :title="preview.title" width="80%" top="5vh" append-to-body class="gen-preview-dialog scrollbar">
      <el-tabs v-model="preview.activeName">
        <el-tab-pane v-for="[key, value] in previewEntries" :key="key" :label="previewName(key)" :name="previewName(key)">
          <el-link :underline="false" icon="DocumentCopy" class="gen-preview-dialog__copy" v-copyText="value" v-copyText:callback="copyTextSuccess">&nbsp;{{ t('common.copy') }}</el-link>
          <pre>{{ value }}</pre>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>

    <import-table-dialog ref="importRef" @ok="handleQuery" />
  </div>
</template>

<script setup lang="ts" name="GenPage">
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import downloadPlugin from '@/plugins/download'
import { delTable, genCode, listTable, previewTable, synchDb, type GenTable, type GenTableQuery } from '@/api/tool/gen'
import ImportTableDialog from './ImportTableDialog.vue'
import { formatUtc } from '@/utils/datetime'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'

const route = useRoute()
const router = useRouter()
const localeStore = useLocaleStore()
const t = (key: string, params?: Record<string, string | number>) => {
  const message = getMessage(key, localeStore.language)
  if (!params) return message
  return Object.entries(params).reduce((text, [name, value]) => text.replaceAll(`{${name}}`, String(value)), message)
}

const tableList = ref<GenTable[]>([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref<Array<number | string>>([])
const total = ref(0)
const tableNames = ref<string[]>([])
const dateRange = ref<string[]>([])
const uniqueId = ref('')
const queryRef = ref<FormInstance>()
const importRef = ref<InstanceType<typeof ImportTableDialog>>()
const queryParams = reactive<GenTableQuery>({
  pageNum: 1,
  pageSize: 10,
  dataName: 'master'
})
const preview = reactive({
  open: false,
  title: t('gen.previewTitle'),
  data: {} as Record<string, string>,
  activeName: 'domain.java'
})

const single = computed(() => ids.value.length !== 1)
const multiple = computed(() => ids.value.length === 0)
const previewEntries = computed(() => Object.entries(preview.data))

localStorage.setItem('dataName', queryParams.dataName || 'master')

function withDateRange(query: GenTableQuery) {
  const params = { ...query }
  if (dateRange.value.length === 2) {
    params.beginTime = dateRange.value[0]
    params.endTime = dateRange.value[1]
  }
  return params
}

async function getList() {
  loading.value = true
  try {
    const response = await listTable(withDateRange(queryParams))
    tableList.value = response.rows || []
    total.value = response.total || 0
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  localStorage.setItem('dataName', queryParams.dataName || 'master')
  queryParams.pageNum = 1
  getList()
}

async function handleGenTable(row: GenTable = {}) {
  const tbNames = row.tableName || tableNames.value.join(',')
  if (!tbNames) {
    ElMessage.error(t('gen.selectDataToGenerate'))
    return
  }
  try {
    if (row.genType === '1' && row.tableName) {
      await genCode(row.tableName)
      ElMessage.success(`${t('gen.generateSuccess')}${row.genPath || ''}`)
    } else {
      downloadPlugin.zip(`/tool/gen/batchGenCode?tables=${tbNames}`, 'bm.zip')
    }
  } catch {
    // Request interceptor already displays the backend error.
  }
}

async function handleSynchDb(row: GenTable) {
  const tableName = row.tableName || ''
  try {
    await ElMessageBox.confirm(t('gen.syncConfirm', { name: tableName }), t('common.prompt'), { type: 'warning' })
    await synchDb(tableName)
    ElMessage.success(t('common.syncSuccess'))
  } catch {
    // User cancelled or the request interceptor already displayed the backend error.
  }
}

function openImportTable() {
  importRef.value?.show()
}

function resetQuery() {
  dateRange.value = []
  queryRef.value?.resetFields()
  handleQuery()
}

async function handlePreview(row: GenTable) {
  if (!row.tableId) return
  try {
    const response = await previewTable(row.tableId)
    preview.data = response.data || {}
    preview.open = true
    preview.activeName = 'domain.java'
  } catch {
    // Request interceptor already displays the backend error.
  }
}

function previewName(key: string) {
  return key.substring(key.lastIndexOf('/') + 1, key.indexOf('.vm'))
}

function copyTextSuccess() {
  ElMessage.success(t('common.copySuccess'))
}

function handleSelectionChange(selection: GenTable[]) {
  ids.value = selection.map((item) => String(item.tableId)).filter(Boolean)
  tableNames.value = selection.map((item) => item.tableName || '').filter(Boolean)
}

function handleEditTable(row: GenTable = {}) {
  const tableId = row.tableId || ids.value[0]
  if (!tableId) return
  router.push({ path: `/tool/gen-edit/index/${tableId}`, query: { pageNum: queryParams.pageNum } })
}

async function handleDelete(row: GenTable = {}) {
  const tableIds = row.tableId || ids.value
  if (!tableIds || (Array.isArray(tableIds) && !tableIds.length)) return
  try {
    await ElMessageBox.confirm(t('gen.deleteConfirm', { ids: Array.isArray(tableIds) ? tableIds.join(',') : tableIds }), t('common.prompt'), { type: 'warning' })
    await delTable(tableIds)
    ElMessage.success(t('common.deleteSuccess'))
    await getList()
  } catch {
    // User cancelled or the request interceptor already displayed the backend error.
  }
}

const time = route.query.t
if (time && time !== uniqueId.value) {
  uniqueId.value = String(time)
  queryParams.pageNum = Number(route.query.pageNum || 1)
}

getList()
</script>
