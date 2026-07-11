<template>
  <div class="app-container role-auth-user-page">
    <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true">
      <el-form-item :label="t('user.userName')" prop="userName">
        <el-input v-model="queryParams.userName" :placeholder="t('user.userNamePlaceholder')" clearable style="width: 240px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item :label="t('user.phonenumber')" prop="phonenumber">
        <el-input v-model="queryParams.phonenumber" :placeholder="t('user.phonenumberPlaceholder')" clearable style="width: 240px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">{{ t('common.search') }}</el-button>
        <el-button icon="Refresh" @click="resetQuery">{{ t('common.reset') }}</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" icon="Plus" @click="openSelectUser" v-hasPermi="['system:role:add']">{{ t('role.addUser') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="CircleClose" :disabled="multiple" @click="cancelAuthUserAll" v-hasPermi="['system:role:remove']">
          {{ t('role.batchCancelAuth') }}
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Close" @click="handleClose">{{ t('common.close') }}</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="userList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
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
      <el-table-column :label="t('common.operate')" align="center" width="140" class-name="small-padding fixed-width">
        <template #default="{ row }">
          <AdminTableActions :actions="[
            { label: t('role.cancelAuth'), icon: 'CircleClose', type: 'danger', permission: 'system:role:remove', onClick: () => cancelAuthUser(row) }
          ]" />
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
    <RoleSelectUser ref="selectRef" :role-id="queryParams.roleId" @ok="handleQuery" />
  </div>
</template>

<script setup lang="ts" name="RoleAuthUserPage">
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { allocatedUserList, authUserCancel, authUserCancelAll, type RoleUser, type RoleUserQuery } from '@/api/system/role'
import { formatUtc } from '@/utils/datetime'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { useDict } from '@/utils/dict'
import RoleSelectUser from './RoleSelectUser.vue'

const route = useRoute()
const router = useRouter()
const localeStore = useLocaleStore()
const t = (key: string, params?: Record<string, string | number>) => {
  const message = getMessage(key, localeStore.language)
  if (!params) return message
  return Object.entries(params).reduce((text, [name, value]) => text.replaceAll(`{${name}}`, String(value)), message)
}
const { sys_normal_disable } = useDict('sys_normal_disable')

const userList = ref<RoleUser[]>([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const userIds = ref<Array<number | string>>([])
const queryRef = ref<FormInstance>()
const selectRef = ref<InstanceType<typeof RoleSelectUser>>()
const queryParams = reactive<RoleUserQuery>({
  pageNum: 1,
  pageSize: 10,
  roleId: String(route.params.roleId || '')
})
const multiple = computed(() => userIds.value.length === 0)

async function getList() {
  loading.value = true
  try {
    const response = await allocatedUserList(queryParams)
    userList.value = response.rows || []
    total.value = response.total || 0
  } finally {
    loading.value = false
  }
}

function handleClose() {
  router.push('/system/role')
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  queryRef.value?.resetFields()
  handleQuery()
}

function handleSelectionChange(selection: RoleUser[]) {
  userIds.value = selection.map((item) => String(item.userId)).filter(Boolean)
}

function openSelectUser() {
  selectRef.value?.show()
}

async function cancelAuthUser(row: RoleUser) {
  try {
    await ElMessageBox.confirm(t('role.cancelAuthConfirm', { name: row.userName || '' }), t('common.prompt'), { type: 'warning' })
    await authUserCancel({ userId: row.userId, roleId: queryParams.roleId })
    ElMessage.success(t('role.cancelAuthSuccess'))
    await getList()
  } catch {
    // User cancelled or the request interceptor already displayed the backend error.
  }
}

async function cancelAuthUserAll() {
  try {
    await ElMessageBox.confirm(t('role.batchCancelAuthConfirm'), t('common.prompt'), { type: 'warning' })
    await authUserCancelAll({ roleId: queryParams.roleId, userIds: userIds.value.join(',') })
    ElMessage.success(t('role.cancelAuthSuccess'))
    await getList()
  } catch {
    // User cancelled or the request interceptor already displayed the backend error.
  }
}

getList()
</script>
