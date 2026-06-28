<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true" label-width="90px">
      <el-form-item :label="t('operlog.operIp')" prop="operIp">
        <el-input v-model="queryParams.operIp" :placeholder="t('operlog.operIpPlaceholder')" clearable style="width: 240px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item :label="t('operlog.title')" prop="title">
        <el-input v-model="queryParams.title" :placeholder="t('operlog.titlePlaceholder')" clearable style="width: 240px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item :label="t('operlog.operName')" prop="operName">
        <el-input v-model="queryParams.operName" :placeholder="t('operlog.operNamePlaceholder')" clearable style="width: 240px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item :label="t('operlog.businessType')" prop="businessType">
        <el-select v-model="queryParams.businessType" :placeholder="t('operlog.businessTypePlaceholder')" clearable style="width: 240px">
          <el-option v-for="dict in sys_oper_type" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('operlog.status')" prop="status">
        <el-select v-model="queryParams.status" :placeholder="t('operlog.statusPlaceholder')" clearable style="width: 240px">
          <el-option v-for="dict in sys_common_status" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('operlog.operTime')" style="width: 330px">
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
        <el-button v-hasPermi="['monitor:operlog:remove']" type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()">
          {{ t('common.delete') }}
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button v-hasPermi="['monitor:operlog:remove']" type="danger" plain icon="Delete" @click="handleClean">
          {{ t('common.clear') }}
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button v-hasPermi="['monitor:operlog:export']" type="warning" plain icon="Download" @click="handleExport">
          {{ t('common.export') }}
        </el-button>
      </el-col>
      <span class="selection-count">{{ t('common.selectedCount', { count: ids.length }) }}</span>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table
      ref="operlogRef"
      v-loading="loading"
      :data="operlogList"
      :default-sort="defaultSort"
      @selection-change="handleSelectionChange"
      @sort-change="handleSortChange"
    >
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column :label="t('operlog.title')" align="center" prop="title" min-width="132" :show-overflow-tooltip="true" />
      <el-table-column :label="t('operlog.businessType')" align="center" prop="businessType" width="116">
        <template #default="scope">
          <dict-tag :options="sys_oper_type" :value="scope.row.businessType" />
        </template>
      </el-table-column>
      <el-table-column :label="t('operlog.operName')" align="center" width="110" prop="operName" :show-overflow-tooltip="true" sortable="custom" :sort-orders="['descending', 'ascending']" />
      <el-table-column :label="t('operlog.operIp')" align="center" prop="operIp" width="130" :show-overflow-tooltip="true" />
      <el-table-column :label="t('operlog.operLocation')" align="center" prop="operLocation" min-width="116" :show-overflow-tooltip="true" />
      <el-table-column :label="t('operlog.operStatus')" align="center" prop="status" width="126">
        <template #default="scope">
          <dict-tag :options="sys_common_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column :label="t('operlog.operDate')" align="center" prop="operTime" width="180" sortable="custom" :sort-orders="['descending', 'ascending']">
        <template #default="scope">
          <span>{{ formatUtc(scope.row.operTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="t('operlog.costTime')" align="center" prop="costTime" width="110" :show-overflow-tooltip="true" sortable="custom" :sort-orders="['descending', 'ascending']">
        <template #default="scope">
          <span>{{ scope.row.costTime }}{{ t('common.milliseconds') }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="t('common.operate')" align="center" width="116" fixed="right" class-name="small-padding fixed-width">
        <template #default="scope">
          <AdminTableActions :actions="[
            { label: t('common.detail'), icon: 'View', permission: 'monitor:operlog:query', onClick: () => handleView(scope.row) }
          ]" />
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />

    <AdminDrawer v-model="open" :title="t('operlog.detailTitle')" size="700px" variant="detail" append-to-body destroy-on-close @closed="resetDetail">
      <div class="admin-detail operation-log-detail">
        <section class="admin-detail__section">
          <div class="admin-detail__section-title">{{ t('operlog.loginInfo') }}</div>
          <dl class="admin-detail__grid">
            <div class="admin-detail__item">
              <dt>{{ t('operlog.loginInfo') }}</dt>
              <dd>{{ codeText(form.operName) }} / {{ codeText(form.deptName) }}</dd>
            </div>
            <div class="admin-detail__item">
              <dt>IP</dt>
              <dd>{{ codeText(form.operIp) }} / {{ codeText(form.operLocation) }}</dd>
            </div>
            <div class="admin-detail__item">
              <dt>{{ t('operlog.moduleInfo') }}</dt>
              <dd>{{ codeText(form.title) }} / {{ typeFormat(form) || '-' }}</dd>
            </div>
            <div class="admin-detail__item">
              <dt>{{ t('operlog.operStatus') }}</dt>
              <dd>{{ statusText(form.status) }}</dd>
            </div>
            <div class="admin-detail__item">
              <dt>{{ t('operlog.operDate') }}</dt>
              <dd>{{ formatUtc(form.operTime) }}</dd>
            </div>
            <div class="admin-detail__item">
              <dt>{{ t('operlog.costTime') }}</dt>
              <dd>{{ codeText(form.costTime) }}{{ t('common.milliseconds') }}</dd>
            </div>
          </dl>
        </section>

        <section class="admin-detail__section">
          <div class="admin-detail__section-title">{{ t('operlog.requestInfo') }}</div>
          <dl class="admin-detail__grid">
            <div class="admin-detail__item admin-detail__item--full">
              <dt>{{ t('operlog.requestInfo') }}</dt>
              <dd><pre class="admin-detail__code">{{ requestInfoText }}</pre></dd>
            </div>
            <div class="admin-detail__item admin-detail__item--full">
              <dt>{{ t('operlog.method') }}</dt>
              <dd><pre class="admin-detail__code">{{ codeText(form.method) }}</pre></dd>
            </div>
            <div class="admin-detail__item admin-detail__item--full">
              <dt>{{ t('operlog.operParam') }}</dt>
              <dd><pre class="admin-detail__code">{{ codeText(form.operParam) }}</pre></dd>
            </div>
            <div class="admin-detail__item admin-detail__item--full">
              <dt>{{ t('operlog.jsonResult') }}</dt>
              <dd><pre class="admin-detail__code">{{ codeText(form.jsonResult) }}</pre></dd>
            </div>
            <div v-if="String(form.status) === '1'" class="admin-detail__item admin-detail__item--full">
              <dt>{{ t('operlog.errorMsg') }}</dt>
              <dd><pre class="admin-detail__code">{{ codeText(form.errorMsg) }}</pre></dd>
            </div>
          </dl>
        </section>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="open = false">{{ t('common.close') }}</el-button>
        </div>
      </template>
    </AdminDrawer>
  </div>
</template>

<script setup lang="ts" name="OperationLogPage">
import { computed, onMounted, reactive, ref } from 'vue'
import type { FormInstance } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cleanOperlog, delOperlog, list, type OperLog, type OperLogQuery } from '@/api/monitor/operlog'
import { download } from '@/utils/request'
import { formatUtc, withUtcDateRangeParams } from '@/utils/datetime'
import { useDict } from '@/utils/dict'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'

const localeStore = useLocaleStore()
const t = (key: string, params?: Record<string, string | number>) => {
  const message = getMessage(key, localeStore.language)
  if (!params) return message
  return Object.entries(params).reduce((text, [name, value]) => text.replaceAll(`{${name}}`, String(value)), message)
}
const { sys_oper_type, sys_common_status } = useDict('sys_oper_type', 'sys_common_status')

const queryRef = ref<FormInstance>()
const operlogList = ref<OperLog[]>([])
const form = ref<Partial<OperLog>>({})
const open = ref(false)
const loading = ref(false)
const showSearch = ref(true)
const ids = ref<Array<number | string>>([])
const multiple = ref(true)
const total = ref(0)
const dateRange = ref<string[]>([])
const defaultSort = ref({ prop: 'operTime', order: 'descending' })
const queryParams = reactive<OperLogQuery>({
  pageNum: 1,
  pageSize: 10,
  operIp: undefined,
  title: undefined,
  operName: undefined,
  businessType: undefined,
  status: undefined
})

const requestInfoText = computed(() => `${codeText(form.value.requestMethod)} ${codeText(form.value.operUrl)}`.trim())

function codeText(value: unknown) {
  if (value === undefined || value === null || value === '') return '-'
  return String(value)
}

function withDateRange(params: OperLogQuery) {
  return withUtcDateRangeParams(params, dateRange.value)
}

async function getList() {
  loading.value = true
  try {
    const response = await list(withDateRange(queryParams))
    operlogList.value = response.rows || []
    total.value = response.total || 0
  } finally {
    loading.value = false
  }
}

function typeFormat(row: Partial<OperLog>) {
  return sys_oper_type.value.find((item) => String(item.value) === String(row.businessType))?.label || row.businessType || ''
}

function statusText(value: unknown) {
  if (String(value) === '0') return t('dataLabels.normal')
  if (String(value) === '1') return t('common.failed')
  return codeText(value)
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  dateRange.value = []
  queryRef.value?.resetFields()
  queryParams.pageNum = 1
  queryParams.orderByColumn = defaultSort.value.prop
  queryParams.isAsc = defaultSort.value.order
  getList()
}

function handleSelectionChange(selection: OperLog[]) {
  ids.value = selection.map((item) => String(item.operId)).filter(Boolean)
  multiple.value = selection.length === 0
}

function handleSortChange(column: { prop?: string; order?: string }) {
  queryParams.orderByColumn = column.prop
  queryParams.isAsc = column.order
  getList()
}

function handleView(row: OperLog) {
  form.value = row
  open.value = true
}

function resetDetail() {
  form.value = {}
}

async function handleDelete(row?: OperLog) {
  const operIds = row?.operId || ids.value
  try {
    await ElMessageBox.confirm(t('operlog.deleteConfirm').replace('{ids}', String(operIds)), t('common.prompt'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
    await delOperlog(operIds)
    await getList()
    ElMessage.success(t('common.deleteSuccess'))
  } catch {
    // User cancelled or the request interceptor already displayed the backend error.
  }
}

async function handleClean() {
  try {
    await ElMessageBox.confirm(t('operlog.cleanConfirm'), t('common.prompt'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
    await cleanOperlog()
    await getList()
    ElMessage.success(t('common.clearSuccess'))
  } catch {
    // User cancelled or the request interceptor already displayed the backend error.
  }
}

function handleExport() {
  download('monitor/operlog/export', { ...queryParams }, `operlog_${Date.now()}.xlsx`)
}

onMounted(getList)
</script>
