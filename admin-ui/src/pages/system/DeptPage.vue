<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true">
      <el-form-item :label="t('legacy.deptName')" prop="deptName">
        <el-input
          v-model="queryParams.deptName"
          :placeholder="t('legacy.deptNamePlaceholder')"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item :label="t('user.status')" prop="status">
        <el-select v-model="queryParams.status" :placeholder="t('legacy.deptStatusPlaceholder')" clearable style="width: 200px">
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
        <el-button type="primary" plain icon="Plus" @click="handleAdd()" v-hasPermi="['system:dept:add']">
          {{ t('common.add') }}
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="Sort" @click="toggleExpandAll">
          {{ t('common.expandCollapse') }}
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table
      v-if="refreshTable"
      v-loading="loading"
      :data="deptList"
      row-key="deptId"
      :default-expand-all="isExpandAll"
      :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
    >
      <el-table-column prop="deptName" :label="t('legacy.deptName')" width="260" />
      <el-table-column prop="orderNum" :label="t('legacy.orderNum')" width="200" />
      <el-table-column prop="status" :label="t('user.status')" width="100">
        <template #default="{ row }">
          <dict-tag :options="sys_normal_disable" :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column :label="t('common.createTime')" align="center" prop="createTime" width="200">
        <template #default="{ row }">
          <span>{{ formatUtc(row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="t('common.operate')" align="center" class-name="small-padding fixed-width">
        <template #default="{ row }">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(row)" v-hasPermi="['system:dept:edit']">
            {{ t('common.edit') }}
          </el-button>
          <el-button link type="primary" icon="Plus" @click="handleAdd(row)" v-hasPermi="['system:dept:add']">
            {{ t('common.add') }}
          </el-button>
          <el-button v-if="row.parentId !== 0" link type="primary" icon="Delete" @click="handleDelete(row)" v-hasPermi="['system:dept:remove']">
            {{ t('common.delete') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-drawer v-model="open" :title="title" size="560px" append-to-body destroy-on-close @closed="reset">
      <el-form ref="deptRef" :model="form" :rules="rules" label-width="90px">
        <el-row>
          <el-col v-if="form.parentId !== 0" :span="24">
            <el-form-item :label="t('legacy.parentDept')" prop="parentId">
              <el-tree-select
                v-model="form.parentId"
                :data="deptOptions"
                :props="{ value: 'deptId', label: 'deptName', children: 'children' }"
                value-key="deptId"
                :placeholder="t('legacy.parentDeptPlaceholder')"
                check-strictly
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="t('legacy.deptName')" prop="deptName">
              <el-input v-model="form.deptName" :placeholder="t('legacy.deptNamePlaceholder')" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="t('legacy.orderNum')" prop="orderNum">
              <el-input-number v-model="form.orderNum" controls-position="right" :min="0" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="t('legacy.leader')" prop="leader">
              <el-input v-model="form.leader" :placeholder="t('legacy.leaderPlaceholder')" maxlength="20" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="t('legacy.phone')" prop="phone">
              <el-input v-model="form.phone" :placeholder="t('legacy.phonePlaceholder')" maxlength="11" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="t('user.email')" prop="email">
              <el-input v-model="form.email" :placeholder="t('user.emailPlaceholder')" maxlength="50" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="t('legacy.deptStatus')">
              <el-radio-group v-model="form.status">
                <el-radio v-for="dict in sys_normal_disable" :key="dict.value" :value="dict.value">{{ dict.label }}</el-radio>
              </el-radio-group>
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

<script setup lang="ts" name="DeptPage">
import { computed, nextTick, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { addDept, delDept, getDept, listDept, listDeptExcludeChild, updateDept, type Dept, type DeptQuery } from '@/api/system/dept'
import { formatUtc } from '@/utils/datetime'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { useDict } from '@/utils/dict'
import { handleTree } from '@/utils/ruoyi'

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
const { sys_normal_disable } = useDict('sys_normal_disable')

const deptList = ref<Dept[]>([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const deptOptions = ref<Dept[]>([])
const isExpandAll = ref(true)
const refreshTable = ref(true)
const queryRef = ref<FormInstance>()
const deptRef = ref<FormInstance>()
const form = ref<Dept>({})
const queryParams = reactive<DeptQuery>({})

const title = computed(() => (form.value.deptId ? t('legacy.editDept') : t('legacy.addDept')))
const rules = computed<FormRules>(() => ({
  parentId: [{ required: true, message: t('legacy.parentDeptRequired'), trigger: 'blur' }],
  deptName: [{ required: true, message: t('legacy.deptNameRequired'), trigger: 'blur' }],
  orderNum: [{ required: true, message: t('legacy.orderNumRequired'), trigger: 'blur' }],
  email: [{ type: 'email', message: t('user.emailInvalid'), trigger: ['blur', 'change'] }],
  phone: [{ pattern: /^1[3-9][0-9]\d{8}$/, message: t('user.phonenumberInvalid'), trigger: 'blur' }]
}))

function reset() {
  form.value = {
    deptId: undefined,
    parentId: undefined,
    deptName: undefined,
    orderNum: 0,
    leader: undefined,
    phone: undefined,
    email: undefined,
    status: '1'
  }
  deptRef.value?.resetFields()
}

async function getList() {
  loading.value = true
  try {
    const rows = await listDept(queryParams)
    deptList.value = handleTree(rows || [], 'deptId') as Dept[]
  } finally {
    loading.value = false
  }
}

function cancel() {
  open.value = false
  reset()
}

function handleQuery() {
  getList()
}

function resetQuery() {
  queryRef.value?.resetFields()
  handleQuery()
}

async function handleAdd(row?: Dept) {
  reset()
  const rows = await listDept()
  deptOptions.value = handleTree(rows || [], 'deptId') as Dept[]
  form.value.parentId = row?.deptId ?? 0
  open.value = true
}

function toggleExpandAll() {
  refreshTable.value = false
  isExpandAll.value = !isExpandAll.value
  nextTick(() => {
    refreshTable.value = true
  })
}

async function handleUpdate(row: Dept) {
  reset()
  const deptId = row.deptId
  if (!deptId) return
  try {
    form.value = await getDept(deptId)
    const rows = await listDeptExcludeChild(deptId)
    deptOptions.value = handleTree(rows || [], 'deptId') as Dept[]
    if (!deptOptions.value.length && form.value.parentId) {
      deptOptions.value.push({ deptId: form.value.parentId, deptName: form.value.parentName, children: [] })
    }
    open.value = true
  } catch {
    // Request interceptor already displays the backend error.
  }
}

async function submitForm() {
  const valid = await deptRef.value?.validate().catch(() => false)
  if (!valid) return
  try {
    if (form.value.deptId) {
      await updateDept(form.value)
      ElMessage.success(t('common.editSuccess'))
    } else {
      await addDept(form.value)
      ElMessage.success(t('common.addSuccess'))
    }
    open.value = false
    reset()
    await getList()
  } catch {
    // Request interceptor already displays the backend error.
  }
}

async function handleDelete(row: Dept) {
  if (!row.deptId) return
  try {
    await ElMessageBox.confirm(t('legacy.deleteDeptConfirm', { name: row.deptName || '' }), t('common.prompt'), { type: 'warning' })
    await delDept(row.deptId)
    ElMessage.success(t('common.deleteSuccess'))
    await getList()
  } catch {
    // User cancelled or the request interceptor already displayed the backend error.
  }
}

getList()
</script>
