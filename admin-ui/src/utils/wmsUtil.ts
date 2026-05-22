interface WarehouseSkuRow {
  warehouseId?: string | number
  sourceWarehouseId?: string | number
  skuId?: string | number
}

export function getWarehouseAndSkuKey(row: WarehouseSkuRow) {
  return `${row.warehouseId}_${row.skuId}`
}

export function getSourceWarehouseAndSkuKey(row: WarehouseSkuRow) {
  return `${row.sourceWarehouseId}_${row.skuId}`
}
