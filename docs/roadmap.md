# Radar 开源项目技术演进 Roadmap

> 版本：v1.1（2026-11-14：新增 P1-7 前端现代化，v2.3 存储演进暂缓顺延为 v2.4；v1.0 初稿）
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
| P2-1 | preItems 插件 SDK 化 | 插件系统是最大卖点但缺接口契约与文档；出 SDK + 示例工程，让数据服务厂商可自助接入 |
| P2-2 | Groovy 沙箱 | 管理端脚本 = 任意代码执行，单租户自部署可接受；**SaaS / 多租户化的前置条件**。方案：Groovy SecureASTCustomizer 白名单 或自研受限 DSL |
| P2-3 | 多租户 / SaaS 化（调研） | 商业化二次探索的技术前提，依赖 P2-2 |
| P2-4 | GitHub 镜像自动同步 | Actions 定时双向同步，消除双仓维护成本 |

## 5. 里程碑建议

```
v2.1（工程基础）   P0-1 ~ P0-5        —— 测试、CI、Docker、发布规范化（[已发布](plan-v2.1.md)）
v2.2（专业度）     P1-1、P1-2         —— JDK 21 + 虚拟线程、可观测性（[已发布](plan-v2.2.md)）
v2.3（存储演进）   P1-3、P1-4         —— ES 8、报表存储调研（**暂缓**：2026-11-14 决定优先 v2.4，版本号保留）
v2.4（前端现代化） P1-7               —— radar-web 复活：React 18 + antd 5 + Vite（[已发布](plan-v2.4.md)）
v3.0（生态）       P2-1、P2-4 →      —— 插件重构：radar-plugin-api SDK / 外置 jar 加载 / 执行加固 / 动态表单（[plan-v3.0.md](plan-v3.0.md)），附带镜像自动同步；P2-2 Groovy 沙箱独立立项（视社区情况启动，前置 P2-3 多租户）
```

## 6. 如果只做三件事

1. **docker-compose 一键起环境**（获客）
2. **引擎核心测试 + CI**（信任）
3. **可观测性指标**（专业度）

三件事均不触碰业务逻辑，但把项目从"能用的代码"推向"值得信赖的开源产品"。
