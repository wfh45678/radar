# Radar 插件注册表（PLUGIN_REGISTRY）

收录社区 preItem 插件（v3.0+ 契约）。插件以独立 jar 分发（各自由 Maven 仓 / GitHub Release 托管），
本表仅做索引。上传方式：管理端「插件管理」页上传 jar，或手工放入 `plugins/` 目录。

## 内置插件（随引擎发布，无需上传）

| key | 功能 | 形态 | 参考实现 |
|-----|------|------|----------|
| IP2LOCATION | IP → 归属地（本地 ip2region 库） | 本地 | `IP2LOCATION.java` |
| MOBILE2LOCATION | 手机号前 7 位 → 归属地（本地号段库） | 本地 | `MOBILE2LOCATION.java` |
| GPS2LOCATION | 经纬度 → 地址（外呼，URL 可配） | 远程 | `GPS2LOCATION.java` |
| HTTP_UTIL | 通用 HTTP 取数 | 远程 | `HTTP_UTIL.java` |
| ALLINONE / SUBSTRING / SENSITIVE_TIME / DATEFORMAT | 字段计算类 | 本地 | 同目录 |

## 官方示例（samples/，编译即用）

| 工程 | key | 演示内容 |
|------|-----|----------|
| [radar-plugin-demo](samples/radar-plugin-demo) | SENSITIVE_DATA | 本地形态基础版：paramSchema 布尔参数、多列输出、单测 |
| [radar-plugin-demo](samples/radar-plugin-demo) | ID_CARD_VALIDATE | 纯算法范本：18 位校验位 + 生日/性别/省份多列，15 位老号宽松/严格模式 options 下拉（v3.5） |
| [radar-plugin-demo](samples/radar-plugin-demo) | USER_AGENT_PARSE | 正则规则表范本：设备类型/OS/浏览器大类/微信·支付宝内嵌，零依赖自实现（v3.5） |
| [radar-plugin-demo](samples/radar-plugin-demo) | BANK_CARD_CHECK | 前缀映射表范本：Luhn 校验 + 卡种粗判 + 主流银行前缀表（v3.5） |
| [radar-plugin-remote-demo](samples/radar-plugin-remote-demo) | EXCHANGE_RATE | 远程形态：remote 外呼、options 下拉、10 分钟缓存、多列输出、testkit 单测（v3.1） |

## 插件开发到上架五分钟

1. 按 [开发指南](docs/plugin-dev-guide.md) 建工程（参照 samples/ 五个官方插件抄结构）；
2. `mvn test` 本地验证 → `mvn package` 出 jar；
3. 管理端「插件管理」页上传 jar → 热重载即时生效（EXTERNAL 装载）；
4. preItem 配置里选你的插件 → 事件流验证输出列；
5. 到本文件提交 PR 收录你的插件（社区插件表格虚位以待）。

## 社区插件

> 欢迎以 PR 提交你的插件：按下方表格格式追加一行，并附源码/发布仓库链接。
> 收录标准：实现 v3 `Plugin` 契约、附带文档、允许公开获取。

| key | 功能 | 提供方 | 版本 | 链接 |
|-----|------|--------|------|------|
| —（虚位以待） | | | | |

## 提交格式

```markdown
| KEY_NAME | 一句话功能描述 | your-github-id | 1.0.0 | https://github.com/you/your-plugin |
```
