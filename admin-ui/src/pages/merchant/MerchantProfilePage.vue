<template>
  <div class="app-container merchant-profile-editor">
    <section class="profile-hero">
      <div class="profile-hero__main">
        <span class="profile-hero__eyebrow">{{ t('merchantProfile.selfTitle') }}</span>
        <div class="profile-hero__title-row">
          <h2>{{ displayValue(form.merchantName) }}</h2>
          <el-tag :type="form.status === '1' ? 'success' : 'info'">{{ merchantStatusText(form.status, t) }}</el-tag>
        </div>
        <p>{{ displayValue(form.companyName) }}</p>
        <div class="profile-hero__meta">
          <span>{{ displayValue(form.primaryEmail) }}</span>
          <span>{{ displayValue(form.country) }}</span>
        </div>
      </div>
      <div class="profile-hero__actions">
        <el-button :loading="loading" icon="Refresh" @click="loadProfile">{{ t('common.refresh') }}</el-button>
      </div>
    </section>

    <div class="profile-layout">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="108px" class="profile-form">
        <section class="profile-section">
          <header>{{ t('merchantProfile.contactSection') }}</header>
          <div class="grid two">
            <el-form-item :label="t('apply.firstName')" prop="contactFirstName"><el-input v-model="form.contactFirstName" /></el-form-item>
            <el-form-item :label="t('apply.lastName')" prop="contactLastName"><el-input v-model="form.contactLastName" /></el-form-item>
            <el-form-item :label="t('merchantProfile.officePhone')" prop="officePhone"><el-input v-model="form.officePhone" /></el-form-item>
            <el-form-item :label="t('merchantProfile.mobilePhone')" prop="mobilePhone"><el-input v-model="form.mobilePhone" /></el-form-item>
          </div>
        </section>

        <section class="profile-section">
          <header>{{ t('merchantProfile.addressSection') }}</header>
          <div class="grid two">
            <el-form-item :label="t('apply.state')" prop="state"><el-input v-model="form.state" /></el-form-item>
            <el-form-item :label="t('apply.city')" prop="city"><el-input v-model="form.city" /></el-form-item>
            <el-form-item :label="t('apply.addressLine1')" prop="addressLine1"><el-input v-model="form.addressLine1" /></el-form-item>
            <el-form-item :label="t('apply.addressLine2')" prop="addressLine2"><el-input v-model="form.addressLine2" /></el-form-item>
            <el-form-item :label="t('apply.postalCode')" prop="postalCode"><el-input v-model="form.postalCode" /></el-form-item>
            <el-form-item :label="t('merchantProfile.remark')" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="3" /></el-form-item>
          </div>
        </section>

        <div class="form-actions">
          <el-button type="primary" :loading="saving" @click="submit">{{ t('common.save') }}</el-button>
        </div>
      </el-form>

      <aside class="profile-side">
        <section class="profile-side__panel">
          <header>
            <strong>{{ t('merchantLevel.name') }}</strong>
            <span>{{ displayValue(form.levelCode) }}</span>
          </header>
          <div class="level-name">{{ displayValue(form.levelName) }}</div>
          <div class="metric-grid">
            <div><span>{{ t('merchantLevel.discount') }}</span><strong>{{ formatDiscountRate(form.discountRate) }}</strong></div>
            <div><span>{{ t('merchantLevel.credit') }}</span><strong>{{ formatCreditLimit(form.creditLimit) }}</strong></div>
          </div>
        </section>

        <section class="profile-side__panel">
          <header><strong>{{ t('merchantProfile.legalSection') }}</strong></header>
          <dl class="profile-facts">
            <div><dt>{{ t('merchantProfile.merchantName') }}</dt><dd>{{ displayValue(form.merchantName) }}</dd></div>
            <div><dt>{{ t('merchantProfile.companyName') }}</dt><dd>{{ displayValue(form.companyName) }}</dd></div>
            <div><dt>{{ t('merchantProfile.primaryEmail') }}</dt><dd>{{ displayValue(form.primaryEmail) }}</dd></div>
            <div><dt>{{ t('merchantProfile.country') }}</dt><dd>{{ displayValue(form.country) }}</dd></div>
          </dl>
        </section>

        <p class="profile-side__hint">{{ t('merchantProfile.lockedHint') }}</p>
      </aside>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { getCurrentMerchantProfile, updateCurrentMerchantProfile, type MerchantProfile } from '@/api/merchant/profile'
import { formatCreditLimit, formatDiscountRate, merchantStatusText } from '@/api/merchant/format'

const { t } = useI18n()
const formRef = ref<FormInstance>()
const loading = ref(false)
const saving = ref(false)
const form = ref<MerchantProfile>({})

const rules = computed<FormRules>(() => ({
  contactFirstName: [{ max: 50, message: t('merchantProfile.max50'), trigger: 'blur' }],
  contactLastName: [{ max: 50, message: t('merchantProfile.max50'), trigger: 'blur' }],
  officePhone: [{ max: 50, message: t('merchantProfile.max50'), trigger: 'blur' }],
  mobilePhone: [{ max: 50, message: t('merchantProfile.max50'), trigger: 'blur' }],
  state: [{ max: 50, message: t('merchantProfile.max50'), trigger: 'blur' }],
  city: [{ max: 50, message: t('merchantProfile.max50'), trigger: 'blur' }],
  addressLine1: [{ max: 255, message: t('merchantProfile.max255'), trigger: 'blur' }],
  addressLine2: [{ max: 255, message: t('merchantProfile.max255'), trigger: 'blur' }],
  postalCode: [{ max: 20, message: t('merchantProfile.max20'), trigger: 'blur' }],
  remark: [{ max: 500, message: t('merchantProfile.max500'), trigger: 'blur' }]
}))

async function loadProfile() {
  loading.value = true
  try {
    const response = await getCurrentMerchantProfile()
    form.value = response.data || {}
  } finally {
    loading.value = false
  }
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    await updateCurrentMerchantProfile(buildEditablePayload())
    ElMessage.success(t('common.editSuccess'))
    await loadProfile()
  } finally {
    saving.value = false
  }
}

function buildEditablePayload(): MerchantProfile {
  const contactFirstName = form.value.contactFirstName?.trim() || ''
  const contactLastName = form.value.contactLastName?.trim() || ''
  return {
    contactFirstName,
    contactLastName,
    contactName: [contactFirstName, contactLastName].filter(Boolean).join(' '),
    officePhone: form.value.officePhone,
    mobilePhone: form.value.mobilePhone,
    state: form.value.state,
    city: form.value.city,
    addressLine1: form.value.addressLine1,
    addressLine2: form.value.addressLine2,
    postalCode: form.value.postalCode,
    remark: form.value.remark
  }
}

function displayValue(value?: string | number | null) {
  return value === null || value === undefined || value === '' ? '-' : String(value)
}

loadProfile()
</script>

<style scoped lang="scss" src="./styles/merchant-profile-editor.scss"></style>
