import request from '@/utils/request'
import qs from "qs";

// 查询数据类型列表
export function listFundType(query) {
    return request({
        url: '/healthbrain/fundType/list',
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

// 查询数据类型详细
export function getFundType(fundTypeId) {
    return request({
        url: '/healthbrain/fundType/' + fundTypeId,
        method: 'get',
        paramsSerializer: params => {
            return qs.stringify(params, { arrayFormat: 'repeat' })
        },
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    })
}

// 新增数据类型
export function addFundType(data) {
    return request({
        url: '/healthbrain/fundType',
        method: 'post',
        data: data
    })
}

// 修改数据类型
export function updateFundType(data) {
    return request({
        url: '/healthbrain/fundType',
        method: 'put',
        data: data
    })
}

// 删除数据类型
export function delFundType(fundTypeId) {
    return request({
        url: '/healthbrain/fundType/' + fundTypeId,
        method: 'delete',
        paramsSerializer: params => {
            return qs.stringify(params, { arrayFormat: 'repeat' })
        },
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    })
}

// 查询数据列表
export function listFundData(query) {
    return request({
        url: '/healthbrain/fundData/list',
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

// 查询数据详细
export function getFundData(fundDataId) {
    return request({
        url: '/healthbrain/fundData/' + fundDataId,
        method: 'get',
        paramsSerializer: params => {
            return qs.stringify(params, { arrayFormat: 'repeat' })
        },
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    })
}

// 新增数据类型
export function addFundData(data) {
    return request({
        url: '/healthbrain/fundData',
        method: 'post',
        data: data
    })
}

// 修改数据类型
export function updateFundData(data) {
    return request({
        url: '/healthbrain/fundData',
        method: 'put',
        data: data
    })
}

// 删除数据类型
export function delFundData(fundDataId) {
    return request({
        url: '/healthbrain/fundData/' + fundDataId,
        method: 'delete',
        paramsSerializer: params => {
            return qs.stringify(params, { arrayFormat: 'repeat' })
        },
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    })
}