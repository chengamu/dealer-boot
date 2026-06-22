# Nginx 同源代理部署说明

生产和 Docker Compose 部署推荐使用 Nginx 做同源代理：

```text
http(s)://your-domain/
        ├─ 前端静态资源：Nginx
        └─ /dev-api/*：Nginx 反向代理到 bocoo-admin
```

## 前端运行时配置

`admin-ui/public/_app.config.js` 保持相对 API 前缀：

```js
window.__APP_CONFIG__ = {
  VITE_APP_BASE_API: '/dev-api'
}
```

浏览器可见配置不写 Docker Compose 服务名。服务名只给容器内部访问使用。

## Nginx 代理规则

前端镜像使用 `admin-ui/nginx.conf.template`，`/dev-api/` 代理到 `API_PROXY_PASS`：

```nginx
location ^~ /dev-api/ {
    proxy_pass ${API_PROXY_PASS}/;
    proxy_http_version 1.1;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}
```

```env
API_PROXY_PASS=http://bocoo-admin:8081
```

请求流向：

```text
浏览器 -> https://your-domain/dev-api/getInfo
Nginx  -> http://bocoo-admin:8081/getInfo
后端   -> API 响应
```

## Docker Compose

同一个 compose 网络下使用服务名互通：

```yaml
services:
  bocoo-admin:
    image: your-bocoo-admin-image
    restart: always
    environment:
      SPRING_PROFILES_ACTIVE: prod
      TZ: UTC
    expose:
      - "8081"

  admin-ui:
    image: your-admin-ui-image
    restart: always
    ports:
      - "80:80"
    environment:
      API_PROXY_PASS: http://bocoo-admin:8081
    depends_on:
      - bocoo-admin
```

## 配置要求

- 外部只暴露前端 Nginx；后端只在 compose 网络内可见。
- HTTPS 可在同一个 Nginx 容器或前置网关配置。
- 上传接口走 `/dev-api/system/oss/**`，按业务设置 `client_max_body_size`。
- 后端日志中的客户端 IP 依赖 Nginx 正确传递 `X-Forwarded-For`。
- WebSocket 需要公网访问时，也通过 Nginx 代理同源路径。
