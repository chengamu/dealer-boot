<template>
  <el-card shadow="never">
    <template #header>
      <div class="panel-header">
        <span>{{ t('ai.settings.provider') }}</span>
        <el-button type="primary" icon="Plus" @click="startCreate" v-hasPermi="['ai:provider:manage']">
          {{ t('ai.settings.addProvider') }}
        </el-button>
      </div>
    </template>

    <el-table v-loading="loading" :data="providers" height="180" highlight-current-row @current-change="selectProvider">
      <el-table-column prop="providerCode" :label="t('ai.settings.providerCode')" min-width="110" />
      <el-table-column prop="providerName" :label="t('ai.settings.providerName')" min-width="120" />
      <el-table-column prop="defaultModel" :label="t('ai.settings.defaultModel')" min-width="150" show-overflow-tooltip />
      <el-table-column prop="enabled" :label="t('ai.settings.enabled')" width="90">
        <template #default="{ row }">
          <el-tag :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? t('ai.settings.enabled') : t('common.disabled') }}</el-tag>
        </template>
      </el-table-column>
    </el-table>

    <el-divider />
    <el-form ref="providerRef" :model="providerForm" :rules="providerRules" label-width="140px">
      <el-form-item :label="t('ai.settings.providerCode')" prop="providerCode">
        <el-input v-model="providerForm.providerCode" />
      </el-form-item>
      <el-form-item :label="t('ai.settings.providerName')" prop="providerName">
        <el-input v-model="providerForm.providerName" />
      </el-form-item>
      <el-form-item :label="t('ai.settings.baseUrl')" prop="baseUrl">
        <el-input v-model="providerForm.baseUrl" />
      </el-form-item>
      <el-form-item :label="t('ai.settings.chatPath')" prop="chatCompletionsPath">
        <el-input v-model="providerForm.chatCompletionsPath" />
      </el-form-item>
      <el-form-item :label="t('ai.settings.timeoutSeconds')" prop="timeoutSeconds">
        <el-input-number v-model="providerForm.timeoutSeconds" :min="10" :max="600" controls-position="right" />
      </el-form-item>
      <el-form-item :label="t('ai.settings.enabled')">
        <el-switch v-model="providerForm.enabled" />
      </el-form-item>
      <el-form-item :label="t('ai.settings.apiKey')">
        <el-input v-model="providerApiKey" type="password" show-password :placeholder="t('ai.settings.apiKeyPlaceholder')" />
        <div class="hint">
          {{ providerForm.keyFingerprint ? t('ai.settings.currentFingerprint', { fingerprint: providerForm.keyFingerprint }) : t('ai.settings.noApiKey') }}
        </div>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Check" @click="saveProviderForm" v-hasPermi="['ai:provider:manage']">{{ t('common.save') }}</el-button>
        <el-button @click="saveProviderKey" v-hasPermi="['ai:provider:manage']">{{ t('ai.settings.updateApiKey') }}</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { listAiProviders, saveAiProvider, saveAiProviderApiKey, type AiProviderConfig } from '@/api/ai-admin'
import { useAiSettingsI18n } from './useAiSettingsI18n'

const emit = defineEmits<{ selected: [provider?: AiProviderConfig] }>()
const t = useAiSettingsI18n()
const loading = ref(false)
const providers = ref<AiProviderConfig[]>([])
const providerRef = ref<FormInstance>()
const providerApiKey = ref('')
const providerForm = reactive<AiProviderConfig>(emptyProvider())

const providerRules = computed<FormRules<AiProviderConfig>>(() => ({
  providerCode: [{ required: true, message: t('ai.settings.providerCodeRequired'), trigger: 'blur' }],
  providerName: [{ required: true, message: t('ai.settings.providerNameRequired'), trigger: 'blur' }],
  baseUrl: [{ required: true, message: t('ai.settings.baseUrlRequired'), trigger: 'blur' }]
}))

function emptyProvider(): AiProviderConfig {
  return {
    providerCode: '',
    providerName: '',
    baseUrl: '',
    chatCompletionsPath: '/chat/completions',
    defaultModel: '',
    timeoutSeconds: 120,
    enabled: false,
    status: '1'
  }
}

async function loadProviders() {
  loading.value = true
  try {
    providers.value = await listAiProviders()
    const active = providers.value.find((item) => item.enabled) || providers.value[0]
    if (active) selectProvider(active)
  } finally {
    loading.value = false
  }
}

function selectProvider(row?: AiProviderConfig) {
  if (!row) return
  Object.assign(providerForm, emptyProvider(), row)
  providerApiKey.value = ''
  emit('selected', { ...row })
}

function startCreate() {
  Object.assign(providerForm, emptyProvider())
  providerApiKey.value = ''
  emit('selected', undefined)
}

async function saveProviderForm() {
  const valid = await providerRef.value?.validate().catch(() => false)
  if (!valid) return
  await saveAiProvider(providerForm)
  ElMessage.success(t('common.success'))
  await loadProviders()
}

async function saveProviderKey() {
  if (!providerForm.providerCode || !providerApiKey.value) {
    ElMessage.warning(t('ai.settings.apiKeyRequired'))
    return
  }
  await saveAiProviderApiKey({
    providerCode: providerForm.providerCode,
    apiKey: providerApiKey.value
  })
  providerApiKey.value = ''
  ElMessage.success(t('common.success'))
  await loadProviders()
}

onMounted(() => {
  void loadProviders()
})
</script>

<style scoped>
.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.hint {
  margin-top: 6px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
</style>
