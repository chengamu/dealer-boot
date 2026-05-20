/**
 * 运行时配置管理工具
 * 用于在运行时获取和管理应用配置
 */

/**
 * 获取运行时配置
 * 优先级：运行时配置 > 编译时环境变量
 * @param {string} key 配置键名
 * @param {any} defaultValue 默认值
 * @returns {any} 配置值
 */
export function getAppConfig(key, defaultValue = undefined) {
    // 首先尝试从运行时配置获取
    if (window.__APP_CONFIG__ && window.__APP_CONFIG__[key] !== undefined) {
        return window.__APP_CONFIG__[key];
    }

    // 如果运行时配置不存在，则使用编译时环境变量
    if (import.meta.env[key] !== undefined) {
        return import.meta.env[key];
    }

    // 返回默认值
    return defaultValue;
}

/**
 * 获取所有运行时配置
 * @returns {object} 所有配置项
 */
export function getAllAppConfig() {
    const compileTimeConfig = import.meta.env;
    const runtimeConfig = window.__APP_CONFIG__ || {};

    // 合并配置，运行时配置优先
    return {
        ...compileTimeConfig,
        ...runtimeConfig
    };
}

/**
 * 检查运行时配置是否已加载
 * @returns {boolean}
 */
export function isRuntimeConfigLoaded() {
    return typeof window.__APP_CONFIG__ === 'object' && window.__APP_CONFIG__ !== null;
}

/**
 * 动态更新运行时配置
 * @param {string} key 配置键名
 * @param {any} value 配置值
 */
export function updateAppConfig(key, value) {
    if (!window.__APP_CONFIG__) {
        window.__APP_CONFIG__ = {};
    }
    window.__APP_CONFIG__[key] = value;
}

/**
 * 获取 API 基础路径
 * @returns {string}
 */
export function getApiBaseUrl() {
    return getAppConfig('VITE_APP_BASE_API', '/dev-api');
}

/**
 * 获取应用标题
 * @returns {string}
 */
export function getAppTitle() {
    return getAppConfig('VITE_APP_TITLE', 'MES后台管理系统');
}

/**
 * 获取应用环境
 * @returns {string}
 */
export function getAppEnv() {
    return getAppConfig('VITE_APP_ENV', 'development');
}

/**
 * 获取上下文路径
 * @returns {string}
 */
export function getContextPath() {
    return getAppConfig('VITE_APP_CONTEXT_PATH', '/');
}

/**
 * 获取监控地址
 * @returns {string}
 */
export function getMonitorUrl() {
    return getAppConfig('VITE_APP_MONITRO_ADMIN', '/admin/applications');
}

/**
 * 获取 XXL-JOB 地址
 * @returns {string}
 */
export function getXxlJobUrl() {
    return getAppConfig('VITE_APP_XXL_JOB_ADMIN', '/xxl-job-admin');
}

/**
 * 是否为生产环境
 * @returns {boolean}
 */
export function isProduction() {
    return getAppEnv() === 'production';
}

/**
 * 是否为开发环境
 * @returns {boolean}
 */
export function isDevelopment() {
    return getAppEnv() === 'development';
}

// 导出一个配置对象，方便直接使用
export const AppConfig = {
    get: getAppConfig,
    getAll: getAllAppConfig,
    update: updateAppConfig,
    isLoaded: isRuntimeConfigLoaded,

    // 便捷方法
    getApiBaseUrl,
    getAppTitle,
    getAppEnv,
    getContextPath,
    getMonitorUrl,
    getXxlJobUrl,
    isProduction,
    isDevelopment
};