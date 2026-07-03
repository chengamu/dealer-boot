<template>
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
          <button class="captcha-image" type="button" :aria-label="t('common.refresh')" :title="t('common.refresh')" @click="emit('loadCaptcha')">
            <img v-if="captcha.img" :src="captcha.img" :alt="t('login.code')" />
            <span v-else>{{ t('common.refresh') }}</span>
          </button>
          <button class="captcha-refresh" type="button" :aria-label="t('common.refresh')" :title="t('common.refresh')" @click="emit('loadCaptcha')">
            <el-icon><RefreshRight /></el-icon>
          </button>
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

    <GoogleSignInButton :refresh-key="googleRefreshKey" />
  </el-card>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import type { FormInstance, FormRules } from 'element-plus'
import { CircleCheckFilled, Lock, Message, RefreshRight } from '@element-plus/icons-vue'
import GoogleSignInButton from './GoogleSignInButton.vue'
import type { CaptchaState, LoginFormModel } from '../types'

defineProps<{
  form: LoginFormModel
  captcha: CaptchaState
  rules: FormRules
  captchaRequired: boolean
  loading: boolean
  googleRefreshKey: string
}>()

const emit = defineEmits<{
  submit: []
  loadCaptcha: []
}>()

const { t } = useI18n()
const formRef = ref<FormInstance>()
const remember = ref(false)

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  emit('submit')
}
</script>

<style scoped lang="scss" src="./LoginFormCard.scss"></style>
