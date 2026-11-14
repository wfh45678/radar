package com.pgmmers.radar.service.impl.engine.plugin;

import com.radar.plugin.api.ParamDescriptor;
import com.radar.plugin.api.ParamType;
import com.radar.plugin.api.Plugin;
import com.radar.plugin.api.PluginMeta;
import com.radar.plugin.api.PluginRequest;
import com.radar.plugin.api.PluginResult;
import com.radar.plugin.api.PluginException;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * 字符串截短（按起止下标）。
 *
 * <p>v3 迁移：start/end 进参数 schema；存量配置（args="0,7"）走 legacy 兜底。</p>
 */
@Component
@PluginMeta(key = "SUBSTRING", name = "SUBSTRING", desc = "字符串截短", type = "STRING")
public class SUBSTRING implements Plugin {

    @Override
    public List<ParamDescriptor> paramSchema() {
        return List.of(
                ParamDescriptor.name("start", "起始下标").type(ParamType.INTEGER).required().build(),
                ParamDescriptor.name("end", "结束下标").type(ParamType.INTEGER).required().build());
    }

    @Override
    public PluginResult execute(PluginRequest request, com.radar.plugin.api.PluginContext context)
            throws PluginException {
        int start;
        int end;
        Map<String, Object> args = request.getArgs();
        if (!args.isEmpty()) {
            start = ((Number) args.get("start")).intValue();
            end = ((Number) args.get("end")).intValue();
        } else {
            // 存量兼容：args="start,end"
            String raw = request.getRawArgs();
            if (raw == null || !raw.contains(",")) {
                throw new PluginException("SUBSTRING 参数格式应为 \"start,end\"");
            }
            String[] parts = raw.split(",");
            start = Integer.parseInt(parts[0].trim());
            end = Integer.parseInt(parts[1].trim());
        }
        Object raw = request.getSourceValues().isEmpty() ? null : request.getSourceValues().get(0);
        String field = raw == null ? null : raw.toString();
        if (field != null && field.length() > end) {
            return PluginResult.of(field.substring(start, end));
        }
        return PluginResult.of(field);
    }
}
