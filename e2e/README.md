# Radar E2E 冒烟（v3.3 开源基建 T3）

Playwright 五步冒烟，守护管理端 UI 回归（背景：v2.4 Cascader 白屏类事故，6dead34）。

## 五步链路

| 步骤 | 通道 | 断言 |
|------|------|------|
| 1. 登录 | UI | 跳转模型列表 + 下发 `x-auth-token` |
| 2. 新建模型 | UI（模板复制弹窗） | 列表出现新模型 |
| 3. 进入模型工作区 | UI | 跳转字段页且表格加载 |
| 4. 事件发布 | 引擎公开接口链：模板 copy → build → enable → `uploadInfo` | 引擎返回打分数据 |
| 5. 报表可见 | UI（事件列表） | 列表包含刚上传的 reqId |

> 口径：规则配置经模板 103 继承（copy 即完整规则链），事件发布走与 Java SDK 相同的公开接口——
> 均为被测系统的公开契约，整体仍是黑盒；纯 UI 的规则编辑器操作列为后续。

## 本地运行

```bash
# 1) 起 compose 环境（验证码免检 + 本地构建应用镜像，需先 mvn package 出 target/radar.jar）
SYSCONF_CAPTCHAENABLED=false docker compose up -d --build --profile app

# 2) 跑冒烟（首次需 npx playwright install --with-deps chromium）
cd e2e && npm ci && npx playwright test
```

## 环境变量

| 变量 | 默认 | 说明 |
|------|------|------|
| `E2E_BASE_URL` | http://localhost:6580 | 被测环境地址 |
| `E2E_USER` / `E2E_PASS` | admin / 123456 | 种子账号 |
| `E2E_CAPTCHA` | 0000 | 验证码免检模式下任意非空值 |

CI：`.github/workflows/e2e.yml`（push master 触发：构建 jar → compose 起环境（验证码关）→ 健康等待 → Playwright）。失败自动上传 trace/screenshot 产物。
