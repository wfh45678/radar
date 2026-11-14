package com.pgmmers.radar.service.impl.engine.plugin;

import com.radar.plugin.api.Plugin;
import com.radar.plugin.api.ParamDescriptor;
import com.radar.plugin.api.PluginMeta;
import com.radar.plugin.api.PluginRequest;
import com.radar.plugin.api.PluginResult;
import java.util.Calendar;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * 时间戳 -> 敏感时段（0-23 小时）。
 */
@Component
@PluginMeta(key = "SENSITIVE_TIME", name = "SENSITIVE_TIME", desc = "敏感时间段(小时)", type = "STRING")
public class SENSITIVE_TIME implements Plugin {

    @Override
    public List<ParamDescriptor> paramSchema() {
        return List.of();
    }

    @Override
    public PluginResult execute(PluginRequest request, com.radar.plugin.api.PluginContext context) {
        Object raw = request.getSourceValues().isEmpty() ? null : request.getSourceValues().get(0);
        long millis = Long.parseLong(raw.toString());
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        return PluginResult.of(c.get(Calendar.HOUR_OF_DAY) + "");
    }
}
