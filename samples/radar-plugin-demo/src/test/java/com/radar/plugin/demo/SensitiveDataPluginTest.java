package com.radar.plugin.demo;

import com.radar.plugin.api.HttpResult;
import com.radar.plugin.api.HttpTool;
import com.radar.plugin.api.KeyValueCache;
import com.radar.plugin.api.PluginContext;
import com.radar.plugin.api.PluginRequest;
import com.radar.plugin.api.PluginResult;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

public class SensitiveDataPluginTest {

    private final SensitiveDataPlugin plugin = new SensitiveDataPlugin();

    /** 测试用上下文：缓存直接透传 loader（等价无缓存），HTTP 不触达。 */
    private PluginContext context() {
        return new PluginContext(LoggerFactory.getLogger("test"),
                (name, ttl) -> new KeyValueCache() {
                    @Override
                    public <K, V> V get(K key, Function<? super K, ? extends V> loader) {
                        return loader.apply(key);
                    }
                },
                new HttpTool() {
                    @Override
                    public HttpResult get(String url) { return new HttpResult(0, "unused"); }

                    @Override
                    public HttpResult postJson(String url, String jsonBody) { return new HttpResult(0, "unused"); }
                });
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> execute(Map<String, Object> args, String value) {
        PluginResult result = plugin.execute(
                new PluginRequest(1L, "sd", Map.of("f1", value), List.of(value),
                        args, null, Map.of("f1", value), null),
                context());
        return (Map<String, Object>) result.getPayload();
    }

    @Test
    public void detectsMobile() {
        // 无显式参数：布尔参数按 schema 默认值（识别手机号=开，识别身份证=开，脱敏=关）
        Map<String, Object> columns = execute(Map.of(), "订单 13812345678 已支付");
        Assertions.assertEquals("true", columns.get("hasMobile"));
        Assertions.assertEquals("false", columns.get("hasIdCard"));
        Assertions.assertEquals("订单 13812345678 已支付", columns.get("masked"));
    }

    @Test
    public void detectsIdCard() {
        Map<String, Object> columns = execute(Map.of(), "身份证 110101199003077516 提交");
        Assertions.assertEquals("true", columns.get("hasIdCard"));
    }

    @Test
    public void masksWhenEnabled() {
        Map<String, Object> columns = execute(
                Map.of("checkMobile", Boolean.TRUE, "mask", Boolean.TRUE),
                "手机 18657150000");
        Assertions.assertEquals("手机 186****0000", columns.get("masked"));
    }

    @Test
    public void schemaIsDeclared() {
        Assertions.assertEquals(3, plugin.paramSchema().size());
        Assertions.assertEquals(3, plugin.outputMeta().size());
        Assertions.assertEquals("SENSITIVE_DATA", plugin.key());
        Assertions.assertFalse(plugin.remote());
    }
}
