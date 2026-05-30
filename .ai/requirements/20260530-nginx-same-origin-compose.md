# Requirement: Nginx 同源代理 Docker Compose 落地

## Goal：目标

把当前前端 Nginx 同源代理方案从文档说明推进到 Docker Compose 可执行配置层面：浏览器只访问前端 Nginx 暴露的同一个 Origin，`/dev-api/*` 由 Nginx 在 compose 内网反向代理到 `bocoo-admin`。

## Background：背景

当前项目已经确认后期采用 Nginx 同源代理，避免生产环境依赖跨域 CORS。现有 `admin-ui/nginx.conf.template` 已具备 `/dev-api/` 代理能力，`admin-ui/public/_app.config.js` 也保持了相对路径 `/dev-api`。但现有 `admin-ui/docker-compose.yml` 和 `bocoo-admin/src/main/bin/docker-compose.yml` 仍使用 `network_mode: host`，与“compose 服务名内网互通”的推荐部署方式不一致。

## Scope：范围

- 调整 Docker Compose 示例/配置，使 `admin-ui` 可通过服务名代理 `bocoo-admin`。
- 保持前端运行时 `VITE_APP_BASE_API=/dev-api`。
- 保持 `admin-ui/nginx.conf.template` 的同源代理语义，必要时只做最小补充。
- 更新部署文档，使说明与实际 compose 配置一致。
- 保留本地开发 CORS 配置，不因生产同源代理影响 Vite 本地开发。

## Out of Scope：不做范围

- 不修改数据库结构、migration 或初始化 SQL。
- 不修改业务 API、权限、租户、登录认证逻辑。
- 不引入 HTTPS 证书、正式域名或公网负载均衡配置。
- 不输出或写入生产密钥、Token、数据库连接串。
- 不强制切换 Cookie / CSRF 认证架构，该项仍等待正式域名和部署拓扑。

## Business Rules：业务规则

- 后端容器对公网不必直接暴露；前端 Nginx 是浏览器入口。
- Docker Compose 服务名只允许出现在容器内部配置中，不写入浏览器可见的前端运行时配置。
- `/dev-api/*` 到后端时应去掉 `/dev-api/` 前缀，保持当前后端接口路径不变。

## API Requirements：接口要求

- 外部请求路径保持 `http://<host>/dev-api/<backend-path>`。
- Nginx 转发目标保持可配置，默认建议为 `http://bocoo-admin:8081`。
- 上传接口继续受 `client_max_body_size` 限制保护，默认保留当前 `100m`。

## Data Requirements：数据要求

无数据库变更。

## Permission / Tenant Rules：权限 / 租户规则

无权限和租户规则变更。

## i18n / UTC Rules：国际化 / 时间规则

无 i18n、UTC、时区变更。

## Options：可选方案

### Option A：只维护文档

Pros：改动最小，风险低。
Cons：实际 compose 仍是 `network_mode: host`，后续部署容易按旧方式继续走。

### Option B：更新现有两个 compose 文件

Pros：最贴近当前文件结构，用户按现有目录启动时能直接看到同源代理配置。
Cons：前后端 compose 分散在两个目录，单独启动时需要明确网络或同一个 compose 项目。

### Option C：新增根目录统一 compose 示例

Pros：最符合“一个 compose 管理前后端”的部署模型，服务名互通清晰。
Cons：可能引入与现有脚本/部署路径并存的维护成本。

## Recommended Option：推荐方案

采用 Option B + 少量 Option C 思路：优先把现有 compose 文件从 host 网络改为 compose 网络，并在文档里明确同一个 compose project / network 的运行方式；如发现两个分散 compose 文件难以可靠互通，再新增根目录示例文件作为部署入口。

## Risks：风险

- 现有部署如果依赖 host 网络访问本机 Redis、PostgreSQL 或 MinIO，改 compose 网络后需要用宿主机地址或把依赖服务纳入 compose 网络。
- 后端 `SPRING_PROFILES_ACTIVE=dev` 当前仍是开发配置；本次不擅自切生产 profile。
- Windows / Linux Docker 对 `host.docker.internal` 支持差异较大，不能把它作为唯一生产方案。

## Open Questions：待确认问题

- 后端容器是否也要在同一个 compose 中统一编排，还是继续保留前后端两个目录分别启动。
- PostgreSQL、Redis、MinIO 后续是否也会纳入同一个 compose，还是使用已有外部服务。

## Acceptance Criteria：验收标准

- `admin-ui/docker-compose.yml` 默认 `API_PROXY_PASS` 指向 compose 内部后端服务名。
- 同源代理入口保持 `/dev-api`，前端配置不写后端服务名。
- 文档说明与实际 compose 配置一致。
- 不修改数据库、业务代码、认证架构或生产密钥。
- 通过 YAML 语法级检查；如环境允许，执行最小 compose config 校验。

## Related Decisions：相关决策

- 2026-05-30：后期 Docker Compose / 生产部署推荐 Nginx 同源代理，正式域名未确定前不收紧生产域名白名单。
