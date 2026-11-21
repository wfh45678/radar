# Radar v3.2 实施方案：模型导入导出（策略包与模板生态起点）

> 版本：v1.0（2026-11-21）
> 范围：模型配置全图的文件化导出与导入（策略包）、导入校验报告；engine_model_conf(+param) 为 ML 休眠配置不入包（随 P1-5 ML 重做再议）；官方模板库一键创建（拉伸目标）、覆盖式更新/合并不在本版
> 状态：方案评审稿
> 前置：v3.1 已发布（插件生态深化）；roadmap 见 [roadmap.md](roadmap.md)（P2-7）

---

## 1. 背景与定位

模型是 Radar 的核心资产：字段、preItems、数据名单、抽象、激活与规则（内嵌 Groovy）、模型配置共同构成
一份完整的风控策略。现状的复制能力只存在于**单实例内**（`POST /model/copy` → copyModel 全图复制），
跨实例迁移靠手工 SQL 或人工重建——这挡住了三类高频场景：

1. **环境迁移**：开发环境调好的模型搬上生产；
2. **求助与支持**：用户把出问题的模型发一个 json 给社区/作者，替代一堆截图和 SQL dump；
3. **模板分发**：行业风控策略（电商开户、信贷反欺诈）以文件形态分享——这是"模板市场"的分发单元。

v3.2 定位为**策略包版本**：给模型一个文件形态。与插件生态构成双护城河——插件共享"数据接入"，
模型包共享"风控策略"。纯增量功能，无 schema 变更、无 API 破坏，minor bump。

## 2. 现状盘点（2026-11-21 代码实证）

| 项 | 现状 | 证据 |
|----|------|------|
| 复制 | `copyModel` 全图复制（字段→preItem→dataList+meta+record→抽象→激活→规则），activationId/dataListId 插入后重映射正确——**导出范围与它对齐即可** | ModelServiceImpl.java:463 |
| engine_model_conf | **ML 特征配置（feed/expressions），Estimator 零实现后处于休眠**；copyModel 不复制它属合理现状。不入策略包，待 P1-5 ML 重做时随新方案重设计 | ModelConfParamPO.feed/expressions → EstimatorContainer.getByModelId 唯一消费链 |
| 缺口 2 | 无任何跨实例导出通道；`/list/template` 的"模板"是 DB 内置标记，不产生文件 | ModelApiController.java:127 |
| 规则脚本 | Groovy 内嵌 `rule.SCRIPTS` 列，无外部文件引用——随图导出即可 | RulePO.java:41 |
| 外部引用面 | preItems 按 plugin key **字符串引用插件**（跨实例可能缺失）；dataLists 自包含随模型走；无其它外部引用 | PreItemPO.plugin |
| 不属配置 | 事件流水/hits（运行数据）、rule_history（审计历史）不进包 | — |
| 序列化基建 | 各 PO 平铺属性（MyBatis-Plus），Jackson 直接可用，主工程 ObjectMapper 已注入 | — |
| 鉴权 | `/services/v1/model/**` 全部在 JWT 拦截范围 | InterceptorConfig |

## 3. 技术决策（定案，不再讨论）

| 决策 | 选择 | 理由 |
|------|------|------|
| 载体格式 | **单 JSON 信封**：`formatVersion` + `exportedAt` + `radarVersion` + 八个子集合（model/fields/preItems/dataLists/dataListMeta/dataListRecords/abstractions/activations/rules）。不用 zip | 配置全文本、无二进制附件，单文件最好发（聊天/issue 里直接贴） |
| model_conf 排除 | `engine_model_conf(+param)` 不入包、copyModel 不补复制：其内容为 ML 特征配置（feed/expressions），Estimator 自 v2.0 零实现已休眠，且 ML 模块计划重做（roadmap P1-5） | 打包休眠配置只会制造假可用感；格式演进时子集合可增（formatVersion 已留门） |
| ID 策略 | 文件保留原 ID 作**图内引用键**；导入时整图分配新 DB ID（沿用 copyModel 插入后重映射模式）；模型 GUID 重新生成、状态置 INIT（导入后手动构建） | 与 copy 语义完全一致，心智零成本 |
| 导入语义 | **仅"导入为新模型"**：merchantCode/名称/label 由导入表单指定，冲突报错（同 copy 查重）。覆盖更新/合并明确不做 | 部分更新的字段级合并复杂度不可控，列后续再议 |
| 校验报告 | 结构校验（信封字段/反序列化）→ 引用完整性（缺失插件 key 警告）→ Groovy 预编译检查（告警不阻断，构建时仍兜底）→ 名单行数提示 | 报告让用户"带着已知问题决策"，而不是黑盒失败 |
| 兼容性 | Jackson `ignore unknown`（向前兼容新版导出文件）；`formatVersion` 不识别即拒绝并提示所需版本 | 版本演进留门 |
| 事务 | 整图插入单事务（`@Transactional`），半途失败全回滚 | 不允许"导入了半个模型" |
| 大小上限 | 上传 10MB；名单记录超阈值（默认 10 万行，可配）仅告警不截断 | 防御性上限；名单记录实际很少超 |
| 模板库 | 官方种子模型导出 JSON 入 `resources/models/` + 模板一键导入——**拉伸目标 T6**，不阻塞主线 | 先闭环文件格式，模板库是分发问题 |
## 4. 实施内容

### 4.1 T1：策略包信封（ModelPackage）

- `ModelPackage` 信封 + 序列化器（读模型 → 八集合，不含 model_conf）；`formatVersion` 常量与版本校验
- 导出文件名：`radar-model-<label>-<yyyyMMdd>.json`

### 4.2 T2：导入引擎（ModelPackageImporter）

- copyModel 的装配段抽为公共函数（源数据 = DB 行 | 信封反序列化结果，两路共用；不含 model_conf）
- ID 重映射（activation/dataList/conf）→ 校验报告（warnings 列表）随结果返回
- `@Transactional` 整图事务

### 4.3 T3：端点

- `GET /services/v1/model/export/{id}`：附件下载
- `POST /services/v1/model/import`：multipart 上传 + merchantCode/名称/label 表单参数，返回新模型 id + 校验报告

### 4.4 T4：前端（ModelList）

- 行操作新增"导出"；页头新增"导入"（上传弹窗：文件 + 商户码/名称/显示名；结果回显 + 警告列表展示）

### 4.5 T5：文档与发布

- `docs/model-package.md`：信封格式说明、导入导出操作、故障排查
- CHANGELOG、主 pom 3.2.0、tag 发布流水线（复用）

### 4.6 T6（拉伸目标）：官方模板库

- 种子模型（电商开户等）导出为 `resources/models/*.json` 随 jar 分发；模型列表"从模板创建"一键导入

## 5. 实施阶段

| 阶段 | 内容 | 产出 | 估计 |
|------|------|------|------|
| S0 | 方案评审 + 信封格式冻结 | 定稿 | 0.5 人日 |
| S1 | 序列化器 + 导入引擎（装配函数抽公共） | 包可导可导回 | 2.5 人日 |
| S2 | 导出/导入端点 | API 就绪 | 0.5 人日 |
| S3 | 前端导入导出 | 管理端闭环 | 1.5 人日 |
| S4 | 测试：信封 round-trip、ID 重映射、缺插件警告、Groovy 校验、导入后构建跑通（L3） | 回归绿 | 1.5 人日 |
| S5 | 文档 + 发布准备 v3.2.0 | 按发布规范 | 1 人日 |

**合计约 7.5 人日**。S1 → S2 → S3 串行，S4 随行验证，S5 收尾。

## 6. 风险与对策

| 风险 | 对策 |
|------|------|
| PO 字段随版本演进（加列） | formatVersion + ignore unknown；导入只填已知列，缺列走 DB 默认值 |
| 名单记录量大拖慢导出 | 超阈值告警不截断（可配）；导出走流式序列化避免 OOM |
| 导入半途失败留脏数据 | 单事务全回滚 + L3 用例验证 |
| 恶意 JSON / 危险 Groovy | 管理员可信模型（与插件/Groovy 编辑一致立场）；10MB 上限 + 结构校验 |
| 导入的模型缺插件不可用 | 校验报告明示缺失 key；运行时插件隔离（v3.0）兜底 |
| 覆盖门禁稀释 | 序列化/重映射/校验均为纯逻辑，L1 单测覆盖；门禁核对进 S4 |

## 7. 验收标准

1. 任意模型导出 JSON → 目标实例导入为新模型 → 构建通过 → 同一事件打分结果与源实例一致（L3 用例）
2. 导入报告列明：缺失插件 key、Groovy 编译告警、名单行数
3. merchantCode+label 冲突报错且无半写（事务回滚可验证）
4. 单实例 copy 与文件导入共用装配代码（不含 model_conf）；导入的模型在无 ML 配置下构建与打分行为与源实例一致
5. 前端导出/导入 + 报告展示可用，零 API 文档外依赖
6. L1/L2 全绿，覆盖率门禁（≥40%）保持；v3.2.0 按发布规范（tag + CHANGELOG + Release + GHCR）
