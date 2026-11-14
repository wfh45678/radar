# Radar v2.2 虚拟线程压测对照报告

> 状态：**框架就绪，数据待填**（需 Docker 环境 + 压测机；方法与场景已固化）
> 方案依据：[plan-v2.2.md](plan-v2.2.md) §4.2
> 实验设计：三组对照——**B−A 隔离 JDK 升级本身收益，C−B 才是虚拟线程净收益**

| 组 | JDK | spring.threads.virtual.enabled | 备注 |
|----|-----|--------------------------------|------|
| A | 17 | false | 基线（用 v2.1.0 jar） |
| B | 21 | false | `enabled: false` 覆盖 |
| C | 21 | true | 当前代码默认 |

## 环境

- 中间件：`docker compose up -d`（MySQL/Mongo/Redis/ES，种子数据自动导入）
- 应用：本地 jar（`java -jar target/radar.jar`，profile dev，中间件 localhost）或 `--profile app` 容器
- 压测机与应用同机时注意 CPU 核数对结果的影响，报告中必须记录：OS / CPU / 内存 / JDK 版本 / 中间件是否同机
- JMeter 场景：`docs/benchmark/uploadInfo.jmx`（表单 POST /services/v1/uploadInfo，事件体与 v2.0 冒烟一致，reqId 参数化）
- 前置操作：注册账号 → copy 模板 → build → enable（可用 `EngineFullChainTest` 十步测试代跑）

## 场景与档位

`POST /services/v1/uploadInfo`（引擎主路径）：
并发 1 / 50 / 200 三档，每档持续 3 分钟，Ramp-up 30s。

`GET /services/v1/getScore`（Redis 读路径）：
并发 200 一档，2 分钟（轻量对照）。

## 结果记录表（每组一表）

| 并发 | RPS | P50 (ms) | P95 (ms) | P99 (ms) | 错误率 |
|------|-----|----------|----------|----------|--------|
| 1    |     |          |          |          |        |
| 50   |     |          |          |          |        |
| 200  |     |          |          |          |        |

## 结论模板

- C−B 净收益（200 并发 P99）：___ ms → ___ ms（__%）
- pinning 观测（组 C）：`jcmd <pid> JFR.start name=vt duration=120s filename=vt.jfr`，
  分析事件 `jdk.VirtualThreadPinned`（关注持续时间与调用栈：重点嫌疑 Druid 连接池 / Groovy 类加载）
- 决策：默认开启 / 回退为 `virtual` profile 可选开关（回退阈值建议：C 组 P99 比 B 组劣化 >10%）

## 复跑步骤

```bash
# 1. 起中间件
docker compose up -d
# 2. 起应用（各组换 jar/开关）
java -jar target/radar.jar
# 3. 初始化模型（跑一次全链路测试即可，需临时放开集成测试标签）
# 4. JMeter
jmeter -n -t docs/benchmark/uploadInfo.jmx -l docs/benchmark/result-A-50.jtl -Jconcurrency=50 -Jduration=180
# 5. JFR（组 C 必做）
jcmd <pid> JFR.start name=vt duration=120s filename=vt.jfr settings=profile
```
