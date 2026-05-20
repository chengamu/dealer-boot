/*
 Navicat Premium Dump SQL

 Source Server         : bocoo66
 Source Server Type    : MySQL
 Source Server Version : 80028 (8.0.28)
 Source Host           : 192.168.120.66:3306
 Source Schema         : cea

 Target Server Type    : MySQL
 Target Server Version : 80028 (8.0.28)
 File Encoding         : 65001

 Date: 20/05/2026 10:46:51
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for gen_table
-- ----------------------------
DROP TABLE IF EXISTS `gen_table`;
CREATE TABLE `gen_table`  (
  `table_id` bigint NOT NULL COMMENT '编号',
  `table_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '表名称',
  `table_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '表描述',
  `sub_table_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关联子表的表名',
  `sub_table_fk_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '子表关联的外键名',
  `class_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '实体类名称',
  `tpl_category` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'crud' COMMENT '使用的模板（crud单表操作 tree树表操作）',
  `package_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '生成包路径',
  `module_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '生成模块名',
  `business_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '生成业务名',
  `function_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '生成功能名',
  `function_author` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '生成功能作者',
  `gen_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0' COMMENT '生成代码方式（0zip压缩包 1自定义路径）',
  `gen_path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '/' COMMENT '生成路径（不填默认项目路径）',
  `options` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '其它生成选项',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by_id` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  PRIMARY KEY (`table_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '代码生成业务表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of gen_table
-- ----------------------------

-- ----------------------------
-- Table structure for gen_table_column
-- ----------------------------
DROP TABLE IF EXISTS `gen_table_column`;
CREATE TABLE `gen_table_column`  (
  `column_id` bigint NOT NULL COMMENT '编号',
  `table_id` bigint NULL DEFAULT NULL COMMENT '归属表编号',
  `column_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '列名称',
  `column_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '列描述',
  `column_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '列类型',
  `java_type` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'JAVA类型',
  `java_field` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'JAVA字段名',
  `is_pk` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否主键（1是）',
  `is_increment` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否自增（1是）',
  `is_required` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否必填（1是）',
  `is_insert` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否为插入字段（1是）',
  `is_edit` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否编辑字段（1是）',
  `is_list` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否列表字段（1是）',
  `is_query` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否查询字段（1是）',
  `query_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'EQ' COMMENT '查询方式（等于、不等于、大于、小于、范围）',
  `html_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '显示类型（文本框、文本域、下拉框、复选框、单选框、日期控件）',
  `dict_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '字典类型',
  `sort` int NULL DEFAULT NULL COMMENT '排序',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `create_by_id` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  PRIMARY KEY (`column_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '代码生成业务表字段' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of gen_table_column
-- ----------------------------

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`  (
  `config_id` bigint NOT NULL COMMENT '参数主键',
  `config_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '参数名称',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '参数键名',
  `config_value` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '参数键值',
  `config_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'N' COMMENT '系统内置（Y是 N否）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by_id` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  PRIMARY KEY (`config_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '参数配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_config
-- ----------------------------
INSERT INTO `sys_config` VALUES (1, '主框架页-默认皮肤样式名称', 'sys.index.skinName', 'skin-blue', 'Y', 'admin', '2024-06-13 16:06:37', '', NULL, '蓝色 skin-blue、绿色 skin-green、紫色 skin-purple、红色 skin-red、黄色 skin-yellow', NULL);
INSERT INTO `sys_config` VALUES (2, '用户管理-账号初始密码', 'sys.user.initPassword', '123456', 'Y', 'admin', '2024-06-13 16:06:37', '', NULL, '初始化密码 123456', NULL);
INSERT INTO `sys_config` VALUES (3, '主框架页-侧边栏主题', 'sys.index.sideTheme', 'theme-light', 'Y', 'admin', '2024-06-13 16:06:37', 'admin', '2024-07-16 11:25:33', '深色主题theme-dark，浅色主题theme-light', NULL);
INSERT INTO `sys_config` VALUES (4, '账号自助-验证码开关', 'sys.account.captchaEnabled', 'true', 'Y', 'admin', '2024-06-13 16:06:37', '', NULL, '是否开启验证码功能（true开启，false关闭）', NULL);
INSERT INTO `sys_config` VALUES (5, '账号自助-是否开启用户注册功能', 'sys.account.registerUser', 'false', 'Y', 'admin', '2024-06-13 16:06:37', '', NULL, '是否开启注册用户功能（true开启，false关闭）', NULL);
INSERT INTO `sys_config` VALUES (11, 'OSS预览列表资源开关', 'sys.oss.previewListResource', 'true', 'Y', 'admin', '2024-06-13 16:06:37', 'adminERP', '2025-12-10 10:32:13', 'true:开启, false:关闭', NULL);
INSERT INTO `sys_config` VALUES (12, 'WVP系统token', 'sys.wvp.token', 'eyJhbGciOiJSUzI1NiIsImtpZCI6IjNlNzk2NDZjNGRiYzQwODM4M2E5ZWVkMDlmMmI4NWFlIn0.eyJqdGkiOiJOOUFIeUtHclZPS2hIYXBpejhzWlNRIiwiaWF0IjoxNzU2MzYyNzgzLCJuYmYiOjE3NTYzNjI3ODMsInN1YiI6ImxvZ2luIiwiYXVkIjoiQXVkaWVuY2UiLCJ1c2VyTmFtZSI6ImFkbWluIiwiYXBpS2V5SWQiOjF9.Jo639PDGEtTlN0Yz0eYQvlZZlTz-DnrfMEqb5Wv5Hg_ha3mikpXYiijF5TlqerYRSWAH4_8rHCX5gJ85ChPPZBW0fFvcU2P5yocPTdgVTzMa_weuN8TwYQ5duKqtXK-Q591y_qKxLlNaxUhEhM2JbCB5CM8fLc9ILqdRH5WxV3BMMIvz_1Et3BhitLQM0RYzQA8pKvnBOlfsubr_avweRUCKQ-Mm2DZBnyUPFVIIcSDFSPb47HoUkaSZ9koVns-E9NvszkjLqYqEDNGWAnKxN2MEC_e6IHLNPhgoAZFPyyXGmMJI6S_e1iR0HRiurDUCzk-G0eCMHu6GHwEJc17-DA', 'Y', 'admin', '2025-08-28 16:11:24', 'admin', '2025-08-28 16:11:24', NULL, 1);
INSERT INTO `sys_config` VALUES (13, 'WVP系统地址', 'sys.wvp.url', 'http://192.168.120.67:9528', 'Y', 'admin', '2025-08-28 16:19:49', 'admin', '2025-10-22 08:08:18', NULL, 1);

-- ----------------------------
-- Table structure for sys_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept`  (
  `dept_id` bigint NOT NULL COMMENT '部门id',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父部门id',
  `ancestors` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '祖级列表',
  `dept_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '部门名称',
  `order_num` int NULL DEFAULT 0 COMMENT '显示顺序',
  `leader` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '负责人',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0' COMMENT '部门状态（0正常 1停用）',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `create_by_id` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  PRIMARY KEY (`dept_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '部门表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dept
-- ----------------------------
INSERT INTO `sys_dept` VALUES (100, 0, '0', '总公司', 0, NULL, NULL, NULL, '1', '0', 'admin', '2024-06-13 16:06:25', 'admin', '2026-05-20 10:24:46', NULL);

-- ----------------------------
-- Table structure for sys_dict_data
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data`  (
  `dict_code` bigint NOT NULL COMMENT '字典编码',
  `dict_sort` int NULL DEFAULT 0 COMMENT '字典排序',
  `dict_label` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '字典标签',
  `dict_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '字典键值',
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '字典类型',
  `css_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '样式属性（其他样式扩展）',
  `list_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '表格回显样式',
  `is_default` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'N' COMMENT '是否默认（Y是 N否）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0' COMMENT '状态（0停用 1正常）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by_id` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  PRIMARY KEY (`dict_code`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '字典数据表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dict_data
-- ----------------------------
INSERT INTO `sys_dict_data` VALUES (1, 1, '男', '0', 'sys_user_sex', '', '', 'Y', '1', 'admin', '2024-06-13 16:06:36', '', NULL, '性别男', NULL);
INSERT INTO `sys_dict_data` VALUES (2, 2, '女', '1', 'sys_user_sex', '', '', 'N', '1', 'admin', '2024-06-13 16:06:36', '', NULL, '性别女', NULL);
INSERT INTO `sys_dict_data` VALUES (3, 3, '未知', '2', 'sys_user_sex', '', '', 'N', '1', 'admin', '2024-06-13 16:06:36', '', NULL, '性别未知', NULL);
INSERT INTO `sys_dict_data` VALUES (4, 1, '显示', '1', 'sys_show_hide', '', 'primary', 'Y', '1', 'admin', '2024-06-13 16:06:36', 'admin', '2024-07-10 16:34:54', '显示菜单', NULL);
INSERT INTO `sys_dict_data` VALUES (5, 2, '隐藏', '0', 'sys_show_hide', '', 'danger', 'N', '1', 'admin', '2024-06-13 16:06:36', 'admin', '2024-07-10 16:35:07', '隐藏菜单', NULL);
INSERT INTO `sys_dict_data` VALUES (6, 1, '正常', '1', 'sys_normal_disable', '', 'primary', 'Y', '1', 'admin', '2024-06-13 16:06:36', 'admin', '2024-07-10 14:30:58', '正常状态', NULL);
INSERT INTO `sys_dict_data` VALUES (7, 2, '停用', '0', 'sys_normal_disable', '', 'danger', 'N', '1', 'admin', '2024-06-13 16:06:36', 'admin', '2024-07-10 14:31:06', '停用状态', NULL);
INSERT INTO `sys_dict_data` VALUES (12, 1, '是', 'Y', 'sys_yes_no', '', 'primary', 'Y', '1', 'admin', '2024-06-13 16:06:36', '', NULL, '系统默认是', NULL);
INSERT INTO `sys_dict_data` VALUES (13, 2, '否', 'N', 'sys_yes_no', '', 'danger', 'N', '1', 'admin', '2024-06-13 16:06:36', '', NULL, '系统默认否', NULL);
INSERT INTO `sys_dict_data` VALUES (14, 1, '通知', '1', 'sys_notice_type', '', 'warning', 'Y', '1', 'admin', '2024-06-13 16:06:36', '', NULL, '通知', NULL);
INSERT INTO `sys_dict_data` VALUES (15, 2, '公告', '2', 'sys_notice_type', '', 'success', 'N', '1', 'admin', '2024-06-13 16:06:36', '', NULL, '公告', NULL);
INSERT INTO `sys_dict_data` VALUES (16, 1, '正常', '1', 'sys_notice_status', '', 'primary', 'Y', '1', 'admin', '2024-06-13 16:06:36', 'admin', '2024-07-10 17:24:35', '正常状态', NULL);
INSERT INTO `sys_dict_data` VALUES (17, 2, '关闭', '0', 'sys_notice_status', '', 'danger', 'N', '1', 'admin', '2024-06-13 16:06:36', 'admin', '2024-07-10 17:24:44', '关闭状态', NULL);
INSERT INTO `sys_dict_data` VALUES (18, 1, '新增', '1', 'sys_oper_type', '', 'info', 'N', '1', 'admin', '2024-06-13 16:06:36', '', NULL, '新增操作', NULL);
INSERT INTO `sys_dict_data` VALUES (19, 2, '修改', '2', 'sys_oper_type', '', 'info', 'N', '1', 'admin', '2024-06-13 16:06:36', '', NULL, '修改操作', NULL);
INSERT INTO `sys_dict_data` VALUES (20, 3, '删除', '3', 'sys_oper_type', '', 'danger', 'N', '1', 'admin', '2024-06-13 16:06:37', '', NULL, '删除操作', NULL);
INSERT INTO `sys_dict_data` VALUES (21, 4, '授权', '4', 'sys_oper_type', '', 'primary', 'N', '1', 'admin', '2024-06-13 16:06:37', '', NULL, '授权操作', NULL);
INSERT INTO `sys_dict_data` VALUES (22, 5, '导出', '5', 'sys_oper_type', '', 'warning', 'N', '1', 'admin', '2024-06-13 16:06:37', '', NULL, '导出操作', NULL);
INSERT INTO `sys_dict_data` VALUES (23, 6, '导入', '6', 'sys_oper_type', '', 'warning', 'N', '1', 'admin', '2024-06-13 16:06:37', '', NULL, '导入操作', NULL);
INSERT INTO `sys_dict_data` VALUES (24, 7, '强退', '7', 'sys_oper_type', '', 'danger', 'N', '1', 'admin', '2024-06-13 16:06:37', '', NULL, '强退操作', NULL);
INSERT INTO `sys_dict_data` VALUES (25, 8, '生成代码', '8', 'sys_oper_type', '', 'warning', 'N', '1', 'admin', '2024-06-13 16:06:37', '', NULL, '生成操作', NULL);
INSERT INTO `sys_dict_data` VALUES (26, 9, '清空数据', '9', 'sys_oper_type', '', 'danger', 'N', '1', 'admin', '2024-06-13 16:06:37', '', NULL, '清空操作', NULL);
INSERT INTO `sys_dict_data` VALUES (27, 1, '失败', '0', 'sys_common_status', '', 'danger', 'N', '1', 'admin', '2024-06-13 16:06:37', 'admin', '2024-07-15 10:50:52', '正常状态', NULL);
INSERT INTO `sys_dict_data` VALUES (28, 2, '成功', '1', 'sys_common_status', '', 'success', 'N', '1', 'admin', '2024-06-13 16:06:37', 'admin', '2024-07-15 10:51:05', '停用状态', NULL);
INSERT INTO `sys_dict_data` VALUES (29, 99, '其他', '0', 'sys_oper_type', '', 'info', 'N', '1', 'admin', '2024-06-13 16:06:36', '', NULL, '其他操作', NULL);
INSERT INTO `sys_dict_data` VALUES (1812692503272718338, 0, '客户', '1', 'merchant_type', NULL, 'default', 'N', '1', 'admin', '2024-07-15 11:35:46', 'admin', '2024-07-16 11:21:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1812694839395135489, 1, '供应商', '2', 'merchant_type', NULL, 'default', 'N', '1', 'admin', '2024-07-15 11:45:03', 'admin', '2024-07-16 11:21:29', '', NULL);
INSERT INTO `sys_dict_data` VALUES (1813051377282904066, 3, '客户/供应商', '3', 'merchant_type', NULL, 'default', 'N', '1', 'admin', '2024-07-16 11:21:48', 'admin', '2024-07-16 11:21:48', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1813153852862160897, 0, '未入库', '0', 'wms_receipt_status', NULL, 'info', 'N', '1', 'admin', '2024-07-16 18:09:00', 'admin', '2024-07-22 09:38:14', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1813153899775451137, 1, '已入库', '1', 'wms_receipt_status', NULL, 'primary', 'N', '1', 'admin', '2024-07-16 18:09:11', 'admin', '2024-07-22 09:38:22', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1813397339171905537, 3, '作废', '-1', 'wms_receipt_status', NULL, 'danger', 'N', '1', 'admin', '2024-07-17 10:16:32', 'admin', '2024-07-22 09:38:29', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1814219171351085057, 0, '生产入库', '1', 'wms_receipt_type', NULL, 'primary', 'N', '1', 'admin', '2024-07-19 16:42:12', 'admin', '2024-07-22 09:38:50', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1814219220520910849, 1, '采购入库', '2', 'wms_receipt_type', NULL, 'primary', 'N', '1', 'admin', '2024-07-19 16:42:23', 'admin', '2024-07-22 09:38:56', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1814219269975949313, 2, '退货入库', '3', 'wms_receipt_type', NULL, 'primary', 'N', '1', 'admin', '2024-07-19 16:42:35', 'admin', '2024-07-22 09:39:01', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1814219304272773121, 3, '归还入库', '4', 'wms_receipt_type', NULL, 'primary', 'N', '1', 'admin', '2024-07-19 16:42:43', 'admin', '2024-07-22 09:39:06', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1818850397680640002, 2, '作废', '-1', 'wms_shipment_status', NULL, 'danger', 'N', '1', 'admin', '2024-08-01 11:25:02', 'admin', '2024-08-01 14:25:24', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1818850512650706945, 0, '未出库', '0', 'wms_shipment_status', NULL, 'info', 'N', '1', 'admin', '2024-08-01 11:25:29', 'admin', '2024-08-01 14:25:37', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1818850565389885441, 1, '已出库', '1', 'wms_shipment_status', NULL, 'primary', 'N', '1', 'admin', '2024-08-01 11:25:42', 'admin', '2024-08-01 14:25:32', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1818850814351187969, 0, '退货出库', '1', 'wms_shipment_type', NULL, 'primary', 'N', '1', 'admin', '2024-08-01 11:26:41', 'wms2_admin', '2024-09-25 18:45:02', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1818850852594851841, 1, '销售出库', '2', 'wms_shipment_type', NULL, 'primary', 'N', '1', 'admin', '2024-08-01 11:26:51', 'wms2_admin', '2024-09-25 18:45:13', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1818850884714831874, 2, '生产出库', '3', 'wms_shipment_type', NULL, 'primary', 'N', '1', 'admin', '2024-08-01 11:26:58', 'wms2_admin', '2024-09-25 18:45:23', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1821067084643434498, 0, '入库', '1', 'wms_inventory_history_type', NULL, 'success', 'N', '1', 'admin', '2024-08-07 14:13:21', 'wms2_admin', '2024-09-27 10:53:49', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1821067144441626625, 1, '出库', '2', 'wms_inventory_history_type', NULL, 'danger', 'N', '1', 'admin', '2024-08-07 14:13:36', 'wms2_admin', '2024-09-27 10:53:39', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1821067181917732866, 2, '移库', '3', 'wms_inventory_history_type', NULL, 'warning', 'N', '1', 'admin', '2024-08-07 14:13:45', 'wms2_admin', '2024-09-27 10:54:01', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1821067222455681026, 3, '盘库', '4', 'wms_inventory_history_type', NULL, 'primary', 'N', '1', 'admin', '2024-08-07 14:13:54', 'admin', '2024-08-07 14:58:06', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1822820748966006786, 0, '未移库', '0', 'wms_movement_status', NULL, 'info', 'N', '1', 'admin', '2024-08-12 10:21:48', 'admin', '2024-08-12 10:21:48', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1822820794864275457, 1, '已移库', '1', 'wms_movement_status', NULL, 'primary', 'N', '1', 'admin', '2024-08-12 10:21:59', 'admin', '2024-08-12 10:21:59', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1822820855526494210, 2, '作废', '-1', 'wms_movement_status', NULL, 'danger', 'N', '1', 'admin', '2024-08-12 10:22:13', 'admin', '2024-08-12 10:22:13', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1823182345731391489, 0, '待盘库', '0', 'wms_check_status', NULL, 'info', 'N', '1', 'admin', '2024-08-13 10:18:39', 'admin', '2024-08-13 10:18:39', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1823182400756465666, 1, '已盘库', '1', 'wms_check_status', NULL, 'primary', 'N', '1', 'admin', '2024-08-13 10:18:52', 'admin', '2024-08-13 10:18:52', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1823182471136886786, 2, '作废', '-1', 'wms_check_status', NULL, 'danger', 'N', '1', 'admin', '2024-08-13 10:19:09', 'admin', '2024-08-13 10:19:09', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1882339863081852929, 0, '采购入库', '5', 'wms_inventory_history_type', NULL, 'success', 'N', '1', 'admin', '2025-01-23 16:09:30', 'admin', '2025-01-23 16:09:30', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1882340243366813697, 2, '销售出库', '6', 'wms_inventory_history_type', NULL, 'danger', 'N', '1', 'admin', '2025-01-23 16:11:01', 'admin', '2025-09-23 16:11:01', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1887784663876640769, 0, '未审核', '0', 'doc_checked_status', NULL, 'default', 'N', '1', 'admin', '2025-02-07 16:45:12', 'admin', '2025-09-07 16:45:23', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1887784806642360321, 1, '审核不通过', '500', 'doc_checked_status', NULL, 'danger', 'N', '1', 'admin', '2025-02-07 16:45:46', 'admin', '2025-09-07 16:45:46', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1887784886057312258, 2, '审核通过', '200', 'doc_checked_status', NULL, 'success', 'N', '1', 'admin', '2025-02-07 16:46:05', 'admin', '2025-09-07 16:46:15', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1892437377550241794, 0, '收款单', 'ReceiptVoucher', 'trans_type', NULL, 'default', 'N', '1', 'admin', '2025-02-20 12:53:25', 'admin', '2025-09-20 12:53:25', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1892457266574835714, 0, '未审核', '0', 'finish_status', NULL, 'warning', 'N', '1', 'admin', '2025-02-20 14:12:27', 'admin', '2025-05-20 13:37:50', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1892457327853617154, 1, '已审核', '1', 'finish_status', NULL, 'success', 'N', '1', 'admin', '2025-02-20 14:12:42', 'admin', '2025-05-20 13:37:58', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1902619096513376257, 9, '采购退货', '7', 'wms_inventory_history_type', NULL, 'danger', 'N', '1', 'admin', '2025-03-20 15:11:56', 'admin', '2025-09-26 09:33:56', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1902897057707679745, 1, '有退货', '1', 'refund_status', NULL, 'warning', 'N', '1', 'admin', '2025-03-21 09:36:27', 'admin', '2025-03-21 09:36:27', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1902897136296353794, 0, '无退货', '0', 'refund_status', NULL, 'success', 'N', '1', 'admin', '2025-03-21 09:36:46', 'admin', '2025-03-21 09:36:46', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1904075748731105281, 16, '销售退货', '8', 'wms_inventory_history_type', NULL, 'success', 'N', '1', 'admin', '2025-03-24 15:40:09', 'admin', '2025-09-26 09:34:10', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1920661667344908289, 3, '入库完成', '2', 'wms_receipt_status', NULL, 'success', 'N', '1', 'admin', '2025-05-09 10:06:40', 'admin', '2025-09-21 10:29:21', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1920733559518945281, 2, '出库完成', '2', 'wms_shipment_status', NULL, 'success', 'N', '1', 'admin', '2025-05-09 14:52:21', 'admin', '2025-09-21 10:30:32', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1955071439921717250, 0, 'TCP协议', 'TCP', 'iiot_protocol_type', NULL, 'default', 'N', '1', 'admin', '2025-08-12 08:58:49', 'admin', '2025-08-12 08:58:49', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1955071536902414338, 1, 'UDP协议', 'UDP', 'iiot_protocol_type', NULL, 'default', 'N', '1', 'admin', '2025-08-12 08:59:13', 'admin', '2025-08-12 08:59:18', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1955071606917931009, 2, 'MQTT协议', 'MQTT', 'iiot_protocol_type', NULL, 'default', 'N', '1', 'admin', '2025-08-12 08:59:29', 'admin', '2025-08-12 08:59:29', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1955071645891403778, 3, 'HTTP协议', 'HTTP', 'iiot_protocol_type', NULL, 'default', 'N', '1', 'admin', '2025-08-12 08:59:39', 'admin', '2025-08-12 08:59:39', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1955071681542987778, 4, 'SERIAL协议', 'SERIAL', 'iiot_protocol_type', NULL, 'default', 'N', '1', 'admin', '2025-08-12 08:59:47', 'admin', '2025-08-12 08:59:47', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1955071722118684674, 5, 'WEBSOCKET协议', 'WEBSOCKET', 'iiot_protocol_type', NULL, 'default', 'N', '1', 'admin', '2025-08-12 08:59:57', 'admin', '2025-08-12 08:59:57', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1955072492822044673, 0, '设备直连', 'Direct', 'iiot_protocol_gateway_type', NULL, 'default', 'N', '1', 'admin', '2025-08-12 09:03:00', 'admin', '2025-08-12 09:03:00', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1955072532953145346, 1, '边缘网关', 'Gateway', 'iiot_protocol_gateway_type', NULL, 'default', 'N', '1', 'admin', '2025-08-12 09:03:10', 'admin', '2025-08-12 09:03:10', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1955073008071319553, 0, '服务端', 'Server', 'iiot_protocol_connection_type', NULL, 'default', 'N', '1', 'admin', '2025-08-12 09:05:03', 'admin', '2025-08-12 09:05:03', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1955073072835567618, 1, '客户端', 'Client', 'iiot_protocol_connection_type', NULL, 'default', 'N', '1', 'admin', '2025-08-12 09:05:19', 'admin', '2025-08-12 09:05:19', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1955142827336507393, 1, '在线', 'online', 'iiot_gateway_status', NULL, 'primary', 'N', '1', 'admin', '2025-08-12 13:42:29', 'admin', '2025-08-12 14:16:02', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1955142874438541314, 2, '离线', 'offline', 'iiot_gateway_status', NULL, 'warning', 'N', '1', 'admin', '2025-08-12 13:42:41', 'admin', '2025-08-12 14:16:07', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1955436708093304834, 1, '订阅', 'publish', 'iiot_acl_action', NULL, 'default', 'N', '1', 'admin', '2025-08-13 09:10:16', 'admin', '2025-08-13 09:10:16', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1955436806135160833, 2, '发布', 'subscribe', 'iiot_acl_action', NULL, 'default', 'N', '1', 'admin', '2025-08-13 09:10:40', 'admin', '2025-08-13 09:10:40', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1955436873579569154, 3, '订阅+发布', 'all', 'iiot_acl_action', NULL, 'default', 'N', '1', 'admin', '2025-08-13 09:10:56', 'admin', '2025-08-13 09:10:56', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1955437267022061570, 1, '允许', 'allow', 'iiot_acl_permission', NULL, 'default', 'N', '1', 'admin', '2025-08-13 09:12:29', 'admin', '2025-08-13 09:12:29', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1955437365009391618, 2, '拒绝', 'deny', 'iiot_acl_permission', NULL, 'default', 'N', '1', 'admin', '2025-08-13 09:12:53', 'admin', '2025-08-13 09:12:53', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1955518941294555137, 1, '属性', 'PROPERTY', 'iiot_model_definition_type', NULL, 'default', 'N', '1', 'admin', '2025-08-13 14:37:02', 'admin', '2025-08-13 14:37:02', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1955518982298071042, 2, '服务', 'SERVICE', 'iiot_model_definition_type', NULL, 'default', 'N', '1', 'admin', '2025-08-13 14:37:12', 'admin', '2025-08-13 14:37:33', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1955519027072266242, 3, '事件', 'EVENT', 'iiot_model_definition_type', NULL, 'default', 'N', '1', 'admin', '2025-08-13 14:37:23', 'admin', '2025-08-13 14:37:23', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1957960252933124097, 0, '不展示', '0', 'iiot_model_definition_show', NULL, 'default', 'N', '1', 'admin', '2025-08-20 08:17:56', 'admin', '2025-08-20 08:17:56', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1957960286223314945, 0, '展示', '1', 'iiot_model_definition_show', NULL, 'default', 'N', '1', 'admin', '2025-08-20 08:18:04', 'admin', '2025-08-20 08:18:04', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1957988580335390722, 0, '离线', '0', 'iiot_product_status', NULL, 'warning', 'N', '1', 'admin', '2025-08-20 10:10:30', 'admin', '2025-08-20 10:10:39', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1957988678289166337, 1, '在线', '1', 'iiot_product_status', NULL, 'primary', 'N', '1', 'admin', '2025-08-20 10:10:53', 'admin', '2025-08-20 10:10:57', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1959168763851485185, 6, '大华SDK协议', 'DHSDK', 'iiot_protocol_type', NULL, 'default', 'N', '1', 'admin', '2025-08-23 16:20:08', 'admin', '2025-08-23 16:20:08', '大华SDK协议', 1);
INSERT INTO `sys_dict_data` VALUES (1961358222118477826, 1, '未知', '-1', 'iiot_access_card_status', NULL, 'default', 'N', '0', 'admin', '2025-08-29 17:20:15', 'admin', '2025-08-30 10:46:29', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1961358264774549506, 2, '正常', '0', 'iiot_access_card_status', NULL, 'default', 'N', '1', 'admin', '2025-08-29 17:20:25', 'admin', '2025-08-29 17:20:25', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1961358328720908289, 3, '挂失', '1', 'iiot_access_card_status', NULL, 'default', 'N', '0', 'admin', '2025-08-29 17:20:41', 'admin', '2025-08-30 12:47:58', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1961358402817482753, 4, '注销', '2', 'iiot_access_card_status', NULL, 'default', 'N', '0', 'admin', '2025-08-29 17:20:58', 'admin', '2025-08-30 10:47:54', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1961358489144647681, 5, '冻结', '3', 'iiot_access_card_status', NULL, 'default', 'N', '0', 'admin', '2025-08-29 17:21:19', 'admin', '2025-08-30 10:48:02', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1961358529535795202, 6, '欠费', '4', 'iiot_access_card_status', NULL, 'default', 'N', '0', 'admin', '2025-08-29 17:21:28', 'admin', '2025-08-30 10:48:05', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1961358594820136962, 7, '逾期', '5', 'iiot_access_card_status', NULL, 'default', 'N', '0', 'admin', '2025-08-29 17:21:44', 'admin', '2025-08-30 10:48:07', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1961358676860723202, 8, '预欠费', '6', 'iiot_access_card_status', NULL, 'default', 'N', '0', 'admin', '2025-08-29 17:22:04', 'admin', '2025-08-30 10:48:10', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1961358890489208834, 1, '未知', '-1', 'iiot_access_card_type', NULL, 'default', 'N', '0', 'admin', '2025-08-29 17:22:54', 'admin', '2025-08-30 10:48:29', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1961358931098460161, 2, '一般卡', '0', 'iiot_access_card_type', NULL, 'default', 'N', '1', 'admin', '2025-08-29 17:23:04', 'admin', '2025-08-29 17:23:04', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1961358992024920065, 3, 'VIP卡', '1', 'iiot_access_card_type', NULL, 'default', 'N', '1', 'admin', '2025-08-29 17:23:19', 'admin', '2025-08-29 17:23:19', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1961359038325841921, 4, '来宾卡', '2', 'iiot_access_card_type', NULL, 'default', 'N', '1', 'admin', '2025-08-29 17:23:30', 'admin', '2025-08-29 17:23:30', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1961359082110181378, 5, '巡逻卡', '3', 'iiot_access_card_type', NULL, 'default', 'N', '0', 'admin', '2025-08-29 17:23:40', 'admin', '2025-08-30 10:48:37', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1961359130554392578, 6, '黑名单卡', '4', 'iiot_access_card_type', NULL, 'default', 'N', '0', 'admin', '2025-08-29 17:23:52', 'admin', '2025-08-30 10:48:40', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1961359176700125185, 7, '胁迫卡', '5', 'iiot_access_card_type', NULL, 'default', 'N', '0', 'admin', '2025-08-29 17:24:03', 'admin', '2025-08-30 10:48:44', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1961359210392969217, 8, '巡检卡', '6', 'iiot_access_card_type', NULL, 'default', 'N', '0', 'admin', '2025-08-29 17:24:11', 'admin', '2025-08-30 10:48:47', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1961359251031580673, 9, '母卡', '7', 'iiot_access_card_type', NULL, 'default', 'N', '0', 'admin', '2025-08-29 17:24:20', 'admin', '2025-08-30 10:48:50', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1961618418589863937, 1, '不是', '0', 'iiot_access_card_first_enter', NULL, 'default', 'N', '1', 'admin', '2025-08-30 10:34:11', 'admin', '2025-08-30 10:34:11', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1961618463385030657, 2, '是', '1', 'iiot_access_card_first_enter', '', 'default', 'N', '1', 'admin', '2025-08-30 10:34:21', 'admin', '2025-08-30 10:34:21', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1961618577814032385, 1, '不是', '0', 'iiot_access_card_enable', NULL, 'default', 'N', '1', 'admin', '2025-08-30 10:34:49', 'admin', '2025-08-30 10:34:49', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1961618602426208257, 2, '是', '1', 'iiot_access_card_enable', NULL, 'default', 'N', '1', 'admin', '2025-08-30 10:34:55', 'admin', '2025-08-30 10:34:55', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962332767772012545, 1, '中药', 'zy', 'brain_enterprise_development', NULL, 'default', 'N', '1', 'admin', '2025-09-01 09:52:45', 'admin', '2025-09-01 09:52:45', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962332820750266369, 2, '化学药', 'hxy', 'brain_enterprise_development', NULL, 'default', 'N', '1', 'admin', '2025-09-01 09:52:58', 'admin', '2025-09-01 09:52:58', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962332877851521025, 3, '生物制品', 'swzp', 'brain_enterprise_development', NULL, 'default', 'N', '1', 'admin', '2025-09-01 09:53:11', 'admin', '2025-09-01 09:53:11', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962332978502234113, 4, '生物制品药械组合', 'swzpyxzh', 'brain_enterprise_development', NULL, 'default', 'N', '1', 'admin', '2025-09-01 09:53:35', 'admin', '2025-09-01 09:53:35', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962333048245121026, 5, '体外诊断', 'twzd', 'brain_enterprise_development', NULL, 'default', 'N', '1', 'admin', '2025-09-01 09:53:52', 'admin', '2025-09-01 09:53:52', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962333115416899585, 6, '质谱仪', 'zpy', 'brain_enterprise_development', NULL, 'default', 'N', '1', 'admin', '2025-09-01 09:54:08', 'admin', '2025-09-01 09:54:08', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962333195641352193, 7, '植入式神经调控', 'zrssjdk', 'brain_enterprise_development', NULL, 'default', 'N', '1', 'admin', '2025-09-01 09:54:27', 'admin', '2025-09-01 09:54:27', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962333288922673154, 8, '康复类医疗器械', 'kflylqx', 'brain_enterprise_development', NULL, 'default', 'N', '1', 'admin', '2025-09-01 09:54:49', 'admin', '2025-09-01 09:54:49', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962333355251396610, 9, '生命循环辅助', 'smxhfz', 'brain_enterprise_development', NULL, 'default', 'N', '1', 'admin', '2025-09-01 09:55:05', 'admin', '2025-09-01 09:55:05', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962333430614650882, 10, '齿科器材', 'ckqc', 'brain_enterprise_development', NULL, 'default', 'N', '1', 'admin', '2025-09-01 09:55:23', 'admin', '2025-09-01 09:55:23', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962336212314812418, 3, '大健康消费', 'djkxf', 'brain_enterprise_type', NULL, 'default', 'N', '1', 'admin', '2025-09-01 10:06:26', 'admin', '2025-09-01 10:08:31', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962336318569115650, 1, '创新药', 'cxy', 'brain_enterprise_type', NULL, 'default', 'N', '1', 'admin', '2025-09-01 10:06:51', 'admin', '2025-09-01 10:06:51', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962336700531798018, 2, '医疗器械', 'yyqx', 'brain_enterprise_type', NULL, 'default', 'N', '1', 'admin', '2025-09-01 10:08:23', 'admin', '2025-09-01 10:08:23', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962336791795658753, 4, 'CRO/CDMO', 'CRO/CDMO', 'brain_enterprise_type', NULL, 'default', 'N', '1', 'admin', '2025-09-01 10:08:44', 'admin', '2025-09-01 10:08:44', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962336838037860353, 5, '研究院', 'yjy', 'brain_enterprise_type', NULL, 'default', 'N', '1', 'admin', '2025-09-01 10:08:55', 'admin', '2025-09-01 10:08:55', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962336913174622209, 6, '医院', 'yy', 'brain_enterprise_type', NULL, 'default', 'N', '1', 'admin', '2025-09-01 10:09:13', 'admin', '2025-09-01 10:09:13', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962337260643348482, 7, '基金机构', 'jjjg', 'brain_enterprise_type', NULL, 'default', 'N', '1', 'admin', '2025-09-01 10:10:36', 'admin', '2025-09-01 10:10:36', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962396532353896449, 1, '企业创新', 'qy_cxyf_cgzh', 'brain_policy_type', NULL, 'default', 'N', '1', 'admin', '2025-09-01 14:06:08', 'admin', '2025-09-04 10:11:03', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962396911414120450, 2, '企业发展', 'qy_zdzq_jjfz', 'brain_policy_type', NULL, 'default', 'N', '1', 'admin', '2025-09-01 14:07:38', 'admin', '2025-09-04 10:11:09', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962397116968570882, 3, '构建产业生态', 'yljkcy_jkst_qmts', 'brain_policy_type', NULL, 'default', 'N', '1', 'admin', '2025-09-01 14:08:27', 'admin', '2025-09-04 10:11:17', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962405542574526466, 1, '省级重点实验室', 'sjzdsys', 'brain_platform_type', NULL, 'default', 'N', '1', 'admin', '2025-09-01 14:41:56', 'admin', '2025-09-01 14:41:56', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962405591157149697, 2, '新型研发机构', 'xxyfjg', 'brain_platform_type', NULL, 'default', 'N', '1', 'admin', '2025-09-01 14:42:07', 'admin', '2025-09-01 14:42:07', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962697239871184897, 1, '是', '1', 'brain_enterprise_important', NULL, 'default', 'N', '1', 'admin', '2025-09-02 10:01:02', 'admin', '2025-09-02 10:01:02', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962697288416059394, 2, '否', '0', 'brain_enterprise_important', NULL, 'default', 'N', '1', 'admin', '2025-09-02 10:01:13', 'admin', '2025-09-02 10:01:13', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962724301830971393, 1, '制剂研究', 'zjyj', 'brain_technology_label', NULL, 'default', 'N', '1', 'admin', '2025-09-02 11:48:34', 'admin', '2025-09-02 11:48:34', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962724384773332993, 2, '细胞/基因治疗药物开发', 'xbjyzlywyf', 'brain_technology_label', NULL, 'default', 'N', '1', 'admin', '2025-09-02 11:48:54', 'admin', '2025-09-02 11:48:54', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962724437755781121, 3, '药物发现', 'ywfx', 'brain_technology_label', NULL, 'default', 'N', '1', 'admin', '2025-09-02 11:49:06', 'admin', '2025-09-02 11:49:06', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962724514461212674, 4, '器械设计开发', 'qxsjkf', 'brain_technology_label', NULL, 'default', 'N', '1', 'admin', '2025-09-02 11:49:25', 'admin', '2025-09-02 11:49:25', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962724573923860482, 5, '临床服务', 'lcfw', 'brain_technology_label', NULL, 'default', 'N', '1', 'admin', '2025-09-02 11:49:39', 'admin', '2025-09-02 11:49:39', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962724650067255297, 6, 'API研究', 'APIyj', 'brain_technology_label', NULL, 'default', 'N', '1', 'admin', '2025-09-02 11:49:57', 'admin', '2025-09-02 11:49:57', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962724718400856065, 7, '药物安全性评价', 'ywaqxpj', 'brain_technology_label', NULL, 'default', 'N', '1', 'admin', '2025-09-02 11:50:13', 'admin', '2025-09-02 11:50:13', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962725110757023745, 1, '六个月', '6m', 'brain_technology_deadline', NULL, 'default', 'N', '1', 'admin', '2025-09-02 11:51:47', 'admin', '2025-09-02 11:51:47', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962725157800337409, 2, '一年', '1y', 'brain_technology_deadline', NULL, 'default', 'N', '1', 'admin', '2025-09-02 11:51:58', 'admin', '2025-09-02 11:51:58', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962725283428130818, 3, '长期', 'long', 'brain_technology_deadline', NULL, 'default', 'N', '1', 'admin', '2025-09-02 11:52:28', 'admin', '2025-09-02 11:52:28', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962754005816311810, 1, 'MQTT网关', '0', 'iiot_protocol_gateway_device_type', NULL, 'default', 'N', '1', 'admin', '2025-09-02 13:46:36', 'admin', '2025-09-02 13:46:36', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962754092839731201, 2, 'Modbus网关', '1', 'iiot_protocol_gateway_device_type', NULL, 'default', 'N', '1', 'admin', '2025-09-02 13:46:57', 'admin', '2025-09-02 13:46:57', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962775026040631298, 1, '启用', 'ENABLED', 'iiot_modbus_model_task_status', NULL, 'primary', 'N', '1', 'admin', '2025-09-02 15:10:07', 'admin', '2025-09-02 15:25:28', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962775078700118017, 2, '禁用', 'DISABLED', 'iiot_modbus_model_task_status', NULL, 'warning', 'N', '1', 'admin', '2025-09-02 15:10:20', 'admin', '2025-09-02 15:25:34', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962775129786740737, 3, '离线', 'OFFLINE', 'iiot_modbus_model_task_status', NULL, 'danger', 'N', '1', 'admin', '2025-09-02 15:10:32', 'admin', '2025-09-02 15:25:38', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962780676523094017, 1, 'UINT16', 'UINT16', 'iiot_modbus_model_point_data_type', NULL, 'default', 'N', '1', 'admin', '2025-09-02 15:32:35', 'admin', '2025-09-02 15:32:35', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962780733221695490, 2, 'INT16', 'INT16', 'iiot_modbus_model_point_data_type', NULL, 'default', 'N', '1', 'admin', '2025-09-02 15:32:48', 'admin', '2025-09-02 15:32:48', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962780764632838146, 3, 'FLOAT32', 'FLOAT32', 'iiot_modbus_model_point_data_type', NULL, 'default', 'N', '1', 'admin', '2025-09-02 15:32:56', 'admin', '2025-09-02 15:32:56', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962780803480481793, 4, 'COIL', 'COIL', 'iiot_modbus_model_point_data_type', NULL, 'default', 'N', '1', 'admin', '2025-09-02 15:33:05', 'admin', '2025-09-02 15:33:05', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1963041437761769474, 1, '临床Ⅰ期', '1', 'brain_drug_phase', NULL, 'default', 'N', '1', 'admin', '2025-09-03 08:48:45', 'admin', '2025-09-03 08:48:45', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1963041476919791618, 2, '临床Ⅱ期', '2', 'brain_drug_phase', NULL, 'default', 'N', '1', 'admin', '2025-09-03 08:48:54', 'admin', '2025-09-03 08:48:54', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1963041524143460353, 3, '临床Ⅲ期', '3', 'brain_drug_phase', NULL, 'default', 'N', '1', 'admin', '2025-09-03 08:49:06', 'admin', '2025-09-03 08:49:06', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1963041570297581570, 4, '上市', '4', 'brain_drug_phase', NULL, 'default', 'N', '1', 'admin', '2025-09-03 08:49:17', 'admin', '2025-09-03 08:49:17', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1963041649767059457, 1, '二类', '2', 'brain_instrument_level', NULL, 'default', 'N', '1', 'admin', '2025-09-03 08:49:36', 'admin', '2025-09-03 08:49:36', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1963041682117726210, 2, '三类', '3', 'brain_instrument_level', NULL, 'default', 'N', '1', 'admin', '2025-09-03 08:49:43', 'admin', '2025-09-03 08:49:43', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1963048785389654017, 1, '领军型专家', 'leader', 'brain_talent_type', NULL, 'default', 'N', '1', 'admin', '2025-09-03 09:17:57', 'admin', '2025-09-03 09:17:57', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1963048923826851841, 2, '应用型人才', 'practical', 'brain_talent_type', NULL, 'default', 'N', '1', 'admin', '2025-09-03 09:18:30', 'admin', '2025-09-03 09:19:07', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1964209065310130177, 1, '开关状态', 'SwitchStatus', 'modbus_aircondition_collect', NULL, 'default', 'N', '1', 'admin', '2025-09-06 14:08:29', 'admin', '2025-09-06 14:08:29', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1964209211905249281, 2, '模式状态', 'Modestatus', 'modbus_aircondition_collect', NULL, 'default', 'N', '1', 'admin', '2025-09-06 14:09:04', 'admin', '2025-09-06 14:09:04', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1964209302716125185, 3, '设定温度', 'SetTemp', 'modbus_aircondition_collect', NULL, 'default', 'N', '1', 'admin', '2025-09-06 14:09:26', 'admin', '2025-09-06 14:09:26', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1964209400695066626, 4, '风速状态', 'FanSpeed', 'modbus_aircondition_collect', NULL, 'default', 'N', '1', 'admin', '2025-09-06 14:09:49', 'admin', '2025-09-06 14:09:49', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1964210037763706882, 1, '内机开关', 'Switch-Set', 'modbus_aircondition_control', NULL, 'default', 'N', '1', 'admin', '2025-09-06 14:12:21', 'admin', '2025-09-06 14:12:21', '0=关，1=开', 1);
INSERT INTO `sys_dict_data` VALUES (1964210292001443842, 2, '内机模式', 'Mode-Set', 'modbus_aircondition_control', NULL, 'default', 'N', '1', 'admin', '2025-09-06 14:13:22', 'admin', '2025-09-06 14:13:22', '1=制热，2-制冷，4=送风，8=除湿', 1);
INSERT INTO `sys_dict_data` VALUES (1964210416597438465, 3, '内机温度设定', 'Temp-Set', 'modbus_aircondition_control', NULL, 'default', 'N', '1', 'admin', '2025-09-06 14:13:51', 'admin', '2025-09-06 14:13:51', '17-30', 1);
INSERT INTO `sys_dict_data` VALUES (1964210662870192130, 4, '内机风速', 'Fan-Set', 'modbus_aircondition_control', NULL, 'default', 'N', '1', 'admin', '2025-09-06 14:14:50', 'admin', '2025-09-06 14:14:50', '0=自动，1=低，2=中，3=高', 1);
INSERT INTO `sys_dict_data` VALUES (1964980799952969730, 1, '是', '1', 'control_scene_current', NULL, 'default', 'N', '1', 'admin', '2025-09-08 17:15:05', 'admin', '2025-09-08 17:15:23', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1964980859835047938, 2, '否', '0', 'control_scene_current', NULL, 'default', 'N', '1', 'admin', '2025-09-08 17:15:19', 'admin', '2025-09-08 17:15:19', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1965319341380005889, 1, '有人:1/无人:0', 'presence', 'control_scene_condition_type', NULL, 'default', 'N', '1', 'admin', '2025-09-09 15:40:20', 'admin', '2025-09-09 16:00:44', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1965319523911921666, 1, '=', '=', 'control_scene_condition_operator', NULL, 'default', 'N', '1', 'admin', '2025-09-09 15:41:03', 'admin', '2025-09-09 15:41:03', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1965602670291660802, 1, '照明', 'Meter-01', 'control_home_energy_code_mobile', NULL, 'default', 'N', '1', 'admin', '2025-09-10 10:26:10', 'admin', '2025-09-10 10:26:10', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1969673655976103937, 4, '人才落地', 'rcld', 'brain_policy_type', NULL, 'default', 'N', '1', 'adminMESCY', '2025-09-21 16:02:49', 'adminMESCY', '2025-09-21 16:02:49', NULL, 1969270531557855233);
INSERT INTO `sys_dict_data` VALUES (1979108938404499458, 1, '是', '1', 'iiot_websocket_model_point_broadcast', NULL, 'default', 'N', '1', 'admin', '2025-10-17 16:55:16', 'admin', '2025-10-17 16:55:16', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1979108977604464642, 2, '否', '0', 'iiot_websocket_model_point_broadcast', NULL, 'default', 'N', '1', 'admin', '2025-10-17 16:55:25', 'admin', '2025-10-17 16:55:25', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1979356249156308993, 3, 'socket网关', '2', 'iiot_protocol_gateway_device_type', NULL, 'default', 'N', '1', 'admin', '2025-10-18 09:17:59', 'admin', '2025-10-18 09:18:07', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1981646494010609665, 1, '未开始', '0', 'erp_project_status', NULL, 'info', 'N', '1', 'admin', '2025-10-24 16:58:36', 'admin', '2025-10-24 16:58:51', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1981646656925765634, 2, '进行中', '1', 'erp_project_status', NULL, 'primary', 'N', '1', 'admin', '2025-10-24 16:59:15', 'admin', '2025-10-24 16:59:15', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1981646706837983234, 3, '已完成', '2', 'erp_project_status', NULL, 'success', 'N', '1', 'admin', '2025-10-24 16:59:27', 'admin', '2025-10-24 16:59:27', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1981646769018540034, 4, '已暂停', '3', 'erp_project_status', NULL, 'danger', 'N', '1', 'admin', '2025-10-24 16:59:42', 'admin', '2025-10-24 16:59:42', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1981901773637849090, 1, '未开始', '0', 'erp_progress_status', NULL, 'info', 'N', '1', 'admin', '2025-10-25 09:52:59', 'admin', '2025-10-25 09:52:59', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1981901834832744449, 2, '进行中', '1', 'erp_progress_status', NULL, 'primary', 'N', '1', 'admin', '2025-10-25 09:53:14', 'admin', '2025-10-25 09:53:40', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1981901901236965378, 3, '已完成', '2', 'erp_progress_status', NULL, 'success', 'N', '1', 'admin', '2025-10-25 09:53:30', 'admin', '2025-10-25 09:53:44', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1981902003385044993, 4, '已暂停', '3', 'erp_progress_status', NULL, 'danger', 'N', '1', 'admin', '2025-10-25 09:53:54', 'admin', '2025-10-25 09:53:54', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1998576117374078977, 1, '合同采购', '1', 'purchase_order_type', NULL, 'default', 'N', '1', 'admin', '2025-12-10 10:10:53', 'admin', '2025-12-10 10:10:53', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1998576159233232898, 2, '内部采购', '2', 'purchase_order_type', NULL, 'default', 'N', '1', 'admin', '2025-12-10 10:11:03', 'admin', '2025-12-10 10:11:03', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1999654702929358849, 1, '未审核', '0', 'erp_report_status', NULL, 'info', 'N', '1', 'admin', '2025-12-13 09:36:48', 'admin', '2025-12-13 09:37:14', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1999654737666584578, 2, '已审核', '1', 'erp_report_status', NULL, 'success', 'N', '1', 'admin', '2025-12-13 09:36:56', 'admin', '2025-12-13 09:37:21', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (2003289892176216065, 1, '未审核', '0', 'purchase_order_finish_status', NULL, 'info', 'N', '1', 'admin', '2025-12-23 10:21:44', 'admin', '2025-12-23 10:21:44', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (2003290086519291906, 2, '部门审核通过', '1', 'purchase_order_finish_status', NULL, 'success', 'N', '1', 'admin', '2025-12-23 10:22:31', 'admin', '2025-12-24 14:15:10', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (2003290162545246209, 3, '总经办审核通过', '2', 'purchase_order_finish_status', NULL, 'success', 'N', '1', 'admin', '2025-12-23 10:22:49', 'admin', '2025-12-23 10:22:49', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (2006910090942865409, 1, '是', '1', 'brain_enterprise_silver_economy', NULL, 'default', 'N', '1', 'adminMESCY', '2026-01-02 10:07:07', 'adminMESCY', '2026-01-02 10:07:07', NULL, 1969270531557855233);
INSERT INTO `sys_dict_data` VALUES (2006910125403267074, 2, '否', '0', 'brain_enterprise_silver_economy', NULL, 'default', 'N', '1', 'adminMESCY', '2026-01-02 10:07:15', 'adminMESCY', '2026-01-02 10:07:15', NULL, 1969270531557855233);
INSERT INTO `sys_dict_data` VALUES (2006910719520215041, 1, '东盛慧谷', 'dongShengHuiGu', 'brain_enterprise_location', NULL, 'default', 'N', '1', 'admin', '2026-01-02 10:09:37', 'admin', '2026-01-02 10:09:37', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (2006910758321721346, 2, '生命健康岛', 'lifeHealthIsland', 'brain_enterprise_location', NULL, 'default', 'N', '1', 'admin', '2026-01-02 10:09:46', 'adminMESCY', '2026-01-02 10:42:39', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (2006911570104094722, 1, '是', '1', 'brain_enterprise_life_health', NULL, 'default', 'N', '1', 'admin', '2026-01-02 10:13:00', 'admin', '2026-01-02 10:13:00', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (2006911593273430017, 2, '否', '0', 'brain_enterprise_life_health', NULL, 'default', 'N', '1', 'admin', '2026-01-02 10:13:05', 'admin', '2026-01-02 10:13:05', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (2006913763125301249, 1, '银发经济企业', 'silverEconomy', 'brain_enterprise_category', NULL, 'default', 'N', '1', 'admin', '2026-01-02 10:21:42', 'admin', '2026-01-02 10:21:42', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (2006913803763912706, 2, '全生命周期企业', 'fullLifecycle', 'brain_enterprise_category', NULL, 'default', 'N', '1', 'admin', '2026-01-02 10:21:52', 'admin', '2026-01-02 10:21:52', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (2006915719659065345, 1, '已入驻', 'settled', 'brain_enterprise_progress', NULL, 'default', 'N', '1', 'admin', '2026-01-02 10:29:29', 'admin', '2026-01-02 10:29:29', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (2006915754102689793, 2, '拟入驻', 'intended', 'brain_enterprise_progress', NULL, 'default', 'N', '1', 'admin', '2026-01-02 10:29:37', 'admin', '2026-01-02 10:29:37', NULL, 1);

-- ----------------------------
-- Table structure for sys_dict_type
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type`  (
  `dict_id` bigint NOT NULL COMMENT '字典主键',
  `dict_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '字典名称',
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '字典类型',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by_id` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  PRIMARY KEY (`dict_id`) USING BTREE,
  UNIQUE INDEX `dict_type`(`dict_type` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '字典类型表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dict_type
-- ----------------------------
INSERT INTO `sys_dict_type` VALUES (1, '用户性别', 'sys_user_sex', '1', 'admin', '2024-06-13 16:06:35', '', NULL, '用户性别列表', NULL);
INSERT INTO `sys_dict_type` VALUES (2, '菜单状态', 'sys_show_hide', '1', 'admin', '2024-06-13 16:06:35', '', NULL, '菜单状态列表', NULL);
INSERT INTO `sys_dict_type` VALUES (3, '系统开关', 'sys_normal_disable', '1', 'admin', '2024-06-13 16:06:35', '', NULL, '系统开关列表', NULL);
INSERT INTO `sys_dict_type` VALUES (6, '系统是否', 'sys_yes_no', '1', 'admin', '2024-06-13 16:06:35', '', NULL, '系统是否列表', NULL);
INSERT INTO `sys_dict_type` VALUES (7, '通知类型', 'sys_notice_type', '1', 'admin', '2024-06-13 16:06:35', '', NULL, '通知类型列表', NULL);
INSERT INTO `sys_dict_type` VALUES (8, '通知状态', 'sys_notice_status', '1', 'admin', '2024-06-13 16:06:35', '', NULL, '通知状态列表', NULL);
INSERT INTO `sys_dict_type` VALUES (9, '操作类型', 'sys_oper_type', '1', 'admin', '2024-06-13 16:06:35', '', NULL, '操作类型列表', NULL);
INSERT INTO `sys_dict_type` VALUES (10, '系统状态', 'sys_common_status', '1', 'admin', '2024-06-13 16:06:36', '', NULL, '登录状态列表', NULL);

-- ----------------------------
-- Table structure for sys_logininfor
-- ----------------------------
DROP TABLE IF EXISTS `sys_logininfor`;
CREATE TABLE `sys_logininfor`  (
  `info_id` bigint NOT NULL COMMENT '访问ID',
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '用户账号',
  `ipaddr` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '登录IP地址',
  `login_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '登录地点',
  `browser` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '浏览器类型',
  `os` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '操作系统',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0' COMMENT '登录状态（0成功 1失败）',
  `msg` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '提示消息',
  `login_time` datetime NULL DEFAULT NULL COMMENT '访问时间',
  `create_by_id` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  PRIMARY KEY (`info_id`) USING BTREE,
  INDEX `idx_sys_logininfor_s`(`status` ASC) USING BTREE,
  INDEX `idx_sys_logininfor_lt`(`login_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统访问记录' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_logininfor
-- ----------------------------

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  `menu_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '菜单名称',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父菜单ID',
  `order_num` int NULL DEFAULT 0 COMMENT '显示顺序',
  `path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '路由地址',
  `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件路径',
  `query_param` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '路由参数',
  `is_frame` int NULL DEFAULT 1 COMMENT '是否为外链（0是 1否）',
  `is_cache` int NULL DEFAULT 0 COMMENT '是否缓存（0缓存 1不缓存）',
  `menu_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '菜单类型（M目录 C菜单 F按钮）',
  `visible` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0' COMMENT '显示状态（0显示 1隐藏）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0' COMMENT '菜单状态（0正常 1停用）',
  `perms` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '#' COMMENT '菜单图标',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '备注',
  `create_by_id` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  PRIMARY KEY (`menu_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '菜单权限表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1, '系统管理', 0, 110, 'system', NULL, '', 0, 0, 'M', '1', '1', '', 'system', 'admin', '2025-09-15 09:01:47', 'admin', '2024-08-20 13:45:48', '系统管理目录', NULL);
INSERT INTO `sys_menu` VALUES (2, '系统监控', 0, 120, 'monitor', NULL, '', 0, 0, 'M', '1', '1', '', 'monitor', 'admin', '2025-09-15 09:01:47', 'admin', '2024-08-20 13:45:57', '系统监控目录', NULL);
INSERT INTO `sys_menu` VALUES (100, '用户管理', 1, 1, 'user', 'system/user/index', '', 0, 0, 'C', '1', '1', 'system:user:list', 'user', 'admin', '2025-09-15 09:01:47', '', NULL, '用户管理菜单', NULL);
INSERT INTO `sys_menu` VALUES (101, '角色管理', 1, 2, 'role', 'system/role/index', '', 0, 0, 'C', '1', '1', 'system:role:list', 'peoples', 'admin', '2025-09-15 09:01:47', '', NULL, '角色管理菜单', NULL);
INSERT INTO `sys_menu` VALUES (102, '菜单管理', 1, 3, 'menu', 'system/menu/index', '', 0, 0, 'C', '1', '1', 'system:menu:list', 'tree-table', 'admin', '2025-09-15 09:01:47', '', NULL, '菜单管理菜单', NULL);
INSERT INTO `sys_menu` VALUES (103, '部门管理', 1, 4, 'dept', 'system/dept/index', '', 0, 0, 'C', '1', '1', 'system:dept:list', 'tree', 'admin', '2025-09-15 09:01:47', '', NULL, '部门管理菜单', NULL);
INSERT INTO `sys_menu` VALUES (104, '岗位管理', 1, 5, 'post', 'system/post/index', '', 0, 0, 'C', '1', '1', 'system:post:list', 'post', 'admin', '2025-09-15 09:01:47', '', NULL, '岗位管理菜单', NULL);
INSERT INTO `sys_menu` VALUES (105, '字典管理', 1, 6, 'dict', 'system/dict/index', '', 0, 0, 'C', '1', '1', 'system:dict:list', 'dict', 'admin', '2025-09-15 09:01:47', '', NULL, '字典管理菜单', NULL);
INSERT INTO `sys_menu` VALUES (106, '参数设置', 1, 7, 'config', 'system/config/index', '', 0, 0, 'C', '1', '1', 'system:config:list', 'edit', 'admin', '2025-09-15 09:01:47', '', NULL, '参数设置菜单', NULL);
INSERT INTO `sys_menu` VALUES (107, '通知公告', 1, 8, 'notice', 'system/notice/index', '', 0, 0, 'C', '1', '1', 'system:notice:list', 'message', 'admin', '2025-09-15 09:01:47', '', NULL, '通知公告菜单', NULL);
INSERT INTO `sys_menu` VALUES (108, '日志管理', 0, 140, 'log', '', '', 0, 0, 'M', '1', '1', '', 'log', 'admin', '2025-09-15 09:01:47', 'admin', '2024-08-20 13:46:16', '日志管理菜单', NULL);
INSERT INTO `sys_menu` VALUES (109, '在线用户', 2, 1, 'online', 'monitor/online/index', '', 0, 0, 'C', '1', '1', 'monitor:online:list', 'online', 'admin', '2025-09-15 09:01:47', '', NULL, '在线用户菜单', NULL);
INSERT INTO `sys_menu` VALUES (112, '缓存列表', 2, 6, 'cacheList', 'monitor/cache/list', '', 0, 0, 'C', '1', '1', 'monitor:cache:list', 'redis-list', 'admin', '2025-09-15 09:01:47', '', NULL, '缓存列表菜单', NULL);
INSERT INTO `sys_menu` VALUES (113, '缓存监控', 2, 5, 'cache', 'monitor/cache/index', '', 0, 0, 'C', '1', '1', 'monitor:cache:list', 'redis', 'admin', '2025-09-15 09:01:47', '', NULL, '缓存监控菜单', NULL);
INSERT INTO `sys_menu` VALUES (115, '代码生成', 0, 130, 'gen', 'tool/gen/index', '', 0, 0, 'C', '1', '1', 'tool:gen:list', 'code', 'admin', '2025-09-15 09:01:47', 'admin', '2024-08-20 13:46:06', '代码生成菜单', NULL);
INSERT INTO `sys_menu` VALUES (118, '文件管理', 1, 10, 'oss', 'system/oss/index', '', 0, 0, 'C', '1', '1', 'system:oss:list', 'upload', 'admin', '2025-09-15 09:01:47', '', NULL, '文件管理菜单', NULL);
INSERT INTO `sys_menu` VALUES (500, '操作日志', 108, 1, 'operlog', 'monitor/operlog/index', '', 0, 0, 'C', '1', '1', 'monitor:operlog:list', 'form', 'admin', '2025-09-15 09:01:47', '', NULL, '操作日志菜单', NULL);
INSERT INTO `sys_menu` VALUES (501, '登录日志', 108, 2, 'logininfor', 'monitor/logininfor/index', '', 0, 0, 'C', '1', '1', 'monitor:logininfor:list', 'logininfor', 'admin', '2025-09-15 09:01:47', '', NULL, '登录日志菜单', NULL);
INSERT INTO `sys_menu` VALUES (1001, '用户查询', 100, 1, '', '', '', 0, 0, 'F', '1', '1', 'system:user:query', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1002, '用户新增', 100, 2, '', '', '', 0, 0, 'F', '1', '1', 'system:user:add', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1003, '用户修改', 100, 3, '', '', '', 0, 0, 'F', '1', '1', 'system:user:edit', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1004, '用户删除', 100, 4, '', '', '', 0, 0, 'F', '1', '1', 'system:user:remove', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1005, '用户导出', 100, 5, '', '', '', 0, 0, 'F', '1', '1', 'system:user:export', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1006, '用户导入', 100, 6, '', '', '', 0, 0, 'F', '1', '1', 'system:user:import', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1007, '重置密码', 100, 7, '', '', '', 0, 0, 'F', '1', '1', 'system:user:resetPwd', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1008, '角色查询', 101, 1, '', '', '', 0, 0, 'F', '1', '1', 'system:role:query', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1009, '角色新增', 101, 2, '', '', '', 0, 0, 'F', '1', '1', 'system:role:add', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1010, '角色修改', 101, 3, '', '', '', 0, 0, 'F', '1', '1', 'system:role:edit', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1011, '角色删除', 101, 4, '', '', '', 0, 0, 'F', '1', '1', 'system:role:remove', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1012, '角色导出', 101, 5, '', '', '', 0, 0, 'F', '1', '1', 'system:role:export', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1013, '菜单查询', 102, 1, '', '', '', 0, 0, 'F', '1', '1', 'system:menu:query', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1014, '菜单新增', 102, 2, '', '', '', 0, 0, 'F', '1', '1', 'system:menu:add', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1015, '菜单修改', 102, 3, '', '', '', 0, 0, 'F', '1', '1', 'system:menu:edit', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1016, '菜单删除', 102, 4, '', '', '', 0, 0, 'F', '1', '1', 'system:menu:remove', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1017, '部门查询', 103, 1, '', '', '', 0, 0, 'F', '1', '1', 'system:dept:query', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1018, '部门新增', 103, 2, '', '', '', 0, 0, 'F', '1', '1', 'system:dept:add', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1019, '部门修改', 103, 3, '', '', '', 0, 0, 'F', '1', '1', 'system:dept:edit', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1020, '部门删除', 103, 4, '', '', '', 0, 0, 'F', '1', '1', 'system:dept:remove', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1021, '岗位查询', 104, 1, '', '', '', 0, 0, 'F', '1', '1', 'system:post:query', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1022, '岗位新增', 104, 2, '', '', '', 0, 0, 'F', '1', '1', 'system:post:add', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1023, '岗位修改', 104, 3, '', '', '', 0, 0, 'F', '1', '1', 'system:post:edit', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1024, '岗位删除', 104, 4, '', '', '', 0, 0, 'F', '1', '1', 'system:post:remove', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1025, '岗位导出', 104, 5, '', '', '', 0, 0, 'F', '1', '1', 'system:post:export', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1026, '字典查询', 105, 1, '#', '', '', 0, 0, 'F', '1', '1', 'system:dict:query', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1027, '字典新增', 105, 2, '#', '', '', 0, 0, 'F', '1', '1', 'system:dict:add', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1028, '字典修改', 105, 3, '#', '', '', 0, 0, 'F', '1', '1', 'system:dict:edit', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1029, '字典删除', 105, 4, '#', '', '', 0, 0, 'F', '1', '1', 'system:dict:remove', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1030, '字典导出', 105, 5, '#', '', '', 0, 0, 'F', '1', '1', 'system:dict:export', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1031, '参数查询', 106, 1, '#', '', '', 0, 0, 'F', '1', '1', 'system:config:query', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1032, '参数新增', 106, 2, '#', '', '', 0, 0, 'F', '1', '1', 'system:config:add', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1033, '参数修改', 106, 3, '#', '', '', 0, 0, 'F', '1', '1', 'system:config:edit', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1034, '参数删除', 106, 4, '#', '', '', 0, 0, 'F', '1', '1', 'system:config:remove', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1035, '参数导出', 106, 5, '#', '', '', 0, 0, 'F', '1', '1', 'system:config:export', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1036, '公告查询', 107, 1, '#', '', '', 0, 0, 'F', '1', '1', 'system:notice:query', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1037, '公告新增', 107, 2, '#', '', '', 0, 0, 'F', '1', '1', 'system:notice:add', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1038, '公告修改', 107, 3, '#', '', '', 0, 0, 'F', '1', '1', 'system:notice:edit', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1039, '公告删除', 107, 4, '#', '', '', 0, 0, 'F', '1', '1', 'system:notice:remove', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1040, '操作查询', 500, 1, '#', '', '', 0, 0, 'F', '1', '1', 'monitor:operlog:query', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1041, '操作删除', 500, 2, '#', '', '', 0, 0, 'F', '1', '1', 'monitor:operlog:remove', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1042, '日志导出', 500, 4, '#', '', '', 0, 0, 'F', '1', '1', 'monitor:operlog:export', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1043, '登录查询', 501, 1, '#', '', '', 0, 0, 'F', '1', '1', 'monitor:logininfor:query', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1044, '登录删除', 501, 2, '#', '', '', 0, 0, 'F', '1', '1', 'monitor:logininfor:remove', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1045, '日志导出', 501, 3, '#', '', '', 0, 0, 'F', '1', '1', 'monitor:logininfor:export', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1046, '在线查询', 109, 1, '#', '', '', 0, 0, 'F', '1', '1', 'monitor:online:query', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1047, '批量强退', 109, 2, '#', '', '', 0, 0, 'F', '1', '1', 'monitor:online:batchLogout', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1048, '单条强退', 109, 3, '#', '', '', 0, 0, 'F', '1', '1', 'monitor:online:forceLogout', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1050, '账户解锁', 501, 4, '#', '', '', 0, 0, 'F', '1', '1', 'monitor:logininfor:unlock', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1055, '生成查询', 115, 1, '#', '', '', 0, 0, 'F', '1', '1', 'tool:gen:query', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1056, '生成修改', 115, 2, '#', '', '', 0, 0, 'F', '1', '1', 'tool:gen:edit', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1057, '生成删除', 115, 3, '#', '', '', 0, 0, 'F', '1', '1', 'tool:gen:remove', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1058, '导入代码', 115, 2, '#', '', '', 0, 0, 'F', '1', '1', 'tool:gen:import', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1059, '预览代码', 115, 4, '#', '', '', 0, 0, 'F', '1', '1', 'tool:gen:preview', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1060, '生成代码', 115, 5, '#', '', '', 0, 0, 'F', '1', '1', 'tool:gen:code', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1600, '文件查询', 118, 1, '#', '', '', 0, 0, 'F', '1', '1', 'system:oss:query', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1601, '文件上传', 118, 2, '#', '', '', 0, 0, 'F', '1', '1', 'system:oss:upload', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1602, '文件下载', 118, 3, '#', '', '', 0, 0, 'F', '1', '1', 'system:oss:download', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1603, '文件删除', 118, 4, '#', '', '', 0, 0, 'F', '1', '1', 'system:oss:remove', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1604, '配置添加', 118, 5, '#', '', '', 0, 0, 'F', '1', '1', 'system:oss:add', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1605, '配置编辑', 118, 6, '#', '', '', 0, 0, 'F', '1', '1', 'system:oss:edit', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1998209331864629250, '通用权限API', 0, 1002, 'commonAuth', NULL, NULL, 0, 0, 'M', '0', '1', NULL, 'edit', 'admin', '2025-12-09 09:53:24', 'admin', '2025-12-09 09:53:24', '', 1);
INSERT INTO `sys_menu` VALUES (1998209631233077249, '用户列表', 1998209331864629250, 1, '', NULL, NULL, 0, 0, 'F', '1', '1', 'system:user:list', '#', 'admin', '2025-12-09 09:54:36', 'admin', '2025-12-09 09:54:36', '', 1);
INSERT INTO `sys_menu` VALUES (1998210121547182082, '部门列表', 1998209331864629250, 2, '', NULL, NULL, 0, 0, 'F', '1', '1', 'system:dept:list', '#', 'admin', '2025-12-09 09:56:33', 'admin', '2025-12-09 09:56:33', '', 1);
INSERT INTO `sys_menu` VALUES (1998210317190492162, '文件列表', 1998209331864629250, 3, '', NULL, NULL, 0, 0, 'F', '1', '1', 'system:oss:list', '#', 'admin', '2025-12-09 09:57:19', 'admin', '2025-12-09 09:57:19', '', 1);
INSERT INTO `sys_menu` VALUES (1998210467015225345, '文件查询', 1998209331864629250, 4, '', NULL, NULL, 0, 0, 'F', '1', '1', 'system:oss:query', '#', 'admin', '2025-12-09 09:57:55', 'admin', '2025-12-09 09:57:55', '', 1);
INSERT INTO `sys_menu` VALUES (1998210567770796033, '文件上传', 1998209331864629250, 5, '', NULL, NULL, 0, 0, 'F', '1', '1', 'system:oss:upload', '#', 'admin', '2025-12-09 09:58:19', 'admin', '2025-12-09 09:58:19', '', 1);
INSERT INTO `sys_menu` VALUES (1998210655570161665, '文件下载', 1998209331864629250, 6, '', NULL, NULL, 0, 0, 'F', '1', '1', 'system:oss:download', '#', 'admin', '2025-12-09 09:58:40', 'admin', '2025-12-09 09:58:40', '', 1);
INSERT INTO `sys_menu` VALUES (1998210747568025601, '文件删除', 1998209331864629250, 7, '', NULL, NULL, 0, 0, 'F', '1', '1', 'system:oss:remove', '#', 'admin', '2025-12-09 09:59:02', 'admin', '2025-12-09 09:59:02', '', 1);

-- ----------------------------
-- Table structure for sys_notice
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice`  (
  `notice_id` bigint NOT NULL COMMENT '公告ID',
  `notice_title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公告标题',
  `notice_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公告类型（1通知 2公告）',
  `notice_content` longblob NULL COMMENT '公告内容',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0' COMMENT '公告状态（0正常 1关闭）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by_id` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  PRIMARY KEY (`notice_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '通知公告表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_notice
-- ----------------------------
INSERT INTO `sys_notice` VALUES (1, '温馨提醒：测试版本发布啦', '2', 0x3C703EE696B0E78988E69CACE58685E5AEB93C2F703E, '1', 'admin', '2024-06-13 16:06:38', 'admin', '2025-07-25 09:14:55', '管理员', NULL);

-- ----------------------------
-- Table structure for sys_oper_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_oper_log`;
CREATE TABLE `sys_oper_log`  (
  `oper_id` bigint NOT NULL COMMENT '日志主键',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '模块标题',
  `business_type` int NULL DEFAULT 0 COMMENT '业务类型（0其它 1新增 2修改 3删除）',
  `method` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '方法名称',
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '请求方式',
  `operator_type` int NULL DEFAULT 0 COMMENT '操作类别（0其它 1后台用户 2手机端用户）',
  `oper_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '操作人员',
  `dept_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '部门名称',
  `oper_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '请求URL',
  `oper_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '主机地址',
  `oper_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '操作地点',
  `oper_param` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '请求参数',
  `json_result` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '返回参数',
  `status` int NULL DEFAULT 0 COMMENT '操作状态（0异常 1正常）',
  `error_msg` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '错误消息',
  `oper_time` datetime NULL DEFAULT NULL COMMENT '操作时间',
  `create_by_id` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  PRIMARY KEY (`oper_id`) USING BTREE,
  INDEX `idx_sys_oper_log_bt`(`business_type` ASC) USING BTREE,
  INDEX `idx_sys_oper_log_s`(`status` ASC) USING BTREE,
  INDEX `idx_sys_oper_log_ot`(`oper_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '操作日志记录' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_oper_log
-- ----------------------------

-- ----------------------------
-- Table structure for sys_oss
-- ----------------------------
DROP TABLE IF EXISTS `sys_oss`;
CREATE TABLE `sys_oss`  (
  `oss_id` bigint NOT NULL COMMENT '对象存储主键',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '文件名',
  `original_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '原名',
  `file_suffix` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '文件后缀名',
  `url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'URL地址',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '上传人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新人',
  `service` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'minio' COMMENT '服务商',
  `dept_id` bigint NULL DEFAULT NULL COMMENT '部门',
  `create_by_id` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  PRIMARY KEY (`oss_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'OSS对象存储表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_oss
-- ----------------------------

-- ----------------------------
-- Table structure for sys_oss_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_oss_config`;
CREATE TABLE `sys_oss_config`  (
  `oss_config_id` bigint NOT NULL COMMENT '主建',
  `config_key` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '配置key',
  `access_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT 'accessKey',
  `secret_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '秘钥',
  `bucket_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '桶名称',
  `prefix` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '前缀',
  `endpoint` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '访问站点',
  `domain` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '自定义域名',
  `is_https` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'N' COMMENT '是否https（Y=是,N=否）',
  `region` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '域',
  `access_policy` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '1' COMMENT '桶权限类型(0=private 1=public 2=custom)',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '1' COMMENT '是否默认（0=是,1=否）',
  `ext1` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '扩展字段',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by_id` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  PRIMARY KEY (`oss_config_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '对象存储配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_oss_config
-- ----------------------------
INSERT INTO `sys_oss_config` VALUES (1, 'minio', '2ATviu0LgiG1ogHFww0C', 'D1f6cSjoYBiKwna9lKo7VRGTR6knjCBcxebJ82B7', 'bocoo', '', '192.168.120.67:9000', '', 'N', '', '1', '0', '', 'admin', '2024-06-13 16:06:38', 'admin', '2026-05-20 10:27:20', '', NULL);
INSERT INTO `sys_oss_config` VALUES (2, 'qiniu', 'XXXXXXXXXXXXXXX', 'XXXXXXXXXXXXXXX', 'bocoo', '', 's3-cn-north-1.qiniucs.com', '', 'N', '', '1', '0', '', 'admin', '2024-06-13 16:06:38', 'admin', '2025-07-25 16:40:21', NULL, NULL);
INSERT INTO `sys_oss_config` VALUES (3, 'aliyun', 'XXXXXXXXXXXXXXX', 'XXXXXXXXXXXXXXX', 'bocoo', '', 'oss-cn-beijing.aliyuncs.com', '', 'N', '', '1', '0', '', 'admin', '2024-06-13 16:06:38', 'admin', '2024-07-10 17:50:41', NULL, NULL);
INSERT INTO `sys_oss_config` VALUES (4, 'qcloud', 'XXXXXXXXXXXXXXX', 'XXXXXXXXXXXXXXX', 'bocoo', '', 'cos.ap-beijing.myqcloud.com', '', 'N', 'ap-beijing', '1', '0', '', 'admin', '2024-06-13 16:06:38', 'admin', '2024-06-13 16:06:38', NULL, NULL);

-- ----------------------------
-- Table structure for sys_post
-- ----------------------------
DROP TABLE IF EXISTS `sys_post`;
CREATE TABLE `sys_post`  (
  `post_id` bigint NOT NULL COMMENT '岗位ID',
  `post_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '岗位编码',
  `post_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '岗位名称',
  `post_sort` int NOT NULL COMMENT '显示顺序',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by_id` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  PRIMARY KEY (`post_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '岗位信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_post
-- ----------------------------
INSERT INTO `sys_post` VALUES (1, 'ceo', 'BOSS', 1, '1', 'admin', '2024-06-13 16:06:25', 'show', '2025-07-25 11:54:02', '', NULL);

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `role_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名称',
  `role_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色权限字符串',
  `role_sort` int NOT NULL COMMENT '显示顺序',
  `data_scope` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '1' COMMENT '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）',
  `menu_check_strictly` tinyint(1) NULL DEFAULT 1 COMMENT '菜单树选择项是否关联显示',
  `dept_check_strictly` tinyint(1) NULL DEFAULT 1 COMMENT '部门树选择项是否关联显示',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色状态（0正常 1停用）',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by_id` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  PRIMARY KEY (`role_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '超级管理员', 'admin', 1, '1', 1, 1, '1', '0', 'admin', '2024-06-13 16:06:26', '', NULL, '超级管理员', NULL);
INSERT INTO `sys_role` VALUES (2056929180041412609, '测试', 'ceshi', 0, '1', 1, 1, '1', '0', 'admin', '2026-05-20 10:45:07', 'admin', '2026-05-20 10:45:07', NULL, 1);

-- ----------------------------
-- Table structure for sys_role_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_dept`;
CREATE TABLE `sys_role_dept`  (
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `dept_id` bigint NOT NULL COMMENT '部门ID',
  `create_by_id` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  PRIMARY KEY (`role_id`, `dept_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色和部门关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role_dept
-- ----------------------------

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  `create_by_id` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  PRIMARY KEY (`role_id`, `menu_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色和菜单关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `dept_id` bigint NULL DEFAULT NULL COMMENT '部门ID',
  `user_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户账号',
  `nick_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户昵称',
  `user_type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'sys_user' COMMENT '用户类型（sys_user系统用户）',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '用户邮箱',
  `phonenumber` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '手机号码',
  `sex` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0' COMMENT '用户性别（0男 1女 2未知）',
  `avatar` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '头像地址',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '密码',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0' COMMENT '帐号状态（0正常 1停用）',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `login_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '最后登录IP',
  `login_date` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by_id` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `file_url1` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `file_url2` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `file_url3` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `file_url4` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `oss_id1` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `oss_id2` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `oss_id3` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `oss_id4` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 103, 'admin', '系统管理员', 'sys_user', '2547117@qq.com', '18888888888', '0', 'http://192.168.120.67:9000/bocoo/2025/09/01/f437fdf9b13c4cf38358ed15893f193a.png', '$2a$10$gkt8GIcTlW28k3a.osOvQus81YBcY9JHr7zLqaaknk4O2x9xX/JMm', '1', '0', '127.0.0.1', '2026-05-20 10:23:51', 'admin', '2024-06-13 16:06:25', 'admin', '2026-05-20 10:23:51', '管理员', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for sys_user_post
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_post`;
CREATE TABLE `sys_user_post`  (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `post_id` bigint NOT NULL COMMENT '岗位ID',
  `create_by_id` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  PRIMARY KEY (`user_id`, `post_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户与岗位关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_post
-- ----------------------------
INSERT INTO `sys_user_post` VALUES (1, 1, NULL);

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `create_by_id` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  PRIMARY KEY (`user_id`, `role_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户和角色关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1, NULL);

-- ----------------------------
-- Table structure for third_client
-- ----------------------------
DROP TABLE IF EXISTS `third_client`;
CREATE TABLE `third_client`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `app_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '第三方应用唯一标识',
  `secret_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '第三方应用密钥',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '应用名称',
  `permissions` json NULL COMMENT '权限列表，JSON数组存储',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `create_by_id` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_app_key`(`app_key` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '第三方应用信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of third_client
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
