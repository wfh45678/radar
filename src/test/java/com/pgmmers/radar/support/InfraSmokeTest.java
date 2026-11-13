package com.pgmmers.radar.support;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestTemplate;

/**
 * S1 测试基建冒烟：验证四件套容器接线与 MySQL 种子脚本导入。
 * radar-init.sql 无建库语句且 1.0.8 增量插入 system 模型（ID=1），
 * 故 engine_model 计数 ≥ 1 即证明四个初始化脚本全部执行成功。
 */
class InfraSmokeTest extends AbstractIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void mysqlSeedImported() {
        Integer modelCount = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM engine_model", Integer.class);
        assertTrue(modelCount != null && modelCount >= 1, "engine_model 应包含种子模型，实际: " + modelCount);
    }

    @Test
    void redisPing() {
        String pong = stringRedisTemplate.execute((RedisCallback<String>) connection -> connection.ping());
        assertTrue("PONG".equalsIgnoreCase(pong));
    }

    @Test
    void esReachable() throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        String clusterInfo = restTemplate.getForObject("http://" + es.getHttpHostAddress(), String.class);
        assertTrue(clusterInfo != null && clusterInfo.contains("You Know, for Search"),
                "ES 集群信息应可达，实际: " + clusterInfo);
    }
}
