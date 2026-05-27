# Project Context

## Summary

本项目是继承自 ruoyi-vue-plus 的 bocoo-base 管理系统，包含 Java Spring Boot 后端、Vue 3 管理端、系统模块、代码生成模块和公共组件模块。

## Tech Stack

- Backend：Java 17、Spring Boot 3.2.9、Maven multi-module、Undertow、Sa-Token、MyBatis-Plus、dynamic-datasource、HikariCP、Redisson、Knife4j/SpringDoc、MapStruct Plus、Lombok、Hutool、EasyExcel。
- Frontend：Vue 3、TypeScript、Vite、Element Plus、Pinia、Vue Router、Axios、vue-i18n、dayjs、ECharts。
- Database：当前项目规则以 PostgreSQL 为主；`application-dev.yml` 使用 PostgreSQL master datasource。MySQL driver 仍在依赖中，但项目规则要求不再为 MySQL 补新增 SQL。
- Cache / Middleware：Redis/Redisson、XXL-JOB 外部入口、MinIO/S3 类文件存储能力。
- Build tools：Maven、Vite、vue-tsc。前端统一使用 pnpm，保留 `pnpm-lock.yaml` 和 `pnpm-workspace.yaml`；不要重新引入 npm/yarn 锁文件或脚本命令。

## Structure

- `bocoo-admin/`：Spring Boot Web 启动入口与配置，主类为 `com.bocoo.BocooApplication`。
- `bocoo-common/`：公共能力模块，包含 core、web、security、mybatis、redis、oss、mail、i18n locale resolver 等。
- `bocoo-modules-system/`：系统管理业务模块。
- `bocoo-modules-generator/`：代码生成模块。
- `admin-ui/`：Vue 3 + TypeScript 管理端。
- `sql/`：数据库脚本目录。修改数据库结构或 migration 前必须暂停确认。
- `docs/`：项目文档。
- `.codex/agents/`：项目级子 Agent 配置。
- `.ai/`：轻量 AI 工作流上下文目录；核心文件为 `RULES.md`、`CONTEXT.md`、`CURRENT.md`、`MEMORY.md`。

## Commands

### Install

- Backend：Maven 自动解析依赖；TODO：确认是否有项目固定的 Maven wrapper 或私服配置。
- Frontend：`cd admin-ui; pnpm install`。

### Dev

- Backend local RUN：`bocoo-admin/src/main/bin/run.bat` 使用 `bocoo-admin.jar` 启动，默认 `SPRING_PROFILES_ACTIVE=dev`，并从同级 `config/` 目录加载 `application-dev.yml`。
- Backend source：如需源码方式临时启动，候选命令为 `mvn -pl bocoo-admin -am spring-boot:run`；运行前仍需确认数据库、Redis、环境变量等本地依赖。
- Frontend local RUN：`admin-ui/bin/run-web.bat` 执行 `pnpm dev`；等价命令为 `cd admin-ui; pnpm dev`，Vite dev server 默认端口 8083，代理 `/dev-api` 到 `http://127.0.0.1:8081`。
- Frontend preview：`cd admin-ui; pnpm preview`。

### Runtime

- Local：前后端本地环境均使用 RUN 脚本运行；后端使用 `bocoo-admin/src/main/bin/run.bat`，前端使用 `admin-ui/bin/run-web.bat`。
- Production：前后端生产环境均使用 Docker Compose 运行。后端 Compose 文件在 `bocoo-admin/src/main/bin/docker-compose.yml`，以 JAR 方式启动并挂载 `./config`；前端 Compose 文件在 `admin-ui/docker-compose.yml`，使用 host 网络，通过 Nginx 托管 Vite `dist` 并默认将 `/dev-api/` 反代到 `http://127.0.0.1:8081`。
- Backend local dependencies：后端本地 RUN 需要 Java 17、`bocoo-admin.jar`、同级 `config/application-dev.yml`、PostgreSQL datasource、Redis `127.0.0.1:6379`；HTTP 端口为 `8081`，WebSocket 端口默认 `2831`。启动脚本会为本地 dev 设置必要 JWT/crypto 环境变量默认值，但生产或共享环境必须使用外部安全配置。
- Backend local check：当前机器已确认 Java 17 可用，配置中的 PostgreSQL 端口和 Redis `127.0.0.1:6379` 端口可达；`bocoo-admin/src/main/bin/bocoo-admin.jar` 与同级外部 `config/application-dev.yml` 当前未在仓库 bin 目录中，需要运行前放置或由发布包提供。

### Build

- Backend：`mvn -DskipTests compile` 或按需 `mvn package`。
- Frontend：`cd admin-ui; pnpm build`，脚本执行 `vue-tsc --noEmit && vite build`。
- Frontend typecheck：`cd admin-ui; pnpm typecheck`。

### Test

- Backend：`mvn test`。
- Frontend：TODO：`package.json` 未发现 test 脚本。

### Lint

- Backend：TODO：未发现独立 lint 命令。
- Frontend：TODO：`package.json` 未发现 lint 脚本。

## Frontend Conventions

- 前端入口在 `admin-ui/src`，路径别名 `@` 指向 `admin-ui/src`。
- Vite 配置位于 `admin-ui/vite.config.ts`。
- API 请求层在 `admin-ui/src/utils/request.ts`，请求会发送 `Content-Language`。
- 时间展示和绝对时间提交优先使用 `admin-ui/src/utils/datetime.ts` 中的 `formatUtc()`、`toUtcPayload()`、`withUtcDateRange()`、`withUtcDateRangeParams()`。
- 新增可见文案必须进入 `admin-ui/src/locales/zh_CN.ts` 和 `admin-ui/src/locales/en_US.ts`，不要在页面局部硬编码。

## Backend Conventions

- Maven 根模块为 `bocoo-base`，聚合 `bocoo-admin`、`bocoo-common`、`bocoo-modules-generator`、`bocoo-modules-system`。
- Web 入口模块 `bocoo-admin` 打包为 jar，主类配置为 `com.bocoo.BocooApplication`。
- 权限使用 Sa-Token 注解，例如 `@SaIgnore`、`@SaCheckPermission(...)`。
- 租户能力在 `bocoo-admin/src/main/resources/application.yml` 的 `bocoo.tenant.enabled` 配置中开启；涉及租户隔离、忽略 URL 或忽略表时必须先分析影响范围。
- 数据权限使用 `@DataPermission` / `@DataColumn`，由 MyBatis-Plus 相关处理器拼接过滤条件。
- 审计字段填充在 `bocoo-common/bocoo-common-mybatis/.../InjectionMetaObjectHandler.java`。

## Database Conventions

- 主数据库是 PostgreSQL；历史 MySQL 脚本或依赖仅视为遗留兼容，新需求默认不补 MySQL SQL。
- SQL 命名优先使用 `snake_case`；Java 实体使用 camelCase，并依赖 MyBatis `mapUnderscoreToCamelCase` 映射。
- 典型实体继承 `BaseEntity`，公共审计字段包括 `create_by_id`、`create_by`、`create_time`、`update_by`、`update_time`，部分表另有 `remark`。
- 审计字段由 `InjectionMetaObjectHandler` 自动填充；当前时间使用 `TimeUtils.utcNow()`，不要直接依赖系统默认时区。
- 主键通常为 `bigint`，字段名按表语义命名为 `*_id`，实体使用 `@TableId`；MyBatis-Plus 配置了 `DefaultIdentifierGenerator`。
- 时间字段优先使用 PostgreSQL `timestamptz`，SQL 注释按 UTC 语义标明；Java `LocalDateTime` 经 `UtcLocalDateTimeTypeHandler` 和 UTC 工具处理。
- 逻辑删除字段常见为 `del_flag char(1)`，实体使用 `@TableLogic`；已确认系统核心表注释约定 `0` 表示存在、`2` 表示删除。
- 多租户表通常包含 `tenant_id bigint NOT NULL DEFAULT 0`；平台/厂家租户约定为 `tenant_id = 0`。
- 租户隔离由 MyBatis-Plus `TenantLineInnerInterceptor` 注入 `tenant_id` 条件；忽略表和忽略 URL 来自 `bocoo.tenant` 配置，改动前必须分析影响范围。
- 数据权限使用 `@DataPermission` / `@DataColumn`，常见按部门列和用户列拼接过滤条件；新增列表查询前先确认是否需要数据权限注解。
- 字典使用 `sys_dict_type` / `sys_dict_data`，字典数据支持 `i18n_key`；菜单和字典等可见配置优先联动 `sys_i18n_message`。
- i18n message 使用 `sys_i18n_message(message_key, locale, message_value)`，现有唯一约束模式为 `(message_key, locale)`。
- 索引命名风格以 `idx_<table>_<columns>` 和 `uk_<table>_<columns>` 为主；PostgreSQL SQL 可使用 partial index，但新增索引前必须说明查询场景。
- 建表 SQL 风格以 `CREATE TABLE IF NOT EXISTS`、`CREATE INDEX IF NOT EXISTS`、`COMMENT ON`、`ON CONFLICT` 为代表；不要把完整 SQL 复制进 `.ai` 文件。
- 涉及建表、改表、索引、唯一约束、初始化数据、菜单权限、字典或 i18n message 时，必须先在 `.ai/CURRENT.md` 分析影响范围并暂停确认。
- TODO：未做全库 reverse engineering；非系统核心模块如有特殊公共字段或状态枚举，需在具体需求中读取相似表后确认。

## API Conventions

- 后端统一响应和异常消息应优先走 i18n message key。
- 请求 locale 通过 `content-language` 请求头解析。
- 绝对时间 API 契约以 ISO-8601 UTC `Z` 为目标；旧无时区字符串不应作为新代码契约。
- 修改 API DTO、响应字段、权限标识或时间格式前，需要在 CURRENT.md 分析影响范围。

## i18n Rules

- 前端 i18n 入口：`admin-ui/src/i18n/index.ts`。
- 前端 locale 文件：`admin-ui/src/locales/zh_CN.ts`、`admin-ui/src/locales/en_US.ts`。
- 前端语言切换通过 `admin-ui/src/stores/locale.ts` 的 `setLocale` / `setLanguage`。
- 后端 message 文件：`bocoo-admin/src/main/resources/i18n/messages*.properties`。
- 后端 locale resolver：`bocoo-common/bocoo-common-web/src/main/java/com/bocoo/common/web/core/I18nLocaleResolver.java`。
- 菜单、字典等数据库配置项优先使用后端 `i18n_key` / `sys_i18n_message`。
- 后端菜单和字典运行时由 `SysI18nMessageService` 按当前请求 locale 查询 `sys_i18n_message`，未命中时回退到原始名称/标签。
- i18n 现代化已完成前端静态 locale、后端 MessageSource、菜单/字典 DB i18n 等主链路。代码生成 SQL 会写入菜单、按钮、页面和字段的 `sys_i18n_message` key；当前真实代码中，后端会用 `SysI18nMessageService` 翻译菜单/字典，生成页面模板自身仍调用前端 `getMessage()`，而 `getMessage()` 只读取本地 `zh_CN.ts` / `en_US.ts`，未发现前端运行时加载 `sys_i18n_message` 的 API/loader。该边界与 `.ai/archive/i18n-modernization.md` 中 Generator 相关 deferred 项一致。

## Time / UTC Rules

- PostgreSQL 使用 UTC 语义；`application-dev.yml` 的 Hikari `connectionInitSql` 设置 `SET TIME ZONE 'UTC'`。
- 后端业务/审计当前时间使用 `TimeUtils.utcNow()`，不要直接使用 `LocalDateTime.now()`。
- MyBatis `LocalDateTime` 使用 `UtcLocalDateTimeTypeHandler` 处理 UTC 语义。
- Jackson/Redis 已有显式 UTC 配置记录；修改前需读取真实源文件确认。
- 前端展示后端时间使用 `formatUtc()`，提交绝对时间使用 UTC payload helper。
- 后端本地 RUN 和生产 Docker Compose 均设置 JVM `-Duser.timezone=UTC`；Compose 设置 `TZ=UTC`，且不挂载宿主机 `/etc/localtime`，避免宿主时区影响容器。

## Important Files

- `AGENTS.md`
- `.ai/RULES.md`
- `.ai/CONTEXT.md`
- `.ai/CURRENT.md`
- `.ai/MEMORY.md`
- `pom.xml`
- `bocoo-admin/pom.xml`
- `bocoo-admin/src/main/resources/application.yml`
- `bocoo-admin/src/main/resources/application-dev.yml`
- `admin-ui/package.json`
- `admin-ui/vite.config.ts`
- `admin-ui/src/i18n/index.ts`
- `admin-ui/src/utils/request.ts`
- `admin-ui/src/utils/datetime.ts`

## CodeGraph Notes

项目已安装 codegraph，可用于影响范围分析、符号定位、调用关系和文件结构初筛；不要在 CONTEXT.md 保存详细调用图、完整文件树或大量符号图。codegraph 结果只作为定位依据，最终结论和修改必须结合真实项目文件确认。

## Unknowns

- TODO：确认 CI 配置是否存在于外部平台；当前未在仓库常见路径发现。
- TODO：确认生产环境配置管理细节；不要输出或复制任何密钥、密码、Token、连接串。
