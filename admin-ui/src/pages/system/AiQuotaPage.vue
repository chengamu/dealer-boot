<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div class="panel-header">
          <span>{{ t('ai.settings.quota') }}</span>
          <el-button type="primary" icon="Plus" @click="openQuotaDialog()" v-hasPermi="['ai:quota:manage']">
            {{ t('ai.settings.addQuota') }}
          </el-button>
        </div>
      </template>

      <el-form :model="query" inline>
        <el-form-item :label="t('ai.settings.tenantId')">
          <el-input-number v-model="query.tenantId" :min="1" controls-position="right" clearable />
        </el-form-item>
        <el-form-item :label="t('ai.settings.userId')">
          <el-input-number v-model="query.userId" :min="1" controls-position="right" clearable />
        </el-form-item>
        <el-form-item :label="t('ai.settings.limit')">
          <el-input-number v-model="query.limit" :min="1" :max="500" controls-position="right" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="loadQuotas">{{ t('common.search') }}</el-button>
          <el-button icon="Refresh" @click="resetQuery">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="quotas" height="520">
        <el-table-column prop="tenantId" :label="t('ai.settings.tenantId')" width="120" />
        <el-table-column prop="userId" :label="t('ai.settings.userId')" width="120" />
        <el-table-column prop="dailyRequestLimit" :label="t('ai.settings.dailyRequestLimit')" min-width="150" />
        <el-table-column prop="dailyTokenLimit" :label="t('ai.settings.dailyTokenLimit')" min-width="150" />
        <el-table-column prop="dailyCostLimit" :label="t('ai.settings.dailyCostLimit')" min-width="150" />
        <el-table-column prop="status" :label="t('common.status')" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === '1' ? 'success' : 'info'">{{ row.status === '1' ? t('ai.settings.enabled') : t('common.disabled') }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" :label="t('common.updateTime')" min-width="170">
          <template #default="{ row }">{{ formatUtc(row.updateTime || row.createTime) || '-' }}</template>
        </el-table-column>
        <el-table-column :label="t('common.operate')" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" icon="Edit" @click="openQuotaDialog(row)" v-hasPermi="['ai:quota:manage']">
              {{ t('common.edit') }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <AdminDialog v-model="dialogOpen" :title="t('ai.settings.quota')" width="640px">
      <el-form ref="quotaRef" :model="quotaForm" :rules="rules" label-width="150px">
        <el-form-item :label="t('ai.settings.tenantId')" prop="tenantId">
          <el-input-number v-model="quotaForm.tenantId" :min="1" controls-position="right" />
        </el-form-item>
        <el-form-item :label="t('ai.settings.userId')" prop="userId">
          <el-input-number v-model="quotaForm.userId" :min="1" controls-position="right" />
        </el-form-item>
        <el-form-item :label="t('ai.settings.dailyRequestLimit')">
          <el-input-number v-model="quotaForm.dailyRequestLimit" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item :label="t('ai.settings.dailyTokenLimit')">
          <el-input-number v-model="quotaForm.dailyTokenLimit" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item :label="t('ai.settings.dailyCostLimit')">
          <el-input-number v-model="quotaForm.dailyCostLimit" :min="0" :precision="6" controls-position="right" />
        </el-form-item>
        <el-form-item :label="t('ai.settings.enabled')">
          <el-switch v-model="enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <AdminDialogFooter>
          <el-button @click="dialogOpen = false">{{ t('common.cancel') }}</el-button>
          <el-button type="primary" @click="handleSaveQuota">{{ t('common.save') }}</el-button>
        </AdminDialogFooter>
      </template>
    </AdminDialog>
  </div>
</template>

<script setup lang="ts" name="AiQuotaPage">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import AdminDialog from '@/components/AdminDialog/index.vue'
import AdminDialogFooter from '@/components/AdminDialogFooter/index.vue'
import { listAiUserQuotas, saveAiUserQuota, type AiUserQuota } from '@/api/ai-admin'
import { formatUtc } from '@/utils/datetime'
import { useAiSettingsI18n } from './ai-settings/useAiSettingsI18n'

const t = useAiSettingsI18n()
const loading = ref(false)
const quotas = ref<AiUserQuota[]>([])
const dialogOpen = ref(false)
const quotaRef = ref<FormInstance>()
const query = reactive({ tenantId: undefined as number | undefined, userId: undefined as number | undefined, limit: 200 })
const quotaForm = reactive<AiUserQuota>(emptyQuota())
const enabled = computed({
  get: () => quotaForm.status !== '0',
  set: (value: boolean) => {
    quotaForm.status = value ? '1' : '0'
  }
})

const rules = computed<FormRules<AiUserQuota>>(() => ({
  tenantId: [{ required: true, message: t('ai.settings.tenantIdRequired'), trigger: 'blur' }],
  userId: [{ required: true, message: t('ai.settings.userIdRequired'), trigger: 'blur' }]
}))

function emptyQuota(): AiUserQuota {
  return { status: '1' }
}

async function loadQuotas() {
  loading.value = true
  try {
    quotas.value = await listAiUserQuotas(query)
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.tenantId = undefined
  query.userId = undefined
  query.limit = 200
  void loadQuotas()
}

function openQuotaDialog(row?: AiUserQuota) {
  Object.assign(quotaForm, emptyQuota(), row || {})
  dialogOpen.value = true
}

async function handleSaveQuota() {
  const valid = await quotaRef.value?.validate().catch(() => false)
  if (!valid) return
  await saveAiUserQuota(quotaForm)
  ElMessage.success(t('common.success'))
  dialogOpen.value = false
  await loadQuotas()
}

onMounted(() => {
  void loadQuotas()
})
</script>

<style scoped>
.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
</style>
