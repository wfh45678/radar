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
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `model_id` int(11) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `path` varchar(255) DEFAULT NULL,
  `tag` varchar(255) DEFAULT NULL,
  `operation` varchar(255) DEFAULT NULL,
  `update_date` datetime DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of engine_model_conf
-- ----------------------------
INSERT INTO `engine_model_conf` VALUES ('1', '103', '交易ai模型', 'd:/radar01', 'serve', 'output_y/BiasAdd', '2019-12-24 17:38:38', 'TENSOR_DNN');

-- ----------------------------
-- Table structure for engine_model_conf_param
-- ----------------------------
DROP TABLE IF EXISTS `engine_model_conf_param`;
CREATE TABLE `engine_model_conf_param` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `mold_id` int(11) DEFAULT NULL,
  `feed` varchar(255) DEFAULT NULL,
  `expressions` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of engine_model_conf_param
-- ----------------------------
INSERT INTO `engine_model_conf_param` VALUES ('1', '1', 'input_x', 'abstractions.tran_uid_ip_1_day_qty,abstractions.tran_did_ip_1_day_qty,abstractions.tran_ip_1_day_qty,abstractions.tran_ip_1_hour_qty,abstractions.tran_ip_1_day_amt,abstractions.tran_did_1_day_qty');
