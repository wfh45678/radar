package com.pgmmers.radar.enums;

/**
 * 
 * 插件类型.
 * 
 * @author feihu.wang
 * @version Revision 1.0.0
 * @since：2016年8月19日
 *
 */
public enum PluginType {

    IP2LOCATION(
            1,
            "ip2location",
            "IP转换成地址",
            null,
            "[{\"column\":\"country\", \"title\":\"国家\", \"type\":\"STRING\"},{\"column\":\"province\", \"title\":\"省份\", \"type\":\"STRING\"},{\"column\":\"city\", \"title\":\"城市\", \"type\":\"STRING\"}]"), //
    GPS2LOCATION(
            2,
            "gps2location",
            "GPS转换成地址",
            null,
            "[{\"column\":\"country\", \"title\":\"国家\", \"type\":\"STRING\"},{\"column\":\"province\", \"title\":\"省份\", \"type\":\"STRING\"},{\"column\":\"city\", \"title\":\"城市\", \"type\":\"STRING\"}]"), //
    ALLINONE(3, "allInOne", "字段合并", "STRING", null), //
    SUBSTRING(4, "subString", "字符串截短", "STRING", null), //
    MOBILE2LOCATION(
            5,
            "mobile2location",
            "手机号码归属地",
            null,
            "[{\"column\":\"country\", \"title\":\"国家\", \"type\":\"STRING\"},{\"column\":\"province\", \"title\":\"省份\", \"type\":\"STRING\"},{\"column\":\"city\", \"title\":\"城市\", \"type\":\"STRING\"}]"), //
    SENSITIVE_TIME(6, "getSensitiveTime", "敏感时间段(小时)", "STRING", null),
    DATEFORMAT(7, "formatDate", "日期时间格式化", "STRING", null),
    HTTP_UTIL(8, "httpRequest", "HttpUtil", "JSON", null),
    ;

    private Integer key;
    private String method;
    private String desc;
    private String type;
    private String meta;

    PluginType(Integer key, String method, String desc, String type, String meta) {
        this.key = key;
        this.method = method;
        this.desc = desc;
        this.type = type;
        this.meta = meta;
    }

    public Integer getKey() {
        return key;
    }

    public void setKey(Integer key) {
        this.key = key;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMeta() {
        return meta;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }

    public static PluginType get(String plugin) {
        for (PluginType pluginType : PluginType.values()) {
            if (pluginType.name().equals(plugin))
                return pluginType;
        }
        return null;
    }

}
