<template>
  <el-dialog v-model="visible" :title="t('role.selectUser')" width="800px" top="5vh" append-to-body>
    <el-form ref="queryRef" :model="queryParams" :inline="true">
      <el-form-item :label="t('user.userName')" prop="userName">
        <el-input v-model="queryParams.userName" :placeholder="t('user.userNamePlaceholder')" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item :label="t('user.phonenumber')" prop="phonenumber">
        <el-input v-model="queryParams.phonenumber" :placeholder="t('user.phonenumberPlaceholder')" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">{{ t('common.search') }}</el-button>
        <el-button icon="Refresh" @click="resetQuery">{{ t('common.reset') }}</el-button>
      </el-form-item>
    </el-form>
    <el-row>
      <el-table ref="tableRef" :data="userList" height="260px" @row-click="clickRow" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column :label="t('user.userName')" prop="userName" :show-overflow-tooltip="true" />
        <el-table-column :label="t('user.nickName')" prop="nickName" :show-overflow-tooltip="true" />
        <el-table-column :label="t('user.email')" prop="email" :show-overflow-tooltip="true" />
        <el-table-column :label="t('user.phonenumber')" prop="phonenumber" :show-overflow-tooltip="true" />
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
      </el-table>
      <pagination
        v-show="total > 0"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        :total="total"
        @pagination="getList"
      />
    </el-row>
    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" @click="handleSelectUser">{{ t('common.confirm') }}</el-button>
        <el-button @click="visible = false">{{ t('common.cancel') }}</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts" name="RoleSelectUser">
import { reactive, ref } from 'vue'
import { ElMessage, type FormInstance } from 'element-plus'
import { authUserSelectAll, unallocatedUserList, type RoleUser, type RoleUserQuery } from '@/api/system/role'
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

const props = defineProps<{
  roleId?: number | string
}>()
const emit = defineEmits<{
  ok: []
}>()

const localeStore = useLocaleStore()
const t = (key: string, params?: Record<string, string | number>) => {
  const message = getMessage(key, localeStore.language)
  if (!params) return message
  return Object.entries(params).reduce((text, [name, value]) => text.replaceAll(`{${name}}`, String(value)), message)
}
const { sys_normal_disable } = useDict('sys_normal_disable') as unknown as { sys_normal_disable: DictOption[] }

const userList = ref<RoleUser[]>([])
const visible = ref(false)
const total = ref(0)
const userIds = ref<Array<number | string>>([])
const queryRef = ref<FormInstance>()
const tableRef = ref()
const queryParams = reactive<RoleUserQuery>({
  pageNum: 1,
  pageSize: 10
})

async function show() {
  queryParams.roleId = props.roleId
  await getList()
  visible.value = true
}

function clickRow(row: RoleUser) {
  tableRef.value?.toggleRowSelection(row)
}

function handleSelectionChange(selection: RoleUser[]) {
  userIds.value = selection.map((item) => String(item.userId)).filter(Boolean)
}

async function getList() {
  const response = await unallocatedUserList(queryParams)
  userList.value = response.rows || []
  total.value = response.total || 0
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  queryRef.value?.resetFields()
  handleQuery()
}

async function handleSelectUser() {
  const selectedUserIds = userIds.value.join(',')
  if (!selectedUserIds) {
    ElMessage.error(t('role.selectUserRequired'))
    return
  }
  const response = await authUserSelectAll({ roleId: queryParams.roleId, userIds: selectedUserIds })
  ElMessage.success(response.msg || t('common.editSuccess'))
  visible.value = false
  emit('ok')
}

defineExpose({
  show
})
</script>
