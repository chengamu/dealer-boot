import request from '@/utils/request'
import qs from 'qs' // 引入 qs 模块用于序列化参数

// 查询网关列表
export function listDevice(query) {
  return request({
    url: '/iiot/device/list',
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
// 查询网关列表（所有）
export function listDeviceAll(query) {
  return request({
    url: '/iiot/device/alllist',
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

// 查询网关详细
export function getDevice(deviceId) {
  return request({
    url: '/iiot/device/' + deviceId,
    method: 'get',
    paramsSerializer: params => {
      return qs.stringify(params, { arrayFormat: 'repeat' })
    },
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}

// 新增网关
export function addDevice(data) {
  return request({
    url: '/iiot/device',
    method: 'post',
    data: data
  })
}

// 修改网关
export function updateDevice(data) {
  return request({
    url: '/iiot/device',
    method: 'put',
    data: data
  })
}

// 删除网关
export function delDevice(deviceId) {
  return request({
    url: '/iiot/device/' + deviceId,
    method: 'delete',
    paramsSerializer: params => {
      return qs.stringify(params, { arrayFormat: 'repeat' })
    },
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}

// 网关断开
export function disconnectDevice(deviceId) {
  return request({
    url: '/iiot/mqtt/gateways/' + deviceId + '/disconnect',
    method: 'post',
    paramsSerializer: params => {
      return qs.stringify(params, { arrayFormat: 'repeat' })
    },
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}

// 网关连接
export function initializeDevice(deviceId) {
  return request({
    url: '/iiot/mqtt/gateways/' + deviceId + '/initialize',
    method: 'post',
    paramsSerializer: params => {
      return qs.stringify(params, { arrayFormat: 'repeat' })
    },
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}

// 网关断开
export function disconnectDeviceModbus(deviceSn) {
  return request({
    url: '/modbus/management/' + deviceSn + '/disconnect',
    method: 'post',
    paramsSerializer: params => {
      return qs.stringify(params, { arrayFormat: 'repeat' })
    },
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}

// 网关连接
export function initializeDeviceModbus(deviceSn) {
  return request({
    url: '/modbus/management/' + deviceSn + '/connect',
    method: 'post',
    paramsSerializer: params => {
      return qs.stringify(params, { arrayFormat: 'repeat' })
    },
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}

// WebSocket网关启用/禁用
export function changeStatusDeviceWebSocket(deviceId, status) {
  return request({
    url: '/iiot/device/changeStatus/' + deviceId + '/' + status,
    method: 'post',
    paramsSerializer: params => {
      return qs.stringify(params, { arrayFormat: 'repeat' })
    },
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}
