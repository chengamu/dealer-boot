<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true">
      <el-form-item :label="t('legacy.noticeTitle')" prop="noticeTitle">
        <el-input
          v-model="queryParams.noticeTitle"
          :placeholder="t('legacy.noticeTitlePlaceholder')"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item :label="t('legacy.operator')" prop="createBy">
        <el-input
          v-model="queryParams.createBy"
          :placeholder="t('legacy.createByPlaceholder')"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item :label="t('legacy.type')" prop="noticeType">
        <el-select v-model="queryParams.noticeType" :placeholder="t('legacy.noticeTypePlaceholder')" clearable style="width: 200px">
          <el-option v-for="dict in sys_notice_type" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">{{ t('common.search') }}</el-button>
        <el-button icon="Refresh" @click="resetQuery">{{ t('common.reset') }}</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['system:notice:add']">
          {{ t('common.add') }}
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()" v-hasPermi="['system:notice:edit']">
          {{ t('common.edit') }}
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['system:notice:remove']">
          {{ t('common.delete') }}
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="noticeList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column :label="t('common.index')" align="center" type="index" width="80" />
      <el-table-column :label="t('legacy.noticeTitle')" align="center" prop="noticeTitle" :show-overflow-tooltip="true" />
      <el-table-column :label="t('legacy.noticeType')" align="center" prop="noticeType" width="120">
        <template #default="{ row }">
          <dict-tag :options="sys_notice_type" :value="row.noticeType" />
        </template>
      </el-table-column>
      <el-table-column :label="t('user.status')" align="center" prop="status" width="100">
        <template #default="{ row }">
          <dict-tag :options="sys_notice_status" :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column :label="t('legacy.createdBy')" align="center" prop="createBy" width="120" />
      <el-table-column :label="t('common.createTime')" align="center" prop="createTime" width="140">
        <template #default="{ row }">
          <span>{{ formatUtc(row.createTime, 'YYYY-MM-DD') }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="t('common.operate')" align="center" class-name="small-padding fixed-width">
        <template #default="{ row }">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(row)" v-hasPermi="['system:notice:edit']">
            {{ t('common.edit') }}
          </el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(row)" v-hasPermi="['system:notice:remove']">
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

    <el-drawer v-model="open" :title="title" size="760px" append-to-body destroy-on-close @closed="reset">
      <el-form ref="noticeRef" :model="form" :rules="rules" label-width="90px">
        <el-row>
          <el-col :span="24">
            <el-form-item :label="t('legacy.noticeTitle')" prop="noticeTitle">
              <el-input v-model="form.noticeTitle" :placeholder="t('legacy.noticeTitlePlaceholder')" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="t('legacy.noticeType')" prop="noticeType">
              <el-select v-model="form.noticeType" :placeholder="t('common.selectPlaceholder')">
                <el-option v-for="dict in sys_notice_type" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="t('user.status')">
              <el-radio-group v-model="form.status">
                <el-radio v-for="dict in sys_notice_status" :key="dict.value" :value="dict.value">{{ dict.label }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="t('legacy.noticeContent')">
              <Editor v-model="form.noticeContent" :min-height="280" />
            </el-form-item>
          </el-col>
        </el-row>
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

<script setup lang="ts" name="NoticePage">
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { addNotice, delNotice, getNotice, listNotice, updateNotice, type Notice, type NoticeQuery } from '@/api/system/notice'
import Editor from '@/components/Editor/index.vue'
import { formatUtc } from '@/utils/datetime'
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
const { sys_notice_status, sys_notice_type } = useDict('sys_notice_status', 'sys_notice_type')

const noticeList = ref<Notice[]>([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref<Array<number | string>>([])
const total = ref(0)
const queryRef = ref<FormInstance>()
const noticeRef = ref<FormInstance>()
const form = ref<Notice>({})
const queryParams = reactive<NoticeQuery>({
  pageNum: 1,
  pageSize: 10
})

const single = computed(() => ids.value.length !== 1)
const multiple = computed(() => ids.value.length === 0)
const title = computed(() => (form.value.noticeId ? t('legacy.editNotice') : t('legacy.addNotice')))
const rules = computed<FormRules<Notice>>(() => ({
  noticeTitle: [{ required: true, message: t('legacy.noticeTitleRequired'), trigger: 'blur' }],
  noticeType: [{ required: true, message: t('legacy.noticeTypeRequired'), trigger: 'change' }]
}))

function reset() {
  form.value = {
    noticeTitle: undefined,
    noticeType: undefined,
    noticeContent: undefined,
    status: '1'
  }
  noticeRef.value?.resetFields()
}

async function getList() {
  loading.value = true
  try {
    const response = await listNotice(queryParams)
    noticeList.value = response.rows || []
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

function handleSelectionChange(selection: Notice[]) {
  ids.value = selection.map((item) => String(item.noticeId)).filter(Boolean)
}

function handleAdd() {
  reset()
  open.value = true
}

async function handleUpdate(row?: Notice) {
  reset()
  const noticeId = row?.noticeId || ids.value[0]
  if (!noticeId) return
  try {
    form.value = await getNotice(noticeId)
    open.value = true
  } catch {
    // Request interceptor already displays the backend error.
  }
}

async function submitForm() {
  const valid = await noticeRef.value?.validate().catch(() => false)
  if (!valid) return
  try {
    if (form.value.noticeId) {
      await updateNotice(form.value)
      ElMessage.success(t('common.editSuccess'))
    } else {
      await addNotice(form.value)
      ElMessage.success(t('common.addSuccess'))
    }
    open.value = false
    reset()
    await getList()
  } catch {
    // Request interceptor already displays the backend error.
  }
}

async function handleDelete(row?: Notice) {
  const noticeIds = row?.noticeId || ids.value
  if (!noticeIds || (Array.isArray(noticeIds) && !noticeIds.length)) return
  try {
    await ElMessageBox.confirm(t('legacy.deleteNoticeConfirm', { ids: Array.isArray(noticeIds) ? noticeIds.join(',') : noticeIds }), t('common.prompt'), {
      type: 'warning'
    })
    await delNotice(noticeIds)
    ElMessage.success(t('common.deleteSuccess'))
    await getList()
  } catch {
    // User cancelled or the request interceptor already displayed the backend error.
  }
}

getList()
</script>
