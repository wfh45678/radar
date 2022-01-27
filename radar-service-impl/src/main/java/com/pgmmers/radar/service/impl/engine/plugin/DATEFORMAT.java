package com.pgmmers.radar.service.impl.engine.plugin;

import com.pgmmers.radar.service.engine.PluginServiceV2;
import com.pgmmers.radar.vo.model.PreItemVO;
import java.util.Map;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.stereotype.Component;

@Component
public class DATEFORMAT implements PluginServiceV2 {

    @Override
    public Integer key() {
        return 7;
    }

    @Override
    public String desc() {
        return "日期时间格式化";
    }

    @Override
    public String getType() {
        return "STRING";
    }

    @Override
    public String getMeta() {
        return null;
    }

    @Override
    public Object handle(PreItemVO item, Map<String, Object> jsonInfo, String[] sourceField) {
        String formatStr = item.getArgs();
        long millis = Long.parseLong(jsonInfo.get(sourceField[0]).toString());
        return DateFormatUtils.format(millis, formatStr);
    }
}
