# 风控引擎（Radar）

一款基于 Java 的轻量级实时风控决策引擎（Spring Boot 3.5 + MySQL + MongoDB + Redis + Groovy + ES），
适用于反欺诈与消费金融场景：可视化配置规则、实时生效、开箱即用。
Radar 的初衷是把散落在各业务代码中的风控逻辑解耦出来，通过统一平台集中化管理：
理解**风险的定义**，进而**量化风险**，最终**集中管理风险**。

[English](README_en.md) · [在线 Demo](https://www.riskengine.cn) · [使用手册](https://gitee.com/freshday/radar/wikis/manual)

## 核心特性

* 实时风控，常规场景 100ms 内响应
* 可视化规则编辑器，支持中文，易用性强
* 六大策略类型：规则引擎 / 决策树 / 评分卡 / 决策表 / 名单匹配 / 规则集
* Groovy 动态规则引擎，复杂场景灵活应对，规则编辑即时生效
* 插件化设计（v3.0 外置 jar 插件 + 动态表单；v3.1 管理端上传/启停/热重载），快速接入外部数据能力
* 单模块单体架构（Java 17 + Spring Boot 3.5），一个 jar 同时提供管理端与引擎端 API

## 快速开始

```bash
# 方式一：Docker 一键环境（MySQL/Mongo/Redis/ES 自动导入种子数据；--profile app 额外启动应用镜像）
docker compose up -d

# 方式二：源码构建（需 JDK 21+，Maven 配置见 .mvn/settings.xml）
mvn package -s .mvn/settings.xml -DskipTests
# （可先 docker compose up -d 起中间件，再）运行：
java -jar target/radar.jar

# 管理端 UI:   http://localhost:6580/
# API 文档:    http://localhost:6580/swagger-ui/index.html （分组：admin / engine）

# 前端开发模式（改前端时用，需 Node 22+；3000 端口，/services 代理到 localhost:6580）
cd frontend && npm ci && npm run dev
# 改完前端后同步产物进 jar：scripts/build-frontend.sh，再 mvn package
```

* 文档站：[docs/architecture.md](docs/architecture.md)（架构图）· [docs/operations.md](docs/operations.md)（运维手册）· GitHub Pages 自动部署（docs/ 内 VitePress）
* 模型策略包：导出/导入跨实例迁移与策略分享，[格式与操作](docs/model-package.md)；官方模板库一键创建场景风控模型（账户盗用/营销反作弊/交易风控）
* 插件开发：[开发指南](docs/plugin-dev-guide.md) · 示例工程 [radar-plugin-demo](samples/radar-plugin-demo)（本地形态）/[radar-plugin-remote-demo](samples/radar-plugin-remote-demo)（远程形态，v3.1）· [插件注册表](PLUGIN_REGISTRY.md)
* 插件管理：管理端顶栏「插件管理」（#/plugins）——上传 jar 热重载即时生效、启停持久化、执行统计；出访管控 `plugin.http.mode`（deny-private / allowlist / off）
* 可观测性：`/actuator/prometheus` 指标，`docker compose --profile monitoring` 一键起 Grafana 看板，见 [压测与监控](docs/benchmark-v2.2.md)
* 测试分层：L1/L2 纯单测 `mvn test` 秒级全绿，L3 Testcontainers 集成测试，见 [CONTRIBUTING.md](CONTRIBUTING.md)、[docs/plan-v2.1.md](docs/plan-v2.1.md)

## 架构

前后端分离。后端：Spring Boot 3.5 + MyBatis-Plus + MySQL + MongoDB + Redis + Groovy + ES + springdoc-openapi；
前端：React 18 + antd 5 + Vite（源码在 [frontend/](frontend/README.md)，构建产物内嵌 jar）。

> v2.0 起由 8 个 Maven 模块合并为单模块单体，持久层迁移至 MyBatis-Plus，
> [技术路线图](docs/roadmap.md)。

## 开源版 vs 企业版

Radar 同时提供开源版与企业版：开源版聚焦核心决策能力，免费开源（Mulan PSL v2）；
企业版面向互金/消费金融生产级场景，**包含开源版全部能力**，并额外提供：

* 决策流可视化编排（DAG）、特征库与实时特征计算
* 多租户隔离 + RBAC + API Key，满足 SaaS 化部署
* 业务监控大盘、SLA 告警与企业微信/邮件通知
* 模型版本管理与 A/B 测试
* 专业技术支持与 SLA 保障

完整能力矩阵与商务咨询请访问官网：<https://www.riskengine.cn>

## 文档与资源

| 资源 | 链接 |
| --- | --- |
| 官网 / 在线 Demo | <https://www.riskengine.cn> |
| 使用手册（推荐从这里入门） | <https://gitee.com/freshday/radar/wikis/manual> |
| Wiki 首页 | <https://gitee.com/freshday/radar/wikis/home> |
| Gitee（主仓，贡献代码请提交到这里） | <https://gitee.com/freshday/radar> |
| GitHub（镜像） | <https://github.com/wfh45678/radar> |

## 致谢与赞助

感恩 XWF 团队的每一位小伙伴：
千面怪, 烈日下的从容, DerekDingLu, king, sanying2012, 紫泉夜, 玄梦, 成书平, 徐帅, 郭锐, 王成, 马兆永…

感谢赞助商对本项目的认可和支持：中和农信项目管理有限公司 · 二十六度数字科技(广州)有限公司

## 特别说明

* 未经授权，禁止使用本项目源码申请软著和专利，保留追究法律责任的权力！
* 如果喜欢本项目，欢迎 Star 支持，让更多人了解，谢谢！
* Copyright © 2019-2026 WangFeiHu
