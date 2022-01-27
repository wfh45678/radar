package com.pgmmers.radar.service.impl.engine.plugin;

import com.pgmmers.radar.service.engine.PluginServiceV2;
import com.pgmmers.radar.vo.model.PreItemVO;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * author: wangcheng Date: 2020/5/19 Time: 上午11:45 Description:
 */
@Component
public class SUBSTRING implements PluginServiceV2 {

    @Override
    public Integer key() {
        return 4;
    }

    @Override
    public String desc() {
        return "字符串截短";
    }

    @Override
    public String getType() {
        return "STRING";
    }

    @Override
    public Object handle(PreItemVO item, Map<String, Object> jsonInfo, String[] sourceField) {
        String[] args = item.getArgs().split(",");
        String field = jsonInfo.get(sourceField[0]).toString();
        int start =Integer.parseInt(args[0]);
        int end = Integer.parseInt(args[1]);
        if (field != null && field.length() > end) {
            return field.substring(start, end);
        } else {
            return field;
        }
    }
}
