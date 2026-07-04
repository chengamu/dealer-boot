<template>
  <div class="app-container legal-document-page system-table-page">
    <el-form ref="queryRef" :model="queryParams" :inline="true" class="system-table-page__search">
      <el-form-item :label="t('legal.documentType')" prop="documentType">
        <el-select v-model="queryParams.documentType" :placeholder="t('common.selectPlaceholder')" clearable class="legal-document-page__select">
          <el-option v-for="item in documentTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('legal.locale')" prop="locale">
        <el-select v-model="queryParams.locale" :placeholder="t('common.selectPlaceholder')" clearable class="legal-document-page__select">
          <el-option v-for="item in localeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">{{ t('common.search') }}</el-button>
        <el-button icon="Refresh" @click="resetQuery">{{ t('common.reset') }}</el-button>
      </el-form-item>
    </el-form>

    <el-row class="mb8 system-table-page__toolbar">
      <el-button type="primary" icon="Plus" @click="openForm()" v-hasPermi="['system:legal:document:add']">{{ t('common.add') }}</el-button>
    </el-row>

    <el-table v-loading="loading" :data="rows" border class="system-table-page__table">
      <el-table-column :label="t('legal.title')" prop="title" min-width="220" />
      <el-table-column :label="t('legal.documentType')" width="170">
        <template #default="{ row }">{{ formatDocumentType(row.documentType) }}</template>
      </el-table-column>
      <el-table-column :label="t('legal.locale')" width="120">
        <template #default="{ row }">{{ formatLocale(row.locale) }}</template>
      </el-table-column>
      <el-table-column :label="t('legal.version')" prop="version" width="130" />
      <el-table-column :label="t('legal.publishedTime')" width="180">
        <template #default="{ row }">{{ formatUtc(row.publishedTime) }}</template>
      </el-table-column>
      <el-table-column :label="t('common.operate')" align="center" width="150" fixed="right" class-name="small-padding fixed-width">
        <template #default="{ row }">
          <AdminTableActions :actions="[
            { label: t('common.edit'), icon: 'Edit', permission: 'system:legal:document:edit', onClick: () => openForm(row) },
            { label: t('common.delete'), icon: 'Delete', type: 'danger', permission: 'system:legal:document:remove', onClick: () => handleDelete(row) }
          ]" />
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" class="system-table-page__pagination" @pagination="getList" />

    <AdminDrawer
      v-model="open"
      :title="form.documentId ? t('common.edit') : t('common.add')"
      size="720px"
      :close-on-click-modal="false"
      :before-close="formCloseGuard.beforeClose"
      @closed="formCloseGuard.handleClosed"
    >
      <el-form :model="form" label-width="120px">
        <el-form-item :label="t('legal.documentType')"><el-select v-model="form.documentType"><el-option v-for="item in documentTypeOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
        <el-form-item :label="t('legal.locale')"><el-select v-model="form.locale"><el-option v-for="item in localeOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
        <el-form-item :label="t('legal.title')"><el-input v-model="form.title" /></el-form-item>
        <el-form-item :label="t('legal.version')"><el-input v-model="form.version" /></el-form-item>
        <el-form-item :label="t('legal.content')"><el-input v-model="form.content" type="textarea" :rows="18" /></el-form-item>
      </el-form>
      <template #footer>
        <AdminDialogFooter>
          <el-button @click="formCloseGuard.closeWithGuard">{{ t('common.cancel') }}</el-button>
          <el-button type="primary" @click="submitForm">{{ t('common.confirm') }}</el-button>
        </AdminDialogFooter>
      </template>
    </AdminDrawer>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { addLegalDocument, deleteLegalDocument, listLegalDocuments, updateLegalDocument, type LegalDocument, type LegalDocumentQuery } from '@/api/system/legalDocument'
import { formatUtc } from '@/utils/datetime'
import { useFormCloseGuard } from '@/composables/useFormCloseGuard'

const { t } = useI18n()
const queryRef = ref<FormInstance>()
const rows = ref<LegalDocument[]>([])
const loading = ref(false)
const open = ref(false)
const total = ref(0)
const queryParams = reactive<LegalDocumentQuery>({ pageNum: 1, pageSize: 10 })
const form = reactive<LegalDocument>({ status: 'DRAFT', locale: 'en_US' })
const formCloseGuard = useFormCloseGuard({
  enabled: () => open.value,
  getSnapshot: () => JSON.stringify(form),
  close: () => {
    open.value = false
  },
  reset: resetForm,
  t
})
const documentTypeOptions = computed(() => [
  { label: t('legal.typePrivacy'), value: 'privacy' },
  { label: t('legal.typeTerms'), value: 'terms' },
  { label: t('legal.typeCookie'), value: 'cookie' }
])
const localeOptions = computed(() => [
  { label: t('legal.localeEnglish'), value: 'en_US' },
  { label: t('legal.localeChinese'), value: 'zh_CN' }
])
function formatDocumentType(value?: string) {
  return documentTypeOptions.value.find((item) => item.value === value)?.label || value || '-'
}

function formatLocale(value?: string) {
  return localeOptions.value.find((item) => item.value === value)?.label || value || '-'
}

async function getList() {
  loading.value = true
  try {
    const response = await listLegalDocuments(queryParams)
    rows.value = response.rows || []
    total.value = response.total || 0
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  queryRef.value?.resetFields()
  handleQuery()
}

function resetForm() {
  Object.keys(form).forEach((key) => delete form[key as keyof LegalDocument])
  Object.assign(form, { documentId: undefined, documentType: 'privacy', locale: 'en_US', title: '', content: '', version: '', status: 'PUBLISHED' })
}

function openForm(row?: LegalDocument) {
  resetForm()
  Object.assign(form, row || {})
  formCloseGuard.markPristine()
  open.value = true
}

async function submitForm() {
  form.status = 'PUBLISHED'
  if (form.documentId) {
    await updateLegalDocument(form)
    ElMessage.success(t('common.editSuccess'))
  } else {
    await addLegalDocument(form)
    ElMessage.success(t('common.addSuccess'))
  }
  formCloseGuard.markPristine()
  open.value = false
  await getList()
}

async function handleDelete(row: LegalDocument) {
  if (!row.documentId) return
  await ElMessageBox.confirm(t('common.delete') + '?', t('common.prompt'), { type: 'warning' })
  await deleteLegalDocument(row.documentId)
  ElMessage.success(t('common.deleteSuccess'))
  await getList()
}

getList()
</script>

<style scoped>
.legal-document-page__select {
  width: 180px;
}
</style>
