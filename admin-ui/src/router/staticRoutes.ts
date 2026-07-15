import type { RouteRecordRaw } from 'vue-router'
import Layout from '@/layout/index.vue'
import LoginPage from '@/pages/auth/LoginPage.vue'
import MerchantApplyPage from '@/pages/auth/MerchantApplyPage.vue'
import LegalDocumentViewPage from '@/pages/auth/LegalDocumentViewPage.vue'

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
    meta: { title: 'gen.genInfo', activeMenu: '/system/gen', hidden: true }
  },
  {
    path: '/product-formula/formulas/materials',
    name: 'ProductFormulaMaterials',
    component: () => import('@/pages/product-formula/FormulaMaterialPage.vue'),
    meta: { title: 'productCenter.formula.actions.materials', activeMenu: '/product-formula/formulas', hidden: true, setupSection: 'content' }
  },
  {
    path: '/product-formula/formulas/workbench',
    name: 'ProductFormulaWorkbench',
    component: () => import('@/pages/product-formula/FormulaWorkbenchPage.vue'),
    meta: { title: 'productCenter.formulaWorkbench.title', activeMenu: '/product-formula/formulas', hidden: true }
  },
  {
    path: '/product-formula/formulas/options',
    name: 'ProductFormulaOptions',
    component: () => import('@/pages/product-formula/FormulaOptionPage.vue'),
    meta: { title: 'productCenter.formula.actions.options', activeMenu: '/product-formula/formulas', hidden: true, setupSection: 'options' }
  },
  {
    path: '/product-formula/formulas/simulation',
    name: 'ProductFormulaSimulation',
    component: () => import('@/pages/product-formula/FormulaSimulationPage.vue'),
    meta: { title: 'productCenter.formula.actions.simulation', activeMenu: '/product-formula/formulas', hidden: true }
  },
  {
    path: '/product-formula/reviews/:reviewId',
    name: 'ProductFormulaReviewDetail',
    component: () => import('@/pages/product-formula/FormulaReviewDetailPage.vue'),
    meta: { title: 'productCenter.formulaReview.detailTitle', activeMenu: '/product-formula/reviews', hidden: true }
  },
  {
    path: '/customer/quotes/workbench',
    name: 'CustomerQuoteWorkbench',
    component: () => import('@/pages/customer/CustomerQuoteWorkbenchPage.vue'),
    meta: { title: 'customer.quote.workbenchTitle', activeMenu: '/sales/estimates', hidden: true }
  },
  {
    path: '/sales/dashboard',
    name: 'SalesDashboard',
    component: () => import('@/pages/dashboard/DashboardPage.vue'),
    meta: { title: 'sales.dashboard.menu', activeMenu: '/sales/dashboard', hidden: true, requiresMenuAuthorization: true }
  },
  {
    path: '/sales/quickOrders',
    name: 'QuickOrderList',
    component: () => import('@/pages/quick-order/QuickOrderDraftListPage.vue'),
    meta: { title: 'dealer.quickOrder.menu', activeMenu: '/sales/quickOrders', hidden: true, requiresMenuAuthorization: true }
  },
  {
    path: '/salesOperations/dashboard',
    name: 'PlatformSalesDashboard',
    component: () => import('@/pages/platform-operations/PlatformOperationsOverviewPage.vue'),
    meta: { title: 'platform.sales.dashboard', activeMenu: '/salesOperations/dashboard', hidden: true, requiresMenuAuthorization: true }
  },
  {
    path: '/sales-orders/documents/:id',
    name: 'SalesDocumentDetail',
    component: () => import('@/pages/dealer-sales/SalesDocumentDetailPage.vue'),
    meta: { title: 'dealer.sales.detail', activeMenu: '/sales/salesDocuments', hidden: true }
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
    path: '/merchant/users',
    name: 'MerchantUsers',
    component: () => import('@/pages/merchant/MerchantUserPage.vue'),
    meta: { title: 'merchantUser.managementTitle' }
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
    path: '/monitor/server',
    name: 'MonitorServer',
    component: () => import('@/pages/monitor/ServerResourcePage.vue'),
    meta: { title: 'serverResource.title' }
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

const menuAuthorizedLegacyRoutes = legacyFallbackRoutes.map((route) => ({
  ...route,
  meta: { ...route.meta, requiresMenuAuthorization: true }
}))

export const routes: RouteRecordRaw[] = [
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
        component: () => import('@/pages/system/DefaultHomePage.vue'),
        meta: { title: 'dashboard.base', icon: 'dashboard', affix: true }
      },
      ...menuAuthorizedLegacyRoutes,
      ...hiddenLayoutRoutes
    ]
  }
]

export const notFoundRoute: RouteRecordRaw = {
  path: '/:pathMatch(.*)*',
  name: 'NotFound',
  component: () => import('@/pages/error/Error404Page.vue')
}
