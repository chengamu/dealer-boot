# Base Boot 生产部署文档

本文档记录当前阿里云服务器的 Docker 部署方式，方便后续重复部署或升级。

## 当前服务器

- 服务器：`47.110.72.31`
- SSH 用户：`root`
- SSH key：`~/.ssh/hk_agent_31dev_rsa`
- 服务器部署目录：`/opt/base-boot/app`
- 访问入口：`http://47.110.72.31/`
- 后端健康检查：`http://47.110.72.31/dev-api/actuator/health`

## 端口规划

公网只暴露 Web 入口：

```text
80 -> base-boot-nginx -> admin-ui / MinIO object proxy / backend API proxy
```

容器和端口：

```text
base-boot-nginx      0.0.0.0:80 -> 80
admin-ui             127.0.0.1:18080 -> 80
base-boot-minio      127.0.0.1:19000 -> 9000
bocoo-admin          Docker 内网 8081
base-boot-postgres   Docker 内网 5432
base-boot-redis      Docker 内网 6379
```

不要在阿里云安全组开放 `8081`、`5432`、`6379`。MinIO 也不直接开放公网，图片通过 `/oss/` 走 Nginx 代理。

## 服务器目录结构

服务器上主要文件位于：

```text
/opt/base-boot/app/deploy/base-boot
```

关键文件：

```text
docker-compose.prod.yml              Docker Compose 编排
.env                                 Compose 使用的本地密钥，不提交 git
config/application-prod.yml           Spring Boot 外部生产配置，不提交 git
bocoo-admin.jar                       后端可执行 jar，不提交 git
nginx/base-boot.conf                  入口 Nginx 代理配置
sql/local-minio-oss-config.sql        同步 MinIO OSS 数据库配置，不提交 git
data/postgres                         PostgreSQL 数据目录
data/redis                            Redis 数据目录
data/minio                            MinIO 数据目录
logs                                  后端日志目录
```

`config/application-prod.yml` 是挂载到容器里的外部配置。修改数据库、Redis、JWT、MinIO 等配置时，只改服务器上的这个文件，然后重启后端容器即可，不需要重新打包。

```bash
cd /opt/base-boot/app
docker compose --env-file deploy/base-boot/.env -f deploy/base-boot/docker-compose.prod.yml restart bocoo-admin
```

## 本地打包

在本地仓库根目录执行：

```bash
mvn -pl bocoo-admin -am -DskipTests package
pnpm --dir admin-ui build
```

注意后端要使用 Spring Boot 可执行 jar：

```text
bocoo-admin/target/dist/bocoo-admin.jar
```

不要使用下面这个普通 jar：

```text
bocoo-admin/target/bocoo-admin.jar
```

复制到部署目录：

```bash
cp bocoo-admin/target/dist/bocoo-admin.jar deploy/base-boot/bocoo-admin.jar
```

## 构建前端运行镜像

前端镜像使用本地已构建好的 `admin-ui/dist`，不在服务器上跑 `pnpm install`。

```bash
rm -rf /tmp/base-boot-admin-ui-image
mkdir -p /tmp/base-boot-admin-ui-image
cp -R admin-ui/dist /tmp/base-boot-admin-ui-image/dist
cp admin-ui/nginx.conf.template /tmp/base-boot-admin-ui-image/nginx.conf.template
cp deploy/base-boot/admin-ui-runtime.Dockerfile /tmp/base-boot-admin-ui-image/Dockerfile

docker buildx build \
  --platform linux/amd64 \
  -t base-boot-admin-ui:latest \
  --load \
  /tmp/base-boot-admin-ui-image
```

## 服务器拉镜像失败时

服务器如果拉 Docker Hub 超时，从本机保存 `linux/amd64` 镜像并传到服务器。

当前需要的镜像：

```text
eclipse-temurin:17-jre
nginx:1.27-alpine
postgres:16-alpine
redis:7-alpine
minio/minio:latest
base-boot-admin-ui:latest
```

本机准备镜像：

```bash
docker pull --platform linux/amd64 eclipse-temurin:17-jre
docker pull --platform linux/amd64 nginx:1.27-alpine
docker pull --platform linux/amd64 postgres:16-alpine
docker pull --platform linux/amd64 redis:7-alpine
docker pull --platform linux/amd64 minio/minio:latest
```

保存镜像：

```bash
mkdir -p /tmp/base-boot-image-tars
docker save --platform linux/amd64 eclipse-temurin:17-jre -o /tmp/base-boot-image-tars/eclipse-temurin-17-jre.tar
docker save --platform linux/amd64 nginx:1.27-alpine -o /tmp/base-boot-image-tars/nginx-1.27-alpine.tar
docker save --platform linux/amd64 postgres:16-alpine -o /tmp/base-boot-image-tars/postgres-16-alpine.tar
docker save --platform linux/amd64 redis:7-alpine -o /tmp/base-boot-image-tars/redis-7-alpine.tar
docker save --platform linux/amd64 minio/minio:latest -o /tmp/base-boot-image-tars/minio-latest.tar
docker save --platform linux/amd64 base-boot-admin-ui:latest -o /tmp/base-boot-image-tars/base-boot-admin-ui.tar
```

传到服务器：

```bash
ssh -i ~/.ssh/hk_agent_31dev_rsa root@47.110.72.31 'mkdir -p /tmp/base-boot-image-tars'
scp -i ~/.ssh/hk_agent_31dev_rsa /tmp/base-boot-image-tars/*.tar root@47.110.72.31:/tmp/base-boot-image-tars/
```

服务器加载并删除临时 tar：

```bash
ssh -i ~/.ssh/hk_agent_31dev_rsa root@47.110.72.31 '
  set -e
  for f in /tmp/base-boot-image-tars/*.tar; do
    echo "loading $(basename "$f")"
    docker load -i "$f" >/dev/null
  done
  rm -rf /tmp/base-boot-image-tars
'
```

## 同步代码到服务器

从本地仓库根目录同步：

```bash
rsync -az --delete \
  --exclude .git \
  --exclude '**/node_modules' \
  --exclude '**/target' \
  --exclude '.DS_Store' \
  --exclude '._*' \
  --exclude '.idea' \
  --exclude '.vscode' \
  -e "ssh -i ~/.ssh/hk_agent_31dev_rsa -o IdentitiesOnly=yes -o StrictHostKeyChecking=no" \
  ./ root@47.110.72.31:/opt/base-boot/app/
```

如果只更新 jar，也可以单独传：

```bash
scp -i ~/.ssh/hk_agent_31dev_rsa deploy/base-boot/bocoo-admin.jar root@47.110.72.31:/opt/base-boot/app/deploy/base-boot/bocoo-admin.jar
```

## 首次启动

服务器执行：

```bash
cd /opt/base-boot/app
docker compose --env-file deploy/base-boot/.env -f deploy/base-boot/docker-compose.prod.yml up -d --no-build
```

首次数据库初始化：

```bash
cd /opt/base-boot/app

docker compose --env-file deploy/base-boot/.env -f deploy/base-boot/docker-compose.prod.yml exec -T postgres \
  sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"' < sql/postgresql/base.sql

docker compose --env-file deploy/base-boot/.env -f deploy/base-boot/docker-compose.prod.yml exec -T postgres \
  sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"' < sql/postgresql/product_capability.sql

docker compose --env-file deploy/base-boot/.env -f deploy/base-boot/docker-compose.prod.yml exec -T postgres \
  sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"' < deploy/base-boot/sql/local-minio-oss-config.sql
```

如果启用支付模块，再执行：

```bash
docker compose --env-file deploy/base-boot/.env -f deploy/base-boot/docker-compose.prod.yml exec -T postgres \
  sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"' < sql/postgresql/pay.sql
```

初始化完成后重启后端：

```bash
docker compose --env-file deploy/base-boot/.env -f deploy/base-boot/docker-compose.prod.yml restart bocoo-admin
```

## 日常更新

只更新后端代码：

```bash
mvn -pl bocoo-admin -am -DskipTests package
cp bocoo-admin/target/dist/bocoo-admin.jar deploy/base-boot/bocoo-admin.jar
scp -i ~/.ssh/hk_agent_31dev_rsa deploy/base-boot/bocoo-admin.jar root@47.110.72.31:/opt/base-boot/app/deploy/base-boot/bocoo-admin.jar

ssh -i ~/.ssh/hk_agent_31dev_rsa root@47.110.72.31 '
  cd /opt/base-boot/app
  docker compose --env-file deploy/base-boot/.env -f deploy/base-boot/docker-compose.prod.yml restart bocoo-admin
'
```

只更新前端代码：

```bash
pnpm --dir admin-ui build

rm -rf /tmp/base-boot-admin-ui-image
mkdir -p /tmp/base-boot-admin-ui-image
cp -R admin-ui/dist /tmp/base-boot-admin-ui-image/dist
cp admin-ui/nginx.conf.template /tmp/base-boot-admin-ui-image/nginx.conf.template
cp deploy/base-boot/admin-ui-runtime.Dockerfile /tmp/base-boot-admin-ui-image/Dockerfile

docker buildx build --platform linux/amd64 -t base-boot-admin-ui:latest --load /tmp/base-boot-admin-ui-image
docker save --platform linux/amd64 base-boot-admin-ui:latest -o /tmp/base-boot-admin-ui.tar

scp -i ~/.ssh/hk_agent_31dev_rsa /tmp/base-boot-admin-ui.tar root@47.110.72.31:/tmp/base-boot-admin-ui.tar

ssh -i ~/.ssh/hk_agent_31dev_rsa root@47.110.72.31 '
  set -e
  docker load -i /tmp/base-boot-admin-ui.tar >/dev/null
  rm -f /tmp/base-boot-admin-ui.tar
  cd /opt/base-boot/app
  docker compose --env-file deploy/base-boot/.env -f deploy/base-boot/docker-compose.prod.yml up -d --no-build --force-recreate admin-ui nginx
'
```

## 验证

服务器本机验证：

```bash
cd /opt/base-boot/app
docker compose --env-file deploy/base-boot/.env -f deploy/base-boot/docker-compose.prod.yml ps

curl -I http://127.0.0.1/
curl -sS http://127.0.0.1/dev-api/actuator/health
curl -I http://127.0.0.1/oss/
```

本机公网验证：

```bash
curl -I http://47.110.72.31/
curl -sS http://47.110.72.31/dev-api/actuator/health
```

期望：

```text
首页返回 HTTP 200
/dev-api/actuator/health 返回 {"status":"UP"}
PostgreSQL 和 Redis 状态为 healthy
bocoo-admin 状态为 Up
```

## 查看日志

```bash
cd /opt/base-boot/app
docker compose --env-file deploy/base-boot/.env -f deploy/base-boot/docker-compose.prod.yml logs --tail=120 bocoo-admin
docker compose --env-file deploy/base-boot/.env -f deploy/base-boot/docker-compose.prod.yml logs --tail=120 nginx
```

## 常见问题

### 后端提示 no main manifest attribute

用了错误的 jar。必须使用：

```text
bocoo-admin/target/dist/bocoo-admin.jar
```

### 后端提示 mybatis-plus.mapperPackage 缺失

不要使用：

```text
--spring.config.location=/config/
```

当前 compose 使用的是：

```text
--spring.config.additional-location=/config/
```

这样 jar 内置 `application.yml` 不会被外部配置覆盖掉。

### 服务器拉镜像超时

按“服务器拉镜像失败时”章节，用本机 `docker save`，服务器 `docker load`。

### 图片访问不了

检查数据库 `sys_oss_config`：

```text
endpoint = minio:9000
domain = 47.110.72.31/oss
is_https = N
```

`endpoint` 给后端容器访问 MinIO，`domain` 给浏览器访问图片。

### WebSocket 功能失败

当前后端 WebSocket 在容器内监听 `2831`，还没有通过公网 Nginx 代理。页面如需实时功能，再给 `base-boot-nginx` 增加 WebSocket 路径代理，不要直接开放公网端口。

## 停止和重启

停止：

```bash
cd /opt/base-boot/app
docker compose --env-file deploy/base-boot/.env -f deploy/base-boot/docker-compose.prod.yml stop
```

启动：

```bash
cd /opt/base-boot/app
docker compose --env-file deploy/base-boot/.env -f deploy/base-boot/docker-compose.prod.yml up -d --no-build
```

不要随手执行 `down -v`，它会删除 volume。当前数据虽然挂在本地目录，但生产操作仍建议避免破坏性命令。
