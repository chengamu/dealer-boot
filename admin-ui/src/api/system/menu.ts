import { request } from '@/utils/request'
import type { TreeOption } from '@/api/system/role'

export interface Menu {
  menuId?: number | string
  parentId?: number | string
  menuName?: string
  i18nKey?: string
  icon?: string
  menuType?: 'M' | 'C' | 'F' | string
  orderNum?: number
  isFrame?: string
  isCache?: string
  visible?: string
  status?: string
  perms?: string
  path?: string
  component?: string
  queryParam?: string
  createTime?: string
  children?: Menu[]
}

export interface MenuQuery {
  menuName?: string
  status?: string
}

export function listMenu(query?: MenuQuery) {
  return request<Menu[]>({
    url: '/system/menu/list',
    method: 'get',
    params: query
  })
}

export function getMenu(menuId: string | number) {
  return request<Menu>({
    url: `/system/menu/${menuId}`,
    method: 'get'
  })
}

export function treeselect() {
  return request<TreeOption[]>({
    url: '/system/menu/treeselect',
    method: 'get'
  })
}

export interface RoleMenuTreeSelectResponse {
  menus?: TreeOption[]
  checkedKeys?: Array<number | string>
}

export function roleMenuTreeselect(roleId: string | number) {
  return request<RoleMenuTreeSelectResponse>({
    url: `/system/menu/roleMenuTreeselect/${roleId}`,
    method: 'get'
  })
}

export function addMenu(data: Menu) {
  return request({
    url: '/system/menu',
    method: 'post',
    data
  })
}

export function updateMenu(data: Menu) {
  return request({
    url: '/system/menu',
    method: 'put',
    data
  })
}

export function delMenu(menuId: string | number) {
  return request({
    url: `/system/menu/${menuId}`,
    method: 'delete'
  })
}
