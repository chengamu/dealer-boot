<template>
  <div class="app-container product-import-page">
    <product-entity-grid-page ref="batchGridRef" :config="batchConfig" />

    <el-drawer v-model="issueOpen" :title="issueDrawerTitle" size="900px" append-to-body destroy-on-close>
      <product-entity-grid-page v-if="selectedBatchId" :config="issueConfig" />
    </el-drawer>

    <el-drawer v-model="uploadOpen" :title="t('productCenter.import.parseExcel')" size="520px" append-to-body destroy-on-close>
      <el-form label-width="128px">
        <el-form-item :label="t('productCenter.import.type')">
          <el-select v-model="uploadForm.importType" filterable style="width: 100%">
            <el-option :label="t('productCenter.import.typeMaterial')" value="MATERIAL" />
            <el-option :label="t('productCenter.import.typeComponent')" value="COMPONENT" />
            <el-option :label="t('productCenter.import.typeOption')" value="OPTION" />
            <el-option :label="t('productCenter.import.typePrice')" value="PRICE" />
            <el-option :label="t('productCenter.import.typeModel')" value="MODEL" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('productCenter.import.file')">
          <el-upload
            ref="uploadRef"
            drag
            :auto-upload="false"
            :limit="1"
            accept=".xls,.xlsx"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
          >
            <el-icon><UploadFilled /></el-icon>
            <div class="el-upload__text">{{ t('productCenter.import.dropFile') }}</div>
            <template #tip>
              <div class="el-upload__tip">{{ t('productCenter.import.fileTip') }}</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="product-import-page__drawer-footer">
          <el-button @click="uploadOpen = false">{{ t('common.cancel') }}</el-button>
          <el-button type="primary" :loading="uploading" :disabled="!selectedFile" @click="submitUpload">
            {{ t('productCenter.import.parseExcel') }}
          </el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts" name="ProductImportCenterPage">
import { computed, reactive, ref } from 'vue'
import { ElMessage, type UploadFile, type UploadInstance } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { productImportBatchApi, productImportIssueApi } from '@/api/product-capability/import'
import type { ProductRecord } from '@/api/product-capability/types'
import ProductEntityGridPage, { type ProductGridConfig } from '@/pages/product-center/components/ProductEntityGridPage.vue'

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const uploadOpen = ref(false)
const issueOpen = ref(false)
const uploading = ref(false)
const selectedFile = ref<File>()
const selectedBatch = ref<ProductRecord>({})
const uploadRef = ref<UploadInstance>()
const batchGridRef = ref<InstanceType<typeof ProductEntityGridPage>>()
const uploadForm = reactive<ProductRecord>({
  importType: 'MATERIAL'
})

const importPermissions = {
  add: 'product:import:add',
  edit: 'product:import:edit',
  remove: 'product:import:remove',
  reference: 'product:import:query'
}
const issuePermissions = {
  add: 'product:import:edit',
  edit: 'product:import:edit',
  remove: 'product:import:remove',
  reference: 'product:import:list'
}

const selectedBatchId = computed(() => selectedBatch.value.batchId as number | string | undefined)
const issueDrawerTitle = computed(() => {
  const code = selectedBatch.value.batchCode || '-'
  return `${t('productCenter.import.issuePreview')} / ${code}`
})

const batchConfig = computed<ProductGridConfig>(() => ({
  key: 'importBatch',
  titleKey: 'productCenter.import.batchTitle',
  descriptionKey: 'productCenter.import.batchDescription',
  idKey: 'batchId',
  permissions: importPermissions,
  api: productImportBatchApi,
  hideReference: true,
  showDetail: true,
  toolbarActions: [
    {
      labelKey: 'productCenter.import.parseExcel',
      icon: 'UploadFilled',
      type: 'primary',
      permission: 'product:import:add',
      handler: () => {
        uploadOpen.value = true
      }
    }
  ],
  rowActions: [
    {
      labelKey: 'productCenter.import.viewIssues',
      icon: 'DocumentChecked',
      type: 'primary',
      permission: 'product:import:list',
      handler: openIssuePreview
    }
  ],
  fields: [
    { prop: 'batchCode', labelKey: 'productCenter.import.batchCode', search: true, required: true },
    { prop: 'importType', labelKey: 'productCenter.import.type', search: true, required: true },
    { prop: 'sourceFileName', labelKey: 'productCenter.import.fileName', search: true },
    { prop: 'targetObjectType', labelKey: 'productCenter.import.targetType', search: true },
    { prop: 'targetObjectCode', labelKey: 'productCenter.import.targetCode', search: true },
    { prop: 'importStatus', labelKey: 'productCenter.import.status', search: true },
    { prop: 'totalRows', labelKey: 'productCenter.import.totalRows', type: 'number' },
    { prop: 'successRows', labelKey: 'productCenter.import.successRows', type: 'number' },
    { prop: 'warningRows', labelKey: 'productCenter.import.warningRows', type: 'number' },
    { prop: 'failedRows', labelKey: 'productCenter.import.failedRows', type: 'number' },
    { prop: 'createTime', labelKey: 'common.createTime', type: 'datetime' },
    { prop: 'remark', labelKey: 'productCenter.common.remark', type: 'textarea', table: false }
  ]
}))

const issueConfig = computed<ProductGridConfig>(() => ({
  key: `importIssue-${selectedBatchId.value || 'none'}`,
  titleKey: 'productCenter.import.issueTitle',
  descriptionKey: 'productCenter.import.issueDescription',
  idKey: 'issueId',
  readonly: true,
  hideReference: true,
  showDetail: true,
  initialQuery: {
    batchId: selectedBatchId.value
  },
  permissions: issuePermissions,
  api: productImportIssueApi,
  fields: [
    { prop: 'rowNo', labelKey: 'productCenter.import.rowNo', type: 'number', search: true },
    { prop: 'columnName', labelKey: 'productCenter.import.columnName', search: true },
    { prop: 'issueLevel', labelKey: 'productCenter.import.issueLevel', search: true },
    { prop: 'issueCode', labelKey: 'productCenter.import.issueCode', search: true },
    { prop: 'issueMessage', labelKey: 'productCenter.import.issueMessage', type: 'textarea' },
    { prop: 'rawRowJson', labelKey: 'productCenter.import.rawRowJson', type: 'textarea', table: false },
    { prop: 'fixedRowJson', labelKey: 'productCenter.import.fixedRowJson', type: 'textarea', table: false },
    { prop: 'status', labelKey: 'productCenter.common.status', type: 'status', search: true },
    { prop: 'createTime', labelKey: 'common.createTime', type: 'datetime' }
  ]
}))

function handleFileChange(file: UploadFile) {
  selectedFile.value = file.raw
}

function handleFileRemove() {
  selectedFile.value = undefined
}

async function submitUpload() {
  if (!selectedFile.value) return
  uploading.value = true
  try {
    const response = await productImportBatchApi.parseExcel(selectedFile.value, uploadForm)
    ElMessage.success(t('productCenter.import.parsed'))
    selectedBatch.value = response.data || {}
    issueOpen.value = Boolean(selectedBatch.value.batchId)
    uploadOpen.value = false
    uploadRef.value?.clearFiles()
    selectedFile.value = undefined
    await batchGridRef.value?.getList()
  } finally {
    uploading.value = false
  }
}

async function openIssuePreview(row: ProductRecord) {
  selectedBatch.value = row
  issueOpen.value = true
}
</script>

<style scoped lang="scss">
.product-import-page__drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
