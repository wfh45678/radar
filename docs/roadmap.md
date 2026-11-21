# Radar 开源项目技术演进 Roadmap

> 版本：v1.4（2026-11-21：开源健康度盘点——新增 P2-7 模型导入导出立项 v3.2、P2-8 开源基建三件事立项 v3.3。v1.3 2026-11-21：新增 P2-6 插件生态深化并立项 v3.1。v1.2 2026-11-20：P2-2/P2-3 挂起——开源版暂不考虑 SaaS 化能力；新增 P2-5 SDK 发布 Maven Central。v1.1 2026-11-14：新增 P1-7 前端现代化，v2.3 存储演进暂缓顺延为 v2.4；v1.0 初稿）
> 范围：v2.0（Java 17 + Spring Boot 3.5 单体）完成后的技术方向规划
> 状态：规划稿，优先级会随社区反馈调整
> 前置文档：[upgrade-plan-java17-boot3.md](upgrade-plan-java17-boot3.md)

---

## 1. 背景与现状

v2.0 已完成工程现代化：Java 17 + Spring Boot 3.5、8 模块合并单模块、dal 层移除、
持久层迁移 MyBatis-Plus，并全链路回归通过（含 115 个 2.4 时代 Groovy 脚本 100% 兼容验证）。

### 1.1 优势

- 技术栈现代，二开与维护成本低（单体架构，一个 jar 跑通全部）
- 规则引擎设计是核心差异化：Groovy 脚本实时生效 + 可视化编辑 + preItems 插件化数据接入
- 引擎向后兼容性扎实（2.4 时代种子脚本在 Groovy 4 下全部可用）

### 1.2 短板（v2.0 升级过程实测确认）

| # | 短板 | 事实依据 |
|---|------|----------|
| 1 | 测试几乎为零 | 219 个源文件仅 3 个测试类（CryptUtil/MapStruct/Plugin），引擎核心零覆盖，v2.0 回归靠手工冒烟 |
| 2 | 无 CI / 无 Docker | 仓库无 GitHub Actions、无 Dockerfile / docker-compose |
| 3 | 环境搭建门槛高 | "开箱即用"实际需要自装 MySQL / MongoDB / Redis / ES / Kafka 五件套 |
| 4 | 发布不规范 | CHANGELOG 缺失；历史 tag 止于 v1.0.8，此后未再打 tag（v2.0.0 起已恢复规范，见 CHANGELOG.md） |
| 5 | 英文门面失修 | README_en.md 仍描述 tk.mybatis / Swagger 旧栈 |
| 6 | 可观测性缺失 | "100ms 响应"卖点无指标背书，无 Micrometer / Prometheus 接入 |

## 2. P0：工程基础（近期，性价比最高）

开源项目的信誉之本，均不改动业务逻辑。

| # | 事项 | 说明 | 工作量估计 |
|---|------|------|-----------|
| P0-1 | 引擎核心单测 + Testcontainers 集成测试 | 覆盖三块命脉：Groovy 规则编译执行、抽象（abstraction）统计、激活（activation）阈值决策。v2.0 冒烟回归场景固化为自动化；2.4 种子脚本兼容性验证脚本改造为持续回归资产 | 3-5 人日 |
| P0-2 | GitHub Actions CI | GitHub 镜像仓启用 Actions：build + test + 推 Docker 镜像；Gitee 主仓手动同步。PR 设门禁 | 1 人日 |
| P0-3 | docker-compose 一键环境 | MySQL / Mongo / Redis / ES / Kafka 编排 + 应用镜像，`docker compose up` 直接跑通 demo。开源获客第一印象 | 1-2 人日 |
| P0-4 | 发布规范化 | v2.0 打 tag、编写 changelog、遵循 semver | 0.5 人日 |
| P0-5 | 社区门面修缮 | CONTRIBUTING.md、good first issue 标注、README_en.md 更新至 v2.0 技术栈 | 1 人日 |

## 3. P1：引擎核心竞争力（中期）

| # | 事项 | 说明 |
|---|------|------|
| P1-1 | 虚拟线程 | Boot 3.5 一个开关（`spring.threads.virtual.enabled=true`）。引擎为典型 IO 密集型（ES/Mongo/Redis 调用），预期吞吐收益明显；**先压测后开启** |
| P1-2 | 可观测性 | Micrometer + Prometheus 暴露：事件 QPS、规则命中率、引擎延迟 P99、Groovy 编译耗时。风控是延迟敏感系统，专业度必备 |
| P1-3 | ES 8 升级 | v2.0 方案明确留作后续（client 已升 7.17）。API 变化较大，单独立项，依赖 P0-1 测试基础 |
| P1-4 | 事件存储演进（调研） | hitsSort 等聚合报表是 MongoDB/ES 弱项，评估 ClickHouse 作为报表侧可选存储 |
| P1-5 | ML 评分重接（ONNX） | Estimator 接口已保留、零实现（TensorDnnEstimator 因 TF Java 不兼容 Java 17 移除，2026-11-14 拍板暂缓）。将来重接**选 ONNX Runtime 而非 TF Java**：abstractions 层即特征工程，接入即闭环；`sys.conf.machine-learning` 开关与 EstimatorContainer 注册机制均无需改动 |
| P1-6 | 引擎/管理端分离部署 profile | 单体仍为默认形态；增加 profile 使 engine 可独立水平扩展、admin 留单实例。Redis pub/sub 已解耦配置推送，改造成本低 |
| P1-7 | 前端现代化（radar-web 复活） | 管理端前端源码（2022-08 封存：React 16.8/antd 3/webpack 4，现代 Node 无法安装）合入主仓 `frontend/`（git subtree），迁移 React 18.3 + antd 5 + Vite + router 7；class 组件保留不 hooks 化、UI 不改版；产物继续内嵌 jar。见 [plan-v2.4.md](plan-v2.4.md)（v2.4.0，v2.3 存储演进暂缓顺延） |

## 4. P2：生态与护城河（长期）

| # | 事项 | 说明 |
|---|------|------|
| P2-1 | preItems 插件 SDK 化 | **已完成**（v3.0.0）：radar-plugin-api 契约 + 示例工程 + 开发指南，插件外置 jar 加载与动态表单 |
| P2-2 | Groovy 沙箱 | **挂起**（2026-11-20 拍板：开源版暂不考虑 SaaS 化能力——多租户为企业版差异化卖点，沙箱价值几乎全部锚定 SaaS，单租户自部署可接受现状，管理员本就持有主机/数据库权限）。方案预案：Groovy SecureASTCustomizer 白名单 或自研受限 DSL；重启时先做存量 115 脚本兼容评估 |
| P2-3 | 多租户 / SaaS 化（调研） | **挂起**（2026-11-20，同 P2-2：商业定位归企业版，开源版不做）。依赖 P2-2 |
| P2-4 | GitHub 镜像自动同步 | **已完成**：Gitee 自带镜像功能自动推送 GitHub（比原定 Actions 方案更省维护），CI/Release 随 push 自动触发 |
| P2-5 | radar-plugin-api 发布 Maven Central | **已完成**（2026-11-20）：坐标迁移 `cn.riskengine`（riskengine.cn 反向域名，Central 命名空间硬要求，Java 包名不变），`cn.riskengine:radar-plugin-api:3.0.0` 上架（jar/sources/javadoc/GPG 签名全齐）；插件开发者一句依赖即可编译插件，生态采用路径闭环；tag 驱动自动发布接入 release.yml（幂等：版本已存在自动跳过） |
| P2-6 | 插件生态深化 | **进行中**（2026-11-21 立项 v3.1）：管理端插件生命周期（上传/启停/删除，loader 代际热重载可回退）、HTTP 出访 allowlist 模式（v3.0 列后续项）、radar-plugin-api 3.1.0 向后兼容演进（paramSchema options、version/provider 元信息、testkit 工件）、远程插件官方示例、PLUGIN_REGISTRY 社区索引。见 [plan-v3.1.md](plan-v3.1.md) |
| P2-7 | 模型导入导出（策略包） | **已完成**（2026-11-21 实施完成，随 v3.2.0 发布）：模型配置全图文件化（JSON 策略包：字段/preItem/名单/抽象/激活/规则；engine_model_conf 为 ML 休眠配置不入包，随 P1-5 ML 重做再议）、导入校验报告（缺插件/Groovy 预编译/名单行数）；（拉伸）官方模板库一键导入。模型文件化 = 环境迁移/求助支持/模板分发的分发单元，与插件生态构成双护城河（插件共享数据接入，模型包共享风控策略）。见 [plan-v3.2.md](plan-v3.2.md) |
| P2-8 | 开源基建三件事 | **进行中**（2026-11-21 立项 v3.3，源自同日开源健康度盘点）：Dependabot + CodeQL 自动依赖与安全扫描、VitePress 文档站 + mermaid 架构图 + 运维手册、Playwright 五步 E2E 冒烟（防白屏类回归）。零业务代码改动，约 5 人日。见 [plan-v3.3.md](plan-v3.3.md) |

## 5. 里程碑建议

```
v2.1（工程基础）   P0-1 ~ P0-5        —— 测试、CI、Docker、发布规范化（[已发布](plan-v2.1.md)）
v2.2（专业度）     P1-1、P1-2         —— JDK 21 + 虚拟线程、可观测性（[已发布](plan-v2.2.md)）
v2.3（存储演进）   P1-3、P1-4         —— ES 8、报表存储调研（**暂缓**：2026-11-14 决定优先 v2.4，版本号保留）
v2.4（前端现代化） P1-7               —— radar-web 复活：React 18 + antd 5 + Vite（[已发布](plan-v2.4.md)）
v3.0（生态）       P2-1、P2-4 →      —— 插件重构：radar-plugin-api SDK / 外置 jar 加载 / 执行加固 / 动态表单（[已发布](plan-v3.0.md)），附带镜像自动同步；收尾 P2-5 SDK 上架 Maven Central（2026-11-20 完成）。P2-2/P2-3（沙箱、多租户）2026-11-20 挂起——开源版暂不考虑 SaaS 化能力
v3.1（插件运营）   P2-6              —— 插件生态深化：管理端生命周期（上传/启停/热重载）、出访 allowlist、SDK 3.1.0、远程示例与社区索引（[方案评审稿](plan-v3.1.md)）
v3.2（策略包）     P2-7              —— 模型导入导出：JSON 策略包 / 导入校验报告 / 整图事务（[方案](plan-v3.2.md)/[格式说明](model-package.md)，官方模板库列后续）
v3.3（开源基建）   P2-8              —— 三件事：Dependabot+CodeQL、文档站+架构图、Playwright E2E 冒烟（[方案评审稿](plan-v3.3.md)）
```

## 6. 如果只做三件事

1. **docker-compose 一键起环境**（获客）
2. **引擎核心测试 + CI**（信任）
3. **可观测性指标**（专业度）

三件事均不触碰业务逻辑，但把项目从"能用的代码"推向"值得信赖的开源产品"。
