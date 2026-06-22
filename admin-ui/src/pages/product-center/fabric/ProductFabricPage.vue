<template>
  <div class="app-container product-center-page">
    <product-entity-grid-page v-if="activeConfig" :config="activeConfig" />
  </div>
</template>

<script setup lang="ts" name="ProductFabricPage">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useLocaleStore } from '@/stores/locale'
import { productUnitApi } from '@/api/product-capability/base'
import { fabricSeriesApi } from '@/api/product-capability/fabric'
import type { ProductUnitVO } from '@/api/product-capability/types'
import ProductEntityGridPage, { type ProductGridConfig } from '@/pages/product-center/components/ProductEntityGridPage.vue'

const router = useRouter()
const localeStore = useLocaleStore()
const unitList = ref<ProductUnitVO[]>([])

function unitOption(unit: ProductUnitVO) {
  const code = String(unit.unitCode || '')
  const name = localeStore.language === 'zh_CN' ? unit.unitNameCn : unit.unitNameEn || unit.unitNameCn
  return { value: code, label: `${code} ${name || ''}`.trim() }
}

const unitOptions = computed(() => unitList.value.map(unitOption))

async function loadUnits() {
  const response = await productUnitApi.options?.({ status: 'ENABLED', pageNum: 1, pageSize: 500 })
  unitList.value = Array.isArray(response) ? response : response?.data || []
}

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
      { prop: 'defaultThicknessUnit', labelKey: 'productCenter.fabricSeries.defaultThicknessUnit', type: 'select', options: unitOptions.value, table: false, sectionKey: 'thickness', sectionLabelKey: 'productCenter.formSection.fabricCapability' },
      { prop: 'thicknessRuleEnabled', labelKey: 'productCenter.fabricSeries.thicknessRuleEnabled', type: 'boolean', table: false, sectionKey: 'thickness' },
      { prop: 'maxThicknessDiff', labelKey: 'productCenter.fabricSeries.maxThicknessDiff', type: 'number', table: false, sectionKey: 'thickness' },
      { prop: 'maxCombinedThickness', labelKey: 'productCenter.fabricSeries.maxCombinedThickness', type: 'number', sectionKey: 'thickness' },
      { prop: 'widthRuleEnabled', labelKey: 'productCenter.fabricSeries.widthRuleEnabled', type: 'boolean', table: false, sectionKey: 'width', sectionLabelKey: 'productCenter.formSection.widthCapability' },
      { prop: 'availableWidths', labelKey: 'productCenter.fabricSeries.availableWidths', minWidth: 180, sectionKey: 'width' },
      { prop: 'minWidthValue', labelKey: 'productCenter.fabricSeries.minWidthValue', type: 'number', table: false, sectionKey: 'width' },
      { prop: 'maxWidthValue', labelKey: 'productCenter.fabricSeries.maxWidthValue', type: 'number', table: false, sectionKey: 'width' },
      { prop: 'widthUnit', labelKey: 'productCenter.fabricSeries.widthUnit', type: 'select', options: unitOptions.value, sectionKey: 'width' },
      { prop: 'status', labelKey: 'productCenter.common.status', type: 'status', search: true },
      { prop: 'remark', labelKey: 'productCenter.common.remark', type: 'textarea', table: false, formSpan: 2, sectionKey: 'note', sectionLabelKey: 'productCenter.formSection.note' }
    ]
  }
])

const activeConfig = computed(() => configs.value[0])

onMounted(async () => {
  await loadUnits()
})
</script>

<style scoped lang="scss">
.product-center-page {
  :deep(.product-grid-page) {
    min-height: 100%;
  }
}
</style>
