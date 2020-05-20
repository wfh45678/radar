package com.pgmmers.radar.vo.common;


import com.pgmmers.radar.enums.PluginType;

public class PluginVO {

    private Integer key;
    private String method;
    private String desc;

    // private JSONArray meta;



    public PluginVO(PluginType plugin) {
        this.key = plugin.getKey();
        this.method = plugin.name();
        this.desc = plugin.getDesc();
        // if (!StringUtils.isEmpty(plugin.getMeta())) {
        // this.meta = JSONArray.parseArray(plugin.getMeta());
        // }
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
