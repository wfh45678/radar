package com.pgmmers.radar.service.impl.engine.plugin;

import com.pgmmers.radar.model.vo.model.PreItemVO;
import com.radar.plugin.api.OutputColumn;
import com.radar.plugin.api.ParamDescriptor;
import java.util.List;
import java.util.Map;

/**
 * 插件注册表统一条目：v3 插件（Spring 内置 / plugins 目录外置）与 v2 旧插件
 * 归一后的执行与元信息视图，引擎执行链只面向本条目。
 *
 * @since 3.0.0
 */
public final class PluginEntry {

    /** 插件来源。 */
    public enum Source {
        /** v2 旧接口插件（Spring 内置，适配保留）。 */
        BUILTIN_V2,
        /** v3 新接口插件（Spring 内置）。 */
        BUILTIN_V3,
        /** v3 新接口插件（plugins/ 目录外置 jar）。 */
        EXTERNAL
    }

    /** 统一执行器签名。 */
    public interface Executor {
        Object execute(PreItemVO item, Map<String, Object> jsonInfo, String[] sourceField) throws Exception;
    }

    private final String key;
    private final String name;
    private final String desc;
    private final String type;
    private final boolean remote;
    private final Source source;
    private final String version;
    private final String provider;
    private final List<OutputColumn> outputMeta;
    private final List<ParamDescriptor> paramSchema;
    private final Executor executor;

    public PluginEntry(String key, String name, String desc, String type, boolean remote,
                       Source source, List<OutputColumn> outputMeta,
                       List<ParamDescriptor> paramSchema, Executor executor) {
        this(key, name, desc, type, remote, source, "", "", outputMeta, paramSchema, executor);
    }

    /** @since 3.1.0 增 version/provider 元信息。 */
    public PluginEntry(String key, String name, String desc, String type, boolean remote,
                       Source source, String version, String provider,
                       List<OutputColumn> outputMeta,
                       List<ParamDescriptor> paramSchema, Executor executor) {
        this.key = key;
        this.name = name;
        this.desc = desc;
        this.type = type;
        this.remote = remote;
        this.source = source;
        this.version = version == null ? "" : version;
        this.provider = provider == null ? "" : provider;
        this.outputMeta = outputMeta == null ? List.of() : List.copyOf(outputMeta);
        this.paramSchema = paramSchema == null ? List.of() : List.copyOf(paramSchema);
        this.executor = executor;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public String getType() {
        return type;
    }

    /**
     * v2 兼容：输出列元信息转 getMeta() 风格的 JSON 字符串（[{column,title,type}]）。
     * 无输出列声明时返回 null（调用方按"单值"语义处理）。
     */
    public String legacyMetaJson() {
        if (outputMeta.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < outputMeta.size(); i++) {
            OutputColumn col = outputMeta.get(i);
            if (i > 0) {
                sb.append(",");
            }
            sb.append("{\"column\":\"").append(col.getName())
                    .append("\",\"title\":\"").append(col.getTitle())
                    .append("\",\"type\":\"").append(col.getType()).append("\"}");
        }
        return sb.append("]").toString();
    }

    public boolean isRemote() {
        return remote;
    }

    public Source getSource() {
        return source;
    }

    /** @return 插件版本声明（@PluginMeta.version，可为空串）。@since 3.1.0 */
    public String getVersion() {
        return version;
    }

    /** @return 提供方声明（@PluginMeta.provider，可为空串）。@since 3.1.0 */
    public String getProvider() {
        return provider;
    }

    public List<OutputColumn> getOutputMeta() {
        return outputMeta;
    }

    public List<ParamDescriptor> getParamSchema() {
        return paramSchema;
    }

    /** 执行插件（由引擎在隔离包装内调用）。 */
    public Object execute(PreItemVO item, Map<String, Object> jsonInfo, String[] sourceField) throws Exception {
        return executor.execute(item, jsonInfo, sourceField);
    }
}
