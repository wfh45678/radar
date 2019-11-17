package com.pgmmers.radar.service.impl.engine;


import com.alibaba.fastjson.JSONObject;
import com.pgmmers.radar.enums.CombineType;
import com.pgmmers.radar.service.data.MobileInfoService;
import com.pgmmers.radar.service.engine.PluginService;
import com.pgmmers.radar.service.engine.vo.Location;
import com.pgmmers.radar.vo.data.MobileInfoVO;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.lionsoul.ip2region.DataBlock;
import org.lionsoul.ip2region.DbConfig;
import org.lionsoul.ip2region.DbSearcher;
import org.lionsoul.ip2region.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.Calendar;
import java.util.List;

@Service
@ConditionalOnProperty(prefix = "sys.conf",name="app", havingValue = "engine")
public class PluginServiceImpl implements PluginService {

    private static Logger logger = LoggerFactory.getLogger(PluginServiceImpl.class);

    private DbSearcher ipSearcher;

    @Value("${ip2region.db.path}")
    private String ipFilePath;

    @Autowired
    private MobileInfoService mobileInfoService;

    @Autowired
    private RestTemplate restTemplate;

    @PostConstruct
    public void init() {
        try {
            DbConfig conf = new DbConfig();
            ipSearcher = new DbSearcher(conf, ipFilePath);
        } catch (Exception e) {
            logger.error("ip2region init fail", e);
        }
    }

    @Override
    public Location ip2location(String ip) {
        Location location = null;
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

    @Override
    public Location gps2location(String lng, String lat) {
        // TODO 可以参考 http://jwd.funnyapi.com/#/index , 最好是本地库。
        return null;
    }

    @Override
    public Object allInOne(List<Object> fields, int combineType) {
        if (combineType == CombineType.CONCAT) {
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

        return null;
    }

    @Override
    public String subString(String field, Integer start, Integer end) {
        if (field != null && field.length() > end) {
            return field.substring(start, end);
        } else {
            return field;
        }
    }

    @Override
    public Location mobile2location(String mobile) {
        if (!StringUtils.isEmpty(mobile) && mobile.length() == 11) {
            mobile = mobile.substring(0, 7);
        }
        MobileInfoVO vo = mobileInfoService.getMobileInfoByMobile(mobile);
        Location location = new Location();
        if (vo != null) {
            location.setProvince(vo.getProvince());
            location.setCity(vo.getCity());
            location.setCountry("中国");
        }
        return location;
    }

    @Override
    public String getSensitiveTime(Long timemills) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timemills);
        return c.get(Calendar.HOUR_OF_DAY) + "";
    }

    @Override
    public String formatDate(Long timemills, String format) {
        String dateStr = DateFormatUtils.format(timemills, format);
        return dateStr;
    }

    @Override
    public JSONObject httpRequest(String url, String reqType, String... args) {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<JSONObject> responseEntity = restTemplate.exchange(url, HttpMethod.valueOf(reqType), entity, JSONObject.class, args);
        logger.info("http plugin:{}\n{}\n {}", url, args, responseEntity.toString());
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            return responseEntity.getBody();
        }
        return null;
    }

}
