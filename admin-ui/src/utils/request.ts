import axios, { type AxiosRequestConfig } from 'axios'
import qs from 'qs'
import { ElMessage, ElNotification } from 'element-plus'
import { ElLoading } from 'element-plus'
import { saveAs } from 'file-saver'
import { getToken, removeToken } from './auth'
import { useLocaleStore } from '@/stores/locale'
import type { ApiResult, PageResult } from '@/types/api'
import { i18n } from '@/i18n'
import { getApiBaseUrl, getContextPath } from '@/utils/config'

const baseURL = getApiBaseUrl()

export type RequestConfig = AxiosRequestConfig & {
  silentError?: boolean
}

const service = axios.create({
  baseURL,
  timeout: 20000,
  paramsSerializer: (params) => qs.stringify(params, { allowDots: true })
})

const AUDIT_FIELDS = ['createTime', 'updateTime', 'createBy', 'updateBy', 'createById', 'updateById']
let authRedirecting = false

function isPlainObject(value: unknown): value is Record<string, unknown> {
  return Object.prototype.toString.call(value) === '[object Object]'
}

function sanitizeMutationData(data: unknown) {
  if (!isPlainObject(data)) return data
  const payload = { ...data }
  AUDIT_FIELDS.forEach((field) => delete payload[field])
  return payload
}

function redirectToLogin(message?: string) {
  if (authRedirecting) return
  authRedirecting = true
  removeToken()
  if (message) ElMessage.error(message)
  const current = `${window.location.pathname}${window.location.search}${window.location.hash}`
  const redirect = current && !current.includes('/login') ? `?redirect=${encodeURIComponent(current)}` : ''
  const basePath = getContextPath().replace(/\/$/, '')
  const loginPath = `${basePath || ''}/login`
  window.location.href = `${loginPath}${redirect}`
}

service.interceptors.request.use((config) => {
  const token = getToken()
  const locale = useLocaleStore().locale
  config.headers = config.headers || {}
  config.headers['Content-Language'] = locale
  if (token && config.headers.isToken !== false) {
    config.headers.Authorization = `Bearer ${token}`
  }
  delete config.headers.isToken
  if (config.method?.toUpperCase() === 'GET') {
    config.headers['Cache-Control'] = 'no-cache'
    config.headers.Pragma = 'no-cache'
  }
  const method = config.method?.toUpperCase()
  const contentType = String(config.headers['Content-Type'] || config.headers['content-type'] || '')
  if ((method === 'POST' || method === 'PUT' || method === 'PATCH') && !contentType.includes('x-www-form-urlencoded')) {
    config.data = sanitizeMutationData(config.data)
  }
  return config
})

service.interceptors.response.use(
  (response) => {
    const data = response.data
    const silentError = Boolean((response.config as RequestConfig).silentError)
    if (response.request?.responseType === 'blob') return data
    const code = data?.code ?? 200
    if (code === 200 || code === 0 || Array.isArray(data?.rows)) return data
    if (code === 401) {
      redirectToLogin(data?.msg || i18n.global.t('request.invalidSession'))
      return Promise.reject(new Error(data?.msg || 'Unauthorized'))
    }
    const message = data?.msg || i18n.global.t('errorCode.default')
    if (!silentError) ElNotification.error({ title: message })
    return Promise.reject(new Error(message))
  },
  (error) => {
    const t = i18n.global.t
    const silentError = Boolean((error.config as RequestConfig | undefined)?.silentError)
    if (error.response?.status === 401) {
      redirectToLogin(error.response?.data?.msg || t('request.invalidSession'))
      return Promise.reject(error)
    }
    const message = error.message?.includes('timeout')
      ? t('request.timeout')
      : error.message === 'Network Error'
        ? t('request.networkError')
        : error.message || t('errorCode.default')
    if (!silentError) ElMessage.error(message)
    return Promise.reject(error)
  }
)

export async function request<T = unknown>(config: RequestConfig) {
  return (await service.request<ApiResult<T>>(config)) as unknown as ApiResult<T>
}

export async function requestData<T = unknown>(config: RequestConfig) {
  const res = await request<T>(config)
  return res.data
}

export async function requestPage<T = unknown>(config: AxiosRequestConfig) {
  return (await service.request<PageResult<T>>(config)) as unknown as PageResult<T>
}

export async function download(url: string, params: Record<string, unknown>, filename: string, config: AxiosRequestConfig = {}) {
  const loading = ElLoading.service({
    text: i18n.global.t('download.loading') || 'Downloading...',
    background: 'rgba(0, 0, 0, 0.7)'
  })
  try {
    const data = (await service.post(url, params, {
      transformRequest: [(payload) => qs.stringify(payload)],
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      responseType: 'blob',
      ...config
    })) as unknown as Blob
    if (data instanceof Blob && data.type !== 'application/json') {
      saveAs(data, filename)
      return
    }
    if (data instanceof Blob) {
      const text = await data.text()
      const body = JSON.parse(text)
      ElMessage.error(body.msg || i18n.global.t('errorCode.default'))
      return
    }
    saveAs(new Blob([data as unknown as BlobPart]), filename)
  } catch (error) {
    ElMessage.error(i18n.global.t('download.error') || i18n.global.t('errorCode.default'))
    throw error
  } finally {
    loading.close()
  }
}

export default service
