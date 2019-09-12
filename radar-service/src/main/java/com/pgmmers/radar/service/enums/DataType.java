package com.pgmmers.radar.service.enums;

public enum DataType {

    FIELDS("fields", "基础字段"), //
    PREITEMS("preItems", "预处理字段"), //
    ABSTRACTIONS("abstractions", "抽象字段"),
	ACTIVATIONS("activations", "策略字段"),
	RULES("rules", "规则字段");

    private String name;
    private String desc;

    DataType(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static DataType get(String name) {
        for (DataType dt : DataType.values()) {
            if (dt.getName().equals(name))
                return dt;
        }
        return null;
    }

}
