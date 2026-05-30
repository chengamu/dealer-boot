<template>
  <div class="auth-page">
    <CurtainIntro />

    <section class="hero-panel">
      <div class="brand">
        <div class="sky-logo">skyspf</div>
        <p>{{ t('login.portalName') }}</p>
      </div>

      <div class="portal-pill">
        <el-icon><Connection /></el-icon>
        <span>{{ t('login.dealerPortal') }}</span>
      </div>

      <div class="hero-copy">
        <h1>{{ t('login.heroLine1') }}<br />{{ t('login.heroLine2') }}<br />{{ t('login.heroLine3') }}</h1>
        <p>{{ t('login.heroDescription') }}</p>
      </div>

      <div class="room-scene" aria-hidden="true">
        <div class="curtain" />
        <div class="window-frame" />
        <div class="blind" />
      </div>

      <article class="float-card order-card">
        <span class="check-dot">✓</span>
        <strong>{{ t('login.newOrder') }}</strong>
        <small>{{ t('login.orderProduct') }}</small>
      </article>

      <article class="float-card chart-card">
        <strong>{{ t('login.orderOverview') }}</strong>
        <div class="mini-chart">
          <span v-for="bar in 7" :key="bar" :style="{ height: `${18 + (bar % 4) * 13}px` }" />
        </div>
        <small>{{ t('login.thisMonth') }} <b>↑ 18.6%</b></small>
      </article>

      <article class="float-card material-card">
        <strong>{{ t('login.topFabricCollections') }}</strong>
        <div class="swatches">
          <i v-for="swatch in swatches" :key="swatch" :style="{ background: swatch }" />
        </div>
      </article>

      <article class="trust-card">
        <span><el-icon><Connection /></el-icon></span>
        <div>
          <strong>{{ t('login.trustTitle') }}</strong>
          <small>{{ t('login.trustText') }}</small>
        </div>
      </article>

    </section>

    <section class="login-panel">
      <div class="locale-wrap">
        <AuthLocaleSelect v-model="localeValue" @change="changeLocale" />
      </div>

      <el-card class="login-card" shadow="never">
        <h2>{{ t('login.title') }}</h2>
        <p>{{ t('login.description') }}</p>
        <el-form ref="formRef" :model="form" :rules="rules" :validate-on-rule-change="false" label-position="top" @keyup.enter="submit">
          <el-form-item :label="t('login.email')" prop="username">
            <el-input v-model="form.username" :placeholder="t('login.emailPlaceholder')" size="large">
              <template #prefix><el-icon><Message /></el-icon></template>
            </el-input>
          </el-form-item>

          <el-form-item :label="t('login.password')" prop="password">
            <el-input v-model="form.password" :placeholder="t('login.passwordPlaceholder')" show-password size="large" type="password">
              <template #prefix><el-icon><Lock /></el-icon></template>
            </el-input>
          </el-form-item>

          <el-form-item v-if="captchaRequired" :label="t('login.code')" prop="code">
            <div class="captcha-row inline-captcha-row">
              <el-input v-model="form.code" :placeholder="t('login.codePlaceholder')" size="large">
                <template #prefix><el-icon><CircleCheckFilled /></el-icon></template>
              </el-input>
              <button class="captcha-image" type="button" :aria-label="t('common.refresh')" :title="t('common.refresh')" @click="loadCaptcha">
                <img v-if="captcha.img" :src="captcha.img" :alt="t('login.code')" />
                <span v-else>{{ t('common.refresh') }}</span>
              </button>
              <button class="captcha-refresh" type="button" :aria-label="t('common.refresh')" :title="t('common.refresh')" @click="loadCaptcha">↻</button>
            </div>
          </el-form-item>

          <div class="login-options">
            <el-checkbox v-model="remember">{{ t('login.remember') }}</el-checkbox>
            <RouterLink to="/merchant/apply">{{ t('login.apply') }}</RouterLink>
          </div>

          <el-button :loading="loading" class="submit" type="primary" size="large" @click="submit">
            {{ t('login.submit') }}
          </el-button>
        </el-form>

        <template v-if="googleClientId">
          <div class="login-divider"><span>{{ t('login.or') }}</span></div>
          <div ref="googleButtonRef" class="google-button-host" :class="{ 'is-loading': googleLoading }" />
        </template>
      </el-card>

    </section>

    <footer class="auth-footer">
      <span>{{ t('apply.footerCopyright') }}</span>
      <i />
      <button type="button" @click="openLegal('privacy')">{{ t('apply.privacy') }}</button>
      <i />
      <button type="button" @click="openLegal('terms')">{{ t('apply.terms') }}</button>
      <i />
      <button type="button" @click="openLegal('cookie')">{{ t('apply.cookiePolicy') }}</button>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Connection, Lock, Message, CircleCheckFilled } from '@element-plus/icons-vue'
import CurtainIntro from '@/components/AuthVisual/CurtainIntro.vue'
import AuthLocaleSelect from '@/components/AuthVisual/AuthLocaleSelect.vue'
import { useUserStore } from '@/stores/user'
import { useLocaleStore } from '@/stores/locale'
import type { AppLocale } from '@/i18n'
import { getCodeImg } from '@/api/auth'
import { getGoogleClientId } from '@/utils/config'

type GoogleCredentialResponse = {
  credential?: string
}

type GoogleAccountsId = {
  initialize: (config: { client_id: string; callback: (response: GoogleCredentialResponse) => void }) => void
  renderButton: (parent: HTMLElement, options: Record<string, string | number | boolean>) => void
}

declare global {
  interface Window {
    google?: {
      accounts?: {
        id?: GoogleAccountsId
      }
    }
  }
}

const { t } = useI18n()
const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const localeStore = useLocaleStore()
const formRef = ref<FormInstance>()
const localeValue = ref<AppLocale>(localeStore.locale)
const remember = ref(false)
const loading = ref(false)
const googleLoading = ref(false)
const captchaRequired = ref(true)
const googleButtonRef = ref<HTMLElement>()
const googleClientId = getGoogleClientId()
const form = reactive({ username: '', password: '', code: '', uuid: '' })
const captcha = reactive({ enabled: false, img: '' })
const swatches = ['#d9c7aa', '#e8e1d2', '#c8c5ba', '#8995a1']
const overlaySelector = '.el-overlay, .v-modal, .el-loading-mask'
let overlayCleanupTimer: number | undefined
let googleScriptPromise: Promise<void> | undefined

const rules = computed<FormRules>(() => ({
  username: [{ required: true, message: t('login.required'), trigger: 'blur' }],
  password: [{ required: true, message: t('login.required'), trigger: 'blur' }],
  code: captchaRequired.value ? [{ required: true, message: t('login.codePlaceholder'), trigger: 'blur' }] : []
}))

function changeLocale(locale: AppLocale) {
  localeStore.setLocale(locale)
}

function openLegal(type: 'privacy' | 'terms' | 'cookie') {
  router.push(`/legal/${type}`)
}

async function loadCaptcha() {
  try {
    const response = await getCodeImg()
    const payload = response?.data ?? response
    captcha.enabled = Boolean(payload?.captchaEnabled ?? payload?.captchaOnOff ?? payload?.img)
    captcha.img = payload?.img ? `data:image/gif;base64,${payload.img}` : ''
    form.uuid = payload?.uuid || ''
    if (!captcha.enabled) form.code = ''
  } catch {
    captcha.enabled = true
    captcha.img = ''
    form.uuid = ''
  }
}

function isCaptchaError(error: unknown) {
  const message = String((error as Error)?.message || error || '').toLowerCase()
  return message.includes('captcha') || message.includes('验证码') || message.includes('verification')
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  await loginWithCurrentForm()
}

async function requireCaptcha() {
  form.code = ''
  form.uuid = ''
  await loadCaptcha()
  captchaRequired.value = captcha.enabled
}

async function loginWithCurrentForm() {
  loading.value = true
  try {
    await userStore.login(form.username, form.password, form.code, form.uuid)
    await userStore.loadUser()
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/index'
    router.push(redirect)
  } catch (error) {
    if (isCaptchaError(error)) {
      await requireCaptcha()
      ElMessage.error((error as Error).message || t('api.error'))
      return
    }
    ElMessage.error((error as Error).message || t('api.error'))
    if (captchaRequired.value) await loadCaptcha()
  } finally {
    loading.value = false
  }
}

function loadGoogleIdentityScript() {
  if (window.google?.accounts?.id) return Promise.resolve()
  if (!googleScriptPromise) {
    googleScriptPromise = new Promise((resolve, reject) => {
      const existing = document.getElementById('google-identity-services') as HTMLScriptElement | null
      if (existing) {
        existing.addEventListener('load', () => resolve(), { once: true })
        existing.addEventListener('error', () => reject(new Error('Google Identity Services load failed')), { once: true })
        return
      }
      const script = document.createElement('script')
      script.id = 'google-identity-services'
      script.src = 'https://accounts.google.com/gsi/client'
      script.async = true
      script.defer = true
      script.onload = () => resolve()
      script.onerror = () => reject(new Error('Google Identity Services load failed'))
      document.head.appendChild(script)
    })
  }
  return googleScriptPromise
}

async function renderGoogleButton() {
  if (!googleClientId || !googleButtonRef.value) return
  await loadGoogleIdentityScript()
  await nextTick()
  const container = googleButtonRef.value
  const googleIdentity = window.google?.accounts?.id
  if (!googleIdentity) return
  container.innerHTML = ''
  googleIdentity.initialize({
    client_id: googleClientId,
    callback: (response) => {
      void handleGoogleCredential(response)
    }
  })
  googleIdentity.renderButton(container, {
    type: 'standard',
    theme: 'outline',
    size: 'large',
    text: 'continue_with',
    shape: 'rectangular',
    logo_alignment: 'left',
    width: Math.min(Math.max(container.clientWidth || 360, 240), 400),
    locale: localeValue.value.replace('_', '-')
  })
}

async function handleGoogleCredential(response: GoogleCredentialResponse) {
  if (!response.credential) return
  googleLoading.value = true
  try {
    const result = await userStore.loginWithGoogle(response.credential)
    if (result.login) {
      await userStore.loadUser()
      const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/index'
      router.push(redirect)
      return
    }
    await handleGoogleApplyStatus(result)
  } catch (error) {
    ElMessage.error((error as Error).message || t('login.googleSignInFailed'))
  } finally {
    googleLoading.value = false
  }
}

async function handleGoogleApplyStatus(result: { status?: string; email?: string; rejectReason?: string }) {
  const email = result.email || ''
  if (result.status === 'APPLY_REQUIRED') {
    ElMessage.info(t('login.googleApplyRequired'))
    router.push({ path: '/merchant/apply', query: { email, source: 'google' } })
    return
  }
  if (result.status === 'APPLY_PENDING') {
    ElMessage.warning(t('login.googleApplyPending'))
    return
  }
  if (result.status === 'APPLY_REJECTED') {
    const message = result.rejectReason ? `${t('login.googleApplyRejected')} ${result.rejectReason}` : t('login.googleApplyRejected')
    await ElMessageBox.confirm(message, t('login.googleApplyRejectedTitle'), {
      confirmButtonText: t('login.googleApplyRejectedAction'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    }).then(() => {
      router.push({ path: '/merchant/apply', query: { email, source: 'google' } })
    }).catch(() => undefined)
    return
  }
  if (result.status === 'APPLY_APPROVED_BUT_USER_MISSING') {
    ElMessage.warning(t('login.googleApplyApprovedMissing'))
    return
  }
  ElMessage.warning(t('login.googleSignInFailed'))
}

watch(captchaRequired, async (required) => {
  if (required && !captcha.img) await loadCaptcha()
})

watch(localeValue, () => {
  void renderGoogleButton()
})

function clearStaleOverlays() {
  document.body.classList.remove('el-popup-parent--hidden')
  const overlays = Array.from(document.querySelectorAll(overlaySelector))
  overlays.forEach((element) => element.remove())
}

onMounted(() => {
  document.body.classList.add('login-route-active')
  clearStaleOverlays()
  overlayCleanupTimer = window.setInterval(clearStaleOverlays, 100)
  window.setTimeout(() => {
    if (overlayCleanupTimer) window.clearInterval(overlayCleanupTimer)
    overlayCleanupTimer = undefined
  }, 3000)
  requireCaptcha()
  void renderGoogleButton()
})

onUnmounted(() => {
  document.body.classList.remove('login-route-active')
  if (overlayCleanupTimer) window.clearInterval(overlayCleanupTimer)
})
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

.hero-panel,
.login-panel {
  position: relative;
  min-height: calc(100vh - 66px);
  overflow: hidden;
  background: rgba(255, 255, 255, 0.78);
}

.hero-panel {
  padding: 50px 46px 118px;
  border-radius: 16px 0 0 16px;
  background:
    linear-gradient(90deg, rgba(255, 255, 255, 0.98) 0 29%, rgba(255, 255, 255, 0.74) 52%, rgba(255, 255, 255, 0.26) 100%),
    linear-gradient(145deg, #fbfdff 0%, #eff7ff 46%, #d7eafa 100%);
}

.brand {
  position: relative;
  z-index: 2;
}

.sky-logo {
  color: #06184a;
  font-size: 60px;
  line-height: 1;
  letter-spacing: 0;
  font-weight: 400;
}

.brand p,
.hero-copy p {
  color: #425985;
}

.portal-pill {
  position: relative;
  z-index: 2;
  display: inline-flex;
  gap: 10px;
  align-items: center;
  margin-top: 34px;
  padding: 10px 18px;
  color: #075cff;
  font-weight: 700;
  border-radius: 999px;
  background: rgba(226, 239, 255, 0.92);
}

.hero-copy {
  position: relative;
  z-index: 2;
  width: min(650px, 88%);
  margin-top: 38px;
}

.hero-copy h1 {
  margin: 0 0 20px;
  color: #06184a;
  font-size: clamp(40px, 3vw, 46px);
  font-weight: 800;
  line-height: 1.24;
  letter-spacing: 0;
  white-space: nowrap;
}

.hero-copy p {
  max-width: 520px;
  font-size: 18px;
  line-height: 1.68;
}

.room-scene {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.room-scene::before {
  position: absolute;
  right: -10%;
  bottom: -5%;
  width: 74%;
  height: 82%;
  content: "";
  background:
    radial-gradient(circle at 56% 76%, rgba(112, 146, 105, 0.24), transparent 10%),
    linear-gradient(180deg, rgba(179, 204, 225, 0.32), rgba(255, 255, 255, 0.96));
  clip-path: polygon(16% 10%, 100% 0, 100% 100%, 0 100%);
}

.curtain {
  position: absolute;
  left: 22%;
  bottom: -11%;
  width: 30%;
  height: 78%;
  background:
    repeating-linear-gradient(92deg, rgba(255, 255, 255, 0.86) 0 14px, rgba(217, 233, 247, 0.36) 15px 28px),
    linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(237, 246, 255, 0.46));
  filter: blur(0.2px);
  transform: skewX(-10deg);
}

.window-frame {
  position: absolute;
  right: -3%;
  bottom: 19%;
  width: 44%;
  height: 50%;
  border: 16px solid rgba(255, 255, 255, 0.82);
  background: linear-gradient(180deg, rgba(218, 236, 252, 0.58), rgba(255, 255, 255, 0.7));
  box-shadow: 0 30px 70px rgba(26, 48, 84, 0.12);
}

.blind {
  position: absolute;
  right: 3%;
  bottom: 46%;
  width: 40%;
  height: 17%;
  background: repeating-linear-gradient(0deg, #d6dce5 0 5px, #f4f7fb 6px 11px);
  box-shadow: 0 12px 28px rgba(40, 55, 80, 0.12);
}

.float-card,
.trust-card {
  position: absolute;
  z-index: 3;
  pointer-events: none;
  border: 1px solid rgba(190, 207, 231, 0.86);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.88);
  box-shadow: 0 18px 46px rgba(18, 45, 90, 0.13);
  backdrop-filter: blur(10px);
}

.float-card {
  display: grid;
  gap: 6px;
  padding: 17px 20px;
}

.float-card small {
  display: block;
  color: #5d6f96;
}

.order-card {
  top: 354px;
  right: 12px;
  min-width: 138px;
}

.check-dot {
  position: absolute;
  top: -10px;
  right: -10px;
  display: grid;
  width: 24px;
  height: 24px;
  place-items: center;
  color: #fff;
  border-radius: 50%;
  background: #075cff;
}

.chart-card {
  right: 42px;
  bottom: 304px;
  width: 210px;
}

.mini-chart {
  display: flex;
  gap: 8px;
  align-items: end;
  height: 78px;
}

.mini-chart span {
  width: 14px;
  border-radius: 4px 4px 0 0;
  background: linear-gradient(180deg, #075cff, #b9dcff);
}

.chart-card b {
  float: right;
  color: #16a34a;
}

.material-card {
  right: 56px;
  bottom: 166px;
  width: 252px;
}

.swatches {
  display: flex;
  gap: 8px;
}

.swatches i {
  width: 44px;
  height: 36px;
  border: 1px solid #d6dfec;
  border-radius: 5px;
}

.trust-card {
  left: 46px;
  bottom: 72px;
  display: grid;
  grid-template-columns: 48px 1fr;
  gap: 16px;
  align-items: center;
  width: 380px;
  padding: 18px 22px;
}

.trust-card span {
  display: grid;
  width: 46px;
  height: 46px;
  place-items: center;
  color: #fff;
  border-radius: 50%;
  background: #075cff;
}

.trust-card small {
  display: block;
  margin-top: 5px;
  color: #627399;
  line-height: 1.45;
}

.login-panel {
  display: grid;
  place-items: center;
  padding: 92px 46px 48px;
  border-radius: 0 16px 16px 0;
  background: rgba(255, 255, 255, 0.84);
}

.locale-wrap {
  position: absolute;
  top: 35px;
  right: 46px;
  width: 150px;
}

.login-card {
  width: min(100%, 552px);
  padding: 48px 45px 34px;
  border: 1px solid rgba(218, 228, 242, 0.92);
  border-radius: 8px;
  box-shadow: 0 28px 72px rgba(18, 45, 90, 0.11);
  background: rgba(255, 255, 255, 0.92);
}

.login-card h2 {
  margin: 0 0 12px;
  color: #06184a;
  font-size: 36px;
  font-weight: 800;
  letter-spacing: 0;
}

.login-card > p {
  margin: 0 0 32px;
  color: #556894;
}

.login-card :deep(.el-form-item) {
  margin-bottom: 19px;
}

.login-card :deep(.el-form-item__label) {
  margin-bottom: 10px;
  color: #06184a;
  font-weight: 700;
}

.login-card :deep(.el-input__wrapper) {
  height: 52px;
  padding: 0 16px;
  border-radius: 8px;
  box-shadow: 0 0 0 1px #d5e0ef inset;
}

.captcha-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 122px;
  gap: 12px;
  width: 100%;
}

.inline-captcha-row {
  grid-template-columns: minmax(0, 1fr) 116px 28px;
  align-items: center;
  gap: 8px;
}

.captcha-image {
  display: grid;
  height: 52px;
  place-items: center;
  overflow: hidden;
  cursor: pointer;
  border: 1px solid #d7e2f0;
  border-radius: 7px;
  background: #f8fbff;
}

.captcha-image img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.captcha-refresh {
  display: grid;
  width: 28px;
  height: 52px;
  place-items: center;
  color: #496392;
  font-size: 22px;
  cursor: pointer;
  border: 0;
  background: transparent;
}

.login-options {
  display: flex;
  gap: 16px;
  align-items: center;
  justify-content: space-between;
  margin: -1px 0 22px;
}

.login-options a {
  max-width: 180px;
  color: #075cff;
  font-weight: 700;
  line-height: 1.2;
  text-align: right;
  white-space: nowrap;
}

.submit {
  width: 100%;
  height: 56px;
  font-weight: 700;
  font-size: 16px;
  border-radius: 8px;
  background: #075cff;
}

.login-divider {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  gap: 18px;
  align-items: center;
  margin: 24px 0;
  color: #6d7da1;
}

.login-divider::before,
.login-divider::after {
  height: 1px;
  content: "";
  background: #d7e0ef;
}

.google-button-host {
  position: relative;
  display: grid;
  width: 100%;
  height: 52px;
  align-items: center;
  justify-content: center;
}

.google-button-host.is-loading {
  opacity: 0.72;
  pointer-events: none;
}

.auth-footer {
  position: absolute;
  right: 0;
  bottom: 18px;
  left: 0;
  z-index: 4;
  display: flex;
  gap: 18px;
  align-items: center;
  justify-content: center;
  color: #7b8bad;
  font-size: 12px;
}

.auth-footer button {
  padding: 0;
  border: 0;
  color: #5d6f96;
  font: inherit;
  background: transparent;
  cursor: pointer;
}

.auth-footer i {
  width: 1px;
  height: 14px;
  background: #aebbd0;
}

@media (max-width: 1280px) {
  .auth-page {
    grid-template-columns: minmax(0, 1fr) minmax(476px, 0.84fr);
    padding: 24px 32px 40px;
  }

  .hero-panel {
    padding-right: 36px;
  }

  .hero-copy {
    width: min(570px, 92%);
  }

  .hero-copy h1 {
    font-size: 39px;
    line-height: 1.24;
  }

  .hero-copy p {
    max-width: 500px;
  }

  .order-card {
    top: 360px;
    right: 24px;
  }

  .chart-card {
    right: 28px;
    bottom: 286px;
  }

  .material-card {
    right: 40px;
    bottom: 154px;
  }

  .trust-card {
    width: 378px;
  }

  .login-panel {
    padding-inline: 38px;
  }

  .login-card {
    padding: 44px 40px 32px;
  }

  .login-card :deep(.el-input__wrapper),
  .captcha-image,
  .captcha-refresh {
    height: 48px;
  }

  .submit {
    height: 50px;
  }

  .login-divider {
    margin: 22px 0;
  }

  .google-button-host {
    height: 48px;
  }

}

@media (max-width: 1100px) {
  .auth-page {
    grid-template-columns: 1fr;
    padding: 0;
  }

  .hero-panel {
    display: none;
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
    padding: 96px 38px 44px;
  }

  .locale-wrap {
    top: 38px;
    right: 38px;
  }

  .login-card {
    padding: 34px 34px 30px;
  }

  .login-card h2 {
    font-size: 34px;
  }

  .login-card > p {
    margin-bottom: 24px;
    line-height: 1.45;
  }

  .login-card :deep(.el-form-item) {
    margin-bottom: 18px;
  }

  .login-card :deep(.el-input__wrapper) {
    height: 48px;
  }

  .inline-captcha-row {
    grid-template-columns: minmax(0, 1fr) 112px 24px;
    gap: 6px;
  }

  .captcha-image {
    min-width: 112px;
  }

  .captcha-refresh {
    width: 24px;
    height: 48px;
    font-size: 19px;
  }

  .login-options {
    gap: 8px;
  }

  .login-options a {
    max-width: none;
    font-size: 15px;
  }
}

@media (max-width: 460px) {
  .login-panel {
    padding: 88px 18px 36px;
  }

  .locale-wrap {
    top: 28px;
    right: 18px;
  }

  .login-card {
    padding: 30px 24px 26px;
  }

  .login-card h2 {
    font-size: 30px;
  }

  .inline-captcha-row {
    grid-template-columns: minmax(0, 1fr) 96px 22px;
  }

  .captcha-image {
    min-width: 96px;
  }

  .login-options a {
    font-size: 14px;
  }
}

@media (max-height: 960px) and (min-width: 1101px) {
  .auth-page {
    padding: 16px 32px 32px;
  }

  .hero-panel,
  .login-panel {
    min-height: calc(100vh - 48px);
  }

  .hero-panel {
    padding-top: 34px;
    padding-bottom: 76px;
  }

  .sky-logo {
    font-size: 52px;
  }

  .portal-pill {
    margin-top: 24px;
    padding: 8px 16px;
  }

  .hero-copy {
    margin-top: 28px;
  }

  .hero-copy h1 {
    margin-bottom: 14px;
    font-size: 38px;
    line-height: 1.2;
  }

  .hero-copy p {
    font-size: 16px;
    line-height: 1.55;
  }

  .order-card {
    top: 266px;
  }

  .chart-card {
    bottom: 206px;
    transform: scale(0.9);
    transform-origin: right bottom;
  }

  .material-card {
    bottom: 98px;
    transform: scale(0.9);
    transform-origin: right bottom;
  }

  .trust-card {
    bottom: 34px;
    transform: scale(0.88);
    transform-origin: left bottom;
  }

  .login-panel {
    padding: 58px 46px 42px;
  }

  .locale-wrap {
    top: 28px;
  }

  .login-card {
    padding: 32px 40px 24px;
  }

  .login-card h2 {
    margin-bottom: 8px;
    font-size: 32px;
  }

  .login-card > p {
    margin-bottom: 22px;
  }

  .login-card :deep(.el-form-item) {
    margin-bottom: 14px;
  }

  .login-card :deep(.el-form-item__label) {
    margin-bottom: 7px;
  }

  .login-card :deep(.el-input__wrapper) {
    height: 46px;
  }

  .login-options {
    margin: 0 0 16px;
  }

  .submit,
  .google-button-host {
    height: 46px;
  }

  .login-divider {
    margin: 16px 0;
  }

  .auth-footer {
    bottom: 9px;
  }
}

@media (max-height: 820px) and (min-width: 1101px) {
  .auth-page {
    padding-bottom: 16px;
    overflow: hidden;
  }

  .hero-panel,
  .login-panel {
    min-height: calc(100vh - 32px);
  }

  .login-panel {
    padding-top: 52px;
    padding-bottom: 28px;
  }

  .login-card {
    padding-top: 28px;
    padding-bottom: 20px;
  }

  .auth-footer {
    display: none;
  }
}
</style>
