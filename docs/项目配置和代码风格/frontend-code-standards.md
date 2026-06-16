# 前端代码规范

## 1. 页面结构

普通管理页面优先对齐系统管理下的角色、菜单、参数配置等标准页面。

```text
app-container
  -> el-form inline 查询区
  -> el-row toolbar + right-toolbar
  -> el-table / tree-table
  -> pagination
  -> el-drawer 表单，短操作可用 el-dialog
```

必须遵守：

- 页面目录优先放在 `admin-ui/src/pages/<domain>/`。
- API 封装放在 `admin-ui/src/api/<domain>/`，页面里不直接写请求 URL。
- 动态路由组件必须补到 `admin-ui/src/stores/permission.ts` 映射。
- 菜单、按钮、表头、placeholder、tooltip、空状态、错误提示使用 i18n key。
- 按钮使用 `v-hasPermi`，权限码和后端 `@SaCheckPermission`、数据库 `sys_menu.perms` 一致。
- 时间展示使用 `formatUtc()`；日期范围提交使用 `withUtcDateRange()` / `withUtcDateRangeParams()`。

## 2. Grid 页面

- 查询区默认不超过两行，字段多时收敛高频搜索项。
- 表格使用 `el-table`，批量操作需要选择列，长文本使用 `show-overflow-tooltip`。
- 标准列顺序：选择列、序号列、业务列、操作列。
- 序号按分页偏移显示，不让用户自己数行。
- 操作列固定右侧，优先使用图标按钮 + `el-tooltip` + `aria-label`。
- 常用操作顺序建议：引用检查、详情、修改、删除、管理子资料。
- 分页使用项目现有 `pagination` 组件。
- 普通 grid 支持双击行打开详情抽屉。
- 业务状态使用开关或明确状态标签，不裸显示 `true/false`、`1/0`、`ENABLED/DISABLED`。
- Boolean 业务字段展示为“是/否”，表单里用 `el-switch`。

## 3. Drawer 和 Dialog

- 新增、编辑、详情、引用检查、复杂预览默认使用右侧 `el-drawer`。
- 抽屉表单要有清晰分组和底部 sticky 操作栏。
- 字段多的页面抽屉宽度建议不小于页面宽度的 80%。
- 删除、启停、强退、清空、同步确认使用 `ElMessageBox.confirm`。
- 审核原因、密码重置、少量输入可以使用小弹窗或 `ElMessageBox.prompt`。
- 图片预览、导入表选择、批量粘贴预览可以使用轻量 dialog。
- 配置编辑器、价格矩阵、发布闸门等复杂页面允许自定义内容区，但不能重做后台外壳。

## 4. 组件和 TypeScript

- 页面 SFC 只负责路由级编排、查询条件、数据加载和动作分发。
- 复杂业务区块优先复用公共组件；没有现成组件时按领域沉淀新组件。
- 通用组件放到 `admin-ui/src/components/` 或现有公共组件目录。
- 只服务单一业务域的组件放到 `admin-ui/src/pages/<domain>/components/`。
- 被两个以上页面复用时再上提为公共组件。
- 组件抽取以可独立理解、可复用或明显降低页面复杂度为准，不为了拆而拆。
- 新增或迁移页面使用 Vue SFC + TypeScript，API 返回值和表单模型要有显式类型。
- 不重新引入 DOM 文案 patch、旧式全局桥接或页面级硬编码翻译。

## 5. 选择器和业务字典

- 下拉选择默认支持模糊过滤。
- 数据量可能增长的选择使用远程搜索、分页或弹出选择器。
- 产品配置中心单位下拉以业务单位表 `pc_unit` 为权威，不使用系统字典 `sys_dict_data.product_unit`。
- 产品业务枚举以下列业务表为权威：`pc_product_dict_type` / `pc_product_dict_item`。
- `sys_dict_*` 只保留后台框架和系统模块自己的底层字典，不承载产品业务枚举。

## 6. 前端验证

新增或调整页面后至少检查：

- `npm run typecheck` 或项目对应 typecheck 命令。
- 页面菜单可进入，刷新不 404。
- 搜索、重置、分页正常。
- 按钮权限可见性符合角色授权。
- 控制台无明显错误。
- 涉及时间展示时，用非中国时区抽查 UTC 显示。
