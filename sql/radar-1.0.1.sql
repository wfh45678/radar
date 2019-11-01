-- developers of radar
CREATE TABLE `contributors` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `NAME` varchar(30) NOT NULL,
  `CONTENT` varchar(200) NOT NULL,
  `DEVELOPER` varchar(60) NOT NULL,
  `RELEASE_VERSION` varchar(30) DEFAULT NULL,
  `CREATE_TIME` datetime DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
