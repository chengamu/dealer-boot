import { request, requestPage } from '@/utils/request'
import type { PageQuery } from '@/types/api'

function parseStrEmpty(value?: number | string) {
  return value === undefined || value === null || value === '' ? '' : value
}

export interface UserQuery extends PageQuery {
  userName?: string
  phonenumber?: string
  status?: string
  deptId?: number | string
  beginTime?: string
  endTime?: string
}

export interface SysUser {
  userId?: number
  deptId?: number
  userName?: string
  nickName?: string
  password?: string
  forcePasswordChange?: boolean | string
  phonenumber?: string
  email?: string
  sex?: string
  status?: string
  remark?: string
  postIds?: Array<number | string>
  roleIds?: Array<number | string>
  createTime?: string
  deptName?: string
  roles?: UserOptionRole[]
}

export interface UserOptionRole {
  roleId?: number
  roleName?: string
  status?: string
  roleKey?: string
  createTime?: string
  flag?: boolean
}

export interface UserOptionPost {
  postId?: number
  postName?: string
  status?: string
}

export interface UserDetail {
  user?: SysUser
  posts?: UserOptionPost[]
  roles?: UserOptionRole[]
  postIds?: Array<number | string>
  roleIds?: Array<number | string>
}

export interface UserProfile {
  user: SysUser
  roleGroup?: string
  postGroup?: string
}

export interface TreeOption {
  id: number | string
  label: string
  children?: TreeOption[]
}

export function listUser(query?: UserQuery) {
  return requestPage<SysUser>({
    url: '/system/user/list',
    method: 'get',
    params: query
  })
}

export function getUser(userId?: number | string) {
  return request<UserDetail>({
    url: `/system/user/${parseStrEmpty(userId)}`,
    method: 'get'
  })
}

export function addUser(data: SysUser) {
  return request({
    url: '/system/user',
    method: 'post',
    data
  })
}

export function updateUser(data: SysUser) {
  return request({
    url: '/system/user',
    method: 'put',
    data
  })
}

export function delUser(userId: number | string | Array<number | string>) {
  return request({
    url: `/system/user/${userId}`,
    method: 'delete'
  })
}

export function resetUserPwd(userId: number | string, password: string) {
  return request({
    url: '/system/user/resetPwd',
    method: 'put',
    data: {
      userId,
      password
    }
  })
}

export function changeUserStatus(userId: number | string, status: string) {
  return request({
    url: '/system/user/changeStatus',
    method: 'put',
    data: {
      userId,
      status
    }
  })
}

export function getUserProfile() {
  return request<UserProfile>({
    url: '/system/user/profile',
    method: 'get'
  })
}

export function updateUserProfile(data: SysUser) {
  return request({
    url: '/system/user/profile',
    method: 'put',
    data
  })
}

export function updateUserPwd(oldPassword: string, newPassword: string) {
  return request({
    url: '/system/user/profile/updatePwd',
    method: 'put',
    params: {
      oldPassword,
      newPassword
    }
  })
}

export function uploadAvatar(data: FormData) {
  return request<{ imgUrl: string }>({
    url: '/system/user/profile/avatar',
    method: 'post',
    data
  })
}

export function getAuthRole(userId: number | string) {
  return request<{ user?: SysUser; roles?: UserOptionRole[] }>({
    url: `/system/user/authRole/${userId}`,
    method: 'get'
  })
}

export function updateAuthRole(data: { userId?: number | string; roleIds?: string }) {
  return request({
    url: '/system/user/authRole',
    method: 'put',
    params: data
  })
}

export function deptTreeSelect() {
  return request<TreeOption[]>({
    url: '/system/user/deptTree',
    method: 'get'
  })
}
