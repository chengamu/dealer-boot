<template>
  <div class="app-container product-center-page">
    <product-entity-grid-page v-if="singleMode && activeConfig" :config="activeConfig" />
    <el-tabs v-else v-model="activeTab" class="product-center-tabs">
      <el-tab-pane v-for="item in configs" :key="item.key" :label="t(item.titleKey)" :name="item.key">
        <product-entity-grid-page v-if="activeTab === item.key" :config="item" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts" name="ProductFabricPage">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { productUnitApi } from '@/api/product-capability/base'
import type { ProductUnitVO } from '@/api/product-capability/types'
import { fabricProfileApi, fabricSeriesApi } from '@/api/product-capability/fabric'
import ProductEntityGridPage, { type ProductGridConfig } from '@/pages/product-center/components/ProductEntityGridPage.vue'

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const route = useRoute()
const router = useRouter()
const activeTab = ref('series')
const unitList = ref<ProductUnitVO[]>([])

const routeTabMap: Record<string, string> = {
  fabrics: 'series',
  'fabric-series': 'series',
  'fabric-profiles': 'profile'
}

function unitOption(unit: ProductUnitVO) {
  const code = String(unit.unitCode || '')
  const name = localeStore.language === 'zh_CN' ? unit.unitNameCn : unit.unitNameEn || unit.unitNameCn
  return { value: code, label: `${code} ${name || ''}`.trim() }
}

function unitOptionsByCodes(codes: string[]) {
  const codeSet = new Set(codes)
  return unitList.value.filter((unit) => codeSet.has(String(unit.unitCode || '').toUpperCase())).map(unitOption)
}

const thicknessUnitOptions = computed(() => {
  return unitOptionsByCodes(['MM', 'CM', 'IN'])
})

const widthUnitOptions = computed(() => {
  return unitOptionsByCodes(['MM', 'CM', 'M', 'IN'])
})

const fabricUnitOptions = computed(() => {
  return unitList.value.map(unitOption)
})

async function loadUnits() {
  const response = await productUnitApi.options?.({ status: 'ENABLED' })
  unitList.value = Array.isArray(response) ? response : response?.data || []
}

onMounted(() => {
  loadUnits()
})

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
    attachments: { targetType: 'FABRIC_SERIES', targetCodeField: 'seriesCode', defaultUsageType: 'SWATCH' },
    rowActions: [
      {
        labelKey: 'productCenter.fabricSeries.manageProfiles',
        icon: 'List',
        permission: 'product:fabric:list',
        handler: async (row) => {
          await router.push({
            path: '/product-master/fabric-profiles',
            query: {
              seriesCode: String(row.seriesCode || '')
            }
          })
        }
      }
    ],
    fields: [
      { prop: 'seriesCode', labelKey: 'productCenter.fabricSeries.code', search: true, required: true, minWidth: 160 },
      { prop: 'seriesNameCn', labelKey: 'productCenter.fabricSeries.name', search: true, required: true, minWidth: 180 },
      { prop: 'seriesNameEn', labelKey: 'productCenter.fabricSeries.nameEn', minWidth: 180 },
      { prop: 'materialType', labelKey: 'productCenter.fabricSeries.materialType', form: false, table: false },
      { prop: 'defaultThicknessUnit', labelKey: 'productCenter.fabricSeries.defaultThicknessUnit', type: 'select', options: thicknessUnitOptions.value },
      { prop: 'thicknessRuleEnabled', labelKey: 'productCenter.fabricSeries.thicknessRuleEnabled', type: 'boolean' },
      { prop: 'maxThicknessDiff', labelKey: 'productCenter.fabricSeries.maxThicknessDiff', type: 'number' },
      { prop: 'maxCombinedThickness', labelKey: 'productCenter.fabricSeries.maxCombinedThickness', type: 'number' },
      { prop: 'widthRuleEnabled', labelKey: 'productCenter.fabricSeries.widthRuleEnabled', type: 'boolean' },
      { prop: 'availableWidths', labelKey: 'productCenter.fabricSeries.availableWidths', minWidth: 180 },
      { prop: 'minWidthValue', labelKey: 'productCenter.fabricSeries.minWidthValue', type: 'number' },
      { prop: 'maxWidthValue', labelKey: 'productCenter.fabricSeries.maxWidthValue', type: 'number' },
      { prop: 'widthUnit', labelKey: 'productCenter.fabricSeries.widthUnit', type: 'select', options: widthUnitOptions.value },
      { prop: 'status', labelKey: 'productCenter.common.status', type: 'status', search: true },
      { prop: 'remark', labelKey: 'productCenter.common.remark', type: 'textarea', table: false, formSpan: 2 }
    ]
  },
  {
    key: 'profile',
    titleKey: 'productCenter.fabricProfile.title',
    descriptionKey: 'productCenter.fabric.description',
    idKey: 'fabricId',
    permissions: {
      add: 'product:fabric:add',
      edit: 'product:fabric:edit',
      remove: 'product:fabric:remove',
      reference: 'product:fabric:reference'
    },
    api: fabricProfileApi,
    attachments: { targetType: 'FABRIC_PROFILE', targetCodeField: 'materialCode', defaultUsageType: 'SWATCH' },
    fields: [
      { prop: 'fabricCode', labelKey: 'productCenter.fabricProfile.fabricCode', form: false, table: false },
      { prop: 'materialCode', labelKey: 'productCenter.fabricProfile.fabricCode', search: true, required: true, minWidth: 160 },
      { prop: 'materialNameCn', labelKey: 'productCenter.fabricProfile.materialNameCn', search: true, minWidth: 180 },
      { prop: 'seriesCode', labelKey: 'productCenter.fabricProfile.seriesCode', search: true, required: true, minWidth: 160 },
      { prop: 'colorCode', labelKey: 'productCenter.fabricProfile.colorCode', search: true },
      { prop: 'colorName', labelKey: 'productCenter.fabricProfile.colorName', minWidth: 140 },
      { prop: 'materialComposition', labelKey: 'productCenter.fabricProfile.materialComposition', minWidth: 180 },
      { prop: 'textureType', labelKey: 'productCenter.fabricProfile.textureType' },
      { prop: 'finishType', labelKey: 'productCenter.fabricProfile.finishType' },
      { prop: 'factoryModel', labelKey: 'productCenter.fabricProfile.factoryModel', minWidth: 160 },
      { prop: 'sampleBookNo', labelKey: 'productCenter.fabricProfile.sampleBookNo', search: true, minWidth: 160 },
      { prop: 'vendorItemNo', labelKey: 'productCenter.fabricProfile.vendorItemNo', minWidth: 160 },
      { prop: 'supplierCode', labelKey: 'productCenter.fabricProfile.supplierCode', search: true, minWidth: 160 },
      { prop: 'supplierName', labelKey: 'productCenter.fabricProfile.supplierName', minWidth: 200 },
      { prop: 'widthValue', labelKey: 'productCenter.fabricProfile.widthValue', type: 'number' },
      { prop: 'widthUnit', labelKey: 'productCenter.fabricProfile.widthUnit', type: 'select', options: widthUnitOptions.value },
      { prop: 'thicknessValue', labelKey: 'productCenter.fabricProfile.thicknessValue', type: 'number' },
      { prop: 'thicknessUnit', labelKey: 'productCenter.fabricProfile.thicknessUnit', type: 'select', options: thicknessUnitOptions.value },
      { prop: 'gsmValue', labelKey: 'productCenter.fabricProfile.gsmValue', type: 'number' },
      { prop: 'purchaseUnitCode', labelKey: 'productCenter.fabricProfile.purchaseUnitCode', type: 'select', options: fabricUnitOptions.value },
      { prop: 'inventoryUnitCode', labelKey: 'productCenter.fabricProfile.inventoryUnitCode', type: 'select', options: fabricUnitOptions.value },
      { prop: 'salesUnitCode', labelKey: 'productCenter.fabricProfile.salesUnitCode', type: 'select', options: fabricUnitOptions.value },
      { prop: 'legacyAttributeText', labelKey: 'productCenter.fabricProfile.legacyAttributeText', type: 'textarea', table: false, formSpan: 2 },
      { prop: 'status', labelKey: 'productCenter.common.status', type: 'status', search: true },
      { prop: 'remark', labelKey: 'productCenter.common.remark', type: 'textarea', table: false, formSpan: 2 }
    ]
  }
])

const routeTab = computed(() => {
  const queryTab = typeof route.query.tab === 'string' ? route.query.tab : ''
  if (queryTab && configs.value.some((item) => item.key === queryTab)) return queryTab
  return routeTabMap[String(route.path).split('/').pop() || '']
})

const singleMode = computed(() => Boolean(routeTab.value))
const activeConfig = computed(() => configs.value.find((item) => item.key === activeTab.value))

watch(routeTab, (tab) => {
  if (tab) activeTab.value = tab
}, { immediate: true })
</script>

<style scoped lang="scss">
.product-center-page {
  :deep(.el-tabs__content) {
    overflow: visible;
  }
}
</style>
