# 架构总览

Radar 是单体应用（一个 jar 同时提供管理端与引擎端 API），Java 17 + Spring Boot 3.5 + MyBatis-Plus，
存储 MySQL（配置）+ MongoDB（事件流水）+ Redis（缓存/配置推送）+ Elasticsearch（事件检索）。

## 事件处理流

一笔事件从接入到决策的完整路径（毫秒级，各阶段耗时已暴露 `/actuator/prometheus`）：

```mermaid
flowchart LR
    A[应用方\nSDK/HTTP/Kafka] -->|uploadInfo| B[事件接入\nRiskAnalysisEngine]
    B --> C[ENRICH 特征\n插件加工·链式\n失败隔离]
    C --> D[DERIVED 衍生\n13 内置函数]
    D --> E[落库\nMongo 流水 + ES 检索]
    E --> F[AGGREGATE 聚合\n频次/累计/时间窗]
    F --> G[RATIO 组合\n比率/差额]
    G --> H[Strategy 策略执行\n按 type 分发 Executor]
    H --> I[决策输出\npass/review/reject\n+ 命中明细]
    H --> R[响应\nscore + features + strategies]
```

要点：

- **特征工程（v4.1，两阶段管线）**：`ENRICH/DERIVED` 在落库前执行（插件加工支持链式——后序特征可
  引用前序产出；衍生表达式 13 内置函数），`AGGREGATE/RATIO` 在落库后执行（统计聚合 10 种 +
  两特征比率/差额）；**单特征异常隔离**（错误标记 + 指标，事件链路继续）。ENRICH 插件以外置
  jar 形态自助接入（SDK 4.0 FeatureProcessor 契约），见[插件开发指南](./plugin-dev-guide.md)；
- **策略类型执行器（v4.0，v5.1 补全五类）**：引擎编排层遍历 ACTIVE 策略，按 `engine_strategy.TYPE` 分发到
  `StrategyExecutorRegistry`（范式对齐 PluginManager）。五类型：**规则集**（RULE_SET，Groovy
  条件求和 + 阈值决策，明细存 engine_rule）、**评分卡**（SCORECARD，DEFINITION JSON 特征分箱
  加权求和 + 阈值决策）、**决策树**（DECISION_TREE，DEFINITION JSON 路径走查、叶节点直出决策）、
  **决策表**（DECISION_TABLE，条件矩阵行优先首中直出）、**名单匹配**（LIST_MATCH，数据名单比对直出）。
  扩展新类型 = 新增 StrategyExecutor bean + 枚举值，编排层零改动；单策略执行异常按策略隔离；
- **规则 Groovy 脚本 / 评分卡 / 决策树 / 决策表 / 名单匹配**管理端编辑保存即生效（Redis pub/sub 推送，无需重启）；
- 引擎对外契约：顶层 `features`（特征产出）+ `strategies`（决策）+ `hitsDetail`（按策略分组的
  命中明细——规则集为规则粒度、评分卡为 feature_* 分箱、决策树为 node_* 路径）；
- 各阶段耗时指标：`radar_engine_stage_seconds{stage}`、插件级 `radar_plugin_seconds{plugin}`、
  策略级 `radar_strategy_executions_total{type,result}`。

## 部署形态

单体为默认形态；v3.1 起管理端可完成插件全生命周期运维，场景可导出策略包迁移。

```mermaid
flowchart TB
    subgraph 默认单体
        APP1[radar.jar\n管理端 + 引擎端]
        APP1 --> DB[(MySQL 配置)]
        APP1 --> MQ[(Mongo 流水)]
        APP1 --> RD[(Redis)]
        APP1 --> ES[(Elasticsearch)]
    end
```

- **一键环境**：`docker compose up -d`（中间件 + 种子数据），`--profile app` 加应用镜像，
  `--profile monitoring` 加 Prometheus/Grafana 看板（见[运维手册](./operations.md)）；
- **水平扩展**：引擎端无状态（配置经 Redis pub/sub 推送），可直接多副本；
  引擎/管理端分离部署 profile 在 [Roadmap](./roadmap.md) P1-6 规划中；
- **插件分发**：`plugins/` 目录外置 jar（管理端上传热重载）；
- **策略分发**：场景导出策略包 JSON，任意实例导入为新场景（见[场景策略包](./scenario-package.md)）。

## 代码地图

```
src/main/java/com/pgmmers/radar
  ├ controller/admin   管理端 API（场景/字段/规则/插件/策略包…）
  ├ service/impl/engine   引擎核心（feature 两阶段管线/strategy 执行链）
  ├ service/impl/model   场景域（历史包名沿用，含 transfer 场景策略包）
  ├ service/impl/dnn     Estimator 容器（ML 预留，零实现）
  └ common/util          Groovy/Crypt 等工具
radar-plugin-api / radar-plugin-testkit   插件 SDK（独立工程，已上 Maven Central）
frontend/               React 18 + antd 5 管理端（产物内嵌 jar）
```
