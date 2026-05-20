import request from '@/utils/request'
import qs from 'qs' // 引入 qs 模块用于序列化参数

// 查询技术列表
export function listTechnology(query) {
    return request({
        url: '/healthbrain/technology/list',
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
// 查询技术列表（所有）
export function listTechnologyAll(query) {
    return request({
        url: '/healthbrain/technology/alllist',
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

// 查询技术详细
export function getTechnology(technologyId) {
    return request({
        url: '/healthbrain/technology/' + technologyId,
        method: 'get',
        paramsSerializer: params => {
            return qs.stringify(params, { arrayFormat: 'repeat' })
        },
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    })
}

// 新增技术
export function addTechnology(data) {
    return request({
        url: '/healthbrain/technology',
        method: 'post',
        data: data
    })
}

// 修改技术
export function updateTechnology(data) {
    return request({
        url: '/healthbrain/technology',
        method: 'put',
        data: data
    })
}

// 删除技术
export function delTechnology(technologyId) {
    return request({
        url: '/healthbrain/technology/' + technologyId,
        method: 'delete',
        paramsSerializer: params => {
            return qs.stringify(params, { arrayFormat: 'repeat' })
        },
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    })
}

// 技术显示状态修改
export function changeStatusTechnology(technologyId, status) {
    return request({
        url: '/healthbrain/technology/changeStatus/' + technologyId + '/' + status,
        method: 'post',
        paramsSerializer: params => {
            return qs.stringify(params, { arrayFormat: 'repeat' })
        },
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    })
}

