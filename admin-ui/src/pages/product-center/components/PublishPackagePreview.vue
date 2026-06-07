<template>
  <div class="publish-package-preview">
    <div class="publish-package-preview__head">
      <h2>{{ t('productCenter.publish.packagePreview') }}</h2>
      <el-tag type="success" effect="light">{{ execution.resultStatus || 'READY' }}</el-tag>
    </div>
    <div class="publish-package-preview__body">
      <strong>{{ form.productModelCode || t('productCenter.publish.packageCode') }}</strong>
      <span>{{ t('productCenter.template.versionNo') }}：{{ form.templateVersionNo || '-' }}</span>
      <span>{{ t('productCenter.price.planCode') }}：{{ form.pricePlanCode || '-' }}</span>
      <span>{{ t('productCenter.publish.packageHash') }}：{{ execution.packageHash || '-' }}</span>
      <span>{{ t('productCenter.publish.approvalStatus') }}：{{ approval.approvalStatus || '-' }}</span>
    </div>
    <el-space fill class="publish-package-preview__actions">
      <slot name="actions" />
    </el-space>
    <el-alert v-if="execution.resultStatus === 'PUBLISHED'" :title="t('productCenter.publish.published')" type="success" show-icon :closable="false" />
  </div>
</template>

<script setup lang="ts">
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import type { ProductRecord } from '@/api/product-capability/types'

defineProps<{
  form: ProductRecord
  execution: ProductRecord
  approval: ProductRecord
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
</script>

<style scoped lang="scss">
.publish-package-preview {
  margin-bottom: 12px;
  padding: 14px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  background: var(--el-bg-color);
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.04);
}

.publish-package-preview__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;

  h2 {
    margin: 0;
    font-size: 16px;
    font-weight: 650;
  }
}

.publish-package-preview__body {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 14px;
  border: 1px solid #e5efff;
  border-radius: 8px;
  background: #f8fbff;

  strong {
    color: #111827;
    font-size: 16px;
  }

  span {
    color: #64748b;
    font-size: 12px;
  }
}

.publish-package-preview__actions {
  width: 100%;
  margin: 12px 0;

  :deep(.el-button) {
    width: 100%;
  }
}
</style>
