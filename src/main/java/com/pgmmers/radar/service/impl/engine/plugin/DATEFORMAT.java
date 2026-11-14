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
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.stereotype.Component;

/**
 * 毫秒时间戳 -> 指定格式字符串。
 *
 * <p>v3 迁移：format 进参数 schema；存量配置（args=格式串）走 legacy 兜底。</p>
 */
@Component
@PluginMeta(key = "DATEFORMAT", name = "DATEFORMAT", desc = "日期时间格式化", type = "STRING")
public class DATEFORMAT implements Plugin {

    @Override
    public List<ParamDescriptor> paramSchema() {
        return List.of(
                ParamDescriptor.name("format", "格式化字符串").type(ParamType.STRING).required()
                        .defaultValue("yyyy-MM-dd HH:mm:ss").build());
    }

    @Override
    public PluginResult execute(PluginRequest request, com.radar.plugin.api.PluginContext context)
            throws PluginException {
        String format;
        Map<String, Object> args = request.getArgs();
        if (!args.isEmpty()) {
            format = String.valueOf(args.get("format"));
        } else {
            // 存量兼容：args 即格式串
            format = request.getRawArgs();
        }
        if (format == null || format.isBlank()) {
            throw new PluginException("DATEFORMAT 缺少格式化字符串");
        }
        Object raw = request.getSourceValues().isEmpty() ? null : request.getSourceValues().get(0);
        long millis = Long.parseLong(raw.toString());
        return PluginResult.of(DateFormatUtils.format(millis, format));
    }
}
