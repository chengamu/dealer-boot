<template>
  <div class="app-container engineering-run-page">
    <section class="engineering-run-page__header">
      <div>
        <p>{{ t('productCenter.engineering.eyebrow') }}</p>
        <h2>{{ t('productCenter.engineering.previewTitle') }}</h2>
        <span>{{ t('productCenter.engineering.previewDescription') }}</span>
      </div>
      <el-button type="primary" :icon="VideoPlay" @click="runPreview">{{ t('productCenter.engineering.preview') }}</el-button>
    </section>

    <section class="engineering-run-page__panel">
      <el-form :model="form" label-width="132px" class="engineering-run-page__form">
        <el-form-item :label="t('productCenter.engineering.planTitle')">
          <el-select v-model="selectedPlanId" filterable :placeholder="t('productCenter.engineering.selectPlan')" @change="loadVersions">
            <el-option v-for="plan in plans" :key="plan.planId" :label="`${plan.planCode} ${plan.planNameCn}`" :value="plan.planId" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('productCenter.engineering.versionTitle')">
          <el-select v-model="form.versionId" filterable :placeholder="t('productCenter.engineering.selectVersion')">
            <el-option v-for="version in versions" :key="version.versionId" :label="`${version.versionNo} ${version.versionName || ''}`" :value="version.versionId" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('productCenter.engineering.width')">
          <el-input-number v-model="form.width" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item :label="t('productCenter.engineering.height')">
          <el-input-number v-model="form.height" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item :label="t('productCenter.engineering.combinedThickness')">
          <el-input-number v-model="form.combinedThickness" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item :label="t('productCenter.engineering.selectedSystem')">
          <el-input v-model="form.selectedItems.SYSTEM" />
        </el-form-item>
      </el-form>
    </section>

    <section class="engineering-run-page__result">
      <article v-for="card in summaryCards" :key="card.key">
        <span>{{ t(card.labelKey) }}</span>
        <strong>{{ card.value }}</strong>
      </article>
    </section>

    <section class="engineering-run-page__tables">
      <result-table :title="t('productCenter.engineering.disabledOptions')" :rows="rowsOf('disabledOptions')" />
      <result-table :title="t('productCenter.engineering.outputComponents')" :rows="rowsOf('outputComponents')" />
      <result-table :title="t('productCenter.engineering.outputMaterials')" :rows="rowsOf('outputMaterials')" />
      <result-table :title="t('productCenter.engineering.outputMediaAssets')" :rows="rowsOf('outputMediaAssets')" />
      <result-table :title="t('productCenter.engineering.blockers')" :rows="rowsOf('blockers')" />
      <result-table :title="t('productCenter.engineering.warnings')" :rows="rowsOf('warnings')" />
    </section>
  </div>
</template>

<script setup lang="ts" name="EngineeringPreviewPage">
import { computed, defineComponent, h, onMounted, reactive, ref } from 'vue'
import { ElTable, ElTableColumn } from 'element-plus'
import { VideoPlay } from '@element-plus/icons-vue'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { engineeringApi, engineeringPlanApi } from '@/api/product-capability/engineering'
import type { EngineeringPlanVO, EngineeringPlanVersionVO } from '@/api/product-capability/types'

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const plans = ref<EngineeringPlanVO[]>([])
const versions = ref<EngineeringPlanVersionVO[]>([])
const selectedPlanId = ref<number>()
const result = ref<Record<string, any>>({})
const form = reactive({
  versionId: undefined as number | undefined,
  width: 180,
  height: 160,
  combinedThickness: 0.6,
  selectedItems: { SYSTEM: 'CHAIN_ROLLER_WHITE' } as Record<string, string>
})

const summaryCards = computed(() => [
  { key: 'resultStatus', labelKey: 'productCenter.engineering.resultStatus', value: result.value.resultStatus || '-' },
  { key: 'disabledOptions', labelKey: 'productCenter.engineering.disabledOptions', value: rowsOf('disabledOptions').length },
  { key: 'outputComponents', labelKey: 'productCenter.engineering.outputComponents', value: rowsOf('outputComponents').length },
  { key: 'outputMaterials', labelKey: 'productCenter.engineering.outputMaterials', value: rowsOf('outputMaterials').length },
  { key: 'outputMediaAssets', labelKey: 'productCenter.engineering.outputMediaAssets', value: rowsOf('outputMediaAssets').length },
  { key: 'blockers', labelKey: 'productCenter.engineering.blockers', value: rowsOf('blockers').length }
])

const ResultTable = defineComponent({
  props: { title: { type: String, default: '' }, rows: { type: Array, default: () => [] } },
  setup(props) {
    return () => h('article', { class: 'engineering-run-page__table' }, [
      h('h3', props.title),
      h(ElTable, { data: props.rows, border: true, maxHeight: 260 }, () => [
        h(ElTableColumn, { prop: 'code', label: t('productCenter.engineering.outputCode'), minWidth: 120, showOverflowTooltip: true }),
        h(ElTableColumn, { prop: 'outputCode', label: t('productCenter.engineering.outputCode'), minWidth: 150, showOverflowTooltip: true }),
        h(ElTableColumn, { prop: 'textCn', label: t('productCenter.engineering.messageCn'), minWidth: 180, showOverflowTooltip: true }),
        h(ElTableColumn, { prop: 'reasonCn', label: t('productCenter.engineering.reasonCn'), minWidth: 180, showOverflowTooltip: true }),
        h(ElTableColumn, { prop: 'scopeCode', label: t('productCenter.engineering.scopeCode'), minWidth: 150, showOverflowTooltip: true })
      ])
    ])
  }
})

onMounted(async () => {
  const response = await engineeringPlanApi.options({ status: 'ENABLED' })
  plans.value = Array.isArray(response) ? response : response?.data || []
  selectedPlanId.value = plans.value[0]?.planId
  await loadVersions()
})

async function loadVersions() {
  const response = await engineeringApi.versions.list({ pageNum: 1, pageSize: 50, planId: selectedPlanId.value })
  versions.value = response.rows || []
  form.versionId = versions.value[0]?.versionId
  await runPreview()
}

async function runPreview() {
  if (!form.versionId) return
  const response = await engineeringApi.preview(form)
  result.value = response?.data || response || {}
}

function rowsOf(key: string) {
  const rows = result.value[key]
  return Array.isArray(rows) ? rows : []
}
</script>

<style scoped lang="scss">
.engineering-run-page {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.engineering-run-page__header,
.engineering-run-page__panel,
.engineering-run-page__result article,
.engineering-run-page__table {
  border: 1px solid #e5edf7;
  border-radius: 6px;
  background: #fff;
}

.engineering-run-page__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 18px;

  p {
    margin: 0 0 4px;
    color: #1f78ff;
    font-weight: 700;
  }

  h2 {
    margin: 0 0 4px;
  }

  span {
    color: #607086;
  }
}

.engineering-run-page__panel {
  padding: 14px;
}

.engineering-run-page__form {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 0 12px;
}

.engineering-run-page__result {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 10px;

  article {
    padding: 12px;
  }

  span {
    display: block;
    color: #607086;
  }

  strong {
    display: block;
    margin-top: 6px;
    font-size: 20px;
  }
}

.engineering-run-page__tables {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.engineering-run-page__table {
  min-width: 0;
  padding: 12px;

  h3 {
    margin: 0 0 10px;
  }
}
</style>
