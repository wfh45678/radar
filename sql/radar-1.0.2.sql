ALTER TABLE `engine_pre_item`
    MODIFY COLUMN `ARGS`  varchar(250) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT '' COMMENT '参数' AFTER `LABEL`;
ALTER TABLE `engine_pre_item`
ADD COLUMN `CONFIG_JSON`  varchar(250) NULL COMMENT '响应字段配置信息' AFTER `PLUGIN`;
ALTER TABLE `engine_pre_item`
    ADD COLUMN `REQ_TYPE`  varchar(16) NULL COMMENT '请求方式' AFTER `CONFIG_JSON`;

