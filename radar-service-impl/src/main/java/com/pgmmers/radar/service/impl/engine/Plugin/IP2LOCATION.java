package com.pgmmers.radar.service.impl.engine.Plugin;

import com.pgmmers.radar.service.engine.PluginServiceV2;
import com.pgmmers.radar.service.engine.vo.Location;
import com.pgmmers.radar.service.impl.util.BeanUtils;
import com.pgmmers.radar.vo.model.PreItemVO;
import java.util.Map;
import org.lionsoul.ip2region.DataBlock;
import org.lionsoul.ip2region.DbConfig;
import org.lionsoul.ip2region.DbSearcher;
import org.lionsoul.ip2region.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class IP2LOCATION implements PluginServiceV2 {

    private static final Logger logger = LoggerFactory.getLogger(IP2LOCATION.class);
    private DbSearcher ipSearcher;

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

    public IP2LOCATION() {
        try {
            String ipFilePath = BeanUtils.getApplicationContext().getEnvironment()
                    .getProperty("ip2region.db.path");
            DbConfig conf = new DbConfig();
            ipSearcher = new DbSearcher(conf, ipFilePath);
            logger.info("IP2LOCATION Plugin load success");
        } catch (Exception e) {
            logger.error("ip2region init failed", e);
        }
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
            DataBlock block = ipSearcher.memorySearch(ip);
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
