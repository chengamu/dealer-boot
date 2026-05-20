import request from '@/utils/request'
import qs from 'qs' // 引入 qs 模块用于序列化参数

// 查询订阅主题列表
export function listAcl(query) {
  return request({
    url: '/iiot/acl/alllist',
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

// 查询订阅主题详细
export function getAcl(aclId) {
  return request({
    url: '/iiot/acl/' + aclId,
    method: 'get',
    paramsSerializer: params => {
      return qs.stringify(params, { arrayFormat: 'repeat' })
    },
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}

// 新增订阅主题
export function addAcl(data) {
  return request({
    url: '/iiot/acl',
    method: 'post',
    data: data
  })
}

// 修改订阅主题
export function updateAcl(data) {
  return request({
    url: '/iiot/acl',
    method: 'put',
    data: data
  })
}

// 删除订阅主题
export function delAcl(aclId) {
  return request({
    url: '/iiot/acl/' + aclId,
    method: 'delete',
    paramsSerializer: params => {
      return qs.stringify(params, { arrayFormat: 'repeat' })
    },
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}

// 初始化
export function initAcl(code, username) {
  return request({
    url: '/iiot/acl/' + code + '/' + username,
    method: 'post',
    paramsSerializer: params => {
      return qs.stringify(params, { arrayFormat: 'repeat' })
    },
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}
