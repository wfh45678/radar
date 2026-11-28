# Changelog

本项目所有重要变更记录于此，格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)，版本号遵循 [semver](https://semver.org/lang/zh-CN/)。

## [4.1.1] - 2026-11-28

补丁版本：v4.0/v4.1 重构的回归收口（真实迁移演练 + 模板/策略包导入链路端到端审查捕获），**首个 v4 tag**。

### Fixed
- **导入链路（v4.1 回归）**：`PackageV1Converter` 三处补齐——① `activations` → `strategies` 键名
  转换（漏改导致策略整组丢弃，规则 STRATEGY_ID 重映射为 null 触发 NOT NULL 违例）；② v1 包内
  `ruleDefinition` 为 JSON 字符串形态，原 object-only 判定丢过滤条件（导入模型聚合特征恒 -1），
  解析回对象并同步改写列前缀；③ 规则脚本/规则名前缀改写（`data.preItems.`/`data.abstractions.` →
  `data.features.`，与 upgrade-4.1.0.sql 对存量行的口径一致）
- **AggregateExecutor filter 缺省语义**：无 filter = 无过滤全量统计（原 `evaluate(null)=false`
  使无过滤特征的聚合恒 -1）
- **ModelServiceImpl.featureMapper 漏注入注解**（v4.1 新增字段）：copy/delete 模型路径 NPE
- **upgrade-4.1.0.sql**：`engine_model_conf_param` 列名 `EXPRESSION` → `expressions`（真实迁移演练捕获）
- **verify-chain.sh**：决策断言对齐 v4.1 `strategies` 契约

### Removed
- `StrategyApiController` 两个 @Deprecated 变量树端点（`/strategy/feature/columns`、
  `/strategy/abstraction/columns`）——前端已切换 `/feature/columns`

### Changed
- **内置官方模板包重导出为 formatVersion 2.0**（features/strategies 信封，脚本前缀已改写），
  catalog stats 键 `abstractions` → `features`、minRadarVersion 4.1.0——模板示范样本与新格式一致
- docs/architecture.md 对齐 v4.1 两阶段特征管线；指标描述/注释去旧术语（指标名不变）

### Verification
- 测试服务器真实迁移演练：备份 → upgrade-4.0.0 + 4.1.0 → 十步全链路绿（ENRICH 46/AGGREGATE 189 搬迁）
- 模板「导入 → build/enable → 事件频控递增 → 导出 v2.0 → 再导入」round-trip 全通
- 全库扫描确认无其他漏注入的 Mapper/Service 字段；L1/L2 回归绿

## [4.1.0] - 2026-11-28

破坏性版本：特征工程重构（[plan-v4.1](docs/plan-v4.1.md)、[升级指南](docs/upgrade-guide.md)）。
预处理（PreItem）与抽象（Abstraction）统一为**特征（Feature）**体系，首批四类型 + SDK 4.0。

### Added
- **特征执行器 SPI**：FeatureExecutor + FeatureExecutorRegistry（对齐 v4.0 策略范式），
  两阶段管线（ENTRY=ENRICH/DERIVED 落库前；ANALYSIS=AGGREGATE/RATIO 落库后），单特征异常隔离
- **插件加工 ENRICH**（原 PreItem）：支持链式（sourceFields 可引用前序特征，按 ORDER_NO 顺序）
- **统计聚合 AGGREGATE**（原 Abstraction）：AggregateFunction SPI 注册表（10 种迁移），
  过滤条件树经 ConditionEvaluator 后端解释（Groovy 脚本退役）；UI 放开全部 10 种聚合
- **衍生表达式 DERIVED**（新）：DerivedFunction SPI + 13 内置函数（算术/字符串/正则提取/映射表/空值兜底）
- **组合比率 RATIO**（新）：两特征组合运算（比率/差额），除零策略 ZERO/NULL
- **radar-plugin-api 4.0.0**：FeatureProcessor/@FeatureMeta/FeatureRequest（新增 features 链式视图）/
  FeatureContext/FeatureResponse；PluginManager 双契约加载（v4 主 + v3 legacy 告警桥，v5 移除）；
  testkit 4.0.0；内置 8 插件与两示例工程全部迁移
- 特征定义变更走 engine_feature 保存即生效（radar_channel_feature 频道）
- L3 EngineFullChainTest 契约断言迁移 + e2e feature-crud.spec

### Changed
- **BREAKING｜特征统一**：engine_pre_item + engine_abstraction → engine_feature
  （TYPE/ORDER_NO/DEFINITION/OUTPUT），存量数据自动搬迁；管理端 API /preitem + /abstraction → /feature；
  Redis 频道合并 radar_channel_feature；Model.jsx Tab 5→4（特征工程）
- **BREAKING｜上下文键**：preItems + abstractions → **features**（引擎响应/ES 文档/Mongo 落库；
  Mongo 仍为 flatten 裸名）；列前缀 preItems./abstractions. → **features.**（升级 SQL 搬迁 +
  ConditionEvaluator alias 兼容）
- **BREAKING｜修复历史失配**：引擎响应/ES 决策键 activations → **strategies**
  （v4.0 写 activations 与报表/导出读 strategies 失配，本次统一为 strategies）
- 统计口径统一：全部聚合过滤 status='1'（v3.x 仅 COUNT）；AVERAGE/VARIANCE 空数据集不再异常（返回 0）

### 不变（兼容承诺）
- 策略包：formatVersion 升 2.0（features 统一信封），导入端**兼容 v4.0 包**（preItems/abstractions
  自动转换 + 策略 DEFINITION 旧列前缀 alias）；v4.1 导出的包不含 v4.0 兼容键（单向升级）
- 条件求值语义：ConditionEvaluator 对 v4.0 列前缀 alias 透明兼容
- 插件出访管控/失败隔离（__plugin_error__ 标记）、HTTP 工具、缓存契约不变

### Verification
- L1/L2：68 例全绿（新增 Feature SPI/求值器/比率/聚合注册表测试；种子脚本 Groovy4 兼容回归）
- 前端 npm run build 通过；testkit/samples 构建测试全过；L3 与 e2e CI 执行

## [4.0.0] - 2026-11-27

破坏性大版本：策略板块重构（[plan-v4.0](docs/plan-v4.0.md)、[升级指南](docs/upgrade-guide.md)）。
"激活器（Activation）"更名"策略（Strategy）"并类型化——README/docs 宣示的多策略类型自此落地实现。

### Added
- **策略类型执行器 SPI**（v4.0 核心）：`StrategyExecutor` + `StrategyExecutorRegistry`
  （范式对齐 PluginManager），引擎编排层按 `engine_strategy.TYPE` 分发、单策略异常隔离、
  扩展新类型零改动编排层
- **评分卡（SCORECARD）**：特征分箱加权求和（首个命中分箱生效 × 权重 + 初始分），阈值出决策；
  `ScoreCardExecutor` 后端解释执行，命中明细逐特征输出（feature_*）
- **决策树（DECISION_TREE）**：条件节点路径走查、叶节点直出决策（pass/review/reject + 分数 + 理由），
  命中明细记录访问路径（node_*）；环防护（步数上限）与脏数据校验（重复 id/悬空分支/非法叶决策）
- **后端条件求值器 `ConditionEvaluator`**：直接解释 ruleDefinition 同构 JSON（PDCT/SMPL +
  20 个操作符，语义与前端 Groovy 编译器逐条对齐），不再依赖前端生成脚本；为后续规则集后端化解析铺路
- **策略定义变更历史**：`engine_strategy_history` 评分卡/决策树保存快照（操作人/类型/定义），
  `GET /strategy/history/{id}` 查询 + 编辑器「变更历史」弹窗
- **transaction-risk 官方模板升级**：新增交易风险评分卡（3 特征 9 分箱）与交易准入决策树（7 节点），
  三种策略类型同模板可验
- 策略级指标 `radar_strategy_executions_total{type,result}`（原有 `radar_rule_hits_total`/
  `radar_strategy_decision_total` 不变）
- L3 全链路 IT：模板导入 → 三种策略类型决策断言（`EngineFullChainTest.step4`）；
  e2e 新增 `strategy-types.spec.ts`（列表类型徽标/评分卡编辑保存/决策树渲染）

### Changed
- **BREAKING｜概念更名**：`engine_activation` → `engine_strategy`（列 `ACTIVATION_NAME` →
  `STRATEGY_NAME`）、`engine_rule.ACTIVATION_ID` → `STRATEGY_ID`，Java 类
  `Activation*` → `Strategy*`，管理端 API `/services/v1/activation` → `/services/v1/strategy`，
  前端路由/组件随迁——升级需执行 `sql/upgrade-4.0.0.sql` 并同步更换前端
- **BREAKING｜策略新增字段**：`engine_strategy` 新增 `TYPE`（存量默认 RULE_SET，行为不变）与
  `DEFINITION`（评分卡/决策树 JSON）
- 新增策略弹窗支持类型选择（默认规则集）；策略列表新增类型徽标；详情路由
  `#/strategyDetail/:modelId/:strategyId` 按类型分发（规则集界面不变）

### 不变（兼容承诺）
- **引擎决策接口响应结构不变**：`activations`/`hitsDetail`/`abstractions`/`respTimes` 键名保持
  （Java 内部更名经 `@JsonProperty` 锚定），调用方与 ES 报表零改造
- **策略包 JSON 格式不变**：信封键 `activations`、条目键 `activationName`/`activationId` 保持
  （`@JsonProperty` 锚定），v3.x 导出的包可直接导入；新包新增条目字段 `type`/`definition`
  （旧引擎不识别，建议先升级引擎再互导），`formatVersion` 保持 `1.0`
- Redis pub/sub 通道、决策三段阈值语义、规则集执行逻辑（原样抽取为 `RuleSetExecutor`）不变

### Verification
- L1/L2：`mvn test` 60 测试全绿（新增 ConditionEvaluatorTest 11 例 + ScoreCardExecutorTest 6 例 +
  DecisionTreeExecutorTest 6 例；92 种子脚本 Groovy4 兼容回归）
- 前端 `npm run build` 通过；e2e 与 L3 需容器环境（CI 执行）
- 存量兼容：模板库/策略包/种子数据回归测试全绿（transaction-risk 模板含三种类型可解析导入）

## [3.5.1] - 2026-11-27

补丁版本：在线模板源首次真实点亮后回归发现的两个修复（官方托管文件已就绪于
`www.riskengine.cn/radar/templates/`）。

### Fixed
- **在线模板源默认地址改用 `www.riskengine.cn`**：裸域 `riskengine.cn` 未配置 DNS 解析
  无法直连（2026-11-27 实测确认，官方托管文件位于 www 站点）；`sys.conf.template-source-url`
  覆盖行为不变，置空禁用行为不变
- **远程模板条目 stats 恒空**：`statsOfRemote` 为空桩且在合并时覆盖 catalog 声明值，
  改为由 `parseCatalog` 解析 catalog stats、`toInfo` 携带——在线源连接后模板库卡片的
  规则/抽象/字段统计恢复正常

### Verification
- 官方托管四文件实测 HTTP 200 且字节数与仓库一致（UTF-8 无损）
- 测试服务器模板库 API：`sourceStatus=CONNECTED`，三模板 REMOTE 覆盖、stats 与 catalog 一致
  （5/13/9、6/8/9、9/14/9）；十步全链路 verify-chain 绿；health UP、启动日志零 ERROR

## [3.5.0] - 2026-11-23

生态内容补强版本（roadmap P2-9，[plan-v3.5](docs/plan-v3.5.md)）：官方种子插件三件套（纯内容，
零引擎改动、SDK 不发版）+ Playwright E2E 覆盖追平 v3.1~v3.4 功能面。

### Added
- **官方种子插件三件套**（samples/radar-plugin-demo 扩展，示例定位不进内置，零外呼零依赖）：
  `ID_CARD_VALIDATE` 身份证校验（GB 11643-1999 校验位 + 生日/性别/省份多列，15 位老号宽松/严格
  模式 options 下拉）、`USER_AGENT_PARSE` UA 解析（设备/OS/浏览器大类 + 微信·支付宝内嵌，
  零依赖自实现）、`BANK_CARD_CHECK` 银行卡校验（Luhn + 卡种粗判 + 主流银行前缀表）——
  三种实现姿势范本（纯算法/正则规则表/前缀映射表）
- 每插件 testkit 单测（正常/边界/严格模式抛异常/多列输出断言），测试数据全部为 GB 标准样例号
- **E2E 扩面**（e2e/tests/）：公共登录 helper（helpers.ts）+ 三个独立 spec——
  `plugin-admin`（插件管理页渲染/启停切换/复位持久化）、`model-package`（导出下载断言/导入弹窗）、
  `template-library`（模板库浏览/一键创建/列表 +1）——CI e2e.yml 零改动自动纳入

### Changed
- PLUGIN_REGISTRY 官方示例表按插件拆行 + 新增「插件开发到上架五分钟」指引；
  plugin-dev-guide 示例工程提及三件套

### Verification
- radar-plugin-demo 20/20 单测绿（新增 16：身份证校验位/严格模式/15 位老号、UA 三形态、
  Luhn/卡种/银行前缀/空格剥离）
- E2E spec 编译解析 9/9（4 文件）；全量 L1/L2 回归绿
- L3 红线/E2E 真环境随 CI 执行

## [3.4.0] - 2026-11-23

官方模板库版本（roadmap P2-7 拉伸项转正，[plan-v3.4](docs/plan-v3.4.md)）：官方场景策略包模板
随 jar 内置分发 + 在线模板源（riskengine.cn，可覆盖/禁用），管理端一键浏览、一键创建——
新用户装好即有完整可跑的场景策略，"十分钟可体验"断层闭环。

### Added
- **官方模板三件套**（resources/models/，账户盗用防护 / 营销反作弊 / 交易风控，
  规则 5/6/9 条、全部内置插件、零真实数据，名单仅结构种子）
- **模板库 API**：`GET /services/v1/model/templates`（内置+在线合并卡片列表，含来源徽标与
  在线源状态）、`POST /services/v1/model/templates/{key}/import`（按 key 加载 → validate →
  importPackage，复用普通导入校验链与整图事务）
- **在线模板源**：默认官方地址 riskengine.cn，`sys.conf.template-source-url` 可覆盖、置空禁用；
  3s 超时 + 60s 负缓存 + 失败降级仅内置，永不阻塞启动与事件链路；远程同名 key 覆盖内置（官方可在线修订）
- **管理端模板库**：模型列表页「模板库」弹窗——卡片（名称/场景/描述/规则·抽象·字段统计/来源徽标）、
  在线源状态、一键创建（label 预填）+ 告警回显（结果展示抽公共组件 ImportResultContent）
- L3 模板质量红线用例（TemplateRedLineTest）：逐模板 HTTP 黑盒——导入零告警 → 构建通过 →
  启用 → 造事件引擎出分，随 CI 常驻

### Verification
- L1 TemplateLibraryTest 5/5（内置 catalog 解析统计 / 在线源 mock 合并+远程覆盖 / 失败降级+负缓存 /
  置空禁用 / 坏包 parse 拒绝）；全量 L1/L2 回归绿
- L3 TemplateRedLineTest（Testcontainers 真库 HTTP 黑盒）随 CI 执行
- 前端 vite build 通过，产物内嵌 jar

## [3.3.0] - 2026-11-21

开源基建版本（roadmap P2-8，源自 2026-11-21 开源健康度盘点"三件事"）：依赖与安全自动扫描、文档站与
架构图、前端 E2E 冒烟。零业务代码改动（唯一后端触碰为登录验证码开关，默认行为不变）。
规划详见 [docs/plan-v3.3.md](docs/plan-v3.3.md)。

### Added
- **Dependabot**：maven / npm / github-actions 三生态周检，升级以 PR 提交
- **CodeQL**：Java 静态安全扫描（push master / PR / 每周复扫；首轮 report-only）
- **文档站**（VitePress，工程根即 docs/，仓库文档零拷贝上站）：GitHub Pages 自动部署（.github/workflows/docs.yml，
  首次启用需仓库 Settings→Pages 选 GitHub Actions）；新增 [docs/architecture.md](docs/architecture.md)
  （事件处理流/部署形态两张 mermaid 架构图）、[docs/operations.md](docs/operations.md)（运维手册首版：
  部署形态/升级/备份/监控/FAQ）、docs/index.md 文档导航
- **E2E 冒烟**（e2e/，Playwright）：五步链路（登录→新建模型→进入工作区→事件发布→报表可见），
  .github/workflows/e2e.yml（push master：构建 jar → compose 全套环境 → Playwright），
  失败自动上传 trace/截图——防 Cascader 白屏类回归
- 登录验证码开关 `sys.conf.captcha-enabled`（默认 true 不变；E2E/演示环境经
  `SYSCONF_CAPTCHAENABLED=false` 关闭，compose 已透传）

### Verification
- 文档站本地构建通过（VitePress + mermaid 渲染）
- 主工程 compile 通过；L1/L2 全量回归绿
- E2E 五步链路随 CI（GitHub 镜像仓）在真实 compose 环境执行

## [3.2.0] - 2026-11-21

策略包版本（roadmap P2-7）：模型配置全图文件化——一个 JSON 策略包带走字段/preItems/数据名单/抽象/激活/规则，
跨实例导入为新模型。环境迁移、备份归档、求助支持与策略分享的分发单元，与插件生态构成双护城河。
规划详见 [docs/plan-v3.2.md](docs/plan-v3.2.md)，格式与操作见 [docs/model-package.md](docs/model-package.md)。

### Added
- **策略包信封**（formatVersion 1.0）：model + fields/preItems/dataLists(+meta/records)/abstractions/activations/rules
  八个子集合；文件内保留原 ID 作图内引用键，导入手动整图重映射分配新 ID；
  `engine_model_conf(+param)` 为 ML 休眠特征配置（Estimator 零实现）不入包，随 P1-5 ML 重做再议
- **导出**：`GET /services/v1/model/export/{id}` JSON 附件下载（RFC 5987 中文文件名）；
  **导入**：`POST /services/v1/model/import`（multipart + 显示名称/商户码/模型名）→ 新模型（INIT，构建后生效）
- **导入校验报告**（告警不阻断）：未安装插件 key（事件执行时隔离跳过）、Groovy 预编译告警、
  名单记录超阈值提示（默认 10 万行可配）；结构错误（格式版本/反序列化）才拒绝
- **整图事务**：导入任一插入失败全回滚，无"半个模型"；冲突（商户码+显示名称）拒绝
- 管理端：模型列表行内「导出」、页头「导入模型」弹窗（文件+命名+告警列表回显）
- 配置项 `model.package.max-upload-size`（10MB）/ `model.package.record-warn-threshold`
- `docs/model-package.md`（格式说明/导入语义/典型场景/故障排查）

### Changed
- **copyModel 装配段抽至 ModelGraphAssembler**（与策略包导入共用同一份 ID 重映射逻辑，
  copy 语义不变并新增单事务保护）；映射关系 activationId/dataListId 维持插入后重映射模式

### Verification
- L1/L2 单测 32/32 全绿（新增策略包 6 个：round-trip/格式版本拒绝/缺插件告警/Groovy 告警/名单阈值）
- L3 集成测试 `ModelPackageIntegrationTest`（Testcontainers）：种子模型导出→往返→导入→图完整性/ID 重映射/冲突回滚断言，随 CI 全量执行
- copy 重构回归：既有全链路 L3（模板复制→构建→事件命中）随 CI 执行
- 前端 vite build 通过，产物内嵌 jar

## [3.1.0] - 2026-11-21

插件运营版本（roadmap P2-6）：v3.0 解决插件"接得进来"，本版解决"管不起来"——
管理端插件生命周期（上传/启停/删除，热重载）、HTTP 出访管控三模式、
radar-plugin-api 3.1.0 向后兼容演进（options/version/provider/testkit）、远程插件官方示例。
规划详见 [docs/plan-v3.1.md](docs/plan-v3.1.md)。无 Breaking Changes，3.0.0 编译的插件 jar 零改动兼容。

### Added
- **管理端插件生命周期**：「插件管理」页（#/plugins，顶栏入口）——上传 jar（校验链：zip 结构/SPI 声明/
  key 冲突/50MB 上限）、列表（版本/提供方/来源/jar 文件/执行统计快照）、启用禁用（持久化
  `engine_plugin_registry`，重启保持）、删除、手动重载；后端 `/services/v1/plugins` 系列端点（JWT 鉴权）
- **热重载**：外置插件上传/删除后默认整代热重载即时生效（loader 代际替换，旧加载器宽限 30s 关闭，
  in-flight 执行由失败隔离兜底）；`plugin.admin.hot-reload=false` 回退重启生效模式；
  `plugin.admin.reload-grace-ms` / `plugin.admin.max-jar-size` 可配
- **出访管控三模式**：`plugin.http.mode=deny-private`（默认，v3.0 行为）/ `allowlist`
  （`plugin.http.allowed-hosts` 精确/通配 `*.host`，内网服务须显式列入）/ `off`；
  拦截指标 `radar_plugin_http_blocked_total{plugin}` + warn 日志；
  v3.0 `plugin.http.deny-private-network=false` 兼容映射为 off
- **radar-plugin-api 3.1.0**（向后兼容追加）：`ParamDescriptor.options()` + `ParamOption`
  （管理端渲染下拉框，value 恒为字符串）；`@PluginMeta.version()/provider()` 插件元信息展示
- **radar-plugin-testkit** 测试工件（零依赖）：`PluginRequestBuilder` / `MockHttpTool`（桩响应+调用录制）/
  `RecordingCache` / `TestContexts`——插件单测无需引擎与真实网络
- **远程插件官方示例** `samples/radar-plugin-remote-demo`（EXCHANGE_RATE 汇率换算：remote 外呼、
  options 下拉、10 分钟缓存、多列输出、4 个 testkit 单测），与本地形态 demo 合称官方两件套
- `PLUGIN_REGISTRY.md` 社区插件索引（内置/官方示例/社区插件三类）
- sql 增量脚本 `sql/upgrade-3.1.0.sql`（engine_plugin_registry 建表；radar-init.sql 基线同步）

### Changed
- `/services/v1/common/plugins` 仅返回启用条目，新增 version/provider 字段
- preItem 新增/编辑弹窗：paramSchema 含 options 时渲染下拉框（label 展示、value 入库）
- 插件开发指南增补：testkit 单测模板、插件管理运营、出访管控章节（§7-§10）
- radar-plugin-demo 示例工程迁移 testkit 写法并升级 api 3.1.0

### Verification
- `radar-plugin-api`/`radar-plugin-testkit` install 通过；两示例工程 test 全绿（4+4）
- 主工程 L1/L2 单测 26/26 全绿（新增 9 个：出访管控三模式与拦截计数 6、注册表禁用/重载语义 3、
  插件执行隔离回归 3），BUILD SUCCESS
- 前端 vite build 通过并同步产物内嵌 jar（scripts/build-frontend.sh）
- 覆盖率门禁（BUNDLE/LINE ≥40%，ci profile 全量 L1/L2/L3，基线 46%）随 CI 执行

## [3.0.0] - 2026-11-15

生态大版本（roadmap P2-1/P2-4）：preItem 插件 SDK 化——插件从"主 jar 内置数组"重构为可独立开发、
外置分发、自动发现、失败隔离的生态接口。后端引擎语义兼容（存量模型/配置零改动），API 形态 major bump。
规划详见 [docs/plan-v3.0.md](docs/plan-v3.0.md)，开发指南见 [docs/plugin-dev-guide.md](docs/plugin-dev-guide.md)。

### Added
- **radar-plugin-api 契约模块**（独立工程，零第三方依赖）：`Plugin` + `@PluginMeta`（key 与类名解耦）、
  `ParamDescriptor` 参数 schema（管理端动态表单数据源）、`OutputColumn` 输出列声明、
  类型化 `PluginRequest/PluginResult`、`KeyValueCache` 进程内 TTL 缓存、
  `HttpTool`（强制超时 + 私网目标防护，缓解 url 配置类 SSRF）、`PluginException` 异常契约
- **外置插件 jar**：`plugins/` 目录启动扫描（SPI + 子优先类加载器隔离，compose 挂载
  `./plugins:/radar/plugins`），数据服务厂商自助接入；同 key 冲突先到先得并告警
- **执行加固**：插件失败隔离（默认：错误标记 + 指标，事件链路继续；`plugin.isolation=strict` 回旧行为）、
  `radar_plugin_executions_total{plugin,result}` / `radar_plugin_seconds{plugin}` 指标
  （缺失插件归并 unknown 标签防基数爆炸）
- `/services/v1/common/plugins` 返回完整元信息（paramSchema/outputMeta/source/remote），
  管理端 preItem 表单按 schema **动态渲染**——新插件上架前端零改动；响应列由 outputMeta 自动生成
- 示例插件工程 `samples/radar-plugin-demo`（敏感数据识别脱敏，4 单测绿，已在测试服务器实测 EXTERNAL 装载）
- 插件开发指南 `docs/plugin-dev-guide.md`（契约/运行语义/存量兼容/v2→v3 迁移对照）
- GitHub 镜像仓 Gitee→GitHub 每小时自动同步 workflow（.github/workflows/sync-gitee.yml），
  CI/GHCR/Release 随主仓推进自动执行

### Changed
- 8 个内置插件全量迁移 v3 契约（**类名=存量 key 不变，DB 配置零改动兼容**）：
  IP2LOCATION searcher 启动加载一次并复用（旧实现每请求重建读盘）+ 结果缓存 1h；
  GPS2LOCATION 硬编码第三方地址移入 `plugin.gps.url` 配置并改走引擎 HTTP 工具；
  MOBILE2LOCATION 删除永不执行的失效百度 API 回填死代码；HTTP_UTIL url/reqType 进 schema、
  支持 `{字段名}` 占位符
- 4 处管理端调用方（抽象列/激活列/事件检索/ES 映射）迁移至 PluginEntry 统一元信息视图

### Removed
- V1 `PluginService`/`PluginServiceImpl`（2019 版方法级 API，v2.0 起已停用）及其残留注入字段

### Breaking Changes
- 插件开发 API 变化：新增插件请实现 v3 `Plugin` 接口（v2 `PluginServiceV2` 仍被适配兼容，
  计划随生态迁移逐步下线）
- 存量 preItem DB 配置零改动兼容；`plugin.isolation` 默认 isolated（插件失败不再拖垮整笔事件，
  需要旧行为设为 strict）

### Verification
- L1/L2 `mvn test` 17/17 绿（含新增 PluginIsolationTest 3 用例：隔离/strict/标签基数）
- 测试服务器（v3.0 正式件）实测：`plugins/` 外置示例 jar 重启装载为 EXTERNAL、
  `/common/plugins` 元信息完整、十步全链路验证通过（规则命中 reject/120）
- 示例插件单测 4/4 绿；前端 vite build 通过

## [2.4.1] - 2026-11-15

### Removed
- **机器学习配置页面**：模型工作区『机器学习配置』Tab 与 `/modelConfig/:id` 路由下线
  （与 Estimator 接口保留零实现、ML 重接暂缓的既定决策保持一致，UI 先行下线；
  后端 `/modelConfig` 端点未动，将来重接时恢复即可）。顺带清理无路由死代码
  `frontend/src/component/config/LearningConfig.jsx`（2022 版起即未接线）

## [2.4.0] - 2026-11-14

前端现代化版本（roadmap P1-7）：管理端前端源码入仓并完成 2022 → 2026 工具链迁移，后端零改动（v2.3 存储演进暂缓顺延，版本号保留）。
规划详见 [docs/plan-v2.4.md](docs/plan-v2.4.md)。

### Added
- **前端源码入仓**：radar-web（gitee.com/freshday/radar-web，2022-08 封存）经 git subtree 合入 `frontend/`，
  构建产物提交于 `src/main/resources/static/` 内嵌 jar，部署拓扑零变化（Dockerfile/compose 无 node 阶段）
- `scripts/build-frontend.sh`：npm ci → vite build → 同步 static/（保留 images/ res/）
- CI：Node 22 + 前端构建 + **产物同步校验**（`git diff --exit-code`，改前端不重建会挂）；
  release workflow 打包前重建兜底
- vite dev 代理目标支持 `RADAR_API_TARGET` 环境变量覆盖（联调非本机后端）
- `frontend/README.md`：开发/构建/迁移约定说明

### Changed
- **构建链**：webpack 4 + babel 7 + node-sass → **Vite 6**（Node 22；node-sass/IE8 兼容件/yarn.lock 全删）
- **技术栈**：React 16.8 → **18.3**、antd 3.16 → **5**、react-router 3 → **7**、echarts 3 → **5**、moment → **dayjs**
- **URL 与行为兼容**（2022 版基线逐页比对）：
  - hash 路由扁平 URL 原样保留（`#/field/:id`、`#/event`、`#/ruleid/...`）；
    rr3 嵌套绝对 path 结构以 Index/Model/Report 三层 pathless layout route 等价重建
  - 49 个 class 组件原样保留（React 18 兼容），不 hooks 化、不引 TypeScript、UI 不改版
  - 旧生命周期迁移（React 17+ 移除 `componentWillReceiveProps/WillMount` → `componentDidUpdate`）
  - antd 5 适配：`visible→open`、具名图标、Select `options`、Menu/Breadcrumb `items`、
    `Form.create→Form.useForm`、`ConfigProvider locale=zhCN`（antd5 默认 en_US）

### Fixed
- **CRUD 尾斜杠 400（v2.2.0 及之前已存在的老 bug，冒烟实测发现）**：
  老前端 CRUD URL 带尾斜杠（`/model/`、`/field/` 等），Spring 6 取消尾斜杠匹配后落到静态资源处理器报 400
  ——即旧前端在 Boot 3 后端上"新建模型（非模板）/删除模型/各实体 PUT"本来就是坏的。
  fetchUtil 统一剥离尾斜杠修复，真实后端实测新建/删除模型通过
- Login 未 import `Modal` 即调用 `Modal.error` 的潜在空引用（原 2022 代码隐患）

### Breaking Changes
- 前端构建需要 Node 22+（仅开发者；使用者产物已内嵌 jar，无感知）
- 对后端与部署零改动；浏览器基线升为现代常青浏览器（放弃 IE）

### Verification
- `npm run build` 产物 2.5M（对照旧 webpack 产物 3.4M）；CI 产物同步校验通过
- dev 代理直连测试服务器 v2.2.0 真实后端实测：注册（绕验证码）→ 登录 → 新建模型 →
  删除模型 → 创建必备字段（9 控件大表单）→ 字段/策略/抽象/名单/预处理/报表三页/配置中心/模型配置
  **九页走查全过**，数据真实落库
- 后端 L1/L2 `mvn test` 14/14 全绿（92 个种子脚本回归通过），零后端改动

## [2.2.0] - 2026-11-14

专业度版本（roadmap P1-1/P1-2）：JDK 21 + 虚拟线程、可观测体系、测试服务器部署流水线。
规划详见 [docs/plan-v2.2.md](docs/plan-v2.2.md)。

### Added
- **可观测体系**：Micrometer + Prometheus（`/actuator/prometheus`）+ Grafana 看板
  （compose `--profile monitoring` 一键起）；业务指标五类：事件吞吐、引擎六阶段耗时
  P50/P95/P99、激活决策分布、规则命中、Groovy 编译计数/耗时（标签纪律：不含 modelGuid 高基数维度）
- **部署流水线脚本**：`scripts/deploy-test.sh`（本地构建→scp→compose 起）、
  `scripts/server-bootstrap.sh`（Docker/JDK21/Maven/swap 初始化）、
  `scripts/verify-chain.sh`（部署后十步全链路验证）
- jacoco 覆盖率机制：`mvn verify` 出报告 + CI artifact；L3 全量行覆盖基线 **45.7%**（门禁阈值待定线）
- 压测框架：三组对照方法（A:17+关 / B:21+关 / C:21+开）+ JMeter 场景 `docs/benchmark/uploadInfo.jmx`，
  实跑数据待专用环境（见 [docs/benchmark-v2.2.md](docs/benchmark-v2.2.md)）

### Changed
- **JDK 17 → 21**（LTS）：pom / Dockerfile（temurin:21-jre）/ CI 三处一致；README/CONTRIBUTING 同步
- **虚拟线程默认开启**（`spring.threads.virtual.enabled=true`，需 JDK 21+）；开关去留待压测对照后定
- compose app 服务补 `build:` 声明，无 GHCR 访问环境可本地构建镜像

### Fixed
- `env.CI` 激活 ci profile 时 local(activeByDefault) 失效，资源过滤留下 `@profileActive@` 原始占位符
  导致 YAML 解析失败——pom 补项目级默认值（测试服务器 L3 实测暴露）
- 集成测试四件套改静态块显式启动：Spring 上下文先于 @Testcontainers 扩展回调加载时容器未就绪
- **Druid starter 不支持 Boot `ConnectionDetails`**（`@ServiceConnection` 对 MySQL 无效）：
  测试四件套连接统一改 `@DynamicPropertySource` 显式注入
- 全链路验证脚本 guid 字段大小写、activations 响应层级、频次规则需双事件三处修正

### Breaking Changes
- **运行时要求 JDK 21+**（虚拟线程前置）
- 其他对应用透明；开发环境说明见 CONTRIBUTING.md

### Verification
- 云服务器（Ubuntu 24.04 / 2C4G / Docker）实测：十步全链路绿（第二事件命中 score=120→reject）、
  `/actuator/prometheus` 41 个业务指标序列、**L3 全量 `mvn verify` 41/41 绿**、
  compose 常驻栈恢复 UP；JDK 21 下本地 L1/L2 全绿

## [2.1.0] - 2026-11-14

工程基础版本（roadmap P0 全量落地）：测试体系、CI/CD、容器化一键环境、社区门面。
规划详见 [docs/plan-v2.1.md](docs/plan-v2.1.md)。

### Added
- **测试三层体系**：
  - L1 纯单测（GroovyScriptUtil 缓存/调用、激活器 pass/review/reject 阈值边界）
  - L2 种子脚本兼容性闸门：`sql/radar-init.sql` 全部 92 个 Groovy 脚本在 Groovy 4 下编译+执行 100% 自动回归
  - L3 Testcontainers 集成测试：MySQL/MongoDB/Redis/ES 四件套容器 + `EngineFullChainTest` 十步全链路（注册→登录→copy 模板→build→enable→事件上传→规则命中→getScore→报表→检索），复刻 v2.0 手工冒烟
- **CI/CD**（GitHub 镜像仓 Actions）：`ci.yml` push/PR 全量测试（env.CI 自动激活集成测试）；`release.yml` tag 触发构建 GHCR 镜像 + GitHub Release（说明取自 CHANGELOG）
- **容器化**：Dockerfile（temurin 17-jre，无 node 阶段）+ docker-compose 一键环境（四件套 healthcheck + 种子数据自动导入 + `--profile app` 启动应用）
- 社区门面：CONTRIBUTING.md、issue 模板（bug/feature）、README_en 重写至 v2.0 技术栈

### Changed
- SQL 合并为 **v2.0.0 全量基线单文件** `sql/radar-init.sql`（由 MySQL 8.0 真实迁移导出并回灌验证，删除原 2019 基线 + 1.0.6/1.0.7/1.0.8 增量三文件，数据内容不变）
- 激活器阈值决策抽取为 `AntiFraudEngineImpl.riskLevel()`（行为不变，可单测）
- 中文 README 快速开始补 docker compose 方式与测试分层说明

### Fixed
- roadmap 中发布不规范表述修正（历史 tag 止于 v1.0.8）

### Breaking Changes
- 无（对应用透明；开发流程变化见 CONTRIBUTING.md：本地无 Docker 时集成测试自动跳过，CI 全量执行）

### Verification
- 本地 `mvn test` 14/14 全绿（L1+L2）；L3 容器链路由 CI 首跑验收
- 合并 SQL 经 MySQL 8.0 真实迁移 + 回灌双重验证（6 模型/35 规则/57 抽象/2 用户，中文完好）

## [2.0.0] - 2026-11-14

架构升级大版本：Java 17 + Spring Boot 3.5，8 模块合并为单模块单体应用。
详见 [docs/upgrade-plan-java17-boot3.md](docs/upgrade-plan-java17-boot3.md)，后续规划见 [docs/roadmap.md](docs/roadmap.md)。

### Added
- 技术演进 roadmap（docs/roadmap.md）：P0 工程基础 / P1 引擎竞争力 / P2 生态护城河
- CHANGELOG 发布规范（本文件）

### Changed
- Java 8 → **17**，Spring Boot 2.2.5 → **3.5.16**
- 8 个 Maven 模块合并为**单模块单体应用**：一个 jar 同时提供管理端与引擎端 API（默认端口 6580）
- 移除 dal 转发层：service 直接注入 Mapper；MapStruct、@Cacheable、Query/PageResult 保留并平移至 service
- 持久层：tk.mybatis + PageHelper → **MyBatis-Plus 3.5.x**（boot3 starter），分页由 selectPage 实现
- 依赖替换：fastjson → **fastjson2 2.0.66**、springfox → **springdoc-openapi**（API 文档路径 /swagger-ui/index.html，分组 admin / engine）、jjwt → **0.12.x**、Groovy → **4.0.x**、ES client → **7.17.x**、easyexcel → **4.x**、druid → **boot3 starter**
- samples/radar-kafka-demo 同步升级（Java 17 + Boot 3.5.16 + fastjson2），并移除未使用的 mysql / guava 依赖

### Removed
- TensorFlow DNN（TensorDnnEstimator）暂时移除；`Estimator` 接口与注册容器保留，重接方案（ONNX）见 roadmap P1-5

### Fixed
- EstimatorContainer 在零 Estimator 实现时（TF 移除后）数组注入导致启动失败，改为 `@Autowired(required=false)`（665790b）

### Breaking Changes
- 运行时要求 **JDK 17+**，Maven 构建配置见 `.mvn/settings.xml`
- 部署形态变化：原 admin / engine 双服务 → 单体 jar（端口 6580，管理端 UI、swagger、engine API 同端口）
- 移除的旧依赖（tk.mybatis / PageHelper / fastjson 1.x / springfox / TF）不再提供，二开代码迁移对照见 upgrade-plan 附录 A
- fastjson2 行为差异（autoType 默认关闭、日期格式）需在业务回归中关注

### Verification
- 全链路冒烟回归通过：注册/登录 → 模板 copy → build → enable → 事件上传 → 规则命中 → ES/Mongo 落库 → getScore → 报表/检索；管理端 CRUD、swagger、静态前端均正常
- **115 个种子 Groovy 脚本（2.4 时代）在 Groovy 4.0.33 下编译 + 执行 100% 通过**
