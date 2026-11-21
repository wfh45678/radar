package com.radar.plugin.testkit;

import com.radar.plugin.api.KeyValueCache;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 测试用 {@link KeyValueCache}：简单内存实现 + 键录制（断言缓存命中/未命中次数）。
 *
 * <pre>{@code
 * RecordingCache cache = new RecordingCache();
 * PluginContext ctx = TestContexts.context(new MockHttpTool(), cache);
 * // ... 执行插件两次 ...
 * assertEquals(1, cache.loads().size()); // 第二次应命中缓存
 * }</pre>
 *
 * @since 3.1.0
 */
public final class RecordingCache implements KeyValueCache {

    private final Map<Object, Object> store = new HashMap<>();
    private final List<Object> loads = new ArrayList<>();

    /** @return 实际触发 loader 的键（未命中记录）。 */
    public List<Object> loads() {
        return List.copyOf(loads);
    }

    /** 清空存储与录制。 */
    public void reset() {
        store.clear();
        loads.clear();
    }

    @Override
    public <K, V> V get(K key, Function<? super K, ? extends V> loader) {
        if (store.containsKey(key)) {
            return (V) store.get(key);
        }
        loads.add(key);
        V value = loader.apply(key);
        if (value != null) {
            store.put(key, value);
        }
        return value;
    }
}
