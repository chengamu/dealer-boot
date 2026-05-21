import { request, requestData, requestPage } from '@/utils/request'
import type { PageQuery, TenantApplication } from '@/types/api'

export interface ModuleColumn {
  prop: string
  labelKey: string
  width?: number
  time?: boolean
  status?: boolean
}

export interface ModuleSearchField {
  prop: string
  labelKey: string
  type?: 'input' | 'select' | 'dateRange'
}

export interface ModuleConfig {
  key: string
  titleKey: string
  baseUrl: string
  listUrl?: string
  columns: ModuleColumn[]
  search: ModuleSearchField[]
  createEnabled?: boolean
  exportEnabled?: boolean
}

export const moduleConfigs: Record<string, ModuleConfig> = {
  user: {
    key: 'user',
    titleKey: 'menu.user',
    baseUrl: '/system/user',
    columns: [
      { prop: 'userId', labelKey: 'ID', width: 90 },
      { prop: 'userName', labelKey: 'module.userName' },
      { prop: 'nickName', labelKey: 'module.nickName' },
      { prop: 'email', labelKey: 'module.email' },
      { prop: 'phonenumber', labelKey: 'module.phone' },
      { prop: 'status', labelKey: 'common.status', status: true },
      { prop: 'createTime', labelKey: 'common.createTime', time: true, width: 170 }
    ],
    search: [
      { prop: 'userName', labelKey: 'module.userName' },
      { prop: 'phonenumber', labelKey: 'module.phone' }
    ],
    createEnabled: true,
    exportEnabled: true
  },
  role: {
    key: 'role',
    titleKey: 'menu.role',
    baseUrl: '/system/role',
    columns: [
      { prop: 'roleId', labelKey: 'ID', width: 90 },
      { prop: 'roleName', labelKey: 'module.roleName' },
      { prop: 'roleKey', labelKey: 'module.roleKey' },
      { prop: 'status', labelKey: 'common.status', status: true },
      { prop: 'createTime', labelKey: 'common.createTime', time: true, width: 170 }
    ],
    search: [
      { prop: 'roleName', labelKey: 'module.roleName' },
      { prop: 'roleKey', labelKey: 'module.roleKey' }
    ],
    createEnabled: true
  },
  menu: {
    key: 'menu',
    titleKey: 'menu.menu',
    baseUrl: '/system/menu',
    columns: [
      { prop: 'menuId', labelKey: 'ID', width: 90 },
      { prop: 'menuName', labelKey: 'module.menuName' },
      { prop: 'perms', labelKey: '权限标识' },
      { prop: 'component', labelKey: '组件路径' },
      { prop: 'status', labelKey: 'common.status', status: true },
      { prop: 'createTime', labelKey: 'common.createTime', time: true, width: 170 }
    ],
    search: [{ prop: 'menuName', labelKey: 'module.menuName' }],
    createEnabled: true
  },
  dept: {
    key: 'dept',
    titleKey: 'menu.dept',
    baseUrl: '/system/dept',
    columns: [
      { prop: 'deptId', labelKey: 'ID', width: 90 },
      { prop: 'deptName', labelKey: 'module.deptName' },
      { prop: 'orderNum', labelKey: '排序' },
      { prop: 'status', labelKey: 'common.status', status: true },
      { prop: 'createTime', labelKey: 'common.createTime', time: true, width: 170 }
    ],
    search: [{ prop: 'deptName', labelKey: 'module.deptName' }],
    createEnabled: true
  },
  post: {
    key: 'post',
    titleKey: 'menu.post',
    baseUrl: '/system/post',
    columns: [
      { prop: 'postId', labelKey: 'ID', width: 90 },
      { prop: 'postName', labelKey: 'module.postName' },
      { prop: 'postCode', labelKey: '岗位编码' },
      { prop: 'status', labelKey: 'common.status', status: true },
      { prop: 'createTime', labelKey: 'common.createTime', time: true, width: 170 }
    ],
    search: [{ prop: 'postName', labelKey: 'module.postName' }],
    createEnabled: true
  },
  dict: {
    key: 'dict',
    titleKey: 'menu.dict',
    baseUrl: '/system/dict/type',
    columns: [
      { prop: 'dictId', labelKey: 'ID', width: 90 },
      { prop: 'dictName', labelKey: 'module.dictName' },
      { prop: 'dictType', labelKey: 'module.dictType' },
      { prop: 'status', labelKey: 'common.status', status: true },
      { prop: 'createTime', labelKey: 'common.createTime', time: true, width: 170 }
    ],
    search: [
      { prop: 'dictName', labelKey: 'module.dictName' },
      { prop: 'dictType', labelKey: 'module.dictType' }
    ],
    createEnabled: true
  },
  config: {
    key: 'config',
    titleKey: 'menu.config',
    baseUrl: '/system/config',
    columns: [
      { prop: 'configId', labelKey: 'ID', width: 90 },
      { prop: 'configName', labelKey: 'module.configName' },
      { prop: 'configKey', labelKey: 'module.configKey' },
      { prop: 'createTime', labelKey: 'common.createTime', time: true, width: 170 }
    ],
    search: [
      { prop: 'configName', labelKey: 'module.configName' },
      { prop: 'configKey', labelKey: 'module.configKey' }
    ],
    createEnabled: true
  },
  notice: {
    key: 'notice',
    titleKey: 'menu.notice',
    baseUrl: '/system/notice',
    columns: [
      { prop: 'noticeId', labelKey: 'ID', width: 90 },
      { prop: 'noticeTitle', labelKey: 'module.noticeTitle' },
      { prop: 'createBy', labelKey: '操作人员' },
      { prop: 'createTime', labelKey: 'common.createTime', time: true, width: 170 }
    ],
    search: [{ prop: 'noticeTitle', labelKey: 'module.noticeTitle' }],
    createEnabled: true
  },
  online: {
    key: 'online',
    titleKey: 'menu.online',
    baseUrl: '/monitor/online',
    columns: [
      { prop: 'tokenId', labelKey: '会话编号' },
      { prop: 'userName', labelKey: 'module.userName' },
      { prop: 'ipaddr', labelKey: 'module.ipaddr' },
      { prop: 'browser', labelKey: 'module.browser' },
      { prop: 'os', labelKey: 'module.os' },
      { prop: 'loginTime', labelKey: '登录时间', time: true, width: 170 }
    ],
    search: [
      { prop: 'ipaddr', labelKey: 'module.ipaddr' },
      { prop: 'userName', labelKey: 'module.userName' }
    ]
  },
  operlog: {
    key: 'operlog',
    titleKey: 'menu.operlog',
    baseUrl: '/monitor/operlog',
    columns: [
      { prop: 'operId', labelKey: '日志编号', width: 120 },
      { prop: 'title', labelKey: 'module.title' },
      { prop: 'businessType', labelKey: 'module.businessType' },
      { prop: 'operName', labelKey: 'module.operName' },
      { prop: 'operIp', labelKey: 'module.operIp' },
      { prop: 'status', labelKey: 'common.status', status: true },
      { prop: 'operTime', labelKey: 'common.createTime', time: true, width: 170 }
    ],
    search: [
      { prop: 'operIp', labelKey: 'module.operIp' },
      { prop: 'title', labelKey: 'module.title' },
      { prop: 'operName', labelKey: 'module.operName' }
    ],
    exportEnabled: true
  },
  logininfor: {
    key: 'logininfor',
    titleKey: 'menu.logininfor',
    baseUrl: '/monitor/logininfor',
    columns: [
      { prop: 'infoId', labelKey: '日志编号', width: 120 },
      { prop: 'userName', labelKey: 'module.loginName' },
      { prop: 'ipaddr', labelKey: 'module.ipaddr' },
      { prop: 'browser', labelKey: 'module.browser' },
      { prop: 'os', labelKey: 'module.os' },
      { prop: 'status', labelKey: 'common.status', status: true },
      { prop: 'loginTime', labelKey: '登录时间', time: true, width: 170 }
    ],
    search: [
      { prop: 'ipaddr', labelKey: 'module.ipaddr' },
      { prop: 'userName', labelKey: 'module.loginName' }
    ],
    exportEnabled: true
  },
  gen: {
    key: 'gen',
    titleKey: 'menu.gen',
    baseUrl: '/tool/gen',
    columns: [
      { prop: 'tableId', labelKey: 'ID', width: 90 },
      { prop: 'tableName', labelKey: 'module.tableName' },
      { prop: 'tableComment', labelKey: 'module.tableComment' },
      { prop: 'className', labelKey: 'module.className' },
      { prop: 'createTime', labelKey: 'common.createTime', time: true, width: 170 },
      { prop: 'updateTime', labelKey: 'common.updateTime', time: true, width: 170 }
    ],
    search: [
      { prop: 'tableName', labelKey: 'module.tableName' },
      { prop: 'tableComment', labelKey: 'module.tableComment' }
    ],
    createEnabled: false
  }
}

export async function listModuleRows<T>(config: ModuleConfig, params: PageQuery) {
  return requestPage<T>({
    url: config.listUrl || `${config.baseUrl}/list`,
    method: 'get',
    params
  })
}

export async function getModuleDetail<T>(config: ModuleConfig, id: string | number) {
  return requestData<T>({
    url: `${config.baseUrl}/${id}`,
    method: 'get'
  })
}

export async function submitModule(config: ModuleConfig, data: Record<string, unknown>, id?: string | number) {
  return request({
    url: config.baseUrl,
    method: id ? 'put' : 'post',
    data
  })
}

export async function deleteModule(config: ModuleConfig, ids: string | number) {
  return request({
    url: `${config.baseUrl}/${ids}`,
    method: 'delete'
  })
}

export async function listTenantApplications(params: PageQuery) {
  return requestPage<TenantApplication>({
    url: '/system/tenant/applications',
    method: 'get',
    params
  })
}

export async function submitMerchantApplication(data: Record<string, unknown>) {
  return request({
    url: '/merchant/applications',
    method: 'post',
    headers: { isToken: false },
    data
  })
}

export async function approveTenantApplication(id: number) {
  return request({ url: `/system/tenant/applications/${id}/approve`, method: 'post' })
}

export async function rejectTenantApplication(id: number, reason: string) {
  return request({
    url: `/system/tenant/applications/${id}/reject`,
    method: 'post',
    data: { reason }
  })
}
