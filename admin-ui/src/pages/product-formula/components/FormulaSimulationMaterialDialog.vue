<template>
  <el-dialog
    :model-value="open"
    :title="title"
    width="860px"
    class="simulation-material-dialog"
    append-to-body
    @update:model-value="open = $event"
  >
    <div class="simulation-material-dialog__tools">
      <el-input v-model="keyword" clearable :prefix-icon="Search" :placeholder="t('productCenter.formulaSimulation.searchOption')" />
      <span>{{ t('productCenter.formulaSimulation.availableValues', { count: filteredValues.length }) }}</span>
    </div>
    <div v-if="filteredValues.length === 0" class="simulation-material-dialog__empty">
      <el-empty :description="t('productCenter.formulaSimulation.noValues')" />
    </div>
    <div v-else class="simulation-material-grid">
      <button
        v-for="value in filteredValues"
        :key="value.valueCode"
        type="button"
        :class="{ 'is-active': modelValue === value.valueCode }"
        @click="selectValue(value.valueCode)"
      >
        <span class="simulation-material-grid__thumb">{{ swatchText(value) }}</span>
        <strong>{{ value.valueNameCn || value.valueNameEn || value.valueCode }}</strong>
        <small>{{ value.valueCode }}</small>
        <small>{{ linkedMaterialSummary(value.valueCode) }}</small>
      </button>
    </div>
    <template #footer>
      <el-button @click="open = false">{{ t('common.close') }}</el-button>
      <el-button v-if="modelValue" @click="selectValue('')">{{ t('productCenter.formulaSimulation.clearSelection') }}</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import type { ProductFormulaOptionMaterialVO, ProductFormulaOptionValueVO } from '@/api/product-capability/types'

const props = defineProps<{
  modelValue?: string
  title: string
  values: ProductFormulaOptionValueVO[]
  materials: ProductFormulaOptionMaterialVO[]
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const open = defineModel<boolean>('open', { default: false })
const localeStore = useLocaleStore()
const keyword = ref('')

const t = (key: string, named?: Record<string, string | number>) => {
  let message = getMessage(key, localeStore.language)
  Object.entries(named || {}).forEach(([name, value]) => {
    message = message.replace(`{${name}}`, String(value))
  })
  return message
}

const filteredValues = computed(() => {
  const text = keyword.value.trim().toLowerCase()
  if (!text) return props.values
  return props.values.filter((value) => `${value.valueCode || ''} ${value.valueNameCn || ''} ${value.valueNameEn || ''}`.toLowerCase().includes(text))
})

function linkedMaterialSummary(valueCode?: string) {
  const rows = props.materials.filter((row) => row.valueCode === valueCode)
  if (rows.length === 0) return t('productCenter.formulaSetup.noLinkedMaterial')
  return rows.map((row) => row.materialNameCn || row.materialCode).filter(Boolean).slice(0, 2).join('、')
    + (rows.length > 2 ? ` +${rows.length - 2}` : '')
}

function selectValue(valueCode?: string) {
  emit('update:modelValue', valueCode || '')
  open.value = false
}

function swatchText(value: ProductFormulaOptionValueVO) {
  return String(value.valueNameCn || value.valueNameEn || value.valueCode || '-').slice(0, 1)
}
</script>

<style scoped>
.simulation-material-dialog__tools {
  display: grid;
  grid-template-columns: minmax(260px, 1fr) auto;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  color: #667085;
  font-size: 13px;
}

.simulation-material-dialog__empty {
  min-height: 220px;
}

.simulation-material-grid {
  display: grid;
  max-height: 520px;
  overflow: auto;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 10px;
  padding-right: 4px;
}

.simulation-material-grid button {
  display: grid;
  grid-template-columns: 42px 1fr;
  gap: 2px 10px;
  min-height: 72px;
  padding: 10px;
  border: 1px solid #e9edf5;
  border-radius: 8px;
  background: #fff;
  color: #1f2937;
  text-align: left;
  cursor: pointer;
}

.simulation-material-grid button:hover,
.simulation-material-grid button.is-active {
  border-color: #1677ff;
  background: #f5f8ff;
}

.simulation-material-grid__thumb {
  grid-row: span 3;
  display: inline-flex;
  width: 42px;
  height: 42px;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  background: #eef5ff;
  color: #1677ff;
  font-weight: 700;
}

.simulation-material-grid strong,
.simulation-material-grid small {
  overflow: hidden;
  min-width: 0;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.simulation-material-grid small {
  color: #667085;
}
</style>
