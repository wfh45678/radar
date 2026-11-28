-- Radar v4.1.0 增量升级脚本
-- 适用：从 v4.0.x 升级。全新部署直接导入 radar-init.sql，无需本脚本。
-- 内容（v4.1 特征工程重构——预处理 PreItem + 抽象 Abstraction 统一为特征 Feature）：
--   1. 统一特征表 engine_feature（ENRICH 插件加工 / AGGREGATE 统计聚合 / DERIVED 衍生 / RATIO 组合），
--      存量数据自动搬迁：engine_pre_item → ENRICH，engine_abstraction → AGGREGATE。
--   2. 上下文/列前缀迁移：preItems.xxx / abstractions.xxx → features.xxx
--      （规则脚本/规则条件 JSON/打分字段/策略定义/规则历史/ML 参数表达式）。
--   3. 引擎响应与 ES 文档键：features（合并原 preItems+abstractions）、strategies
--      （修复 v4.0 写 activations 与报表读 strategies 的失配）。调用方与看板需按升级指南调整。
-- 注意：
--   - 升级前请备份（本脚本含 DROP TABLE）；多副本实例必须全部停机后同步升级
--     （Redis 频道合并为 radar_channel_feature，混版本实例互相收不到失效推送）。
--   - 特征名重复检测（历史无唯一约束，建议升级前执行，重复名会导致引用歧义）：
--       SELECT MODEL_ID, DEST_FIELD AS NAME, COUNT(*) C FROM engine_pre_item GROUP BY MODEL_ID, DEST_FIELD HAVING C > 1
--       UNION ALL
--       SELECT MODEL_ID, NAME, COUNT(*) C FROM engine_abstraction GROUP BY MODEL_ID, NAME HAVING C > 1;
--   - 统计口径变化：DISTINCT_COUNT 与数值族聚合（SUM/AVERAGE/MAX/MIN/SD/VARIANCE/MEDIAN）
--     升级后开始过滤 status='1'（与 COUNT 一致）；如不可接受请回滚本脚本。
-- 详细说明与验收清单见 docs/upgrade-guide.md（v4.0→v4.1 章节）。

SET NAMES utf8mb4;

-- 1. 统一特征表
CREATE TABLE IF NOT EXISTS `engine_feature` (
  `ID` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `FEATURE_NAME` varchar(64) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT '特征名（上下文引用键 features.<featureName>）',
  `TYPE` varchar(32) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT '特征类型：ENRICH/AGGREGATE/DERIVED/RATIO',
  `LABEL` varchar(32) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT '',
  `MODEL_ID` bigint NOT NULL COMMENT 'model id',
  `ORDER_NO` int DEFAULT NULL COMMENT '阶段内执行顺序（链式依赖按此排序）',
  `COMMENT` varchar(128) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '备注',
  `STATUS` int NOT NULL DEFAULT '1' COMMENT '状态：1=启用',
  `DEFINITION` mediumtext CHARACTER SET utf8 COLLATE utf8_unicode_ci COMMENT '类型化定义 JSON（结构随 TYPE）',
  `OUTPUT` mediumtext CHARACTER SET utf8 COLLATE utf8_unicode_ci COMMENT '多列输出列声明 [{column,title,type}]',
  `CREATE_TIME` datetime NOT NULL,
  `UPDATE_TIME` datetime NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `idx_feature_model` (`MODEL_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='特征表（v4.1 特征工程统一）';

-- 2. 存量搬迁：engine_pre_item → ENRICH
INSERT INTO `engine_feature`
  (`FEATURE_NAME`, `TYPE`, `LABEL`, `MODEL_ID`, `STATUS`, `DEFINITION`, `OUTPUT`, `CREATE_TIME`, `UPDATE_TIME`)
SELECT
  DEST_FIELD,
  'ENRICH',
  LABEL,
  MODEL_ID,
  IFNULL(STATUS, 1),
  JSON_OBJECT('plugin', PLUGIN,
              'reqType', REQ_TYPE,
              'args', CASE WHEN JSON_VALID(ARGS) THEN CAST(ARGS AS JSON) ELSE ARGS END,
              'sourceFields', SOURCE_FIELD),
  CASE WHEN JSON_VALID(CONFIG_JSON) THEN CAST(CONFIG_JSON AS JSON) ELSE NULL END,
  NOW(), NOW()
FROM `engine_pre_item`;

-- 3. 存量搬迁：engine_abstraction → AGGREGATE
INSERT INTO `engine_feature`
  (`FEATURE_NAME`, `TYPE`, `LABEL`, `MODEL_ID`, `STATUS`, `COMMENT`, `DEFINITION`, `CREATE_TIME`, `UPDATE_TIME`)
SELECT
  NAME,
  'AGGREGATE',
  LABEL,
  MODEL_ID,
  IFNULL(STATUS, 1),
  COMMENT,
  JSON_OBJECT('aggregateType',
              (CASE AGGREGATE_TYPE
                 WHEN 1 THEN 'COUNT' WHEN 2 THEN 'DISTINCT_COUNT' WHEN 3 THEN 'SUM' WHEN 4 THEN 'AVERAGE'
                 WHEN 5 THEN 'MAX' WHEN 6 THEN 'MIN' WHEN 7 THEN 'SD' WHEN 8 THEN 'VARIANCE'
                 WHEN 9 THEN 'DEVIATION' WHEN 10 THEN 'MEDIAN' ELSE 'COUNT' END),
              'searchField', SEARCH_FIELD,
              'searchIntervalType', SEARCH_INTERVAL_TYPE,
              'searchIntervalValue', SEARCH_INTERVAL_VALUE,
              'functionField', FUNCTION_FIELD,
              'filter', CASE WHEN JSON_VALID(RULE_DEFINITION) THEN CAST(RULE_DEFINITION AS JSON) ELSE NULL END),
  NOW(), NOW()
FROM `engine_abstraction`;

-- 4. 上下文/列前缀迁移（路径引号/前缀锚定，防误伤）
-- 4.1 规则脚本：data.preItems.* → data.features.*，data.abstractions.* → data.features.*
UPDATE `engine_rule` SET
  `SCRIPTS` = REPLACE(REPLACE(`SCRIPTS`, 'data.preItems.', 'data.features.'),
                      'data.abstractions.', 'data.features.'),
  `RULE_DEFINITION` = REPLACE(REPLACE(`RULE_DEFINITION`, '"preItems.', '"features.'),
                              '"abstractions.', '"features.'),
  `ABSTRACTION_NAME` = REPLACE(`ABSTRACTION_NAME`, 'abstractions.', 'features.');
-- 4.2 规则历史
UPDATE `engine_rule_history` SET
  `RULE_DEFINITION` = REPLACE(REPLACE(`RULE_DEFINITION`, '"preItems.', '"features.'),
                              '"abstractions.', '"features.'),
  `ABSTRACTION_NAME` = REPLACE(`ABSTRACTION_NAME`, 'abstractions.', 'features.');
-- 4.3 策略定义（评分卡 column / 决策树条件）与历史快照
UPDATE `engine_strategy` SET
  `DEFINITION` = REPLACE(REPLACE(`DEFINITION`, '"preItems.', '"features.'),
                         '"abstractions.', '"features.');
UPDATE `engine_strategy_history` SET
  `DEFINITION` = REPLACE(REPLACE(`DEFINITION`, '"preItems.', '"features.'),
                         '"abstractions.', '"features.');
-- 4.4 ML 参数表达式（休眠配置，随迁保持一致；列名为小写 expressions——v1 时代建表从未更名）
UPDATE `engine_model_conf_param` SET
  `expressions` = REPLACE(REPLACE(`expressions`, 'preItems.', 'features.'),
                          'abstractions.', 'features.')
  WHERE `expressions` IS NOT NULL;

-- 5. 拆除旧表（升级前请确认已备份）
DROP TABLE IF EXISTS `engine_pre_item`;
DROP TABLE IF EXISTS `engine_abstraction`;

-- 6. 搬迁校验（应等于原两表行数之和）
-- SELECT TYPE, COUNT(*) FROM engine_feature GROUP BY TYPE;
