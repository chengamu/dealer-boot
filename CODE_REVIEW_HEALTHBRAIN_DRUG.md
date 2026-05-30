# Code Review Report

**Date**: 2026-05-30
**Branch**: main
**Scope**: Google 登录全栈 / BaseBo 提取 / POM 编码修复 / i18n / HealthbrainDrug Mapper XML 位移
**Reviewer**: Claude Code (deepseek-v4-pro)

---

## 一、正面确认

1. **根 `pom.xml` 中文乱码已修复** — 9 处乱码注释恢复正确中文（`榛樿鐜` → `默认环境`，`绗笁鏂规湇鍔￠厤缃` → `第三方服务配置` 等）。
2. **`BaseBo` 提取完成** — `BaseEntity` 拆出 `BaseBo`（仅含 `searchValue` + `params`），不再携带 `@TableField` 和审计字段。15 个 Bo 类全部从 `extends BaseEntity` 迁移到 `extends BaseBo`，代码生成器模板 `bo.java.vm` 同步更新。
3. **`GoogleLoginBody`** — 独立请求 DTO，不继承任何基类，`@NotBlank` + i18n key，干净。
4. **Google 登录后端 JWT 校验链完整** — 三段分割 / RS256 签名验证 / audience + issuer + exp 校验 / Google 公钥双重检查锁缓存（6h TTL）/ email trust 白名单（Gmail + hosted domain + allow-unhosted-email 开关）。
5. **前端 Google 按钮使用 Google Identity Services 官方渲染** — 动态加载 GSI script、locale 联动重渲染、4 种申请状态分流。

---

## 二、High

无。

---

## 三、Medium

### M1. `SysLoginService` 职责膨胀

- **文件**: `bocoo-modules-system/.../service/SysLoginService.java`（现已 ~700 行）
- **问题**: 该类集中了：密码登录、短信登录、第三方登录、Google JWT 解析、Google 公钥拉取与缓存、商户申请状态查询。
- **风险**: 继续叠加第三方登录（GitHub / Apple / 微信）会失控。
- **建议**: 提取 `GoogleAuthService`，把 `verifyGoogleCredential` / `fetchGooglePublicKeys` / `getGooglePublicKey` 迁出。`SysLoginService` 只做"找到用户 → 构建 LoginUser → 登录"。

### M2. `SysLoginService` 直接注入 `SysTenantApplyMapper`

- **文件**: `SysLoginService.java:83`
- **问题**: `selectLatestApplyByEmail` 绕过了 Service 层直调 Mapper。虽然该类对 `userMapper` 也是同样模式，但后续如果申请查询需要加上业务校验，这里会被绕过。
- **建议**: 当前登录流程可接受。后续若申请查询逻辑变复杂（权限校验、脱敏等），再迁到 `SysTenantApplyService`。

### M3. i18n 六份文件仍然 triple-duplicated

- **现象**: 新增 7 个 `auth.google.*` / `login.google*` key 正确地同步到了 6 个 JSON 文件（`i18n/locales/`、`admin-ui/public/i18n/`、`bocoo-admin/src/main/resources/i18n/` 各一份 zh_CN + en_US）。
- **建议**: 不阻塞本次合并，但需建 issue 跟踪去重。



---

## 四、Low

### L1. `HttpClient` 静态字段非 Spring 管理

- **文件**: `SysLoginService.java:92-94`
- **问题**: `private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()...build()` 不经过 Spring 管理，无法使用 Spring 的 HTTP proxy 或 SSL 配置。
- **建议**: 当前可接受。若公司网络需要 HTTP 代理访问外网，改为注入 Spring 的 `RestClient`。

### L2. Google Certs URL 硬编码

- **文件**: `SysLoginService.java:85`
- **问题**: `https://www.googleapis.com/oauth2/v3/certs` 硬编码，内网隔离环境无法覆盖。
- **建议**: 提取为 `@Value("${auth.google.certs-url:https://www.googleapis.com/oauth2/v3/certs}")`。

### L3. `GoogleLoginBody` 缺少 `@NoArgsConstructor`

- **文件**: `GoogleLoginBody.java`
- **问题**: 只有 `@Data`。当前 Lombok 会自动生成无参构造器，但如果后续加 `@AllArgsConstructor` 或手动构造器，反序列化会炸。
- **建议**: 加 `@NoArgsConstructor`。

### L4. `LoginPage.vue` 中 Google 类型声明内联

- **文件**: `LoginPage.vue:139-143`
- **问题**: `GoogleCredentialResponse`、`GoogleAccountsId` 类型定义在组件 `.vue` 文件内。
- **建议**: 移到 `admin-ui/src/types/google.d.ts`，保持组件逻辑和类型声明分离。

### L5. `SysNoticeBo`、`SysOssBo`、`SysUserBo` 新增显式 `createBy` / `updateBy` 字段

- **文件**: `SysNoticeBo.java`, `SysOssBo.java`, `SysUserBo.java`
- **问题**: `BaseBo` 移除了审计字段后，这三个 Bo 手动声明了 `createBy` 和/或 `updateBy`。这些字段在新增场景下不需要客户端传入，在列表展示场景下可能需要返回。
- **建议**: 确认这些字段是否同时用于"请求入参"和"列表出参"。如是出参专用，应放在 Vo 而非 Bo 中。

---

## 五、总结

| 级别 | 数量 | 关键项 |
|---|---|---|
| High | 0 | - |
| Medium | 4 | SysLoginService 膨胀 / Mapper 直调 / i18n 重复 / Mapper XML 包位移 |
| Low | 5 | HttpClient 非 Spring 管理 / Google URL 硬编码 / 缺 @NoArgsConstructor / TS 类型内联 / Bo 显式声明审计字段 |

**结论**: **可以合并**。BaseBo 拆分和 pom 乱码修复是实质性改进。M1（SysLoginService 拆分）建议在下一个第三方登录接入前处理。M4（Mapper XML 包位移）需要确认有对应的 Java 接口，否则启动报错。
