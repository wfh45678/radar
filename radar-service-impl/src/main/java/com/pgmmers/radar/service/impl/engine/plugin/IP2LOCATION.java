package com.pgmmers.radar.service.impl.engine.plugin;

import com.pgmmers.radar.service.engine.PluginServiceV2;
import com.pgmmers.radar.service.engine.vo.Location;
import com.pgmmers.radar.service.impl.util.BeanUtils;
import com.pgmmers.radar.vo.model.PreItemVO;
import org.lionsoul.ip2region.DataBlock;
import org.lionsoul.ip2region.DbConfig;
import org.lionsoul.ip2region.DbSearcher;
import org.lionsoul.ip2region.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

@Component
public class IP2LOCATION implements PluginServiceV2 {

    private static final Logger logger = LoggerFactory.getLogger(IP2LOCATION.class);

    @Override
    public Integer key() {
        return 1;
    }

    @Override
    public String desc() {
        return "IP转换成地址";
    }

    @Override
    public String getType() {
        return null;
    }

    @Override
    public String getMeta() {
        return "[{\"column\":\"country\", \"title\":\"国家\", \"type\":\"STRING\"},{\"column\":\"province\", \"title\":\"省份\", \"type\":\"STRING\"},{\"column\":\"city\", \"title\":\"城市\", \"type\":\"STRING\"}]";
    }

    static DbSearcher getDbSearcher() {
        final DbSearcher ipSearcher;
        try {
            String ipFilePath = BeanUtils.getApplicationContext().getEnvironment()
                    .getProperty("ip2region.db.path");
            DbConfig conf = new DbConfig();

            ipSearcher = new DbSearcher(conf, ipFilePath);
            logger.info("IP2LOCATION Plugin load success");
            return ipSearcher;
        } catch (Exception e) {
            logger.error("ip2region init failed", e);
        }
        return null;
    }

    @Override
    public Object handle(PreItemVO item, Map<String, Object> jsonInfo,
            String[] sourceField) {
        Location location;
        String ip = jsonInfo.get(sourceField[0]).toString();
        if (!Util.isIpAddress(ip)) {
            return null;
        }
        try {
            DataBlock block = Objects.requireNonNull(IP2LOCATION.getDbSearcher()).memorySearch(ip);
            String[] detail = block.getRegion().split("\\|");
            location = new Location();
            location.setCountry(detail[0]);
            location.setRegion(detail[1]);
            location.setProvince(detail[2]);
            location.setCity(detail[3]);
            location.setAddress(detail[4]);
        } catch (Exception e) {
            location = new Location();
            logger.error("ip2region error", e);
        }
        return location;
    }
}
