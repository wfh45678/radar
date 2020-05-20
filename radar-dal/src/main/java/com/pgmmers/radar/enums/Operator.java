package com.pgmmers.radar.enums;

/**
 * 
 * 运算符.
 * 
 * @author feihu.wang
 * @since：2016年8月19日
 *
 */
public enum Operator {

    ADD(1, "add"), SUB(2, "sub"), MUL(3, "mul"), DIV(4, "div"), NONE(5, "none");

    private Integer key;

    private String desc;

    Operator(Integer key, String desc) {
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
