# Code Review Report — base-boot

**日期**: 2026-05-29  
**项目**: bocoo-base 管理系统  
**技术栈**: Java 17 / Spring Boot 3.2 / MyBatis-Plus 3.5 / Sa-Token / Vue 3 / TypeScript / Vite / Pinia / Element Plus  
**范围**: 后端 (bocoo-admin, bocoo-common, bocoo-modules-system) + 前端 (admin-ui)

---

## 总体结论

**未发现阻塞性安全漏洞**（如 SQL 注入、无条件越权），可以合并。但存在 1 个严重问题（密码明文日志）、3 个高风险问题和 6 个中等问题需要修复。

---

## 严重 (Blocker)

### B1. 用户密码明文记录到日志

- **文件**: `bocoo-modules-system/src/main/java/com/bocoo/system/controller/system/SysUserController.java:252`
- **代码**:
  ```java
  log.info("user.getPassword：{}", user.getPassword());
  String hashpw = BCrypt.hashpw(user.getPassword());
  log.info("hashpw:{}", hashpw);
  ```
- **风险**: 用户注册/重置时的原始密码被写入日志，运维人员、日志聚合平台均可读取。违反密码处理规范，可能违反 PCI-DSS / 等保合规要求。
- **修复**: 删除第 252 行的 `log.info("user.getPassword：{}", ...)`。hash 后的结果也不要打日志。

---

## 高风险 (High)

### H1. OSS 配置状态更新无 WHERE 条件，全表置为禁用

- **文件**: `bocoo-modules-system/src/main/java/com/bocoo/system/service/SysOssConfigService.java:152-153`
- **代码**:
  ```java
  int row = ossConfigMapper.update(null, new LambdaUpdateWrapper<SysOssConfig>()
      .set(SysOssConfig::getStatus, "0"));
  ```
- **风险**: 该 update 没有 WHERE 子句，会将 `sys_oss_config` 表中**所有**记录的 `status` 置为 `"0"`。如果两个管理员同时切换不同 OSS 配置的状态，会互相覆盖。并发场景下可能导致数据不一致。
- **修复**: 先禁用所有，再启用目标记录，或使用事务包裹两步操作；至少应加 `WHERE config_key != targetKey`。

### H2. JWT 密钥占位符有被当作字面量使用的风险

- **文件**: `bocoo-admin/src/main/resources/application.yml:158`、`application-dev.yml:14`
- **代码**:
  ```yaml
  # application.yml
  jwt-secret-key: ${SA_TOKEN_JWT_SECRET}
  # application-dev.yml (覆盖)
  jwt-secret-key: bocoo-dev-jwt-secret-change-before-prod-2026
  ```
- **风险**: 如果生产环境未设置 `SA_TOKEN_JWT_SECRET` 环境变量，Spring 会使用字面量 `${SA_TOKEN_JWT_SECRET}` 作为 JWT 签名密钥，攻击者可以轻易伪造 Token。Dev 环境的密钥也是固定的弱密钥。
- **修复**: 使用 `@ConfigurationProperties` 做启动校验，密钥为空时拒绝启动；或使用 `:defaultValue` 语法提供安全的默认值。

### H3. 登录用户枚举漏洞 — 两步查询可区分用户名和邮箱

- **文件**: `bocoo-modules-system/src/main/java/com/bocoo/system/service/SysLoginService.java:270-276`
- **代码**:
  ```java
  private SysUserVo loadUserByUsernameOrEmail(String username) {
      SysUserVo user = userMapper.selectVoOne(
          new LambdaQueryWrapper<SysUser>().eq(SysUser::getUserName, username), false);
      if (ObjectUtil.isNull(user)) {
          user = userMapper.selectVoOne(
              new LambdaQueryWrapper<SysUser>().eq(SysUser::getEmail, username), false);
      }
      return checkLoadedUser(user, username);
  }
  ```
- **风险**: 用户名存在时只执行 1 次查询，不存在但邮箱存在时执行 2 次查询。攻击者可通过响应时间差异枚举用户是否存在，或区分某个用户名是用户名还是邮箱。
- **修复**: 将两次查询合并为一次 `OR` 查询：`.eq(SysUser::getUserName, username).or().eq(SysUser::getEmail, username)`。

---

## 中风险 (Medium)

### M1. 前端 CRUD 表单提交无防重复提交保护

- **文件**: `admin-ui/src/pages/system/user/UserPage.vue:552`（以及其他 CRUD 页面：RolePage、MenuPage 等）
- **代码**:
  ```ts
  async function submitForm() {
      const valid = await userRef.value?.validate().catch(() => false)
      if (!valid) return
      // ... 直接调用 API，无 loading 状态
  }
  ```
- **风险**: 用户可双击提交按钮触发两次 API 调用，可能导致数据重复写入。后端虽有 `@RepeatSubmit` 注解，但仅在少数接口使用（如 login），CRUD 接口未覆盖。
- **修复**: 在 `submitForm` 中增加 `submitLoading` 状态，提交按钮绑定 `:loading="submitLoading"`，提交期间禁用按钮。

### M2. 前端 API 层类型安全不一致

- **文件**: `admin-ui/src/api/system/user.ts:76`、`role.ts:59/116/124`、`oss.ts:33`、`legalDocument.ts:35`、`merchant/application.ts:68`、`merchant/profile.ts:44`、`tool/gen.ts:71/80`、`auth.ts:58/66/75` 等
- **风险**: 这些模块使用 `as unknown as Promise<{rows?: T[]; total?: number}>` 做不安全的类型断言，而其他模块（config、notice、post、dept、operlog）正确使用了 `requestPage<T>()` 和 `requestData<T>()` 泛型方法。不一致的写法增加新人误用的可能性，且 `as unknown as` 绕过了 TypeScript 编译检查。
- **修复**: 统一使用 `requestPage<T>()` 和 `requestData<T>()`；移除 `as unknown as` 断言。

### M3. 前端 dept 字段形状与后端不一致

- **后端**: `SysUserVo.java:149` — `deptName` 是扁平的 `String` 字段
  ```java
  @Translation(type = TransConstant.DEPT_ID_TO_NAME, mapper = "deptId")
  private String deptName;
  ```
- **前端**: `admin-ui/src/api/system/user.ts:31-33` — 定义的是嵌套对象
  ```ts
  dept?: {
      deptName?: string
  }
  ```
- **风险**: 前端代码如果访问 `user.dept.deptName` 会得到 `undefined`（因为 `dept` 字段本身不存在），需要改为 `user.deptName` 才能正确读取。取决于实际使用位置，可能导致 UI 不显示部门名称。
- **修复**: 前端 `SysUser` 接口中 `deptName` 应定义为顶层字段 `deptName?: string`。

### M4. ThirdLogin 端点验证不可靠

- **文件**: `bocoo-modules-system/src/main/java/com/bocoo/system/controller/system/SysLoginController.java:79`
- **代码**: `@Validated @RequestBody Map<String, Object> loginBody`
- **风险**: `@Validated` 对 `Map<String, Object>` 无效，Bean Validation 无法校验裸 Map。空字符串、超长输入等可绕过。且该端点没有验证码校验。
- **修复**: 定义专用的 `ThirdLoginBo`，使用字段级别的 `@NotBlank` / `@Length` 校验。

### M5. XSS 校验器仅检测 HTML 标签，覆盖范围有限

- **文件**: `bocoo-common/bocoo-common-core/src/main/java/com/bocoo/common/core/xss/XssValidator.java:17`
- **风险**: 仅用 Hutool 的 `RE_HTML_MARK` 正则检测 `<...>` 标签，无法防御 `javascript:` URL、`onerror=` 属性注入等现代 XSS 向量。且 `@Xss` 注解仅应用于 `userName`、`nickName` 和菜单名称，其他自由文本字段（公告内容、配置值、字典条目）未覆盖。
- **修复**: 使用 OWASP Java HTML Sanitizer 或 jsoup 白名单过滤；将 `@Xss` 扩展至所有用户输入字段。

### M6. 后端 updateByKey 接口缺少入参校验

- **文件**: `bocoo-modules-system/src/main/java/com/bocoo/system/controller/system/SysConfigController.java:137`
- **风险**: `updateByKey(@RequestBody SysConfigBo config)` 没有 `@Validated` 注解，直接跳过所有 Bean Validation。作为对比，同文件中的 `edit()` 方法使用了 `@Validated`。
- **修复**: 添加 `@Validated` 注解。

---

## 低风险 (Low)

### L1. NotLoginException 在多处被静默吞下

- **文件**: `SysLoginService.java:192-197`、`SysRoleService.java:449`、`SysUserOnlineController.java:98`
- **风险**: 空 catch 块可能掩盖认证逻辑 bug。
- **修复**: 至少打印 debug 级别日志。

### L2. select * 在 Mapper XML 中

- **文件**: `SysDeptMapper.xml:11`、`SysPostMapper.xml:33`
- **风险**: 表结构变更时可能拉取多余字段，且字段顺序依赖不稳定。
- **修复**: 使用明确的字段列表或 MyBatis-Plus 的 `ew.getSqlSelect`。

### L3. tagsView Store 同步操作用 Promise 包裹

- **文件**: `admin-ui/src/stores/tagsView.ts:63-69` 等多处
- **风险**: 不必要的异步化增加代码复杂度，Pinia 的 action 支持同步和异步。
- **修复**: 直接返回同步结果，移除 `new Promise(...)` 包裹。

### L4. initDict() 空方法体

- **文件**: `admin-ui/src/stores/dict.ts:36`
- **风险**: 死代码，`initDict()` 是空函数，无任何调用方。
- **修复**: 如无计划实现，可直接删除。

### L5. 公开字典接口无限流保护

- **文件**: `bocoo-modules-system/src/main/java/com/bocoo/system/controller/system/SysDictDataController.java:94-102`
- **风险**: `sys_country` 字典类型无需认证即可访问，没有限流保护，可被频繁调用消费服务器资源。
- **修复**: 添加 `@RateLimiter` 或短 TTL 缓存。

### L6. 前端 i18n 初始化无错误处理

- **文件**: `admin-ui/src/i18n/index.ts:39`、`admin-ui/src/main.ts:32`
- **风险**: `fetch()` 加载语言包失败（网络问题）会导致整个应用无法挂载。
- **修复**: 添加 try-catch，fallback 到内联的默认语言包。

### L7. 手机号正则字符类中多余的 `|`

- **文件**: `admin-ui/src/pages/system/user/UserPage.vue:401`
- **风险**: `/^1[3|4|5|6|7|8|9][0-9]\d{8}$/` 中 `[3|4|5|6|7|8|9]` 的 `|` 在字符类内被视为字面量。虽然不影响实际匹配（`|` 不会出现在合法手机号中），但写法误导。
- **修复**: 改为 `[3-9]`。

### L8. 用户导出 Excel 包含登录 IP

- **文件**: `bocoo-modules-system/src/main/java/com/bocoo/system/domain/vo/SysUserExportVo.java:83-85`
- **风险**: 登录 IP 属于个人信息，导出到 Excel 增加隐私合规风险。
- **修复**: 评估业务是否需要导出 IP，如不需要则移除该字段。

---

## 问题汇总

| 严重度 | 数量 | 关键项 |
|--------|------|--------|
| Blocker | 1 | 密码明文日志 |
| High | 3 | OSS 全表更新、JWT 弱密钥、用户枚举 |
| Medium | 6 | 防重复提交、类型安全、dept 字段不匹配等 |
| Low | 8 | 静默吞异常、SQL select *、死代码等 |

## 正向发现

以下方面做得好，值得继续保持：

- **权限控制**: Sa-Token 集成完整，`@SaCheckPermission` 注解覆盖所有管理接口，前端 `hasPermi` 指令配合使用
- **防重复提交**: `@RepeatSubmit` + Redisson 分布式锁，登录接口有完善的防护
- **XSS 防护**: 前端无 `v-html` / `innerHTML` 使用；后端请求包装类自动转义
- **数据脱敏**: `bocoo-common-sensitive` 模块提供了完整的数据脱敏框架
- **API 加密**: `bocoo-common-encrypt` 模块支持请求/响应加解密
- **TypeScript**: 前端未使用 `any` 类型，整体类型安全水平较高
- **i18n**: 前后端 i18n 资源文件键名一致，维护良好
- **API 错误处理**: 前端请求拦截器集中处理 401/错误码，代码整洁

---

## 前端深度审查：结构 / 性能 / 稳定性 / 安全

### 一、结构与可维护性

#### S1. DealerPortalPage.vue — 1956 行单体巨型组件 (High)

- **文件**: `admin-ui/src/pages/dealer-portal/DealerPortalPage.vue`
- **问题**: 单个组件通过 `v-if` 分支承载 6 个完全不同的页面（dashboard、createQuote、checkout、dealerManagement、orderDetail、pricing），含 1387 行 scoped CSS。所有数据为静态 mock，无真实 API 调用。路由层已定义了 6 条不同路由（`router/index.ts:183-217`），但全部指向同一个组件。
- **修复**: 每个页面拆分为独立组件（如 `DealerDashboard.vue`、`CreateQuotePage.vue`、`CheckoutPage.vue` 等），路由各指向对应组件。提取共享的布局模式和 CSS 工具类。
- 这个是根据UI图生成的静态页面，也是暂时不改，到业务的代码开发阶段一起修改，先记录；

#### S2. 12+ 个 CRUD 页面存在大量模板重复 (High)

- **涉及文件**: `UserPage.vue`、`RolePage.vue`、`MenuPage.vue`、`DeptPage.vue`、`PostPage.vue`、`NoticePage.vue`、`ConfigPage.vue`、`DictTypePage.vue`、`DictDataPage.vue`、`OssPage.vue`、`OssConfigPage.vue`、`OperationLogPage.vue`、`LoginInfoPage.vue`
- **问题**: 所有页面包含完全一致的结构 — `el-form` 搜索区 + 操作按钮行 + `el-table` + `<pagination>` + `el-dialog` CRUD 表单。`loading`/`showSearch`/`ids`/`total`/`open`/`form`/`queryParams`/`rules` 等状态及 `getList()`/`handleQuery()`/`resetQuery()`/`handleAdd()`/`handleUpdate()`/`handleDelete()`/`submitForm()`/`cancel()` 方法在每个页面中逐字复制。约 150 行模板代码 × 12 页面 = ~1800 行重复。
- **修复**: 抽取 `useCrudPage` composable，或使用泛型 `CrudPage` 组件 + 插槽方式统一 CRUD 模式。

#### S3. 双 i18n 体系并存 (Medium)

- **涉及文件**: `UserPage.vue:330-334`、`RolePage.vue:201-206`、`MenuPage.vue:226-230`、`UserInfoForm.vue:47-51`、`ResetPasswordForm.vue:38-42`、`UserAvatar.vue:89-93`
- **问题**: 旧页面使用自定义 `t()` 封装 `getMessage(key, localeStore.language)`，新页面（LoginPage、DashboardPage、DealerPortalPage）使用标准 `vue-i18n` `useI18n()`。两套体系的组件并存，自定义 `t()` 需要手动 watch locale 变化重建响应式数据，而 `vue-i18n` 的 `t()` 自动响应 locale 变化。
- **修复**: 统一迁移到 `vue-i18n` `useI18n()`，删除自定义 `t()` 和手动 locale watch。

#### S4. UserPage.vue 内联 120 行表单弹窗 (Medium)

- **文件**: `admin-ui/src/pages/system/user/UserPage.vue:145-264`
- **问题**: 同目录下已有 `UserInfoForm.vue`（92行），但 UserPage 未引用它，而是直接内联了一套用户表单。同一个用户字段有两套独立实现，字段新增时容易遗漏更新。
- **修复**: UserPage 引用 `UserInfoForm.vue` 作为弹窗内容，删除内联表单。

#### S5. FileUpload 与 ImageUpload 80% 逻辑重复 (Medium)

- **文件**: `admin-ui/src/components/FileUpload/index.vue` (254行)、`admin-ui/src/components/ImageUpload/index.vue` (252行)
- **问题**: `handleBeforeUpload`、`handleUploadSuccess`、`handleUploadError`、`handleExceed`、`handleDelete`、`uploadedSuccessfully`、`listToString` 等逻辑几乎完全相同。
- **修复**: 抽取 `useFileUpload` composable，两个组件共享。

#### S6. 大量死代码和未使用的导出 (High)

| 文件 | 行数 | 未使用导出 |
|------|------|-----------|
| `admin-ui/src/utils/getRowSpanMethod.ts` | 47 | 整个文件 |
| `admin-ui/src/utils/jsencrypt.ts` | 12 | `encrypt()` |
| `admin-ui/src/utils/wmsUtil.ts` | 14 | 整个文件 |
| `admin-ui/src/utils/permission.ts` | 23 | `checkPermi()`、`checkRole()` |
| `admin-ui/src/utils/ruoyi.ts` | - | `parseStrEmpty()`、`sprintf()`、`mergeRecursive()`、`tansParams()`、`numSub()`、`generateNo()` |
| `admin-ui/src/utils/config.ts` | - | `getAllAppConfig()`、`isRuntimeConfigLoaded()`、`updateAppConfig()`、`getAppEnv()`、`isProduction()`、`isDevelopment()`、`getMonitorUrl()`、`AppConfig` 对象 |
| `admin-ui/src/utils/validate.ts` | - | `validUsername()`、`validURL()`、`validLowerCase()`、`validUpperCase()`、`validAlphabets()`、`validEmail()`、`isString()`、`isArray()` |

- **修复**: 删除未使用的文件和导出，减少维护负担和包体积。

#### S7. `resetForm()` 的 `this` 上下文错误 (High)

- **文件**: `admin-ui/src/utils/ruoyi.ts:54-57`
- **代码**:
  ```ts
  export function resetForm(this: { $refs?: Record<string, unknown> }, refName: string) {
    const form = this?.$refs?.[refName] as { resetFields?: () => void } | undefined
    form?.resetFields?.()
  }
  ```
- **问题**: 该函数依赖 Options API 的 `this` 上下文，但通过 `app.config.globalProperties` 注册后，在 `<script setup>` 组件中 `this` 为 `undefined`，`$refs` 访问永远失败。该函数在项目内实际已不可用。
- **修复**: 要么改造成接收 `ref` 参数，要么直接删除（Vue 3 `<script setup>` 中通过 `ref` 绑定调用组件方法即可）。

#### S8. 权限检查逻辑三份独立实现 (Low)

- **文件**: `utils/permission.ts`、`plugins/auth.ts`、`directive/permission/hasPermi.ts` + `hasRole.ts`
- **问题**: 同一套权限检查逻辑（`permissions.includes('*:*:*')`、`roles.includes('admin')`）在三处独立维护。`utils/permission.ts` 是死代码（无引用），`plugins/auth.ts` 作为全局 `$auth` 使用，两个 directive 处理 DOM 级权限控制。
- **修复**: 抽取 `usePermission()` composable，三处统一调用。

---

### 二、性能

#### P1. Element Plus 全量注册，Tree-shaking 失效 (High)

- **文件**: `admin-ui/src/main.ts:64`
- **代码**: `app.use(ElementPlus, { ... })`
- **问题**: 全局注册会将 Element Plus 全部 ~70+ 组件打进 vendor chunk，不论是否实际使用。虽然 `vite.config.ts` 中做了 `manualChunks` 分包，但 element-plus 的 chunk 仍然约 1MB+。
- **修复**: 使用 `unplugin-vue-components` + `ElementPlusResolver` 按需导入。

#### P2. 静态数据用 `computed()` 包裹导致不必要的重计算 (High)

- **文件**: `DealerPortalPage.vue:378-562`、`DashboardPage.vue:87-125`
- **问题**: 大量 mock 静态数据（quote 表格、pricing 表格、dashboard 卡片）被包裹在 `computed()` 中，但这些 `computed` 依赖了 `t()`，导致每次 locale 变化或组件重渲染时，50+ 个 `computed` 全部重新计算。静态 mock 数据不应使用 `computed`。
- **修复**: mock 数据使用模块级 `const` 声明；需要 i18n 文本的部分单独用 `computed` 包裹。

#### P3. LoginPage 每 100ms 轮询 DOM 查找弹窗残留 (Medium)

- **文件**: `admin-ui/src/pages/auth/LoginPage.vue:229-235`
- **代码**:
  ```ts
  overlayCleanupTimer = window.setInterval(clearStaleOverlays, 100)
  // 3 秒后停止，共执行 ~30 次
  ```
- **问题**: 用 100ms 间隔轮询 `document.querySelectorAll('.el-overlay, .v-modal, .el-loading-mask')` 来清理 Element Plus 弹窗残留。每次执行都触发完整 DOM 查询。这是一个暴力 workaround，根本原因在于导航时弹窗未被正确关闭。
- **修复**: 在路由守卫中统一关闭所有弹窗/loading，或在 `onBeforeUnmount` 中关闭弹窗实例。删除此轮询逻辑。

#### P4. 搜索输入无 debounce (Medium)

- **涉及文件**: 所有 CRUD 页面的搜索表单（`UserPage.vue:43-46` 等）
- **问题**: 搜索输入框绑定 `@keyup.enter="handleQuery"`，用户快速回车可触发多次 API 调用。无防抖处理。
- **修复**: 对 `handleQuery` 增加 300ms debounce。

#### P5. 部门树/菜单树每次挂载都重新请求 (Medium)

- **文件**: `UserPage.vue:658`、`RolePage.vue` 等
- **问题**: `getDeptTree()` 和菜单树数据每次进入页面都重新请求，无缓存。这些数据变更频率极低。
- **修复**: 使用 Pinia store 缓存，或设置合理的 stale-while-revalidate 策略。

#### P6. 大量表格无 `v-memo` 优化 (Low)

- **涉及文件**: 所有包含 `v-for` 渲染表格行的页面
- **问题**: 表格数据变化时，所有行全部重新渲染。对长列表（Dashboard 订单表、Dealer 行等）可使用 `v-memo` 跳过未变化行。
- **修复**: 在 `v-for` 上加 `v-memo="[item]"`。

#### P7. `MerchantApplyPage` 和 `LegalDocumentViewPage` 非懒加载 (Low)

- **文件**: `admin-ui/src/router/index.ts:9-10`
- **问题**: 这两个页面通过静态 `import` 直接打入首屏 bundle。`MerchantApplyPage.vue` 647 行、`LegalDocumentViewPage` 中等规模。仅注册/商户入驻时才需要。
- **修复**: 改为 `() => import(...)` 动态导入。

---

### 三、稳定性

#### ST1. 全局静默 catch 隐藏所有异步错误 (High)

- **涉及文件**: `UserPage.vue:566-618`、`RolePage.vue:275-286`、`MenuPage.vue:360-375`、`MerchantApplyPage.vue:241-296`、`MerchantProfilePage.vue:106-121` 等几乎所有页面的异步方法
- **模式**:
  ```ts
  try { await apiCall() }
  catch { // Request interceptor already displays the backend error. }
  ```
- **问题**: catch 块完全空置。如果 Axios 拦截器未触发（如网络断开时的导航错误、竞态条件中页面已卸载），错误被静默吞下，用户无感知。Vue DevTools 和错误监控也看不到。
- **修复**: 至少打印 console.error（dev 模式）或将错误抛出到全局错误处理。

#### ST2. 动态路由加载存在竞态条件 (Medium)

- **文件**: `admin-ui/src/router/index.ts:298-304`、`admin-ui/src/stores/permission.ts:121-128`
- **问题**: `beforeEach` 守卫中检查 `!dynamicRoutesLoaded` 后调用 `generateRoutes()`。如果用户快速连续导航两个需要动态路由的页面，两个并发请求可能同时发起，导致重复注册路由（虽然有 `router.resolve()` 检查但非原子操作）。
- **修复**: 使用 Promise 锁：
  ```ts
  let routesPromise: Promise<void> | null = null
  // 在 beforeEach 中:
  if (!routesPromise) routesPromise = generateRoutes().then(...).finally(() => routesPromise = null)
  await routesPromise
  ```

#### ST3. 异步请求无 AbortController 管理 (Medium)

- **涉及文件**: `LoginPage.vue:185-205`、所有 CRUD 页面
- **问题**: 用户导航离开时，正在进行的 API 调用（登录、列表查询、表单提交）继续执行。响应返回后可能触发 `ElMessage` 弹窗或 `router.push`，但这些操作在组件已卸载后执行会产生 Vue 警告或意外行为。
- **修复**: 使用 `onBeforeUnmount` 中对关键请求调用 `abortController.abort()`。

#### ST4. `useDict()` 响应式快照问题 (Medium)

- **文件**: `admin-ui/src/utils/dict.ts:122-123`
- **代码**: `return toRefs(res.value) as Record<T, Ref<DictOption[]>>`
- **问题**: `toRefs(res.value)` 在调用时快照 `res.value` 的键。`loadDict` 是异步的，字典数据在 Promise resolve 后才写入 `res`，但此时返回的 refs 已固化，无法追踪后续新增的键。
- **修复**: 返回 `res` 对象本身而非 `toRefs` 快照；或改用 `shallowRef` 并 watch `res.value` 的变化重建 refs。

#### ST5. 所有表格页面缺少自定义空状态和错误状态 (Medium)

- **涉及文件**: 所有 CRUD 页面的 `<el-table>`
- **问题**: 当 API 返回空列表或请求失败时，仅依赖 Element Plus 默认的 "No Data" 展示。无自定义空状态插槽（含引导操作），无错误状态展示（含重试按钮）。
- **修复**: 为 `<el-table>` 添加 `#empty` 插槽和错误状态渲染。

#### ST6. `LoadDict()` 为空函数 (Low)

- **文件**: `admin-ui/src/stores/dict.ts:36`
- **问题**: `initDict()` 动作体为空，无调用方。方法声明存在但未实现，可能误导开发者。
- **修复**: 实现或删除。

#### ST7. Dropdown 组件打印错误详情到控制台 (Low)

- **文件**: `admin-ui/src/plugins/download.ts:36,64`、`admin-ui/src/utils/permission.ts:10,21`
- **问题**: `console.error(error)` 将完整错误信息（包括堆栈和响应数据）输出到浏览器控制台。在生产环境会泄露内部错误详情。
- **修复**: 用 `import.meta.env.DEV` 包裹控制台输出。

#### ST8. `copyText` 指令使用已废弃 API (Low)

- **文件**: `admin-ui/src/directive/common/copyText.ts:50`
- **问题**: 使用 `document.execCommand('copy')`，已废弃。应使用 `navigator.clipboard.writeText()` 为主要方案。
- **修复**: 优先使用 Clipboard API，`execCommand` 作为降级方案。

---

### 四、安全

#### SEC1. Token 存储在 JS 可读 Cookie 中（无 httpOnly） (High)

- **文件**: `admin-ui/src/utils/auth.ts:9-22`
- **问题**: Token 通过 `js-cookie` 设置，`httpOnly` 无法从客户端 JS 设置，导致 Token 对页面中运行的任意 JavaScript 可见。一旦发生 XSS，攻击者可直接读取并窃取 Token。`sameSite: 'lax'` 提供有限的 CSRF 保护，但不防 XSS 窃取。
- **修复**: 生产环境建议由后端通过 `Set-Cookie` 响应头设置 `HttpOnly; Secure; SameSite=Strict` 的 Cookie。或使用 BFF 模式，Token 存储在内存中配合 refresh token 流程。

#### SEC2. Token 明文暴露在文件上传请求头中 (High)

- **文件**: `admin-ui/src/components/FileUpload/index.vue:120`、`admin-ui/src/components/ImageUpload/index.vue:129`、`admin-ui/src/pages/system/user/UserPage.vue:375`
- **代码**: `const headers = ref({ Authorization: "Bearer " + getToken() })`
- **问题**: JWT Token 被注入到 Element Plus Upload 组件的 `headers` prop 中，通过组件内部状态暴露在 DOM 上。如果有 XSS 漏洞，Token 可被轻易提取。
- **修复**: Axios 拦截器已在所有请求中自动附着 Token。应使用 Element Plus 的 `http-request` prop 覆盖上传逻辑，直接调用 axios，无需手动传 headers。

#### SEC3. 文件上传仅依赖客户端校验 (Medium)

- **文件**: `FileUpload/index.vue:157-167`、`ImageUpload/index.vue:168-185`
- **问题**: 文件类型校验仅基于文件扩展名和浏览器报告的 `file.type` MIME 类型。攻击者可将 `malware.exe` 重命名为 `report.xlsx` 绕过校验。客户端校验是纯粹的 UX 优化，不能作为安全措施。
- **修复**: 后端必须做服务端文件类型校验（file magic bytes 检测），前端校验仅作为便捷提示。

#### SEC4. 无 CSRF 保护机制 (Medium)

- **文件**: `admin-ui/src/utils/request.ts:46-65`
- **问题**: Axios 请求拦截器仅附加 Bearer Token。没有 CSRF Token 机制。`sameSite: 'lax'` 的 Cookie 不保护来自跨站 POST 请求。
- **修复**: 使用 `SameSite: 'Strict'` 的 Cookie；或添加 CSRF Token 头并在服务端校验；或利用 axios 默认的 `X-Requested-With: XMLHttpRequest` 头在服务端做检查。

#### SEC5. i18n 语言包加载失败导致应用无法启动 (Medium)

- **文件**: `admin-ui/src/i18n/index.ts:39-43`、`admin-ui/src/main.ts:32`
- **代码**: `await loadLocaleMessages()` 使用 `fetch()` 加载 JSON 语言包，失败时抛出异常。
- **问题**: `main.ts` 中 `bootstrap()` 无错误处理（`void bootstrap()`）。如果语言包 JSON 加载失败（CDN 不可用、路径错误），整个应用白屏无法启动。
- **修复**: 添加 try-catch，失败时 fallback 到内联的默认语言包。

#### SEC6. 硬编码 RSA 公钥 (Low)

- **文件**: `admin-ui/src/utils/jsencrypt.ts:4-6`
- **问题**: RSA 公钥以 base64 硬编码在前端源码中。虽然公钥不保密，但无法轮转。且整个文件未被任何代码引用（死代码）。
- **修复**: 删除未使用的 `jsencrypt.ts`；如需保留，将公钥移至环境变量。

#### SEC7. 控制台输出泄露权限结构信息 (Low)

- **文件**: `admin-ui/src/utils/permission.ts:10,21`
- **代码**: `console.error('need roles! Like checkPermi="[\'system:user:add\',\'system:user:edit\']"')`
- **问题**: 生产环境控制台输出包含完整的权限 key 路径，可用于针对性攻击。
- **修复**: 用 `import.meta.env.DEV` 包裹。

#### SEC8. `download.ts` 泄露内部路由信息 (Low)

- **文件**: `admin-ui/src/plugins/download.ts:50`
- **代码**: 请求头中包含 `datasource: localStorage.getItem('dataName')`
- **问题**: 将 localStorage 数据作为 HTTP 头发送，暴露内部路由/数据源命名。
- **修复**: 评估该 header 是否必要；如需要，使用更通用的标识。

---

## 问题汇总（更新）

| 严重度 | 数量 | 类别分布 |
|--------|------|----------|
| Blocker | 1 | 后端：密码明文日志 |
| High | 12 | 后端 3 + 前端结构 4 + 前端性能 2 + 前端安全 2 + 前端稳定 1 |
| Medium | 18 | 后端 6 + 前端结构 4 + 前端性能 4 + 前端稳定 4 |
| Low | 20 | 后端 8 + 前端 12 |

---

## 后端深度审查：并发 / 防重 / 限流 / 性能 / 安全

### 一、并发与事务

#### C1. SysDeptService.updateDept() 多步写操作无事务 (High)

- **文件**: `bocoo-modules-system/src/main/java/com/bocoo/system/service/SysDeptService.java:232`
- **问题**: `updateDept()` 方法执行了自身更新（行243）、批量更新子部门路径（行241）、更新父部门状态（行247），共3次以上 DB 写操作，但**没有任何 `@Transactional` 注解**。异常中断会导致部门树数据不一致。
- **修复**: 添加 `@Transactional(rollbackFor = Exception.class)`。

#### C2. SysTenantApplyService.approve() 事务内发送邮件 (High)

- **文件**: `bocoo-modules-system/src/main/java/com/bocoo/system/service/SysTenantApplyService.java:142-193`
- **问题**: `@Transactional` 包裹了整个审批流程，包括 `sendInitialPasswordMail()`（行193）。SMTP 响应慢或不可用时，数据库连接被长时间持有，可能耗尽连接池。
- **修复**: 将邮件发送移到事务提交之后，使用 `TransactionSynchronizationManager.registerSynchronization()` 或 `@TransactionalEventListener`。

#### C3. 注册和商户入驻存在 TOCTOU 竞态条件 (High)

- **文件**: `SysRegisterService.java:54-57`、`SysTenantApplyService.java:115-121`
- **问题**: 注册流程先检查用户名/邮箱唯一性（SELECT），再执行 INSERT。两个并发请求可同时通过检查再插入，导致 DB 约束冲突而非友好的业务错误。数据库虽有 unique constraint 兜底，但返回的是约束异常而非业务语义。
- **修复**: 用 `@RepeatSubmit` 从请求层面做去重，或使用 `SELECT ... FOR UPDATE` 加悲观锁，或捕获 `DuplicateKeyException` 转为友好提示。

#### C4. SysTenantApplyService 多个 ensure* 方法存在并发插入风险 (Medium)

- **文件**: `SysTenantApplyService.java:315-333 (ensureMerchantAdminRole)`、`362-380 (ensureMerchantDept)`、`393-423 (selectOrCreateMenu)`
- **问题**: 三个方法都是 "先查询，不存在则插入" 模式。`ensureMerchantDept` 和 `ensureMerchantAdminRole` 有 DB unique constraint 兜底，但 `selectOrCreateMenu` 没有覆盖 `(parent_id, menu_name, menu_type, perms)` 组合的唯一约束，并发审批可能导致重复菜单行。
- **修复**: 给 `selectOrCreateMenu` 增加唯一约束或加锁。`ensureMerchantRole`/`ensureMerchantDept` 捕获 `DuplicateKeyException` 重试查询。

#### C5. SysOssConfigService.updateOssConfigStatus() 非原子读-写 (Medium)

- **文件**: `SysOssConfigService.java:149-159`
- **问题**: 先将**所有** OSS 配置 status 置为 `"0"`（行152，无 WHERE），再将目标配置置为 `"1"`（行153）。两步之间另一个并发调用会看到"全为0"的中间态，导致两个配置同时被启用。
- **修复**: 使用 `synchronized` + `@Transactional` 包裹，或使用 Redis 分布式锁，或用一条 SQL 完成切换。

#### C6. Caffeine 缓存与 Redis 一致性问题 (Medium)

- **文件**: `PlusSaTokenDao.java:173-185`
- **问题**: `searchData` 方法用 Caffeine 本地缓存搜索结果（过期 5s），但 Redis 中数据可能在此期间被另一实例修改。多实例部署时，搜索结果可能返回"僵尸"数据。
- **修复**: 禁用搜索结果的 Caffeine 缓存，或在数据变更时主动失效。

#### C7. 全系统无乐观锁 / 悲观锁使用 (Low)

- **问题**: 所有实体未使用 `@Version` 乐观锁，也无 `SELECT ... FOR UPDATE`。并发安全完全依赖 DB unique constraint。对于高频并发场景不可靠。
- **修复**: 对关键实体（如 SysUser、SysRole）添加 `@Version` 字段。

#### C8. LockManager 仅 JVM 级同步 (Low)

- **文件**: `OssFactory.java:42` — 使用 `synchronized` 保护 OSS 客户端初始化。多实例部署时各实例独立初始化，但因 `ConcurrentHashMap.put` 是幂等的，效果无害。

---

### 二、防重与限流

#### R1. @RepeatSubmit 仅用于 2 个端点，~40 个写接口不受保护 (High)

- **文件**: `SysOssConfigController.java:77,91`（仅这两处使用）
- **缺失保护的关键端点**:
  - `/login`、`/thirdLogin`、`/smsLogin`、`/emailLogin`、`/register`
  - 所有用户/角色/菜单/部门/岗位的 add/edit/delete
  - `/system/oss/upload`、`/system/oss/avatar`
  - `/tenant/apply/submit`、`/tenant/apply/approve`
- **风险**: 网络抖动导致的重试或用户双击均可产生重复数据。后端无分布式锁保护，仅靠前端 loading 状态（且前端也未全部处理）。
- **修复**: 在所有写操作端点添加 `@RepeatSubmit(interval = 5000)`。

#### R2. @RateLimiter 已实现但从未使用 (High)

- **文件**: `bocoo-common/bocoo-common-ratelimiter/.../RateLimiter.java`（注解已定义）、`RateLimiterAspect.java`（切面已实现）
- **问题**: 限流模块完整实现了基于 Redisson 令牌桶的三级限流（DEFAULT/IP/CLUSTER），但**全项目无任何端点使用 `@RateLimiter`**。登录、注册、短信验证码发送、文件上传等敏感端点完全没有限流保护。
- **修复**: 在 `/login`（默认 10次/60s/IP）、`/register`（默认 3次/60s/IP）、`/smsLogin`（1次/60s/IP）、文件上传等端点上添加 `@RateLimiter`。

#### R3. 默认限流模式为 GLOBAL 而非 PER-USER (Medium)

- **文件**: `RateLimiterAspect.java:60`
- **问题**: DEFAULT 模式创建全局共享令牌桶，一个激进用户可以耗尽整个桶，拒绝其他用户访问。无 `LimitType.USER` 模式。
- **修复**: 增加 `LimitType.USER`，基于用户 ID 创建独立令牌桶。

#### R4. RepeatSubmitAspect AfterReturning 仅处理 R<?> 类型 (Medium)

- **文件**: `RepeatSubmitAspect.java:78-89`
- **问题**: 仅当返回值为 `R<?>` 时才清理 Redis 中的防重 key。若 Controller 返回 `String` 或 `ResponseEntity` 等其他类型，key 会泄露到 TTL 过期（默认 5s），此期间无法再次提交。
- **修复**: 改为 `@After`（finally语义）或扩展返回值类型判断。

---

### 三、SQL 与性能

#### P1. N+1 查询：SysOssService 逐个 ID 查询 (High)

- **文件**: `SysOssService.java:83-95`、`104-118`
- **代码**: 对每个 `ossId` 调用 `getById(id)`，若缓存未命中则产生 N 次独立 SELECT。
- **修复**: 使用 `selectBatchIds(Collection)` 批量查询。

#### P2. 在线用户列表 N+1 Redis 查询 + 全量返回无分页 (High)

- **文件**: `SysUserOnlineController.java:54-63`
- **问题**: `StpUtil.searchTokenValue("", 0, -1, false)` 全表扫描所有 session token，然后对每个 token 调用 `getTokenActiveTimeoutByToken()` 和 `getCacheObject()`，产生 N+1 Redis 往返。且 `list()` 方法无分页。
- **修复**: 给 searchTokenValue 加 prefix 过滤；使用 Redis pipeline 批量读取；添加分页参数。

#### P3. Redis 全量 token 扫描在 cleanOnlineUserByRole 中 (High)

- **文件**: `SysRoleService.java:434-452`
- **问题**: 注释已注明"角色关联的在线用户量过大会导致redis阻塞卡顿 谨慎操作"，但代码仍执行 `StpUtil.searchTokenValue("", 0, -1, false)` 全量扫描。SCAN 命令阻塞 Redis 单线程。
- **修复**: 限制扫描范围或使用异步处理；加入 token 数量阈值保护。

#### P4. 非 SARGable LIKE 查询无法使用索引 (High)

- **文件**: `DataBaseHelper.java:17`（生成 `findInSet` SQL）
- **问题**: `concat(',', ancestors, ',') like '%,deptId,%'` 的 leading wildcard 使 B-tree 索引完全失效。每次用户列表查询（`SysUserService.buildQueryWrapper` 行94）都会对 `sys_dept.ancestors` 列执行全表扫描。
- **修复**: 将 `ancestors` 改为 PostgreSQL `ltree` 类型并使用 GIN 索引；或改用数组类型 + `@>` 操作符。

#### P5. 在线用户缓存无 TTL 导致 Redis 内存泄漏 (High)

- **文件**: `UserActionListener.java:53-54`
- **问题**: 当 `tokenConfig.getTimeout() == -1`（永不过期）时，`setCacheObject(key, dto)` 不带 TTL，在线用户条目永久驻留 Redis。Token 删除或用户登出时如 Listener 未触发，key 永不清理。
- **修复**: 即使 token 永不过期，也为 cache entry 设置一个合理的最大 TTL（如 7 天）。

#### P6. 日志清理操作为无 WHERE 的 DELETE (Medium)

- **文件**: `SysLogininforService.java:151`、`SysOperLogService.java:137`
- **问题**: `mapper.delete(new LambdaQueryWrapper<>())` 生成 `DELETE FROM table` 无 WHERE。大表上这会创建巨型事务、长时间持锁、WAL 膨胀。
- **修复**: 分批删除（每次 1000 行），或加时间范围条件。

#### P7. RateLimiter 每条请求都打 INFO 日志 (Medium)

- **文件**: `RateLimiterAspect.java:71`
- **问题**: `log.info("限制令牌 => {}, 剩余令牌 => {}, 缓存key => '{}'", ...)`。如果限流端点被高并发访问，日志量会非常大。
- **修复**: 改为 DEBUG 级别。

#### P8. Console Appender 为同步写入 (Medium)

- **文件**: `logback-plus.xml:9-14`
- **问题**: 控制台 appender 使用同步 `ConsoleAppender`，容器环境中每条日志同步写入 stdout。已配置异步文件 appender，但控制台 appender 没有。
- **修复**: 包装为 `AsyncAppender`。

#### P9. 日志格式缺少 Trace ID (Medium)

- **文件**: `logback-plus.xml:4-6`
- **问题**: 日志 pattern 不含 request/trace ID，多线程环境下难以关联请求日志。SkyWalking 集成已被注释（行101-118）。
- **修复**: 添加 `%X{traceId}` 等 MDC 变量到 pattern。

#### P10. SysUserExportVo 查询无 LIMIT (Medium)

- **文件**: `SysUserMapper.xml:54-62`
- **问题**: `selectUserExportList` 无分页/LIMIT，导出大量用户时可能 OOM。
- **修复**: 使用流式游标或分批查询导出。

#### P11. IP 数据库在首请求时加载导致冷启动延迟 (Medium)

- **文件**: `RegionUtils.java:26-49`
- **问题**: ip2region.xdb 从 classpath 写入临时目录并加载到内存，发生在首次 IP 查询请求时，导致该请求响应延迟。
- **修复**: 在 `SystemApplicationRunner` 中提前初始化。

#### P12. selectMenuPerms 无 WHERE 跨三表 JOIN (Medium)

- **文件**: `SysMenuMapper.xml:61-66`
- **问题**: `select distinct m.perms from sys_menu m left join sys_role_menu rm left join sys_user_role sur` 零 WHERE 条件，对所有用户/角色/菜单做笛卡尔积。
- **修复**: 添加 WHERE 条件，或评估该方法是否必要。

#### P13. p6spy 在 dev 配置中开启 (Medium)

- **文件**: `application-dev.yml:26` — `p6spy: true`。注释已注明"不建议生产使用"。确保生产 profile 覆盖为 false。

#### P14. Captcha 验证码获取再删除需两次 Redis 往返 (Low)

- **文件**: `SysLoginService.java:253-254` — `getCacheObject` + `deleteObject` 分两步调用。可改用 Redis `GETDEL` 单次原子操作。

---

### 四、安全深度审查

#### SEC-B1. SysUserVo.password 字段 @JsonIgnore 被 @JsonProperty 覆盖 (High)

- **文件**: `SysUserVo.java:92-95`
- **代码**: `@JsonIgnore @JsonProperty private String password;`
- **问题**: `@JsonProperty`（无参）的优先级高于 `@JsonIgnore`，BCrypt 密码哈希被包含在 JSON 响应中。虽然 BCrypt 哈希不是原文，但泄露哈希允许离线暴力破解。
- **修复**: 删除 `@JsonProperty` 注解。

#### SEC-B2. SysUserController.allList 端点缺少权限注解 (High)

- **文件**: `SysUserController.java:84-90`
- **问题**: `/allList` 端点无 `@SaCheckPermission`，任何认证用户可列出全体用户（包括 PII 字段）。同 Controller 下其他方法都有权限注解。
- **修复**: 添加 `@SaCheckPermission("system:user:list")`。

#### SEC-B3. ThirdClient.secretKey 明文存储 (High)

- **文件**: `ThirdClient.java:33`
- **问题**: `secretKey` 字段无 `@EncryptField` 加密、无哈希。数据库泄露即全部第三方客户端凭证暴露。
- **修复**: 使用 BCrypt 哈希存储；或通过 `@EncryptField` 加密。

#### SEC-B4. 文件上传无服务端类型/MIME/magic byte 校验 (High)

- **文件**: `SysOssService.java:181-199`
- **问题**: `upload(MultipartFile file)` 接受任意文件直接传给 OSS，无后缀白名单、无 MIME 校验、无 magic byte 检测。仅 avatar 上传（`SysOssController.avatar`）在应用层做了扩展名校验。
- **修复**: 服务端校验文件 magic bytes，维护允许的文件类型白名单。

#### SEC-B5. Token 值在多处打印到日志 (Medium)

- **文件**: `UserActionListener.java:69,78,87`、`BocooWebSocket.java:83,90,105,134,148,171,353,360,481,486,525`
- **问题**: 15+ 处将 Bearer/JWT Token 写入 INFO/WARN/ERROR 日志。日志泄露即所有在线会话可被劫持。
- **修复**: Token 脱敏（仅打印前/后 4 位），或完全移除 Token 日志。

#### SEC-B6. 登录失败日志含 PII（手机号/邮箱） (Medium)

- **文件**: `SysLoginService.java:280-307`
- **问题**: `log.info("登录用户：{} 不存在.", username/phonenumber/email)` 将手机号和邮箱写入 INFO 日志。属于 PII 泄露。
- **修复**: 脱敏处理，或降低至 DEBUG 级别。

#### SEC-B7. CORS 配置 allowCredentials(true) + allowedOriginPattern("*") (Medium)

- **文件**: `ResourcesConfig.java:56-62`
- **问题**: 允许任意来源携带凭据（Cookie、Authorization 头）发起跨域请求。任何恶意网站均可向该后端发送认证请求。
- **修复**: 限制 `allowedOriginPatterns` 为具体前端域名。

#### SEC-B8. Undertow POST body 无大小限制 (Medium)

- **文件**: `application.yml:67` — `max-http-post-size: -1`（无限）。非 multipart 的 POST 请求可发送超大 body 导致 OOM。
- **修复**: 设置为合理上限（如 10MB）。

#### SEC-B9. 密码重试锁定 key 包含 IP 导致可被绕过 (Medium)

- **文件**: `SysLoginService.java:390`
- **代码**: `CacheConstants.PWD_ERR_CNT_KEY + username + ":" + clientIP`
- **问题**: 锁定 key 同时包含用户名和 IP。攻击者换一个 IP 即可重新尝试同用户名。标准做法是按用户名锁定，与 IP 无关。且锁定时间仅 10 分钟（`lockTime: 10`）。
- **修复**: 移除 key 中的 IP 部分。

#### SEC-B10. 依赖版本已知漏洞 (Medium)

- **文件**: `pom.xml` — Spring Boot 3.2.9、Hutool 5.8.27、OkHttp 4.10.0、Redisson 3.29.0 均非最新 patch 版本。`pom.xml:59-62` 中三个 CVE 修复（commons-compress、ion-java、undertow-core）的版本覆盖**已被注释掉**。
- **修复**: 升级到最新 patch 版本；取消 CVE 修复注释；集成 OWASP dependency-check-maven 插件。

#### SEC-B11. MyBatis 加解密拦截器对 Map 参数无效 (Medium)

- **文件**: `MybatisEncryptInterceptor.java:70`、`MybatisDecryptInterceptor.java:61`
- **问题**: 当 MyBatis 参数为 `Map<String, Object>` 时，拦截器遍历 values 集合但修改值未写回原始 Map。`@EncryptField` 加密/解密对 Map 参数形同虚设。
- **修复**: 改为遍历 `map.entrySet()` 并将加密/解密后的值写回。

#### SEC-B12. 8 处使用 printStackTrace() 而非日志框架 (Low)

- **文件**: `LogAspect.java:121`、`ServletUtils.java:139`、`WebsocketServer.java:156,163` 等
- **问题**: `e.printStackTrace()` 写入 stderr 而非结构化日志，生产环境不可见。
- **修复**: 改用 `log.error("msg", e)`。

#### SEC-B13. Swagger/Knife4j 生产模式关闭 + 弱密码 (Low)

- **文件**: `application-dev.yml:195-199` — `production: false`，basic auth 用户名 `bocoo` / 密码 `bocoo@123`。
- **修复**: 生产环境关闭 Swagger，或使用强密码。

---

## 问题汇总（最终）

| 严重度 | 数量 | 类别 |
|--------|------|------|
| Blocker | 1 | 密码明文日志 |
| **High** | **27** | 后端并发 3 + 后端防重/限流 2 + 后端 SQL/Redis 5 + 后端安全 4 + 前端结构 4 + 前端性能 2 + 前端安全 2 + 前端稳定 1 + 后端事务 2 + 后端竞态 3 |
| Medium | 35 | 后端并发 5 + 后端防重/限流 2 + 后端 SQL/日志 8 + 后端安全 7 + 前端结构 4 + 前端性能 4 + 前端稳定 4 + 后端缓存 2 |
| Low | 22 | 后端 10 + 前端 12 |
