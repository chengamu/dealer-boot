import request from '@/utils/request'
import qs from 'qs' // 引入 qs 模块用于序列化参数

// 查询订阅主题列表
export function listTask(query) {
  return request({
    url: '/iiot/task/alllist',
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
export function getTask(taskId) {
  return request({
    url: '/iiot/task/' + taskId,
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
export function addTask(data) {
  return request({
    url: '/iiot/task',
    method: 'post',
    data: data
  })
}

// 修改订阅主题
export function updateTask(data) {
  return request({
    url: '/iiot/task',
    method: 'put',
    data: data
  })
}

// 删除订阅主题
export function delTask(taskId) {
  return request({
    url: '/iiot/task/' + taskId,
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
export function changeStatusTask(taskId, status) {
  return request({
    url: '/iiot/task/changeStatus/' + taskId + '/' + status,
    method: 'put',
    paramsSerializer: params => {
      return qs.stringify(params, { arrayFormat: 'repeat' })
    },
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}

// Modbus设备-启动单个点位任务
export function startModbusTask(productId) {
  return request({
    url: '/modbus/runtime/tasks/' + productId + '/start',
    method: 'post',
    paramsSerializer: params => {
      return qs.stringify(params, { arrayFormat: 'repeat' })
    },
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}

// Modbus设备-停止单个点位任务
export function stopModbusTask(productId) {
  return request({
    url: '/modbus/runtime/tasks/' + productId + '/stop',
    method: 'post',
    paramsSerializer: params => {
      return qs.stringify(params, { arrayFormat: 'repeat' })
    },
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}