package com.pgmmers.radar.vo.common;


public class PluginVO {

    private Integer key;
    private String method;
    private String desc;

    // private JSONArray meta;


    public PluginVO(Integer key, String method, String desc) {
        this.key = key;
        this.method = method;
        this.desc = desc;
    }

    public Integer getKey() {
        return key;
    }

    public void setKey(Integer key) {
        this.key = key;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

}
