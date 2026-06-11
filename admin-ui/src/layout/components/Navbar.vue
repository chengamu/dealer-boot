<template>
  <div class="navbar">
    <hamburger
      id="hamburger-container"
      :is-active="appStore.sidebar.opened"
      class="hamburger-container"
      @toggleClick="toggleSideBar"
    />
    <breadcrumb id="breadcrumb-container" class="breadcrumb-container" v-if="!settingsStore.topNav" />
    <top-nav id="topmenu-container" class="topmenu-container" v-if="settingsStore.topNav" />

    <div class="right-menu">
      <header-search class="right-menu-item hover-effect" />

      <screenfull class="right-menu-item hover-effect" />

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

      <message-bell class="right-menu-item message-wrapper" />

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

<style lang="scss" scoped>
.navbar {
  height: 50px;
  overflow: hidden;
  position: relative;
  background: #F8F9FD;

  .hamburger-container {
    line-height: 46px;
    height: 100%;
    float: left;
    cursor: pointer;
    transition: background 0.3s;
    -webkit-tap-highlight-color: transparent;

    &:hover {
      background: rgba(0, 0, 0, 0.025);
    }
  }

  .breadcrumb-container {
    float: left;
  }

  .topmenu-container {
    position: absolute;
    left: 50px;
  }

  .right-menu {
    float: right;
    height: 100%;
    line-height: 50px;
    display: flex;

    &:focus {
      outline: none;
    }

    .right-menu-item {
      display: inline-block;
      padding: 0 8px;
      height: 100%;
      font-size: 18px;
      color: #5a5e66;
      vertical-align: text-bottom;

      &.hover-effect {
        cursor: pointer;
        transition: background 0.3s;

        &:hover {
          background: rgba(0, 0, 0, 0.025);
        }
      }
    }

    .language-menu {
      display: inline-flex;
      align-items: center;
      padding: 0 4px;
      font-size: 14px;

      .language-wrapper {
        display: flex;
        align-items: center;
        gap: 7px;
        height: 34px;
        padding: 0 12px;
        border: 0;
        border-radius: 12px;
        background: #f5f7fb;
        color: #354052;
        cursor: pointer;
        transition: background 0.18s ease, box-shadow 0.18s ease;

        &:hover {
          background: #eef3fa;
          box-shadow: 0 4px 12px rgba(15, 23, 42, 0.06);
        }
      }

      .language-flag {
        font-size: 15px;
        line-height: 1;
      }

      .language-code {
        font-weight: 700;
        letter-spacing: 0;
      }

      .language-arrow {
        color: #94a3b8;
        font-size: 12px;
      }
    }

    .message-wrapper {
      display: inline-flex;
      align-items: center;
      padding: 0 6px;
    }

    .navbar-icon-button {
      border: 0;
      background: transparent;
      line-height: 50px;
    }

    .avatar-container {
      margin-right: 18px;

      .avatar-wrapper {
        display: flex;
        align-items: center;
        gap: 10px;
        height: 44px;
        margin-top: 3px;
        padding: 4px 9px 4px 4px;
        border: 0;
        border-radius: 16px;
        background: transparent;
        position: relative;
        cursor: pointer;
        transition: background 0.18s ease;

        &:hover {
          background: #f5f7fb;
        }

        .user-avatar {
          width: 36px;
          height: 36px;
          border-radius: 12px;
          box-shadow: 0 0 0 1px #dbe5f2;
        }

        .user-summary {
          display: grid;
          gap: 1px;
          min-width: 78px;
          color: #172033;
          text-align: left;
          line-height: 1.15;

          strong {
            max-width: 96px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            font-size: 14px;
            font-weight: 700;
          }

          small {
            color: #667085;
            font-size: 12px;
          }
        }

        .user-arrow {
          color: #94a3b8;
          font-size: 14px;
        }
      }
    }
  }
}

:global(.shell-language-dropdown.el-popper),
:global(.shell-user-dropdown.el-popper) {
  border: 1px solid #dbe5f2;
  border-radius: 14px !important;
  box-shadow: 0 18px 44px rgba(15, 23, 42, 0.14);
  overflow: hidden;
}

:global(.shell-language-dropdown .el-dropdown-menu) {
  min-width: 176px;
  padding: 8px;
}

:global(.shell-language-dropdown .el-dropdown-menu__item) {
  display: grid;
  grid-template-columns: 24px minmax(0, 1fr) 20px;
  gap: 10px;
  height: 42px;
  border-radius: 10px;
  color: #354052;
  font-size: 15px;
}

:global(.shell-language-dropdown .el-dropdown-menu__item.is-active) {
  background: #ecfdf7;
  color: #0f9f8f;
}

:global(.language-option-check) {
  color: #0f9f8f;
  font-size: 18px;
  font-weight: 600;
}

:global(.shell-user-dropdown .el-dropdown-menu) {
  min-width: 218px;
  padding: 0;
}

:global(.shell-user-dropdown .user-dropdown-head) {
  display: grid;
  gap: 4px;
  padding: 16px 18px 14px;
  border-bottom: 1px solid #edf2f8;
}

:global(.shell-user-dropdown .user-dropdown-head strong) {
  color: #172033;
  font-size: 15px;
}

:global(.shell-user-dropdown .user-dropdown-head small) {
  color: #667085;
  font-size: 13px;
}

:global(.shell-user-dropdown .el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 44px;
  padding: 0 18px;
  color: #344054;
  font-size: 14px;
}

:global(.shell-user-dropdown .el-dropdown-menu__item--divided) {
  margin: 6px 0 0;
  border-top: 1px solid #edf2f8;
  color: #ef4444;
}
</style>
