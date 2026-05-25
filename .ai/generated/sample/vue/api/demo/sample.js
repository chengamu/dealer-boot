import request from '@/utils/request'

// 查询样本列表
export function listSample(query) {
  return request({
    url: '/demo/sample/list',
    method: 'get',
    params: query
  })
}

// 查询样本详细
export function getSample(id) {
  return request({
    url: '/demo/sample/' + id,
    method: 'get'
  })
}

// 新增样本
export function addSample(data) {
  return request({
    url: '/demo/sample',
    method: 'post',
    data: data
  })
}

// 修改样本
export function updateSample(data) {
  return request({
    url: '/demo/sample',
    method: 'put',
    data: data
  })
}

// 删除样本
export function delSample(id) {
  return request({
    url: '/demo/sample/' + id,
    method: 'delete'
  })
}
