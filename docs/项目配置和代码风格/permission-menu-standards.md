# 菜单和权限规范

菜单、按钮、路由、权限必须以数据库 `sys_menu` 为权威，不允许靠前端硬编码兜底长期运行。

## 1. sys_menu 类型

| 类型 | `menu_type` | 用途 | 要求 |
| --- | --- | --- | --- |
| 目录 | `M` | 一级菜单分组 | `component` 通常为空，`path` 是分组路径 |
| 菜单 | `C` | 可进入的二级页面 | 必须有稳定 `path`、`component`、`perms` |
| 按钮 | `F` | 页面内操作权限 | `path='#'`，`component=''`，必须挂父菜单 |

普通页面必须通过 `C` 菜单进入，不能只写前端静态路由。页面内新增、修改、删除、导出、审核、发布、重试等动作必须建 `F` 按钮权限。

## 2. 权限码命名

普通 grid 权限建议：

```text
<module>:<business>:list
<module>:<business>:query
<module>:<business>:add
<module>:<business>:edit
<module>:<business>:remove
<module>:<business>:export
```

特殊动作单独命名：

```text
<module>:<business>:approve
<module>:<business>:reject
<module>:<business>:publish
<module>:<business>:check
<module>:<business>:retrySync
<module>:<business>:test
<module>:<business>:bind
<module>:<business>:upload
```

不要用一个泛化 `audit` 同时代表通过和拒绝；如果页面上是两个独立按钮，数据库也要拆成两个按钮权限，后端接口也要拆对应权限。

## 3. 三方一致

每个按钮必须同时满足：

| 位置 | 示例 |
| --- | --- |
| 数据库 | `sys_menu.perms = 'system:tenant:application:approve'` |
| 前端 | `v-hasPermi="['system:tenant:application:approve']"` |
| 后端 | `@SaCheckPermission("system:tenant:application:approve")` |

缺一不可：

- 数据库没有按钮：角色无法授权，非超级管理员看不到按钮。
- 前端权限没写：按钮可能越权显示。
- 后端权限没写：接口可能被越权调用。
- 三方权限码不一致：页面显示、接口调用和角色授权会互相冲突。

## 4. 菜单路径和刷新

- 后端 `sys_menu.path`、`component` 必须和前端动态路由映射一致。
- 前端 `permission.ts` 组件映射必须覆盖后端 `component`。
- 刷新页面不能 404；不能依赖临时 alias 或前端兜底长期解决菜单路径错误。
- 侧边栏层级最多两级；高频主功能独立成二级菜单，不藏在页面 tabs。

## 5. i18n 和排序

- 菜单和按钮建议写 `i18n_key`，显示名由 i18n JSON 提供。
- 中文一级、二级菜单优先使用四个字；确有成熟业务术语时可以例外，但同一业务域必须保持命名节奏一致。
- 新增菜单或按钮 i18n key 要写入对应业务模块的 `i18n/source/<module>/{en_US,zh_CN}.json`，再运行 `pnpm i18n` 生成 `i18n/locales/*.json` 和运行时 JSON。
- `i18n/locales/*.json`、`admin-ui/public/i18n/*.json`、`bocoo-admin/src/main/resources/i18n/*.json` 都是生成产物，不手工修改。
- 同一父菜单下 `order_num` 要稳定。
- 按钮顺序通常为 query、add、edit、remove、export，再放业务动作。

## 6. 角色授权和落库校验

菜单 SQL 或数据补丁必须处理：

- `sys_menu` 目录、菜单、按钮。
- 管理员或目标角色的 `sys_role_menu` 授权。
- 已存在菜单的 `ON CONFLICT` / upsert 或等价幂等更新。
- `tenant_id`、`visible`、`status`、`is_frame`、`is_cache` 等字段符合当前项目口径。

落库后至少检查：

```sql
select menu_id, menu_name, parent_id, order_num, menu_type, path, component, perms
from sys_menu
where perms like '<module>:<business>:%'
order by parent_id, order_num;
```

页面还要验证：

- 普通用户无按钮权限时不显示按钮。
- 有按钮权限时按钮显示，接口不 403。
- 页面刷新不 404。
- 菜单管理、角色授权里能看到对应按钮。

## 7. 菜单图标

- 每个可见目录和菜单必须配置与业务含义匹配的本地 SVG 图标，不允许长期使用不存在的图标名或空白占位。
- `sys_menu.icon` 必须能在 `admin-ui/src/assets/icons/svg/` 找到同名 `<icon>.svg`；初始化 SQL、菜单管理和前端资源必须保持一致。
- 新增菜单前先复用项目已有图标，禁止仅因命名不同重复创建相似图标。
- 确实没有合适图标时，可以使用 Imagegen 生成造型参考，再按项目现有线性图标规范重绘或矢量化为 SVG；运行时不得直接把生成的位图当作菜单图标。
- 最终 SVG 应使用一致的画布、线宽、留白和单色 `currentColor` 语义，不携带文字、背景、渐变、阴影或外部字体。
- 菜单变更必须执行自动扫描：收集 SQL 与当前数据库菜单中的非空 `icon`，逐项验证对应 SVG 文件存在。扫描失败时不得完成菜单验收。
- 人工验证至少覆盖展开态、选中态、浅色主题和无权限隐藏，不能只检查文件存在。
