package com.radar.plugin.testkit;

import com.radar.plugin.api.KeyValueCache;
import com.radar.plugin.api.FeatureContext;
import org.slf4j.LoggerFactory;

/**
 * 测试用 {@link FeatureContext} 快捷构造：静默 logger + 内存缓存工厂。
 *
 * <pre>{@code
 * FeatureContext ctx = TestContexts.context(new MockHttpTool());
 * FeatureContext ctx2 = TestContexts.context(new MockHttpTool(), new RecordingCache());
 * }</pre>
 *
 * @since 3.1.0
 */
public final class TestContexts {

    private TestContexts() {
    }

    /** 静默 logger + 每次 cache(name, ttl) 返回独立 RecordingCache。 */
    public static FeatureContext context(MockHttpTool http) {
        return context(http, null);
    }

    /** 静默 logger + 共享缓存实例（断言缓存行为用）。 */
    public static FeatureContext context(MockHttpTool http, KeyValueCache sharedCache) {
        return new FeatureContext(LoggerFactory.getLogger(TestContexts.class),
                (name, ttl) -> sharedCache == null ? new RecordingCache() : sharedCache,
                http);
    }
}
