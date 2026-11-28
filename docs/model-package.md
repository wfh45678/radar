# Radar 模型策略包（v3.2 模型导入导出）

模型策略包是模型配置全图的文件形态：一个 JSON 文件带走字段、preItems、数据名单、抽象、激活、规则，
可在任意 Radar v3.2+ 实例导入为一个新模型。适用于**环境迁移、备份归档、求助支持（把 json 发社区/作者）
与策略分享**——与插件生态构成双护城河：插件共享"数据接入"，模型包共享"风控策略"。

管理端入口：**模型列表 → 行内「导出」/ 页头「导入模型」**。

## 1. 策略包格式

```json
{
  "formatVersion": "2.0",
  "exportedAt": "2026-11-21T08:00:00Z",
  "radarVersion": "4.1.0",
  "model":       { "guid": "...", "label": "电商开户", "entityName": "Event", "...": "..." },
  "fields":      [ "..." ],
  "dataLists":   [ "..." ],
  "dataListMeta":    [ "..." ],
  "dataListRecords": [ "..." ],
  "features":    [ { "featureName": "...", "type": "ENRICH|AGGREGATE|DERIVED|RATIO", "definition": "类型化定义（JSON 字符串形态，v4.1）", "...": "..." } ],
  "strategies": [ { "strategyName": "...", "type": "RULE_SET|SCORECARD|DECISION_TREE", "definition": "类型化定义（JSON 字符串形态）", "...": "..." } ],
  "rules":       [ "...": { "scripts": "内嵌 Groovy 规则脚本" } ]
}
```

- 范围对齐单实例「复制」：`engine_model` + `engine_field/data_lists(+meta/record)/feature/strategy/rule`；
  事件流水、规则历史**不在包内**（运行数据不随策略分发）；
- `engine_model_conf(+param)`（ML 特征 feed/expressions 配置）**不入包**——Estimator 自 v2.0 零实现，
  该配置处于休眠状态，待 ML 模块重做后随新方案扩展；
- 文件内保留原 ID 作为**图内引用键**（`activationId`/`dataListId` 等），导入时整图重映射分配新 ID；
- `formatVersion` 由导入端协商：v4.1 导出 2.0，导入兼容 1.0（v4.0 包自动转换，见下）；
- **v4.1 兼容性**：v4.0 包（preItems/abstractions 数组）导入时**自动转换**为特征形态
  （PackageV1Converter），策略 DEFINITION 旧列前缀由条件求值器 alias 兼容；v4.1 导出仅写
  `features`（ENRICH/AGGREGATE/DERIVED/RATIO，definition/output 字符串形态），v4.0 引擎
  无法完整导入 v4.1 包（策略仍在但特征丢失），请先升级引擎；
- **v4.0 说明**（历史）：v4.0 曾承诺信封键 activations 保持原名以兼容 v3.x；v4.1 起统一为
  strategies/features，单向升级；
- **v4.0 兼容性**：信封键 `activations` 与条目键 `activationName`/`activationId` **保持 v3.x 原名不变**
  （代码内部已更名"策略 Strategy"），v3.x 导出的包可直接导入 v4.0；v4.0 新增条目字段
  `type`（缺省按 RULE_SET 处理）与 `definition`（评分卡/决策树的 JSON 定义，**字符串形态**，
  与 `ruleDefinition` 同约定），旧引擎不识别这两个字段——v4.0 导出的含评分卡/决策树的包
  导入 v3.x 会丢失对应策略，请先升级引擎。

## 2. 导入语义

| 项 | 语义 |
|----|------|
| 导入结果 | **新模型**：新 GUID、状态 INIT（需「构建」后生效），原模型不受影响 |
| 命名 | 显示名称必填（同商户码下唯一）；商户码留空=当前商户；模型名留空自动 `model_<id>` |
| 冲突 | 商户码+显示名称已存在 → 拒绝导入（无覆盖模式，避免部分更新歧义） |
| 事务 | 整图单事务，任何插入失败全部回滚，不会出现"半个模型" |
| 覆盖更新/合并 | 不支持（列后续再议） |

### 导入报告（告警不阻断）

导入成功时可能附带告警，帮助你"带着已知问题决策"：

- **未安装插件**：preItem 按 key 引用插件，目标实例缺失时该插件在事件执行时被隔离跳过
  （模型仍可用，仅对应字段无数据）→ 可先装插件再重新导入，或接受降级；
- **规则脚本编译告警**：Groovy 预编译失败的规则 → 构建模型时会在该规则上报错，请修正后保存；
- **名单记录超阈值**：仅提示行数（默认 10 万行，`model.package.record-warn-threshold` 可调），不截断。

## 3. 接口

| 端点 | 说明 |
|------|------|
| `GET /services/v1/model/export/{id}` | 导出策略包（JSON 附件下载；JWT 鉴权） |
| `POST /services/v1/model/import` | multipart `file` + `label`（必填）+ `merchantCode`/`modelName`（可选）；返回新模型 `id` + `warnings` 列表 |

配置项：`model.package.max-upload-size`（默认 10MB）、`model.package.record-warn-threshold`（默认 100000）。

## 4. 官方模板库（v3.4）

管理端「模型列表 → 模板库」：浏览官方场景模板（账户盗用防护 / 营销反作弊 / 交易风控），
一键创建为可编辑副本——新用户装好即有完整可跑的场景策略，无需从零配置。

| 模板 key | 场景 | 要点 |
|----------|------|------|
| `account-takeover` | 账户盗用/暴力破解 | IP/设备/用户三维登录频控 + 关联聚集检测 |
| `promo-abuse` | 营销反作弊 | 注册领奖批量小号：设备/IP 聚集 + 模式规则 |
| `transaction-risk` | 交易风控 | 大额 + 非常用时段 + 异地 IP 组合规则 |

- **在线模板源**：默认官方地址 `https://www.riskengine.cn/radar/templates/catalog.json`，
  官方上新模板无需升级引擎；远程同名 key 覆盖内置（可在线修订）；
- **自托管/禁用**：`sys.conf.template-source-url` 覆盖为自己的 catalog 地址（静态 JSON，
  结构同内置 `resources/models/catalog.json`），**置空则禁用在线源**（纯内置模式）；
  拉取失败自动降级为仅内置并提示，永不阻塞应用与事件链路（3s 超时 + 60s 负缓存）；
- 模板导入与普通策略包同一校验链（validate + 整图事务），导入告警说明见上文。

## 5. 典型场景

**环境迁移**：开发实例导出 → 生产实例导入 → 构建 → 事件打分。

**策略分享**：把 json 发给另一个使用者（群/issue 均可，纯文本直接贴）→ 对方导入。
分享前建议导出后自查：包内不含事件数据，但含业务字段定义与规则逻辑，注意敏感信息。

**求助支持**：出问题的模型导出附件发给社区，比截图和 SQL dump 高效得多。

## 6. 故障排查

| 现象 | 处理 |
|------|------|
| "格式版本不支持" | 包由更新/更旧版本的引擎导出，`formatVersion` 不匹配；升级引擎或用对应版本导入 |
| 导入后事件里某 preItem 字段为 `__plugin_error__` | 报告里的"未安装插件"告警生效——按插件开发指南安装对应插件 |
| 导入成功但构建失败 | 查看构建报错的规则（多为 Groovy 告警规则），修正脚本后保存再构建 |
| 大名单导入慢 | 正常现象（逐行落库），阈值告警仅提示；导入有单事务保护，失败全回滚 |
