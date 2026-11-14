# Radar 插件开发指南（v3.0 preItem 插件 SDK）

preItem 插件在事件进入规则运算前执行：把外部数据（归属地、数据服务结果、业务风控数据等）
以 `destField -> 结果` 的形式挂载进事件上下文，供抽象（abstraction）/规则（rules）以
`preItems.<destField>[.<列名>]` 消费。

- 契约模块：`radar-plugin-api`（零第三方依赖，仅 slf4j-api provided）
- 示例工程：`samples/radar-plugin-demo`（敏感数据识别与脱敏，含单测）
- 内置官方插件：`src/main/java/com/pgmmers/radar/service/impl/engine/plugin/`（同一契约的参考实现）

## 1. 快速开始

```bash
# 1) 安装插件 API 到本地仓库（在主仓根目录）
mvn -f radar-plugin-api/pom.xml install -s .mvn/settings.xml

# 2) 参照 samples/radar-plugin-demo 建工程，实现 Plugin 接口并标注 @PluginMeta

# 3) 打包并放入引擎 plugins/ 目录，重启引擎
cp target/your-plugin.jar /your/workdir/plugins/
```

## 2. 核心契约

```java
@PluginMeta(key = "YOUR_PLUGIN", name = "YOUR_PLUGIN", desc = "你的插件描述",
            type = "JSON", remote = false)          // remote=true 表示会外呼，受 HTTP 工具超时/私网防护约束
public class YourPlugin implements Plugin {

    /** 可配置参数描述（管理端据此动态渲染表单；空列表 = 无参数，管理端回退自由文本 args） */
    public List<ParamDescriptor> paramSchema() {
        return List.of(
            ParamDescriptor.name("level", "风控等级").type(ParamType.STRING).required()
                    .placeholder("HIGH | LOW").build(),
            ParamDescriptor.name("days", "回溯天数").type(ParamType.INTEGER)
                    .defaultValue("7").build());
    }

    /** 多列结果声明（管理端自动生成响应列配置，替代手填 configJson） */
    public List<OutputColumn> outputMeta() {
        return List.of(OutputColumn.of("score", "风险分", "DOUBLE"),
                       OutputColumn.of("level", "等级"));
    }

    public PluginResult execute(PluginRequest request, PluginContext context) throws PluginException {
        String field = String.valueOf(request.getSourceValues().get(0));   // 源字段值（按配置顺序）
        Integer days = (Integer) request.getArgs().get("days");            // 已按 schema 解析的类型化参数

        // 带缓存的远程查询（进程内 TTL 缓存，同名同 TTL 复用）
        Map<String, Object> data = context.cache("your_plugin", Duration.ofMinutes(10))
                .get(field, v -> {
                    HttpResult resp = context.http().get("https://api.example.com/query?key=" + v);
                    if (!resp.isOk()) throw new PluginException("上游服务 " + resp.getStatus());
                    return parse(resp.getBody());
                });
        return PluginResult.of(data);
    }
}
```

### 2.1 SPI 注册（外置 jar 必需）

`src/main/resources/META-INF/services/com.radar.plugin.api.Plugin` 内容为实现类全名（多行=多插件）。

### 2.2 元信息解析顺序

`key()/name()/desc()/type()/remote()` 读取 `@PluginMeta` 注解；未标注时 key 回退为类简名
（与 v2 习惯一致）。**推荐显式标注**——key 与类名解耦后可自由命名实现类。

### 2.3 结果约定

| 形态 | 返回 | 管理端/下游 |
|------|------|-------------|
| 单值 | `PluginResult.of("abc")` | `preItems.<destField>` 即该值 |
| 多列 | `PluginResult.of(Map)` | 各列按 `outputMeta` 声明；下游 `preItems.<destField>.<列名>` |

## 3. 引擎侧运行语义（写插件前必读）

| 语义 | 说明 |
|------|------|
| 失败隔离（默认） | 插件抛异常/结果缺失 → 计入 `radar_plugin_executions_total{result=fail}`、destField 写入 `{"__plugin_error__": 原因}`，**事件链路继续**。`plugin.isolation=strict` 恢复旧行为（失败即事件失败） |
| 超时 | 仅约束远程型：请一律用 `context.http()`（连接/读取超时引擎统一配置，默认 2s/2s，可配 `plugin.http.*`）。本地纯计算不设超时 |
| 私网防护 | `context.http()` 默认拒绝私网/回环目标（`plugin.http.deny-private-network=true`），缓解 url 配置类 SSRF |
| 缓存 | `context.cache(name, ttl)` 进程内缓存（Caffeine，10k 条上限/缓存），归属地等冷数据建议 10 分钟~1 小时 |
| 类加载 | 外置 jar 使用子优先类加载器：插件自带依赖与引擎隔离；JDK/`com.radar.plugin.api`/`org.slf4j` 走 parent 共享 |
| 发现 | plugins/ 目录 `*.jar` 启动扫描（SPI），**重启生效**（非热加载）；Spring 容器内插件走 bean 扫描。同 key 冲突以先注册者（内置）为准并告警 |

## 4. 参数 schema 与存量兼容

- v3 配置：`args` 存 JSON 对象字符串（`{"days":7}`），引擎按 schema 解析为类型化 Map 传入；
- 存量 v2 配置（`args="0,7"`、纯 url 等自由字符串）：解析失败时引擎传空 Map，**插件需自行兜底**
  （内置 SUBSTRING/DATEFORMAT/HTTP_UTIL 的兜底写法见其 `execute`）；
- 必填校验：schema 声明 `required()` 的参数由管理端表单校验；引擎侧不重复拦截（避免存量配置失败），
  插件对拿不到的关键参数应抛 `PluginException` 给出可读信息。

## 5. v2 → v3 迁移对照

| v2（PluginServiceV2） | v3（Plugin） |
|---|---|
| `key()` Integer 排序 | `@PluginMeta.key()` 字符串（DB 配置 key=类简名不变即可兼容） |
| `desc()` / `getType()` | `@PluginMeta.desc / type`（type 传 `""` 表示"多列"旧语义） |
| `getMeta()` JSON 字符串 | `outputMeta()` 结构化 `List<OutputColumn>` |
| `handle(item, jsonInfo, sourceField)` 裸 Object | `execute(request, context)` 类型化入出参 |
| 无参数描述，args 语义私有 | `paramSchema()` + JSON args + legacy 兜底 |
| 无异常/超时契约 | PluginException + 引擎隔离 + 远程超时 |

## 6. 内置插件清单（v3.0，均可作参考实现）

| key | 功能 | 参数（schema） | 输出列 |
|-----|------|----------------|--------|
| IP2LOCATION | IP→归属地（本地 ip2region.db，结果缓存 1h） | 无 | country/province/city |
| GPS2LOCATION | 经纬度→地址（外部服务，`plugin.gps.url` 可配） | 无 | country/province/city |
| MOBILE2LOCATION | 手机号前 7 位→归属地（本地号段库） | 无 | country/province/city |
| ALLINONE | 多字段 `_` 拼接 | 无 | — |
| SUBSTRING | 字符串截短 | start/end（INTEGER） | — |
| SENSITIVE_TIME | 时间戳→小时(0-23) | 无 | — |
| DATEFORMAT | 毫秒→格式串 | format（STRING） | — |
| HTTP_UTIL | 通用 HTTP 取数 | url/reqType（STRING） | —（响应列手填 configJson） |

## 7. 调试建议

1. 单元测试直连：`new PluginContext(logger, cacheFactory, httpTool)` 可用 lambda 传空实现，
   `PluginRequest` 直接构造（见示例工程 `SensitiveDataPluginTest`）；
2. 引擎日志：插件 logger 名为 `plugin.<key>`，异常会带 destField 与原因打印在 warn 级别；
3. 指标核对：`/actuator/prometheus` 中 `radar_plugin_executions_total` / `radar_plugin_seconds`；
4. 注册表核对：`GET /services/v1/common/plugins` 应包含你的插件（source=EXTERNAL）。
