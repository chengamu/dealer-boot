<template>
  <div class="app-container engineering-page">
    <section class="engineering-hero">
      <div>
        <p class="engineering-eyebrow">{{ t('productCenter.engineering.eyebrow') }}</p>
        <h2>{{ t('productCenter.engineering.title') }}</h2>
        <p>{{ t('productCenter.engineering.description') }}</p>
      </div>
      <div class="engineering-actions">
        <el-select v-model="selectedPlanId" filterable :placeholder="t('productCenter.engineering.selectPlan')" @change="onPlanChange">
          <el-option v-for="plan in plans" :key="plan.planId" :label="`${plan.planCode} ${plan.planNameCn}`" :value="plan.planId" />
        </el-select>
        <el-select v-model="selectedVersionId" filterable :placeholder="t('productCenter.engineering.selectVersion')" @change="loadWorkbench">
          <el-option v-for="version in versions" :key="version.versionId" :label="`${version.versionNo} ${version.versionName || ''}`" :value="version.versionId" />
        </el-select>
        <el-button type="primary" :icon="VideoPlay" @click="runPreview">{{ t('productCenter.engineering.preview') }}</el-button>
        <el-button :icon="CircleCheck" @click="runCheck">{{ t('productCenter.engineering.check') }}</el-button>
      </div>
    </section>

    <section class="engineering-preview">
      <el-form :model="previewForm" inline label-width="92px">
        <el-form-item :label="t('productCenter.engineering.width')">
          <el-input-number v-model="previewForm.width" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item :label="t('productCenter.engineering.height')">
          <el-input-number v-model="previewForm.height" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item :label="t('productCenter.engineering.selectedSystem')">
          <el-select v-model="previewForm.selectedItems.SYSTEM" filterable clearable>
            <el-option v-for="option in systemOptions" :key="String(option.code)" :label="`${option.code} ${option.nameCn || ''}`" :value="option.code" />
          </el-select>
        </el-form-item>
      </el-form>
      <div class="engineering-result">
        <span>{{ t('productCenter.engineering.resultStatus') }}: <b>{{ previewResult?.resultStatus || '-' }}</b></span>
        <span>{{ t('productCenter.engineering.disabledOptions') }}: {{ countOf('disabledOptions') }}</span>
        <span>{{ t('productCenter.engineering.outputComponents') }}: {{ countOf('outputComponents') }}</span>
        <span>{{ t('productCenter.engineering.blockers') }}: {{ countOf('blockers') }}</span>
      </div>
    </section>

    <section class="engineering-layout">
      <aside class="engineering-panel engineering-items">
        <div class="panel-head">
          <div>
            <p>{{ t('productCenter.engineering.stepItems') }}</p>
            <h3>{{ t('productCenter.engineering.items') }}</h3>
          </div>
          <el-button type="primary" :icon="Plus" @click="openEditor('item')">{{ t('common.add') }}</el-button>
        </div>
        <button
          v-for="item in items"
          :key="item.itemId"
          class="item-card"
          :class="{ active: selectedItem?.itemId === item.itemId }"
          @click="selectedItem = item"
        >
          <span class="item-order">{{ item.sortOrder || '-' }}</span>
          <span class="item-main">
            <strong>{{ item.itemNameCn }}</strong>
            <span>{{ item.itemCode }} · {{ item.sourceType }}</span>
          </span>
          <span class="item-flags">
            <el-tag v-if="item.requiredFlag === '1'" type="danger" size="small">{{ t('productCenter.engineering.required') }}</el-tag>
            <el-button link :icon="Edit" @click.stop="openEditor('item', item)" />
            <el-button link type="danger" :icon="Delete" @click.stop="removeRow('item', item)" />
          </span>
        </button>
      </aside>

      <main class="engineering-panel engineering-detail">
        <section class="detail-block detail-block--hero">
          <div>
            <p>{{ t('productCenter.engineering.scopes') }}</p>
            <h3>{{ selectedItem?.itemNameCn || '-' }}</h3>
          </div>
          <div class="detail-actions">
            <el-button :icon="Link" @click="goEntity('scope')">{{ t('productCenter.engineering.openStandalone') }}</el-button>
            <el-button type="primary" :icon="Plus" @click="openEditor('scope')">{{ t('productCenter.engineering.addScope') }}</el-button>
          </div>
        </section>
        <data-table :rows="scopesForItem" :columns="scopeColumns" @edit="openEditor('scope', $event)" @remove="removeRow('scope', $event)" />

        <section class="detail-grid">
          <article class="detail-block">
            <div class="detail-head">
              <div>
                <p>{{ t('productCenter.engineering.rules') }}</p>
                <h3>{{ rules.length }}</h3>
              </div>
              <el-space>
                <el-button :icon="Link" @click="goEntity('rule')">{{ t('productCenter.engineering.openStandalone') }}</el-button>
                <el-button type="primary" :icon="Plus" @click="openEditor('rule')">{{ t('productCenter.engineering.addRule') }}</el-button>
              </el-space>
            </div>
            <data-table :rows="rules" :columns="ruleColumns" @edit="openEditor('rule', $event)" @remove="removeRow('rule', $event)" />
          </article>

          <article class="detail-block">
            <div class="detail-head">
              <div>
                <p>{{ t('productCenter.engineering.outputRules') }}</p>
                <h3>{{ outputRules.length }}</h3>
              </div>
              <el-space>
                <el-button :icon="Link" @click="goEntity('output-rule')">{{ t('productCenter.engineering.openStandalone') }}</el-button>
                <el-button type="primary" :icon="Plus" @click="openEditor('output')">{{ t('productCenter.engineering.addOutput') }}</el-button>
              </el-space>
            </div>
            <data-table :rows="outputRules" :columns="outputColumns" @edit="openEditor('output', $event)" @remove="removeRow('output', $event)" />
          </article>
        </section>

        <section class="detail-grid detail-grid--compact">
          <article class="detail-block">
            <div class="detail-head">
              <div>
                <p>{{ t('productCenter.engineering.standardSkus') }}</p>
                <h3>{{ standardSkus.length }}</h3>
              </div>
              <el-space>
                <el-button :icon="Link" @click="goEntity('standard-sku')">{{ t('productCenter.engineering.openStandalone') }}</el-button>
                <el-button type="primary" :icon="Plus" @click="openEditor('sku')">{{ t('productCenter.engineering.addStandardSku') }}</el-button>
              </el-space>
            </div>
            <data-table :rows="standardSkus" :columns="skuColumns" @edit="openEditor('sku', $event)" @remove="removeRow('sku', $event)" />
          </article>

          <article class="detail-block">
            <div class="detail-head">
              <div>
                <p>{{ t('productCenter.engineering.checkCases') }}</p>
                <h3>{{ checkCases.length }}</h3>
              </div>
              <el-space>
                <el-button :icon="Link" @click="goEntity('check-case')">{{ t('productCenter.engineering.openStandalone') }}</el-button>
                <el-button type="primary" :icon="Plus" @click="openEditor('case')">{{ t('productCenter.engineering.addCheckCase') }}</el-button>
              </el-space>
            </div>
            <data-table :rows="checkCases" :columns="caseColumns" @edit="openEditor('case', $event)" @remove="removeRow('case', $event)" />
          </article>
        </section>
      </main>
    </section>

    <el-drawer v-model="drawer.visible" :title="drawer.title" size="46%">
      <el-form :model="drawer.form" label-width="150px" class="engineering-form">
        <el-form-item v-for="field in drawerFields" :key="field.prop" :label="t(field.labelKey)">
          <el-input-number v-if="field.type === 'number'" v-model="drawer.form[field.prop]" controls-position="right" />
          <el-select v-else-if="field.type === 'select'" v-model="drawer.form[field.prop]" filterable clearable>
            <el-option v-for="option in field.options || []" :key="String(option.value)" :label="option.label" :value="option.value" />
          </el-select>
          <el-input v-else-if="field.type === 'textarea'" v-model="drawer.form[field.prop]" type="textarea" :rows="4" />
          <el-input v-else v-model="drawer.form[field.prop]" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="drawer.visible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="saveDrawer">{{ t('common.save') }}</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts" name="EngineeringWorkbenchPage">
import { computed, defineComponent, h, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { CircleCheck, Delete, Edit, Link, Plus, VideoPlay } from '@element-plus/icons-vue'
import { ElButton, ElMessage, ElMessageBox, ElTable, ElTableColumn } from 'element-plus'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { useProductDict } from '@/hooks/useProductDict'
import { engineeringApi, engineeringPlanApi } from '@/api/product-capability/engineering'
import type { EngineeringItemVO, EngineeringPlanVO, EngineeringPlanVersionVO } from '@/api/product-capability/types'

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const router = useRouter()
const { options: productDictOptions } = useProductDict(
  'engineering_item_type',
  'engineering_scope_type',
  'engineering_rule_source',
  'engineering_rule_type',
  'engineering_output_type',
  'engineering_severity'
)

const plans = ref<EngineeringPlanVO[]>([])
const versions = ref<EngineeringPlanVersionVO[]>([])
const selectedPlanId = ref<number>()
const selectedVersionId = ref<number>()
const selectedItem = ref<EngineeringItemVO>()
const workbench = ref<Record<string, any>>({})
const previewResult = ref<Record<string, any>>()
const previewForm = reactive({ width: 180, height: 160, selectedItems: { SYSTEM: '' } as Record<string, string> })
const drawer = reactive({ visible: false, kind: '', title: '', form: {} as Record<string, any> })

const items = computed(() => (workbench.value.items || []) as EngineeringItemVO[])
const scopes = computed(() => (workbench.value.scopes || []) as Record<string, any>[])
const rules = computed(() => (workbench.value.rules || []) as Record<string, any>[])
const outputRules = computed(() => (workbench.value.outputRules || []) as Record<string, any>[])
const standardSkus = computed(() => (workbench.value.standardSkus || []) as Record<string, any>[])
const checkCases = computed(() => (workbench.value.checkCases || []) as Record<string, any>[])
const scopesForItem = computed(() => scopes.value.filter((scope) => !selectedItem.value?.itemId || scope.itemId === selectedItem.value.itemId))
const systemOptions = computed(() => ((previewResult.value?.availableOptions as Record<string, any[]>)?.SYSTEM || []) as Array<Record<string, any>>)

const optionFromDict = (rows: Array<{ label?: string; value?: string }>) => rows.map((row) => ({
  label: row.label || row.value || '',
  value: row.value || ''
}))
const itemTypeOptions = computed(() => optionFromDict(productDictOptions.value.engineering_item_type || []))
const scopeTypeOptions = computed(() => optionFromDict(productDictOptions.value.engineering_scope_type || []))
const sourceTypeOptions = computed(() => optionFromDict(productDictOptions.value.engineering_rule_source || []))
const ruleTypeOptions = computed(() => optionFromDict(productDictOptions.value.engineering_rule_type || []))
const outputTypeOptions = computed(() => optionFromDict(productDictOptions.value.engineering_output_type || []))
const severityOptions = computed(() => optionFromDict(productDictOptions.value.engineering_severity || []))

const scopeColumns = [
  ['scopeType', 'productCenter.engineering.scopeType'],
  ['scopeCode', 'productCenter.engineering.scopeCode'],
  ['scopeNameCn', 'productCenter.engineering.scopeNameCn'],
  ['includeFlag', 'productCenter.engineering.includeFlag']
]
const ruleColumns = [
  ['ruleCode', 'productCenter.engineering.ruleCode'],
  ['ruleNameCn', 'productCenter.engineering.ruleNameCn'],
  ['ruleType', 'productCenter.engineering.ruleType'],
  ['severity', 'productCenter.engineering.severity']
]
const outputColumns = [
  ['ruleCode', 'productCenter.engineering.ruleCode'],
  ['outputType', 'productCenter.engineering.outputType'],
  ['outputCode', 'productCenter.engineering.outputCode'],
  ['defaultQty', 'productCenter.engineering.defaultQty']
]
const skuColumns = [
  ['standardSkuCode', 'productCenter.engineering.standardSkuCode'],
  ['standardSkuNameCn', 'productCenter.engineering.standardSkuNameCn'],
  ['status', 'productCenter.common.status']
]
const caseColumns = [
  ['caseCode', 'productCenter.engineering.caseCode'],
  ['caseNameCn', 'productCenter.engineering.caseNameCn'],
  ['status', 'productCenter.common.status']
]

const DataTable = defineComponent({
  props: { rows: { type: Array, default: () => [] }, columns: { type: Array, default: () => [] } },
  emits: ['edit', 'remove'],
  setup(props, { emit }) {
    return () => h(ElTable, { data: props.rows, border: true, class: 'engineering-table' }, () => [
      ...(props.columns as string[][]).map(([prop, labelKey]) => h(ElTableColumn, { prop, label: t(labelKey), minWidth: 130, showOverflowTooltip: true })),
      h(ElTableColumn, { label: t('productCenter.common.open'), width: 92, fixed: 'right' }, {
        default: ({ row }: { row: Record<string, any> }) => [
          h(ElButton, { link: true, icon: Edit, onClick: () => emit('edit', row) }),
          h(ElButton, { link: true, type: 'danger', icon: Delete, onClick: () => emit('remove', row) })
        ]
      })
    ])
  }
})

onMounted(async () => {
  await loadPlans()
})

async function loadPlans() {
  const response = await engineeringPlanApi.options({ status: 'ENABLED' })
  plans.value = Array.isArray(response) ? response : response?.data || []
  if (!selectedPlanId.value && plans.value[0]?.planId) {
    selectedPlanId.value = plans.value[0].planId
    await onPlanChange()
  }
}

async function onPlanChange() {
  const versionResponse = await engineeringApi.versions.list({ pageNum: 1, pageSize: 50, planId: selectedPlanId.value })
  versions.value = versionResponse.rows || []
  selectedVersionId.value = versions.value[0]?.versionId
  await loadWorkbench()
}

async function loadWorkbench() {
  if (!selectedVersionId.value) return
  const response = await engineeringApi.workbench(selectedVersionId.value)
  workbench.value = response?.data || response || {}
  selectedItem.value = items.value[0]
  await runPreview()
}

async function runPreview() {
  if (!selectedVersionId.value) return
  const response = await engineeringApi.preview({ ...previewForm, versionId: selectedVersionId.value })
  previewResult.value = response?.data || response || {}
}

async function runCheck() {
  if (!selectedVersionId.value) return
  const response = await engineeringApi.check(selectedVersionId.value)
  const data = response?.data || response || {}
  ElMessage.success(`${t('productCenter.engineering.check')}: ${data.status || '-'}`)
}

function countOf(key: string) {
  const rows = previewResult.value?.[key]
  return Array.isArray(rows) ? rows.length : 0
}

function goEntity(path: string) {
  router.push({ path: `/product-engineering/${path}`, query: { versionId: selectedVersionId.value ? String(selectedVersionId.value) : undefined } })
}

function openEditor(kind: string, row?: Record<string, any>) {
  drawer.kind = kind
  drawer.form = { ...(row || defaultsFor(kind)) }
  drawer.title = row ? t('productCenter.engineering.editRecord') : t('productCenter.engineering.addRecord')
  drawer.visible = true
}

function defaultsFor(kind: string) {
  const base = { versionId: selectedVersionId.value, status: 'ENABLED', sortOrder: 10 }
  if (kind === 'item') return { ...base, planId: selectedPlanId.value, requiredFlag: '1', multiSelectFlag: '0', customerSelectable: '1' }
  if (kind === 'scope') return { ...base, itemId: selectedItem.value?.itemId, itemCode: selectedItem.value?.itemCode, includeFlag: 'INCLUDE' }
  if (kind === 'rule') return { ...base, severity: 'WARNING', conditionJson: '{"all":[]}', actionJson: '{"type":"DISABLE_OPTION"}' }
  if (kind === 'output') return { ...base, conditionJson: '{"all":[]}', defaultQty: 1 }
  if (kind === 'case') return { ...base, inputJson: '{"width":180,"height":160,"selectedItems":{}}', expectedJson: '{}' }
  return { ...base }
}

const drawerFields = computed(() => {
  if (drawer.kind === 'item') return [
    field('itemCode', 'productCenter.engineering.itemCode'),
    field('itemNameCn', 'productCenter.engineering.itemNameCn'),
    field('itemNameEn', 'productCenter.engineering.itemNameEn'),
    field('itemType', 'productCenter.engineering.itemType', 'select', itemTypeOptions.value),
    field('sourceType', 'productCenter.engineering.sourceType', 'select', sourceTypeOptions.value),
    field('requiredFlag', 'productCenter.engineering.requiredFlag'),
    field('sortOrder', 'productCenter.common.sortOrder', 'number')
  ]
  if (drawer.kind === 'scope') return [
    field('scopeType', 'productCenter.engineering.scopeType', 'select', scopeTypeOptions.value),
    field('scopeCode', 'productCenter.engineering.scopeCode'),
    field('scopeNameCn', 'productCenter.engineering.scopeNameCn'),
    field('scopeNameEn', 'productCenter.engineering.scopeNameEn'),
    field('includeFlag', 'productCenter.engineering.includeFlag'),
    field('conditionJson', 'productCenter.engineering.conditionJson', 'textarea'),
    field('sortOrder', 'productCenter.common.sortOrder', 'number')
  ]
  if (drawer.kind === 'rule') return [
    field('ruleCode', 'productCenter.engineering.ruleCode'),
    field('ruleNameCn', 'productCenter.engineering.ruleNameCn'),
    field('ruleType', 'productCenter.engineering.ruleType', 'select', ruleTypeOptions.value),
    field('severity', 'productCenter.engineering.severity', 'select', severityOptions.value),
    field('conditionJson', 'productCenter.engineering.conditionJson', 'textarea'),
    field('actionJson', 'productCenter.engineering.actionJson', 'textarea'),
    field('messageCn', 'productCenter.engineering.messageCn')
  ]
  if (drawer.kind === 'output') return [
    field('ruleCode', 'productCenter.engineering.ruleCode'),
    field('ruleNameCn', 'productCenter.engineering.ruleNameCn'),
    field('conditionJson', 'productCenter.engineering.conditionJson', 'textarea'),
    field('outputType', 'productCenter.engineering.outputType', 'select', outputTypeOptions.value),
    field('outputCode', 'productCenter.engineering.outputCode'),
    field('outputNameCn', 'productCenter.engineering.outputNameCn'),
    field('defaultQty', 'productCenter.engineering.defaultQty', 'number')
  ]
  if (drawer.kind === 'sku') return [
    field('standardSkuCode', 'productCenter.engineering.standardSkuCode'),
    field('standardSkuNameCn', 'productCenter.engineering.standardSkuNameCn'),
    field('fixedItemsJson', 'productCenter.engineering.fixedItemsJson', 'textarea')
  ]
  return [
    field('caseCode', 'productCenter.engineering.caseCode'),
    field('caseNameCn', 'productCenter.engineering.caseNameCn'),
    field('inputJson', 'productCenter.engineering.inputJson', 'textarea'),
    field('expectedJson', 'productCenter.engineering.expectedJson', 'textarea')
  ]
})

function field(prop: string, labelKey: string, type = 'text', options: Array<{ label: string; value: string }> = []) {
  return { prop, labelKey, type, options }
}

async function saveDrawer() {
  const api = apiFor(drawer.kind)
  const idKey = idKeyFor(drawer.kind)
  if (drawer.kind === 'scope' && !drawer.form.itemId) {
    ElMessage.warning(t('productCenter.engineering.selectItemFirst'))
    return
  }
  if (drawer.form[idKey]) await api.update(drawer.form)
  else await api.add(drawer.form)
  drawer.visible = false
  await loadWorkbench()
}

async function removeRow(kind: string, row: Record<string, any>) {
  const id = row[idKeyFor(kind)]
  if (!id) return
  await ElMessageBox.confirm(t('productCenter.common.deleteConfirm'))
  await apiFor(kind).remove(id)
  await loadWorkbench()
}

function apiFor(kind: string) {
  if (kind === 'item') return engineeringApi.items
  if (kind === 'scope') return engineeringApi.scopes
  if (kind === 'rule') return engineeringApi.rules
  if (kind === 'output') return engineeringApi.outputRules
  if (kind === 'sku') return engineeringApi.standardSkus
  return engineeringApi.checkCases
}

function idKeyFor(kind: string) {
  if (kind === 'item') return 'itemId'
  if (kind === 'scope') return 'scopeId'
  if (kind === 'rule') return 'ruleId'
  if (kind === 'output') return 'outputRuleId'
  if (kind === 'sku') return 'skuEngineeringId'
  return 'checkCaseId'
}
</script>

<style scoped lang="scss">
.engineering-page {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-width: 1180px;
}

.engineering-hero,
.engineering-preview,
.engineering-panel {
  border: 1px solid #e5edf7;
  border-radius: 6px;
  background: #fff;
}

.engineering-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 18px;

  h2 {
    margin: 2px 0 6px;
    font-size: 22px;
    color: #1f2d3d;
  }

  p {
    margin: 0;
    color: #607086;
  }
}

.engineering-eyebrow {
  color: #1f78ff !important;
  font-weight: 700;
}

.engineering-actions {
  display: grid;
  grid-template-columns: 220px 160px auto auto;
  gap: 8px;
  align-items: center;
}

.engineering-preview {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
}

.engineering-result {
  display: flex;
  gap: 16px;
  color: #53657d;
}

.engineering-layout {
  display: grid;
  grid-template-columns: 340px minmax(0, 1fr);
  gap: 10px;
  min-height: calc(100vh - 285px);
}

.engineering-panel {
  padding: 12px;
  min-height: 640px;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;

  p {
    margin: 0;
    color: #1f78ff;
    font-weight: 700;
  }

  h3 {
    margin: 2px 0 0;
    font-size: 18px;
  }
}

.item-card {
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr) auto;
  gap: 10px;
  align-items: start;
  width: 100%;
  min-height: 84px;
  margin-bottom: 8px;
  padding: 12px;
  border: 1px solid #dce5f1;
  border-radius: 6px;
  background: #fff;
  text-align: left;
  cursor: pointer;

  &.active {
    border-color: #1f78ff;
    background: #eef5ff;
  }
}

.item-order {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border: 1px solid #b9d5ff;
  border-radius: 50%;
  color: #1f78ff;
  font-weight: 700;
}

.item-main {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;

  span {
    overflow: hidden;
    color: #607086;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.item-flags {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.engineering-detail {
  display: flex;
  flex-direction: column;
  gap: 10px;
  overflow: hidden;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.detail-grid--compact {
  align-items: start;
}

.detail-block {
  min-width: 0;
  padding: 12px;
  border: 1px solid #e5edf7;
  border-radius: 6px;
  background: #fbfdff;
}

.detail-block--hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.detail-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 10px;
}

.detail-block p,
.detail-head p {
  margin: 0;
  color: #1f78ff;
  font-weight: 700;
}

.detail-block h3,
.detail-head h3 {
  margin: 2px 0 0;
  font-size: 18px;
}

.detail-actions {
  display: flex;
  gap: 8px;
}

.engineering-table {
  width: 100%;
  margin-bottom: 10px;
}

.engineering-form :deep(.el-select),
.engineering-form :deep(.el-input-number) {
  width: 100%;
}
</style>
