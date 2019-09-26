# 风控引擎（Radar）
## 项目介绍
 实时风控引擎，实时可配置，规则配置即时生效。 开箱即用！！！开箱即用！！！开箱即用！！！  
 A real-time risk analysis engine,which can update risk rule in real-time and  make it effective immediately.   
 It admirably applies to the anti-fraud application.  
 The project code called Radar,  like the code,  monitor the transaction at the back. 

## 背景
  伴随着移动互联网的高速发展，羊毛党快速崛起，从一平台到另一个平台，所过之处一地鸡毛，这还不是最可怕的，
  随之而来的黑产令大部分互联网应用为之胆寒，通常新上线的APP的福利比较大，风控系统不完善，BUG 被发现的频率也比较高，
  黑产利用BUG短时间给平台带来了巨大的损失，某多多的（100元测试优惠券，一夜损失上百万W）就是一例。 
  针对这一现象， 拥有一款实时的风控引擎是所有带有金融性质的APP 的当务之急，
  Radar 应景而生，Radar本来是笔者前公司的一个内部项目，公司现在不复存在，考虑到项目本身的价值，
  现在使用Springboot进行改造，并删除了很多本地化功能，只保留风控引擎核心，更具通用型，二次开发成本低。

## 项目架构

前后端分离架构

后端技术框架： SpringBoot + Mysql +  MongoDB + Redis + Groovy + Swagger

前端技术框架： React(SPA) 

### 架构图
![系统模块](https://github.com/wfh45678/radar/blob/master/resources/images/radar_model_arch.jpg)

## 技术选型
* Springboot：笔者是java 出生， 选择 Springboot 理所当然，方便自己， 也方便其他Java使用者进行扩展。

* Mysql ： 本项目中关系数据库的作用不大，主要用于存放 风险模型的元信息。

* MongoDB： 用于存放事件JSON， 提供基本统计学计算（例如：max, min, sum, avg, ），
复杂的统计学概念（sd,variance, etc...）在内存中计算。

* Redis： 提供缓存支持，利用发布订阅特性监听配置更新

* Groovy： 脚本引擎，风控规则最后都生成 groovy 脚本， 可以动态配置，即时生效。

* Swagger:  Rest API 管理

## 名词解释
### Model: 模型
 用户行为事件， 例如：注册，登录，购买，提现。。。
### PreItem: 预处理 
 像IP，手机号码段等事件属性，可能无法直接计算，通过预处理插件 转换成 其他格式，
  例如:ip 可以通过IP 插件变成位置和地址
### Abstraction: 特征
特征工程，例如用户小时交易次数，IP 一天交易金额，设备一小时交易次数。。。
### Adaptation: 机器学习模型适配器
 使用训练好的机器学习模型，进行检测
### Activation: 激活点
概念类似于机器学习里面的 (Activation Function)， 一个模型可以定义多个 activation（相当于不同维度的检测报告）,每个activation都可以独立配置规则，单独打分。
例如，用户注册行为， 可以定义：异常注册， 垃圾注册， 可以输出多个activation。
### Rule: 规则
在计算 abstraction 和 activation 之前，需要先检查数据是否正常，检查就是按照rule 配置进行检测。

---
## 演示入口
演示Demo只提供管理端配置功能，暂时不提供引擎计算功能。  

[Demo URL：](http://radar.pgmmer.top) http://radar.pgmmer.top
admin/123456

## [帮助手册](https://github.com/wfh45678/radar/wiki/manual)
https://github.com/wfh45678/radar/wiki/manual

## 未完待续
### 小迭代
* 集成嵌入式redis版本，本地调试的时候就不用再单独部署redis
* 集成 JWT(JSON WEB TOKEN)，前后端分离标准化
### 重大特性
* 支持机器学习模型
* 数据分析子项目

## Contact to

 Copyright by XWF, mail to wfh45678@163.com , QQ 240159429 
