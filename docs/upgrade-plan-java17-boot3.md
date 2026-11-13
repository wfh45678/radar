# Radar 架构升级方案：Java 17 + Spring Boot 3.x 单体工程

> 版本：v1.0（2026-11-13）
> 范围：工程合并（8 模块 → 1 模块）、Java 8 → 17、Spring Boot 2.2.5 → 3.5.x、移除 dal 层
> 状态：方案评审稿

---

## 1. 背景与目标

Radar 是基于 Spring Boot 2.2.5 + Java 8 的实时风控引擎，当前拆分为 8 个 Maven 模块，
分层过细（dao / dal / service / service-impl / admin / engine），dal 层职责单薄（纯转发 + PO↔VO 转换），
且核心依赖（tk.mybatis、springfox、jjwt 0.9、fastjson 1.x、Groovy 2.4、TensorFlow 1.12）均已停止维护或与
Java 17 生态不兼容。

本次升级目标：

| # | 目标 | 说明 |
|---|------|------|
| 1 | Java 17 + Spring Boot 3.5.x | 3.x 系列最终版本，通往 4.0 的桥梁 |
| 2 | 所有 Java 代码合并为单模块工程 | 8 个 Maven 模块 → 1 个，构建与维护成本显著下降 |
| 3 | 移除 dal 层 | service 直连持久层，删除纯转发的中间层 |
| 4 | 部署形态：单体应用 | 一个 jar 同时提供管理端与引擎端 API |

## 2. 现状盘点

### 2.1 模块依赖链

```
radar-commons ← radar-dao ← radar-dal ← radar-service ← radar-service-impl ← radar-admin（管理端 :6580）
                                                                             ← radar-engine（引擎端 :6581）
radar-kafka-demo（独立演示程序，不参与主链路）
```

### 2.2 各层职责与规模

| 模块 | 内容 | 规模 / 关键事实 |
|------|------|----------------|
| radar-commons | 工具类（Groovy 脚本、加密、Excel、IP 归属地等）+ mbg 生成器插件 | 10 个类 |
| radar-dao | tk.mybatis 空壳 Mapper 接口 + PO（`javax.persistence` 注解）+ 14 个 resultMap XML | **XML 中 0 条自定义 SQL**，全部查询走 Example API |
| radar-dal | `XxxDal` 接口 + Impl：包装 Mapper、MapStruct PO↔VO、PageHelper 分页、`@Cacheable`；另含 21 个 VO、MapStruct 转换器、Query/PageResult | 12 个 DalImpl 约 1700 行 |
| radar-service | 业务接口 | 40 个文件 |
| radar-service-impl | 业务实现：反欺诈引擎核心、Groovy 规则、插件体系、缓存订阅、ES 写入 | 40 个文件 |
| radar-admin | 管理端启动器：14 个 ApiController、JWT 拦截器、Thymeleaf + 静态前端 | 端口 6580 |
| radar-engine | 引擎端启动器：事件接入 Controller、全局异常处理 | 端口 6581 |
| radar-kafka-demo | Kafka 消费 demo | 3 个类 |

### 2.3 外部依赖

MySQL(Druid) + MongoDB + Redis(Lettuce，缓存 + 发布订阅) + Elasticsearch 7.11(High-Level Client)
+ Groovy 2.4 + TensorFlow 1.12 + fastjson + springfox + jjwt。

### 2.4 对升级有利的关键发现

1. **所有模块共享同一基础包 `com.pgmmers.radar`** → 工程合并主要是文件搬运 + config 去重，包名几乎不变。
2. **PageHelper 与 tk.mybatis Example 的使用 100% 集中在 dal 模块**（即将删除的层）
   → 删 dal 时顺势完成持久层替换，service 侧只改注入对象与调用点。
3. **Groovy 仅使用 `GroovyClassLoader`/`GroovyObject` 动态编译**，无编译期依赖 → Groovy 4 升级风险低。
4. `spring.factories` 内容已全部注释（no-op）、14 个 mapper XML 无 SQL → 直接删除。
5. jjwt 仅 `JsonWebTokenService`（97 行）一处使用；ES 改造集中在 `SearchEngineServiceImpl`（201 行）。

## 3. 已确认的技术决策

| 决策点 | 结论 | 理由 |
|--------|------|------|
| 部署形态 | **合并为单体应用**（一个进程、一个 jar） | 部署最简单，代码改动最小；模块本就同库同中间件 |
| 持久层 | **MyBatis-Plus 3.5.x（boot3 starter）** | tk.mybatis 无 Boot 3 官方支持；MP 通用 CRUD + `LambdaQueryWrapper` 平替 Example；内置分页可同时移除 PageHelper |
| TensorFlow DNN | **暂时移除** | TF 1.12 不兼容 Java 17 生态；保留 `Estimator` 接口与规则引擎主流程，后续按需基于 TF-Java 0.5.x 或外部推理服务重接 |
| ES 客户端 | 升至 7.17.x，**不升 ES 8 新客户端** | High-Level Client 7.17 兼容 Java 17；换新客户端的 API 重写独立为后续迭代，缩小本期风险窗口 |
| JSON | fastjson → **fastjson2**（兼容 API） | 27 个文件的 import 替换为主，避免全部重写为 Jackson |

## 4. 目标架构

### 4.1 工程结构

```
radar/                                    # 单模块 Maven 工程
├── pom.xml                               # Spring Boot 3.5.x + Java 17
├── docs/                                 # 本文档
├── sql/                                  # 保留
├── samples/radar-kafka-demo/             # demo 移出主构建（可选保留）
├── src/main/java/com/pgmmers/radar/
│   ├── RadarApplication.java             # 唯一启动类（@MapperScan 保留）
│   ├── config/                           # admin/engine 两套 config 合并去重
│   ├── controller/
│   │   ├── admin/                        # 14 个管理端 Controller（JWT 保护，/api/**）
│   │   └── engine/                       # 引擎端 Controller（事件接入，公开 + 签名）
│   ├── interceptor/                      # JWT 拦截器（原 intercpt，只拦管理端路径）
│   ├── service/                          # 原样平移
│   │   └── impl/                         # 直接注入 Mapper（不再经过 dal）
│   ├── mapper/                           # MyBatis-Plus BaseMapper（原 tk.mybatis Mapper）
│   ├── model/
│   │   ├── entity/                       # PO：javax.persistence → MP 注解
│   │   ├── vo/                           # 原 radar-dal/vo
│   │   └── query/                        # 原 radar-dal/bean（Query/PageResult）
│   ├── converter/                        # 原 mapstruct（保留 MapStruct）
│   ├── enums/                            # 原 radar-dal/enums
│   └── common/util/                      # 原 radar-commons/util
├── src/main/resources/
│   ├── application.yml                   # 两套配置合并，profile 区分环境
│   ├── templates/                        # Thymeleaf
│   └── static/                           # 管理端前端产物
└── src/test/java/                        # 修正（现有测试误置于 src/main/test）
```

### 4.2 删除项清单

| 删除项 | 原因 |
|--------|------|
| `radar-dal` 的 `dal/**` 全部接口与实现 | 本方案核心目标 |
| 14 个 mapper XML（`mapping/*.xml`） | 无自定义 SQL，纯 resultMap，MP 注解模式不需要 |
| `ModelRenamePlugin`（radar-commons） | mbg 代码生成器插件，tk.mybatis 退场后无用 |
| `spring.factories`（service-impl） | 内容已全部注释 |
| `TensorDnnEstimator` + `tensorflow` 依赖 + `machine_learning` 配置 | 已确认移除（保留 `Estimator` 接口） |
| 8 个子模块 pom + 根 pom 的 modules/pluginManagement | 单模块化 |
| PageHelper 依赖 | MP 分页替代 |

### 4.3 单体融合要点

- **端口统一**为一个（默认 6580），管理端与引擎端 API 共存。
- **路由与安全边界**：JWT 拦截器只拦截 `/api/**`（管理端）；引擎端 `/event`、`/status` 等保持原有公开 + 签名逻辑。
- **异步隔离**：事件处理走既有 `RiskResultAsyncService` 异步链路，合并后为引擎事件处理配置**独立线程池**，
  避免管理端请求与事件计算互相争抢。
- **可拆分预留**：包结构按 `controller.admin` / `controller.engine` 划分，未来如需拆回双应用，仅需拆 controller 与启动类。

## 5. 依赖升级对照表

| 现状 | 升级到 | 改造点与影响面 |
|------|--------|----------------|
| Boot 2.2.5 / Java 8 | **Boot 3.5.x / Java 17** | `maven-compiler-plugin` 改 `release=17`；主类 `RestClientAutoConfiguration` exclude 的全类名需更新 |
| `javax.*`（56 个文件） | `jakarta.*` | Resource / PostConstruct / servlet / validation → jakarta；**OpenRewrite 自动迁移**；`javax.crypto` 为 JDK 自带不动 |
| tk.mybatis 4.0 + PageHelper 1.2.10 | **mybatis-plus-spring-boot3-starter 3.5.x** | Example → `LambdaQueryWrapper`；`PageHelper.startPage` → `selectPage`；移除 PageHelper |
| springfox 2.9.2（20 个文件） | **springdoc-openapi-starter-webmvc-ui 2.x** | `@Api→@Tag`、`@ApiOperation→@Operation`、`@ApiParam→@Parameter`；OpenRewrite 覆盖大部分 |
| fastjson 1.2.83（27 个文件） | **fastjson2** | import 替换为主；回归 autoType、日期序列化、null 处理行为差异 |
| jjwt 0.9.1 | jjwt 0.12.x | 仅 `JsonWebTokenService`（97 行），parser API 变化手工改 |
| groovy-all 2.4.6 | **groovy 4.0.x** | 注意 4.x 的 `groovy-all` 为 pom 型依赖；`GroovyClassLoader` 用法兼容 |
| elasticsearch-rest-high-level-client 7.11.2 | **7.17.x** | 改造集中 `SearchEngineServiceImpl`（201 行）及 EsConfig；服务端 ES 版本不动 |
| druid-spring-boot-starter 1.1.16 | **druid-spring-boot-3-starter 1.2.x** | 配置前缀基本不变 |
| easyexcel 2.1.3 / POI 3.17 | easyexcel 4.x（传递 POI 5.x） | 检查 `ExcelUtils` 对 POI 的直接引用；导入导出接口回归 |
| MapStruct 1.4.1 | MapStruct 1.6.x | 注意注解处理器与 Lombok 的执行顺序配置 |
| mysql-connector-java 8.0.16 | `com.mysql:mysql-connector-j`（版本随 Boot BOM） | 无代码改动 |
| pagehelper-spring-boot-starter 1.2.10 | **删除** | MP 分页替代 |
| tensorflow 1.12.0 | **删除** | DNN 功能随决策移除 |

**配置文件同步变化**：

- `spring.redis.*` → `spring.data.redis.*`
- MyBatis 配置段 `mybatis:` / `mapper:` → MP 的 `mybatis-plus:` 配置段
- 两套 `application*.yml` 合并为一套多 profile（dev/local），dev 中 MySQL/Mongo/Redis/ES 地址统一

## 6. dal 层移除方案

### 6.1 职责去向

| dal 中的职责 | 去向 |
|--------------|------|
| `XxxDal` 接口 + Impl（12 个） | **删除**，ServiceImpl 直接注入 `XxxMapper` |
| PO↔VO 转换（MapStruct） | 保留 `XxxMapping`，调用点从 DalImpl 移入 ServiceImpl |
| `@Cacheable` 缓存注解 | 平移到 ServiceImpl 对应方法（缓存 key 不变） |
| PageHelper 分页调用 | MP 的 `Page<T>` + `selectPage` |
| Query / PageResult bean | 移至 `model/query`，继续使用 |

### 6.2 改造模式示例

以 `FieldServiceImpl` 为例：

```java
// 改造前：service → dal → mapper
private final FieldDal fieldDal;
...
PageResult<FieldVO> page = fieldDal.query(query);

// 改造后：service → mapper（MyBatis-Plus）
private final FieldMapper fieldMapper;
private final FieldMapping fieldMapping;   // MapStruct 保留
...
Page<FieldPO> p = fieldMapper.selectPage(
        new Page<>(query.getPageNum(), query.getPageSize()),
        new LambdaQueryWrapper<FieldPO>()
                .eq(query.getModelId() != null, FieldPO::getModelId, query.getModelId()));
PageResult<FieldVO> page =
        new PageResult<>(fieldMapping.sourceToTarget(p.getRecords()), p.getTotal());
```

### 6.3 Example → Wrapper 映射表

| tk.mybatis Example 用法 | MyBatis-Plus 等价 |
|--------------------------|-------------------|
| `new Example(X.class)` + `andEqualTo("guid", x)` | `new LambdaQueryWrapper<XPO>().eq(XPO::getGuid, x)` |
| `andLike("name", "%x%")` | `.like(XPO::getName, "x")` |
| `orderByDesc("id")` | `.orderByDesc(XPO::getId)` |
| `selectByExample` | `selectList` |
| `selectCountByExample` | `selectCount` |
| `selectByPrimaryKey` | `selectById` |
| `insertSelective` | `insert` |
| `updateByPrimaryKeySelective` | `updateById` |
| `deleteByExample` | `delete(wrapper)` |
| `PageHelper.startPage(n, s)` + 查询 | `selectPage(new Page<>(n, s), wrapper)` |

### 6.4 PO 注解迁移（14 个 PO，模式固定，可脚本化）

| javax.persistence | MyBatis-Plus |
|-------------------|--------------|
| `@Table(name = "field")` | `@TableName("field")` |
| `@Id` / `@GeneratedValue` | `@TableId(type = IdType.AUTO)` |
| `@Column(name = "xxx")` | `@TableField("xxx")` |
| `@Transient` | `@TableField(exist = false)` |

## 7. 实施阶段

> **总体策略**：不做“先升级后合并”或“先合并后升级”的两步走，直接在新骨架上一次到位，
> 避免中间态维护两套 pom；机械迁移（jakarta、swagger 注解、配置）交给 OpenRewrite，
> 人工聚焦 dal 下沉与持久层改写。
> 全程在独立分支 `feature/java17-boot3` 进行，旧 master 保持可发布，升级期间不合入功能需求。

| 阶段 | 内容 | 验证标准 | 工期 |
|------|------|----------|------|
| **P0 骨架** | 新建单模块 pom（Boot 3.5.x 依赖树）；删 8 个子 pom 与根 pom 模块声明；建分支 | `mvn compile` 依赖树收敛、无冲突 | 1-2 天 |
| **P1 迁移 + jakarta** | 代码按目标结构搬入（包名基本不变）；OpenRewrite 跑 `UpgradeSpringBoot_3_x` recipe（javax→jakarta、springfox→springdoc、配置迁移）；admin/engine 两套 config 去重合并；双启动类合一 | 编译通过、应用可启动 | 2-3 天 |
| **P2 删 dal + MP 改造** | 删 dal 包 / mapper XML / PageHelper / tk.mybatis；PO 换 MP 注解；12 个 DalImpl 逻辑下沉到对应 ServiceImpl；`@Cacheable` 平移 | 模型 / 字段 / 规则 CRUD 冒烟通过 | 3-4 天 |
| **P3 专项依赖** | fastjson→fastjson2（27 文件）；jjwt 0.12；Groovy 4；ES client 7.17；easyexcel 4；移除 TF/DNN 与 `machine_learning` 配置 | 登录（JWT）、规则脚本动态编译、ES 查询正常 | 2 天 |
| **P4 单体融合** | JWT 拦截器路由规划（只拦 `/api/**`）；两套 yml 合并；端口统一；引擎事件处理独立线程池 | 单进程全链路：登录→建模型→发事件→命中→ES 报表 | 2 天 |
| **P5 回归 + 收尾** | 全功能回归（对照 wiki 手册主流程）；补最小冒烟测试集；修正测试目录 `src/main/test` → `src/test/java`；更新 README / 构建脚本 | 回归清单全绿 | 2 天 |

## 8. 风险与对策

| # | 风险 | 对策 |
|---|------|------|
| 1 | fastjson→fastjson2 行为差异（autoType、日期、null 处理） | P3 对核心 `JSON.parseObject` 的 VO 做序列化快照对比测试 |
| 2 | `RedisCacheAutoConfig` 自定义 CacheManager 在 Boot 3 下 API 变化 | 两套重复类先合并再适配，重点回归 `@Cacheable("models")` 缓存链路 |
| 3 | **存量 Groovy 规则脚本兼容性**（脚本存于 MySQL/Mongo，编译于 Groovy 2.4） | 回归时必须用**生产规则脚本样本**在 Groovy 4 下编译执行验证；保留 Groovy 版本回退预案 |
| 4 | 单体后管理端与引擎端互相影响 | 事件异步链路独立线程池；包结构按 controller.admin/engine 划分保留拆分能力 |
| 5 | POI 3.17 → 5.x 导致 Excel 导入导出异常 | `ExcelUtils` 及名单导入 / 事件导出接口逐一回归 |
| 6 | ES High-Level Client 为 deprecated 技术 | 本期锁 7.17.x；“换 ES 8 新 Java 客户端”立项为后续独立迭代 |
| 7 | 升级期间线上问题修复与改造冲突 | 独立分支 + 升级窗口内冻结功能需求；必要时以 cherry-pick 方式同步线上紧急修复 |

## 9. 工作量与验收

- **工作量估算**：熟悉项目的 1-2 人，合计约 **12-15 人日**（日历时间 2-3 周）。
  最大单块工作为 P2（dal 下沉 + MP 改造，12 个 ServiceImpl 逐个改写）。

- **验收标准**：
  1. 单模块工程 `mvn package` 产出单一可执行 jar，Java 17 运行；
  2. 单进程同时提供管理端与引擎端 API，管理端前端静态资源正常；
  3. 全链路回归通过：注册登录 → 建模型/字段/规则/名单 → 引擎发事件 → 规则命中 → 结果落 ES/Mongo → 报表查询；
  4. 存量生产 Groovy 规则脚本编译执行通过；
  5. 代码中不再存在 `radar.dal`、`tk.mybatis`、`javax.servlet/persistence/validation`、`springfox`、`fastjson`(1.x) 引用。

---

## 附录 A：包结构映射表

| 旧模块 | 旧位置 | 新位置 | 动作 |
|--------|--------|--------|------|
| radar-commons | `com.pgmmers.radar.util` | `com.pgmmers.radar.common.util` | 平移 |
| radar-commons | `mybatis.plugins.ModelRenamePlugin` | — | 删除 |
| radar-dao | `com.pgmmers.radar.mapper` | `com.pgmmers.radar.mapper` | 改继承 MP `BaseMapper` |
| radar-dao | `com.pgmmers.radar.model`（PO） | `com.pgmmers.radar.model.entity` | 换 MP 注解 |
| radar-dao | `resources/mapping/*.xml` | — | 删除 |
| radar-dal | `com.pgmmers.radar.dal.**` | — | 删除 |
| radar-dal | `com.pgmmers.radar.vo` | `com.pgmmers.radar.model.vo` | 平移 |
| radar-dal | `com.pgmmers.radar.mapstruct` | `com.pgmmers.radar.converter` | 平移 |
| radar-dal | `com.pgmmers.radar.enums` | `com.pgmmers.radar.enums` | 平移 |
| radar-dal | `com.pgmmers.radar.dal.bean`（Query 等） | `com.pgmmers.radar.model.query` | 平移 |
| radar-service / impl | `com.pgmmers.radar.service(**.impl)` | 原位 | 直连 mapper 改造 |
| radar-admin | `com.pgmmers.radar.controller` | `com.pgmmers.radar.controller.admin` | 平移 |
| radar-admin | `com.pgmmers.radar.intercpt` | `com.pgmmers.radar.interceptor` | 平移（改拼写） |
| radar-engine | `com.pgmmers.radar.controller` | `com.pgmmers.radar.controller.engine` | 平移 |
| admin/engine | `com.pgmmers.radar.config`（两套重复） | `com.pgmmers.radar.config` | **合并去重** |
| radar-kafka-demo | 独立模块 | `samples/radar-kafka-demo` | 移出主构建 |

## 附录 B：OpenRewrite 迁移配置（P1 使用）

```xml
<plugin>
    <groupId>org.openrewrite.maven</groupId>
    <artifactId>rewrite-maven-plugin</artifactId>
    <version>5.x</version>
    <configuration>
        <activeRecipes>
            <recipe>org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_5</recipe>
            <recipe>org.openrewrite.java.spring.boot2.SpringBootPropertiesToBoot3</recipe>
        </activeRecipes>
    </configuration>
    <dependencies>
        <dependency>
            <groupId>org.openrewrite.recipe</groupId>
            <artifactId>rewrite-spring</artifactId>
            <version>5.x</version>
        </dependency>
    </dependencies>
</plugin>
```

执行：`mvn rewrite:run`（先 `mvn rewrite:dryRun` 预览变更）。
OpenRewrite 覆盖：javax→jakarta、springfox→springdoc（大部分）、`spring.redis.*`→`spring.data.redis.*` 等
配置迁移与依赖坐标升级；dal 下沉、MP 改造、fastjson2 替换为人工处理。
