import request from '@/utils/request'
import qs from "qs";

// 查询场景列表
export function listScene(query) {
    return request({
        url: '/iiot/scene/list',
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

// 查询场景列表（全部）
export function listSceneAll(query) {
    return request({
        url: '/iiot/scene/alllist',
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

// 查询场景详细
export function getScene(sceneId) {
    return request({
        url: '/iiot/scene/' + sceneId,
        method: 'get',
        paramsSerializer: params => {
            return qs.stringify(params, { arrayFormat: 'repeat' })
        },
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    })
}

// 新增场景
export function addScene(data) {
    return request({
        url: '/iiot/scene',
        method: 'post',
        data: data
    })
}

// 修改场景
export function updateScene(data) {
    return request({
        url: '/iiot/scene',
        method: 'put',
        data: data
    })
}

// 删除场景
export function delScene(sceneId) {
    return request({
        url: '/iiot/scene/' + sceneId,
        method: 'delete',
        paramsSerializer: params => {
            return qs.stringify(params, { arrayFormat: 'repeat' })
        },
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    })
}

// 场景状态修改
export function changeStatusScene(sceneId, status) {
    return request({
        url: '/iiot/scene/changeStatus/' + sceneId + '/' + status,
        method: 'put',
        paramsSerializer: params => {
            return qs.stringify(params, { arrayFormat: 'repeat' })
        },
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    })
}

// 查询场景联动列表
export function listSceneAction(query) {
    return request({
        url: '/iiot/sceneAction/list',
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

// 查询场景联动列表（全部）
export function listSceneActionAll(query) {
    return request({
        url: '/iiot/sceneAction/alllist',
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

// 查询场景联动详细
export function getSceneAction(sceneActionId) {
    return request({
        url: '/iiot/sceneAction/' + sceneActionId,
        method: 'get',
        paramsSerializer: params => {
            return qs.stringify(params, { arrayFormat: 'repeat' })
        },
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    })
}

// 新增场景联动
export function addSceneAction(data) {
    return request({
        url: '/iiot/sceneAction',
        method: 'post',
        data: data
    })
}

// 新增场景联动（批量）
export function addSceneActionBatch(data) {
    return request({
        url: '/iiot/sceneAction/batch',
        method: 'post',
        data: data
    })
}

// 修改场景联动
export function updateSceneAction(data) {
    return request({
        url: '/iiot/sceneAction',
        method: 'put',
        data: data
    })
}

// 修改场景联动（批量）
export function updateSceneActionBatch(data) {
    return request({
        url: '/iiot/sceneAction/batch',
        method: 'put',
        data: data
    })
}

// 删除场景联动
export function delSceneAction(sceneActionId) {
    return request({
        url: '/iiot/sceneAction/' + sceneActionId,
        method: 'delete',
        paramsSerializer: params => {
            return qs.stringify(params, { arrayFormat: 'repeat' })
        },
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    })
}

// 场景联动状态修改
export function changeStatusSceneAction(sceneActionId, status) {
    return request({
        url: '/iiot/sceneAction/changeStatus/' + sceneActionId + '/' + status,
        method: 'put',
        paramsSerializer: params => {
            return qs.stringify(params, { arrayFormat: 'repeat' })
        },
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    })
}

// 查询产品与可写点位关联列表-场景联动
export function getProductPointListAction(query) {
    return request({
        url: '/iiot/sceneAction/productPointList',
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

// 查询规则引擎列表
export function listSceneCondition(query) {
    return request({
        url: '/iiot/sceneCondition/list',
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

// 查询规则引擎列表（全部）
export function listSceneConditionAll(query) {
    return request({
        url: '/iiot/sceneCondition/alllist',
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

// 查询规则引擎详细
export function getSceneCondition(sceneConditionId) {
    return request({
        url: '/iiot/sceneCondition/' + sceneConditionId,
        method: 'get',
        paramsSerializer: params => {
            return qs.stringify(params, { arrayFormat: 'repeat' })
        },
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    })
}

// 新增规则引擎
export function addSceneCondition(data) {
    return request({
        url: '/iiot/sceneCondition',
        method: 'post',
        data: data
    })
}

// 新增规则引擎（批量）
export function addSceneConditionBatch(data) {
    return request({
        url: '/iiot/sceneCondition/batch',
        method: 'post',
        data: data
    })
}

// 修改规则引擎
export function updateSceneCondition(data) {
    return request({
        url: '/iiot/sceneCondition',
        method: 'put',
        data: data
    })
}

// 修改规则引擎
export function updateSceneConditionBatch(data) {
    return request({
        url: '/iiot/sceneCondition/batch',
        method: 'put',
        data: data
    })
}

// 删除规则引擎
export function delSceneCondition(sceneConditionId) {
    return request({
        url: '/iiot/sceneCondition/' + sceneConditionId,
        method: 'delete',
        paramsSerializer: params => {
            return qs.stringify(params, { arrayFormat: 'repeat' })
        },
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    })
}

// 规则引擎状态修改
export function changeStatusSceneCondition(sceneConditionId, status) {
    return request({
        url: '/iiot/sceneCondition/changeStatus/' + sceneConditionId + '/' + status,
        method: 'put',
        paramsSerializer: params => {
            return qs.stringify(params, { arrayFormat: 'repeat' })
        },
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    })
}

// 查询产品与可写点位关联列表-规则引擎
export function getProductPointListCondition(query) {
    return request({
        url: '/iiot/sceneCondition/productPointList',
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