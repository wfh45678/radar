# Radar v4.0 实施方案：策略板块重构（多策略类型：规则集 / 评分卡 / 决策树）

> 状态：**已实施完成**（2026-11-27，随 v4.0.0 发布）。本文档为方案存档。

## 1. 背景与定位

README 与 docs 早已宣示"六大策略类型"，但代码只实现了规则一种：
`AntiFraudEngineImpl` 硬编码"遍历激活器 → Groovy 执行规则 → 求和 → 阈值决策"。
v4.0 是破坏性大版本（用户决策允许 Breaking Changes），把这一层重构为可扩展的
**策略（Strategy）**体系：

- "激活器（Activation）"更名"策略（Strategy）"——代码词汇与产品术语（策略类型）对齐；
- 首批三种类型：**规则集 RULE_SET**（原行为）、**评分卡 SCORECARD**、**决策树 DECISION_TREE**；
- 架构留扩展点：决策表/名单匹配/外部模型后续各加一个执行器即可。

## 2. 现状盘点（2026-11-27 代码实证）

- `executeStrategy` 前身 `executeActivation` 是"规则集"语义的单点（无类型分发）；
- `engine_activation` 无 `type` 字段；条件操作符只存在于前端 `operatorUtil.jsx`（后端零定义）；
- `ModelGraphAssembler` 整 PO 插入 → 策略表新增字段自动随策略包/复制/模板流转；
- Redis pub/sub 已有 activation 通道，策略保存即生效链路现成；
- `PluginManager` 注册表范式（key 分发 + Spring bean 收集）可直接仿照。

## 3. 技术决策（已确认）

| 决策点 | 结论 | 理由 |
|--------|------|------|
| 版本号 | v4.0.0（破坏性大版本） | 表/类/API 路径重命名 + 数据结构演进，用户拍板"定义重构，版本直接定义为 v4.0" |
| 命名 | 彻底重命名 Strategy（表/类/API/前端路由） | 拖到以后要破两次；引擎响应 JSON 键经 @JsonProperty 锚定保持不变 |
| 类型承载 | `engine_strategy.TYPE` + `DEFINITION`（JSON 文本） | 决策树/评分卡是结构化定义，JSON 自然；规则集明细继续存 engine_rule；整 PO 流转零装配改动 |
| 执行方式 | 新类型走**后端解释器**（ConditionEvaluator），规则集保持 Groovy | 后端拿不到语义的旧债（操作符只在前端）就此补上；命中明细可解释、可单测；规则集行为零变化 |
| 策略包格式 | **完全不变**（信封键/条目键保持 v3.x 原名，@JsonProperty 锚定；formatVersion 保持 1.0） | 新旧双向可导入零风险；bump 版本会拒绝所有存量包 |
| 评分卡语义 | 总分 = initialScore + Σ(首个命中分箱分 × 权重)；阈值决策沿用 median/high | 与规则集心智一致 |
| 决策树语义 | 叶节点直出决策（pass/review/reject + score + reason），不走阈值 | 决策树的定义即如此；命中明细记录访问路径 |
| 前端编辑器 | 条件复用 ComplexCondition/SimpleCondition；决策树为"antd Tree 预览 + 节点面板"（画布版列后续） | class 组件约束下成本最低的可用形态 |

## 4. 实施内容

### 4.1 M1：Activation → Strategy 全量重命名
- Java 类/表/列（`sql/upgrade-4.0.0.sql`）、API 路径 `/services/v1/strategy`、前端目录/路由；
- JSON 契约锚定：`AntiFraudProcessResult.activations`、策略包 `activations`/`activationName`/`activationId`。

### 4.2 M2：策略类型 SPI
- `enums/StrategyType`、`service/engine/strategy/StrategyExecutor|StrategyOutcome`、
  `StrategyExecutorRegistry`（仿 PluginManager）；
- `RuleSetExecutor`：原规则循环原样抽取（Groovy check + 计分 + 阈值）；
- `AntiFraudEngineImpl` 保留编排层：遍历 ACTIVE 策略 → 按 type 分发 → 单策略异常隔离 → 决策指标。

### 4.3 M3/M4：新类型执行器
- `ConditionEvaluator`：PDCT/SMPL + 20 操作符后端求值（对齐前端 groovyUtil 语义；
  名单缺失 InList=false/NotInList=true，取值缺失排序类 false、NotEqual 视为命中）；
- `ScoreCardExecutor` / `DecisionTreeExecutor`（环防护、脏数据校验、feature_*/node_* 明细）。

### 4.4 M5/M6：前端
- 策略列表类型徽标 + 新建弹窗类型选择；详情路由 `#/strategyDetail/:id/:strategyId` 按 type 分发；
- `ScorecardEditor`（特征卡片 × 分箱表格，变量级联复用 feature/columns，InList 分箱走名单下拉）；
- `DecisionTreeEditor`（antd Tree 结构预览 + 节点编辑面板，条件复用条件树组件，保存前
  单根/无环/分支完整/可达性校验）；
- 两编辑器均带「变更历史」弹窗。

### 4.5 M7：配套
- 策略定义历史：`engine_strategy_history` + `GET /strategy/history/{id}`；
- 策略包：`type`/`definition` 随包流转（格式不变，旧包缺 type 按 RULE_SET）；
- transaction-risk 模板新增评分卡（3 特征 9 分箱）+ 决策树（7 节点）示例；
- 指标 `radar_strategy_executions_total{type,result}`；
- 文档：README（中/英）、architecture、model-package、upgrade-guide、roadmap、本方案、CHANGELOG；
- 测试：L1 三执行器/求值器 23 例单测；L3 `EngineFullChainTest.step4` 三类型全链路；e2e `strategy-types.spec.ts`。

## 5. 风险与对策

| 风险 | 对策 |
|------|------|
| 重命名漏改 | 编译期兜底（类型系统全捕获）+ `mvn test` 全绿后进入下一阶段 |
| JSON 契约误伤 | `@JsonProperty` 显式锚定 + ModelPackageTest/TemplateLibraryTest 回归 |
| ConditionEvaluator 与前端语义漂移 | 单测逐操作符对齐 groovyUtil 生成语义；差异点（名单缺失/取值缺失）在 javadoc 显式声明 |
| 存量数据 | TYPE 默认 RULE_SET + `StrategyType.of` 空值兜底；upgrade SQL 可重复执行（IF NOT EXISTS） |
| 决策树死循环 | 执行期步数上限（节点数+1）+ 编辑器保存前无环校验 |

## 6. 验收标准（已达成）

1. `mvn test` 60 例全绿；`mvn verify`（L3，需 Docker）含 `EngineFullChainTest.step4`；
2. 前端 `npm run build` 通过；e2e CI 覆盖三种类型 UI 流；
3. 模板库 transaction-risk 一键创建 → 三种策略类型可编辑、保存即生效、事件命中明细正确；
4. v3.x 策略包导入 v4.0 全图无损；引擎决策接口响应结构与升级前一致。
