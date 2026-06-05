# Nginx 同源代理部署说明

## 推荐结论

后期 Docker Compose / 生产部署推荐使用 Nginx 做同源代理：

```text
浏览器访问同一个 Origin
http(s)://your-domain/
        ├─ 前端静态资源：Nginx 直接返回
        └─ /dev-api/*：Nginx 反向代理到 bocoo-admin
```

这样浏览器看到的前端和 API 都来自同一个 `scheme + host + port`，通常不触发 CORS。后端的 CORS 白名单仍保留本地开发兜底，但生产访问主要依赖 Nginx 同源代理。

## 为什么这样做

- 减少 CORS 配置复杂度，避免前端域名、API 域名不一致导致浏览器拦截。
- 后端服务不直接暴露给公网，只由 Nginx 转发。
- 前端运行时配置可以保持相对路径 `/dev-api`，部署时不需要把 API 域名写进前端包。
- Docker Compose 内部可以用服务名访问后端，例如 `http://bocoo-admin:8081`；浏览器不会看到这个内部地址。

## 前端运行时配置

`admin-ui/public/_app.config.js` 中保持：

```js
window.__APP_CONFIG__ = {
  VITE_APP_BASE_API: '/dev-api'
}
```

不要在前端配置里写 Docker Compose 服务名，例如 `http://bocoo-admin:8081`。服务名只给容器内部使用，浏览器无法解析。

## Nginx 代理规则

当前前端镜像已使用 `admin-ui/nginx.conf.template`，其中 `/dev-api/` 会代理到 `API_PROXY_PASS`：

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

示例：

```env
API_PROXY_PASS=http://bocoo-admin:8081
```

请求流向：

```text
浏览器: GET https://your-domain/dev-api/getInfo
Nginx:  转发到 http://bocoo-admin:8081/getInfo
后端:   返回 API 响应
```

## Docker Compose 示例

同一个 compose 网络下，不建议使用 `network_mode: host`；推荐使用服务名互通：

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

如果要使用 HTTPS，建议在 Nginx 前再接入证书配置，或把 443 暴露到同一个 Nginx 容器。外部仍只暴露前端 Nginx，后端只在 compose 内网可见。

## CORS 和域名的关系

CORS 检查的是浏览器地址栏里前端页面的 Origin，不是 Docker Compose 服务名。

需要 CORS 的情况：

```text
前端：https://web.example.com
后端：https://api.example.com
```

不需要或很少依赖 CORS 的推荐情况：

```text
前端：https://example.com/
后端：https://example.com/dev-api/
```

当前后端 `web.cors.allowed-origin-patterns` 默认保留 localhost / 127.0.0.1，主要服务本地开发。生产同源代理模式下，可以保持默认或按需配置正式前端 Origin，但不要配置 `* + allow-credentials=true`。

## 本地开发影响

本地开发不受影响：

- Vite dev server 访问后端时，仍可通过本地 CORS 默认值或 Vite proxy 工作。
- 访问 `http://localhost:5173`、`http://127.0.0.1:5173` 等本地来源时，后端默认允许。
- 后期 Docker Compose 同源代理与本地开发是两套入口，不冲突。

## 注意事项

- 上传接口走 `/dev-api/system/oss/**`，需要保留 `client_max_body_size 100m` 或按业务调整。
- WebSocket 如果走公网，也应通过 Nginx 代理同源路径；当前 WebSocket 端口和路径需单独确认后再收敛。
- 后端日志中的 `X-Forwarded-For` 依赖 Nginx 正确传递请求头。
- 如果将来启用 Cookie/CSRF，同源代理是更合适的前置条件。
