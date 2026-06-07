import { request, requestPage } from '@/utils/request'
import type { ProductPageQuery, ProductRecord } from './types'

function listApi(url: string, query?: ProductPageQuery) {
  return requestPage<ProductRecord>({ url, method: 'get', params: query })
}

function getApi(url: string) {
  return request<ProductRecord>({ url, method: 'get' })
}

function saveApi(url: string, method: 'post' | 'put', data: ProductRecord) {
  return request({ url, method, data })
}

function removeApi(url: string) {
  return request({ url, method: 'delete' })
}

export const productImportBatchApi = {
  list: (query?: ProductPageQuery) => listApi('/product-capability/import/batches/list', query),
  get: (id: string | number) => getApi('/product-capability/import/batches/' + id),
  add: (data: ProductRecord) => saveApi('/product-capability/import/batches', 'post', data),
  update: (data: ProductRecord) => saveApi('/product-capability/import/batches', 'put', data),
  remove: (ids: Array<string | number> | string | number) => removeApi('/product-capability/import/batches/' + ids),
  changeStatus: (id: string | number, status: string) => request({ url: '/product-capability/import/batches/change-status/' + id + '/' + status, method: 'put' }),
  parseExcel: (file: File, data?: ProductRecord) => {
    const formData = new FormData()
    formData.append('file', file)
    Object.entries(data ?? {}).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        formData.append(key, String(value))
      }
    })
    return request<ProductRecord>({
      url: '/product-capability/import/batches/parse-excel',
      method: 'post',
      data: formData,
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }
}

export const productImportIssueApi = {
  list: (query?: ProductPageQuery) => listApi('/product-capability/import/issues/list', query),
  get: (id: string | number) => getApi('/product-capability/import/issues/' + id),
  add: (data: ProductRecord) => saveApi('/product-capability/import/issues', 'post', data),
  update: (data: ProductRecord) => saveApi('/product-capability/import/issues', 'put', data),
  remove: (ids: Array<string | number> | string | number) => removeApi('/product-capability/import/issues/' + ids)
}
