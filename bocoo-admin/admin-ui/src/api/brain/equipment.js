import request from '@/utils/request'
import qs from 'qs' // 引入 qs 模块用于序列化参数

// 查询共享设备列表
export function listEquipment(query) {
    return request({
        url: '/healthbrain/equipment/list',
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
// 查询共享设备列表（所有）
export function listEquipmentAll(query) {
    return request({
        url: '/healthbrain/equipment/alllist',
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

// 查询共享设备详细
export function getEquipment(equipmentId) {
    return request({
        url: '/healthbrain/equipment/' + equipmentId,
        method: 'get',
        paramsSerializer: params => {
            return qs.stringify(params, { arrayFormat: 'repeat' })
        },
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    })
}

// 新增共享设备
export function addEquipment(data) {
    return request({
        url: '/healthbrain/equipment',
        method: 'post',
        data: data
    })
}

// 修改共享设备
export function updateEquipment(data) {
    return request({
        url: '/healthbrain/equipment',
        method: 'put',
        data: data
    })
}

// 删除共享设备
export function delEquipment(equipmentId) {
    return request({
        url: '/healthbrain/equipment/' + equipmentId,
        method: 'delete',
        paramsSerializer: params => {
            return qs.stringify(params, { arrayFormat: 'repeat' })
        },
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    })
}

// 共享设备显示状态修改
export function changeStatusEquipment(equipmentId, status) {
    return request({
        url: '/healthbrain/equipment/changeStatus/' + equipmentId + '/' + status,
        method: 'post',
        paramsSerializer: params => {
            return qs.stringify(params, { arrayFormat: 'repeat' })
        },
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    })
}
