<template>
  <div class="app-container user-auth-role-page">
    <h4 class="form-header h4">{{ t('user.basicInfo') }}</h4>
    <el-form :model="form" label-width="90px">
      <el-row>
        <el-col :span="8" :offset="2">
          <el-form-item :label="t('user.nickName')" prop="nickName">
            <el-input v-model="form.nickName" disabled />
          </el-form-item>
        </el-col>
        <el-col :span="8" :offset="2">
          <el-form-item :label="t('user.userName')" prop="userName">
            <el-input v-model="form.userName" disabled />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <h4 class="form-header h4">{{ t('user.roleInfo') }}</h4>
    <el-table
      ref="roleRef"
      v-loading="loading"
      :row-key="getRowKey"
      :data="pagedRoles"
      @row-click="clickRow"
      @selection-change="handleSelectionChange"
    >
      <el-table-column :label="t('common.index')" width="70" type="index" align="center">
        <template #default="{ $index }">
          <span>{{ (pageNum - 1) * pageSize + $index + 1 }}</span>
        </template>
      </el-table-column>
      <el-table-column type="selection" :reserve-selection="true" width="55" />
      <el-table-column :label="t('role.roleName')" align="center" prop="roleName" />
      <el-table-column :label="t('role.roleKey')" align="center" prop="roleKey" />
      <el-table-column :label="t('common.createTime')" align="center" prop="createTime" width="180">
        <template #default="{ row }">
          <span>{{ formatUtc(row.createTime) }}</span>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" v-model:page="pageNum" v-model:limit="pageSize" :total="total" />

    <el-form class="auth-page-actions" label-width="100px">
      <div>
        <el-button type="primary" @click="submitForm">{{ t('common.submit') }}</el-button>
        <el-button @click="close">{{ t('common.back') }}</el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup lang="ts" name="UserAuthRolePage">
import { computed, nextTick, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getAuthRole, updateAuthRole, type SysUser, type UserOptionRole } from '@/api/system/user'
import { formatUtc } from '@/utils/datetime'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { useTagsViewStore, type TagView } from '@/stores/tagsView'

const route = useRoute()
const router = useRouter()
const tagsViewStore = useTagsViewStore()
const localeStore = useLocaleStore()
const t = (key: string, params?: Record<string, string | number>) => {
  const message = getMessage(key, localeStore.language)
  if (!params) return message
  return Object.entries(params).reduce((text, [name, value]) => text.replaceAll(`{${name}}`, String(value)), message)
}

const loading = ref(true)
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const roleIds = ref<Array<number | string>>([])
const roles = ref<UserOptionRole[]>([])
const form = ref<SysUser>({})
const roleRef = ref()

const pagedRoles = computed(() => roles.value.slice((pageNum.value - 1) * pageSize.value, pageNum.value * pageSize.value))

function clickRow(row: UserOptionRole) {
  roleRef.value?.toggleRowSelection(row)
}

function handleSelectionChange(selection: UserOptionRole[]) {
  roleIds.value = selection.map((item) => String(item.roleId)).filter(Boolean)
}

function getRowKey(row: UserOptionRole) {
  return row.roleId
}

function close() {
  const target = { path: '/system/user' }
  tagsViewStore.delView(route as unknown as TagView)
  router.push(target)
}

async function submitForm() {
  try {
    await updateAuthRole({
      userId: form.value.userId,
      roleIds: roleIds.value.join(',')
    })
    ElMessage.success(t('user.authRoleSuccess'))
    close()
  } catch {
    // Request interceptor already displays the backend error.
  }
}

async function loadAuthRoles() {
  const userId = String(route.params.userId || '')
  if (!userId) return
  loading.value = true
  try {
    const response = await getAuthRole(userId)
    form.value = response.data.user || {}
    roles.value = response.data.roles || []
    total.value = roles.value.length
    await nextTick()
    roles.value.forEach((row) => {
      if (row.flag) roleRef.value?.toggleRowSelection(row, true)
    })
  } finally {
    loading.value = false
  }
}

loadAuthRoles()
</script>
