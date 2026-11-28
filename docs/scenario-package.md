# Radar 场景策略包（v5.0 场景导入导出）

场景策略包是场景配置全图的文件形态：一个 JSON 文件带走字段、特征（v4.1 四类型：插件加工/统计聚合/
衍生/组合比率）、数据名单、策略（v4.0 类型化：规则集/评分卡/决策树）与规则，
可在任意 Radar 实例导入为一个新场景。适用于**环境迁移、备份归档、求助支持（把 json 发社区/作者）
与策略分享**——与插件生态构成双护城河：插件共享"数据接入"，场景包共享"风控策略"。

管理端入口：**场景列表 → 行内「导出」/ 页头「导入场景」**。

## 1. 策略包格式（formatVersion 3.0）

```json
{
  "formatVersion": "3.0",
  "exportedAt": "2026-11-28T08:00:00Z",
  "radarVersion": "5.0.0",
  "scenario":     { "guid": "...", "label": "电商开户", "entityName": "Event", "...": "..." },
  "fields":       [ "..." ],
  "dataLists":    [ "..." ],
  "dataListMeta":     [ "..." ],
  "dataListRecords":  [ "..." ],
  "features":     [ { "featureName": "...", "type": "ENRICH|AGGREGATE|DERIVED|RATIO", "definition": "类型化定义（JSON 字符串形态，v4.1）", "...": "..." } ],
  "strategies":   [ { "strategyName": "...", "type": "RULE_SET|SCORECARD|DECISION_TREE|DECISION_TABLE|LIST_MATCH", "definition": "类型化定义（JSON 字符串形态）", "...": "..." } ],
  "rules":        [ { "strategyId": "...", "featureName": "features.xxx", "scripts": "内嵌 Groovy 规则脚本" } ]
}
```

- 范围对齐单实例「复制」：`engine_scenario` + `engine_field/data_lists(+meta/record)/feature/strategy/rule`；
  事件流水、规则历史**不在包内**（运行数据不随策略分发）；
- `engine_model_conf(+param)`（算法模型配置，v5.0 起"模型"一词专属此概念）**不入包**——
  Estimator 零实现期间休眠，待 ML 模块重做（roadmap P1-5）后随新方案扩展；
- 文件内保留原 ID 作为**图内引用键**（`strategyId`/`dataListId` 等），导入时整图重映射分配新 ID；
- **v5.0 起仅支持 3.0 格式**：历史 1.0/2.0 包不再导入（v5.0 断包袱决策，见
  [plan-v5.0.md](plan-v5.0.md)）；如需迁移历史包，请用 v4.1.5 引擎导出后按
  [upgrade-guide.md](upgrade-guide.md) 术语对照表手工调整（模型→场景、activationId→strategyId、
  abstractionName→featureName、activationName→strategyName）。

## 2. 导入语义

| 项 | 语义 |
|----|------|
| 导入结果 | **新场景**：新 GUID、状态 INIT（需「构建」后生效），原场景不受影响 |
| 命名 | 显示名称必填（同商户码下唯一）；商户码留空=当前商户 |
| 冲突 | 商户码+显示名称已存在 → 拒绝导入（无覆盖模式，避免部分更新歧义） |
| 事务 | 整图单事务，任何插入失败全部回滚，不会出现"半个场景" |
| 覆盖更新/合并 | 不支持（列后续再议） |

### 导入报告（告警不阻断）

导入成功时可能附带告警，帮助你"带着已知问题决策"：

- **未安装插件**：ENRICH 特征按 key 引用插件，目标实例缺失时该插件在事件执行时被隔离跳过
  （场景仍可用，仅对应字段无数据）→ 可先装插件再重新导入，或接受降级；
- **规则脚本编译告警**：Groovy 预编译失败的规则 → 构建场景时会在该规则上报错，请修正后保存；
- **名单记录超阈值**：仅提示行数（默认 10 万行，`scenario.package.record-warn-threshold` 可调），不截断。

## 3. 接口

| 端点 | 说明 |
|------|------|
| `GET /services/v1/scenario/export/{id}` | 导出场景策略包（JSON 附件下载；JWT 鉴权） |
| `POST /services/v1/scenario/import` | multipart `file` + `label`（必填）+ `merchantCode`/`scenarioName`（可选）；返回新场景 `id` + `warnings` 列表 |

配置项：`scenario.package.max-upload-size`（默认 10MB）、`scenario.package.record-warn-threshold`（默认 100000）。

## 4. 官方模板库（v3.4）

管理端「场景列表 → 模板库」：浏览官方场景模板（账户盗用防护 / 营销反作弊 / 交易风控），
一键创建为可编辑副本——新用户装好即有完整可跑的场景策略，无需从零配置。

| 模板 key | 场景 | 要点 |
|----------|------|------|
| `account-takeover` | 账户盗用/暴力破解 | IP/设备/用户三维登录频控 + 关联聚集检测 |
| `promo-abuse` | 营销反作弊 | 注册领奖批量小号：设备/IP 聚集 + 模式规则 |
| `transaction-risk` | 交易风控 | 大额 + 非常用时段 + 异地 IP 组合规则 |

- **在线模板源**：默认官方地址 `https://www.riskengine.cn/radar/templates/catalog.json`，
  官方上新模板无需升级引擎；远程同名 key 覆盖内置（可在线修订）；
- **自托管/禁用**：`sys.conf.template-source-url` 覆盖为自己的 catalog 地址（静态 JSON，
  结构同内置 `resources/scenarios/catalog.json`），**置空则禁用在线源**（纯内置模式）；
  拉取失败自动降级为仅内置并提示，永不阻塞应用与事件链路（3s 超时 + 60s 负缓存）；
- 模板导入与普通策略包同一校验链（validate + 整图事务），导入告警说明见上文。

## 5. 典型场景

**环境迁移**：开发实例导出 → 生产实例导入 → 构建 → 事件打分。

**策略分享**：把 json 发给另一个使用者（群/issue 均可，纯文本直接贴）→ 对方导入。
分享前建议导出后自查：包内不含事件数据，但含业务字段定义与规则逻辑，注意敏感信息。

**求助支持**：出问题的场景导出附件发给社区，比截图和 SQL dump 高效得多。

## 6. 故障排查

| 现象 | 处理 |
|------|------|
| "格式版本不支持: 2.0/1.0" | v5.0 起仅支持 3.0 格式——用 v4.1.5 导出后按术语对照表手工调整，或重建配置 |
| 导入后事件里某特征字段为 `__plugin_error__` | 报告里的"未安装插件"告警生效——按插件开发指南安装对应插件 |
| 导入成功但构建失败 | 查看构建报错的规则（多为 Groovy 告警规则），修正脚本后保存再构建 |
| 大名单导入慢 | 正常现象（逐行落库），阈值告警仅提示；导入有单事务保护，失败全回滚 |
