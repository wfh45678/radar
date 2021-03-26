package com.pgmmers.radar.service.impl.engine.plugin;

import com.pgmmers.radar.service.engine.PluginServiceV2;
import com.pgmmers.radar.vo.model.PreItemVO;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * author: wangcheng Date: 2020/5/19 Time: 上午11:45 Description:
 */
public class ALLINONE implements PluginServiceV2 {

    @Override
    public Integer key() {
        return 3;
    }

    @Override
    public String desc() {
        return "字段合并";
    }

    @Override
    public String getType() {
        return "STRING";
    }

    @Override
    public Object handle(PreItemVO item, Map<String, Object> jsonInfo,
                         String[] sourceField) {
        if (sourceField == null || sourceField.length == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (String field : sourceField) {
            Object f = jsonInfo.get(field);
            builder.append(f == null ? "" : f.toString());
            builder.append("_");
        }
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }
    
}
