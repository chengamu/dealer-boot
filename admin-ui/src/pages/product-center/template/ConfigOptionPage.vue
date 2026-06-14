<template>
  <div class="app-container product-center-page">
    <product-entity-grid-page :config="optionConfig" />
  </div>
</template>

<script setup lang="ts" name="ConfigOptionPage">
import { computed, onMounted, ref } from 'vue'
import { configOptionApi, configQuestionApi } from '@/api/product-capability/config'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import ProductEntityGridPage, { type ProductGridConfig } from '@/pages/product-center/components/ProductEntityGridPage.vue'
import type { ConfigQuestionVO, ProductOption, ProductRecord } from '@/api/product-capability/types'

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)

const questions = ref<ConfigQuestionVO[]>([])

const templatePermissions = {
  add: 'product:template:edit',
  edit: 'product:template:edit',
  remove: 'product:template:edit',
  reference: 'product:template:list'
}

const sourceTypeOptions = computed<ProductOption[]>(() => [
  { label: t('productCenter.sourceType.manual'), value: 'MANUAL' },
  { label: t('productCenter.sourceType.baseAttribute'), value: 'BASE_ATTRIBUTE' },
  { label: t('productCenter.sourceType.material'), value: 'MATERIAL' },
  { label: t('productCenter.sourceType.fabricProfile'), value: 'FABRIC_PROFILE' },
  { label: t('productCenter.sourceType.fabricSeries'), value: 'FABRIC_SERIES' },
  { label: t('productCenter.sourceType.component'), value: 'COMPONENT' },
  { label: t('productCenter.sourceType.mediaAsset'), value: 'MEDIA_ASSET' }
])
const questionOptions = computed<ProductOption[]>(() =>
  questions.value.map((item) => ({
    label: `${item.questionCode || ''} ${item.questionNameCn || item.questionNameEn || ''}`.trim(),
    value: item.questionId || 0
  }))
)

function applyQuestion(value: unknown, form: ProductRecord) {
  const question = questions.value.find((item) => item.questionId === value)
  form.templateVersionId = question?.templateVersionId
}

const optionConfig = computed<ProductGridConfig>(() => ({
  key: 'configOption',
  titleKey: 'productCenter.template.option',
  descriptionKey: 'productCenter.template.optionDescription',
  idKey: 'optionId',
  permissions: templatePermissions,
  api: configOptionApi,
  defaultRecord: {
    sourceType: 'MANUAL'
  },
  fields: [
    { prop: 'templateVersionId', labelKey: 'productCenter.template.templateVersion', table: false, form: false },
    { prop: 'questionId', labelKey: 'productCenter.template.question', type: 'select', search: true, required: true, options: questionOptions.value, onChange: applyQuestion },
    { prop: 'optionCode', labelKey: 'productCenter.template.optionCode', search: true, required: true },
    { prop: 'optionNameCn', labelKey: 'productCenter.template.optionNameCn', search: true, required: true },
    { prop: 'optionNameEn', labelKey: 'productCenter.template.optionNameEn' },
    { prop: 'optionValue', labelKey: 'productCenter.template.optionValue' },
    { prop: 'sourceType', labelKey: 'productCenter.template.sourceType', type: 'select', search: true, options: sourceTypeOptions.value },
    { prop: 'sourceRefId', labelKey: 'productCenter.template.sourceRefId', type: 'number', table: false },
    { prop: 'sourceCode', labelKey: 'productCenter.template.sourceCode', search: true },
    { prop: 'sourceName', labelKey: 'productCenter.template.sourceName' },
    { prop: 'displayNameCn', labelKey: 'productCenter.template.displayNameCn' },
    { prop: 'displayNameEn', labelKey: 'productCenter.template.displayNameEn' },
    { prop: 'valueCode', labelKey: 'productCenter.template.valueCode' },
    { prop: 'componentJson', labelKey: 'productCenter.template.componentJson', type: 'textarea', table: false, formSpan: 2 },
    { prop: 'mediaJson', labelKey: 'productCenter.template.mediaJson', type: 'textarea', table: false, formSpan: 2 },
    { prop: 'ruleJson', labelKey: 'productCenter.template.ruleJson', type: 'textarea', table: false, formSpan: 2 },
    { prop: 'status', labelKey: 'productCenter.common.status', type: 'status', search: true },
    { prop: 'sortOrder', labelKey: 'productCenter.common.sortOrder', type: 'number' },
    { prop: 'remark', labelKey: 'productCenter.common.remark', type: 'textarea', table: false, formSpan: 2 }
  ]
}))

onMounted(async () => {
  const response = await configQuestionApi.options?.({ pageNum: 1, pageSize: 300, status: 'ENABLED' })
  questions.value = Array.isArray(response) ? response : response?.data || []
})
</script>
