-- Radar v4.0.0 增量升级脚本
-- 适用：从 v3.x 升级。全新部署直接导入 radar-init.sql，无需本脚本。
-- 内容（v4.0 策略板块重构）：
--   1. "激活器"更名"策略"：engine_activation → engine_strategy，列 ACTIVATION_NAME → STRATEGY_NAME，
--      engine_rule.ACTIVATION_ID → STRATEGY_ID（纯改名，数据不动）。
--   2. 策略类型化：新增 TYPE（RULE_SET/SCORECARD/DECISION_TREE）与 DEFINITION（评分卡/决策树 JSON 定义）。
--      存量策略 TYPE 默认 RULE_SET，行为与升级前完全一致。
--   3. 新增 engine_strategy_history 策略定义变更历史（评分卡/决策树保存时快照审计）。
-- 注意：
--   - 管理端 API 路径 /services/v1/activation → /services/v1/strategy，前端与 v4.0 安装包同步发布。
--   - 引擎决策接口（/services/v1/upload、/getScore）响应结构不变，调用方无需改造。
--   - 策略包文件 JSON 键保持 "activations"/"activationName"/"activationId"，v3.x 导出的包可直接导入。
-- 详细说明与验收清单见 docs/upgrade-guide.md。

SET NAMES utf8mb4;

-- 1. 表更名 + 列更名
RENAME TABLE `engine_activation` TO `engine_strategy`;
ALTER TABLE `engine_strategy`
  CHANGE COLUMN `ACTIVATION_NAME` `STRATEGY_NAME` varchar(64) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT '名称';
ALTER TABLE `engine_rule`
  CHANGE COLUMN `ACTIVATION_ID` `STRATEGY_ID` bigint NOT NULL COMMENT '所属策略ID';

-- 2. 策略类型化（存量默认规则集，行为不变）
ALTER TABLE `engine_strategy`
  ADD COLUMN `TYPE` varchar(32) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL DEFAULT 'RULE_SET' COMMENT '策略类型：RULE_SET/SCORECARD/DECISION_TREE' AFTER `STRATEGY_NAME`,
  ADD COLUMN `DEFINITION` mediumtext CHARACTER SET utf8 COLLATE utf8_unicode_ci COMMENT '策略类型化定义（评分卡/决策树 JSON，规则集为空）' AFTER `RULE_ORDER`;

-- 3. 策略定义变更历史
CREATE TABLE IF NOT EXISTS `engine_strategy_history` (
  `ID` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `STRATEGY_ID` bigint NOT NULL COMMENT '策略ID',
  `MERCHANT_CODE` varchar(64) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '操作人',
  `TYPE` varchar(32) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '策略类型',
  `DEFINITION` mediumtext CHARACTER SET utf8 COLLATE utf8_unicode_ci COMMENT '保存时的定义快照',
  `UPDATE_TIME` datetime NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `idx_strategy_history_sid` (`STRATEGY_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='策略定义变更历史（v4.0）';
