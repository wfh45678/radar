package com.pgmmers.radar.test;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pgmmers.radar.config.EngineMetrics;
import com.pgmmers.radar.enums.FeatureType;
import com.pgmmers.radar.model.vo.model.FeatureVO;
import com.pgmmers.radar.service.impl.engine.AntiFraudServiceImpl;
import com.pgmmers.radar.service.impl.engine.feature.EnrichExecutor;
import com.pgmmers.radar.service.impl.engine.feature.FeatureExecutorRegistry;
import com.pgmmers.radar.service.impl.engine.plugin.PluginEntry;
import com.pgmmers.radar.service.impl.engine.plugin.PluginManager;
import com.pgmmers.radar.service.model.FeatureService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * L1 纯单测（v3.0 引入，v4.1 随特征 SPI 迁移）：ENRICH 特征插件失败隔离 /
 * strict 模式 / 插件缺失 / 指标计数。
 * 不依赖任何容器与真实插件实现，PluginEntry 执行器用 lambda 注入。
 */
public class PluginIsolationTest {

    private PluginManager pluginManager;
    private AntiFraudServiceImpl service;
    private EnrichExecutor enrichExecutor;
    private SimpleMeterRegistry registry;
    private FeatureService featureService;

    @BeforeEach
    public void setUp() {
        registry = new SimpleMeterRegistry();
        pluginManager = new PluginManager();
        pluginManager.register(new PluginEntry("GOOD", "GOOD", "正常插件", "STRING", false,
                PluginEntry.Source.BUILTIN_V3, List.of(), List.of(),
                (feature, jsonInfo, sf) -> "ok"));
        pluginManager.register(new PluginEntry("BOOM", "BOOM", "抛异常插件", "STRING", false,
                PluginEntry.Source.BUILTIN_V3, List.of(), List.of(),
                (feature, jsonInfo, sf) -> { throw new IllegalStateException("boom"); }));

        service = new AntiFraudServiceImpl();
        featureService = mock(FeatureService.class);
        enrichExecutor = new EnrichExecutor();
        ReflectionTestUtils.setField(enrichExecutor, "pluginManager", pluginManager);
        ReflectionTestUtils.setField(enrichExecutor, "engineMetrics", new EngineMetrics(registry));
        FeatureExecutorRegistry registryHolder = new FeatureExecutorRegistry(List.of(enrichExecutor));
        ReflectionTestUtils.setField(service, "executorRegistry", registryHolder);
        ReflectionTestUtils.setField(service, "featureService", featureService);
        ReflectionTestUtils.setField(service, "engineMetrics", new EngineMetrics(registry));
    }

    private FeatureVO item(String plugin, String featureName) {
        FeatureVO feature = new FeatureVO();
        feature.setStatus(1);
        feature.setType(FeatureType.ENRICH.name());
        feature.setFeatureName(featureName);
        feature.setScenarioId(1L);
        com.fasterxml.jackson.databind.node.ObjectNode definition = com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.objectNode();
        definition.put("plugin", plugin);
        definition.putArray("sourceFields").add("f1");
        feature.setDefinition(definition);
        return feature;
    }

    private Map<String, Object> item0 = null;

    private Map<String, Object> prepare(FeatureVO... items) {
        when(featureService.listFeature(anyLong())).thenReturn(List.of(items));
        Map<String, Object> json = new HashMap<>();
        json.put("f1", "v");
        return service.executeEntryFeatures(1L, json);
    }

    @Test
    public void isolatedByDefault() {
        ReflectionTestUtils.setField(enrichExecutor, "isolation", "isolated");
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
        ReflectionTestUtils.setField(enrichExecutor, "isolation", "strict");
        Assertions.assertThrows(RuntimeException.class,
                () -> prepare(item("BOOM", "boomField")));
    }

    @Test
    public void unknownPluginTagIsBounded() {
        ReflectionTestUtils.setField(enrichExecutor, "isolation", "isolated");
        Map<String, Object> result = prepare(item("WHATEVER_TYPO_XYZ", "missingField"));
        Assertions.assertTrue(((Map<?, ?>) result.get("missingField")).containsKey("__plugin_error__"));
        // 缺失插件计数归并到 unknown 标签，防指标基数爆炸
        Assertions.assertTrue(registry.find("radar_plugin_executions_total")
                .tags("plugin", "WHATEVER_TYPO_XYZ").counter() == null);
        Assertions.assertEquals(1.0, registry.get("radar_plugin_executions_total")
                .tag("plugin", "unknown").tag("result", "not_found").counter().count());
    }
}
