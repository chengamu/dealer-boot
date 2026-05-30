# Requirement: Google Login Hardening Follow-up

## Goal：目标

基于更新后的 `D:\work\base-boot\CODE_REVIEW_HEALTHBRAIN_DRUG.md`，对已完成的 Google 登录 / BaseBo 批次做一次后续加固计划：优先处理低风险、可验证、不会扩大业务范围的维护性问题。

## Report Triage：报告核验

- High：报告为 0，无紧急漏洞。
- M1 `SysLoginService` 职责膨胀：真实存在，Google JWT 解析、公钥拉取、签名校验和登录编排混在同一个类中；建议本批提取 `GoogleAuthService`。
- M2 `SysLoginService` 直调 `SysTenantApplyMapper`：真实存在，但当前只在 Google token 已验证后查询本人邮箱申请状态；可接受。本批不强行迁移到 `SysTenantApplyService`，避免把申请服务公共 API 扩大。
- M3 i18n 六份文件重复：项目既定模型是 `i18n/locales` 为源、前后端运行时资源由 `pnpm i18n:sync` 同步；不作为漏洞修复。
- 报告总结提到 `Mapper XML 包位移 / M4`，但当前代码树未找到 `HealthbrainDrugMapper` Java 接口或 XML 文件；用户已确认 `HealthbrainDrug Mapper XML` 已移除，本批不处理。
- L1 静态 `HttpClient` 非 Spring 管理：当前可接受；如公司网络需要代理再改为 Spring `RestClient` 或配置化 HTTP client。
- L2 Google certs URL 硬编码：真实存在，适合随 `GoogleAuthService` 提取一起改为 `auth.google.certs-url` 配置项。
- L3 `GoogleLoginBody` 缺少 `@NoArgsConstructor`：低风险，可直接补。
- L4 `LoginPage.vue` Google 类型声明内联：真实存在，可迁到 `admin-ui/src/types/google.d.ts`。
- L5 `SysNoticeBo`、`SysOssBo`、`SysUserBo` 显式审计字段：当前分别用于查询过滤或注册写入，不能简单删除；本批保留。

## Scope：范围

- 提取 `GoogleAuthService`，只迁移 Google ID token 校验、公钥缓存、Google certs 拉取和 claim 校验逻辑。
- `SysLoginService` 保留登录编排：匹配用户、构建 `LoginUser`、Sa-Token 登录、登录日志和申请状态分流。
- 增加 `auth.google.certs-url` 配置默认值，默认仍为 Google 官方 certs URL。
- 给 `GoogleLoginBody` 增加 `@NoArgsConstructor`。
- 将登录页 Google 全局类型声明迁移到 `admin-ui/src/types/google.d.ts`，组件只保留业务逻辑。
- 更新 README 或任务记录中已核验的 Google 配置说明，如需要只做小补充。

## Out of Scope：不做范围

- 不修改数据库结构和 migration。
- 不新增 Google 账号绑定表。
- 不改变 Google 登录业务策略：仍只匹配系统内已有且启用的邮箱用户。
- 不新增第三方登录提供商。
- 不改 i18n 同步模型，不删除运行时 i18n JSON。
- 不删除 `SysNoticeBo`、`SysOssBo`、`SysUserBo` 中当前仍被服务层使用的审计字段。
- 不处理 `HealthbrainDrugMapper` Java/XML 包路径；对应 XML 已移除。
- 不升级依赖。

## Acceptance Criteria：验收标准

- `SysLoginService` 中不再直接包含 Google JWT 解析、公钥拉取、签名校验和 certs URL 常量。
- 新增 `GoogleAuthService` 后，Google 登录成功/申请状态分流行为保持不变。
- `auth.google.client-id`、`auth.google.allow-unhosted-email`、`auth.google.certs-url` 均有明确配置入口。
- `GoogleLoginBody` 显式具备无参构造能力。
- `LoginPage.vue` 不再内联 Google 全局类型声明，类型由 `admin-ui/src/types/google.d.ts` 提供。
- Java compile、前端 typecheck/build 至少执行 targeted check；未执行的检查必须标记 `Not Run`。

## Risks：风险

- `GoogleAuthService` 提取如果边界过大，可能误改登录编排；实现时只移动 Google token 校验相关方法。
- Google certs URL 配置化不能改变默认值，否则会影响本地和生产登录。
- 前端 `.d.ts` 类型迁移必须被 TypeScript 自动包含，避免 `Window.google` 类型丢失。

## Recommendation：推荐方案

执行一个轻量加固批次：`GoogleAuthService` 提取 + 小型类型/DTO 整理 + 配置化 certs URL。M2/M3/M4/L1/L5 只记录，不在本轮动代码。
