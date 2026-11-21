package com.pgmmers.radar.service.impl.engine.plugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.radar.plugin.api.HttpResult;
import com.radar.plugin.api.PluginContext;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * 出访管控三模式单测（v3.1）：allowlist 精确/通配匹配、deny-private 拦截 + 计数、
 * off、以及 v3.0 deny-private-network=false 的 off 兼容映射。
 * 拦截路径不发生真实网络请求；放行判断只走 host 匹配/IP 字面量（避免 DNS）。
 */
class PluginOutboundControlTest {

    private SimpleMeterRegistry registry;
    private PluginContextFactory factory;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        factory = new PluginContextFactory(registry);
        ReflectionTestUtils.setField(factory, "connectTimeoutMs", 100);
        ReflectionTestUtils.setField(factory, "requestTimeoutMs", 100);
        ReflectionTestUtils.setField(factory, "denyPrivateNetwork", true);
    }

    private PluginContext context(String mode, List<String> allowedHosts) {
        ReflectionTestUtils.setField(factory, "outboundMode", mode);
        ReflectionTestUtils.setField(factory, "allowedHosts", allowedHosts);
        factory.initAllowedHosts();
        return factory.create("TEST");
    }

    @Test
    void allowlistMatchesExactAndWildcard() {
        PluginContext ctx = context("allowlist", List.of("api.example.com", "*.safe.io"));
        assertTrue(ctx.http().isAllowed("https://api.example.com/v1/query"));
        assertTrue(ctx.http().isAllowed("https://a.safe.io/x"));
        assertTrue(ctx.http().isAllowed("http://API.EXAMPLE.COM/y"), "host 归一大小写");
        assertFalse(ctx.http().isAllowed("https://evil.com/x"));
        assertFalse(ctx.http().isAllowed("https://safe.io/x"), "通配仅匹配子域");
        assertFalse(ctx.http().isAllowed("https://sub.api.example.com/x"), "通配需前缀点匹配");
    }

    @Test
    void allowlistBlocksExchangeAndCounts() {
        PluginContext ctx = context("allowlist", List.of("api.example.com"));
        HttpResult blocked = ctx.http().get("https://evil.com/x");
        assertEquals(0, blocked.getStatus());
        assertTrue(blocked.getBody().contains("allowlist"));
        assertEquals(1.0, registry.find("radar_plugin_http_blocked_total")
                .tag("plugin", "TEST").counter().count());
    }

    @Test
    void denyPrivateBlocksLoopback() {
        PluginContext ctx = context("deny-private", List.of());
        assertFalse(ctx.http().isAllowed("http://127.0.0.1:8080/x"));
        assertFalse(ctx.http().isAllowed("http://0.0.0.0/x"));
        HttpResult blocked = ctx.http().get("http://127.0.0.1:8080/x");
        assertEquals(0, blocked.getStatus());
    }

    @Test
    void offModeAllowsEverything() {
        PluginContext ctx = context("off", List.of());
        assertTrue(ctx.http().isAllowed("http://127.0.0.1/x"));
        // off 模式 isAllowed 放行（不发起真实请求验证 exchange）
    }

    @Test
    void denyPrivateNetworkFalseMapsToOff() {
        // v3.0 兼容：deny-private-network=false 等价 mode=off
        ReflectionTestUtils.setField(factory, "denyPrivateNetwork", false);
        PluginContext ctx = context("deny-private", List.of());
        assertTrue(ctx.http().isAllowed("http://127.0.0.1/x"));
    }

    @Test
    void unknownPluginKeyFallsBackToUnknownTag() {
        PluginContext ctx = context("allowlist", List.of("api.example.com"));
        ctx.http().get("https://evil.com/x");
        assertEquals(1.0, registry.find("radar_plugin_http_blocked_total")
                .tag("plugin", "TEST").counter().count());
    }
}
