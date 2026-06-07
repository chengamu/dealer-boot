<template>
  <div class="dealer-preview-panel">
    <el-tabs :model-value="active" @update:model-value="$emit('update:active', String($event))">
      <el-tab-pane :label="t('productCenter.template.dealerPreview')" name="dealer">
        <el-empty v-if="!result.resultStatus" :description="t('productCenter.template.previewEmpty')" />
        <div v-else class="dealer-preview-panel__card">
          <div>
            <strong>{{ t('productCenter.template.dealerPreview') }}</strong>
            <span>{{ t('productCenter.template.availableOptions') }}：{{ availableOptions.length }}</span>
            <span>{{ t('productCenter.template.visibleQuestions') }}：{{ visibleQuestions.length }}</span>
          </div>
          <el-button size="small" icon="View">{{ t('common.preview') }}</el-button>
        </div>
      </el-tab-pane>
      <el-tab-pane :label="t('productCenter.template.checks')" name="checks">
        <div class="dealer-preview-panel__list">
          <span>{{ t('productCenter.template.blockers') }}：{{ blockers.length }}</span>
          <span>{{ t('productCenter.template.warnings') }}：{{ warnings.length }}</span>
          <span>{{ t('productCenter.template.resultStatus') }}：{{ result.resultStatus || '-' }}</span>
        </div>
      </el-tab-pane>
      <el-tab-pane :label="t('productCenter.template.bomPreview')" name="bom">
        <el-empty v-if="!autoComponents.length" :description="t('productCenter.template.previewEmpty')" />
        <div v-else class="dealer-preview-panel__bom">
          <div v-for="(item, index) in autoComponents" :key="index" class="dealer-preview-panel__bom-row">
            <strong>{{ displayText(item, 'componentName') || displayText(item, 'name') || displayText(item, 'optionNameCn') || displayText(item, 'optionCode') }}</strong>
            <span>{{ displayText(item, 'questionNameCn') || displayText(item, 'questionCode') }} / {{ displayText(item, 'optionCode') }}</span>
            <small v-if="displayText(item, 'quantity')">{{ t('productCenter.common.quantity') }}：{{ displayText(item, 'quantity') }}</small>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import type { ProductRecord } from '@/api/product-capability/types'

const props = defineProps<{
  result: ProductRecord
  active: string
}>()

defineEmits<{
  'update:active': [active: string]
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)

const blockers = computed(() => (props.result.blockers as unknown[] | undefined) || [])
const warnings = computed(() => (props.result.warnings as unknown[] | undefined) || [])
const availableOptions = computed(() => (props.result.availableOptions as unknown[] | undefined) || [])
const visibleQuestions = computed(() => (props.result.visibleQuestions as unknown[] | undefined) || [])
const autoComponents = computed(() => (props.result.autoComponents as Record<string, unknown>[] | undefined) || [])

function displayText(item: Record<string, unknown>, key: string) {
  const value = item[key]
  return value === undefined || value === null ? '' : String(value)
}
</script>

<style scoped lang="scss">
.dealer-preview-panel {
  margin-top: 12px;
}

.dealer-preview-panel__card {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 14px;
  border: 1px solid #e5efff;
  border-radius: 8px;
  background: #f8fbff;

  strong,
  span {
    display: block;
  }

  strong {
    margin-bottom: 8px;
    color: #111827;
  }

  span {
    color: #64748b;
    font-size: 12px;
  }
}

.dealer-preview-panel__list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  color: #475569;
  font-size: 13px;
}

.dealer-preview-panel__bom {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 10px;
}

.dealer-preview-panel__bom-row {
  display: flex;
  min-height: 74px;
  flex-direction: column;
  justify-content: center;
  gap: 4px;
  padding: 12px;
  border: 1px solid #e5efff;
  border-radius: 8px;
  background: #f8fbff;

  strong {
    color: #111827;
    font-size: 14px;
  }

  span,
  small {
    color: #64748b;
    font-size: 12px;
  }
}
</style>
