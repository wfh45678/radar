package com.pgmmers.radar.service.impl.model.transfer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pgmmers.radar.service.impl.model.transfer.ModelPackageService.ModelPackageException;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

/**
 * 模板目录服务（v3.4）：启动扫描内置模板（resources/models/），
 * 合并在线源（远程同名 key 覆盖内置），提供模板库列表与按 key 导入。
 *
 * <p>统计以包体解析为准（catalog 仅存展示元数据）；单文件损坏只告警不阻断启动。
 * 导入复用 {@link ModelPackageService} 的 validate + importPackage——模板导入
 * 就是特定来源的普通导入，零新导入逻辑。</p>
 *
 * @since 3.4.0
 */
@Service
public class TemplateCatalogService {

    private static final Logger logger = LoggerFactory.getLogger(TemplateCatalogService.class);

    /** 内置模板目录资源路径。 */
    public static final String CATALOG_RESOURCE = "models/catalog.json";

    /** 在线源状态（透传给前端展示）。 */
    public enum SourceStatus {
        CONNECTED, UNAVAILABLE, DISABLED
    }

    @Autowired
    private ModelPackageService modelPackageService;
    @Autowired
    private TemplateSourceService templateSourceService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    /** key -> 内置条目（info + 包体字节）。 */
    private final Map<String, BuiltinTemplate> builtins = new LinkedHashMap<>();

    public static class BuiltinTemplate {
        TemplateInfo info;
        byte[] body;
    }

    /** 启动扫描：catalog + 各包体 parse 预校验；损坏只告警不阻断。 */
    @PostConstruct
    public void loadBuiltins() {
        try {
            byte[] catalogBody = readResource(CATALOG_RESOURCE);
            JsonNode root = objectMapper.readTree(catalogBody);
            JsonNode templates = root.get("templates");
            if (templates == null || !templates.isArray()) {
                logger.warn("builtin template catalog missing templates array: {}", CATALOG_RESOURCE);
                return;
            }
            for (JsonNode t : templates) {
                String key = t.path("key").asText("");
                String file = t.path("file").asText("");
                if (key.isEmpty() || file.isEmpty()) {
                    continue;
                }
                try {
                    byte[] body = readResource("models/" + file);
                    ModelPackage pkg = modelPackageService.parse(body);
                    BuiltinTemplate bt = new BuiltinTemplate();
                    bt.body = body;
                    bt.info = new TemplateInfo();
                    bt.info.setKey(key);
                    bt.info.setName(t.path("name").asText(pkg.getModel().getLabel()));
                    bt.info.setDescription(t.path("description").asText(""));
                    bt.info.setMinRadarVersion(t.path("minRadarVersion").asText(""));
                    bt.info.setTags(readTags(t));
                    bt.info.setSource(TemplateInfo.Source.BUILTIN);
                    bt.info.setStats(statsOf(pkg));
                    builtins.put(key, bt);
                } catch (Exception e) {
                    // 单文件损坏：跳过并告警，不阻断启动
                    logger.warn("builtin template skipped (corrupt): {} - {}", file, e.getMessage());
                }
            }
            logger.info("builtin template catalog loaded: {} templates", builtins.size());
        } catch (Exception e) {
            logger.warn("builtin template catalog load failed (templates disabled): {}", e.getMessage());
        }
    }

    /** 模板库列表：内置 + 在线合并（远程同名 key 覆盖内置），附源状态。 */
    public Map<String, Object> library() {
        List<TemplateInfo> merged = new ArrayList<>();
        Map<String, TemplateInfo> mergedMap = new LinkedHashMap<>();
        for (BuiltinTemplate bt : builtins.values()) {
            mergedMap.put(bt.info.getKey(), bt.info);
        }
        boolean sourceConfigured = templateSourceService.isEnabled();
        if (sourceConfigured) {
            for (TemplateInfo remote : templateSourceService.fetchRemote()) {
                // stats 已随 catalog 条目解析（TemplateSourceService.toInfo），不再覆盖
                mergedMap.put(remote.getKey(), remote);
            }
        }
        merged.addAll(mergedMap.values());
        TemplateSourceService.Status status = templateSourceService.getStatus();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("templates", merged);
        result.put("sourceStatus", status == TemplateSourceService.Status.CONNECTED
                ? SourceStatus.CONNECTED.name()
                : (status == TemplateSourceService.Status.UNAVAILABLE
                        ? SourceStatus.UNAVAILABLE.name() : SourceStatus.DISABLED.name()));
        return result;
    }

    /** 按 key 加载包体（合并语义：远程覆盖内置）。 */
    public byte[] bodyByKey(String key) {
        BuiltinTemplate bt = builtins.get(key);
        TemplateInfo.Source source = TemplateInfo.Source.BUILTIN;
        if (bt == null && templateSourceService.isEnabled()) {
            source = TemplateInfo.Source.REMOTE;
        }
        if (source == TemplateInfo.Source.BUILTIN && bt == null) {
            // 远程可能新增了内置没有的 key：确认远程
            for (TemplateInfo remote : templateSourceService.fetchRemote()) {
                if (remote.getKey().equals(key)) {
                    source = TemplateInfo.Source.REMOTE;
                    break;
                }
            }
            if (source == TemplateInfo.Source.BUILTIN) {
                throw new ModelPackageException("模板不存在: " + key);
            }
        }
        byte[] body = templateSourceService.bodyByKey(key, source);
        if (source == TemplateInfo.Source.BUILTIN) {
            body = bt.body;
        }
        return body;
    }

    /** 按 key 导入模板：加载 → validate → importPackage（复用普通导入全链路）。 */
    public Long importTemplate(String key, String merchantCode, String modelName, String label) {
        byte[] body = bodyByKey(key);
        ModelPackage pkg = modelPackageService.parse(body);
        if (label == null || label.isBlank()) {
            label = pkg.getModel().getLabel();
        }
        if (modelName == null || modelName.isBlank()) {
            modelName = pkg.getModel().getModelName();
        }
        List<String> warnings = modelPackageService.validate(pkg);
        Long newId = modelPackageService.importPackage(pkg, merchantCode, modelName, label);
        logger.info("template imported: key={}, new modelId={}, warnings={}", key, newId, warnings.size());
        return newId;
    }

    // ---------- 内部 ----------

    private byte[] readResource(String path) throws Exception {
        ClassPathResource resource = new ClassPathResource(path);
        try (var in = resource.getInputStream()) {
            return in.readAllBytes();
        }
    }

    private List<String> readTags(JsonNode t) {
        String[] tags = objectMapper.convertValue(t.path("tags"), String[].class);
        return tags == null ? List.of() : List.of(tags);
    }

    private Map<String, Object> statsOf(ModelPackage pkg) {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("rules", pkg.getRules().size());
        stats.put("features", pkg.getFeatures().size());
        stats.put("fields", pkg.getFields().size());
        return stats;
    }

    /** 远程条目的统计需拉包体解析——列表阶段以远程 catalog 未含统计为常态，置空即可（前端不强依赖）。 */
}
