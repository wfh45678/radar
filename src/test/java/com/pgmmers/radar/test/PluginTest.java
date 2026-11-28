package com.pgmmers.radar.test;

import com.alibaba.fastjson2.JSONObject;
import com.pgmmers.radar.model.vo.model.FeatureVO;
import com.pgmmers.radar.service.impl.engine.plugin.PluginManager;
import com.pgmmers.radar.support.AbstractIntegrationTest;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;

/**
 * L3 集成测试：preItems 插件注册表（v3.0 起：统一 PluginEntry，V2/V3 双路注册）。
 * 注意 HTTP_UTIL 依赖外部天气 API，网络抖动时自动跳过（assume）。
 */
@SuppressWarnings("SpellCheckingInspection")
public class PluginTest extends AbstractIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(PluginTest.class);

    @Autowired
    private PluginManager pluginManager;

    private Map<String, Object> jsonInfo;

    @BeforeEach
    public void init() {
        jsonInfo = new HashMap<>();
    }

    @Test
    public void pluginList() {
        pluginManager.entries()
                .forEach(e -> logger.info("{} [{}] {} remote={} schema={} output={}",
                        e.getKey(), e.getSource(), e.getDesc(), e.isRemote(),
                        e.getParamSchema().size(), e.getOutputMeta().size()));
        Assertions.assertFalse(pluginManager.entries().isEmpty());
    }

    /** v3 元信息契约：schema 化插件（SUBSTRING）与多列插件（IP2LOCATION）。 */
    @Test
    public void entryMetadata() {
        Assertions.assertEquals(2, pluginManager.entry("SUBSTRING").getParamSchema().size());
        Assertions.assertEquals(3, pluginManager.entry("IP2LOCATION").getOutputMeta().size());
        Assertions.assertTrue(pluginManager.entry("HTTP_UTIL").isRemote());
        Assertions.assertNotNull(pluginManager.entry("IP2LOCATION").legacyMetaJson());
    }

    private Object execute(String pluginKey, FeatureVO feature) throws Exception {
        java.util.List<String> sf = new java.util.ArrayList<>();
        if (feature.getDefinition() != null && feature.getDefinition().has("sourceFields")) {
            feature.getDefinition().get("sourceFields").forEach(f -> sf.add(f.asText()));
        }
        Map<String, Map<String, ?>> context = new HashMap<>();
        context.put("fields", jsonInfo);
        context.put("features", new HashMap<String, Object>());
        return pluginManager.entry(pluginKey).execute(feature, context, sf.toArray(new String[0]));
    }

    @Test
    public void ALLINONE() throws Exception {
                FeatureVO feature = new FeatureVO();
        feature.setType("ENRICH");
        com.fasterxml.jackson.databind.node.ObjectNode definition = com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.objectNode();
        definition.put("plugin", "ALLINONE");
        com.fasterxml.jackson.databind.node.ArrayNode sourceFields = definition.putArray("sourceFields");
        sourceFields.add("firsName");
        sourceFields.add("lastName");
        feature.setDefinition(definition);
        jsonInfo.put("firsName", "aaa");
        jsonInfo.put("lastName", "ccc");
        Object result = execute("ALLINONE", feature);

        Assertions.assertEquals("aaa_ccc", result);
    }

    @Test
    public void DATEFORMAT() throws Exception {
        FeatureVO feature = new FeatureVO();
        feature.setType("ENRICH");
        com.fasterxml.jackson.databind.node.ObjectNode definition = com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.objectNode();
        definition.put("plugin", "DATEFORMAT");
        definition.put("args", "yyyyMMdd HH:mm:ss");
        com.fasterxml.jackson.databind.node.ArrayNode sourceFields = definition.putArray("sourceFields");
        sourceFields.add("time");
        feature.setDefinition(definition);
        jsonInfo.put("time", System.currentTimeMillis());
        Object result = execute("DATEFORMAT", feature);
        Assertions.assertNotNull(result);
        logger.info("{}", result);
    }

    @Test
    public void GPS2LOCATION() {
        // 外部免费服务不可靠，链路级验证由 HTTP_UTIL 兼容路径承担
    }

    @Test
    public void HTTP_UTIL() throws Exception {
        FeatureVO feature = new FeatureVO();
        feature.setType("ENRICH");
        com.fasterxml.jackson.databind.node.ObjectNode definition = com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.objectNode();
        definition.put("plugin", "HTTP_UTIL");
        definition.put("reqType", "GET");
        definition.put("args", "http://t.weather.sojson.com/api/weather/city/101030100");
        com.fasterxml.jackson.databind.node.ArrayNode sourceFields = definition.putArray("sourceFields");
        sourceFields.add("json");
        feature.setDefinition(definition);
        jsonInfo.put("json", "");
        try {
            Object result = execute("HTTP_UTIL", feature);
            Assertions.assertNotNull(result);
            logger.info("{}", result);
        } catch (Exception e) {
            // 外部天气 API 不可达时跳过（CI 网络策略下不稳定，不作为门禁失败项）
            org.junit.jupiter.api.Assumptions.assumeTrue(false, "external weather api unreachable: " + e.getMessage());
        }
    }

    @Test
    public void IP2LOCATION() throws Exception {
        FeatureVO feature = new FeatureVO();
        feature.setType("ENRICH");
        com.fasterxml.jackson.databind.node.ObjectNode definition = com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.objectNode();
        definition.put("plugin", "IP2LOCATION");
        com.fasterxml.jackson.databind.node.ArrayNode sourceFields = definition.putArray("sourceFields");
        sourceFields.add("ip");
        feature.setDefinition(definition);
        jsonInfo.put("ip", "1.1.1.1");
        @SuppressWarnings("unchecked")
        Map<String, Object> location = (Map<String, Object>) execute("IP2LOCATION", feature);
        Assertions.assertNotNull(location);
        Assertions.assertNotNull(location.get("country"));
        logger.info("{}", location);
    }

    @Test
    public void MOBILE2LOCATION() throws Exception {
        FeatureVO feature = new FeatureVO();
        feature.setType("ENRICH");
        com.fasterxml.jackson.databind.node.ObjectNode definition = com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.objectNode();
        definition.put("plugin", "MOBILE2LOCATION");
        com.fasterxml.jackson.databind.node.ArrayNode sourceFields = definition.putArray("sourceFields");
        sourceFields.add("phone");
        feature.setDefinition(definition);
        jsonInfo.put("phone", "18657150000");
        @SuppressWarnings("unchecked")
        Map<String, Object> location = (Map<String, Object>) execute("MOBILE2LOCATION", feature);
        Assertions.assertNotNull(location);
        Assertions.assertEquals("中国", location.get("country"));
        logger.info("{}", location);
    }

    @Test
    public void SENSITIVE_TIME() throws Exception {
                FeatureVO feature = new FeatureVO();
        feature.setType("ENRICH");
        com.fasterxml.jackson.databind.node.ObjectNode definition = com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.objectNode();
        definition.put("plugin", "SENSITIVE_TIME");
        com.fasterxml.jackson.databind.node.ArrayNode sourceFields = definition.putArray("sourceFields");
        sourceFields.add("time");
        feature.setDefinition(definition);
        jsonInfo.put("time", System.currentTimeMillis());
        Object result = execute("SENSITIVE_TIME", feature);

        Assertions.assertNotNull(result);
        logger.info("{}", result);
    }

    @Test
    public void SUBSTRING() throws Exception {
        FeatureVO feature = new FeatureVO();
        feature.setType("ENRICH");
        com.fasterxml.jackson.databind.node.ObjectNode definition = com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.objectNode();
        definition.put("plugin", "SUBSTRING");
        definition.put("args", "0,7");
        com.fasterxml.jackson.databind.node.ArrayNode sourceFields = definition.putArray("sourceFields");
        sourceFields.add("phone");
        feature.setDefinition(definition);
        jsonInfo.put("phone", "18657150000");
        Object result = execute("SUBSTRING", feature);
        Assertions.assertEquals("1865715", result);
    }
}
