CREATE TABLE `engine_model_conf` (
                                     `id` bigint(20) NOT NULL AUTO_INCREMENT,
                                     `model_id` bigint(20) DEFAULT NULL,
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



CREATE TABLE `engine_model_conf_param` (
                                           `id` bigint(20) NOT NULL AUTO_INCREMENT,
                                           `mold_id` bigint(20) DEFAULT NULL,
                                           `feed` varchar(255) DEFAULT NULL,
                                           `expressions` varchar(255) DEFAULT NULL,
                                           PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

ALTER TABLE `engine_pre_item`
    MODIFY COLUMN `ARGS`  varchar(250) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT '' COMMENT '参数' AFTER `LABEL`;
ALTER TABLE `engine_pre_item`
    ADD COLUMN `CONFIG_JSON`  varchar(250) NULL COMMENT '响应字段配置信息' AFTER `PLUGIN`;
ALTER TABLE `engine_pre_item`
    ADD COLUMN `REQ_TYPE`  varchar(16) NULL COMMENT '请求方式' AFTER `CONFIG_JSON`;


ALTER TABLE `users`    ADD COLUMN `VIP_LEVEL` int(0) NULL DEFAULT 1 AFTER `STATUS`;
ALTER TABLE `users`    ADD COLUMN `MOBILE` varchar(20) NULL AFTER `VIP_LEVEL`;
ALTER TABLE `users`    ADD COLUMN `EMAIL` varchar(32) NULL AFTER `MOBILE`;
ALTER TABLE `users`    ADD COLUMN `GITEE_ACCOUNT` varchar(32) NULL AFTER `EMAIL`;

INSERT INTO `radar`.`engine_model`(`ID`, `GUID`, `MODEL_NAME`, `LABEL`, `ENTITY_NAME`, `ENTRY_NAME`, `REFERENCE_DATE`, `FIELD_VALIDATE`, `CODE`, `TEMPLATE`, `DASHBOARD_URL`, `STATUS`, `CREATE_TIME`, `UPDATE_TIME`) VALUES (1, '00000000-0000-0000-0000-000000000001', 'system', 'system', 'userId', 'eventId', 'loginTime', 0, 'pgmmer.top', 1, NULL, 0, '2016-11-17 10:59:43', '2016-11-18 18:02:15');