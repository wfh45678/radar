# Radar v4.1 实施方案：特征工程重构（统一 Feature 体系）

> 状态：**已实施完成**（随 v4.1.0 发布）。本文档为方案存档。

## 1. 背景与定位

预处理（PreItem/插件加工）与抽象（Abstraction/统计聚合）本质都是特征工程，但 v3.x 中是两套完全独立的
模型/服务/API/页面，且扩展点弱：聚合是 int 常量 + 引擎内联 switch-case（4/10 种全量拉内存、
AVERAGE 空数据集 NPE、UI 仅暴露 6/10 种）、PreItem 无法链式、新特征类型无处安放。

v4.1（破坏性版本，用户已确认）仿照 v4.0 策略重构范式，把两层统一为**特征（Feature）**体系：

- 首批四种类型：**ENRICH 插件加工**（原 PreItem）、**AGGREGATE 统计聚合**（原 Abstraction）、
  **DERIVED 衍生表达式**（新）、**RATIO 组合比率**（新）；
- 架构留扩展点：后续类型加 Executor 即可；
- radar-plugin-api SDK 升 **4.0.0**（破坏性：FeatureProcessor 主契约取代 Plugin，存量插件改代码重发；
  引擎保留 3.x 旧契约 deprecated 桥作为迁移期，v5 移除）。

## 2. 现状盘点（2026-11-28 代码实证）

- `AggregateType` 是 int 常量类；switch-case 内联在 `AntiFraudEngineImpl`（10 分支 × 8 参数）；
- `AggregateCommandImpl`：COUNT 过滤 status='1' 而 DISTINCT_COUNT/数值族不过滤（口径不一致）；
  AVERAGE 空数据集 NPE；VARIANCE 空集 ArithmeticException；MEDIAN/VARIANCE/SD 全量拉内存；
- PreItem：sourceField 只能取事件原始字段（不可链式）；多列插件输出 `preItems.<destField>.<col>`；
- 抽象过滤条件为前端生成的 Groovy 脚本（`AbstractionCheckScript`）——后端 v4.0 已具备条件树
  解释器 `ConditionEvaluator`，脚本通路可以退役；
- 契约面（探索实证）：`features`/`strategies` 键、列前缀 preItems./abstractions.、策略包
  preItems/abstractions 数组、Redis 双频道、`/preitem`+`/abstraction` 双 API。
- 遗留 bug：引擎写 ES 键 `activations` 与报表/导出读键 `strategies` 长期失配。

## 3. 技术决策（已确认）

| 决策点 | 结论 | 理由 |
|--------|------|------|
| 版本 | v4.1.0（破坏性） | 用户确认"允许破坏性变更"+ SDK 升 4.0 |
| 概念 | PreItem+Abstraction 统一为 Feature（四类型） | 同质域统一，策略/评分卡/决策树经 features.xxx 消费 |
| 表结构 | engine_feature 统一表（FEATURE_NAME/TYPE/ORDER_NO/DEFINITION/OUTPUT） | 类型化 JSON 承载差异；ORDER_NO 承载链式顺序 |
| 执行 | FeatureExecutor SPI + Registry（对齐 v4.0 StrategyExecutor）；两阶段管线（ENTRY=ENRICH/DERIVED 落库前；ANALYSIS=AGGREGATE/RATIO 落库后） | 阶段间隐式依赖（加工产物随事件落库，供聚合检索）；阶段内按 ORDER_NO 链式 |
| 聚合 | AggregateKind 枚举 + AggregateFunction SPI 注册表（10 实现迁移） | 替换 switch-case；修复 AVERAGE NPE/VARIANCE 空集；口径统一 status='1' |
| 过滤 | 抽象 Groovy 脚本退役 → ConditionEvaluator 条件树解释 | 复用 v4.0 资产；前端不再为特征生成脚本 |
| 派生 | DerivedExecutor + ExpressionEvaluator（AST JSON）+ DerivedFunction SPI（13 内置函数） | 函数可注册扩展 |
| SDK | radar-plugin-api 4.0.0：FeatureProcessor/@FeatureMeta/FeatureRequest(+features 链式视图)/FeatureContext/FeatureResponse；3.x Plugin 系列保留 @Deprecated 桥 | 用户确认 breaking；PluginManager 双契约加载（v4 主 + v3 legacy 告警，v5 移除） |
| 契约迁移 | 引擎响应/ES：features（合并 preItems+abstractions）、strategies（修复 activations 失配）；列前缀统一 features.（升级 SQL 搬迁 + resolveColumn alias 兼容）；Redis 频道合并；/feature API 取代 /preitem+/abstraction | 用户确认破坏性；搬迁以引号/路径锚定 REPLACE 防误伤 |

## 4. 实施内容

- **F1 数据模型**：FeaturePO/VO/Service/Controller + `sql/upgrade-4.1.0.sql`（建表/两表搬迁/前缀
  REPLACE/旧表 DROP）+ radar-init.sql 种子重写（73 条特征：57 AGGREGATE + 16 ENRICH）；
- **F2 执行 SPI**：FeatureType/FeatureStage/FeatureExecutor/FeatureOutcome + Registry +
  EnrichExecutor/AggregateExecutor 抽取；编排层单特征异常隔离 + `radar_feature_executions_total`；
- **F3 聚合注册表**：AggregateKind 枚举 + AggregateFunction SPI + 10 实现（含 NPE/空集修复、
  口径统一）+ AggregateFunctionRegistry；
- **F4 衍生/比率**：DerivedFunction SPI + 13 内置函数 + ExpressionEvaluator（AST 递归求值）+
  DerivedExecutor + RatioExecutor（除零策略 ZERO/NULL）+ resolveColumn 多级点路径嵌套；
- **F5 SDK 4.0**：radar-plugin-api 4.0.0（新主契约 + legacy 桥）+ testkit 4.0.0 +
  PluginManager 双契约（FeatureProcessor 主 + Plugin legacy 告警桥）+ 内置 8 插件/两示例工程迁移；
- **F6 前端**：特征工程统一列表页（类型徽标/阶段列/ORDER 排序）+ FeatureModal 四类型编辑
  （ENRICH 动态表单/AGGREGATE 全 10 种聚合 + 条件树/DERIVED 操作数构建/RATIO 表单）+
  Model.jsx 5 Tab→4 Tab + 变量树 features 分组；
- **F7 配套**：EngineFullChainTest 断言迁移（strategies/features）+ e2e feature-crud.spec；
- **F8 文档**：plan-v4.1/upgrade-guide v4.0→v4.1/CHANGELOG/architecture/plugin-dev-guide/
  model-package/roadmap/README。

## 5. 风险与对策

| 风险 | 对策 |
|------|------|
| 前缀搬迁误伤 | 引号/路径锚定 REPLACE；指南附校验 SQL |
| 特征名重复 | 不设唯一约束；指南附检测 SQL |
| 统计口径变化 | 升级指南明确标注 |
| 存量 3.x 插件 | legacy 桥加载 + 告警（v5 移除）；plugin-dev-guide 迁移表 |
| Grafana 看板/ES 存量 | 指南注明 stage 标签与 ES 键变更；ES 存量文档不动 |

## 6. 验收标准（已达成）

1. `mvn test` 68 例全绿；testkit/samples 构建测试全过（CI 补跑 L3）；
2. 前端 `npm run build` 通过；
3. v4.0 包导入自动转换特征形态（PackageV1Converter，TemplateLibraryTest 回归）；
4. 三类策略经 features.xxx 消费特征（ConditionEvaluatorTest/ScoreCard/DecisionTree 回归）。
