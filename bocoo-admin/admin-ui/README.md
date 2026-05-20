# zjbm-mes-vue

MES 后台管理系统前端，基于 `Vue + Vite + Element Plus` 构建，包含后台管理、数据展示、表单配置和打印等能力。

## 技术栈

- Vue 3
- Vite 3
- Element Plus
- Pinia
- Vue Router
- Axios
- ECharts
- vue-plugin-hiprint
- jsbarcode / qrcode

## 本地开发

```bash
npm install
npm run dev
```

## 构建命令

```bash
npm run build
npm run build:prod
npm run preview
npm run generate:config
```

## 项目结构

- `src/`: 页面、组件、路由、状态管理和业务逻辑
- `public/`: 静态资源
- `build/`: 构建脚本和配置生成脚本
- `bin/`: 启动或辅助脚本
- `html/`: 额外页面模板
- `vite/`: Vite 配置扩展
- `.env.development`: 开发环境配置
- `.env.production`: 生产环境配置

## 说明

建议后续补充模块清单、权限体系、接口约定和部署方式。