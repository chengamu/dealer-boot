<template>
  <div class="app-container user-profile-page">
    <el-row :gutter="20" class="user-profile-page__layout">
      <el-col :span="6" :lg="7" :xl="6" :xs="24">
        <el-card class="box-card user-profile-page__card">
          <template #header>
            <div class="clearfix">
              <span>{{ t('user.profileTitle') }}</span>
            </div>
          </template>
          <div>
            <div v-if="state.user.userName !== 'wms2'" class="text-center">
              <user-avatar />
            </div>
            <ul class="list-group list-group-striped user-profile-page__list">
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
                <div v-if="state.user.deptName" class="pull-right">{{ state.user.deptName }} / {{ state.postGroup }}</div>
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
      <el-col v-if="state.user.userName !== 'wms2'" :span="18" :lg="17" :xl="18" :xs="24">
        <el-card class="user-profile-page__main-card">
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

<style scoped lang="scss">
.user-profile-page {
  .user-profile-page__layout {
    row-gap: 10px;
  }

  .user-profile-page__card,
  .user-profile-page__main-card {
    border-color: #eef0f5;
    border-radius: 8px;
    box-shadow: none;
  }

  .user-profile-page__main-card {
    min-height: 360px;
  }

  :deep(.el-card__header) {
    min-height: 42px;
    padding: 10px 12px;
    border-bottom-color: #eef0f5;
    color: #1d2129;
    font-weight: 600;
  }

  :deep(.el-card__body) {
    padding: 12px;
  }

  :deep(.el-tabs__nav-wrap::after) {
    height: 1px;
  }

  :deep(.el-tabs__content) {
    padding: 14px 16px 4px;
  }

  :deep(.el-input__wrapper) {
    min-height: 32px;
  }

  :deep(.el-button) {
    height: 32px;
    padding: 0 12px;
    border-radius: 6px;
  }

  .user-profile-page__list {
    margin: 12px 0 0;
    border: 1px solid #eef0f5;
    border-radius: 8px;
    overflow: hidden;

    .list-group-item {
      display: grid;
      grid-template-columns: 24px minmax(86px, 1fr) minmax(120px, auto);
      column-gap: 8px;
      align-items: center;
      min-height: 44px;
      padding: 8px 12px;
      word-break: break-word;
    }

    .svg-icon {
      color: var(--el-color-primary);
    }

    .pull-right {
      float: none;
      min-width: 0;
      text-align: right;
      font-weight: 600;
      color: var(--el-text-color-primary);
      overflow-wrap: anywhere;
    }
  }
}
</style>
