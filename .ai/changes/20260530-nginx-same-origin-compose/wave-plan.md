# Wave Plan

## Execution Mode

- Mode: wave-scheduler
- MaxParallelAgents: 1
- UseWorktree: false
- ConflictPolicy: fail-fast
- MergePolicy: orchestrator-only

## Requirement Source

- `.ai/requirements/20260530-nginx-same-origin-compose.md`

## Options

- Option A：只维护文档，风险最低，但实际 compose 仍与推荐部署不一致。
- Option B：更新现有 `admin-ui` 与 `bocoo-admin` compose 文件，贴近当前项目结构。
- Option C：新增根目录统一 compose 示例，部署模型更清晰，但会增加一个入口文件。

## Recommendation

先执行 Option B。若验证发现分散 compose 文件无法稳定通过服务名互通，再补一个根目录统一 compose 示例作为部署入口，避免过早引入新部署结构。

## Trade-offs

- 使用 compose 网络后，后端不再依赖 host 网络直接暴露，符合生产同源代理目标。
- 如果已有本机外部 Redis、PostgreSQL、MinIO，后端容器连接这些服务的方式可能需要后续单独配置；本计划不改数据库连接串。

## Risks

- `network_mode: host` 改为 compose 网络可能影响已有本地容器启动习惯。
- 后端 DEV 配置仍可能指向宿主机服务；本计划只保证前端 Nginx 到后端服务名代理链路。
- 不具备完整 Docker runtime 时，只能做 `docker compose config` 或静态 YAML 检查。

## Pause Points

- 如果发现当前 compose 依赖宿主机网络访问数据库/Redis 且无明确替代地址，暂停并记录 TODO。
- 如果需要新增生产域名、证书、HTTPS 或 Cookie/CSRF 架构，暂停回到 `/plan`。

## Wave 0: Scope / Deployment Boundary

### Task C1: 确认同源代理部署边界

TaskId: C1
Title: 确认同源代理部署边界
Owner: main
AgentRole: contract
Status: pending
Priority: high
RiskLevel: medium，原因是 compose 网络会影响部署入口，但不涉及业务代码。
Wave: 0

Files:
- `admin-ui/docker-compose.yml`
- `admin-ui/nginx.conf.template`
- `admin-ui/public/_app.config.js`
- `bocoo-admin/src/main/bin/docker-compose.yml`
- `docs/deployment-nginx-same-origin.md`

Forbidden:
- `bocoo-admin/src/main/resources/application-prod.yml`
- `bocoo-admin/src/main/resources/application-dev.yml`
- `**/src/main/resources/sql/**`
- `**/migration/**`
- `pom.xml`
- `admin-ui/package.json`
- `.ai/archive/**`

DependsOn: none
ParallelGroup: deployment-contract
ConflictBoundary: deployment-config-only

Acceptance:
- 明确浏览器入口、Nginx 代理路径、后端容器服务名和不做范围。
- 明确不修改 DB、认证架构、生产域名和业务接口。

## Barrier 0

Checks:
- Scope 已限定为 deployment config / docs。
- 不存在未授权的数据库、依赖升级、认证架构调整。

## Wave 1: Compose / Docs Implementation

### Task D1: 调整 compose 同源代理配置

TaskId: D1
Title: 调整 compose 同源代理配置
Owner: main
AgentRole: deployment
Status: pending
Priority: high
RiskLevel: medium，原因是 `network_mode: host` 到 compose 网络的变化会改变容器互通方式。
Wave: 1

Files:
- `admin-ui/docker-compose.yml`
- `bocoo-admin/src/main/bin/docker-compose.yml`

Forbidden:
- `bocoo-admin/src/main/resources/application-prod.yml`
- `bocoo-admin/src/main/resources/application-dev.yml`
- `**/src/main/resources/sql/**`
- `**/migration/**`
- `pom.xml`
- `admin-ui/package.json`
- `.ai/archive/**`

DependsOn:
- C1

ParallelGroup: deployment-config
ConflictBoundary: compose-only

Acceptance:
- `admin-ui` 默认通过 `http://bocoo-admin:8081` 代理后端。
- compose 配置不再把 Docker 服务名写入浏览器运行时配置。
- 尽量不直接暴露后端端口；如保留端口只用于本地调试，必须写清楚。

### Task D2: 对齐部署文档

TaskId: D2
Title: 对齐部署文档
Owner: main
AgentRole: docs
Status: pending
Priority: medium
RiskLevel: low，原因是只更新部署说明。
Wave: 1

Files:
- `docs/deployment-nginx-same-origin.md`
- `admin-ui/README.md`
- `README.md`

Forbidden:
- `bocoo-admin/src/main/java/**`
- `admin-ui/src/**`
- `pom.xml`
- `admin-ui/package.json`
- `.ai/archive/**`

DependsOn:
- C1

ParallelGroup: docs
ConflictBoundary: docs-only

Acceptance:
- 文档说明与实际 compose 默认值一致。
- 明确本地开发不受影响。
- 明确正式域名、HTTPS、Cookie/CSRF 仍是后续事项。

## Barrier 1

Checks:
- `Files` 没有越界修改。
- compose 与文档没有互相矛盾。
- 前端 `VITE_APP_BASE_API` 仍是 `/dev-api`。

## Wave 2: Integration Alignment

### Task I1: 校验 Nginx 路径与 compose 服务名一致

TaskId: I1
Title: 校验 Nginx 路径与 compose 服务名一致
Owner: main
AgentRole: integration
Status: pending
Priority: medium
RiskLevel: low，原因是只做配置一致性检查。
Wave: 2

Files:
- `admin-ui/nginx.conf.template`
- `admin-ui/docker-compose.yml`
- `bocoo-admin/src/main/bin/docker-compose.yml`
- `docs/deployment-nginx-same-origin.md`

Forbidden:
- `bocoo-admin/src/main/java/**`
- `admin-ui/src/**`
- `**/src/main/resources/sql/**`
- `.ai/archive/**`

DependsOn:
- D1
- D2

ParallelGroup: integration
ConflictBoundary: deployment-alignment-only

Acceptance:
- `/dev-api/*` 转发目标和文档请求流向一致。
- 后端服务名、端口、暴露策略在 compose 与文档中一致。

## Barrier 2

Checks:
- 同源代理链路可从配置静态推导。
- 没有引入生产域名或敏感配置。

## Wave 3: Code Review / Deployment Review

### Task R1: 部署配置审查

TaskId: R1
Title: 部署配置审查
Owner: code-reviewer
AgentRole: code-reviewer
Status: pending
Priority: medium
RiskLevel: medium，原因是部署配置错误会导致运行时不可访问。
Wave: 3

Files:
- `admin-ui/docker-compose.yml`
- `bocoo-admin/src/main/bin/docker-compose.yml`
- `admin-ui/nginx.conf.template`
- `docs/deployment-nginx-same-origin.md`

Forbidden:
- `bocoo-admin/src/main/resources/application-prod.yml`
- `**/src/main/resources/sql/**`
- `.ai/archive/**`

DependsOn:
- I1

ParallelGroup: review
ConflictBoundary: review-only

Acceptance:
- 检查是否存在服务名不可解析、路径前缀错误、后端暴露策略矛盾、文档误导。

## Wave 4: Check / Validation

### Task V1: 执行配置校验

TaskId: V1
Title: 执行配置校验
Owner: main
AgentRole: check
Status: pending
Priority: high
RiskLevel: low，原因是只运行静态或 Docker Compose 配置校验。
Wave: 4

Files:
- `admin-ui/docker-compose.yml`
- `bocoo-admin/src/main/bin/docker-compose.yml`
- `docs/deployment-nginx-same-origin.md`

Forbidden:
- `**/src/main/resources/sql/**`
- `.ai/archive/**`

DependsOn:
- R1

ParallelGroup: validation
ConflictBoundary: validation-only

Acceptance:
- 执行 `git diff --check`。
- 如本机 Docker Compose 可用，执行相关 `docker compose config` 校验。
- 如果 Docker 不可用，记录未执行原因，不伪造通过。
