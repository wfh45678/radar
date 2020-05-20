package com.pgmmers.radar.vo.common;

public class FieldValueVO {

    private String key;

    private String value;
    
    private String field;

    public FieldValueVO(String key, String value) {
        this.key = key;
        this.value = value;
    }
    
    public FieldValueVO(String key, String field, String value) {
        this.key = key;
        this.value = value;
        this.field = field;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

}
