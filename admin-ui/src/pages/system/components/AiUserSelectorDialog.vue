<template>
  <AdminDialog v-model="visible" :title="title" width="920px" top="5vh" variant="picker" class="admin-selector-dialog" append-to-body>
    <div class="admin-dialog__toolbar">
      <el-form ref="queryRef" :model="queryParams" :inline="true" class="admin-selector-dialog__form">
        <el-form-item :label="t('user.userName')" prop="userName">
          <el-input v-model="queryParams.userName" :placeholder="t('user.userNamePlaceholder')" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item :label="t('user.phonenumber')" prop="phonenumber">
          <el-input v-model="queryParams.phonenumber" :placeholder="t('user.phonenumberPlaceholder')" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">{{ t('common.search') }}</el-button>
          <el-button icon="Refresh" @click="resetQuery">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="admin-dialog__table admin-selector-dialog__table">
      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="userList"
        height="100%"
        border
        @row-click="clickRow"
        @selection-change="handleSelectionChange"
      >
        <el-table-column v-if="multiple" type="selection" width="55" />
        <el-table-column v-else width="55" align="center">
          <template #default="{ row }">
            <el-radio :model-value="singleUserId" :value="row.userId" @change="selectSingle(row)" />
          </template>
        </el-table-column>
        <el-table-column :label="t('user.userName')" prop="userName" show-overflow-tooltip />
        <el-table-column :label="t('user.nickName')" prop="nickName" show-overflow-tooltip />
        <el-table-column :label="t('user.email')" prop="email" show-overflow-tooltip />
        <el-table-column :label="t('user.phonenumber')" prop="phonenumber" show-overflow-tooltip />
        <el-table-column :label="t('user.status')" align="center" prop="status" width="100">
          <template #default="{ row }">
            <dict-tag :options="sys_normal_disable" :value="row.status" />
          </template>
        </el-table-column>
        <el-table-column :label="t('common.createTime')" align="center" prop="createTime" width="180">
          <template #default="{ row }">{{ formatUtc(row.createTime) || '-' }}</template>
        </el-table-column>
      </el-table>
    </div>

    <pagination
      v-show="total > 0"
      class="admin-selector-dialog__pagination"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      :total="total"
      @pagination="getList"
    />

    <template #footer>
      <AdminDialogFooter :status="selectedCountText">
        <el-button type="primary" @click="handleConfirm">{{ t('common.confirm') }}</el-button>
        <el-button @click="visible = false">{{ t('common.cancel') }}</el-button>
      </AdminDialogFooter>
    </template>
  </AdminDialog>
</template>

<script setup lang="ts" name="AiUserSelectorDialog">
import { computed, reactive, ref } from 'vue'
import { ElMessage, type FormInstance } from 'element-plus'
import AdminDialog from '@/components/AdminDialog/index.vue'
import AdminDialogFooter from '@/components/AdminDialogFooter/index.vue'
import { listUser, type SysUser, type UserQuery } from '@/api/system/user'
import { formatUtc } from '@/utils/datetime'
import { useDict } from '@/utils/dict'
import { useAiI18n } from '../useAiI18n'

const props = withDefaults(defineProps<{
  multiple?: boolean
  title?: string
}>(), {
  multiple: true,
  title: ''
})

const emit = defineEmits<{
  confirm: [users: SysUser[]]
}>()

const t = useAiI18n()
const { sys_normal_disable } = useDict('sys_normal_disable')
const visible = ref(false)
const loading = ref(false)
const total = ref(0)
const userList = ref<SysUser[]>([])
const selectedUsers = ref<SysUser[]>([])
const singleUser = ref<SysUser>()
const queryRef = ref<FormInstance>()
const tableRef = ref()
const queryParams = reactive<UserQuery>({
  pageNum: 1,
  pageSize: 10
})

const title = computed(() => props.title || t('ai.settings.selectUser'))
const singleUserId = computed(() => singleUser.value?.userId)
const selectedCountText = computed(() => t('common.selectedCount', { count: props.multiple ? selectedUsers.value.length : singleUser.value ? 1 : 0 }))

async function show() {
  selectedUsers.value = []
  singleUser.value = undefined
  await getList()
  visible.value = true
}

async function getList() {
  loading.value = true
  try {
    const res = await listUser(queryParams)
    userList.value = res.rows || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

function clickRow(row: SysUser) {
  if (props.multiple) {
    tableRef.value?.toggleRowSelection(row)
  } else {
    selectSingle(row)
  }
}

function selectSingle(row: SysUser) {
  singleUser.value = row
}

function handleSelectionChange(selection: SysUser[]) {
  selectedUsers.value = selection
}

function handleQuery() {
  queryParams.pageNum = 1
  void getList()
}

function resetQuery() {
  queryRef.value?.resetFields()
  handleQuery()
}

function handleConfirm() {
  const users = props.multiple ? selectedUsers.value : singleUser.value ? [singleUser.value] : []
  if (!users.length) {
    ElMessage.warning(t('ai.settings.selectUserRequired'))
    return
  }
  emit('confirm', users)
  visible.value = false
}

defineExpose({
  show
})
</script>
