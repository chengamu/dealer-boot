import { request, requestPage } from '@/utils/request'
import type { PageQuery } from '@/types/api'

export interface RoleQuery extends PageQuery {
  roleName?: string
  roleKey?: string
  status?: string
  beginTime?: string
  endTime?: string
}

export interface Role {
  roleId?: number
  roleName?: string
  roleKey?: string
  roleSort?: number
  status?: string
  dataScope?: string
  menuIds?: Array<number | string>
  deptIds?: Array<number | string>
  menuCheckStrictly?: boolean
  deptCheckStrictly?: boolean
  remark?: string
  createTime?: string
}

export interface RoleUserQuery extends PageQuery {
  roleId?: number | string
  userName?: string
  phonenumber?: string
}

export interface RoleUser {
  userId?: number
  userName?: string
  nickName?: string
  email?: string
  phonenumber?: string
  status?: string
  createTime?: string
}

export interface TreeOption {
  id: number | string
  label: string
  children?: TreeOption[]
}

export interface DeptTreeSelectResponse {
  depts?: TreeOption[]
  checkedKeys?: Array<number | string>
}

export function listRole(query?: RoleQuery) {
  return requestPage<Role>({
    url: '/system/role/list',
    method: 'get',
    params: query
  })
}

export function getRole(roleId: number | string) {
  return request<Role>({
    url: `/system/role/${roleId}`,
    method: 'get'
  })
}

export function addRole(data: Role) {
  return request({
    url: '/system/role',
    method: 'post',
    data
  })
}

export function updateRole(data: Role) {
  return request({
    url: '/system/role',
    method: 'put',
    data
  })
}

export function dataScope(data: Role) {
  return request({
    url: '/system/role/dataScope',
    method: 'put',
    data
  })
}

export function changeRoleStatus(roleId: number | string, status: string) {
  return request({
    url: '/system/role/changeStatus',
    method: 'put',
    data: {
      roleId,
      status
    }
  })
}

export function delRole(roleId: number | string | Array<number | string>) {
  return request({
    url: `/system/role/${roleId}`,
    method: 'delete'
  })
}

export function allocatedUserList(query?: RoleUserQuery) {
  return requestPage<RoleUser>({
    url: '/system/role/authUser/allocatedList',
    method: 'get',
    params: query
  })
}

export function unallocatedUserList(query?: RoleUserQuery) {
  return requestPage<RoleUser>({
    url: '/system/role/authUser/unallocatedList',
    method: 'get',
    params: query
  })
}

export function authUserCancel(data: { userId?: number | string; roleId?: number | string }) {
  return request({
    url: '/system/role/authUser/cancel',
    method: 'put',
    data
  })
}

export function authUserCancelAll(data: { userIds?: string; roleId?: number | string }) {
  return request({
    url: '/system/role/authUser/cancelAll',
    method: 'put',
    params: data
  })
}

export function authUserSelectAll(data: { userIds?: string; roleId?: number | string }) {
  return request({
    url: '/system/role/authUser/selectAll',
    method: 'put',
    params: data
  })
}

export function deptTreeSelect(roleId: number | string) {
  return request<DeptTreeSelectResponse>({
    url: `/system/role/deptTree/${roleId}`,
    method: 'get'
  })
}
