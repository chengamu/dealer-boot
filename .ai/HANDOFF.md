# Handoff

## Current Status

本波次无进行中 `/plan`/`/do` 任务。`产品配置中心基础资料模块重构` 已完成并归档，状态为 `Accepted`。

## Archive

- `.ai/archive/20260612-product-base-info-refactor.md`
- `.ai/archive/20260607-product-capability-phase1.md`
- `.ai/archive/20260607-product-capability-usability-wave.md`
- `.ai/archive/20260530-pay-module-migration.md`
- `.ai/archive/20260530-security-business-remediation.md`
- `.ai/archive/20260530-code-review-curtain-intro.md`
- `.ai/archive/20260530-healthbrain-drug-google-login.md`
- `.ai/archive/20260530-google-login-hardening.md`
- `.ai/archive/20260606-product-capability-center-batch1.md`
- `.ai/archive/20260606-product-capability-center-batch2.md`
- `.ai/archive/20260606-product-capability-center-batch3.md`
- `.ai/archive/20260606-product-capability-center-batch4-db-readiness.md`
- `.ai/archive/20260606-product-capability-center-batch5-sql-trial.md`
- `.ai/archive/20260606-product-capability-center-batch6-db-execution-plan.md`
- `.ai/archive/20260606-product-capability-center-batch7-snapshot-instance.md`
- `.ai/archive/20260606-product-capability-center-batch8-import-center.md`
- `.ai/archive/20260606-product-capability-center-batch9-sync-outbox.md`
- `.ai/archive/20260606-product-capability-center-batch10-xls-import-preview.md`

## Validation Snapshot

- `codegraph sync` completed for product base info refactor.
- `sql/postgresql/product_capability.sql` executed against local dev PostgreSQL.
- `mvn -pl bocoo-modules-product -DskipTests compile` passed.
- `mvn -pl bocoo-admin -DskipTests clean package` passed before dist runtime validation.
- `pnpm --dir admin-ui build` passed.
- Playwright + local Chrome smoke passed for six pages under `http://127.0.0.1:8083/product-master/*`.
- Runtime used `bocoo-admin/target/dist/bocoo-admin.jar`, not `spring-boot:run`.
- `codegraph sync` completed.
- `git diff --check` completed.
- `mvn -pl bocoo-module-pay -am -DskipTests compile` passed.
- `mvn -pl bocoo-admin -am -DskipTests compile` passed.
- `mvn -pl bocoo-admin -am -DskipTests package` passed.
- `pnpm --dir admin-ui typecheck` passed.
- `pnpm --dir admin-ui build` passed.

## Remaining Follow-ups

- 产品配置中心基础资料下一步可继续做：逐项 CRUD 保存/编辑/删除验收、配置器试算器、BOM/工程规则、OFBiz 数据迁移导入工具。
- 基础资料服务当前是按领域拆分后的轻量 service；如后续要严格贴合代码生成器风格，可单独计划拆 `IService` / `ServiceImpl`，不要混在业务功能里顺手改。
- Real PayPal / Stripe / Alipay / Weixin payment and webhook integration is deferred in `.ai/changes/20260530-paypal-pay-module-migration/real-payment-todo.md`.
- Real transfer submission, query, and callback sync are deferred.
- Payment admin frontend pages, menu SQL, role permission initialization, and channel config write API are still pending.
- Real payment unfreeze requires sandbox or production merchant config, certificates / webhook secret, and a public webhook URL.
- The Nginx same-origin compose plan remains deferred in `.ai/requirements/20260530-nginx-same-origin-compose.md`.
- Google production origin remains deferred until the final domain or same-origin Nginx topology is confirmed.
- Google account binding table remains deferred unless multi-account binding becomes a real requirement.

## Next Step

当前基础资料重构已完成归档；如有新增需求，请在新 Wave 下发起新 `/plan`。
