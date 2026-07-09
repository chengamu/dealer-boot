<template>
  <AdminDrawer
    :model-value="modelValue"
    :title="form.shippingTemplateId ? t('productCenter.shippingTemplate.editTitle') : t('productCenter.shippingTemplate.addTitle')"
    size="880px"
    append-to-body
    :close-on-click-modal="false"
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <el-form label-width="110px" :model="form" class="shipping-template-drawer">
      <section class="shipping-template-drawer__base">
        <el-form-item :label="t('productCenter.shippingTemplate.code')" required><el-input v-model="form.templateCode" /></el-form-item>
        <el-form-item :label="t('productCenter.shippingTemplate.name')" required><el-input v-model="form.templateName" /></el-form-item>
        <el-form-item :label="t('productCenter.shippingTemplate.currency')"><el-input v-model="form.currencyCode" /></el-form-item>
        <el-form-item :label="t('productCenter.shippingTemplate.defaultFlag')"><el-switch v-model="form.defaultFlag" /></el-form-item>
        <el-form-item :label="t('productCenter.common.status')">
          <el-radio-group v-model="form.status">
            <el-radio value="ENABLED">{{ t('common.enabled') }}</el-radio>
            <el-radio value="DISABLED">{{ t('common.disabled') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="t('productCenter.common.sortOrder')"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
        <el-form-item :label="t('productCenter.common.remark')" class="is-span-2"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
      </section>

      <section class="shipping-template-rules">
        <header>
          <div>
            <h3>{{ t('productCenter.shippingTemplate.rules') }}</h3>
            <p>{{ t('productCenter.shippingTemplate.rulesHint') }}</p>
          </div>
          <el-button type="primary" plain icon="Plus" @click="addRule">{{ t('productCenter.shippingTemplate.addRule') }}</el-button>
        </header>
        <el-table :data="form.rules" border height="360">
          <el-table-column type="index" :label="t('common.index')" width="58" align="center" />
          <el-table-column :label="t('productCenter.shippingTemplate.scenario')" width="130">
            <template #default="{ row }">
              <el-select v-model="row.feeCode" @change="syncFeeName(row)">
                <el-option :label="t('productCenter.shippingTemplate.manual')" value="MANUAL" />
                <el-option :label="t('productCenter.shippingTemplate.motorized')" value="MOTORIZED" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column :label="t('productCenter.shippingTemplate.areaRange')" width="220">
            <template #default="{ row }">
              <div class="shipping-template-rules__range">
                <el-input-number v-model="row.minAreaSqft" :controls="false" :min="0" :precision="2" />
                <span>-</span>
                <el-input-number v-model="row.maxAreaSqft" :controls="false" :min="0" :precision="2" :placeholder="t('productCenter.shippingTemplate.unlimited')" />
              </div>
            </template>
          </el-table-column>
          <el-table-column :label="t('productCenter.shippingTemplate.formula')" min-width="340">
            <template #default="{ row }">
              <div class="shipping-template-rules__formula">
                <el-input v-model="row.formulaText" />
                <el-button plain @click="openFormula(row)">{{ t('productCenter.formulaSetup.formulaSelectorShort') }}</el-button>
              </div>
            </template>
          </el-table-column>
          <el-table-column :label="t('common.operate')" width="84" align="center">
            <template #default="{ $index }">
              <el-button link type="danger" @click="form.rules?.splice($index, 1)">{{ t('common.delete') }}</el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>
    </el-form>

    <ShippingFormulaEditorDialog v-model="formulaOpen" v-model:text="formulaText" @confirm="confirmFormula" />

    <template #footer>
      <el-button @click="$emit('update:modelValue', false)">{{ t('common.cancel') }}</el-button>
      <el-button type="primary" @click="save">{{ t('common.save') }}</el-button>
    </template>
  </AdminDrawer>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import type { ShippingTemplateRuleVO, ShippingTemplateVO } from '@/api/product-pricing/types'
import ShippingFormulaEditorDialog from './ShippingFormulaEditorDialog.vue'

const props = defineProps<{ modelValue: boolean, template?: ShippingTemplateVO }>()
const emit = defineEmits<{ 'update:modelValue': [value: boolean], save: [payload: ShippingTemplateVO] }>()
const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const form = reactive<ShippingTemplateVO>(blankForm())
const formulaOpen = ref(false)
const formulaText = ref('')
const editingRule = ref<ShippingTemplateRuleVO>()

watch(() => props.modelValue, (open) => {
  if (!open) return
  Object.assign(form, blankForm(), props.template || {})
  form.rules = props.template?.rules?.length ? props.template.rules.map(row => ({ ...row })) : defaultRules()
})

function blankForm(): ShippingTemplateVO {
  return { currencyCode: 'USD', defaultFlag: false, status: 'ENABLED', sortOrder: 0, rules: defaultRules() }
}

function defaultRules() {
  return [
    newRule('MANUAL', 0),
    newRule('MOTORIZED', 1)
  ]
}

function newRule(code: string, index: number): ShippingTemplateRuleVO {
  return {
    feeCode: code,
    feeName: feeName(code),
    minAreaSqft: 0,
    maxAreaSqft: undefined,
    formulaText: code === 'MOTORIZED' ? '25 + MAX(areaSqft - 20, 0) * 0.45' : '18 + MAX(areaSqft - 20, 0) * 0.35',
    sortOrder: index
  }
}

function addRule() {
  form.rules ||= []
  form.rules.push(newRule('MANUAL', form.rules.length))
}

function syncFeeName(row: ShippingTemplateRuleVO) {
  row.feeName = feeName(row.feeCode || 'MANUAL')
}

function feeName(code: string) {
  return code === 'MOTORIZED' ? t('productCenter.shippingTemplate.motorized') : t('productCenter.shippingTemplate.manual')
}

function openFormula(row: ShippingTemplateRuleVO) {
  editingRule.value = row
  formulaText.value = row.formulaText || ''
  formulaOpen.value = true
}

function confirmFormula() {
  if (editingRule.value) editingRule.value.formulaText = formulaText.value
  formulaOpen.value = false
}

function save() {
  emit('save', {
    ...form,
    rules: (form.rules || []).map((row, index) => ({ ...row, feeName: feeName(row.feeCode || 'MANUAL'), sortOrder: index }))
  })
}
</script>

<style scoped>
.shipping-template-drawer {
  display: grid;
  gap: 14px;
}

.shipping-template-drawer__base {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

.is-span-2 {
  grid-column: 1 / -1;
}

.shipping-template-rules header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.shipping-template-rules h3,
.shipping-template-rules p {
  margin: 0;
}

.shipping-template-rules p {
  margin-top: 4px;
  color: #667085;
  font-size: 12px;
}

.shipping-template-rules__range,
.shipping-template-rules__formula {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
