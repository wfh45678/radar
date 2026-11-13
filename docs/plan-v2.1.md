# Radar v2.1 实施方案：工程基础

> 版本：v1.0（2026-11-14）
> 范围：roadmap §2 P0-1 ~ P0-5（测试、CI、Docker/compose、发布规范收尾、社区门面）
> 状态：方案评审稿
> 前置：v2.0.0 已发布（tag + CHANGELOG），见 [roadmap.md](roadmap.md)、[upgrade-plan-java17-boot3.md](upgrade-plan-java17-boot3.md)

---

## 1. 背景与目标

v2.0.0 完成技术栈现代化并已按规范发布。但工程基础仍是短板：核心引擎零测试覆盖、无 CI、
无容器化交付，大版本升级依赖手工冒烟（v2.0 回归耗时且不可复用）。

| # | 目标 | 对应 roadmap |
|---|------|--------------|
| 1 | 测试资产化：引擎核心单测 + Testcontainers 集成测试，v2.0 手工回归场景固化为自动化 | P0-1 |
| 2 | GitHub Actions CI：PR 门禁 + master 构建 + 镜像发布 | P0-2 |
| 3 | `docker compose up` 一键起环境跑通 demo | P0-3 |
| 4 | 发布规范收尾：GitHub Release / 镜像发布 / roadmap 表述修正 | P0-4 |
| 5 | 社区门面：CONTRIBUTING、README_en、issue/PR 模板 | P0-5 |

**版本与分支**：v2.1.0（semver minor），分支 `feature/v2.1-engineering-basics`。

## 2. 现状盘点（2026-11-14 实测）

| 项 | 现状 | 影响 |
|----|------|------|
| 测试 | 3 个测试类：CryptUtilTest（纯单测）；MapStructTest / PluginTest 均为 `@SpringBootTest`，需 MySQL/Mongo/Redis 全套环境 | 裸 CI 无法运行；`mvn package` 一直 `-DskipTests` |
| 测试依赖 | spring-boot-starter-test 已含 JUnit 5；未引入 Testcontainers / jacoco | 基建缺位但无需大动 |
| 中间件 | 应用依赖 MySQL、MongoDB、Redis、ES 四件；**主工程无 Kafka 依赖**（事件走 HTTP，kafka-demo 是外部生产者） | compose 四件套即可，无需 Kafka |
| 种子资产 | `sql/radar-init.sql` 含全量模型元数据（含 115 个 2.4 时代 Groovy 脚本）；v2.0 冒烟回归场景完整记录 | 回归脚本化有现成蓝本 |
| 前端 | 预构建静态文件位于 `src/main/resources/static`（index.html + hash css） | Dockerfile 无需 node 构建阶段 |
| CI/容器 | 无 .github、无 Dockerfile/compose | 全部新建 |

## 3. 技术决策（定案，不再讨论）

| 决策 | 选择 | 理由 |
|------|------|------|
| 测试分层 | 纯单测（无容器）+ 集成测试（Testcontainers，`@Tag("integration")`） | 单测秒级反馈；集成可按环境跳过 |
| MySQL 模拟方案 | **不用 H2**，一律 Testcontainers `mysql:8` | MyBatis-Plus 方言与 MySQL 函数（如 `DATE_FORMAT`）在 H2 下风险高 |
| Testcontainers 接入 | `spring-boot-testcontainers` + `@ServiceConnection`（Boot 3.1+ 官方方式） | 免写 dynamic property 胶水代码 |
| CI 平台 | GitHub Actions（ubuntu-latest，自带 Docker） | GitHub 为镜像仓且免费；Gitee 主仓手动同步触发（自动化同步留 P2-4） |
| 基础镜像 | `eclipse-temurin:17-jre`（运行层）；可选 Spring Boot 分层 jar 优化层缓存 | 无前端构建，单阶段足够 |
| compose 画像 | mysql:8 / mongo:4.4 / redis:7 / elasticsearch:7.17.x 单节点；app 服务放 `profile: app` 可选 | ES 限堆 512m，笔记本可跑 |

## 4. 测试方案（P0-1，本版核心）

### 4.1 三层结构

| 层 | 内容 | 外部依赖 | 运行时机 |
|----|------|----------|----------|
| L1 纯单测 | `GroovyScriptUtil`（编译缓存/执行/失效）、Operator 求值、激活阈值决策（high/median → reject/review/pass）、`JsonParserUtil`、CryptUtils | 无 | 每次 `mvn test` |
| L2 种子脚本回归 | 115 个种子 Groovy 脚本编译 + 执行断言，从 `sql/radar-init.sql` 一次性提取为测试 fixture | 无 | 每次 `mvn test` |
| L3 集成测试 | ① mapper CRUD（改造现有 MapStructTest）② 模型 copy→build→enable ③ 事件上传→规则命中→ES/Mongo 落库→getScore 断言（v2.0 手工回归全场景）④ Redis pub/sub 配置刷新 ⑤ hitsSort 报表 | Testcontainers 四件套 | `mvn verify` / CI |

### 4.2 关键改造点

- **测试基建**：`AbstractIntegrationTest` 基类（`@Testcontainers` + 四容器声明 + `@ServiceConnection`）；`application-test.yml` 隔离配置
- **可测性微重构**：`executeActivation` 阈值决策等纯逻辑若与 Spring 上下文耦合，抽为可独立构造的组件（行为不变，最小改动）
- **现有测试处置**：CryptUtilTest 保留为 L1；MapStructTest / PluginTest 移入 L3 并改造，不再拖累 `mvn test`
- **fixture 提取**：一次性脚本从 radar-init.sql 抽取 115 个脚本 + 输入样例 → `src/test/resources/fixture/`

### 4.3 覆盖目标（务实，不搞数字 KPI）

- L1 / L2 全绿且纳入默认构建；L3 全链路冒烟场景 100% 固化
- 引入 jacoco 仅出报告供 CI 展示，v2.1 不设强制覆盖率门禁（留给 v2.2 视基线再定）

## 5. CI 方案（P0-2）

```yaml
# .github/workflows/ci.yml（示意）
on: [push, pull_request]        # 分支: master + feature/**
steps:
  - setup-java: temurin 17      # + actions/cache maven 仓库
  - mvn -B verify               # L1+L2+L3 全跑，产出 jacoco 报告 artifact
# .github/workflows/release.yml：tag v* 触发 → 构建 Docker 镜像 → 推 GHCR
```

- Gitee 主仓仍手动双推；文档写明"GitHub 镜像同步后 CI 才触发"
- 建议 master 开分支保护（要求 CI 通过）——GitHub 侧生效

## 6. Docker / compose 方案（P0-3）

- **Dockerfile**：temurin 17-jre + `radar.jar`，`EXPOSE 6580`，TZ=Asia/Shanghai；可选 layertools 分层加速镜像重建
- **docker-compose.yml**：四件套 + healthcheck；mysql 挂载 `sql/radar-init.sql` 容器首启自动建库；ES `discovery.type=single-node` + 堆 512m；`--profile app` 时启动本应用
- **验收即用户体验**：`docker compose up -d` → localhost:6580 注册登录 → copy 模板 → 事件命中规则

## 7. 发布规范收尾（P0-4）与社区门面（P0-5）

- P0-4 剩余：v2.1.0 发布时 GitHub Release（release note 引用 CHANGELOG）+ GHCR 镜像；修正 roadmap §1.2"无版本 tag"表述（实情：历史 tag v1.0.4~v1.0.8 存在，1.0.8 后未再打）
- P0-5：`CONTRIBUTING.md`（环境要求/构建/测试/PR 流程）、README_en.md 重写至 v2.0 技术栈 + compose 快速开始、issue/PR 模板、中文 README 补 compose 一节

## 8. 实施阶段

| 阶段 | 内容 | 产出 | 估计 |
|------|------|------|------|
| S1 测试基建 | Testcontainers 依赖、基类、application-test.yml、存量测试归类 | `mvn test` 快速全绿 | 1 人日 |
| S2 L1+L2 | 引擎核心单测 + 115 脚本 fixture 化 | 引擎命脉覆盖 | 2 人日 |
| S3 L3 集成 | 全链路冒烟自动化（v2.0 回归场景复刻） | `mvn verify` 一键回归 | 2 人日 |
| S4 CI | ci.yml + release.yml | PR 门禁 + GHCR 镜像 | 1 人日 |
| S5 Docker/compose | Dockerfile + compose + 实测 | 一键起环境 | 1.5 人日 |
| S6 门面收尾 | CONTRIBUTING / README_en / 模板 / roadmap 修正 | 门面达标 | 1 人日 |

**合计约 8.5 人日**（含 10% 缓冲）；S1→S3 串行，S4 依赖 S3，S5/S6 可并行。

## 9. 风险与对策

| 风险 | 对策 |
|------|------|
| 本地无 Docker Desktop，L3 跑不了 | `@Tag("integration")` 隔离，本地可 `-DexcludedGroups=integration`；CI 必跑 |
| GitHub runner 内存吃紧（4 容器 + 应用） | ES 限堆 512m、mongo/redis 轻量默认；runner 7GB 实测足够，超出再拆 job |
| radar-init.sql 与 mysql:8 导出格式兼容性 | S1 首日先验证导入，不兼容则调整 dump 参数 |
| `@SpringBootTest` 需全部中间件就绪才能起上下文 | AbstractIntegrationTest 统一编排容器生命周期，`@ServiceConnection` 自动注入连接 |
| 种子脚本 fixture 解析成本 | 一次性脚本生成，人工只抽检 |
| CI 依赖 GitHub 镜像手动同步，容易忘 | CONTRIBUTING 写明双推步骤；P2-4 自动同步后消除 |

## 10. 验收标准

1. `mvn verify` 一键全绿：L1/L2/L3 含 v2.0 全部冒烟场景，无手工步骤
2. GitHub PR 出现 CI 状态门禁；master 构建产出 jacoco 报告
3. `docker compose up -d` 后按 README 步骤跑通注册→建模型→命中规则全流程
4. v2.1.0 发布：tag + CHANGELOG + GitHub Release + GHCR 镜像齐备
5. CONTRIBUTING / README_en / issue 模板上线，roadmap 表述修正完成
