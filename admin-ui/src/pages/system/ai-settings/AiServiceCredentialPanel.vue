<template>
  <el-card shadow="never">
    <template #header>
      <div class="panel-header">
        <span>{{ t('ai.settings.serviceCredential') }}</span>
        <el-button type="primary" icon="Plus" @click="openGenerateDialog" v-hasPermi="['ai:credential:manage']">
          {{ t('ai.settings.generateKey') }}
        </el-button>
      </div>
    </template>
    <el-table v-loading="loading" :data="credentials" height="320">
      <el-table-column prop="serviceName" :label="t('ai.settings.serviceName')" min-width="120" />
      <el-table-column prop="keyVersion" :label="t('ai.settings.keyVersion')" min-width="170" show-overflow-tooltip />
      <el-table-column prop="secretFingerprint" :label="t('ai.settings.fingerprint')" min-width="140" />
      <el-table-column prop="status" :label="t('common.status')" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === '1' ? 'success' : 'info'">
            {{ row.status === '1' ? t('ai.settings.enabled') : t('common.disabled') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="lastUsedTime" :label="t('ai.settings.lastUsedTime')" min-width="170">
        <template #default="{ row }">{{ formatUtc(row.lastUsedTime) || '-' }}</template>
      </el-table-column>
      <el-table-column :label="t('common.operate')" width="110" fixed="right">
        <template #default="{ row }">
          <el-button link type="danger" :disabled="row.status !== '1'" @click="handleDisableCredential(row)" v-hasPermi="['ai:credential:manage']">
            {{ t('ai.settings.disable') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <AdminDialog v-model="generateOpen" :title="t('ai.settings.generateKey')" width="520px">
      <el-form :model="generateForm" label-width="120px">
        <el-form-item :label="t('ai.settings.serviceName')">
          <el-input v-model="generateForm.serviceName" />
        </el-form-item>
        <el-form-item :label="t('user.remark')">
          <el-input v-model="generateForm.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <AdminDialogFooter>
          <el-button @click="generateOpen = false">{{ t('common.cancel') }}</el-button>
          <el-button type="primary" @click="handleGenerateCredential">{{ t('common.confirm') }}</el-button>
        </AdminDialogFooter>
      </template>
    </AdminDialog>

    <AdminDialog v-model="secretOpen" :title="t('ai.settings.copySecretTitle')" width="640px">
      <el-alert :title="t('ai.settings.copySecretAlert')" type="warning" show-icon :closable="false" />
      <el-input class="mt16" :model-value="generatedSecret" type="textarea" :rows="4" readonly />
      <template #footer>
        <AdminDialogFooter>
          <el-button type="primary" @click="secretOpen = false">{{ t('common.close') }}</el-button>
        </AdminDialogFooter>
      </template>
    </AdminDialog>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import AdminDialog from '@/components/AdminDialog/index.vue'
import AdminDialogFooter from '@/components/AdminDialogFooter/index.vue'
import { disableAiServiceCredential, generateAiServiceCredential, listAiServiceCredentials, type AiServiceCredential } from '@/api/ai-admin'
import { formatUtc } from '@/utils/datetime'
import { useAiSettingsI18n } from './useAiSettingsI18n'

const t = useAiSettingsI18n()
const loading = ref(false)
const credentials = ref<AiServiceCredential[]>([])
const generateOpen = ref(false)
const secretOpen = ref(false)
const generatedSecret = ref('')
const generateForm = reactive({ serviceName: 'ai-runtime', remark: '' })

async function loadCredentials() {
  loading.value = true
  try {
    credentials.value = await listAiServiceCredentials()
  } finally {
    loading.value = false
  }
}

function openGenerateDialog() {
  generateForm.serviceName = 'ai-runtime'
  generateForm.remark = ''
  generateOpen.value = true
}

async function handleGenerateCredential() {
  const result = await generateAiServiceCredential(generateForm)
  generatedSecret.value = result.secret || ''
  generateOpen.value = false
  secretOpen.value = true
  await loadCredentials()
}

async function handleDisableCredential(row: AiServiceCredential) {
  if (!row.credentialId) return
  await ElMessageBox.confirm(t('ai.settings.disableCredentialConfirm'), t('common.prompt'), { type: 'warning' })
  await disableAiServiceCredential(row.credentialId)
  ElMessage.success(t('common.success'))
  await loadCredentials()
}

onMounted(() => {
  void loadCredentials()
})
</script>

<style scoped>
.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.mt16 {
  margin-top: 16px;
}
</style>
