<template>
  <div class="admin-detail">
    <section class="admin-detail__section">
      <h3 class="admin-detail__section-title">{{ t('merchantProfile.detailTitle') }}</h3>
      <dl class="admin-detail__grid">
        <div class="admin-detail__item"><dt>{{ t('merchantProfile.merchantName') }}</dt><dd>{{ profile.merchantName || '-' }}</dd></div>
        <div class="admin-detail__item"><dt>{{ t('merchantProfile.companyName') }}</dt><dd>{{ profile.companyName || '-' }}</dd></div>
        <div class="admin-detail__item"><dt>{{ t('merchantProfile.contactName') }}</dt><dd>{{ profile.contactName || '-' }}</dd></div>
        <div class="admin-detail__item"><dt>{{ t('merchantProfile.primaryEmail') }}</dt><dd>{{ profile.primaryEmail || '-' }}</dd></div>
        <div class="admin-detail__item"><dt>{{ t('merchantProfile.officePhone') }}</dt><dd>{{ profile.officePhone || '-' }}</dd></div>
        <div class="admin-detail__item"><dt>{{ t('merchantProfile.mobilePhone') }}</dt><dd>{{ profile.mobilePhone || '-' }}</dd></div>
        <div class="admin-detail__item"><dt>{{ t('merchantProfile.country') }}</dt><dd>{{ profile.country || '-' }}</dd></div>
        <div class="admin-detail__item"><dt>{{ t('merchantProfile.status') }}</dt><dd>{{ profile.status === '1' ? t('common.normal') : t('common.disabled') }}</dd></div>
        <div class="admin-detail__item admin-detail__item--full"><dt>{{ t('merchantProfile.address') }}</dt><dd>{{ address || '-' }}</dd></div>
        <div class="admin-detail__item"><dt>{{ t('merchantProfile.auditStatus') }}</dt><dd>{{ auditStatusText }}</dd></div>
        <div class="admin-detail__item"><dt>{{ t('merchantProfile.auditTime') }}</dt><dd>{{ formatUtc(profile.auditTime) }}</dd></div>
        <div class="admin-detail__item admin-detail__item--full"><dt>{{ t('merchantProfile.remark') }}</dt><dd class="admin-detail__value--long">{{ profile.remark || '-' }}</dd></div>
      </dl>
    </section>
  </div>
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
