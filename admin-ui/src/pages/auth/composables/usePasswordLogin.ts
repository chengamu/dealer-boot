import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import type { FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { resolveHomeRouteTarget } from '@/stores/homeRoute'
import { getCodeImg } from '@/api/auth'
import type { CaptchaState, LoginFormModel } from '../types'

export function usePasswordLogin() {
  const { t } = useI18n()
  const route = useRoute()
  const router = useRouter()
  const userStore = useUserStore()
  const loading = ref(false)
  const captchaRequired = ref(true)
  const form = reactive<LoginFormModel>({ username: '', password: '', code: '', uuid: '' })
  const captcha = reactive<CaptchaState>({ enabled: false, img: '' })

  const rules = computed<FormRules>(() => ({
    username: [{ required: true, message: t('login.required'), trigger: 'blur' }],
    password: [{ required: true, message: t('login.required'), trigger: 'blur' }],
    code: captchaRequired.value ? [{ required: true, message: t('login.codePlaceholder'), trigger: 'blur' }] : []
  }))

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
      const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : resolveHomeRouteTarget().path
      router.push(redirect)
    } catch (error) {
      const message = String((error as Error)?.message || error || '').toLowerCase()
      if (message.includes('captcha') || message.includes('验证码') || message.includes('verification')) {
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

  onMounted(() => {
    void requireCaptcha()
  })

  return {
    form,
    captcha,
    rules,
    captchaRequired,
    loading,
    loadCaptcha,
    loginWithCurrentForm
  }
}
