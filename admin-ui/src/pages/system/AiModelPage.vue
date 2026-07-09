<template>
  <div class="app-container ai-table-page">
    <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true" class="ai-table-page__search">
      <el-form-item :label="t('ai.settings.channel')" prop="providerCode">
        <el-select v-model="queryParams.providerCode" clearable filterable style="width: 220px">
          <el-option v-for="item in providers" :key="item.providerCode" :label="providerLabel(item)" :value="item.providerCode || ''" />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('ai.settings.modelName')" prop="modelName">
        <el-input v-model="queryParams.modelName" clearable style="width: 220px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item :label="t('common.status')" prop="status">
        <el-select v-model="queryParams.status" clearable style="width: 140px">
          <el-option v-for="dict in sys_normal_disable" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">{{ t('common.search') }}</el-button>
        <el-button icon="Refresh" @click="resetQuery">{{ t('common.reset') }}</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8 ai-table-page__toolbar">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="openModelDrawer()" v-hasPermi="['ai:model:add']">
          {{ t('common.add') }}
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="dataList" border class="ai-table-page__table">
      <el-table-column type="index" :label="t('common.index')" width="70" align="center" />
      <el-table-column :label="t('ai.settings.channel')" min-width="160" show-overflow-tooltip>
        <template #default="{ row }">{{ channelName(row) }}</template>
      </el-table-column>
      <el-table-column :label="t('ai.settings.modelName')" prop="modelName" min-width="180" show-overflow-tooltip />
      <el-table-column :label="t('ai.settings.modelCode')" prop="modelCode" min-width="180" show-overflow-tooltip />
      <el-table-column :label="t('ai.settings.modelType')" prop="modelType" width="130" align="center">
        <template #default="{ row }">
          <dict-tag :options="ai_model_type" :value="row.modelType" />
        </template>
      </el-table-column>
      <el-table-column :label="t('ai.settings.contextWindow')" prop="contextWindow" width="130" align="right">
        <template #default="{ row }">{{ formatContextWindow(row.contextWindow) }}</template>
      </el-table-column>
      <el-table-column :label="t('ai.settings.defaultModel')" prop="defaultModel" width="110" align="center">
        <template #default="{ row }">
          <el-tag :type="row.defaultModel ? 'success' : 'info'">{{ row.defaultModel ? t('common.yes') : t('common.no') }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="t('common.status')" prop="status" width="100" align="center">
        <template #default="{ row }">
          <dict-tag :options="sys_normal_disable" :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column :label="t('common.updateTime')" prop="updateTime" width="180" align="center">
        <template #default="{ row }">{{ formatUtc(row.updateTime || row.createTime) || '-' }}</template>
      </el-table-column>
      <el-table-column :label="t('common.operate')" width="180" align="center" fixed="right">
        <template #default="{ row }">
          <AdminTableActions :actions="modelActions(row)" />
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" class="ai-table-page__pagination" @pagination="getList" />

    <AdminDrawer v-model="drawerOpen" :title="modelForm.modelId ? t('ai.settings.editModel') : t('ai.settings.addModel')" size="640px" append-to-body destroy-on-close>
      <el-form ref="modelRef" :model="modelForm" :rules="modelRules" label-width="132px">
        <el-form-item :label="t('ai.settings.channel')" prop="providerCode">
          <el-select v-model="modelForm.providerCode" filterable :disabled="!!modelForm.modelId" style="width: 100%">
            <el-option v-for="item in providers" :key="item.providerCode" :label="providerLabel(item)" :value="item.providerCode || ''" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('ai.settings.modelCode')" prop="modelCode">
          <el-input v-model="modelForm.modelCode" :disabled="!!modelForm.modelId" />
        </el-form-item>
        <el-form-item :label="t('ai.settings.modelName')" prop="modelName">
          <el-input v-model="modelForm.modelName" />
        </el-form-item>
        <el-form-item :label="t('ai.settings.modelType')" prop="modelType">
          <el-select v-model="modelForm.modelType" style="width: 100%">
            <el-option v-for="dict in ai_model_type" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('ai.settings.contextWindow')" prop="contextWindow">
          <div class="model-number-field">
            <el-input-number v-model="contextWindowMillion" :min="0" :precision="4" :step="0.1" controls-position="right" />
            <span class="model-number-field__unit">{{ t('ai.settings.contextWindowUnit') }}</span>
          </div>
        </el-form-item>
        <el-form-item :label="t('ai.settings.inputPrice')" prop="inputPrice">
          <div class="model-number-field">
            <el-input-number v-model="modelForm.inputPrice" :min="0" :precision="4" controls-position="right" />
            <span class="model-number-field__unit">{{ t('ai.settings.priceUnit') }}</span>
          </div>
        </el-form-item>
        <el-form-item :label="t('ai.settings.outputPrice')" prop="outputPrice">
          <div class="model-number-field">
            <el-input-number v-model="modelForm.outputPrice" :min="0" :precision="4" controls-position="right" />
            <span class="model-number-field__unit">{{ t('ai.settings.priceUnit') }}</span>
          </div>
        </el-form-item>
        <el-form-item :label="t('ai.settings.defaultModel')" prop="defaultModel">
          <el-switch v-model="modelForm.defaultModel" />
        </el-form-item>
        <el-form-item :label="t('common.status')" prop="status">
          <el-radio-group v-model="modelForm.status">
            <el-radio v-for="dict in sys_normal_disable" :key="dict.value" :value="dict.value">{{ dict.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="t('user.remark')" prop="remark">
          <el-input v-model="modelForm.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="admin-drawer__footer-actions">
          <el-button @click="drawerOpen = false">{{ t('common.cancel') }}</el-button>
          <el-button type="primary" @click="handleSaveModel">{{ t('common.save') }}</el-button>
        </div>
      </template>
    </AdminDrawer>
  </div>
</template>

<script setup lang="ts" name="AiModelPage">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import AdminDrawer from '@/components/AdminDrawer/index.vue'
import AdminTableActions, { type AdminTableAction } from '@/components/AdminTableActions/index.vue'
import {
  addAiProviderModel,
  listAiProviderModels,
  listAiProviderOptions,
  setDefaultAiProviderModel,
  updateAiProviderModel,
  type AiProviderConfig,
  type AiProviderModel,
  type AiProviderModelQuery
} from '@/api/ai-admin'
import { formatUtc } from '@/utils/datetime'
import { useDict } from '@/utils/dict'
import { useAiI18n } from './useAiI18n'

const t = useAiI18n()
const { ai_model_type, sys_normal_disable } = useDict('ai_model_type', 'sys_normal_disable')
const loading = ref(false)
const showSearch = ref(true)
const total = ref(0)
const providers = ref<AiProviderConfig[]>([])
const dataList = ref<AiProviderModel[]>([])
const queryRef = ref<FormInstance>()
const modelRef = ref<FormInstance>()
const drawerOpen = ref(false)
const contextWindowMillion = ref<number>()

const queryParams = reactive<AiProviderModelQuery>({
  pageNum: 1,
  pageSize: 10
})
const modelForm = reactive<AiProviderModel>(emptyModel())

const modelRules = computed<FormRules>(() => ({
  providerCode: [{ required: true, message: t('ai.settings.providerCodeRequired'), trigger: 'change' }],
  modelCode: [{ required: true, message: t('ai.settings.modelCodeRequired'), trigger: 'blur' }],
  modelName: [{ required: true, message: t('ai.settings.modelNameRequired'), trigger: 'blur' }],
  modelType: [{ required: true, message: t('ai.settings.modelTypeRequired'), trigger: 'change' }]
}))

function emptyModel(): AiProviderModel {
  return {
    modelType: 'CHAT',
    defaultModel: false,
    status: '1'
  }
}

function providerLabel(provider: AiProviderConfig) {
  return `${provider.providerName || provider.providerCode} / ${provider.providerCode}`
}

function providerByCode(providerCode?: string) {
  return providers.value.find((item) => item.providerCode === providerCode)
}

function providerCodeById(providerId?: number) {
  return providers.value.find((item) => item.providerId === providerId)?.providerCode
}

function channelName(row: AiProviderModel) {
  const providerCode = row.providerCode || providerCodeById(row.providerId)
  return providerByCode(providerCode)?.providerName || providerCode || '-'
}

function tokensToMillion(tokens?: number) {
  if (tokens === null || typeof tokens === 'undefined') return undefined
  return Number((tokens / 1000000).toFixed(4))
}

function millionToTokens(value?: number) {
  if (value === null || typeof value === 'undefined') return undefined
  return Math.round(value * 1000000)
}

function formatContextWindow(tokens?: number) {
  const value = tokensToMillion(tokens)
  return typeof value === 'undefined' ? '-' : `${value}M`
}

async function getProviderOptions() {
  providers.value = await listAiProviderOptions()
}

async function getList() {
  loading.value = true
  try {
    const res = await listAiProviderModels(queryParams)
    dataList.value = res.rows
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  queryParams.pageNum = 1
  void getList()
}

function resetQuery() {
  queryRef.value?.resetFields()
  handleQuery()
}

function openModelDrawer(row?: AiProviderModel) {
  Object.assign(modelForm, emptyModel(), row || {})
  modelForm.providerCode = modelForm.providerCode || providerCodeById(modelForm.providerId) || queryParams.providerCode
  contextWindowMillion.value = tokensToMillion(modelForm.contextWindow)
  drawerOpen.value = true
}

function modelActions(row: AiProviderModel): AdminTableAction[] {
  return [
    {
      label: t('common.edit'),
      icon: 'Edit',
      primary: true,
      permission: 'ai:model:edit',
      onClick: () => openModelDrawer(row)
    },
    {
      label: t('ai.settings.setDefault'),
      icon: 'Star',
      disabled: row.defaultModel,
      permission: 'ai:model:default',
      onClick: () => handleSetDefault(row)
    }
  ]
}

async function handleSaveModel() {
  const valid = await modelRef.value?.validate().catch(() => false)
  if (!valid) return
  const payload = { ...modelForm, contextWindow: millionToTokens(contextWindowMillion.value) }
  if (modelForm.modelId) {
    await updateAiProviderModel(payload)
  } else {
    await addAiProviderModel(payload)
  }
  ElMessage.success(t('common.success'))
  drawerOpen.value = false
  await getList()
}

async function handleSetDefault(row: AiProviderModel) {
  if (!row.modelId) return
  await setDefaultAiProviderModel(row.modelId)
  ElMessage.success(t('common.success'))
  await getList()
}

onMounted(async () => {
  await getProviderOptions()
  await getList()
})
</script>
<style scoped lang="scss">
@use './styles/ai-table-page';
.model-number-field {
  display: flex;
  align-items: center;
  gap: 8px;
}
.model-number-field__unit {
  color: var(--el-text-color-secondary);
  font-size: 13px;
  white-space: nowrap;
}
</style>
