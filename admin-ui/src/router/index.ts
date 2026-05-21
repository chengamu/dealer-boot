import NProgress from 'nprogress'
import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getToken } from '@/utils/auth'
import { useUserStore } from '@/stores/user'
import { usePermissionStore } from '@/stores/permission'
import Layout from '@/layout/index.vue'
import LoginPage from '@/pages/auth/LoginPage.vue'
import MerchantApplyPage from '@/pages/auth/MerchantApplyPage.vue'

const hiddenLayoutRoutes: RouteRecordRaw[] = [
  {
    path: '/redirect/:path(.*)',
    name: 'Redirect',
    component: () => import('@/views/redirect/index.vue'),
    meta: { hidden: true }
  },
  {
    path: '/user/profile',
    name: 'Profile',
    component: () => import('@/views/system/user/profile/index.vue'),
    meta: { title: 'Profile', icon: 'user', hidden: true }
  },
  {
    path: '/system/user-auth/role/:userId(\\d+)',
    name: 'AuthRole',
    component: () => import('@/views/system/user/authRole.vue'),
    meta: { title: 'Auth Role', activeMenu: '/system/user', hidden: true }
  },
  {
    path: '/system/role-auth/user/:roleId(\\d+)',
    name: 'AuthUser',
    component: () => import('@/views/system/role/authUser.vue'),
    meta: { title: 'Auth User', activeMenu: '/system/role', hidden: true }
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
    component: () => import('@/views/system/oss/config.vue'),
    meta: { title: 'Oss Config', activeMenu: '/system/oss', hidden: true }
  },
  {
    path: '/tool/gen-edit/index/:tableId(\\d+)',
    name: 'GenEdit',
    component: () => import('@/views/tool/gen/editTable.vue'),
    meta: { title: 'Gen Edit', activeMenu: '/tool/gen', hidden: true }
  }
]

const legacyFallbackRoutes: RouteRecordRaw[] = [
  {
    path: '/system/user',
    name: 'LegacySystemUser',
    component: () => import('@/views/system/user/index.vue'),
    meta: { title: '鐢ㄦ埛绠＄悊' }
  },
  {
    path: '/system/role',
    name: 'LegacySystemRole',
    component: () => import('@/views/system/role/index.vue'),
    meta: { title: '瑙掕壊绠＄悊' }
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
    name: 'LegacySystemOss',
    component: () => import('@/views/system/oss/index.vue'),
    meta: { title: '鏂囦欢绠＄悊' }
  },
  {
    path: '/system/tenant/applications',
    name: 'LegacyTenantApplications',
    component: () => import('@/views/system/tenant/applications.vue'),
    meta: { title: '鍟嗗瀹℃牳' }
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
    name: 'LegacyToolGen',
    component: () => import('@/views/tool/gen/index.vue'),
    meta: { title: '浠ｇ爜鐢熸垚' }
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
  { path: '/register', component: () => import('@/views/register.vue'), meta: { public: true } },
  { path: '/merchant/apply', component: MerchantApplyPage, meta: { public: true } },
  {
    path: '/',
    name: 'Root',
    component: Layout,
    children: [
      {
        path: 'index',
        name: 'Index',
        component: () => import('@/views/dashboard/charts.vue'),
        meta: { title: 'dashboard.base', icon: 'dashboard', affix: true }
      },
      ...legacyFallbackRoutes,
      ...hiddenLayoutRoutes
    ]
  },
  { path: '/:pathMatch(.*)*', name: 'NotFound', component: () => import('@/views/error/404.vue') }
]

export const constantRoutes = routes
export const dynamicRoutes: RouteRecordRaw[] = []

const router = createRouter({
  history: createWebHistory(import.meta.env.VITE_APP_CONTEXT_PATH || '/'),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

let dynamicRoutesLoaded = false

router.beforeEach(async (to) => {
  NProgress.start()
  const userStore = useUserStore()
  const permissionStore = usePermissionStore()
  if (to.meta.public) return true
  if (!getToken()) return `/login?redirect=${encodeURIComponent(to.fullPath)}`
  try {
    if (!userStore.user) await userStore.loadUser()
    if (!dynamicRoutesLoaded) {
      const accessRoutes = await permissionStore.generateRoutes()
      accessRoutes.forEach((route) => {
        const alreadyRegistered = router.resolve(route.path).matched.some((matched) => matched.path === route.path)
        if (!alreadyRegistered) router.addRoute('Root', route)
      })
      dynamicRoutesLoaded = true
      return { ...to, replace: true }
    }
  } catch (error) {
    await userStore.logout()
    permissionStore.reset()
    dynamicRoutesLoaded = false
    ElMessage.error(error instanceof Error ? error.message : 'Unable to load user profile')
    return '/login'
  }
  return true
})

router.afterEach(() => NProgress.done())
router.onError(() => NProgress.done())

export default router

