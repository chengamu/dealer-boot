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

      <el-dropdown @command="handleLanguage" class="right-menu-item hover-effect language-menu" trigger="click">
        <button
          type="button"
          class="language-wrapper"
          :aria-label="t('shell.language')"
          :title="t('shell.language')"
        >
          <svg-icon icon-class="language" />
          <span>{{ currentLanguageLabel }}</span>
        </button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item
              v-for="item in languages"
              :key="item.value"
              :command="item.value"
              :disabled="localeStore.language === item.value"
            >
              {{ t(item.labelKey) }}
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>

      <message-bell class="right-menu-item message-wrapper" />

      <button
        type="button"
        class="right-menu-item hover-effect navbar-icon-button"
        :aria-label="t('shell.layoutSettings')"
        :title="t('shell.layoutSettings')"
        @click="setLayout"
      >
        <svg-icon icon-class="theme" />
      </button>

      <div class="avatar-container">
        <el-dropdown @command="handleCommand" class="right-menu-item hover-effect" trigger="click">
          <button
            type="button"
            class="avatar-wrapper"
            :aria-label="t('shell.userMenu')"
            :title="t('shell.userMenu')"
          >
            <img :src="userStore.avatar" class="user-avatar" alt="" />
            <el-icon><caret-bottom /></el-icon>
          </button>
          <template #dropdown>
            <el-dropdown-menu>
              <router-link to="/user/profile">
                <el-dropdown-item>{{ t('shell.profile') }}</el-dropdown-item>
              </router-link>
              <el-dropdown-item divided command="logout">
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
import { getMessage } from '@/locales'
import { getContextPath } from '@/utils/config'

const emits = defineEmits<{
  (e: 'setLayout'): void
}>()

const appStore = useAppStore()
const userStore = useUserStore()
const settingsStore = useSettingsStore()
const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)

const languages = [
  { value: 'zh_CN', labelKey: 'language.zhCN' },
  { value: 'en_US', labelKey: 'language.enUS' }
]

const currentLanguageLabel = computed(() => t(languages.find(item => item.value === localeStore.language)?.labelKey || 'language.zhCN'))

function toggleSideBar() {
  appStore.toggleSideBar()
}

function handleCommand(command: string) {
  switch (command) {
    case 'setLayout':
      setLayout()
      break
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

function handleLanguage(language: 'zh_CN' | 'en_US') {
  if (localeStore.language === language) return
  localeStore.setLanguage(language)
}

function setLayout() {
  emits('setLayout')
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
      font-size: 14px;

      .language-wrapper {
        display: flex;
        align-items: center;
        gap: 6px;
        height: 50px;
        padding: 0;
        border: 0;
        background: transparent;
        color: inherit;
        cursor: pointer;
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
      margin-right: 40px;

      .avatar-wrapper {
        margin-top: 5px;
        padding: 0;
        border: 0;
        background: transparent;
        position: relative;

        .user-avatar {
          cursor: pointer;
          width: 40px;
          height: 40px;
          border-radius: 10px;
        }

        i {
          cursor: pointer;
          position: absolute;
          right: -20px;
          top: 25px;
          font-size: 12px;
        }
      }
    }
  }
}
</style>
