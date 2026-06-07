<template>
  <el-row :gutter="12" class="readonly-ability-cards">
    <el-col v-for="item in items" :key="item.key" :xs="24" :sm="12" :xl="6">
      <div class="readonly-ability-cards__item">
        <div>
          <h3>{{ t(item.titleKey) }}</h3>
          <el-tag :type="item.type" effect="light">{{ item.status }}</el-tag>
        </div>
        <p>{{ t(item.hintKey) }}</p>
        <el-button link type="primary">{{ t('productCenter.sales.viewDetail') }}</el-button>
      </div>
    </el-col>
  </el-row>
</template>

<script setup lang="ts">
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'

defineProps<{
  items: Array<{ key: string; titleKey: string; hintKey: string; type: 'success' | 'warning' | 'danger' | 'info'; status: string }>
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
</script>

<style scoped lang="scss">
.readonly-ability-cards__item {
  min-height: 136px;
  padding: 14px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.04);

  div {
    display: flex;
    justify-content: space-between;
    gap: 12px;
  }

  h3 {
    margin: 0;
    font-size: 15px;
  }

  p {
    min-height: 40px;
    margin: 12px 0 4px;
    color: #64748b;
    font-size: 12px;
    line-height: 1.6;
  }
}
</style>
