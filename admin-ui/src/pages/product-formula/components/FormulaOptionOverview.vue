<template>
  <div class="option-overview">
    <div class="option-overview__card option-overview__card--option">
      <span class="option-overview__icon"><Operation /></span>
      <span class="option-overview__label">{{ t('productCenter.formulaSetup.optionCount') }}</span>
      <strong>{{ optionCount }}</strong>
    </div>
    <div class="option-overview__card option-overview__card--value">
      <span class="option-overview__icon"><List /></span>
      <span class="option-overview__label">{{ t('productCenter.formulaSetup.optionValueCount') }}</span>
      <strong>{{ optionValueCount }}</strong>
    </div>
    <div class="option-overview__card option-overview__card--material">
      <span class="option-overview__icon"><Connection /></span>
      <span class="option-overview__label">{{ t('productCenter.formulaSetup.linkedMaterial') }}</span>
      <strong>{{ linkedMaterialCount }}</strong>
    </div>
    <div class="option-overview__card option-overview__card--restriction">
      <span class="option-overview__icon"><Lock /></span>
      <span class="option-overview__label">{{ t('productCenter.formulaSetup.restrictionCount') }}</span>
      <strong>{{ restrictionCount }}</strong>
    </div>
    <div class="option-overview__card option-overview__card--exception">
      <span class="option-overview__icon"><WarningFilled /></span>
      <span class="option-overview__label">{{ t('productCenter.formulaSetup.businessExceptionCount') }}</span>
      <strong>{{ exceptionCount }}</strong>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Connection, List, Lock, Operation, WarningFilled } from '@element-plus/icons-vue'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'

defineProps<{
  optionCount: number
  optionValueCount: number
  linkedMaterialCount: number
  restrictionCount: number
  exceptionCount: number
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
</script>

<style scoped>
.option-overview {
  display: grid;
  grid-template-columns: repeat(5, minmax(150px, 1fr));
  gap: 14px;
  margin-bottom: 12px;
  padding: 16px;
  background: #fff;
  border: 1px solid #e6ebf2;
  border-radius: 8px;
}

.option-overview__card {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 72px;
  padding: 12px 16px;
  background: linear-gradient(180deg, #fff, #f8fbff);
  border: 1px solid #e6ebf2;
  border-radius: 8px;
  box-shadow: 0 4px 14px rgb(15 23 42 / 3%);
}

.option-overview__icon {
  display: inline-flex;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  color: #1677ff;
  font-size: 24px;
  background: #f5f9ff;
  border-radius: 10px;
}

.option-overview__label {
  color: #1f2937;
  font-weight: 600;
}

.option-overview__card strong {
  margin-left: auto;
  color: #111827;
  font-size: 24px;
  line-height: 1;
}

.option-overview__card--value .option-overview__icon {
  color: #22c55e;
}

.option-overview__card--material .option-overview__icon {
  color: #8b5cf6;
}

.option-overview__card--restriction .option-overview__icon {
  color: #f59e0b;
}

.option-overview__card--exception .option-overview__icon,
.option-overview__card--exception strong {
  color: #dc2626;
}
</style>
