type RuntimeConfigValue = string | boolean | number | undefined
type RuntimeConfig = Record<string, RuntimeConfigValue>

function getRuntimeConfig(): RuntimeConfig {
  return window.__APP_CONFIG__ || {}
}

export function getAppConfig<T extends RuntimeConfigValue = RuntimeConfigValue>(key: string, defaultValue?: T): T {
  const runtimeConfig = getRuntimeConfig()
  if (runtimeConfig[key] !== undefined) {
    return runtimeConfig[key] as T
  }

  if (import.meta.env[key] !== undefined) {
    return import.meta.env[key] as T
  }

  return defaultValue as T
}

export function getAllAppConfig() {
  return {
    ...import.meta.env,
    ...getRuntimeConfig()
  }
}

export function isRuntimeConfigLoaded() {
  return typeof window.__APP_CONFIG__ === 'object' && window.__APP_CONFIG__ !== null
}

export function updateAppConfig(key: string, value: RuntimeConfigValue) {
  if (!window.__APP_CONFIG__) {
    window.__APP_CONFIG__ = {}
  }
  window.__APP_CONFIG__[key] = value
}

export function getApiBaseUrl() {
  return getAppConfig<string>('VITE_APP_BASE_API', '/dev-api')
}

export function getAppTitle() {
  return getAppConfig<string>('VITE_APP_TITLE', 'Dealer Admin Portal')
}

export function getAppEnv() {
  return getAppConfig<string>('VITE_APP_ENV', 'development')
}

export function getContextPath() {
  return getAppConfig<string>('VITE_APP_CONTEXT_PATH', '/')
}

export function getMonitorAdminUrl() {
  return getAppConfig<string>('VITE_APP_MONITOR_ADMIN', '/admin/applications')
}

export function getXxlJobUrl() {
  return getAppConfig<string>('VITE_APP_XXL_JOB_ADMIN', '/xxl-job-admin')
}

export function getGoogleClientId() {
  return getAppConfig<string>('VITE_APP_GOOGLE_CLIENT_ID', '')
}

export function isPageAgentEnabled() {
  return String(getAppConfig<string | boolean>('VITE_PAGE_AGENT_ENABLED', false)) === 'true'
}

export function getPageAgentApiKey() {
  return getAppConfig<string>('VITE_PAGE_AGENT_API_KEY', '')
}

export function getPageAgentBaseUrl() {
  return getAppConfig<string>('VITE_PAGE_AGENT_BASE_URL', '')
}

export function getPageAgentModel() {
  return getAppConfig<string>('VITE_PAGE_AGENT_MODEL', '')
}

export function isProduction() {
  return getAppEnv() === 'production'
}

export function isDevelopment() {
  return getAppEnv() === 'development'
}

export const AppConfig = {
  get: getAppConfig,
  getAll: getAllAppConfig,
  update: updateAppConfig,
  isLoaded: isRuntimeConfigLoaded,
  getApiBaseUrl,
  getAppTitle,
  getAppEnv,
  getContextPath,
  getMonitorAdminUrl,
  getXxlJobUrl,
  getGoogleClientId,
  isPageAgentEnabled,
  getPageAgentApiKey,
  getPageAgentBaseUrl,
  getPageAgentModel,
  isProduction,
  isDevelopment
}
