package com.pgmmers.radar.service.impl.engine.plugin;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.radar.plugin.api.HttpResult;
import com.radar.plugin.api.HttpTool;
import com.radar.plugin.api.KeyValueCache;
import com.radar.plugin.api.PluginContext;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 引擎侧 {@link PluginContext} 工厂：为每个插件提供日志/进程内缓存/带出访管控的 HTTP 工具。
 *
 * <p>v3.1 出访管控三模式：{@code plugin.http.mode=deny-private}（默认，拒绝私网/回环目标）/
 * {@code allowlist}（仅放行 {@code plugin.http.allowed-hosts}，通配 {@code *.host}）/
 * {@code off}。v3.0 的 {@code plugin.http.deny-private-network=false} 等价于 off（向后兼容）。</p>
 *
 * @since 3.0.0
 */
@Component
public class PluginContextFactory {

    private static final Logger logger = LoggerFactory.getLogger(PluginContextFactory.class);

    private final MeterRegistry meterRegistry;

    public PluginContextFactory(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Value("${plugin.http.connect-timeout-ms:2000}")
    private int connectTimeoutMs;

    @Value("${plugin.http.request-timeout-ms:2000}")
    private int requestTimeoutMs;

    /**
     * 出访管控模式（v3.1）：deny-private（默认，拒绝私网/回环目标）/
     * allowlist（仅放行 plugin.http.allowed-hosts）/
     * off（仅保留超时兜底）。兼容 v3.0 的 deny-private-network=false 等价于 off。
     */
    @Value("${plugin.http.mode:deny-private}")
    private String outboundMode;

    /** allowlist 模式放行主机（精确如 api.example.com，或通配如 *.example.com）。 */
    @Value("${plugin.http.allowed-hosts:}")
    private List<String> allowedHosts;

    /** v3.0 遗留开关：false 等价于 mode=off（保留向后兼容）。 */
    @Value("${plugin.http.deny-private-network:true}")
    private boolean denyPrivateNetwork;

    /** 允许的私网目标（allowlist 模式下显式列入即可放行内网服务）。 */
    private volatile Set<String> allowedHostSet = Set.of();

    /** @Value 注入晚于字段初始化，HTTP 客户端按首次调用懒构建。 */
    private volatile HttpClient httpClient;

    private final ConcurrentHashMap<String, KeyValueCache> caches = new ConcurrentHashMap<>();

    /** 构建指定插件 key 的运行时上下文。 */
    public PluginContext create(String pluginKey) {
        Logger pluginLogger = LoggerFactory.getLogger("plugin." + pluginKey);
        return new PluginContext(pluginLogger, this::createCache, new GuardedHttpTool(pluginKey));
    }

    /** 进程内 TTL 缓存（同名复用；TTL 变化时以新实例替换）。 */
    private KeyValueCache createCache(String name, Duration ttl) {
        return caches.compute(name, (k, existing) -> {
            if (existing instanceof PluginCaffeineCache caffeine
                    && caffeine.ttlEquals(ttl)) {
                return existing;
            }
            Cache<Object, Object> cache = Caffeine.newBuilder()
                    .expireAfterWrite(ttl)
                    .maximumSize(10_000)
                    .build();
            return new PluginCaffeineCache(ttl, cache);
        });
    }

    /** 出访管控生效模式（含 v3.0 deny-private-network 兼容归一）。 */
    private String effectiveMode() {
        if (!denyPrivateNetwork) {
            return "off";
        }
        return outboundMode == null || outboundMode.isBlank() ? "deny-private" : outboundMode.toLowerCase();
    }

    /** allowlist 主机归一（小写、去空白、去掉尾部点）。 */
    @PostConstruct
    void initAllowedHosts() {
        allowedHostSet = allowedHosts == null ? Set.of() : allowedHosts.stream()
                .filter(h -> h != null && !h.isBlank())
                .map(h -> h.trim().toLowerCase().replaceAll("\\.$", ""))
                .collect(Collectors.toSet());
        if ("allowlist".equals(effectiveMode()) && allowedHostSet.isEmpty()) {
            logger.warn("plugin.http.mode=allowlist but plugin.http.allowed-hosts is empty: "
                    + "all plugin http calls will be blocked");
        }
    }

    /** 主机是否被 allowlist 放行（精确或 *.example.com 通配）。 */
    private boolean hostAllowed(String host) {
        if (host == null) {
            return false;
        }
        String normalized = host.toLowerCase().replaceAll("\\.$", "");
        if (allowedHostSet.contains(normalized)) {
            return true;
        }
        for (String allowed : allowedHostSet) {
            if (allowed.startsWith("*.") && normalized.endsWith(allowed.substring(1))) {
                return true;
            }
        }
        return false;
    }

    /** 拦截计数（EngineMetrics 体系，plugin 标签防基数爆炸走注册表 key）。 */
    void recordBlocked(String pluginKey, String url) {
        Counter.builder("radar_plugin_http_blocked_total")
                .tag("plugin", pluginKey == null || pluginKey.isEmpty() ? "unknown" : pluginKey)
                .description("插件 HTTP 出访拦截计数")
                .register(meterRegistry)
                .increment();
        logger.warn("plugin http blocked by {} policy: {}", effectiveMode(), url);
    }

    /** Caffeine 适配（实现 api 的 KeyValueCache）。 */
    static final class PluginCaffeineCache implements KeyValueCache {

        private final Duration ttl;
        private final Cache<Object, Object> cache;

        PluginCaffeineCache(Duration ttl, Cache<Object, Object> cache) {
            this.ttl = ttl;
            this.cache = cache;
        }

        boolean ttlEquals(Duration other) {
            return this.ttl.equals(other);
        }

        @Override
        public <K, V> V get(K key, Function<? super K, ? extends V> loader) {
            return (V) cache.get(key, k -> loader.apply((K) k));
        }
    }

    /** JDK HttpClient 实现（强制超时 + 出访管控三模式），实现 api 的 HttpTool。 */
    final class GuardedHttpTool implements HttpTool {

        private final String pluginKey;

        GuardedHttpTool(String pluginKey) {
            this.pluginKey = pluginKey;
        }

        @Override
        public HttpResult get(String url) {
            return exchange(buildRequest(url, HttpRequest.newBuilder().GET()));
        }

        @Override
        public HttpResult postJson(String url, String jsonBody) {
            HttpRequest request = buildRequest(url, HttpRequest.newBuilder()
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody == null ? "" : jsonBody)));
            return exchange(request);
        }

        @Override
        public boolean isAllowed(String url) {
            String mode = effectiveMode();
            if ("off".equals(mode)) {
                return true;
            }
            if ("allowlist".equals(mode)) {
                return hostAllowed(URI.create(url).getHost());
            }
            return !denied(url);
        }

        private HttpRequest buildRequest(String url, HttpRequest.Builder builder) {
            Objects.requireNonNull(url, "url");
            return builder
                    .uri(URI.create(url))
                    .timeout(Duration.ofMillis(Math.max(100, requestTimeoutMs)))
                    .build();
        }

        private HttpResult exchange(HttpRequest request) {
            String url = request.uri().toString();
            String mode = effectiveMode();
            if ("allowlist".equals(mode)) {
                String host = request.uri().getHost();
                if (!hostAllowed(host)) {
                    recordBlocked(pluginKey, url);
                    return new HttpResult(0, "blocked by allowlist policy: " + host);
                }
                // allowlist 与私网防护叠加：目标须显式列入白名单（内网服务也不例外）
            } else if ("deny-private".equals(mode) && denied(url)) {
                recordBlocked(pluginKey, url);
                return new HttpResult(0, "blocked by private-network policy: " + url);
            }
            HttpClient client = PluginContextFactory.this.httpClient;
            if (client == null) {
                synchronized (PluginContextFactory.this) {
                    if (httpClient == null) {
                        httpClient = HttpClient.newBuilder()
                                .connectTimeout(Duration.ofMillis(Math.max(100, connectTimeoutMs)))
                                .proxy(ProxySelector.getDefault())
                                .build();
                    }
                    client = httpClient;
                }
            }
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                return new HttpResult(response.statusCode(), response.body());
            } catch (Exception e) {
                logger.warn("plugin http call failed: {}", request.uri(), e);
                return new HttpResult(0, "http error: " + e.getMessage());
            }
        }

        private boolean denied(String url) {
            try {
                URI uri = URI.create(url);
                String host = uri.getHost();
                if (host == null) {
                    return true;
                }
                InetAddress[] addresses = InetAddress.getAllByName(host);
                for (InetAddress address : addresses) {
                    if (address.isLoopbackAddress() || address.isSiteLocalAddress()
                            || address.isLinkLocalAddress() || address.isAnyLocalAddress()) {
                        return true;
                    }
                }
                return false;
            } catch (Exception e) {
                return true;
            }
        }
    }
}
