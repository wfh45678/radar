package com.pgmmers.radar.test;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pgmmers.radar.config.EngineMetrics;
import com.pgmmers.radar.model.vo.model.PreItemVO;
import com.pgmmers.radar.service.impl.engine.AntiFraudServiceImpl;
import com.pgmmers.radar.service.impl.engine.plugin.PluginManager;
import com.pgmmers.radar.service.impl.engine.plugin.PluginEntry;
import com.pgmmers.radar.service.model.PreItemService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * L1 纯单测（v3.0）：preItems 插件失败隔离 / strict 模式 / 插件缺失 / 指标计数。
 * 不依赖任何容器与真实插件实现，PluginEntry 执行器用 lambda 注入。
 */
public class PluginIsolationTest {

    private PluginManager pluginManager;
    private AntiFraudServiceImpl service;
    private SimpleMeterRegistry registry;
    private PreItemService preItemService;

    @BeforeEach
    public void setUp() {
        registry = new SimpleMeterRegistry();
        pluginManager = new PluginManager();
        pluginManager.register(new PluginEntry("GOOD", "GOOD", "正常插件", "STRING", false,
                PluginEntry.Source.BUILTIN_V3, List.of(), List.of(),
                (item, jsonInfo, sf) -> "ok"));
        pluginManager.register(new PluginEntry("BOOM", "BOOM", "抛异常插件", "STRING", false,
                PluginEntry.Source.BUILTIN_V3, List.of(), List.of(),
                (item, jsonInfo, sf) -> { throw new IllegalStateException("boom"); }));

        service = new AntiFraudServiceImpl();
        preItemService = mock(PreItemService.class);
        ReflectionTestUtils.setField(service, "pluginManager", pluginManager);
        ReflectionTestUtils.setField(service, "preItemService", preItemService);
        ReflectionTestUtils.setField(service, "engineMetrics", new EngineMetrics(registry));
    }

    private PreItemVO item(String plugin, String destField) {
        PreItemVO item = new PreItemVO();
        item.setStatus(1);
        item.setPlugin(plugin);
        item.setDestField(destField);
        item.setSourceField("f1");
        return item;
    }

    private Map<String, Object> prepare(PreItemVO... items) {
        when(preItemService.listPreItem(anyLong())).thenReturn(List.of(items));
        Map<String, Object> json = new HashMap<>();
        json.put("f1", "v");
        return service.prepare(1L, json);
    }

    @Test
    public void isolatedByDefault() {
        ReflectionTestUtils.setField(service, "isolation", "isolated");
        Map<String, Object> result = prepare(
                item("GOOD", "goodField"),
                item("BOOM", "boomField"),
                item("NOT_REGISTERED", "missingField"));
        // 正常插件正常产出
        Assertions.assertEquals("ok", result.get("goodField"));
        // 异常插件：错误标记，事件链路继续
        Assertions.assertTrue(((Map<?, ?>) result.get("boomField")).containsKey("__plugin_error__"));
        // 缺失插件：错误标记，事件链路继续
        Assertions.assertTrue(((Map<?, ?>) result.get("missingField")).containsKey("__plugin_error__"));
        // 指标：fail 与 not_found 均计数
        Assertions.assertEquals(1.0, registry.get("radar_plugin_executions_total")
                .tag("plugin", "BOOM").tag("result", "fail").counter().count());
        Assertions.assertEquals(1.0, registry.get("radar_plugin_executions_total")
                .tag("plugin", "unknown").tag("result", "not_found").counter().count());
    }

    @Test
    public void strictFailsEvent() {
        ReflectionTestUtils.setField(service, "isolation", "strict");
        Assertions.assertThrows(RuntimeException.class,
                () -> prepare(item("BOOM", "boomField")));
    }

    @Test
    public void unknownPluginTagIsBounded() {
        ReflectionTestUtils.setField(service, "isolation", "isolated");
        Map<String, Object> result = prepare(item("WHATEVER_TYPO_XYZ", "missingField"));
        Assertions.assertTrue(((Map<?, ?>) result.get("missingField")).containsKey("__plugin_error__"));
        // 缺失插件计数归并到 unknown 标签，防指标基数爆炸
        Assertions.assertTrue(registry.find("radar_plugin_executions_total")
                .tags("plugin", "WHATEVER_TYPO_XYZ").counter() == null);
        Assertions.assertEquals(1.0, registry.get("radar_plugin_executions_total")
                .tag("plugin", "unknown").tag("result", "not_found").counter().count());
    }
}
