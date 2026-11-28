package com.radar.plugin.demo.remote;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.radar.plugin.api.FeatureProcessor;
import com.radar.plugin.api.FeatureException;
import com.radar.plugin.api.FeatureResponse;
import com.radar.plugin.testkit.MockHttpTool;
import com.radar.plugin.testkit.FeatureRequestBuilder;
import com.radar.plugin.testkit.RecordingCache;
import com.radar.plugin.testkit.TestContexts;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * testkit 单测模板（v3.1）：MockHttpTool 桩外呼、RecordingCache 断言缓存、
 * FeatureRequestBuilder 构造入参——远程插件无需真实网络即可全链路验证。
 */
class ExchangeRatePluginTest {

    private static final String RATE_BODY =
            "{\"result\":\"success\",\"base_code\":\"USD\","
                    + "\"rates\":{\"USD\":1.0,\"CNY\":7.2,\"EUR\":0.9,\"JPY\":150.0}}";

    @Test
    void shouldConvertAmountWithStubbedRates() throws FeatureException {
        MockHttpTool http = new MockHttpTool()
                .stub(ExchangeRatePlugin.API_URL, 200, RATE_BODY);
        FeatureProcessor processor = new ExchangeRatePlugin();

        FeatureResponse result = processor.execute(
                FeatureRequestBuilder.create()
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
    void shouldHitCacheOnSecondCall() throws FeatureException {
        MockHttpTool http = new MockHttpTool()
                .stub(ExchangeRatePlugin.API_URL, 200, RATE_BODY);
        RecordingCache cache = new RecordingCache();
        FeatureProcessor processor = new ExchangeRatePlugin();
        var context = TestContexts.context(http, cache);
        var request = FeatureRequestBuilder.create()
                .sourceField("amount", 1).arg("from", "CNY").arg("to", "EUR").build();

        processor.execute(request, context);
        processor.execute(request, context);

        // 两次执行只应外呼一次（第二次命中 10 分钟缓存）
        assertEquals(1, http.calls().size());
        assertEquals(1, cache.loads().size());
    }

    @Test
    void shouldFailIsolatedWhenRemoteDown() {
        MockHttpTool http = new MockHttpTool()
                .stub(ExchangeRatePlugin.API_URL, 503, "service unavailable");
        FeatureProcessor processor = new ExchangeRatePlugin();

        // 引擎隔离语义：业务失败抛 FeatureException，事件链路不受影响
        FeatureException e = assertThrows(FeatureException.class, () -> processor.execute(
                FeatureRequestBuilder.create()
                        .sourceField("amount", 1).arg("from", "CNY").arg("to", "USD").build(),
                TestContexts.context(http)));
        assertTrue(e.getMessage().contains("汇率接口调用失败"));
    }

    @Test
    void shouldFailWhenAmountMissing() {
        FeatureProcessor processor = new ExchangeRatePlugin();
        FeatureException e = assertThrows(FeatureException.class, () -> processor.execute(
                FeatureRequestBuilder.create().arg("from", "CNY").arg("to", "USD").build(),
                TestContexts.context(new MockHttpTool())));
        assertEquals("源字段取值为空，无法换算", e.getMessage());
    }
}
