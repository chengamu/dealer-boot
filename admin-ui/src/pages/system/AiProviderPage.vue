<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true">
      <el-form-item :label="t('ai.settings.channelName')" prop="providerName">
        <el-input v-model="queryParams.providerName" clearable style="width: 220px" @keyup.enter="handleQuery" />
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

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="openProviderDrawer()" v-hasPermi="['ai:provider:add']">
          {{ t('common.add') }}
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="dataList" border>
      <el-table-column type="index" :label="t('common.index')" width="70" align="center" />
      <el-table-column :label="t('ai.settings.channelName')" prop="providerName" min-width="150" show-overflow-tooltip />
      <el-table-column :label="t('ai.settings.channelCode')" prop="providerCode" min-width="140" show-overflow-tooltip />
      <el-table-column :label="t('ai.settings.baseUrl')" prop="baseUrl" min-width="220" show-overflow-tooltip />
      <el-table-column :label="t('ai.settings.defaultModel')" prop="defaultModel" min-width="180" show-overflow-tooltip />
      <el-table-column :label="t('ai.settings.enabled')" width="100" align="center">
        <template #default="{ row }">
          <el-switch
            :model-value="row.enabled"
            :disabled="!checkPermi(['ai:provider:edit'])"
            @change="handleToggleEnabled(row, Boolean($event))"
          />
        </template>
      </el-table-column>
      <el-table-column :label="t('common.status')" prop="status" width="100" align="center">
        <template #default="{ row }">
          <dict-tag :options="sys_normal_disable" :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column :label="t('ai.settings.secretMarker')" min-width="150" show-overflow-tooltip>
        <template #default="{ row }">{{ maskSecret(row.keyFingerprint) }}</template>
      </el-table-column>
      <el-table-column :label="t('common.updateTime')" prop="updateTime" width="180">
        <template #default="{ row }">{{ formatUtc(row.updateTime || row.createTime) || '-' }}</template>
      </el-table-column>
      <el-table-column :label="t('common.operate')" width="120" align="center" fixed="right">
        <template #default="{ row }">
          <AdminTableActions :actions="providerActions(row)" />
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />

    <AdminDrawer
      v-model="drawerOpen"
      :title="providerForm.providerId ? t('ai.settings.editChannel') : t('ai.settings.addChannel')"
      size="640px"
      append-to-body
      destroy-on-close
    >
      <el-form ref="providerRef" :model="providerForm" :rules="providerRules" label-width="132px">
        <el-form-item :label="t('ai.settings.channelCode')" prop="providerCode">
          <el-input v-model="providerForm.providerCode" :disabled="!!providerForm.providerId" />
        </el-form-item>
        <el-form-item :label="t('ai.settings.channelName')" prop="providerName">
          <el-input v-model="providerForm.providerName" />
        </el-form-item>
        <el-form-item :label="t('ai.settings.baseUrl')" prop="baseUrl">
          <el-input v-model="providerForm.baseUrl" />
        </el-form-item>
        <el-form-item :label="t('ai.settings.chatPath')" prop="chatCompletionsPath">
          <el-input v-model="providerForm.chatCompletionsPath" />
        </el-form-item>
        <el-form-item :label="t('ai.settings.timeoutSeconds')" prop="timeoutSeconds">
          <el-input-number v-model="providerForm.timeoutSeconds" :min="1" controls-position="right" />
        </el-form-item>
        <el-form-item v-if="providerForm.providerId" :label="t('ai.settings.apiKey')" prop="apiKey">
          <el-input v-model="providerForm.apiKey" type="password" show-password autocomplete="new-password" :placeholder="t('ai.settings.apiKeyPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('ai.settings.enabled')" prop="enabled">
          <el-switch v-model="providerForm.enabled" />
        </el-form-item>
        <el-form-item :label="t('common.status')" prop="status">
          <el-radio-group v-model="providerForm.status">
            <el-radio v-for="dict in sys_normal_disable" :key="dict.value" :value="dict.value">{{ dict.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="t('user.remark')" prop="remark">
          <el-input v-model="providerForm.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="admin-drawer__footer-actions">
          <el-button @click="drawerOpen = false">{{ t('common.cancel') }}</el-button>
          <el-button type="primary" @click="handleSaveProvider">{{ t('common.save') }}</el-button>
        </div>
      </template>
    </AdminDrawer>
  </div>
</template>

<script setup lang="ts" name="AiProviderPage">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import AdminDrawer from '@/components/AdminDrawer/index.vue'
import AdminTableActions, { type AdminTableAction } from '@/components/AdminTableActions/index.vue'
import { addAiProvider, listAiProviders, saveAiProviderApiKey, updateAiProvider, type AiProviderConfig, type AiProviderQuery } from '@/api/ai-admin'
import { formatUtc } from '@/utils/datetime'
import { checkPermi } from '@/utils/permission'
import { useDict } from '@/utils/dict'
import { useAiI18n } from './useAiI18n'

const t = useAiI18n()
const { sys_normal_disable } = useDict('sys_normal_disable')
const loading = ref(false)
const showSearch = ref(true)
const total = ref(0)
const dataList = ref<AiProviderConfig[]>([])
const queryRef = ref<FormInstance>()
const providerRef = ref<FormInstance>()
const drawerOpen = ref(false)

const queryParams = reactive<AiProviderQuery>({
  pageNum: 1,
  pageSize: 10
})
const providerForm = reactive<AiProviderConfig>(emptyProvider())

const providerRules = computed<FormRules>(() => ({
  providerCode: [{ required: true, message: t('ai.settings.providerCodeRequired'), trigger: 'blur' }],
  providerName: [{ required: true, message: t('ai.settings.providerNameRequired'), trigger: 'blur' }],
  baseUrl: [{ required: true, message: t('ai.settings.baseUrlRequired'), trigger: 'blur' }]
}))

function emptyProvider(): AiProviderConfig {
  return {
    chatCompletionsPath: '/chat/completions',
    timeoutSeconds: 120,
    enabled: true,
    status: '1',
    apiKey: ''
  }
}

async function getList() {
  loading.value = true
  try {
    const res = await listAiProviders(queryParams)
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

function maskSecret(value?: string) {
  return value ? `***${value.slice(-4)}` : t('ai.settings.noApiKey')
}

function openProviderDrawer(row?: AiProviderConfig) {
  Object.assign(providerForm, emptyProvider(), row || {}, { apiKey: '' })
  drawerOpen.value = true
}

function providerActions(row: AiProviderConfig): AdminTableAction[] {
  return [
    {
      label: t('common.edit'),
      icon: 'Edit',
      primary: true,
      permission: 'ai:provider:edit',
      onClick: () => openProviderDrawer(row)
    }
  ]
}

async function handleToggleEnabled(row: AiProviderConfig, enabled: boolean) {
  await updateAiProvider({ ...row, enabled })
  ElMessage.success(t('common.editSuccess'))
  await getList()
}

async function handleSaveProvider() {
  const valid = await providerRef.value?.validate().catch(() => false)
  if (!valid) return
  if (providerForm.providerId) {
    await updateAiProvider(providerForm)
    if (providerForm.apiKey?.trim()) {
      await saveAiProviderApiKey(providerForm.providerId, {
        providerCode: providerForm.providerCode,
        apiKey: providerForm.apiKey.trim(),
        remark: providerForm.remark
      })
    }
  } else {
    await addAiProvider(providerForm)
  }
  ElMessage.success(t('common.success'))
  drawerOpen.value = false
  await getList()
}

onMounted(() => {
  void getList()
})
</script>
