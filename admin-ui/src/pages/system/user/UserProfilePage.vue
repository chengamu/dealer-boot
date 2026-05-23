<template>
  <div class="app-container user-profile-page">
    <el-row :gutter="20">
      <el-col :span="6" :xs="24">
        <el-card class="box-card">
          <template #header>
            <div class="clearfix">
              <span>{{ t('user.profileTitle') }}</span>
            </div>
          </template>
          <div>
            <div v-if="state.user.userName !== 'wms2'" class="text-center">
              <user-avatar />
            </div>
            <ul class="list-group list-group-striped">
              <li class="list-group-item">
                <svg-icon icon-class="user" />{{ t('user.userName') }}
                <div class="pull-right">{{ state.user.userName }}</div>
              </li>
              <li class="list-group-item">
                <svg-icon icon-class="phone" />{{ t('user.phonenumber') }}
                <div class="pull-right">{{ state.user.phonenumber }}</div>
              </li>
              <li class="list-group-item">
                <svg-icon icon-class="email" />{{ t('user.email') }}
                <div class="pull-right">{{ state.user.email }}</div>
              </li>
              <li class="list-group-item">
                <svg-icon icon-class="tree" />{{ t('user.deptName') }}
                <div v-if="state.user.dept" class="pull-right">{{ state.user.dept.deptName }} / {{ state.postGroup }}</div>
              </li>
              <li class="list-group-item">
                <svg-icon icon-class="peoples" />{{ t('user.role') }}
                <div class="pull-right">{{ state.roleGroup }}</div>
              </li>
              <li class="list-group-item">
                <svg-icon icon-class="date" />{{ t('common.createTime') }}
                <div class="pull-right">{{ formatUtc(state.user.createTime) }}</div>
              </li>
            </ul>
          </div>
        </el-card>
      </el-col>
      <el-col v-if="state.user.userName !== 'wms2'" :span="18" :xs="24">
        <el-card>
          <template #header>
            <div class="clearfix">
              <span>{{ t('user.basicProfile') }}</span>
            </div>
          </template>
          <el-tabs v-model="activeTab">
            <el-tab-pane :label="t('user.basicProfile')" name="userinfo">
              <user-info-form :user="state.user" />
            </el-tab-pane>
            <el-tab-pane :label="t('user.changePassword')" name="resetPwd">
              <reset-password-form />
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts" name="UserProfilePage">
import { reactive, ref } from 'vue'
import UserAvatar from './UserAvatar.vue'
import UserInfoForm from './UserInfoForm.vue'
import ResetPasswordForm from './ResetPasswordForm.vue'
import { getUserProfile, type SysUser } from '@/api/system/user'
import { formatUtc } from '@/utils/datetime'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { runUiAction } from '@/utils/action'

const localeStore = useLocaleStore()
const t = (key: string, params?: Record<string, string | number>) => {
  const message = getMessage(key, localeStore.language)
  if (!params) return message
  return Object.entries(params).reduce((text, [name, value]) => text.replaceAll(`{${name}}`, String(value)), message)
}

const activeTab = ref('userinfo')
const state = reactive({
  user: {} as SysUser,
  roleGroup: '',
  postGroup: ''
})

async function getUser() {
  await runUiAction(async () => {
    const response = await getUserProfile()
    state.user = response.data.user || {}
    state.roleGroup = response.data.roleGroup || ''
    state.postGroup = response.data.postGroup || ''
  })
}

getUser()
</script>
