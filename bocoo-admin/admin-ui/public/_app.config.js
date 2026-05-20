/**
 * 运行时配置文件
 * 此文件在构建后可以直接修改，无需重新构建项目
 * 配置项会覆盖编译时的环境变量
 */
window.__APP_CONFIG__ = {
    // 应用标题
    VITE_APP_TITLE: 'MES后台管理系统',

    // 环境标识
    VITE_APP_ENV: 'production',

    // API 基础路径
    VITE_APP_BASE_API: '/dev-api',

    // 应用访问路径
    VITE_APP_CONTEXT_PATH: '/',

    // 监控地址
    VITE_APP_MONITRO_ADMIN: '/admin/applications',

    // XXL-JOB 控制台地址
    VITE_APP_XXL_JOB_ADMIN: '/xxl-job-admin',

    // 构建压缩类型
    VITE_BUILD_COMPRESS: 'gzip',

    // 其他自定义配置
    // 可以根据需要添加更多配置项
    // 例如：
    // API_TIMEOUT: 10000,
    // THEME_COLOR: '#1890ff',
    // FEATURE_FLAGS: {
    //   enableNewFeature: true,
    //   enableDebugMode: false
    // }
};
