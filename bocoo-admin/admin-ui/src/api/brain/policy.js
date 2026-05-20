import request from '@/utils/request'
import qs from 'qs' // 引入 qs 模块用于序列化参数

// 查询里程碑列表
export function listPolicy(query) {
    return request({
        url: '/healthbrain/policy/list',
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
// 查询里程碑列表（所有）
export function listPolicyAll(query) {
    return request({
        url: '/healthbrain/policy/alllist',
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

// 查询里程碑详细
export function getPolicy(policyId) {
    return request({
        url: '/healthbrain/policy/' + policyId,
        method: 'get',
        paramsSerializer: params => {
            return qs.stringify(params, { arrayFormat: 'repeat' })
        },
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    })
}

// 新增里程碑
export function addPolicy(data) {
    return request({
        url: '/healthbrain/policy',
        method: 'post',
        data: data
    })
}

// 修改里程碑
export function updatePolicy(data) {
    return request({
        url: '/healthbrain/policy',
        method: 'put',
        data: data
    })
}

// 删除里程碑
export function delPolicy(policyId) {
    return request({
        url: '/healthbrain/policy/' + policyId,
        method: 'delete',
        paramsSerializer: params => {
            return qs.stringify(params, { arrayFormat: 'repeat' })
        },
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    })
}
