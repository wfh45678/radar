package com.radar.plugin.api;

import java.time.Duration;
import java.util.Objects;
import org.slf4j.Logger;

/**
 * 引擎提供的插件运行时工具集：日志、进程内缓存、HTTP。
 * 每次执行都会传入，插件可保存引用但不应跨模型缓存业务数据。
 *
 * @since 3.0.0
 */
public final class FeatureContext {

    /** 缓存工厂：按 (名称, TTL) 创建/复用进程内缓存（同名同 TTL 复用同一实例）。 */
    public interface CacheFactory {
        KeyValueCache create(String name, Duration ttl);
    }

    private final Logger logger;
    private final CacheFactory cacheFactory;
    private final HttpTool http;

    /** 由引擎构造，插件不应自行实例化。 */
    public FeatureContext(Logger logger, CacheFactory cacheFactory, HttpTool http) {
        this.logger = Objects.requireNonNull(logger, "logger");
        this.cacheFactory = Objects.requireNonNull(cacheFactory, "cacheFactory");
        this.http = Objects.requireNonNull(http, "http");
    }

    /** 插件专属 logger（名字为插件 key）。 */
    public Logger logger() {
        return logger;
    }

    /**
     * 获取进程内 TTL 缓存。同名同 TTL 多次调用返回同一实例。
     *
     * <pre>{@code
     * KeyValueCache cache = context.cache("ip2location", Duration.ofHours(1));
     * Location loc = cache.get(ip, this::lookup);
     * }</pre>
     */
    public KeyValueCache cache(String name, Duration ttl) {
        Objects.requireNonNull(name, "name");
        Duration effective = ttl == null ? Duration.ofMinutes(5) : ttl;
        return cacheFactory.create(name, effective);
    }

    /** HTTP 工具（带超时与私网防护）。 */
    public HttpTool http() {
        return http;
    }
}
