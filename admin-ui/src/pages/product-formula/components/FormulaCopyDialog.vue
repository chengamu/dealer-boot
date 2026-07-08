<template>
  <el-dialog
    :model-value="modelValue"
    :title="t('productCenter.formulaWorkbench.copyTitle')"
    width="520px"
    destroy-on-close
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
      <el-form-item :label="t('productCenter.formula.code')" prop="formulaCode">
        <el-input v-model.trim="form.formulaCode" />
      </el-form-item>
      <el-form-item :label="t('productCenter.formula.name')" prop="formulaName">
        <el-input v-model.trim="form.formulaName" />
      </el-form-item>
      <el-form-item :label="t('productCenter.common.remark')">
        <el-input v-model="form.remark" type="textarea" :rows="3" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="$emit('update:modelValue', false)">{{ t('common.cancel') }}</el-button>
      <el-button type="primary" :loading="loading" @click="submit">{{ t('common.confirm') }}</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import type { ProductFormulaVO } from '@/api/product-capability/types'

const props = defineProps<{
  modelValue: boolean
  source?: ProductFormulaVO
  loading?: boolean
  t: (key: string) => string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  submit: [value: Partial<ProductFormulaVO>]
}>()

const formRef = ref<FormInstance>()
const form = reactive<Partial<ProductFormulaVO>>({ formulaCode: '', formulaName: '', remark: '' })
const rules: FormRules = {
  formulaCode: [{ required: true, message: props.t('product.formula.copyCodeRequired'), trigger: 'blur' }],
  formulaName: [{ required: true, message: props.t('product.formula.copyNameRequired'), trigger: 'blur' }]
}

watch(() => props.modelValue, (visible) => {
  if (!visible) return
  form.formulaCode = ''
  form.formulaName = props.source?.formulaName ? `${props.source.formulaName} Copy` : ''
  form.remark = props.source?.remark || ''
})

async function submit() {
  await formRef.value?.validate()
  emit('submit', { formulaCode: form.formulaCode, formulaName: form.formulaName, remark: form.remark })
}
</script>
