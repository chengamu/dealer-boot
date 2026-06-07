<template>
  <div class="app-container quote-preview">
    <section class="quote-preview__header">
      <div>
        <p>{{ t('productCenter.menu.configPricing') }}</p>
        <h1>{{ t('productCenter.quote.title') }}</h1>
        <span>{{ t('productCenter.quote.description') }}</span>
      </div>
      <el-button type="primary" icon="Money" :loading="loading" @click="runQuote" v-hasPermi="['product:price:test']">
        {{ t('productCenter.quote.run') }}
      </el-button>
    </section>

    <el-row :gutter="12">
      <el-col :xs="24" :xl="9">
        <div class="quote-preview__panel">
          <h2>{{ t('productCenter.quote.input') }}</h2>
          <el-form label-position="top">
            <el-form-item :label="t('productCenter.price.planVersionId')">
              <el-select
                v-model="form.pricePlanVersionId"
                filterable
                remote
                clearable
                :remote-method="loadPriceVersions"
                :loading="optionLoading"
                style="width: 100%"
                @change="applySelectedPriceVersion"
              >
                <el-option
                  v-for="option in priceVersionOptions"
                  :key="String(option.pricePlanVersionId)"
                  :label="priceVersionLabel(option)"
                  :value="option.pricePlanVersionId as number"
                />
              </el-select>
            </el-form-item>
            <el-form-item :label="t('productCenter.price.currencyCode')">
              <el-input v-model="form.currencyCode" />
            </el-form-item>
            <el-form-item :label="t('productCenter.price.quantity')">
              <el-input-number v-model="form.quantity" :min="1" controls-position="right" style="width: 100%" />
            </el-form-item>
            <div class="quote-preview__inline">
              <el-form-item :label="t('productCenter.template.widthCm')">
                <el-input-number v-model="form.widthCm" :min="0" controls-position="right" />
              </el-form-item>
              <el-form-item :label="t('productCenter.template.heightCm')">
                <el-input-number v-model="form.heightCm" :min="0" controls-position="right" />
              </el-form-item>
            </div>
            <el-form-item :label="t('productCenter.template.selectedOptions')">
              <el-select
                v-model="selectedOptions"
                multiple
                filterable
                allow-create
                default-first-option
                collapse-tags
                collapse-tags-tooltip
                style="width: 100%"
              >
                <el-option v-for="option in quickOptions" :key="option" :label="option" :value="option" />
              </el-select>
            </el-form-item>
          </el-form>
        </div>
      </el-col>

      <el-col :xs="24" :xl="15">
        <div class="quote-preview__panel quote-preview__result">
          <div class="quote-preview__result-head">
            <div>
              <h2>{{ t('productCenter.quote.result') }}</h2>
              <span>{{ t('productCenter.quote.resultHint') }}</span>
            </div>
            <el-tag :type="resultTag">{{ resultStatusText }}</el-tag>
          </div>
          <div class="quote-preview__amounts">
            <div>
              <span>{{ t('productCenter.price.baseAmount') }}</span>
              <strong>{{ quote.baseAmount ?? 0 }}</strong>
            </div>
            <div>
              <span>{{ t('productCenter.price.optionAmount') }}</span>
              <strong>{{ quote.optionAmount ?? 0 }}</strong>
            </div>
            <div class="is-total">
              <span>{{ t('productCenter.price.totalAmount') }}</span>
              <strong>{{ quote.totalAmount ?? 0 }}</strong>
            </div>
          </div>
          <el-table :data="matchedItems" border>
            <el-table-column :label="t('productCenter.price.itemCode')" prop="itemCode" min-width="140" />
            <el-table-column :label="t('productCenter.price.itemNameCn')" prop="itemNameCn" min-width="160" />
            <el-table-column :label="t('productCenter.price.itemType')" prop="itemType" width="130" />
            <el-table-column :label="t('productCenter.price.baseAmount')" prop="amount" width="120" />
          </el-table>
          <el-empty v-if="!matchedItems.length" :description="t('productCenter.quote.empty')" />
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts" name="QuotePreviewPage">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { calculatePrice, pricePlanVersionApi } from '@/api/product-capability/pricing'
import type { ProductRecord } from '@/api/product-capability/types'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'

const localeStore = useLocaleStore()
const route = useRoute()
const t = (key: string) => getMessage(key, localeStore.language)
const loading = ref(false)
const optionLoading = ref(false)
const quote = ref<ProductRecord>({})
const priceVersionOptions = ref<ProductRecord[]>([])
const selectedOptions = ref<string[]>(['FABRIC_TYPE:LF'])
const quickOptions = ['FABRIC_TYPE:LF', 'FABRIC_TYPE:RD', 'CONTROL:MOTOR', 'CONTROL:CHAIN']
const form = reactive({
  pricePlanVersionId: undefined as number | undefined,
  currencyCode: 'USD',
  quantity: 1,
  widthCm: 120,
  heightCm: 160
})

const matchedItems = computed(() => (quote.value.matchedItems as ProductRecord[] | undefined) || [])
const resultTag = computed(() => {
  if (quote.value.resultStatus === 'BLOCKER') return 'danger'
  if (quote.value.resultStatus === 'WARNING') return 'warning'
  return 'success'
})
const resultStatusText = computed(() => {
  const status = String(quote.value.resultStatus || 'READY').toUpperCase()
  const keyMap: Record<string, string> = {
    READY: 'productCenter.quote.status.ready',
    PRICED: 'productCenter.quote.status.priced',
    BLOCKER: 'productCenter.quote.status.blocker',
    WARNING: 'productCenter.quote.status.warning'
  }
  return keyMap[status] ? t(keyMap[status]) : status
})

function priceVersionLabel(option: ProductRecord) {
  return [option.pricePlanCode, option.versionNo, option.currencyCode].filter(Boolean).join(' / ') || String(option.pricePlanVersionId || '-')
}

function applyRouteQuery() {
  const pricePlanVersionId = Number(route.query.pricePlanVersionId)
  if (Number.isFinite(pricePlanVersionId) && pricePlanVersionId > 0) {
    form.pricePlanVersionId = pricePlanVersionId
  }
  if (typeof route.query.currencyCode === 'string' && route.query.currencyCode) {
    form.currencyCode = route.query.currencyCode
  }
}

function applySelectedPriceVersion(value?: number) {
  const selected = priceVersionOptions.value.find((item) => item.pricePlanVersionId === value)
  if (typeof selected?.currencyCode === 'string') {
    form.currencyCode = selected.currencyCode
  }
}

async function loadPriceVersions(keyword?: string) {
  optionLoading.value = true
  try {
    const response = await pricePlanVersionApi.options({ pricePlanCode: keyword, pageNum: 1, pageSize: 20 })
    priceVersionOptions.value = response.data || []
  } finally {
    optionLoading.value = false
  }
}

async function runQuote() {
  loading.value = true
  try {
    const response = await calculatePrice({
      pricePlanVersionId: form.pricePlanVersionId,
      currencyCode: form.currencyCode,
      quantity: form.quantity,
      selectedOptions: Object.fromEntries(selectedOptions.value.map((item) => item.split(':')).filter((item) => item.length === 2)),
      inputValues: {
        width_cm: form.widthCm,
        height_cm: form.heightCm
      }
    })
    quote.value = response.data || {}
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  applyRouteQuery()
  await loadPriceVersions(typeof route.query.pricePlanCode === 'string' ? route.query.pricePlanCode : undefined)
})
</script>

<style scoped lang="scss">
.quote-preview {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.quote-preview__header,
.quote-preview__panel {
  padding: 18px 20px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
}

.quote-preview__header,
.quote-preview__result-head,
.quote-preview__amounts {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.quote-preview h1,
.quote-preview h2,
.quote-preview p {
  margin: 0;
}

.quote-preview h1 {
  margin: 4px 0;
  font-size: 24px;
}

.quote-preview h2 {
  margin-bottom: 12px;
  font-size: 18px;
}

.quote-preview p,
.quote-preview span {
  color: #64748b;
}

.quote-preview__inline {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.quote-preview__result {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.quote-preview__amounts > div {
  flex: 1;
  padding: 14px;
  border-radius: 8px;
  background: #f8fafc;
}

.quote-preview__amounts strong {
  display: block;
  margin-top: 6px;
  font-size: 24px;
  color: #111827;
}

.quote-preview__amounts .is-total {
  background: #eff6ff;
}
</style>
