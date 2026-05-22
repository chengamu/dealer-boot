<template>
  <ExternalFrame :src="url" :title="t('external.xxlJobTitle')" :empty-text="t('external.missingUrl')" />
</template>

<script setup lang="ts" name="XxlJobPage">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import ExternalFrame from '@/components/ExternalFrame.vue'
import { getXxlJobUrl } from '@/utils/config'

const { t } = useI18n()
const url = computed(() => getXxlJobUrl())
</script>
