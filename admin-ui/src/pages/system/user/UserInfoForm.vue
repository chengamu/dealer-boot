<template>
  <el-form ref="userRef" :model="user" :rules="rules" label-width="90px">
    <el-form-item :label="t('user.nickName')" prop="nickName">
      <el-input v-model="user.nickName" maxlength="30" />
    </el-form-item>
    <el-form-item :label="t('user.phonenumber')" prop="phonenumber">
      <el-input v-model="user.phonenumber" maxlength="11" />
    </el-form-item>
    <el-form-item :label="t('user.email')" prop="email">
      <el-input v-model="user.email" maxlength="50" disabled />
    </el-form-item>
    <el-form-item :label="t('user.sex')">
      <el-radio-group v-model="user.sex">
        <el-radio v-for="dict in sys_user_sex" :key="dict.value" :value="dict.value">{{ getUserSexLabel(dict) }}</el-radio>
      </el-radio-group>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="submit">{{ t('common.save') }}</el-button>
      <el-button type="danger" @click="close">{{ t('common.close') }}</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { updateUserProfile, type SysUser } from '@/api/system/user'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { useTagsViewStore, type TagView } from '@/stores/tagsView'
import { useDict } from '@/utils/dict'

interface DictOption {
  label?: string
  value?: string
}

const props = defineProps<{
  user: SysUser
}>()

const route = useRoute()
const router = useRouter()
const tagsViewStore = useTagsViewStore()
const localeStore = useLocaleStore()
const t = (key: string, params?: Record<string, string | number>) => {
  const message = getMessage(key, localeStore.language)
  if (!params) return message
  return Object.entries(params).reduce((text, [name, value]) => text.replaceAll(`{${name}}`, String(value)), message)
}
const { sys_user_sex } = useDict('sys_user_sex')
const userSexLabelKeys: Record<string, string> = {
  '0': 'user.sexMale',
  '1': 'user.sexFemale',
  '2': 'user.sexUnknown'
}

const userRef = ref<FormInstance>()
const rules = computed<FormRules<SysUser>>(() => ({
  nickName: [{ required: true, message: t('user.nickNameRequired'), trigger: 'blur' }],
  phonenumber: [
    { required: true, message: t('user.phonenumberRequired'), trigger: 'blur' },
    { pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/, message: t('user.phonenumberInvalid'), trigger: 'blur' }
  ]
}))

function getUserSexLabel(dict: DictOption) {
  return dict.value && userSexLabelKeys[dict.value] ? t(userSexLabelKeys[dict.value]) : dict.label || ''
}

async function submit() {
  const valid = await userRef.value?.validate().catch(() => false)
  if (!valid) return
  try {
    await updateUserProfile({
      nickName: props.user.nickName,
      phonenumber: props.user.phonenumber,
      sex: props.user.sex
    })
    ElMessage.success(t('common.editSuccess'))
  } catch {
    // Request interceptor already displays the backend error.
  }
}

function close() {
  tagsViewStore.delView(route as unknown as TagView)
  router.push('/')
}
</script>
