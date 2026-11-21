package com.radar.plugin.demo.remote;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.radar.plugin.api.Plugin;
import com.radar.plugin.api.PluginException;
import com.radar.plugin.api.PluginResult;
import com.radar.plugin.testkit.MockHttpTool;
import com.radar.plugin.testkit.PluginRequestBuilder;
import com.radar.plugin.testkit.RecordingCache;
import com.radar.plugin.testkit.TestContexts;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * testkit 单测模板（v3.1）：MockHttpTool 桩外呼、RecordingCache 断言缓存、
 * PluginRequestBuilder 构造入参——远程插件无需真实网络即可全链路验证。
 */
class ExchangeRatePluginTest {

    private static final String RATE_BODY =
            "{\"result\":\"success\",\"base_code\":\"USD\","
                    + "\"rates\":{\"USD\":1.0,\"CNY\":7.2,\"EUR\":0.9,\"JPY\":150.0}}";

    @Test
    void shouldConvertAmountWithStubbedRates() throws PluginException {
        MockHttpTool http = new MockHttpTool()
                .stub(ExchangeRatePlugin.API_URL, 200, RATE_BODY);
        Plugin plugin = new ExchangeRatePlugin();

        PluginResult result = plugin.execute(
                PluginRequestBuilder.create()
                        .modelId(99L)
                        .destField("amountCny")
                        .sourceField("amount", 100)
                        .arg("from", "USD")
                        .arg("to", "CNY")
                        .build(),
                TestContexts.context(http, new RecordingCache()));

        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) result.getPayload();
        assertEquals(7.2, (double) (Double) payload.get("RATE"), 1e-9);
        assertEquals(720.0, (double) (Double) payload.get("CONVERTED"), 1e-9);
        // 外呼真实发生一次（GET 汇率接口）
        assertEquals(1, http.calls().size());
        assertEquals("GET", http.calls().get(0).method());
    }

    @Test
    void shouldHitCacheOnSecondCall() throws PluginException {
        MockHttpTool http = new MockHttpTool()
                .stub(ExchangeRatePlugin.API_URL, 200, RATE_BODY);
        RecordingCache cache = new RecordingCache();
        Plugin plugin = new ExchangeRatePlugin();
        var context = TestContexts.context(http, cache);
        var request = PluginRequestBuilder.create()
                .sourceField("amount", 1).arg("from", "CNY").arg("to", "EUR").build();

        plugin.execute(request, context);
        plugin.execute(request, context);

        // 两次执行只应外呼一次（第二次命中 10 分钟缓存）
        assertEquals(1, http.calls().size());
        assertEquals(1, cache.loads().size());
    }

    @Test
    void shouldFailIsolatedWhenRemoteDown() {
        MockHttpTool http = new MockHttpTool()
                .stub(ExchangeRatePlugin.API_URL, 503, "service unavailable");
        Plugin plugin = new ExchangeRatePlugin();

        // 引擎隔离语义：业务失败抛 PluginException，事件链路不受影响
        PluginException e = assertThrows(PluginException.class, () -> plugin.execute(
                PluginRequestBuilder.create()
                        .sourceField("amount", 1).arg("from", "CNY").arg("to", "USD").build(),
                TestContexts.context(http)));
        assertTrue(e.getMessage().contains("汇率接口调用失败"));
    }

    @Test
    void shouldFailWhenAmountMissing() {
        Plugin plugin = new ExchangeRatePlugin();
        PluginException e = assertThrows(PluginException.class, () -> plugin.execute(
                PluginRequestBuilder.create().arg("from", "CNY").arg("to", "USD").build(),
                TestContexts.context(new MockHttpTool())));
        assertEquals("源字段取值为空，无法换算", e.getMessage());
    }
}
