package com.pgmmers.radar.support;

import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

/**
 * L3 集成测试基类：Testcontainers 拉起 MySQL/MongoDB/Redis/Elasticsearch 四件套。
 *
 * <p>依赖 Docker 环境。本地无 Docker 时通过 {@code mvn test -DexcludedGroups=integration} 跳过，
 * CI 环境经 ci profile（env.CI）自动全量执行，见 pom 的 surefire.excludedGroups 配置。</p>
 *
 * <p>连接注入说明：
 * MySQL / MongoDB / Redis 由 Boot 3.1+ 的 {@code @ServiceConnection} 自动接线（覆盖 yml）；
 * 应用 ES 客户端走自定义 {@code EsConfig}（读 elasticsearch.url 属性），
 * {@code @ServiceConnection} 覆盖不到，需 {@code @DynamicPropertySource} 手动注册。</p>
 *
 * <p>MySQL 初始化：sql/radar-init.sql 为 v2.0.0 全量基线（已合并原 2019 基线与
 * 1.0.6/1.0.7/1.0.8 增量，经 MySQL 8.0 迁移导出并回灌验证），无建库语句，
 * 容器由 MYSQL_DATABASE=radar 建库后自动执行。</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Tag("integration")
public abstract class AbstractIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
            .withDatabaseName("radar")
            .withUsername("radar")
            .withPassword("radar")
            .withCopyFileToContainer(MountableFile.forHostPath("sql/radar-init.sql"),
                    "/docker-entrypoint-initdb.d/1-radar-init.sql");

    @Container
    @ServiceConnection
    static MongoDBContainer mongo = new MongoDBContainer(DockerImageName.parse("mongo:4.4"));

    @Container
    @ServiceConnection
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    /**
     * 服务端用 7.17.x（tag 7.17.15 确定存在），与客户端 7.17.29 兼容；
     * xpack 安全关闭对应应用 EsConfig 的无认证模式。
     */
    @Container
    static ElasticsearchContainer es = new ElasticsearchContainer(
                    DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:7.17.15"))
            .withEnv("discovery.type", "single-node")
            .withEnv("xpack.security.enabled", "false")
            .withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m");

    @DynamicPropertySource
    static void registerCustomProperties(DynamicPropertyRegistry registry) {
        registry.add("elasticsearch.url", () -> "http://" + es.getHttpHostAddress());
    }
}
