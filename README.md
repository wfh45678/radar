# 风控引擎（Radar）
## 项目介绍
 一款基于java语言，使用Springboot + Mongodb + Groovy 等框架搭建实时风控引擎，适用于反欺诈应用场景。  
A real-time risk analysis engine,which can update risk rule in real-time and make it effective immediately.  
It applies to the anti-fraud application perfectly. 
The project code called Radar, like the code, monitor the transaction at the back.
## 项目特点

 * 实时风控，特殊场景可以做到100ms内响应
 * 可视化编辑，丰富的运算符、计算规则灵活
 * 支持中文，易用性更强
 * 自定义规则引擎，更加灵活，支持复杂多变的场景
 * 特性插件化，快速接入其它数据能力
 * Nosql，易扩展，高性能
 * 配置简单，开箱即用！

## 背景
  伴随着移动互联网的高速发展，羊毛党快速崛起，从一平台到另一个平台，所过之处一地鸡毛，这还不是最可怕的，
  随之而来的黑产令大部分互联网应用为之胆寒，通常新上线的APP的福利比较大，风控系统不完善，BUG 被发现的频率也比较高，
  黑产利用BUG短时间给平台带来了巨大的损失，某多多的（100元测试优惠券，一夜损失上百万W）就是一例。 
  针对这一现象， 拥有一款实时的风控引擎是所有带有金融性质的APP 的当务之急，
  Radar 应景而生，Radar本来是笔者前公司的一个内部项目，公司现在不复存在，考虑到项目本身的价值，
  现在使用Springboot进行改造，并删除了很多本地化功能，只保留风控引擎核心，更具通用型，二次开发成本低。

## 项目架构

前后端分离架构

后端技术框架： SpringBoot + Mybatis + tkMapper + Mysql +  MongoDB + Redis + Groovy + Swagger

前端技术框架： React(SPA) 

### 架构图
![系统模块](http://www.pgmmer.top/radar/sys_model_arch.png)

## 技术选型
* Springboot：笔者是java 出生， 选择 Springboot 理所当然，方便自己， 也方便其他Java使用者进行扩展。

* Mybatis + tkMapper： 持久层框架， tkMapper 提供mapper 通用模板功能，减少重复代码的生成。

* Mysql ： 本项目中关系数据库，主要用于存放 风险模型的元信息。

* MongoDB： 用于存放事件JSON， 提供基本统计学计算（例如：max, min, sum, avg, ），
复杂的统计学概念（sd,variance, etc...）在内存中计算。

* Redis： 提供缓存支持，Engine 利用发布订阅特性监听管理端相关配置的更新

* Groovy： 规则引擎，风控规则最后都生成 groovy 脚本， 实时编辑，动态生成，即时生效。

* Swagger:  Rest API 管理

## 名词解释
### Model: 模型
  用户行为事件， 例如：注册，登录，购买，提现。。。
### 模型三要素 
  也就是事件行为三要素，风控系统的核心定义：事件流水ID(例如：交易流水号)，实体ID（例如：userId），事件发生时间（例如：交易时间）,
  简单来说就是谁什么时候做了什么事。（who, when, what）
### PreItem: 预处理 
 像经纬度，IP，手机号码段等事件属性，可能无法直接计算，通过预处理插件 转换成 其他格式，
 例如:经纬度，ip 可以通过对应插件变成位置和地址，手机号码可以通过插件获取关联征信信息等。  
### Abstraction: 特征
特征工程，例如用户小时交易次数，IP 一天交易金额，设备一小时交易次数。。。
### Adaptation: 机器学习模型适配器
 使用训练好的机器学习模型，进行检测
### Activation: 激活点
概念类似于机器学习里面的 (Activation Function)， 一个模型可以定义多个 activation（相当于不同维度的检测报告）,每个activation都可以独立配置规则，单独打分。
例如，用户注册行为， 可以定义：异常注册， 垃圾注册， 可以输出多个activation。
### Rule: 规则
在计算 abstraction 和 activation 之前，需要先检查数据是否正常，检查就是按照rule 配置进行检测。
### 插件
 为了通用性，项目自身并不提供关联数据能力，但是通过扩展插件来获得其它系统或者中间数据功能，
 目前项目自带手机号码和IP地址转换两款插件，理论上可以通过插件可以集成任何其它数据的能力，
 例如：如果商户自身带有内部征信，可以自定义一个插件用过用户ID来获取征信数据。

---
## 演示入口
演示Demo只提供管理端配置功能，暂时不提供引擎计算功能。  

[Demo URL：](http://radar.pgmmer.top) http://radar.pgmmer.top
admin/123456

## [使用手册](https://gitee.com/freshday/radar/wikis/manual?sort_id=1637446)
使用手册里面有大量的图片，为了方便使用故推荐码云的wiki 链接,  
https://gitee.com/freshday/radar/wikis/manual?sort_id=1637446

## 未完待续
### 小迭代
* React 版本升级
* 用户注册功能，以后演示环境支持多租户
* 集成嵌入式redis版本，本地调试的时候就不用再单独部署redis
* 集成 JWT(JSON WEB TOKEN)，前后端分离标准化
* 插件配置管理，可以集成其他中间件的数据能力
* 支持Flink，增加特征（abstraction）的提取基于Flink 的实现，以应对时间窗口相对较短，实时性要求更高的情况。
### 重大特性
* 支持机器学习
* 数据分析平台

## Contact to

 如果喜欢本项目，点下star支持一下, 让更多人看到，谢谢！   
 独乐乐不如众乐乐，微信（nicedream7758）扫码加群一起交流  
 ![微信交流群](http://www.pgmmer.top/radar/wxgroup.png)
 
 Copyright © XWF
