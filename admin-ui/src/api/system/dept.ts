import { request, requestData } from '@/utils/request'

export interface Dept {
  deptId?: number | string
  parentId?: number | string
  parentName?: string
  deptName?: string
  orderNum?: number
  leader?: string
  phone?: string
  email?: string
  status?: string
  createTime?: string
  children?: Dept[]
}

export interface DeptQuery {
  deptName?: string
  status?: string
}

export function listDept(query?: DeptQuery) {
  return requestData<Dept[]>({
    url: '/system/dept/list',
    method: 'get',
    params: query
  })
}

export function listDeptExcludeChild(deptId: string | number) {
  return requestData<Dept[]>({
    url: `/system/dept/list/exclude/${deptId}`,
    method: 'get'
  })
}

export function getDept(deptId: string | number) {
  return requestData<Dept>({
    url: `/system/dept/${deptId}`,
    method: 'get'
  })
}

export function addDept(data: Dept) {
  return request({
    url: '/system/dept',
    method: 'post',
    data
  })
}

export function updateDept(data: Dept) {
  return request({
    url: '/system/dept',
    method: 'put',
    data
  })
}

export function delDept(deptId: string | number) {
  return request({
    url: `/system/dept/${deptId}`,
    method: 'delete'
  })
}
