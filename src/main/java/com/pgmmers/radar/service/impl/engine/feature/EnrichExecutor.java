package com.pgmmers.radar.service.impl.engine.feature;

import com.pgmmers.radar.config.EngineMetrics;
import com.pgmmers.radar.enums.FeatureType;
import com.pgmmers.radar.enums.StatusType;
import com.pgmmers.radar.model.vo.model.FeatureVO;
import com.pgmmers.radar.service.engine.feature.FeatureExecutor;
import com.pgmmers.radar.service.engine.feature.FeatureOutcome;
import com.pgmmers.radar.service.impl.engine.plugin.PluginEntry;
import com.pgmmers.radar.service.impl.engine.plugin.PluginManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 插件加工特征执行器（v4.1，原 v3.x PreItem prepare 逻辑）：
 * 按插件 key 调用 PluginManager，产出写入 features.&lt;featureName&gt;（多列插件为嵌套 Map）。
 * 支持链式：definition.sourceFields 可引用前序特征（同阶段按 ORDER_NO 顺序执行）。
 * 失败隔离：isolated（默认，错误标记 {@code __plugin_error__} 后链路继续）/
 * strict（旧行为，失败即事件失败，异常上抛）。
 */
@Component
public class EnrichExecutor implements FeatureExecutor {

    private static final Logger logger = LoggerFactory.getLogger(EnrichExecutor.class);

    private static final String STRICT = "strict";

    @Value("${plugin.isolation:isolated}")
    private String isolation;

    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private EngineMetrics engineMetrics;

    @Override
    public FeatureType type() {
        return FeatureType.ENRICH;
    }

    @Override
    public FeatureOutcome execute(FeatureVO feature, Map<String, Map<String, ?>> data, Map<String, ?> lists) {
        String pluginKey = pluginKeyOf(feature);
        String[] sourceField = sourceFieldsOf(feature);
        PluginEntry entry = pluginManager.entry(pluginKey);
        long start = System.currentTimeMillis();
        Object transfer = null;
        String failReason = null;
        if (entry == null) {
            failReason = "plugin not found: " + pluginKey;
            logger.warn("enrich feature plugin missing, scenarioId={}, feature={}, {}",
                    feature.getScenarioId(), feature.getFeatureName(), failReason);
        } else {
            try {
                // v4.1：插件取值源为整体上下文（事件字段经 request.getEvent()，链式读前序特征经 request.getFeatures()）
                transfer = entry.execute(feature, data, sourceField);
            } catch (Throwable t) {
                failReason = t.getMessage() == null ? t.getClass().getSimpleName() : t.getMessage();
                logger.warn("enrich feature plugin failed: {}, feature={}, reason={}",
                        pluginKey, feature.getFeatureName(), failReason);
            }
        }
        long elapsed = System.currentTimeMillis() - start;
        String metricResult = failReason == null ? "success" : (entry == null ? "not_found" : "fail");
        engineMetrics.recordPluginExecution(entry == null ? "unknown" : pluginKey, metricResult, elapsed);
        if (failReason != null) {
            if (STRICT.equals(isolation)) {
                throw new com.pgmmers.radar.service.engine.feature.FeatureIsolationException(
                        "enrich feature plugin fail: " + pluginKey + " - " + failReason);
            }
            Map<String, Object> errorMarker = new HashMap<>();
            errorMarker.put("__plugin_error__", failReason);
            transfer = errorMarker;
        }
        return FeatureOutcome.of(transfer);
    }

    /** ENRICH 定义中的插件 key（definition.plugin）。 */
    private String pluginKeyOf(FeatureVO feature) {
        return feature.getDefinition() == null ? null : feature.getDefinition().path("plugin").asText(null);
    }

    /** ENRICH 定义中的来源字段数组（definition.sourceFields，缺省空数组）。 */
    private String[] sourceFieldsOf(FeatureVO feature) {
        java.util.List<String> fields = new ArrayList<>();
        if (feature.getDefinition() != null && feature.getDefinition().has("sourceFields")
                && feature.getDefinition().get("sourceFields").isArray()) {
            feature.getDefinition().get("sourceFields").forEach(f -> fields.add(f.asText()));
        }
        return fields.toArray(new String[0]);
    }
}
