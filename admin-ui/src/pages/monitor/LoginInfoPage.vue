<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true" label-width="90px">
      <el-form-item :label="t('legacy.loginIp')" prop="ipaddr">
        <el-input
          v-model="queryParams.ipaddr"
          :placeholder="t('legacy.loginIpPlaceholder')"
          clearable
          style="width: 240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item :label="t('user.userName')" prop="userName">
        <el-input
          v-model="queryParams.userName"
          :placeholder="t('user.userNamePlaceholder')"
          clearable
          style="width: 240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item :label="t('legacy.loginStatus')" prop="status">
        <el-select v-model="queryParams.status" :placeholder="t('legacy.loginStatusPlaceholder')" clearable style="width: 240px">
          <el-option v-for="dict in sys_common_status" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('legacy.loginTime')" style="width: 330px">
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
        <el-button v-hasPermi="['monitor:logininfor:remove']" type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()">
          {{ t('common.delete') }}
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button v-hasPermi="['monitor:logininfor:remove']" type="danger" plain icon="Delete" @click="handleClean">
          {{ t('common.clear') }}
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button v-hasPermi="['monitor:logininfor:unlock']" type="primary" plain icon="Unlock" :disabled="single" @click="handleUnlock">
          {{ t('legacy.unlock') }}
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button v-hasPermi="['monitor:logininfor:export']" type="warning" plain icon="Download" @click="handleExport">
          {{ t('common.export') }}
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table
      ref="logininforRef"
      v-loading="loading"
      :data="logininforList"
      :default-sort="defaultSort"
      @selection-change="handleSelectionChange"
      @sort-change="handleSortChange"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column :label="t('legacy.accessId')" align="center" prop="infoId" />
      <el-table-column :label="t('user.userName')" align="center" prop="userName" :show-overflow-tooltip="true" sortable="custom" :sort-orders="['descending', 'ascending']" />
      <el-table-column :label="t('legacy.loginIp')" align="center" prop="ipaddr" :show-overflow-tooltip="true" />
      <el-table-column :label="t('legacy.loginLocation')" align="center" prop="loginLocation" :show-overflow-tooltip="true" />
      <el-table-column :label="t('legacy.os')" align="center" prop="os" :show-overflow-tooltip="true" />
      <el-table-column :label="t('legacy.browser')" align="center" prop="browser" :show-overflow-tooltip="true" />
      <el-table-column :label="t('legacy.loginStatus')" align="center" prop="status">
        <template #default="scope">
          <dict-tag :options="sys_common_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column :label="t('legacy.description')" align="center" prop="msg" :show-overflow-tooltip="true" />
      <el-table-column :label="t('legacy.accessTime')" align="center" prop="loginTime" sortable="custom" :sort-orders="['descending', 'ascending']" width="180">
        <template #default="scope">
          <span>{{ formatUtc(scope.row.loginTime) }}</span>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
  </div>
</template>

<script setup lang="ts" name="LoginInfoPage">
import { onMounted, reactive, ref } from 'vue'
import type { FormInstance } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cleanLogininfor, delLogininfor, list, unlockLogininfor, type LoginInfo, type LoginInfoQuery } from '@/api/monitor/logininfor'
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
const { sys_common_status } = useDict('sys_common_status') as unknown as { sys_common_status: DictOption[] }

const queryRef = ref<FormInstance>()
const logininforList = ref<LoginInfo[]>([])
const loading = ref(false)
const showSearch = ref(true)
const ids = ref<Array<number | string>>([])
const single = ref(true)
const multiple = ref(true)
const selectName = ref('')
const total = ref(0)
const dateRange = ref<string[]>([])
const defaultSort = ref({ prop: 'loginTime', order: 'descending' })
const queryParams = reactive<LoginInfoQuery>({
  pageNum: 1,
  pageSize: 10,
  ipaddr: undefined,
  userName: undefined,
  status: undefined,
  orderByColumn: undefined,
  isAsc: undefined
})

function withDateRange(params: LoginInfoQuery) {
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
    logininforList.value = response.rows || []
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
  queryParams.pageNum = 1
  queryParams.orderByColumn = defaultSort.value.prop
  queryParams.isAsc = defaultSort.value.order
  getList()
}

function handleSelectionChange(selection: LoginInfo[]) {
  ids.value = selection.map((item) => item.infoId)
  multiple.value = selection.length === 0
  single.value = selection.length !== 1
  selectName.value = selection.map((item) => item.userName).filter(Boolean).join(',')
}

function handleSortChange(column: { prop?: string; order?: string }) {
  queryParams.orderByColumn = column.prop
  queryParams.isAsc = column.order
  getList()
}

async function handleDelete(row?: LoginInfo) {
  const infoIds = row?.infoId || ids.value
  await ElMessageBox.confirm(t('legacy.deleteLoginInfoConfirm').replace('{ids}', String(infoIds)), t('common.prompt'), {
    confirmButtonText: t('common.confirm'),
    cancelButtonText: t('common.cancel'),
    type: 'warning'
  })
  await delLogininfor(infoIds)
  await getList()
  ElMessage.success(t('common.deleteSuccess'))
}

async function handleClean() {
  await ElMessageBox.confirm(t('legacy.clearLoginInfoConfirm'), t('common.prompt'), {
    confirmButtonText: t('common.confirm'),
    cancelButtonText: t('common.cancel'),
    type: 'warning'
  })
  await cleanLogininfor()
  await getList()
  ElMessage.success(t('common.clearSuccess'))
}

async function handleUnlock() {
  const username = selectName.value
  await ElMessageBox.confirm(t('legacy.unlockLoginInfoConfirm').replace('{name}', username), t('common.prompt'), {
    confirmButtonText: t('common.confirm'),
    cancelButtonText: t('common.cancel'),
    type: 'warning'
  })
  await unlockLogininfor(username)
  ElMessage.success(t('legacy.unlockLoginInfoSuccess').replace('{name}', username))
}

function handleExport() {
  download('monitor/logininfor/export', { ...queryParams }, `logininfor_${Date.now()}.xlsx`)
}

onMounted(getList)
</script>
