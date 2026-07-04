<template>
  <div class="app-container merchant-profile-editor">
    <el-card shadow="never" class="merchant-profile-editor__card">
      <template #header>
        <div class="page-header">
          <span>{{ t('merchantProfile.selfTitle') }}</span>
          <el-button :loading="loading" icon="Refresh" @click="loadProfile">{{ t('common.refresh') }}</el-button>
        </div>
      </template>

      <el-alert :title="t('merchantProfile.lockedHint')" type="info" show-icon :closable="false" class="mb16" />

      <el-form ref="formRef" :model="form" :rules="rules" label-width="150px" class="profile-form">
        <el-divider content-position="left">{{ t('merchantProfile.legalSection') }}</el-divider>
        <div class="grid two">
          <el-form-item :label="t('merchantProfile.merchantName')">
            <el-input v-model="form.merchantName" disabled />
          </el-form-item>
          <el-form-item :label="t('merchantProfile.companyName')">
            <el-input v-model="form.companyName" disabled />
          </el-form-item>
          <el-form-item :label="t('merchantProfile.primaryEmail')">
            <el-input v-model="form.primaryEmail" disabled />
          </el-form-item>
          <el-form-item :label="t('merchantProfile.country')">
            <el-input v-model="form.country" disabled />
          </el-form-item>
        </div>

        <el-divider content-position="left">{{ t('merchantProfile.contactSection') }}</el-divider>
        <div class="grid two">
          <el-form-item :label="t('apply.firstName')" prop="contactFirstName">
            <el-input v-model="form.contactFirstName" />
          </el-form-item>
          <el-form-item :label="t('apply.lastName')" prop="contactLastName">
            <el-input v-model="form.contactLastName" />
          </el-form-item>
          <el-form-item :label="t('merchantProfile.officePhone')" prop="officePhone">
            <el-input v-model="form.officePhone" />
          </el-form-item>
          <el-form-item :label="t('merchantProfile.mobilePhone')" prop="mobilePhone">
            <el-input v-model="form.mobilePhone" />
          </el-form-item>
        </div>

        <el-divider content-position="left">{{ t('merchantProfile.addressSection') }}</el-divider>
        <div class="grid two">
          <el-form-item :label="t('apply.state')" prop="state">
            <el-input v-model="form.state" />
          </el-form-item>
          <el-form-item :label="t('apply.city')" prop="city">
            <el-input v-model="form.city" />
          </el-form-item>
          <el-form-item :label="t('apply.addressLine1')" prop="addressLine1">
            <el-input v-model="form.addressLine1" />
          </el-form-item>
          <el-form-item :label="t('apply.addressLine2')" prop="addressLine2">
            <el-input v-model="form.addressLine2" />
          </el-form-item>
          <el-form-item :label="t('apply.postalCode')" prop="postalCode">
            <el-input v-model="form.postalCode" />
          </el-form-item>
          <el-form-item :label="t('merchantProfile.remark')" prop="remark">
            <el-input v-model="form.remark" type="textarea" :rows="3" />
          </el-form-item>
        </div>

        <div class="form-actions">
          <el-button type="primary" :loading="saving" @click="submit">{{ t('common.save') }}</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { getCurrentMerchantProfile, updateCurrentMerchantProfile, type MerchantProfile } from '@/api/merchant/profile'

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

loadProfile()
</script>

<style scoped lang="scss">
.merchant-profile-editor {
  background: transparent;
}

.merchant-profile-editor__card {
  border-color: #eef0f5;
  border-radius: 8px;

  :deep(.el-card__header) {
    min-height: 42px;
    padding: 10px 12px;
    border-bottom-color: #eef0f5;
  }

  :deep(.el-card__body) {
    padding: 12px;
  }
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
}

.mb16 {
  margin-bottom: 10px;
}

.grid {
  display: grid;
  gap: 0 16px;
}

.two {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  padding-top: 4px;
  gap: 8px;
}

.profile-form {
  :deep(.el-divider) {
    margin: 12px 0;
  }

  :deep(.el-divider__text) {
    color: #1d2129;
    font-size: 14px;
    font-weight: 600;
  }

  :deep(.el-form-item) {
    margin-bottom: 12px;
  }

  :deep(.el-form-item__label) {
    color: #475467;
    font-weight: 500;
  }

  :deep(.el-input__wrapper),
  :deep(.el-select__wrapper) {
    min-height: 32px;
  }

  :deep(.el-button) {
    height: 32px;
    padding: 0 12px;
    border-radius: 6px;
  }
}

@media (max-width: 900px) {
  .two {
    grid-template-columns: 1fr;
  }

  .profile-form {
    :deep(.el-form-item) {
      display: block;
    }
  }
}
</style>
