<template>
  <AdminDrawer
    :model-value="modelValue"
    :title="title"
    size="760px"
    append-to-body
    destroy-on-close
    :close-on-click-modal="false"
    :before-close="beforeClose"
    @update:model-value="emit('update:modelValue', $event)"
    @closed="emit('closed')"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="130px">
      <el-form-item :label="t('legacy.configKey')" prop="configKey">
        <el-input v-model="form.configKey" :placeholder="t('legacy.configKeyPlaceholder')" />
      </el-form-item>
      <el-form-item :label="t('ossConfig.authMode')" prop="ext1">
        <el-select v-model="authMode">
          <el-option :label="t('ossConfig.authMode.accessKey')" value="access_key" />
          <el-option :label="t('ossConfig.authMode.localSts')" value="local_sts" />
          <el-option :label="t('ossConfig.authMode.ecsRamRole')" value="ecs_ram_role" />
        </el-select>
      </el-form-item>
      <el-alert
        v-if="authMode === 'local_sts'"
        class="oss-config-form__alert"
        :title="t('ossConfig.localStsHint')"
        type="info"
        show-icon
        :closable="false"
      />
      <el-alert
        v-else-if="authMode === 'ecs_ram_role'"
        class="oss-config-form__alert"
        :title="t('ossConfig.ecsRamRoleHint')"
        type="info"
        show-icon
        :closable="false"
      />
      <el-form-item :label="t('legacy.endpoint')" prop="endpoint">
        <el-input v-model="form.endpoint" :placeholder="t('legacy.endpointPlaceholder')" />
      </el-form-item>
      <el-form-item :label="t('legacy.domain')" prop="domain">
        <el-input v-model="form.domain" :placeholder="t('legacy.domainPlaceholder')" />
      </el-form-item>
      <template v-if="authMode === 'access_key'">
        <el-form-item :label="t('ossConfig.accessKey')" prop="accessKey">
          <el-input v-model="form.accessKey" :placeholder="t('ossConfig.accessKey')" />
        </el-form-item>
        <el-form-item :label="t('ossConfig.secretKey')" prop="secretKey">
          <el-input v-model="form.secretKey" :placeholder="t('ossConfig.secretKey')" show-password />
        </el-form-item>
      </template>
      <el-form-item :label="t('legacy.bucketName')" prop="bucketName">
        <el-input v-model="form.bucketName" :placeholder="t('legacy.bucketNamePlaceholder')" />
      </el-form-item>
      <el-form-item :label="t('legacy.prefix')" prop="prefix">
        <el-input v-model="form.prefix" :placeholder="t('legacy.prefixPlaceholder')" />
      </el-form-item>
      <el-form-item :label="t('legacy.isHttps')">
        <el-radio-group v-model="form.isHttps">
          <el-radio v-for="option in httpsOptions" :key="option.value" :value="option.value">{{ option.label }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item :label="t('legacy.accessPolicy')" prop="accessPolicy">
        <el-radio-group v-model="form.accessPolicy">
          <el-radio value="0">private</el-radio>
          <el-radio value="1">public</el-radio>
          <el-radio value="2">custom</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item :label="t('legacy.region')" prop="region">
        <el-input v-model="form.region" :placeholder="t('legacy.regionPlaceholder')" />
      </el-form-item>
      <el-form-item :label="t('user.remark')" prop="remark">
        <el-input v-model="form.remark" type="textarea" :placeholder="t('user.remarkPlaceholder')" />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="oss-config-form__footer">
        <el-button @click="emit('cancel')">{{ t('common.cancel') }}</el-button>
        <el-button :loading="buttonLoading" type="primary" @click="emit('submit')">{{ t('common.confirm') }}</el-button>
      </div>
    </template>
  </AdminDrawer>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { type FormInstance, type FormRules } from 'element-plus'
import { useI18n } from 'vue-i18n'
import type { OssConfig } from '@/api/system/ossConfig'
import type { DictOption } from '@/utils/dict'

const props = defineProps<{
  modelValue: boolean
  title: string
  form: OssConfig
  buttonLoading: boolean
  httpsOptions: DictOption[]
  beforeClose: (done: () => void) => void
}>()

const emit = defineEmits<{
  (event: 'update:modelValue', value: boolean): void
  (event: 'cancel'): void
  (event: 'submit'): void
  (event: 'closed'): void
}>()

const { t } = useI18n()
const formRef = ref<FormInstance>()
const authMode = computed({
  get: () => props.form.ext1 || 'access_key',
  set: (value: string) => {
    props.form.ext1 = value
    if (value !== 'access_key') {
      props.form.accessKey = undefined
      props.form.secretKey = undefined
      formRef.value?.clearValidate(['accessKey', 'secretKey'])
    }
  }
})
const rules = computed<FormRules<OssConfig>>(() => ({
  configKey: [{ required: true, message: t('legacy.configKeyRequired'), trigger: 'blur' }],
  accessKey:
    authMode.value === 'access_key'
      ? [
          { required: true, message: t('legacy.accessKeyRequired'), trigger: 'blur' },
          { min: 2, max: 200, message: t('legacy.accessKeyLength'), trigger: 'blur' }
        ]
      : [],
  secretKey:
    authMode.value === 'access_key'
      ? [
          { required: true, message: t('legacy.secretKeyRequired'), trigger: 'blur' },
          { min: 2, max: 100, message: t('legacy.secretKeyLength'), trigger: 'blur' }
        ]
      : [],
  bucketName: [
    { required: true, message: t('legacy.bucketNameRequired'), trigger: 'blur' },
    { min: 2, max: 100, message: t('legacy.bucketNameLength'), trigger: 'blur' }
  ],
  endpoint: [
    { required: true, message: t('legacy.endpointRequired'), trigger: 'blur' },
    { min: 2, max: 100, message: t('legacy.endpointLength'), trigger: 'blur' }
  ],
  accessPolicy: [{ required: true, message: t('legacy.accessPolicyRequired'), trigger: 'change' }]
}))

async function validate() {
  return Boolean(await formRef.value?.validate().catch(() => false))
}

defineExpose({ validate })
</script>

<style scoped>
.oss-config-form__alert {
  width: calc(100% - 130px);
  margin: 0 0 18px 130px;
}

.oss-config-form__footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
