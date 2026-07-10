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
    <el-upload
      drag
      :auto-upload="false"
      :limit="1"
      accept=".xls,.xlsx"
      :on-change="selectFile"
      :on-remove="clearFile"
    >
      <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
      <div class="el-upload__text">{{ t('productCenter.shippingTemplate.importDropText') }}</div>
      <template #tip>
        <div class="el-upload__tip">{{ t('productCenter.shippingTemplate.importFileTip') }}</div>
      </template>
    </el-upload>

    <template #footer>
      <div class="shipping-rule-import__footer">
        <el-button icon="Download" @click="downloadTemplate">
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
import type { UploadFile } from 'element-plus'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { download } from '@/utils/request'
import { shippingTemplateApi } from '@/api/product-pricing/pricing'
import type { ShippingTemplateRuleVO } from '@/api/product-pricing/types'

const props = defineProps<{ modelValue: boolean }>()
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  imported: [rows: ShippingTemplateRuleVO[]]
}>()
const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const file = ref<File>()
const loading = ref(false)

watch(() => props.modelValue, (open) => {
  if (open) file.value = undefined
})

function selectFile(uploadFile: UploadFile) {
  file.value = uploadFile.raw
}

function clearFile() {
  file.value = undefined
}

function downloadTemplate() {
  download('product-pricing/shipping-templates/import-template', {}, `shipping_rule_template_${Date.now()}.xlsx`)
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
