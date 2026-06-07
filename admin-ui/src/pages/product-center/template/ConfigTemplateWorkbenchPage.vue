<template>
  <div class="app-container config-workbench">
    <section class="config-workbench__header">
      <div>
        <p>{{ t('productCenter.menu.title') }}</p>
        <h1>{{ t('productCenter.template.title') }}</h1>
        <span>{{ t('productCenter.template.workbenchHint') }}</span>
      </div>
      <el-space wrap>
        <el-tag type="primary" effect="light">{{ t('productCenter.template.draftVersion') }}</el-tag>
        <span class="config-workbench__saved">{{ t('productCenter.template.autoSaved') }}</span>
        <el-button icon="DocumentCopy" @click="pasteOpen = true" v-hasPermi="['product:template:edit']">
          {{ t('productCenter.template.batchPaste') }}
        </el-button>
        <el-button type="primary" icon="VideoPlay" @click="runEvaluate" v-hasPermi="['product:template:test']">
          {{ t('productCenter.template.evaluate') }}
        </el-button>
      </el-space>
    </section>

    <el-row :gutter="12" class="config-workbench__layout">
      <el-col :xs="24" :xl="6">
        <div class="config-workbench__rail">
          <div class="config-workbench__rail-head">
            <h2>{{ t('productCenter.template.templateList') }}</h2>
            <el-button link type="primary" icon="Plus" v-hasPermi="['product:template:edit']">{{ t('common.add') }}</el-button>
          </div>
          <product-entity-grid-page :config="templateConfig" />
        </div>
      </el-col>
      <el-col :xs="24" :xl="12">
        <div class="config-workbench__editor">
          <div class="config-workbench__toolbar">
            <div>
              <h2>{{ activeEditorTitle }}</h2>
              <p>{{ t('productCenter.template.editorHint') }}</p>
            </div>
            <el-space wrap>
              <el-button size="small" icon="CopyDocument" @click="pasteOpen = true">{{ t('productCenter.template.batchPaste') }}</el-button>
              <el-button size="small" type="primary" icon="Check">{{ t('common.save') }}</el-button>
            </el-space>
          </div>
          <el-tabs v-model="activeEditor" class="config-workbench__tabs">
            <el-tab-pane v-for="item in editorConfigs" :key="item.key" :label="t(item.titleKey)" :name="item.key">
              <product-entity-grid-page v-if="activeEditor === item.key" :config="item" />
            </el-tab-pane>
          </el-tabs>
        </div>
      </el-col>
      <el-col :xs="24" :xl="6">
        <live-check-panel
          :result="evaluation"
          :form="evaluationForm"
          :selected-option-codes="selectedOptionCodes"
          :option-options="quickOptionOptions"
          @update:form="updateEvaluationForm"
          @update:selected-option-codes="selectedOptionCodes = $event"
          @evaluate="runEvaluate"
        />
        <dealer-preview-panel :result="evaluation" :active="activePreview" @update:active="activePreview = $event" />
      </el-col>
    </el-row>

    <batch-paste-drawer
      v-model="pasteOpen"
      v-model:text="pasteText"
      :title="t('productCenter.template.batchPaste')"
      :tip="t('productCenter.template.batchPasteTip')"
      :rows="pasteRows"
      :columns="pasteColumns"
      @parse="parsePaste"
    />
  </div>
</template>

<script setup lang="ts" name="ConfigTemplateWorkbenchPage">
import { computed, reactive, ref } from 'vue'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { configOptionApi, configQuestionApi, configRuleApi, configTemplateApi, configTemplateVersionApi, evaluateConfig, questionGroupApi } from '@/api/product-capability/config'
import type { ProductRecord } from '@/api/product-capability/types'
import ProductEntityGridPage, { type ProductGridConfig } from '@/pages/product-center/components/ProductEntityGridPage.vue'
import LiveCheckPanel from '@/pages/product-center/components/LiveCheckPanel.vue'
import DealerPreviewPanel from '@/pages/product-center/components/DealerPreviewPanel.vue'
import BatchPasteDrawer from '@/pages/product-center/components/BatchPasteDrawer.vue'

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)

const activeEditor = ref('question')
const activePreview = ref('dealer')
const pasteOpen = ref(false)
const pasteText = ref('')
const pasteRows = ref<ProductRecord[]>([])
const selectedOptionCodes = ref<string[]>(['FABRIC_TYPE:LF'])
const quickOptionOptions = ['FABRIC_TYPE:LF', 'FABRIC_TYPE:RD', 'CONTROL:MOTOR', 'CONTROL:CHAIN']
const evaluation = ref<ProductRecord>({})
const evaluationForm = reactive<{ templateVersionId?: number; widthCm: number; heightCm: number }>({
  templateVersionId: undefined,
  widthCm: 120,
  heightCm: 160
})

const templatePermissions = { add: 'product:template:edit', edit: 'product:template:edit', remove: 'product:template:edit', reference: 'product:template:list' }
const rulePermissions = { add: 'product:template:rule', edit: 'product:template:rule', remove: 'product:template:rule', reference: 'product:template:list' }

const templateConfig = computed<ProductGridConfig>(() => ({
  key: 'configTemplate',
  titleKey: 'productCenter.template.templateList',
  descriptionKey: 'productCenter.template.description',
  idKey: 'templateId',
  permissions: templatePermissions,
  api: configTemplateApi,
  fields: [
    { prop: 'templateCode', labelKey: 'productCenter.template.code', search: true, required: true },
    { prop: 'templateNameCn', labelKey: 'productCenter.template.nameCn', search: true, required: true },
    { prop: 'templateNameEn', labelKey: 'productCenter.template.nameEn' },
    { prop: 'productModelCode', labelKey: 'productCenter.model.code', search: true },
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
    titleKey: 'productCenter.template.version',
    descriptionKey: 'productCenter.template.versionDescription',
    idKey: 'templateVersionId',
    permissions: templatePermissions,
    api: configTemplateVersionApi,
    fields: [
      { prop: 'templateId', labelKey: 'productCenter.template.id', type: 'number', required: true },
      { prop: 'templateCode', labelKey: 'productCenter.template.code', search: true },
      { prop: 'versionNo', labelKey: 'productCenter.template.versionNo', search: true, required: true },
      { prop: 'versionName', labelKey: 'productCenter.template.versionName' },
      { prop: 'versionStatus', labelKey: 'productCenter.template.versionStatus', search: true },
      { prop: 'productModelCode', labelKey: 'productCenter.model.code', search: true },
      { prop: 'salesVariantCode', labelKey: 'productCenter.variant.code' },
      { prop: 'pricePlanCode', labelKey: 'productCenter.price.planCode' },
      { prop: 'draftHash', labelKey: 'productCenter.template.draftHash' },
      { prop: 'remark', labelKey: 'productCenter.common.remark', type: 'textarea', table: false }
    ]
  },
  {
    key: 'group',
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
      { prop: 'status', labelKey: 'productCenter.common.status', type: 'status', search: true },
      { prop: 'sortOrder', labelKey: 'productCenter.common.sortOrder', type: 'number' }
    ]
  },
  {
    key: 'question',
    titleKey: 'productCenter.template.question',
    descriptionKey: 'productCenter.template.questionDescription',
    idKey: 'questionId',
    permissions: templatePermissions,
    api: configQuestionApi,
    fields: [
      { prop: 'templateVersionId', labelKey: 'productCenter.template.templateVersionId', type: 'number', search: true, required: true },
      { prop: 'questionGroupId', labelKey: 'productCenter.template.questionGroupId', type: 'number', search: true },
      { prop: 'questionCode', labelKey: 'productCenter.template.questionCode', search: true, required: true },
      { prop: 'questionNameCn', labelKey: 'productCenter.template.questionNameCn', search: true, required: true },
      { prop: 'questionNameEn', labelKey: 'productCenter.template.questionNameEn' },
      { prop: 'inputType', labelKey: 'productCenter.template.inputType', search: true },
      { prop: 'requiredFlag', labelKey: 'productCenter.template.requiredFlag' },
      { prop: 'customerVisible', labelKey: 'productCenter.template.customerVisible' },
      { prop: 'status', labelKey: 'productCenter.common.status', type: 'status', search: true },
      { prop: 'sortOrder', labelKey: 'productCenter.common.sortOrder', type: 'number' }
    ]
  },
  {
    key: 'option',
    titleKey: 'productCenter.template.option',
    descriptionKey: 'productCenter.template.optionDescription',
    idKey: 'optionId',
    permissions: templatePermissions,
    api: configOptionApi,
    fields: [
      { prop: 'questionId', labelKey: 'productCenter.template.questionId', type: 'number', search: true, required: true },
      { prop: 'templateVersionId', labelKey: 'productCenter.template.templateVersionId', type: 'number', search: true },
      { prop: 'optionCode', labelKey: 'productCenter.template.optionCode', search: true, required: true },
      { prop: 'optionNameCn', labelKey: 'productCenter.template.optionNameCn', search: true, required: true },
      { prop: 'optionNameEn', labelKey: 'productCenter.template.optionNameEn' },
      { prop: 'optionValue', labelKey: 'productCenter.template.optionValue' },
      { prop: 'componentJson', labelKey: 'productCenter.template.componentJson', type: 'textarea', table: false },
      { prop: 'status', labelKey: 'productCenter.common.status', type: 'status', search: true },
      { prop: 'sortOrder', labelKey: 'productCenter.common.sortOrder', type: 'number' }
    ]
  },
  {
    key: 'rule',
    titleKey: 'productCenter.template.rule',
    descriptionKey: 'productCenter.template.ruleDescription',
    idKey: 'ruleId',
    permissions: rulePermissions,
    api: configRuleApi,
    fields: [
      { prop: 'templateVersionId', labelKey: 'productCenter.template.templateVersionId', type: 'number', search: true, required: true },
      { prop: 'ruleCode', labelKey: 'productCenter.template.ruleCode', search: true, required: true },
      { prop: 'ruleNameCn', labelKey: 'productCenter.template.ruleNameCn', search: true, required: true },
      { prop: 'ruleNameEn', labelKey: 'productCenter.template.ruleNameEn' },
      { prop: 'ruleType', labelKey: 'productCenter.template.ruleType', search: true },
      { prop: 'priority', labelKey: 'productCenter.template.priority', type: 'number' },
      { prop: 'conditionJson', labelKey: 'productCenter.template.conditionJson', type: 'textarea', table: false },
      { prop: 'actionJson', labelKey: 'productCenter.template.actionJson', type: 'textarea', table: false },
      { prop: 'status', labelKey: 'productCenter.common.status', type: 'status', search: true }
    ]
  }
])

const activeEditorTitle = computed(() => {
  const current = editorConfigs.value.find((item) => item.key === activeEditor.value)
  return current ? t(current.titleKey) : t('productCenter.template.question')
})
const pasteColumns = computed(() => [
  { prop: 'sortOrder', label: t('productCenter.common.sortOrder'), width: 90 },
  { prop: 'code', label: t('productCenter.variant.code'), minWidth: 140 },
  { prop: 'nameCn', label: t('productCenter.category.nameCn'), minWidth: 160 },
  { prop: 'nameEn', label: t('productCenter.category.nameEn'), minWidth: 160 }
])

function buildSelectedOptions() {
  return selectedOptionCodes.value.reduce<Record<string, string>>((result, code) => {
    const [questionCode, optionCode] = code.split(':')
    if (questionCode && optionCode) result[questionCode] = optionCode
    return result
  }, {})
}

async function runEvaluate() {
  const response = await evaluateConfig({
    templateVersionId: evaluationForm.templateVersionId,
    selectedOptions: buildSelectedOptions(),
    inputValues: {
      width_cm: evaluationForm.widthCm,
      height_cm: evaluationForm.heightCm
    }
  })
  evaluation.value = response.data || {}
}

function updateEvaluationForm(next: { templateVersionId?: number; widthCm: number; heightCm: number }) {
  Object.assign(evaluationForm, next)
}

function parsePaste() {
  pasteRows.value = pasteText.value
    .split(/\r?\n/)
    .flatMap((line, index): ProductRecord[] => {
      const [code, nameCn, nameEn] = line.split(/\t|,/).map((item) => item?.trim())
      return code ? [{ sortOrder: index + 1, code, nameCn, nameEn }] : []
    })
}
</script>

<style scoped lang="scss">
.config-workbench {
  display: flex;
  flex-direction: column;
  gap: 12px;
  color: #111827;
}

.config-workbench__header,
.config-workbench__panel,
.config-workbench__rail,
.config-workbench__editor {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.04);
}

.config-workbench__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px;

  p {
    margin: 0 0 4px;
    color: #64748b;
    font-size: 13px;
  }

  h1 {
    margin: 0;
    color: #111827;
    font-size: 22px;
  }

  span {
    display: block;
    margin-top: 8px;
    color: #64748b;
    font-size: 13px;
  }
}

.config-workbench__saved {
  color: #16a34a;
  font-size: 12px;
}

.config-workbench__rail,
.config-workbench__editor,
.config-workbench__panel {
  padding: 14px;
}

.config-workbench__layout {
  align-items: flex-start;
}

.config-workbench__panel {
  position: sticky;
  top: 84px;
}

.config-workbench__rail :deep(.product-grid-page) {
  border: 0;
  box-shadow: none;
  padding: 0;
}

.config-workbench__rail-head,
.config-workbench__toolbar,
.config-workbench__panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;

  h2 {
    margin: 0;
    color: #111827;
    font-size: 16px;
  }
}

.config-workbench__toolbar {
  padding-bottom: 12px;
  border-bottom: 1px solid #eef2f7;

  p {
    margin: 4px 0 0;
    color: #64748b;
    font-size: 12px;
  }
}

.config-workbench__tabs {
  margin-top: 8px;
}

.config-workbench__evaluate-form {
  :deep(.el-input-number),
  :deep(.el-select) {
    width: 100%;
  }
}

.config-workbench__input-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.config-workbench__check-list {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
  margin-bottom: 14px;

  div {
    padding: 10px;
    border-radius: 8px;
    border: 1px solid #eef2f7;
    background: #f8fafc;
  }

  strong {
    display: block;
    font-size: 20px;
    line-height: 1;
  }

  span {
    display: block;
    margin-top: 6px;
    color: #64748b;
    font-size: 12px;
  }

  .is-danger { background: #fff7f7; color: #dc2626; }
  .is-warning { background: #fffbeb; color: #d97706; }
  .is-success { background: #f0fdf4; color: #16a34a; }
}

.config-workbench__full-button {
  width: 100%;
}

.config-workbench__preview-tabs {
  margin-top: 10px;
}

.config-workbench__preview-card {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-top: 14px;
  padding: 12px;
  border: 1px solid #e5efff;
  border-radius: 8px;
  background: #f8fbff;

  strong,
  span {
    display: block;
  }

  strong {
    color: #111827;
    font-size: 13px;
  }

  span {
    margin-top: 4px;
    color: #64748b;
    font-size: 12px;
  }
}

.config-workbench__result-list {
  display: grid;
  gap: 8px;
  padding: 12px;
  border: 1px solid #eef2f7;
  border-radius: 8px;
  color: #475569;
  font-size: 13px;
  background: #f8fafc;
}

.config-workbench__paste {
  margin: 12px 0;
}

@media (max-width: 768px) {
  .config-workbench__header,
  .config-workbench__toolbar {
    align-items: flex-start;
    flex-direction: column;
  }

  .config-workbench__check-list {
    grid-template-columns: 1fr;
  }
}
</style>
