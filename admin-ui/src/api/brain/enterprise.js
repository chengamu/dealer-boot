import request from '@/utils/request'
import qs from 'qs' // 引入 qs 模块用于序列化参数

// 查询企业列表
export function listEnterprise(query) {
    return request({
        url: '/healthbrain/enterprise/list',
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
// 查询企业列表（所有）
export function listEnterpriseAll(query) {
    return request({
        url: '/healthbrain/enterprise/alllist',
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

// 查询企业详细
export function getEnterprise(enterpriseId) {
    return request({
        url: '/healthbrain/enterprise/' + enterpriseId,
        method: 'get',
        paramsSerializer: params => {
            return qs.stringify(params, { arrayFormat: 'repeat' })
        },
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    })
}

// 新增企业
export function addEnterprise(data) {
    return request({
        url: '/healthbrain/enterprise',
        method: 'post',
        data: data
    })
}

// 修改企业
export function updateEnterprise(data) {
    return request({
        url: '/healthbrain/enterprise',
        method: 'put',
        data: data
    })
}

// 删除企业
export function delEnterprise(enterpriseId) {
    return request({
        url: '/healthbrain/enterprise/' + enterpriseId,
        method: 'delete',
        paramsSerializer: params => {
            return qs.stringify(params, { arrayFormat: 'repeat' })
        },
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    })
}

// 企业显示状态修改
export function changeStatusEnterprise(enterpriseId, status) {
    return request({
        url: '/healthbrain/enterprise/changeStatus/' + enterpriseId + '/' + status,
        method: 'post',
        paramsSerializer: params => {
            return qs.stringify(params, { arrayFormat: 'repeat' })
        },
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    })
}

