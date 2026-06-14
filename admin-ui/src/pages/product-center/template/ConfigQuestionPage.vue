<template>
  <div class="app-container product-center-page">
    <product-entity-grid-page :config="questionConfig" />
  </div>
</template>

<script setup lang="ts" name="ConfigQuestionPage">
import { computed, onMounted, ref } from 'vue'
import { configQuestionApi, configTemplateVersionApi, questionGroupApi } from '@/api/product-capability/config'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import ProductEntityGridPage, { type ProductGridConfig } from '@/pages/product-center/components/ProductEntityGridPage.vue'
import type { ConfigTemplateVersionVO, ProductOption, QuestionGroupVO } from '@/api/product-capability/types'

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)

const templateVersions = ref<ConfigTemplateVersionVO[]>([])
const questionGroups = ref<QuestionGroupVO[]>([])

const templatePermissions = {
  add: 'product:template:edit',
  edit: 'product:template:edit',
  remove: 'product:template:edit',
  reference: 'product:template:list'
}

const inputTypeOptions = computed<ProductOption[]>(() => [
  { label: t('productCenter.inputType.singleSelect'), value: 'SINGLE_SELECT' },
  { label: t('productCenter.inputType.multiSelect'), value: 'MULTI_SELECT' },
  { label: t('productCenter.inputType.number'), value: 'NUMBER' },
  { label: t('productCenter.inputType.text'), value: 'TEXT' },
  { label: t('productCenter.inputType.boolean'), value: 'BOOLEAN' }
])
const flagOptions = computed<ProductOption[]>(() => [
  { label: t('common.yes'), value: '1' },
  { label: t('common.no'), value: '0' }
])
const templateVersionOptions = computed<ProductOption[]>(() =>
  templateVersions.value.map((item) => ({
    label: `${item.templateCode || ''} / ${item.versionNo || ''}`.trim(),
    value: item.templateVersionId || 0
  }))
)
const questionGroupOptions = computed<ProductOption[]>(() =>
  questionGroups.value.map((item) => ({
    label: `${item.groupCode || ''} ${item.groupNameCn || item.groupNameEn || ''}`.trim(),
    value: item.questionGroupId || 0
  }))
)

const questionConfig = computed<ProductGridConfig>(() => ({
  key: 'configQuestion',
  titleKey: 'productCenter.template.question',
  descriptionKey: 'productCenter.template.questionDescription',
  idKey: 'questionId',
  permissions: templatePermissions,
  api: configQuestionApi,
  defaultRecord: {
    inputType: 'SINGLE_SELECT',
    requiredFlag: '1',
    customerVisible: '1'
  },
  fields: [
    { prop: 'templateVersionId', labelKey: 'productCenter.template.templateVersion', type: 'select', required: true, options: templateVersionOptions.value },
    { prop: 'questionGroupId', labelKey: 'productCenter.template.questionGroup', type: 'select', options: questionGroupOptions.value },
    { prop: 'questionCode', labelKey: 'productCenter.template.questionCode', search: true, required: true },
    { prop: 'questionNameCn', labelKey: 'productCenter.template.questionNameCn', search: true, required: true },
    { prop: 'questionNameEn', labelKey: 'productCenter.template.questionNameEn' },
    { prop: 'inputType', labelKey: 'productCenter.template.inputType', type: 'select', search: true, required: true, options: inputTypeOptions.value },
    { prop: 'requiredFlag', labelKey: 'productCenter.template.requiredFlag', type: 'select', options: flagOptions.value },
    { prop: 'customerVisible', labelKey: 'productCenter.template.customerVisible', type: 'select', options: flagOptions.value },
    { prop: 'defaultValue', labelKey: 'productCenter.template.defaultValue' },
    { prop: 'validationJson', labelKey: 'productCenter.template.validationJson', type: 'textarea', table: false, formSpan: 2 },
    { prop: 'displayRuleJson', labelKey: 'productCenter.template.displayRuleJson', type: 'textarea', table: false, formSpan: 2 },
    { prop: 'status', labelKey: 'productCenter.common.status', type: 'status', search: true },
    { prop: 'sortOrder', labelKey: 'productCenter.common.sortOrder', type: 'number' },
    { prop: 'remark', labelKey: 'productCenter.common.remark', type: 'textarea', table: false, formSpan: 2 }
  ]
}))

onMounted(async () => {
  const [versionResponse, groupResponse] = await Promise.all([
    configTemplateVersionApi.options?.({ pageNum: 1, pageSize: 200 }),
    questionGroupApi.options?.({ pageNum: 1, pageSize: 100, status: 'ENABLED' })
  ])
  templateVersions.value = Array.isArray(versionResponse) ? versionResponse : versionResponse?.data || []
  questionGroups.value = Array.isArray(groupResponse) ? groupResponse : groupResponse?.data || []
})
</script>
