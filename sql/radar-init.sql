/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50720
Source Host           : localhost:3306
Source Database       : radar

Target Server Type    : MYSQL
Target Server Version : 50720
File Encoding         : 65001

Date: 2019-09-11 18:24:16
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for data_moble_info
-- ----------------------------
DROP TABLE IF EXISTS `data_moble_info`;
CREATE TABLE `data_moble_info` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `MOBILE` varchar(7) COLLATE utf8_unicode_ci NOT NULL COMMENT '主键',
  `PROVINCE` varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT '省',
  `CITY` varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT '市',
  `SUPPLIER` varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT '卡信息',
  `REGION_CODE` varchar(4) COLLATE utf8_unicode_ci NOT NULL COMMENT '区号',
  `CREATE_TIME` datetime NOT NULL,
  `UPDATE_TIME` datetime NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ----------------------------
-- Records of data_moble_info
-- ----------------------------

-- ----------------------------
-- Table structure for engine_abstraction
-- ----------------------------
DROP TABLE IF EXISTS `engine_abstraction`;
CREATE TABLE `engine_abstraction` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `NAME` varchar(64) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Abstraction 名称',
  `LABEL` varchar(32) COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  `MODEL_ID` bigint(20) NOT NULL COMMENT 'MODEL ID',
  `AGGREGATE_TYPE` int(11) NOT NULL COMMENT '统计类型',
  `SEARCH_FIELD` varchar(64) COLLATE utf8_unicode_ci DEFAULT NULL,
  `SEARCH_INTERVAL_TYPE` int(11) NOT NULL COMMENT '时间段类型',
  `SEARCH_INTERVAL_VALUE` int(11) NOT NULL COMMENT '时间段具体值',
  `FUNCTION_FIELD` varchar(64) COLLATE utf8_unicode_ci DEFAULT NULL,
  `RULE_SCRIPT` varchar(2048) COLLATE utf8_unicode_ci NOT NULL COMMENT '数据校验',
  `RULE_DEFINITION` varchar(2048) COLLATE utf8_unicode_ci DEFAULT NULL,
  `STATUS` int(11) NOT NULL COMMENT '状态',
  `COMMENT` varchar(128) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '备注',
  `CREATE_TIME` datetime NOT NULL,
  `UPDATE_TIME` datetime NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=475 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ----------------------------
-- Records of engine_abstraction
-- ----------------------------
INSERT INTO `engine_abstraction` VALUES ('85', 'log_ip_1_day_qty', '1天内IP登录次数', '99', '1', 'fields.userIP', '5', '1', '', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2016-11-17 11:48:07', '2016-11-17 11:48:07');
INSERT INTO `engine_abstraction` VALUES ('87', 'log_did_1_day_qty', '1天内设备登录次数', '99', '1', 'fields.deviceId', '5', '1', '', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.deviceId)\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.deviceId\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2016-11-17 11:49:44', '2016-11-17 11:49:51');
INSERT INTO `engine_abstraction` VALUES ('89', 'log_uid_1_day_qty', '1天内用户登录次数', '99', '1', 'fields.userId', '5', '1', '', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userId)\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userId\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2016-11-17 11:51:21', '2016-11-17 13:55:41');
INSERT INTO `engine_abstraction` VALUES ('91', 'log_ip_1_hour_qty', '1小时内IP登录次数', '99', '2', 'fields.userIP', '11', '1', '', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2016-11-17 14:03:40', '2016-11-17 14:03:40');
INSERT INTO `engine_abstraction` VALUES ('93', 'log_did_1_hour_qty', '1小时内设备登录次数', '99', '1', 'fields.deviceId', '11', '1', '', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.deviceId)\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.deviceId\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2016-11-17 14:05:53', '2016-11-17 18:12:11');
INSERT INTO `engine_abstraction` VALUES ('95', 'log_uid_1_hour_qty', '1小时内用户登录次数', '99', '1', 'fields.userId', '11', '1', '', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userId)\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userId\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2016-11-17 14:16:45', '2016-11-17 18:12:01');
INSERT INTO `engine_abstraction` VALUES ('97', 'log_ip_10_min_qty', '10分钟内IP登录次数', '99', '1', 'fields.userIP', '12', '10', '', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2016-11-17 14:23:54', '2016-11-17 18:11:39');
INSERT INTO `engine_abstraction` VALUES ('99', 'log_did_10_min_qty', '10分钟内设备登录次数', '99', '1', 'fields.deviceId', '12', '10', '', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.deviceId)\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.deviceId\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2016-11-17 14:29:25', '2016-11-17 14:47:48');
INSERT INTO `engine_abstraction` VALUES ('101', 'log_uid_10_min_qty', '10分钟内用户登录次数', '99', '1', 'fields.userId', '12', '10', '', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userId)\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userId\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2016-11-17 14:47:38', '2016-11-17 18:11:32');
INSERT INTO `engine_abstraction` VALUES ('103', 'log_did_ip_1_day_qty', '1天内IP关联设备数', '99', '2', 'fields.userIP', '5', '1', 'fields.deviceId', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2016-11-17 15:34:50', '2016-11-17 15:34:50');
INSERT INTO `engine_abstraction` VALUES ('105', 'log_uid_ip_1_day_qty', '1天内IP关联用户数', '99', '2', 'fields.userIP', '5', '1', 'fields.userId', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2016-11-17 15:36:18', '2016-11-17 15:36:18');
INSERT INTO `engine_abstraction` VALUES ('107', 'log_did_ip_1_hour_qty', '1小时内IP关联设备数', '99', '2', 'fields.userIP', '11', '1', 'fields.deviceId', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if ((data.fields.userIP))\n        return true;\n    else\n        return false;\n}}', '{\"class\":\"PDCT\",\"enabled\":true,\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"enabled\":true,\"operator\":\"IsNotNull\",\"expressions\":[{\"class\":\"ENTATTR\",\"type\":\"STRING\",\"column\":\"fields.userIP\"}]}]}', '1', '', '2016-11-17 15:39:18', '2019-09-11 13:53:43');
INSERT INTO `engine_abstraction` VALUES ('109', 'log_uid_ip_1_hour_qty', '1小时内IP关联用户数', '99', '2', 'fields.userIP', '11', '1', 'fields.userId', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2016-11-17 15:41:37', '2016-11-17 15:41:37');
INSERT INTO `engine_abstraction` VALUES ('111', 'tran_ip_1_day_qty', '1天内IP交易量_笔数', '103', '1', 'fields.userIP', '5', '1', '', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2016-11-17 18:14:34', '2016-11-17 18:14:34');
INSERT INTO `engine_abstraction` VALUES ('113', 'tran_ip_1_hour_qty', '1小时内IP交易量_笔数', '103', '1', 'fields.userIP', '11', '1', '', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2016-11-17 18:17:32', '2016-11-17 18:17:32');
INSERT INTO `engine_abstraction` VALUES ('115', 'tran_ip_10_min_qty', '10分钟内IP交易量_笔数', '103', '1', 'fields.userIP', '12', '10', '', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2016-11-17 18:19:03', '2016-11-17 18:19:03');
INSERT INTO `engine_abstraction` VALUES ('117', 'tran_did_1_day_qty', '1天内设备交易量_笔数', '103', '1', 'fields.deviceId', '5', '1', '', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.deviceId)\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.deviceId\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2016-11-17 18:26:09', '2016-11-17 18:26:09');
INSERT INTO `engine_abstraction` VALUES ('119', 'tran_did_1_hour_qty', '1小时内设备交易量_笔数', '103', '1', 'fields.deviceId', '11', '1', '', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.deviceId)\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.deviceId\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2016-11-17 18:27:11', '2016-11-17 18:27:11');
INSERT INTO `engine_abstraction` VALUES ('121', 'tran_did_10_min_qty', '10分钟内设备交易量_笔数', '103', '1', 'fields.deviceId', '12', '10', '', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.deviceId)\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.deviceId\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2016-11-17 18:29:09', '2016-11-17 18:29:09');
INSERT INTO `engine_abstraction` VALUES ('123', 'tran_ip_1_day_amt', '1天内IP交易总金额', '103', '3', 'fields.userIP', '5', '1', 'fields.amount', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2016-11-18 10:57:29', '2016-11-18 10:57:29');
INSERT INTO `engine_abstraction` VALUES ('125', 'tran_ip_1_hour_amt', '1小时内IP交易总金额', '103', '3', 'fields.userIP', '11', '1', 'fields.amount', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2016-11-18 10:58:28', '2016-11-18 10:58:41');
INSERT INTO `engine_abstraction` VALUES ('127', 'tran_ip_10_min_amt', '10分钟内IP交易总金额', '103', '3', 'fields.userIP', '12', '10', 'fields.amount', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2016-11-18 11:14:42', '2016-11-18 11:14:42');
INSERT INTO `engine_abstraction` VALUES ('129', 'tran_uid_1_day_amt', '1天内用户交易总金额', '103', '3', 'fields.userId', '5', '1', 'fields.amount', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userId)\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userId\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2016-11-18 11:18:00', '2016-11-18 11:18:00');
INSERT INTO `engine_abstraction` VALUES ('131', 'tran_uid_1_hour_amt', '1小时内用户交易总金额', '103', '3', 'fields.userId', '11', '1', 'fields.amount', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if ((data.fields.userId))\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userId\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2016-11-18 11:27:42', '2019-09-11 13:44:32');
INSERT INTO `engine_abstraction` VALUES ('133', 'tran_uid_10_min_amt', '10分钟内用户交易总金额', '103', '3', 'fields.userId', '12', '10', 'fields.amount', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userId)\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userId\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2016-11-18 11:29:42', '2016-11-18 11:29:42');
INSERT INTO `engine_abstraction` VALUES ('135', 'tran_uid_ip_1_day_qty', '1天内IP关联用户数', '103', '2', 'fields.userIP', '5', '1', 'fields.userId', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2016-11-18 14:00:10', '2016-11-18 14:00:10');
INSERT INTO `engine_abstraction` VALUES ('137', 'tran_did_ip_1_day_qty', '1天内IP关联设备数', '103', '2', 'fields.userIP', '5', '1', 'fields.deviceId', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2016-11-18 14:01:59', '2016-11-18 14:01:59');
INSERT INTO `engine_abstraction` VALUES ('139', 'reg_ip_1_day_qty', '1天内IP关联注册数', '101', '1', 'fields.userIP', '5', '1', '', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2016-11-18 15:06:57', '2016-11-18 15:06:57');
INSERT INTO `engine_abstraction` VALUES ('141', 'reg_ip_1_hour_qty', '1小时内IP关联注册数', '101', '1', 'fields.userIP', '11', '1', '', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2016-11-18 15:07:51', '2016-11-18 15:08:10');
INSERT INTO `engine_abstraction` VALUES ('143', 'reg_ip_10_min_qty', '10分钟内IP关联注册数', '101', '1', 'fields.userIP', '12', '10', '', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2016-11-18 15:09:15', '2016-11-18 15:09:15');
INSERT INTO `engine_abstraction` VALUES ('145', 'reg_did_1_day_qty', '1天内设备关联注册数', '101', '1', 'fields.deviceId', '5', '1', '', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.deviceId)\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.deviceId\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2016-11-18 15:10:23', '2016-11-18 15:10:41');
INSERT INTO `engine_abstraction` VALUES ('147', 'reg_did_1_hour_qty', '1小时内设备关联注册数', '101', '1', 'fields.deviceId', '11', '1', '', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.deviceId)\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.deviceId\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2016-11-18 15:20:13', '2016-11-18 15:20:13');
INSERT INTO `engine_abstraction` VALUES ('149', 'reg_did_10_min_qty', '10分钟内设备关联注册数', '101', '1', 'fields.deviceId', '12', '10', '', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.deviceId)\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.deviceId\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2016-11-18 15:21:52', '2016-11-18 15:21:52');
INSERT INTO `engine_abstraction` VALUES ('151', 'reg_uid_ip_1_day_qty', '1天内IP关联用户数', '101', '2', 'fields.userIP', '5', '1', 'fields.userId', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2016-11-18 15:23:24', '2016-11-18 15:23:24');
INSERT INTO `engine_abstraction` VALUES ('153', 'reg_uid_did_1_day_qty', '1天内设备关联用户数', '101', '2', 'fields.deviceId', '5', '1', 'fields.userId', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.deviceId)\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.deviceId\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2016-11-18 15:53:34', '2016-11-18 15:53:34');
INSERT INTO `engine_abstraction` VALUES ('453', 'reg_ip_1_day_qty', '1天内IP关联注册数', '222', '1', 'fields.userIP', '5', '1', '', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2019-09-10 11:16:12', '2019-09-10 11:16:12');
INSERT INTO `engine_abstraction` VALUES ('454', 'reg_ip_1_hour_qty', '1小时内IP关联注册数', '222', '1', 'fields.userIP', '11', '1', '', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2019-09-10 11:16:12', '2019-09-10 11:16:12');
INSERT INTO `engine_abstraction` VALUES ('455', 'reg_ip_10_min_qty', '10分钟内IP关联注册数', '222', '1', 'fields.userIP', '12', '10', '', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2019-09-10 11:16:12', '2019-09-10 11:16:12');
INSERT INTO `engine_abstraction` VALUES ('456', 'reg_did_1_day_qty', '1天内设备关联注册数', '222', '1', 'fields.deviceId', '5', '1', '', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.deviceId)\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.deviceId\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2019-09-10 11:16:12', '2019-09-10 11:16:12');
INSERT INTO `engine_abstraction` VALUES ('457', 'reg_did_1_hour_qty', '1小时内设备关联注册数', '222', '1', 'fields.deviceId', '11', '1', '', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.deviceId)\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.deviceId\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2019-09-10 11:16:12', '2019-09-10 11:16:12');
INSERT INTO `engine_abstraction` VALUES ('458', 'reg_did_10_min_qty', '10分钟内设备关联注册数', '222', '1', 'fields.deviceId', '12', '10', '', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.deviceId)\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.deviceId\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2019-09-10 11:16:12', '2019-09-10 11:16:12');
INSERT INTO `engine_abstraction` VALUES ('459', 'reg_uid_ip_1_day_qty', '1天内IP关联用户数', '222', '2', 'fields.userIP', '5', '1', 'fields.userId', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2019-09-10 11:16:12', '2019-09-10 11:16:12');
INSERT INTO `engine_abstraction` VALUES ('460', 'reg_uid_did_1_day_qty', '1天内设备关联用户数', '222', '2', 'fields.deviceId', '5', '1', 'fields.userId', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.deviceId)\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.deviceId\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2019-09-10 11:16:12', '2019-09-10 11:16:12');
INSERT INTO `engine_abstraction` VALUES ('461', 'tran_ip_1_day_qty', '1天内IP交易量_笔数', '224', '1', 'fields.userIP', '5', '1', '', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2019-09-11 18:05:35', '2019-09-11 18:05:35');
INSERT INTO `engine_abstraction` VALUES ('462', 'tran_ip_1_hour_qty', '1小时内IP交易量_笔数', '224', '1', 'fields.userIP', '11', '1', '', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2019-09-11 18:05:35', '2019-09-11 18:05:35');
INSERT INTO `engine_abstraction` VALUES ('463', 'tran_ip_10_min_qty', '10分钟内IP交易量_笔数', '224', '1', 'fields.userIP', '12', '10', '', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2019-09-11 18:05:35', '2019-09-11 18:05:35');
INSERT INTO `engine_abstraction` VALUES ('464', 'tran_did_1_day_qty', '1天内设备交易量_笔数', '224', '1', 'fields.deviceId', '5', '1', '', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.deviceId)\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.deviceId\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2019-09-11 18:05:35', '2019-09-11 18:05:35');
INSERT INTO `engine_abstraction` VALUES ('465', 'tran_did_1_hour_qty', '1小时内设备交易量_笔数', '224', '1', 'fields.deviceId', '11', '1', '', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.deviceId)\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.deviceId\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2019-09-11 18:05:35', '2019-09-11 18:05:35');
INSERT INTO `engine_abstraction` VALUES ('466', 'tran_did_10_min_qty', '10分钟内设备交易量_笔数', '224', '1', 'fields.deviceId', '12', '10', '', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.deviceId)\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.deviceId\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2019-09-11 18:05:35', '2019-09-11 18:05:35');
INSERT INTO `engine_abstraction` VALUES ('467', 'tran_ip_1_day_amt', '1天内IP交易总金额', '224', '3', 'fields.userIP', '5', '1', 'fields.amount', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2019-09-11 18:05:35', '2019-09-11 18:05:35');
INSERT INTO `engine_abstraction` VALUES ('468', 'tran_ip_1_hour_amt', '1小时内IP交易总金额', '224', '3', 'fields.userIP', '11', '1', 'fields.amount', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2019-09-11 18:05:35', '2019-09-11 18:05:35');
INSERT INTO `engine_abstraction` VALUES ('469', 'tran_ip_10_min_amt', '10分钟内IP交易总金额', '224', '3', 'fields.userIP', '12', '10', 'fields.amount', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2019-09-11 18:05:35', '2019-09-11 18:05:35');
INSERT INTO `engine_abstraction` VALUES ('470', 'tran_uid_1_day_amt', '1天内用户交易总金额', '224', '3', 'fields.userId', '5', '1', 'fields.amount', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userId)\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userId\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2019-09-11 18:05:35', '2019-09-11 18:05:35');
INSERT INTO `engine_abstraction` VALUES ('471', 'tran_uid_1_hour_amt', '1小时内用户交易总金额', '224', '3', 'fields.userId', '11', '1', 'fields.amount', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if ((data.fields.userId))\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userId\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2019-09-11 18:05:35', '2019-09-11 18:05:35');
INSERT INTO `engine_abstraction` VALUES ('472', 'tran_uid_10_min_amt', '10分钟内用户交易总金额', '224', '3', 'fields.userId', '12', '10', 'fields.amount', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userId)\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userId\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2019-09-11 18:05:35', '2019-09-11 18:05:35');
INSERT INTO `engine_abstraction` VALUES ('473', 'tran_uid_ip_1_day_qty', '1天内IP关联用户数', '224', '2', 'fields.userIP', '5', '1', 'fields.userId', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2019-09-11 18:05:35', '2019-09-11 18:05:35');
INSERT INTO `engine_abstraction` VALUES ('474', 'tran_did_ip_1_day_qty', '1天内IP关联设备数', '224', '2', 'fields.userIP', '5', '1', 'fields.deviceId', 'class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}', '1', '', '2019-09-11 18:05:35', '2019-09-11 18:05:35');

-- ----------------------------
-- Table structure for engine_activation
-- ----------------------------
DROP TABLE IF EXISTS `engine_activation`;
CREATE TABLE `engine_activation` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `ACTIVATION_NAME` varchar(64) COLLATE utf8_unicode_ci NOT NULL COMMENT '名称',
  `LABEL` varchar(32) COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  `MODEL_ID` bigint(11) NOT NULL COMMENT 'model id',
  `COMMENT` varchar(128) COLLATE utf8_unicode_ci NOT NULL COMMENT '注释',
  `BOTTOM` int(11) NOT NULL DEFAULT '0' COMMENT '底部阀值',
  `MEDIAN` int(11) NOT NULL DEFAULT '0' COMMENT '中间阀值',
  `HIGH` int(11) NOT NULL DEFAULT '0' COMMENT '顶部阀值',
  `STATUS` int(11) NOT NULL COMMENT '状态',
  `RULE_ORDER` varchar(256) COLLATE utf8_unicode_ci DEFAULT NULL,
  `CREATE_TIME` datetime NOT NULL,
  `UPDATE_TIME` datetime NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=118 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ----------------------------
-- Records of engine_activation
-- ----------------------------
INSERT INTO `engine_activation` VALUES ('37', 'transaction_exception', '异常交易', '103', '', '0', '40', '80', '1', '85,87,89,91,93,95,97,99,127', '2016-11-18 11:38:38', '2019-09-11 17:41:30');
INSERT INTO `engine_activation` VALUES ('39', 'login_exception', '登录异常', '99', '', '0', '40', '80', '1', '101,103,105,107,121', '2016-11-18 14:40:13', '2019-09-09 16:17:21');
INSERT INTO `engine_activation` VALUES ('41', 'register_exception', '异常注册', '101', '', '0', '40', '80', '1', '109,111,113,115,117,119', '2016-11-18 15:57:13', '2019-09-11 17:41:04');
INSERT INTO `engine_activation` VALUES ('116', 'register_exception', '异常注册', '222', '', '0', '40', '80', '1', '373,374,375,376,377,378', '2019-09-10 11:16:12', '2019-09-10 14:22:46');
INSERT INTO `engine_activation` VALUES ('117', 'transaction_exception', '异常交易', '224', '', '0', '40', '80', '1', '379,380,381,382,383,384,385,386,387', '2019-09-11 18:05:35', '2019-09-11 18:11:07');

-- ----------------------------
-- Table structure for engine_data_lists
-- ----------------------------
DROP TABLE IF EXISTS `engine_data_lists`;
CREATE TABLE `engine_data_lists` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `NAME` varchar(32) COLLATE utf8_unicode_ci NOT NULL COMMENT '列表名',
  `LABEL` varchar(32) COLLATE utf8_unicode_ci NOT NULL COMMENT '列表名中文描叙',
  `MODEL_ID` bigint(20) NOT NULL COMMENT '模型ID',
  `COMMENT` varchar(128) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '注释',
  `LIST_TYPE` varchar(11) COLLATE utf8_unicode_ci NOT NULL DEFAULT 'BLACK' COMMENT '列表类型',
  `STATUS` int(11) NOT NULL COMMENT '状态',
  `CREATE_TIME` datetime NOT NULL,
  `UPDATE_TIME` datetime NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=86 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ----------------------------
-- Records of engine_data_lists
-- ----------------------------
INSERT INTO `engine_data_lists` VALUES ('1', 'radar_brand_black_List', '[系统]手机品牌黑名单', '1', '', 'BLACK', '1', '2016-08-10 16:02:29', '2016-08-10 16:02:31');
INSERT INTO `engine_data_lists` VALUES ('3', 'radar_mobile_black_list', '[系统]手机号码黑名单', '1', '', 'BLACK', '1', '2016-08-24 18:37:52', '2016-08-24 18:37:55');
INSERT INTO `engine_data_lists` VALUES ('5', 'radar_mobile_white_list', '[系统]手机号码白名单', '1', '', 'WHITE', '1', '2016-08-24 18:38:55', '2016-08-24 18:38:58');
INSERT INTO `engine_data_lists` VALUES ('7', 'radar_ip_black_list', '[系统]IP黑名单', '1', '', 'BLACK', '1', '2016-08-25 11:22:12', '2016-08-25 11:22:16');
INSERT INTO `engine_data_lists` VALUES ('9', 'radar_ip_white_list', '[系统]IP白名单', '1', '', 'WHITE', '1', '2016-09-18 10:27:25', '2016-09-18 10:27:25');
INSERT INTO `engine_data_lists` VALUES ('10', 'mobile_brand_white_list', '[系统]手机品牌白名单', '1', '', 'WHITE', '1', '2016-11-23 16:07:31', '2016-11-23 16:07:31');
INSERT INTO `engine_data_lists` VALUES ('29', 'mobile_white_list', '手机号码白名单', '101', '', 'WHITE', '1', '2016-11-21 14:07:29', '2016-11-21 14:07:29');
INSERT INTO `engine_data_lists` VALUES ('31', 'mobile_white_list', '手机号码白名单', '103', '', 'WHITE', '1', '2016-11-21 14:09:57', '2016-11-21 14:09:57');
INSERT INTO `engine_data_lists` VALUES ('33', 'mobile_white_list', '手机号码白名单', '99', '', 'WHITE', '1', '2016-11-21 14:11:37', '2016-11-21 14:11:37');
INSERT INTO `engine_data_lists` VALUES ('84', 'mobile_white_list', '手机号码白名单', '222', '', 'WHITE', '1', '2019-09-10 11:16:12', '2019-09-10 11:16:12');
INSERT INTO `engine_data_lists` VALUES ('85', 'mobile_white_list', '手机号码白名单', '224', '', 'WHITE', '1', '2019-09-11 18:05:35', '2019-09-11 18:05:35');

-- ----------------------------
-- Table structure for engine_data_list_meta
-- ----------------------------
DROP TABLE IF EXISTS `engine_data_list_meta`;
CREATE TABLE `engine_data_list_meta` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `DATA_LIST_ID` bigint(20) NOT NULL COMMENT '数据列表ID',
  `FIELD_NAME` varchar(32) COLLATE utf8_unicode_ci NOT NULL COMMENT '字段名',
  `LABEL` varchar(32) COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  `SEQ_NUM` int(11) NOT NULL COMMENT '字段序号',
  `CREATE_TIME` datetime NOT NULL,
  `UPDATE_TIME` datetime NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=96 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ----------------------------
-- Records of engine_data_list_meta
-- ----------------------------
INSERT INTO `engine_data_list_meta` VALUES ('1', '1', 'brand', '手机品牌', '1', '2016-08-17 17:42:49', '2016-11-18 18:41:33');
INSERT INTO `engine_data_list_meta` VALUES ('3', '3', 'mobile', '手机号码', '1', '2016-08-24 18:42:35', '2016-11-18 18:41:35');
INSERT INTO `engine_data_list_meta` VALUES ('5', '5', 'mobile', '手机号码', '1', '2016-08-24 18:43:39', '2016-11-18 18:41:38');
INSERT INTO `engine_data_list_meta` VALUES ('7', '7', 'ip', 'IP', '1', '2016-08-25 13:58:24', '2016-11-18 18:41:41');
INSERT INTO `engine_data_list_meta` VALUES ('9', '9', 'ip', 'IP', '1', '2016-09-18 10:27:46', '2016-11-18 18:32:16');
INSERT INTO `engine_data_list_meta` VALUES ('15', '13', 'sjhm', '', '1', '2016-09-23 15:54:40', '2016-09-23 15:54:40');
INSERT INTO `engine_data_list_meta` VALUES ('17', '21', 'testaaa', '', '1', '2016-09-28 14:41:34', '2016-09-29 11:32:50');
INSERT INTO `engine_data_list_meta` VALUES ('19', '23', 'ip', 'IP', '1', '2016-10-14 11:12:36', '2016-10-14 11:12:36');
INSERT INTO `engine_data_list_meta` VALUES ('21', '25', 'ip', 'IP', '1', '2016-10-14 11:15:32', '2016-10-14 11:19:57');
INSERT INTO `engine_data_list_meta` VALUES ('23', '29', 'mobile', '手机号码', '1', '2016-11-21 14:08:20', '2016-11-21 14:08:20');
INSERT INTO `engine_data_list_meta` VALUES ('25', '31', 'mobile', '手机号码', '1', '2016-11-21 14:10:12', '2016-11-21 14:10:12');
INSERT INTO `engine_data_list_meta` VALUES ('27', '33', 'mobile', '手机号码', '1', '2016-11-21 14:11:50', '2016-11-21 14:11:50');
INSERT INTO `engine_data_list_meta` VALUES ('29', '35', 'brand', '手机品牌', '1', '2016-11-25 17:28:07', '2016-11-25 17:28:07');
INSERT INTO `engine_data_list_meta` VALUES ('94', '84', 'mobile', '手机号码', '1', '2019-09-10 11:16:12', '2019-09-10 11:16:12');
INSERT INTO `engine_data_list_meta` VALUES ('95', '85', 'mobile', '手机号码', '1', '2019-09-11 18:05:35', '2019-09-11 18:05:35');

-- ----------------------------
-- Table structure for engine_data_list_records
-- ----------------------------
DROP TABLE IF EXISTS `engine_data_list_records`;
CREATE TABLE `engine_data_list_records` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `DATA_LIST_ID` bigint(20) NOT NULL COMMENT '数据列表ID',
  `DATA_RECORD` varchar(255) COLLATE utf8_unicode_ci NOT NULL COMMENT '数据记录',
  `CREATE_TIME` datetime NOT NULL,
  `UPDATE_TIME` datetime NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=30910 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ----------------------------
-- Records of engine_data_list_records
-- ----------------------------
INSERT INTO `engine_data_list_records` VALUES ('30387', '23', '114.114.114.114', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30391', '25', '114.114.114.114', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30393', '29', '13800000000', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30395', '31', '13800000000', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30397', '33', '13800000000', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30401', '1', 'apple', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30403', '1', 'Xiaomi', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30405', '1', 'HUAWEI', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30407', '1', 'OPPO', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30409', '1', 'samsung', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30411', '1', 'vivo', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30413', '1', 'LENOVO', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30415', '1', 'Meizu', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30417', '1', 'YuLong', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30419', '1', 'Coolpad', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30421', '1', 'ZTE', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30423', '1', 'GiONEE', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30425', '1', 'BBK', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30427', '1', 'alps', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30429', '1', 'Hisense', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30431', '1', 'Letv', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30433', '1', 'DOOV', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30435', '1', 'TCL', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30437', '1', 'LeMobile', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30439', '1', 'HTC', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30441', '1', 'K-Touch', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30443', '1', 'CMDC', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30445', '1', 'YUSUN', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30447', '1', 'BIRD', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30449', '1', 'nubia', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30453', '1', 'Sony', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30455', '1', '360', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30457', '1', 'Haier', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30459', '1', 'LTTY', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30461', '1', 'koobee', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30463', '1', 'motorola', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30465', '1', 'lephone', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30467', '1', 'QiKU', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30469', '1', 'asus', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30471', '1', '4G', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30473', '1', 'LG', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30475', '1', 'OUKI', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30477', '1', 'Spreadtrum', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30479', '1', 'M5', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30481', '1', 'smartisan', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30483', '1', 'Qmi', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30485', '1', 'LGE', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30487', '1', 'Wiselinksz', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30489', '1', 'Q9', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30491', '1', 'Mikee', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30493', '1', 'C906', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30495', '1', 'HOSIN', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30497', '1', 'G4', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30499', '1', 'Xianmi', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30501', '1', 'meetuu', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30503', '1', 'DESAY', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30505', '1', 'SBYH', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30507', '1', 'Poulo', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30509', '1', 'Android TD', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30511', '1', 'xiaolajiao', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30513', '1', 'KOPO', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30515', '1', 'ONEPLUS', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30517', '1', 'bignox', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30519', '1', 'konka', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30521', '1', 'honor', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30523', '1', 'SPRD', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30525', '1', 'Changhong', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30527', '1', 'PHICOMM', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30529', '1', 'Philips', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30531', '1', 'BOWAY', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30533', '1', 'unknown', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30535', '1', 'Meitu', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30537', '1', 'ETON', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30539', '1', 'ZUK', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30541', '1', 'VOTO', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30543', '1', 'android', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30545', '1', 'bigsamsung', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30547', '1', 'AUX', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30549', '1', 'NOAIN', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30551', '1', 'GuangXin', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30553', '1', 'JTY', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30555', '1', 'bifer', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30559', '1', 'JinXing', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30561', '1', 'UTOUU', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30563', '1', 'Lovme', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30567', '1', 'HUASEN', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30569', '1', 'XXAndroid', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30571', '1', 'UOOGOU', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30573', '1', 'feixun', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30575', '1', 'MI', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30577', '1', 'CUBE', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30579', '1', 'iPhone', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30581', '1', 'Teclast', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30583', '1', 'Hasee', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30585', '1', 'SANMEI', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30587', '1', 'EOOM', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30589', '1', 'XMSD', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30591', '1', 'Nokia', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30593', '1', 'Genymotion', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30595', '1', 'WellPhone', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30597', '1', 'aloes', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30599', '1', 'InFocus', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30601', '1', 'Sony Ericsson', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30605', '1', 'TCT', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30607', '1', 'BESTSONNY', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30609', '1', 'Pioneer', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30611', '1', 'skyhon', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30613', '1', 'NFT', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30615', '1', 'IUNI', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30617', '1', 'PANTECH', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30621', '1', 'Qingcheng', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30623', '1', 'SUGAR', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30625', '1', 'ASUSTek Computer Inc', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30627', '1', 'GREE', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30629', '1', 'Foxconn International Holdings Limited', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30631', '1', 'rockchip', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30633', '1', 'Newman', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30635', '1', 'x-apple', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30637', '1', 'AMOI', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30639', '1', 'ramos', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30641', '1', 'Volte', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30643', '1', 'Baidu', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30645', '1', 'HT', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30647', '1', 'ZEEMI', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30649', '1', 'OPSSON', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30651', '1', 'RY', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30653', '1', 'freeme', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30655', '1', 'PPTV', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30657', '1', 'softwinner', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30659', '1', 'EBEST', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30661', '1', '4G+', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30665', '1', 'LIMI', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30667', '1', 'SOP', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30669', '1', 'SHOWN_P1', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30671', '1', 'BAIMI', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30675', '1', 'Comio', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30677', '1', 'onda', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30679', '1', 'AoleDior', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30681', '1', 'OBEE', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30683', '1', 'WaterWorld', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30685', '1', 'SAGA', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30687', '1', 'KDOOR', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30689', '1', 'SHARP', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30691', '1', 'OWWO', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30693', '1', 'Hasee X50 TS', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30697', '1', 'HUIZUU', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30699', '1', 'JXD', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30701', '1', 'BL', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30703', '1', 'GM', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30707', '1', 'NUOFEI', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30709', '1', 'TianYu', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30711', '1', 'XM-T', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30713', '1', 'F8909', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30715', '1', '4G-VOLTE', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30717', '1', 'YEPEN', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30719', '1', 'HY', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30721', '1', 'IPH', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30723', '1', 'T1', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30725', '1', 'MYTEL', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30727', '1', 'HEEYU', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30729', '1', 'DAQ', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30731', '1', 'YOORD', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30733', '1', 'MLLED', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30735', '1', 'hipad', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30737', '1', 'WEIBI', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30739', '1', 'MTK6592', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30741', '1', 'Allwinner', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30743', '1', 'XBS', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30745', '1', 'MediaTek', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30747', '1', 'ZWX', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30749', '1', 'QIKAI', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30751', '1', 'TETC', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30753', '1', 'Intel', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30755', '1', 'Blephone', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30757', '1', 'XMi', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30759', '1', 'ThL', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30761', '1', 'RLT', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30763', '1', 'MT6595', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30765', '1', 'XYS', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30767', '1', 'Acer', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30769', '1', 'YOUYU', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30771', '1', 'READBOY', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30773', '1', 'BROR', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30775', '1', 'bicix', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30777', '1', 'ZHUOMI', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30779', '1', 'ERENEBEN', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30781', '1', 'weiimi', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30783', '1', 'OPSSON_V6', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30785', '1', 'iPhone6 Plus', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30787', '1', 'GUOMI', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30789', '1', 'XLJ', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30791', '1', 'ares', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30793', '1', 'laaboo', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30795', '1', 'HYF-A1', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30797', '1', 'iphone 6s', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30799', '1', 'KANGJIA', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30801', '1', 'Xshitou', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30803', '1', 'BOBI', '2016-11-25 10:55:52', '2016-11-25 10:55:52');
INSERT INTO `engine_data_list_records` VALUES ('30805', '35', 'apple', '2016-11-25 17:32:19', '2016-11-25 17:32:19');
INSERT INTO `engine_data_list_records` VALUES ('30807', '35', 'Xiaomi', '2016-11-25 17:32:27', '2016-11-25 17:32:27');
INSERT INTO `engine_data_list_records` VALUES ('30809', '35', 'HUAWEI', '2016-11-25 17:32:37', '2016-11-25 17:32:37');
INSERT INTO `engine_data_list_records` VALUES ('30811', '35', 'OPPO', '2016-11-25 17:32:44', '2016-11-25 17:32:44');
INSERT INTO `engine_data_list_records` VALUES ('30813', '35', 'samsung', '2016-11-25 17:32:52', '2016-11-25 17:32:52');
INSERT INTO `engine_data_list_records` VALUES ('30815', '35', 'vivo', '2016-11-25 17:33:00', '2016-11-25 17:33:00');
INSERT INTO `engine_data_list_records` VALUES ('30817', '35', 'LENOVO', '2016-11-25 17:33:10', '2016-11-25 17:33:10');
INSERT INTO `engine_data_list_records` VALUES ('30819', '35', 'Meizu', '2016-11-25 17:33:18', '2016-11-25 17:33:18');
INSERT INTO `engine_data_list_records` VALUES ('30821', '35', 'ZTE', '2016-11-25 17:33:27', '2016-11-25 17:33:27');
INSERT INTO `engine_data_list_records` VALUES ('30823', '35', 'HTC', '2016-11-25 17:33:38', '2016-11-25 17:33:38');
INSERT INTO `engine_data_list_records` VALUES ('30825', '35', 'iPhone', '2016-11-25 17:34:19', '2016-11-25 17:34:19');
INSERT INTO `engine_data_list_records` VALUES ('30827', '35', 'nubia', '2016-11-25 17:34:46', '2016-11-25 17:34:46');
INSERT INTO `engine_data_list_records` VALUES ('30829', '35', 'BBK', '2016-11-25 17:35:24', '2016-11-25 17:35:24');
INSERT INTO `engine_data_list_records` VALUES ('30831', '35', 'LG', '2016-11-25 17:36:19', '2016-11-25 17:36:19');
INSERT INTO `engine_data_list_records` VALUES ('30908', '84', '13800000000', '2019-09-10 11:16:12', '2019-09-10 11:16:12');
INSERT INTO `engine_data_list_records` VALUES ('30909', '85', '13800000000', '2019-09-11 18:05:35', '2019-09-11 18:05:35');

-- ----------------------------
-- Table structure for engine_field
-- ----------------------------
DROP TABLE IF EXISTS `engine_field`;
CREATE TABLE `engine_field` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `MODEL_ID` bigint(20) NOT NULL COMMENT 'MODEL ID',
  `FIELD_NAME` varchar(32) COLLATE utf8_unicode_ci NOT NULL COMMENT '事件信息中的包含的字段',
  `LABEL` varchar(32) COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  `FIELD_TYPE` varchar(16) COLLATE utf8_unicode_ci NOT NULL COMMENT '字段类型',
  `VALIDATE_TYPE` varchar(15) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '校验类型',
  `VALIDATE_ARGS` varchar(15) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '校验参数',
  `INDEXED` tinyint(1) DEFAULT '0' COMMENT '1 标识索引字段',
  `CREATE_TIME` datetime NOT NULL,
  `UPDATE_TIME` datetime NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=705 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ----------------------------
-- Records of engine_field
-- ----------------------------
INSERT INTO `engine_field` VALUES ('179', '99', 'loginTime', '登录时间', 'LONG', null, null, '1', '2016-11-17 11:06:17', '2016-11-17 11:06:17');
INSERT INTO `engine_field` VALUES ('181', '99', 'userId', '用户ID', 'STRING', null, null, '1', '2016-11-17 11:06:17', '2016-11-17 11:06:17');
INSERT INTO `engine_field` VALUES ('183', '99', 'eventId', '登录事件流水号', 'STRING', null, null, '1', '2016-11-17 11:06:17', '2016-11-17 15:55:08');
INSERT INTO `engine_field` VALUES ('185', '99', 'mobile', '用户手机号', 'STRING', null, null, '0', '2016-11-17 11:07:38', '2016-11-17 11:07:38');
INSERT INTO `engine_field` VALUES ('187', '99', 'userIP', '用户IP', 'STRING', null, null, '0', '2016-11-17 11:08:36', '2016-11-17 11:08:36');
INSERT INTO `engine_field` VALUES ('189', '99', 'deviceId', '设备ID', 'STRING', null, null, '0', '2016-11-17 11:10:12', '2016-11-17 11:10:12');
INSERT INTO `engine_field` VALUES ('191', '99', 'os', '操作系统', 'STRING', null, null, '0', '2016-11-17 11:17:00', '2016-11-17 11:17:00');
INSERT INTO `engine_field` VALUES ('193', '99', 'passwd', '登录密码密文', 'STRING', null, null, '0', '2016-11-17 11:18:28', '2016-11-17 11:18:28');
INSERT INTO `engine_field` VALUES ('195', '99', 'channel', '来源渠道', 'STRING', null, null, '0', '2016-11-17 11:22:18', '2016-11-17 11:22:18');
INSERT INTO `engine_field` VALUES ('203', '101', 'eventId', '注册事件流水号', 'STRING', null, null, '1', '2016-11-17 15:54:48', '2016-11-17 15:57:39');
INSERT INTO `engine_field` VALUES ('205', '101', 'registerTime', '注册时间', 'LONG', null, null, '1', '2016-11-17 15:54:48', '2016-11-17 15:54:48');
INSERT INTO `engine_field` VALUES ('207', '101', 'userId', '用户ID', 'STRING', null, null, '1', '2016-11-17 15:54:48', '2016-11-17 15:54:48');
INSERT INTO `engine_field` VALUES ('209', '101', 'mobile', '用户手机号', 'STRING', null, null, '0', '2016-11-17 15:58:04', '2016-11-17 15:58:04');
INSERT INTO `engine_field` VALUES ('211', '101', 'userIP', '用户IP', 'STRING', null, null, '0', '2016-11-17 15:58:28', '2016-11-17 15:58:28');
INSERT INTO `engine_field` VALUES ('213', '101', 'deviceId', '设备ID', 'STRING', null, null, '0', '2016-11-17 15:59:09', '2016-11-17 15:59:09');
INSERT INTO `engine_field` VALUES ('215', '101', 'os', '操作系统', 'STRING', null, null, '0', '2016-11-17 15:59:45', '2016-11-17 15:59:45');
INSERT INTO `engine_field` VALUES ('217', '101', 'channel', '来源渠道', 'STRING', null, null, '0', '2016-11-17 16:00:11', '2016-11-17 16:00:11');
INSERT INTO `engine_field` VALUES ('219', '101', 'passwd', '密码密文', 'STRING', null, null, '0', '2016-11-17 16:00:50', '2016-11-17 16:00:50');
INSERT INTO `engine_field` VALUES ('221', '103', 'eventId', '交易流水号', 'STRING', null, null, '1', '2016-11-17 16:10:02', '2016-11-17 16:47:49');
INSERT INTO `engine_field` VALUES ('223', '103', 'eventTime', '交易发生时间', 'LONG', null, null, '1', '2016-11-17 16:10:02', '2016-11-17 17:00:10');
INSERT INTO `engine_field` VALUES ('225', '103', 'userId', '用户ID', 'STRING', null, null, '1', '2016-11-17 16:10:02', '2016-11-17 16:10:02');
INSERT INTO `engine_field` VALUES ('227', '103', 'mobile', '用户手机号', 'STRING', null, null, '0', '2016-11-17 16:48:11', '2016-11-17 16:48:11');
INSERT INTO `engine_field` VALUES ('229', '103', 'userIP', '用户IP', 'STRING', null, null, '0', '2016-11-17 16:59:41', '2016-11-17 16:59:41');
INSERT INTO `engine_field` VALUES ('231', '103', 'deviceId', '设备ID', 'STRING', null, null, '0', '2016-11-17 17:03:32', '2016-11-17 17:03:32');
INSERT INTO `engine_field` VALUES ('233', '103', 'os', '操作系统', 'STRING', null, null, '0', '2016-11-17 17:07:59', '2016-11-17 17:07:59');
INSERT INTO `engine_field` VALUES ('235', '103', 'channel', '来源渠道', 'STRING', null, null, '0', '2016-11-17 17:08:32', '2016-11-17 17:08:32');
INSERT INTO `engine_field` VALUES ('237', '103', 'amount', '交易金额', 'DOUBLE', null, null, '0', '2016-11-17 17:09:26', '2016-11-17 17:09:26');
INSERT INTO `engine_field` VALUES ('684', '222', 'eventId', '注册事件流水号', 'STRING', null, null, '1', '2019-09-10 11:16:12', '2019-09-10 11:16:12');
INSERT INTO `engine_field` VALUES ('685', '222', 'registerTime', '注册时间', 'LONG', null, null, '1', '2019-09-10 11:16:12', '2019-09-10 11:16:12');
INSERT INTO `engine_field` VALUES ('686', '222', 'userId', '用户ID', 'STRING', null, null, '1', '2019-09-10 11:16:12', '2019-09-10 11:16:12');
INSERT INTO `engine_field` VALUES ('687', '222', 'mobile', '用户手机号', 'STRING', null, null, '0', '2019-09-10 11:16:12', '2019-09-10 11:16:12');
INSERT INTO `engine_field` VALUES ('688', '222', 'userIP', '用户IP', 'STRING', null, null, '0', '2019-09-10 11:16:12', '2019-09-10 11:16:12');
INSERT INTO `engine_field` VALUES ('689', '222', 'deviceId', '设备ID', 'STRING', null, null, '0', '2019-09-10 11:16:12', '2019-09-10 11:16:12');
INSERT INTO `engine_field` VALUES ('690', '222', 'os', '操作系统', 'STRING', null, null, '0', '2019-09-10 11:16:12', '2019-09-10 11:16:12');
INSERT INTO `engine_field` VALUES ('691', '222', 'channel', '来源渠道', 'STRING', null, null, '0', '2019-09-10 11:16:12', '2019-09-10 11:16:12');
INSERT INTO `engine_field` VALUES ('692', '222', 'passwd', '密码密文', 'STRING', null, null, '0', '2019-09-10 11:16:12', '2019-09-10 11:16:12');
INSERT INTO `engine_field` VALUES ('696', '224', 'eventId', '交易流水号', 'STRING', null, null, '1', '2019-09-11 18:05:35', '2019-09-11 18:05:35');
INSERT INTO `engine_field` VALUES ('697', '224', 'eventTime', '交易发生时间', 'LONG', null, null, '1', '2019-09-11 18:05:35', '2019-09-11 18:05:35');
INSERT INTO `engine_field` VALUES ('698', '224', 'userId', '用户ID', 'STRING', null, null, '1', '2019-09-11 18:05:35', '2019-09-11 18:05:35');
INSERT INTO `engine_field` VALUES ('699', '224', 'mobile', '用户手机号', 'STRING', null, null, '0', '2019-09-11 18:05:35', '2019-09-11 18:05:35');
INSERT INTO `engine_field` VALUES ('700', '224', 'userIP', '用户IP', 'STRING', null, null, '0', '2019-09-11 18:05:35', '2019-09-11 18:05:35');
INSERT INTO `engine_field` VALUES ('701', '224', 'deviceId', '设备ID', 'STRING', null, null, '0', '2019-09-11 18:05:35', '2019-09-11 18:05:35');
INSERT INTO `engine_field` VALUES ('702', '224', 'os', '操作系统', 'STRING', null, null, '0', '2019-09-11 18:05:35', '2019-09-11 18:05:35');
INSERT INTO `engine_field` VALUES ('703', '224', 'channel', '来源渠道', 'STRING', null, null, '0', '2019-09-11 18:05:35', '2019-09-11 18:05:35');
INSERT INTO `engine_field` VALUES ('704', '224', 'amount', '交易金额', 'DOUBLE', null, null, '0', '2019-09-11 18:05:35', '2019-09-11 18:05:35');

-- ----------------------------
-- Table structure for engine_model
-- ----------------------------
DROP TABLE IF EXISTS `engine_model`;
CREATE TABLE `engine_model` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `GUID` varchar(64) COLLATE utf8_unicode_ci NOT NULL,
  `MODEL_NAME` varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT '模型名称',
  `LABEL` varchar(32) COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  `ENTITY_NAME` varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT '事件中标识实体的主键',
  `ENTRY_NAME` varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT '事件主键',
  `REFERENCE_DATE` varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT '事件时间',
  `FIELD_VALIDATE` tinyint(1) NOT NULL DEFAULT '0',
  `CODE` varchar(60) COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  `TEMPLATE` tinyint(1) NOT NULL DEFAULT '0' COMMENT '1 标识模板',
  `DASHBOARD_URL` varchar(300) COLLATE utf8_unicode_ci DEFAULT NULL,
  `STATUS` int(11) NOT NULL COMMENT '状态',
  `CREATE_TIME` datetime NOT NULL,
  `UPDATE_TIME` datetime NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=225 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ----------------------------
-- Records of engine_model
-- ----------------------------
INSERT INTO `engine_model` VALUES ('99', '697AA0D6-4B0F-4B57-A487-345CB10BDD8A', 'login_template', '登录行为模板', 'userId', 'eventId', 'loginTime', '0', 'pgmmer.top', '1', null, '0', '2016-11-17 10:59:43', '2016-11-18 18:02:15');
INSERT INTO `engine_model` VALUES ('101', '3EAE4543-814B-4AAB-9C96-F3172D2C9B16', 'register_template', '注册行为模板', 'userId', 'eventId', 'registerTime', '0', 'pgmmer.top', '1', null, '0', '2016-11-17 15:50:49', '2016-11-18 18:02:22');
INSERT INTO `engine_model` VALUES ('103', '03355AB9-4396-4740-8CA2-4E6643CDC0F1', 'transaction_template', '交易行为模板', 'userId', 'eventId', 'eventTime', '0', 'pgmmer.top', '1', null, '0', '2016-11-17 16:06:31', '2016-11-17 16:10:02');
INSERT INTO `engine_model` VALUES ('222', 'B6B69670-2E27-4363-8D7D-B6CBC73CEB60', 'model_222', 'Ting注册', 'userId', 'eventId', 'registerTime', '0', 'pgmmer.top', '0', null, '0', '2016-11-17 15:50:49', '2019-09-11 13:42:48');
INSERT INTO `engine_model` VALUES ('224', 'DB8A078F-97FE-4A7F-AAC0-5AF1A6C36CE8', 'model_224', 'Ting提现', 'userId', 'eventId', 'eventTime', '0', 'pgmmer.top', '0', null, '0', '2016-11-17 16:06:31', '2019-09-11 18:07:23');

-- ----------------------------
-- Table structure for engine_pre_item
-- ----------------------------
DROP TABLE IF EXISTS `engine_pre_item`;
CREATE TABLE `engine_pre_item` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `MODEL_ID` bigint(20) NOT NULL COMMENT '模型ID',
  `SOURCE_FIELD` varchar(32) COLLATE utf8_unicode_ci NOT NULL COMMENT '来源字段',
  `SOURCE_LABEL` varchar(32) COLLATE utf8_unicode_ci NOT NULL,
  `DEST_FIELD` varchar(32) COLLATE utf8_unicode_ci NOT NULL COMMENT '目标字段',
  `LABEL` varchar(32) COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  `ARGS` varchar(32) COLLATE utf8_unicode_ci DEFAULT '' COMMENT '参数',
  `PLUGIN` varchar(32) COLLATE utf8_unicode_ci NOT NULL COMMENT '转换插件',
  `STATUS` int(11) NOT NULL,
  `CREATE_TIME` datetime NOT NULL,
  `UPDATE_TIME` datetime NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=175 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ----------------------------
-- Records of engine_pre_item
-- ----------------------------
INSERT INTO `engine_pre_item` VALUES ('43', '99', 'userIP', '用户IP', 'ipLocationPre', 'IP归属地', '', 'IP2LOCATION', '1', '2016-11-17 11:23:45', '2016-11-17 11:26:21');
INSERT INTO `engine_pre_item` VALUES ('45', '99', 'mobile', '用户手机', 'mobileLocationPre', '手机号码归属地', '', 'MOBILE2LOCATION', '1', '2016-11-17 11:27:24', '2016-11-17 11:27:24');
INSERT INTO `engine_pre_item` VALUES ('47', '99', 'mobile', '用户手机', 'mobile7bPre', '手机号码段', '0,7', 'SUBSTRING', '1', '2016-11-17 11:37:33', '2016-11-17 11:37:33');
INSERT INTO `engine_pre_item` VALUES ('49', '101', 'userIP', '用户IP', 'ipLocationPre', 'IP归属地', '', 'IP2LOCATION', '1', '2016-11-17 17:11:05', '2016-11-17 17:11:05');
INSERT INTO `engine_pre_item` VALUES ('51', '101', 'mobile', '用户手机', 'mobileLocationPre', '手机号码归属地', '', 'MOBILE2LOCATION', '1', '2016-11-17 17:12:22', '2016-11-17 17:12:22');
INSERT INTO `engine_pre_item` VALUES ('53', '101', 'mobile', '用户手机', 'mobile7bPre', '手机号码段', '0,7', 'SUBSTRING', '1', '2016-11-17 17:13:10', '2016-11-17 17:13:10');
INSERT INTO `engine_pre_item` VALUES ('55', '103', 'userIP', '用户IP', 'ipLocationPre', 'IP归属地', '', 'IP2LOCATION', '1', '2016-11-17 17:14:25', '2016-11-17 17:14:25');
INSERT INTO `engine_pre_item` VALUES ('57', '103', 'mobile', '用户手机', 'mobileLocationPre', '手机号码归属地', '', 'MOBILE2LOCATION', '1', '2016-11-17 17:14:50', '2016-11-17 17:14:50');
INSERT INTO `engine_pre_item` VALUES ('59', '103', 'mobile', '用户手机', 'mobile7bPre', '手机号码段', '0,7', 'SUBSTRING', '1', '2016-11-17 17:15:34', '2016-11-17 17:15:34');
INSERT INTO `engine_pre_item` VALUES ('168', '222', 'userIP', '用户IP', 'ipLocationPre', 'IP归属地', '', 'IP2LOCATION', '1', '2019-09-10 11:16:12', '2019-09-10 11:16:12');
INSERT INTO `engine_pre_item` VALUES ('169', '222', 'mobile', '用户手机', 'mobileLocationPre', '手机号码归属地', '', 'MOBILE2LOCATION', '1', '2019-09-10 11:16:12', '2019-09-10 11:16:12');
INSERT INTO `engine_pre_item` VALUES ('170', '222', 'mobile', '用户手机', 'mobile7bPre', '手机号码段', '0,7', 'SUBSTRING', '1', '2019-09-10 11:16:12', '2019-09-10 11:16:12');
INSERT INTO `engine_pre_item` VALUES ('171', '222', 'registerTime', '注册时间', 'preItem_171', '小时', '', 'SENSITIVE_TIME', '1', '2019-09-10 14:52:56', '2019-09-10 14:52:56');
INSERT INTO `engine_pre_item` VALUES ('172', '224', 'userIP', '用户IP', 'ipLocationPre', 'IP归属地', '', 'IP2LOCATION', '1', '2019-09-11 18:05:35', '2019-09-11 18:05:35');
INSERT INTO `engine_pre_item` VALUES ('173', '224', 'mobile', '用户手机', 'mobileLocationPre', '手机号码归属地', '', 'MOBILE2LOCATION', '1', '2019-09-11 18:05:35', '2019-09-11 18:05:35');
INSERT INTO `engine_pre_item` VALUES ('174', '224', 'mobile', '用户手机', 'mobile7bPre', '手机号码段', '0,7', 'SUBSTRING', '1', '2019-09-11 18:05:35', '2019-09-11 18:05:35');

-- ----------------------------
-- Table structure for engine_rule
-- ----------------------------
DROP TABLE IF EXISTS `engine_rule`;
CREATE TABLE `engine_rule` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `MODEL_ID` bigint(20) NOT NULL COMMENT '模型ID',
  `ACTIVATION_ID` bigint(20) NOT NULL COMMENT '激活ID',
  `NAME` varchar(64) COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  `LABEL` varchar(64) COLLATE utf8_unicode_ci NOT NULL DEFAULT '' COMMENT '规则名称',
  `SCRIPTS` varchar(2048) COLLATE utf8_unicode_ci NOT NULL COMMENT '检验脚本',
  `INIT_SCORE` int(11) NOT NULL DEFAULT '0' COMMENT '初始分数',
  `BASE_NUM` int(11) NOT NULL DEFAULT '0' COMMENT '基数',
  `OPERATOR` varchar(32) COLLATE utf8_unicode_ci NOT NULL COMMENT '运算符',
  `ABSTRACTION_NAME` varchar(64) COLLATE utf8_unicode_ci NOT NULL COMMENT '抽象名称',
  `RATE` int(11) NOT NULL DEFAULT '100' COMMENT '比例',
  `STATUS` int(11) NOT NULL COMMENT '状态',
  `RULE_DEFINITION` varchar(2048) COLLATE utf8_unicode_ci DEFAULT NULL,
  `CREATE_TIME` datetime NOT NULL,
  `UPDATE_TIME` datetime NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=388 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ----------------------------
-- Records of engine_rule
-- ----------------------------
INSERT INTO `engine_rule` VALUES ('85', '103', '37', 'rule_85', '1天内IP交易次数大于30', 'class ActivationCheckScript {\n  public boolean check(def data, def lists) {    if (data.abstractions.tran_ip_1_day_qty>=30)\n        return true;\n    else\n        return false;\n}}', '20', '1', 'MUL', 'tran_ip_1_day_qty', '100', '1', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"abstractions.tran_ip_1_day_qty\",\"type\":\"DOUBLE\",\"class\":\"ENTATTR\"},{\"type\":\"DOUBLE\",\"class\":\"CONST\",\"value\":\"30\"}],\"enabled\":true,\"operator\":\"Greater_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}', '2016-11-18 11:41:21', '2016-11-18 11:41:21');
INSERT INTO `engine_rule` VALUES ('87', '103', '37', 'rule_87', '1小时内IP交易次数大于15', 'class ActivationCheckScript {\n  public boolean check(def data, def lists) {    if (data.abstractions.tran_ip_1_hour_qty>=15)\n        return true;\n    else\n        return false;\n}}', '20', '1', 'MUL', 'tran_ip_1_hour_qty', '100', '1', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"abstractions.tran_ip_1_hour_qty\",\"type\":\"DOUBLE\",\"class\":\"ENTATTR\"},{\"type\":\"DOUBLE\",\"class\":\"CONST\",\"value\":\"15\"}],\"enabled\":true,\"operator\":\"Greater_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}', '2016-11-18 11:43:14', '2016-11-18 11:43:14');
INSERT INTO `engine_rule` VALUES ('89', '103', '37', 'rule_89', '1天内IP交易金额大于1000', 'class ActivationCheckScript {\n  public boolean check(def data, def lists) {    if (data.abstractions.tran_ip_1_day_amt>=1000)\n        return true;\n    else\n        return false;\n}}', '20', '1', 'MUL', 'tran_ip_1_day_amt', '10', '1', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"abstractions.tran_ip_1_day_amt\",\"type\":\"DOUBLE\",\"class\":\"ENTATTR\"},{\"type\":\"DOUBLE\",\"class\":\"CONST\",\"value\":\"1000\"}],\"enabled\":true,\"operator\":\"Greater_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}', '2016-11-18 11:46:11', '2016-11-18 11:46:11');
INSERT INTO `engine_rule` VALUES ('91', '103', '37', 'rule_91', '1天内设备交易次数大于30', 'class ActivationCheckScript {\n  public boolean check(def data, def lists) {    if (data.abstractions.tran_did_1_day_qty>=30)\n        return true;\n    else\n        return false;\n}}', '20', '1', 'ADD', 'tran_did_1_day_qty', '100', '1', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"abstractions.tran_did_1_day_qty\",\"type\":\"DOUBLE\",\"class\":\"ENTATTR\"},{\"type\":\"DOUBLE\",\"class\":\"CONST\",\"value\":\"30\"}],\"enabled\":true,\"operator\":\"Greater_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}', '2016-11-18 11:47:24', '2016-11-18 11:47:24');
INSERT INTO `engine_rule` VALUES ('93', '103', '37', 'rule_93', '1天内IP关联设备数大于10', 'class ActivationCheckScript {\n  public boolean check(def data, def lists) {    if (data.abstractions.tran_did_ip_1_day_qty>=10)\n        return true;\n    else\n        return false;\n}}', '20', '1', 'MUL', 'tran_did_ip_1_day_qty', '100', '1', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"abstractions.tran_did_ip_1_day_qty\",\"type\":\"DOUBLE\",\"class\":\"ENTATTR\"},{\"type\":\"DOUBLE\",\"class\":\"CONST\",\"value\":\"10\"}],\"enabled\":true,\"operator\":\"Greater_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}', '2016-11-18 14:03:33', '2016-11-18 14:03:33');
INSERT INTO `engine_rule` VALUES ('95', '103', '37', 'rule_95', '1天内IP关联用户数大于10', 'class ActivationCheckScript {\n  public boolean check(def data, def lists) {    if (data.abstractions.tran_uid_ip_1_day_qty>=10)\n        return true;\n    else\n        return false;\n}}', '20', '1', 'MUL', 'tran_uid_ip_1_day_qty', '100', '1', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"abstractions.tran_uid_ip_1_day_qty\",\"type\":\"DOUBLE\",\"class\":\"ENTATTR\"},{\"type\":\"DOUBLE\",\"class\":\"CONST\",\"value\":\"10\"}],\"enabled\":true,\"operator\":\"Greater_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}', '2016-11-18 14:04:40', '2016-11-18 14:04:40');
INSERT INTO `engine_rule` VALUES ('97', '103', '37', 'rule_97', '手机归属地和IP归属地城市不一致', 'class ActivationCheckScript {\n  public boolean check(def data, def lists) {    if ((data.preItems.mobileLocationPre.city!=data.preItems.ipLocationPre.city))\n        return true;\n    else\n        return false;\n}}', '20', '0', 'NONE', '', '100', '1', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"preItems.mobileLocationPre.city\",\"type\":\"STRING\",\"class\":\"ENTATTR\"},{\"column\":\"preItems.ipLocationPre.city\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"Field_Not_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}', '2016-11-18 14:05:39', '2019-09-11 17:42:52');
INSERT INTO `engine_rule` VALUES ('99', '103', '37', 'rule_99', 'IP归属地不在中国', 'class ActivationCheckScript {\n  public boolean check(def data, def lists) {    if ((data.preItems.ipLocationPre.country!=\'中国\'))\n        return true;\n    else\n        return false;\n}}', '20', '0', 'NONE', '', '100', '1', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"preItems.ipLocationPre.country\",\"type\":\"STRING\",\"class\":\"ENTATTR\"},{\"type\":\"STRING\",\"class\":\"CONST\",\"value\":\"中国\"}],\"enabled\":true,\"operator\":\"NotEqual\"}],\"class\":\"PDCT\",\"enabled\":true}', '2016-11-18 14:06:43', '2019-09-11 17:42:57');
INSERT INTO `engine_rule` VALUES ('101', '99', '39', 'rule_101', '1天内IP关联用户数大于10', 'class ActivationCheckScript {\n  public boolean check(def data, def lists) {    if (data.abstractions.log_uid_ip_1_day_qty>=10)\n        return true;\n    else\n        return false;\n}}', '20', '1', 'MUL', 'log_uid_ip_1_day_qty', '100', '1', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"abstractions.log_uid_ip_1_day_qty\",\"type\":\"DOUBLE\",\"class\":\"ENTATTR\"},{\"type\":\"DOUBLE\",\"class\":\"CONST\",\"value\":\"10\"}],\"enabled\":true,\"operator\":\"Greater_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}', '2016-11-18 14:43:03', '2016-11-18 14:43:03');
INSERT INTO `engine_rule` VALUES ('103', '99', '39', 'rule_103', '1天内IP关联设备数大于10', 'class ActivationCheckScript {\n  public boolean check(def data, def lists) {    if (data.abstractions.log_did_ip_1_hour_qty>=10)\n        return true;\n    else\n        return false;\n}}', '20', '1', 'MUL', 'log_did_ip_1_day_qty', '100', '1', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"abstractions.log_did_ip_1_hour_qty\",\"type\":\"DOUBLE\",\"class\":\"ENTATTR\"},{\"type\":\"DOUBLE\",\"class\":\"CONST\",\"value\":\"10\"}],\"enabled\":true,\"operator\":\"Greater_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}', '2016-11-18 14:45:57', '2016-11-18 14:45:57');
INSERT INTO `engine_rule` VALUES ('105', '99', '39', 'rule_105', '10分钟内IP登录次数大于10', 'class ActivationCheckScript {\n  public boolean check(def data, def lists) {    if (data.abstractions.log_ip_10_min_qty>=10)\n        return true;\n    else\n        return false;\n}}', '20', '1', 'MUL', 'log_ip_10_min_qty', '100', '1', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"abstractions.log_ip_10_min_qty\",\"type\":\"DOUBLE\",\"class\":\"ENTATTR\"},{\"type\":\"DOUBLE\",\"class\":\"CONST\",\"value\":\"10\"}],\"enabled\":true,\"operator\":\"Greater_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}', '2016-11-18 14:47:12', '2016-11-18 14:47:12');
INSERT INTO `engine_rule` VALUES ('107', '99', '39', 'rule_107', 'IP登录位置显示国外', 'class ActivationCheckScript {\n  public boolean check(def data, def lists) {    if ((data.preItems.ipLocationPre.country!=\'中国\'))\n        return true;\n    else\n        return false;\n}}', '20', '0', 'NONE', '', '100', '1', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"preItems.ipLocationPre.country\",\"type\":\"STRING\",\"class\":\"ENTATTR\"},{\"type\":\"STRING\",\"class\":\"CONST\",\"value\":\"中国\"}],\"enabled\":true,\"operator\":\"NotEqual\"}],\"class\":\"PDCT\",\"enabled\":true}', '2016-11-18 14:48:19', '2019-09-11 17:40:42');
INSERT INTO `engine_rule` VALUES ('109', '101', '41', 'rule_109', '1天内IP关联注册数大于20', 'class ActivationCheckScript {\n  public boolean check(def data, def lists) {    if (data.abstractions.reg_ip_1_day_qty>=20)\n        return true;\n    else\n        return false;\n}}', '20', '1', 'MUL', 'reg_ip_1_day_qty', '100', '1', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"abstractions.reg_ip_1_day_qty\",\"type\":\"DOUBLE\",\"class\":\"ENTATTR\"},{\"type\":\"DOUBLE\",\"class\":\"CONST\",\"value\":\"20\"}],\"enabled\":true,\"operator\":\"Greater_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}', '2016-11-18 16:00:55', '2016-11-18 16:00:55');
INSERT INTO `engine_rule` VALUES ('111', '101', '41', 'rule_111', '1天内设备关联注册数大于20', 'class ActivationCheckScript {\n  public boolean check(def data, def lists) {    if (data.abstractions.reg_did_1_day_qty>=20)\n        return true;\n    else\n        return false;\n}}', '20', '1', 'MUL', 'reg_did_1_day_qty', '100', '1', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"abstractions.reg_did_1_day_qty\",\"type\":\"DOUBLE\",\"class\":\"ENTATTR\"},{\"type\":\"DOUBLE\",\"class\":\"CONST\",\"value\":\"20\"}],\"enabled\":true,\"operator\":\"Greater_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}', '2016-11-18 16:01:44', '2016-11-18 16:01:44');
INSERT INTO `engine_rule` VALUES ('113', '101', '41', 'rule_113', '10分钟内IP关联注册数大于15', 'class ActivationCheckScript {\n  public boolean check(def data, def lists) {    if (data.abstractions.reg_ip_10_min_qty>=15)\n        return true;\n    else\n        return false;\n}}', '30', '1', 'MUL', 'reg_ip_10_min_qty', '100', '1', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"abstractions.reg_ip_10_min_qty\",\"type\":\"DOUBLE\",\"class\":\"ENTATTR\"},{\"type\":\"DOUBLE\",\"class\":\"CONST\",\"value\":\"15\"}],\"enabled\":true,\"operator\":\"Greater_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}', '2016-11-18 16:02:57', '2016-11-18 16:02:57');
INSERT INTO `engine_rule` VALUES ('115', '101', '41', 'rule_115', '1天内设备关联用户数大于10', 'class ActivationCheckScript {\n  public boolean check(def data, def lists) {    if (data.abstractions.reg_uid_did_1_day_qty>=10)\n        return true;\n    else\n        return false;\n}}', '30', '1', 'MUL', 'reg_uid_did_1_day_qty', '100', '1', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"abstractions.reg_uid_did_1_day_qty\",\"type\":\"DOUBLE\",\"class\":\"ENTATTR\"},{\"type\":\"DOUBLE\",\"class\":\"CONST\",\"value\":\"10\"}],\"enabled\":true,\"operator\":\"Greater_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}', '2016-11-18 16:04:13', '2016-11-18 16:04:13');
INSERT INTO `engine_rule` VALUES ('117', '101', '41', 'rule_117', 'IP归属地城市与手机号码归属地城市不一致', 'class ActivationCheckScript {\n  public boolean check(def data, def lists) {    if ((data.preItems.ipLocationPre.city!=data.preItems.mobileLocationPre.city))\n        return true;\n    else\n        return false;\n}}', '20', '0', 'NONE', '', '100', '1', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"preItems.ipLocationPre.city\",\"type\":\"STRING\",\"class\":\"ENTATTR\"},{\"column\":\"preItems.mobileLocationPre.city\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"Field_Not_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}', '2016-11-18 16:05:29', '2019-09-11 17:41:16');
INSERT INTO `engine_rule` VALUES ('119', '101', '41', 'rule_119', '注册IP来源于国外', 'class ActivationCheckScript {\n  public boolean check(def data, def lists) {    if ((data.preItems.ipLocationPre.country!=\'中国\'))\n        return true;\n    else\n        return false;\n}}', '30', '0', 'NONE', '', '100', '1', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"preItems.ipLocationPre.country\",\"type\":\"STRING\",\"class\":\"ENTATTR\"},{\"type\":\"STRING\",\"class\":\"CONST\",\"value\":\"中国\"}],\"enabled\":true,\"operator\":\"NotEqual\"}],\"class\":\"PDCT\",\"enabled\":true}', '2016-11-18 16:07:01', '2019-09-11 17:41:08');
INSERT INTO `engine_rule` VALUES ('121', '99', '39', 'rule_121', '1天内用户登录次数大于30', 'class ActivationCheckScript {\n  public boolean check(def data, def lists) {    if (data.abstractions.log_uid_1_day_qty>=30)\n        return true;\n    else\n        return false;\n}}', '20', '1', 'MUL', 'log_uid_1_day_qty', '100', '1', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"abstractions.log_uid_1_day_qty\",\"type\":\"DOUBLE\",\"class\":\"ENTATTR\"},{\"type\":\"DOUBLE\",\"class\":\"CONST\",\"value\":\"30\"}],\"enabled\":true,\"operator\":\"Greater_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}', '2016-11-18 18:04:03', '2016-11-18 18:04:24');
INSERT INTO `engine_rule` VALUES ('127', '103', '37', 'rule_127', '命中手机号码白名单', 'class ActivationCheckScript {\n  public boolean check(def data, def lists) {    if (lists.mobile_white_list.containsKey(data.fields.mobile))\n        return true;\n    else\n        return false;\n}}', '-200', '0', 'NONE', '', '100', '1', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.mobile\",\"type\":\"STRING\",\"class\":\"ENTATTR\"},{\"type\":\"LIST\",\"class\":\"CONST\",\"value\":\"mobile_white_list\"}],\"enabled\":true,\"operator\":\"InList\"}],\"class\":\"PDCT\",\"enabled\":true}', '2016-11-22 14:12:57', '2016-11-22 14:13:06');
INSERT INTO `engine_rule` VALUES ('373', '222', '116', 'rule_109', '1天内IP关联注册数大于20', 'class ActivationCheckScript {\n  public boolean check(def data, def lists) {    if (data.abstractions.reg_ip_1_day_qty>=20)\n        return true;\n    else\n        return false;\n}}', '20', '1', 'MUL', 'reg_ip_1_day_qty', '100', '1', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"abstractions.reg_ip_1_day_qty\",\"type\":\"DOUBLE\",\"class\":\"ENTATTR\"},{\"type\":\"DOUBLE\",\"class\":\"CONST\",\"value\":\"20\"}],\"enabled\":true,\"operator\":\"Greater_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}', '2019-09-10 11:16:12', '2019-09-10 11:16:12');
INSERT INTO `engine_rule` VALUES ('374', '222', '116', 'rule_111', '1天内设备关联注册数大于20', 'class ActivationCheckScript {\n  public boolean check(def data, def lists) {    if (data.abstractions.reg_did_1_day_qty>=20)\n        return true;\n    else\n        return false;\n}}', '20', '1', 'MUL', 'reg_did_1_day_qty', '100', '1', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"abstractions.reg_did_1_day_qty\",\"type\":\"DOUBLE\",\"class\":\"ENTATTR\"},{\"type\":\"DOUBLE\",\"class\":\"CONST\",\"value\":\"20\"}],\"enabled\":true,\"operator\":\"Greater_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}', '2019-09-10 11:16:12', '2019-09-10 11:16:12');
INSERT INTO `engine_rule` VALUES ('375', '222', '116', 'rule_113', '10分钟内IP关联注册数大于15', 'class ActivationCheckScript {\n  public boolean check(def data, def lists) {    if (data.abstractions.reg_ip_10_min_qty>=15)\n        return true;\n    else\n        return false;\n}}', '30', '1', 'MUL', 'reg_ip_10_min_qty', '100', '1', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"abstractions.reg_ip_10_min_qty\",\"type\":\"DOUBLE\",\"class\":\"ENTATTR\"},{\"type\":\"DOUBLE\",\"class\":\"CONST\",\"value\":\"15\"}],\"enabled\":true,\"operator\":\"Greater_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}', '2019-09-10 11:16:12', '2019-09-10 11:16:12');
INSERT INTO `engine_rule` VALUES ('376', '222', '116', 'rule_115', '1天内设备关联用户数大于10', 'class ActivationCheckScript {\n  public boolean check(def data, def lists) {    if (data.abstractions.reg_uid_did_1_day_qty>=10)\n        return true;\n    else\n        return false;\n}}', '30', '1', 'MUL', 'reg_uid_did_1_day_qty', '100', '1', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"abstractions.reg_uid_did_1_day_qty\",\"type\":\"DOUBLE\",\"class\":\"ENTATTR\"},{\"type\":\"DOUBLE\",\"class\":\"CONST\",\"value\":\"10\"}],\"enabled\":true,\"operator\":\"Greater_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}', '2019-09-10 11:16:12', '2019-09-10 11:16:12');
INSERT INTO `engine_rule` VALUES ('377', '222', '116', 'rule_377', 'IP归属地城市与手机号码归属地城市不一致', 'class ActivationCheckScript {\n  public boolean check(def data, def lists) {    if ((data.preItems.ipLocationPre.city!=data.preItems.mobileLocationPre.city))\n        return true;\n    else\n        return false;\n}}', '20', '0', 'NONE', '', '100', '1', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"preItems.ipLocationPre.city\",\"type\":\"STRING\",\"class\":\"ENTATTR\"},{\"column\":\"preItems.mobileLocationPre.city\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"Field_Not_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}', '2019-09-10 11:16:12', '2019-09-11 17:27:20');
INSERT INTO `engine_rule` VALUES ('378', '222', '116', 'rule_378', '注册IP来源于国外', 'class ActivationCheckScript {\n  public boolean check(def data, def lists) {    if ((data.preItems.ipLocationPre.country!=\'中国\'))\n        return true;\n    else\n        return false;\n}}', '30', '0', 'NONE', '', '100', '1', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"preItems.ipLocationPre.country\",\"type\":\"STRING\",\"class\":\"ENTATTR\"},{\"type\":\"STRING\",\"class\":\"CONST\",\"value\":\"中国\"}],\"enabled\":true,\"operator\":\"NotEqual\"}],\"class\":\"PDCT\",\"enabled\":true}', '2019-09-10 11:16:12', '2019-09-11 17:29:18');
INSERT INTO `engine_rule` VALUES ('379', '224', '117', 'rule_85', '1天内IP交易次数大于30', 'class ActivationCheckScript {\n  public boolean check(def data, def lists) {    if (data.abstractions.tran_ip_1_day_qty>=30)\n        return true;\n    else\n        return false;\n}}', '20', '1', 'MUL', 'tran_ip_1_day_qty', '100', '1', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"abstractions.tran_ip_1_day_qty\",\"type\":\"DOUBLE\",\"class\":\"ENTATTR\"},{\"type\":\"DOUBLE\",\"class\":\"CONST\",\"value\":\"30\"}],\"enabled\":true,\"operator\":\"Greater_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}', '2019-09-11 18:05:35', '2019-09-11 18:05:35');
INSERT INTO `engine_rule` VALUES ('380', '224', '117', 'rule_87', '1小时内IP交易次数大于15', 'class ActivationCheckScript {\n  public boolean check(def data, def lists) {    if (data.abstractions.tran_ip_1_hour_qty>=15)\n        return true;\n    else\n        return false;\n}}', '20', '1', 'MUL', 'tran_ip_1_hour_qty', '100', '1', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"abstractions.tran_ip_1_hour_qty\",\"type\":\"DOUBLE\",\"class\":\"ENTATTR\"},{\"type\":\"DOUBLE\",\"class\":\"CONST\",\"value\":\"15\"}],\"enabled\":true,\"operator\":\"Greater_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}', '2019-09-11 18:05:35', '2019-09-11 18:05:35');
INSERT INTO `engine_rule` VALUES ('381', '224', '117', 'rule_89', '1天内IP交易金额大于1000', 'class ActivationCheckScript {\n  public boolean check(def data, def lists) {    if (data.abstractions.tran_ip_1_day_amt>=1000)\n        return true;\n    else\n        return false;\n}}', '20', '1', 'MUL', 'tran_ip_1_day_amt', '10', '1', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"abstractions.tran_ip_1_day_amt\",\"type\":\"DOUBLE\",\"class\":\"ENTATTR\"},{\"type\":\"DOUBLE\",\"class\":\"CONST\",\"value\":\"1000\"}],\"enabled\":true,\"operator\":\"Greater_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}', '2019-09-11 18:05:35', '2019-09-11 18:05:35');
INSERT INTO `engine_rule` VALUES ('382', '224', '117', 'rule_91', '1天内设备交易次数大于30', 'class ActivationCheckScript {\n  public boolean check(def data, def lists) {    if (data.abstractions.tran_did_1_day_qty>=30)\n        return true;\n    else\n        return false;\n}}', '20', '1', 'ADD', 'tran_did_1_day_qty', '100', '1', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"abstractions.tran_did_1_day_qty\",\"type\":\"DOUBLE\",\"class\":\"ENTATTR\"},{\"type\":\"DOUBLE\",\"class\":\"CONST\",\"value\":\"30\"}],\"enabled\":true,\"operator\":\"Greater_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}', '2019-09-11 18:05:35', '2019-09-11 18:05:35');
INSERT INTO `engine_rule` VALUES ('383', '224', '117', 'rule_93', '1天内IP关联设备数大于10', 'class ActivationCheckScript {\n  public boolean check(def data, def lists) {    if (data.abstractions.tran_did_ip_1_day_qty>=10)\n        return true;\n    else\n        return false;\n}}', '20', '1', 'MUL', 'tran_did_ip_1_day_qty', '100', '1', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"abstractions.tran_did_ip_1_day_qty\",\"type\":\"DOUBLE\",\"class\":\"ENTATTR\"},{\"type\":\"DOUBLE\",\"class\":\"CONST\",\"value\":\"10\"}],\"enabled\":true,\"operator\":\"Greater_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}', '2019-09-11 18:05:35', '2019-09-11 18:05:35');
INSERT INTO `engine_rule` VALUES ('384', '224', '117', 'rule_95', '1天内IP关联用户数大于10', 'class ActivationCheckScript {\n  public boolean check(def data, def lists) {    if (data.abstractions.tran_uid_ip_1_day_qty>=10)\n        return true;\n    else\n        return false;\n}}', '20', '1', 'MUL', 'tran_uid_ip_1_day_qty', '100', '1', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"abstractions.tran_uid_ip_1_day_qty\",\"type\":\"DOUBLE\",\"class\":\"ENTATTR\"},{\"type\":\"DOUBLE\",\"class\":\"CONST\",\"value\":\"10\"}],\"enabled\":true,\"operator\":\"Greater_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}', '2019-09-11 18:05:35', '2019-09-11 18:05:35');
INSERT INTO `engine_rule` VALUES ('385', '224', '117', 'rule_97', '手机归属地和IP归属地城市不一致', 'class ActivationCheckScript {\n  public boolean check(def data, def lists) {    if ((data.preItems.mobileLocationPre.city!=data.preItems.ipLocationPre.city))\n        return true;\n    else\n        return false;\n}}', '20', '0', 'NONE', '', '100', '1', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"preItems.mobileLocationPre.city\",\"type\":\"STRING\",\"class\":\"ENTATTR\"},{\"column\":\"preItems.ipLocationPre.city\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"Field_Not_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}', '2019-09-11 18:05:35', '2019-09-11 18:05:35');
INSERT INTO `engine_rule` VALUES ('386', '224', '117', 'rule_99', 'IP归属地不在中国', 'class ActivationCheckScript {\n  public boolean check(def data, def lists) {    if ((data.preItems.ipLocationPre.country!=\'中国\'))\n        return true;\n    else\n        return false;\n}}', '20', '0', 'NONE', '', '100', '1', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"preItems.ipLocationPre.country\",\"type\":\"STRING\",\"class\":\"ENTATTR\"},{\"type\":\"STRING\",\"class\":\"CONST\",\"value\":\"中国\"}],\"enabled\":true,\"operator\":\"NotEqual\"}],\"class\":\"PDCT\",\"enabled\":true}', '2019-09-11 18:05:35', '2019-09-11 18:05:35');
INSERT INTO `engine_rule` VALUES ('387', '224', '117', 'rule_127', '命中手机号码白名单', 'class ActivationCheckScript {\n  public boolean check(def data, def lists) {    if (lists.mobile_white_list.containsKey(data.fields.mobile))\n        return true;\n    else\n        return false;\n}}', '-200', '0', 'NONE', '', '100', '1', '{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.mobile\",\"type\":\"STRING\",\"class\":\"ENTATTR\"},{\"type\":\"LIST\",\"class\":\"CONST\",\"value\":\"mobile_white_list\"}],\"enabled\":true,\"operator\":\"InList\"}],\"class\":\"PDCT\",\"enabled\":true}', '2019-09-11 18:05:35', '2019-09-11 18:05:35');

-- ----------------------------
-- Table structure for engine_rule_history
-- ----------------------------
DROP TABLE IF EXISTS `engine_rule_history`;
CREATE TABLE `engine_rule_history` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `RULE_ID` bigint(20) NOT NULL COMMENT '模型ID',
  `MERCHANT_CODE` varchar(60) COLLATE utf8_unicode_ci NOT NULL,
  `LABEL` varchar(64) COLLATE utf8_unicode_ci NOT NULL DEFAULT '' COMMENT '规则名称',
  `INIT_SCORE` int(11) NOT NULL DEFAULT '0' COMMENT '初始分数',
  `BASE_NUM` int(11) NOT NULL DEFAULT '0' COMMENT '基数',
  `OPERATOR` varchar(32) COLLATE utf8_unicode_ci NOT NULL COMMENT '运算符',
  `ABSTRACTION_NAME` varchar(64) COLLATE utf8_unicode_ci NOT NULL COMMENT '抽象名称',
  `RATE` int(11) NOT NULL DEFAULT '100' COMMENT '比例',
  `RULE_DEFINITION` varchar(2048) COLLATE utf8_unicode_ci NOT NULL,
  `UPDATE_TIME` datetime NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ----------------------------
-- Records of engine_rule_history
-- ----------------------------
INSERT INTO `engine_rule_history` VALUES ('1', '377', 'admin', 'IP归属地城市与手机号码归属地城市不一致', '20', '0', 'NONE', '', '100', '', '2019-09-11 17:27:20');
INSERT INTO `engine_rule_history` VALUES ('2', '378', 'admin', '注册IP来源于国外', '30', '0', 'NONE', '', '100', '', '2019-09-11 17:29:18');
INSERT INTO `engine_rule_history` VALUES ('3', '107', 'admin', 'IP登录位置显示国外', '20', '0', 'NONE', '', '100', '', '2019-09-11 17:40:42');
INSERT INTO `engine_rule_history` VALUES ('4', '119', 'admin', '注册IP来源于国外', '30', '0', 'NONE', '', '100', '', '2019-09-11 17:41:08');
INSERT INTO `engine_rule_history` VALUES ('5', '117', 'admin', 'IP归属地城市与手机号码归属地城市不一致', '20', '0', 'NONE', '', '100', '', '2019-09-11 17:41:16');
INSERT INTO `engine_rule_history` VALUES ('6', '97', 'admin', '手机归属地和IP归属地城市不一致', '20', '0', 'NONE', '', '100', '', '2019-09-11 17:42:52');
INSERT INTO `engine_rule_history` VALUES ('7', '99', 'admin', 'IP归属地不在中国', '20', '0', 'NONE', '', '100', '', '2019-09-11 17:42:57');

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `USER_NAME` varchar(32) DEFAULT NULL,
  `PASSWD` varchar(64) DEFAULT NULL,
  `CODE` varchar(64) DEFAULT NULL,
  `STATUS` varchar(2) DEFAULT NULL,
  `CREATE_TIME` datetime DEFAULT NULL,
  `UPDATE_TIME` datetime DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES ('1', 'admin', '223CE7B851123353479D85757FBBF4E320D1E251', 'pgmmer.top', '1', '2019-07-24 18:10:11', '2019-07-24 18:10:14');
INSERT INTO `users` VALUES ('2', 'test', 'B2478239CD7E68E8052398D8EB87D385BF962085', 'ting.pgmmer.top', '1', '2019-07-24 18:15:30', '2019-07-24 18:15:33');
