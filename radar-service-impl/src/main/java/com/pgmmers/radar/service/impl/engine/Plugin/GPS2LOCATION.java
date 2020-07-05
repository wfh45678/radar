package com.pgmmers.radar.service.impl.engine.Plugin;

import com.pgmmers.radar.service.engine.PluginServiceV2;
import com.pgmmers.radar.vo.model.PreItemVO;
import java.util.Map;

/**
 * author: wangcheng Date: 2020/5/19 Time: 上午11:44 Description:
 */
public class GPS2LOCATION implements PluginServiceV2 {

    @Override
    public Integer key() {
        return 2;
    }

    @Override
    public String desc() {
        return "GPS转换成地址";
    }

    @Override
    public String getType() {
        return null;
    }

    @Override
    public String getMeta() {
        return "[{\"column\":\"country\", \"title\":\"国家\", \"type\":\"STRING\"},{\"column\":\"province\", \"title\":\"省份\", \"type\":\"STRING\"},{\"column\":\"city\", \"title\":\"城市\", \"type\":\"STRING\"}]";
    }

    @Override
    public Object handle(PreItemVO item, Map<String, Object> jsonInfo,
            String[] sourceField) {
        // TODO 可以参考 http://jwd.funnyapi.com/#/index , 最好是本地库。
        return null;
    }
}
