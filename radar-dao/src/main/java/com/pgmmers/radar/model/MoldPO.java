package com.pgmmers.radar.model;

import javax.persistence.Table;
import java.util.Date;

@Table(name = "engine_mold")
public class MoldPO {
    /**
     * 自增ID，主键
     */
    private Long id;
    /**
     * 模型名称
     */
    private String name;
    /**
     * 输入层参数的key
     */
    private String feed;
    /**
     * 模型文件路径
     */
    private String path;
    /**
     * 模型更新时间
     */
    private Date updateDate;
}
