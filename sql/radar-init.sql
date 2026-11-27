-- Radar v2.0.0 全量基线（schema + 种子数据）
-- 合并自：radar-init.sql（2019 基线）+ radar-1.0.6/1.0.7/1.0.8 增量，数据内容未变更。
-- 由 MySQL 8.0 真实迁移后导出，已回灌验证；charset 使用 utf8（mb3 别名），MySQL 5.7/8.0 均可导入。
-- 用途：docker-entrypoint-initdb.d 自动建库 / AbstractIntegrationTest 容器初始化 / 手工部署。

SET NAMES utf8mb4;

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `data_moble_info`
--

DROP TABLE IF EXISTS `data_moble_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `data_moble_info` (
  `ID` bigint NOT NULL AUTO_INCREMENT,
  `MOBILE` varchar(7) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT '主键',
  `PROVINCE` varchar(30) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT '省',
  `CITY` varchar(30) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT '市',
  `SUPPLIER` varchar(30) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT '卡信息',
  `REGION_CODE` varchar(4) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT '区号',
  `CREATE_TIME` datetime NOT NULL,
  `UPDATE_TIME` datetime NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `data_moble_info`
--

LOCK TABLES `data_moble_info` WRITE;
/*!40000 ALTER TABLE `data_moble_info` DISABLE KEYS */;
/*!40000 ALTER TABLE `data_moble_info` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `engine_abstraction`
--

DROP TABLE IF EXISTS `engine_abstraction`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `engine_abstraction` (
  `ID` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `NAME` varchar(64) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT 'Abstraction 名称',
  `LABEL` varchar(32) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  `MODEL_ID` bigint NOT NULL COMMENT 'MODEL ID',
  `AGGREGATE_TYPE` int NOT NULL COMMENT '统计类型',
  `SEARCH_FIELD` varchar(64) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL,
  `SEARCH_INTERVAL_TYPE` int NOT NULL COMMENT '时间段类型',
  `SEARCH_INTERVAL_VALUE` int NOT NULL COMMENT '时间段具体值',
  `FUNCTION_FIELD` varchar(64) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL,
  `RULE_SCRIPT` varchar(2048) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT '数据校验',
  `RULE_DEFINITION` varchar(2048) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL,
  `STATUS` int NOT NULL COMMENT '状态',
  `COMMENT` varchar(128) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '备注',
  `CREATE_TIME` datetime NOT NULL,
  `UPDATE_TIME` datetime NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=475 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `engine_abstraction`
--

LOCK TABLES `engine_abstraction` WRITE;
/*!40000 ALTER TABLE `engine_abstraction` DISABLE KEYS */;
INSERT INTO `engine_abstraction` VALUES (85,'log_ip_1_day_qty','1天内IP登录次数',99,1,'fields.userIP',5,1,'','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2016-11-17 11:48:07','2016-11-17 11:48:07'),(87,'log_did_1_day_qty','1天内设备登录次数',99,1,'fields.deviceId',5,1,'','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.deviceId)\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.deviceId\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2016-11-17 11:49:44','2016-11-17 11:49:51'),(89,'log_uid_1_day_qty','1天内用户登录次数',99,1,'fields.userId',5,1,'','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userId)\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userId\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2016-11-17 11:51:21','2016-11-17 13:55:41'),(91,'log_ip_1_hour_qty','1小时内IP登录次数',99,2,'fields.userIP',11,1,'','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2016-11-17 14:03:40','2016-11-17 14:03:40'),(93,'log_did_1_hour_qty','1小时内设备登录次数',99,1,'fields.deviceId',11,1,'','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.deviceId)\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.deviceId\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2016-11-17 14:05:53','2016-11-17 18:12:11'),(95,'log_uid_1_hour_qty','1小时内用户登录次数',99,1,'fields.userId',11,1,'','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userId)\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userId\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2016-11-17 14:16:45','2016-11-17 18:12:01'),(97,'log_ip_10_min_qty','10分钟内IP登录次数',99,1,'fields.userIP',12,10,'','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2016-11-17 14:23:54','2016-11-17 18:11:39'),(99,'log_did_10_min_qty','10分钟内设备登录次数',99,1,'fields.deviceId',12,10,'','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.deviceId)\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.deviceId\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2016-11-17 14:29:25','2016-11-17 14:47:48'),(101,'log_uid_10_min_qty','10分钟内用户登录次数',99,1,'fields.userId',12,10,'','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userId)\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userId\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2016-11-17 14:47:38','2016-11-17 18:11:32'),(103,'log_did_ip_1_day_qty','1天内IP关联设备数',99,2,'fields.userIP',5,1,'fields.deviceId','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2016-11-17 15:34:50','2016-11-17 15:34:50'),(105,'log_uid_ip_1_day_qty','1天内IP关联用户数',99,2,'fields.userIP',5,1,'fields.userId','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2016-11-17 15:36:18','2016-11-17 15:36:18'),(107,'log_did_ip_1_hour_qty','1小时内IP关联设备数',99,2,'fields.userIP',11,1,'fields.deviceId','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if ((data.fields.userIP))\n        return true;\n    else\n        return false;\n}}','{\"class\":\"PDCT\",\"enabled\":true,\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"enabled\":true,\"operator\":\"IsNotNull\",\"expressions\":[{\"class\":\"ENTATTR\",\"type\":\"STRING\",\"column\":\"fields.userIP\"}]}]}',1,'','2016-11-17 15:39:18','2019-09-11 13:53:43'),(109,'log_uid_ip_1_hour_qty','1小时内IP关联用户数',99,2,'fields.userIP',11,1,'fields.userId','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2016-11-17 15:41:37','2016-11-17 15:41:37'),(111,'tran_ip_1_day_qty','1天内IP交易量_笔数',103,1,'fields.userIP',5,1,'','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2016-11-17 18:14:34','2016-11-17 18:14:34'),(113,'tran_ip_1_hour_qty','1小时内IP交易量_笔数',103,1,'fields.userIP',11,1,'','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2016-11-17 18:17:32','2016-11-17 18:17:32'),(115,'tran_ip_10_min_qty','10分钟内IP交易量_笔数',103,1,'fields.userIP',12,10,'','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2016-11-17 18:19:03','2016-11-17 18:19:03'),(117,'tran_did_1_day_qty','1天内设备交易量_笔数',103,1,'fields.deviceId',5,1,'','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.deviceId)\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.deviceId\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2016-11-17 18:26:09','2016-11-17 18:26:09'),(119,'tran_did_1_hour_qty','1小时内设备交易量_笔数',103,1,'fields.deviceId',11,1,'','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.deviceId)\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.deviceId\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2016-11-17 18:27:11','2016-11-17 18:27:11'),(121,'tran_did_10_min_qty','10分钟内设备交易量_笔数',103,1,'fields.deviceId',12,10,'','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.deviceId)\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.deviceId\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2016-11-17 18:29:09','2016-11-17 18:29:09'),(123,'tran_ip_1_day_amt','1天内IP交易总金额',103,3,'fields.userIP',5,1,'fields.amount','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2016-11-18 10:57:29','2016-11-18 10:57:29'),(125,'tran_ip_1_hour_amt','1小时内IP交易总金额',103,3,'fields.userIP',11,1,'fields.amount','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2016-11-18 10:58:28','2016-11-18 10:58:41'),(127,'tran_ip_10_min_amt','10分钟内IP交易总金额',103,3,'fields.userIP',12,10,'fields.amount','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2016-11-18 11:14:42','2016-11-18 11:14:42'),(129,'tran_uid_1_day_amt','1天内用户交易总金额',103,3,'fields.userId',5,1,'fields.amount','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userId)\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userId\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2016-11-18 11:18:00','2016-11-18 11:18:00'),(131,'tran_uid_1_hour_amt','1小时内用户交易总金额',103,3,'fields.userId',11,1,'fields.amount','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if ((data.fields.userId))\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userId\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2016-11-18 11:27:42','2019-09-11 13:44:32'),(133,'tran_uid_10_min_amt','10分钟内用户交易总金额',103,3,'fields.userId',12,10,'fields.amount','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userId)\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userId\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2016-11-18 11:29:42','2016-11-18 11:29:42'),(135,'tran_uid_ip_1_day_qty','1天内IP关联用户数',103,2,'fields.userIP',5,1,'fields.userId','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2016-11-18 14:00:10','2016-11-18 14:00:10'),(137,'tran_did_ip_1_day_qty','1天内IP关联设备数',103,2,'fields.userIP',5,1,'fields.deviceId','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2016-11-18 14:01:59','2016-11-18 14:01:59'),(139,'reg_ip_1_day_qty','1天内IP关联注册数',101,1,'fields.userIP',5,1,'','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2016-11-18 15:06:57','2016-11-18 15:06:57'),(141,'reg_ip_1_hour_qty','1小时内IP关联注册数',101,1,'fields.userIP',11,1,'','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2016-11-18 15:07:51','2016-11-18 15:08:10'),(143,'reg_ip_10_min_qty','10分钟内IP关联注册数',101,1,'fields.userIP',12,10,'','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2016-11-18 15:09:15','2016-11-18 15:09:15'),(145,'reg_did_1_day_qty','1天内设备关联注册数',101,1,'fields.deviceId',5,1,'','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.deviceId)\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.deviceId\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2016-11-18 15:10:23','2016-11-18 15:10:41'),(147,'reg_did_1_hour_qty','1小时内设备关联注册数',101,1,'fields.deviceId',11,1,'','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.deviceId)\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.deviceId\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2016-11-18 15:20:13','2016-11-18 15:20:13'),(149,'reg_did_10_min_qty','10分钟内设备关联注册数',101,1,'fields.deviceId',12,10,'','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.deviceId)\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.deviceId\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2016-11-18 15:21:52','2016-11-18 15:21:52'),(151,'reg_uid_ip_1_day_qty','1天内IP关联用户数',101,2,'fields.userIP',5,1,'fields.userId','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2016-11-18 15:23:24','2016-11-18 15:23:24'),(153,'reg_uid_did_1_day_qty','1天内设备关联用户数',101,2,'fields.deviceId',5,1,'fields.userId','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.deviceId)\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.deviceId\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2016-11-18 15:53:34','2016-11-18 15:53:34'),(453,'reg_ip_1_day_qty','1天内IP关联注册数',222,1,'fields.userIP',5,1,'','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2019-09-10 11:16:12','2019-09-10 11:16:12'),(454,'reg_ip_1_hour_qty','1小时内IP关联注册数',222,1,'fields.userIP',11,1,'','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2019-09-10 11:16:12','2019-09-10 11:16:12'),(455,'reg_ip_10_min_qty','10分钟内IP关联注册数',222,1,'fields.userIP',12,10,'','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2019-09-10 11:16:12','2019-09-10 11:16:12'),(456,'reg_did_1_day_qty','1天内设备关联注册数',222,1,'fields.deviceId',5,1,'','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.deviceId)\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.deviceId\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2019-09-10 11:16:12','2019-09-10 11:16:12'),(457,'reg_did_1_hour_qty','1小时内设备关联注册数',222,1,'fields.deviceId',11,1,'','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.deviceId)\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.deviceId\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2019-09-10 11:16:12','2019-09-10 11:16:12'),(458,'reg_did_10_min_qty','10分钟内设备关联注册数',222,1,'fields.deviceId',12,10,'','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.deviceId)\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.deviceId\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2019-09-10 11:16:12','2019-09-10 11:16:12'),(459,'reg_uid_ip_1_day_qty','1天内IP关联用户数',222,2,'fields.userIP',5,1,'fields.userId','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2019-09-10 11:16:12','2019-09-10 11:16:12'),(460,'reg_uid_did_1_day_qty','1天内设备关联用户数',222,2,'fields.deviceId',5,1,'fields.userId','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.deviceId)\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.deviceId\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2019-09-10 11:16:12','2019-09-10 11:16:12'),(461,'tran_ip_1_day_qty','1天内IP交易量_笔数',224,1,'fields.userIP',5,1,'','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2019-09-11 18:05:35','2019-09-11 18:05:35'),(462,'tran_ip_1_hour_qty','1小时内IP交易量_笔数',224,1,'fields.userIP',11,1,'','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2019-09-11 18:05:35','2019-09-11 18:05:35'),(463,'tran_ip_10_min_qty','10分钟内IP交易量_笔数',224,1,'fields.userIP',12,10,'','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2019-09-11 18:05:35','2019-09-11 18:05:35'),(464,'tran_did_1_day_qty','1天内设备交易量_笔数',224,1,'fields.deviceId',5,1,'','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.deviceId)\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.deviceId\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2019-09-11 18:05:35','2019-09-11 18:05:35'),(465,'tran_did_1_hour_qty','1小时内设备交易量_笔数',224,1,'fields.deviceId',11,1,'','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.deviceId)\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.deviceId\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2019-09-11 18:05:35','2019-09-11 18:05:35'),(466,'tran_did_10_min_qty','10分钟内设备交易量_笔数',224,1,'fields.deviceId',12,10,'','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.deviceId)\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.deviceId\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2019-09-11 18:05:35','2019-09-11 18:05:35'),(467,'tran_ip_1_day_amt','1天内IP交易总金额',224,3,'fields.userIP',5,1,'fields.amount','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2019-09-11 18:05:35','2019-09-11 18:05:35'),(468,'tran_ip_1_hour_amt','1小时内IP交易总金额',224,3,'fields.userIP',11,1,'fields.amount','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2019-09-11 18:05:35','2019-09-11 18:05:35'),(469,'tran_ip_10_min_amt','10分钟内IP交易总金额',224,3,'fields.userIP',12,10,'fields.amount','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2019-09-11 18:05:35','2019-09-11 18:05:35'),(470,'tran_uid_1_day_amt','1天内用户交易总金额',224,3,'fields.userId',5,1,'fields.amount','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userId)\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userId\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2019-09-11 18:05:35','2019-09-11 18:05:35'),(471,'tran_uid_1_hour_amt','1小时内用户交易总金额',224,3,'fields.userId',11,1,'fields.amount','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if ((data.fields.userId))\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userId\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2019-09-11 18:05:35','2019-09-11 18:05:35'),(472,'tran_uid_10_min_amt','10分钟内用户交易总金额',224,3,'fields.userId',12,10,'fields.amount','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userId)\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userId\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2019-09-11 18:05:35','2019-09-11 18:05:35'),(473,'tran_uid_ip_1_day_qty','1天内IP关联用户数',224,2,'fields.userIP',5,1,'fields.userId','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2019-09-11 18:05:35','2019-09-11 18:05:35'),(474,'tran_did_ip_1_day_qty','1天内IP关联设备数',224,2,'fields.userIP',5,1,'fields.deviceId','class AbstractionCheckScript {\n  public boolean check(def data, def lists) {    if (data.fields.userIP)\n        return true;\n    else\n        return false;\n}}','{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.userIP\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"IsNotNull\"}],\"class\":\"PDCT\",\"enabled\":true}',1,'','2019-09-11 18:05:35','2019-09-11 18:05:35');
/*!40000 ALTER TABLE `engine_abstraction` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `engine_strategy`
--

DROP TABLE IF EXISTS `engine_strategy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `engine_strategy` (
  `ID` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `STRATEGY_NAME` varchar(64) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT '名称',
  `TYPE` varchar(32) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL DEFAULT 'RULE_SET' COMMENT '策略类型：RULE_SET/SCORECARD/DECISION_TREE',
  `LABEL` varchar(32) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  `MODEL_ID` bigint NOT NULL COMMENT 'model id',
  `COMMENT` varchar(128) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT '注释',
  `BOTTOM` int NOT NULL DEFAULT '0' COMMENT '底部阀值',
  `MEDIAN` int NOT NULL DEFAULT '0' COMMENT '中间阀值',
  `HIGH` int NOT NULL DEFAULT '0' COMMENT '顶部阀值',
  `STATUS` int NOT NULL COMMENT '状态',
  `RULE_ORDER` varchar(256) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL,
  `DEFINITION` mediumtext CHARACTER SET utf8 COLLATE utf8_unicode_ci COMMENT '策略类型化定义（评分卡/决策树 JSON，规则集为空）',
  `CREATE_TIME` datetime NOT NULL,
  `UPDATE_TIME` datetime NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=118 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `engine_strategy_history`
--

DROP TABLE IF EXISTS `engine_strategy_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `engine_strategy_history` (
  `ID` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `STRATEGY_ID` bigint NOT NULL COMMENT '策略ID',
  `MERCHANT_CODE` varchar(64) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '操作人',
  `TYPE` varchar(32) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '策略类型',
  `DEFINITION` mediumtext CHARACTER SET utf8 COLLATE utf8_unicode_ci COMMENT '保存时的定义快照',
  `UPDATE_TIME` datetime NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `idx_strategy_history_sid` (`STRATEGY_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='策略定义变更历史（v4.0）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `engine_strategy`
--

LOCK TABLES `engine_strategy` WRITE;
/*!40000 ALTER TABLE `engine_strategy` DISABLE KEYS */;
INSERT INTO `engine_strategy` VALUES (37,'transaction_exception','RULE_SET','异常交易',103,'',0,40,80,1,'85,87,89,91,93,95,97,99,127',NULL,'2016-11-18 11:38:38','2019-09-11 17:41:30'),(39,'login_exception','RULE_SET','登录异常',99,'',0,40,80,1,'101,103,105,107,121',NULL,'2016-11-18 14:40:13','2019-09-09 16:17:21'),(41,'register_exception','RULE_SET','异常注册',101,'',0,40,80,1,'109,111,113,115,117,119',NULL,'2016-11-18 15:57:13','2019-09-11 17:41:04'),(116,'register_exception','RULE_SET','异常注册',222,'',0,40,80,1,'373,374,375,376,377,378',NULL,'2019-09-10 11:16:12','2019-09-10 14:22:46'),(117,'transaction_exception','RULE_SET','异常交易',224,'',0,40,80,1,'379,380,381,382,383,384,385,386,387',NULL,'2019-09-11 18:05:35','2019-09-11 18:11:07');
/*!40000 ALTER TABLE `engine_strategy` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `engine_data_list_meta`
--

DROP TABLE IF EXISTS `engine_data_list_meta`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `engine_data_list_meta` (
  `ID` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `DATA_LIST_ID` bigint NOT NULL COMMENT '数据列表ID',
  `FIELD_NAME` varchar(32) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT '字段名',
  `LABEL` varchar(32) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  `SEQ_NUM` int NOT NULL COMMENT '字段序号',
  `CREATE_TIME` datetime NOT NULL,
  `UPDATE_TIME` datetime NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=96 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `engine_data_list_meta`
--

LOCK TABLES `engine_data_list_meta` WRITE;
/*!40000 ALTER TABLE `engine_data_list_meta` DISABLE KEYS */;
INSERT INTO `engine_data_list_meta` VALUES (1,1,'brand','手机品牌',1,'2016-08-17 17:42:49','2016-11-18 18:41:33'),(3,3,'mobile','手机号码',1,'2016-08-24 18:42:35','2016-11-18 18:41:35'),(5,5,'mobile','手机号码',1,'2016-08-24 18:43:39','2016-11-18 18:41:38'),(7,7,'ip','IP',1,'2016-08-25 13:58:24','2016-11-18 18:41:41'),(9,9,'ip','IP',1,'2016-09-18 10:27:46','2016-11-18 18:32:16'),(15,13,'sjhm','',1,'2016-09-23 15:54:40','2016-09-23 15:54:40'),(17,21,'testaaa','',1,'2016-09-28 14:41:34','2016-09-29 11:32:50'),(19,23,'ip','IP',1,'2016-10-14 11:12:36','2016-10-14 11:12:36'),(21,25,'ip','IP',1,'2016-10-14 11:15:32','2016-10-14 11:19:57'),(23,29,'mobile','手机号码',1,'2016-11-21 14:08:20','2016-11-21 14:08:20'),(25,31,'mobile','手机号码',1,'2016-11-21 14:10:12','2016-11-21 14:10:12'),(27,33,'mobile','手机号码',1,'2016-11-21 14:11:50','2016-11-21 14:11:50'),(29,35,'brand','手机品牌',1,'2016-11-25 17:28:07','2016-11-25 17:28:07'),(94,84,'mobile','手机号码',1,'2019-09-10 11:16:12','2019-09-10 11:16:12'),(95,85,'mobile','手机号码',1,'2019-09-11 18:05:35','2019-09-11 18:05:35');
/*!40000 ALTER TABLE `engine_data_list_meta` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `engine_data_list_records`
--

DROP TABLE IF EXISTS `engine_data_list_records`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `engine_data_list_records` (
  `ID` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `DATA_LIST_ID` bigint NOT NULL COMMENT '数据列表ID',
  `DATA_RECORD` varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT '数据记录',
  `DATA_REMARK` varchar(32) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '数据备注',
  `CREATE_TIME` datetime NOT NULL,
  `UPDATE_TIME` datetime NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=30910 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `engine_data_list_records`
--

LOCK TABLES `engine_data_list_records` WRITE;
/*!40000 ALTER TABLE `engine_data_list_records` DISABLE KEYS */;
INSERT INTO `engine_data_list_records` VALUES (30387,23,'114.114.114.114',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30391,25,'114.114.114.114',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30393,29,'13800000000',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30395,31,'13800000000',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30397,33,'13800000000',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30401,1,'apple',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30403,1,'Xiaomi',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30405,1,'HUAWEI',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30407,1,'OPPO',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30409,1,'samsung',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30411,1,'vivo',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30413,1,'LENOVO',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30415,1,'Meizu',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30417,1,'YuLong',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30419,1,'Coolpad',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30421,1,'ZTE',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30423,1,'GiONEE',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30425,1,'BBK',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30427,1,'alps',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30429,1,'Hisense',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30431,1,'Letv',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30433,1,'DOOV',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30435,1,'TCL',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30437,1,'LeMobile',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30439,1,'HTC',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30441,1,'K-Touch',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30443,1,'CMDC',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30445,1,'YUSUN',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30447,1,'BIRD',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30449,1,'nubia',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30453,1,'Sony',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30455,1,'360',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30457,1,'Haier',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30459,1,'LTTY',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30461,1,'koobee',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30463,1,'motorola',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30465,1,'lephone',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30467,1,'QiKU',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30469,1,'asus',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30471,1,'4G',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30473,1,'LG',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30475,1,'OUKI',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30477,1,'Spreadtrum',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30479,1,'M5',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30481,1,'smartisan',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30483,1,'Qmi',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30485,1,'LGE',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30487,1,'Wiselinksz',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30489,1,'Q9',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30491,1,'Mikee',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30493,1,'C906',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30495,1,'HOSIN',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30497,1,'G4',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30499,1,'Xianmi',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30501,1,'meetuu',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30503,1,'DESAY',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30505,1,'SBYH',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30507,1,'Poulo',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30509,1,'Android TD',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30511,1,'xiaolajiao',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30513,1,'KOPO',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30515,1,'ONEPLUS',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30517,1,'bignox',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30519,1,'konka',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30521,1,'honor',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30523,1,'SPRD',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30525,1,'Changhong',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30527,1,'PHICOMM',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30529,1,'Philips',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30531,1,'BOWAY',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30533,1,'unknown',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30535,1,'Meitu',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30537,1,'ETON',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30539,1,'ZUK',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30541,1,'VOTO',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30543,1,'android',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30545,1,'bigsamsung',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30547,1,'AUX',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30549,1,'NOAIN',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30551,1,'GuangXin',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30553,1,'JTY',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30555,1,'bifer',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30559,1,'JinXing',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30561,1,'UTOUU',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30563,1,'Lovme',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30567,1,'HUASEN',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30569,1,'XXAndroid',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30571,1,'UOOGOU',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30573,1,'feixun',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30575,1,'MI',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30577,1,'CUBE',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30579,1,'iPhone',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30581,1,'Teclast',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30583,1,'Hasee',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30585,1,'SANMEI',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30587,1,'EOOM',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30589,1,'XMSD',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30591,1,'Nokia',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30593,1,'Genymotion',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30595,1,'WellPhone',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30597,1,'aloes',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30599,1,'InFocus',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30601,1,'Sony Ericsson',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30605,1,'TCT',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30607,1,'BESTSONNY',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30609,1,'Pioneer',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30611,1,'skyhon',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30613,1,'NFT',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30615,1,'IUNI',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30617,1,'PANTECH',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30621,1,'Qingcheng',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30623,1,'SUGAR',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30625,1,'ASUSTek Computer Inc',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30627,1,'GREE',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30629,1,'Foxconn International Holdings Limited',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30631,1,'rockchip',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30633,1,'Newman',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30635,1,'x-apple',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30637,1,'AMOI',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30639,1,'ramos',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30641,1,'Volte',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30643,1,'Baidu',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30645,1,'HT',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30647,1,'ZEEMI',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30649,1,'OPSSON',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30651,1,'RY',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30653,1,'freeme',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30655,1,'PPTV',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30657,1,'softwinner',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30659,1,'EBEST',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30661,1,'4G+',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30665,1,'LIMI',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30667,1,'SOP',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30669,1,'SHOWN_P1',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30671,1,'BAIMI',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30675,1,'Comio',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30677,1,'onda',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30679,1,'AoleDior',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30681,1,'OBEE',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30683,1,'WaterWorld',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30685,1,'SAGA',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30687,1,'KDOOR',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30689,1,'SHARP',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30691,1,'OWWO',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30693,1,'Hasee X50 TS',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30697,1,'HUIZUU',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30699,1,'JXD',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30701,1,'BL',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30703,1,'GM',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30707,1,'NUOFEI',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30709,1,'TianYu',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30711,1,'XM-T',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30713,1,'F8909',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30715,1,'4G-VOLTE',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30717,1,'YEPEN',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30719,1,'HY',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30721,1,'IPH',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30723,1,'T1',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30725,1,'MYTEL',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30727,1,'HEEYU',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30729,1,'DAQ',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30731,1,'YOORD',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30733,1,'MLLED',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30735,1,'hipad',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30737,1,'WEIBI',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30739,1,'MTK6592',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30741,1,'Allwinner',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30743,1,'XBS',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30745,1,'MediaTek',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30747,1,'ZWX',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30749,1,'QIKAI',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30751,1,'TETC',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30753,1,'Intel',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30755,1,'Blephone',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30757,1,'XMi',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30759,1,'ThL',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30761,1,'RLT',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30763,1,'MT6595',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30765,1,'XYS',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30767,1,'Acer',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30769,1,'YOUYU',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30771,1,'READBOY',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30773,1,'BROR',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30775,1,'bicix',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30777,1,'ZHUOMI',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30779,1,'ERENEBEN',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30781,1,'weiimi',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30783,1,'OPSSON_V6',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30785,1,'iPhone6 Plus',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30787,1,'GUOMI',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30789,1,'XLJ',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30791,1,'ares',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30793,1,'laaboo',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30795,1,'HYF-A1',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30797,1,'iphone 6s',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30799,1,'KANGJIA',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30801,1,'Xshitou',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30803,1,'BOBI',NULL,'2016-11-25 10:55:52','2016-11-25 10:55:52'),(30805,35,'apple',NULL,'2016-11-25 17:32:19','2016-11-25 17:32:19'),(30807,35,'Xiaomi',NULL,'2016-11-25 17:32:27','2016-11-25 17:32:27'),(30809,35,'HUAWEI',NULL,'2016-11-25 17:32:37','2016-11-25 17:32:37'),(30811,35,'OPPO',NULL,'2016-11-25 17:32:44','2016-11-25 17:32:44'),(30813,35,'samsung',NULL,'2016-11-25 17:32:52','2016-11-25 17:32:52'),(30815,35,'vivo',NULL,'2016-11-25 17:33:00','2016-11-25 17:33:00'),(30817,35,'LENOVO',NULL,'2016-11-25 17:33:10','2016-11-25 17:33:10'),(30819,35,'Meizu',NULL,'2016-11-25 17:33:18','2016-11-25 17:33:18'),(30821,35,'ZTE',NULL,'2016-11-25 17:33:27','2016-11-25 17:33:27'),(30823,35,'HTC',NULL,'2016-11-25 17:33:38','2016-11-25 17:33:38'),(30825,35,'iPhone',NULL,'2016-11-25 17:34:19','2016-11-25 17:34:19'),(30827,35,'nubia',NULL,'2016-11-25 17:34:46','2016-11-25 17:34:46'),(30829,35,'BBK',NULL,'2016-11-25 17:35:24','2016-11-25 17:35:24'),(30831,35,'LG',NULL,'2016-11-25 17:36:19','2016-11-25 17:36:19'),(30908,84,'13800000000',NULL,'2019-09-10 11:16:12','2019-09-10 11:16:12'),(30909,85,'13800000000',NULL,'2019-09-11 18:05:35','2019-09-11 18:05:35');
/*!40000 ALTER TABLE `engine_data_list_records` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `engine_data_lists`
--

DROP TABLE IF EXISTS `engine_data_lists`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `engine_data_lists` (
  `ID` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `NAME` varchar(32) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT '列表名',
  `LABEL` varchar(32) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT '列表名中文描叙',
  `MODEL_ID` bigint NOT NULL COMMENT '模型ID',
  `COMMENT` varchar(128) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '注释',
  `LIST_TYPE` varchar(11) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL DEFAULT 'BLACK' COMMENT '列表类型',
  `STATUS` int NOT NULL COMMENT '状态',
  `CREATE_TIME` datetime NOT NULL,
  `UPDATE_TIME` datetime NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=86 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `engine_data_lists`
--

LOCK TABLES `engine_data_lists` WRITE;
/*!40000 ALTER TABLE `engine_data_lists` DISABLE KEYS */;
INSERT INTO `engine_data_lists` VALUES (1,'radar_brand_black_List','[系统]手机品牌黑名单',1,'','BLACK',1,'2016-08-10 16:02:29','2016-08-10 16:02:31'),(3,'radar_mobile_black_list','[系统]手机号码黑名单',1,'','BLACK',1,'2016-08-24 18:37:52','2016-08-24 18:37:55'),(5,'radar_mobile_white_list','[系统]手机号码白名单',1,'','WHITE',1,'2016-08-24 18:38:55','2016-08-24 18:38:58'),(7,'radar_ip_black_list','[系统]IP黑名单',1,'','BLACK',1,'2016-08-25 11:22:12','2016-08-25 11:22:16'),(9,'radar_ip_white_list','[系统]IP白名单',1,'','WHITE',1,'2016-09-18 10:27:25','2016-09-18 10:27:25'),(10,'mobile_brand_white_list','[系统]手机品牌白名单',1,'','WHITE',1,'2016-11-23 16:07:31','2016-11-23 16:07:31'),(29,'mobile_white_list','手机号码白名单',101,'','WHITE',1,'2016-11-21 14:07:29','2016-11-21 14:07:29'),(31,'mobile_white_list','手机号码白名单',103,'','WHITE',1,'2016-11-21 14:09:57','2016-11-21 14:09:57'),(33,'mobile_white_list','手机号码白名单',99,'','WHITE',1,'2016-11-21 14:11:37','2016-11-21 14:11:37'),(84,'mobile_white_list','手机号码白名单',222,'','WHITE',1,'2019-09-10 11:16:12','2019-09-10 11:16:12'),(85,'mobile_white_list','手机号码白名单',224,'','WHITE',1,'2019-09-11 18:05:35','2019-09-11 18:05:35');
/*!40000 ALTER TABLE `engine_data_lists` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `engine_field`
--

DROP TABLE IF EXISTS `engine_field`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `engine_field` (
  `ID` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `MODEL_ID` bigint NOT NULL COMMENT 'MODEL ID',
  `FIELD_NAME` varchar(32) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT '事件信息中的包含的字段',
  `LABEL` varchar(32) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  `FIELD_TYPE` varchar(16) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT '字段类型',
  `VALIDATE_TYPE` varchar(15) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '校验类型',
  `VALIDATE_ARGS` varchar(15) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '校验参数',
  `INDEXED` tinyint(1) DEFAULT '0' COMMENT '1 标识索引字段',
  `CREATE_TIME` datetime NOT NULL,
  `UPDATE_TIME` datetime NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=705 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `engine_field`
--

LOCK TABLES `engine_field` WRITE;
/*!40000 ALTER TABLE `engine_field` DISABLE KEYS */;
INSERT INTO `engine_field` VALUES (179,99,'loginTime','登录时间','LONG',NULL,NULL,1,'2016-11-17 11:06:17','2016-11-17 11:06:17'),(181,99,'userId','用户ID','STRING',NULL,NULL,1,'2016-11-17 11:06:17','2016-11-17 11:06:17'),(183,99,'eventId','登录事件流水号','STRING',NULL,NULL,1,'2016-11-17 11:06:17','2016-11-17 15:55:08'),(185,99,'mobile','用户手机号','STRING',NULL,NULL,0,'2016-11-17 11:07:38','2016-11-17 11:07:38'),(187,99,'userIP','用户IP','STRING',NULL,NULL,0,'2016-11-17 11:08:36','2016-11-17 11:08:36'),(189,99,'deviceId','设备ID','STRING',NULL,NULL,0,'2016-11-17 11:10:12','2016-11-17 11:10:12'),(191,99,'os','操作系统','STRING',NULL,NULL,0,'2016-11-17 11:17:00','2016-11-17 11:17:00'),(193,99,'passwd','登录密码密文','STRING',NULL,NULL,0,'2016-11-17 11:18:28','2016-11-17 11:18:28'),(195,99,'channel','来源渠道','STRING',NULL,NULL,0,'2016-11-17 11:22:18','2016-11-17 11:22:18'),(203,101,'eventId','注册事件流水号','STRING',NULL,NULL,1,'2016-11-17 15:54:48','2016-11-17 15:57:39'),(205,101,'registerTime','注册时间','LONG',NULL,NULL,1,'2016-11-17 15:54:48','2016-11-17 15:54:48'),(207,101,'userId','用户ID','STRING',NULL,NULL,1,'2016-11-17 15:54:48','2016-11-17 15:54:48'),(209,101,'mobile','用户手机号','STRING',NULL,NULL,0,'2016-11-17 15:58:04','2016-11-17 15:58:04'),(211,101,'userIP','用户IP','STRING',NULL,NULL,0,'2016-11-17 15:58:28','2016-11-17 15:58:28'),(213,101,'deviceId','设备ID','STRING',NULL,NULL,0,'2016-11-17 15:59:09','2016-11-17 15:59:09'),(215,101,'os','操作系统','STRING',NULL,NULL,0,'2016-11-17 15:59:45','2016-11-17 15:59:45'),(217,101,'channel','来源渠道','STRING',NULL,NULL,0,'2016-11-17 16:00:11','2016-11-17 16:00:11'),(219,101,'passwd','密码密文','STRING',NULL,NULL,0,'2016-11-17 16:00:50','2016-11-17 16:00:50'),(221,103,'eventId','交易流水号','STRING',NULL,NULL,1,'2016-11-17 16:10:02','2016-11-17 16:47:49'),(223,103,'eventTime','交易发生时间','LONG',NULL,NULL,1,'2016-11-17 16:10:02','2016-11-17 17:00:10'),(225,103,'userId','用户ID','STRING',NULL,NULL,1,'2016-11-17 16:10:02','2016-11-17 16:10:02'),(227,103,'mobile','用户手机号','STRING',NULL,NULL,0,'2016-11-17 16:48:11','2016-11-17 16:48:11'),(229,103,'userIP','用户IP','STRING',NULL,NULL,0,'2016-11-17 16:59:41','2016-11-17 16:59:41'),(231,103,'deviceId','设备ID','STRING',NULL,NULL,0,'2016-11-17 17:03:32','2016-11-17 17:03:32'),(233,103,'os','操作系统','STRING',NULL,NULL,0,'2016-11-17 17:07:59','2016-11-17 17:07:59'),(235,103,'channel','来源渠道','STRING',NULL,NULL,0,'2016-11-17 17:08:32','2016-11-17 17:08:32'),(237,103,'amount','交易金额','DOUBLE',NULL,NULL,0,'2016-11-17 17:09:26','2016-11-17 17:09:26'),(684,222,'eventId','注册事件流水号','STRING',NULL,NULL,1,'2019-09-10 11:16:12','2019-09-10 11:16:12'),(685,222,'registerTime','注册时间','LONG',NULL,NULL,1,'2019-09-10 11:16:12','2019-09-10 11:16:12'),(686,222,'userId','用户ID','STRING',NULL,NULL,1,'2019-09-10 11:16:12','2019-09-10 11:16:12'),(687,222,'mobile','用户手机号','STRING',NULL,NULL,0,'2019-09-10 11:16:12','2019-09-10 11:16:12'),(688,222,'userIP','用户IP','STRING',NULL,NULL,0,'2019-09-10 11:16:12','2019-09-10 11:16:12'),(689,222,'deviceId','设备ID','STRING',NULL,NULL,0,'2019-09-10 11:16:12','2019-09-10 11:16:12'),(690,222,'os','操作系统','STRING',NULL,NULL,0,'2019-09-10 11:16:12','2019-09-10 11:16:12'),(691,222,'channel','来源渠道','STRING',NULL,NULL,0,'2019-09-10 11:16:12','2019-09-10 11:16:12'),(692,222,'passwd','密码密文','STRING',NULL,NULL,0,'2019-09-10 11:16:12','2019-09-10 11:16:12'),(696,224,'eventId','交易流水号','STRING',NULL,NULL,1,'2019-09-11 18:05:35','2019-09-11 18:05:35'),(697,224,'eventTime','交易发生时间','LONG',NULL,NULL,1,'2019-09-11 18:05:35','2019-09-11 18:05:35'),(698,224,'userId','用户ID','STRING',NULL,NULL,1,'2019-09-11 18:05:35','2019-09-11 18:05:35'),(699,224,'mobile','用户手机号','STRING',NULL,NULL,0,'2019-09-11 18:05:35','2019-09-11 18:05:35'),(700,224,'userIP','用户IP','STRING',NULL,NULL,0,'2019-09-11 18:05:35','2019-09-11 18:05:35'),(701,224,'deviceId','设备ID','STRING',NULL,NULL,0,'2019-09-11 18:05:35','2019-09-11 18:05:35'),(702,224,'os','操作系统','STRING',NULL,NULL,0,'2019-09-11 18:05:35','2019-09-11 18:05:35'),(703,224,'channel','来源渠道','STRING',NULL,NULL,0,'2019-09-11 18:05:35','2019-09-11 18:05:35'),(704,224,'amount','交易金额','DOUBLE',NULL,NULL,0,'2019-09-11 18:05:35','2019-09-11 18:05:35');
/*!40000 ALTER TABLE `engine_field` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `engine_model`
--

DROP TABLE IF EXISTS `engine_model`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `engine_model` (
  `ID` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `GUID` varchar(64) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  `MODEL_NAME` varchar(30) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT '模型名称',
  `LABEL` varchar(32) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  `ENTITY_NAME` varchar(30) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT '事件中标识实体的主键',
  `ENTRY_NAME` varchar(30) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT '事件主键',
  `REFERENCE_DATE` varchar(30) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT '事件时间',
  `FIELD_VALIDATE` tinyint(1) NOT NULL DEFAULT '0',
  `CODE` varchar(60) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  `TEMPLATE` tinyint(1) NOT NULL DEFAULT '0' COMMENT '1 标识模板',
  `DASHBOARD_URL` varchar(300) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL,
  `STATUS` int NOT NULL COMMENT '状态',
  `CREATE_TIME` datetime NOT NULL,
  `UPDATE_TIME` datetime NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=225 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `engine_model`
--

LOCK TABLES `engine_model` WRITE;
/*!40000 ALTER TABLE `engine_model` DISABLE KEYS */;
INSERT INTO `engine_model` VALUES (1,'00000000-0000-0000-0000-000000000001','system','system','userId','eventId','loginTime',0,'pgmmer.top',1,NULL,0,'2016-11-17 10:59:43','2016-11-18 18:02:15'),(99,'697AA0D6-4B0F-4B57-A487-345CB10BDD8A','login_template','登录行为模板','userId','eventId','loginTime',0,'pgmmer.top',1,NULL,0,'2016-11-17 10:59:43','2016-11-18 18:02:15'),(101,'3EAE4543-814B-4AAB-9C96-F3172D2C9B16','register_template','注册行为模板','userId','eventId','registerTime',0,'pgmmer.top',1,NULL,0,'2016-11-17 15:50:49','2016-11-18 18:02:22'),(103,'03355AB9-4396-4740-8CA2-4E6643CDC0F1','transaction_template','交易行为模板','userId','eventId','eventTime',0,'pgmmer.top',1,NULL,0,'2016-11-17 16:06:31','2016-11-17 16:10:02'),(222,'B6B69670-2E27-4363-8D7D-B6CBC73CEB60','model_222','Ting注册','userId','eventId','registerTime',0,'pgmmer.top',0,NULL,0,'2016-11-17 15:50:49','2019-09-11 13:42:48'),(224,'DB8A078F-97FE-4A7F-AAC0-5AF1A6C36CE8','model_224','Ting提现','userId','eventId','eventTime',0,'pgmmer.top',0,NULL,0,'2016-11-17 16:06:31','2019-09-11 18:07:23');
/*!40000 ALTER TABLE `engine_model` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `engine_model_conf`
--

DROP TABLE IF EXISTS `engine_model_conf`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `engine_model_conf` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `model_id` bigint DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `path` varchar(255) DEFAULT NULL,
  `tag` varchar(255) DEFAULT NULL,
  `operation` varchar(255) DEFAULT NULL,
  `update_date` datetime DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `comment` varchar(128) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `engine_model_conf`
--

LOCK TABLES `engine_model_conf` WRITE;
/*!40000 ALTER TABLE `engine_model_conf` DISABLE KEYS */;
/*!40000 ALTER TABLE `engine_model_conf` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `engine_model_conf_param`
--

DROP TABLE IF EXISTS `engine_model_conf_param`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `engine_model_conf_param` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `mold_id` bigint DEFAULT NULL,
  `feed` varchar(255) DEFAULT NULL,
  `expressions` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `engine_model_conf_param`
--

LOCK TABLES `engine_model_conf_param` WRITE;
/*!40000 ALTER TABLE `engine_model_conf_param` DISABLE KEYS */;
/*!40000 ALTER TABLE `engine_model_conf_param` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `engine_pre_item`
--

DROP TABLE IF EXISTS `engine_pre_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `engine_pre_item` (
  `ID` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `MODEL_ID` bigint NOT NULL COMMENT '模型ID',
  `SOURCE_FIELD` varchar(32) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT '来源字段',
  `SOURCE_LABEL` varchar(32) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  `DEST_FIELD` varchar(32) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT '目标字段',
  `LABEL` varchar(32) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  `ARGS` varchar(250) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT '' COMMENT '参数',
  `PLUGIN` varchar(32) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT '转换插件',
  `CONFIG_JSON` varchar(250) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '响应字段配置信息',
  `REQ_TYPE` varchar(16) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '请求方式',
  `STATUS` int NOT NULL,
  `CREATE_TIME` datetime NOT NULL,
  `UPDATE_TIME` datetime NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=175 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `engine_pre_item`
--

LOCK TABLES `engine_pre_item` WRITE;
/*!40000 ALTER TABLE `engine_pre_item` DISABLE KEYS */;
INSERT INTO `engine_pre_item` VALUES (43,99,'userIP','用户IP','ipLocationPre','IP归属地','','IP2LOCATION',NULL,NULL,1,'2016-11-17 11:23:45','2016-11-17 11:26:21'),(45,99,'mobile','用户手机','mobileLocationPre','手机号码归属地','','MOBILE2LOCATION',NULL,NULL,1,'2016-11-17 11:27:24','2016-11-17 11:27:24'),(47,99,'mobile','用户手机','mobile7bPre','手机号码段','0,7','SUBSTRING',NULL,NULL,1,'2016-11-17 11:37:33','2016-11-17 11:37:33'),(49,101,'userIP','用户IP','ipLocationPre','IP归属地','','IP2LOCATION',NULL,NULL,1,'2016-11-17 17:11:05','2016-11-17 17:11:05'),(51,101,'mobile','用户手机','mobileLocationPre','手机号码归属地','','MOBILE2LOCATION',NULL,NULL,1,'2016-11-17 17:12:22','2016-11-17 17:12:22'),(53,101,'mobile','用户手机','mobile7bPre','手机号码段','0,7','SUBSTRING',NULL,NULL,1,'2016-11-17 17:13:10','2016-11-17 17:13:10'),(55,103,'userIP','用户IP','ipLocationPre','IP归属地','','IP2LOCATION',NULL,NULL,1,'2016-11-17 17:14:25','2016-11-17 17:14:25'),(57,103,'mobile','用户手机','mobileLocationPre','手机号码归属地','','MOBILE2LOCATION',NULL,NULL,1,'2016-11-17 17:14:50','2016-11-17 17:14:50'),(59,103,'mobile','用户手机','mobile7bPre','手机号码段','0,7','SUBSTRING',NULL,NULL,1,'2016-11-17 17:15:34','2016-11-17 17:15:34'),(168,222,'userIP','用户IP','ipLocationPre','IP归属地','','IP2LOCATION',NULL,NULL,1,'2019-09-10 11:16:12','2019-09-10 11:16:12'),(169,222,'mobile','用户手机','mobileLocationPre','手机号码归属地','','MOBILE2LOCATION',NULL,NULL,1,'2019-09-10 11:16:12','2019-09-10 11:16:12'),(170,222,'mobile','用户手机','mobile7bPre','手机号码段','0,7','SUBSTRING',NULL,NULL,1,'2019-09-10 11:16:12','2019-09-10 11:16:12'),(171,222,'registerTime','注册时间','preItem_171','小时','','SENSITIVE_TIME',NULL,NULL,1,'2019-09-10 14:52:56','2019-09-10 14:52:56'),(172,224,'userIP','用户IP','ipLocationPre','IP归属地','','IP2LOCATION',NULL,NULL,1,'2019-09-11 18:05:35','2019-09-11 18:05:35'),(173,224,'mobile','用户手机','mobileLocationPre','手机号码归属地','','MOBILE2LOCATION',NULL,NULL,1,'2019-09-11 18:05:35','2019-09-11 18:05:35'),(174,224,'mobile','用户手机','mobile7bPre','手机号码段','0,7','SUBSTRING',NULL,NULL,1,'2019-09-11 18:05:35','2019-09-11 18:05:35');
/*!40000 ALTER TABLE `engine_pre_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `engine_rule`
--

DROP TABLE IF EXISTS `engine_rule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `engine_rule` (
  `ID` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `MODEL_ID` bigint NOT NULL COMMENT '模型ID',
  `STRATEGY_ID` bigint NOT NULL COMMENT '所属策略ID',
  `NAME` varchar(64) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  `LABEL` varchar(64) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL DEFAULT '' COMMENT '规则名称',
  `SCRIPTS` varchar(2048) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT '检验脚本',
  `INIT_SCORE` int NOT NULL DEFAULT '0' COMMENT '初始分数',
  `BASE_NUM` int NOT NULL DEFAULT '0' COMMENT '基数',
  `OPERATOR` varchar(32) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT '运算符',
  `ABSTRACTION_NAME` varchar(64) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT '抽象名称',
  `RATE` int NOT NULL DEFAULT '100' COMMENT '比例',
  `MAX` int NOT NULL DEFAULT '0' COMMENT '最大得分值',
  `STATUS` int NOT NULL COMMENT '状态',
  `RULE_DEFINITION` varchar(2048) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL,
  `CREATE_TIME` datetime NOT NULL,
  `UPDATE_TIME` datetime NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=388 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `engine_rule`
--

LOCK TABLES `engine_rule` WRITE;
/*!40000 ALTER TABLE `engine_rule` DISABLE KEYS */;
INSERT INTO `engine_rule` VALUES (85,103,37,'rule_85','1天内IP交易次数大于30','class StrategyCheckScript {\n  public boolean check(def data, def lists) {    if (data.abstractions.tran_ip_1_day_qty>=30)\n        return true;\n    else\n        return false;\n}}',20,1,'MUL','abstractions.tran_ip_1_day_qty',100,0,1,'{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"abstractions.tran_ip_1_day_qty\",\"type\":\"DOUBLE\",\"class\":\"ENTATTR\"},{\"type\":\"DOUBLE\",\"class\":\"CONST\",\"value\":\"30\"}],\"enabled\":true,\"operator\":\"Greater_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}','2016-11-18 11:41:21','2016-11-18 11:41:21'),(87,103,37,'rule_87','1小时内IP交易次数大于15','class StrategyCheckScript {\n  public boolean check(def data, def lists) {    if (data.abstractions.tran_ip_1_hour_qty>=15)\n        return true;\n    else\n        return false;\n}}',20,1,'MUL','abstractions.tran_ip_1_hour_qty',100,0,1,'{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"abstractions.tran_ip_1_hour_qty\",\"type\":\"DOUBLE\",\"class\":\"ENTATTR\"},{\"type\":\"DOUBLE\",\"class\":\"CONST\",\"value\":\"15\"}],\"enabled\":true,\"operator\":\"Greater_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}','2016-11-18 11:43:14','2016-11-18 11:43:14'),(89,103,37,'rule_89','1天内IP交易金额大于1000','class StrategyCheckScript {\n  public boolean check(def data, def lists) {    if (data.abstractions.tran_ip_1_day_amt>=1000)\n        return true;\n    else\n        return false;\n}}',20,1,'MUL','abstractions.tran_ip_1_day_amt',10,0,1,'{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"abstractions.tran_ip_1_day_amt\",\"type\":\"DOUBLE\",\"class\":\"ENTATTR\"},{\"type\":\"DOUBLE\",\"class\":\"CONST\",\"value\":\"1000\"}],\"enabled\":true,\"operator\":\"Greater_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}','2016-11-18 11:46:11','2016-11-18 11:46:11'),(91,103,37,'rule_91','1天内设备交易次数大于30','class StrategyCheckScript {\n  public boolean check(def data, def lists) {    if (data.abstractions.tran_did_1_day_qty>=30)\n        return true;\n    else\n        return false;\n}}',20,1,'ADD','abstractions.tran_did_1_day_qty',100,0,1,'{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"abstractions.tran_did_1_day_qty\",\"type\":\"DOUBLE\",\"class\":\"ENTATTR\"},{\"type\":\"DOUBLE\",\"class\":\"CONST\",\"value\":\"30\"}],\"enabled\":true,\"operator\":\"Greater_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}','2016-11-18 11:47:24','2016-11-18 11:47:24'),(93,103,37,'rule_93','1天内IP关联设备数大于10','class StrategyCheckScript {\n  public boolean check(def data, def lists) {    if (data.abstractions.tran_did_ip_1_day_qty>=10)\n        return true;\n    else\n        return false;\n}}',20,1,'MUL','abstractions.tran_did_ip_1_day_qty',100,0,1,'{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"abstractions.tran_did_ip_1_day_qty\",\"type\":\"DOUBLE\",\"class\":\"ENTATTR\"},{\"type\":\"DOUBLE\",\"class\":\"CONST\",\"value\":\"10\"}],\"enabled\":true,\"operator\":\"Greater_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}','2016-11-18 14:03:33','2016-11-18 14:03:33'),(95,103,37,'rule_95','1天内IP关联用户数大于10','class StrategyCheckScript {\n  public boolean check(def data, def lists) {    if (data.abstractions.tran_uid_ip_1_day_qty>=10)\n        return true;\n    else\n        return false;\n}}',20,1,'MUL','abstractions.tran_uid_ip_1_day_qty',100,0,1,'{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"abstractions.tran_uid_ip_1_day_qty\",\"type\":\"DOUBLE\",\"class\":\"ENTATTR\"},{\"type\":\"DOUBLE\",\"class\":\"CONST\",\"value\":\"10\"}],\"enabled\":true,\"operator\":\"Greater_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}','2016-11-18 14:04:40','2016-11-18 14:04:40'),(97,103,37,'rule_97','手机归属地和IP归属地城市不一致','class StrategyCheckScript {\n  public boolean check(def data, def lists) {    if ((data.preItems.mobileLocationPre.city!=data.preItems.ipLocationPre.city))\n        return true;\n    else\n        return false;\n}}',20,0,'NONE','',100,0,1,'{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"preItems.mobileLocationPre.city\",\"type\":\"STRING\",\"class\":\"ENTATTR\"},{\"column\":\"preItems.ipLocationPre.city\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"Field_Not_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}','2016-11-18 14:05:39','2019-09-11 17:42:52'),(99,103,37,'rule_99','IP归属地不在中国','class StrategyCheckScript {\n  public boolean check(def data, def lists) {    if ((data.preItems.ipLocationPre.country!=\'中国\'))\n        return true;\n    else\n        return false;\n}}',20,0,'NONE','',100,0,1,'{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"preItems.ipLocationPre.country\",\"type\":\"STRING\",\"class\":\"ENTATTR\"},{\"type\":\"STRING\",\"class\":\"CONST\",\"value\":\"中国\"}],\"enabled\":true,\"operator\":\"NotEqual\"}],\"class\":\"PDCT\",\"enabled\":true}','2016-11-18 14:06:43','2019-09-11 17:42:57'),(101,99,39,'rule_101','1天内IP关联用户数大于10','class StrategyCheckScript {\n  public boolean check(def data, def lists) {    if (data.abstractions.log_uid_ip_1_day_qty>=10)\n        return true;\n    else\n        return false;\n}}',20,1,'MUL','abstractions.log_uid_ip_1_day_qty',100,0,1,'{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"abstractions.log_uid_ip_1_day_qty\",\"type\":\"DOUBLE\",\"class\":\"ENTATTR\"},{\"type\":\"DOUBLE\",\"class\":\"CONST\",\"value\":\"10\"}],\"enabled\":true,\"operator\":\"Greater_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}','2016-11-18 14:43:03','2016-11-18 14:43:03'),(103,99,39,'rule_103','1天内IP关联设备数大于10','class StrategyCheckScript {\n  public boolean check(def data, def lists) {    if (data.abstractions.log_did_ip_1_hour_qty>=10)\n        return true;\n    else\n        return false;\n}}',20,1,'MUL','abstractions.log_did_ip_1_day_qty',100,0,1,'{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"abstractions.log_did_ip_1_hour_qty\",\"type\":\"DOUBLE\",\"class\":\"ENTATTR\"},{\"type\":\"DOUBLE\",\"class\":\"CONST\",\"value\":\"10\"}],\"enabled\":true,\"operator\":\"Greater_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}','2016-11-18 14:45:57','2016-11-18 14:45:57'),(105,99,39,'rule_105','10分钟内IP登录次数大于10','class StrategyCheckScript {\n  public boolean check(def data, def lists) {    if (data.abstractions.log_ip_10_min_qty>=10)\n        return true;\n    else\n        return false;\n}}',20,1,'MUL','abstractions.log_ip_10_min_qty',100,0,1,'{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"abstractions.log_ip_10_min_qty\",\"type\":\"DOUBLE\",\"class\":\"ENTATTR\"},{\"type\":\"DOUBLE\",\"class\":\"CONST\",\"value\":\"10\"}],\"enabled\":true,\"operator\":\"Greater_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}','2016-11-18 14:47:12','2016-11-18 14:47:12'),(107,99,39,'rule_107','IP登录位置显示国外','class StrategyCheckScript {\n  public boolean check(def data, def lists) {    if ((data.preItems.ipLocationPre.country!=\'中国\'))\n        return true;\n    else\n        return false;\n}}',20,0,'NONE','',100,0,1,'{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"preItems.ipLocationPre.country\",\"type\":\"STRING\",\"class\":\"ENTATTR\"},{\"type\":\"STRING\",\"class\":\"CONST\",\"value\":\"中国\"}],\"enabled\":true,\"operator\":\"NotEqual\"}],\"class\":\"PDCT\",\"enabled\":true}','2016-11-18 14:48:19','2019-09-11 17:40:42'),(109,101,41,'rule_109','1天内IP关联注册数大于20','class StrategyCheckScript {\n  public boolean check(def data, def lists) {    if (data.abstractions.reg_ip_1_day_qty>=20)\n        return true;\n    else\n        return false;\n}}',20,1,'MUL','abstractions.reg_ip_1_day_qty',100,0,1,'{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"abstractions.reg_ip_1_day_qty\",\"type\":\"DOUBLE\",\"class\":\"ENTATTR\"},{\"type\":\"DOUBLE\",\"class\":\"CONST\",\"value\":\"20\"}],\"enabled\":true,\"operator\":\"Greater_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}','2016-11-18 16:00:55','2016-11-18 16:00:55'),(111,101,41,'rule_111','1天内设备关联注册数大于20','class StrategyCheckScript {\n  public boolean check(def data, def lists) {    if (data.abstractions.reg_did_1_day_qty>=20)\n        return true;\n    else\n        return false;\n}}',20,1,'MUL','abstractions.reg_did_1_day_qty',100,0,1,'{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"abstractions.reg_did_1_day_qty\",\"type\":\"DOUBLE\",\"class\":\"ENTATTR\"},{\"type\":\"DOUBLE\",\"class\":\"CONST\",\"value\":\"20\"}],\"enabled\":true,\"operator\":\"Greater_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}','2016-11-18 16:01:44','2016-11-18 16:01:44'),(113,101,41,'rule_113','10分钟内IP关联注册数大于15','class StrategyCheckScript {\n  public boolean check(def data, def lists) {    if (data.abstractions.reg_ip_10_min_qty>=15)\n        return true;\n    else\n        return false;\n}}',30,1,'MUL','abstractions.reg_ip_10_min_qty',100,0,1,'{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"abstractions.reg_ip_10_min_qty\",\"type\":\"DOUBLE\",\"class\":\"ENTATTR\"},{\"type\":\"DOUBLE\",\"class\":\"CONST\",\"value\":\"15\"}],\"enabled\":true,\"operator\":\"Greater_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}','2016-11-18 16:02:57','2016-11-18 16:02:57'),(115,101,41,'rule_115','1天内设备关联用户数大于10','class StrategyCheckScript {\n  public boolean check(def data, def lists) {    if (data.abstractions.reg_uid_did_1_day_qty>=10)\n        return true;\n    else\n        return false;\n}}',30,1,'MUL','abstractions.reg_uid_did_1_day_qty',100,0,1,'{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"abstractions.reg_uid_did_1_day_qty\",\"type\":\"DOUBLE\",\"class\":\"ENTATTR\"},{\"type\":\"DOUBLE\",\"class\":\"CONST\",\"value\":\"10\"}],\"enabled\":true,\"operator\":\"Greater_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}','2016-11-18 16:04:13','2016-11-18 16:04:13'),(117,101,41,'rule_117','IP归属地城市与手机号码归属地城市不一致','class StrategyCheckScript {\n  public boolean check(def data, def lists) {    if ((data.preItems.ipLocationPre.city!=data.preItems.mobileLocationPre.city))\n        return true;\n    else\n        return false;\n}}',20,0,'NONE','',100,0,1,'{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"preItems.ipLocationPre.city\",\"type\":\"STRING\",\"class\":\"ENTATTR\"},{\"column\":\"preItems.mobileLocationPre.city\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"Field_Not_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}','2016-11-18 16:05:29','2019-09-11 17:41:16'),(119,101,41,'rule_119','注册IP来源于国外','class StrategyCheckScript {\n  public boolean check(def data, def lists) {    if ((data.preItems.ipLocationPre.country!=\'中国\'))\n        return true;\n    else\n        return false;\n}}',30,0,'NONE','',100,0,1,'{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"preItems.ipLocationPre.country\",\"type\":\"STRING\",\"class\":\"ENTATTR\"},{\"type\":\"STRING\",\"class\":\"CONST\",\"value\":\"中国\"}],\"enabled\":true,\"operator\":\"NotEqual\"}],\"class\":\"PDCT\",\"enabled\":true}','2016-11-18 16:07:01','2019-09-11 17:41:08'),(121,99,39,'rule_121','1天内用户登录次数大于30','class StrategyCheckScript {\n  public boolean check(def data, def lists) {    if (data.abstractions.log_uid_1_day_qty>=30)\n        return true;\n    else\n        return false;\n}}',20,1,'MUL','abstractions.log_uid_1_day_qty',100,0,1,'{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"abstractions.log_uid_1_day_qty\",\"type\":\"DOUBLE\",\"class\":\"ENTATTR\"},{\"type\":\"DOUBLE\",\"class\":\"CONST\",\"value\":\"30\"}],\"enabled\":true,\"operator\":\"Greater_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}','2016-11-18 18:04:03','2016-11-18 18:04:24'),(127,103,37,'rule_127','命中手机号码白名单','class StrategyCheckScript {\n  public boolean check(def data, def lists) {    if (lists.mobile_white_list.containsKey(data.fields.mobile))\n        return true;\n    else\n        return false;\n}}',-200,0,'NONE','',100,0,1,'{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.mobile\",\"type\":\"STRING\",\"class\":\"ENTATTR\"},{\"type\":\"LIST\",\"class\":\"CONST\",\"value\":\"mobile_white_list\"}],\"enabled\":true,\"operator\":\"InList\"}],\"class\":\"PDCT\",\"enabled\":true}','2016-11-22 14:12:57','2016-11-22 14:13:06'),(373,222,116,'rule_109','1天内IP关联注册数大于20','class StrategyCheckScript {\n  public boolean check(def data, def lists) {    if (data.abstractions.reg_ip_1_day_qty>=20)\n        return true;\n    else\n        return false;\n}}',20,1,'MUL','abstractions.reg_ip_1_day_qty',100,0,1,'{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"abstractions.reg_ip_1_day_qty\",\"type\":\"DOUBLE\",\"class\":\"ENTATTR\"},{\"type\":\"DOUBLE\",\"class\":\"CONST\",\"value\":\"20\"}],\"enabled\":true,\"operator\":\"Greater_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}','2019-09-10 11:16:12','2019-09-10 11:16:12'),(374,222,116,'rule_111','1天内设备关联注册数大于20','class StrategyCheckScript {\n  public boolean check(def data, def lists) {    if (data.abstractions.reg_did_1_day_qty>=20)\n        return true;\n    else\n        return false;\n}}',20,1,'MUL','abstractions.reg_did_1_day_qty',100,0,1,'{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"abstractions.reg_did_1_day_qty\",\"type\":\"DOUBLE\",\"class\":\"ENTATTR\"},{\"type\":\"DOUBLE\",\"class\":\"CONST\",\"value\":\"20\"}],\"enabled\":true,\"operator\":\"Greater_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}','2019-09-10 11:16:12','2019-09-10 11:16:12'),(375,222,116,'rule_113','10分钟内IP关联注册数大于15','class StrategyCheckScript {\n  public boolean check(def data, def lists) {    if (data.abstractions.reg_ip_10_min_qty>=15)\n        return true;\n    else\n        return false;\n}}',30,1,'MUL','abstractions.reg_ip_10_min_qty',100,0,1,'{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"abstractions.reg_ip_10_min_qty\",\"type\":\"DOUBLE\",\"class\":\"ENTATTR\"},{\"type\":\"DOUBLE\",\"class\":\"CONST\",\"value\":\"15\"}],\"enabled\":true,\"operator\":\"Greater_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}','2019-09-10 11:16:12','2019-09-10 11:16:12'),(376,222,116,'rule_115','1天内设备关联用户数大于10','class StrategyCheckScript {\n  public boolean check(def data, def lists) {    if (data.abstractions.reg_uid_did_1_day_qty>=10)\n        return true;\n    else\n        return false;\n}}',30,1,'MUL','abstractions.reg_uid_did_1_day_qty',100,0,1,'{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"abstractions.reg_uid_did_1_day_qty\",\"type\":\"DOUBLE\",\"class\":\"ENTATTR\"},{\"type\":\"DOUBLE\",\"class\":\"CONST\",\"value\":\"10\"}],\"enabled\":true,\"operator\":\"Greater_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}','2019-09-10 11:16:12','2019-09-10 11:16:12'),(377,222,116,'rule_377','IP归属地城市与手机号码归属地城市不一致','class StrategyCheckScript {\n  public boolean check(def data, def lists) {    if ((data.preItems.ipLocationPre.city!=data.preItems.mobileLocationPre.city))\n        return true;\n    else\n        return false;\n}}',20,0,'NONE','',100,0,1,'{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"preItems.ipLocationPre.city\",\"type\":\"STRING\",\"class\":\"ENTATTR\"},{\"column\":\"preItems.mobileLocationPre.city\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"Field_Not_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}','2019-09-10 11:16:12','2019-09-11 17:27:20'),(378,222,116,'rule_378','注册IP来源于国外','class StrategyCheckScript {\n  public boolean check(def data, def lists) {    if ((data.preItems.ipLocationPre.country!=\'中国\'))\n        return true;\n    else\n        return false;\n}}',30,0,'NONE','',100,0,1,'{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"preItems.ipLocationPre.country\",\"type\":\"STRING\",\"class\":\"ENTATTR\"},{\"type\":\"STRING\",\"class\":\"CONST\",\"value\":\"中国\"}],\"enabled\":true,\"operator\":\"NotEqual\"}],\"class\":\"PDCT\",\"enabled\":true}','2019-09-10 11:16:12','2019-09-11 17:29:18'),(379,224,117,'rule_85','1天内IP交易次数大于30','class StrategyCheckScript {\n  public boolean check(def data, def lists) {    if (data.abstractions.tran_ip_1_day_qty>=30)\n        return true;\n    else\n        return false;\n}}',20,1,'MUL','abstractions.tran_ip_1_day_qty',100,0,1,'{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"abstractions.tran_ip_1_day_qty\",\"type\":\"DOUBLE\",\"class\":\"ENTATTR\"},{\"type\":\"DOUBLE\",\"class\":\"CONST\",\"value\":\"30\"}],\"enabled\":true,\"operator\":\"Greater_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}','2019-09-11 18:05:35','2019-09-11 18:05:35'),(380,224,117,'rule_87','1小时内IP交易次数大于15','class StrategyCheckScript {\n  public boolean check(def data, def lists) {    if (data.abstractions.tran_ip_1_hour_qty>=15)\n        return true;\n    else\n        return false;\n}}',20,1,'MUL','abstractions.tran_ip_1_hour_qty',100,0,1,'{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"abstractions.tran_ip_1_hour_qty\",\"type\":\"DOUBLE\",\"class\":\"ENTATTR\"},{\"type\":\"DOUBLE\",\"class\":\"CONST\",\"value\":\"15\"}],\"enabled\":true,\"operator\":\"Greater_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}','2019-09-11 18:05:35','2019-09-11 18:05:35'),(381,224,117,'rule_89','1天内IP交易金额大于1000','class StrategyCheckScript {\n  public boolean check(def data, def lists) {    if (data.abstractions.tran_ip_1_day_amt>=1000)\n        return true;\n    else\n        return false;\n}}',20,1,'MUL','abstractions.tran_ip_1_day_amt',10,0,1,'{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"abstractions.tran_ip_1_day_amt\",\"type\":\"DOUBLE\",\"class\":\"ENTATTR\"},{\"type\":\"DOUBLE\",\"class\":\"CONST\",\"value\":\"1000\"}],\"enabled\":true,\"operator\":\"Greater_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}','2019-09-11 18:05:35','2019-09-11 18:05:35'),(382,224,117,'rule_91','1天内设备交易次数大于30','class StrategyCheckScript {\n  public boolean check(def data, def lists) {    if (data.abstractions.tran_did_1_day_qty>=30)\n        return true;\n    else\n        return false;\n}}',20,1,'ADD','abstractions.tran_did_1_day_qty',100,0,1,'{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"abstractions.tran_did_1_day_qty\",\"type\":\"DOUBLE\",\"class\":\"ENTATTR\"},{\"type\":\"DOUBLE\",\"class\":\"CONST\",\"value\":\"30\"}],\"enabled\":true,\"operator\":\"Greater_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}','2019-09-11 18:05:35','2019-09-11 18:05:35'),(383,224,117,'rule_93','1天内IP关联设备数大于10','class StrategyCheckScript {\n  public boolean check(def data, def lists) {    if (data.abstractions.tran_did_ip_1_day_qty>=10)\n        return true;\n    else\n        return false;\n}}',20,1,'MUL','abstractions.tran_did_ip_1_day_qty',100,0,1,'{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"abstractions.tran_did_ip_1_day_qty\",\"type\":\"DOUBLE\",\"class\":\"ENTATTR\"},{\"type\":\"DOUBLE\",\"class\":\"CONST\",\"value\":\"10\"}],\"enabled\":true,\"operator\":\"Greater_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}','2019-09-11 18:05:35','2019-09-11 18:05:35'),(384,224,117,'rule_95','1天内IP关联用户数大于10','class StrategyCheckScript {\n  public boolean check(def data, def lists) {    if (data.abstractions.tran_uid_ip_1_day_qty>=10)\n        return true;\n    else\n        return false;\n}}',20,1,'MUL','abstractions.tran_uid_ip_1_day_qty',100,0,1,'{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"abstractions.tran_uid_ip_1_day_qty\",\"type\":\"DOUBLE\",\"class\":\"ENTATTR\"},{\"type\":\"DOUBLE\",\"class\":\"CONST\",\"value\":\"10\"}],\"enabled\":true,\"operator\":\"Greater_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}','2019-09-11 18:05:35','2019-09-11 18:05:35'),(385,224,117,'rule_97','手机归属地和IP归属地城市不一致','class StrategyCheckScript {\n  public boolean check(def data, def lists) {    if ((data.preItems.mobileLocationPre.city!=data.preItems.ipLocationPre.city))\n        return true;\n    else\n        return false;\n}}',20,0,'NONE','',100,0,1,'{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"preItems.mobileLocationPre.city\",\"type\":\"STRING\",\"class\":\"ENTATTR\"},{\"column\":\"preItems.ipLocationPre.city\",\"type\":\"STRING\",\"class\":\"ENTATTR\"}],\"enabled\":true,\"operator\":\"Field_Not_Equal\"}],\"class\":\"PDCT\",\"enabled\":true}','2019-09-11 18:05:35','2019-09-11 18:05:35'),(386,224,117,'rule_99','IP归属地不在中国','class StrategyCheckScript {\n  public boolean check(def data, def lists) {    if ((data.preItems.ipLocationPre.country!=\'中国\'))\n        return true;\n    else\n        return false;\n}}',20,0,'NONE','',100,0,1,'{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"preItems.ipLocationPre.country\",\"type\":\"STRING\",\"class\":\"ENTATTR\"},{\"type\":\"STRING\",\"class\":\"CONST\",\"value\":\"中国\"}],\"enabled\":true,\"operator\":\"NotEqual\"}],\"class\":\"PDCT\",\"enabled\":true}','2019-09-11 18:05:35','2019-09-11 18:05:35'),(387,224,117,'rule_127','命中手机号码白名单','class StrategyCheckScript {\n  public boolean check(def data, def lists) {    if (lists.mobile_white_list.containsKey(data.fields.mobile))\n        return true;\n    else\n        return false;\n}}',-200,0,'NONE','',100,0,1,'{\"linking\":\"All\",\"conditions\":[{\"class\":\"SMPL\",\"expressions\":[{\"column\":\"fields.mobile\",\"type\":\"STRING\",\"class\":\"ENTATTR\"},{\"type\":\"LIST\",\"class\":\"CONST\",\"value\":\"mobile_white_list\"}],\"enabled\":true,\"operator\":\"InList\"}],\"class\":\"PDCT\",\"enabled\":true}','2019-09-11 18:05:35','2019-09-11 18:05:35');
/*!40000 ALTER TABLE `engine_rule` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `engine_rule_history`
--

DROP TABLE IF EXISTS `engine_rule_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `engine_rule_history` (
  `ID` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `RULE_ID` bigint NOT NULL COMMENT '模型ID',
  `MERCHANT_CODE` varchar(60) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  `LABEL` varchar(64) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL DEFAULT '' COMMENT '规则名称',
  `INIT_SCORE` int NOT NULL DEFAULT '0' COMMENT '初始分数',
  `BASE_NUM` int NOT NULL DEFAULT '0' COMMENT '基数',
  `OPERATOR` varchar(32) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT '运算符',
  `ABSTRACTION_NAME` varchar(64) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT '抽象名称',
  `RATE` int NOT NULL DEFAULT '100' COMMENT '比例',
  `RULE_DEFINITION` varchar(2048) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  `UPDATE_TIME` datetime NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `engine_rule_history`
--

LOCK TABLES `engine_rule_history` WRITE;
/*!40000 ALTER TABLE `engine_rule_history` DISABLE KEYS */;
INSERT INTO `engine_rule_history` VALUES (1,377,'admin','IP归属地城市与手机号码归属地城市不一致',20,0,'NONE','',100,'','2019-09-11 17:27:20'),(2,378,'admin','注册IP来源于国外',30,0,'NONE','',100,'','2019-09-11 17:29:18'),(3,107,'admin','IP登录位置显示国外',20,0,'NONE','',100,'','2019-09-11 17:40:42'),(4,119,'admin','注册IP来源于国外',30,0,'NONE','',100,'','2019-09-11 17:41:08'),(5,117,'admin','IP归属地城市与手机号码归属地城市不一致',20,0,'NONE','',100,'','2019-09-11 17:41:16'),(6,97,'admin','手机归属地和IP归属地城市不一致',20,0,'NONE','',100,'','2019-09-11 17:42:52'),(7,99,'admin','IP归属地不在中国',20,0,'NONE','',100,'','2019-09-11 17:42:57');
/*!40000 ALTER TABLE `engine_rule_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `ID` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `USER_NAME` varchar(32) DEFAULT NULL,
  `PASSWD` varchar(64) DEFAULT NULL,
  `CODE` varchar(64) DEFAULT NULL,
  `STATUS` varchar(2) DEFAULT NULL,
  `VIP_LEVEL` int DEFAULT '1',
  `MOBILE` varchar(20) DEFAULT NULL,
  `EMAIL` varchar(32) DEFAULT NULL,
  `GITEE_ACCOUNT` varchar(32) DEFAULT NULL,
  `CREATE_TIME` datetime DEFAULT NULL,
  `UPDATE_TIME` datetime DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'admin','223CE7B851123353479D85757FBBF4E320D1E251','pgmmer.top','1',1,NULL,NULL,NULL,'2019-07-24 18:10:11','2019-07-24 18:10:14'),(2,'test','B2478239CD7E68E8052398D8EB87D385BF962085','ting.pgmmer.top','1',1,NULL,NULL,NULL,'2019-07-24 18:15:30','2019-07-24 18:15:33');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;

--
-- Table structure for table `engine_plugin_registry` (v3.1 插件生命周期)
--

DROP TABLE IF EXISTS `engine_plugin_registry`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `engine_plugin_registry` (
  `ID` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `PLUGIN_KEY` varchar(64) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT '插件 key',
  `SOURCE` varchar(16) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT '来源：BUILTIN_V2/BUILTIN_V3/EXTERNAL',
  `JAR_FILE` varchar(128) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '外置插件 jar 文件名（内置为空）',
  `VERSION` varchar(32) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL DEFAULT '' COMMENT '插件版本声明',
  `PROVIDER` varchar(64) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL DEFAULT '' COMMENT '提供方声明',
  `ENABLED` int NOT NULL DEFAULT 1 COMMENT '1=启用 0=禁用',
  `CREATE_TIME` datetime NOT NULL,
  `UPDATE_TIME` datetime NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UK_PLUGIN_KEY` (`PLUGIN_KEY`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='插件注册表（v3.1 插件生命周期）';
/*!40101 SET @saved_cs_client     = @@character_set_client */;

/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-11-14  8:56:37
