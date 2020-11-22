package com.pgmmers.radar.service.impl.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.StringTokenizer;

/**
 * @author 爱好者提供.
 * @since 2020-10-18
 */
public class JsonParserUtil {

    private static final  Logger logger = LoggerFactory.getLogger(JsonParserUtil.class);

    private static final String AQL_SEPARATOR = ".";

    /**
     * 获取方法链的值
     */
    public static JSONObject jsonObject(JSONObject json, String aql) {
        logger.debug("#获取 json 节点 safety ".concat(aql));
        StringTokenizer token = new StringTokenizer(aql, AQL_SEPARATOR);
        try {
            while (token.hasMoreElements()) {
                String argsString = token.nextToken();
                json = json.getJSONObject(argsString);
            }
            return json;
        } catch (Exception e) {
            logger.warn("解析 json 节点".concat(aql).concat("错误:"), e);
        }
        return new JSONObject();
    }

    /**
     * 获取安全方法链的 JSONArray
     */
    public static JSONArray array(JSONObject json, String aql) {
        logger.debug("#获取 json 节点组 safety ".concat(aql));
        JSONArray json_array = new JSONArray();
        StringTokenizer token = new StringTokenizer(aql, AQL_SEPARATOR);
        try {
            while (token.hasMoreElements()) {
                String argsString = token.nextToken();
                if (token.hasMoreElements()) {
                    json = json.getJSONObject(argsString);
                } else {
                    json_array = json.getJSONArray(argsString);
                }
            }
        } catch (Exception e) {
            logger.warn("解析 json 节点".concat(aql).concat("错误:"), e);
        }
        if (json_array == null) {
            json_array = new JSONArray();
        }
        return json_array;
    }

    /**
     * 获取安全方法链的值，方法中出现错误返回传入的 value
     */
    @SuppressWarnings("unchecked")
    public static <T> T value(String jsonStr, String aql, T defaultResult) {
        logger.debug("#获取 json 节点 safety ".concat(aql));
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        StringTokenizer token = new StringTokenizer(aql, AQL_SEPARATOR);
        T tmp_result = null;
        try {
            while (token.hasMoreElements()) {
                String argsString = token.nextToken();
                if (token.hasMoreElements()) {
                    jsonObject = jsonObject.getJSONObject(argsString);
                } else {
                    tmp_result = (T) jsonObject.get(argsString);
                }
            }
        } catch (Exception e) {
            String message = "解析 json 节点".concat(aql).concat("错误:");
            logger.warn(message, e);
        }
        defaultResult = (tmp_result == null) ? defaultResult : tmp_result;
        return defaultResult;
    }

    /**
     * 获取安全方法链的值，方法中出现错误返回传入的 value
     */
    @SuppressWarnings("unchecked")
    public static <T> T value(Object javaObject, String aql, T defaultValue) {
        logger.debug("#获取 json 节点 safety ".concat(aql));
        JSONObject json_object = (JSONObject) JSONObject.toJSON(javaObject);
        StringTokenizer token = new StringTokenizer(aql, AQL_SEPARATOR);
        T tmp_result = null;
        try {
            while (token.hasMoreElements()) {
                String args_string = token.nextToken();
                if (token.hasMoreElements()) {
                    json_object = json_object.getJSONObject(args_string);
                } else {
                    tmp_result = (T) json_object.get(args_string);
                }
            }
        } catch (Exception e) {
            logger.warn("解析 json 节点".concat(aql).concat("错误:"), e);
        }
        defaultValue = (tmp_result == null) ? defaultValue : tmp_result;
        return defaultValue;
    }

    public static void main(String[] args) {
        JSONObject jsonObject = JSONObject.parseObject("{\"a\":{\"b\":{\"c\":1}}}");
        System.out.println(JsonParserUtil.jsonObject(jsonObject, "a.b"));
        Integer value = JsonParserUtil.value(jsonObject, "a.b.c", null);
        System.out.println(value);
        Integer value1 = JsonParserUtil.value("{\"a\":{\"b\":{\"c\":1}}}", "a.b.c", null);
        System.out.println(value1);
    }
}