# Radar v5.0 实施方案：场景化更名（"模型"→"场景"，彻底断历史包袱）

> 版本：v1.0（2026-11-28）
> 范围：术语更名（模型→场景，model→scenario）全量落地（DB/API/代码/前端/脚本/文档/模板）+
> 策略包格式升 3.0（信封键正名）+ Plugin v3 legacy 桥移除（兑现 v4.1 预告）+ **删除全部历史升级脚本（只留全新安装）**
> 状态：方案评审稿
> 前置：v4.1.5 已发布验收；**排期调整：本方案先行，原 v4.2 内容（决策表/名单匹配/死代码清理/CI 扫描）顺延 v5.1**（新术语上实施，避免做完再改名一遍）

---

## 1. 背景与定位

系统根概念"模型"实际是**场景级决策配置**（字段+特征+名单+策略的登录/交易/营销场景打包），
并非算法模型。风控行业标准叫法是"场景"；而"模型"应留给算法模型本体（评分卡/ONNX 模型，
roadmap P1-5 回归时的概念）。改名后 `engine_model_conf(+param)` 这张休眠的 ML 配置表
**保留原名不动**——它语义上就是"算法模型配置"，恰是 v5 术语下"模型"二字的正当归属，
层级一次理顺：**场景（scenario）挂算法模型（model）**。

**用户决策（2026-11-28 拍板）**：初期版本功能简单，存量用户基本均已二次开发，
**不做历史数据迁移、不做 API 兼容双读、不做旧格式包导入**——彻底断包袱，升级路径只有
"重新安装 + 配置按新术语重建"。本方案因此大幅简化（无迁移 SQL、无转换器、无 deprecated 层）。

## 2. 影响面（2026-11-28 实证）

| 影响面 | 规模 |
|--------|------|
| `*Model*.java` 文件 | 23 个（PO/VO/Controller/Service/Package/GraphAssembler…） |
| 引擎对外契约 `modelGuid` | 后端 30 处（MainController upload/getScore）+ 前端 4 文件 + verify-chain/e2e |
| 管理端 API 基路径 | `/services/v1/model/**`（模型/字段/特征/策略/策略包/名单全部挂在下面） |
| 前端 | 39 个文件（路由 #/model、组件 Model*、API 路径、参数、文案） |
| DB | engine_model + 8 张子表的 MODEL_ID 列；Redis 频道 radar_channel_model |
| 策略包 | 信封 `model`/`modelName` 键 + rules[].`activationId`/`abstractionName`（@JsonProperty 兼容名） |
| 插件 | PluginManager v3 legacy 桥（v4.1 预告"v5 移除"） |

## 3. 改名对照表（本方案核心资产）

| v4.x | v5.0 | 层 |
|------|------|-----|
| 模型 | **场景（scenario）** | 用户可见术语/文档/官网 |
| engine_model | engine_scenario | DB |
| 子表 MODEL_ID 列 | SCENARIO_ID | DB（field/feature/strategy/rule/rule_history/data_lists/data_list_meta/data_list_records） |
| modelGuid | scenarioGuid | 引擎 API（/uploadInfo、/getScore）+ 管理端 API + 前端 + 脚本 |
| /services/v1/model/** | /services/v1/scenario/** | 管理端 API |
| Model*（PO/VO/Controller/Service/Package/GraphAssembler/List…） | Scenario* | 代码 23 文件 |
| radar_channel_model | radar_channel_scenario | Redis |
| #/model/… 路由 | #/scenario/…（**不做旧 hash 兼容跳转**） | 前端 |
| 模型策略包 | 场景策略包 | 文档 |
| formatVersion 2.0 | **3.0**：信封 `model→scenario`、`modelName→scenarioName`；`rules[].activationId→strategyId`、`abstractionName→featureName`；`strategies[].activationName→strategyName` | 策略包格式 |
| Plugin v3 legacy 桥 | **移除**（插件必须 FeatureProcessor 4.x） | PluginManager |
| upgrade-3.1.0/4.0.0/4.1.0.sql | **删除**（sql/ 只留 radar-init.sql） | 升级路径 |

**明确不改**：
- `engine_model_conf(+param)` ——语义就是"算法模型配置"（休眠，P1-5 回归本体），model 二字正当；
- feature/strategy/插件术语（v4.1 已收敛）；
- Mongo `entity_<id>` 集合 / ES 索引（纯 id 命名，无 model 字样，零影响）；
- radar-plugin-api（独立 SDK 4.x 线，坐标与契约不变）。

## 4. 实施内容

### 4.1 T1：DB 与基线
- engine_model → engine_scenario；8 张子表 MODEL_ID → SCENARIO_ID；radar-init.sql 全量重写
- **删除 sql/upgrade-*.sql**（git 史可考）；升级说明改为"重新安装 + 术语对照表"

### 4.2 T2：后端更名与 API
- 23 个 Model* 文件更名 Scenario*（PO/VO/Mapper/Service/Controller/Package/GraphAssembler/Conf 参数除外）
- `@RequestMapping("/services/v1/model")` → `/scenario`；modelGuid → scenarioGuid（含 swagger 注解）
- Redis 频道改名；@JsonProperty 兼容注解清零（正名直出：strategyId/strategyName/featureName）

### 4.3 T3：策略包 3.0 + legacy 桥移除
- ModelPackage → ScenarioPackage，formatVersion "3.0"；parse **只认 3.0**（旧格式报"v5.0 起仅支持
  3.0 格式，请用 v4.1.5 导出后手工调整"）
- **删除 PackageV1Converter**；PluginManager 移除 v3 legacy 桥（加载到旧契约插件给出明确报错与迁移指引）
- 导出文件名前缀 radar-model-* → radar-scenario-*

### 4.4 T4：前端
- 路由 #/model → #/scenario；组件 Model.jsx/ModelList → Scenario.jsx/ScenarioList 等
- API 路径/参数名/文案（"模型管理"→"场景管理"）39 文件全量

### 4.5 T5：脚本与测试
- verify-chain.sh（modelGuid→scenarioGuid）、e2e 七 spec、L3 EngineFullChainTest 断言
- scan-jsx 扫描器落 scripts/scan-jsx.js + ci.yml 前端构建前执行（B 类待办顺带）

### 4.6 T6：模板与在线源
- 三内置模板重导出 formatVersion 3.0（catalog minRadarVersion → 5.0.0）
- **托管文件需再次更新**（用户侧）——未更新前在线源会因格式不支持被 v5 引擎明确拒绝并提示

### 4.7 T7：文档与发布
- README/docs 站（架构图/运维/包格式/插件指南）/升级指南（**术语对照表**：v3/v4 模型 = v5 场景，
  升级路径 = 重新安装 + 配置重建）全量；官网口径（用户侧）
- CHANGELOG [5.0.0]（breaking 清单完整罗列）、pom 5.0.0、**发版前真实全新安装演练 + 用户验收后打 tag**

## 5. 实施阶段

| 阶段 | 内容 | 产出 | 估计 |
|------|------|------|------|
| S0 | 方案评审 + 对照表冻结 | 定稿 | 0.5 人日 |
| S1 | DB/init 重写 + 删升级脚本 | 基线就绪 | 1 人日 |
| S2 | 后端 23 文件更名 + API/频道 + 注解清零 | 编译绿 + API 就绪 | 2 人日 |
| S3 | 包格式 3.0 + 桥移除 + 删转换器 | 契约就绪 | 1 人日 |
| S4 | 前端 39 文件 + 路由 + 文案 | 管理端闭环 | 1.5 人日 |
| S5 | 脚本/L3/e2e 对齐 + scan-jsx 进 CI | 工程配套 | 1 人日 |
| S6 | 模板重导出 3.0 + 文档全量 + 托管文件（用户侧） | 内容就绪 | 1 人日 |
| S7 | 全新安装演练（清库重装）+ 全链路 + 发版 v5.0.0 | 按发布规范 | 1 人日 |

**合计约 9 人日**。更名为机械性改动，风险集中在"遗漏"——以编译器 + 全库 grep 复核清单
（modelGuid/activationId/engine_model 零残留，白名单：engine_model_conf、术语对照表）兜底。

## 6. 风险与对策

| 风险 | 对策 |
|------|------|
| 更名遗漏导致运行时错误 | 编译器兜底 + grep 复核清单 + 全新安装演练全页面走查；featureMapper 式漏注解以注入扫描器复查 |
| 外部集成者 modelGuid 硬断 | 发布说明/README 快速开始显著标注（参数改名一行改动）；官网同步；这是用户拍板的取舍 |
| 在线源托管文件未同步（3.0 时差） | v5 引擎对旧格式**明确报错提示**（含迁移指引文案）；托管文件更新前模板货架不可用属预期，内置货架不受影响 |
| 社区 v4 用户升级成本 | 升级指南术语对照表 + "重新安装"唯一路径明示；不提供迁移工具是用户拍板的范围决策 |
| 策略包 3.0 无回退 | 不可变版本原则（Central/包格式同策略）；v4.1.5 长期可用作旧格式出口 |

## 7. 验收标准

1. 全新安装演练：清库 → radar-init.sql → 十步链路（scenarioGuid 版）全绿
2. 全库 grep 复核：`modelGuid`/`activationId`/`engine_model`（白名单外）零残留；
   `abstractionName`/`preItems`/`activations` 前端零残留
3. 五类型策略、特征工程、策略包导入导出（3.0 round-trip）、模板一键创建全功能回归绿
4. 插件加载仅 FeatureProcessor 契约；legacy 桥移除后 samples（已迁移）正常装载
5. 升级指南含术语对照表；README/文档站/官网口径统一"场景"
6. L1/L2 全绿 + 覆盖率门禁保持；v5.0.0 按发布规范（用户验收后打 tag）
