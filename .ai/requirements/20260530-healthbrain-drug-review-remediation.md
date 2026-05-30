# Requirement: Healthbrain Drug Review Remediation

## Goal：目标

基于 `D:\work\base-boot\CODE_REVIEW_HEALTHBRAIN_DRUG.md` 和当前代码，拆解可执行修复计划：修复低风险编码问题，同时利用当前业务代码尚未沉淀的窗口期，优先治理 `BO extends BaseEntity` 这一会持续污染 API 契约和代码生成器模板的问题。

## Background：背景

报告覆盖根 `pom.xml` 编码乱码、健康大脑相关字典初始化数据乱码、i18n 运行时资源重复，以及 `BaseEntity` / `IBaseService` / `BaseServiceImpl` / `BO` / `VO` 的框架约束问题。

当前核验结果：

- `pom.xml` 内确有多处中文注释乱码，范围比报告列出的 L31/L89 更广；属于注释级问题，修复风险低。
- `sql/base.sql` 中 `brain_drug_phase`、`brain_instrument_level`、`brain_talent_type` 等数据存在乱码；但该文件是 MySQL 风格旧脚本，当前项目长期决策是 PostgreSQL 为主，`sql/postgresql/base.sql` 未发现这些健康大脑字典项。
- `i18n/locales`、`admin-ui/public/i18n`、`bocoo-admin/src/main/resources/i18n` 当前 hash 一致；项目已有 `pnpm i18n:sync`，三份文件重复是构建/运行时分发模型，不按漏洞处理。
- `HealthbrainDrugBo extends BaseEntity`、系统模块大量 `*Bo extends BaseEntity`、`IBaseService extends IService`、代码生成器模板继续生成 `Bo extends BaseEntity` 均与当前代码一致。因为当前项目真实业务尚少，这是治理模板和基础契约的合适窗口期。

## Scope：范围

- 修复根 `pom.xml` 中已确认的乱码中文注释，不改依赖版本和模块结构。
- 对健康大脑字典乱码只处理仍被当前部署链路使用的脚本；默认不修 MySQL 旧脚本，除非用户明确要求恢复该旧脚本。
- 补充或校准文档，明确 `i18n/locales` 是 source of truth，运行时资源由 `pnpm i18n:sync` 生成。
- 新增一个轻量请求/查询基类，让 `Bo` 不再继承 `BaseEntity`，避免请求 DTO 携带审计字段和 MyBatis 注解。
- 迁移当前系统模块、健康大脑参考模块中的 `*Bo extends BaseEntity` 到新的 BO 基类，保持 `getParams()` 查询能力。
- 更新代码生成器模板，后续新业务默认生成 `Bo extends BaseBo`，不再扩散 `Bo extends BaseEntity`。
- 暂不拆分 `IBaseService` / `ICommandService` / `IQueryService`，避免为了架构纯度增加后续新功能编码负担。
- `VO` 生成模板暂不强制改为 `BaseVo`，除非实施中确认不会增加复杂度。
- 实现登录页 Google 快速登录第一版：前端使用 Google Identity Services，后端校验 Google ID token；已存在且邮箱匹配、状态正常的用户直接登录，未匹配用户先查询商户申请状态再给出下一步提示。
- 修正 Google 登录按钮视觉，不再使用单色字母 `G` 伪 Logo；优先使用 Google Identity Services 官方渲染按钮，或使用官方规范的彩色 `G` 和按钮样式。

## Out of Scope：不做范围

- 不修改数据库结构。
- 不修改 PostgreSQL 初始化表结构或 migration。
- 不升级依赖。
- 不一次性移除现有 Entity 对 `BaseEntity` 的审计填充兼容。
- 不一次性删除 `BaseEntity` 中的 `params/searchValue`，先完成 BO 和模板迁移后再评估系统查询入参改造。
- 不拆分 Service 接口体系。
- 不做 Google 自动创建系统用户。
- 不新增 Google 账号绑定表；第一版先按已验证邮箱匹配现有用户。
- 不接入额外 Google API scopes，只做登录身份识别。
- 不恢复或维护 MySQL 初始化脚本，除非用户重新确认 MySQL 仍需要使用。
- 不处理报告明确排除且用户拟删除的健康大脑参考业务代码，除非用户确认这部分要保留上线。

## Options：可选方案

### Option A：低风险治理

Pros：能快速修复真实可见问题，避免大规模破坏现有系统。
Cons：基础框架设计问题只记录边界，不立即彻底重构。

### Option B：轻量 BO 契约治理

Pros：趁业务代码尚少时修正 BO 基类和生成模板，避免后续所有业务模块继续继承错误模式；新增文件很少，不改变日常功能文件数量。
Cons：只能解决 API 请求 DTO 污染和模板扩散问题，`IBaseService` 暴露过多仍作为后续 TODO。

## Recommended Option：推荐方案

推荐 Option B 的轻量版：只新增 `BaseBo` 这类请求/查询基类，迁移现有 `*Bo` 和代码生成模板；`BaseEntity`、`BaseServiceImpl`、`IBaseService` 先保持兼容，避免把架构治理做重。

Google 登录推荐使用 Google Identity Services 的 ID token 模式：前端拿到 `credential` 后提交给后端，后端按官方要求验证签名、`aud`、`iss`、`exp` 和邮箱可信度。第一版不自动创建系统用户、不写绑定表，避免租户和权限归属不明确。

Google 邮箱未匹配现有用户时，按 `sys_tenant_apply.email` 查询申请状态：

- `PENDING`：提示“申请已提交，等待平台审核”，不引导重复申请。
- `REJECTED`：提示申请未通过，允许重新申请；历史拒绝记录保留作为审核轨迹，新申请插入一条新的 `PENDING` 记录。如果展示拒绝原因，必须只展示给 Google 已验证邮箱本人。
- `APPROVED` 但没有用户：视为数据异常，提示联系平台处理。
- 无申请记录：引导到商户申请页，并尽量预填 Google 邮箱。

当前 PostgreSQL 脚本已有 `uk_sys_tenant_apply_email_active` 局部唯一索引，只限制同一邮箱同时存在 `PENDING` / `APPROVED` 申请；`REJECTED` 不阻止重新提交。因此重新申请不需要覆盖旧记录，也不需要删除历史记录。

## Risks：风险

- `sql/base.sql` 旧 MySQL 脚本存在大量乱码，但当前项目已决定 PostgreSQL 为主；若未来有人误用该旧脚本初始化，会继承乱码数据。
- `params` 当前被系统查询、健康大脑查询和代码生成模板实际使用；BO 迁移必须保留 `getParams()` 能力。
- `InjectionMetaObjectHandler` 当前基于 `BaseEntity` 填充审计字段；本轮不能直接让已有 Entity 脱离 `BaseEntity`。
- `IBaseService` 暴露过多是长期架构问题，但本轮不处理，避免增加新功能开发复杂度。
- Google ID token 校验需要官方 Java client 或等价 JWT 校验能力；若当前依赖不足，需要新增轻量官方依赖并说明原因。
- 非 Gmail / 非 Workspace 邮箱即使 `email_verified=true` 也有额外账号归属风险；第一版建议仅允许 `@gmail.com` 或有 `hd` 的 Workspace 邮箱匹配，其他邮箱先拒绝或要求账号密码登录。
- 没有 Google OAuth Client ID 时，前端按钮应隐藏或提示暂未配置，不能做假登录。
- 申请状态查询不能泄漏他人申请信息；只有 Google ID token 邮箱验证通过后，才能按该邮箱返回申请状态。

## Open Questions：待确认问题

- 健康大脑 `bocoo-modules-dealer/src/main/java/com/bocoo/healthbrain/` 代码是否仍计划删除？如果仍删除，只需要模板和基础模块治理；如果保留，则一并迁移。
- `sql/base.sql` 旧 MySQL 脚本是否还要保留为可部署资产？本计划默认只保留风险记录，不投入修复。
- Google 登录是否允许非 Gmail / 非 Workspace 邮箱？默认不允许，后续可通过配置开放。
- Google 登录是否允许跳过商户申请页的邮件验证码？默认不跳过，只预填邮箱；若后续要“Google 一键申请”，需单独设计申请表字段补全和条款确认。

## Acceptance Criteria：验收标准

- 根 `pom.xml` 不再包含已识别的乱码中文注释。
- i18n 分发模型在项目文档或任务记录中明确：`i18n/locales` 为源，运行时目标由 `pnpm i18n:sync` 同步。
- 新增 BO 请求/查询基类，`*Bo` 不再继承 `BaseEntity`，并保留查询 `params` 能力。
- 代码生成器模板不再生成 `Bo extends BaseEntity`。
- 新功能代码生成后的文件数量不增加；仍保持现有 Entity / BO / VO / Mapper / Service / ServiceImpl 模式。
- `IBaseService` 拆分和 `BaseEntity` 深层拆分记录为 TODO，不并入本轮。
- 登录页 Google 按钮使用官方 GIS 渲染或符合 Google 品牌规范的彩色 `G`，文案可用 `Continue with Google` / 对应中文 i18n。
- 前端配置 `Google Client ID` 后可触发 Google 登录；未配置时不展示不可用假按钮。
- 后端新增 Google 登录入口，验证 ID token 后复用现有 Sa-Token 登录响应结构。
- Google 登录匹配现有启用用户邮箱时直接登录，不自动创建用户、不绕过租户/角色/权限加载。
- Google 邮箱未匹配用户时返回结构化下一步状态：`APPLY_REQUIRED`、`APPLY_PENDING`、`APPLY_REJECTED` 或 `APPLY_APPROVED_BUT_USER_MISSING`。
- 前端根据结构化状态展示对应提示：去申请、等待审核、重新申请/联系平台、联系平台处理。
- 审核失败后允许同邮箱重新申请；旧 `REJECTED` 记录保留，新申请创建新的 `PENDING` 记录。
- 不引入数据库结构变化、不升级依赖、不改生产配置。

## References：参考资料

- Google Identity Services Branding Guidelines: https://developers.google.com/identity/branding-guidelines
- Sign in with Google HTML API Reference: https://developers.google.com/identity/gsi/web/reference/html-reference
- Verify the Google ID token on your server side: https://developers.google.com/identity/gsi/web/guides/verify-google-id-token

## Related Decisions：相关决策

- PostgreSQL 是当前主数据库；新项目不再补 MySQL SQL。
- 前后端 i18n 以 `i18n/locales/en_US.json` / `zh_CN.json` 为单源，运行时资源通过同步脚本生成。
