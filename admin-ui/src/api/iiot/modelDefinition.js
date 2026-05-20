import request from '@/utils/request'
import qs from 'qs' // 引入 qs 模块用于序列化参数

// 查询功能模型列表
export function listModelDefinition(query) {
  return request({
    url: '/iiot/modelDefinition/alllist',
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

// 查询功能模型详细
export function getModelDefinition(modelDefinitionId) {
  return request({
    url: '/iiot/modelDefinition/' + modelDefinitionId,
    method: 'get',
    paramsSerializer: params => {
      return qs.stringify(params, { arrayFormat: 'repeat' })
    },
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}

// 新增功能模型
export function addModelDefinition(data) {
  return request({
    url: '/iiot/modelDefinition',
    method: 'post',
    data: data
  })
}

// 修改功能模型
export function updateModelDefinition(data) {
  return request({
    url: '/iiot/modelDefinition',
    method: 'put',
    data: data
  })
}

// 删除功能模型
export function delModelDefinition(modelDefinitionId) {
  return request({
    url: '/iiot/modelDefinition/' + modelDefinitionId,
    method: 'delete',
    paramsSerializer: params => {
      return qs.stringify(params, { arrayFormat: 'repeat' })
    },
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}

// 删除功能模型缓存
export function refreshModelDefinition(code) {
  return request({
    url: '/iiot/mqtt/gateways/' + code + '/syncCache',
    method: 'post',
    paramsSerializer: params => {
      return qs.stringify(params, { arrayFormat: 'repeat' })
    },
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}
