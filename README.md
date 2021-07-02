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
    Gitee:  https://gitee.com/freshday/radar    
    Github: https://github.com/wfh45678/radar  // github 为镜像网站，贡献代码请提交到 gitee  
    官网：  https://www.riskengine.cn  
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
![系统模块](https://www.riskengine.cn/radar/sys_model_arch.png) 

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


---

## [使用手册](https://gitee.com/freshday/radar/wikis/manual)
使用手册里面有大量的图片，为了方便国内用户使用，故推荐码云的wiki 链接,  
https://gitee.com/freshday/radar/wikis/manual


## 演示入口
通过管理端能够快速了解系统是怎么从风险的定义到风险的量化再到风险的集中管理的整个工作流程。  
为了更好的体验，请花一分钟观看 [使用手册](https://gitee.com/freshday/radar/wikis/manual?sort_id=1637446)  
[Demo URL：](https://www.riskengine.cn) https://www.riskengine.cn   
建议大家自行注册用户，避免使用同样的测试账号受干扰.   

## 相关文档
[WIKI:](https://gitee.com/freshday/radar/wikis/home?sort_id=1637444) https://gitee.com/freshday/radar/wikis/home?sort_id=1637444



## 致谢
感恩 XWF 团队，感谢参入的每一位小伙伴，后续征得同意后会一一列出名字。  
千面怪, 烈日下的从容, DerekDingLu, king, sanying2012, 紫泉夜, 玄梦     
成书平, 徐帅，郭锐， 王成，马兆永...

## 赞助商
* 中和农信项目管理有限公司   
* 二十六度数字科技(广州)有限公司  
感谢赞助商大大们对本项目的认可和支持。

## Contact to

 如果喜欢本项目，Star支持一下, 让更多人了解本项目，谢谢！   
 独乐乐不如众乐乐，微信（nicedream7758）加群一起嗨！  
 提示：进群需要捐赠到gitee  

 
 ## 特别说明
 前端源码仅对企业级用户开放，需付费购买。   
 未经授权，禁止使用本项目源码申请软著和专利，保留追究法律责任的权力！   
 Copyright ©  2019-2021 WangFeiHu
