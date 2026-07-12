<template>
  <section class="sales-document-pdf-preview">
    <header class="sales-document-pdf-preview__header">
      <strong>{{ t('dealer.sales.orderPdf') }}</strong>
      <div class="sales-document-pdf-preview__actions">
        <el-button text @click="$emit('new-window')">{{ t('dealer.sales.preview.newWindow') }}</el-button>
        <el-button text @click="$emit('print')">{{ t('dealer.sales.preview.print') }}</el-button>
        <el-button text @click="$emit('close')">{{ t('common.close') }}</el-button>
      </div>
    </header>
    <iframe v-if="src" :src="src" class="sales-document-pdf-preview__frame" :title="t('dealer.sales.orderPdf')" />
    <el-empty v-else :description="t('dealer.sales.preview.empty')" :image-size="48" />
  </section>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'

defineProps<{ src?: string }>()
defineEmits<{ close: []; print: []; 'new-window': [] }>()
const { t } = useI18n()
</script>

<style scoped>
.sales-document-pdf-preview { display: flex; min-height: 720px; flex-direction: column; border: 1px solid #e4eaf2; border-radius: 8px; background: #fff; }
.sales-document-pdf-preview__header,
.sales-document-pdf-preview__actions { display: flex; align-items: center; gap: 8px; }
.sales-document-pdf-preview__header { justify-content: space-between; padding: 10px 12px; border-bottom: 1px solid #edf1f6; }
.sales-document-pdf-preview__frame { flex: 1; width: 100%; border: 0; }
</style>
