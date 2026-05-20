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
  `table_id` bigint NOT NULL COMMENT '缂栧彿',
  `table_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '琛ㄥ悕绉?,
  `table_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '琛ㄦ弿杩?,
  `sub_table_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鍏宠仈瀛愯〃鐨勮〃鍚?,
  `sub_table_fk_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '瀛愯〃鍏宠仈鐨勫閿悕',
  `class_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '瀹炰綋绫诲悕绉?,
  `tpl_category` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'crud' COMMENT '浣跨敤鐨勬ā鏉匡紙crud鍗曡〃鎿嶄綔 tree鏍戣〃鎿嶄綔锛?,
  `package_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鐢熸垚鍖呰矾寰?,
  `module_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鐢熸垚妯″潡鍚?,
  `business_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鐢熸垚涓氬姟鍚?,
  `function_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鐢熸垚鍔熻兘鍚?,
  `function_author` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鐢熸垚鍔熻兘浣滆€?,
  `gen_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0' COMMENT '鐢熸垚浠ｇ爜鏂瑰紡锛?zip鍘嬬缉鍖?1鑷畾涔夎矾寰勶級',
  `gen_path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '/' COMMENT '鐢熸垚璺緞锛堜笉濉粯璁ら」鐩矾寰勶級',
  `options` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鍏跺畠鐢熸垚閫夐」',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '鍒涘缓鑰?,
  `create_time` datetime NULL DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '鏇存柊鑰?,
  `update_time` datetime NULL DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '澶囨敞',
  `create_by_id` bigint NULL DEFAULT NULL COMMENT '鍒涘缓浜篒D',
  PRIMARY KEY (`table_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '浠ｇ爜鐢熸垚涓氬姟琛? ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of gen_table
-- ----------------------------

-- ----------------------------
-- Table structure for gen_table_column
-- ----------------------------
DROP TABLE IF EXISTS `gen_table_column`;
CREATE TABLE `gen_table_column`  (
  `column_id` bigint NOT NULL COMMENT '缂栧彿',
  `table_id` bigint NULL DEFAULT NULL COMMENT '褰掑睘琛ㄧ紪鍙?,
  `column_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鍒楀悕绉?,
  `column_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鍒楁弿杩?,
  `column_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鍒楃被鍨?,
  `java_type` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'JAVA绫诲瀷',
  `java_field` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'JAVA瀛楁鍚?,
  `is_pk` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鏄惁涓婚敭锛?鏄級',
  `is_increment` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鏄惁鑷锛?鏄級',
  `is_required` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鏄惁蹇呭～锛?鏄級',
  `is_insert` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鏄惁涓烘彃鍏ュ瓧娈碉紙1鏄級',
  `is_edit` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鏄惁缂栬緫瀛楁锛?鏄級',
  `is_list` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鏄惁鍒楄〃瀛楁锛?鏄級',
  `is_query` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鏄惁鏌ヨ瀛楁锛?鏄級',
  `query_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'EQ' COMMENT '鏌ヨ鏂瑰紡锛堢瓑浜庛€佷笉绛変簬銆佸ぇ浜庛€佸皬浜庛€佽寖鍥达級',
  `html_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鏄剧ず绫诲瀷锛堟枃鏈銆佹枃鏈煙銆佷笅鎷夋銆佸閫夋銆佸崟閫夋銆佹棩鏈熸帶浠讹級',
  `dict_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '瀛楀吀绫诲瀷',
  `sort` int NULL DEFAULT NULL COMMENT '鎺掑簭',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '鍒涘缓鑰?,
  `create_time` datetime NULL DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '鏇存柊鑰?,
  `update_time` datetime NULL DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
  `create_by_id` bigint NULL DEFAULT NULL COMMENT '鍒涘缓浜篒D',
  PRIMARY KEY (`column_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '浠ｇ爜鐢熸垚涓氬姟琛ㄥ瓧娈? ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of gen_table_column
-- ----------------------------

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`  (
  `config_id` bigint NOT NULL COMMENT '鍙傛暟涓婚敭',
  `config_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '鍙傛暟鍚嶇О',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '鍙傛暟閿悕',
  `config_value` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '鍙傛暟閿€?,
  `config_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'N' COMMENT '绯荤粺鍐呯疆锛圷鏄?N鍚︼級',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '鍒涘缓鑰?,
  `create_time` datetime NULL DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '鏇存柊鑰?,
  `update_time` datetime NULL DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '澶囨敞',
  `create_by_id` bigint NULL DEFAULT NULL COMMENT '鍒涘缓浜篒D',
  PRIMARY KEY (`config_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鍙傛暟閰嶇疆琛? ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_config
-- ----------------------------
INSERT INTO `sys_config` VALUES (1, '涓绘鏋堕〉-榛樿鐨偆鏍峰紡鍚嶇О', 'sys.index.skinName', 'skin-blue', 'Y', 'admin', '2024-06-13 16:06:37', '', NULL, '钃濊壊 skin-blue銆佺豢鑹?skin-green銆佺传鑹?skin-purple銆佺孩鑹?skin-red銆侀粍鑹?skin-yellow', NULL);
INSERT INTO `sys_config` VALUES (2, '鐢ㄦ埛绠＄悊-璐﹀彿鍒濆瀵嗙爜', 'sys.user.initPassword', '123456', 'Y', 'admin', '2024-06-13 16:06:37', '', NULL, '鍒濆鍖栧瘑鐮?123456', NULL);
INSERT INTO `sys_config` VALUES (3, '涓绘鏋堕〉-渚ц竟鏍忎富棰?, 'sys.index.sideTheme', 'theme-light', 'Y', 'admin', '2024-06-13 16:06:37', 'admin', '2024-07-16 11:25:33', '娣辫壊涓婚theme-dark锛屾祬鑹蹭富棰榯heme-light', NULL);
INSERT INTO `sys_config` VALUES (4, '璐﹀彿鑷姪-楠岃瘉鐮佸紑鍏?, 'sys.account.captchaEnabled', 'true', 'Y', 'admin', '2024-06-13 16:06:37', '', NULL, '鏄惁寮€鍚獙璇佺爜鍔熻兘锛坱rue寮€鍚紝false鍏抽棴锛?, NULL);
INSERT INTO `sys_config` VALUES (5, '璐﹀彿鑷姪-鏄惁寮€鍚敤鎴锋敞鍐屽姛鑳?, 'sys.account.registerUser', 'false', 'Y', 'admin', '2024-06-13 16:06:37', '', NULL, '鏄惁寮€鍚敞鍐岀敤鎴峰姛鑳斤紙true寮€鍚紝false鍏抽棴锛?, NULL);
INSERT INTO `sys_config` VALUES (11, 'OSS棰勮鍒楄〃璧勬簮寮€鍏?, 'sys.oss.previewListResource', 'true', 'Y', 'admin', '2024-06-13 16:06:37', 'adminERP', '2025-12-10 10:32:13', 'true:寮€鍚? false:鍏抽棴', NULL);
INSERT INTO `sys_config` VALUES (12, 'WVP绯荤粺token', 'sys.wvp.token', 'eyJhbGciOiJSUzI1NiIsImtpZCI6IjNlNzk2NDZjNGRiYzQwODM4M2E5ZWVkMDlmMmI4NWFlIn0.eyJqdGkiOiJOOUFIeUtHclZPS2hIYXBpejhzWlNRIiwiaWF0IjoxNzU2MzYyNzgzLCJuYmYiOjE3NTYzNjI3ODMsInN1YiI6ImxvZ2luIiwiYXVkIjoiQXVkaWVuY2UiLCJ1c2VyTmFtZSI6ImFkbWluIiwiYXBpS2V5SWQiOjF9.Jo639PDGEtTlN0Yz0eYQvlZZlTz-DnrfMEqb5Wv5Hg_ha3mikpXYiijF5TlqerYRSWAH4_8rHCX5gJ85ChPPZBW0fFvcU2P5yocPTdgVTzMa_weuN8TwYQ5duKqtXK-Q591y_qKxLlNaxUhEhM2JbCB5CM8fLc9ILqdRH5WxV3BMMIvz_1Et3BhitLQM0RYzQA8pKvnBOlfsubr_avweRUCKQ-Mm2DZBnyUPFVIIcSDFSPb47HoUkaSZ9koVns-E9NvszkjLqYqEDNGWAnKxN2MEC_e6IHLNPhgoAZFPyyXGmMJI6S_e1iR0HRiurDUCzk-G0eCMHu6GHwEJc17-DA', 'Y', 'admin', '2025-08-28 16:11:24', 'admin', '2025-08-28 16:11:24', NULL, 1);
INSERT INTO `sys_config` VALUES (13, 'WVP绯荤粺鍦板潃', 'sys.wvp.url', 'http://localhost:9528', 'Y', 'admin', '2025-08-28 16:19:49', 'admin', '2025-10-22 08:08:18', NULL, 1);

-- ----------------------------
-- Table structure for sys_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept`  (
  `dept_id` bigint NOT NULL COMMENT '閮ㄩ棬id',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '鐖堕儴闂╥d',
  `ancestors` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '绁栫骇鍒楄〃',
  `dept_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '閮ㄩ棬鍚嶇О',
  `order_num` int NULL DEFAULT 0 COMMENT '鏄剧ず椤哄簭',
  `leader` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '璐熻矗浜?,
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鑱旂郴鐢佃瘽',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '閭',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0' COMMENT '閮ㄩ棬鐘舵€侊紙0姝ｅ父 1鍋滅敤锛?,
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0' COMMENT '鍒犻櫎鏍囧織锛?浠ｈ〃瀛樺湪 2浠ｈ〃鍒犻櫎锛?,
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '鍒涘缓鑰?,
  `create_time` datetime NULL DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '鏇存柊鑰?,
  `update_time` datetime NULL DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
  `create_by_id` bigint NULL DEFAULT NULL COMMENT '鍒涘缓浜篒D',
  PRIMARY KEY (`dept_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '閮ㄩ棬琛? ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dept
-- ----------------------------
INSERT INTO `sys_dept` VALUES (100, 0, '0', '鎬诲叕鍙?, 0, NULL, NULL, NULL, '1', '0', 'admin', '2024-06-13 16:06:25', 'admin', '2026-05-20 10:24:46', NULL);

-- ----------------------------
-- Table structure for sys_dict_data
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data`  (
  `dict_code` bigint NOT NULL COMMENT '瀛楀吀缂栫爜',
  `dict_sort` int NULL DEFAULT 0 COMMENT '瀛楀吀鎺掑簭',
  `dict_label` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '瀛楀吀鏍囩',
  `dict_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '瀛楀吀閿€?,
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '瀛楀吀绫诲瀷',
  `css_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鏍峰紡灞炴€э紙鍏朵粬鏍峰紡鎵╁睍锛?,
  `list_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '琛ㄦ牸鍥炴樉鏍峰紡',
  `is_default` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'N' COMMENT '鏄惁榛樿锛圷鏄?N鍚︼級',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0' COMMENT '鐘舵€侊紙0鍋滅敤 1姝ｅ父锛?,
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '鍒涘缓鑰?,
  `create_time` datetime NULL DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '鏇存柊鑰?,
  `update_time` datetime NULL DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '澶囨敞',
  `create_by_id` bigint NULL DEFAULT NULL COMMENT '鍒涘缓浜篒D',
  PRIMARY KEY (`dict_code`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '瀛楀吀鏁版嵁琛? ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dict_data
-- ----------------------------
INSERT INTO `sys_dict_data` VALUES (1, 1, '鐢?, '0', 'sys_user_sex', '', '', 'Y', '1', 'admin', '2024-06-13 16:06:36', '', NULL, '鎬у埆鐢?, NULL);
INSERT INTO `sys_dict_data` VALUES (2, 2, '濂?, '1', 'sys_user_sex', '', '', 'N', '1', 'admin', '2024-06-13 16:06:36', '', NULL, '鎬у埆濂?, NULL);
INSERT INTO `sys_dict_data` VALUES (3, 3, '鏈煡', '2', 'sys_user_sex', '', '', 'N', '1', 'admin', '2024-06-13 16:06:36', '', NULL, '鎬у埆鏈煡', NULL);
INSERT INTO `sys_dict_data` VALUES (4, 1, '鏄剧ず', '1', 'sys_show_hide', '', 'primary', 'Y', '1', 'admin', '2024-06-13 16:06:36', 'admin', '2024-07-10 16:34:54', '鏄剧ず鑿滃崟', NULL);
INSERT INTO `sys_dict_data` VALUES (5, 2, '闅愯棌', '0', 'sys_show_hide', '', 'danger', 'N', '1', 'admin', '2024-06-13 16:06:36', 'admin', '2024-07-10 16:35:07', '闅愯棌鑿滃崟', NULL);
INSERT INTO `sys_dict_data` VALUES (6, 1, '姝ｅ父', '1', 'sys_normal_disable', '', 'primary', 'Y', '1', 'admin', '2024-06-13 16:06:36', 'admin', '2024-07-10 14:30:58', '姝ｅ父鐘舵€?, NULL);
INSERT INTO `sys_dict_data` VALUES (7, 2, '鍋滅敤', '0', 'sys_normal_disable', '', 'danger', 'N', '1', 'admin', '2024-06-13 16:06:36', 'admin', '2024-07-10 14:31:06', '鍋滅敤鐘舵€?, NULL);
INSERT INTO `sys_dict_data` VALUES (12, 1, '鏄?, 'Y', 'sys_yes_no', '', 'primary', 'Y', '1', 'admin', '2024-06-13 16:06:36', '', NULL, '绯荤粺榛樿鏄?, NULL);
INSERT INTO `sys_dict_data` VALUES (13, 2, '鍚?, 'N', 'sys_yes_no', '', 'danger', 'N', '1', 'admin', '2024-06-13 16:06:36', '', NULL, '绯荤粺榛樿鍚?, NULL);
INSERT INTO `sys_dict_data` VALUES (14, 1, '閫氱煡', '1', 'sys_notice_type', '', 'warning', 'Y', '1', 'admin', '2024-06-13 16:06:36', '', NULL, '閫氱煡', NULL);
INSERT INTO `sys_dict_data` VALUES (15, 2, '鍏憡', '2', 'sys_notice_type', '', 'success', 'N', '1', 'admin', '2024-06-13 16:06:36', '', NULL, '鍏憡', NULL);
INSERT INTO `sys_dict_data` VALUES (16, 1, '姝ｅ父', '1', 'sys_notice_status', '', 'primary', 'Y', '1', 'admin', '2024-06-13 16:06:36', 'admin', '2024-07-10 17:24:35', '姝ｅ父鐘舵€?, NULL);
INSERT INTO `sys_dict_data` VALUES (17, 2, '鍏抽棴', '0', 'sys_notice_status', '', 'danger', 'N', '1', 'admin', '2024-06-13 16:06:36', 'admin', '2024-07-10 17:24:44', '鍏抽棴鐘舵€?, NULL);
INSERT INTO `sys_dict_data` VALUES (18, 1, '鏂板', '1', 'sys_oper_type', '', 'info', 'N', '1', 'admin', '2024-06-13 16:06:36', '', NULL, '鏂板鎿嶄綔', NULL);
INSERT INTO `sys_dict_data` VALUES (19, 2, '淇敼', '2', 'sys_oper_type', '', 'info', 'N', '1', 'admin', '2024-06-13 16:06:36', '', NULL, '淇敼鎿嶄綔', NULL);
INSERT INTO `sys_dict_data` VALUES (20, 3, '鍒犻櫎', '3', 'sys_oper_type', '', 'danger', 'N', '1', 'admin', '2024-06-13 16:06:37', '', NULL, '鍒犻櫎鎿嶄綔', NULL);
INSERT INTO `sys_dict_data` VALUES (21, 4, '鎺堟潈', '4', 'sys_oper_type', '', 'primary', 'N', '1', 'admin', '2024-06-13 16:06:37', '', NULL, '鎺堟潈鎿嶄綔', NULL);
INSERT INTO `sys_dict_data` VALUES (22, 5, '瀵煎嚭', '5', 'sys_oper_type', '', 'warning', 'N', '1', 'admin', '2024-06-13 16:06:37', '', NULL, '瀵煎嚭鎿嶄綔', NULL);
INSERT INTO `sys_dict_data` VALUES (23, 6, '瀵煎叆', '6', 'sys_oper_type', '', 'warning', 'N', '1', 'admin', '2024-06-13 16:06:37', '', NULL, '瀵煎叆鎿嶄綔', NULL);
INSERT INTO `sys_dict_data` VALUES (24, 7, '寮洪€€', '7', 'sys_oper_type', '', 'danger', 'N', '1', 'admin', '2024-06-13 16:06:37', '', NULL, '寮洪€€鎿嶄綔', NULL);
INSERT INTO `sys_dict_data` VALUES (25, 8, '鐢熸垚浠ｇ爜', '8', 'sys_oper_type', '', 'warning', 'N', '1', 'admin', '2024-06-13 16:06:37', '', NULL, '鐢熸垚鎿嶄綔', NULL);
INSERT INTO `sys_dict_data` VALUES (26, 9, '娓呯┖鏁版嵁', '9', 'sys_oper_type', '', 'danger', 'N', '1', 'admin', '2024-06-13 16:06:37', '', NULL, '娓呯┖鎿嶄綔', NULL);
INSERT INTO `sys_dict_data` VALUES (27, 1, '澶辫触', '0', 'sys_common_status', '', 'danger', 'N', '1', 'admin', '2024-06-13 16:06:37', 'admin', '2024-07-15 10:50:52', '姝ｅ父鐘舵€?, NULL);
INSERT INTO `sys_dict_data` VALUES (28, 2, '鎴愬姛', '1', 'sys_common_status', '', 'success', 'N', '1', 'admin', '2024-06-13 16:06:37', 'admin', '2024-07-15 10:51:05', '鍋滅敤鐘舵€?, NULL);
INSERT INTO `sys_dict_data` VALUES (29, 99, '鍏朵粬', '0', 'sys_oper_type', '', 'info', 'N', '1', 'admin', '2024-06-13 16:06:36', '', NULL, '鍏朵粬鎿嶄綔', NULL);
INSERT INTO `sys_dict_data` VALUES (1812692503272718338, 0, '瀹㈡埛', '1', 'merchant_type', NULL, 'default', 'N', '1', 'admin', '2024-07-15 11:35:46', 'admin', '2024-07-16 11:21:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1812694839395135489, 1, '渚涘簲鍟?, '2', 'merchant_type', NULL, 'default', 'N', '1', 'admin', '2024-07-15 11:45:03', 'admin', '2024-07-16 11:21:29', '', NULL);
INSERT INTO `sys_dict_data` VALUES (1813051377282904066, 3, '瀹㈡埛/渚涘簲鍟?, '3', 'merchant_type', NULL, 'default', 'N', '1', 'admin', '2024-07-16 11:21:48', 'admin', '2024-07-16 11:21:48', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1813153852862160897, 0, '鏈叆搴?, '0', 'wms_receipt_status', NULL, 'info', 'N', '1', 'admin', '2024-07-16 18:09:00', 'admin', '2024-07-22 09:38:14', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1813153899775451137, 1, '宸插叆搴?, '1', 'wms_receipt_status', NULL, 'primary', 'N', '1', 'admin', '2024-07-16 18:09:11', 'admin', '2024-07-22 09:38:22', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1813397339171905537, 3, '浣滃簾', '-1', 'wms_receipt_status', NULL, 'danger', 'N', '1', 'admin', '2024-07-17 10:16:32', 'admin', '2024-07-22 09:38:29', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1814219171351085057, 0, '鐢熶骇鍏ュ簱', '1', 'wms_receipt_type', NULL, 'primary', 'N', '1', 'admin', '2024-07-19 16:42:12', 'admin', '2024-07-22 09:38:50', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1814219220520910849, 1, '閲囪喘鍏ュ簱', '2', 'wms_receipt_type', NULL, 'primary', 'N', '1', 'admin', '2024-07-19 16:42:23', 'admin', '2024-07-22 09:38:56', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1814219269975949313, 2, '閫€璐у叆搴?, '3', 'wms_receipt_type', NULL, 'primary', 'N', '1', 'admin', '2024-07-19 16:42:35', 'admin', '2024-07-22 09:39:01', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1814219304272773121, 3, '褰掕繕鍏ュ簱', '4', 'wms_receipt_type', NULL, 'primary', 'N', '1', 'admin', '2024-07-19 16:42:43', 'admin', '2024-07-22 09:39:06', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1818850397680640002, 2, '浣滃簾', '-1', 'wms_shipment_status', NULL, 'danger', 'N', '1', 'admin', '2024-08-01 11:25:02', 'admin', '2024-08-01 14:25:24', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1818850512650706945, 0, '鏈嚭搴?, '0', 'wms_shipment_status', NULL, 'info', 'N', '1', 'admin', '2024-08-01 11:25:29', 'admin', '2024-08-01 14:25:37', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1818850565389885441, 1, '宸插嚭搴?, '1', 'wms_shipment_status', NULL, 'primary', 'N', '1', 'admin', '2024-08-01 11:25:42', 'admin', '2024-08-01 14:25:32', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1818850814351187969, 0, '閫€璐у嚭搴?, '1', 'wms_shipment_type', NULL, 'primary', 'N', '1', 'admin', '2024-08-01 11:26:41', 'wms2_admin', '2024-09-25 18:45:02', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1818850852594851841, 1, '閿€鍞嚭搴?, '2', 'wms_shipment_type', NULL, 'primary', 'N', '1', 'admin', '2024-08-01 11:26:51', 'wms2_admin', '2024-09-25 18:45:13', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1818850884714831874, 2, '鐢熶骇鍑哄簱', '3', 'wms_shipment_type', NULL, 'primary', 'N', '1', 'admin', '2024-08-01 11:26:58', 'wms2_admin', '2024-09-25 18:45:23', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1821067084643434498, 0, '鍏ュ簱', '1', 'wms_inventory_history_type', NULL, 'success', 'N', '1', 'admin', '2024-08-07 14:13:21', 'wms2_admin', '2024-09-27 10:53:49', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1821067144441626625, 1, '鍑哄簱', '2', 'wms_inventory_history_type', NULL, 'danger', 'N', '1', 'admin', '2024-08-07 14:13:36', 'wms2_admin', '2024-09-27 10:53:39', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1821067181917732866, 2, '绉诲簱', '3', 'wms_inventory_history_type', NULL, 'warning', 'N', '1', 'admin', '2024-08-07 14:13:45', 'wms2_admin', '2024-09-27 10:54:01', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1821067222455681026, 3, '鐩樺簱', '4', 'wms_inventory_history_type', NULL, 'primary', 'N', '1', 'admin', '2024-08-07 14:13:54', 'admin', '2024-08-07 14:58:06', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1822820748966006786, 0, '鏈Щ搴?, '0', 'wms_movement_status', NULL, 'info', 'N', '1', 'admin', '2024-08-12 10:21:48', 'admin', '2024-08-12 10:21:48', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1822820794864275457, 1, '宸茬Щ搴?, '1', 'wms_movement_status', NULL, 'primary', 'N', '1', 'admin', '2024-08-12 10:21:59', 'admin', '2024-08-12 10:21:59', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1822820855526494210, 2, '浣滃簾', '-1', 'wms_movement_status', NULL, 'danger', 'N', '1', 'admin', '2024-08-12 10:22:13', 'admin', '2024-08-12 10:22:13', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1823182345731391489, 0, '寰呯洏搴?, '0', 'wms_check_status', NULL, 'info', 'N', '1', 'admin', '2024-08-13 10:18:39', 'admin', '2024-08-13 10:18:39', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1823182400756465666, 1, '宸茬洏搴?, '1', 'wms_check_status', NULL, 'primary', 'N', '1', 'admin', '2024-08-13 10:18:52', 'admin', '2024-08-13 10:18:52', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1823182471136886786, 2, '浣滃簾', '-1', 'wms_check_status', NULL, 'danger', 'N', '1', 'admin', '2024-08-13 10:19:09', 'admin', '2024-08-13 10:19:09', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1882339863081852929, 0, '閲囪喘鍏ュ簱', '5', 'wms_inventory_history_type', NULL, 'success', 'N', '1', 'admin', '2025-01-23 16:09:30', 'admin', '2025-01-23 16:09:30', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1882340243366813697, 2, '閿€鍞嚭搴?, '6', 'wms_inventory_history_type', NULL, 'danger', 'N', '1', 'admin', '2025-01-23 16:11:01', 'admin', '2025-09-23 16:11:01', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1887784663876640769, 0, '鏈鏍?, '0', 'doc_checked_status', NULL, 'default', 'N', '1', 'admin', '2025-02-07 16:45:12', 'admin', '2025-09-07 16:45:23', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1887784806642360321, 1, '瀹℃牳涓嶉€氳繃', '500', 'doc_checked_status', NULL, 'danger', 'N', '1', 'admin', '2025-02-07 16:45:46', 'admin', '2025-09-07 16:45:46', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1887784886057312258, 2, '瀹℃牳閫氳繃', '200', 'doc_checked_status', NULL, 'success', 'N', '1', 'admin', '2025-02-07 16:46:05', 'admin', '2025-09-07 16:46:15', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1892437377550241794, 0, '鏀舵鍗?, 'ReceiptVoucher', 'trans_type', NULL, 'default', 'N', '1', 'admin', '2025-02-20 12:53:25', 'admin', '2025-09-20 12:53:25', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1892457266574835714, 0, '鏈鏍?, '0', 'finish_status', NULL, 'warning', 'N', '1', 'admin', '2025-02-20 14:12:27', 'admin', '2025-05-20 13:37:50', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1892457327853617154, 1, '宸插鏍?, '1', 'finish_status', NULL, 'success', 'N', '1', 'admin', '2025-02-20 14:12:42', 'admin', '2025-05-20 13:37:58', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1902619096513376257, 9, '閲囪喘閫€璐?, '7', 'wms_inventory_history_type', NULL, 'danger', 'N', '1', 'admin', '2025-03-20 15:11:56', 'admin', '2025-09-26 09:33:56', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1902897057707679745, 1, '鏈夐€€璐?, '1', 'refund_status', NULL, 'warning', 'N', '1', 'admin', '2025-03-21 09:36:27', 'admin', '2025-03-21 09:36:27', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1902897136296353794, 0, '鏃犻€€璐?, '0', 'refund_status', NULL, 'success', 'N', '1', 'admin', '2025-03-21 09:36:46', 'admin', '2025-03-21 09:36:46', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1904075748731105281, 16, '閿€鍞€€璐?, '8', 'wms_inventory_history_type', NULL, 'success', 'N', '1', 'admin', '2025-03-24 15:40:09', 'admin', '2025-09-26 09:34:10', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1920661667344908289, 3, '鍏ュ簱瀹屾垚', '2', 'wms_receipt_status', NULL, 'success', 'N', '1', 'admin', '2025-05-09 10:06:40', 'admin', '2025-09-21 10:29:21', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1920733559518945281, 2, '鍑哄簱瀹屾垚', '2', 'wms_shipment_status', NULL, 'success', 'N', '1', 'admin', '2025-05-09 14:52:21', 'admin', '2025-09-21 10:30:32', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (1955071439921717250, 0, 'TCP鍗忚', 'TCP', 'iiot_protocol_type', NULL, 'default', 'N', '1', 'admin', '2025-08-12 08:58:49', 'admin', '2025-08-12 08:58:49', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1955071536902414338, 1, 'UDP鍗忚', 'UDP', 'iiot_protocol_type', NULL, 'default', 'N', '1', 'admin', '2025-08-12 08:59:13', 'admin', '2025-08-12 08:59:18', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1955071606917931009, 2, 'MQTT鍗忚', 'MQTT', 'iiot_protocol_type', NULL, 'default', 'N', '1', 'admin', '2025-08-12 08:59:29', 'admin', '2025-08-12 08:59:29', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1955071645891403778, 3, 'HTTP鍗忚', 'HTTP', 'iiot_protocol_type', NULL, 'default', 'N', '1', 'admin', '2025-08-12 08:59:39', 'admin', '2025-08-12 08:59:39', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1955071681542987778, 4, 'SERIAL鍗忚', 'SERIAL', 'iiot_protocol_type', NULL, 'default', 'N', '1', 'admin', '2025-08-12 08:59:47', 'admin', '2025-08-12 08:59:47', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1955071722118684674, 5, 'WEBSOCKET鍗忚', 'WEBSOCKET', 'iiot_protocol_type', NULL, 'default', 'N', '1', 'admin', '2025-08-12 08:59:57', 'admin', '2025-08-12 08:59:57', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1955072492822044673, 0, '璁惧鐩磋繛', 'Direct', 'iiot_protocol_gateway_type', NULL, 'default', 'N', '1', 'admin', '2025-08-12 09:03:00', 'admin', '2025-08-12 09:03:00', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1955072532953145346, 1, '杈圭紭缃戝叧', 'Gateway', 'iiot_protocol_gateway_type', NULL, 'default', 'N', '1', 'admin', '2025-08-12 09:03:10', 'admin', '2025-08-12 09:03:10', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1955073008071319553, 0, '鏈嶅姟绔?, 'Server', 'iiot_protocol_connection_type', NULL, 'default', 'N', '1', 'admin', '2025-08-12 09:05:03', 'admin', '2025-08-12 09:05:03', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1955073072835567618, 1, '瀹㈡埛绔?, 'Client', 'iiot_protocol_connection_type', NULL, 'default', 'N', '1', 'admin', '2025-08-12 09:05:19', 'admin', '2025-08-12 09:05:19', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1955142827336507393, 1, '鍦ㄧ嚎', 'online', 'iiot_gateway_status', NULL, 'primary', 'N', '1', 'admin', '2025-08-12 13:42:29', 'admin', '2025-08-12 14:16:02', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1955142874438541314, 2, '绂荤嚎', 'offline', 'iiot_gateway_status', NULL, 'warning', 'N', '1', 'admin', '2025-08-12 13:42:41', 'admin', '2025-08-12 14:16:07', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1955436708093304834, 1, '璁㈤槄', 'publish', 'iiot_acl_action', NULL, 'default', 'N', '1', 'admin', '2025-08-13 09:10:16', 'admin', '2025-08-13 09:10:16', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1955436806135160833, 2, '鍙戝竷', 'subscribe', 'iiot_acl_action', NULL, 'default', 'N', '1', 'admin', '2025-08-13 09:10:40', 'admin', '2025-08-13 09:10:40', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1955436873579569154, 3, '璁㈤槄+鍙戝竷', 'all', 'iiot_acl_action', NULL, 'default', 'N', '1', 'admin', '2025-08-13 09:10:56', 'admin', '2025-08-13 09:10:56', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1955437267022061570, 1, '鍏佽', 'allow', 'iiot_acl_permission', NULL, 'default', 'N', '1', 'admin', '2025-08-13 09:12:29', 'admin', '2025-08-13 09:12:29', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1955437365009391618, 2, '鎷掔粷', 'deny', 'iiot_acl_permission', NULL, 'default', 'N', '1', 'admin', '2025-08-13 09:12:53', 'admin', '2025-08-13 09:12:53', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1955518941294555137, 1, '灞炴€?, 'PROPERTY', 'iiot_model_definition_type', NULL, 'default', 'N', '1', 'admin', '2025-08-13 14:37:02', 'admin', '2025-08-13 14:37:02', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1955518982298071042, 2, '鏈嶅姟', 'SERVICE', 'iiot_model_definition_type', NULL, 'default', 'N', '1', 'admin', '2025-08-13 14:37:12', 'admin', '2025-08-13 14:37:33', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1955519027072266242, 3, '浜嬩欢', 'EVENT', 'iiot_model_definition_type', NULL, 'default', 'N', '1', 'admin', '2025-08-13 14:37:23', 'admin', '2025-08-13 14:37:23', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1957960252933124097, 0, '涓嶅睍绀?, '0', 'iiot_model_definition_show', NULL, 'default', 'N', '1', 'admin', '2025-08-20 08:17:56', 'admin', '2025-08-20 08:17:56', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1957960286223314945, 0, '灞曠ず', '1', 'iiot_model_definition_show', NULL, 'default', 'N', '1', 'admin', '2025-08-20 08:18:04', 'admin', '2025-08-20 08:18:04', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1957988580335390722, 0, '绂荤嚎', '0', 'iiot_product_status', NULL, 'warning', 'N', '1', 'admin', '2025-08-20 10:10:30', 'admin', '2025-08-20 10:10:39', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1957988678289166337, 1, '鍦ㄧ嚎', '1', 'iiot_product_status', NULL, 'primary', 'N', '1', 'admin', '2025-08-20 10:10:53', 'admin', '2025-08-20 10:10:57', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1959168763851485185, 6, '澶у崕SDK鍗忚', 'DHSDK', 'iiot_protocol_type', NULL, 'default', 'N', '1', 'admin', '2025-08-23 16:20:08', 'admin', '2025-08-23 16:20:08', '澶у崕SDK鍗忚', 1);
INSERT INTO `sys_dict_data` VALUES (1961358222118477826, 1, '鏈煡', '-1', 'iiot_access_card_status', NULL, 'default', 'N', '0', 'admin', '2025-08-29 17:20:15', 'admin', '2025-08-30 10:46:29', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1961358264774549506, 2, '姝ｅ父', '0', 'iiot_access_card_status', NULL, 'default', 'N', '1', 'admin', '2025-08-29 17:20:25', 'admin', '2025-08-29 17:20:25', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1961358328720908289, 3, '鎸傚け', '1', 'iiot_access_card_status', NULL, 'default', 'N', '0', 'admin', '2025-08-29 17:20:41', 'admin', '2025-08-30 12:47:58', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1961358402817482753, 4, '娉ㄩ攢', '2', 'iiot_access_card_status', NULL, 'default', 'N', '0', 'admin', '2025-08-29 17:20:58', 'admin', '2025-08-30 10:47:54', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1961358489144647681, 5, '鍐荤粨', '3', 'iiot_access_card_status', NULL, 'default', 'N', '0', 'admin', '2025-08-29 17:21:19', 'admin', '2025-08-30 10:48:02', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1961358529535795202, 6, '娆犺垂', '4', 'iiot_access_card_status', NULL, 'default', 'N', '0', 'admin', '2025-08-29 17:21:28', 'admin', '2025-08-30 10:48:05', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1961358594820136962, 7, '閫炬湡', '5', 'iiot_access_card_status', NULL, 'default', 'N', '0', 'admin', '2025-08-29 17:21:44', 'admin', '2025-08-30 10:48:07', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1961358676860723202, 8, '棰勬瑺璐?, '6', 'iiot_access_card_status', NULL, 'default', 'N', '0', 'admin', '2025-08-29 17:22:04', 'admin', '2025-08-30 10:48:10', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1961358890489208834, 1, '鏈煡', '-1', 'iiot_access_card_type', NULL, 'default', 'N', '0', 'admin', '2025-08-29 17:22:54', 'admin', '2025-08-30 10:48:29', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1961358931098460161, 2, '涓€鑸崱', '0', 'iiot_access_card_type', NULL, 'default', 'N', '1', 'admin', '2025-08-29 17:23:04', 'admin', '2025-08-29 17:23:04', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1961358992024920065, 3, 'VIP鍗?, '1', 'iiot_access_card_type', NULL, 'default', 'N', '1', 'admin', '2025-08-29 17:23:19', 'admin', '2025-08-29 17:23:19', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1961359038325841921, 4, '鏉ュ鍗?, '2', 'iiot_access_card_type', NULL, 'default', 'N', '1', 'admin', '2025-08-29 17:23:30', 'admin', '2025-08-29 17:23:30', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1961359082110181378, 5, '宸￠€诲崱', '3', 'iiot_access_card_type', NULL, 'default', 'N', '0', 'admin', '2025-08-29 17:23:40', 'admin', '2025-08-30 10:48:37', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1961359130554392578, 6, '榛戝悕鍗曞崱', '4', 'iiot_access_card_type', NULL, 'default', 'N', '0', 'admin', '2025-08-29 17:23:52', 'admin', '2025-08-30 10:48:40', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1961359176700125185, 7, '鑳佽揩鍗?, '5', 'iiot_access_card_type', NULL, 'default', 'N', '0', 'admin', '2025-08-29 17:24:03', 'admin', '2025-08-30 10:48:44', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1961359210392969217, 8, '宸℃鍗?, '6', 'iiot_access_card_type', NULL, 'default', 'N', '0', 'admin', '2025-08-29 17:24:11', 'admin', '2025-08-30 10:48:47', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1961359251031580673, 9, '姣嶅崱', '7', 'iiot_access_card_type', NULL, 'default', 'N', '0', 'admin', '2025-08-29 17:24:20', 'admin', '2025-08-30 10:48:50', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1961618418589863937, 1, '涓嶆槸', '0', 'iiot_access_card_first_enter', NULL, 'default', 'N', '1', 'admin', '2025-08-30 10:34:11', 'admin', '2025-08-30 10:34:11', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1961618463385030657, 2, '鏄?, '1', 'iiot_access_card_first_enter', '', 'default', 'N', '1', 'admin', '2025-08-30 10:34:21', 'admin', '2025-08-30 10:34:21', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1961618577814032385, 1, '涓嶆槸', '0', 'iiot_access_card_enable', NULL, 'default', 'N', '1', 'admin', '2025-08-30 10:34:49', 'admin', '2025-08-30 10:34:49', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1961618602426208257, 2, '鏄?, '1', 'iiot_access_card_enable', NULL, 'default', 'N', '1', 'admin', '2025-08-30 10:34:55', 'admin', '2025-08-30 10:34:55', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962332767772012545, 1, '涓嵂', 'zy', 'brain_enterprise_development', NULL, 'default', 'N', '1', 'admin', '2025-09-01 09:52:45', 'admin', '2025-09-01 09:52:45', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962332820750266369, 2, '鍖栧鑽?, 'hxy', 'brain_enterprise_development', NULL, 'default', 'N', '1', 'admin', '2025-09-01 09:52:58', 'admin', '2025-09-01 09:52:58', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962332877851521025, 3, '鐢熺墿鍒跺搧', 'swzp', 'brain_enterprise_development', NULL, 'default', 'N', '1', 'admin', '2025-09-01 09:53:11', 'admin', '2025-09-01 09:53:11', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962332978502234113, 4, '鐢熺墿鍒跺搧鑽缁勫悎', 'swzpyxzh', 'brain_enterprise_development', NULL, 'default', 'N', '1', 'admin', '2025-09-01 09:53:35', 'admin', '2025-09-01 09:53:35', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962333048245121026, 5, '浣撳璇婃柇', 'twzd', 'brain_enterprise_development', NULL, 'default', 'N', '1', 'admin', '2025-09-01 09:53:52', 'admin', '2025-09-01 09:53:52', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962333115416899585, 6, '璐ㄨ氨浠?, 'zpy', 'brain_enterprise_development', NULL, 'default', 'N', '1', 'admin', '2025-09-01 09:54:08', 'admin', '2025-09-01 09:54:08', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962333195641352193, 7, '妞嶅叆寮忕缁忚皟鎺?, 'zrssjdk', 'brain_enterprise_development', NULL, 'default', 'N', '1', 'admin', '2025-09-01 09:54:27', 'admin', '2025-09-01 09:54:27', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962333288922673154, 8, '搴峰绫诲尰鐤楀櫒姊?, 'kflylqx', 'brain_enterprise_development', NULL, 'default', 'N', '1', 'admin', '2025-09-01 09:54:49', 'admin', '2025-09-01 09:54:49', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962333355251396610, 9, '鐢熷懡寰幆杈呭姪', 'smxhfz', 'brain_enterprise_development', NULL, 'default', 'N', '1', 'admin', '2025-09-01 09:55:05', 'admin', '2025-09-01 09:55:05', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962333430614650882, 10, '榻跨鍣ㄦ潗', 'ckqc', 'brain_enterprise_development', NULL, 'default', 'N', '1', 'admin', '2025-09-01 09:55:23', 'admin', '2025-09-01 09:55:23', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962336212314812418, 3, '澶у仴搴锋秷璐?, 'djkxf', 'brain_enterprise_type', NULL, 'default', 'N', '1', 'admin', '2025-09-01 10:06:26', 'admin', '2025-09-01 10:08:31', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962336318569115650, 1, '鍒涙柊鑽?, 'cxy', 'brain_enterprise_type', NULL, 'default', 'N', '1', 'admin', '2025-09-01 10:06:51', 'admin', '2025-09-01 10:06:51', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962336700531798018, 2, '鍖荤枟鍣ㄦ', 'yyqx', 'brain_enterprise_type', NULL, 'default', 'N', '1', 'admin', '2025-09-01 10:08:23', 'admin', '2025-09-01 10:08:23', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962336791795658753, 4, 'CRO/CDMO', 'CRO/CDMO', 'brain_enterprise_type', NULL, 'default', 'N', '1', 'admin', '2025-09-01 10:08:44', 'admin', '2025-09-01 10:08:44', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962336838037860353, 5, '鐮旂┒闄?, 'yjy', 'brain_enterprise_type', NULL, 'default', 'N', '1', 'admin', '2025-09-01 10:08:55', 'admin', '2025-09-01 10:08:55', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962336913174622209, 6, '鍖婚櫌', 'yy', 'brain_enterprise_type', NULL, 'default', 'N', '1', 'admin', '2025-09-01 10:09:13', 'admin', '2025-09-01 10:09:13', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962337260643348482, 7, '鍩洪噾鏈烘瀯', 'jjjg', 'brain_enterprise_type', NULL, 'default', 'N', '1', 'admin', '2025-09-01 10:10:36', 'admin', '2025-09-01 10:10:36', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962396532353896449, 1, '浼佷笟鍒涙柊', 'qy_cxyf_cgzh', 'brain_policy_type', NULL, 'default', 'N', '1', 'admin', '2025-09-01 14:06:08', 'admin', '2025-09-04 10:11:03', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962396911414120450, 2, '浼佷笟鍙戝睍', 'qy_zdzq_jjfz', 'brain_policy_type', NULL, 'default', 'N', '1', 'admin', '2025-09-01 14:07:38', 'admin', '2025-09-04 10:11:09', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962397116968570882, 3, '鏋勫缓浜т笟鐢熸€?, 'yljkcy_jkst_qmts', 'brain_policy_type', NULL, 'default', 'N', '1', 'admin', '2025-09-01 14:08:27', 'admin', '2025-09-04 10:11:17', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962405542574526466, 1, '鐪佺骇閲嶇偣瀹為獙瀹?, 'sjzdsys', 'brain_platform_type', NULL, 'default', 'N', '1', 'admin', '2025-09-01 14:41:56', 'admin', '2025-09-01 14:41:56', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962405591157149697, 2, '鏂板瀷鐮斿彂鏈烘瀯', 'xxyfjg', 'brain_platform_type', NULL, 'default', 'N', '1', 'admin', '2025-09-01 14:42:07', 'admin', '2025-09-01 14:42:07', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962697239871184897, 1, '鏄?, '1', 'brain_enterprise_important', NULL, 'default', 'N', '1', 'admin', '2025-09-02 10:01:02', 'admin', '2025-09-02 10:01:02', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962697288416059394, 2, '鍚?, '0', 'brain_enterprise_important', NULL, 'default', 'N', '1', 'admin', '2025-09-02 10:01:13', 'admin', '2025-09-02 10:01:13', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962724301830971393, 1, '鍒跺墏鐮旂┒', 'zjyj', 'brain_technology_label', NULL, 'default', 'N', '1', 'admin', '2025-09-02 11:48:34', 'admin', '2025-09-02 11:48:34', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962724384773332993, 2, '缁嗚優/鍩哄洜娌荤枟鑽墿寮€鍙?, 'xbjyzlywyf', 'brain_technology_label', NULL, 'default', 'N', '1', 'admin', '2025-09-02 11:48:54', 'admin', '2025-09-02 11:48:54', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962724437755781121, 3, '鑽墿鍙戠幇', 'ywfx', 'brain_technology_label', NULL, 'default', 'N', '1', 'admin', '2025-09-02 11:49:06', 'admin', '2025-09-02 11:49:06', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962724514461212674, 4, '鍣ㄦ璁捐寮€鍙?, 'qxsjkf', 'brain_technology_label', NULL, 'default', 'N', '1', 'admin', '2025-09-02 11:49:25', 'admin', '2025-09-02 11:49:25', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962724573923860482, 5, '涓村簥鏈嶅姟', 'lcfw', 'brain_technology_label', NULL, 'default', 'N', '1', 'admin', '2025-09-02 11:49:39', 'admin', '2025-09-02 11:49:39', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962724650067255297, 6, 'API鐮旂┒', 'APIyj', 'brain_technology_label', NULL, 'default', 'N', '1', 'admin', '2025-09-02 11:49:57', 'admin', '2025-09-02 11:49:57', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962724718400856065, 7, '鑽墿瀹夊叏鎬ц瘎浠?, 'ywaqxpj', 'brain_technology_label', NULL, 'default', 'N', '1', 'admin', '2025-09-02 11:50:13', 'admin', '2025-09-02 11:50:13', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962725110757023745, 1, '鍏釜鏈?, '6m', 'brain_technology_deadline', NULL, 'default', 'N', '1', 'admin', '2025-09-02 11:51:47', 'admin', '2025-09-02 11:51:47', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962725157800337409, 2, '涓€骞?, '1y', 'brain_technology_deadline', NULL, 'default', 'N', '1', 'admin', '2025-09-02 11:51:58', 'admin', '2025-09-02 11:51:58', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962725283428130818, 3, '闀挎湡', 'long', 'brain_technology_deadline', NULL, 'default', 'N', '1', 'admin', '2025-09-02 11:52:28', 'admin', '2025-09-02 11:52:28', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962754005816311810, 1, 'MQTT缃戝叧', '0', 'iiot_protocol_gateway_device_type', NULL, 'default', 'N', '1', 'admin', '2025-09-02 13:46:36', 'admin', '2025-09-02 13:46:36', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962754092839731201, 2, 'Modbus缃戝叧', '1', 'iiot_protocol_gateway_device_type', NULL, 'default', 'N', '1', 'admin', '2025-09-02 13:46:57', 'admin', '2025-09-02 13:46:57', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962775026040631298, 1, '鍚敤', 'ENABLED', 'iiot_modbus_model_task_status', NULL, 'primary', 'N', '1', 'admin', '2025-09-02 15:10:07', 'admin', '2025-09-02 15:25:28', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962775078700118017, 2, '绂佺敤', 'DISABLED', 'iiot_modbus_model_task_status', NULL, 'warning', 'N', '1', 'admin', '2025-09-02 15:10:20', 'admin', '2025-09-02 15:25:34', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962775129786740737, 3, '绂荤嚎', 'OFFLINE', 'iiot_modbus_model_task_status', NULL, 'danger', 'N', '1', 'admin', '2025-09-02 15:10:32', 'admin', '2025-09-02 15:25:38', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962780676523094017, 1, 'UINT16', 'UINT16', 'iiot_modbus_model_point_data_type', NULL, 'default', 'N', '1', 'admin', '2025-09-02 15:32:35', 'admin', '2025-09-02 15:32:35', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962780733221695490, 2, 'INT16', 'INT16', 'iiot_modbus_model_point_data_type', NULL, 'default', 'N', '1', 'admin', '2025-09-02 15:32:48', 'admin', '2025-09-02 15:32:48', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962780764632838146, 3, 'FLOAT32', 'FLOAT32', 'iiot_modbus_model_point_data_type', NULL, 'default', 'N', '1', 'admin', '2025-09-02 15:32:56', 'admin', '2025-09-02 15:32:56', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1962780803480481793, 4, 'COIL', 'COIL', 'iiot_modbus_model_point_data_type', NULL, 'default', 'N', '1', 'admin', '2025-09-02 15:33:05', 'admin', '2025-09-02 15:33:05', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1963041437761769474, 1, '涓村簥鈪犳湡', '1', 'brain_drug_phase', NULL, 'default', 'N', '1', 'admin', '2025-09-03 08:48:45', 'admin', '2025-09-03 08:48:45', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1963041476919791618, 2, '涓村簥鈪℃湡', '2', 'brain_drug_phase', NULL, 'default', 'N', '1', 'admin', '2025-09-03 08:48:54', 'admin', '2025-09-03 08:48:54', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1963041524143460353, 3, '涓村簥鈪㈡湡', '3', 'brain_drug_phase', NULL, 'default', 'N', '1', 'admin', '2025-09-03 08:49:06', 'admin', '2025-09-03 08:49:06', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1963041570297581570, 4, '涓婂競', '4', 'brain_drug_phase', NULL, 'default', 'N', '1', 'admin', '2025-09-03 08:49:17', 'admin', '2025-09-03 08:49:17', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1963041649767059457, 1, '浜岀被', '2', 'brain_instrument_level', NULL, 'default', 'N', '1', 'admin', '2025-09-03 08:49:36', 'admin', '2025-09-03 08:49:36', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1963041682117726210, 2, '涓夌被', '3', 'brain_instrument_level', NULL, 'default', 'N', '1', 'admin', '2025-09-03 08:49:43', 'admin', '2025-09-03 08:49:43', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1963048785389654017, 1, '棰嗗啗鍨嬩笓瀹?, 'leader', 'brain_talent_type', NULL, 'default', 'N', '1', 'admin', '2025-09-03 09:17:57', 'admin', '2025-09-03 09:17:57', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1963048923826851841, 2, '搴旂敤鍨嬩汉鎵?, 'practical', 'brain_talent_type', NULL, 'default', 'N', '1', 'admin', '2025-09-03 09:18:30', 'admin', '2025-09-03 09:19:07', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1964209065310130177, 1, '寮€鍏崇姸鎬?, 'SwitchStatus', 'modbus_aircondition_collect', NULL, 'default', 'N', '1', 'admin', '2025-09-06 14:08:29', 'admin', '2025-09-06 14:08:29', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1964209211905249281, 2, '妯″紡鐘舵€?, 'Modestatus', 'modbus_aircondition_collect', NULL, 'default', 'N', '1', 'admin', '2025-09-06 14:09:04', 'admin', '2025-09-06 14:09:04', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1964209302716125185, 3, '璁惧畾娓╁害', 'SetTemp', 'modbus_aircondition_collect', NULL, 'default', 'N', '1', 'admin', '2025-09-06 14:09:26', 'admin', '2025-09-06 14:09:26', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1964209400695066626, 4, '椋庨€熺姸鎬?, 'FanSpeed', 'modbus_aircondition_collect', NULL, 'default', 'N', '1', 'admin', '2025-09-06 14:09:49', 'admin', '2025-09-06 14:09:49', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1964210037763706882, 1, '鍐呮満寮€鍏?, 'Switch-Set', 'modbus_aircondition_control', NULL, 'default', 'N', '1', 'admin', '2025-09-06 14:12:21', 'admin', '2025-09-06 14:12:21', '0=鍏筹紝1=寮€', 1);
INSERT INTO `sys_dict_data` VALUES (1964210292001443842, 2, '鍐呮満妯″紡', 'Mode-Set', 'modbus_aircondition_control', NULL, 'default', 'N', '1', 'admin', '2025-09-06 14:13:22', 'admin', '2025-09-06 14:13:22', '1=鍒剁儹锛?-鍒跺喎锛?=閫侀锛?=闄ゆ箍', 1);
INSERT INTO `sys_dict_data` VALUES (1964210416597438465, 3, '鍐呮満娓╁害璁惧畾', 'Temp-Set', 'modbus_aircondition_control', NULL, 'default', 'N', '1', 'admin', '2025-09-06 14:13:51', 'admin', '2025-09-06 14:13:51', '17-30', 1);
INSERT INTO `sys_dict_data` VALUES (1964210662870192130, 4, '鍐呮満椋庨€?, 'Fan-Set', 'modbus_aircondition_control', NULL, 'default', 'N', '1', 'admin', '2025-09-06 14:14:50', 'admin', '2025-09-06 14:14:50', '0=鑷姩锛?=浣庯紝2=涓紝3=楂?, 1);
INSERT INTO `sys_dict_data` VALUES (1964980799952969730, 1, '鏄?, '1', 'control_scene_current', NULL, 'default', 'N', '1', 'admin', '2025-09-08 17:15:05', 'admin', '2025-09-08 17:15:23', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1964980859835047938, 2, '鍚?, '0', 'control_scene_current', NULL, 'default', 'N', '1', 'admin', '2025-09-08 17:15:19', 'admin', '2025-09-08 17:15:19', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1965319341380005889, 1, '鏈変汉:1/鏃犱汉:0', 'presence', 'control_scene_condition_type', NULL, 'default', 'N', '1', 'admin', '2025-09-09 15:40:20', 'admin', '2025-09-09 16:00:44', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1965319523911921666, 1, '=', '=', 'control_scene_condition_operator', NULL, 'default', 'N', '1', 'admin', '2025-09-09 15:41:03', 'admin', '2025-09-09 15:41:03', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1965602670291660802, 1, '鐓ф槑', 'Meter-01', 'control_home_energy_code_mobile', NULL, 'default', 'N', '1', 'admin', '2025-09-10 10:26:10', 'admin', '2025-09-10 10:26:10', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1969673655976103937, 4, '浜烘墠钀藉湴', 'rcld', 'brain_policy_type', NULL, 'default', 'N', '1', 'adminMESCY', '2025-09-21 16:02:49', 'adminMESCY', '2025-09-21 16:02:49', NULL, 1969270531557855233);
INSERT INTO `sys_dict_data` VALUES (1979108938404499458, 1, '鏄?, '1', 'iiot_websocket_model_point_broadcast', NULL, 'default', 'N', '1', 'admin', '2025-10-17 16:55:16', 'admin', '2025-10-17 16:55:16', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1979108977604464642, 2, '鍚?, '0', 'iiot_websocket_model_point_broadcast', NULL, 'default', 'N', '1', 'admin', '2025-10-17 16:55:25', 'admin', '2025-10-17 16:55:25', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1979356249156308993, 3, 'socket缃戝叧', '2', 'iiot_protocol_gateway_device_type', NULL, 'default', 'N', '1', 'admin', '2025-10-18 09:17:59', 'admin', '2025-10-18 09:18:07', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1981646494010609665, 1, '鏈紑濮?, '0', 'erp_project_status', NULL, 'info', 'N', '1', 'admin', '2025-10-24 16:58:36', 'admin', '2025-10-24 16:58:51', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1981646656925765634, 2, '杩涜涓?, '1', 'erp_project_status', NULL, 'primary', 'N', '1', 'admin', '2025-10-24 16:59:15', 'admin', '2025-10-24 16:59:15', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1981646706837983234, 3, '宸插畬鎴?, '2', 'erp_project_status', NULL, 'success', 'N', '1', 'admin', '2025-10-24 16:59:27', 'admin', '2025-10-24 16:59:27', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1981646769018540034, 4, '宸叉殏鍋?, '3', 'erp_project_status', NULL, 'danger', 'N', '1', 'admin', '2025-10-24 16:59:42', 'admin', '2025-10-24 16:59:42', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1981901773637849090, 1, '鏈紑濮?, '0', 'erp_progress_status', NULL, 'info', 'N', '1', 'admin', '2025-10-25 09:52:59', 'admin', '2025-10-25 09:52:59', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1981901834832744449, 2, '杩涜涓?, '1', 'erp_progress_status', NULL, 'primary', 'N', '1', 'admin', '2025-10-25 09:53:14', 'admin', '2025-10-25 09:53:40', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1981901901236965378, 3, '宸插畬鎴?, '2', 'erp_progress_status', NULL, 'success', 'N', '1', 'admin', '2025-10-25 09:53:30', 'admin', '2025-10-25 09:53:44', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1981902003385044993, 4, '宸叉殏鍋?, '3', 'erp_progress_status', NULL, 'danger', 'N', '1', 'admin', '2025-10-25 09:53:54', 'admin', '2025-10-25 09:53:54', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1998576117374078977, 1, '鍚堝悓閲囪喘', '1', 'purchase_order_type', NULL, 'default', 'N', '1', 'admin', '2025-12-10 10:10:53', 'admin', '2025-12-10 10:10:53', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1998576159233232898, 2, '鍐呴儴閲囪喘', '2', 'purchase_order_type', NULL, 'default', 'N', '1', 'admin', '2025-12-10 10:11:03', 'admin', '2025-12-10 10:11:03', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1999654702929358849, 1, '鏈鏍?, '0', 'erp_report_status', NULL, 'info', 'N', '1', 'admin', '2025-12-13 09:36:48', 'admin', '2025-12-13 09:37:14', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (1999654737666584578, 2, '宸插鏍?, '1', 'erp_report_status', NULL, 'success', 'N', '1', 'admin', '2025-12-13 09:36:56', 'admin', '2025-12-13 09:37:21', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (2003289892176216065, 1, '鏈鏍?, '0', 'purchase_order_finish_status', NULL, 'info', 'N', '1', 'admin', '2025-12-23 10:21:44', 'admin', '2025-12-23 10:21:44', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (2003290086519291906, 2, '閮ㄩ棬瀹℃牳閫氳繃', '1', 'purchase_order_finish_status', NULL, 'success', 'N', '1', 'admin', '2025-12-23 10:22:31', 'admin', '2025-12-24 14:15:10', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (2003290162545246209, 3, '鎬荤粡鍔炲鏍搁€氳繃', '2', 'purchase_order_finish_status', NULL, 'success', 'N', '1', 'admin', '2025-12-23 10:22:49', 'admin', '2025-12-23 10:22:49', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (2006910090942865409, 1, '鏄?, '1', 'brain_enterprise_silver_economy', NULL, 'default', 'N', '1', 'adminMESCY', '2026-01-02 10:07:07', 'adminMESCY', '2026-01-02 10:07:07', NULL, 1969270531557855233);
INSERT INTO `sys_dict_data` VALUES (2006910125403267074, 2, '鍚?, '0', 'brain_enterprise_silver_economy', NULL, 'default', 'N', '1', 'adminMESCY', '2026-01-02 10:07:15', 'adminMESCY', '2026-01-02 10:07:15', NULL, 1969270531557855233);
INSERT INTO `sys_dict_data` VALUES (2006910719520215041, 1, '涓滅洓鎱ц胺', 'dongShengHuiGu', 'brain_enterprise_location', NULL, 'default', 'N', '1', 'admin', '2026-01-02 10:09:37', 'admin', '2026-01-02 10:09:37', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (2006910758321721346, 2, '鐢熷懡鍋ュ悍宀?, 'lifeHealthIsland', 'brain_enterprise_location', NULL, 'default', 'N', '1', 'admin', '2026-01-02 10:09:46', 'adminMESCY', '2026-01-02 10:42:39', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (2006911570104094722, 1, '鏄?, '1', 'brain_enterprise_life_health', NULL, 'default', 'N', '1', 'admin', '2026-01-02 10:13:00', 'admin', '2026-01-02 10:13:00', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (2006911593273430017, 2, '鍚?, '0', 'brain_enterprise_life_health', NULL, 'default', 'N', '1', 'admin', '2026-01-02 10:13:05', 'admin', '2026-01-02 10:13:05', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (2006913763125301249, 1, '閾跺彂缁忔祹浼佷笟', 'silverEconomy', 'brain_enterprise_category', NULL, 'default', 'N', '1', 'admin', '2026-01-02 10:21:42', 'admin', '2026-01-02 10:21:42', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (2006913803763912706, 2, '鍏ㄧ敓鍛藉懆鏈熶紒涓?, 'fullLifecycle', 'brain_enterprise_category', NULL, 'default', 'N', '1', 'admin', '2026-01-02 10:21:52', 'admin', '2026-01-02 10:21:52', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (2006915719659065345, 1, '宸插叆椹?, 'settled', 'brain_enterprise_progress', NULL, 'default', 'N', '1', 'admin', '2026-01-02 10:29:29', 'admin', '2026-01-02 10:29:29', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (2006915754102689793, 2, '鎷熷叆椹?, 'intended', 'brain_enterprise_progress', NULL, 'default', 'N', '1', 'admin', '2026-01-02 10:29:37', 'admin', '2026-01-02 10:29:37', NULL, 1);

-- ----------------------------
-- Table structure for sys_dict_type
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type`  (
  `dict_id` bigint NOT NULL COMMENT '瀛楀吀涓婚敭',
  `dict_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '瀛楀吀鍚嶇О',
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '瀛楀吀绫诲瀷',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0' COMMENT '鐘舵€侊紙0姝ｅ父 1鍋滅敤锛?,
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '鍒涘缓鑰?,
  `create_time` datetime NULL DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '鏇存柊鑰?,
  `update_time` datetime NULL DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '澶囨敞',
  `create_by_id` bigint NULL DEFAULT NULL COMMENT '鍒涘缓浜篒D',
  PRIMARY KEY (`dict_id`) USING BTREE,
  UNIQUE INDEX `dict_type`(`dict_type` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '瀛楀吀绫诲瀷琛? ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dict_type
-- ----------------------------
INSERT INTO `sys_dict_type` VALUES (1, '鐢ㄦ埛鎬у埆', 'sys_user_sex', '1', 'admin', '2024-06-13 16:06:35', '', NULL, '鐢ㄦ埛鎬у埆鍒楄〃', NULL);
INSERT INTO `sys_dict_type` VALUES (2, '鑿滃崟鐘舵€?, 'sys_show_hide', '1', 'admin', '2024-06-13 16:06:35', '', NULL, '鑿滃崟鐘舵€佸垪琛?, NULL);
INSERT INTO `sys_dict_type` VALUES (3, '绯荤粺寮€鍏?, 'sys_normal_disable', '1', 'admin', '2024-06-13 16:06:35', '', NULL, '绯荤粺寮€鍏冲垪琛?, NULL);
INSERT INTO `sys_dict_type` VALUES (6, '绯荤粺鏄惁', 'sys_yes_no', '1', 'admin', '2024-06-13 16:06:35', '', NULL, '绯荤粺鏄惁鍒楄〃', NULL);
INSERT INTO `sys_dict_type` VALUES (7, '閫氱煡绫诲瀷', 'sys_notice_type', '1', 'admin', '2024-06-13 16:06:35', '', NULL, '閫氱煡绫诲瀷鍒楄〃', NULL);
INSERT INTO `sys_dict_type` VALUES (8, '閫氱煡鐘舵€?, 'sys_notice_status', '1', 'admin', '2024-06-13 16:06:35', '', NULL, '閫氱煡鐘舵€佸垪琛?, NULL);
INSERT INTO `sys_dict_type` VALUES (9, '鎿嶄綔绫诲瀷', 'sys_oper_type', '1', 'admin', '2024-06-13 16:06:35', '', NULL, '鎿嶄綔绫诲瀷鍒楄〃', NULL);
INSERT INTO `sys_dict_type` VALUES (10, '绯荤粺鐘舵€?, 'sys_common_status', '1', 'admin', '2024-06-13 16:06:36', '', NULL, '鐧诲綍鐘舵€佸垪琛?, NULL);

-- ----------------------------
-- Table structure for sys_logininfor
-- ----------------------------
DROP TABLE IF EXISTS `sys_logininfor`;
CREATE TABLE `sys_logininfor`  (
  `info_id` bigint NOT NULL COMMENT '璁块棶ID',
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '鐢ㄦ埛璐﹀彿',
  `ipaddr` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '鐧诲綍IP鍦板潃',
  `login_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '鐧诲綍鍦扮偣',
  `browser` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '娴忚鍣ㄧ被鍨?,
  `os` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '鎿嶄綔绯荤粺',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0' COMMENT '鐧诲綍鐘舵€侊紙0鎴愬姛 1澶辫触锛?,
  `msg` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '鎻愮ず娑堟伅',
  `login_time` datetime NULL DEFAULT NULL COMMENT '璁块棶鏃堕棿',
  `create_by_id` bigint NULL DEFAULT NULL COMMENT '鍒涘缓浜篒D',
  PRIMARY KEY (`info_id`) USING BTREE,
  INDEX `idx_sys_logininfor_s`(`status` ASC) USING BTREE,
  INDEX `idx_sys_logininfor_lt`(`login_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '绯荤粺璁块棶璁板綍' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_logininfor
-- ----------------------------

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `menu_id` bigint NOT NULL COMMENT '鑿滃崟ID',
  `menu_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '鑿滃崟鍚嶇О',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '鐖惰彍鍗旾D',
  `order_num` int NULL DEFAULT 0 COMMENT '鏄剧ず椤哄簭',
  `path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '璺敱鍦板潃',
  `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '缁勪欢璺緞',
  `query_param` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '璺敱鍙傛暟',
  `is_frame` int NULL DEFAULT 1 COMMENT '鏄惁涓哄閾撅紙0鏄?1鍚︼級',
  `is_cache` int NULL DEFAULT 0 COMMENT '鏄惁缂撳瓨锛?缂撳瓨 1涓嶇紦瀛橈級',
  `menu_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '鑿滃崟绫诲瀷锛圡鐩綍 C鑿滃崟 F鎸夐挳锛?,
  `visible` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0' COMMENT '鏄剧ず鐘舵€侊紙0鏄剧ず 1闅愯棌锛?,
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0' COMMENT '鑿滃崟鐘舵€侊紙0姝ｅ父 1鍋滅敤锛?,
  `perms` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鏉冮檺鏍囪瘑',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '#' COMMENT '鑿滃崟鍥炬爣',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '鍒涘缓鑰?,
  `create_time` datetime NULL DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '鏇存柊鑰?,
  `update_time` datetime NULL DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '澶囨敞',
  `create_by_id` bigint NULL DEFAULT NULL COMMENT '鍒涘缓浜篒D',
  PRIMARY KEY (`menu_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鑿滃崟鏉冮檺琛? ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1, '绯荤粺绠＄悊', 0, 110, 'system', NULL, '', 0, 0, 'M', '1', '1', '', 'system', 'admin', '2025-09-15 09:01:47', 'admin', '2024-08-20 13:45:48', '绯荤粺绠＄悊鐩綍', NULL);
INSERT INTO `sys_menu` VALUES (2, '绯荤粺鐩戞帶', 0, 120, 'monitor', NULL, '', 0, 0, 'M', '1', '1', '', 'monitor', 'admin', '2025-09-15 09:01:47', 'admin', '2024-08-20 13:45:57', '绯荤粺鐩戞帶鐩綍', NULL);
INSERT INTO `sys_menu` VALUES (100, '鐢ㄦ埛绠＄悊', 1, 1, 'user', 'system/user/index', '', 0, 0, 'C', '1', '1', 'system:user:list', 'user', 'admin', '2025-09-15 09:01:47', '', NULL, '鐢ㄦ埛绠＄悊鑿滃崟', NULL);
INSERT INTO `sys_menu` VALUES (101, '瑙掕壊绠＄悊', 1, 2, 'role', 'system/role/index', '', 0, 0, 'C', '1', '1', 'system:role:list', 'peoples', 'admin', '2025-09-15 09:01:47', '', NULL, '瑙掕壊绠＄悊鑿滃崟', NULL);
INSERT INTO `sys_menu` VALUES (102, '鑿滃崟绠＄悊', 1, 3, 'menu', 'system/menu/index', '', 0, 0, 'C', '1', '1', 'system:menu:list', 'tree-table', 'admin', '2025-09-15 09:01:47', '', NULL, '鑿滃崟绠＄悊鑿滃崟', NULL);
INSERT INTO `sys_menu` VALUES (103, '閮ㄩ棬绠＄悊', 1, 4, 'dept', 'system/dept/index', '', 0, 0, 'C', '1', '1', 'system:dept:list', 'tree', 'admin', '2025-09-15 09:01:47', '', NULL, '閮ㄩ棬绠＄悊鑿滃崟', NULL);
INSERT INTO `sys_menu` VALUES (104, '宀椾綅绠＄悊', 1, 5, 'post', 'system/post/index', '', 0, 0, 'C', '1', '1', 'system:post:list', 'post', 'admin', '2025-09-15 09:01:47', '', NULL, '宀椾綅绠＄悊鑿滃崟', NULL);
INSERT INTO `sys_menu` VALUES (105, '瀛楀吀绠＄悊', 1, 6, 'dict', 'system/dict/index', '', 0, 0, 'C', '1', '1', 'system:dict:list', 'dict', 'admin', '2025-09-15 09:01:47', '', NULL, '瀛楀吀绠＄悊鑿滃崟', NULL);
INSERT INTO `sys_menu` VALUES (106, '鍙傛暟璁剧疆', 1, 7, 'config', 'system/config/index', '', 0, 0, 'C', '1', '1', 'system:config:list', 'edit', 'admin', '2025-09-15 09:01:47', '', NULL, '鍙傛暟璁剧疆鑿滃崟', NULL);
INSERT INTO `sys_menu` VALUES (107, '閫氱煡鍏憡', 1, 8, 'notice', 'system/notice/index', '', 0, 0, 'C', '1', '1', 'system:notice:list', 'message', 'admin', '2025-09-15 09:01:47', '', NULL, '閫氱煡鍏憡鑿滃崟', NULL);
INSERT INTO `sys_menu` VALUES (108, '鏃ュ織绠＄悊', 0, 140, 'log', '', '', 0, 0, 'M', '1', '1', '', 'log', 'admin', '2025-09-15 09:01:47', 'admin', '2024-08-20 13:46:16', '鏃ュ織绠＄悊鑿滃崟', NULL);
INSERT INTO `sys_menu` VALUES (109, '鍦ㄧ嚎鐢ㄦ埛', 2, 1, 'online', 'monitor/online/index', '', 0, 0, 'C', '1', '1', 'monitor:online:list', 'online', 'admin', '2025-09-15 09:01:47', '', NULL, '鍦ㄧ嚎鐢ㄦ埛鑿滃崟', NULL);
INSERT INTO `sys_menu` VALUES (112, '缂撳瓨鍒楄〃', 2, 6, 'cacheList', 'monitor/cache/list', '', 0, 0, 'C', '1', '1', 'monitor:cache:list', 'redis-list', 'admin', '2025-09-15 09:01:47', '', NULL, '缂撳瓨鍒楄〃鑿滃崟', NULL);
INSERT INTO `sys_menu` VALUES (113, '缂撳瓨鐩戞帶', 2, 5, 'cache', 'monitor/cache/index', '', 0, 0, 'C', '1', '1', 'monitor:cache:list', 'redis', 'admin', '2025-09-15 09:01:47', '', NULL, '缂撳瓨鐩戞帶鑿滃崟', NULL);
INSERT INTO `sys_menu` VALUES (115, '浠ｇ爜鐢熸垚', 0, 130, 'gen', 'tool/gen/index', '', 0, 0, 'C', '1', '1', 'tool:gen:list', 'code', 'admin', '2025-09-15 09:01:47', 'admin', '2024-08-20 13:46:06', '浠ｇ爜鐢熸垚鑿滃崟', NULL);
INSERT INTO `sys_menu` VALUES (118, '鏂囦欢绠＄悊', 1, 10, 'oss', 'system/oss/index', '', 0, 0, 'C', '1', '1', 'system:oss:list', 'upload', 'admin', '2025-09-15 09:01:47', '', NULL, '鏂囦欢绠＄悊鑿滃崟', NULL);
INSERT INTO `sys_menu` VALUES (500, '鎿嶄綔鏃ュ織', 108, 1, 'operlog', 'monitor/operlog/index', '', 0, 0, 'C', '1', '1', 'monitor:operlog:list', 'form', 'admin', '2025-09-15 09:01:47', '', NULL, '鎿嶄綔鏃ュ織鑿滃崟', NULL);
INSERT INTO `sys_menu` VALUES (501, '鐧诲綍鏃ュ織', 108, 2, 'logininfor', 'monitor/logininfor/index', '', 0, 0, 'C', '1', '1', 'monitor:logininfor:list', 'logininfor', 'admin', '2025-09-15 09:01:47', '', NULL, '鐧诲綍鏃ュ織鑿滃崟', NULL);
INSERT INTO `sys_menu` VALUES (1001, '鐢ㄦ埛鏌ヨ', 100, 1, '', '', '', 0, 0, 'F', '1', '1', 'system:user:query', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1002, '鐢ㄦ埛鏂板', 100, 2, '', '', '', 0, 0, 'F', '1', '1', 'system:user:add', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1003, '鐢ㄦ埛淇敼', 100, 3, '', '', '', 0, 0, 'F', '1', '1', 'system:user:edit', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1004, '鐢ㄦ埛鍒犻櫎', 100, 4, '', '', '', 0, 0, 'F', '1', '1', 'system:user:remove', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1005, '鐢ㄦ埛瀵煎嚭', 100, 5, '', '', '', 0, 0, 'F', '1', '1', 'system:user:export', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1006, '鐢ㄦ埛瀵煎叆', 100, 6, '', '', '', 0, 0, 'F', '1', '1', 'system:user:import', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1007, '閲嶇疆瀵嗙爜', 100, 7, '', '', '', 0, 0, 'F', '1', '1', 'system:user:resetPwd', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1008, '瑙掕壊鏌ヨ', 101, 1, '', '', '', 0, 0, 'F', '1', '1', 'system:role:query', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1009, '瑙掕壊鏂板', 101, 2, '', '', '', 0, 0, 'F', '1', '1', 'system:role:add', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1010, '瑙掕壊淇敼', 101, 3, '', '', '', 0, 0, 'F', '1', '1', 'system:role:edit', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1011, '瑙掕壊鍒犻櫎', 101, 4, '', '', '', 0, 0, 'F', '1', '1', 'system:role:remove', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1012, '瑙掕壊瀵煎嚭', 101, 5, '', '', '', 0, 0, 'F', '1', '1', 'system:role:export', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1013, '鑿滃崟鏌ヨ', 102, 1, '', '', '', 0, 0, 'F', '1', '1', 'system:menu:query', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1014, '鑿滃崟鏂板', 102, 2, '', '', '', 0, 0, 'F', '1', '1', 'system:menu:add', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1015, '鑿滃崟淇敼', 102, 3, '', '', '', 0, 0, 'F', '1', '1', 'system:menu:edit', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1016, '鑿滃崟鍒犻櫎', 102, 4, '', '', '', 0, 0, 'F', '1', '1', 'system:menu:remove', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1017, '閮ㄩ棬鏌ヨ', 103, 1, '', '', '', 0, 0, 'F', '1', '1', 'system:dept:query', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1018, '閮ㄩ棬鏂板', 103, 2, '', '', '', 0, 0, 'F', '1', '1', 'system:dept:add', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1019, '閮ㄩ棬淇敼', 103, 3, '', '', '', 0, 0, 'F', '1', '1', 'system:dept:edit', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1020, '閮ㄩ棬鍒犻櫎', 103, 4, '', '', '', 0, 0, 'F', '1', '1', 'system:dept:remove', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1021, '宀椾綅鏌ヨ', 104, 1, '', '', '', 0, 0, 'F', '1', '1', 'system:post:query', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1022, '宀椾綅鏂板', 104, 2, '', '', '', 0, 0, 'F', '1', '1', 'system:post:add', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1023, '宀椾綅淇敼', 104, 3, '', '', '', 0, 0, 'F', '1', '1', 'system:post:edit', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1024, '宀椾綅鍒犻櫎', 104, 4, '', '', '', 0, 0, 'F', '1', '1', 'system:post:remove', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1025, '宀椾綅瀵煎嚭', 104, 5, '', '', '', 0, 0, 'F', '1', '1', 'system:post:export', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1026, '瀛楀吀鏌ヨ', 105, 1, '#', '', '', 0, 0, 'F', '1', '1', 'system:dict:query', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1027, '瀛楀吀鏂板', 105, 2, '#', '', '', 0, 0, 'F', '1', '1', 'system:dict:add', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1028, '瀛楀吀淇敼', 105, 3, '#', '', '', 0, 0, 'F', '1', '1', 'system:dict:edit', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1029, '瀛楀吀鍒犻櫎', 105, 4, '#', '', '', 0, 0, 'F', '1', '1', 'system:dict:remove', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1030, '瀛楀吀瀵煎嚭', 105, 5, '#', '', '', 0, 0, 'F', '1', '1', 'system:dict:export', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1031, '鍙傛暟鏌ヨ', 106, 1, '#', '', '', 0, 0, 'F', '1', '1', 'system:config:query', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1032, '鍙傛暟鏂板', 106, 2, '#', '', '', 0, 0, 'F', '1', '1', 'system:config:add', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1033, '鍙傛暟淇敼', 106, 3, '#', '', '', 0, 0, 'F', '1', '1', 'system:config:edit', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1034, '鍙傛暟鍒犻櫎', 106, 4, '#', '', '', 0, 0, 'F', '1', '1', 'system:config:remove', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1035, '鍙傛暟瀵煎嚭', 106, 5, '#', '', '', 0, 0, 'F', '1', '1', 'system:config:export', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1036, '鍏憡鏌ヨ', 107, 1, '#', '', '', 0, 0, 'F', '1', '1', 'system:notice:query', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1037, '鍏憡鏂板', 107, 2, '#', '', '', 0, 0, 'F', '1', '1', 'system:notice:add', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1038, '鍏憡淇敼', 107, 3, '#', '', '', 0, 0, 'F', '1', '1', 'system:notice:edit', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1039, '鍏憡鍒犻櫎', 107, 4, '#', '', '', 0, 0, 'F', '1', '1', 'system:notice:remove', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1040, '鎿嶄綔鏌ヨ', 500, 1, '#', '', '', 0, 0, 'F', '1', '1', 'monitor:operlog:query', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1041, '鎿嶄綔鍒犻櫎', 500, 2, '#', '', '', 0, 0, 'F', '1', '1', 'monitor:operlog:remove', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1042, '鏃ュ織瀵煎嚭', 500, 4, '#', '', '', 0, 0, 'F', '1', '1', 'monitor:operlog:export', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1043, '鐧诲綍鏌ヨ', 501, 1, '#', '', '', 0, 0, 'F', '1', '1', 'monitor:logininfor:query', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1044, '鐧诲綍鍒犻櫎', 501, 2, '#', '', '', 0, 0, 'F', '1', '1', 'monitor:logininfor:remove', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1045, '鏃ュ織瀵煎嚭', 501, 3, '#', '', '', 0, 0, 'F', '1', '1', 'monitor:logininfor:export', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1046, '鍦ㄧ嚎鏌ヨ', 109, 1, '#', '', '', 0, 0, 'F', '1', '1', 'monitor:online:query', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1047, '鎵归噺寮洪€€', 109, 2, '#', '', '', 0, 0, 'F', '1', '1', 'monitor:online:batchLogout', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1048, '鍗曟潯寮洪€€', 109, 3, '#', '', '', 0, 0, 'F', '1', '1', 'monitor:online:forceLogout', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1050, '璐︽埛瑙ｉ攣', 501, 4, '#', '', '', 0, 0, 'F', '1', '1', 'monitor:logininfor:unlock', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1055, '鐢熸垚鏌ヨ', 115, 1, '#', '', '', 0, 0, 'F', '1', '1', 'tool:gen:query', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1056, '鐢熸垚淇敼', 115, 2, '#', '', '', 0, 0, 'F', '1', '1', 'tool:gen:edit', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1057, '鐢熸垚鍒犻櫎', 115, 3, '#', '', '', 0, 0, 'F', '1', '1', 'tool:gen:remove', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1058, '瀵煎叆浠ｇ爜', 115, 2, '#', '', '', 0, 0, 'F', '1', '1', 'tool:gen:import', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1059, '棰勮浠ｇ爜', 115, 4, '#', '', '', 0, 0, 'F', '1', '1', 'tool:gen:preview', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1060, '鐢熸垚浠ｇ爜', 115, 5, '#', '', '', 0, 0, 'F', '1', '1', 'tool:gen:code', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1600, '鏂囦欢鏌ヨ', 118, 1, '#', '', '', 0, 0, 'F', '1', '1', 'system:oss:query', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1601, '鏂囦欢涓婁紶', 118, 2, '#', '', '', 0, 0, 'F', '1', '1', 'system:oss:upload', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1602, '鏂囦欢涓嬭浇', 118, 3, '#', '', '', 0, 0, 'F', '1', '1', 'system:oss:download', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1603, '鏂囦欢鍒犻櫎', 118, 4, '#', '', '', 0, 0, 'F', '1', '1', 'system:oss:remove', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1604, '閰嶇疆娣诲姞', 118, 5, '#', '', '', 0, 0, 'F', '1', '1', 'system:oss:add', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1605, '閰嶇疆缂栬緫', 118, 6, '#', '', '', 0, 0, 'F', '1', '1', 'system:oss:edit', '#', 'admin', '2025-09-15 09:01:47', '', NULL, '', NULL);
INSERT INTO `sys_menu` VALUES (1998209331864629250, '閫氱敤鏉冮檺API', 0, 1002, 'commonAuth', NULL, NULL, 0, 0, 'M', '0', '1', NULL, 'edit', 'admin', '2025-12-09 09:53:24', 'admin', '2025-12-09 09:53:24', '', 1);
INSERT INTO `sys_menu` VALUES (1998209631233077249, '鐢ㄦ埛鍒楄〃', 1998209331864629250, 1, '', NULL, NULL, 0, 0, 'F', '1', '1', 'system:user:list', '#', 'admin', '2025-12-09 09:54:36', 'admin', '2025-12-09 09:54:36', '', 1);
INSERT INTO `sys_menu` VALUES (1998210121547182082, '閮ㄩ棬鍒楄〃', 1998209331864629250, 2, '', NULL, NULL, 0, 0, 'F', '1', '1', 'system:dept:list', '#', 'admin', '2025-12-09 09:56:33', 'admin', '2025-12-09 09:56:33', '', 1);
INSERT INTO `sys_menu` VALUES (1998210317190492162, '鏂囦欢鍒楄〃', 1998209331864629250, 3, '', NULL, NULL, 0, 0, 'F', '1', '1', 'system:oss:list', '#', 'admin', '2025-12-09 09:57:19', 'admin', '2025-12-09 09:57:19', '', 1);
INSERT INTO `sys_menu` VALUES (1998210467015225345, '鏂囦欢鏌ヨ', 1998209331864629250, 4, '', NULL, NULL, 0, 0, 'F', '1', '1', 'system:oss:query', '#', 'admin', '2025-12-09 09:57:55', 'admin', '2025-12-09 09:57:55', '', 1);
INSERT INTO `sys_menu` VALUES (1998210567770796033, '鏂囦欢涓婁紶', 1998209331864629250, 5, '', NULL, NULL, 0, 0, 'F', '1', '1', 'system:oss:upload', '#', 'admin', '2025-12-09 09:58:19', 'admin', '2025-12-09 09:58:19', '', 1);
INSERT INTO `sys_menu` VALUES (1998210655570161665, '鏂囦欢涓嬭浇', 1998209331864629250, 6, '', NULL, NULL, 0, 0, 'F', '1', '1', 'system:oss:download', '#', 'admin', '2025-12-09 09:58:40', 'admin', '2025-12-09 09:58:40', '', 1);
INSERT INTO `sys_menu` VALUES (1998210747568025601, '鏂囦欢鍒犻櫎', 1998209331864629250, 7, '', NULL, NULL, 0, 0, 'F', '1', '1', 'system:oss:remove', '#', 'admin', '2025-12-09 09:59:02', 'admin', '2025-12-09 09:59:02', '', 1);

-- ----------------------------
-- Table structure for sys_notice
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice`  (
  `notice_id` bigint NOT NULL COMMENT '鍏憡ID',
  `notice_title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '鍏憡鏍囬',
  `notice_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '鍏憡绫诲瀷锛?閫氱煡 2鍏憡锛?,
  `notice_content` longblob NULL COMMENT '鍏憡鍐呭',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0' COMMENT '鍏憡鐘舵€侊紙0姝ｅ父 1鍏抽棴锛?,
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '鍒涘缓鑰?,
  `create_time` datetime NULL DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '鏇存柊鑰?,
  `update_time` datetime NULL DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '澶囨敞',
  `create_by_id` bigint NULL DEFAULT NULL COMMENT '鍒涘缓浜篒D',
  PRIMARY KEY (`notice_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '閫氱煡鍏憡琛? ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_notice
-- ----------------------------
INSERT INTO `sys_notice` VALUES (1, '娓╅Θ鎻愰啋锛氭祴璇曠増鏈彂甯冨暒', '2', 0x3C703EE696B0E78988E69CACE58685E5AEB93C2F703E, '1', 'admin', '2024-06-13 16:06:38', 'admin', '2025-07-25 09:14:55', '绠＄悊鍛?, NULL);

-- ----------------------------
-- Table structure for sys_oper_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_oper_log`;
CREATE TABLE `sys_oper_log`  (
  `oper_id` bigint NOT NULL COMMENT '鏃ュ織涓婚敭',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '妯″潡鏍囬',
  `business_type` int NULL DEFAULT 0 COMMENT '涓氬姟绫诲瀷锛?鍏跺畠 1鏂板 2淇敼 3鍒犻櫎锛?,
  `method` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '鏂规硶鍚嶇О',
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '璇锋眰鏂瑰紡',
  `operator_type` int NULL DEFAULT 0 COMMENT '鎿嶄綔绫诲埆锛?鍏跺畠 1鍚庡彴鐢ㄦ埛 2鎵嬫満绔敤鎴凤級',
  `oper_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '鎿嶄綔浜哄憳',
  `dept_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '閮ㄩ棬鍚嶇О',
  `oper_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '璇锋眰URL',
  `oper_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '涓绘満鍦板潃',
  `oper_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '鎿嶄綔鍦扮偣',
  `oper_param` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '璇锋眰鍙傛暟',
  `json_result` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '杩斿洖鍙傛暟',
  `status` int NULL DEFAULT 0 COMMENT '鎿嶄綔鐘舵€侊紙0寮傚父 1姝ｅ父锛?,
  `error_msg` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '閿欒娑堟伅',
  `oper_time` datetime NULL DEFAULT NULL COMMENT '鎿嶄綔鏃堕棿',
  `create_by_id` bigint NULL DEFAULT NULL COMMENT '鍒涘缓浜篒D',
  PRIMARY KEY (`oper_id`) USING BTREE,
  INDEX `idx_sys_oper_log_bt`(`business_type` ASC) USING BTREE,
  INDEX `idx_sys_oper_log_s`(`status` ASC) USING BTREE,
  INDEX `idx_sys_oper_log_ot`(`oper_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鎿嶄綔鏃ュ織璁板綍' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_oper_log
-- ----------------------------

-- ----------------------------
-- Table structure for sys_oss
-- ----------------------------
DROP TABLE IF EXISTS `sys_oss`;
CREATE TABLE `sys_oss`  (
  `oss_id` bigint NOT NULL COMMENT '瀵硅薄瀛樺偍涓婚敭',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '鏂囦欢鍚?,
  `original_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '鍘熷悕',
  `file_suffix` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '鏂囦欢鍚庣紑鍚?,
  `url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'URL鍦板潃',
  `create_time` datetime NULL DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '涓婁紶浜?,
  `update_time` datetime NULL DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '鏇存柊浜?,
  `service` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'minio' COMMENT '鏈嶅姟鍟?,
  `dept_id` bigint NULL DEFAULT NULL COMMENT '閮ㄩ棬',
  `create_by_id` bigint NULL DEFAULT NULL COMMENT '鍒涘缓浜篒D',
  PRIMARY KEY (`oss_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'OSS瀵硅薄瀛樺偍琛? ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_oss
-- ----------------------------

-- ----------------------------
-- Table structure for sys_oss_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_oss_config`;
CREATE TABLE `sys_oss_config`  (
  `oss_config_id` bigint NOT NULL COMMENT '涓诲缓',
  `config_key` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '閰嶇疆key',
  `access_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT 'accessKey',
  `secret_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '绉橀挜',
  `bucket_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '妗跺悕绉?,
  `prefix` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '鍓嶇紑',
  `endpoint` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '璁块棶绔欑偣',
  `domain` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '鑷畾涔夊煙鍚?,
  `is_https` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'N' COMMENT '鏄惁https锛圷=鏄?N=鍚︼級',
  `region` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '鍩?,
  `access_policy` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '1' COMMENT '妗舵潈闄愮被鍨?0=private 1=public 2=custom)',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '1' COMMENT '鏄惁榛樿锛?=鏄?1=鍚︼級',
  `ext1` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '鎵╁睍瀛楁',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '鍒涘缓鑰?,
  `create_time` datetime NULL DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '鏇存柊鑰?,
  `update_time` datetime NULL DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '澶囨敞',
  `create_by_id` bigint NULL DEFAULT NULL COMMENT '鍒涘缓浜篒D',
  PRIMARY KEY (`oss_config_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '瀵硅薄瀛樺偍閰嶇疆琛? ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_oss_config
-- ----------------------------
INSERT INTO `sys_oss_config` VALUES (1, 'minio', 'CHANGE_ME_ACCESS_KEY', 'CHANGE_ME_SECRET_KEY', 'bocoo', '', 'localhost:9000', '', 'N', '', '1', '0', '', 'admin', '2024-06-13 16:06:38', 'admin', '2026-05-20 10:27:20', '', NULL);
INSERT INTO `sys_oss_config` VALUES (2, 'qiniu', 'XXXXXXXXXXXXXXX', 'XXXXXXXXXXXXXXX', 'bocoo', '', 's3-cn-north-1.qiniucs.com', '', 'N', '', '1', '0', '', 'admin', '2024-06-13 16:06:38', 'admin', '2025-07-25 16:40:21', NULL, NULL);
INSERT INTO `sys_oss_config` VALUES (3, 'aliyun', 'XXXXXXXXXXXXXXX', 'XXXXXXXXXXXXXXX', 'bocoo', '', 'oss-cn-beijing.aliyuncs.com', '', 'N', '', '1', '0', '', 'admin', '2024-06-13 16:06:38', 'admin', '2024-07-10 17:50:41', NULL, NULL);
INSERT INTO `sys_oss_config` VALUES (4, 'qcloud', 'XXXXXXXXXXXXXXX', 'XXXXXXXXXXXXXXX', 'bocoo', '', 'cos.ap-beijing.myqcloud.com', '', 'N', 'ap-beijing', '1', '0', '', 'admin', '2024-06-13 16:06:38', 'admin', '2024-06-13 16:06:38', NULL, NULL);

-- ----------------------------
-- Table structure for sys_post
-- ----------------------------
DROP TABLE IF EXISTS `sys_post`;
CREATE TABLE `sys_post`  (
  `post_id` bigint NOT NULL COMMENT '宀椾綅ID',
  `post_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '宀椾綅缂栫爜',
  `post_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '宀椾綅鍚嶇О',
  `post_sort` int NOT NULL COMMENT '鏄剧ず椤哄簭',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '鐘舵€侊紙0姝ｅ父 1鍋滅敤锛?,
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '鍒涘缓鑰?,
  `create_time` datetime NULL DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '鏇存柊鑰?,
  `update_time` datetime NULL DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '澶囨敞',
  `create_by_id` bigint NULL DEFAULT NULL COMMENT '鍒涘缓浜篒D',
  PRIMARY KEY (`post_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '宀椾綅淇℃伅琛? ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_post
-- ----------------------------
INSERT INTO `sys_post` VALUES (1, 'ceo', 'BOSS', 1, '1', 'admin', '2024-06-13 16:06:25', 'show', '2025-07-25 11:54:02', '', NULL);

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `role_id` bigint NOT NULL COMMENT '瑙掕壊ID',
  `role_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '瑙掕壊鍚嶇О',
  `role_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '瑙掕壊鏉冮檺瀛楃涓?,
  `role_sort` int NOT NULL COMMENT '鏄剧ず椤哄簭',
  `data_scope` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '1' COMMENT '鏁版嵁鑼冨洿锛?锛氬叏閮ㄦ暟鎹潈闄?2锛氳嚜瀹氭暟鎹潈闄?3锛氭湰閮ㄩ棬鏁版嵁鏉冮檺 4锛氭湰閮ㄩ棬鍙婁互涓嬫暟鎹潈闄愶級',
  `menu_check_strictly` tinyint(1) NULL DEFAULT 1 COMMENT '鑿滃崟鏍戦€夋嫨椤规槸鍚﹀叧鑱旀樉绀?,
  `dept_check_strictly` tinyint(1) NULL DEFAULT 1 COMMENT '閮ㄩ棬鏍戦€夋嫨椤规槸鍚﹀叧鑱旀樉绀?,
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '瑙掕壊鐘舵€侊紙0姝ｅ父 1鍋滅敤锛?,
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0' COMMENT '鍒犻櫎鏍囧織锛?浠ｈ〃瀛樺湪 2浠ｈ〃鍒犻櫎锛?,
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '鍒涘缓鑰?,
  `create_time` datetime NULL DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '鏇存柊鑰?,
  `update_time` datetime NULL DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '澶囨敞',
  `create_by_id` bigint NULL DEFAULT NULL COMMENT '鍒涘缓浜篒D',
  PRIMARY KEY (`role_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '瑙掕壊淇℃伅琛? ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '瓒呯骇绠＄悊鍛?, 'admin', 1, '1', 1, 1, '1', '0', 'admin', '2024-06-13 16:06:26', '', NULL, '瓒呯骇绠＄悊鍛?, NULL);
INSERT INTO `sys_role` VALUES (2056929180041412609, '娴嬭瘯', 'ceshi', 0, '1', 1, 1, '1', '0', 'admin', '2026-05-20 10:45:07', 'admin', '2026-05-20 10:45:07', NULL, 1);

-- ----------------------------
-- Table structure for sys_role_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_dept`;
CREATE TABLE `sys_role_dept`  (
  `role_id` bigint NOT NULL COMMENT '瑙掕壊ID',
  `dept_id` bigint NOT NULL COMMENT '閮ㄩ棬ID',
  `create_by_id` bigint NULL DEFAULT NULL COMMENT '鍒涘缓浜篒D',
  PRIMARY KEY (`role_id`, `dept_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '瑙掕壊鍜岄儴闂ㄥ叧鑱旇〃' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role_dept
-- ----------------------------

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `role_id` bigint NOT NULL COMMENT '瑙掕壊ID',
  `menu_id` bigint NOT NULL COMMENT '鑿滃崟ID',
  `create_by_id` bigint NULL DEFAULT NULL COMMENT '鍒涘缓浜篒D',
  PRIMARY KEY (`role_id`, `menu_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '瑙掕壊鍜岃彍鍗曞叧鑱旇〃' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `dept_id` bigint NULL DEFAULT NULL COMMENT '閮ㄩ棬ID',
  `user_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '鐢ㄦ埛璐﹀彿',
  `nick_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '鐢ㄦ埛鏄电О',
  `user_type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'sys_user' COMMENT '鐢ㄦ埛绫诲瀷锛坰ys_user绯荤粺鐢ㄦ埛锛?,
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '鐢ㄦ埛閭',
  `phonenumber` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '鎵嬫満鍙风爜',
  `sex` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0' COMMENT '鐢ㄦ埛鎬у埆锛?鐢?1濂?2鏈煡锛?,
  `avatar` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '澶村儚鍦板潃',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '瀵嗙爜',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0' COMMENT '甯愬彿鐘舵€侊紙0姝ｅ父 1鍋滅敤锛?,
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0' COMMENT '鍒犻櫎鏍囧織锛?浠ｈ〃瀛樺湪 2浠ｈ〃鍒犻櫎锛?,
  `login_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '鏈€鍚庣櫥褰旾P',
  `login_date` datetime NULL DEFAULT NULL COMMENT '鏈€鍚庣櫥褰曟椂闂?,
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '鍒涘缓鑰?,
  `create_time` datetime NULL DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '鏇存柊鑰?,
  `update_time` datetime NULL DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '澶囨敞',
  `create_by_id` bigint NULL DEFAULT NULL COMMENT '鍒涘缓浜篒D',
  `file_url1` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `file_url2` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `file_url3` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `file_url4` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `oss_id1` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `oss_id2` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `oss_id3` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `oss_id4` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鐢ㄦ埛淇℃伅琛? ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 103, 'admin', '绯荤粺绠＄悊鍛?, 'sys_user', 'admin@example.com', '18888888888', '0', '', '$2a$10$gkt8GIcTlW28k3a.osOvQus81YBcY9JHr7zLqaaknk4O2x9xX/JMm', '1', '0', '127.0.0.1', '2026-05-20 10:23:51', 'admin', '2024-06-13 16:06:25', 'admin', '2026-05-20 10:23:51', '绠＄悊鍛?, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for sys_user_post
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_post`;
CREATE TABLE `sys_user_post`  (
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `post_id` bigint NOT NULL COMMENT '宀椾綅ID',
  `create_by_id` bigint NULL DEFAULT NULL COMMENT '鍒涘缓浜篒D',
  PRIMARY KEY (`user_id`, `post_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鐢ㄦ埛涓庡矖浣嶅叧鑱旇〃' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_post
-- ----------------------------
INSERT INTO `sys_user_post` VALUES (1, 1, NULL);

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `role_id` bigint NOT NULL COMMENT '瑙掕壊ID',
  `create_by_id` bigint NULL DEFAULT NULL COMMENT '鍒涘缓浜篒D',
  PRIMARY KEY (`user_id`, `role_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鐢ㄦ埛鍜岃鑹插叧鑱旇〃' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1, NULL);

-- ----------------------------
-- Table structure for third_client
-- ----------------------------
DROP TABLE IF EXISTS `third_client`;
CREATE TABLE `third_client`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
  `app_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '绗笁鏂瑰簲鐢ㄥ敮涓€鏍囪瘑',
  `secret_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '绗笁鏂瑰簲鐢ㄥ瘑閽?,
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '搴旂敤鍚嶇О',
  `permissions` json NULL COMMENT '鏉冮檺鍒楄〃锛孞SON鏁扮粍瀛樺偍',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '鍒涘缓鑰?,
  `create_time` datetime NULL DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '鏇存柊鑰?,
  `update_time` datetime NULL DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
  `create_by_id` bigint NULL DEFAULT NULL COMMENT '鍒涘缓浜篒D',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_app_key`(`app_key` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '绗笁鏂瑰簲鐢ㄤ俊鎭〃' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of third_client
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
