<template>
  <div class="app-container formula-simulation-page">
    <div class="simulation-header">
      <div>
        <div class="simulation-header__breadcrumb">{{ t('productCenter.formula.title') }} / {{ t('productCenter.formula.actions.simulation') }}</div>
        <h2>{{ formula.formulaName || '-' }}</h2>
        <div class="simulation-header__meta">
          <span>{{ t('productCenter.formula.code') }}：{{ formula.formulaCode || '-' }}</span>
          <span>{{ t('productCenter.formula.category') }}：{{ formula.categoryNameCn || '-' }}</span>
          <span>{{ t('productCenter.formula.productType') }}：{{ formula.productTypeNameCn || '-' }}</span>
          <span>{{ t('productCenter.formulaSimulation.status') }}：{{ validationText(formula.simulationValidationStatus) }}</span>
        </div>
      </div>
      <div class="simulation-header__actions">
        <el-button icon="Back" @click="router.back()">{{ t('common.back') }}</el-button>
        <el-button icon="DataAnalysis" :loading="running" @click="run">{{ t('productCenter.formulaSimulation.run') }}</el-button>
        <el-button type="primary" icon="CircleCheck" :loading="validating" @click="validate">{{ t('productCenter.formulaSimulation.validate') }}</el-button>
      </div>
    </div>

    <section class="simulation-panel">
      <div class="simulation-form">
        <el-form :model="form" label-width="110px">
          <el-form-item :label="t('productCenter.formulaSimulation.orderWidth')">
            <el-input-number v-model="form.orderWidth" :precision="2" :step="1" />
          </el-form-item>
          <el-form-item :label="t('productCenter.formulaSimulation.orderHeight')">
            <el-input-number v-model="form.orderHeight" :precision="2" :step="1" />
          </el-form-item>
        </el-form>
      </div>
      <div class="simulation-result">
        <el-tag :type="formulaValidationTagType(result.status)">{{ validationText(result.status) }}</el-tag>
        <span>{{ messageText(result.message) }}</span>
        <strong>{{ t('productCenter.formulaSimulation.totalAmount') }}：{{ money(result.totalAmount) }}</strong>
      </div>
    </section>

    <section class="simulation-panel">
      <div class="section-title">{{ t('productCenter.formulaSimulation.bomItems') }}</div>
      <el-table v-loading="loading" :data="result.items || []" border>
        <el-table-column type="index" :label="t('common.index')" width="64" align="center" />
        <el-table-column prop="attributeGroupNameCn" :label="t('productCenter.formulaSetup.attributeGroup')" width="110" show-overflow-tooltip />
        <el-table-column prop="materialTypeNameCn" :label="t('productCenter.formulaSetup.materialType')" width="130" show-overflow-tooltip />
        <el-table-column prop="materialCode" :label="t('productCenter.formulaSetup.materialCode')" width="130" show-overflow-tooltip />
        <el-table-column prop="materialNameCn" :label="t('productCenter.formulaSetup.materialName')" min-width="220" show-overflow-tooltip />
        <el-table-column prop="specModelText" :label="t('productCenter.formulaSetup.specModel')" min-width="180" show-overflow-tooltip />
        <el-table-column prop="unitCode" :label="t('productCenter.formulaSetup.unit')" width="90" />
        <el-table-column :label="t('productCenter.formulaSimulation.usageQty')" width="110" align="right">
          <template #default="{ row }">{{ quantity(row.usageQty) }}</template>
        </el-table-column>
        <el-table-column :label="t('productCenter.formulaSimulation.unitPrice')" width="110" align="right">
          <template #default="{ row }">{{ money(row.unitPrice) }}</template>
        </el-table-column>
        <el-table-column :label="t('productCenter.formulaSimulation.salesPrice')" width="110" align="right">
          <template #default="{ row }">{{ money(row.salesPrice) }}</template>
        </el-table-column>
        <el-table-column :label="t('productCenter.formulaSimulation.amount')" width="110" align="right">
          <template #default="{ row }">{{ money(row.amount) }}</template>
        </el-table-column>
        <el-table-column prop="productionRemark" :label="t('productCenter.formulaSetup.productionRemark')" min-width="160" show-overflow-tooltip />
      </el-table>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { productFormulaApi } from '@/api/product-formula/formula'
import type { ProductFormulaSimulationBO, ProductFormulaSimulationVO, ProductFormulaVO } from '@/api/product-capability/types'
import { formulaValidationStatusText, formulaValidationTagType } from '@/constants/productStatus'

const route = useRoute()
const router = useRouter()
const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const formulaId = String(route.params.id || '')
const loading = ref(false)
const running = ref(false)
const validating = ref(false)
const formula = ref<ProductFormulaVO>({})
const result = ref<ProductFormulaSimulationVO>({})
const form = reactive<ProductFormulaSimulationBO>({
  orderWidth: undefined,
  orderHeight: undefined
})

function validationText(status?: string) {
  return formulaValidationStatusText(status, t)
}

function messageText(message?: string) {
  return message ? t(message) : '-'
}

function quantity(value?: number) {
  return value == null ? '-' : Number(value).toFixed(2)
}

function money(value?: number) {
  return value == null ? '-' : Number(value).toFixed(2)
}

async function load() {
  if (!formulaId) return
  loading.value = true
  try {
    const [formulaResponse, simulationResponse] = await Promise.all([
      productFormulaApi.get(formulaId),
      productFormulaApi.simulation(formulaId)
    ])
    formula.value = formulaResponse.data || {}
    result.value = simulationResponse.data || {}
  } finally {
    loading.value = false
  }
}

async function run() {
  running.value = true
  try {
    const response = await productFormulaApi.runSimulation(formulaId, form)
    result.value = response.data || {}
  } finally {
    running.value = false
  }
}

async function validate() {
  validating.value = true
  try {
    await productFormulaApi.validateSimulation(formulaId, form)
    ElMessage.success(t('productCenter.formula.validation.pass'))
    await load()
  } finally {
    validating.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.formula-simulation-page {
  display: grid;
  gap: 12px;
}

.simulation-header,
.simulation-panel {
  padding: 16px;
  background: #fff;
  border: 1px solid #e6ebf2;
  border-radius: 8px;
}

.simulation-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
}

.simulation-header h2 {
  margin: 8px 0;
  color: #1f2937;
}

.simulation-header__breadcrumb,
.simulation-header__meta {
  color: #6b7280;
  font-size: 13px;
}

.simulation-header__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 20px;
}

.simulation-header__actions,
.simulation-result {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.simulation-form :deep(.el-input-number) {
  width: 220px;
}

.simulation-result {
  justify-content: flex-start;
  margin-top: 8px;
}

.section-title {
  margin-bottom: 12px;
  color: #111827;
  font-size: 16px;
  font-weight: 700;
}
</style>
