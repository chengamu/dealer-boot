<template>
  <section class="setup-section restriction-section">
    <div class="setup-section__toolbar">
      <div>
        <h3>{{ t('productCenter.formulaSetup.exceptionRestriction') }}</h3>
        <p>{{ t('productCenter.formulaSetup.exceptionRestrictionHint') }}</p>
      </div>
      <el-button type="primary" plain :icon="Plus" @click="$emit('addRestriction')">{{ t('productCenter.formulaSetup.addRestriction') }}</el-button>
    </div>

    <el-table :data="restrictions" border class="setup-table">
      <el-table-column :label="t('productCenter.formulaSetup.restrictionSentence')" min-width="920">
        <template #default="{ row }">
          <div class="restriction-sentence">
            <span>{{ t('productCenter.formulaSetup.when') }}</span>
            <el-select v-model="row.conditionType" class="restriction-sentence__condition" @change="handleRestrictionTypeChange(row)">
              <el-option value="WIDTH" :label="t('productCenter.formulaSetup.conditionWidth')" />
              <el-option value="HEIGHT" :label="t('productCenter.formulaSetup.conditionHeight')" />
              <el-option value="WEIGHT" :label="t('productCenter.formulaSetup.conditionWeight')" />
              <el-option value="OPTION_VALUE" :label="t('productCenter.formulaSetup.conditionOptionValue')" />
            </el-select>
            <template v-if="row.conditionType === 'OPTION_VALUE'">
              <el-select v-model="row.conditionOptionCode" class="restriction-sentence__target" filterable clearable @change="handleRestrictionConditionOptionChange(row)">
                <el-option v-for="option in options" :key="String(option.optionCode)" :label="option.optionNameCn || option.optionCode" :value="option.optionCode" />
              </el-select>
              <el-select v-model="row.conditionOperator" class="restriction-sentence__operator">
                <el-option value="EQ" label="=" />
                <el-option value="NE" label="!=" />
              </el-select>
              <el-select v-model="row.conditionValueCode" class="restriction-sentence__target" filterable clearable>
                <el-option
                  v-for="value in valuesForOption(row.conditionOptionCode)"
                  :key="String(value.valueCode)"
                  :label="value.valueNameCn || value.valueCode"
                  :value="value.valueCode"
                />
              </el-select>
            </template>
            <template v-else>
              <el-select v-model="row.conditionOperator" class="restriction-sentence__operator">
                <el-option value="GT" label=">" />
                <el-option value="GTE" label=">=" />
                <el-option value="EQ" label="=" />
                <el-option value="NE" label="!=" />
                <el-option value="LTE" label="<=" />
                <el-option value="LT" label="<" />
              </el-select>
              <el-input v-model="row.conditionValueCode" class="restriction-sentence__value" :placeholder="t('productCenter.formulaSetup.conditionValue')" />
            </template>
            <span>{{ t('productCenter.formulaSetup.then') }}</span>
            <el-select v-model="row.actionType" class="restriction-sentence__action">
              <el-option value="DISABLE" :label="t('productCenter.formulaSetup.disable')" />
              <el-option value="WARN" :label="t('productCenter.formulaSetup.warn')" />
            </el-select>
            <el-select v-model="row.targetOptionCode" class="restriction-sentence__target" filterable clearable @change="row.targetValueCode = ''">
              <el-option v-for="option in options" :key="String(option.optionCode)" :label="option.optionNameCn || option.optionCode" :value="option.optionCode" />
            </el-select>
            <el-select v-model="row.targetValueCode" class="restriction-sentence__target" filterable clearable>
              <el-option :label="t('productCenter.formulaSetup.allOptionValues')" value="" />
              <el-option v-for="value in valuesForOption(row.targetOptionCode)" :key="String(value.valueCode)" :label="value.valueNameCn || value.valueCode" :value="value.valueCode" />
            </el-select>
          </div>
        </template>
      </el-table-column>
      <el-table-column :label="t('productCenter.formulaSetup.messageText')" min-width="240">
        <template #default="{ row }"><el-input v-model="row.messageText" clearable /></template>
      </el-table-column>
      <el-table-column :label="t('common.operate')" width="110" align="center">
        <template #default="{ $index }">
          <AdminTableActions :actions="[
            { label: t('common.delete'), icon: 'Delete', type: 'danger', onClick: () => $emit('removeRestriction', $index) }
          ]" />
        </template>
      </el-table-column>
    </el-table>
  </section>
</template>

<script setup lang="ts">
import { Plus } from '@element-plus/icons-vue'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import type {
  ProductFormulaOptionVO,
  ProductFormulaOptionValueVO,
  ProductFormulaRestrictionVO
} from '@/api/product-capability/types'

const props = defineProps<{
  restrictions: ProductFormulaRestrictionVO[]
  options: ProductFormulaOptionVO[]
  allOptionValues: ProductFormulaOptionValueVO[]
}>()

defineEmits<{
  addRestriction: []
  removeRestriction: [index: number]
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)

function valuesForOption(optionCode?: string) {
  return props.allOptionValues
    .filter((row) => row.optionCode === optionCode)
    .sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
}

function handleRestrictionTypeChange(row: ProductFormulaRestrictionVO) {
  row.conditionOptionCode = ''
  row.conditionValueCode = ''
  if (row.conditionType === 'OPTION_VALUE') {
    row.conditionOperator = 'EQ'
  }
}

function handleRestrictionConditionOptionChange(row: ProductFormulaRestrictionVO) {
  row.conditionValueCode = ''
}
</script>

<style scoped>
.setup-section {
  padding: 16px;
  background: #fff;
  border: 1px solid #e6ebf2;
  border-radius: 8px;
}

.setup-section__toolbar {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 12px;
}

.setup-section__toolbar h3 {
  margin: 0 0 4px;
  color: #111827;
  font-size: 16px;
}

.setup-section__toolbar p {
  margin: 0;
  color: #6b7280;
  font-size: 13px;
}

.restriction-sentence {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.restriction-sentence__condition,
.restriction-sentence__action {
  width: 140px;
}

.restriction-sentence__operator {
  width: 96px;
}

.restriction-sentence__value,
.restriction-sentence__target {
  width: 180px;
}

.setup-table {
  width: 100%;
}
</style>
