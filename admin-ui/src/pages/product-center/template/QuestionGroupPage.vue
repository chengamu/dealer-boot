<template>
  <div class="app-container product-center-page">
    <product-entity-grid-page :config="questionGroupConfig" />
  </div>
</template>

<script setup lang="ts" name="QuestionGroupPage">
import { computed } from 'vue'
import { questionGroupApi } from '@/api/product-capability/config'
import ProductEntityGridPage, { type ProductGridConfig } from '@/pages/product-center/components/ProductEntityGridPage.vue'

const templatePermissions = {
  add: 'product:template:edit',
  edit: 'product:template:edit',
  remove: 'product:template:edit',
  reference: 'product:template:list'
}

const questionGroupConfig = computed<ProductGridConfig>(() => ({
  key: 'questionGroup',
  titleKey: 'productCenter.template.questionGroup',
  descriptionKey: 'productCenter.template.questionGroupDescription',
  idKey: 'questionGroupId',
  permissions: templatePermissions,
  api: questionGroupApi,
  fields: [
    { prop: 'groupCode', labelKey: 'productCenter.template.groupCode', search: true, required: true },
    { prop: 'groupNameCn', labelKey: 'productCenter.template.groupNameCn', search: true, required: true },
    { prop: 'groupNameEn', labelKey: 'productCenter.template.groupNameEn' },
    { prop: 'descriptionCn', labelKey: 'productCenter.template.descriptionCn', type: 'textarea', table: false },
    { prop: 'descriptionEn', labelKey: 'productCenter.template.descriptionEn', type: 'textarea', table: false },
    { prop: 'status', labelKey: 'productCenter.common.status', type: 'status', search: true },
    { prop: 'sortOrder', labelKey: 'productCenter.common.sortOrder', type: 'number' },
    { prop: 'remark', labelKey: 'productCenter.common.remark', type: 'textarea', table: false }
  ]
}))
</script>
