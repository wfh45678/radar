package com.pgmmers.radar.enums;

/**
 * 
 * 状态.
 * 
 * @author feihu.wang
 * @since：2016年8月19日
 *
 */
public enum StatusType {

    INIT(0, "初始"), ACTIVE(1, "可用"), INACTIVE(2, "不可用");

    private int key;
    private String value;

    StatusType(int key, String value) {
        this.setKey(key);
        this.setValue(value);
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
