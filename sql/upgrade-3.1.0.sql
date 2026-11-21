-- Radar v3.1.0 增量升级脚本
-- 适用：从 v3.0.x 升级。全新部署直接导入 radar-init.sql，无需本脚本。
-- 内容：engine_plugin_registry 插件注册表（v3.1 插件生命周期：上传/启停/删除）。
-- 应用启动时自动与注册表对账（内置/外置插件自动写入行），无需手工插数据。

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `engine_plugin_registry` (
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
