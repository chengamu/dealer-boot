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
      <el-table-column :label="t('operlog.operId')" align="center" prop="operId" />
      <el-table-column :label="t('operlog.title')" align="center" prop="title" :show-overflow-tooltip="true" />
      <el-table-column :label="t('operlog.businessType')" align="center" prop="businessType">
        <template #default="scope">
          <dict-tag :options="sys_oper_type" :value="scope.row.businessType" />
        </template>
      </el-table-column>
      <el-table-column :label="t('operlog.requestMethod')" align="center" prop="requestMethod" />
      <el-table-column :label="t('operlog.operName')" align="center" width="110" prop="operName" :show-overflow-tooltip="true" sortable="custom" :sort-orders="['descending', 'ascending']" />
      <el-table-column :label="t('operlog.deptName')" align="center" prop="deptName" width="130" :show-overflow-tooltip="true" />
      <el-table-column :label="t('operlog.operIp')" align="center" prop="operIp" width="130" :show-overflow-tooltip="true" />
      <el-table-column :label="t('operlog.operLocation')" align="center" prop="operLocation" :show-overflow-tooltip="true" />
      <el-table-column :label="t('operlog.operStatus')" align="center" prop="status">
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
      <el-table-column :label="t('common.operate')" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button v-hasPermi="['monitor:operlog:query']" link type="primary" icon="View" @click="handleView(scope.row)">
            {{ t('common.detail') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />

    <el-dialog v-model="open" :title="t('operlog.detailTitle')" width="700px" append-to-body>
      <el-form :model="form" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item :label="t('operlog.loginInfo')">{{ form.operName }} / {{ form.deptName }} / {{ form.operIp }} / {{ form.operLocation }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('operlog.requestInfo')">{{ form.requestMethod }} {{ form.operUrl }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('operlog.moduleInfo')">{{ form.title }} / {{ typeFormat(form) }}</el-form-item>
          </el-col>
          <el-col :span="24"><el-form-item :label="t('operlog.method')">{{ form.method }}</el-form-item></el-col>
          <el-col :span="24"><el-form-item :label="t('operlog.operParam')">{{ form.operParam }}</el-form-item></el-col>
          <el-col :span="24"><el-form-item :label="t('operlog.jsonResult')">{{ form.jsonResult }}</el-form-item></el-col>
          <el-col :span="6">
            <el-form-item :label="`${t('operlog.operStatus')}:`">
              <div v-if="String(form.status) === '0'">{{ t('dataLabels.normal') }}</div>
              <div v-else-if="String(form.status) === '1'">{{ t('common.failed') }}</div>
            </el-form-item>
          </el-col>
          <el-col :span="8"><el-form-item :label="`${t('operlog.costTime')}:`">{{ form.costTime }}{{ t('common.milliseconds') }}</el-form-item></el-col>
          <el-col :span="10"><el-form-item :label="`${t('operlog.operDate')}:`">{{ formatUtc(form.operTime) }}</el-form-item></el-col>
          <el-col v-if="String(form.status) === '1'" :span="24"><el-form-item :label="t('operlog.errorMsg')">{{ form.errorMsg }}</el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="open = false">{{ t('common.close') }}</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="OperationLogPage">
import { onMounted, reactive, ref } from 'vue'
import type { FormInstance } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cleanOperlog, delOperlog, list, type OperLog, type OperLogQuery } from '@/api/monitor/operlog'
import { download } from '@/utils/request'
import { formatUtc } from '@/utils/datetime'
import { useDict } from '@/utils/dict'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'

interface DictOption {
  label: string
  value: string
  elTagType?: string
  elTagClass?: string
  status?: string
}

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const { sys_oper_type, sys_common_status } = useDict('sys_oper_type', 'sys_common_status') as unknown as {
  sys_oper_type: DictOption[]
  sys_common_status: DictOption[]
}

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

function withDateRange(params: OperLogQuery) {
  return {
    ...params,
    params: {
      beginTime: dateRange.value[0],
      endTime: dateRange.value[1]
    }
  }
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
  return sys_oper_type.find((item) => String(item.value) === String(row.businessType))?.label || row.businessType || ''
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

async function handleDelete(row?: OperLog) {
  const operIds = row?.operId || ids.value
  await ElMessageBox.confirm(t('operlog.deleteConfirm').replace('{ids}', String(operIds)), t('common.prompt'), {
    confirmButtonText: t('common.confirm'),
    cancelButtonText: t('common.cancel'),
    type: 'warning'
  })
  await delOperlog(operIds)
  await getList()
  ElMessage.success(t('common.deleteSuccess'))
}

async function handleClean() {
  await ElMessageBox.confirm(t('operlog.cleanConfirm'), t('common.prompt'), {
    confirmButtonText: t('common.confirm'),
    cancelButtonText: t('common.cancel'),
    type: 'warning'
  })
  await cleanOperlog()
  await getList()
  ElMessage.success(t('common.clearSuccess'))
}

function handleExport() {
  download('monitor/operlog/export', { ...queryParams }, `operlog_${Date.now()}.xlsx`)
}

onMounted(getList)
</script>
