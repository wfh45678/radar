package com.radar.plugin.testkit;

import com.radar.plugin.api.PluginRequest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试用 {@link PluginRequest} 构造器：链式设置源字段/参数/事件，替代引擎构造。
 *
 * <pre>{@code
 * PluginRequest request = PluginRequestBuilder.create()
 *         .modelId(99L)
 *         .destField("ipInfo")
 *         .sourceField("userIP", "1.2.3.4")
 *         .arg("mode", "full")
 *         .event("mobile", "13800000000")
 *         .build();
 * }</pre>
 *
 * @since 3.1.0
 */
public final class PluginRequestBuilder {

    private Long modelId;
    private String destField = "testDest";
    private final Map<String, Object> sourceFields = new LinkedHashMap<>();
    private final List<Object> sourceValues = new ArrayList<>();
    private final Map<String, Object> args = new LinkedHashMap<>();
    private String rawArgs;
    private final Map<String, Object> event = new LinkedHashMap<>();

    private PluginRequestBuilder() {
    }

    /** 创建构建器。 */
    public static PluginRequestBuilder create() {
        return new PluginRequestBuilder();
    }

    /** 模型 id。 */
    public PluginRequestBuilder modelId(Long modelId) {
        this.modelId = modelId;
        return this;
    }

    /** 结果写回字段名（缺省 testDest）。 */
    public PluginRequestBuilder destField(String destField) {
        if (destField != null && !destField.isEmpty()) {
            this.destField = destField;
        }
        return this;
    }

    /** 声明源字段与取值（同时进入 sourceFields/sourceValues/event）。 */
    public PluginRequestBuilder sourceField(String name, Object value) {
        sourceFields.put(name, value);
        sourceValues.add(value);
        event.putIfAbsent(name, value);
        return this;
    }

    /** 类型化参数（引擎已按 schema 解析后的形态）。 */
    public PluginRequestBuilder arg(String name, Object value) {
        args.put(name, value);
        return this;
    }

    /** 批量声明类型化参数。 */
    public PluginRequestBuilder args(Map<String, Object> args) {
        if (args != null) {
            this.args.putAll(args);
        }
        return this;
    }

    /** 原始 args 字符串（存量自由格式兜底场景）。 */
    public PluginRequestBuilder rawArgs(String rawArgs) {
        this.rawArgs = rawArgs;
        return this;
    }

    /** 事件上下文（sourceField 之外插件还想读的字段）。 */
    public PluginRequestBuilder event(String name, Object value) {
        event.put(name, value);
        return this;
    }

    /** 构造只读 PluginRequest（config 载体对 v3 插件无意义，恒为 null）。 */
    public PluginRequest build() {
        return new PluginRequest(modelId, destField,
                new LinkedHashMap<>(sourceFields), new ArrayList<>(sourceValues),
                new LinkedHashMap<>(args), rawArgs, new LinkedHashMap<>(event), null);
    }
}
