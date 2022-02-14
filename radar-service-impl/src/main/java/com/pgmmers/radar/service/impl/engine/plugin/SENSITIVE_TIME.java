package com.pgmmers.radar.service.impl.engine.plugin;

import com.pgmmers.radar.service.engine.PluginServiceV2;
import com.pgmmers.radar.vo.model.PreItemVO;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Map;

/**
 * author: wangcheng Date: 2020/5/19 Time: 上午11:46 Description:
 */
@Component
public class SENSITIVE_TIME implements PluginServiceV2 {

    @Override
    public Integer key() {
        return 6;
    }

    @Override
    public String desc() {
        return "敏感时间段(小时)";
    }

    @Override
    public String getType() {
        return "STRING";
    }

    @Override
    public Object handle(PreItemVO item, Map<String, Object> jsonInfo, String[] sourceField) {
        long millis = Long.parseLong(jsonInfo.get(sourceField[0]).toString());
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        return c.get(Calendar.HOUR_OF_DAY) + "";
    }
}

