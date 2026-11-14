# 贡献指南（Contributing）

感谢关注 Radar！欢迎通过 Issue 反馈问题、通过 PR 提交改进。

## 环境要求

- JDK 21+（v2.2 起编译目标与运行时均为 21；本机默认 JDK 为 17 时可 zip 版并存：
  `JAVA_HOME=D:/Java/jdk-21 PATH="D:/Java/jdk-21/bin:$PATH" mvn package`）
- Maven 3.8+（仓库自带镜像配置 `.mvn/settings.xml`，命令行示例已带 `-s` 参数）
- Node.js 22+ 与 npm（仅前端开发 / 重新构建前端产物需要，见 `frontend/`）
- Docker（仅运行集成测试 / 一键环境需要；没有 Docker 也能做日常开发与单测）

## 本地开发

```bash
# 构建（跳过测试）
mvn package -s .mvn/settings.xml -DskipTests

# 只跑单测（L1/L2，秒级，无需 Docker）
mvn test -s .mvn/settings.xml

# 全量验证（L3 集成测试走 Testcontainers 四件套，需要 Docker）
mvn verify -s .mvn/settings.xml -Denv.CI=true

# 一键起中间件（MySQL/Mongo/Redis/ES，种子数据自动导入），再 java -jar 直连
docker compose up -d

# 运行
java -jar target/radar.jar        # 管理端 http://localhost:6580
```

配置说明见 `src/main/resources/application*.yml`（profile：local/dev/prod，测试 profile 为 test）。

## 前端开发（frontend/）

管理端 SPA：React 18 + antd 5 + Vite（v2.4 起源码入仓，构建产物提交于 `src/main/resources/static/`）。

```bash
cd frontend
npm ci && npm run dev      # 开发模式：http://localhost:3000，/services 代理到 localhost:6580
                           # 联调其它后端：RADAR_API_TARGET=http://主机:6580 npm run dev
npm run build              # 产物出 dist/
```

改动前端源码后，**必须**重跑 `scripts/build-frontend.sh` 把产物同步进 `src/main/resources/static/` 并一并提交
（CI 有产物同步校验，源码与产物漂移会挂）。部署形态不变：产物内嵌 jar，Dockerfile/compose 无 node 阶段。

## 测试分层约定

| 层 | 位置 | 要求 |
|----|------|------|
| L1 纯单测 | 任意，无外部依赖 | 必须，`mvn test` 全绿 |
| L2 种子脚本回归 | `Groovy4SeedScriptTest` | 不新增种子脚本时无需改动 |
| L3 集成测试 | 继承 `AbstractIntegrationTest` | 标注由基类携带（`@Tag("integration")`），本地无 Docker 会自动跳过 |

新增功能请至少附带 L1 测试；影响引擎行为（规则/抽象/激活/插件）请补 L3 场景。

## 提交 PR

1. 从 `master` 切出分支，命名建议 `feature/xxx` 或 `fix/xxx`
2. 提交信息遵循现有风格：`feat:` / `fix:` / `test:` / `docs:` / `chore:` 前缀 + 中文简述
3. PR 描述写清：改了什么、为什么、如何验证（测试结果贴出来）
4. GitHub 镜像仓的 CI（[workflow](.github/workflows/ci.yml)）会在 PR 上全量跑测试；Gitee 为主仓，请在 Gitee 提交 PR 或双端同步
5. 版本发布由维护者操作：semver tag + CHANGELOG.md 条目 + GitHub Release（见 release workflow）

## 代码风格

- 与既有代码保持一致：SLF4J 日志、构造器/@Autowired 注入、MapStruct 做对象转换
- 不要引入新的重依赖；有替代方案先讨论
- 公共 API 变更（engine 端点、admin 端点）请在 PR 描述中标注兼容性影响

## 问题反馈

Issue 请附：版本号（tag 或 commit）、复现步骤、关键日志。安全漏洞请勿公开发布，联系维护者。
