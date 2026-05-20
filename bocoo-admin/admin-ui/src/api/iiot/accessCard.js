import request from '@/utils/request'
import qs from 'qs' // 引入 qs 模块用于序列化参数

// 查询门禁卡列表
export function listCard(query) {
    return request({
        url: '/dhsdk/device/card/list',
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
// 查询门禁卡列表（所有）
export function listCardAll(query) {
    return request({
        url: '/dhsdk/device/card/alllist',
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

// 查询门禁卡详细
export function getCard(cardId) {
    return request({
        url: '/dhsdk/device/card/' + cardId,
        method: 'get',
        paramsSerializer: params => {
            return qs.stringify(params, { arrayFormat: 'repeat' })
        },
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    })
}

// 新增门禁卡
export function addCard(data) {
    return request({
        url: '/dhsdk/device/card',
        method: 'post',
        data: data
    })
}

// 修改门禁卡
export function updateCard(data) {
    return request({
        url: '/dhsdk/device/card',
        method: 'put',
        data: data
    })
}

// 删除门禁卡
export function delCard(cardId) {
    return request({
        url: '/dhsdk/device/card/' + cardId,
        method: 'delete',
        paramsSerializer: params => {
            return qs.stringify(params, { arrayFormat: 'repeat' })
        },
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    })
}

// 同步门禁卡
export function syncCard(cardId) {
    return request({
        url: '/dhsdk/device/card/sync-to-device?Id=' + cardId,
        method: 'post',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
    })
}
