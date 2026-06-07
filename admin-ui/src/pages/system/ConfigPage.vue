<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true" label-width="80px">
      <el-form-item :label="t('legacy.configName')" prop="configName">
        <el-input
          v-model="queryParams.configName"
          :placeholder="t('legacy.configNamePlaceholder')"
          clearable
          style="width: 240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item :label="t('legacy.configKey')" prop="configKey">
        <el-input
          v-model="queryParams.configKey"
          :placeholder="t('legacy.configKeyPlaceholder')"
          clearable
          style="width: 240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item :label="t('legacy.configType')" prop="configType">
        <el-select v-model="queryParams.configType" :placeholder="t('legacy.configTypePlaceholder')" clearable style="width: 200px">
          <el-option v-for="dict in sys_yes_no" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('common.createTime')" style="width: 330px">
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
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['system:config:add']">
          {{ t('common.add') }}
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()" v-hasPermi="['system:config:edit']">
          {{ t('common.edit') }}
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['system:config:remove']">
          {{ t('common.delete') }}
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['system:config:export']">
          {{ t('common.export') }}
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Refresh" @click="handleRefreshCache" v-hasPermi="['system:config:remove']">
          {{ t('legacy.refreshConfigCache') }}
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="configList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column :label="t('legacy.configName')" align="center" prop="configName" :show-overflow-tooltip="true" />
      <el-table-column :label="t('legacy.configKey')" align="center" prop="configKey" :show-overflow-tooltip="true" />
      <el-table-column :label="t('legacy.configValue')" align="center" prop="configValue" :show-overflow-tooltip="true" />
      <el-table-column :label="t('legacy.configType')" align="center" prop="configType">
        <template #default="{ row }">
          <dict-tag :options="sys_yes_no" :value="row.configType" />
        </template>
      </el-table-column>
      <el-table-column :label="t('user.remark')" align="center" prop="remark" :show-overflow-tooltip="true" />
      <el-table-column :label="t('common.createTime')" align="center" prop="createTime" width="180">
        <template #default="{ row }">
          <span>{{ formatUtc(row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="t('common.operate')" align="center" width="150" class-name="small-padding fixed-width">
        <template #default="{ row }">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(row)" v-hasPermi="['system:config:edit']">
            {{ t('common.edit') }}
          </el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(row)" v-hasPermi="['system:config:remove']">
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

    <el-drawer v-model="open" :title="title" size="520px" append-to-body destroy-on-close @closed="reset">
      <el-form ref="configRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item :label="t('legacy.configName')" prop="configName">
          <el-input v-model="form.configName" :placeholder="t('legacy.configNamePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('legacy.configKey')" prop="configKey">
          <el-input v-model="form.configKey" :placeholder="t('legacy.configKeyPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('legacy.configValue')" prop="configValue">
          <el-input v-model="form.configValue" :placeholder="t('legacy.configValuePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('legacy.configType')" prop="configType">
          <el-radio-group v-model="form.configType">
            <el-radio v-for="dict in sys_yes_no" :key="dict.value" :value="dict.value">{{ dict.label }}</el-radio>
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
    </el-drawer>
  </div>
</template>

<script setup lang="ts" name="ConfigPage">
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { addConfig, delConfig, getConfig, listConfig, refreshCache, updateConfig, type Config, type ConfigQuery } from '@/api/system/config'
import { download } from '@/utils/request'
import { formatUtc, withUtcDateRange } from '@/utils/datetime'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { useDict } from '@/utils/dict'

interface DictOption {
  label: string
  value: string
  elTagType?: string
  elTagClass?: string
}

const localeStore = useLocaleStore()
const t = (key: string, params?: Record<string, string | number>) => {
  const message = getMessage(key, localeStore.language)
  if (!params) return message
  return Object.entries(params).reduce((text, [name, value]) => text.replaceAll(`{${name}}`, String(value)), message)
}
const { sys_yes_no } = useDict('sys_yes_no')

const configList = ref<Config[]>([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref<Array<number | string>>([])
const total = ref(0)
const dateRange = ref<string[]>([])
const queryRef = ref<FormInstance>()
const configRef = ref<FormInstance>()
const form = ref<Config>({})
const queryParams = reactive<ConfigQuery>({
  pageNum: 1,
  pageSize: 10
})

const single = computed(() => ids.value.length !== 1)
const multiple = computed(() => ids.value.length === 0)
const title = computed(() => (form.value.configId ? t('legacy.editConfig') : t('legacy.addConfig')))
const rules = computed<FormRules<Config>>(() => ({
  configName: [{ required: true, message: t('legacy.configNameRequired'), trigger: 'blur' }],
  configKey: [{ required: true, message: t('legacy.configKeyRequired'), trigger: 'blur' }],
  configValue: [{ required: true, message: t('legacy.configValueRequired'), trigger: 'blur' }]
}))

function withDateRange(query: ConfigQuery) {
  return withUtcDateRange(query, dateRange.value)
}

function reset() {
  form.value = {
    configName: undefined,
    configKey: undefined,
    configValue: undefined,
    configType: 'Y',
    remark: undefined
  }
  configRef.value?.resetFields()
}

async function getList() {
  loading.value = true
  try {
    const response = await listConfig(withDateRange(queryParams))
    configList.value = response.rows || []
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
  dateRange.value = []
  queryRef.value?.resetFields()
  handleQuery()
}

function handleSelectionChange(selection: Config[]) {
  ids.value = selection.map((item) => String(item.configId)).filter(Boolean)
}

function handleAdd() {
  reset()
  open.value = true
}

async function handleUpdate(row?: Config) {
  reset()
  const configId = row?.configId || ids.value[0]
  if (!configId) return
  try {
    form.value = await getConfig(configId)
    open.value = true
  } catch {
    // Request interceptor already displays the backend error.
  }
}

async function submitForm() {
  const valid = await configRef.value?.validate().catch(() => false)
  if (!valid) return
  try {
    if (form.value.configId) {
      await updateConfig(form.value)
      ElMessage.success(t('common.editSuccess'))
    } else {
      await addConfig(form.value)
      ElMessage.success(t('common.addSuccess'))
    }
    open.value = false
    reset()
    await getList()
  } catch {
    // Request interceptor already displays the backend error.
  }
}

async function handleDelete(row?: Config) {
  const configIds = row?.configId || ids.value
  if (!configIds || (Array.isArray(configIds) && !configIds.length)) return
  try {
    await ElMessageBox.confirm(t('legacy.deleteConfigConfirm', { ids: Array.isArray(configIds) ? configIds.join(',') : configIds }), t('common.prompt'), {
      type: 'warning'
    })
    await delConfig(configIds)
    ElMessage.success(t('common.deleteSuccess'))
    await getList()
  } catch {
    // User cancelled or the request interceptor already displayed the backend error.
  }
}

function handleExport() {
  download('system/config/export', withDateRange(queryParams), `config_${Date.now()}.xlsx`)
}

async function handleRefreshCache() {
  try {
    await refreshCache()
    ElMessage.success(t('legacy.refreshConfigCacheSuccess'))
  } catch {
    // Request interceptor already displays the backend error.
  }
}

getList()
</script>
