# Radar v3.1 实施方案：插件生态深化（管理运营、出访管控与 SDK 演进）

> 版本：v1.0（2026-11-21）
> 范围：P2-1 插件 SDK 化的生态延续——管理端插件生命周期、HTTP 出访管控强化、radar-plugin-api 3.1.0 向后兼容演进、生态交付物；P1-3/P1-4 存储演进、P1-5 ML 重接、P2-2/P2-3（挂起）均不在本版
> 状态：方案评审稿
> 前置：v3.0.0 已发布（插件 SDK 重构 + `cn.riskengine:radar-plugin-api:3.0.0` 上架 Maven Central）；roadmap 见 [roadmap.md](roadmap.md)，前置方案见 [plan-v3.0.md](plan-v3.0.md)

---

## 1. 背景与定位

v3.0 把插件从"内置数组"做成了"可接入的生态接口"：契约、外置 jar、失败隔离、动态表单、SDK 上架 Central。
但自助接入的最后一公里是**管理运营**，2026-11-21 对插件系统运行态的盘点结论是：**插件现在"接得进来，管不起来"**——

- 外置插件上线仍是"拷 jar + 重启引擎"（compose 注释原话"放入 ./plugins/*.jar 重启生效"），管理端无上传、无列表、无启停；
- 管理端可见的插件信息只有渲染配置表单所需的元信息，无版本、无来源、无执行统计（指标埋了，但要自己开 Grafana 看）；
- HTTP 出访只有 `deny-private-network` 单一开关，v3.0 方案明确把"白名单放行模式"列为后续；
- `ParamType` 仅 STRING/INTEGER/DOUBLE/BOOLEAN 四种原子类型，插件无法声明枚举选项（如 `format=JSON|XML` 只能 STRING + placeholder 口头约定），动态表单表达力受限；
- 官方示例只有 1 个本地纯计算插件，**远程插件（remote=true + 缓存 + 多列输出 + JSON 解析）没有参考实现**——这恰是数据服务厂商接入的主流形态；
- 插件单测要手搓 context（示例工程测试里 `new PluginContext(logger, lambda, lambda)`），缺正式 testkit。

v3.1 定位为**插件运营版本**：让管理员在管理端完成插件上线下线全流程，让厂商有远程插件的照抄模板，让 SDK 表达力补齐。
无 breaking change，API 只做向后兼容追加（3.0.0 → 3.1.0），符合 minor bump 语义。

| # | 目标 | 说明 |
|---|------|------|
| T1 | **管理端插件生命周期**：上传 jar → 注册生效（默认热重载，可配回退重启模式）、列表（版本/来源/执行统计）、启用/禁用、删除 | 告别 scp + 重启 |
| T2 | **出访管控强化**：deny-private / allowlist / off 三模式 + 拦截计数指标 | v3.0 列后续项收尾 |
| T3 | **SDK 3.1.0**：ParamDescriptor 增 options（枚举选项）、PluginMeta 增 version/provider、新增 radar-plugin-testkit 测试工件 | API 追加不破坏 |
| T4 | **远程插件官方示例**：完整演示 remote=true / options / 缓存 / 多列输出 / JSON 解析 / testkit 单测 | 厂商照抄模板 |
| T5 | **生态交付物**：开发指南运营章节、PLUGIN_REGISTRY.md 社区插件索引、README 生态章节刷新 | 生态信号 |

## 2. 现状盘点（2026-11-21 代码实证）

| 项 | 现状 | 证据 |
|----|------|------|
| 加载 | 单一 `PluginJarClassLoader` 启动时一次构建，无运行期重建入口；loader 是 URLClassLoader（**可 close()**，close 后持有的 jar 文件句柄释放，Windows 换文件才有路） | PluginManager.loadExternalPlugins:198 |
| 注册表 | `entries` ConcurrentHashMap，`register()` 同 key 先到先得；条目 PluginEntry 无版本字段 | PluginManager.java:65 / PluginEntry.java |
| 管理端点 | `GET /common/plugins` 只返回元信息（key/name/desc/source/paramSchema/outputMeta），无上传/启停/统计端点 | CommonApiController.java:60 |
| 前端 | 无插件管理页（页面只有 abstraction/activation/field/model/preItem/report 等业务页）；AddPreItem/EditPreItem 按 paramSchema 动态渲染 | frontend/src/component/ |
| 出访 | `GuardedHttpTool` 硬编码 deny-private 单模式（DNS 解析后判回环/私网/链路本地）；API 侧 `HttpTool.isAllowed()` 已预留 | PluginContextFactory.java:41,121,145 |
| 参数 schema | ParamType 四原子类型，`parse()` 类型化；ParamDescriptor 无 options 字段 | ParamType.java / ParamDescriptor.java |
| 插件元信息 | `@PluginMeta(key,name,desc,type,remote)` 无 version/provider；jar MANIFEST 未约定 | PluginMeta.java |
| 执行统计 | `radar_plugin_executions_total{plugin,result}` / `radar_plugin_seconds{plugin}` 已埋点（v3.0），MeterRegistry 可直读，但管理端点未暴露 | v3.0 T3 交付 |
| 示例与测试 | 仅 radar-plugin-demo（SensitiveDataPlugin 纯本地计算）；测试手搓 `new PluginContext(logger, cacheFactory, httpTool)` lambda | samples/radar-plugin-demo |
| 部署 | compose 挂 `./plugins:/radar/plugins`，注释"重启生效"；`radar.plugins-dir` 可配 | docker-compose.yml:92-95 |

## 3. 技术决策（定案，不再讨论）

| 决策 | 选择 | 理由 |
|------|------|------|
| 热重载模型 | **loader 整代替换**：重建新 URLClassLoader 覆盖 plugins/ 全部 jar → 清除旧 EXTERNAL 条目 → 注册新条目 → 旧 loader 延迟 close。**不做单类热替换**（那才是 v3.0 否决的高危形态）；`plugin.admin.hot-reload` 默认 true，false 时上传仅落盘并提示"重启生效" | 代际替换是插件系统标准安全模式；URLClassLoader.close() 原生支持；粒度粗但插件数量少（个位数），全量重建成本可忽略 |
| in-flight 保护 | 旧 loader 延迟 30s close（`plugin.admin.reload-grace-ms` 可配）：执行中的旧插件实例自然跑完；仍卡在旧类上的执行由隔离策略兜底（异常 → `__plugin_error__` + 计数，事件链路继续） | 复用 v3.0 失败隔离，不新增执行模型 |
| 上传校验 | zip 结构完整性 → `META-INF/services/com.radar.plugin.api.Plugin` 存在 → 临时 loader 试加载并提取 key → 与内置 key / 已上传 key 冲突即拒绝（422 带冲突详情）→ 大小上限（默认 50MB 可配）。**不做字节码审计与签名校验**（单租户管理员可信模型，同 v3.0 SSRF 立场；文档警示，签名列后续） | 校验目标是"传错"而不是"传毒"；前置校验避免坏 jar 毒化注册表 |
| 插件元信息 | `@PluginMeta` 追加 `version`/`provider` 可选属性（默认空串），注解优先、jar MANIFEST 兜底；PluginEntry 增 version/provider 字段，`/common/plugins` 返回 | 管理页展示与后续升级判断的数据基础；全可选，存量插件零改动 |
| 启停持久化 | 新表 `engine_plugin_registry`（key, source, jar 文件名, version, enabled, 上传时间）；禁用 = enabled=false，引擎执行链与 `/common/plugins` 双侧过滤；重启后保持 | 启停是最安全的"热"操作（纯注册表过滤，不碰类）；持久化避免重启后禁用丢失 |
| 删除/替换语义 | 删除 jar + 立即触发重载（条目消失）；同 key 重传 = 替换（走整代重载）；`hot-reload=false` 时删除/替换均标记"重启生效" | 文件系统即事实源，注册表是其视图 |
| Windows 文件锁 | 上传写临时文件后原子 move；删文件前先等旧 loader close（宽限期后）；文档标注 Windows 自部署需等宽限 | dev 机 Windows 占相当比例 |
| 出访管控 | `plugin.http.mode=deny-private`（默认，现行为）`|allowlist|off`；allowlist 用 `plugin.http.allowed-hosts`（精确或 `*.api.com` 通配，host 字符串匹配不做 DNS 反解）；拦截计数 `radar_plugin_http_blocked_total{plugin}`；`HttpTool.isAllowed()` 语义随 mode | 白名单是 v3.0 明确列的后续项；默认模式不变，存量零影响 |
| API 演进 | 仅向后兼容追加：ParamDescriptor 增 `options(): List<ParamOption>`（label/value 字符串对，**不加 SELECT 枚举值**——options 非空即渲染下拉，value 恒为字符串，parse 逻辑不变）；PluginMeta 增 version/provider。`radar-plugin-api` 3.0.0 → 3.1.0 minor bump | 枚举扩张会逼所有 switch 适配；options 复用 STRING 通道最小且前端够用 |
| testkit | 独立小工件 `radar-plugin-testkit`（PluginRequestBuilder / MockHttpTool / RecordingCache），插件工程 test scope 引用；demo 测试迁移为 testkit 写法 | 正式化"手搓 lambda"现状；独立工件避免 api 主件带测试依赖 |
| 管理页统计 | `/common/plugins` 附每插件 executions/failures/平均耗时**快照**（MeterRegistry 直读），不做历史曲线 | 历史曲线已有 Prometheus/Grafana，管理页只回答"它活着吗、在干活吗" |
| 远程示例 | 新增 `samples/radar-plugin-remote-demo`：汇率或天气类远程插件（remote=true、paramSchema 含 options、缓存 10min、多列 outputMeta、JSON 响应解析、testkit 单测），jar 内含单插件即可，指南强调"一 jar 多插件"同样支持 | 远程形态是厂商主流；与现有本地 demo 互补 |
| 社区索引 | 根目录 `PLUGIN_REGISTRY.md`：社区插件表格（名称/作者/链接/说明），PR 提交，纯文档不设 CI 校验 | 生态信号的低成本形态；插件分发继续走各自 Maven 仓/GH Release，主仓不做托管 |

## 4. 实施内容

### 4.1 T1：插件生命周期（后端）

- `PluginManager` 新增 `rebuildExternal()`：整代重载（§3 模型），`register()` 增加条目版本字段透传
- 新增 `PluginAdminService`：upload（校验链 §3）/ list（注册表 + MeterRegistry 快照合成）/ enable / disable / delete / reload
- 新增管理端点（挂现有 admin 鉴权）：`POST /admin/plugins`（multipart 上传）、`GET /admin/plugins`（全量含统计）、`PUT /admin/plugins/{key}/enabled`、`DELETE /admin/plugins/{key}`、`POST /admin/plugins/reload`
- `GET /common/plugins` 增补 version/provider/enabled 字段（配置表单侧过滤 disabled）
- SQL 增量脚本：`engine_plugin_registry` 建表（sql/ 目录，含升级说明）
- 配置项：`plugin.admin.hot-reload`（默认 true）、`plugin.admin.reload-grace-ms`（默认 30000）、`plugin.admin.max-jar-size`（默认 50MB）

### 4.2 T1：插件生命周期（前端）

- 新增"插件管理"页（主框架菜单接入）：列表列 = key / 名称 / 版本 / 提供方 / 来源（builtin-v2/builtin-v3/external）/ 状态（启用、禁用、重启生效待定）/ 执行次数 / 失败次数 / 平均耗时 / 操作（启停、删除）
- 上传弹窗（拖拽 + 校验错误回显，冲突 422 提示具体 key）；删除二次确认
- `hot-reload=false` 时上传/删除按钮显示"重启生效"徽标，不触发重载
- AddPreItem/EditPreItem 的插件下拉过滤 disabled 条目

### 4.3 T2：出访管控

- `PluginContextFactory.GuardedHttpTool` 按 mode 分派：deny-private（现逻辑不动）/ allowlist（host 匹配 `allowed-hosts`，不匹配即拦截）/ off（仅超时兜底）
- 拦截计数 `radar_plugin_http_blocked_total{plugin}`（复用 v3.0 指标体系）；拦截原因写入插件 logger（warn）
- 文档：三模式语义 + allowlist 通配规则 + "allowlist 与 deny-private 叠加建议"（allowlist 模式下私网目标仍拒绝，除非显式列入）

### 4.4 T3：radar-plugin-api 3.1.0 + testkit

- `ParamOption`（label/value）+ `ParamDescriptor.Builder.options(...)` + `ParamDescriptor.options()`；引擎 `parseArgs` 不变（options 值仍是字符串）
- `@PluginMeta` 增 `version()`/`provider()`（默认空串）；`Plugin` 接口默认方法不新增（注解即声明）
- 新模块 `radar-plugin-testkit`：`PluginRequestBuilder`（链式构造 sourceMap/args）、`MockHttpTool`（录制 + 桩响应）、`RecordingCache`；零业务依赖
- 发布：随 release.yml tag 驱动流水线上 Central（v3.0 P2-5 已就绪，幂等）

### 4.5 T4：远程示例插件

- `samples/radar-plugin-remote-demo`：remote=true 插件完整形态——paramSchema（含 options 下拉、INTEGER、BOOLEAN 三种形态）、`context.cache(10min)`、`context.http()` 调用、JSON 解析、多列 outputMeta、PluginException 用户可读消息、testkit 单测
- 与 SensitiveDataPlugin（本地形态）合起来构成"官方两件套"：指南里分"本地插件 / 远程插件"两章互相引用

### 4.6 T5：生态交付物

- `docs/plugin-dev-guide.md` 增补：插件管理页使用（上传/启停/删除）、热重载语义与宽限期、options 用法、version/provider 声明、testkit 单测模板、出访管控三模式 FAQ
- 根目录 `PLUGIN_REGISTRY.md`：内置插件清单 + 社区插件提交说明（表格模板）
- README 生态章节链接刷新（插件管理截图占位）

### 4.7 发布准备 v3.1.0

- 主 pom 3.0.0 → 3.1.0；CHANGELOG（Added/Changed/Fixed + 兼容性说明）；tag 触发镜像 + Central 自动流水线

## 5. 实施阶段

| 阶段 | 内容 | 产出 | 估计 |
|------|------|------|------|
| S0 | 方案评审 + api 3.1.0 追加项契约冻结 | 定稿 | 0.5 人日 |
| S1 | 后端生命周期：rebuildExternal / PluginAdminService / 端点 / SQL / 配置项 | 上传→重载链路可用（API 层） | 3 人日 |
| S2 | 前端插件管理页 + 表单侧过滤 | 管理闭环 | 2 人日 |
| S3 | 出访管控三模式 + 拦截指标 | T2 收尾 | 1 人日 |
| S4 | api 3.1.0（options/version/provider）+ testkit 模块 | SDK 可发布 | 1.5 人日 |
| S5 | 远程示例插件 + 指南增补 + PLUGIN_REGISTRY + README | 生态交付物 | 2 人日 |
| S6 | 回归（L1/L2 全绿 + L3 补插件场景：上传热重载 / 启停持久化 / 3.0.0 旧 jar 兼容 / allowlist）+ 发布准备 | 回归报告 + v3.1.0 | 1.5 人日 |

**合计约 11.5 人日**。S1 → S2 串行（端点先行）；S3、S4 与 S1 并行无依赖；S5 依赖 S4；S6 收尾。

## 6. 风险与对策

| 风险 | 对策 |
|------|------|
| 热重载类泄漏（旧 loader 未及时卸载，Metaspace 增长） | 代际替换 + 延迟 close；文档标注观察点（JMX classloader 数）；插件数量个位数场景重载频率极低；`hot-reload=false` 一键回退重启模式 |
| Windows 下换 jar 文件被锁 | close-before-delete + 上传临时文件原子 move；宽限期语义写进指南；compose/Linux 为推荐部署形态 |
| 上传恶意 jar（任意代码执行） | 结构/SPI/key 冲突校验挡"传错"；单租户管理员可信模型 + 文档警示（同 v3.0 SSRF 立场）；jar 签名校验列后续 |
| 重载窗口期 in-flight 执行失败 | 失败隔离兜底（`__plugin_error__` + 计数，事件链路继续）；宽限期覆盖绝大多数执行 |
| API 追加破坏 3.0.0 存量插件 | 全部为可选默认项（options 空、version/provider 空串）；L3 用例：3.0.0 编译的 jar 不重编译直接跑 |
| 上传接口成为运维面攻击点 | 挂现有 admin 鉴权；大小上限；无鉴权环境（demo）文档警示关闭管理页 |
| 覆盖门禁（≥40%）被新增代码稀释 | 校验链/重载/allowlist 均为纯逻辑可单测；S6 门禁核对 |
| Central 发布 | tag 驱动流水线已在 P2-5 就绪（幂等），复用，不新增工作量 |

## 7. 验收标准

1. 管理端上传含新插件的 jar → **无重启** → `GET /common/plugins` 可见（含 version/provider）→ 配置 preItem 后事件链路真实生效；管理页可见来源与执行统计快照
2. 启用/禁用即时生效且重启后保持（engine_plugin_registry 持久化）；删除 jar 触发重载后条目消失
3. `plugin.admin.hot-reload=false` 时：上传/删除仅落盘并明确提示"重启生效"，重启后行为正确（回退路径可验证）
4. 出访管控：allowlist 模式下白名单外目标被拦截且 `radar_plugin_http_blocked_total` 可见；默认 deny-private 行为与 v3.0 完全一致（存量回归）
5. `cn.riskengine:radar-plugin-api:3.1.0` 上架 Central；**3.0.0 编译的插件 jar 不重编译可在 v3.1 引擎运行**（含 v2 适配插件）；options 参数在管理端渲染为下拉且值正确入库/解析
6. 远程示例插件按指南 30 分钟内完成"上传 → 配置 → 事件生效"全流程；testkit 单测通过并成为指南模板
7. 插件开发指南运营章节、PLUGIN_REGISTRY.md、README 生态章节就绪；v3.1.0 按发布规范（tag + CHANGELOG + GitHub Release + GHCR 镜像）
8. L1/L2/L3 全绿，CI 覆盖门禁（BUNDLE/LINE ≥40%）保持
