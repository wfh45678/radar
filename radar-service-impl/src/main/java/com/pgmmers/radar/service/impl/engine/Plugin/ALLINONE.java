package com.pgmmers.radar.service.impl.engine.Plugin;

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
        List<Object> fields = new ArrayList<>();
        for (String field : sourceField) {
            fields.add(jsonInfo.get(field));
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < fields.size(); i++) {
            Object f = fields.get(i);
            builder.append(f == null ? "" : f.toString());
            if (i < fields.size() - 1) {
                builder.append("_");
            }
        }
        return builder.toString();
    }
}
