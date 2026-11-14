package com.pgmmers.radar.service.impl.engine.plugin;

import com.pgmmers.radar.model.vo.data.MobileInfoVO;
import com.pgmmers.radar.service.data.MobileInfoService;
import com.radar.plugin.api.OutputColumn;
import com.radar.plugin.api.Plugin;
import com.radar.plugin.api.ParamDescriptor;
import com.radar.plugin.api.PluginMeta;
import com.radar.plugin.api.PluginRequest;
import com.radar.plugin.api.PluginResult;
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
@PluginMeta(key = "MOBILE2LOCATION", name = "MOBILE2LOCATION", desc = "手机号码归属地", type = "")
public class MOBILE2LOCATION implements Plugin {

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
    public PluginResult execute(PluginRequest request, com.radar.plugin.api.PluginContext context) {
        Object raw = request.getSourceValues().isEmpty() ? null : request.getSourceValues().get(0);
        if (raw == null) {
            return PluginResult.of(null);
        }
        String mobile = raw.toString();
        if (!StringUtils.isEmpty(mobile) && mobile.length() == 11) {
            mobile = mobile.substring(0, 7);
        }
        MobileInfoVO vo = mobileInfoService.getMobileInfoByMobile(mobile);
        if (vo == null) {
            return PluginResult.of(null);
        }
        Map<String, Object> location = new LinkedHashMap<>();
        location.put("country", "中国");
        location.put("province", vo.getProvince());
        location.put("city", vo.getCity());
        return PluginResult.of(location);
    }
}
