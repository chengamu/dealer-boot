<template>
  <div class="app-container config-entry">
    <section class="config-entry__topbar">
      <div class="config-entry__title">
        <p>{{ t('productCenter.menu.configPricing') }}</p>
        <h1>{{ t('productCenter.template.entryWorkbench') }}</h1>
        <span>{{ t('productCenter.template.entryWorkbenchHint') }}</span>
      </div>
      <el-space class="config-entry__actions" wrap>
        <span class="config-entry__product-pill">
          <span>{{ t('productCenter.template.currentProduct') }}</span>
          <strong>{{ selectedProductLabel }}</strong>
        </span>
        <el-button type="primary" icon="VideoPlay" :loading="evaluating" @click="runEvaluate" v-hasPermi="['product:template:test']">
          {{ t('productCenter.template.evaluate') }}
        </el-button>
      </el-space>
    </section>

    <section class="config-entry__layout">
      <aside class="config-entry__products">
        <div class="config-entry__pane-head">
          <h2>{{ t('productCenter.salesProduct.title') }}</h2>
          <p>{{ t('productCenter.template.productListHint') }}</p>
          <el-input v-model="productKeyword" clearable :placeholder="t('productCenter.template.productSearchPlaceholder')" prefix-icon="Search" />
        </div>
        <div class="config-entry__product-list">
          <button
            v-for="(item, index) in filteredProducts"
            :key="item.salesProductId"
            class="config-entry__product-card"
            :class="{ 'is-active': item.salesProductId === selectedProductId }"
            type="button"
            @click="selectProduct(item)"
          >
            <span class="config-entry__index">{{ String(index + 1).padStart(2, '0') }}</span>
            <span class="config-entry__product-body">
              <span class="config-entry__product-line">
                <strong>{{ item.salesProductNameCn || item.salesProductCode }}</strong>
                <span class="config-entry__type-chip">{{ optionText(productTypeOptions, item.productType) }}</span>
              </span>
              <small>{{ item.salesProductCode }}</small>
              <em>{{ item.salesProductNameEn || item.categoryNameCn || '-' }}</em>
            </span>
          </button>
        </div>
      </aside>

      <main class="config-entry__main">
        <section class="config-entry__summary">
          <div class="config-entry__summary-title">
            <p>{{ t('productCenter.template.stepProduct') }}</p>
            <h2>{{ selectedProduct?.salesProductNameCn || '-' }}</h2>
            <span>{{ selectedProduct?.salesProductNameEn || selectedProduct?.salesProductCode || '-' }}</span>
          </div>
          <div class="config-entry__facts">
            <div class="config-entry__fact">
              <span>{{ t('productCenter.salesProduct.category') }}</span>
              <strong>{{ selectedProduct?.categoryNameCn || selectedProduct?.categoryCode || '-' }}</strong>
            </div>
            <div class="config-entry__fact">
              <span>{{ t('productCenter.salesProduct.productType') }}</span>
              <strong>{{ optionText(productTypeOptions, selectedProduct?.productType) }}</strong>
            </div>
            <div class="config-entry__fact">
              <span>{{ t('productCenter.template.currentTemplateVersion') }}</span>
              <strong>{{ templateVersionLabel }}</strong>
            </div>
            <div class="config-entry__fact">
              <span>{{ t('productCenter.template.question') }}</span>
              <strong>{{ questionRows.length }} / {{ optionRows.length }} {{ t('productCenter.template.answerCount') }}</strong>
            </div>
            <div class="config-entry__fact">
              <span>{{ t('productCenter.template.requiredFlag') }}</span>
              <strong>{{ requiredQuestionCount }}</strong>
            </div>
            <div class="config-entry__fact">
              <span>{{ t('productCenter.template.bizStatus') }}</span>
              <strong>{{ optionText(bizStatusOptions, selectedProduct?.bizStatus) }}</strong>
            </div>
          </div>
        </section>

        <section class="config-entry__workbench">
          <div class="config-entry__question-list">
            <div class="config-entry__section-head">
              <div>
                <p>{{ t('productCenter.template.stepQuestions') }}</p>
                <h2>{{ t('productCenter.template.questionEntry') }}</h2>
              </div>
              <el-button type="primary" icon="Plus" @click="startAddQuestion" v-hasPermi="['product:template:edit']">
                {{ t('productCenter.template.addQuestion') }}
              </el-button>
            </div>

            <div
              v-for="(question, index) in questionRows"
              :key="question.questionId"
              class="config-entry__question-card"
              :class="{ 'is-active': question.questionId === selectedQuestionId }"
              role="button"
              tabindex="0"
              @click="selectQuestion(question)"
            >
              <span class="config-entry__index">{{ index + 1 }}</span>
              <span class="config-entry__question-content">
                <span class="config-entry__question-headline">
                  <strong>{{ question.questionNameCn || question.questionCode }}</strong>
                </span>
                <span class="config-entry__badges">
                  <el-tag v-if="question.requiredFlag === '1'" type="danger" effect="light">{{ t('productCenter.template.requiredFlag') }}</el-tag>
                  <el-tag type="success" effect="light">{{ optionCount(question.questionId) }} {{ t('productCenter.template.answerCount') }}</el-tag>
                </span>
                <span class="config-entry__question-copy">
                  <small>{{ question.questionNameEn || question.questionCode }}</small>
                  <em>{{ question.questionCode }} · {{ t('productCenter.template.sortOrderShort') }} {{ question.sortOrder ?? '-' }}</em>
                </span>
              </span>
              <span class="config-entry__question-actions" @click.stop>
                <el-button link type="primary" icon="Edit" @click="editQuestionRecord(question)" v-hasPermi="['product:template:edit']" />
                <el-button link type="danger" icon="Delete" @click="deleteQuestionRecord(question)" v-hasPermi="['product:template:edit']" />
              </span>
            </div>
            <el-empty v-if="!questionRows.length" :description="t('productCenter.template.noQuestions')" />
          </div>

          <div class="config-entry__editor">
            <div class="config-entry__section-head config-entry__editor-head">
              <div>
                <p>{{ t('productCenter.template.stepAnswers') }}</p>
                <h2>{{ selectedQuestion?.questionNameCn || t('productCenter.template.questionEntry') }}</h2>
              </div>
              <el-space wrap>
                <el-button type="primary" icon="Plus" @click="startAddOption" :disabled="!selectedQuestionId" v-hasPermi="['product:template:edit']">
                  {{ t('productCenter.template.addAnswer') }}
                </el-button>
              </el-space>
            </div>

            <div class="config-entry__question-detail">
              <div class="config-entry__question-meta">
                <div v-for="item in questionFacts" :key="item.key" class="config-entry__detail-item">
                  <span>{{ item.label }}</span>
                  <strong>{{ item.value }}</strong>
                </div>
              </div>
              <div class="config-entry__usage">
                <span>{{ t('productCenter.template.businessUsage') }}</span>
                <p>{{ selectedQuestion?.remark || '-' }}</p>
              </div>
            </div>

            <div class="config-entry__evaluation-strip">
              <strong>{{ t('productCenter.template.evaluationResult') }}</strong>
              <el-tag :type="resultTagType">{{ evaluation.resultStatus || '-' }}</el-tag>
              <em>{{ t('productCenter.template.availableOptions') }} {{ evaluation.availableOptions?.length || 0 }}</em>
              <em>{{ t('productCenter.template.disabledOptions') }} {{ evaluation.disabledOptions?.length || 0 }}</em>
              <em>{{ t('productCenter.template.autoComponents') }} {{ evaluation.autoComponents?.length || 0 }}</em>
            </div>

            <div class="config-entry__table-box">
              <el-table :data="currentQuestionOptions" border height="100%" :fit="true">
                <el-table-column type="index" :label="t('common.index')" width="46" align="center" />
                <el-table-column prop="optionNameEn" :label="t('productCenter.template.optionNameEn')" min-width="128" show-overflow-tooltip />
                <el-table-column prop="optionNameCn" :label="t('productCenter.template.optionNameCn')" min-width="132" show-overflow-tooltip />
                <el-table-column :label="t('productCenter.template.sourceType')" width="86" show-overflow-tooltip>
                  <template #default="{ row }">{{ optionText(sourceTypeOptions, row.sourceType) }}</template>
                </el-table-column>
                <el-table-column :label="t('common.operate')" width="88" align="center" class-name="config-entry__option-actions">
                  <template #default="{ row }">
                    <el-button link type="primary" icon="Edit" @click="editOption(row)" v-hasPermi="['product:template:edit']" />
                    <el-button link type="danger" icon="Delete" @click="deleteOption(row)" v-hasPermi="['product:template:edit']" />
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </section>
      </main>
    </section>

    <el-dialog v-model="questionDialogOpen" :title="questionForm.questionId ? t('productCenter.template.editQuestion') : t('productCenter.template.addQuestion')" width="880px">
      <el-form :model="questionForm" label-position="top" class="config-entry__form">
        <el-form-item :label="t('productCenter.template.templateVersion')">
          <el-input :model-value="templateVersionLabel" disabled />
        </el-form-item>
        <el-form-item :label="t('productCenter.template.questionGroup')">
          <el-select v-model="questionForm.questionGroupId" filterable clearable>
            <el-option v-for="item in questionGroupOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('productCenter.template.questionCode')">
          <el-input v-model="questionForm.questionCode" />
        </el-form-item>
        <el-form-item :label="t('productCenter.common.sortOrder')">
          <el-input-number v-model="questionForm.sortOrder" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item :label="t('productCenter.template.questionNameCn')">
          <el-input v-model="questionForm.questionNameCn" />
        </el-form-item>
        <el-form-item :label="t('productCenter.template.questionNameEn')">
          <el-input v-model="questionForm.questionNameEn" />
        </el-form-item>
        <el-form-item :label="t('productCenter.template.inputType')">
          <el-select v-model="questionForm.inputType">
            <el-option v-for="item in inputTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('productCenter.template.requiredFlag')">
          <el-select v-model="questionForm.requiredFlag">
            <el-option v-for="item in flagOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('productCenter.template.customerVisible')">
          <el-select v-model="questionForm.customerVisible">
            <el-option v-for="item in flagOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('productCenter.template.defaultValue')">
          <el-input v-model="questionForm.defaultValue" />
        </el-form-item>
        <el-form-item class="is-wide" :label="t('productCenter.template.businessUsage')">
          <el-input v-model="questionForm.remark" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="questionDialogOpen = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="savingQuestion" @click="saveQuestion" v-hasPermi="['product:template:edit']">{{ t('common.save') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="optionDialogOpen" :title="optionForm.optionId ? t('productCenter.template.editAnswer') : t('productCenter.template.addAnswer')" width="720px">
      <el-form :model="optionForm" label-position="top" class="config-entry__form">
        <el-form-item :label="t('productCenter.template.optionCode')">
          <el-input v-model="optionForm.optionCode" />
        </el-form-item>
        <el-form-item :label="t('productCenter.template.optionValue')">
          <el-input v-model="optionForm.optionValue" />
        </el-form-item>
        <el-form-item :label="t('productCenter.template.optionNameCn')">
          <el-input v-model="optionForm.optionNameCn" />
        </el-form-item>
        <el-form-item :label="t('productCenter.template.optionNameEn')">
          <el-input v-model="optionForm.optionNameEn" />
        </el-form-item>
        <el-form-item :label="t('productCenter.template.sourceType')">
          <el-select v-model="optionForm.sourceType">
            <el-option v-for="item in sourceTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('productCenter.template.sourceCode')">
          <el-input v-model="optionForm.sourceCode" />
        </el-form-item>
        <el-form-item :label="t('productCenter.template.sourceName')">
          <el-input v-model="optionForm.sourceName" />
        </el-form-item>
        <el-form-item :label="t('productCenter.common.sortOrder')">
          <el-input-number v-model="optionForm.sortOrder" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item class="is-wide" :label="t('productCenter.template.componentJson')">
          <el-input v-model="optionForm.componentJson" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item class="is-wide" :label="t('productCenter.template.mediaJson')">
          <el-input v-model="optionForm.mediaJson" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="optionDialogOpen = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="savingOption" @click="saveOption" v-hasPermi="['product:template:edit']">{{ t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="ConfigTemplateWorkbenchPage">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { configOptionApi, configQuestionApi, evaluateConfig, questionGroupApi, salesProductApi } from '@/api/product-capability/config'
import type { ConfigEvaluationResultVO, ConfigOptionVO, ConfigQuestionVO, ProductOption, ProductRecord, QuestionGroupVO, SalesProductVO } from '@/api/product-capability/types'

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)

const loading = ref(false)
const evaluating = ref(false)
const savingQuestion = ref(false)
const savingOption = ref(false)
const productKeyword = ref('')
const selectedProductId = ref<number>()
const selectedQuestionId = ref<number>()
const salesProducts = ref<SalesProductVO[]>([])
const questionRows = ref<ConfigQuestionVO[]>([])
const optionRows = ref<ConfigOptionVO[]>([])
const questionGroups = ref<QuestionGroupVO[]>([])
const evaluation = ref<ConfigEvaluationResultVO>({})
const questionDialogOpen = ref(false)
const optionDialogOpen = ref(false)

const questionForm = reactive<ConfigQuestionVO>({
  inputType: 'SINGLE_SELECT',
  requiredFlag: '1',
  customerVisible: '1',
  status: 'ENABLED',
  sortOrder: 10
})
const optionForm = reactive<ConfigOptionVO>({
  sourceType: 'MANUAL',
  status: 'ENABLED',
  sortOrder: 10
})

const productTypeOptions = computed<ProductOption[]>(() => [
  { label: t('productCenter.salesProduct.productTypeRollerShade'), value: 'ROLLER_SHADE' },
  { label: t('productCenter.salesProduct.productTypeOutdoorShade'), value: 'OUTDOOR_SHADE' },
  { label: t('productCenter.salesProduct.productTypeZebraShade'), value: 'ZEBRA_SHADE' },
  { label: t('productCenter.salesProduct.productTypeCurtainTrack'), value: 'CURTAIN_TRACK' }
])
const bizStatusOptions = computed<ProductOption[]>(() => [
  { label: t('productCenter.bizStatus.draft'), value: 'DRAFT' },
  { label: t('productCenter.bizStatus.ready'), value: 'READY' },
  { label: t('productCenter.bizStatus.published'), value: 'PUBLISHED' },
  { label: t('productCenter.bizStatus.archived'), value: 'ARCHIVED' }
])
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
const sourceTypeOptions = computed<ProductOption[]>(() => [
  { label: t('productCenter.sourceType.manual'), value: 'MANUAL' },
  { label: t('productCenter.sourceType.baseAttribute'), value: 'BASE_ATTRIBUTE' },
  { label: t('productCenter.sourceType.material'), value: 'MATERIAL' },
  { label: t('productCenter.sourceType.fabricProfile'), value: 'FABRIC_PROFILE' },
  { label: t('productCenter.sourceType.fabricSeries'), value: 'FABRIC_SERIES' },
  { label: t('productCenter.sourceType.component'), value: 'COMPONENT' },
  { label: t('productCenter.sourceType.mediaAsset'), value: 'MEDIA_ASSET' }
])
const questionGroupOptions = computed<ProductOption[]>(() =>
  questionGroups.value.map((item) => ({
    label: `${item.groupCode || ''} ${item.groupNameCn || item.groupNameEn || ''}`.trim(),
    value: item.questionGroupId || 0
  }))
)
const filteredProducts = computed(() => {
  const keyword = productKeyword.value.trim().toLowerCase()
  if (!keyword) return salesProducts.value
  return salesProducts.value.filter((item) => [item.salesProductCode, item.salesProductNameCn, item.salesProductNameEn, item.categoryNameCn, item.productType, item.salesMode].join(' ').toLowerCase().includes(keyword))
})
const selectedProduct = computed(() => salesProducts.value.find((item) => item.salesProductId === selectedProductId.value))
const selectedQuestion = computed(() => questionRows.value.find((item) => item.questionId === selectedQuestionId.value))
const selectedProductLabel = computed(() => `${selectedProduct.value?.salesProductCode || ''} ${selectedProduct.value?.salesProductNameCn || ''}`.trim() || '-')
const templateVersionLabel = computed(() => {
  const product = selectedProduct.value
  return product?.templateVersionId ? `${product.templateCode || ''} / ${product.templateVersionNo || product.templateVersionId}`.trim() : '-'
})
const currentQuestionOptions = computed(() => optionRows.value.filter((item) => item.questionId === selectedQuestionId.value))
const requiredQuestionCount = computed(() => questionRows.value.filter((item) => item.requiredFlag === '1').length)
const questionFacts = computed(() => [
  { key: 'questionCode', label: t('productCenter.template.questionCode'), value: selectedQuestion.value?.questionCode || '-' },
  { key: 'inputType', label: t('productCenter.template.inputType'), value: optionText(inputTypeOptions.value, selectedQuestion.value?.inputType) },
  { key: 'questionGroup', label: t('productCenter.template.questionGroup'), value: questionGroupLabel(selectedQuestion.value?.questionGroupId) },
  { key: 'sortOrder', label: t('productCenter.common.sortOrder'), value: selectedQuestion.value?.sortOrder ?? '-' }
])
const resultTagType = computed(() => {
  if (evaluation.value.resultStatus === 'BLOCKER') return 'danger'
  if (evaluation.value.resultStatus === 'WARNING') return 'warning'
  return 'success'
})

function optionText(options: ProductOption[], value?: unknown) {
  return options.find((item) => item.value === value)?.label || String(value ?? '-')
}

function optionCount(questionId?: number) {
  return optionRows.value.filter((item) => item.questionId === questionId).length
}

function questionGroupLabel(questionGroupId?: number) {
  return questionGroupOptions.value.find((item) => item.value === questionGroupId)?.label || '-'
}

function resetQuestionForm(record?: ConfigQuestionVO) {
  Object.keys(questionForm).forEach((key) => delete (questionForm as ProductRecord)[key])
  Object.assign(questionForm, {
    inputType: 'SINGLE_SELECT',
    requiredFlag: '1',
    customerVisible: '1',
    status: 'ENABLED',
    sortOrder: 10,
    templateVersionId: selectedProduct.value?.templateVersionId,
    ...(record || {})
  })
}

function resetOptionForm(record?: ConfigOptionVO) {
  Object.keys(optionForm).forEach((key) => delete (optionForm as ProductRecord)[key])
  Object.assign(optionForm, {
    sourceType: 'MANUAL',
    status: 'ENABLED',
    sortOrder: 10,
    questionId: selectedQuestionId.value,
    templateVersionId: selectedProduct.value?.templateVersionId,
    ...(record || {})
  })
}

function selectProduct(product: SalesProductVO) {
  selectedProductId.value = product.salesProductId
  selectedQuestionId.value = undefined
  evaluation.value = {}
  resetQuestionForm()
  loadTemplateData()
}

function selectQuestion(question: ConfigQuestionVO) {
  selectedQuestionId.value = question.questionId
}

function startAddQuestion() {
  selectedQuestionId.value = undefined
  resetQuestionForm({ sortOrder: (questionRows.value.length + 1) * 10 })
  questionDialogOpen.value = true
}

function editQuestionRecord(question: ConfigQuestionVO) {
  selectedQuestionId.value = question.questionId
  resetQuestionForm(question)
  questionDialogOpen.value = true
}

async function deleteQuestion() {
  if (!selectedQuestionId.value) return
  await ElMessageBox.confirm(t('productCenter.common.deleteConfirm'), t('common.prompt'), {
    type: 'warning',
    confirmButtonText: t('common.confirm'),
    cancelButtonText: t('common.cancel')
  })
  await configQuestionApi.remove(selectedQuestionId.value)
  ElMessage.success(t('common.deleteSuccess'))
  selectedQuestionId.value = undefined
  await loadTemplateData()
}

async function deleteQuestionRecord(question: ConfigQuestionVO) {
  if (!question.questionId) return
  selectedQuestionId.value = question.questionId
  await deleteQuestion()
}

function startAddOption() {
  resetOptionForm({ sortOrder: (currentQuestionOptions.value.length + 1) * 10 })
  optionDialogOpen.value = true
}

function editOption(option: ConfigOptionVO) {
  resetOptionForm(option)
  optionDialogOpen.value = true
}

async function deleteOption(option: ConfigOptionVO) {
  if (!option.optionId) return
  await ElMessageBox.confirm(t('productCenter.common.deleteConfirm'), t('common.prompt'), {
    type: 'warning',
    confirmButtonText: t('common.confirm'),
    cancelButtonText: t('common.cancel')
  })
  await configOptionApi.remove(option.optionId)
  ElMessage.success(t('common.deleteSuccess'))
  await loadTemplateData()
}

async function loadProducts() {
  const response = await salesProductApi.options?.({ pageNum: 1, pageSize: 200, status: 'ENABLED' })
  salesProducts.value = Array.isArray(response) ? response : response?.data || []
  if (!selectedProductId.value && salesProducts.value[0]) {
    selectedProductId.value = salesProducts.value[0].salesProductId
  }
}

async function loadQuestionGroups() {
  const response = await questionGroupApi.options?.({ pageNum: 1, pageSize: 100, status: 'ENABLED' })
  questionGroups.value = Array.isArray(response) ? response : response?.data || []
}

async function loadTemplateData() {
  if (!selectedProduct.value?.templateVersionId) {
    questionRows.value = []
    optionRows.value = []
    return
  }
  loading.value = true
  try {
    const [questionResponse, optionResponse] = await Promise.all([
      configQuestionApi.options?.({ pageNum: 1, pageSize: 300, templateVersionId: selectedProduct.value.templateVersionId, status: 'ENABLED' }),
      configOptionApi.options?.({ pageNum: 1, pageSize: 800, templateVersionId: selectedProduct.value.templateVersionId, status: 'ENABLED' })
    ])
    questionRows.value = (Array.isArray(questionResponse) ? questionResponse : questionResponse?.data || []).sort((a, b) => Number(a.sortOrder || 0) - Number(b.sortOrder || 0))
    optionRows.value = (Array.isArray(optionResponse) ? optionResponse : optionResponse?.data || []).sort((a, b) => Number(a.sortOrder || 0) - Number(b.sortOrder || 0))
    selectQuestion(questionRows.value[0] || {})
  } finally {
    loading.value = false
  }
}

async function reloadAll() {
  await Promise.all([loadProducts(), loadQuestionGroups()])
  await loadTemplateData()
}

async function saveQuestion() {
  if (!selectedProduct.value?.templateVersionId) {
    ElMessage.warning(t('productCenter.template.templateVersionRequired'))
    return
  }
  if (!questionForm.questionCode || !questionForm.questionNameCn) {
    ElMessage.warning(t('productCenter.template.questionRequired'))
    return
  }
  savingQuestion.value = true
  try {
    questionForm.templateVersionId = selectedProduct.value.templateVersionId
    if (questionForm.questionId) {
      await configQuestionApi.update(questionForm)
    } else {
      await configQuestionApi.add(questionForm)
    }
    questionDialogOpen.value = false
    ElMessage.success(t('common.editSuccess'))
    await loadTemplateData()
  } finally {
    savingQuestion.value = false
  }
}

async function saveOption() {
  if (!selectedQuestionId.value || !selectedProduct.value?.templateVersionId) {
    ElMessage.warning(t('productCenter.template.questionRequired'))
    return
  }
  if (!optionForm.optionCode || !optionForm.optionNameCn) {
    ElMessage.warning(t('productCenter.template.optionRequired'))
    return
  }
  savingOption.value = true
  try {
    optionForm.questionId = selectedQuestionId.value
    optionForm.templateVersionId = selectedProduct.value.templateVersionId
    if (optionForm.optionId) {
      await configOptionApi.update(optionForm)
    } else {
      await configOptionApi.add(optionForm)
    }
    optionDialogOpen.value = false
    ElMessage.success(t('common.editSuccess'))
    await loadTemplateData()
  } finally {
    savingOption.value = false
  }
}

function buildSelectedOptions() {
  const result: Record<string, string> = {}
  questionRows.value.forEach((question) => {
    const firstOption = optionRows.value.find((option) => option.questionId === question.questionId)
    if (question.questionCode && firstOption?.optionCode) {
      result[question.questionCode] = firstOption.optionCode
    }
  })
  return result
}

async function runEvaluate() {
  if (!selectedProduct.value?.templateVersionId) {
    ElMessage.warning(t('productCenter.template.templateVersionRequired'))
    return
  }
  evaluating.value = true
  try {
    const response = await evaluateConfig({
      salesProductId: selectedProduct.value.salesProductId,
      templateVersionId: selectedProduct.value.templateVersionId,
      width: selectedProduct.value.defaultWidth,
      height: selectedProduct.value.defaultHeight,
      selectedOptions: buildSelectedOptions(),
      inputValues: {
        width: selectedProduct.value.defaultWidth,
        height: selectedProduct.value.defaultHeight
      }
    })
    evaluation.value = response.data || {}
  } finally {
    evaluating.value = false
  }
}

onMounted(() => {
  reloadAll()
})
</script>

<style scoped lang="scss">
.config-entry {
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  min-height: 1180px;
  overflow: visible;
  padding: 8px;
  color: #1f2937;
  background:
    linear-gradient(180deg, #f7fafc 0%, #f3f6f8 100%);
}

.config-entry__topbar,
.config-entry__products,
.config-entry__summary,
.config-entry__question-list,
.config-entry__editor,
.config-entry__result {
  border: 1px solid #dbe3ea;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.04);
}

.config-entry__topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex: 0 0 auto;
  gap: 10px;
  margin-bottom: 6px;
  padding: 12px 14px;
  border-left: 4px solid #2563eb;

  p,
  h1,
  span {
    margin: 0;
  }

  p {
    color: #2563eb;
    font-size: 12px;
    font-weight: 800;
    text-transform: uppercase;
  }

  h1 {
    margin-top: 3px;
    color: #111827;
    font-size: 20px;
    line-height: 1.2;
  }

  span {
    display: block;
    margin-top: 3px;
    color: #64748b;
    font-size: 13px;
  }
}

.config-entry__title {
  min-width: 0;
}

.config-entry__actions {
  flex-shrink: 0;
}

.config-entry__product-pill {
  display: grid;
  gap: 2px;
  min-width: 240px;
  border: 1px solid #bfdbfe;
  border-radius: 8px;
  background: #eff6ff;
  padding: 6px 10px;

  span {
    color: #64748b;
    font-size: 11px;
    line-height: 1.2;
  }

  strong {
    overflow: hidden;
    color: #334155;
    font-size: 13px;
    line-height: 1.25;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.config-entry__layout {
  display: grid;
  grid-template-columns: clamp(246px, 18vw, 276px) minmax(0, 1fr);
  flex: 1 1 auto;
  gap: 6px;
  min-height: 1100px;
  align-items: stretch;
}

.config-entry__products,
.config-entry__main,
.config-entry__workbench,
.config-entry__question-list,
.config-entry__editor {
  min-height: 0;
}

.config-entry__products {
  display: flex;
  flex-direction: column;
  height: 100%;
  margin-bottom: 0;
  overflow: hidden;
  padding: 0;
  line-height: normal;
}

.config-entry__pane-head,
.config-entry__summary,
.config-entry__question-list,
.config-entry__editor,
.config-entry__result {
  padding: 8px;
}

.config-entry__pane-head {
  box-sizing: border-box;
  width: 100%;
  border-bottom: 1px solid #e2e8f0;
  background: #fbfdff;

  h2,
  p {
    margin: 0;
  }

  p {
    margin: 4px 0 9px;
    color: #64748b;
    font-size: 12px;
    line-height: 1.45;
  }
}

.config-entry__product-list {
  box-sizing: border-box;
  display: grid;
  align-content: start;
  flex: 1 1 auto;
  gap: 6px;
  min-height: 0;
  overflow: auto;
  padding: 6px;
  width: 100%;
}

.config-entry__product-card,
.config-entry__question-card {
  width: 100%;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #fff;
  color: inherit;
  text-align: left;
  transition:
    border-color 0.16s ease,
    background-color 0.16s ease,
    transform 0.16s ease;
}

.config-entry__product-card:hover,
.config-entry__question-card:hover {
  border-color: #93c5fd;
}

.config-entry__product-card:active,
.config-entry__question-card:active {
  transform: translateY(1px);
}

.config-entry__product-card {
  display: grid;
  grid-template-columns: 32px minmax(0, 1fr);
  gap: 7px;
  min-height: 74px;
  padding: 7px;

  strong {
    display: inline-block;
    min-width: 0;
    overflow: hidden;
    color: #111827;
    font-size: 14px;
    line-height: 1.35;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  small,
  em {
    display: block;
    overflow: hidden;
    margin-top: 3px;
    color: #64748b;
    font-size: 12px;
    font-style: normal;
    line-height: 1.35;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.config-entry__type-chip {
  display: inline-block;
  flex: 0 0 auto;
  max-width: 64px;
  overflow: hidden;
  border: 1px solid #dbe3ea;
  border-radius: 6px;
  background: #f8fafc;
  padding: 1px 5px;
  color: #475569;
  font-size: 11px;
  line-height: 1.3;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.config-entry__product-body {
  display: grid;
  align-content: start;
  min-width: 0;
}

.config-entry__product-line {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
}

.config-entry__product-card.is-active,
.config-entry__question-card.is-active {
  border-color: #2563eb;
  background: #eff6ff;
  box-shadow: inset 3px 0 0 #2563eb;
}

.config-entry__index {
  display: inline-grid;
  place-items: center;
  width: 30px;
  height: 30px;
  border: 1px solid #bfdbfe;
  border-radius: 999px;
  background: #ffffff;
  color: #1d4ed8;
  font-weight: 800;
}

.config-entry__main {
  display: flex;
  flex-direction: column;
  gap: 6px;
  height: 100%;
  min-width: 0;
  overflow: visible;
}

.config-entry__summary {
  display: grid;
  grid-template-columns: minmax(220px, 0.55fr) minmax(0, 1.45fr);
  flex: 0 0 auto;
  gap: 8px;
  padding: 10px 12px;
  align-items: stretch;

  p,
  h2,
  span {
    margin: 0;
  }

  p {
    color: #2563eb;
    font-size: 12px;
    font-weight: 800;
  }

  h2 {
    margin-top: 4px;
    color: #111827;
    font-size: 20px;
    line-height: 1.25;
  }

  span {
    color: #64748b;
    font-size: 13px;
  }
}

.config-entry__summary-title {
  display: grid;
  align-content: center;
  min-height: 64px;
  border-right: 1px solid #e2e8f0;
  padding-right: 14px;
}

.config-entry__facts {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 5px;
}

.config-entry__fact {
  min-width: 0;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #fbfdff;
  padding: 7px 9px;

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
  }

  strong {
    margin-top: 3px;
    color: #111827;
    font-size: 13px;
    font-weight: 700;
  }
}

.config-entry__workbench {
  display: grid;
  grid-template-columns: clamp(272px, 25%, 304px) minmax(0, 1fr);
  flex: 1 1 auto;
  gap: 6px;
  min-height: 0;
}

.config-entry__section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 10px;

  p,
  h2 {
    margin: 0;
  }

  p {
    color: #2563eb;
    font-size: 12px;
    font-weight: 800;
  }

  h2 {
    margin-top: 4px;
    font-size: 16px;
  }
}

.config-entry__question-list {
  height: 100%;
  min-height: 0;
  overflow: auto;
}

.config-entry__question-list .config-entry__section-head {
  position: sticky;
  top: 0;
  z-index: 2;
  margin: -8px -8px 20px;
  padding: 10px 10px 12px;
  border-bottom: 1px solid #e2e8f0;
  background: #ffffff;
}

.config-entry__question-card {
  display: grid;
  grid-template-columns: 28px minmax(0, 1fr) 22px;
  align-items: start;
  gap: 7px;
  margin-bottom: 8px;
  padding: 9px 8px;

  strong,
  small,
  em {
    display: block;
  }

  small,
  em {
    margin-top: 4px;
    color: #64748b;
    font-size: 12px;
    font-style: normal;
    line-height: 1.35;
  }

  strong,
  small,
  em {
    overflow: hidden;
    text-overflow: ellipsis;
  }

  strong,
  small {
    white-space: nowrap;
  }
}

.config-entry__question-content {
  display: grid;
  gap: 4px;
  min-width: 0;
}

.config-entry__question-headline {
  display: block;
  min-width: 0;

  strong {
    padding-top: 1px;
  }
}

.config-entry__badges {
  display: flex;
  flex: 0 0 auto;
  flex-wrap: nowrap;
  align-content: center;
  gap: 4px;
  justify-content: flex-start;
  margin-top: 0;
  white-space: nowrap;

  :deep(.el-tag) {
    padding: 0 7px;
  }
}

.config-entry__question-copy {
  display: grid;
  gap: 2px;
  min-width: 0;
}

.config-entry__question-actions {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 0 0 auto;
  gap: 3px;
  opacity: 0.72;

  :deep(.el-button) {
    margin-left: 0;
  }
}

.config-entry__question-card:hover .config-entry__question-actions,
.config-entry__question-card.is-active .config-entry__question-actions {
  opacity: 1;
}

.config-entry__editor {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-width: 0;
  min-height: 0;
  overflow: hidden;
}

.config-entry__editor-head {
  flex: 0 0 auto;
}

.config-entry__question-detail {
  display: grid;
  gap: 6px;
  flex: 0 0 auto;
  margin-bottom: 8px;
}

.config-entry__question-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: stretch;
  gap: 8px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #fbfdff;
  padding: 6px;
}

.config-entry__detail-item {
  flex: 1 1 120px;
  min-width: 0;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  background: #ffffff;
  padding: 7px 9px;

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
    line-height: 1.35;
  }

  strong {
    margin-top: 3px;
    color: #1f2937;
    font-size: 13px;
    font-weight: 700;
    line-height: 1.45;
  }

}

.config-entry__usage {
  display: grid;
  grid-template-columns: 150px minmax(0, 1fr);
  align-items: start;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #fbfdff;
  padding: 7px 9px;

  span {
    display: block;
    color: #64748b;
    font-size: 12px;
  }

  p {
    min-height: 0;
    margin: 0;
    color: #1f2937;
    font-size: 13px;
    line-height: 1.55;
  }
}

.config-entry__form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 14px;

  :deep(.el-select),
  :deep(.el-input-number) {
    width: 100%;
  }

  .is-wide {
    grid-column: 1 / -1;
  }
}

.config-entry__table-box {
  flex: 1 1 430px;
  min-width: 0;
  min-height: 430px;
  overflow: hidden;
  border-radius: 8px;

  :deep(.el-table) {
    width: 100%;
    height: 100%;
  }

  :deep(.el-scrollbar__bar.is-horizontal) {
    display: none;
  }

  :deep(.config-entry__option-actions .cell) {
    display: flex;
    justify-content: center;
    gap: 8px;
  }

  :deep(.config-entry__option-actions .el-button) {
    margin-left: 0;
  }
}

.config-entry__evaluation-strip {
  display: flex;
  align-items: center;
  flex: 0 0 auto;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 8px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #fbfdff;
  padding: 8px 10px;

  span {
    color: #2563eb;
    font-size: 12px;
    font-weight: 800;
  }

  strong {
    color: #111827;
    font-size: 13px;
  }

  em {
    border-left: 1px solid #e2e8f0;
    padding-left: 8px;
    color: #64748b;
    font-size: 12px;
    font-style: normal;
  }
}

@media (max-width: 1200px) {
  .config-entry {
    height: auto;
    min-height: calc(100vh - 84px);
    overflow: visible;
  }

  .config-entry__layout,
  .config-entry__workbench {
    grid-template-columns: 1fr;
  }

  .config-entry__main,
  .config-entry__products,
  .config-entry__question-list,
  .config-entry__editor {
    overflow: visible;
  }

  .config-entry__product-list,
  .config-entry__question-list {
    max-height: 420px;
  }

  .config-entry__summary {
    grid-template-columns: 1fr;
  }

  .config-entry__summary-title {
    min-height: 0;
    border-right: 0;
    border-bottom: 1px solid #e2e8f0;
    padding-right: 0;
    padding-bottom: 12px;
  }
}

@media (max-width: 768px) {
  .config-entry__topbar,
  .config-entry__section-head {
    align-items: flex-start;
    flex-direction: column;
  }

  .config-entry__form {
    grid-template-columns: 1fr;
  }

  .config-entry__facts {
    grid-template-columns: 1fr;
  }

  .config-entry__question-meta {
    grid-template-columns: 1fr;
  }

  .config-entry__usage {
    grid-template-columns: 1fr;
  }
}
</style>
