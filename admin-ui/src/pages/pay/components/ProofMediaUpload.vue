<template>
  <el-upload :show-file-list="true" :limit="1" accept=".pdf,.png,.jpg,.jpeg" :http-request="upload" :before-upload="beforeUpload" :on-remove="clear">
    <el-button icon="Upload" :loading="uploading">{{ t('pay.bank.upload') }}</el-button>
  </el-upload>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage, type UploadRawFile, type UploadRequestOptions } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { uploadOssFile } from '@/api/system/ossUpload'

const model = defineModel<string>({ default: '' })
const { t } = useI18n()
const uploading = ref(false)

function beforeUpload(file: UploadRawFile) {
  const allowed = ['application/pdf', 'image/png', 'image/jpeg'].includes(file.type)
  if (!allowed) ElMessage.error(t('pay.bank.fileType'))
  return allowed
}

async function upload(options: UploadRequestOptions) {
  uploading.value = true
  try {
    const response = await uploadOssFile(options.file, options.filename)
    model.value = String(response.data.ossId)
    options.onSuccess(response)
  } catch (error) {
    options.onError(error as never)
  } finally {
    uploading.value = false
  }
}

function clear() { model.value = '' }
</script>
