# Current

## Current Status：当前状态

当前安全漏洞与业务安全修复批次已归档。

归档文件：`.ai/archive/20260530-security-business-remediation.md`

最终状态：Accepted with Risks。

## Completed：已完成

- 已完成 `CODE_REVIEW.md` 与 `CODE_REVIEW_BUSINESS.md` 中可直接修复的主要安全/业务安全项。
- 已完成 PostgreSQL-only DB/架构批次：`sys_user.force_password_change`、DEV PG 列/字典/索引应用、首次登录强制改密链路、Sa-Token 单用户最大登录数。
- 已完成新增审计类型和敏感/跨租户操作审计补充。
- 已完成前端依赖 audit 的可兼容降险，`pnpm audit` 从 8 个问题降到 1 个剩余 `quill` advisory。

## Validation Summary：验证摘要

- Passed: `pnpm i18n:validate`
- Passed: `pnpm i18n:sync`
- Passed: `mvn -DskipTests compile`
- Passed: `mvn -pl bocoo-admin -am -DskipTests package`
- Passed: admin-ui `pnpm typecheck`
- Passed: admin-ui `pnpm build`
- Passed: DEV backend unauthenticated smoke
- Passed: DEV backend authenticated smoke using existing local Redis admin Token
- Blocked: backend OWASP Dependency-Check, due to CISA/NVD external data-source failures and no local vulnerability database

## Remaining Risks / TODO：剩余风险

- TODO: rerun backend OWASP Dependency-Check after NVD API Key, internal mirror, or external source availability is ready.
- TODO: evaluate `@vueup/vue-quill -> quill@1.3.7` migration; do not force Quill 2.x without component compatibility testing.
- TODO: revisit HttpOnly Cookie + CSRF after formal domain and deployment topology exist.
- TODO: tighten production CORS origins when a formal domain exists.
- TODO: merchant application progress self-service query remains deferred by user decision.

## Next Step：下一步

等待用户发起下一项任务。
