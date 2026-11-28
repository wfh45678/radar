# Radar 升级指南：v1.x → v3.x → v4.x → v5.x

> 面向仍在运行 v1.x（2020-2021 年版本）或 v2.x/v3.x/v4.x 的存量用户。
> v3.x 不是原地升级，而是一次"底座重铺、模型延续"的现代化——这份指南说清楚：什么变了、什么没变、怎么迁。

## v4.x → v5.0（场景化更名，破坏性大版本）

"模型（model）"更名为"**场景（scenario）**"——它本来就是场景级决策配置；"模型"一词自 v5.0 起
专指**算法模型**（评分卡/ONNX 模型，休眠配置 `engine_model_conf` 保留原名即为此用途）。

**升级路径 = 重新安装 + 配置按术语对照表重建**：v5.0 不提供数据迁移脚本、不兼容旧 API 参数与
旧格式策略包（1.0/2.0 不可导入）——存量用户均为二次开发用户，请按对照表重建配置并调整
事件接入参数。

### 术语对照表

| v4.x | v5.0 | 说明 |
|------|------|------|
| 模型 | **场景（scenario）** | 用户可见术语/文档/界面 |
| engine_model | engine_scenario | DB 表 |
| 子表 MODEL_ID 列 | SCENARIO_ID | field/feature/strategy/rule/rule_history/data_lists 等八张子表 |
| modelGuid | scenarioGuid | 引擎接口 `/uploadInfo`、`/getScore` 参数及管理端 API |
| `/services/v1/model/**` | `/services/v1/scenario/**` | 管理端 API 基路径 |
| 模型策略包 | 场景策略包 | formatVersion **3.0**（信封键 `model`→`scenario`，正名 `strategyId`/`featureName`/`strategyName`） |
| Plugin v3 契约插件 | **不再支持** | PluginManager legacy 桥移除，必须 FeatureProcessor（radar-plugin-api 4.x） |
| 算法模型配置 engine_model_conf(+param) | **保留原名** | "模型"一词的正当归属（休眠，P1-5 回归本体） |

### 必做步骤

1. 全新部署：清库导入 `sql/radar-init.sql`（v5 基线；历史 `upgrade-*.sql` 已随 v5.0 移除）；
2. 配置重建：按对照表在管理端重建字段/特征/名单/策略（或用 v4.1.5 导出包按对照表手工调整后——
   注意 1.0/2.0 包 v5 不识别，需调整为 3.0 键名）；
3. 事件接入改造：`modelGuid` → `scenarioGuid`（对接代码一行改动）；
4. 插件：全部使用 FeatureProcessor 契约（v3 契约插件不再装载）；
5. 官方模板：模板库一键创建（在线源/内置均为 3.0 格式）。

## v4.0 → v4.1.0（特征工程重构，破坏性）

预处理（PreItem）与抽象（Abstraction）统一为**特征（Feature）**体系（ENRICH/AGGREGATE/DERIVED/RATIO），
插件 SDK 升 radar-plugin-api **4.0.0**。详见 [plan-v4.1.md](plan-v4.1.md)。

### 必做步骤

1. **备份**后执行 `sql/upgrade-4.1.0.sql`：
   - 建 engine_feature 并自动搬迁 engine_pre_item（→ENRICH）与 engine_abstraction（→AGGREGATE）；
   - 列前缀搬迁：规则脚本/条件 JSON/打分字段/策略 DEFINITION 中的 `preItems.`/`abstractions.` → `features.`；
   - DROP 旧表（脚本不可重入，失败请从备份恢复重跑）。
   - 升级前建议执行脚本头部注释中的**特征名重复检测 SQL**（历史无唯一约束）。
2. 前端随 v4.1 安装包发布（Tab 合并为「特征工程」，路由 /preitem、/abstractionList 移除，新增 /feature）。
3. 直连管理端 API 的自建脚本：`/services/v1/preitem`+`/abstraction` → `/services/v1/feature`。

### BREAKING 变更对照

| 项 | v4.0 | v4.1 | 对策 |
|----|------|------|------|
| 引擎响应/ES 特征键 | `preItems` + `abstractions` | `features`（合并） | 业务调用方读取侧改键；ES 存量文档不动 |
| 引擎响应/ES 决策键 | `activations` | `strategies` | 修复历史失配；调用方改键 |
| 列引用前缀 | `preItems.x`/`abstractions.x` | `features.x` | DB 已自动搬迁；策略包经 alias 兼容 |
| 插件 SDK | com.radar.plugin.api.Plugin（3.x） | FeatureProcessor（4.0，主契约） | 存量插件改类名/注解/类型后重发；引擎保留 legacy 桥（加载时告警，v5 移除） |
| 插件链式 | 不支持 | FeatureRequest.getFeatures() 只读视图 | 见 plugin-dev-guide |
| Redis 频道 | radar_channel_preitem + abstraction | radar_channel_feature | 多副本必须同时升级 |
| Prometheus stage | preItems/abstractions | features + radar_feature_executions_total{type} | Grafana 看板更新 |
| 统计口径 | 仅 COUNT 过滤 status='1' | 全部聚合过滤 status='1' | 数值可能有变化（人工改过状态的事件不再计入） |

### 不变（兼容承诺）

- 策略包 v4.0（formatVersion 1.0）可直接导入 v4.1：preItems/abstractions 自动转换为特征形态；
  v4.0 策略 DEFINITION 中旧列前缀由条件求值器 alias 透明兼容；
- 条件求值/评分卡/决策树语义不变（除列前缀）；
- 插件 HTTP 出访管控、失败隔离（__plugin_error__ 标记）、HttpTool/缓存契约不变；
- Mongo 事件流水结构不变（特征产出仍 flatten 裸名落库，聚合查询不受影响）。

### 升级后验收清单

1. upgrade SQL 执行无报错；`SELECT TYPE, COUNT(*) FROM engine_feature GROUP BY TYPE` 行数与原两表一致；
2. 登录管理端 → 模型工作区「特征工程」页正常列出存量特征（类型徽标：插件加工/统计聚合）；
3. 上传测试事件 → 决策结果与升级前一致（分数/命中明细对比）；
4. 新建一个「衍生表达式」或「组合比率」特征 → 事件命中 → 特征值出现在响应 features 中；
5. `/actuator/prometheus` 出现 `radar_feature_executions_total{type,result}` 序列。

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
| v3.2.0 | 2026-11-21 | 场景策略包：全图导出/导入 + 校验报告 |
| v3.3.0 | 2026-11-22 | 开源基建：Dependabot/CodeQL + 文档站 + Playwright E2E |
| v3.4.0 | 2026-11-23 | 官方模板库：三场景模板 + 在线源（一键创建） |
| v3.5.0 | 2026-11-23 | 官方种子插件三件套 + E2E 扩面 |
| v4.0.0 | 2026-11-27 | 策略板块重构：激活器更名策略 + 类型化（规则集/评分卡/决策树），见下方 v3.x → v4.0 章节 |
| v4.1.0 | 2026-11-28 | 特征工程重构：PreItem+抽象统一特征（Feature 四类型）+ 插件 SDK 4.0，见顶部 v4.0 → v4.1 章节 |

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

- 文档站：见仓库 [docs/](.) ；插件开发：[plugin-dev-guide.md](plugin-dev-guide.md)；策略包格式：[scenario-package.md](scenario-package.md)
- 问题反馈：GitHub / Gitee Issues（附策略包 json 可加速定位——这是 v3.2 的设计初衷之一）
