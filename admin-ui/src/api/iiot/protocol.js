import request from '@/utils/request'
import qs from 'qs' // 引入 qs 模块用于序列化参数

// 查询协议列表
export function listProtocol(query) {
  return request({
    url: '/iiot/protocol/list',
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
// 查询协议列表（所有）
export function listProtocolAll(query) {
  return request({
    url: '/iiot/protocol/alllist',
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

// 查询协议详细
export function getProtocol(protocolId) {
  return request({
    url: '/iiot/protocol/' + protocolId,
    method: 'get',
    paramsSerializer: params => {
      return qs.stringify(params, { arrayFormat: 'repeat' })
    },
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}

// 新增协议
export function addProtocol(data) {
  return request({
    url: '/iiot/protocol',
    method: 'post',
    data: data
  })
}

// 修改协议
export function updateProtocol(data) {
  return request({
    url: '/iiot/protocol',
    method: 'put',
    data: data
  })
}

// 删除协议
export function delProtocol(protocolId) {
  return request({
    url: '/iiot/protocol/' + protocolId,
    method: 'delete',
    paramsSerializer: params => {
      return qs.stringify(params, { arrayFormat: 'repeat' })
    },
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}
