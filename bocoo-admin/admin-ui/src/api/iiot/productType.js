import request from '@/utils/request'
import qs from 'qs' // 引入 qs 模块用于序列化参数

// 查询产品类型列表
export function listProductType(query) {
  return request({
    url: '/iiot/productType/list',
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
export function getProductType(productTypeId) {
  return request({
    url: '/iiot/productType/' + productTypeId,
    method: 'get',
    paramsSerializer: params => {
      return qs.stringify(params, { arrayFormat: 'repeat' })
    },
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}

// 查询产品类型下拉树结构
export function treeSelect() {
  return request({
    url: '/iiot/productType/tree',
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
export function addProductType(data) {
  return request({
    url: '/iiot/productType',
    method: 'post',
    data: data
  })
}

// 修改产品类型
export function updateProductType(data) {
  return request({
    url: '/iiot/productType',
    method: 'put',
    data: data
  })
}

// 删除产品类型
export function delProductType(productTypeId) {
  return request({
    url: '/iiot/productType/' + productTypeId,
    method: 'delete',
    paramsSerializer: params => {
      return qs.stringify(params, { arrayFormat: 'repeat' })
    },
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}
