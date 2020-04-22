# 风控引擎（Radar）
## 项目介绍
 一款基于java语言，使用Springboot + Mongodb + Groovy + Es等框架搭建的轻量级实时风控引擎，适用于反欺诈应用场景，极简的配置，真正做到了开箱即用。     
 通过学习本项目能快速了解**风险的定义**，进而**量化风险** ，最后达到**集中管理风险**的目的。   
A real-time risk analysis engine,which can update risk rule in real-time and make it effective immediately.  
It applies to the anti-fraud application perfectly. 
The project code called Radar, like the code, monitor the transaction at the back.

## 项目特点

 * 实时风控，特殊场景可以做到100ms内响应
 * 可视化规则编辑器，丰富的运算符、计算规则灵活
 * 支持中文，易用性更强
 * 自定义规则引擎，更加灵活，支持复杂多变的场景
 * 插件化的设计，快速接入其它数据能力平台
 * NoSQL，易扩展，高性能
 * 配置简单，开箱即用！

## 相关站点
    Gitee:  https://gitee.com/freshday/radar  // 码云为镜像网站，贡献代码请提交到 github   
    Github: https://github.com/wfh45678/radar  
    官网：  http://radar.pgmmer.top  
    Wiki:   https://gitee.com/freshday/radar/wikis/home


## 背景
  伴随着移动互联网的高速发展，羊毛党快速崛起，从一平台到另一个平台，所过之处一地鸡毛，这还不是最可怕的，
  随之而来的黑产令大部分互联网应用为之胆寒，通常新上线的APP的福利比较大，风控系统不完善，BUG 被发现的频率也比较高，
  黑产利用BUG短时间给平台带来了巨大的损失，某多多的（100元测试优惠券，一夜损失上百万W）就是一例。 
  针对这一现象， 拥有一款实时的风控引擎是所有带有金融性质的APP 的当务之急，Radar应景而生。  
  Radar前身是笔者前公司的一个内部研究项目，由于众多原因项目商业化失败，考虑到项目本身的价值，弃之可惜，
  现使用Springboot进行重构，删除了很多本地化功能，只保留风控引擎核心，更加通用，更加轻量，二次开发成本低，
  开源出来，希望能给有风控需求的你们带来一些帮助。

## 项目初衷
我们知道企业做大后，会有很多产品线，而几乎每一个产品都需要做风险控制，通常我们都是把风险控制的逻辑写在相应的业务功能代码里，
大量重复的风控逻辑代码耦合在我们的业务逻辑里面，随着时间的累积，代码会变得异常复杂，会给后期的维护造成巨大的人力成本和风险。

所以风险的集中化管理势在必行，只有通过一个统一的管理平台，使用规则引擎，采用可视化配置的形式，
平台化管理不同产品的风控策略才是一种更好的方式, 而这正是Radar的初衷。

## 项目架构

前后端分离架构

后端技术框架： SpringBoot + Mybatis + tkMapper + Mysql +  MongoDB + Redis + Groovy + ES + Swagger

前端技术框架： React(SPA) 

### 架构图
![系统模块](http://www.pgmmer.top/radar/sys_model_arch.png)

## 技术选型
* Springboot：笔者是java 出生， 选择 Springboot 理所当然，方便自己， 也方便其他Java使用者进行扩展。

* Mybatis + tkMapper： 持久层框架， tkMapper 提供mapper 通用模板功能，减少重复代码的生成。

* Mysql ： 本项目中关系数据库，主要用于存放 风险模型的元信息。

* MongoDB： 用于存放事件JSON， 提供基本统计学计算（例如：max, min, sum, avg,），
复杂的统计学概念（sd,variance, etc...）在内存中计算。

* ES： 提供数据查询和规则命中报表服务。

* Redis： 提供缓存支持，Engine 利用发布订阅特性监听管理端相关配置的更新

* Groovy： 规则引擎，风控规则最后都生成 groovy 脚本， 实时编辑，动态生成，即时生效。

* Swagger:  Rest API 管理

## 名词解释
### Model: 事件模型
  用户行为事件， 例如：注册，登录，购买，提现等具体的业务行为。
### 事件模型三要素 
  也就是事件行为三要素，风控系统的核心定义：事件流水ID(例如：交易流水号)，实体ID（例如：userId,表示某个人或物），事件发生时间（例如：交易时间）,
  简单来说就是谁（可以是人，也可以是物）什么时候做了什么事。（who, when, what）
### PreItem: 预处理 
 像经纬度，IP，手机号码段等事件属性，可能无法直接计算，通过预处理插件 转换成 其他格式，
 例如:经纬度，ip 可以通过对应插件变成位置和地址，手机号码可以通过插件获取其它系统的用户画像信息等。  
### Abstraction: 特征
特征工程，例如用户小时交易次数，IP 一天交易金额，设备一小时交易次数。。。
### Adaptation: 机器学习模型适配器
 使用训练好的机器学习模型，进行检测
### Activation: 激活点
概念类似于机器学习里面的 (Activation Function)， 一个模型可以定义多个 activation（相当于不同维度的检测报告）,每个activation都可以独立配置规则，单独打分。
例如，用户注册行为， 可以定义：异常注册， 垃圾注册， 可以输出多个activation。
### Rule: 规则
在计算 abstraction 和 activation 之前，需要先检查数据是否正常，检查就是按照rule 配置进行检测。
### Plugin：插件
 为了通用性，项目自身并不提供敏感数据能力，但是通过扩展插件来获得其它系统或者中间数据功能，
 目前项目自带手机号码段和IP地址转换两款插件，理论上可以通过扩展插件集成任何其它数据的能力，
 例如：如果商户自身带有内部用户画像，可以自定义一个插件用过用户ID来获取用户画像数据。

---

## [使用手册](https://gitee.com/freshday/radar/wikis/manual)
使用手册里面有大量的图片，为了方便国内用户使用，故推荐码云的wiki 链接,  
https://gitee.com/freshday/radar/wikis/manual


## 演示入口
演示Demo只提供管理端配置功能，暂时不提供引擎计算功能。  
通过管理端能够快速了解系统是怎么从风险的定义到风险的量化再到风险的集中管理的整个工作流程。  
为了更好的体验，请花一分钟观看 [使用手册](https://gitee.com/freshday/radar/wikis/manual?sort_id=1637446)  
[Demo URL：](http://radar.pgmmer.top) http://radar.pgmmer.top   
建议大家自行注册用户，避免使用同样的测试账号受干扰.   

## 未完待续
[Release Note:](https://gitee.com/freshday/radar/wikis/release%20note?sort_id=1723765) https://gitee.com/freshday/radar/wikis/release%20note
### 重大特性预告
* 新建模型支持一键自动提取特征 自动生成规则 自动设置阈值


## 致谢
感恩 XWF 团队，感谢参入的每一位小伙伴，后续征得同意后会一一列出名字。  
千面怪, 烈日下的从容, DerekDingLu, king, sanying2012, 紫泉夜, 玄梦     
成书平, 徐帅，郭锐， 王成，马兆永...
## Contact to

 如果喜欢本项目，Star支持一下, 让更多人了解本项目，谢谢！   
 独乐乐不如众乐乐，微信（nicedream7758）加群一起嗨！  
 提示：进群需要捐赠到gitee，0.1元起  
 ![微信交流群](http://radar.pgmmer.top/radar/wx2.jpg)   
 
 Copyright ©  2019-2020 WangFeiHu
