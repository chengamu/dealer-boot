<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true" label-width="80px">
      <el-form-item :label="t('legacy.dictName')" prop="dictName">
        <el-input
          v-model="queryParams.dictName"
          :placeholder="t('legacy.dictNamePlaceholder')"
          clearable
          style="width: 240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item :label="t('legacy.dictType')" prop="dictType">
        <el-input
          v-model="queryParams.dictType"
          :placeholder="t('legacy.dictTypePlaceholder')"
          clearable
          style="width: 240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item :label="t('user.status')" prop="status">
        <el-select v-model="queryParams.status" :placeholder="t('legacy.dictStatusPlaceholder')" clearable style="width: 200px">
          <el-option v-for="dict in sys_normal_disable" :key="dict.value" :label="dict.label" :value="dict.value" />
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
        <el-button type="danger" plain icon="Refresh" @click="handleRefreshCache" v-hasPermi="['system:dict:remove']">
          {{ t('legacy.refreshDictCache') }}
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="typeList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column :label="t('legacy.dictId')" align="center" prop="dictId" />
      <el-table-column :label="t('legacy.dictName')" align="center" prop="dictName" :show-overflow-tooltip="true" />
      <el-table-column :label="t('legacy.dictType')" align="center" :show-overflow-tooltip="true">
        <template #default="{ row }">
          <router-link :to="`/system/dict-data/index/${row.dictId}`" class="link-type">
            <span>{{ row.dictType }}</span>
          </router-link>
        </template>
      </el-table-column>
      <el-table-column :label="t('user.status')" align="center" prop="status">
        <template #default="{ row }">
          <dict-tag :options="sys_normal_disable" :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column :label="t('user.remark')" align="center" prop="remark" :show-overflow-tooltip="true" />
      <el-table-column :label="t('common.createTime')" align="center" prop="createTime" width="180">
        <template #default="{ row }">
          <span>{{ formatUtc(row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="t('common.operate')" align="center" width="160" class-name="small-padding fixed-width">
        <template #default="{ row }">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(row)" v-hasPermi="['system:dict:edit']">
            {{ t('common.edit') }}
          </el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(row)" v-hasPermi="['system:dict:remove']">
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

    <el-dialog v-model="open" :title="title" width="500px" append-to-body>
      <el-form ref="dictRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item :label="t('legacy.dictName')" prop="dictName">
          <el-input v-model="form.dictName" :placeholder="t('legacy.dictNamePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('legacy.dictType')" prop="dictType">
          <el-input v-model="form.dictType" :placeholder="t('legacy.dictTypePlaceholder')" />
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
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="DictTypePage">
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { addType, delType, getType, listType, refreshCache, updateType, type DictType, type DictTypeQuery } from '@/api/system/dict/type'
import { download } from '@/utils/request'
import { formatUtc } from '@/utils/datetime'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { useDict } from '@/utils/dict'
import { useDictStore } from '@/stores/dict'

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
const { sys_normal_disable } = useDict('sys_normal_disable') as unknown as { sys_normal_disable: DictOption[] }

const typeList = ref<DictType[]>([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref<number[]>([])
const total = ref(0)
const dateRange = ref<string[]>([])
const queryRef = ref<FormInstance>()
const dictRef = ref<FormInstance>()
const form = ref<DictType>({})
const queryParams = reactive<DictTypeQuery>({
  pageNum: 1,
  pageSize: 10
})

const single = computed(() => ids.value.length !== 1)
const multiple = computed(() => ids.value.length === 0)
const title = computed(() => (form.value.dictId ? t('legacy.editDictType') : t('legacy.addDictType')))
const rules = computed<FormRules<DictType>>(() => ({
  dictName: [{ required: true, message: t('legacy.dictNameRequired'), trigger: 'blur' }],
  dictType: [{ required: true, message: t('legacy.dictTypeRequired'), trigger: 'blur' }]
}))

function withDateRange(query: DictTypeQuery) {
  const params = { ...query }
  if (dateRange.value?.length === 2) {
    params.beginTime = dateRange.value[0]
    params.endTime = dateRange.value[1]
  }
  return params
}

function reset() {
  form.value = {
    dictName: undefined,
    dictType: undefined,
    status: '1',
    remark: undefined
  }
  dictRef.value?.resetFields()
}

async function getList() {
  loading.value = true
  try {
    const response = await listType(withDateRange(queryParams))
    typeList.value = response.rows || []
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

function handleSelectionChange(selection: DictType[]) {
  ids.value = selection.map((item) => Number(item.dictId)).filter(Boolean)
}

function handleAdd() {
  reset()
  open.value = true
}

async function handleUpdate(row?: DictType) {
  reset()
  const dictId = row?.dictId || ids.value[0]
  if (!dictId) return
  const response = await getType(dictId)
  form.value = response.data
  open.value = true
}

async function submitForm() {
  const valid = await dictRef.value?.validate().catch(() => false)
  if (!valid) return
  if (form.value.dictId) {
    await updateType(form.value)
    ElMessage.success(t('common.editSuccess'))
  } else {
    await addType(form.value)
    ElMessage.success(t('common.addSuccess'))
  }
  open.value = false
  await getList()
}

async function handleDelete(row?: DictType) {
  const dictIds = row?.dictId || ids.value
  if (!dictIds || (Array.isArray(dictIds) && !dictIds.length)) return
  await ElMessageBox.confirm(t('legacy.deleteDictTypeConfirm', { ids: Array.isArray(dictIds) ? dictIds.join(',') : dictIds }), t('common.prompt'), {
    type: 'warning'
  })
  await delType(dictIds)
  ElMessage.success(t('common.deleteSuccess'))
  await getList()
}

function handleExport() {
  download('system/dict/type/export', withDateRange(queryParams), `dict_${Date.now()}.xlsx`)
}

async function handleRefreshCache() {
  await refreshCache()
  useDictStore().cleanDict()
  ElMessage.success(t('legacy.refreshDictCacheSuccess'))
}

getList()
</script>
