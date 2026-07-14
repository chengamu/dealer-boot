import { ref, type Ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { ComposerTranslation } from 'vue-i18n'
import {
  businessFulfillmentApi,
  factoryProductionApi,
  factoryShipmentApi,
  factoryTrackingApi,
  platformFulfillmentApi,
  type FulfillmentAudience,
  type FulfillmentOrder,
  type Shipment,
  type ShipmentPayload
} from '@/api/dealer-fulfillment'

export function useFulfillmentActions(
  order: FulfillmentOrder,
  reload: () => Promise<void>,
  t: ComposerTranslation,
  audience: FulfillmentAudience
) {
  const editorVisible = ref(false)
  const editingShipment: Ref<Shipment | undefined> = ref()

  async function startProduction() {
    await ElMessageBox.confirm(t('dealer.fulfillment.startConfirm', { orderNo: order.orderNo || '-' }), t('dealer.fulfillment.startProduction'))
    await factoryProductionApi.start(order.salesDocumentId)
    success()
  }

  async function completeProduction() {
    const result = await ElMessageBox.prompt(t('dealer.fulfillment.completeConfirm', { count: order.totalQuantity || 0 }), t('dealer.fulfillment.completeProduction'), { inputType: 'textarea' })
    await factoryProductionApi.complete(order.salesDocumentId, result.value)
    success()
  }

  function createPackage() { editingShipment.value = undefined; editorVisible.value = true }
  function editPackage(shipment: Shipment) { editingShipment.value = shipment; editorVisible.value = true }

  async function savePackage(payload: ShipmentPayload) {
    if (editingShipment.value?.shipmentId) await factoryShipmentApi.update(editingShipment.value.shipmentId, payload)
    else await factoryShipmentApi.create(order.salesDocumentId, payload)
    editorVisible.value = false
    success()
  }

  async function removePackage(shipment: Shipment) {
    await ElMessageBox.confirm(t('dealer.fulfillment.removePackageConfirm'), t('common.delete'))
    await factoryShipmentApi.remove(shipment.shipmentId || '')
    success()
  }

  async function dispatchPackage(shipment: Shipment) {
    await ElMessageBox.confirm(t('dealer.fulfillment.dispatchConfirm', { trackingNo: shipment.trackingNo || '-' }), t('dealer.fulfillment.confirmDispatch'))
    await factoryShipmentApi.dispatch(shipment.shipmentId || '')
    success()
  }

  async function syncTracking(shipment: Shipment) {
    if (audience === 'platform') await platformFulfillmentApi.sync(shipment.shipmentId || '')
    else await factoryTrackingApi.sync(shipment.shipmentId || '')
    success(t('dealer.fulfillment.trackingSynced'))
  }

  async function confirmReceipt(shipment: Shipment) {
    await ElMessageBox.confirm(t('dealer.fulfillment.receiptConfirm', { packageNo: shipment.packageNo || shipment.shipmentNo || '-' }), t('dealer.fulfillment.confirmReceipt'))
    await businessFulfillmentApi.confirmReceipt(shipment.shipmentId || '')
    success()
  }

  async function overrideReceipt(shipment: Shipment) {
    const result = await ElMessageBox.prompt(t('dealer.fulfillment.overrideReceiptHint'), t('dealer.fulfillment.overrideReceipt'), { inputType: 'textarea', inputValidator: (value) => Boolean(value?.trim()) || t('dealer.fulfillment.reasonRequired') })
    await platformFulfillmentApi.overrideReceipt(shipment.shipmentId || '', result.value)
    success()
  }

  async function openProductionSheet() {
    const blob = await factoryProductionApi.sheet(order.salesDocumentId)
    const url = URL.createObjectURL(blob)
    window.open(url, '_blank', 'noopener')
    window.setTimeout(() => URL.revokeObjectURL(url), 60_000)
  }

  function success(message = t('common.operationSuccess')) {
    ElMessage.success(message)
    void reload()
  }

  return { editorVisible, editingShipment, startProduction, completeProduction, createPackage, editPackage, savePackage, removePackage, dispatchPackage, syncTracking, confirmReceipt, overrideReceipt, openProductionSheet }
}
