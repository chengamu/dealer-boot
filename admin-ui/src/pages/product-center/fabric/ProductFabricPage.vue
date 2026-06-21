<template>
  <div class="app-container product-center-page">
    <product-entity-grid-page v-if="activeConfig" :config="activeConfig" />
  </div>
</template>

<script setup lang="ts" name="ProductFabricPage">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { fabricSeriesApi } from '@/api/product-capability/fabric'
import ProductEntityGridPage, { type ProductGridConfig } from '@/pages/product-center/components/ProductEntityGridPage.vue'

const router = useRouter()

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
    defaultRecord: { materialType: 'FABRIC' },
    defaultSort: { prop: 'updateTime', order: 'descending' },
    attachments: { targetType: 'FABRIC_SERIES', targetCodeField: 'seriesCode', defaultUsageType: 'SWATCH' },
    rowActions: [
      {
        labelKey: 'productCenter.fabricSeries.manageProfiles',
        icon: 'List',
        permission: 'product:base:list',
        handler: async (row) => {
          await router.push({
            path: '/product-master/materials',
            query: {
              materialType: 'FABRIC',
              fabricSeriesCode: String(row.seriesCode || '')
            }
          })
        }
      }
    ],
    fields: [
      { prop: 'seriesCode', labelKey: 'productCenter.fabricSeries.code', search: true, required: true, sortable: true, minWidth: 160, sectionKey: 'basic', sectionLabelKey: 'productCenter.formSection.basic' },
      { prop: 'seriesNameCn', labelKey: 'productCenter.fabricSeries.name', search: true, required: true, sortable: true, minWidth: 180, sectionKey: 'basic' },
      { prop: 'seriesNameEn', labelKey: 'productCenter.fabricSeries.nameEn', minWidth: 180, table: false, sectionKey: 'basic' },
      { prop: 'materialType', labelKey: 'productCenter.fabricSeries.materialType', form: false, table: false },
      { prop: 'defaultThicknessUnit', labelKey: 'productCenter.fabricSeries.defaultThicknessUnit', table: false, sectionKey: 'thickness', sectionLabelKey: 'productCenter.formSection.fabricCapability' },
      { prop: 'thicknessRuleEnabled', labelKey: 'productCenter.fabricSeries.thicknessRuleEnabled', type: 'boolean', table: false, sectionKey: 'thickness' },
      { prop: 'maxThicknessDiff', labelKey: 'productCenter.fabricSeries.maxThicknessDiff', type: 'number', table: false, sectionKey: 'thickness' },
      { prop: 'maxCombinedThickness', labelKey: 'productCenter.fabricSeries.maxCombinedThickness', type: 'number', sectionKey: 'thickness' },
      { prop: 'widthRuleEnabled', labelKey: 'productCenter.fabricSeries.widthRuleEnabled', type: 'boolean', table: false, sectionKey: 'width', sectionLabelKey: 'productCenter.formSection.widthCapability' },
      { prop: 'availableWidths', labelKey: 'productCenter.fabricSeries.availableWidths', minWidth: 180, sectionKey: 'width' },
      { prop: 'minWidthValue', labelKey: 'productCenter.fabricSeries.minWidthValue', type: 'number', table: false, sectionKey: 'width' },
      { prop: 'maxWidthValue', labelKey: 'productCenter.fabricSeries.maxWidthValue', type: 'number', table: false, sectionKey: 'width' },
      { prop: 'widthUnit', labelKey: 'productCenter.fabricSeries.widthUnit', sectionKey: 'width' },
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
