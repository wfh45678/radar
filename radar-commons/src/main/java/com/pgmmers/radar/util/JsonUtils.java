package com.pgmmers.radar.util;

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
    public static ObjectMapper objectMapper = new ObjectMapper();

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
            return (new ObjectMapper()).setSerializationInclusion(Include.NON_NULL)
                    .writeValueAsString(object);
        } catch (JsonProcessingException var2) {
            throw new RuntimeException(var2);
        }
    }

    public static <E> E fromJson(String json, Class<E> type) {
        try {
            return (new ObjectMapper())
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .readValue(json, type);
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
