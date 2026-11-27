package com.pgmmers.radar.service.impl.model.transfer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 在线模板源（v3.4）：官方地址 www.riskengine.cn（内置默认；裸域 riskengine.cn 未配解析，
 * 2026-11-27 实测后改用 www），`sys.conf.template-source-url`
 * 可覆盖、置空禁用。纯静态协议——catalog.json + 包体 json 两个 GET，包体地址相对 catalog URL 解析。
 *
 * <p>失败路径三保险：3s 超时、60s 负缓存（防重试风暴）、任一失败降级为仅内置并带状态标记。
 * 拉取只发生在打开模板库时，永不阻塞应用启动与核心链路。</p>
 *
 * @since 3.4.0
 */
@Service
public class TemplateSourceService {

    private static final Logger logger = LoggerFactory.getLogger(TemplateSourceService.class);

    /** 在线源官方地址；置空禁用在线源（纯内置模式）。 */
    @Value("${sys.conf.template-source-url:https://www.riskengine.cn/radar/templates/catalog.json}")
    private String sourceUrl;

    /** catalog/包体拉取超时。 */
    @Value("${sys.conf.template-source-timeout-ms:3000}")
    private long timeoutMs;

    /** 负缓存：一次失败后的静默期（防重试风暴）。 */
    @Value("${sys.conf.template-source-negative-cache-ms:60000}")
    private long negativeCacheMs;

    /** 在线源状态（模板库接口返回给前端展示）。 */
    public enum Status {
        /** 已配置且最近一次拉取成功。 */
        CONNECTED,
        /** 已配置但最近一次拉取失败（降级仅内置）。 */
        UNAVAILABLE,
        /** 未配置（纯内置模式）。 */
        DISABLED
    }

    private final ObjectMapper objectMapper = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    /** 最近一次成功拉取的远程条目（key -> info + 包体 URL）。 */
    private volatile Map<String, RemoteTemplate> remoteTemplates = Map.of();
    private volatile Status status = Status.DISABLED;
    private volatile long lastAttemptAt = 0;

    public static class RemoteTemplate {
        TemplateInfo info;
        String bodyUrl;
    }

    public boolean isEnabled() {
        return sourceUrl != null && !sourceUrl.isBlank();
    }

    public Status getStatus() {
        return isEnabled() ? status : Status.DISABLED;
    }

    /**
     * 拉取远程模板条目（60s 缓存/负缓存窗口内直接返回缓存结果）。
     * 失败返回空列表并把状态置为 UNAVAILABLE。
     */
    public synchronized List<TemplateInfo> fetchRemote() {
        if (!isEnabled()) {
            status = Status.DISABLED;
            return List.of();
        }
        long now = System.currentTimeMillis();
        if (now - lastAttemptAt < negativeCacheMs) {
            // 缓存窗口内：返回上次结果（成功缓存或负缓存的空列表）
            return List.copyOf(remoteTemplates.values()).stream().map(this::toInfo).toList();
        }
        lastAttemptAt = now;
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(sourceUrl))
                    .timeout(Duration.ofMillis(Math.max(500, timeoutMs)))
                    .GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalStateException("HTTP " + response.statusCode());
            }
            Map<String, RemoteTemplate> fetched = parseCatalog(response.body(), sourceUrl);
            remoteTemplates = fetched;
            status = Status.CONNECTED;
            logger.info("template source fetched: {} remote templates from {}", fetched.size(), sourceUrl);
        } catch (Exception e) {
            remoteTemplates = Map.of();
            status = Status.UNAVAILABLE;
            logger.warn("template source unavailable (degrade to builtin only): {} - {}", sourceUrl, e.getMessage());
        }
        return List.copyOf(remoteTemplates.values()).stream().map(this::toInfo).toList();
    }

    /** 懒拉取远程包体（按条目记录的相对地址解析）。 */
    public byte[] fetchBody(RemoteTemplate remote) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(remote.bodyUrl))
                .timeout(Duration.ofMillis(Math.max(500, timeoutMs)))
                .GET().build();
        try {
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200) {
                throw new IllegalStateException("HTTP " + response.statusCode());
            }
            return response.body();
        } catch (Exception e) {
            throw new IllegalStateException("模板包体拉取失败: " + remote.bodyUrl + " - " + e.getMessage(), e);
        }
    }

    /** 解析远程 catalog：key 条目 + 包体相对地址解析。 */
    private Map<String, RemoteTemplate> parseCatalog(String body, String catalogUrl) throws Exception {
        JsonNode root = objectMapper.readTree(body);
        JsonNode templates = root.get("templates");
        if (templates == null || !templates.isArray()) {
            throw new IllegalStateException("catalog 缺少 templates 数组");
        }
        URI base = URI.create(catalogUrl);
        Map<String, RemoteTemplate> out = new LinkedHashMap<>();
        for (JsonNode t : templates) {
            String key = t.path("key").asText("");
            String file = t.path("file").asText("");
            if (key.isEmpty() || file.isEmpty()) {
                continue;
            }
            RemoteTemplate rt = new RemoteTemplate();
            rt.info = new TemplateInfo();
            rt.info.setKey(key);
            rt.info.setName(t.path("name").asText(key));
            rt.info.setDescription(t.path("description").asText(""));
            rt.info.setMinRadarVersion(t.path("minRadarVersion").asText(""));
            rt.info.setTags(objectMapper.convertValue(t.path("tags"),
                    objectMapper.getTypeFactory().constructParametricType(List.class, String.class)));
            rt.info.setSource(TemplateInfo.Source.REMOTE);
            rt.bodyUrl = base.resolve(file).toString();
            out.put(key, rt);
        }
        return out;
    }

    private TemplateInfo toInfo(RemoteTemplate rt) {
        // info.stats 由合并方填充
        TemplateInfo info = new TemplateInfo();
        info.setKey(rt.info.getKey());
        info.setName(rt.info.getName());
        info.setDescription(rt.info.getDescription());
        info.setTags(rt.info.getTags());
        info.setMinRadarVersion(rt.info.getMinRadarVersion());
        info.setSource(TemplateInfo.Source.REMOTE);
        List<String> tags = info.getTags();
        info.setTags(tags == null ? List.of() : List.copyOf(tags));
        info.setStats(Map.of());
        return info;
    }

    /** 供合并方按 key 取远程包体（含状态刷新：负缓存过期后自动重拉）。 */
    public byte[] bodyByKey(String key, TemplateInfo.Source source) {
        if (source == TemplateInfo.Source.BUILTIN) {
            return null;
        }
        fetchRemote();
        RemoteTemplate rt = remoteTemplates.get(key);
        if (rt == null) {
            throw new IllegalStateException("在线源中不存在模板: " + key);
        }
        return fetchBody(rt);
    }
}
