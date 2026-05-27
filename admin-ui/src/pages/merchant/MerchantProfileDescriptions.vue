<template>
  <el-descriptions :column="1" border>
    <el-descriptions-item :label="t('merchantProfile.merchantName')">{{ profile.merchantName || '-' }}</el-descriptions-item>
    <el-descriptions-item :label="t('merchantProfile.companyName')">{{ profile.companyName || '-' }}</el-descriptions-item>
    <el-descriptions-item :label="t('merchantProfile.contactName')">{{ profile.contactName || '-' }}</el-descriptions-item>
    <el-descriptions-item :label="t('merchantProfile.primaryEmail')">{{ profile.primaryEmail || '-' }}</el-descriptions-item>
    <el-descriptions-item :label="t('merchantProfile.officePhone')">{{ profile.officePhone || '-' }}</el-descriptions-item>
    <el-descriptions-item :label="t('merchantProfile.mobilePhone')">{{ profile.mobilePhone || '-' }}</el-descriptions-item>
    <el-descriptions-item :label="t('merchantProfile.country')">{{ profile.country || '-' }}</el-descriptions-item>
    <el-descriptions-item :label="t('merchantProfile.address')">{{ address || '-' }}</el-descriptions-item>
    <el-descriptions-item :label="t('merchantProfile.status')">{{ profile.status === '1' ? t('common.normal') : t('common.disabled') }}</el-descriptions-item>
    <el-descriptions-item :label="t('merchantProfile.auditStatus')">{{ auditStatusText }}</el-descriptions-item>
    <el-descriptions-item :label="t('merchantProfile.auditTime')">{{ formatUtc(profile.auditTime) }}</el-descriptions-item>
    <el-descriptions-item :label="t('merchantProfile.remark')">{{ profile.remark || '-' }}</el-descriptions-item>
  </el-descriptions>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type { MerchantProfile } from '@/api/merchant/profile'
import { formatUtc } from '@/utils/datetime'

const props = defineProps<{ profile: MerchantProfile }>()
const { t } = useI18n()
const address = computed(() =>
  [props.profile.addressLine1, props.profile.addressLine2, props.profile.city, props.profile.state, props.profile.postalCode].filter(Boolean).join(', ')
)
const auditStatusText = computed(() => {
  if (!props.profile.auditStatus) return '-'
  const key = `merchantProfile.auditStatusMap.${props.profile.auditStatus}`
  const text = t(key)
  return text === key ? props.profile.auditStatus : text
})
</script>
