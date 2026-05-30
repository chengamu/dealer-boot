# Tasks

## Active Queue

No active task.

## Archived

- 2026-05-30 security and business remediation batch archived in `.ai/archive/20260530-security-business-remediation.md`.
- 2026-05-30 code review risk and curtain intro batch archived in `.ai/archive/20260530-code-review-curtain-intro.md`.

## Follow-up Candidates

### F001 - Backend Dependency-Check

Status: pending
Reason: OWASP Dependency-Check could not complete because CISA/NVD external vulnerability data sources failed and no local cache existed.
Next: rerun with NVD API Key, internal vulnerability database mirror, or after external source availability is restored.

### F002 - Rich Text Editor Security

Status: pending
Reason: `@vueup/vue-quill` still resolves to `quill@1.3.7`, which has one moderate advisory. No `quill@1.3.8` is published.
Next: evaluate editor replacement or Quill 2.x migration with compatibility testing.

### F003 - Cookie / CSRF Architecture

Status: deferred
Reason: current Bearer Token can remain until a formal domain and deployment topology exist.
Next: create a dedicated `/plan` after deployment topology is known.

### F004 - Production CORS Origins

Status: deferred
Reason: formal domain does not exist yet.
Next: update YAML origins when domain is available.

### F005 - Merchant Application Progress Self-service Query

Status: deferred
Reason: user decided this is not needed for the current batch.
Next: revisit only if product/business requires it.

### F006 - 日志清理批量删除复用

Status: pending
Reason: `cleanLogininfor()` 和 `cleanOperLog()` 逻辑重复，但抽象泛型工具对当前安全修复不是必要条件。
Next: 后续有清理窗口时再做。

### F007 - XssFilter GET/DELETE 性能策略

Status: pending
Reason: 恢复 method 级别跳过会改变 XSS 防护面。
Next: 后续结合压测或明确安全策略单独评估。
