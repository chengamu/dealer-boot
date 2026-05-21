<template>
  <ExternalFrame :src="url" :title="t('external.adminTitle')" :empty-text="t('external.missingUrl')" />
</template>

<script setup lang="ts" name="AdminMonitorPage">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import ExternalFrame from '@/components/ExternalFrame.vue'

const { t } = useI18n()
const url = computed(() => window.__APP_CONFIG__?.VITE_APP_MONITRO_ADMIN || import.meta.env.VITE_APP_MONITRO_ADMIN || '/admin/applications')
</script>
