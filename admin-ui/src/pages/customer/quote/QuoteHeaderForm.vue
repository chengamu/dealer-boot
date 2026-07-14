<template>
  <section v-if="readonly" class="quote-header-form quote-header-form--readonly">
    <article class="quote-header-form__fact">
      <span>{{ t('customer.quote.customer') }}</span>
      <strong>{{ displayCustomer }}</strong>
    </article>
    <article class="quote-header-form__fact">
      <span>{{ t('customer.quote.project') }}</span>
      <strong>{{ quote.projectName || '-' }}</strong>
    </article>
    <article class="quote-header-form__fact">
      <span>{{ t('customer.quote.owner') }}</span>
      <strong>{{ displayOwner }}</strong>
    </article>
    <article class="quote-header-form__fact">
      <span>{{ t('customer.quote.language') }}</span>
      <strong>{{ displayLanguage }}</strong>
    </article>
    <article class="quote-header-form__fact">
      <span>{{ t('customer.quote.validUntil') }}</span>
      <strong>{{ quote.validUntil || '-' }}</strong>
    </article>
  </section>
  <el-form v-else ref="formRef" :model="quote" :rules="rules" label-position="top" class="quote-header-form">
    <el-form-item :label="t('customer.quote.customer')" prop="customerId">
      <el-select v-model="quote.customerId" filterable>
        <el-option v-for="item in customers" :key="item.customerId" :label="item.companyName || item.customerName" :value="String(item.customerId)" />
      </el-select>
    </el-form-item>
    <el-form-item :label="t('customer.quote.project')" prop="projectName">
      <el-input v-model="quote.projectName" maxlength="160" />
    </el-form-item>
    <el-form-item :label="t('customer.quote.owner')">
      <el-select v-model="quote.ownerUserId" filterable>
        <el-option v-for="item in owners" :key="item.userId" :label="item.nickName || item.userName" :value="String(item.userId)" />
      </el-select>
    </el-form-item>
    <el-form-item :label="t('customer.quote.language')">
      <el-segmented v-model="quote.quoteLanguage" :options="languageOptions" />
    </el-form-item>
    <el-form-item :label="t('customer.quote.validUntil')">
      <el-date-picker v-model="quote.validUntil" type="date" value-format="YYYY-MM-DD" />
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
const displayCustomer = computed(() => props.quote.companyName || props.quote.customerName || '-')
const displayOwner = computed(() => {
  const owner = props.owners.find((item) => String(item.userId) === String(props.quote.ownerUserId || ''))
  return owner?.nickName || owner?.userName || props.quote.ownerName || '-'
})
const displayLanguage = computed(() => props.quote.quoteLanguage === 'ZH_CN'
  ? t('customer.quote.languageOption.zh')
  : t('customer.quote.languageOption.en'))
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
.quote-header-form {
  display: grid;
  grid-template-columns: minmax(220px, 1.35fr) minmax(240px, 1.55fr) minmax(160px, 1fr) 180px 180px;
  gap: 10px 14px;
  padding: 14px 18px;
  border: 1px solid #dfe6ef;
  border-radius: 8px;
  background: #fff;
}
.quote-header-form--readonly {
  grid-template-columns: minmax(220px, 1.35fr) minmax(240px, 1.55fr) repeat(3, minmax(140px, 1fr));
  align-items: stretch;
}
.quote-header-form__fact {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 8px;
  padding: 6px 0;
}
.quote-header-form__fact span {
  color: #667085;
  font-size: 12px;
  font-weight: 600;
  line-height: 18px;
}
.quote-header-form__fact strong {
  color: #1d2939;
  font-size: 15px;
  font-weight: 600;
  line-height: 24px;
}
.quote-header-form :deep(.el-form-item) { margin-bottom: 10px; }
.quote-header-form :deep(.el-form-item__label) { margin-bottom: 5px; color: #475467; font-size: 12px; font-weight: 600; line-height: 18px; }
.quote-header-form :deep(.el-select), .quote-header-form :deep(.el-date-editor) { width: 100%; }
@media (max-width: 1440px) {
  .quote-header-form,
  .quote-header-form--readonly {
    grid-template-columns: repeat(3, minmax(180px, 1fr));
  }
}
@media (max-width: 1080px) {
  .quote-header-form,
  .quote-header-form--readonly {
    grid-template-columns: repeat(2, minmax(180px, 1fr));
  }
}
</style>
