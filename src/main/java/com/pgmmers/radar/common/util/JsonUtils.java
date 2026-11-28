package com.pgmmers.radar.common.util;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * json对象转换
 *
 * @author xushuai
 */
public class JsonUtils {

    private static Logger logger = LoggerFactory.getLogger(JsonUtils.class);

    /**
     * 共享 mapper：日期格式 yyyy-MM-dd HH:mm:ss（与 fastjson2 发布侧对称——
     * v5.0 修复：radar_channel_* 配置推送为 fastjson2 序列化（日期为格式化字符串），
     * 订阅端裸 Jackson 只认 epoch/ISO 导致反序列化崩溃、缓存失效推送自 v2.x 起静默失效）。
     */
    public static ObjectMapper objectMapper = new ObjectMapper()
            .setSerializationInclusion(Include.NON_NULL)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setDateFormat(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

    public static JsonNode getJsonNode(String str) {
        try {
            if (StringUtils.isEmpty(str)) {
                return null;
            }
            return objectMapper.readTree(str);
        } catch (JsonProcessingException e) {
            logger.error("getJsonNode error{}", str, e);
            return null;
        }
    }

    public static String jsonNodeToString(JsonNode jsonNode) {
        try {
            if (jsonNode == null) {
                return null;
            }
            return objectMapper.writeValueAsString(jsonNode);
        } catch (JsonProcessingException e) {
            logger.error("jsonNodeToString error{}", jsonNode.asText(), e);
            return null;
        }
    }

    public JsonUtils() {
    }

    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException var2) {
            throw new RuntimeException(var2);
        }
    }

    public static <E> E fromJson(String json, Class<E> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (IOException var3) {
            throw new RuntimeException(var3);
        }
    }

    public static JavaType getCollectionType(Class<?> collectionClass, Class<?>... elementClasses) {
        return (new ObjectMapper()).getTypeFactory()
                .constructParametricType(collectionClass, elementClasses);
    }

    public static String toJsonString(Object object) {
        try {
            return (new ObjectMapper()).writeValueAsString(object);
        } catch (JsonProcessingException var2) {
            var2.printStackTrace();
            return null;
        }
    }

    public static Map writeJsonToMap(String json) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        Map map = null;

        try {
            map = (Map) mapper.readValue(json, Map.class);
            return map;
        } catch (IOException var4) {
            throw new RuntimeException(var4);
        }
    }
}
