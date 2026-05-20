import request from '@/utils/request'
import qs from 'qs' // 引入 qs 模块用于序列化参数

// 查询产品类型列表
export function listProduct(query) {
  return request({
    url: '/iiot/product/list',
    method: 'get',
    params: query,
    paramsSerializer: params => {
      return qs.stringify(params, { arrayFormat: 'repeat' })
    },
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}

// 查询产品类型详细
export function getProduct(productId) {
  return request({
    url: '/iiot/product/' + productId,
    method: 'get',
    paramsSerializer: params => {
      return qs.stringify(params, { arrayFormat: 'repeat' })
    },
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}

// 新增产品类型
export function addProduct(data) {
  return request({
    url: '/iiot/product',
    method: 'post',
    data: data
  })
}

// 修改产品类型
export function updateProduct(data) {
  return request({
    url: '/iiot/product',
    method: 'put',
    data: data
  })
}

// 删除产品类型
export function delProduct(productId) {
  return request({
    url: '/iiot/product/' + productId,
    method: 'delete',
    paramsSerializer: params => {
      return qs.stringify(params, { arrayFormat: 'repeat' })
    },
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}

// 产品状态修改
export function changeStatusProduct(productId, status) {
  return request({
    url: '/iiot/product/changeStatus/' + productId + '/' + status,
    method: 'put',
    paramsSerializer: params => {
      return qs.stringify(params, { arrayFormat: 'repeat' })
    },
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}

// 按产品类型分组统计产品数量
export function countByProductType() {
  return request({
    url: '/iiot/product/countByProductType',
    method: 'get',
    paramsSerializer: params => {
      return qs.stringify(params, { arrayFormat: 'repeat' })
    },
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}

// 产品logo上传
export function uploadLogo(data) {
  return request({
    url: '/system/oss/avatar',
    method: 'post',
    data: data
  })
}

// 大华摄像头设备连接
export function connectDHDevice(deviceId) {
  return request({
    url: '/dhsdk/device/connect/' + deviceId,
    method: 'post',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}

// 大华摄像头设备断开连接
export function disconnectDHDevice(deviceId) {
  return request({
    url: '/dhsdk/device/disconnect/' + deviceId,
    method: 'post',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}

// Modbus设备-批量启动点位任务
export function startModbusDeviceTask(productId) {
  return request({
    url: '/modbus/runtime/devices/' + productId + '/tasks/start',
    method: 'post',
    paramsSerializer: params => {
      return qs.stringify(params, { arrayFormat: 'repeat' })
    },
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}

// Modbus设备-批量停止点位任务
export function stopModbusDeviceTask(productId) {
  return request({
    url: '/modbus/runtime/devices/' + productId + '/tasks/stop',
    method: 'post',
    paramsSerializer: params => {
      return qs.stringify(params, { arrayFormat: 'repeat' })
    },
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}
