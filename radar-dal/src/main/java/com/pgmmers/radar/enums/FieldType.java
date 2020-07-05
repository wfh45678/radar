package com.pgmmers.radar.enums;

/**
 * 
 * 字段类型.
 * 
 * @author feihu.wang
 * @version Revision 1.0.0
 * @since：2016年8月19日
 *
 */
public enum FieldType {
    STRING(1, "字符串", String.class), INTEGER(2, "整数", Integer.class), LONG(3, "长整数", Long.class), DOUBLE(4, "浮点数", Double.class);

    private int key;
    private String desc;

    private Class<?> clazz;

    FieldType(int key,String desc, Class<?> clazz) {
        this.key = key;
        this.desc = desc;
        this.clazz = clazz;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

}
