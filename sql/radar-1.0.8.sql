ALTER TABLE `engine_rule`
    ADD COLUMN `MAX` int(11)  NOT NULL DEFAULT 0 COMMENT '最大得分值' AFTER `RATE`;
	
ALTER TABLE  `engine_data_list_records`
   ADD COLUMN  `DATA_REMARK` varchar(32) NULL COMMENT '数据备注';
