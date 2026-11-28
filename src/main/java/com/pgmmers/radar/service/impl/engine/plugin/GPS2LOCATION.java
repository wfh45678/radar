package com.pgmmers.radar.service.impl.engine.plugin;

import com.alibaba.fastjson2.JSONObject;
import com.radar.plugin.api.HttpResult;
import com.radar.plugin.api.OutputColumn;
import com.radar.plugin.api.FeatureProcessor;
import com.radar.plugin.api.ParamDescriptor;
import com.radar.plugin.api.FeatureMeta;
import com.radar.plugin.api.FeatureRequest;
import com.radar.plugin.api.FeatureResponse;
import jakarta.annotation.PostConstruct;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * GPS 经纬度 -> 地址（外部 HTTP 服务）。
 *
 * <p>v3 迁移：服务地址从硬编码移入配置（默认值保留兼容），改走引擎 HTTP 工具
 * （强制超时 + 私网防护 + 连接复用），并声明 remote 型。</p>
 */
@Component
@FeatureMeta(key = "GPS2LOCATION", name = "GPS2LOCATION", desc = "GPS转换成地址", type = "", remote = true)
public class GPS2LOCATION implements FeatureProcessor {

    private static final Logger logger = LoggerFactory.getLogger(GPS2LOCATION.class);

    /** 参考 http://jwd.funnyapi.com/#/index ，公共免费服务不可靠，生产建议自建本地库。 */
    @Value("${plugin.gps.url:http://106.75.35.67:60000/gis}")
    private String gpsUrl;

    @PostConstruct
    public void warn() {
        logger.warn("GPS2LOCATION uses external free service ({}); set plugin.gps.url to your own service for production", gpsUrl);
    }

    @Override
    public List<ParamDescriptor> paramSchema() {
        return List.of();
    }

    @Override
    public List<OutputColumn> outputMeta() {
        return List.of(
                OutputColumn.of("country", "国家"),
                OutputColumn.of("province", "省份"),
                OutputColumn.of("city", "城市"));
    }

    @Override
    public FeatureResponse execute(FeatureRequest request, com.radar.plugin.api.FeatureContext context) {
        List<Object> values = request.getSourceValues();
        if (values.size() < 2 || values.get(0) == null || values.get(1) == null) {
            return FeatureResponse.of(null);
        }
        // 纬度 / 经度
        String latitude = values.get(0).toString();
        String longitude = values.get(1).toString();
        if (latitude.isEmpty() || longitude.isEmpty()) {
            return FeatureResponse.of(null);
        }
        Map<String, Object> location = new LinkedHashMap<>();
        try {
            String url = gpsUrl + "?auth_user=freevip&latitude=" + latitude + "&longitude=" + longitude;
            HttpResult response = context.http().get(url);
            if (response.isOk()) {
                JSONObject body = JSONObject.parseObject(response.getBody());
                String data = body.getString("data");
                Map<String, Object> detail = JSONObject.parseObject(data);
                location.put("country", String.valueOf(detail.get("zh0")));
                location.put("region", String.valueOf(detail.get("zh3")));
                location.put("province", String.valueOf(detail.get("zh1")));
                location.put("city", String.valueOf(detail.get("zh2")));
            }
        } catch (Exception e) {
            logger.error("GPS2LOCATION error", e);
        }
        return FeatureResponse.of(location);
    }
}
