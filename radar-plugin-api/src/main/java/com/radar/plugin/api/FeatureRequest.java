package com.radar.plugin.api;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 插件执行入参（只读视图）。由引擎在每次 preItem 执行时构造。
 *
 * @since 3.0.0
 */
public final class FeatureRequest {

    private final Long modelId;
    private final String destField;
    private final Map<String, Object> sourceFields;
    private final List<Object> sourceValues;
    private final Map<String, Object> args;
    private final String rawArgs;
    private final Map<String, Object> event;
    private final Object config;

    /** 前序特征只读视图（v4.1 链式：features.<name>[.<column>]，ENTRY 阶段按 ORDER_NO 顺序累积）。 */
    private final Map<String, Object> features;

    /**
     * 由引擎构造，插件不应自行实例化。
     *
     * @param modelId       所属模型 id
     * @param destField     本插件结果写回的字段名
     * @param sourceFields  源字段名 -> 当前事件中的取值
     * @param sourceValues  源字段取值（按配置顺序，多输入插件保序用）
     * @param args          已按 paramSchema 解析的类型化参数（schema 为空时为空 Map）
     * @param rawArgs       管理端存储的原始 args 字符串（兼容/逃逸用）
     * @param event         当前事件只读视图
     * @param config        框架内部兼容载体（v2 适配插件用，v3 插件勿依赖）
     */
    public FeatureRequest(Long modelId, String destField,
                         Map<String, Object> sourceFields, List<Object> sourceValues,
                         Map<String, Object> args, String rawArgs,
                         Map<String, Object> event, Object config) {
        this(modelId, destField, sourceFields, sourceValues, args, rawArgs, event, config, java.util.Map.of());
    }

    /**
     * v4.1 构造器：携带前序特征只读视图（链式加工）。
     */
    public FeatureRequest(Long modelId, String destField, Map<String, Object> sourceFields,
                          List<Object> sourceValues, Map<String, Object> args, String rawArgs,
                          Map<String, Object> event, Object config, java.util.Map<String, Object> features) {
        this.modelId = modelId;
        this.destField = destField;
        this.sourceFields = sourceFields == null ? Collections.emptyMap() : Collections.unmodifiableMap(sourceFields);
        this.sourceValues = sourceValues == null ? Collections.emptyList() : Collections.unmodifiableList(sourceValues);
        this.args = args == null ? Collections.emptyMap() : Collections.unmodifiableMap(args);
        this.rawArgs = rawArgs;
        this.event = event == null ? Collections.emptyMap() : Collections.unmodifiableMap(event);
        this.config = config;
        this.features = features == null ? java.util.Map.of() : java.util.Collections.unmodifiableMap(features);
    }

    public Long getModelId() {
        return modelId;
    }

    /** @return 本插件结果写回的事件字段名。 */
    public String getDestField() {
        return destField;
    }

    /** @return 源字段名 -> 事件取值（只读）。 */
    public Map<String, Object> getSourceFields() {
        return sourceFields;
    }

    /** @return 源字段取值，按配置的 sourceField 顺序排列（多输入插件保序用）。 */
    public List<Object> getSourceValues() {
        return sourceValues;
    }

    /** @return 已按 paramSchema 解析的类型化参数（只读；缺失的必填参数引擎已提前拦截）。 */
    public Map<String, Object> getArgs() {
        return args;
    }

    /** @return 管理端存储的原始 args 字符串。 */
    public String getRawArgs() {
        return rawArgs;
    }

    /** @return 当前事件只读视图。 */
    public Map<String, Object> getEvent() {
        return event;
    }

    /** @return 框架内部兼容载体（v2 适配），v3 插件勿依赖。 */
    /** 前序特征只读视图（v4.1 链式）。 */
    public Map<String, Object> getFeatures() {
        return features;
    }

    public Object getConfig() {
        return config;
    }
}
