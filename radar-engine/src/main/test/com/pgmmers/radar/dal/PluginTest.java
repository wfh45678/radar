package com.pgmmers.radar.dal;

import com.alibaba.fastjson.JSONObject;
import com.pgmmers.radar.EngineApplication;
import com.pgmmers.radar.service.engine.PluginServiceV2;
import com.pgmmers.radar.service.engine.vo.Location;
import com.pgmmers.radar.service.impl.engine.Plugin.PluginManager;
import com.pgmmers.radar.vo.model.PreItemVO;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit4.SpringRunner;


@SuppressWarnings("SpellCheckingInspection")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = EngineApplication.class)
public class PluginTest {

    private static final Logger logger = LoggerFactory.getLogger(PluginTest.class);

    @Test
    public void pluginList() {
        PluginManager.pluginServiceMap().values()
                .stream()
                .sorted(Comparator.comparing(PluginServiceV2::key))
                .forEach(t -> logger.info(t.info()));
    }

    private Map<String, Object> jsonInfo;

    @Before
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
        PluginServiceV2 pluginServiceV2 = PluginManager.pluginServiceMap().get("ALLINONE");
        String result = (String) pluginServiceV2
                .handle(item, jsonInfo, sourceField);
        Assert.assertNotNull(result);
        logger.info("{}", result);
    }

    @Test
    public void DATEFORMAT() {
        PreItemVO item = new PreItemVO();
        item.setArgs("yyyyMMdd HH:mm:ss");
        item.setSourceField("time");
        jsonInfo.put("time", System.currentTimeMillis());
        String[] sourceField = item.getSourceField().split(",");
        PluginServiceV2 pluginServiceV2 = PluginManager.pluginServiceMap().get("DATEFORMAT");
        String result = (String) pluginServiceV2
                .handle(item, jsonInfo, sourceField);
        Assert.assertNotNull(result);
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
        PluginServiceV2 pluginServiceV2 = PluginManager.pluginServiceMap().get("HTTP_UTIL");
        JSONObject result = (JSONObject) pluginServiceV2
                .handle(item, jsonInfo, sourceField);
        Assert.assertNotNull(result);
        logger.info("{}", result);
    }

    @Test
    public void IP2LOCATION() {
        PreItemVO item = new PreItemVO();
        item.setSourceField("ip");
        jsonInfo.put("ip", "1.1.1.1");
        String[] sourceField = item.getSourceField().split(",");
        PluginServiceV2 pluginServiceV2 = PluginManager.pluginServiceMap().get("IP2LOCATION");
        Location location = (Location) pluginServiceV2
                .handle(item, jsonInfo, sourceField);
        Assert.assertNotNull(location);
        logger.info("{}", location);
    }

    @Test
    public void MOBILE2LOCATION() {
        PreItemVO item = new PreItemVO();
        item.setSourceField("phone");
        jsonInfo.put("phone", "18657150000");
        String[] sourceField = item.getSourceField().split(",");
        PluginServiceV2 pluginServiceV2 = PluginManager.pluginServiceMap().get("MOBILE2LOCATION");
        Location location = (Location) pluginServiceV2
                .handle(item, jsonInfo, sourceField);
        Assert.assertNotNull(location);
        logger.info("{}", location);
    }

    @Test
    public void SENSITIVE_TIME() {
        PreItemVO item = new PreItemVO();
        item.setSourceField("time");
        jsonInfo.put("time",System.currentTimeMillis());
        String[] sourceField = item.getSourceField().split(",");
        PluginServiceV2 pluginServiceV2 = PluginManager.pluginServiceMap().get("SENSITIVE_TIME");
        String location = (String) pluginServiceV2
                .handle(item, jsonInfo, sourceField);
        Assert.assertNotNull(location);
        logger.info("{}", location);
    }


    @Test
    public void SUBSTRING() {
        PreItemVO item = new PreItemVO();
        item.setArgs("0,7");
        item.setSourceField("phone");
        jsonInfo.put("phone", "18657150000");
        String[] sourceField = item.getSourceField().split(",");
        PluginServiceV2 pluginServiceV2 = PluginManager.pluginServiceMap().get("SUBSTRING");
        String location = (String) pluginServiceV2
                .handle(item, jsonInfo, sourceField);
        Assert.assertNotNull(location);
        logger.info("{}", location);
    }
}
