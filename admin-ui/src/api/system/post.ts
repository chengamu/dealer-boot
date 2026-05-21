import { request, requestData, requestPage } from '@/utils/request'
import type { PageQuery } from '@/types/api'

export interface PostQuery extends PageQuery {
  postCode?: string
  postName?: string
  status?: string
}

export interface Post {
  postId?: number
  postCode?: string
  postName?: string
  postSort?: number
  status?: string
  remark?: string
  createTime?: string
}

export function listPost(query: PostQuery) {
  return requestPage<Post>({
    url: '/system/post/list',
    method: 'get',
    params: query
  })
}

export function getPost(postId: number | string) {
  return requestData<Post>({
    url: `/system/post/${postId}`,
    method: 'get'
  })
}

export function addPost(data: Post) {
  return request({
    url: '/system/post',
    method: 'post',
    data
  })
}

export function updatePost(data: Post) {
  return request({
    url: '/system/post',
    method: 'put',
    data
  })
}

export function delPost(postId: number | string | Array<number | string>) {
  return request({
    url: `/system/post/${postId}`,
    method: 'delete'
  })
}
