<template>
  <div class="app-container monitor-table-page">
    <el-form ref="queryRef" :model="queryParams" :inline="true" class="monitor-table-page__search">
      <el-form-item :label="t('legacy.loginIp')" prop="ipaddr">
        <el-input
          v-model="queryParams.ipaddr"
          :placeholder="t('legacy.loginIpPlaceholder')"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item :label="t('user.userName')" prop="userName">
        <el-input
          v-model="queryParams.userName"
          :placeholder="t('user.userNamePlaceholder')"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">{{ t('common.search') }}</el-button>
        <el-button icon="Refresh" @click="resetQuery">{{ t('common.reset') }}</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="pagedOnlineList" border class="monitor-table-page__table" style="width: 100%">
      <el-table-column :label="t('common.index')" width="70" type="index" align="center">
        <template #default="scope">
          <span>{{ (pageNum - 1) * pageSize + scope.$index + 1 }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="t('legacy.loginName')" align="left" prop="userName" :show-overflow-tooltip="true" />
      <el-table-column :label="t('legacy.dept')" align="left" prop="deptName" :show-overflow-tooltip="true" />
      <el-table-column :label="t('legacy.host')" align="left" prop="ipaddr" :show-overflow-tooltip="true" />
      <el-table-column :label="t('legacy.loginLocation')" align="left" prop="loginLocation" :show-overflow-tooltip="true" />
      <el-table-column :label="t('legacy.os')" align="left" prop="os" :show-overflow-tooltip="true" />
      <el-table-column :label="t('legacy.browser')" align="left" prop="browser" :show-overflow-tooltip="true" />
      <el-table-column :label="t('legacy.loginTime')" align="center" prop="loginTime" width="180">
        <template #default="scope">
          <span>{{ formatUtc(scope.row.loginTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="t('common.operate')" align="center" width="130" class-name="small-padding fixed-width">
        <template #default="scope">
          <AdminTableActions :actions="[
            { label: t('legacy.forceLogout'), icon: 'Delete', type: 'danger', permission: 'monitor:online:forceLogout', onClick: () => handleForceLogout(scope.row) }
          ]" />
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" v-model:page="pageNum" v-model:limit="pageSize" :total="total" class="monitor-table-page__pagination" />
  </div>
</template>

<script setup lang="ts" name="OnlineUsersPage">
import { computed, onMounted, reactive, ref } from 'vue'
import type { FormInstance } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { forceLogout, list, type OnlineUser, type OnlineUserQuery } from '@/api/monitor/online'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { formatUtc } from '@/utils/datetime'

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)

const queryRef = ref<FormInstance>()
const onlineList = ref<OnlineUser[]>([])
const loading = ref(false)
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const queryParams = reactive<OnlineUserQuery>({
  ipaddr: undefined,
  userName: undefined
})

const pagedOnlineList = computed(() => onlineList.value.slice((pageNum.value - 1) * pageSize.value, pageNum.value * pageSize.value))

async function getList() {
  loading.value = true
  try {
    const response = await list(queryParams)
    onlineList.value = response.rows || []
    total.value = response.total ?? onlineList.value.length
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  pageNum.value = 1
  getList()
}

function resetQuery() {
  queryRef.value?.resetFields()
  handleQuery()
}

async function handleForceLogout(row: OnlineUser) {
  try {
    await ElMessageBox.confirm(t('legacy.forceLogoutConfirm').replace('{name}', row.userName || row.tokenId), t('common.prompt'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
    await forceLogout(row.tokenId)
    await getList()
    ElMessage.success(t('legacy.forceLogoutSuccess'))
  } catch {
    // User cancelled or the request interceptor already displayed the backend error.
  }
}

onMounted(getList)
</script>
