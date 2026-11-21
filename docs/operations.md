# 运维手册（首版）

自部署 Radar 的部署、升级、备份与常见问题。一键体验请先看仓库 [README](https://gitee.com/freshday/radar#快速开始) 的 docker compose 三行命令。

## 1. 部署形态

| 形态 | 命令 | 适用 |
|------|------|------|
| 中间件 + 种子数据 | `docker compose up -d` | 起依赖，应用用本地 jar 跑 |
| 全套（含应用镜像） | `docker compose up -d --build --profile app` | 交付/演示 |
| 监控栈 | `docker compose --profile monitoring up -d` | Grafana 看板（事件 QPS/P99/决策分布） |
| 源码构建 | `mvn package -s .mvn/settings.xml -DskipTests` 后 `java -jar target/radar.jar` | 二次开发 |

环境要求：Docker + compose v2（容器化部署）/ JDK 21+（裸跑 jar）。默认端口 6580，
管理端 `http://localhost:6580/`，种子账号 `admin / 123456`（**上线前必改**）。

**登录验证码开关**（v3.3）：`SYSCONF_CAPTCHAENABLED=false` 可关闭登录验证码——仅供自动化测试/
演示环境使用，生产保持默认开启。

## 2. 升级

- **小版本升级**（如 3.1→3.2）：替换 jar/镜像 + 执行 `sql/upgrade-<version>.sql` 增量脚本
  （如 v3.2 前从 3.0 升级需跑 `sql/upgrade-3.1.0.sql` 建插件注册表）；全新部署直接导入
  `sql/radar-init.sql` 全量基线，无需增量脚本；
- **模型/策略迁移**：不需要动库——旧实例导出[模型策略包](./model-package.md)，新实例导入；
- 升级前：备份 MySQL（配置与插件注册表都在里面）+ 确认目标版本的 CHANGELOG「Breaking Changes」小节。

## 3. 备份

| 数据 | 位置 | 建议 |
|------|------|------|
| 配置（模型/规则/插件注册） | MySQL `radar` 库 | 定期全备 + 升级前必备 |
| 事件流水 | MongoDB（保留 `sys.conf.mongo-restore-days` 天） | 按合规要求归档 |
| 事件检索 | Elasticsearch | 可重建（数据源为 Mongo） |
| 外置插件 | `plugins/` 目录 | 与 MySQL 一起纳入备份 |

最小可用备份 = MySQL 全备 + `plugins/` 目录。灾备恢复：建库导备份 + 拷回插件目录 + 应用启动。

## 4. 监控

- 指标端点 `/actuator/prometheus`（事件计数/阶段 P99/插件执行/HTTP 出访拦截等，见
  [压测与监控](./benchmark.md)）；
- `docker compose --profile monitoring` 一键起 Prometheus + Grafana（内置 Radar 看板）；
- 插件故障表现：事件字段出现 `__plugin_error__` 标记 + `radar_plugin_executions_total{result="fail"}` 上涨。

## 5. FAQ / 故障排查

| 现象 | 排查 |
|------|------|
| 管理端白屏/页面异常 | 确认 jar 为最新构建（前端产物内嵌）；浏览器控制台报错 + 版本号贴 Issue |
| 登录提示"验证码无效" | 服务端 Redis 会话失效或时钟漂移；自部署演示环境可用 `SYSCONF_CAPTCHAENABLED=false` 绕过 |
| 事件无某字段数据 | 对应 preItem 插件缺失/失败：看管理端「插件管理」与 `radar_plugin_http_blocked_total` |
| 导入模型后构建失败 | 见[策略包故障排查](./model-package.md)——多为 Groovy 告警规则，修正脚本后重建 |
| ES 检索无结果 | ES 未就绪或重建索引中；`/actuator/health` 与容器日志 |
| 插件上传后未生效 | 管理端「插件管理」看状态（热重载关闭时需重启）；Windows 下宽限期内文件占用属预期 |
