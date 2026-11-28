# Radar v4.2 实施方案：策略类型补全（决策表/名单匹配）+ v4 收口

> 版本：v1.0（2026-11-28）
> 范围：新增决策表（DECISION_TABLE）与名单匹配（LIST_MATCH）两个策略类型执行器 + `engine_rule.ABSTRACTION_NAME`
> 列改名（v4 遗留命名债）+ 前端死代码目录清理 + 工程配套（JSX 扫描进 CI、copy/导入路径测试补位）
> 状态：方案评审稿
> 前置：v4.1.5 已发布（策略类型 SPI 就绪）；roadmap P2-9 后续立项（v4.2）

---

## 1. 背景与定位

v4.0 把策略做成类型化 SPI（StrategyExecutor + Registry），首批落地规则集/评分卡/决策树三类；
v4.1 统一特征工程后，特征层（四类型）与策略层（类型化）的双层架构就位。遗留三件事：

1. **策略类型不全**：README/官网宣示的"决策表/名单匹配"仍是"规划中"（README 已如实标注）——
   补齐后"多策略类型"叙事完全成立，决策表也是企业版对比表上的既有卖点，开源版补上可消除认知差；
2. **列名债**：`engine_rule.ABSTRACTION_NAME`（含 engine_rule_history）内容已随 v4.1 迁移为
   `features.*`，仅列名是历史名；
3. **前端死代码**：`component/preItem/`（v4.1 后无路由引用）、`component/abstraction/` 内
   Abstraction.jsx/AbstractionList.jsx 同样死代码（ComplexCondition 仍被 4 处引用需保留/迁移）。

v4.2 = 策略类型补全（二类）+ v4 收口（命名/死代码）+ 工程配套，minor bump（新增类型）。

## 2. 现状盘点（2026-11-28 实证）

| 项 | 现状 | 证据 |
|----|------|------|
| 策略类型枚举 | StrategyType：RULE_SET / SCORECARD / DECISION_TREE 三值 | enums/StrategyType.java |
| 执行器 SPI | StrategyExecutor + Registry，编排层按 `engine_strategy.TYPE` 分发，新增类型零改动编排层 | service/engine/strategy/ |
| 评分卡/决策树前端 | ScorecardEditor / DecisionTreeEditor + StrategyDetail 类型分发路由 + strategyTypes.jsx 类型常量 | component/strategy/ |
| 名单基建 | engine_data_lists + engine_data_list_records + DataListType（黑/白名单）齐备，黑/白名单管理页在位 | component/datalist/ |
| 列名债 | engine_rule(370 行 SQL 处)/engine_rule_history 均为 `ABSTRACTION_NAME`，内容已是 features.*（v4.1 迁移改写）；RulePO/RuleVO/RuleHistoryPO 字段名 abstractionName | sql/radar-init.sql:366,402 |
| 死代码 | component/preItem/ 全目录 + component/abstraction/{Abstraction,AbstractionList}.jsx 无路由引用（构建已 tree-shake）；ComplexCondition.jsx 被 4 处 import | component/ 目录 |
| 工程缺口 | copy/导入路径仅 verify-chain（CI 外）覆盖；JSX 引用扫描未进 CI（v4.1 连环 ReferenceError 的教训） | scripts/verify-chain.sh |

## 3. 技术决策（待评审确认）

| 决策 | 选择 | 理由 |
|------|------|------|
| 决策表语义 | **行优先首中直出**：DEFINITION = 条件列定义（2~4 个特征变量各带分箱/区间条件）+ 决策行（每行 = 各条件列命中组合 → decision/score/reason）；执行器按行序首个全命中行直出决策（pass/review/reject） | 决策表的传统语义（条件组合矩阵）；首中行简单确定、与决策树叶节点决策同构，复用 ConditionEvaluator 求值 |
| 名单匹配语义 | **DEFINITION = 名单选择（数据名单 id）+ 匹配字段（事件字段）+ 命中决策/分数/理由**；执行器查名单记录（复用黑白名单存储），命中字段值直出决策；支持白名单放行（PASS）与黑名单拦截语义 | 名单基建现成；把"名单匹配"从 feature（名单管理页）升格为可编排策略类型 |
| 列改名范围 | **DB 列 + Java 字段全改名**（ABSTRACTION_NAME → FEATURE_NAME，abstractionName → featureName）；**策略包 JSON 键保持 `abstractionName` 不动**（2.0 格式刚发布不折腾，@JsonProperty 兼容名继续承担包体绑定） | 内部彻底、外部契约稳定；升级脚本一行 ALTER |
| 包格式 | 不升版本（2.0 内部含义微调：rules[].abstractionName 语义即"规则阈值特征变量"） | 避免 2.0→2.1 的兼容矩阵膨胀 |
| 死代码处理 | 删 component/preItem/ 全目录 + component/abstraction/{Abstraction,AbstractionList}.jsx；ComplexCondition.jsx 迁至 component/common/（改 4 处 import） | 名实归位；ComplexCondition 是活的共享组件不该住在 abstraction/ 里 |
| 兼容承诺 | API JSON 键 featureName 为 breaking（管理端前后端同步发布）；引擎决策接口不变；策略包 1.0/2.0 继续可导入 | minor 版本内管理端同步发，外部无消费方 |

## 4. 实施内容

### 4.1 T1：决策表执行器（后端）

- StrategyType 增 `DECISION_TABLE`；`DecisionTableExecutor`（DEFINITION 解析 + ConditionEvaluator
  逐行求值 + 首中行决策 + 命中明细 row_*）；`engine_strategy.TYPE` 注释同步

### 4.2 T2：名单匹配执行器（后端）

- StrategyType 增 `LIST_MATCH`；`ListMatchExecutor`（名单加载复用 DataListsService + 命中明细 list_*）；
  注意名单记录量级防护（与数据名单页一致的分页/上限语义）

### 4.3 T3：决策表编辑器（前端）

- DecisionTableEditor.jsx：条件列（特征选择 + 操作符 + 分箱值）+ 决策行网格（行内编辑决策/分数/理由），
  行增删；StrategyDetail 分发路由 + strategyTypes.jsx 增类型徽标

### 4.4 T4：名单匹配编辑器（前端）

- ListMatchEditor.jsx：名单选择（数据名单下拉）+ 匹配字段选择 + 命中决策/分数/理由表单；
  分发路由同上

### 4.5 T5：列改名 + 死代码清理

- `upgrade-4.2.0.sql`：engine_rule / engine_rule_history 列改名；radar-init.sql 同步
- RulePO/RuleHistoryPO/RuleVO/Service/Mapper 全链改名（@JsonProperty("abstractionName") 保持）
- 删 component/preItem/ 与两个死组件；ComplexCondition 迁 component/common/

### 4.6 T6：工程配套

- **CI 加 JSX 引用扫描**：扫描器落 scripts/scan-jsx.js（v4.1 连环 ReferenceError 的教训，
  esbuild 不查未定义标识符）；ci.yml 前端构建前执行
- **L3/e2e 补 copy/导入路径**：EngineFullChainTest 增 copyModel 断言（featureMapper NPE 的教训）；
  e2e 增策略包导入用例

### 4.7 T7：文档与发布

- docs：plugin/model-package/架构图/README 策略类型口径更新（决策表/名单匹配从"规划中"转正）
- CHANGELOG、主 pom 4.2.0、升级指南 v4.1→v4.2 章节、tag 发布（SDK 不发版，Central 幂等跳过）

## 5. 实施阶段

| 阶段 | 内容 | 产出 | 估计 |
|------|------|------|------|
| S0 | 方案评审 + 决策表/名单匹配 DEFINITION 结构定案 | 定稿 | 0.5 人日 |
| S1 | 两执行器 + 枚举 + 注册（L1 单测对齐三类型范式） | 后端就绪 | 2 人日 |
| S2 | 两编辑器 + 分发路由 + 类型徽标 | 管理端闭环 | 2.5 人日 |
| S3 | 列改名 + 迁移脚本 + init 同步 + 死代码清理 | v4 收口 | 1 人日 |
| S4 | CI 扫描 + L3/e2e 补用例 | 工程配套 | 1 人日 |
| S5 | 文档 + 发布准备 v4.2.0（真实迁移演练 + 十步链路 + 导入 round-trip 验收后打 tag） | 按发布规范 | 1 人日 |

**合计约 8 人日**。S1/S2 并行推进，S3 独立可先行。

## 6. 风险与对策

| 风险 | 对策 |
|------|------|
| 决策表 DEFINITION 结构设计反复 | S0 与评分卡/决策树对齐评审冻结；执行器只认 DEFINITION，UI 迭代不动后端 |
| 名单记录量大拖慢匹配 | 名单匹配走集合查询（与黑白名单页同源），量级上限沿用数据名单页语义；必要时内存集合单次加载 |
| 列改名漏改点 | 改名按"DB 列→PO→VO→Service→前端"链路清单逐项过 + 全库 grep 复核（沿用 featureMapper 教训的扫描器口径） |
| 前端编辑器复杂度（决策表网格） | 行内编辑沿用 ScorecardEditor 的分箱交互范式，不引入新表格组件 |
| e2e/CI 对新增类型的断言遗漏 | L3 全链路测试增三类型→五类型断言；e2e 策略类型 spec 增两类型创建 |

## 7. 验收标准

1. 新增策略可选五类型；评分卡/决策树/决策表/名单匹配四编辑器保存 → 引擎按类型分发执行 → 命中明细（feature_*/node_*/row_*/list_*）齐备
2. 决策表：多行条件组合按首中行直出决策（造事件可验证 pass/reject 两条路径）；名单匹配：命中/未命中名单两分支决策正确
3. `engine_rule` 列名 FEATURE_NAME；升级路径 upgrade-4.2.0.sql 对 v4.1.5 存量数据无损（内容不迁移仅改名）
4. component/preItem 与死组件删除后构建、路由、页面全绿（ComplexCondition 迁移后 4 处 import 全通）
5. CI 含 JSX 引用扫描步骤；L3 覆盖 copyModel 断言；e2e 含策略包导入用例
6. L1/L2 全绿 + 覆盖率门禁保持；v4.2.0 按发布规范（真实迁移演练 + 十步链路 + 验收后打 tag）
