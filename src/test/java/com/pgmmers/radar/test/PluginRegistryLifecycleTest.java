package com.pgmmers.radar.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pgmmers.radar.service.impl.engine.plugin.PluginEntry;
import com.pgmmers.radar.service.impl.engine.plugin.PluginManager;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * 插件注册表生命周期语义单测（v3.1）：禁用集过滤（entry/activeEntries）、
 * 整代重载（目录缺失回退安全、内置优先于外置）。
 */
class PluginRegistryLifecycleTest {

    private PluginManager pluginManager;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        pluginManager = new PluginManager();
        pluginManager.register(new PluginEntry("GOOD", "GOOD", "正常插件", "STRING", false,
                PluginEntry.Source.BUILTIN_V3, List.of(), List.of(),
                (item, jsonInfo, sf) -> "ok"));
        pluginManager.register(new PluginEntry("BOOM", "BOOM", "抛异常插件", "STRING", false,
                PluginEntry.Source.BUILTIN_V3, List.of(), List.of(),
                (item, jsonInfo, sf) -> "bad"));
    }

    @Test
    void disabledKeysHideEntryFromEngineAndForms() throws Exception {
        pluginManager.setDisabledKeys(Set.of("GOOD"));
        assertNull(pluginManager.entry("GOOD"), "引擎执行链视角：禁用=不可见");
        assertEquals("bad", pluginManager.entry("BOOM").execute(null, null, new String[0]),
                "未禁用条目正常可执行");
        List<String> activeKeys = pluginManager.activeEntries().stream()
                .map(PluginEntry::getKey).toList();
        assertTrue(activeKeys.contains("BOOM"));
        assertTrue(!activeKeys.contains("GOOD"), "表单渲染视角：禁用=过滤");
        // 全量视图仍可见（管理页数据源）
        assertEquals(2, pluginManager.entries().size());
    }

    @Test
    void rebuildOnMissingDirKeepsRegistryAndReturnsEmpty() {
        ReflectionTestUtils.setField(pluginManager, "pluginsDir",
                tempDir.resolve("not-exist").toString());
        List<String> registered = pluginManager.rebuildExternal(0);
        assertTrue(registered.isEmpty());
        assertEquals(2, pluginManager.entries().size(), "内置条目不受影响");
    }

    @Test
    void versionAndProviderCarriedThroughEntry() {
        PluginEntry entry = new PluginEntry("V", "V", "版本插件", "STRING", false,
                PluginEntry.Source.EXTERNAL, "1.2.3", "vendor-x",
                List.of(), List.of(), (item, jsonInfo, sf) -> null);
        pluginManager.register(entry);
        assertEquals("1.2.3", pluginManager.entry("V").getVersion());
        assertEquals("vendor-x", pluginManager.entry("V").getProvider());
    }
}
