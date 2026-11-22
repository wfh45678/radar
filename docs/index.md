# Radar 文档导航

Radar 是开源实时风控决策引擎：事件毫秒级打分，六类策略（规则/决策树/评分卡/决策表/名单/规则集），
Groovy 规则编辑即时生效，插件外置接入外部数据，模型可导出为策略包跨实例分发。

- 主仓库：[Gitee](https://gitee.com/freshday/radar) · [GitHub 镜像](https://github.com/freshday/radar)

## 三条上手路径

**我想先跑起来** → [运维手册：部署形态与快速开始](./operations.md)

**我想理解它怎么工作** → [架构总览](./architecture.md)（事件处理流 + 部署形态两张图）

**我想二次开发** →

| 方向 | 文档 |
|------|------|
| 接入外部数据（插件） | [插件开发指南](./plugin-dev-guide.md) · [插件注册表](./PLUGIN_REGISTRY.md) |
| 迁移/分享风控策略（模型包） | [模型策略包：格式与操作](./model-package.md) |
| 事件接入（应用侧） | samples/ 下 Java/Kafka 示例工程 |

## 版本演进

当前版本与历史方案见 [Roadmap](./roadmap.md)（v2.1 工程基础 → v2.2 专业度 → v2.4 前端 → v3.0 插件 → v3.1 插件运营 → v3.2 策略包 → v3.3 开源基建），每个版本的实施方案都在仓库 `docs/plan-*.md` 内存档。

## 反馈

- 问题与需求：Gitee Issue（带 [issue 模板](https://gitee.com/freshday/radar/issues)）
- 安全漏洞：见 [SECURITY.md](https://gitee.com/freshday/radar/blob/master/SECURITY.md)
