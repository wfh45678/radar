# Changelog

本项目所有重要变更记录于此，格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)，版本号遵循 [semver](https://semver.org/lang/zh-CN/)。

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
