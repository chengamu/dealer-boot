import request from '@/utils/request'
import qs from 'qs' // 引入 qs 模块用于序列化参数

// 查询分组列表
export function listGroup(query) {
  return request({
    url: '/iiot/deviceGroup/list',
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

// 查询分组详细
export function getGroup(groupId) {
  return request({
    url: '/iiot/deviceGroup/' + groupId,
    method: 'get',
    paramsSerializer: params => {
      return qs.stringify(params, { arrayFormat: 'repeat' })
    },
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}

// 查询分组下拉树结构
export function treeSelect() {
  return request({
    url: '/iiot/deviceGroup/tree',
    method: 'get',
    paramsSerializer: params => {
      return qs.stringify(params, { arrayFormat: 'repeat' })
    },
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}

// 新增分组
export function addGroup(data) {
  return request({
    url: '/iiot/deviceGroup',
    method: 'post',
    data: data
  })
}

// 修改分组
export function updateGroup(data) {
  return request({
    url: '/iiot/deviceGroup',
    method: 'put',
    data: data
  })
}

// 删除分组
export function delGroup(groupId) {
  return request({
    url: '/iiot/deviceGroup/' + groupId,
    method: 'delete',
    paramsSerializer: params => {
      return qs.stringify(params, { arrayFormat: 'repeat' })
    },
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}
