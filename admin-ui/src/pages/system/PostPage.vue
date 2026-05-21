<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true">
      <el-form-item :label="t('legacy.postCode')" prop="postCode">
        <el-input
          v-model="queryParams.postCode"
          :placeholder="t('legacy.postCodePlaceholder')"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item :label="t('legacy.postName')" prop="postName">
        <el-input
          v-model="queryParams.postName"
          :placeholder="t('legacy.postNamePlaceholder')"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item :label="t('user.status')" prop="status">
        <el-select v-model="queryParams.status" :placeholder="t('legacy.postStatusPlaceholder')" clearable style="width: 200px">
          <el-option v-for="dict in sys_normal_disable" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">{{ t('common.search') }}</el-button>
        <el-button icon="Refresh" @click="resetQuery">{{ t('common.reset') }}</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['system:post:add']">
          {{ t('common.add') }}
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()" v-hasPermi="['system:post:edit']">
          {{ t('common.edit') }}
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['system:post:remove']">
          {{ t('common.delete') }}
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['system:post:export']">
          {{ t('common.export') }}
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="postList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column :label="t('legacy.postId')" align="center" prop="postId" />
      <el-table-column :label="t('legacy.postCode')" align="center" prop="postCode" />
      <el-table-column :label="t('legacy.postName')" align="center" prop="postName" />
      <el-table-column :label="t('legacy.postSort')" align="center" prop="postSort" />
      <el-table-column :label="t('user.status')" align="center" prop="status">
        <template #default="{ row }">
          <dict-tag :options="sys_normal_disable" :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column :label="t('common.createTime')" align="center" prop="createTime" width="180">
        <template #default="{ row }">
          <span>{{ formatUtc(row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="t('common.operate')" width="180" align="center" class-name="small-padding fixed-width">
        <template #default="{ row }">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(row)" v-hasPermi="['system:post:edit']">
            {{ t('common.edit') }}
          </el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(row)" v-hasPermi="['system:post:remove']">
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
      <el-form ref="postRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item :label="t('legacy.postName')" prop="postName">
          <el-input v-model="form.postName" :placeholder="t('legacy.postNamePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('legacy.postCode')" prop="postCode">
          <el-input v-model="form.postCode" :placeholder="t('legacy.postCodePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('legacy.postSort')" prop="postSort">
          <el-input-number v-model="form.postSort" controls-position="right" :min="0" />
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

<script setup lang="ts" name="PostPage">
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { addPost, delPost, getPost, listPost, updatePost, type Post, type PostQuery } from '@/api/system/post'
import { download } from '@/utils/request'
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
const { sys_normal_disable } = useDict('sys_normal_disable') as unknown as { sys_normal_disable: DictOption[] }

const postList = ref<Post[]>([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref<number[]>([])
const total = ref(0)
const queryRef = ref<FormInstance>()
const postRef = ref<FormInstance>()
const form = ref<Post>({})
const queryParams = reactive<PostQuery>({
  pageNum: 1,
  pageSize: 10
})

const single = computed(() => ids.value.length !== 1)
const multiple = computed(() => ids.value.length === 0)
const title = computed(() => (form.value.postId ? t('legacy.editPost') : t('legacy.addPost')))
const rules = computed<FormRules<Post>>(() => ({
  postName: [{ required: true, message: t('legacy.postNameRequired'), trigger: 'blur' }],
  postCode: [{ required: true, message: t('legacy.postCodeRequired'), trigger: 'blur' }],
  postSort: [{ required: true, message: t('legacy.postSortRequired'), trigger: 'blur' }]
}))

function reset() {
  form.value = {
    postCode: undefined,
    postName: undefined,
    postSort: 0,
    status: '1',
    remark: undefined
  }
  postRef.value?.resetFields()
}

async function getList() {
  loading.value = true
  try {
    const response = await listPost(queryParams)
    postList.value = response.rows || []
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

function handleSelectionChange(selection: Post[]) {
  ids.value = selection.map((item) => Number(item.postId)).filter(Boolean)
}

function handleAdd() {
  reset()
  open.value = true
}

async function handleUpdate(row?: Post) {
  reset()
  const postId = row?.postId || ids.value[0]
  if (!postId) return
  form.value = await getPost(postId)
  open.value = true
}

async function submitForm() {
  const valid = await postRef.value?.validate().catch(() => false)
  if (!valid) return
  if (form.value.postId) {
    await updatePost(form.value)
    ElMessage.success(t('common.editSuccess'))
  } else {
    await addPost(form.value)
    ElMessage.success(t('common.addSuccess'))
  }
  open.value = false
  await getList()
}

async function handleDelete(row?: Post) {
  const postIds = row?.postId || ids.value
  if (!postIds || (Array.isArray(postIds) && !postIds.length)) return
  await ElMessageBox.confirm(t('legacy.deletePostConfirm', { ids: Array.isArray(postIds) ? postIds.join(',') : postIds }), t('common.prompt'), {
    type: 'warning'
  })
  await delPost(postIds)
  ElMessage.success(t('common.deleteSuccess'))
  await getList()
}

function handleExport() {
  download('system/post/export', { ...queryParams }, `post_${Date.now()}.xlsx`)
}

getList()
</script>
