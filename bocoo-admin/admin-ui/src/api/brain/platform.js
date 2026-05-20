import request from '@/utils/request'
import qs from 'qs' // 引入 qs 模块用于序列化参数

// 查询平台列表
export function listPlatform(query) {
    return request({
        url: '/healthbrain/platform/list',
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
// 查询平台列表（所有）
export function listPlatformAll(query) {
    return request({
        url: '/healthbrain/platform/alllist',
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

// 查询平台详细
export function getPlatform(platformId) {
    return request({
        url: '/healthbrain/platform/' + platformId,
        method: 'get',
        paramsSerializer: params => {
            return qs.stringify(params, { arrayFormat: 'repeat' })
        },
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    })
}

// 新增平台
export function addPlatform(data) {
    return request({
        url: '/healthbrain/platform',
        method: 'post',
        data: data
    })
}

// 修改平台
export function updatePlatform(data) {
    return request({
        url: '/healthbrain/platform',
        method: 'put',
        data: data
    })
}

// 删除平台
export function delPlatform(platformId) {
    return request({
        url: '/healthbrain/platform/' + platformId,
        method: 'delete',
        paramsSerializer: params => {
            return qs.stringify(params, { arrayFormat: 'repeat' })
        },
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    })
}
