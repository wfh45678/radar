package com.pgmmers.radar.service.impl.engine.plugin;

import com.radar.plugin.api.OutputColumn;
import com.radar.plugin.api.Plugin;
import com.radar.plugin.api.ParamDescriptor;
import com.radar.plugin.api.PluginMeta;
import com.radar.plugin.api.PluginRequest;
import com.radar.plugin.api.PluginResult;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.lionsoul.ip2region.DataBlock;
import org.lionsoul.ip2region.DbConfig;
import org.lionsoul.ip2region.DbSearcher;
import org.lionsoul.ip2region.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * IP -> 国家/省/市（本地 ip2region 数据文件）。
 *
 * <p>v3 迁移：searcher 启动加载一次并复用（旧实现每次请求重建读盘），结果接入插件缓存。</p>
 */
@Component
@PluginMeta(key = "IP2LOCATION", name = "IP2LOCATION", desc = "IP转换成地址", type = "")
public class IP2LOCATION implements Plugin {

    private static final Logger logger = LoggerFactory.getLogger(IP2LOCATION.class);

    @Value("${ip2region.db.path}")
    private String ipFilePath;

    private volatile DbSearcher searcher;

    @PostConstruct
    public void init() {
        try {
            searcher = new DbSearcher(new DbConfig(), ipFilePath);
            logger.info("IP2LOCATION Plugin load success");
        } catch (Exception e) {
            logger.error("ip2region init failed", e);
        }
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
    public PluginResult execute(PluginRequest request, com.radar.plugin.api.PluginContext context) {
        Object raw = request.getSourceValues().isEmpty() ? null : request.getSourceValues().get(0);
        if (raw == null) {
            return PluginResult.of(null);
        }
        String ip = raw.toString();
        if (!Util.isIpAddress(ip)) {
            return PluginResult.of(null);
        }
        DbSearcher dbSearcher = this.searcher;
        if (dbSearcher == null) {
            return PluginResult.of(null);
        }
        Map<String, Object> location = context.cache("ip2location", Duration.ofHours(1))
                .get(ip, key -> {
                    try {
                        DataBlock block = Objects.requireNonNull(dbSearcher).memorySearch(key);
                        String[] detail = block.getRegion().split("\\|");
                        Map<String, Object> loc = new LinkedHashMap<>();
                        loc.put("country", detail[0]);
                        loc.put("region", detail[1]);
                        loc.put("province", detail[2]);
                        loc.put("city", detail[3]);
                        loc.put("address", detail[4]);
                        return loc;
                    } catch (Exception e) {
                        logger.error("ip2region error", e);
                        return null;
                    }
                });
        return PluginResult.of(location);
    }
}
