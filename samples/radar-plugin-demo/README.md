# Radar Plugin Demo（v3.0 插件示例工程）

演示如何用 `radar-plugin-api` 开发一个 preItem 插件：**敏感数据识别与脱敏**
（识别文本中的手机号/身份证号，可脱敏输出），含参数 schema、输出列声明与单元测试。

## 前置

```bash
# 1. 构建主仓时先安装插件 API 到本地仓库（CI/脚本已编排，手动开发时执行一次）
cd radar/radar-plugin-api && mvn install -s ../.mvn/settings.xml
```

## 构建

```bash
mvn package    # 产出 target/radar-plugin-demo-3.0.0.jar
```

## 部署到引擎

```bash
# 方式一：本地目录
cp target/radar-plugin-demo-3.0.0.jar <引擎工作目录>/plugins/

# 方式二：docker compose（挂载 plugins 卷，见 docker-compose.yml app 服务）
# 重启引擎（plugins/ 目录为启动扫描，非热加载）
docker compose --profile app up -d --build app
```

重启后：

1. `GET /services/v1/common/plugins` 中可见 `SENSITIVE_DATA`（含 paramSchema/outputMeta）；
2. 管理端「预处理」新增配置：选择"敏感数据识别与脱敏"插件，表单按 schema 自动生成
   （识别手机号/识别身份证/结果脱敏 三个布尔开关）；
3. 事件进入引擎后，`preItems.<destField>.hasMobile` 等列即可被 abstraction/rules 消费。

## 关键代码

- [SensitiveDataPlugin.java](src/main/java/com/radar/plugin/demo/SensitiveDataPlugin.java)
- SPI 注册：`src/main/resources/META-INF/services/com.radar.plugin.api.Plugin`
- 开发指南：主仓 `docs/plugin-dev-guide.md`
