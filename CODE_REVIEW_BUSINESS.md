# 代码审查报告 — 业务逻辑安全性与合理性

> **项目**: bocoo-base (MES 后台管理系统 / 跨境经销商管理平台)
> **审查日期**: 2026-05-29
> **审查范围**: 认证授权、多租户隔离、RBAC权限、业务安全、架构设计

---

## 一、总体评价

项目整体架构设计合理，基于 RuoYi-Vue 框架进行了深度定制。多模块 Maven 结构清晰，前后端分离，基础设施层（common 模块）功能完善。多租户方案采用 Schema 级别隔离 + MyBatis-Plus 拦截器，RBAC 权限模型实现完整。以下按严重程度列出发现的问题。

---

## 二、高风险问题

### 2.1 TenantContextHolder ThreadLocal 泄漏风险

**位置**: `bocoo-common-core/.../TenantContextHolder.java`

**问题**: 使用 `ThreadLocal` 存储租户 ID，但未在请求结束后显式清理。Undertow 使用 Worker 线程池，线程复用场景下上一个请求的租户 ID 可能泄漏到下一个请求。

**影响**: 多租户数据隔离失效，租户 A 的请求可能看到租户 B 的数据。

**建议**: 在 `TenantFilter`（负责设置租户上下文）的 `finally` 块中必须调用 `TenantContextHolder.remove()` 清理。建议审查当前 Filter 链是否保证清理。

```java
// 应在 TenantFilter.doFilter 的 finally 块中:
TenantContextHolder.removeTenantId();
```

### 2.2 登录接口缺少限流保护

**位置**: `SysLoginService.java:83` — `login()` 方法

**问题**: 虽然有密码错误次数限制（`checkLogin` 基于 Redis 计数），但登录接口本身没有 `@RateLimiter` 注解。攻击者可以进行暴力枚举用户名（每次尝试不同用户名来绕过单用户错误计数）。

**建议**: 在登录 Controller 方法上添加 IP 级别的限流：

```java
@RateLimiter(count = 10, time = 60, limitType = LimitType.IP)
@PostMapping("/login")
public R<LoginResult> login(@RequestBody LoginBody body) { ... }
```

### 2.3 RepeatSubmitAspect 防重复提交存在绕过风险

**位置**: `RepeatSubmitAspect.java:58`

**问题**:

```java
String submitKey = StringUtils.trimToEmpty(request.getHeader(SaManager.getConfig().getTokenName()));
```

当请求头中没有 token 时，`submitKey` 为空字符串，防重 Key 退化为 `url + params`。这意味着：
1. 两个不同用户提交相同参数会被误拦
2. 攻击者可构造无 token 请求绕过防重机制

**建议**: token 为空时应使用 IP + Session 作为 fallback：

```java
if (StringUtils.isBlank(submitKey)) {
    submitKey = ServletUtils.getClientIP(request);
}
```

### 2.4 XssFilter 对 GET 请求完全跳过

**位置**: `XssFilter.java:49-50`

**问题**:

```java
if (method == null || HttpMethod.GET.matches(method) || HttpMethod.DELETE.matches(method)) {
    return true; // 完全跳过过滤
}
```

反射型 XSS 最常见的就是通过 GET 参数的 `?q=<script>...` 方式。当前实现直接放行所有 GET 请求。

**建议**: 至少对 GET 请求的 query string 参数做 HTML 编码输出。不要完全跳过。

---

## 三、中等风险问题

### 3.1 商户申请邮件接口缺少每日发送上限

**位置**: `SysTenantApplyService.java:82` — `sendEmailCode()`

**问题**: 虽然有冷却时间（`EMAIL_CODE_COOLDOWN`），但没有每日单邮箱发送次数限制。恶意用户可以在每次冷却结束后持续发送。

**建议**: 增加每日计数限制（如每天最多 5 次）：

```java
String dailyKey = EMAIL_CODE_DAILY_PREFIX + normalizedEmail;
long dailyCount = RedisUtils.incrAtomicValue(dailyKey);
if (dailyCount == 1) {
    RedisUtils.expire(dailyKey, Duration.ofDays(1));
}
if (dailyCount > 5) {
    throw ServiceException.ofMessageKey("tenant.apply.emailCode.dailyLimit");
}
```

### 3.2 商户审批创建用户使用明文临时密码

**位置**: `SysTenantApplyService.java:163` — `approve()`

**问题**:

```java
String tempPassword = RandomUtil.randomString(PASSWORD_CHARS, 12);
// ...
user.setPassword(BCrypt.hashpw(tempPassword));
```

临时密码通过邮件发送给商户。问题是：用户首次登录后**未强制要求修改密码**。密码可能长期保持为初始值。

**建议**: 增加 `forcePasswordChange` 标志位，首次登录时要求修改密码。

### 3.3 邮件验证码 4 位纯数字易被暴力破解

**位置**: `SysTenantApplyService.java:94`

**问题**:

```java
String code = RandomUtil.randomNumbers(4);
```

4 位纯数字验证码有 10000 种组合，配合冷却时间有一定防护，但建议增强。

**建议**: 增加到 6 位数字，并增加验证次数限制（如 3 次错误后作废）。

### 3.4 邮箱枚举风险

**位置**: `SysTenantApplyService.java:114-116` — `submit()` 方法

**问题**: `checkApplyEmailAvailable` 和 `checkUserEmailAvailable` 返回不同错误消息，攻击者可通过返回消息区分：
- 邮箱是否已申请
- 邮箱是否已注册为用户

**建议**: 统一错误消息为 "申请提交失败，请检查信息后重试"，审计日志中记录具体原因。

### 3.5 并发登录 Token 无上限

**位置**: `application.yml:148-150`

```yaml
is-concurrent: true   # 允许并发登录
is-share: false       # 每次登录新建 token
```

每次登录都创建新 token，旧 token 仍然有效。没有 token 数目上限或旧 token 自动失效机制，长期累积可能导致 token 泄漏风险增大。

**建议**: 设置单用户最大并发 token 数（如 5 个），超出时自动淘汰最老的 token。

### 3.6 验证码消耗时机问题

**位置**: `SysLoginService.java:253-254`

```java
String captcha = RedisUtils.getCacheObject(verifyKey);
RedisUtils.deleteObject(verifyKey); // 立即删除
if (captcha == null) { throw ... }
```

验证码被读取后立即删除，即使用户输入错误。这是正确的安全实践（防重放），但用户体验差 —— 输错就要换新验证码。

**建议**: 当前实现安全方面是正确的，维持现状。可在前端做输入校验减少无效提交。

---

## 四、架构与设计优化建议

### 4.1 缺少 TenantContext 清理 Filter

**位置**: 需确认 TenantFilter 实现

**问题**: `TenantContextHolder` 的 `ThreadLocal` 必须在请求结束后清理。目前未找到明确的清理逻辑。

**建议**: 确保 TenantFilter 的 `doFilter` 方法有如下结构：

```java
try {
    TenantContextHolder.setTenantId(tenantId);
    chain.doFilter(request, response);
} finally {
    TenantContextHolder.remove(); // 必须清理
}
```

### 4.2 缺少 Token 刷新机制

**位置**: JWT 配置

**问题**: 当前 token 固定有效期 86400 秒（24 小时），没有 refresh token 机制。用户使用中突然掉线体验差。

**建议**: 
- 短有效期 access token (30 min) + 长有效期 refresh token (7 days)
- 或使用 Sa-Token 的 `active-timeout` 自动续期（当前已配置）

当前配置了 `active-timeout: 86400` 和 `dynamic-active-timeout: true`，用户在活跃期间 token 会自动续期，此问题影响较小。

### 4.3 前端数据加载缺少全局 Loading 和错误重试

**位置**: 前端各 Page 组件

**问题**: 各页面的 `getList()` 方法模式相似，但错误处理只有空的 catch 块：

```typescript
} catch {
    // Request interceptor already displays the backend error.
}
```

**建议**: 
1. 若拦截器确实已显示错误，应确保所有错误码都被覆盖
2. 对于网络超时等场景，考虑增加"重试"按钮

### 4.4 数据权限超级管理员硬编码绕过

**位置**: `PlusDataPermissionHandler.java:98`

```java
if (LoginHelper.isAdmin()) {
    return where; // 直接跳过所有数据权限过滤
}
```

**问题**: 超级管理员可看到所有租户的数据，这在运维层面合理，但缺少审计日志。

**建议**: 当管理员绕过数据权限时，增加操作日志记录（此处为查询操作，高频记录可能影响性能，可采样记录）。

### 4.5 SMS 与 Email 验证码共用 Key 前缀

**位置**: `SysLoginService.java:224` 和 `:237`

```java
// SMS:
String code = RedisUtils.getCacheObject(CacheConstants.CAPTCHA_CODE_KEY + phonenumber);
// Email:
String code = RedisUtils.getCacheObject(CacheConstants.CAPTCHA_CODE_KEY + email);
```

**问题**: 共用 `CAPTCHA_CODE_KEY` 前缀。如果手机号恰好是纯数字且被误当作邮箱，可能造成 key 冲突。

**建议**: 使用不同的前缀区分 SMS 和 Email：

```java
CacheConstants.SMS_CODE_KEY + phonenumber
CacheConstants.EMAIL_CODE_KEY + email
```

### 4.6 商户审批未使用分布式锁

**位置**: `SysTenantApplyService.java:143` — `approve()`

**问题**: 虽然有 `@Transactional`，但没有分布式锁。两个管理员同时审批同一条申请可能产生竞态条件（虽然数据库事务会保护大部分操作，但邮件发送等外部操作不在事务内）。

**建议**: 添加分布式锁或乐观锁：

```java
@Lock4j(keys = {"#applyId"})
@Transactional(rollbackFor = Exception.class)
public void approve(Long applyId) { ... }
```

### 4.7 密码修改未验证旧密码

**位置**: 需确认 `SysProfileController` 中的密码修改逻辑

**问题**: 应确认修改密码时是否校验了旧密码，避免通过 XSS/CSRF 修改密码。建议在服务层强制要求 `oldPassword` 参数。

### 4.8 文件上传类型校验

**位置**: 需确认 OSS 上传逻辑

**问题**: 应确认文件上传是否有文件类型白名单校验（不只是前端校验），防止上传 webshell。

---

## 五、业务流程审查

### 5.1 商户入驻流程

```
商户提交申请 → 邮件验证码验证 → 平台审批 → 创建租户 → 创建管理员用户 → 分配角色
```

**审查意见**: 流程完整。建议增加：
- 审批通过后发送"账户激活"邮件（含临时密码和登录指引）
- 首次登录强制修改密码
- 商户自助查询申请进度功能

### 5.2 登录流程

```
多方式登录（密码/SMS/邮件/微信/第三方）→ 验证码校验 → 密码校验 → 
构建 LoginUser → 生成 JWT → 记录登录日志
```

**审查意见**: 多登录方式设计合理。建议：
- 增加登录二次验证（敏感操作时）
- 异地登录检测与告警

### 5.3 RBAC 权限流程

```
用户 → 角色 → 菜单权限 + 数据权限范围
```

**审查意见**: 标准 RBAC 实现正确。数据权限支持五种范围（全部/自定义/本部门/本部门及以下/仅本人），满足常见需求。

---

## 六、快速修复优先级

| 优先级 | 问题 | 修复成本 |
|--------|------|----------|
| P0 | ThreadLocal 泄露 | 低 — 确认 Filter finally 块 |
| P0 | 登录接口加限流 | 低 — 加注解 |
| P1 | 防重提交 fallback key | 低 — 改一行 |
| P1 | 邮件验证码增加每日上限 | 中 — 加 Redis 计数器 |
| P2 | XSS 过滤 GET 请求 | 中 — 需要改动过滤逻辑 |
| P2 | 审批分布式锁 | 低 — 加注解 |
| P2 | SMS/Email key 前缀分离 | 低 — 改常量 |
| P3 | 首次登录强制改密码 | 中 — 需要增加字段和流程 |
| P3 | Token 数目上限 | 中 — 需要改动 Sa-Token 配置 |

---

## 七、亮点

1. **多租户 Schema 级别隔离** — 使用 MyBatis-Plus TenantLineHandler 自动注入 tenant_id 条件，比应用层过滤更安全
2. **密码错误锁定机制** — 基于 Redis + IP + 用户名的复合 Key，可配置次数和锁定时长
3. **防重复提交** — 基于 Redis + token + 参数的幂等性保护，注解式使用方便
4. **数据权限过滤器** — 基于注解的声明式数据权限控制，支持 SpEL 表达式
5. **全局异常处理** — 分层处理 Sa-Token / MyBatis / Redis / 通用异常，返回一致的结构化响应
6. **操作日志** — 基于 Spring Event 异步记录，不阻塞主业务流程
7. **前端审计字段清理** — `sanitizeMutationData` 自动去除 createTime/updateTime 等字段，防止前端篡改

---

> **审查人**: Claude Code (AI)
> **下次审查建议**: 补充自动化测试覆盖率分析、依赖安全扫描结果
