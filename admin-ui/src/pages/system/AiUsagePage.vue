<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div class="panel-header">
          <span>{{ t('ai.settings.usage') }}</span>
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
          <el-button type="primary" icon="Search" @click="loadUsage">{{ t('common.search') }}</el-button>
          <el-button icon="Refresh" @click="resetQuery">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="rows" height="560">
        <el-table-column prop="createdTime" :label="t('ai.settings.createdTime')" min-width="170">
          <template #default="{ row }">{{ formatUtc(row.createdTime) || '-' }}</template>
        </el-table-column>
        <el-table-column prop="tenantId" :label="t('ai.settings.tenantId')" width="110" />
        <el-table-column prop="userId" :label="t('ai.settings.userId')" width="110" />
        <el-table-column prop="provider" :label="t('ai.settings.providerCode')" min-width="120" />
        <el-table-column prop="model" :label="t('ai.settings.modelCode')" min-width="160" show-overflow-tooltip />
        <el-table-column prop="inputTokens" :label="t('ai.settings.inputTokens')" width="120" />
        <el-table-column prop="outputTokens" :label="t('ai.settings.outputTokens')" width="120" />
        <el-table-column prop="costAmount" :label="t('ai.settings.costAmount')" width="120" />
        <el-table-column prop="latencyMs" :label="t('ai.settings.latencyMs')" width="120" />
        <el-table-column prop="status" :label="t('common.status')" width="110" />
        <el-table-column prop="requestId" :label="t('ai.settings.requestId')" min-width="180" show-overflow-tooltip />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts" name="AiUsagePage">
import { onMounted, reactive, ref } from 'vue'
import { listAiUsageLedgers, type AiUsageLedger } from '@/api/ai-admin'
import { formatUtc } from '@/utils/datetime'
import { useAiSettingsI18n } from './ai-settings/useAiSettingsI18n'

const t = useAiSettingsI18n()
const loading = ref(false)
const rows = ref<AiUsageLedger[]>([])
const query = reactive({ tenantId: undefined as number | undefined, userId: undefined as number | undefined, limit: 200 })

async function loadUsage() {
  loading.value = true
  try {
    rows.value = await listAiUsageLedgers(query)
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.tenantId = undefined
  query.userId = undefined
  query.limit = 200
  void loadUsage()
}

onMounted(() => {
  void loadUsage()
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
