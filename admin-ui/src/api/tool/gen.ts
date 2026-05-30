import { request, requestPage } from '@/utils/request'
import type { PageQuery } from '@/types/api'

function datasourceHeader() {
  return { datasource: localStorage.getItem('dataName') || '' }
}

export interface GenTableQuery extends PageQuery {
  dataName?: string
  tableName?: string
  tableComment?: string
  beginTime?: string
  endTime?: string
}

export interface GenColumn {
  columnId?: number
  columnName?: string
  columnComment?: string
  columnType?: string
  javaType?: string
  javaField?: string
  isInsert?: string
  isEdit?: string
  isList?: string
  isQuery?: string
  isRequired?: string
  queryType?: string
  htmlType?: string
  dictType?: string
}

export interface GenTable {
  tableId?: number
  tableName?: string
  tableComment?: string
  className?: string
  functionAuthor?: string
  remark?: string
  createTime?: string
  updateTime?: string
  genType?: string
  genPath?: string
  tplCategory?: string
  packageName?: string
  moduleName?: string
  businessName?: string
  functionName?: string
  parentMenuId?: number | string
  treeCode?: string
  treeName?: string
  treeParentCode?: string
  subTableName?: string
  subTableFkName?: string
  columns?: GenColumn[]
  params?: Record<string, unknown>
}

export interface GenTableDetail {
  rows?: GenColumn[]
  info?: GenTable
  tables?: GenTable[]
}

export function listTable(query?: GenTableQuery) {
  return requestPage<GenTable>({
    headers: datasourceHeader(),
    url: '/tool/gen/list',
    method: 'get',
    params: query
  })
}

export function listDbTable(query?: GenTableQuery) {
  return requestPage<GenTable>({
    headers: datasourceHeader(),
    url: '/tool/gen/db/list',
    method: 'get',
    params: query
  })
}

export function getGenTable(tableId: number | string) {
  return request<GenTableDetail>({
    headers: datasourceHeader(),
    url: `/tool/gen/${tableId}`,
    method: 'get'
  })
}

export function updateGenTable(data: GenTable) {
  return request({
    headers: datasourceHeader(),
    url: '/tool/gen',
    method: 'put',
    data
  })
}

export function importTable(data: { tables: string }) {
  return request({
    headers: datasourceHeader(),
    url: '/tool/gen/importTable',
    method: 'post',
    params: data
  })
}

export function previewTable(tableId: number | string) {
  return request<Record<string, string>>({
    headers: datasourceHeader(),
    url: `/tool/gen/preview/${tableId}`,
    method: 'get'
  })
}

export function delTable(tableId: number | string | Array<number | string>) {
  return request({
    headers: datasourceHeader(),
    url: `/tool/gen/${tableId}`,
    method: 'delete'
  })
}

export function genCode(tableName: string) {
  return request({
    headers: datasourceHeader(),
    url: `/tool/gen/genCode/${tableName}`,
    method: 'get'
  })
}

export function synchDb(tableName: string) {
  return request({
    headers: datasourceHeader(),
    url: `/tool/gen/synchDb/${tableName}`,
    method: 'get'
  })
}
