import service, { request } from '@/utils/request'
import type { RouterVo } from '@/types/api'

export interface LoginPayload {
  username: string
  password: string
  code?: string
  uuid?: string
}

export async function login(data: LoginPayload) {
  return (await service.request({
    url: '/login',
    method: 'post',
    headers: { isToken: false },
    data
  })) as any
}

export async function logout() {
  return request({ url: '/logout', method: 'post' })
}

export async function getInfo() {
  return (await service.request({ url: '/getInfo', method: 'get' })) as any
}

export async function getCodeImg() {
  return (await service.request({
    url: '/captchaImage',
    method: 'get',
    headers: { isToken: false },
    timeout: 20000
  })) as any
}

export async function getRouters() {
  const res = await request<RouterVo[]>({ url: '/getRouters', method: 'get' })
  return res.data || []
}
