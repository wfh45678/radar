# Changelog

本项目所有重要变更记录于此，格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)，版本号遵循 [semver](https://semver.org/lang/zh-CN/)。

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
