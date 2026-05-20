import request from '@/utils/request'
import qs from 'qs' // 引入 qs 模块用于序列化参数

// 查询创新药列表
export function listDrug(query) {
    return request({
        url: '/healthbrain/drug/list',
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
// 查询创新药列表（所有）
export function listDrugAll(query) {
    return request({
        url: '/healthbrain/drug/alllist',
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

// 查询创新药详细
export function getDrug(drugId) {
    return request({
        url: '/healthbrain/drug/' + drugId,
        method: 'get',
        paramsSerializer: params => {
            return qs.stringify(params, { arrayFormat: 'repeat' })
        },
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    })
}

// 新增创新药
export function addDrug(data) {
    return request({
        url: '/healthbrain/drug',
        method: 'post',
        data: data
    })
}

// 修改创新药
export function updateDrug(data) {
    return request({
        url: '/healthbrain/drug',
        method: 'put',
        data: data
    })
}

// 删除创新药
export function delDrug(drugId) {
    return request({
        url: '/healthbrain/drug/' + drugId,
        method: 'delete',
        paramsSerializer: params => {
            return qs.stringify(params, { arrayFormat: 'repeat' })
        },
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    })
}
