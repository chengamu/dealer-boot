import { request, requestData, requestPage } from '@/utils/request'
import type { PageQuery } from '@/types/api'

export interface NoticeQuery extends PageQuery {
  noticeTitle?: string
  createBy?: string
  noticeType?: string
}

export interface Notice {
  noticeId?: number | string
  noticeTitle?: string
  noticeType?: string
  noticeContent?: string
  status?: string
  createBy?: string
  createTime?: string
  read?: boolean
  readTime?: string
}

export function listMyNotices(query: PageQuery) {
  return requestPage<Notice>({
    url: '/system/notice/my/list',
    method: 'get',
    params: query
  })
}

export function getMyUnreadNoticeCount() {
  return requestData<number>({
    url: '/system/notice/my/unread-count',
    method: 'get'
  })
}

export function getMyNotice(noticeId: number | string) {
  return requestData<Notice>({
    url: `/system/notice/my/${noticeId}`,
    method: 'get'
  })
}

export function markAllMyNoticesRead() {
  return request({
    url: '/system/notice/my/read-all',
    method: 'put'
  })
}

export function listNotice(query: NoticeQuery) {
  return requestPage<Notice>({
    url: '/system/notice/list',
    method: 'get',
    params: query
  })
}

export function getNotice(noticeId: number | string) {
  return requestData<Notice>({
    url: `/system/notice/${noticeId}`,
    method: 'get'
  })
}

export function addNotice(data: Notice) {
  return request({
    url: '/system/notice',
    method: 'post',
    data
  })
}

export function updateNotice(data: Notice) {
  return request({
    url: '/system/notice',
    method: 'put',
    data
  })
}

export function delNotice(noticeId: number | string | Array<number | string>) {
  return request({
    url: `/system/notice/${noticeId}`,
    method: 'delete'
  })
}
