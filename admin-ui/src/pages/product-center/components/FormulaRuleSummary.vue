<template>
  <div class="formula-rule-summary">
    <div>
      <strong>{{ t('productCenter.price.formulaPanel') }}</strong>
      <span>{{ t('productCenter.price.formulaHint') }}</span>
    </div>
    <el-button size="small" link type="primary">{{ t('common.edit') }}</el-button>
  </div>
</template>

<script setup lang="ts">
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
</script>

<style scoped lang="scss">
.formula-rule-summary {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-top: 12px;
  padding: 12px;
  border-radius: 8px;
  background: #f8fafc;

  strong,
  span {
    display: block;
  }

  strong {
    color: #111827;
  }

  span {
    margin-top: 4px;
    color: #64748b;
    font-size: 12px;
  }
}
</style>
