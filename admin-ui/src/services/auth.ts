import service, { request } from '@/utils/request'
import type { LoginUser, RouterVo } from '@/types/api'

export interface LoginPayload {
  username: string
  password: string
  code?: string
  uuid?: string
}

export interface LoginResponse {
  code?: number
  msg?: string
  token?: string
  data?: string | { token?: string }
}

export interface UserInfoResponse {
  code?: number
  msg?: string
  user?: LoginUser
  roles?: string[]
  permissions?: string[]
  data?: LoginUser | {
    user?: LoginUser
    roles?: string[]
    permissions?: string[]
  }
}

export interface CaptchaResponse {
  code?: number
  msg?: string
  captchaEnabled?: boolean
  captchaOnOff?: boolean
  img?: string
  uuid?: string
  data?: {
    captchaEnabled?: boolean
    captchaOnOff?: boolean
    img?: string
    uuid?: string
  }
}

export async function login(data: LoginPayload) {
  return (await service.request({
    url: '/login',
    method: 'post',
    headers: { isToken: false },
    data
  })) as unknown as LoginResponse
}

export async function logout() {
  return request({ url: '/logout', method: 'post' })
}

export async function getInfo() {
  return (await service.request({ url: '/getInfo', method: 'get' })) as unknown as UserInfoResponse
}

export async function getCodeImg() {
  return (await service.request({
    url: '/captchaImage',
    method: 'get',
    headers: { isToken: false },
    timeout: 20000
  })) as unknown as CaptchaResponse
}

export async function getRouters() {
  const res = await request<RouterVo[]>({ url: '/getRouters', method: 'get' })
  return res.data || []
}
