import NProgress from 'nprogress'
import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getToken } from '@/utils/auth'
import { useUserStore } from '@/stores/user'
import { usePermissionStore } from '@/stores/permission'
import Layout from '@/layout/index.vue'
import LoginPage from '@/pages/auth/LoginPage.vue'
import MerchantApplyPage from '@/pages/auth/MerchantApplyPage.vue'
import LegalDocumentViewPage from '@/pages/auth/LegalDocumentViewPage.vue'
import { getContextPath } from '@/utils/config'

const hiddenLayoutRoutes: RouteRecordRaw[] = [
  {
    path: '/redirect/:path(.*)',
    name: 'Redirect',
    component: () => import('@/pages/redirect/RedirectPage.vue'),
    meta: { hidden: true }
  },
  {
    path: '/user/profile',
    name: 'Profile',
    component: () => import('@/pages/system/user/UserProfilePage.vue'),
    meta: { title: 'user.profileTitle', icon: 'user', hidden: true }
  },
  {
    path: '/system/user-auth/role/:userId(\\d+)',
    name: 'AuthRole',
    component: () => import('@/pages/system/user/UserAuthRolePage.vue'),
    meta: { title: 'user.authRoleTitle', activeMenu: '/system/user', hidden: true }
  },
  {
    path: '/system/role-auth/user/:roleId(\\d+)',
    name: 'AuthUser',
    component: () => import('@/pages/system/RoleAuthUserPage.vue'),
    meta: { title: 'role.authUserTitle', activeMenu: '/system/role', hidden: true }
  },
  {
    path: '/system/dict-data/index/:dictId(\\d+)',
    name: 'DictData',
    component: () => import('@/pages/system/DictDataPage.vue'),
    meta: { title: 'legacy.dictDataTitle', activeMenu: '/system/dict', hidden: true }
  },
  {
    path: '/system/oss-config/index',
    name: 'OssConfig',
    component: () => import('@/pages/system/OssConfigPage.vue'),
    meta: { title: 'legacy.ossConfigTitle', activeMenu: '/system/oss', hidden: true }
  },
  {
    path: '/tool/gen-edit/index/:tableId(\\d+)',
    name: 'GenEdit',
    component: () => import('@/pages/tool/gen/GenEditPage.vue'),
    meta: { title: 'gen.genInfo', activeMenu: '/tool/gen', hidden: true }
  }
]

const legacyFallbackRoutes: RouteRecordRaw[] = [
  {
    path: '/system/user',
    name: 'SystemUser',
    component: () => import('@/pages/system/user/UserPage.vue'),
    meta: { title: 'user.userTitle' }
  },
  {
    path: '/system/role',
    name: 'SystemRole',
    component: () => import('@/pages/system/RolePage.vue'),
    meta: { title: 'role.roleTitle' }
  },
  {
    path: '/system/menu',
    name: 'SystemMenu',
    component: () => import('@/pages/system/MenuPage.vue'),
    meta: { title: 'menu.menuName' }
  },
  {
    path: '/system/dept',
    name: 'SystemDept',
    component: () => import('@/pages/system/DeptPage.vue'),
    meta: { title: 'legacy.deptName' }
  },
  {
    path: '/system/post',
    name: 'SystemPost',
    component: () => import('@/pages/system/PostPage.vue'),
    meta: { title: 'legacy.postName' }
  },
  {
    path: '/system/dict',
    name: 'SystemDict',
    component: () => import('@/pages/system/DictTypePage.vue'),
    meta: { title: 'legacy.dictTitle' }
  },
  {
    path: '/system/config',
    name: 'SystemConfig',
    component: () => import('@/pages/system/ConfigPage.vue'),
    meta: { title: 'legacy.configName' }
  },
  {
    path: '/system/notice',
    name: 'SystemNotice',
    component: () => import('@/pages/system/NoticePage.vue'),
    meta: { title: 'legacy.noticeTitle' }
  },
  {
    path: '/system/oss',
    name: 'SystemOss',
    component: () => import('@/pages/system/OssPage.vue'),
    meta: { title: 'legacy.ossTitle' }
  },
  {
    path: '/system/tenant/applications',
    name: 'LegacyTenantApplications',
    alias: ['/system/tenantApplication', '/merchantManagement/tenantApplication'],
    component: () => import('@/pages/system/TenantApplicationsPlaceholder.vue'),
    meta: { title: 'tenant.applicationsTitle' }
  },
  {
    path: '/system/merchant/profile',
    name: 'SystemMerchantProfile',
    alias: ['/system/merchantProfile', '/merchantManagement/merchantProfile'],
    component: () => import('@/pages/system/MerchantProfilePage.vue'),
    meta: { title: 'merchantProfile.managementTitle' }
  },
  {
    path: '/system/legal/document',
    name: 'SystemLegalDocument',
    component: () => import('@/pages/system/LegalDocumentPage.vue'),
    meta: { title: 'legal.managementTitle' }
  },
  {
    path: '/merchant/profile',
    name: 'MerchantProfile',
    component: () => import('@/pages/merchant/MerchantProfilePage.vue'),
    meta: { title: 'merchantProfile.selfTitle' }
  },
  {
    path: '/monitor/online',
    name: 'MonitorOnline',
    component: () => import('@/pages/monitor/OnlineUsersPage.vue'),
    meta: { title: 'legacy.onlineUsers' }
  },
  {
    path: '/monitor/cache',
    name: 'MonitorCache',
    component: () => import('@/pages/monitor/CacheMonitorPage.vue'),
    meta: { title: 'cache.monitorTitle' }
  },
  {
    path: '/monitor/cache/list',
    name: 'MonitorCacheList',
    alias: '/monitor/cacheList',
    component: () => import('@/pages/monitor/CacheListPage.vue'),
    meta: { title: 'cache.listTitle' }
  },
  {
    path: '/monitor/operlog',
    name: 'MonitorOperlog',
    component: () => import('@/pages/monitor/OperationLogPage.vue'),
    meta: { title: 'operlog.title' }
  },
  {
    path: '/monitor/logininfor',
    name: 'MonitorLoginInfo',
    component: () => import('@/pages/monitor/LoginInfoPage.vue'),
    meta: { title: 'legacy.loginInfoList' }
  },
  {
    path: '/tool/gen',
    name: 'ToolGen',
    component: () => import('@/pages/tool/gen/GenPage.vue'),
    meta: { title: 'tool.gen' }
  },
  {
    path: '/tool/build',
    name: 'ToolBuild',
    component: () => import('@/pages/tool/FormBuildPage.vue'),
    meta: { title: 'toolBuild.title' }
  },
  {
    path: '/monitor/admin',
    name: 'MonitorAdmin',
    component: () => import('@/pages/monitor/AdminMonitorPage.vue'),
    meta: { title: 'external.adminTitle' }
  },
  {
    path: '/monitor/xxljob',
    name: 'MonitorXxlJob',
    component: () => import('@/pages/monitor/XxlJobPage.vue'),
    meta: { title: 'external.xxlJobTitle' }
  }
]

const routes: RouteRecordRaw[] = [
  { path: '/', redirect: '/index' },
  { path: '/login', component: LoginPage, meta: { public: true } },
  { path: '/register', component: MerchantApplyPage, meta: { public: true } },
  { path: '/merchant/apply', component: MerchantApplyPage, meta: { public: true } },
  { path: '/legal/:type(privacy|terms|cookie)', component: LegalDocumentViewPage, meta: { public: true } },
  {
    path: '/',
    name: 'Root',
    component: Layout,
    children: [
      {
        path: 'index',
        name: 'Index',
        component: () => import('@/pages/dashboard/DashboardPage.vue'),
        meta: { title: 'dashboard.base', icon: 'dashboard', affix: true }
      },
      ...legacyFallbackRoutes,
      ...hiddenLayoutRoutes
    ]
  },
  { path: '/:pathMatch(.*)*', name: 'NotFound', component: () => import('@/pages/error/Error404Page.vue') }
]

export const constantRoutes = routes
export const dynamicRoutes: RouteRecordRaw[] = []

const router = createRouter({
  history: createWebHistory(getContextPath()),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

let dynamicRoutesLoaded = false
const dynamicRouteNames = new Set<string>()

export function resetDynamicRoutes() {
  dynamicRouteNames.forEach((name) => {
    if (router.hasRoute(name)) router.removeRoute(name)
  })
  dynamicRouteNames.clear()
  dynamicRoutesLoaded = false
}

export function registerDynamicRoutes(routesToRegister: RouteRecordRaw[]) {
  routesToRegister.forEach((route) => {
    const alreadyRegistered = router.resolve(route.path).matched.some((matched) => matched.path === route.path)
    if (!alreadyRegistered) {
      router.addRoute('Root', route)
      if (route.name && typeof route.name === 'string') dynamicRouteNames.add(route.name)
    }
  })
  dynamicRoutesLoaded = true
}

router.beforeEach(async (to) => {
  NProgress.start()
  const userStore = useUserStore()
  const permissionStore = usePermissionStore()
  const token = getToken()
  if (to.path === '/login' && token) {
    const redirect = typeof to.query.redirect === 'string' ? to.query.redirect : '/index'
    return redirect
  }
  if (to.meta.public) return true
  if (!token) return `/login?redirect=${encodeURIComponent(to.fullPath)}`
  try {
    if (!userStore.user) await userStore.loadUser()
    if (!dynamicRoutesLoaded) {
      const accessRoutes = await permissionStore.generateRoutes()
      registerDynamicRoutes(accessRoutes)
      return { ...to, replace: true }
    }
  } catch (error) {
    await userStore.logout()
    permissionStore.reset()
    resetDynamicRoutes()
    ElMessage.error(error instanceof Error ? error.message : 'Unable to load user profile')
    return '/login'
  }
  return true
})

router.afterEach(() => NProgress.done())
router.onError(() => NProgress.done())

export default router

