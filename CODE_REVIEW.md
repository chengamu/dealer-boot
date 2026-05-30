# Code Review — Git Diff 业务安全审查

> **审查范围**: 当前分支（未提交）全部变更，仅关注 diff 中引入的风险
> **审查日期**: 2026-05-30
> **原则**: 只输出真实风险，不吹毛求疵

---

## 一、变更概要

84 个文件，核心主题：**安全加固 + 首次登录强制改密**。

- 所有登录/验证码/上传/注册接口新增 `@RateLimiter`
- 文件上传增加扩展名白名单 + MIME 校验 + 文件头魔数校验
- 邮件验证码 4位→6位，新增每日上限(5次) + 错误锁定(5次错锁10分钟) + 防邮箱枚举
- 登录防暴力破解 key 从 `username:IP` 改为 `loginType:username`
- 防重复提交 fallback 修复（无 token 时用 IP）+ 新增多个 `@RepeatSubmit`
- `forcePasswordChange` 功能（商户审批后首次登录强制跳转改密页）
- CORS 从 `*` 改为配置化白名单
- JWT 密钥启动校验（≥32字符，非dev不能用默认值）
- WebSocket/日志 token 脱敏，密码明文日志移除
- 日志 clean 改为分批删除（1000条/批）、在线用户扫描限制 1000 条
- OSS listByIds N+1 优化、encrypt Map 写回修复
- 依赖升级：Spring Boot 3.2.9→3.2.12、Redisson 3.29→3.52、Hutool 5.8.27→5.8.46 等

---

## 二、真实风险

### 风险 1 (中): 登录锁定 Key 去除 IP 维度 — 账号可被全局 DoS

**文件**: `SysLoginService.java:397` — `checkLogin()`

```java
// 旧：仅锁定攻击者所在 IP
String errorKey = PWD_ERR_CNT_KEY + username + ":" + clientIP;
// 新：任何 IP 的错误都计入同一计数器
String errorKey = PWD_ERR_CNT_KEY + loginType.name().toLowerCase() + ":" + username;
```

攻击者用任意用户名 + 错误密码调用 5 次，**该账号在所有 IP 上均被锁定**。虽然防暴力破解效果更好，但也引入了 DoS 向量 — 恶意用户可故意输错密码来锁死目标账号。

**建议**: 保留 IP 维度作为主 key，额外增加全局计数器作为第二层（如 10 分钟内超过 20 次全局错误才触发全局限锁）。

---

### 风险 2 (中): `sendEmailCode` 静默消耗已注册邮箱的每日配额

**文件**: `SysTenantApplyService.java:82` — `sendEmailCode()`

```java
// 检查 order：冷却 → 每日计数(对所有邮箱) → 可用性检查
if (!RedisUtils.setObjectIfAbsent(cooldownKey, ...)) { throw; }
enforceDailyEmailCodeLimit(normalizedEmail);  // 已注册的也计费
if (!isEmailAvailableForPublicApply(normalizedEmail)) {
    return;  // 静默返回成功
}
```

已注册邮箱虽然收不到邮件，但每次冷却（60s）过后仍消耗每日配额（5次）。攻击者配合换 IP（绕过控制器 `@RateLimiter`）可耗尽目标邮箱当日配额。

**建议**: 将 `enforceDailyEmailCodeLimit` 移到 `isEmailAvailableForPublicApply` 之后，仅对真正会发送邮件的请求计数。

---

### 风险 3 (低): `loadUserByUsernameOrEmail` 冲突时排序不确定

**文件**: `SysLoginService.java:272`

```java
List<SysUserVo> users = userMapper.selectVoList(
    new LambdaQueryWrapper<SysUser>()
        .and(w -> w.eq(SysUser::getUserName, username).or().eq(SysUser::getEmail, username)));
```

当用户 A 的 username = `x@a.com`，用户 B 的 email = `x@a.com`，查询返回两条。代码优先找 username 匹配，找不到则取 `users.get(0)` — **无 ORDER BY，顺序由数据库决定，不可预测**。

**建议**: 加 `ORDER BY` 或在无精确匹配时记录 WARN 日志。

---

### 风险 4 (低): CORS 配置无默认值保护

**文件**: `ResourcesConfig.java:63` + `application.yml:135`

```java
config.setAllowCredentials(corsProperties.getAllowCredentials());
addAllowedOrigins(config, corsProperties.getAllowedOriginPatterns(), true);
```

如果某个 profile 缺少 `web.cors.*` 配置，`getAllowedOriginPatterns()` 返回空列表，**所有跨域请求都会失败**。

**建议**: 在 `CorsProperties` 中设置合理默认值，或为空时 fallback 到 localhost 列表。

---

### 风险 5 (低): 字典接口限流可能过紧

**文件**: `SysDictDataController.java:94`

```java
@SaIgnore
@RateLimiter(count = 60, time = 60, limitType = LimitType.IP)
@GetMapping(value = "/type/{dictType}")
```

前端每个页面加载多个字典，共享 IP / VPN 环境下 60次/分钟很容易触发。限流命中后字典下拉无数据，前端 catch 块无用户提示。

**建议**: 提高到 120–200次/分钟 或改为 `LimitType.CLUSTER`。

---

## 三、不构成风险的改进（确认正确）

| 改动 | 评价 |
|------|------|
| 验证码 4→6 位 | 安全提升，暴力破解难度 10000→1000000 |
| 邮箱验证码每日上限 + 错误锁定 | 显著提升防护 |
| 文件上传白名单 + MIME + 魔数校验 | 防御文件上传漏洞的关键措施 |
| JWT 密钥启动校验 | 防止生产环境使用弱密钥 |
| WebSocket/日志 token 脱敏 | 防止 token 泄漏到日志 |
| 密码明文日志移除 | 修复了之前审查指出的严重问题 |
| `forcePasswordChange` 流程 | 首次登录强制改密，设计合理 |
| 日志分批删除 + 在线用户扫描限制 | 防 OOM/长锁 |
| OSS listByIds 批量查询 | N+1 优化 |
| Encrypt Map 写回修复 | 修了 Map 参数加密无效的 bug |
| SMS/Email code key 分离 | 修了验证码 key 冲突的 bug |
| CORS 从 `*` 改为白名单 | 安全提升 |
| `@RepeatSubmit` 覆盖上传/注册/审批 | 安全提升 |
| `runAfterCommit` 发送邮件 | 修了事务内发邮件持锁的问题 |
| `@Lock4j` 审批/OSS 状态切换 | 修了并发竞态条件 |
| 前端 `http-request` 替代 `headers` | token 不再暴露在 DOM |
| 前端 i18n 加载加 fallback | 修了语言包加载失败白屏问题 |
| 前端 `submitLoading` 防双击 | 修了重复提交风险 |
| 前端 `requestPage<T>` 类型安全 | 修了 `as unknown as` 不安全断言 |
| 依赖版本升级 | 修了多个 CVE |

---

## 四、总结

本次 diff 整体质量很高，大量安全加固直接覆盖了之前审查报告（`CODE_REVIEW_BUSINESS.md`）中列出的大部分问题。5 个风险全部为中/低级别，**无阻塞性问题**，建议修复风险 1、2 后合入。

---

## 五、代码质量 / 复用 / 效率审查（simplify 技能）

### 已修复的问题

| # | 类别 | 文件 | 问题 | 修复 |
|---|------|------|------|------|
| 1 | 效率-HIGH | `SysLoginController.java:178` | `buildLoginResponse()` 每次登录做一次冗余的 `selectUserById` DB 查询，但 `LoginUser` 对象已携带 `forcePasswordChange` 字段 | 改为直接读 `loginUser.getForcePasswordChange()`，省去 DB 往返 |
| 2 | 效率-HIGH | `admin-ui/src/i18n/index.ts:86` | `setupI18n()` 将 fallback 和当前语言包改为串行加载（旧代码用 `Promise.all` 并行），启动延长约一倍 | 恢复 `Promise.all` 并行加载，保留新的 fallback 安全逻辑 |
| 3 | 复用-MED | `SysUserOnlineController.java` + `SysRoleService.java` | `ONLINE_TOKEN_SCAN_LIMIT = 1000` 在两个类中重复定义 | 移至 `CacheConstants.ONLINE_TOKEN_SCAN_LIMIT`，两处引用统一 |
| 4 | 质量-MED | `admin-ui/src/stores/user.ts` | `forcePasswordChange` 同时存在于 `state`（boolean）和 `user` 对象（raw value），需手动同步 | 改为 getter，从 `user.forcePasswordChange` 派生，删除 `state` 中的冗余字段和手动同步代码 |

### 未修复（建议后续处理）

| # | 类别 | 文件 | 问题 | 建议 |
|---|------|------|------|------|
| 5 | 复用 | 4 个 Vue 组件 | `Editor/index.vue`、`FileUpload/index.vue`、`ImageUpload/index.vue`、`UserPage.vue` 各自实现相同的 FormData 上传逻辑 | 抽取 `@/utils/upload.ts` 共享 `uploadFile(options, url)` |
| 6 | 复用 | `SysLogininforService.java` + `SysOperLogService.java` | `cleanLogininfor()` 和 `cleanOperLog()` 批量删除逻辑完全相同（仅 Mapper/ID列不同） | 抽取泛型 `batchDeleteByLimit` 工具方法 |
| 7 | 质量 | `SysLoginService.java` 等多处 | `forcePasswordChange` 用字面量 `"1"` / `"0"` 比较，缺少常量 | 定义 `FORCE_YES` / `FORCE_NO` 常量 |
| 8 | 效率-MED | `XssFilter.java` | 移除 GET/DELETE 跳过逻辑后，所有读请求都经过 XSS wrapper（GET 参数本来不应该有 XSS — 这是输出层的问题） | 评估是否恢复 GET/DELETE 的 method 级别跳过（保留 query param sanitization） |

### 正向确认

- `buildLoginResponse` 重构为独立方法减少 5 个登录端点的重复，方向正确
- `requestPage<T>` 替换 `as unknown as` 类型断言，消除 10+ 处不安全转换
- 4 个上传组件从 `:headers` 改为 `:http-request`，消除了 token 在 DOM 中的暴露
- 所有新增的 `@RateLimiter`、`@RepeatSubmit`、文件魔数校验等安全措施都是合理的
