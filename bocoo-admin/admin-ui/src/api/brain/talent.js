import request from '@/utils/request'
import qs from 'qs' // 引入 qs 模块用于序列化参数

// 查询人才列表
export function listTalent(query) {
    return request({
        url: '/healthbrain/talent/list',
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
// 查询人才列表（所有）
export function listTalentAll(query) {
    return request({
        url: '/healthbrain/talent/alllist',
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

// 查询人才详细
export function getTalent(talentId) {
    return request({
        url: '/healthbrain/talent/' + talentId,
        method: 'get',
        paramsSerializer: params => {
            return qs.stringify(params, { arrayFormat: 'repeat' })
        },
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    })
}

// 新增人才
export function addTalent(data) {
    return request({
        url: '/healthbrain/talent',
        method: 'post',
        data: data
    })
}

// 修改人才
export function updateTalent(data) {
    return request({
        url: '/healthbrain/talent',
        method: 'put',
        data: data
    })
}

// 删除人才
export function delTalent(talentId) {
    return request({
        url: '/healthbrain/talent/' + talentId,
        method: 'delete',
        paramsSerializer: params => {
            return qs.stringify(params, { arrayFormat: 'repeat' })
        },
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    })
}

// 人才显示状态修改
export function changeStatusTalent(talentId, status) {
    return request({
        url: '/healthbrain/talent/changeStatus/' + talentId + '/' + status,
        method: 'post',
        paramsSerializer: params => {
            return qs.stringify(params, { arrayFormat: 'repeat' })
        },
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    })
}

