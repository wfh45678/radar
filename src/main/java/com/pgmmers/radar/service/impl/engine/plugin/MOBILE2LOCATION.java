package com.pgmmers.radar.service.impl.engine.plugin;

import com.pgmmers.radar.model.vo.data.MobileInfoVO;
import com.pgmmers.radar.service.data.MobileInfoService;
import com.radar.plugin.api.OutputColumn;
import com.radar.plugin.api.FeatureProcessor;
import com.radar.plugin.api.ParamDescriptor;
import com.radar.plugin.api.FeatureMeta;
import com.radar.plugin.api.FeatureRequest;
import com.radar.plugin.api.FeatureResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 手机号（前 7 位）-> 归属地（本地号段库）。
 *
 * <p>v3 迁移：删除旧实现中永不执行的百度 API 回填死代码（失效 key + 明文密钥）。</p>
 */
@Component
@FeatureMeta(key = "MOBILE2LOCATION", name = "MOBILE2LOCATION", desc = "手机号码归属地", type = "")
public class MOBILE2LOCATION implements FeatureProcessor {

    @Autowired
    private MobileInfoService mobileInfoService;

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
        Object raw = request.getSourceValues().isEmpty() ? null : request.getSourceValues().get(0);
        if (raw == null) {
            return FeatureResponse.of(null);
        }
        String mobile = raw.toString();
        if (!StringUtils.isEmpty(mobile) && mobile.length() == 11) {
            mobile = mobile.substring(0, 7);
        }
        MobileInfoVO vo = mobileInfoService.getMobileInfoByMobile(mobile);
        if (vo == null) {
            return FeatureResponse.of(null);
        }
        Map<String, Object> location = new LinkedHashMap<>();
        location.put("country", "中国");
        location.put("province", vo.getProvince());
        location.put("city", vo.getCity());
        return FeatureResponse.of(location);
    }
}
