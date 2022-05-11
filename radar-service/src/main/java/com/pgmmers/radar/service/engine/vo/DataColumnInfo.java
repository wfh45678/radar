package com.pgmmers.radar.service.engine.vo;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.ArrayList;
import java.util.List;

public class DataColumnInfo {

    @JSONField(ordinal = 1)
    private String label;
    @JSONField(ordinal = 2)
    private String value;
    @JSONField(ordinal = 3)
    private String type;
    @JSONField(ordinal = 4)
    private List<DataColumnInfo> children;

    public DataColumnInfo() {

    }

    public DataColumnInfo(String label, String value) {
        this.label = label;
        this.value = value;
    }

    public DataColumnInfo(String label, String value, String type) {
        this.label = label;
        this.value = value;
        this.type = type;
    }

    public DataColumnInfo(String label, String value, List<DataColumnInfo> children) {
        this.label = label;
        this.value = value;
        this.children = children;
    }

    public DataColumnInfo(String label, String value, String type, List<DataColumnInfo> children) {
        this.label = label;
        this.value = value;
        this.type = type;
        this.children = children;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<DataColumnInfo> getChildren() {
        return children;
    }

    public void addChildren(String label, String value, String type) {
        if (this.children == null) {
            this.children = new ArrayList<DataColumnInfo>();
        }
        this.children.add(new DataColumnInfo(label, value, type));
    }

    public void addChildren(String label, String value, List<DataColumnInfo> children) {
        if (this.children == null) {
            this.children = new ArrayList<DataColumnInfo>();
        }
        this.children.add(new DataColumnInfo(label, value, children));
    }

}
