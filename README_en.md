# Risk Engine（Radar）
## Introduction

A real-time risk analysis engine,which can update risk rule in real-time and make it effective immediately.  
It applies to the anti-fraud application perfectly. 
The project code called Radar, like the code, monitor the transaction at the back.

## FEATURE

 * real-time
 * visual rule editor
 * plugin design
 * nosql, easy extended
 * simple configuration 
 * out-of-the-box

## Relation Site
    Gitee:  https://gitee.com/freshday/radar  
    Github: https://github.com/wfh45678/radar  
    Official Site：  https://www.riskengine.cn  
    Wiki:   https://gitee.com/freshday/radar/wikis/home


## Architecture


Server-end： SpringBoot + Mybatis + tkMapper + Mysql +  MongoDB + Redis + Groovy + ES + Swagger

Front-end： React(SPA) 

### System Component Diagram
![Architecture](https://www.riskengine.cn/radar/sys_model_arch.png) 

## Technology stack
* Springboot：base spring boot 2.x framework.

* Mybatis + tkMapper： data persistence layer framework.

* Mysql ： model meta data storage.

* MongoDB： for event data storage.

* ES： risk result data analysis.

* Redis： cache, pub sub when model meta data change.

* Groovy： rule engine. 

* Swagger:  Rest API.


---

## [manual](https://gitee.com/freshday/radar/wikis/manual)
https://gitee.com/freshday/radar/wikis/manual


## Demo site
[Demo URL：](http://121.36.168.6:6580/) http://121.36.168.6:6580/   

## wiki
[WIKI:](https://gitee.com/freshday/radar/wikis/home?sort_id=1637444) https://gitee.com/freshday/radar/wikis/home?sort_id=1637444



## Thanks

千面怪, 烈日下的从容, DerekDingLu, king, sanying2012, 紫泉夜, 玄梦
成书平, 徐帅，郭锐， 王成，马兆永...


 
 ## Statement
 Copyright ©  2019-2021 WangFeiHu
