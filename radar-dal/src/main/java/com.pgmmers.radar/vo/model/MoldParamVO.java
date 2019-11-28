package com.pgmmers.radar.vo.model;

import java.io.Serializable;

/**
 * <p>
 * 机器学习模型配置，目前只考虑输入层为离散值的情况，不考虑需要词嵌入和融入卷积层，其中
 * 离散值通过表达式取数从前置流程传递过来.
 * </p>
 *
 * @author guor
 * @date 2019/11/28
 */
public class MoldParamVO implements Serializable {
    private Long id;
    /**
     * 取数表达式
     */
    private String exp;
    /**
     * 参数顺序，显式配置，强制有序
     */
    private int order;
}
