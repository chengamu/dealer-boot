<template>
  <div class="app-container product-center-page">
    <product-entity-grid-page v-if="activeConfig" :config="activeConfig" />
  </div>
</template>

<script setup lang="ts" name="ProductFabricPage">
import { computed } from 'vue'
import { fabricSeriesApi } from '@/api/product-capability/fabric'
import ProductEntityGridPage, { type ProductGridConfig } from '@/pages/product-center/components/ProductEntityGridPage.vue'

const configs = computed<ProductGridConfig[]>(() => [
  {
    key: 'series',
    titleKey: 'productCenter.fabricSeries.title',
    descriptionKey: 'productCenter.fabric.description',
    idKey: 'seriesId',
    permissions: {
      add: 'product:fabric:add',
      edit: 'product:fabric:edit',
      remove: 'product:fabric:remove',
      reference: 'product:fabric:reference'
    },
    api: fabricSeriesApi,
    singleRowActions: true,
    defaultRecord: { materialType: 'FABRIC' },
    submitFields: ['seriesId', 'seriesCode', 'seriesNameCn', 'seriesNameEn', 'materialType', 'status', 'remark'],
    defaultSort: { prop: 'updateTime', order: 'descending' },
    attachments: { targetType: 'FABRIC_SERIES', targetCodeField: 'seriesCode', defaultUsageType: 'REFERENCE' },
    fields: [
      { prop: 'seriesCode', labelKey: 'productCenter.fabricSeries.code', search: true, required: true, sortable: true, minWidth: 160, sectionKey: 'basic', sectionLabelKey: 'productCenter.formSection.basic' },
      { prop: 'seriesNameCn', labelKey: 'productCenter.fabricSeries.name', search: true, required: true, sortable: true, minWidth: 180, sectionKey: 'basic' },
      { prop: 'seriesNameEn', labelKey: 'productCenter.fabricSeries.nameEn', minWidth: 220, sectionKey: 'basic' },
      { prop: 'materialType', labelKey: 'productCenter.fabricSeries.materialType', form: false, table: false },
      { prop: 'status', labelKey: 'productCenter.common.status', type: 'status', search: true },
      { prop: 'remark', labelKey: 'productCenter.common.remark', type: 'textarea', table: false, formSpan: 2, sectionKey: 'note', sectionLabelKey: 'productCenter.formSection.note' }
    ]
  }
])

const activeConfig = computed(() => configs.value[0])
</script>

<style scoped lang="scss">
.product-center-page {
  :deep(.product-grid-page) {
    min-height: 100%;
  }
}
</style>
