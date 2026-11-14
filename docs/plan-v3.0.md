# Radar v3.0 实施方案：插件重构（preItems 插件 SDK 化与生态化）

> 版本：v1.0（2026-11-15）
> 范围：roadmap §4 P2-1 插件 SDK 化（本方案主体）、P2-4 镜像自动同步（附带）；P2-2 Groovy 沙箱 / P2-3 多租户不在本版
> 状态：方案评审稿
> 前置：v2.4.1 已发布（前端源码入仓、测试体系/CI/可观测就绪）；roadmap 见 [roadmap.md](roadmap.md)

---

## 1. 背景与定位

preItems 插件是 Radar 的头号卖点：事件进入规则运算前，通过插件把外部数据（IP 归属地、手机号归属地、
任意 HTTP 数据服务）挂载进事件上下文，abstraction/rules 直接消费。但 2026-11-15 对插件系统全量盘点的结论是：
**它目前是"内置数组"而非"生态接口"**——数据服务厂商无法自助接入，这正是 roadmap P2-1 的定性
（"插件系统是最大卖点但缺接口契约与文档"）。

v3.0 定位为**生态大版本**：把插件从主 jar 里的 8 个类，重构为可独立开发、独立分发、自动发现、
失败隔离的 SDK 体系。API 形态变化（发布即兼容性承诺）+ 加载机制变化，符合 major bump 语义。

| # | 目标 | 说明 |
|---|------|------|
| T1 | **插件 API 契约**（radar-plugin-api 独立模块）：类型化入出参、参数 schema、异常契约、javadoc | 替代裸 `Object handle()` |
| T2 | **加载机制**：plugins/ 目录外置 jar SPI 加载 + 内置插件全量迁移新 API | 厂商自助接入的前提 |
| T3 | **执行加固**：失败隔离、超时、结果缓存、HTTP 连接池/超时 | 插件失败 ≠ 整笔事件失败 |
| T4 | **管理端动态化**：/common/plugins 返回参数 schema，前端动态渲染配置表单 | 新增插件前端零改动 |
| T5 | **SDK 交付**：示例插件工程 + 插件开发指南文档 + artifact 发布 | 生态闭环 |

## 2. 现状盘点（2026-11-15 代码实证）

| 项 | 现状 | 证据 |
|----|------|------|
| 接口 | `PluginServiceV2`：`key()/desc()/getType()/getMeta()/handle(PreItemVO, Map, String[])`，出参裸 Object，零 javadoc、无异常/超时契约 | service/engine/PluginServiceV2.java（33 行） |
| 注册 | 纯 Spring bean 扫描：`PluginManager.getBeansOfType` 收进 ConcurrentHashMap，**key=类简名**；无 SPI（曾试 `SpringFactoriesLoader` 后弃，代码注释残留）；插件配置存 DB 表 `engine_pre_item` | service/impl/engine/plugin/PluginManager.java |
| 内置插件 | 8 个全挤在 `service/impl/engine/plugin/`：IP2LOCATION（本地 ip2region.db）、MOBILE2LOCATION（CSV 全量进内存 + 失效的百度 API 回填死代码）、GPS2LOCATION（**硬编码第三方 IP** 无超时）、HTTP_UTIL（通用外呼）、ALLINONE/SUBSTRING/SENSITIVE_TIME/DATEFORMAT（纯计算） | 同目录 8 个全大写类 |
| 执行 | `AntiFraudServiceImpl.prepare` 同步串行循环；`pluginManager.pluginServiceMap(item.getPlugin()).handle(...)` **无 null 检查无 try-catch**——插件配错名抛 NPE，整笔事件失败 | AntiFraudServiceImpl.java:121 |
| 性能隐患 | IP2LOCATION 每次 handle 重建 DbSearcher 读盘；远程插件无连接池、无超时、无结果缓存 | IP2LOCATION.java:68 等 |
| 安全面 | HTTP_UTIL 的 url 完全来自管理端 DB 配置，引擎侧无校验（SSRF 面，单租户自部署可接受但需文档警示 + 可选白名单） | HTTP_UTIL.java:43 |
| 前端 | AddPreItem.jsx 按 plugin key 逐个硬编码表单 UI 与 args 校验——**新增插件必须改前端**；后端 `getMeta()` 输出列元信息前端未消费（HTTP_UTIL 场景让用户手填 configJson） | AddPreItem.jsx:184-323 |
| 遗留 | V1 `PluginService`（@Deprecated 未删）+ `AntiFraudServiceImpl` 残留 V1 注入死字段 | PluginServiceImpl.java:31 |
| 可复用 | PluginManager 容器模式、`GET /common/plugins` 发现端点、Redis pub/sub 配置推送、Caffeine 本地缓存基建 | 与 EstimatorContainer 同构 |

## 3. 技术决策（定案，不再讨论）

| 决策 | 选择 | 理由 |
|------|------|------|
| 新契约 | 独立模块 `radar-plugin-api`（零 Spring 依赖，纯 Java），新接口 `Plugin` + `@PluginMeta` 注解声明元信息；**V2 保留适配层**（`PluginServiceV2` 旧插件经适配器继续工作），V1 删除 | 厂商依赖最小化；存量 DB 配置 plugin key（类简名）不变即完全兼容 |
| key 兼容 | 存量 8 插件**不改类名不迁移包名**（类简名=key 已写进种子与用户 DB）；新 API 插件 key 由注解显式声明，与类名解耦 | DB 兼容是底线 |
| 参数 schema | 新增 `paramSchema()` 返回 `List<ParamDescriptor>`（name/label/type/required/defaultValue/placeholder），替代自由字符串 args 的隐式约定；引擎按 schema 解析为类型化值传入 | 前端动态表单的数据源；args 字符串格式对 V2 适配插件保持私有 |
| 入出参 | 入参 `PluginRequest`（sourceField 值 Map、args 已解析 Map、原始事件引用、模型 id）；出参 `PluginResult`（单值 / 多列 Map / JSON 三形态），替代裸 Object | 类型安全 + 前端渲染可依赖 |
| 加载机制 | **plugins/ 目录启动扫描**：外置 jar 用独立 URLClassLoader 加载其中 `META-INF/services` SPI 实现；内置插件继续走 Spring 扫描，两路汇入同一注册表。**不做管理端上传热加载**（类卸载风险高，列后续） | 外置 jar 是"自助接入"的最小可行形态；热加载性价比低风险高 |
| 失败隔离 | 默认开启：单插件异常/超时 → 记 `radar_plugin_failures_total` 指标 + destField 标记 `__plugin_error__` + 引擎链路继续；可配 `plugin.isolation=strict` 回退旧行为（失败即事件失败） | 风控链路可用性优先；strict 保留给强一致场景 |
| 超时 | 远程型插件（声明 `remote=true`）默认 2s 强制超时（HTTP client 层实现，可配）；本地纯计算不设超时（线程 interrupt 不可靠，避免复杂线程池模型） | 同步串行模型下超时只对远程有意义；避免过度工程 |
| 缓存 | API 提供 `PluginContext.cache(ttl)`：Caffeine 包装的 computeIfAbsent（key 由插件自定义）；IP/手机号归属地类插件接入后查询量大幅下降 | 归属地数据天然冷热分明 |
| HTTP 工具 | API 提供带连接池/超时的 HttpClient 帮助类，替代逐请求 `HttpClients.createDefault()`；HTTP_UTIL 迁移至该工具 | 连接复用 + 超时兜底 |
| SSRF | `plugin.http.deny-private-network`（默认 true：禁私网/回环地址目标）+ 文档警示；白名单放行模式列后续 | 自部署默认安全、灵活性保留 |
| GPS2LOCATION | 硬编码第三方 IP 移入 `application.yml` 可配（保留原默认值 + 启动 warning：公共免费服务不可靠，建议替换） | 行为兼容 + 风险显性化 |
| MOBILE2LOCATION 回填 | 删除失效百度 API 回填死代码（MobileInfoServiceImpl:84-109 线程 start 被注释、API key 明文失效）；本地 CSV 逻辑保留 | 死代码 + 安全隐患 |
| IP2LOCATION | searcher 进静态单例（启动加载一次复用），配合缓存 API | 每请求读盘问题实证 |
| 前端 | AddPreItem 表单按 `/common/plugins` 返回的 paramSchema + outputMeta **动态渲染**，删除逐插件 if-else；V2 适配插件（无 schema）保留旧硬编码路径兜底 | 渐进迁移，不断旧配置 |
| SDK 发布 | `radar-plugin-api` artifact：v3.0 先随主仓多模块产出 + `mvn install` 手动可用；Maven Central 发布在 GitHub 镜像仓 CI 就绪后由 release workflow 追加（deploy 插件 + Central 凭证） | 不阻塞 v3.0 主线；发布渠道独立演进 |
| 示例 | `samples/radar-plugin-demo`：完整可跑插件（含 paramSchema/缓存/单测/plugins 目录部署说明），兼作文档的活样例 | 厂商自助接入的最低门槛 |

## 4. 实施内容

### 4.1 T1：radar-plugin-api 模块（契约层）

```
radar-plugin-api/
  com.radar.plugin.api
    ├ @PluginMeta(key, name, desc, type, remote)      // 注解式元信息（可选，注解优先于方法覆写）
    ├ Plugin                                           // 核心接口
    │   ├ meta() / outputMeta(): List<OutputColumn>    // 输出列（替代 getMeta 的 JSON 字符串）
    │   ├ paramSchema(): List<ParamDescriptor>         // 入参描述（前端动态表单数据源）
    │   └ execute(PluginRequest, PluginContext): PluginResult
    ├ PluginRequest / PluginResult / OutputColumn / ParamDescriptor
    ├ PluginContext                                    // cache(ttl) / logger / httpClient / 工具入口
    └ PluginException                                  // 可携带用户可读 msg
```

- 主 pom 改多模块（`radar-plugin-api` + 原单模块改名 `radar-app` 或保持默认目录不动、以 profile 聚合——**采用最小结构变更：新增子模块 + 原代码目录不动**，避免大规模路径迁移）
- V2 适配器：`PluginServiceV2Adapter implements Plugin`，meta 缺省从类简名/desc/getType 推导，paramSchema 返回空（前端走旧兜底）

### 4.2 T2：加载机制（PluginManager v2）

- 注册表合并两路：Spring 扫描（内置 + 适配器）∪ plugins/ 目录 SPI（`ServiceLoader.load(Plugin.class, pluginClassLoader)`）
- plugins/ 目录路径可配（默认 `${user.dir}/plugins`，compose 挂载卷），启动扫描 + 冲突检测（同 key 拒绝并告警）
- 注册表条目携带来源（builtin/external）与 paramSchema，`GET /common/plugins` 返回全量元信息（含 schema 与 outputMeta）
- 删除 V1 `PluginService/PluginServiceImpl` 与 `AntiFraudServiceImpl` V1 死字段

### 4.3 T3：执行加固（AntiFraudServiceImpl.prepare 重写）

- 插件执行包装：null 检查（配置了不存在的插件 → 指标 + 标记 + 继续）→ try-catch → remote 型超时 → 结果写回
- 指标：`radar_plugin_executions_total{plugin,result}`、`radar_plugin_seconds{plugin}`（复用 v2.2 EngineMetrics 体系，无 modelGuid 标签）
- 内置插件迁移清单（新 API 重写 = 官方参考实现）：

| 插件 | 迁移动作 |
|------|---------|
| IP2LOCATION | searcher 单例化 + 接缓存 API |
| MOBILE2LOCATION | 删回填死代码，保留内存查询 |
| GPS2LOCATION | URL 可配化 + HttpClient 工具 + remote 声明 |
| HTTP_UTIL | HttpClient 工具 + remote + 私网拦截 + outputMeta 保留用户 configJson 兼容 |
| ALLINONE/SUBSTRING/SENSITIVE_TIME/DATEFORMAT | 直接重写（纯计算，最简单的参考实现） |

### 4.4 T4：管理端动态表单

- AddPreItem.jsx：选中插件后按 paramSchema 动态生成控件（text/number/boolean/select），outputMeta 自动生成响应列 configJson 预览；无 schema 的 V2 适配插件走现有硬编码分支
- preItem 详情/编辑同步支持

### 4.5 T5：SDK 交付物

- `samples/radar-plugin-demo`：示例插件（选"身份证前 6 位→省市归属"或"天气 API"类真实场景），含 paramSchema + 缓存 + 单测 + 部署说明
- `docs/plugin-dev-guide.md`：契约详解、schema 编写、缓存/HTTP 工具、打包（SPI 声明）、plugins/ 部署、调试方法、V2→V3 迁移对照
- README 增"插件生态"章节入口

### 4.6 附带：P2-4 GitHub 镜像自动同步

- GitHub 侧 workflow：定时 `git fetch gitee && git push` 双向同步（Gitee 主仓 → GitHub 镜像为单向定时即可），替代手动同步；顺带解决 v2.1 以来的 CI/GHCR/Release 遗留

## 5. 实施阶段

| 阶段 | 内容 | 产出 | 估计 |
|------|------|------|------|
| S0 | 多模块结构改造 + radar-plugin-api 契约冻结（API 草案评审） | api 模块可 install | 2 人日 |
| S1 | PluginManager v2：双路注册 + plugins/ 目录 SPI + 冲突检测 + /plugins 元信息端点 | 外置 jar 可被发现 | 2 人日 |
| S2 | 8 个内置插件迁新 API + V2 适配层 + V1 删除 | 内置插件全量新 API | 2 人日 |
| S3 | 执行加固：隔离/超时/缓存/HttpClient 工具/指标 + 实证 bug 修复 | prepare 重写 | 2.5 人日 |
| S4 | 前端动态表单（AddPreItem/编辑） | 新插件前端零改动 | 2 人日 |
| S5 | SDK 交付：示例工程 + 开发指南 + README 章节 | 生态闭环 | 2 人日 |
| S6 | 回归（L1/L2 全绿 + L3 补插件场景用例：隔离/超时/外置 jar）+ 测试服务器部署验证 | 回归报告 | 1.5 人日 |
| S7 | 镜像同步 workflow + 发布准备 v3.0.0 | 按发布规范 | 1 人日 |

**合计约 15 人日**。S0→S1→S2 串行（契约先行），S3/S4 依赖 S0-S2，S5 与 S6 可部分并行。

## 6. 风险与对策

| 风险 | 对策 |
|------|------|
| API 一旦发布即兼容承诺，草案考虑不周 | S0 契约评审关（对照本方案 §3 决策逐条过）；发布前 API 以 v3.0.0-SNAPSHOT 演进 |
| 外置 jar 类冲突（厂商带 Spring/Jackson 等传递依赖） | URLClassLoader child-first 隔离 + 文档明确 api 模块依赖 exclusions；api 零依赖原则 |
| 插件失败隔离改变存量行为（原来失败=事件失败，现在跳过） | `plugin.isolation` 开关保留旧行为；CHANGELOG Breaking 标注；存量无配置=新默认隔离 |
| 超时对本地插件无效（同步模型 interrupt 不可靠） | 明确文档：超时仅约束远程型；本地插件性能问题靠缓存与自身优化 |
| 前端动态表单 schema 来自 jar，存在恶意 schema 可能 | 单租户自部署模型下管理员即运维者，风险可接受；文档警示 |
| 存量 preItem DB 配置兼容 | plugin key（类简名）不变原则 + L3 补存量配置回归用例 |
| Maven Central 发布依赖镜像仓 CI | v3.0 先 `mvn install` 手动模式，Central 发布独立小版本追加 |

## 7. 验收标准

1. `radar-plugin-api` 模块独立、零 Spring 依赖、javadoc 完整；`mvn install` 后示例插件工程可独立编译
2. 示例插件 jar 放入 plugins/ 目录重启后，`GET /common/plugins` 可见（含 paramSchema），配置后事件链路真实生效
3. 8 个内置插件全部运行于新 API（或 V2 适配器），存量 DB preItem 配置零改动兼容，L1/L2/L3 全绿
4. 故障注入验证：配错插件名/远程超时/插件抛异常，事件链路继续（隔离模式）且指标可见；strict 模式回旧行为
5. 管理端新插件零前端改动：paramSchema 动态渲染表单 + outputMeta 自动响应列
6. 插件开发指南 + 示例工程 + README 生态章节就绪；v3.0.0 按发布规范（tag + CHANGELOG + GitHub Release + GHCR 镜像）
7. P2-4：GitHub 镜像仓定时自动同步生效，CI 在镜像仓自动跑通（含 GHCR 镜像 + Release）
