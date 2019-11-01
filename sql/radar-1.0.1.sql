-- developers of radar
CREATE TABLE `contributors` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `NAME` varchar(30) NOT NULL COMMENT '功能名称',
  `CONTENT` varchar(200) NOT NULL COMMENT '功能描叙',
  `DEVELOPER` varchar(60) NOT NULL COMMENT '开发者',
  `RELEASE_VERSION` varchar(30) DEFAULT NULL COMMENT '发布版本',
  `CREATE_TIME` datetime DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
