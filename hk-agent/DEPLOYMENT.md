# HK Agent 部署与升级记录

本文记录 31dev 阿里云服务器的 Docker Compose 部署方式，方便后续复用。

## 服务器信息

- 服务器：31dev
- SSH 用户：`root`
- 服务端口：`8493`
- 应用目录：`/opt/hk-agent/app`
- 外置配置目录：`/opt/hk-agent/config`
- 外置数据目录：`/opt/hk-agent/data/reports`
- Docker Hub 镜像加速器：`https://nqlxl8gh.mirror.aliyuncs.com`

## 目录规划

```text
/opt/hk-agent/
  app/              # 应用代码和 docker-compose.yml
  config/.env       # 运行环境变量，root 600 权限，不进镜像
  data/reports/     # 报告 JSON 数据，挂载到容器 /data/reports
```

数据和密钥都放在宿主机外部目录，重建容器或同步代码不会删除：

```text
/opt/hk-agent/config/.env
/opt/hk-agent/data/reports
```

## 首次部署

### 1. 安装 Docker 和 Compose

Alibaba Cloud Linux 3 可使用 Docker CE 的 CentOS/RHEL 兼容仓库。如果 Docker 官方源下载不稳定，优先使用阿里云镜像源：

```bash
dnf config-manager --add-repo https://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo
dnf clean metadata
dnf install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
systemctl enable --now docker
```

验证：

```bash
docker --version
docker compose version
systemctl is-enabled docker
systemctl is-active docker
```

### 2. 配置 Docker 镜像加速和日志限额

确认没有已有配置或先合并现有配置，避免覆盖其他 Docker 设置：

```bash
test -f /etc/docker/daemon.json && cat /etc/docker/daemon.json || echo NO_DAEMON_JSON
docker ps
```

写入配置：

```bash
mkdir -p /etc/docker
cat >/etc/docker/daemon.json <<'EOF'
{
  "registry-mirrors": ["https://nqlxl8gh.mirror.aliyuncs.com"],
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "100m",
    "max-file": "3"
  }
}
EOF
systemctl daemon-reload
systemctl restart docker
docker info --format '{{json .RegistryConfig.Mirrors}} {{.LoggingDriver}}'
```

### 3. 创建部署目录

```bash
mkdir -p /opt/hk-agent/app /opt/hk-agent/config /opt/hk-agent/data/reports
chmod 755 /opt/hk-agent /opt/hk-agent/app /opt/hk-agent/data /opt/hk-agent/data/reports
chmod 700 /opt/hk-agent/config
```

### 4. 上传 `.env`

本地 `.env` 不进入镜像，不跟代码同步，单独上传到服务器：

```bash
scp -i ~/.ssh/hk_agent_31dev_rsa .env root@47.110.72.31:/opt/hk-agent/config/.env
ssh -i ~/.ssh/hk_agent_31dev_rsa root@47.110.72.31 \
  "chmod 600 /opt/hk-agent/config/.env && chown root:root /opt/hk-agent/config/.env"
```

不要在终端、日志或文档中输出 `.env` 内容。

### 5. 同步代码

推荐用 `rsync`，排除敏感和本地副产物：

```bash
rsync -az --delete \
  --exclude .git \
  --exclude __pycache__ \
  --exclude .pytest_cache \
  --exclude .venv \
  --exclude web/frontend/node_modules \
  --exclude '*.pyc' \
  --exclude .DS_Store \
  --exclude .env \
  --exclude '.env.*' \
  --exclude '._*' \
  -e "ssh -i ~/.ssh/hk_agent_31dev_rsa -o IdentitiesOnly=yes -o StrictHostKeyChecking=no" \
  ./ root@47.110.72.31:/opt/hk-agent/app/
```

### 6. 构建镜像

如果服务器能正常拉取基础镜像：

```bash
ssh -i ~/.ssh/hk_agent_31dev_rsa root@47.110.72.31 \
  "cd /opt/hk-agent/app && docker compose build"
```

如果服务器访问 Docker Hub 超时，可在本机构建 `linux/amd64` 镜像后传到服务器：

```bash
docker buildx build --platform linux/amd64 -t hk-agent:latest --load .
docker image inspect hk-agent:latest --format '{{.Architecture}} {{.Os}} {{.Size}}'
docker save hk-agent:latest -o /tmp/hk-agent-image.tar
scp -i ~/.ssh/hk_agent_31dev_rsa /tmp/hk-agent-image.tar root@47.110.72.31:/tmp/hk-agent-image.tar
ssh -i ~/.ssh/hk_agent_31dev_rsa root@47.110.72.31 \
  "docker load -i /tmp/hk-agent-image.tar && rm -f /tmp/hk-agent-image.tar"
```

### 7. 启动服务

```bash
ssh -i ~/.ssh/hk_agent_31dev_rsa root@47.110.72.31 \
  "cd /opt/hk-agent/app && docker compose up -d --no-build"
```

验证：

```bash
ssh -i ~/.ssh/hk_agent_31dev_rsa root@47.110.72.31 \
  "cd /opt/hk-agent/app && docker compose ps && curl -sS http://127.0.0.1:8493/api/agents"
```

公网验证：

```bash
curl -sS http://47.110.72.31:8493/api/agents
```

## 升级流程

### 1. 本地检查

```bash
PYTHONPYCACHEPREFIX=/tmp/hk-agent-pycache python3 -m compileall agents web main.py
```

### 2. 同步代码

使用首次部署中的 `rsync` 命令。它不会同步 `.env`，也不会影响 `/opt/hk-agent/data/reports`。

### 3. 构建并发布镜像

优先本机构建 amd64 镜像，避免服务器拉 Docker Hub 超时：

```bash
docker buildx build --platform linux/amd64 -t hk-agent:latest --load .
docker image inspect hk-agent:latest --format '{{.Architecture}} {{.Os}} {{.Size}}'
docker save hk-agent:latest -o /tmp/hk-agent-image.tar
scp -i ~/.ssh/hk_agent_31dev_rsa /tmp/hk-agent-image.tar root@47.110.72.31:/tmp/hk-agent-image.tar
ssh -i ~/.ssh/hk_agent_31dev_rsa root@47.110.72.31 \
  "docker load -i /tmp/hk-agent-image.tar && rm -f /tmp/hk-agent-image.tar"
```

### 4. 重启服务

```bash
ssh -i ~/.ssh/hk_agent_31dev_rsa root@47.110.72.31 \
  "cd /opt/hk-agent/app && docker compose up -d --no-build --force-recreate"
```

### 5. 验证

```bash
ssh -i ~/.ssh/hk_agent_31dev_rsa root@47.110.72.31 "
  cd /opt/hk-agent/app &&
  docker compose ps &&
  curl -sS http://127.0.0.1:8493/api/agents &&
  docker inspect hk-agent --format '{{range .Mounts}}{{.Source}} -> {{.Destination}} {{.RW}}{{println}}{{end}}' &&
  df -h /opt/hk-agent /var/lib/docker
"
```

成功标准：

- `hk-agent` 状态为 `Up`
- `http://127.0.0.1:8493/api/agents` 返回 `{"agents":["fabric_cutting"]}`
- 挂载包含 `/opt/hk-agent/data/reports -> /data/reports true`
- 磁盘空间仍有充足余量

## 修改 `.env`

编辑服务器外置配置：

```bash
vi /opt/hk-agent/config/.env
cd /opt/hk-agent/app
docker compose up -d --force-recreate
```

只检查变量是否存在，不打印值：

```bash
docker exec hk-agent python -c 'import os; keys=["DS_API_KEY","DS_BASE_URL","DS_MODEL","DEEPSEEK_API_KEY","OPENAI_API_KEY","OPENAI_BASE_URL","OPENAI_MODEL","HK_AGENT_REPORT_DIR"]; print("\n".join(f"{k}={'set' if os.getenv(k) else 'unset'}" for k in keys))'
```

## 清理规则

可以清理：

```bash
rm -f /tmp/hk-agent-image.tar /tmp/hk-agent-deploy.tgz
docker builder prune -f
```

不要清理：

```text
/opt/hk-agent/app/docker-compose.yml
/opt/hk-agent/config/.env
/opt/hk-agent/data/reports
hk-agent:latest
hk-agent 容器
```

清理后复查：

```bash
cd /opt/hk-agent/app
docker compose ps
curl -sS http://127.0.0.1:8493/api/agents
find /tmp -maxdepth 1 \( -name 'hk-agent*' -o -name 'hk_agent*' \) -print
find /opt/hk-agent/app -maxdepth 2 \( -name '._*' -o -name '.DS_Store' -o -name '.env*' \) -print
df -h /opt/hk-agent /var/lib/docker
```

## 常用命令

```bash
cd /opt/hk-agent/app
docker compose ps
docker compose logs -f hk-agent
docker compose restart hk-agent
docker compose down
docker compose up -d --no-build
```

`docker compose down` 会停止并删除容器和 Compose 网络，但不会删除 `/opt/hk-agent/data/reports` 和 `/opt/hk-agent/config/.env`。
