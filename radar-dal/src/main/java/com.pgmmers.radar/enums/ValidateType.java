package com.pgmmers.radar.enums;

/**
 * 
 * 校验类型.
 * @author feihu.wang
 * @version Revision 0.0.1
 */
public enum ValidateType {

    INTEGER(1, "整型"), LONG(2, "整型"), DOUBLE(3, "浮点型"), STRING(4, "字符串"),

    LENGTH(11, "字符串长度"), MAX_LENGTH(11, "最大长度"), MIN_LENGTH(11, "最小长度"),

    EMAIL(21, "邮箱"), MOBILE(22, "手机"), IP(23, "IP"),

    REGEX(99, "正则表达式");

    ValidateType(int key, String desc) {
        this.key = key;
        this.desc = desc;
    }

    private int key;
    private String desc;

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

}
