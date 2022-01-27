package com.pgmmers.radar.service.impl.engine.plugin;

import com.pgmmers.radar.service.engine.PluginServiceV2;
import com.pgmmers.radar.vo.model.PreItemVO;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * author: wangcheng Date: 2020/5/19 Time: 上午11:45 Description:
 */
@Component
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
        String str = Arrays.asList(sourceField).stream()
                .map(f -> jsonInfo.get(f) == null ? "" : f.toString())
                .collect(Collectors.joining("_"));
        return str;
    }
    
}
