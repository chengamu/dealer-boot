<template>
  <UploadDropZone :label="t('upload.dropOrClick')" :hint="t('upload.pasteHint')" @files="replaceFiles">
    <el-upload
      ref="uploadRef"
      :show-file-list="true"
      :limit="1"
      accept=".pdf,.png,.jpg,.jpeg"
      :http-request="upload"
      :before-upload="beforeUpload"
      :before-remove="remove"
      :on-exceed="replaceFiles"
    >
      <el-button type="primary" plain icon="Upload" :loading="uploading">{{ t('upload.selectFile') }}</el-button>
    </el-upload>
    <template #tip><div class="el-upload__tip">{{ t('upload.maxFileSizeHint', { size: 10 }) }}</div></template>
  </UploadDropZone>
</template>

<script setup lang="ts">
import { onBeforeUnmount, ref } from 'vue'
import { ElMessage, type UploadInstance, type UploadRawFile, type UploadRequestOptions } from 'element-plus'
import { useI18n } from 'vue-i18n'
import UploadDropZone from '@/components/UploadDropZone/index.vue'
import { delOwnedOss } from '@/api/system/oss'
import { uploadOssFile } from '@/api/system/ossUpload'
import { enqueueUploadFiles, selectUploadFiles, validateUploadFile, type UploadValidationIssue } from '@/composables/uploadIntake'

const model = defineModel<string>({ default: '' })
const { t } = useI18n()
const uploading = ref(false)
const uploadRef = ref<UploadInstance>()
const pendingOssId = ref('')
const allowedTypes = ['pdf', 'png', 'jpg', 'jpeg']

function beforeUpload(file: UploadRawFile) {
  const issue = validateUploadFile(file, { allowedExtensions: allowedTypes, maxSizeMb: 10 })
  if (issue) return showValidationIssue(issue)
  return true
}

async function replaceFiles(files: File[]) {
  if (uploading.value || !files.length) return
  if (!await cleanup()) return
  const result = selectUploadFiles(files, {
    allowedExtensions: allowedTypes,
    maxSizeMb: 10,
    limit: 1,
    currentCount: 0
  })
  if (result.issue) showValidationIssue(result.issue)
  uploadRef.value?.clearFiles()
  enqueueUploadFiles(uploadRef.value, result.files)
}

function showValidationIssue(issue: UploadValidationIssue) {
  const message = issue === 'limit'
    ? t('upload.limitExceeded', { limit: 1 })
    : issue === 'size'
      ? t('upload.fileTooLarge', { size: 10 })
      : t('pay.bank.fileType')
  ElMessage.error(message)
  return false
}

async function upload(options: UploadRequestOptions) {
  uploading.value = true
  try {
    const response = await uploadOssFile(options.file, options.filename)
    pendingOssId.value = String(response.data.ossId)
    model.value = pendingOssId.value
    options.onSuccess(response)
  } catch (error) {
    options.onError(error as never)
  } finally {
    uploading.value = false
  }
}

async function remove() {
  return cleanup()
}

async function cleanup() {
  if (!pendingOssId.value) {
    model.value = ''
    return true
  }
  try {
    await delOwnedOss(pendingOssId.value)
    pendingOssId.value = ''
    model.value = ''
    uploadRef.value?.clearFiles()
    return true
  } catch {
    return false
  }
}

function commit() {
  pendingOssId.value = ''
  model.value = ''
  uploadRef.value?.clearFiles()
}

onBeforeUnmount(() => { void cleanup() })
defineExpose({ cleanup, commit })
</script>
