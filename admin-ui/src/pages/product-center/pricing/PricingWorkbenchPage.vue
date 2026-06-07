<template>
  <div class="app-container pricing-workbench">
    <section class="pricing-workbench__header">
      <div>
        <p>{{ t('productCenter.menu.title') }}</p>
        <h1>{{ t('productCenter.price.title') }}</h1>
        <span>{{ t('productCenter.price.workbenchHint') }}</span>
      </div>
      <el-space wrap>
        <el-button icon="Switch">{{ t('productCenter.price.versionCompare') }}</el-button>
        <el-button icon="DocumentCopy" @click="pasteOpen = true" v-hasPermi="['product:price:edit']">
          {{ t('productCenter.price.batchPaste') }}
        </el-button>
        <el-button type="primary" icon="Money" @click="runCalculate" v-hasPermi="['product:price:test']">
          {{ t('productCenter.price.calculate') }}
        </el-button>
      </el-space>
    </section>

    <div class="pricing-workbench__scheme">
      <el-form inline label-position="left">
        <el-form-item :label="t('productCenter.price.planCode')">
          <el-input :placeholder="t('productCenter.price.planCode')" />
        </el-form-item>
        <el-form-item :label="t('productCenter.model.code')">
          <el-input :placeholder="t('productCenter.model.code')" />
        </el-form-item>
        <el-form-item :label="t('productCenter.price.currencyCode')">
          <el-input v-model="calculationForm.currencyCode" />
        </el-form-item>
      </el-form>
    </div>

    <el-row :gutter="12" class="pricing-workbench__layout">
      <el-col :xs="24" :xl="6">
        <div class="pricing-workbench__rail">
          <div class="pricing-workbench__panel-head">
            <h2>{{ t('productCenter.price.planList') }}</h2>
            <el-tag type="success" effect="light">{{ t('productCenter.price.effective') }}</el-tag>
          </div>
          <product-entity-grid-page :config="pricePlanConfig" />
        </div>
      </el-col>
      <el-col :xs="24" :xl="12">
        <div class="pricing-workbench__editor">
          <div class="pricing-workbench__toolbar">
            <div>
              <h2>{{ activeEditorTitle }}</h2>
              <p>{{ t('productCenter.price.matrixHint') }}</p>
            </div>
            <el-space wrap>
              <el-button size="small" icon="Edit">{{ t('productCenter.price.batchAdjust') }}</el-button>
              <el-button size="small" icon="DocumentCopy" @click="pasteOpen = true">{{ t('productCenter.price.batchPaste') }}</el-button>
              <el-button size="small" type="primary" icon="Check">{{ t('common.save') }}</el-button>
            </el-space>
          </div>
          <el-tabs v-model="activeEditor" class="pricing-workbench__tabs">
            <el-tab-pane v-for="item in editorConfigs" :key="item.key" :label="t(item.titleKey)" :name="item.key">
              <product-entity-grid-page v-if="activeEditor === item.key" :config="item" />
            </el-tab-pane>
          </el-tabs>
          <formula-rule-summary />
        </div>
      </el-col>
      <el-col :xs="24" :xl="6">
        <price-tester-panel
          :form="calculationForm"
          :input-values-text="inputValuesText"
          :selected-options-text="selectedOptionsText"
          :result="calculation"
          @update:form="updateCalculationForm"
          @update:input-values-text="inputValuesText = $event"
          @update:selected-options-text="selectedOptionsText = $event"
          @calculate="runCalculate"
          @open-quote-preview="openQuotePreview"
        />
      </el-col>
    </el-row>

    <batch-paste-drawer
      v-model="pasteOpen"
      v-model:text="pasteText"
      :title="t('productCenter.price.batchPaste')"
      :tip="t('productCenter.price.batchPasteTip')"
      :rows="pasteRows"
      :columns="pasteColumns"
      @parse="parsePaste"
    />
  </div>
</template>

<script setup lang="ts" name="PricingWorkbenchPage">
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { calculatePrice, pricePlanApi, pricePlanVersionApi, priceRuleItemApi } from '@/api/product-capability/pricing'
import type { ProductRecord } from '@/api/product-capability/types'
import ProductEntityGridPage, { type ProductGridConfig } from '@/pages/product-center/components/ProductEntityGridPage.vue'
import PriceTesterPanel from '@/pages/product-center/components/PriceTesterPanel.vue'
import FormulaRuleSummary from '@/pages/product-center/components/FormulaRuleSummary.vue'
import BatchPasteDrawer from '@/pages/product-center/components/BatchPasteDrawer.vue'

const localeStore = useLocaleStore()
const router = useRouter()
const t = (key: string) => getMessage(key, localeStore.language)

const activeEditor = ref('ruleItem')
const pasteOpen = ref(false)
const pasteText = ref('')
const pasteRows = ref<ProductRecord[]>([])
const selectedOptionsText = ref('{}')
const inputValuesText = ref('{"width": 24, "height": 72}')
const calculation = ref<ProductRecord>({})
const calculationForm = reactive<{ pricePlanVersionId?: number; currencyCode?: string; quantity?: number }>({
  currencyCode: 'USD',
  quantity: 1
})

const pricePermissions = { add: 'product:price:edit', edit: 'product:price:edit', remove: 'product:price:edit', reference: 'product:price:list' }

const pricePlanConfig = computed<ProductGridConfig>(() => ({
  key: 'pricePlan',
  titleKey: 'productCenter.price.planList',
  descriptionKey: 'productCenter.price.description',
  idKey: 'pricePlanId',
  permissions: pricePermissions,
  api: pricePlanApi,
  fields: [
    { prop: 'pricePlanCode', labelKey: 'productCenter.price.planCode', search: true, required: true },
    { prop: 'pricePlanNameCn', labelKey: 'productCenter.price.planNameCn', search: true, required: true },
    { prop: 'pricePlanNameEn', labelKey: 'productCenter.price.planNameEn' },
    { prop: 'productModelCode', labelKey: 'productCenter.model.code', search: true },
    { prop: 'salesVariantCode', labelKey: 'productCenter.variant.code', search: true },
    { prop: 'currencyCode', labelKey: 'productCenter.price.currencyCode', search: true },
    { prop: 'pricingMode', labelKey: 'productCenter.price.pricingMode', search: true },
    { prop: 'currentVersionNo', labelKey: 'productCenter.template.currentVersion' },
    { prop: 'publishedVersionNo', labelKey: 'productCenter.template.publishedVersion' },
    { prop: 'bizStatus', labelKey: 'productCenter.template.bizStatus' },
    { prop: 'status', labelKey: 'productCenter.common.status', type: 'status', search: true },
    { prop: 'remark', labelKey: 'productCenter.common.remark', type: 'textarea', table: false }
  ]
}))

const editorConfigs = computed<ProductGridConfig[]>(() => [
  {
    key: 'version',
    titleKey: 'productCenter.price.version',
    descriptionKey: 'productCenter.price.versionDescription',
    idKey: 'pricePlanVersionId',
    permissions: pricePermissions,
    api: pricePlanVersionApi,
    fields: [
      { prop: 'pricePlanId', labelKey: 'productCenter.price.planId', type: 'number', required: true },
      { prop: 'pricePlanCode', labelKey: 'productCenter.price.planCode', search: true },
      { prop: 'versionNo', labelKey: 'productCenter.template.versionNo', search: true, required: true },
      { prop: 'versionName', labelKey: 'productCenter.template.versionName' },
      { prop: 'versionStatus', labelKey: 'productCenter.template.versionStatus', search: true },
      { prop: 'currencyCode', labelKey: 'productCenter.price.currencyCode', search: true },
      { prop: 'pricingMode', labelKey: 'productCenter.price.pricingMode', search: true },
      { prop: 'baseAmount', labelKey: 'productCenter.price.baseAmount', type: 'number' },
      { prop: 'priceSchemaJson', labelKey: 'productCenter.price.schemaJson', type: 'textarea', table: false },
      { prop: 'draftHash', labelKey: 'productCenter.template.draftHash' },
      { prop: 'remark', labelKey: 'productCenter.common.remark', type: 'textarea', table: false }
    ]
  },
  {
    key: 'ruleItem',
    titleKey: 'productCenter.price.ruleItem',
    descriptionKey: 'productCenter.price.ruleItemDescription',
    idKey: 'ruleItemId',
    permissions: pricePermissions,
    api: priceRuleItemApi,
    fields: [
      { prop: 'pricePlanVersionId', labelKey: 'productCenter.price.planVersionId', type: 'number', search: true, required: true },
      { prop: 'pricePlanCode', labelKey: 'productCenter.price.planCode', search: true },
      { prop: 'itemCode', labelKey: 'productCenter.price.itemCode', search: true, required: true },
      { prop: 'itemNameCn', labelKey: 'productCenter.price.itemNameCn', search: true, required: true },
      { prop: 'itemNameEn', labelKey: 'productCenter.price.itemNameEn' },
      { prop: 'itemType', labelKey: 'productCenter.price.itemType', search: true },
      { prop: 'matchJson', labelKey: 'productCenter.price.matchJson', type: 'textarea', table: false },
      { prop: 'formulaJson', labelKey: 'productCenter.price.formulaJson', type: 'textarea', table: false },
      { prop: 'baseAmount', labelKey: 'productCenter.price.baseAmount', type: 'number' },
      { prop: 'unitPrice', labelKey: 'productCenter.price.unitPrice', type: 'number' },
      { prop: 'currencyCode', labelKey: 'productCenter.price.currencyCode', search: true },
      { prop: 'priority', labelKey: 'productCenter.template.priority', type: 'number' },
      { prop: 'status', labelKey: 'productCenter.common.status', type: 'status', search: true }
    ]
  }
])

const activeEditorTitle = computed(() => {
  const current = editorConfigs.value.find((item) => item.key === activeEditor.value)
  return current ? t(current.titleKey) : t('productCenter.price.ruleItem')
})
const pasteColumns = computed(() => [
  { prop: 'itemCode', label: t('productCenter.price.itemCode'), minWidth: 140 },
  { prop: 'itemNameCn', label: t('productCenter.price.itemNameCn'), minWidth: 160 },
  { prop: 'itemType', label: t('productCenter.price.itemType'), minWidth: 130 },
  { prop: 'baseAmount', label: t('productCenter.price.baseAmount'), width: 120 },
  { prop: 'unitPrice', label: t('productCenter.price.unitPrice'), width: 120 }
])

function parseJson(text: string) {
  try {
    return JSON.parse(text || '{}') as Record<string, unknown>
  } catch {
    ElMessage.error(t('productCenter.template.invalidJson'))
    return {}
  }
}

async function runCalculate() {
  const response = await calculatePrice({
    pricePlanVersionId: calculationForm.pricePlanVersionId,
    currencyCode: calculationForm.currencyCode,
    quantity: calculationForm.quantity,
    selectedOptions: parseJson(selectedOptionsText.value),
    inputValues: parseJson(inputValuesText.value)
  })
  calculation.value = response.data || {}
}

function openQuotePreview() {
  router.push({
    path: '/product-config/quote-preview',
    query: {
      pricePlanVersionId: calculationForm.pricePlanVersionId ? String(calculationForm.pricePlanVersionId) : undefined,
      currencyCode: calculationForm.currencyCode || undefined,
      quantity: calculationForm.quantity ? String(calculationForm.quantity) : undefined
    }
  })
}

function updateCalculationForm(next: { pricePlanVersionId?: number; currencyCode?: string; quantity?: number }) {
  Object.assign(calculationForm, next)
}

function parsePaste() {
  pasteRows.value = pasteText.value
    .split(/\r?\n/)
    .flatMap((line): ProductRecord[] => {
      const [itemCode, itemNameCn, itemType, baseAmount, unitPrice] = line.split(/\t|,/).map((item) => item?.trim())
      return itemCode ? [{ itemCode, itemNameCn, itemType, baseAmount, unitPrice }] : []
    })
}
</script>

<style scoped lang="scss">
.pricing-workbench {
  display: flex;
  flex-direction: column;
  gap: 12px;
  color: #111827;
}

.pricing-workbench__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.04);
}

.pricing-workbench__header p {
  margin: 0 0 4px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.pricing-workbench__header h1 {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.pricing-workbench__header span,
.pricing-workbench__tester-hint {
  display: block;
  margin-top: 8px;
  color: #64748b;
  font-size: 13px;
}

.pricing-workbench__scheme,
.pricing-workbench__rail,
.pricing-workbench__editor,
.pricing-workbench__panel {
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  padding: 14px;
  background: var(--el-bg-color);
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.04);
}

.pricing-workbench__scheme {
  padding-bottom: 0;
}

.pricing-workbench__rail :deep(.product-grid-page) {
  border: 0;
  box-shadow: none;
  padding: 0;
}

.pricing-workbench__panel {
  min-height: 580px;
}

.pricing-workbench__panel-head,
.pricing-workbench__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.pricing-workbench__panel-head h2,
.pricing-workbench__toolbar h2 {
  margin: 0;
  font-size: 16px;
  font-weight: 650;
}

.pricing-workbench__toolbar {
  padding-bottom: 12px;
  border-bottom: 1px solid #eef2f7;

  p {
    margin: 4px 0 0;
    color: #64748b;
    font-size: 12px;
  }
}

.pricing-workbench__tabs {
  margin-top: 8px;
}

.pricing-workbench__formula {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-top: 12px;
  padding: 12px;
  border: 1px solid #e5efff;
  border-radius: 8px;
  background: #f8fbff;

  strong,
  span {
    display: block;
  }

  strong {
    font-size: 13px;
  }

  span {
    margin-top: 4px;
    color: #64748b;
    font-size: 12px;
  }
}

.pricing-workbench__full-button {
  width: 100%;
}

.pricing-workbench__result {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 14px;

  div {
    display: flex;
    justify-content: space-between;
    gap: 12px;
    padding: 9px 0;
    border-bottom: 1px solid #eef2f7;
  }

  span {
    color: #64748b;
    font-size: 12px;
  }

  strong {
    color: #111827;
    font-size: 13px;
  }

  .is-total strong {
    color: #2563eb;
    font-size: 22px;
  }
}

.pricing-workbench__paste {
  margin: 12px 0;
}

@media (max-width: 768px) {
  .pricing-workbench__header,
  .pricing-workbench__toolbar {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
