import request from '@/utils/request'
import qs from 'qs' // 引入 qs 模块用于序列化参数

// 查询里程碑列表
export function listEvent(query) {
    return request({
        url: '/healthbrain/milestone/list',
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
export function listEventAll(query) {
    return request({
        url: '/healthbrain/milestone/alllist',
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
export function getEvent(eventId) {
    return request({
        url: '/healthbrain/milestone/' + eventId,
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
export function addEvent(data) {
    return request({
        url: '/healthbrain/milestone',
        method: 'post',
        data: data
    })
}

// 修改里程碑
export function updateEvent(data) {
    return request({
        url: '/healthbrain/milestone',
        method: 'put',
        data: data
    })
}

// 删除里程碑
export function delEvent(eventId) {
    return request({
        url: '/healthbrain/milestone/' + eventId,
        method: 'delete',
        paramsSerializer: params => {
            return qs.stringify(params, { arrayFormat: 'repeat' })
        },
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    })
}

// 里程碑显示状态修改
export function changeStatusEvent(eventId, status) {
    return request({
        url: '/healthbrain/milestone/changeStatus/' + eventId + '/' + status,
        method: 'post',
        paramsSerializer: params => {
            return qs.stringify(params, { arrayFormat: 'repeat' })
        },
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    })
}
