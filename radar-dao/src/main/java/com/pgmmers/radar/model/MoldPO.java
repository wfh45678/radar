package com.pgmmers.radar.model;

import javax.persistence.Table;
import java.util.Date;

@Table(name = "engine_mold")
public class MoldPO {
    /**
     * 自增ID，主键
     */
    private Long id;
    private Long modelId;
    /**
     * 模型名称
     */
    private String name;
    /**
     * 模型文件路径
     */
    private String path;
    private String tag;
    private String operation;
    /**
     * 模型更新时间
     */
    private Date updateDate;
    /**
     * 模型类型
     */
    private String type;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getModelId() {
        return modelId;
    }

    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }
}
