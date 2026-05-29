export interface ApiResult<T = unknown> {
  code: number
  msg?: string
  data: T
}

export interface PageResult<T = unknown> {
  code?: number
  msg?: string
  rows: T[]
  total: number
}

export interface PageQuery {
  pageNum?: number
  pageSize?: number
  [key: string]: unknown
}

export interface RouteMeta {
  title?: string
  icon?: string
  noCache?: boolean
  link?: string
  activeMenu?: string
  i18nKey?: string
}

export interface RouterVo {
  name?: string
  path: string
  hidden?: boolean
  redirect?: string
  component?: string
  query?: string
  alwaysShow?: boolean
  meta?: RouteMeta
  children?: RouterVo[]
}

export interface DictData {
  dictCode?: number
  dictSort?: number
  dictLabel: string
  dictValue: string
  dictType?: string
  cssClass?: string
  listClass?: string
  isDefault?: string
  status?: string
}

export interface LoginUser {
  userId?: number
  deptId?: number
  tenantId?: number
  tenantType?: string
  merchantId?: number
  userName: string
  nickName?: string
  email?: string
  avatar?: string
  roles?: string[]
  permissions?: string[]
}
