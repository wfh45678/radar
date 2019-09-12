package com.pgmmers.radar.enums;

/**
 * 
 * 名单类型.
 * 
 * @author feihu.wang
 * @version Revision 1.0.0
 * @since：2016年8月19日
 *
 */
public enum DataListType {

    BLACK(1, "黑名单"), WHITE(2, "白名单");
    private Integer key;
    private String desc;

    DataListType(Integer key, String desc) {
        this.key = key;
        this.desc = desc;
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

}
