# Changelog

本项目所有重要变更记录于此，格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)，版本号遵循 [semver](https://semver.org/lang/zh-CN/)。

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
