<template>
  <div class="app-container ai-table-page">
    <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true" class="ai-table-page__search">
      <el-form-item :label="t('ai.settings.serviceName')" prop="serviceName">
        <el-input v-model="queryParams.serviceName" clearable style="width: 220px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item :label="t('common.status')" prop="status">
        <el-select v-model="queryParams.status" clearable style="width: 160px">
          <el-option :label="t('ai.settings.enabled')" value="1" />
          <el-option :label="t('common.disabled')" value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">{{ t('common.search') }}</el-button>
        <el-button icon="Refresh" @click="resetQuery">{{ t('common.reset') }}</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8 ai-table-page__toolbar">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="openGenerateDialog" v-hasPermi="['ai:credential:generate']">
          {{ t('ai.settings.generateKey') }}
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="dataList" border class="ai-table-page__table">
      <el-table-column type="index" :label="t('common.index')" width="70" align="center" />
      <el-table-column :label="t('ai.settings.serviceName')" prop="serviceName" min-width="150" show-overflow-tooltip />
      <el-table-column :label="t('ai.settings.secretMarker')" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">{{ row.keyVersion || '-' }}</template>
      </el-table-column>
      <el-table-column :label="t('ai.settings.fingerprint')" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">{{ maskSecret(row.secretFingerprint) }}</template>
      </el-table-column>
      <el-table-column :label="t('common.status')" prop="status" width="110" align="center">
        <template #default="{ row }">
          <el-switch
            :model-value="row.status"
            active-value="1"
            inactive-value="0"
            :disabled="!canChangeStatus(row)"
            @change="handleStatusChange(row, String($event))"
          />
        </template>
      </el-table-column>
      <el-table-column :label="t('ai.settings.lastUsedTime')" prop="lastUsedTime" width="180" align="center">
        <template #default="{ row }">{{ formatUtc(row.lastUsedTime) || '-' }}</template>
      </el-table-column>
      <el-table-column :label="t('common.createTime')" prop="createTime" width="180" align="center">
        <template #default="{ row }">{{ formatUtc(row.createTime) || '-' }}</template>
      </el-table-column>
      <el-table-column :label="t('user.remark')" prop="remark" min-width="180" show-overflow-tooltip />
      <el-table-column :label="t('common.operate')" width="110" align="center" fixed="right">
        <template #default="{ row }">
          <AdminTableActions :actions="credentialActions(row)" />
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" class="ai-table-page__pagination" @pagination="getList" />

    <AdminDialog v-model="generateOpen" :title="t('ai.settings.generateKey')" width="560px" class="ai-credential-dialog" append-to-body>
      <el-form ref="generateRef" :model="generateForm" :rules="generateRules" label-width="110px" class="ai-credential-dialog__form">
        <el-form-item :label="t('ai.settings.serviceName')" prop="serviceName">
          <el-input v-model="generateForm.serviceName" />
        </el-form-item>
        <el-form-item :label="t('user.remark')" prop="remark">
          <el-input v-model="generateForm.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <AdminDialogFooter>
          <el-button @click="generateOpen = false">{{ t('common.cancel') }}</el-button>
          <el-button type="primary" @click="handleGenerate">{{ t('common.confirm') }}</el-button>
        </AdminDialogFooter>
      </template>
    </AdminDialog>

    <AdminDialog v-model="secretOpen" :title="t('ai.settings.copySecretTitle')" width="680px" class="ai-credential-dialog" append-to-body>
      <el-alert :title="t('ai.settings.copySecretAlert')" type="warning" :closable="false" show-icon />
      <el-input v-model="generatedSecret" class="secret-input" type="textarea" :rows="4" readonly />
      <template #footer>
        <AdminDialogFooter>
          <el-button type="primary" @click="secretOpen = false">{{ t('common.confirm') }}</el-button>
        </AdminDialogFooter>
      </template>
    </AdminDialog>
  </div>
</template>

<script setup lang="ts" name="AiCredentialPage">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import AdminDialog from '@/components/AdminDialog/index.vue'
import AdminDialogFooter from '@/components/AdminDialogFooter/index.vue'
import AdminTableActions, { type AdminTableAction } from '@/components/AdminTableActions/index.vue'
import {
  deleteAiServiceCredential,
  disableAiServiceCredential,
  enableAiServiceCredential,
  generateAiServiceCredential,
  listAiServiceCredentials,
  type AiCredentialQuery,
  type AiServiceCredential
} from '@/api/ai-admin'
import { formatUtc } from '@/utils/datetime'
import { checkPermi } from '@/utils/permission'
import { useAiI18n } from './useAiI18n'

const t = useAiI18n()
const loading = ref(false)
const showSearch = ref(true)
const total = ref(0)
const dataList = ref<AiServiceCredential[]>([])
const queryRef = ref<FormInstance>()
const generateRef = ref<FormInstance>()
const generateOpen = ref(false)
const secretOpen = ref(false)
const generatedSecret = ref('')

const queryParams = reactive<AiCredentialQuery>({
  pageNum: 1,
  pageSize: 10
})

const generateForm = reactive({
  serviceName: 'ai-runtime',
  remark: ''
})

const generateRules = computed<FormRules>(() => ({
  serviceName: [{ required: true, message: t('ai.settings.serviceNameRequired'), trigger: 'blur' }]
}))

async function getList() {
  loading.value = true
  try {
    const res = await listAiServiceCredentials(queryParams)
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
  if (!value) return '***'
  return `***${value.slice(-4)}`
}

function credentialActions(row: AiServiceCredential): AdminTableAction[] {
  return [
    {
      label: t('common.delete'),
      icon: 'Delete',
      type: 'danger',
      danger: true,
      permission: 'ai:credential:remove',
      onClick: () => handleDelete(row)
    }
  ]
}

function canChangeStatus(row: AiServiceCredential) {
  return row.status === '1' ? checkPermi(['ai:credential:disable']) : checkPermi(['ai:credential:enable'])
}

function openGenerateDialog() {
  generateForm.serviceName = 'ai-runtime'
  generateForm.remark = ''
  generateOpen.value = true
}

async function handleGenerate() {
  const valid = await generateRef.value?.validate().catch(() => false)
  if (!valid) return
  const res = await generateAiServiceCredential(generateForm)
  generatedSecret.value = res.secret || ''
  generateOpen.value = false
  secretOpen.value = true
  ElMessage.success(t('common.success'))
  await getList()
}

async function handleStatusChange(row: AiServiceCredential, status: string) {
  if (!row.credentialId) return
  const confirmMessage = status === '1' ? t('ai.settings.enableCredentialConfirm') : t('ai.settings.disableCredentialConfirm')
  await ElMessageBox.confirm(confirmMessage, t('ai.settings.confirmTitle'), { type: 'warning' })
  if (status === '1') {
    await enableAiServiceCredential(row.credentialId)
  } else {
    await disableAiServiceCredential(row.credentialId)
  }
  ElMessage.success(t('common.success'))
  await getList()
}

async function handleDelete(row: AiServiceCredential) {
  if (!row.credentialId) return
  await ElMessageBox.confirm(t('ai.settings.deleteCredentialConfirm'), t('ai.settings.confirmTitle'), { type: 'warning' })
  await deleteAiServiceCredential(row.credentialId)
  ElMessage.success(t('common.deleteSuccess'))
  await getList()
}

onMounted(() => {
  void getList()
})
</script>

<style scoped lang="scss">
@use './styles/ai-table-page';

.secret-input {
  margin-top: 12px;
}

:deep(.ai-credential-dialog__form) {
  padding-top: 4px;

  .el-form-item {
    margin-bottom: 12px;
  }

  .el-form-item__label {
    color: #475467;
    font-weight: 500;
  }

  .el-input__wrapper {
    min-height: 32px;
  }
}

:deep(.ai-credential-dialog .el-alert) {
  margin-bottom: 10px;
  border: 1px solid #fde7bd;
  border-radius: 8px;
  background: #fffbf0;
}

:deep(.ai-credential-dialog .el-textarea__inner) {
  border-color: #d9e0ea;
  border-radius: 8px;
  background: #fbfcfe;
  box-shadow: none;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
}

:deep(.ai-credential-dialog .el-button) {
  height: 32px;
  padding: 0 12px;
  border-radius: 6px;
}
</style>
