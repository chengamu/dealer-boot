import request from '@/utils/request'
import qs from 'qs' // 引入 qs 模块用于序列化参数

// 查询订阅主题列表
export function listPoint(query) {
  return request({
    url: '/iiot/point/alllist',
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
export function getPoint(pointId) {
  return request({
    url: '/iiot/point/' + pointId,
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
export function addPoint(data) {
  return request({
    url: '/iiot/point',
    method: 'post',
    data: data
  })
}

// 修改订阅主题
export function updatePoint(data) {
  return request({
    url: '/iiot/point',
    method: 'put',
    data: data
  })
}

// 删除订阅主题
export function delPoint(pointId) {
  return request({
    url: '/iiot/point/' + pointId,
    method: 'delete',
    paramsSerializer: params => {
      return qs.stringify(params, { arrayFormat: 'repeat' })
    },
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}