<template>
  <section class="quote-pdf-preview-card">
    <header class="quote-pdf-preview-card__header">
      <strong>{{ t('customer.quote.action.pdf') }}</strong>
      <div class="quote-pdf-preview-card__actions">
        <el-button text @click="$emit('new-window')">{{ t('customer.quote.preview.newWindow') }}</el-button>
        <el-button text @click="$emit('print')">{{ t('customer.quote.action.print') }}</el-button>
        <el-button text @click="$emit('close')">{{ t('common.close') }}</el-button>
      </div>
    </header>
    <iframe v-if="src" :src="src" class="quote-pdf-preview-card__frame" :title="t('customer.quote.action.pdf')" />
    <el-empty v-else :description="t('customer.quote.preview.empty')" :image-size="48" />
  </section>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'

defineProps<{ src?: string }>()
defineEmits<{ close: []; print: []; 'new-window': [] }>()

const { t } = useI18n()
</script>

<style scoped>
.quote-pdf-preview-card {
  display: flex;
  min-height: 720px;
  flex-direction: column;
  border: 1px solid #dfe6ef;
  border-radius: 8px;
  background: #fff;
}
.quote-pdf-preview-card__header,
.quote-pdf-preview-card__actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
.quote-pdf-preview-card__header {
  justify-content: space-between;
  padding: 10px 12px;
  border-bottom: 1px solid #edf1f6;
}
.quote-pdf-preview-card__frame {
  flex: 1;
  width: 100%;
  border: 0;
}
</style>
