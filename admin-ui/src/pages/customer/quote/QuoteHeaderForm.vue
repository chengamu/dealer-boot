<template>
  <el-form ref="formRef" :model="quote" :rules="rules" label-position="top" class="quote-header-form">
    <el-form-item :label="t('customer.quote.no')">
      <el-input :model-value="quote.quoteNo || t('customer.quote.addTitle')" disabled />
    </el-form-item>
    <el-form-item :label="t('customer.quote.customer')" prop="customerId">
      <el-select v-model="quote.customerId" :disabled="readonly" filterable>
        <el-option v-for="item in customers" :key="item.customerId" :label="item.companyName || item.customerName" :value="String(item.customerId)" />
      </el-select>
    </el-form-item>
    <el-form-item :label="t('customer.quote.project')" prop="projectName">
      <el-input v-model="quote.projectName" :disabled="readonly" maxlength="160" />
    </el-form-item>
    <el-form-item :label="t('customer.quote.validUntil')">
      <el-date-picker v-model="quote.validUntil" :disabled="readonly" type="date" value-format="YYYY-MM-DD" />
    </el-form-item>
    <el-form-item :label="t('customer.quote.owner')">
      <el-select v-model="quote.ownerUserId" :disabled="readonly" filterable>
        <el-option v-for="item in owners" :key="item.userId" :label="item.nickName || item.userName" :value="String(item.userId)" />
      </el-select>
    </el-form-item>
    <el-form-item :label="t('customer.quote.language')">
      <el-segmented v-model="quote.quoteLanguage" :disabled="readonly" :options="languageOptions" />
    </el-form-item>
    <el-form-item :label="t('customer.quote.remark')">
      <el-input v-model="quote.remark" :disabled="readonly" maxlength="500" />
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import type { FormInstance, FormRules } from 'element-plus'
import type { CustomerQuote } from '@/api/customer/quote'
import type { CustomerOwnerOption, CustomerProfile } from '@/api/customer/profile'

const props = defineProps<{
  quote: CustomerQuote
  customers: CustomerProfile[]
  owners: CustomerOwnerOption[]
  readonly?: boolean
}>()
const { t } = useI18n()
const formRef = ref<FormInstance>()
const languageOptions = computed(() => [
  { label: t('customer.quote.languageOption.zh'), value: 'ZH_CN' },
  { label: t('customer.quote.languageOption.en'), value: 'EN_US' }
])
const rules = computed<FormRules<CustomerQuote>>(() => ({
  customerId: [{ required: true, message: t('customer.quote.customer.required'), trigger: 'change' }],
  projectName: [{ required: true, message: t('customer.quote.project.required'), trigger: 'blur' }]
}))
async function validate() { return formRef.value?.validate().catch(() => false) }
defineExpose({ validate })
</script>

<style scoped>
.quote-header-form { display: grid; grid-template-columns: 165px minmax(180px, 1.15fr) minmax(220px, 1.4fr) 150px 150px 190px minmax(180px, 1fr); gap: 10px 14px; padding: 12px 16px 2px; border: 1px solid #dfe6ef; border-radius: 7px; background: #fff; }
.quote-header-form :deep(.el-form-item) { margin-bottom: 10px; }
.quote-header-form :deep(.el-form-item__label) { margin-bottom: 5px; color: #475467; font-size: 12px; font-weight: 600; line-height: 18px; }
.quote-header-form :deep(.el-select), .quote-header-form :deep(.el-date-editor) { width: 100%; }
@media (max-width: 1440px) { .quote-header-form { grid-template-columns: repeat(4, minmax(170px, 1fr)); } }
</style>
