# Code Review Report — 健康大脑创新药管理模块

**Date**: 2026-05-30  
**Branch**: main  
**Scope**: `bocoo-modules-dealer` 新增模块 + `bocoo-module-pay` 脚手架 + pom 变更  
**Reviewer**: Claude Code (deepseek-v4-pro)

---

## 一、Blocker（阻塞合并）

无。

---

## 二、High（强烈建议修复）

### H1. `updateStatus()` 死方法 — 总是返回 0

- **文件**: `HealthbrainDrugServiceImpl.java:1061` / `IHealthbrainDrugService.java:94`
- **问题**: 方法实现直接 `return 0`，注释写明"实体类中没有 status 字段"。调用者会误以为更新成功。
- **风险**: 前端或其他 service 调用后静默失败。
- **修复**: 从接口和实现中删除 `updateStatus` 方法；如果后续需要，等 entity 加了 status 字段再实现。

### H2. `selectDrugPhaseCount` SQL — `COUNT(a.phase)` 忽略 NULL

- **文件**: `HealthbrainDrugMapper.xml:10`
- **问题**: `COUNT(column)` 不统计 NULL 值。`phase` 为 NULL 的药物记录会被静默排除。
- **风险**: 统计数字与实际记录数不一致。
- **修复**: 改为 `count(*)`。

### H3. `/healthbrain/drug/alllist` — 无分页

- **文件**: `HealthbrainDrugController.java:72-77`
- **问题**: 返回全量 `List<HealthbrainDrugVo>`，无分页、无 limit。
- **风险**: 数据量大时 OOM / 响应超时。
- **修复**: 添加分页参数，或至少加 `LIMIT 1000`。

---

## 三、Medium

### M1. `buildQueryWrapper` 中的死代码

- **文件**: `HealthbrainDrugServiceImpl.java:1032`
- **问题**: `Map<String, Object> params = bo.getParams();` 赋值后从未使用。
- **修复**: 删除该行。

### M2. 未使用的 imports（多处）

| 文件 | 未使用的 import |
|---|---|
| `HealthbrainDrugServiceImpl.java` | `ObjectUtil`, `LambdaUpdateWrapper`, `Page`, `ServiceException`, `HealthbrainEnterprise` |
| `HealthbrainDrug.java` | `JsonFormat`, `LocalDateTime` |
| `HealthbrainDrugBo.java` | `LocalDateTime` |
| `HealthbrainDrugVo.java` | `BaseEntity` |

`HealthbrainEnterprise` import 尤其可疑，暗示代码是从其他模块复制过来的。

### M3. 复制粘贴的 JavaDoc

- **文件**: `IHealthbrainDrugService.java:94`
- **问题**: `updateStatus` 的 JavaDoc 写的是"修改设备分组状态"（来自设备分组模块）。
- **修复**: 与 H1 一起删除。

### M4. `validEntityBeforeSave()` 和 `deleteWithValidByIds()` 无实际校验

- **文件**: `HealthbrainDrugServiceImpl.java:1006-1008`, `1017-1022`
- **问题**: 保存前无唯一性校验（如 drug name 重复）；删除前无关联检查（如是否有子表引用）。
- **修复**: 
  - `validEntityBeforeSave`: 增加 name 唯一性检查
  - `deleteWithValidByIds`: 确认无外键依赖再删，或改为逻辑删除

### M5. `bocoo-modules-dealer/pom.xml` — artifactId / 目录名 / description 三者不一致

- **文件**: `bocoo-modules-dealer/pom.xml`
- **问题**: 
  - 目录名: `bocoo-modules-dealer`
  - `<artifactId>`: `bocoo-modules-healthbrain`
  - `<description>`: `订单模块`
  - 实际类名: `HealthbrainDrug*`（健康大脑 - 创新药）
- **修复**: 统一命名。建议 `<artifactId>` 改为 `bocoo-modules-dealer`，`<description>` 改为"订单模块"。

---

## 四、Low

### L1. `enterpriseId` 命名与注释不一致

- **文件**: `HealthbrainDrugBo.java:37`, `HealthbrainDrug.java:38`, `HealthbrainDrugVo.java:42`
- **问题**: 字段名是 `enterpriseId`（暗示外键ID），注释写"企业名称"。
- **修复**: 确认数据库列实际含义，统一命名和注释。

### L2. Entity 缺少 `@TableId`

- **文件**: `HealthbrainDrug.java`
- **问题**: `id` 字段没有 `@TableId` 注解，主键策略依赖 MyBatis-Plus 自动推断。
- **修复**: 添加 `@TableId(type = IdType.AUTO)`（若是自增主键）。

---

## 五、POM 编码检查（IDE 中文乱码）

### 根 pom.xml — 中文注释已损坏（预存问题）

- **文件**: `pom.xml`（项目根）
- **编码声明**: `<?xml version="1.0" encoding="UTF-8"?>`
- **实际编码**: UTF-8 without BOM
- **问题**: 文件中多处中文注释已是乱码字节，非本次 diff 引入：
  - L31: `<!-- 榛樿鐜 -->`（应为 `<!-- 默认环境 -->`）
  - L89: `<!-- 绗笁鏂规湇鍔￠厤缃?-->`（应为 `<!-- 第三方服务配置 -->`）
- **本次 diff 影响**: 新加的支付 SDK version 属性在 L89 乱码注释之后，未新增乱码。
- **IDE 乱码原因**: 文件是 UTF-8 without BOM，且 IDE 可能默认用 GBK 打开 `.xml` 文件。对于已损坏的字节，即使 IDE 正确识别 UTF-8 也无法恢复原文。
- **修复建议**:
  1. 手动将乱码注释替换为正确的中文（或删除这些注释）
  2. 在 IntelliJ IDEA 中设置 `File → Settings → Editor → File Encodings → Default encoding for properties files: UTF-8`，并勾选 `Transparent native-to-ascii conversion`
  3. 新文件统一使用 UTF-8 with BOM 或确保 IDE 正确识别编码

### 其他 POM 文件 — 正常

`bocoo-admin/pom.xml`、`bocoo-modules-dealer/pom.xml`、`bocoo-module-pay/pom.xml` 及所有 `bocoo-common` 子模块 pom 的中文均正常，读取为正确的中文。

---

## 六、i18n 检查

### I1. 无 healthbrain/drug 相关 i18n key

- **检查范围**: 
  - `i18n/locales/zh_CN.json` (1586 keys)
  - `i18n/locales/en_US.json` (1586 keys)
  - `admin-ui/public/i18n/zh_CN.json` (1586 keys)
  - `admin-ui/public/i18n/en_US.json` (1586 keys)
  - `bocoo-admin/src/main/resources/i18n/zh_CN.json` (1586 keys)
  - `bocoo-admin/src/main/resources/i18n/en_US.json` (1586 keys)
- **结果**: 全部没有 `healthbrain`、`drug`、`创新药`、`brain_drug` 等 key。
- **影响**: 低 — 当前没有前端 Vue 页面，后端代码也不使用 i18n 消息查找（Controller 返回的 `@NotEmpty(message = "主键不能为空")` 是硬编码中文）。
- **后续动作**: 当前端页面开发时，需要同步添加 i18n keys（菜单、表格列、表单标签、按钮文案的中英文）。

### I2. `brain_drug_phase` 字典数据的 label 已损坏（预存问题）

- **文件**: `sql/base.sql:327-330`
- **问题**: `sys_dict_data` 中 `brain_drug_phase` 类型的 dict_label 已是乱码：
  ```
  实际存储: '涓村簥鈪犳湡' → 应为 '临床Ⅰ期'
  实际存储: '涓村簥鈪℃湡' → 应为 '临床Ⅱ期'  
  实际存储: '涓村簥鈪㈡湡' → 应为 '临床Ⅲ期'
  实际存储: '涓婂競'     → 应为 '上市'
  ```
- **影响**: `selectDrugPhaseCount` SQL 直接返回 `dict_label`，如果生产数据库也是从该 SQL 初始化的，API `/healthbrain/drug/countByPhase` 返回的阶段名称会是乱码。
- **修复**: 
  1. 在数据库中手动修复 `sys_dict_data` 表 `brain_drug_phase` 类型的 `dict_label`
  2. 修复 `sql/base.sql` 中的 INSERT 语句（但该文件不在本次 diff 范围内）
  3. 考虑将字典标签改为走 i18n key 而非数据库直存中文

### I3. 所有 i18n 文件 triple-duplicated

- **现象**: 三个路径的 `zh_CN.json` 完全相同（83449 bytes, 1586 keys），`en_US.json` 同样。
  - `i18n/locales/` （项目根）
  - `admin-ui/public/i18n/` （前端 public）
  - `bocoo-admin/src/main/resources/i18n/` （后端 resources）
- **风险**: 修改时容易漏同步，导致前后端/构建产物 i18n 不一致。
- **建议**: 确认哪个是 source of truth，其余作为构建时复制产物；或在 CLAUDE.md 中注明 i18n 同步规则。

---

## 七、总结

| 级别 | 数量 | 关键项 |
|---|---|---|
| Blocker | 0 | - |
| High | 3 | 死方法 updateStatus / SQL COUNT 语义 / alllist 无分页 |
| Medium | 5 | 死代码 params / unused imports / 复制粘贴注释 / 空校验方法 / artifactId 不一致 |
| Low | 2 | enterpriseId 命名 / 缺 @TableId |
| POM 编码 | 1 | 根 pom.xml 中文已损坏（预存） |
| i18n | 3 | 无模块 i18n key / 字典数据乱码（预存） / 三份 i18n 重复 |

**结论**: 无阻塞问题，**可以合并**。建议优先修 H1-H3，M4 补校验逻辑，其余可在后续迭代中处理。
