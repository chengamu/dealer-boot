<template>
  <div class="app-container config-evaluator-page">
    <section class="config-evaluator-page__header">
      <div>
        <p>{{ t('productCenter.menu.configPricing') }}</p>
        <h1>{{ t('productCenter.template.evaluate') }}</h1>
      </div>
      <el-button type="primary" icon="VideoPlay" :loading="loading" @click="runEvaluate" v-hasPermi="['product:template:test']">
        {{ t('productCenter.template.evaluate') }}
      </el-button>
    </section>

    <section class="config-evaluator-page__controls">
      <div class="config-evaluator-page__control is-product">
        <span>{{ t('productCenter.salesProduct.title') }}</span>
        <el-select v-model="form.salesProductId" filterable clearable :placeholder="t('productCenter.common.selectPlaceholder')">
          <el-option v-for="item in salesProducts" :key="item.salesProductId" :label="productLabel(item)" :value="item.salesProductId" />
        </el-select>
      </div>
      <div class="config-evaluator-page__control">
        <span>{{ t('productCenter.template.currentTemplateVersion') }}</span>
        <strong>{{ templateVersionLabel }}</strong>
      </div>
      <div class="config-evaluator-page__control">
        <span>{{ t('productCenter.template.width') }}</span>
        <el-input-number v-model="form.width" :min="0" controls-position="right" />
      </div>
      <div class="config-evaluator-page__control">
        <span>{{ t('productCenter.template.height') }}</span>
        <el-input-number v-model="form.height" :min="0" controls-position="right" />
      </div>
      <div class="config-evaluator-page__control">
        <span>{{ t('productCenter.salesProduct.dimensionUnit') }}</span>
        <strong>{{ selectedProduct?.dimensionUnit || '-' }}</strong>
      </div>
    </section>

    <section class="config-evaluator-page__layout">
      <div class="config-evaluator-page__panel">
        <div class="config-evaluator-page__section-title">{{ t('productCenter.template.answerQuestions') }}</div>
        <el-table :data="questions" border height="100%" class="config-evaluator-page__table">
          <el-table-column type="index" :label="t('common.index')" width="64" align="center" />
          <el-table-column prop="questionCode" :label="t('productCenter.template.questionCode')" min-width="160" show-overflow-tooltip />
          <el-table-column prop="questionNameCn" :label="t('productCenter.template.questionNameCn')" min-width="180" show-overflow-tooltip />
          <el-table-column :label="t('productCenter.template.selectedOptions')" min-width="220">
            <template #default="{ row }">
              <el-select v-model="selectedOptions[row.questionCode]" filterable clearable :placeholder="t('productCenter.common.selectPlaceholder')" style="width: 100%">
                <el-option v-for="item in optionRows(row.questionId)" :key="item.optionCode" :label="optionLabel(item)" :value="item.optionCode" />
              </el-select>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="config-evaluator-page__panel">
        <div class="config-evaluator-page__section-title">{{ t('productCenter.template.evaluationResult') }}</div>
        <div class="config-evaluator-page__summary">
          <div>
            <span>{{ t('productCenter.template.resultStatus') }}</span>
            <el-tag :type="resultTagType">{{ result.resultStatus || '-' }}</el-tag>
          </div>
          <div>
            <span>{{ t('productCenter.template.availableOptions') }}</span>
            <strong>{{ result.availableOptions?.length || 0 }}</strong>
          </div>
          <div>
            <span>{{ t('productCenter.template.disabledOptions') }}</span>
            <strong>{{ result.disabledOptions?.length || 0 }}</strong>
          </div>
          <div>
            <span>{{ t('productCenter.template.validations') }}</span>
            <strong>{{ result.validations?.length || 0 }}</strong>
          </div>
        </div>

        <el-tabs class="config-evaluator-page__tabs" type="border-card">
          <el-tab-pane :label="t('productCenter.template.disabledOptions')">
            <el-table :data="result.disabledOptions || []" border>
              <el-table-column prop="questionCode" :label="t('productCenter.template.questionCode')" min-width="140" show-overflow-tooltip />
              <el-table-column prop="optionCode" :label="t('productCenter.template.optionCode')" min-width="140" show-overflow-tooltip />
              <el-table-column :label="t('productCenter.template.disabledReason')" min-width="220" show-overflow-tooltip>
                <template #default="{ row }">{{ messageLabel(row.disabledReason) }}</template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
          <el-tab-pane :label="t('productCenter.template.validations')">
            <el-table :data="messageRows" border>
              <el-table-column prop="code" :label="t('productCenter.template.messageCode')" min-width="180" show-overflow-tooltip />
              <el-table-column :label="t('productCenter.template.message')" min-width="260" show-overflow-tooltip>
                <template #default="{ row }">{{ messageLabel(row.message) }}</template>
              </el-table-column>
              <el-table-column prop="targetType" :label="t('productCenter.template.targetType')" width="140" />
            </el-table>
          </el-tab-pane>
          <el-tab-pane :label="t('productCenter.template.autoComponents')">
            <el-table :data="result.autoComponents || []" border>
              <el-table-column type="index" :label="t('common.index')" width="64" align="center" />
              <el-table-column prop="componentCode" :label="t('productCenter.component.code')" min-width="180" show-overflow-tooltip />
              <el-table-column prop="optionCode" :label="t('productCenter.template.optionCode')" min-width="140" show-overflow-tooltip />
              <el-table-column prop="qty" :label="t('productCenter.common.quantity')" width="120" />
            </el-table>
          </el-tab-pane>
          <el-tab-pane :label="t('productCenter.template.mediaAssets')">
            <el-table :data="result.mediaAssets || []" border>
              <el-table-column type="index" :label="t('common.index')" width="64" align="center" />
              <el-table-column prop="assetCode" :label="t('productCenter.asset.code')" min-width="180" show-overflow-tooltip />
              <el-table-column prop="usageType" :label="t('productCenter.asset.usageType')" min-width="140" show-overflow-tooltip />
              <el-table-column prop="optionCode" :label="t('productCenter.template.optionCode')" min-width="140" show-overflow-tooltip />
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts" name="ConfigEvaluatorPage">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { configOptionApi, configQuestionApi, evaluateConfig, salesProductApi } from '@/api/product-capability/config'
import type { ConfigEvaluationResultVO, ConfigOptionVO, ConfigQuestionVO, SalesProductVO } from '@/api/product-capability/types'

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)

const loading = ref(false)
const salesProducts = ref<SalesProductVO[]>([])
const questions = ref<ConfigQuestionVO[]>([])
const options = ref<ConfigOptionVO[]>([])
const result = ref<ConfigEvaluationResultVO>({})
const selectedOptions = reactive<Record<string, string | number>>({})
const form = reactive({
  salesProductId: undefined as number | undefined,
  templateVersionId: undefined as number | undefined,
  width: 120,
  height: 160
})

const resultTagType = computed(() => {
  if (result.value.resultStatus === 'BLOCKER') return 'danger'
  if (result.value.resultStatus === 'WARNING') return 'warning'
  return 'success'
})
const selectedProduct = computed(() => salesProducts.value.find((item) => item.salesProductId === form.salesProductId))
const templateVersionLabel = computed(() => {
  const product = selectedProduct.value
  if (!product?.templateVersionId) return '-'
  return `${product.templateCode || ''} / ${product.templateVersionNo || product.templateVersionId}`.trim()
})
const messageRows = computed(() => [...(result.value.validations || []), ...(result.value.warnings || []), ...(result.value.blockers || [])])

function productLabel(item: SalesProductVO) {
  return `${item.salesProductCode || ''} ${item.salesProductNameCn || ''}`.trim()
}

function optionLabel(item: ConfigOptionVO) {
  return `${item.optionCode || ''} ${item.optionNameCn || ''}`.trim()
}

function optionRows(questionId?: number) {
  return options.value.filter((item) => item.questionId === questionId)
}

function messageLabel(value?: string) {
  if (!value) return '-'
  const [key, suffix] = value.split(':')
  const message = t(key)
  return suffix && message !== key ? `${message}: ${suffix}` : message
}

async function loadSalesProducts() {
  const response = await salesProductApi.options?.({ pageNum: 1, pageSize: 100, status: 'ENABLED' })
  salesProducts.value = Array.isArray(response) ? response : response?.data || []
  if (!form.salesProductId && salesProducts.value[0]) {
    form.salesProductId = salesProducts.value[0].salesProductId
    form.templateVersionId = salesProducts.value[0].templateVersionId
    form.width = Number(salesProducts.value[0].defaultWidth || form.width)
    form.height = Number(salesProducts.value[0].defaultHeight || form.height)
  }
}

async function loadTemplateData() {
  if (!form.templateVersionId) return
  const [questionResponse, optionResponse] = await Promise.all([
    configQuestionApi.options?.({ pageNum: 1, pageSize: 200, templateVersionId: form.templateVersionId, status: 'ENABLED' }),
    configOptionApi.options?.({ pageNum: 1, pageSize: 500, templateVersionId: form.templateVersionId, status: 'ENABLED' })
  ])
  questions.value = Array.isArray(questionResponse) ? questionResponse : questionResponse?.data || []
  options.value = Array.isArray(optionResponse) ? optionResponse : optionResponse?.data || []
}

async function runEvaluate() {
  if (!form.templateVersionId) {
    ElMessage.warning(t('productCenter.template.templateVersionRequired'))
    return
  }
  loading.value = true
  try {
    const response = await evaluateConfig({
      salesProductId: form.salesProductId,
      templateVersionId: form.templateVersionId,
      width: form.width,
      height: form.height,
      selectedOptions: { ...selectedOptions },
      inputValues: { width: form.width, height: form.height }
    })
    result.value = response.data || {}
  } finally {
    loading.value = false
  }
}

watch(
  () => form.salesProductId,
  () => {
    const product = salesProducts.value.find((item) => item.salesProductId === form.salesProductId)
    if (product?.templateVersionId) {
      form.templateVersionId = product.templateVersionId
      form.width = Number(product.defaultWidth || form.width)
      form.height = Number(product.defaultHeight || form.height)
    }
  }
)

watch(
  () => form.templateVersionId,
  () => {
    Object.keys(selectedOptions).forEach((key) => delete selectedOptions[key])
    loadTemplateData()
  }
)

onMounted(async () => {
  await loadSalesProducts()
  await loadTemplateData()
})
</script>

<style scoped lang="scss">
.config-evaluator-page {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-height: 860px;
  padding: 8px;
  background: linear-gradient(180deg, #f7fafc 0%, #f3f6f8 100%);
}

.config-evaluator-page__header,
.config-evaluator-page__controls,
.config-evaluator-page__panel {
  border: 1px solid #dbe3ea;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.04);
}

.config-evaluator-page__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  border-left: 4px solid #2563eb;

  p,
  h1 {
    margin: 0;
  }

  p {
    color: #2563eb;
    font-size: 12px;
    font-weight: 800;
  }

  h1 {
    margin-top: 3px;
    color: #111827;
    font-size: 20px;
    line-height: 1.2;
  }
}

.config-evaluator-page__controls {
  display: grid;
  grid-template-columns: minmax(300px, 1.35fr) minmax(180px, 0.9fr) repeat(3, minmax(140px, 0.58fr));
  gap: 6px;
  padding: 10px;
}

.config-evaluator-page__control {
  display: grid;
  align-content: center;
  min-width: 0;
  min-height: 58px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #fbfdff;
  padding: 7px 9px;

  span {
    margin-bottom: 4px;
    color: #64748b;
    font-size: 12px;
    line-height: 1.25;
  }

  strong {
    overflow: hidden;
    color: #111827;
    font-size: 13px;
    line-height: 1.35;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  :deep(.el-select),
  :deep(.el-input-number) {
    width: 100%;
  }
}

.config-evaluator-page__layout {
  display: grid;
  grid-template-columns: minmax(420px, 0.9fr) minmax(0, 1.1fr);
  flex: 1 1 auto;
  gap: 8px;
  min-height: 680px;
}

.config-evaluator-page__panel {
  display: flex;
  flex-direction: column;
  min-width: 0;
  min-height: 0;
  padding: 12px;
}

.config-evaluator-page__section-title {
  flex: 0 0 auto;
  margin: 0 0 8px;
  color: var(--el-text-color-primary);
  font-size: 16px;
  font-weight: 700;
}

.config-evaluator-page__table {
  flex: 1 1 auto;
  min-height: 0;
}

.config-evaluator-page__summary {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  flex: 0 0 auto;
  gap: 6px;
  margin-bottom: 8px;

  div {
    min-width: 0;
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    background: #fbfdff;
    padding: 7px 9px;
  }

  span,
  strong {
    display: block;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  span {
    color: #64748b;
    font-size: 12px;
    line-height: 1.25;
  }

  strong {
    margin-top: 4px;
    color: #111827;
    font-size: 14px;
  }
}

.config-evaluator-page__tabs {
  flex: 1 1 auto;
  min-height: 0;

  :deep(.el-tabs__content) {
    height: calc(100% - 41px);
    overflow: auto;
  }
}

@media (max-width: 1200px) {
  .config-evaluator-page__controls,
  .config-evaluator-page__layout {
    grid-template-columns: 1fr;
  }

  .config-evaluator-page__summary {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
