<template>
  <div class="navbar" :class="{ 'is-compact': compact }">
    <hamburger
      id="hamburger-container"
      :is-active="appStore.sidebar.opened"
      class="hamburger-container"
      @toggleClick="toggleSideBar"
    />
    <div v-if="compact" class="navbar-workspace"><slot /></div>
    <breadcrumb v-else-if="!settingsStore.topNav" id="breadcrumb-container" class="breadcrumb-container" />
    <top-nav v-else id="topmenu-container" class="topmenu-container" />

    <div class="right-menu">
      <header-search v-if="!compact" class="right-menu-item hover-effect" />

      <screenfull class="right-menu-item hover-effect screenfull-entry" />

      <el-dropdown @command="handleLanguage" class="right-menu-item language-menu" trigger="click" popper-class="shell-language-dropdown">
        <button
          type="button"
          class="language-wrapper"
          :aria-label="t('shell.language')"
          :title="t('shell.language')"
        >
          <span class="language-flag">{{ currentLanguageFlag }}</span>
          <span class="language-code">{{ currentLanguageCode }}</span>
          <el-icon class="language-arrow"><caret-bottom /></el-icon>
        </button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item
              v-for="item in languages"
              :key="item.value"
              :command="item.value"
              :class="{ 'is-active': localeStore.language === item.value }"
            >
              <span class="language-option-flag">{{ item.flag }}</span>
              <span>{{ t(item.labelKey) }}</span>
              <span v-if="localeStore.language === item.value" class="language-option-check">✓</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>

      <div class="right-menu-item message-wrapper">
        <message-bell />
      </div>

      <div class="avatar-container">
        <el-dropdown @command="handleCommand" class="right-menu-item user-menu" trigger="click" popper-class="shell-user-dropdown">
          <button
            type="button"
            class="avatar-wrapper"
            :aria-label="t('shell.userMenu')"
            :title="t('shell.userMenu')"
          >
            <img :src="userStore.avatar" class="user-avatar" alt="" />
            <span class="user-summary">
              <strong>{{ userStore.displayName }}</strong>
              <small>{{ userRoleLabel }}</small>
            </span>
            <el-icon class="user-arrow"><caret-bottom /></el-icon>
          </button>
          <template #dropdown>
            <el-dropdown-menu>
              <div class="user-dropdown-head">
                <strong>{{ userStore.displayName }}</strong>
                <small>{{ userStore.user?.email || userStore.name || '-' }}</small>
              </div>
              <router-link to="/user/profile" class="user-dropdown-link">
                <el-dropdown-item>
                  <svg-icon icon-class="user" />
                  <span>{{ t('shell.profile') }}</span>
                </el-dropdown-item>
              </router-link>
              <el-dropdown-item divided command="logout">
                <svg-icon icon-class="exit" />
                <span>{{ t('shell.logout') }}</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ElMessageBox } from 'element-plus'
import { computed } from 'vue'
import Breadcrumb from '@/components/Breadcrumb/index.vue'
import TopNav from '@/components/TopNav/index.vue'
import Hamburger from '@/components/Hamburger/index.vue'
import HeaderSearch from '@/components/HeaderSearch/index.vue'
import Screenfull from '@/components/Screenfull/index.vue'
import MessageBell from '@/shell/MessageBell.vue'
import useAppStore from '@/stores/app'
import useUserStore from '@/stores/user'
import useSettingsStore from '@/stores/settings'
import useLocaleStore from '@/stores/locale'
import usePermissionStore from '@/stores/permission'
import { getMessage } from '@/locales'
import { getContextPath } from '@/utils/config'
import { getToken } from '@/utils/auth'
import { registerDynamicRoutes, resetDynamicRoutes } from '@/router'

const appStore = useAppStore()
const userStore = useUserStore()
const settingsStore = useSettingsStore()
const localeStore = useLocaleStore()
const permissionStore = usePermissionStore()
const t = (key: string) => getMessage(key, localeStore.language)

defineProps<{ compact?: boolean }>()

const languages = [
  { value: 'zh_CN', labelKey: 'language.zhCN', code: 'ZH', flag: '🇨🇳' },
  { value: 'en_US', labelKey: 'language.enUS', code: 'EN', flag: '🇺🇸' }
]

const currentLanguage = computed(() => languages.find(item => item.value === localeStore.language) || languages[0])
const currentLanguageCode = computed(() => currentLanguage.value.code)
const currentLanguageFlag = computed(() => currentLanguage.value.flag)
const userRoleLabel = computed(() => userStore.roles?.[0]?.replace(/^ROLE_/, '') || 'User')

function toggleSideBar() {
  appStore.toggleSideBar()
}

function handleCommand(command: string) {
  switch (command) {
    case 'logout':
      logout()
      break
    default:
      break
  }
}

function logout() {
  ElMessageBox.confirm(t('shell.logoutConfirm'), t('common.prompt'), {
    confirmButtonText: t('common.confirm'),
    cancelButtonText: t('common.cancel'),
    type: 'warning'
  }).then(() => {
    userStore.logOut().then(() => {
      const contextPath = getContextPath()
      const basePath = contextPath.endsWith('/') ? contextPath : `${contextPath}/`
      location.href = `${basePath}index`
    })
  }).catch(() => {})
}

async function handleLanguage(language: 'zh_CN' | 'en_US') {
  if (localeStore.language === language) return
  localeStore.setLanguage(language)
  if (!getToken()) return
  permissionStore.reset()
  resetDynamicRoutes()
  const accessRoutes = await permissionStore.generateRoutes()
  registerDynamicRoutes(accessRoutes)
}
</script>

<style lang="scss" scoped src="./Navbar.scss"></style>
