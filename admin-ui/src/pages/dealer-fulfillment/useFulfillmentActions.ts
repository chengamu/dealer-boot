import { ref, type Ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { ComposerTranslation } from 'vue-i18n'
import { productionApi, shipmentApi, trackingApi, type FulfillmentOrder, type Shipment, type ShipmentPayload } from '@/api/dealer-fulfillment'

export function useFulfillmentActions(order: FulfillmentOrder, reload: () => Promise<void>, t: ComposerTranslation) {
  const editorVisible = ref(false)
  const editingShipment: Ref<Shipment | undefined> = ref()

  async function startProduction() {
    await ElMessageBox.confirm(t('dealer.fulfillment.startConfirm', { orderNo: order.orderNo || '-' }), t('dealer.fulfillment.startProduction'))
    await productionApi.start(order.salesDocumentId)
    success()
  }

  async function completeProduction() {
    const result = await ElMessageBox.prompt(t('dealer.fulfillment.completeConfirm', { count: order.totalQuantity || 0 }), t('dealer.fulfillment.completeProduction'), { inputType: 'textarea' })
    await productionApi.complete(order.salesDocumentId, result.value)
    success()
  }

  function createPackage() { editingShipment.value = undefined; editorVisible.value = true }
  function editPackage(shipment: Shipment) { editingShipment.value = shipment; editorVisible.value = true }

  async function savePackage(payload: ShipmentPayload) {
    if (editingShipment.value?.shipmentId) await shipmentApi.update(editingShipment.value.shipmentId, payload)
    else await shipmentApi.create(order.salesDocumentId, payload)
    editorVisible.value = false
    success()
  }

  async function removePackage(shipment: Shipment) {
    await ElMessageBox.confirm(t('dealer.fulfillment.removePackageConfirm'), t('common.delete'))
    await shipmentApi.remove(shipment.shipmentId || '')
    success()
  }

  async function dispatchPackage(shipment: Shipment) {
    await ElMessageBox.confirm(t('dealer.fulfillment.dispatchConfirm', { trackingNo: shipment.trackingNo || '-' }), t('dealer.fulfillment.confirmDispatch'))
    await shipmentApi.dispatch(shipment.shipmentId || '')
    success()
  }

  async function syncTracking(shipment: Shipment) {
    await trackingApi.sync(shipment.shipmentId || '')
    success(t('dealer.fulfillment.trackingSynced'))
  }

  async function confirmReceipt(shipment: Shipment) {
    await ElMessageBox.confirm(t('dealer.fulfillment.receiptConfirm', { packageNo: shipment.packageNo || shipment.shipmentNo || '-' }), t('dealer.fulfillment.confirmReceipt'))
    await trackingApi.confirmReceipt(shipment.shipmentId || '')
    success()
  }

  async function overrideReceipt(shipment: Shipment) {
    const result = await ElMessageBox.prompt(t('dealer.fulfillment.overrideReceiptHint'), t('dealer.fulfillment.overrideReceipt'), { inputType: 'textarea', inputValidator: (value) => Boolean(value?.trim()) || t('dealer.fulfillment.reasonRequired') })
    await trackingApi.overrideReceipt(shipment.shipmentId || '', result.value)
    success()
  }

  async function openProductionSheet() {
    const blob = await productionApi.sheet(order.salesDocumentId)
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
