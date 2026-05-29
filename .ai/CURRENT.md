# Current

## Current Goal：当前目标

基于 `CODE_REVIEW.md`、`CODE_REVIEW_BUSINESS.md` 和 `.ai.workflow-backup` 制定安全漏洞与业务安全改进计划。

## Requirement Source：需求来源

`D:\work\base-boot\CODE_REVIEW.md`、`D:\work\base-boot\CODE_REVIEW_BUSINESS.md`；补充读取 `D:\work\base-boot\.ai.workflow-backup` 中租户/权限历史记录。

## Current Phase：当前阶段

`/plan`：已完成安全报告与业务安全报告核验、业务约束补充和分批方案拆解，等待用户确认是否进入第一批 `/do`。

## Scope：范围

- 分析 `CODE_REVIEW.md` 中的漏洞项。
- 分析 `CODE_REVIEW_BUSINESS.md` 中的业务安全项。
- 结合当前代码验证高风险与安全相关问题是否真实存在。
- 读取 `.ai.workflow-backup`，确认平台租户全量查询/数据权限 bypass 是业务需求。
- 生成安全漏洞、业务安全和后续专项改进计划。

## Out of Scope：不做范围

- 当前阶段不修改业务代码。
- 不运行 build/test/lint。
- 不归档历史工作。
- 第一批不修改数据库结构、不修改 migration、不升级依赖、不做大型前端重构。

## Key Decisions：关键决策

- 需求来源沉淀为 `.ai/requirements/20260529-security-vulnerability-remediation.md`。
- 推荐采用三批推进：第一批修局部可修的真实漏洞；第二批修中风险安全/稳定性/性能；第三批处理依赖、DB、Token/CSRF 架构和前端技术债。
- 业务安全报告中 `TenantContextFilter` 清理和个人密码修改旧密码校验已由现有代码满足，不安排无效修复。
- 业务安全报告中的登录限流、RepeatSubmit fallback、商户验证码滥用、邮箱枚举、审批并发、GET XSS 跳过、SMS/Email key 共用进入 Phase 1/2。
- 首次登录强制改密码、单用户 Token 数量上限、超级管理员查询审计、商户申请进度查询进入业务流程专项。
- 当前没有正式域名，CORS 第一批做配置化并保留本地开发来源。
- 上传允许常见图片、PDF、Excel、Word。
- `allList` / 平台用户查询的租户/数据权限 bypass 是业务需求，修复时只复核受控边界，不直接移除。

## Active Tasks：当前任务

- 等待用户确认是否进入第一批 `/do`。

## Risks / Pause Points：风险 / 暂停点

- 生产 CORS 域名尚不存在，后续上线前需配置。
- 限流阈值可先采用保守默认，后续按业务调整。
- 商户验证码和公开申请错误消息改动涉及 i18n 与公开接口语义，执行前需要按 Pause Rules 明确影响范围。
- SMS/Email 验证码 key 拆分会使发布切换期间未消费的旧验证码失效，需要作为发布说明。
- 首次登录强制改密码和 Token 数量上限涉及数据库/认证主流程，必须专项确认。
- 准备运行 build/test 前需暂停确认。

## Validation Plan：验证计划

- 后端：优先 Maven 编译检查；必要时针对修改模块运行测试。
- 前端：涉及 `admin-ui` 时运行 `pnpm typecheck`，必要时运行 `pnpm build`。

## Next Step：下一步

等待用户确认按更新后的计划进入第一批 `/do`；第二批业务安全项在第一批完成后继续推进，涉及数据库/认证主流程的业务专项单独 `/plan`。

---

# Current Work

## 2026-05-29 Admin UI dealer checkout static page

- Target project: `D:\work\base-boot\admin-ui`.
- Current focus: dealer portal static pages, with checkout page polished first.
- Implemented checkout visual pass in `admin-ui/src/pages/dealer-portal/DealerPortalPage.vue`.
- Cropped checkout product thumbnails from `admin-ui/UI/checkout.png` into `admin-ui/src/assets/generated/dealer/`.
- Added/updated i18n keys in `i18n/locales/en_US.json` and `i18n/locales/zh_CN.json`, then synced runtime i18n JSON.
- Verification completed in this phase:
  - `pnpm i18n:validate`
  - `pnpm typecheck`
  - `pnpm build`
  - Playwright screenshot check with mocked auth for `/dealer/checkout`
- Visual QA artifacts:
  - `admin-ui/UI/checkout-render-check.png`
  - `admin-ui/UI/checkout-render-check-collapsed.png`

## Notes

- No project-approved image2 generation entrypoint was confirmed in this phase, so no generated image2 asset is claimed.
- The checkout thumbnails are crops from the provided UI reference image.
- Existing unrelated dirty workspace changes were left untouched.
- Suggested next page: `admin-ui/src/pages/dealer-portal/DealerPortalPage.vue` quote creation view, reusing the checkout card/table/sidebar patterns.

## 2026-05-29 Admin UI dealer order detail static page

- Continued in `admin-ui/src/pages/dealer-portal/DealerPortalPage.vue`.
- Reworked `/dealer/orders/detail` static view against `admin-ui/UI/orderdatail.png`.
- Main changes:
  - Order facts card with order/quote/customer/status/payment/date and action buttons.
  - Horizontal/stacked order progress tracker.
  - Order item detail table using the existing cropped shade asset.
  - Shipping address and attachment cards.
  - Right-side order summary, payment facts, ERP sync progress, and factory updates.
- Added i18n keys in `i18n/locales/en_US.json` and `i18n/locales/zh_CN.json`, then synced runtime files.
- Verification completed:
  - `pnpm i18n:validate`
  - `pnpm typecheck`
  - `pnpm i18n:sync`
  - `pnpm build`
  - Playwright visual check for `/dealer/orders/detail` with mocked auth.
- Visual QA artifacts:
  - `admin-ui/UI/order-detail-render-check.png`
  - `admin-ui/UI/order-detail-render-check-mobile.png`
- Visual check result:
  - No `????` text.
  - No unresolved `dealerPortal.*` keys.
  - No document-level horizontal overflow at `1224x918` or `736x918`.
  - Main content can scroll.

## 2026-05-29 Admin UI dealer pricing static page

- Current focus: `/dealer/pricing` static visual pass against `admin-ui/UI/prductspricing.png`.
- Implemented:
  - Product/pricing tabs with icons.
  - Price grid filter panel.
  - Price grid rules table with version, effective dates, status and pagination.
  - Import results side panel with validation summary, error list, import mode controls and actions.
  - Pricing summary side cards.
  - Added i18n keys in `i18n/locales/en_US.json` and `i18n/locales/zh_CN.json`.
- Verification completed:
  - `pnpm i18n:validate`
  - `pnpm typecheck`
  - `pnpm i18n:sync`
  - `pnpm build`
  - Playwright visual checks for `/dealer/pricing`.
- Visual QA artifacts:
  - `admin-ui/UI/pricing-render-check.png`
  - `admin-ui/UI/pricing-render-check-mobile.png`
- Visual check result:
  - No `????` text.
  - No unresolved `dealerPortal.*` keys.
  - No document-level horizontal overflow at `1224x918` or `736x918`.
  - Main content can scroll.

## 2026-05-29 Admin UI dealer dashboard static page completion

- Continued in `admin-ui/src/pages/dealer-portal/DealerPortalPage.vue`.
- Completed `/dealer/dashboard` against `admin-ui/UI/商家首页.png`.
- Main changes:
  - Added full recent orders panel under recent quotes.
  - Expanded right-side Need Attention into expiring quotes, missing attachments, and orders awaiting payment groups.
  - Added dashboard table actions, status tones, attention group links, and Eastern Time note.
  - Added i18n keys in `i18n/locales/en_US.json` and `i18n/locales/zh_CN.json`, then synced runtime files.
- Verification completed:
  - `pnpm i18n:validate`
  - `pnpm typecheck`
  - `pnpm i18n:sync`
  - `pnpm build`
  - Playwright visual checks for `/dealer/dashboard`.
- Visual QA artifacts:
  - `admin-ui/UI/dealer-dashboard-render-check.png`
  - `admin-ui/UI/dealer-dashboard-render-check-mobile.png`
- Visual check result:
  - No `????` text.
  - No unresolved `dealerPortal.*` keys.
  - No document-level horizontal overflow at `1224x918` or `736x918`.
  - Main content can scroll.

## 2026-05-29 Admin UI dealer create quote static page

- Continued in `admin-ui/src/pages/dealer-portal/DealerPortalPage.vue`.
- Reworked `/dealer/quotes/create` static view to better match `admin-ui/UI/createquote.png`.
- Main changes:
  - Stepper visual flow for quote creation.
  - Customer selection card.
  - Product configuration card with dense Element Plus controls.
  - Dimension inputs, deduction notice, production-size callout.
  - Validation/action row and quote items table.
  - Quote summary panel and sticky bottom actions.
- Added i18n keys in `i18n/locales/en_US.json` and `i18n/locales/zh_CN.json`, synced runtime files.
- Fixed newly added zh_CN values after PowerShell encoding converted them to `?`.
- Verification completed:
  - `pnpm i18n:validate`
  - `pnpm typecheck`
  - `pnpm build`
  - Playwright visual check for `/dealer/quotes/create` with mocked auth.
- Visual QA artifacts:
  - `admin-ui/UI/createquote-render-check.png`
  - `admin-ui/UI/createquote-render-check-bottom.png`
- Visual check result:
  - No `????` text.
  - No unresolved `dealerPortal.*` keys.
  - No horizontal overflow at `1224x918`.
  - Bottom sticky action bar is visible after scrolling.

## 2026-05-29 Admin UI dealer management static page

- Continued in `admin-ui/src/pages/dealer-portal/DealerPortalPage.vue`.
- Reworked `/dealer/dealers` static view against `admin-ui/UI/dealerManagement.png`.
- Main changes:
  - Dealer metrics cards with colored icon treatments and sparklines.
  - Dealer table toolbar with search, filters, tier select, and responsive export icon button.
  - Dealer table with selectable rows, status/tier/tax/quote/order columns, and selected-row styling.
  - Review detail panel for pending dealer approval.
  - Bottom insight cards for dealer summary, tier breakdown, and recent approval activity.
- Fixed a shared metric-card selector issue: `.dealer-metric svg` was also styling Element Plus icon SVGs, so it was narrowed to direct sparkline SVGs.
- Added i18n keys in `i18n/locales/en_US.json` and `i18n/locales/zh_CN.json`, synced runtime files.
- Also repaired existing trend labels that had become `? {value} vs last month`.
- Verification completed:
  - `pnpm i18n:validate`
  - `pnpm typecheck`
  - `pnpm build`
  - Playwright visual check for `/dealer/dealers` with mocked auth.
- Visual QA artifact:
  - `admin-ui/UI/dealer-management-render-check.png`
- Visual check result:
  - No `????` text.
  - No unresolved `dealerPortal.*` keys.
  - No document-level horizontal overflow at `1224x918`.
  - The table intentionally scrolls horizontally inside its own container when the viewport is narrow.

## 2026-05-30 Admin UI dealer portal static data handoff

- Continued dealer portal static page handoff work in `admin-ui`.
- Added `admin-ui/src/pages/dealer-portal/dealerPortalMock.ts` for static data that does not depend on i18n.
- Updated `admin-ui/src/pages/dealer-portal/DealerPortalPage.vue` to import the extracted static data while leaving the existing template and styles intact.
- Added `admin-ui/UI/dealer-pages-api-notes.md` to document page purpose and future API replacement points.
- Validation completed:
  - `pnpm typecheck`
  - `pnpm build`

## 2026-05-30 Admin UI dealer portal component cleanup

- Extracted two low-risk shared presentation components:
  - `admin-ui/src/pages/dealer-portal/components/DealerSparkline.vue`
  - `admin-ui/src/pages/dealer-portal/components/DealerStatusBadge.vue`
- Replaced repeated dealer portal status badges and metric sparklines in `DealerPortalPage.vue`.
- Kept page layout, data shape, i18n keys, and visual CSS unchanged.
- Validation completed:
  - `pnpm typecheck`
  - `pnpm build`
- Next validation:
  - Lightweight Playwright visual smoke check for affected dealer pages.

## 2026-05-30 Dealer portal zh_CN i18n repair

- Playwright visual smoke check found several `dealerPortal.*` Chinese values rendered as `???`.
- Scope is limited to existing dealer portal i18n values in `i18n/locales/zh_CN.json`.
- Runtime JSON files will be regenerated by `pnpm i18n:sync`; no manual runtime i18n edits.
- Validation completed:
  - `pnpm i18n:validate`
  - `pnpm i18n:sync`
  - `pnpm typecheck`
  - `pnpm build`
  - Lightweight Playwright visual smoke check.
