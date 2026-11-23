# Radar v3.5 实施方案：生态内容补强（官方种子插件 + E2E 扩面）

> 版本：v1.0（2026-11-23）
> 范围：samples 新增 3 个实用官方插件（数据校验增强三件套）+ PLUGIN_REGISTRY 收录更新 + Playwright E2E 覆盖 v3.1~v3.4 新功能面；不触碰引擎核心与 SDK 契约（radar-plugin-api 停在 3.1.0 不发版）
> 状态：方案评审稿
> 前置：v3.4 模板库实施中（E2E 模板库用例依赖其合入，阶段安排上放最后）；roadmap 见 [roadmap.md](roadmap.md)

---

## 1. 背景与定位

v3.0~v3.4 把插件生态的"路"修完了（SDK 上 Central、生命周期管理、策略包、模板库），但"路上的车"
还太少：官方示例只有 SENSITIVE_DATA（本地形态）和 EXCHANGE_RATE（远程形态）两个教学级 demo，
社区开发者照着它不知道"实用插件长什么样"；E2E 只有五步主链路冒烟，v3.1 插件管理页、v3.2 策略包、
v3.4 模板库这些新功能面全靠手工验证——回归成本随功能增长线性上涨。

v3.5 定位为**纯内容版本**：补 3 个真实场景里拿来即用的插件（同时是三类常见插件形态的范本）、
把 E2E 覆盖面追平功能面。零引擎改动、零 schema 变更、零 API 破坏，minor bump。

## 2. 现状盘点（2026-11-23 代码实证）

| 项 | 现状 | 证据 |
|----|------|------|
| 官方示例 | radar-plugin-demo（SENSITIVE_DATA：布尔参数/多列/单测）+ radar-plugin-remote-demo（EXCHANGE_RATE：外呼/缓存/options/testkit） | samples/、PLUGIN_REGISTRY.md |
| testkit | MockHttpTool/PluginRequestBuilder/RecordingCache/TestContexts 四件，示例已用 | radar-plugin-testkit/ |
| 内置插件 | 8 个 BUILTIN_V3 稳定集（IP2LOCATION/MOBILE2LOCATION/GPS2LOCATION/HTTP_UTIL/ALLINONE/SUBSTRING/SENSITIVE_TIME/DATEFORMAT），覆盖归属地/外呼/字段计算，**校验增强类为零** | 服务器实测注册表 9 行（含外置 SENSITIVE_DATA） |
| E2E | smoke.spec.ts 五步（登录→模板复制建模型→工作区→事件发布→报表），插件管理页/策略包/模板库零覆盖 | e2e/tests/smoke.spec.ts:52-111 |
| e2e 基建 | compose 全栈起 + Playwright，验证码可关（SYSCONF_CAPTCHAENABLED），CI 已跑通（v3.3 经六轮修复后绿） | e2e.yml |

## 3. 技术决策（待评审确认）

| 决策 | 选择 | 理由 |
|------|------|------|
| 插件定位 | **官方示例（samples），不进内置**：扩展现有 radar-plugin-demo 工程（同为本地形态，paramSchema/多列/单测的范本地位不变） | 内置 8 个是稳定集，进内置=动主工程+全量回归；示例定位正好引导用户走一遍 v3.1 插件管理页上传流程（dogfooding 生命周期功能）；引擎不膨胀 |
| 插件选型 | **数据校验增强三件套**（中文风控高频、零外呼、纯离线）：`ID_CARD_VALIDATE` 身份证校验（18 位校验位算法 + 提取生日/性别/省份）、`USER_AGENT_PARSE` UA 解析（设备类型/OS/浏览器/微信内嵌标识，自实现正则版零依赖）、`BANK_CARD_CHECK` 银行卡 Luhn 校验 + BIN 卡种粗判 | 三者对应注册实名/设备维度/绑卡提现三大场景；展示三种实现姿势：纯算法、正则规则表、BIN 前缀映射；全部多列输出（type=""） |
| 依赖原则 | 示例插件**零第三方依赖**（含 UA 解析自实现简化版） | 官方示范必须零依赖可编译；UA 精确识别交给社区专业插件（PLUGIN_REGISTRY 虚位以待的正主） |
| E2E 扩面策略 | **按功能页拆 spec**：plugin-admin.spec.ts（插件管理页渲染+启停切换+复位）、model-package.spec.ts（导出下载断言+导入弹窗+报告回显）、template-library.spec.ts（v3.4 模板库打开+一键创建+列表 +1） | 每文件独立可跑；一个功能挂了不拖垮主链路冒烟；template-library 依赖 v3.4 合入，排最后 |
| E2E 断言深度 | 页面级可达性 + 核心交互（不追求全覆盖）：渲染出关键 DOM、操作产生预期状态变化 | E2E 定位是防白屏/断链类回归（v3.3 原则），不是 UI 自动化测试全集 |
| 插件测试 | 每插件 testkit 单测：正常/异常/边界（校验位错、超长 UA、空 BIN）+ 多列输出快照断言 | 与两个既有示例同等质量线 |

## 4. 实施内容

### 4.1 T1：三个种子插件（radar-plugin-demo 扩展）

- `ID_CARD_VALIDATE`：18 位身份证校验位 + 生日/性别/省份多列输出；15 位老号告警不阻断
- `USER_AGENT_PARSE`：设备类型（mobile/pc/tablet）+ OS 大类 + 浏览器大类 + 微信/支付宝内嵌布尔列
- `BANK_CARD_CHECK`：Luhn 校验 + BIN 卡种（借记/贷记粗判）+ 银行名（主流 20 家前缀表）
- 每个：paramSchema（如校验严格模式 options 下拉）+ SPI 注册 + testkit 单测

### 4.2 T2：PLUGIN_REGISTRY 与文档

- 官方示例表更新（radar-plugin-demo 一行拆多行或合并描述）；PLUGIN_REGISTRY 正文补"插件开发到上架五分钟"指引（指向 plugin-dev-guide）
- docs/plugin-dev-guide.md 增补三插件作为进阶范例引用

### 4.3 T3：E2E 扩面

- 公共登录 helper 抽取（复用 smoke 的登录态，captcha 关闭路径）
- 三个新 spec（见 §3）；CI e2e.yml 零改动（自动纳入 tests/ 全部 spec）
- template-library.spec.ts 等 v3.4 合入后补齐（其余先行）

### 4.4 T4：发布准备 v3.5.0

- CHANGELOG、主 pom 3.5.0、tag 发布（复用流水线；SDK 无变化走幂等跳过）

## 5. 实施阶段

| 阶段 | 内容 | 产出 | 估计 |
|------|------|------|------|
| S0 | 方案评审 + 插件字段/输出列定案 | 定稿 | 0.5 人日 |
| S1 | 三插件开发 + testkit 单测 | 示例可编译可测 | 2 人日 |
| S2 | PLUGIN_REGISTRY + dev-guide 文档 | 生态索引更新 | 0.5 人日 |
| S3 | E2E 两个 spec（plugin-admin/model-package） | 新功能面自动化 | 1 人日 |
| S4 | E2E template-library spec（依赖 v3.4）+ 全量回归 | 覆盖追平 | 0.5 人日 |
| S5 | 发布准备 v3.5.0 | 按发布规范 | 0.5 人日 |

**合计约 5 人日**。S1/S3 可并行；S4 卡在 v3.4 合入之后，其余不受影响。

## 6. 风险与对策

| 风险 | 对策 |
|------|------|
| UA/银行卡识别精度不足被质疑 | 文档明示"风控特征提取用，非精确识别"；精确场景引导社区专业插件收录 |
| 插件演示数据（身份证/卡号）涉敏感 | 测试数据全部为 GB 标准样例号/虚构 BIN，仓库审查确认无真实数据 |
| E2E 启停内置插件影响其他用例 | plugin-admin spec 内启停后复位 + 断言恢复；独立 spec 互不依赖 |
| v3.4 延期卡住 S4 | template-library spec 独立文件，v3.4 合入前 CI 跳过该文件（命名排除或 @ignore 注释切换），不阻塞 v3.5 其余部分 |
| samples 构建路径陷阱（.mvn/maven.config 相对解析） | 已知坑已记录（绝对路径覆盖），CI 同理验证 |

## 7. 验收标准

1. 三插件在插件管理页上传后正常装载（EXTERNAL 形态），preItem 表单按 paramSchema 渲染，事件流中多列输出正确（L1 testkit + 服务器手工验证）
2. PLUGIN_REGISTRY 官方示例表与实际工程一致；plugin-dev-guide 范例链接可达
3. E2E 五步冒烟 + 三个新 spec 全绿（CI），插件管理页/策略包/模板库回归自动化覆盖
4. 引擎核心零改动（git diff 无 src/main 业务代码变更，Dockerfile/compose 不变）
5. L1/L2 全绿 + 覆盖率门禁保持；v3.5.0 按发布规范（tag + CHANGELOG + Release + GHCR + Central 幂等跳过）
