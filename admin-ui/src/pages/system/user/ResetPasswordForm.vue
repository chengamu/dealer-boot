<template>
  <el-form ref="pwdRef" :model="user" :rules="rules" label-width="100px">
    <el-form-item :label="t('user.oldPassword')" prop="oldPassword">
      <el-input v-model="user.oldPassword" :placeholder="t('user.oldPasswordPlaceholder')" type="password" show-password />
    </el-form-item>
    <el-form-item :label="t('user.newPassword')" prop="newPassword">
      <el-input v-model="user.newPassword" :placeholder="t('user.newPasswordPlaceholder')" type="password" show-password />
    </el-form-item>
    <el-form-item :label="t('user.confirmPassword')" prop="confirmPassword">
      <el-input v-model="user.confirmPassword" :placeholder="t('user.confirmPasswordPlaceholder')" type="password" show-password />
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="submit">{{ t('common.save') }}</el-button>
      <el-button type="danger" @click="close">{{ t('common.close') }}</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { updateUserPwd } from '@/api/system/user'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { useTagsViewStore, type TagView } from '@/stores/tagsView'

interface PasswordForm {
  oldPassword?: string
  newPassword?: string
  confirmPassword?: string
}

const route = useRoute()
const router = useRouter()
const tagsViewStore = useTagsViewStore()
const localeStore = useLocaleStore()
const t = (key: string, params?: Record<string, string | number>) => {
  const message = getMessage(key, localeStore.language)
  if (!params) return message
  return Object.entries(params).reduce((text, [name, value]) => text.replaceAll(`{${name}}`, String(value)), message)
}

const pwdRef = ref<FormInstance>()
const user = reactive<PasswordForm>({})
const equalToPassword = (_rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (user.newPassword !== value) {
    callback(new Error(t('user.passwordNotMatch')))
  } else {
    callback()
  }
}
const rules = computed<FormRules<PasswordForm>>(() => ({
  oldPassword: [{ required: true, message: t('user.oldPasswordRequired'), trigger: 'blur' }],
  newPassword: [
    { required: true, message: t('user.newPasswordRequired'), trigger: 'blur' },
    { min: 6, max: 20, message: t('user.profilePasswordLength'), trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: t('user.confirmPasswordRequired'), trigger: 'blur' },
    { validator: equalToPassword, trigger: 'blur' }
  ]
}))

async function submit() {
  const valid = await pwdRef.value?.validate().catch(() => false)
  if (!valid || !user.oldPassword || !user.newPassword) return
  await updateUserPwd(user.oldPassword, user.newPassword)
  ElMessage.success(t('common.editSuccess'))
}

function close() {
  tagsViewStore.delView(route as unknown as TagView)
  router.push('/')
}
</script>
