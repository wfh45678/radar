package com.pgmmers.radar.service.impl.engine.plugin;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.radar.plugin.api.HttpResult;
import com.radar.plugin.api.HttpTool;
import com.radar.plugin.api.KeyValueCache;
import com.radar.plugin.api.PluginContext;
import java.net.InetAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 引擎侧 {@link PluginContext} 工厂：为每个插件提供日志/进程内缓存/带防护的 HTTP 工具。
 *
 * @since 3.0.0
 */
@Component
public class PluginContextFactory {

    private static final Logger logger = LoggerFactory.getLogger(PluginContextFactory.class);

    @Value("${plugin.http.connect-timeout-ms:2000}")
    private int connectTimeoutMs;

    @Value("${plugin.http.request-timeout-ms:2000}")
    private int requestTimeoutMs;

    /** 拒绝指向私网/回环/链路本地地址的请求（缓解管理端配置 url 的 SSRF 面）。 */
    @Value("${plugin.http.deny-private-network:true}")
    private boolean denyPrivateNetwork;

    /** @Value 注入晚于字段初始化，HTTP 客户端按首次调用懒构建。 */
    private volatile HttpClient httpClient;

    private final ConcurrentHashMap<String, KeyValueCache> caches = new ConcurrentHashMap<>();

    /** 构建指定插件 key 的运行时上下文。 */
    public PluginContext create(String pluginKey) {
        Logger pluginLogger = LoggerFactory.getLogger("plugin." + pluginKey);
        return new PluginContext(pluginLogger, this::createCache, new GuardedHttpTool());
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

    /** JDK HttpClient 实现（强制超时 + 私网目标防护），实现 api 的 HttpTool。 */
    final class GuardedHttpTool implements HttpTool {

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
            if (denyPrivateNetwork && denied(request.uri().toString())) {
                return new HttpResult(0, "blocked by private-network policy: " + request.uri());
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
