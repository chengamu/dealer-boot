<template>
  <div class="auth-page">
    <CurtainIntro />
    <LoginHeroPanel />

    <section class="login-panel">
      <div class="locale-wrap">
        <AuthLocaleSelect v-model="localeValue" @change="changeLocale" />
      </div>

      <LoginFormCard
        :form="form"
        :captcha="captcha"
        :rules="rules"
        :captcha-required="captchaRequired"
        :loading="loading"
        :google-refresh-key="localeValue"
        @submit="loginWithCurrentForm"
        @load-captcha="loadCaptcha"
      />
    </section>

    <AuthFooter @open-legal="openLegal" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import CurtainIntro from '@/components/AuthVisual/CurtainIntro.vue'
import AuthLocaleSelect from '@/components/AuthVisual/AuthLocaleSelect.vue'
import { useLocaleStore } from '@/stores/locale'
import type { AppLocale } from '@/i18n'
import AuthFooter from './components/AuthFooter.vue'
import LoginFormCard from './components/LoginFormCard.vue'
import LoginHeroPanel from './components/LoginHeroPanel.vue'
import { useLoginRouteOverlay } from './composables/useLoginRouteOverlay'
import { usePasswordLogin } from './composables/usePasswordLogin'
import type { LoginLegalType } from './types'

const router = useRouter()
const localeStore = useLocaleStore()
const localeValue = ref<AppLocale>(localeStore.locale)

useLoginRouteOverlay()

const { form, captcha, rules, captchaRequired, loading, loadCaptcha, loginWithCurrentForm } = usePasswordLogin()

function changeLocale(locale: AppLocale) {
  localeStore.setLocale(locale)
}

function openLegal(type: LoginLegalType) {
  router.push(`/legal/${type}`)
}
</script>

<style scoped lang="scss">
.auth-page {
  position: relative;
  box-sizing: border-box;
  display: grid;
  grid-template-columns: minmax(0, 1.08fr) minmax(540px, 0.92fr);
  gap: 0;
  width: 100%;
  min-height: 100vh;
  padding: 24px 32px 42px;
  overflow: auto;
  background:
    radial-gradient(circle at 18% 0, rgba(255, 255, 255, 0.98), transparent 26%),
    linear-gradient(180deg, #edf6ff 0%, #f6fbff 100%);
}

.login-panel {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 66px);
  padding: 72px 46px 64px;
  overflow: hidden;
  border-radius: 0 16px 16px 0;
  background: rgba(255, 255, 255, 0.84);
}

.locale-wrap {
  position: relative;
  z-index: 5;
  display: flex;
  justify-content: flex-end;
  width: min(100%, 552px);
  margin-bottom: 12px;
}

@media (max-width: 1280px) {
  .auth-page {
    grid-template-columns: minmax(0, 1fr) minmax(476px, 0.84fr);
    padding: 24px 32px 40px;
  }

  .login-panel {
    padding-inline: 38px;
  }
}

@media (max-width: 1100px) {
  .auth-page {
    grid-template-columns: 1fr;
    padding: 0;
  }

  .login-panel {
    min-height: 100vh;
    border-radius: 0;
  }
}

@media (max-width: 560px) {
  .auth-page {
    min-height: 100vh;
    overflow: auto;
  }

  .login-panel {
    padding: 72px 38px 54px;
  }

  .locale-wrap {
    width: 100%;
    margin-bottom: 10px;
  }
}

@media (max-width: 460px) {
  .login-panel {
    padding: 62px 18px 44px;
  }

  .locale-wrap {
    width: 100%;
  }
}

@media (max-height: 960px) and (min-width: 1101px) {
  .auth-page {
    padding: 16px 32px 32px;
  }

  .login-panel {
    min-height: calc(100vh - 48px);
    padding: 48px 46px 42px;
  }
}

@media (max-height: 820px) and (min-width: 1101px) {
  .auth-page {
    padding-bottom: 16px;
    overflow: auto;
  }

  .login-panel {
    min-height: calc(100vh - 32px);
    padding-top: 52px;
    padding-bottom: 34px;
  }
}
</style>
