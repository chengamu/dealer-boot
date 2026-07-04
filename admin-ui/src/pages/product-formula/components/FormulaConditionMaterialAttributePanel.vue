<template>
  <el-alert
    v-if="emptyMessage"
    :closable="false"
    show-icon
    type="info"
    :title="emptyMessage"
  />
  <div v-else class="condition-grid condition-grid--material-attribute">
    <div v-for="(row, index) in sources" :key="index" class="condition-grid__row">
      <span class="condition-grid__joiner-text">{{ index === 0 ? '取' : '+' }}</span>
      <el-select v-model="row.optionCode" filterable :placeholder="t('productCenter.formulaSetup.attributeSourceOption')" @change="row.attributeCode = ''">
        <el-option v-for="option in optionChoices" :key="option.value" :label="option.label" :value="option.value" />
      </el-select>
      <el-select v-model="row.attributeCode" filterable :placeholder="t('productCenter.formulaSetup.materialAttribute')">
        <el-option v-for="attribute in attributesForOption(row.optionCode)" :key="attribute.value" :label="attribute.label" :value="attribute.value" />
      </el-select>
      <el-button v-if="sources.length > 1" text type="danger" @click="removeSource(index)">{{ t('common.delete') }}</el-button>
      <p v-if="row.optionCode && attributesForOption(row.optionCode).length === 0" class="condition-grid__hint">
        {{ t('productCenter.formulaSetup.conditionNoAttributesForOption') }}
      </p>
    </div>
    <div class="condition-grid__row condition-grid__row--compare">
      <el-button plain @click="addSource">{{ t('productCenter.formulaSetup.addAttributeSource') }}</el-button>
      <span class="condition-grid__joiner-text">{{ t('productCenter.formulaSetup.totalCompare') }}</span>
      <el-select v-model="operator" class="condition-grid__operator">
        <el-option v-for="item in compareOperators" :key="item" :label="item" :value="item" />
      </el-select>
      <el-input v-model="value" :placeholder="t('productCenter.formulaSetup.conditionValue')" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import {
  allMaterialAttributes,
  buildMaterialAttributeCondition,
  compareOperators,
  materialAttributeOptions,
  optionSelectOptions,
  type ConditionBuildResult,
  type MaterialAttributeSourceRow
} from './formulaConditionEditor'
import type {
  ProductFormulaMaterialVO,
  ProductFormulaOptionMaterialVO,
  ProductFormulaOptionVO
} from '@/api/product-capability/types'

const props = defineProps<{
  t: (key: string) => string
  options: ProductFormulaOptionVO[]
  optionMaterials: ProductFormulaOptionMaterialVO[]
  materials: ProductFormulaMaterialVO[]
}>()

const emit = defineEmits<{
  change: [value: ConditionBuildResult]
}>()

const sources = reactive<MaterialAttributeSourceRow[]>([emptySource()])
const operator = ref('>')
const value = ref('')
const optionChoices = computed(() => optionSelectOptions(props.options))
const allAttributes = computed(() => allMaterialAttributes(props.optionMaterials, props.materials))
const emptyMessage = computed(() => {
  if (optionChoices.value.length === 0) return props.t('productCenter.formulaSetup.conditionNoOptions')
  if (props.optionMaterials.length === 0) return props.t('productCenter.formulaSetup.conditionNoLinkedMaterials')
  if (allAttributes.value.length === 0) return props.t('productCenter.formulaSetup.conditionNoMaterialAttributes')
  return ''
})

watch([sources, operator, value, allAttributes], () => {
  emit('change', buildMaterialAttributeCondition(sources, operator.value, value.value, props.options, allAttributes.value.map(toAttribute)))
}, { deep: true, immediate: true })

function attributesForOption(optionCode: string) {
  return materialAttributeOptions(optionCode, props.optionMaterials, props.materials)
}

function addSource() {
  sources.push(emptySource())
}

function removeSource(index: number) {
  sources.splice(index, 1)
}

function emptySource(): MaterialAttributeSourceRow {
  return { optionCode: '', attributeCode: '' }
}

function toAttribute(option: { value: string; label: string }) {
  return { attributeCode: option.value, attributeNameCn: option.label }
}
</script>
