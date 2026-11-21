# Radar v3.3 实施方案：开源基建三件事

> 版本：v1.0（2026-11-21）
> 范围：2026-11-21 项目盘点的"如果只做三件事"——依赖与安全自动扫描、文档站与架构图、前端 E2E 冒烟
> 状态：方案评审稿
> 前置：v3.2 已立项（模型导入导出，[plan-v3.2.md](plan-v3.2.md)）；roadmap 见 [roadmap.md](roadmap.md)（P2-8）

---

## 1. 背景与定位

2026-11-21 开源健康度盘点的结论：项目工程主线（v2.1→v3.2）推进扎实，但**开源门面的自动化与
可发现性**存在三个高性价比缺口——依赖升级靠人肉盯、无静态安全扫描、前端零测试（v2.4 有 Cascader
白屏回归前科 6dead34）、文档只存在于 git 仓库里。

v3.3 定位为**基建版本**：三项均为配置级/工具级工作，不触碰业务代码，合计约 5 人日。

## 2. 实施内容

### T1：依赖与安全自动扫描（约 0.5 人日）

- `.github/dependabot.yml`：maven + npm + github-actions 三生态周检，自动升级 PR
- GitHub CodeQL（OSS 免费）：Java 分析接 push/PR 触发，跑在镜像仓 CI（已就绪）
- 验收：Dependabot 首批 PR 可评估合入；CodeQL 首扫报告产出，高危项清零或建档

### T2：文档站 + 架构图（约 2 人日）

- `docs-site/` VitePress：导航 = 快速开始 / 架构 / 插件生态（dev guide）/ 模型策略包 / 运维手册 / roadmap 版本化档案
- **架构图两张（mermaid）**：事件处理流（事件→preItems→abstraction→rules→activation→hits 落库）、部署形态（compose 单机 / 引擎分离）
- 运维手册首版：备份、升级（引用 `sql/upgrade-*.sql` 约定）、故障排查 FAQ
- 发布：GitHub Pages（镜像仓托管）先跑通，riskengine.cn 收录后置
- 验收：docs/ 全部 md 有站内入口；首页一屏内可看到"是什么/跑起来/二次开发"三条路径

### T3：Playwright E2E 冒烟（约 2.5 人日）

- compose 起环境 → **五步断言**：登录 → 建模型 → 配字段/规则 → 发布事件 → 报表可见
- 种子数据依赖已就绪（radar-init.sql）；选择器优先走 data-testid 防脆弱
- 接入 GitHub Actions：PR 触发冒烟（视耗时，必要时降级 nightly）
- 验收：白屏类事故（Cascader 6dead34 级别）在 CI 被拦住；冒烟全程 <10 分钟

## 3. 风险与对策

| 风险 | 对策 |
|------|------|
| 文档站维护成本（内容腐化） | 站点仅做 docs/ 的导航与渲染，内容仍以仓库 md 为源；CI 校验死链 |
| E2E 环境漂移（依赖 compose 网络/端口） | CI 用固定 compose profile + 健康检查等待，失败重试一次 |
| CodeQL 首扫误报噪音 | 先 report-only 一轮，清档后再设门禁 |
| 双语策略 | 先中文，英文摘要列后续（不阻塞） |

## 4. 验收标准

1. Dependabot 周 PR 流水线运行；CodeQL 报告可见且高危清零/建档
2. 文档站可访问（GitHub Pages），架构图与运维手册首版就绪
3. PR 触发 E2E 冒烟通过；人为注入一个前端路由错误能被冒烟拦住（演练）
4. 主工程零业务代码改动；L1/L2 全绿保持
