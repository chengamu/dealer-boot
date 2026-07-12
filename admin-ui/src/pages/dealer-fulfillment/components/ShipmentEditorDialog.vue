<template>
  <el-dialog v-model="visible" :title="shipment?.shipmentId ? t('dealer.fulfillment.editPackage') : t('dealer.fulfillment.createPackage')" width="820px" :close-on-click-modal="false" @closed="resetForm">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
      <div class="shipment-form__grid">
        <el-form-item :label="t('dealer.fulfillment.packageNo')"><el-input v-model="form.packageNo" maxlength="64" /></el-form-item>
        <el-form-item :label="t('dealer.fulfillment.carrier')" prop="carrierName"><el-input v-model="form.carrierName" maxlength="100" /></el-form-item>
        <el-form-item :label="t('dealer.fulfillment.carrierCode')"><el-input v-model="form.carrierCode" maxlength="64" /></el-form-item>
        <el-form-item :label="t('dealer.fulfillment.trackingNo')" prop="trackingNo"><el-input v-model="form.trackingNo" maxlength="128" /></el-form-item>
        <el-form-item :label="t('dealer.fulfillment.weight')"><el-input-number v-model="form.weight" :min="0" :precision="3" /><el-input v-model="form.weightUnit" class="shipment-form__unit" /></el-form-item>
        <el-form-item :label="t('dealer.fulfillment.dimensions')"><div class="shipment-form__dimensions"><el-input-number v-model="form.length" :min="0" /><span>x</span><el-input-number v-model="form.width" :min="0" /><span>x</span><el-input-number v-model="form.height" :min="0" /><el-input v-model="form.dimensionUnit" /></div></el-form-item>
      </div>
      <el-form-item :label="t('dealer.fulfillment.packageAllocation')" prop="items">
        <el-table :data="allocationRows" border>
          <el-table-column prop="lineNo" :label="t('common.index')" width="58" align="center" />
          <el-table-column :label="t('dealer.fulfillment.product')" min-width="210"><template #default="{ row }">{{ row.saleProductName || row.saleProductCode || '-' }}</template></el-table-column>
          <el-table-column prop="quantity" :label="t('dealer.fulfillment.orderQuantity')" width="100" align="center" />
          <el-table-column :label="t('dealer.fulfillment.allocatedQuantity')" width="100" align="center"><template #default="{ row }">{{ allocatedOutside(row.salesItemId) }}</template></el-table-column>
          <el-table-column :label="t('dealer.fulfillment.remainingQuantity')" width="100" align="center"><template #default="{ row }">{{ remaining(row) }}</template></el-table-column>
          <el-table-column :label="t('dealer.fulfillment.thisPackageQuantity')" width="150"><template #default="{ row }"><el-input-number v-model="quantities[row.salesItemId]" :min="0" :max="remaining(row)" controls-position="right" /></template></el-table-column>
        </el-table>
      </el-form-item>
      <el-form-item :label="t('dealer.fulfillment.remark')"><el-input v-model="form.remark" type="textarea" :rows="2" maxlength="1000" show-word-limit /></el-form-item>
    </el-form>
    <template #footer><el-button @click="visible = false">{{ t('common.cancel') }}</el-button><el-button type="primary" :loading="saving" @click="submit">{{ t('common.save') }}</el-button></template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { useI18n } from 'vue-i18n'
import type { FulfillmentItem, Shipment, ShipmentPayload } from '@/api/dealer-fulfillment'

const props = defineProps<{ modelValue: boolean; items: FulfillmentItem[]; shipments: Shipment[]; shipment?: Shipment }>()
const emit = defineEmits<{ 'update:modelValue': [value: boolean]; save: [payload: ShipmentPayload] }>()
const { t } = useI18n()
const formRef = ref<FormInstance>()
const saving = ref(false)
const quantities = reactive<Record<string, number>>({})
const form = reactive<ShipmentPayload>({ items: [], weightUnit: 'kg', dimensionUnit: 'cm' })
const visible = computed({ get: () => props.modelValue, set: (value) => emit('update:modelValue', value) })
const allocationRows = computed(() => props.items.filter((item) => remaining(item) > 0 || (quantities[item.salesItemId] || 0) > 0))
const rules: FormRules = {
  carrierName: [{ required: true, message: t('dealer.fulfillment.required'), trigger: 'blur' }],
  trackingNo: [{ required: true, message: t('dealer.fulfillment.required'), trigger: 'blur' }],
  items: [{ validator: (_rule, _value, callback) => selectedItems().length ? callback() : callback(new Error(t('dealer.fulfillment.allocationRequired'))), trigger: 'change' }]
}

watch(() => props.modelValue, (open) => { if (open) fillForm() })

function allocatedOutside(salesItemId: string) {
  return props.shipments.filter((item) => item.status !== 'CANCELLED' && item.shipmentId !== props.shipment?.shipmentId)
    .flatMap((item) => item.items || []).filter((item) => item.salesItemId === salesItemId)
    .reduce((sum, item) => sum + (item.quantity || 0), 0)
}
function remaining(item: FulfillmentItem) { return Math.max(0, (item.quantity || 0) - allocatedOutside(item.salesItemId)) }
function selectedItems() { return props.items.filter((item) => (quantities[item.salesItemId] || 0) > 0).map((item) => ({ salesItemId: item.salesItemId, quantity: quantities[item.salesItemId] })) }
function fillForm() {
  resetForm()
  Object.assign(form, props.shipment || { weightUnit: 'kg', dimensionUnit: 'cm' })
  props.shipment?.items?.forEach((item) => { quantities[item.salesItemId] = item.quantity })
}
function resetForm() {
  Object.keys(quantities).forEach((key) => delete quantities[key])
  Object.assign(form, { packageNo: '', carrierCode: '', carrierName: '', trackingNo: '', weight: undefined, weightUnit: 'kg', length: undefined, width: undefined, height: undefined, dimensionUnit: 'cm', remark: '', items: [] })
  formRef.value?.clearValidate()
}
async function submit() {
  form.items = selectedItems()
  if (!await formRef.value?.validate().catch(() => false)) return
  saving.value = true
  try { emit('save', { ...form, items: form.items.map((item) => ({ ...item })) }) }
  finally { saving.value = false }
}
</script>

<style scoped>
.shipment-form__grid { display: grid; grid-template-columns: 1fr 1fr; gap: 0 12px; }
.shipment-form__grid .el-form-item:last-child { grid-column: 1 / -1; }
.shipment-form__unit { width: 70px; margin-left: 6px; }
.shipment-form__dimensions { display: grid; width: 100%; align-items: center; grid-template-columns: 1fr auto 1fr auto 1fr 70px; gap: 4px; }
.shipment-form__dimensions :deep(.el-input-number) { width: 100%; }
</style>
