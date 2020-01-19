/*
Navicat MySQL Data Transfer

Source Server         : test@172.30.0.6
Source Server Version : 50726
Source Host           : 172.30.0.6:3306
Source Database       : radar

Target Server Type    : MYSQL
Target Server Version : 50726
File Encoding         : 65001

Date: 2019-12-24 18:02:12
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for engine_model_conf
-- ----------------------------
DROP TABLE IF EXISTS `engine_model_conf`;
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


-- ----------------------------
-- Table structure for engine_model_conf_param
-- ----------------------------
DROP TABLE IF EXISTS `engine_model_conf_param`;
CREATE TABLE `engine_model_conf_param` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `mold_id` bigint(20) DEFAULT NULL,
  `feed` varchar(255) DEFAULT NULL,
  `expressions` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
