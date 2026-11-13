package com.pgmmers.radar.test;

import com.alibaba.fastjson2.JSONObject;
import com.pgmmers.radar.RadarApplication;
import com.pgmmers.radar.service.engine.PluginServiceV2;
import com.pgmmers.radar.service.engine.vo.Location;
import com.pgmmers.radar.service.impl.engine.plugin.PluginManager;
import com.pgmmers.radar.model.vo.model.PreItemVO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;


import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;


@SuppressWarnings("SpellCheckingInspection")
@SpringBootTest(classes = RadarApplication.class)
public class PluginTest {

    private static final Logger logger = LoggerFactory.getLogger(PluginTest.class);

    @Autowired
    private PluginManager pluginManager;


    @Test
    @Deprecated
    public void pluginList() {
//        PluginManager.pluginServiceMap().values()
//                .stream()
//                .sorted(Comparator.comparing(PluginServiceV2::key))
//                .forEach(t -> logger.info(t.info()));
        pluginManager.getPluginServiceMap().values()
                .stream()
                .sorted(Comparator.comparing(PluginServiceV2::key))
                .forEach(t -> logger.info(t.info()));
    }





    private Map<String, Object> jsonInfo;

    @BeforeEach
    public void init() {
        jsonInfo = new HashMap<>();
    }

    @Test
    public void ALLINONE() {
        PreItemVO item = new PreItemVO();
        item.setSourceField("firsName,lastName");
        jsonInfo.put("firsName", "aaa");
        jsonInfo.put("lastName", "ccc");
        String[] sourceField = item.getSourceField().split(",");
//        PluginServiceV2 pluginServiceV2 = PluginManager.pluginServiceMap().get("ALLINONE");
        PluginServiceV2 pluginServiceV2 = pluginManager.getPluginServiceMap().get("ALLINONE");
        String result = (String) pluginServiceV2
                .handle(item, jsonInfo, sourceField);
        Assertions.assertNotNull(result);
        logger.info("{}", result);
    }

    @Test
    public void DATEFORMAT() {
        PreItemVO item = new PreItemVO();
        item.setArgs("yyyyMMdd HH:mm:ss");
        item.setSourceField("time");
        jsonInfo.put("time", System.currentTimeMillis());
        String[] sourceField = item.getSourceField().split(",");
//        PluginServiceV2 pluginServiceV2 = PluginManager.pluginServiceMap().get("DATEFORMAT");
        PluginServiceV2 pluginServiceV2 = pluginManager.getPluginServiceMap().get("DATEFORMAT");
        String result = (String) pluginServiceV2
                .handle(item, jsonInfo, sourceField);
        Assertions.assertNotNull(result);
        logger.info("{}", result);
    }

    @Test
    public void GPS2LOCATION() {

    }

    @Test
    public void HTTP_UTIL() {
        PreItemVO item = new PreItemVO();
        item.setReqType(HttpMethod.GET.name());
        item.setArgs("http://t.weather.sojson.com/api/weather/city/101030100");
        item.setSourceField("json");
        jsonInfo.put("json","");
        String[] sourceField = item.getSourceField().split(",");
//        PluginServiceV2 pluginServiceV2 = PluginManager.pluginServiceMap().get("HTTP_UTIL");
        PluginServiceV2 pluginServiceV2 = pluginManager.getPluginServiceMap().get("HTTP_UTIL");
        JSONObject result = (JSONObject) pluginServiceV2
                .handle(item, jsonInfo, sourceField);
        Assertions.assertNotNull(result);
        logger.info("{}", result);
    }

    @Test
    public void IP2LOCATION() {
        PreItemVO item = new PreItemVO();
        item.setSourceField("ip");
        jsonInfo.put("ip", "1.1.1.1");
        String[] sourceField = item.getSourceField().split(",");
//        PluginServiceV2 pluginServiceV2 = PluginManager.pluginServiceMap().get("IP2LOCATION");
        PluginServiceV2 pluginServiceV2 = pluginManager.getPluginServiceMap().get("IP2LOCATION");
        Location location = (Location) pluginServiceV2
                .handle(item, jsonInfo, sourceField);
        Assertions.assertNotNull(location);
        logger.info("{}", location);
    }

    @Test
    public void MOBILE2LOCATION() {
        PreItemVO item = new PreItemVO();
        item.setSourceField("phone");
        jsonInfo.put("phone", "18657150000");
        String[] sourceField = item.getSourceField().split(",");
//        PluginServiceV2 pluginServiceV2 = PluginManager.pluginServiceMap().get("MOBILE2LOCATION");
        PluginServiceV2 pluginServiceV2 = pluginManager.getPluginServiceMap().get("MOBILE2LOCATION");
        Location location = (Location) pluginServiceV2
                .handle(item, jsonInfo, sourceField);
        Assertions.assertNotNull(location);
        logger.info("{}", location);
    }

    @Test
    public void SENSITIVE_TIME() {
        PreItemVO item = new PreItemVO();
        item.setSourceField("time");
        jsonInfo.put("time",System.currentTimeMillis());
        String[] sourceField = item.getSourceField().split(",");
//        PluginServiceV2 pluginServiceV2 = PluginManager.pluginServiceMap().get("SENSITIVE_TIME");
        PluginServiceV2 pluginServiceV2 = pluginManager.getPluginServiceMap().get("SENSITIVE_TIME");
        String location = (String) pluginServiceV2
                .handle(item, jsonInfo, sourceField);
        Assertions.assertNotNull(location);
        logger.info("{}", location);
    }


    @Test
    public void SUBSTRING() {
        PreItemVO item = new PreItemVO();
        item.setArgs("0,7");
        item.setSourceField("phone");
        jsonInfo.put("phone", "18657150000");
        String[] sourceField = item.getSourceField().split(",");
//        PluginServiceV2 pluginServiceV2 = PluginManager.pluginServiceMap().get("SUBSTRING");
        PluginServiceV2 pluginServiceV2 = pluginManager.getPluginServiceMap().get("SUBSTRING");
        String location = (String) pluginServiceV2
                .handle(item, jsonInfo, sourceField);
        Assertions.assertNotNull(location);
        logger.info("{}", location);
    }
}
