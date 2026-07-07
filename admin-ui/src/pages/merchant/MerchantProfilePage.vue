<template>
  <div class="app-container merchant-profile-editor">
    <section class="profile-toolbar">
      <span class="profile-toolbar__avatar"><el-icon><OfficeBuilding /></el-icon></span>
      <div class="profile-toolbar__title">
        <h2>{{ t('merchantProfile.selfTitle') }}</h2>
        <div class="profile-toolbar__meta">
          <span>{{ displayValue(form.merchantName) }}</span>
          <el-tag :type="form.status === '1' ? 'success' : 'info'">{{ merchantStatusText(form.status, t) }}</el-tag>
          <span>{{ t('common.updateTime') }} {{ displayDate(form.updateTime || form.createTime) }}</span>
        </div>
      </div>
      <div class="profile-toolbar__actions">
        <el-button :loading="loading" :icon="Refresh" @click="loadProfile">{{ t('common.refresh') }}</el-button>
        <el-button type="primary" :icon="DocumentChecked" :loading="saving" @click="submit">{{ t('common.save') }}</el-button>
      </div>
    </section>

    <div class="profile-workspace">
      <aside class="section-rail">
        <span class="section-rail__item is-active"><el-icon><OfficeBuilding /></el-icon>{{ t('merchantProfile.legalSection') }}</span>
        <span class="section-rail__item"><el-icon><UserFilled /></el-icon>{{ t('merchantProfile.contactSection') }}</span>
        <span class="section-rail__item"><el-icon><Location /></el-icon>{{ t('merchantProfile.addressSection') }}</span>
      </aside>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="profile-form">
        <section class="profile-section profile-section--readonly">
          <header><span class="section-title"><el-icon><OfficeBuilding /></el-icon>{{ t('merchantProfile.legalSection') }}</span></header>
          <div class="readonly-grid">
            <div><el-icon><OfficeBuilding /></el-icon><span><em>{{ t('merchantProfile.merchantName') }}</em><strong>{{ displayValue(form.merchantName) }}</strong></span></div>
            <div><el-icon><Postcard /></el-icon><span><em>{{ t('merchantProfile.companyName') }}</em><strong>{{ displayValue(form.companyName) }}</strong></span></div>
            <div><el-icon><Message /></el-icon><span><em>{{ t('merchantProfile.primaryEmail') }}</em><strong>{{ displayValue(form.primaryEmail) }}</strong></span></div>
            <div><el-icon><Location /></el-icon><span><em>{{ t('merchantProfile.country') }}</em><strong>{{ displayValue(form.country) }}</strong></span></div>
          </div>
        </section>

        <section class="profile-section">
          <header>
            <span class="section-title"><el-icon><UserFilled /></el-icon>{{ t('merchantProfile.contactSection') }}</span>
            <small>{{ displayValue(contactFullName()) }}</small>
          </header>
          <div class="grid two">
            <el-form-item :label="t('apply.firstName')" prop="contactFirstName"><el-input v-model="form.contactFirstName"><template #prefix><el-icon><UserFilled /></el-icon></template></el-input></el-form-item>
            <el-form-item :label="t('apply.lastName')" prop="contactLastName"><el-input v-model="form.contactLastName"><template #prefix><el-icon><UserFilled /></el-icon></template></el-input></el-form-item>
            <el-form-item :label="t('merchantProfile.officePhone')" prop="officePhone"><el-input v-model="form.officePhone"><template #prefix><el-icon><Phone /></el-icon></template></el-input></el-form-item>
            <el-form-item :label="t('merchantProfile.mobilePhone')" prop="mobilePhone"><el-input v-model="form.mobilePhone"><template #prefix><el-icon><Phone /></el-icon></template></el-input></el-form-item>
          </div>
        </section>

        <section class="profile-section">
          <header>
            <span class="section-title"><el-icon><Location /></el-icon>{{ t('merchantProfile.addressSection') }}</span>
            <small>{{ addressPreview() }}</small>
          </header>
          <div class="grid two">
            <el-form-item :label="t('apply.state')" prop="state"><el-input v-model="form.state"><template #prefix><el-icon><Location /></el-icon></template></el-input></el-form-item>
            <el-form-item :label="t('apply.city')" prop="city"><el-input v-model="form.city"><template #prefix><el-icon><Location /></el-icon></template></el-input></el-form-item>
            <el-form-item :label="t('apply.addressLine1')" prop="addressLine1"><el-input v-model="form.addressLine1"><template #prefix><el-icon><Location /></el-icon></template></el-input></el-form-item>
            <el-form-item :label="t('apply.addressLine2')" prop="addressLine2"><el-input v-model="form.addressLine2"><template #prefix><el-icon><Location /></el-icon></template></el-input></el-form-item>
            <el-form-item :label="t('apply.postalCode')" prop="postalCode"><el-input v-model="form.postalCode"><template #prefix><el-icon><Postcard /></el-icon></template></el-input></el-form-item>
            <el-form-item :label="t('merchantProfile.remark')" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="3" /></el-form-item>
          </div>
        </section>

        <div class="form-actions">
          <el-button @click="loadProfile">{{ t('common.reset') }}</el-button>
          <el-button type="primary" :loading="saving" @click="submit">{{ t('common.save') }}</el-button>
        </div>
      </el-form>

      <aside class="preview-panel">
        <section class="preview-card">
          <header><span class="card-icon"><el-icon><View /></el-icon></span>{{ t('common.preview') }}</header>
          <strong>{{ displayValue(form.companyName || form.merchantName) }}</strong>
          <p><el-icon><UserFilled /></el-icon>{{ displayValue(contactFullName()) }}</p>
          <p><el-icon><Message /></el-icon>{{ displayValue(form.primaryEmail) }}</p>
          <p><el-icon><Phone /></el-icon>{{ displayValue(form.officePhone || form.mobilePhone) }}</p>
        </section>

        <section class="preview-card">
          <header><span class="card-icon"><el-icon><Medal /></el-icon></span>{{ t('merchantLevel.name') }}</header>
          <strong>{{ displayValue(form.levelName) }}</strong>
          <div class="metric-grid">
            <div><el-icon><Money /></el-icon><span>{{ t('merchantLevel.discount') }}</span><b>{{ formatDiscountRate(form.discountRate) }}</b></div>
            <div><el-icon><Postcard /></el-icon><span>{{ t('merchantLevel.credit') }}</span><b>{{ formatCreditLimit(form.creditLimit) }}</b></div>
          </div>
        </section>

        <p class="preview-hint">{{ t('merchantProfile.lockedHint') }}</p>
      </aside>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { DocumentChecked, Location, Medal, Message, Money, OfficeBuilding, Phone, Postcard, Refresh, UserFilled, View } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import { getCurrentMerchantProfile, updateCurrentMerchantProfile, type MerchantProfile } from '@/api/merchant/profile'
import { formatCreditLimit, formatDiscountRate, merchantStatusText } from '@/api/merchant/format'
import { formatUtc } from '@/utils/datetime'

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

function displayDate(value?: string | null) {
  return value ? formatUtc(value, 'YYYY-MM-DD HH:mm') : '-'
}

function contactFullName() {
  return [form.value.contactFirstName, form.value.contactLastName].filter(Boolean).join(' ')
}

function addressPreview() {
  return [form.value.city, form.value.state, form.value.postalCode].filter(Boolean).join(' / ') || '-'
}

loadProfile()
</script>

<style scoped lang="scss" src="./styles/merchant-profile-editor.scss"></style>
