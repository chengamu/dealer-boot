<template>
  <AdminDrawer
    :model-value="modelValue"
    :title="title"
    size="840px"
    append-to-body
    destroy-on-close
    :close-on-click-modal="false"
    :before-close="beforeClose"
    @update:model-value="emit('update:modelValue', $event)"
    @closed="emit('closed')"
  >
    <div v-if="identityLocked" class="sales-store-form__alert">
      <el-alert :title="t('salesStore.identityLockedHint')" type="info" show-icon :closable="false" />
    </div>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
      <el-row :gutter="16">
        <el-col :xs="24" :sm="12">
          <el-form-item :label="t('salesStore.code')" prop="storeCode">
            <el-input v-model="form.storeCode" maxlength="64" :disabled="identityLocked" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12">
          <el-form-item :label="t('salesStore.name')" prop="storeName">
            <el-input v-model="form.storeName" maxlength="120" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12">
          <el-form-item :label="t('salesStore.dept')" prop="deptId">
            <el-select v-model="form.deptId" filterable :disabled="identityLocked">
              <el-option
                v-for="option in deptOptions"
                :key="option.deptId"
                :label="deptOptionLabel(option)"
                :value="option.deptId"
                :disabled="isDeptDisabled(option)"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12">
          <el-form-item :label="t('salesStore.currency')">
            <el-input :model-value="form.currencyCode || 'USD'" disabled />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12">
          <el-form-item :label="t('salesStore.contactName')">
            <el-input v-model="form.contactName" maxlength="100" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12">
          <el-form-item :label="t('salesStore.contactPhone')">
            <el-input v-model="form.contactPhone" maxlength="64" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12">
          <el-form-item :label="t('salesStore.creditLimit')">
            <BusinessNumberInput v-model="form.creditLimit" mode="MONEY" :currency-digits="2" :min="0" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12">
          <el-form-item :label="t('salesStore.paymentTermDays')">
            <el-input-number v-model="form.paymentTermDays" :min="0" :step="1" controls-position="right" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="6">
          <el-form-item :label="t('salesStore.country')">
            <el-input v-model="form.country" maxlength="64" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="6">
          <el-form-item :label="t('salesStore.state')">
            <el-input v-model="form.state" maxlength="64" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="6">
          <el-form-item :label="t('salesStore.city')">
            <el-input v-model="form.city" maxlength="64" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="6">
          <el-form-item :label="t('salesStore.postalCode')">
            <el-input v-model="form.postalCode" maxlength="32" />
          </el-form-item>
        </el-col>
        <el-col :xs="24">
          <el-form-item :label="t('salesStore.addressLine1')">
            <el-input v-model="form.addressLine1" maxlength="255" />
          </el-form-item>
        </el-col>
        <el-col :xs="24">
          <el-form-item :label="t('salesStore.addressLine2')">
            <el-input v-model="form.addressLine2" maxlength="255" />
          </el-form-item>
        </el-col>
        <el-col :xs="24">
          <el-form-item :label="t('common.remark')">
            <el-input v-model="form.remark" type="textarea" :rows="3" maxlength="500" show-word-limit />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <template #footer>
      <div class="sales-store-form__footer">
        <el-button @click="emit('cancel')">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="emit('submit')">{{ t('common.confirm') }}</el-button>
      </div>
    </template>
  </AdminDrawer>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { type FormInstance, type FormRules } from 'element-plus'
import { useI18n } from 'vue-i18n'
import type { SalesStore, SalesStoreDeptOption } from '@/api/system/sales-store'

const props = defineProps<{
  modelValue: boolean
  title: string
  mode: 'add' | 'edit'
  form: SalesStore
  deptOptions: SalesStoreDeptOption[]
  beforeClose: (done: () => void) => void
  isDeptDisabled: (option: SalesStoreDeptOption) => boolean
}>()

const emit = defineEmits<{
  (event: 'update:modelValue', value: boolean): void
  (event: 'cancel'): void
  (event: 'submit'): void
  (event: 'closed'): void
}>()

const { t } = useI18n()
const formRef = ref<FormInstance>()
const identityLocked = computed(() => props.mode === 'edit' && String(props.form.status) === '1')
const rules = computed<FormRules>(() => ({
  storeCode: [{ required: true, message: t('sales.store.code.required'), trigger: 'blur' }],
  storeName: [{ required: true, message: t('sales.store.name.required'), trigger: 'blur' }],
  deptId: [{ required: true, message: t('sales.store.dept.required'), trigger: 'change' }]
}))

function deptOptionLabel(option: SalesStoreDeptOption) {
  return option.linked ? `${option.deptName} · ${t('salesStore.linkedDept')}` : option.deptName
}

async function validate() {
  return Boolean(await formRef.value?.validate().catch(() => false))
}

defineExpose({ validate })
</script>

<style scoped>
.sales-store-form__alert {
  margin-bottom: 12px;
}

.sales-store-form__footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
