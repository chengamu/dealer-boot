import { request, requestData } from '@/utils/request'
import type { PageQuery } from '@/types/api'

export interface LegalDocument {
  documentId?: number
  documentType?: string
  locale?: string
  title?: string
  content?: string
  version?: string
  status?: string
  publishedTime?: string
  remark?: string
}

export interface LegalDocumentQuery extends PageQuery {
  documentType?: string
  locale?: string
  status?: string
}

export function getPublishedLegalDocument(documentType: string) {
  return requestData<LegalDocument>({
    url: `/legal/documents/${documentType}`,
    method: 'get',
    headers: { isToken: false }
  })
}

export function listLegalDocuments(query?: LegalDocumentQuery) {
  return request({
    url: '/system/legal/documents',
    method: 'get',
    params: query
  }) as unknown as Promise<{ rows?: LegalDocument[]; total?: number }>
}

export function getLegalDocument(documentId: number | string) {
  return requestData<LegalDocument>({
    url: `/system/legal/documents/${documentId}`,
    method: 'get'
  })
}

export function addLegalDocument(data: LegalDocument) {
  return request({
    url: '/system/legal/documents',
    method: 'post',
    data
  })
}

export function updateLegalDocument(data: LegalDocument) {
  return request({
    url: '/system/legal/documents',
    method: 'put',
    data
  })
}

export function deleteLegalDocument(documentId: number | string) {
  return request({
    url: `/system/legal/documents/${documentId}`,
    method: 'delete'
  })
}
