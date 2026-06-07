<template>
  <div class="config-side-panel">
    <div class="config-side-panel__head">
      <h2>{{ t('productCenter.template.liveChecks') }}</h2>
      <el-tag :type="resultTag">{{ result.resultStatus || 'ALLOW' }}</el-tag>
    </div>
    <div class="live-check-panel__stats">
      <div class="is-danger">
        <strong>{{ blockers.length }}</strong>
        <span>{{ t('productCenter.template.blockers') }}</span>
      </div>
      <div class="is-warning">
        <strong>{{ warnings.length }}</strong>
        <span>{{ t('productCenter.template.warnings') }}</span>
      </div>
      <div class="is-success">
        <strong>{{ visibleQuestions.length }}</strong>
        <span>{{ t('productCenter.template.visibleQuestions') }}</span>
      </div>
    </div>
    <el-form label-position="top" class="live-check-panel__form">
      <el-form-item :label="t('productCenter.template.templateVersionId')">
        <el-input-number :model-value="form.templateVersionId" :min="0" controls-position="right" @update:model-value="updateForm('templateVersionId', $event)" />
      </el-form-item>
      <div class="live-check-panel__grid">
        <el-form-item :label="t('productCenter.template.widthCm')">
          <el-input-number :model-value="form.widthCm" :min="0" controls-position="right" @update:model-value="updateForm('widthCm', $event)" />
        </el-form-item>
        <el-form-item :label="t('productCenter.template.heightCm')">
          <el-input-number :model-value="form.heightCm" :min="0" controls-position="right" @update:model-value="updateForm('heightCm', $event)" />
        </el-form-item>
      </div>
      <el-form-item :label="t('productCenter.template.selectedOptionCodes')">
        <el-select
          :model-value="selectedOptionCodes"
          multiple
          filterable
          allow-create
          default-first-option
          collapse-tags
          collapse-tags-tooltip
          :placeholder="t('productCenter.template.optionCodePlaceholder')"
          @update:model-value="$emit('update:selectedOptionCodes', $event)"
        >
          <el-option v-for="option in optionOptions" :key="option" :label="option" :value="option" />
        </el-select>
      </el-form-item>
    </el-form>
    <el-button class="live-check-panel__button" type="primary" icon="VideoPlay" @click="$emit('evaluate')" v-hasPermi="['product:template:test']">
      {{ t('productCenter.template.evaluate') }}
    </el-button>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import type { ProductRecord } from '@/api/product-capability/types'

const props = defineProps<{
  result: ProductRecord
  form: { templateVersionId?: number; widthCm: number; heightCm: number }
  selectedOptionCodes: string[]
  optionOptions: string[]
}>()

const emit = defineEmits<{
  evaluate: []
  'update:form': [form: { templateVersionId?: number; widthCm: number; heightCm: number }]
  'update:selectedOptionCodes': [codes: string[]]
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)

const blockers = computed(() => (props.result.blockers as unknown[] | undefined) || [])
const warnings = computed(() => (props.result.warnings as unknown[] | undefined) || [])
const visibleQuestions = computed(() => (props.result.visibleQuestions as unknown[] | undefined) || [])
const resultTag = computed(() => {
  if (props.result.resultStatus === 'BLOCKER') return 'danger'
  if (props.result.resultStatus === 'WARNING') return 'warning'
  return 'success'
})

function updateForm(key: 'templateVersionId' | 'widthCm' | 'heightCm', value: number | undefined) {
  emit('update:form', { ...props.form, [key]: value ?? 0 })
}
</script>

<style scoped lang="scss">
.config-side-panel {
  padding: 14px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.04);
}

.config-side-panel__head {
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

.live-check-panel__stats,
.live-check-panel__grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
}

.live-check-panel__stats > div {
  padding: 10px;
  border-radius: 8px;
  background: #f8fafc;

  strong,
  span {
    display: block;
  }

  strong {
    font-size: 22px;
  }

  span {
    color: #64748b;
    font-size: 12px;
  }
}

.live-check-panel__stats .is-danger strong { color: #dc2626; }
.live-check-panel__stats .is-warning strong { color: #d97706; }
.live-check-panel__stats .is-success strong { color: #16a34a; }

.live-check-panel__form {
  margin-top: 12px;
}

.live-check-panel__button {
  width: 100%;
}

@media (max-width: 520px) {
  .live-check-panel__stats,
  .live-check-panel__grid {
    grid-template-columns: 1fr;
  }
}
</style>
