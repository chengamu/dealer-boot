import { nextTick, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getGoogleClientId } from '@/utils/config'
import { useUserStore } from '@/stores/user'
import type { GoogleLoginResult } from '@/api/auth'

let googleScriptPromise: Promise<void> | undefined

const googleButtonLocale = 'en'
const googleIdentityScriptSrc = `https://accounts.google.com/gsi/client?hl=${googleButtonLocale}`

export function useGoogleSignIn() {
  const { t } = useI18n()
  const route = useRoute()
  const router = useRouter()
  const userStore = useUserStore()
  const googleLoading = ref(false)
  const googleButtonRef = ref<HTMLElement>()
  const googleClientId = getGoogleClientId()

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
        script.src = googleIdentityScriptSrc
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
      locale: googleButtonLocale
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

  async function handleGoogleApplyStatus(result: GoogleLoginResult) {
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

  return {
    googleButtonRef,
    googleClientId,
    googleLoading,
    renderGoogleButton
  }
}
