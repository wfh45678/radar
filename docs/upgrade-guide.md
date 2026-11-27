# Radar 升级指南：v1.x → v3.x → v4.0

> 面向仍在运行 v1.x（2020-2021 年版本）或 v2.x 早期版本的存量用户。
> v3.x 不是原地升级，而是一次"底座重铺、模型延续"的现代化——这份指南说清楚：什么变了、什么没变、怎么迁。

## v3.x → v4.0.0（策略板块重构，破坏性）

v4.0 把"激活器（Activation）"更名"策略（Strategy）"并类型化（规则集/评分卡/决策树），
详见 [plan-v4.0.md](plan-v4.0.md)。升级成本低（一个脚本 + 换前端），心智与数据全保留：

**必做两步**

1. 执行增量脚本 `sql/upgrade-4.0.0.sql`（表更名 `engine_activation`→`engine_strategy`、
   列更名、新增 `TYPE`/`DEFINITION` 列、新建策略历史表——存量策略 `TYPE` 默认 `RULE_SET`，
   行为与升级前完全一致）；
2. 前端随 v4.0 安装包内嵌发布，管理端 API 路径 `/services/v1/activation` → `/services/v1/strategy`
   （自建脚本/工具直连管理端 API 的需同步修改；管理端 UI 用户无感知除地址外）。

**不变（零改造承诺）**

| 项 | 说明 |
|----|------|
| 引擎决策接口 | `/services/v1/upload`、`/getScore` 响应 JSON 键 `activations`/`hitsDetail` **保持原名**（代码内部更名经 @JsonProperty 锚定），业务调用方与 ES 报表零改造 |
| 策略包 | JSON 键 `activations`/`activationName`/`activationId` 保持原名，v3.x 导出的包直接导入 v4.0；v4.0 新包条目新增 `type`/`definition` 字段（含评分卡/决策树的包导入旧引擎会丢策略，建议先升级） |
| 规则集行为 | 原 Groovy 执行逻辑原样抽取为 RuleSetExecutor，决策三段阈值、命中明细结构、rule_history 均不变 |
| 事件数据 | Mongo/ES 存量事件不动；context 键 `activations` 保持 |

**升级后验收**

1. `sql/upgrade-4.0.0.sql` 执行无报错；登录管理端 → 模型工作区"策略管理"页正常列出存量策略（类型徽标为"规则集"）；
2. 上传测试事件 → 决策结果与升级前一致（分数/命中明细对比）；
3. 新建一个"评分卡"或"决策树"策略（或从模板库导入 transaction-risk 模板体验三种类型）→ 保存 → 事件命中；
4. `/actuator/prometheus` 出现 `radar_strategy_executions_total{type,result}` 序列。

## 版本时间线

| 版本 | 日期 | 一句话 |
|------|------|--------|
| v1.0.4 ~ v1.0.8 | 2020-04 ~ 2021-03 | 老版本线：Java 8 + Spring Boot 2.7 + Aviator 表达式 + React16/antd3 前端 |
| （间隔） | 2021 ~ 2026 | 项目沉寂期 |
| v2.0.0 | 2026-11-14 | **底座重铺**：Java 21 + Boot 3.5 + Groovy 4（替代 Aviator）+ 删 dal 层换 MyBatis-Plus + 移除 TF DNN + DB 全量基线 `radar-init.sql`（MySQL 8.0） |
| v2.1.0 | 2026-11-14 | 工程基础：引擎测试 + CI + docker-compose + 发布规范 |
| v2.2.0 | 2026-11-14 | 专业度：虚拟线程 + Micrometer 指标 + 监控 profile + L3 Testcontainers |
| v2.4.x | 2026-11-14/15 | 前端现代化：React 18 + antd 5 + Vite（产物内嵌 jar，UI 无需单独部署） |
| v3.0.0 | 2026-11-15 | preItem 插件 SDK 化：radar-plugin-api + 外置 jar 加载 + 动态表单 |
| v3.1.0 | 2026-11-21 | 插件生命周期（上传/启停/热重载）+ 出访管控 + testkit |
| v3.2.0 | 2026-11-21 | 模型策略包：全图导出/导入 + 校验报告 |
| v3.3.0 | 2026-11-22 | 开源基建：Dependabot/CodeQL + 文档站 + Playwright E2E |
| v3.4.0 | 2026-11-23 | 官方模板库：三场景模板 + 在线源（一键创建） |
| v3.5.0 | 2026-11-23 | 官方种子插件三件套 + E2E 扩面 |
| v4.0.0 | 2026-11-27 | 策略板块重构：激活器更名策略 + 类型化（规则集/评分卡/决策树），见上方 v3.x → v4.0 章节 |

## 什么没变（心智可以全盘保留）

- **风控模型本身**：字段 / preItems / 数据名单 / 抽象 / 激活 / 规则（内嵌脚本）的模型组织方式完全延续；
- **Java 包名** `com.pgmmers` 与主工程坐标不变——二开代码的包结构认知仍然有效；
- **管理端 URL 风格**：扁平 hash 路由（`#/field/:id`、`#/event` 等）兼容保留；
- **四件套依赖**：MySQL / MongoDB / Redis / Elasticsearch（7.x）不变。

## 什么变了（v1.x 用户必读的 Breaking Changes）

| # | 变更 | 影响 | 对策 |
|---|------|------|------|
| 1 | **运行时 Java 8 → 21**（Boot 2.7 → 3.5，javax → jakarta） | 部署环境需 JDK 21（或直接用 GHCR/compose 容器） | 推荐容器部署，零环境迁移 |
| 2 | **规则脚本引擎 Aviator → Groovy 4** | **存量自定义规则/抽象脚本需改写**——这是迁移工作量的大头 | v2.4 时代的 115 个种子脚本已在 Groovy 4 下 100% 验证；自定义脚本按 Groovy 语法重写（表达力是超集，多数为机械改写） |
| 3 | **dal 层删除 + MyBatis-Plus** | 深度二开者：DAO/SQL 层结构变化 | 业务服务层 API 面基本保留；参考 v3 源码结构 |
| 4 | **TF DNN 移除**（Estimator 接口保留、零实现） | 用过 ML 评分的配置休眠；机器学习配置页已移除 | ML 将以 ONNX 路线重做（roadmap P1-5）；规则/抽象/名单链路不受影响 |
| 5 | **DB 结构大改** | 老库表结构与 v3 基线差异大，无原地 ALTER 路径 | 见下节"数据迁移" |
| 6 | **前端完全重写** | 老页面交互细节有差异；产物内嵌 jar，无需独立部署 | 新前端视觉与旧版对齐、功能全覆盖（含插件管理/模板库新增页） |
| 7 | **尾斜杠路由取消**（Spring 6 行为） | 直连老 API 的外部调用 `/model/` 等会 400 | 去掉尾斜杠；管理端 fetchUtil 已内置处理 |
| 8 | 虚拟线程默认开启、新增 `sys.conf.*` 配置 | 老配置文件需对照新增项 | 默认值向后兼容；验证码开关 `sys.conf.captcha-enabled` 默认开 |

## 数据迁移策略（诚实版）

- **全新部署优先**：v3 推荐路径 = 干净库导 `sql/radar-init.sql`（全量基线，含模板与种子模型）；
- **配置类数据**（模型/字段/规则/名单）：v1.x 无导出通道，需手工重建——**重建成本低**（管理端几分钟一个模型），且 v3.4 起可用官方模板库一键创建基线再调整；
- **事件/hits 历史数据**：老版本 ES/Mongo 数据可按需脚本搬迁，但风控决策链路不依赖历史事件，建议新库冷启动；
- **从 v3.2 起再无此类问题**：模型配置全图可导出/导入（策略包），环境迁移=文件拷贝。

## v2.x → v3.x（增量，轻松）

- v2.x 库只需补 `sql/upgrade-3.1.0.sql`（插件注册表，应用启动也会自动对账）；
- v3.2/3.3/3.4/3.5 **零 schema 变更**；
- 插件 SDK 向后兼容：v2 风格内置插件走 BUILTIN_V2 适配器渐进迁移，`type=""` 多列旧语义保留；
- 老自定义插件建议迁到 v3 `Plugin` 契约 + EXTERNAL jar 加载（参考 samples 与 [plugin-dev-guide.md](plugin-dev-guide.md)）。

## 升级后验收清单

1. `GET /actuator/health` → UP；
2. 登录管理端 → 模板库一键创建一个官方模板 → build → enable；
3. 上传一条测试事件，确认规则按预期命中（或跑 `scripts/verify-chain.sh` 十步全链路）；
4. swagger（admin/engine 分组）可访问；`/actuator/prometheus` 有 `radar_` 指标序列。

## 帮助

- 文档站：见仓库 [docs/](.) ；插件开发：[plugin-dev-guide.md](plugin-dev-guide.md)；策略包格式：[model-package.md](model-package.md)
- 问题反馈：GitHub / Gitee Issues（附策略包 json 可加速定位——这是 v3.2 的设计初衷之一）
