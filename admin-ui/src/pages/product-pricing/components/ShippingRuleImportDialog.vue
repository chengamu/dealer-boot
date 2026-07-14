<template>
  <AdminDialog
    :model-value="modelValue"
    :title="t('productCenter.shippingTemplate.importRulesTitle')"
    width="620px"
    variant="form"
    append-to-body
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <el-alert
      :title="t('productCenter.shippingTemplate.importReplaceHint')"
      type="warning"
      show-icon
      :closable="false"
      class="shipping-rule-import__alert"
    />
    <UploadDropZone :label="t('productCenter.shippingTemplate.importDropText')" :hint="t('upload.pasteHint')" @files="handleIncomingFiles">
      <el-upload
        ref="uploadRef"
        :auto-upload="false"
        :limit="1"
        accept=".xls,.xlsx"
        :on-change="selectFile"
        :on-exceed="handleExceed"
        :on-remove="clearFile"
      >
        <el-button type="primary" plain icon="Upload">{{ t('upload.selectFile') }}</el-button>
        <template #tip>
          <div class="el-upload__tip">{{ t('productCenter.shippingTemplate.importFileTip') }}</div>
          <div class="el-upload__tip">{{ t('upload.maxFileSizeHint', { size: 10 }) }}</div>
        </template>
      </el-upload>
    </UploadDropZone>

    <template #footer>
      <div class="shipping-rule-import__footer">
        <el-button icon="Download" :loading="downloading" :disabled="downloading" @click="downloadTemplate">
          {{ t('productCenter.shippingTemplate.downloadImportTemplate') }}
        </el-button>
        <div>
          <el-button @click="$emit('update:modelValue', false)">{{ t('common.cancel') }}</el-button>
          <el-button type="primary" :loading="loading" :disabled="!file" @click="submit">
            {{ t('common.confirm') }}
          </el-button>
        </div>
      </div>
    </template>
  </AdminDialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage, type UploadFile, type UploadInstance } from 'element-plus'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { download } from '@/utils/request'
import { shippingTemplateApi } from '@/api/product-pricing/pricing'
import type { ShippingTemplateRuleVO } from '@/api/product-pricing/types'
import UploadDropZone from '@/components/UploadDropZone/index.vue'
import { enqueueUploadFiles, selectUploadFiles, validateUploadFile, type UploadValidationIssue } from '@/composables/uploadIntake'

const props = defineProps<{ modelValue: boolean }>()
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  imported: [rows: ShippingTemplateRuleVO[]]
}>()
const localeStore = useLocaleStore()
const t = (key: string, params?: Record<string, string | number>) => {
  const message = getMessage(key, localeStore.language)
  if (!params) return message
  return Object.entries(params).reduce((text, [name, value]) => text.replaceAll(`{${name}}`, String(value)), message)
}
const file = ref<File>()
const uploadRef = ref<UploadInstance>()
const loading = ref(false)
const downloading = ref(false)

watch(() => props.modelValue, (open) => {
  if (open) {
    file.value = undefined
    uploadRef.value?.clearFiles()
  }
})

function selectFile(uploadFile: UploadFile) {
  if (!uploadFile.raw) return
  const issue = validateUploadFile(uploadFile.raw, { allowedExtensions: ['xls', 'xlsx'], maxSizeMb: 10 })
  if (issue) {
    showValidationIssue(issue)
    file.value = undefined
    uploadRef.value?.clearFiles()
    return
  }
  file.value = uploadFile.raw
}

function clearFile() {
  file.value = undefined
}

function handleIncomingFiles(files: File[]) {
  uploadRef.value?.clearFiles()
  file.value = undefined
  const result = selectUploadFiles(files, { allowedExtensions: ['xls', 'xlsx'], maxSizeMb: 10, limit: 1 })
  if (result.issue) showValidationIssue(result.issue)
  enqueueUploadFiles(uploadRef.value, result.files, false)
}

function handleExceed() {
  showValidationIssue('limit')
}

function showValidationIssue(issue: UploadValidationIssue) {
  const message = issue === 'limit'
    ? t('upload.limitExceeded', { limit: 1 })
    : issue === 'size'
      ? t('upload.fileTooLarge', { size: 10 })
      : t('upload.invalidFileType', { types: 'xls/xlsx' })
  ElMessage.error(message)
}

async function downloadTemplate() {
  if (downloading.value) return
  downloading.value = true
  try {
    await download('product-pricing/shipping-templates/import-template', {}, `shipping_rule_template_${Date.now()}.xlsx`)
  } finally {
    downloading.value = false
  }
}

async function submit() {
  if (!file.value) return
  loading.value = true
  try {
    const response = await shippingTemplateApi.importRules(file.value)
    emit('imported', response.data || [])
    emit('update:modelValue', false)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.shipping-rule-import__alert {
  margin-bottom: 14px;
}

.shipping-rule-import__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}
</style>
