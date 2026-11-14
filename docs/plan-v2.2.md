# Radar v2.2 实施方案：专业度（JDK 21 + 虚拟线程 + 可观测性）

> 版本：v1.0（2026-11-14）
> 范围：roadmap §3 P1-1 虚拟线程、P1-2 可观测性，附带 v2.1 遗留的 jacoco 覆盖率机制
> 状态：方案评审稿
> 前置：v2.1.0 已发布（测试三层 + CI + compose 就绪），见 [roadmap.md](roadmap.md)、[plan-v2.1.md](plan-v2.1.md)

---

## 1. 背景与目标

v2.1.0 补齐了工程基础，但性能弹性无法证明（"100ms 响应"无数据背书）、运行状态不可观测。
v2.2 主打**专业度**：让性能弹性可测量、让运行状态可视。

| # | 目标 | 对应 roadmap |
|---|------|--------------|
| T1 | JDK 17 → **21**（LTS）——虚拟线程的前置条件 | P1-1 前置 |
| T2 | 虚拟线程启用 + **压测对照报告**（性能弹性有数据） | P1-1 |
| T3 | Micrometer + Prometheus + Grafana 可观测体系（引擎各阶段 P99 可视） | P1-2 |
| T4 | jacoco 覆盖率报告 + 门禁机制（v2.1 遗留） | — |

**版本与分支**：v2.2.0（semver minor），分支 `feature/v2.2-professional-grade`。

## 2. 现状盘点（2026-11-14 实测）

| 项 | 现状 | 对 v2.2 的意义 |
|----|------|----------------|
| JDK | 本机默认 17（`D:\Java\jdk-17.0.12`）；**JDK 21.0.2 已备于 `D:\Java\jdk-21`**（zip 版，不占系统 PATH），21 下 `mvn package` + 14 测试已验证全绿 | T1 本地环境就绪，兼容性已实证 |
| 线程模型 | Tomcat 默认池；`AsyncConfig.riskResultExecutor`（8~32 线程、队列 1000、CallerRuns 兜底）承担事件结果异步写入 | T2 的开关点与背压语义确认点 |
| 可观测 | actuator 已在依赖；**无 prometheus registry、无自定义业务指标**；compose 已有 profile 机制（app） | T3 基建缺口明确 |
| 引擎链路 | entity/fields → preItems → abstractions → rules → adaptation → activation 六段（`AntiFraudEngineImpl.execute*` 各方法） | T3 阶段 Timer 的现成埋点位 |
| Groovy 缓存 | `GroovyScriptUtil.passedClassMap`（md5 → GroovyObject） | 编译计数/耗时指标点 |
| CI | `setup-java` temurin **17**；`env.CI` 自动跑全量测试 | T1 需同步改 21 |

## 3. 技术决策（定案，不再讨论）

| 决策 | 选择 | 理由 |
|------|------|------|
| JDK 21 落点 | pom `java.version=21` + Dockerfile `temurin:21-jre` + CI `setup-java` 21，三处一致 | 编译目标与运行时统一，避免混跑 |
| 虚拟线程形态 | `spring.threads.virtual.enabled=true` **默认开启**，压测后定去留 | 开关一行；若 pinning 损益严重则回退为可选开关（`virtual` profile）并留优化项 |
| riskResultExecutor | **保留现有 ThreadPoolTaskExecutor** 不动 | 它有队列 + CallerRuns 背压语义（防结果丢失），非请求路径；虚拟线程收益主要在 Tomcat 请求线程。压测中一并对照，数据说话 |
| 指标标签基数 | **不加 modelGuid 维度标签**（防基数爆炸）；模型维度分析已有 hitsSort 报表承担 | Prometheus 指标纪律 |
| 压测工具 | JMeter（场景脚本进 repo `docs/benchmark/`） | 可复跑、可出报告；wrk 不便写断言 |
| 监控栈 | compose `--profile monitoring`（prometheus + grafana 两容器），dashboard JSON 进 repo | 复用 v2.1 compose 约定 |
| jacoco 门禁 | 机制先行（prepare-agent + verify 报告 + CI artifact），**阈值数字拿到首跑基线后中期定** | 不拍脑袋设线 |

## 4. 实施内容

### 4.1 T1：JDK 21 升级（前置）

- pom `java.version=21`；Dockerfile 基础镜像换 `temurin:21-jre`；`ci.yml`/`release.yml` `setup-java` 改 21
- README / CONTRIBUTING 环境说明同步
- **全量回归**：本地 L1/L2（JDK 21 已验绿）+ CI L3（容器链路，setup-java 已换 21）

### 4.2 T2：虚拟线程 + 压测对照

- 开关：`application.yml` 增加 `spring.threads.virtual.enabled: true`
- **三组对照**（同一中间件环境，compose 起）：

| 组 | JDK | 虚拟线程 |
|----|-----|----------|
| A | 17 | 关 |
| B | 21 | 关 |
| C | 21 | 开 |

- 场景：`POST /services/v1/uploadInfo`（引擎主路径）与 `GET /services/v1/getScore`，并发 1 / 50 / 200 三档
- 指标：RPS、P50/P95/P99、错误率；JFR 采集 `jdk.VirtualThreadPinned` 事件定位 pinning（嫌疑：Druid 连接池、Groovy 类加载）
- 产物：`docs/benchmark-v2.2.md` 对照报告 + 结论（开/关/回退优化项）
- 注意：B−A 隔离"JDK 本身收益"，C−B 才是虚拟线程净收益

### 4.3 T3：可观测性

- 依赖：`micrometer-registry-prometheus`；`management.endpoints.web.exposure.include` 加 prometheus,health,info
- 自定义指标（新增 `config/EngineMetrics`，注入各 execute* 方法）：

| 指标 | 类型 | 标签 |
|------|------|------|
| radar_events_total（成功/失败） | Counter | result |
| radar_engine_stage_seconds（六阶段） | Timer | stage |
| radar_activation_decision_total | Counter | risk（pass/review/reject） |
| radar_rule_hits_total | Counter | — |
| radar_groovy_compiles_total / _seconds | Counter / Timer | — |

- compose 增加 `monitoring` profile：prometheus（挂载 `docs/monitoring/prometheus.yml` 抓 `app:6580`）+ grafana（datasource 自动 provisioning + dashboard JSON 进 `docs/monitoring/grafana/`）
- 验收图：事件 QPS、六阶段 P50/P99、决策分布饼图

### 4.4 T4：jacoco

- pom：`jacoco-maven-plugin`（prepare-agent + `report@verify`）；CI 上传报告 artifact
- 门禁机制：`check` rule 占位（threshold 待首跑基线数据），写明"基线 ± 合理裕量"的定线方法

## 5. 实施阶段

| 阶段 | 内容 | 产出 | 估计 |
|------|------|------|------|
| S1 | JDK 21 三处切换 + 全量回归 | 21 统一 | 1 人日 |
| S2 | 可观测指标埋点（T3 前半） | /actuator/prometheus 可抓 | 2 人日 |
| S3 | monitoring profile + Grafana 看板 | 一键可视化 | 1 人日 |
| S4 | 虚拟线程开关 + 三组压测 + 报告 | benchmark-v2.2.md | 2 人日 |
| S5 | jacoco 机制 | 报告 + 门禁占位 | 0.5 人日 |
| S6 | 文档（README 性能页）+ 发布准备 v2.2.0 | 按发布规范 | 0.5 人日 |

**合计约 7 人日**。S1→S2→S3→S4 串行（**可观测先于虚拟线程**——压测要用阶段 Timer 读 P99），S5/S6 可并行。

## 6. 风险与对策

| 风险 | 对策 |
|------|------|
| synchronized pinning 导致虚拟线程负收益（Druid 1.2.28、Groovy 4 类加载） | JFR pinning 事件定位；对照数据说话；必要时回退为 `virtual` profile 可选开关并记 roadmap 优化项（JDK 24 彻底解决 pinning） |
| 指标基数爆炸 | 已定案不加 modelGuid 标签；新增指标 PR 自查标签基数 |
| 压测环境噪声（本机 Windows + 混杂服务） | 压测统一用 compose 隔离中间件；报告记录环境；后续可移 CI 复跑 |
| ES client（Apache HC 4.x）自管线程与虚拟线程交互未知 | HC 为 async 自管线程池，理论不受影响；压测期间观察线程数与错误率 |
| L3 测试在 CI 尚未首跑（v2.1 遗留） | S1 同步触发（GitHub 镜像同步后）；若暴露容器问题先修再继续 |
| 本地压测无 Docker | 中间件用 compose 起（需 Docker Desktop）；纯 JDK 对照可在本机双 JDK 进行 |

## 7. 验收标准

1. pom / Dockerfile / CI 三处 JDK 一致为 21，全量测试绿
2. `spring.threads.virtual.enabled=true` 默认生效；`docs/benchmark-v2.2.md` 含三组对照数据与明确结论
3. `/actuator/prometheus` 可抓取；Grafana 展示事件 QPS / 六阶段 P99 / 决策分布；`docker compose --profile monitoring up` 一键起看板
4. jacoco 报告 CI 产出，门禁机制就绪（阈值注明基线数据来源）
5. v2.2.0 按发布规范：tag + CHANGELOG + GitHub Release + GHCR 镜像
