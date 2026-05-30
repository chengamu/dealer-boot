# Requirement: CODE_REVIEW 风险收敛与登录页 Curtain Reveal

## Goal：目标

基于 2026-05-30 更新后的 `CODE_REVIEW.md` 和桌面 `需求.txt`，修复当前 diff 中仍存在的真实中低风险，并在登录页新增轻量 CSS Curtain Reveal 开场动画。

## Background：背景

上一轮安全与业务修复已经归档，但新的 `CODE_REVIEW.md` 针对当前未提交 diff 又识别出 5 个真实风险和若干代码质量建议。用户同时提出登录页需要一个不阻塞加载、最多约 1.5 秒、可跳过且本会话只播放一次的开场动画。

## Scope：范围

- 修复登录错误锁定从账号级全局锁定引入的 DoS 风险。
- 修复商户申请邮箱验证码对已注册邮箱静默消耗每日配额的问题。
- 让 `loadUserByUsernameOrEmail` 在 username/email 冲突时具备确定性。
- 为 CORS 配置提供合理默认保护，避免 profile 缺少 `web.cors.*` 时全部跨域失效。
- 调整公开字典接口限流，降低共享 IP / VPN 场景误伤。
- 新增登录页 CSS-only Curtain Reveal 动画组件，并接入 `LoginPage.vue`。
- 做小范围低风险清理：`forcePasswordChange` 字面量常量化、上传 FormData 复用评估后再做。

## Out of Scope：不做范围

- 不修改数据库结构，不新增 migration，不调整 PostgreSQL schema。
- 不升级依赖。
- 不切换 Bearer Token 到 Cookie/CSRF。
- 不做 Quill 2.x 迁移。
- 不为没有正式域名的生产环境硬编码新域名。
- 不默认重构日志清理泛型工具和 `XssFilter` GET/DELETE 策略；这两项只做评估或后续任务。

## User Stories / Use Cases：用户故事 / 使用场景

- 管理员账号不会因为攻击者在任意 IP 输错 5 次密码就被全局锁死。
- 商户申请时，已注册邮箱请求验证码不会消耗当日发送额度。
- 登录名和邮箱出现同值冲突时，系统选择用户结果稳定可解释。
- 本地开发和缺省 profile 下，CORS 至少保留 localhost/127.0.0.1 的可用默认值。
- 登录页首次进入时看到轻量 Curtain Reveal 效果，但不会感觉系统变慢，点击可跳过，本会话不重复播放。

## Business Rules：业务规则

- 登录错误锁定采用“IP 维度主锁定 + 全局错误次数兜底”的双层策略。
- 全局错误阈值必须高于单 IP 阈值，避免轻易触发账号级 DoS。
- 公开字典仍只允许 `sys_country` 匿名访问，其他字典继续要求登录。
- CORS 默认值只覆盖本地开发来源，正式域名后续仍通过 YAML 配置。

## UX / UI Requirements：交互 / 界面要求

- Curtain Reveal 只使用 CSS + `div`，不加载图片、视频、GIF、Lottie 或复杂 SVG 滤镜。
- 动画使用 `transform` / `opacity`，总时长控制在 1.2 到 1.5 秒。
- 动画不阻塞登录页渲染，登录页先正常挂载。
- 用户点击遮罩可跳过。
- 动画结束后移除 DOM。
- 使用 `sessionStorage` 记录本会话已看过。
- `prefers-reduced-motion: reduce` 下跳过动画。

## API Requirements：接口要求

- 不新增业务 API。
- 登录错误锁定修复只调整现有登录校验和解锁相关 Redis key 策略。

## Data Requirements：数据要求

- 不修改数据库结构。
- Redis key 命名需要兼容监控缓存清理和账户解锁。

## Permission / Tenant Rules：权限 / 租户规则

- 不改变现有 `Sa-Token` 登录权限边界。
- 不放开字典管理接口权限。
- 字典匿名访问范围仍保持白名单。

## i18n / UTC Rules：国际化 / 时间规则

- Curtain Reveal 不新增可见说明文案，避免新增 i18n key。
- 后端如新增用户可见错误消息必须走现有 i18n；本计划优先复用现有登录锁定消息。
- 不涉及 UTC / 时区调整。

## Options：可选方案

### Option A：只修 `CODE_REVIEW.md` 5 个真实风险 + Curtain Reveal
Pros：改动集中，风险最低，能覆盖当前报告要求和前端需求。
Cons：代码质量建议中的重复逻辑暂不处理。

### Option B：同时处理全部“未修复建议”
Pros：报告尾部建议一次性清掉更多。
Cons：会引入跨组件上传抽象、后端泛型删除工具、XSS 策略变化，修改面扩大，验证成本更高。

## Recommended Option：推荐方案

采用 Option A，并额外纳入 `forcePasswordChange` 字面量常量化这种局部低风险清理。上传复用可以进入同一批次低优先级任务，但若实现时发现组件差异较大，应停止并记录后续任务。

## Risks：风险

- 登录锁定双层 key 改动必须同步解锁逻辑，否则管理员手动解锁可能失效。
- CORS 默认值不能重新引入 `* + credentials` 风险。
- Curtain Reveal 需要在真实浏览器验证遮罩能移除、不会挡住登录表单、不会造成移动端文本或布局异常。
- 字典限流提高会降低一点点防刷强度，但接口仍有登录检查和公开字典白名单。

## Open Questions：待确认问题

- 全局登录失败阈值默认建议为 20 次 / lockTime 窗口；如用户后续有明确策略，可改为 YAML 配置值。
- 上传 FormData 复用是否本批必须完成；当前建议作为低优先级，执行时看差异决定。

## Acceptance Criteria：验收标准

- `CODE_REVIEW.md` 风险 1 到 5 均被修复或有明确记录。
- 登录错误锁定支持 IP 主维度和全局兜底，账户解锁能清理相关 key。
- 已注册邮箱请求商户申请验证码不消耗每日配额。
- `loadUserByUsernameOrEmail` 查询顺序确定。
- CORS 缺省配置不会导致本地开发跨域全部失败，且不允许 `* + credentials`。
- 字典接口限流调整后仍保持非公开字典必须登录。
- 登录页 Curtain Reveal 首次会话播放一次，可点击跳过，动画结束后 DOM 移除。
- 后端编译、前端 typecheck/build、i18n 校验和登录页浏览器验证按 `/check` 执行并记录真实结果。

## Related Decisions：相关决策

- PostgreSQL 是主数据库，但本需求不涉及 DB schema。
- 当前没有正式域名，CORS 生产来源仍暂不硬编码。
- Bearer Token 架构继续保留，Cookie/CSRF 等待正式域名和部署拓扑。
