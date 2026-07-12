<template>
  <section class="shipping-template-rules">
    <header class="shipping-template-rules__header">
      <div>
        <h3>{{ t('productCenter.shippingTemplate.rules') }}</h3>
        <p>{{ t('productCenter.shippingTemplate.rulesHint') }}</p>
      </div>
      <div class="shipping-template-rules__actions">
        <el-button type="primary" plain icon="Upload" @click="importOpen = true">
          {{ t('productCenter.shippingTemplate.importRules') }}
        </el-button>
        <el-button type="primary" plain icon="Plus" @click="addRule">
          {{ t('productCenter.shippingTemplate.addRule') }}
        </el-button>
      </div>
    </header>

    <el-table :data="modelValue" border max-height="430" :empty-text="t('productCenter.common.empty')">
      <el-table-column type="index" :label="t('common.index')" width="58" align="center" />
      <el-table-column :label="t('productCenter.shippingTemplate.condition')" min-width="250">
        <template #default="{ row }">
          <el-select v-model="row.feeCode" @change="syncFeeName(row)">
            <el-option :label="t('productCenter.shippingTemplate.manualCondition')" value="MANUAL" />
            <el-option :label="t('productCenter.shippingTemplate.motorizedCondition')" value="MOTORIZED" />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column :label="t('productCenter.shippingTemplate.areaRange')" width="260">
        <template #default="{ row }">
          <div class="shipping-template-rules__range">
            <BusinessNumberInput v-model="row.minAreaSqft" mode="QUANTITY" :min="0" :unit-precision="4" />
            <span>-</span>
            <BusinessNumberInput v-model="row.maxAreaSqft" mode="QUANTITY" :min="0" :unit-precision="4" :placeholder="t('productCenter.shippingTemplate.unlimited')" />
          </div>
        </template>
      </el-table-column>
      <el-table-column :label="t('productCenter.shippingTemplate.feeAmount')" width="180" align="right">
        <template #default="{ row }">
          <BusinessNumberInput v-model="row.feeAmount" mode="UNIT_PRICE" :min="0" :max-fraction-digits="4" />
        </template>
      </el-table-column>
      <el-table-column :label="t('productCenter.common.remark')" min-width="220">
        <template #default="{ row }">
          <el-input v-model="row.remark" :placeholder="t('productCenter.common.inputPlaceholder')" />
        </template>
      </el-table-column>
      <el-table-column :label="t('common.operate')" width="84" align="center" fixed="right">
        <template #default="{ $index }">
          <el-button link type="danger" @click="removeRule($index)">{{ t('common.delete') }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <ShippingRuleImportDialog v-model="importOpen" @imported="applyImportedRules" />
  </section>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import type { ShippingTemplateRuleVO } from '@/api/product-pricing/types'
import ShippingRuleImportDialog from './ShippingRuleImportDialog.vue'

const props = defineProps<{ modelValue: ShippingTemplateRuleVO[] }>()
const emit = defineEmits<{ 'update:modelValue': [value: ShippingTemplateRuleVO[]] }>()
const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const importOpen = ref(false)

function addRule() {
  emit('update:modelValue', [
    ...props.modelValue,
    { feeCode: 'MANUAL', feeName: t('productCenter.shippingTemplate.manual'), minAreaSqft: '0', feeAmount: '0', sortOrder: props.modelValue.length }
  ])
}

function removeRule(index: number) {
  emit('update:modelValue', props.modelValue.filter((_, rowIndex) => rowIndex !== index))
}

function syncFeeName(row: ShippingTemplateRuleVO) {
  row.feeName = row.feeCode === 'MOTORIZED'
    ? t('productCenter.shippingTemplate.motorized')
    : t('productCenter.shippingTemplate.manual')
}

function applyImportedRules(rows: ShippingTemplateRuleVO[]) {
  emit('update:modelValue', rows.map((row, index) => ({ ...row, sortOrder: index })))
  ElMessage.success(t('productCenter.shippingTemplate.importSuccess'))
}
</script>

<style scoped>
.shipping-template-rules {
  padding: 12px;
  background: #fff;
  border: 1px solid #e9edf5;
  border-radius: 8px;
}

.shipping-template-rules__header,
.shipping-template-rules__actions,
.shipping-template-rules__range {
  display: flex;
  align-items: center;
}

.shipping-template-rules__header {
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.shipping-template-rules__actions,
.shipping-template-rules__range {
  gap: 8px;
}

.shipping-template-rules h3,
.shipping-template-rules p {
  margin: 0;
}

.shipping-template-rules p {
  margin-top: 3px;
  color: #667085;
  font-size: 12px;
}

.shipping-template-rules__range :deep(.el-input-number) {
  width: 108px;
}

.shipping-template-rules :deep(.el-input-number) {
  width: 100%;
}
</style>
