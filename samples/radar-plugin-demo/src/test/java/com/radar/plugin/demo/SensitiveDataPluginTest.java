package com.radar.plugin.demo;

import com.radar.plugin.api.PluginResult;
import com.radar.plugin.testkit.MockHttpTool;
import com.radar.plugin.testkit.PluginRequestBuilder;
import com.radar.plugin.testkit.RecordingCache;
import com.radar.plugin.testkit.TestContexts;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SensitiveDataPluginTest {

    private final SensitiveDataPlugin plugin = new SensitiveDataPlugin();

    /**
     * v3.1 起 context/request 构造统一走 testkit（本测试 v3.0 时手搓 lambda 上下文，
     * 现迁移为 PluginRequestBuilder + MockHttpTool + RecordingCache 组合）。
     */
    private final RecordingCache cache = new RecordingCache();

    @SuppressWarnings("unchecked")
    private Map<String, Object> execute(Map<String, Object> args, String value) {
        PluginResult result = plugin.execute(
                PluginRequestBuilder.create()
                        .modelId(1L)
                        .destField("sd")
                        .sourceField("f1", value)
                        .event("f1", value)
                        .args(args)
                        .build(),
                TestContexts.context(new MockHttpTool(), cache));
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
