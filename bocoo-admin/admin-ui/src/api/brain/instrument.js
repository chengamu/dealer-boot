import request from '@/utils/request'
import qs from 'qs' // 引入 qs 模块用于序列化参数

// 查询医疗器械列表
export function listInstrument(query) {
    return request({
        url: '/healthbrain/instrument/list',
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
// 查询医疗器械列表（所有）
export function listInstrumentAll(query) {
    return request({
        url: '/healthbrain/instrument/alllist',
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

// 查询医疗器械详细
export function getInstrument(instrumentId) {
    return request({
        url: '/healthbrain/instrument/' + instrumentId,
        method: 'get',
        paramsSerializer: params => {
            return qs.stringify(params, { arrayFormat: 'repeat' })
        },
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    })
}

// 新增医疗器械
export function addInstrument(data) {
    return request({
        url: '/healthbrain/instrument',
        method: 'post',
        data: data
    })
}

// 修改医疗器械
export function updateInstrument(data) {
    return request({
        url: '/healthbrain/instrument',
        method: 'put',
        data: data
    })
}

// 删除医疗器械
export function delInstrument(instrumentId) {
    return request({
        url: '/healthbrain/instrument/' + instrumentId,
        method: 'delete',
        paramsSerializer: params => {
            return qs.stringify(params, { arrayFormat: 'repeat' })
        },
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    })
}
