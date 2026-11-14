package com.pgmmers.radar.service.impl.engine.plugin;

import com.radar.plugin.api.Plugin;
import com.radar.plugin.api.ParamDescriptor;
import com.radar.plugin.api.PluginMeta;
import com.radar.plugin.api.PluginRequest;
import com.radar.plugin.api.PluginResult;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * 多字段按配置顺序以 "_" 拼接。
 *
 * <p>v3 迁移：无参数，源字段顺序语义由 {@link PluginRequest#getSourceValues()} 保序承载。</p>
 */
@Component
@PluginMeta(key = "ALLINONE", name = "ALLINONE", desc = "字段合并", type = "STRING")
public class ALLINONE implements Plugin {

    @Override
    public List<ParamDescriptor> paramSchema() {
        return List.of();
    }

    @Override
    public PluginResult execute(PluginRequest request, com.radar.plugin.api.PluginContext context) {
        String str = request.getSourceValues().stream()
                .map(v -> v == null ? "" : v.toString())
                .collect(Collectors.joining("_"));
        return PluginResult.of(str);
    }
}
