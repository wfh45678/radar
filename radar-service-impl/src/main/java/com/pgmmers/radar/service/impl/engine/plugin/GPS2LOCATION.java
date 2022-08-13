package com.pgmmers.radar.service.impl.engine.plugin;

import com.alibaba.fastjson.JSONObject;
import com.pgmmers.radar.service.engine.PluginServiceV2;
import com.pgmmers.radar.service.engine.vo.Location;
import com.pgmmers.radar.vo.model.PreItemVO;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;


import java.util.Map;

/**
 * author: wangcheng Date: 2020/5/19 Time: 上午11:44 Description:
 */
@Component
public class GPS2LOCATION implements PluginServiceV2 {

    private static final Logger logger = LoggerFactory.getLogger(GPS2LOCATION.class);
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
        //参考 http://jwd.funnyapi.com/#/index , 最好是本地库。
        Location location = new Location();
        //纬度
        String latitude = String.valueOf(jsonInfo.get(sourceField[0]));
        //经度
        String longitude = String.valueOf(jsonInfo.get(sourceField[1]));
        if ("".equals(latitude) || "".equals(longitude)) {
            return null;
        }
        try {
            String url ="http://106.75.35.67:60000/gis?auth_user=freevip&latitude="+latitude+"&longitude="+longitude;
            //创建httpclient对象
            CloseableHttpClient httpclient = HttpClients.createDefault();
            //创建GET对象
            HttpGet httpget = new HttpGet(url);
            //执行请求
            CloseableHttpResponse response = httpclient.execute(httpget);
            if(response.getStatusLine().getStatusCode()== HttpStatus.OK.value()) {
                HttpEntity entity = response.getEntity();
                //所需内容都在entity里面，这里可以对数据操作
                String detail = EntityUtils.toString(entity,"UTF-8");
                JSONObject parseObject = JSONObject.parseObject(detail);
                //String转map
                String data = parseObject.getString("data");
                Map stringToMap =  JSONObject.parseObject(data);

                location.setCountry(String.valueOf(stringToMap.get("zh0")));
                location.setRegion(String.valueOf(stringToMap.get("zh3")));
                location.setProvince(String.valueOf(stringToMap.get("zh1")));
                location.setCity(String.valueOf(stringToMap.get("zh2")));

            }
            response.close();
            httpclient.close();

        } catch (Exception e) {
            logger.error("GPS2LOCATION error", e);
        }
        return location;

    }
}
