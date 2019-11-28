package com.pgmmers.radar.model;

import javax.persistence.Table;

@Table(name = "engine_mold_param")
public class MoldParamPO {
    private Long id;
    private Long moldId;
    /**
     * 取数表达式
     */
    private String exp;
    /**
     * 参数顺序，显式配置，强制有序
     */
    private int order;
}
