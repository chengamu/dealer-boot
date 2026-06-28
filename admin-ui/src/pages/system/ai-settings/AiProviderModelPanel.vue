<template>
  <el-card shadow="never">
    <template #header>
      <div class="panel-header">
        <span>{{ t('ai.settings.models') }}</span>
        <el-button type="primary" icon="Plus" :disabled="!provider?.providerCode" @click="startCreate" v-hasPermi="['ai:provider:manage']">
          {{ t('ai.settings.addModel') }}
        </el-button>
      </div>
    </template>

    <el-empty v-if="!provider?.providerCode" :description="t('ai.settings.selectProviderFirst')" />
    <template v-else>
      <el-table v-loading="loading" :data="models" height="240" highlight-current-row @current-change="selectModel">
        <el-table-column prop="modelCode" :label="t('ai.settings.modelCode')" min-width="180" show-overflow-tooltip />
        <el-table-column prop="modelName" :label="t('ai.settings.modelName')" min-width="160" />
        <el-table-column prop="modelType" :label="t('ai.settings.modelType')" width="100" />
        <el-table-column prop="contextWindow" :label="t('ai.settings.contextWindow')" width="120" />
        <el-table-column prop="defaultModel" :label="t('ai.settings.defaultModel')" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.defaultModel" type="success">{{ t('ai.settings.defaultModel') }}</el-tag>
            <el-button v-else link type="primary" @click.stop="handleSetDefault(row)" v-hasPermi="['ai:provider:manage']">
              {{ t('ai.settings.setDefault') }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="status" :label="t('common.status')" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === '1' ? 'success' : 'info'">{{ row.status === '1' ? t('ai.settings.enabled') : t('common.disabled') }}</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <el-divider />
      <el-form ref="modelRef" :model="modelForm" :rules="modelRules" label-width="140px">
        <el-row :gutter="16">
          <el-col :xs="24" :md="12">
            <el-form-item :label="t('ai.settings.modelCode')" prop="modelCode">
              <el-input v-model="modelForm.modelCode" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item :label="t('ai.settings.modelName')" prop="modelName">
              <el-input v-model="modelForm.modelName" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item :label="t('ai.settings.modelType')" prop="modelType">
              <el-select v-model="modelForm.modelType">
                <el-option label="CHAT" value="CHAT" />
                <el-option label="EMBEDDING" value="EMBEDDING" />
                <el-option label="VISION" value="VISION" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item :label="t('ai.settings.contextWindow')">
              <el-input-number v-model="modelForm.contextWindow" :min="0" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item :label="t('ai.settings.enabled')">
              <el-switch v-model="enabled" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item :label="t('ai.settings.defaultModel')">
              <el-switch v-model="modelForm.defaultModel" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item>
          <el-button type="primary" icon="Check" @click="saveModelForm" v-hasPermi="['ai:provider:manage']">{{ t('common.save') }}</el-button>
        </el-form-item>
      </el-form>
    </template>
  </el-card>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { listAiProviderModels, saveAiProviderModel, setDefaultAiProviderModel, type AiProviderConfig, type AiProviderModel } from '@/api/ai-admin'
import { useAiSettingsI18n } from './useAiSettingsI18n'

const props = defineProps<{ provider?: AiProviderConfig }>()
const t = useAiSettingsI18n()
const loading = ref(false)
const models = ref<AiProviderModel[]>([])
const modelRef = ref<FormInstance>()
const modelForm = reactive<AiProviderModel>(emptyModel())
const enabled = computed({
  get: () => modelForm.status !== '0',
  set: (value: boolean) => {
    modelForm.status = value ? '1' : '0'
  }
})

const modelRules = computed<FormRules<AiProviderModel>>(() => ({
  modelCode: [{ required: true, message: t('ai.settings.modelCodeRequired'), trigger: 'blur' }],
  modelName: [{ required: true, message: t('ai.settings.modelNameRequired'), trigger: 'blur' }],
  modelType: [{ required: true, message: t('ai.settings.modelTypeRequired'), trigger: 'change' }]
}))

function emptyModel(): AiProviderModel {
  return {
    modelCode: '',
    modelName: '',
    modelType: 'CHAT',
    defaultModel: false,
    status: '1'
  }
}

async function loadModels() {
  if (!props.provider?.providerCode) {
    models.value = []
    Object.assign(modelForm, emptyModel())
    return
  }
  loading.value = true
  try {
    models.value = await listAiProviderModels(props.provider.providerCode)
    selectModel(models.value.find((item) => item.defaultModel) || models.value[0])
  } finally {
    loading.value = false
  }
}

function selectModel(row?: AiProviderModel) {
  Object.assign(modelForm, emptyModel(), row || {})
}

function startCreate() {
  Object.assign(modelForm, emptyModel())
}

async function saveModelForm() {
  if (!props.provider?.providerCode) return
  const valid = await modelRef.value?.validate().catch(() => false)
  if (!valid) return
  await saveAiProviderModel({ ...modelForm, providerCode: props.provider.providerCode })
  ElMessage.success(t('common.success'))
  await loadModels()
}

async function handleSetDefault(row: AiProviderModel) {
  if (!props.provider?.providerCode || !row.modelId) return
  await setDefaultAiProviderModel(props.provider.providerCode, row.modelId)
  ElMessage.success(t('common.success'))
  await loadModels()
}

watch(() => props.provider?.providerCode, () => void loadModels(), { immediate: true })
</script>

<style scoped>
.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
</style>
