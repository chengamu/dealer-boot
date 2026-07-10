<template>
  <AdminDrawer
    :model-value="modelValue"
    :title="form.shippingTemplateId ? t('productCenter.shippingTemplate.editTitle') : t('productCenter.shippingTemplate.addTitle')"
    size="min(1120px, 92vw)"
    append-to-body
    :close-on-click-modal="false"
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="96px" class="shipping-template-drawer">
      <section class="shipping-template-drawer__base">
        <el-form-item :label="t('productCenter.shippingTemplate.code')" prop="templateCode">
          <el-input v-model="form.templateCode" disabled :placeholder="t('productCenter.shippingTemplate.autoCodePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('productCenter.shippingTemplate.name')" prop="templateName">
          <el-input v-model="form.templateName" :placeholder="t('productCenter.shippingTemplate.namePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('productCenter.shippingTemplate.currency')" prop="currencyCode">
          <el-select v-model="form.currencyCode" filterable :placeholder="t('productCenter.shippingTemplate.currencyPlaceholder')">
            <el-option v-for="item in currencies" :key="item.currencyCode" :label="currencyLabel(item)" :value="item.currencyCode" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('productCenter.common.remark')" class="shipping-template-drawer__remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </section>

      <ShippingTemplateRuleTable
        :model-value="form.rules || []"
        @update:model-value="form.rules = $event"
      />
    </el-form>

    <template #footer>
      <el-button @click="$emit('update:modelValue', false)">{{ t('common.cancel') }}</el-button>
      <el-button type="primary" @click="save">{{ t('common.save') }}</el-button>
    </template>
  </AdminDrawer>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { listCurrencies, type StandardCurrency } from '@/api/system/standard'
import type { ShippingTemplateVO } from '@/api/product-pricing/types'
import ShippingTemplateRuleTable from './ShippingTemplateRuleTable.vue'

const props = defineProps<{ modelValue: boolean, template?: ShippingTemplateVO }>()
const emit = defineEmits<{ 'update:modelValue': [value: boolean], save: [payload: ShippingTemplateVO] }>()
const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const formRef = ref<FormInstance>()
const form = reactive<ShippingTemplateVO>(blankForm())
const currencies = ref<StandardCurrency[]>([])
const rules = computed<FormRules<ShippingTemplateVO>>(() => ({
  templateName: [{ required: true, message: t('product.shippingTemplate.nameRequired'), trigger: 'blur' }],
  currencyCode: [{ required: true, message: t('productCenter.shippingTemplate.currencyRequired'), trigger: 'change' }]
}))

watch(() => props.modelValue, async (open) => {
  if (!open) return
  Object.assign(form, blankForm(), props.template || {})
  form.rules = props.template?.rules?.length ? props.template.rules.map(row => ({ ...row })) : defaultRules()
  formRef.value?.clearValidate()
  if (!currencies.value.length) {
    const response = await listCurrencies()
    currencies.value = response.data || []
  }
})

function blankForm(): ShippingTemplateVO {
  return { currencyCode: 'USD', defaultFlag: false, status: 'DISABLED', sortOrder: 0, rules: defaultRules() }
}

function defaultRules() {
  return [
    { feeCode: 'MANUAL', feeName: t('productCenter.shippingTemplate.manual'), minAreaSqft: 0, feeAmount: 0, sortOrder: 0 },
    { feeCode: 'MOTORIZED', feeName: t('productCenter.shippingTemplate.motorized'), minAreaSqft: 0, feeAmount: 0, sortOrder: 1 }
  ]
}

function currencyLabel(item: StandardCurrency) {
  return `${item.currencyCode} ${item.name || item.nameZh || item.nameEn || ''}`.trim()
}

async function save() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  emit('save', {
    ...form,
    rules: (form.rules || []).map((row, index) => ({ ...row, sortOrder: index }))
  })
}
</script>

<style scoped>
.shipping-template-drawer {
  display: grid;
  gap: 12px;
}

.shipping-template-drawer__base {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 0 14px;
  padding: 12px 12px 0;
  background: #fff;
  border: 1px solid #e9edf5;
  border-radius: 8px;
}

.shipping-template-drawer__base :deep(.el-select),
.shipping-template-drawer__base :deep(.el-input-number) {
  width: 100%;
}

.shipping-template-drawer__remark {
  grid-column: 1 / -1;
}

@media (max-width: 960px) {
  .shipping-template-drawer__base {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
