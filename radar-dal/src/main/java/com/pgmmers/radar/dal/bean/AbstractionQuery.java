package com.pgmmers.radar.dal.bean;


public class AbstractionQuery extends PageQuery {

    private Long modelId;
    private Integer aggregateType;
    private String name;
    private Integer status;

    public Long getModelId() {
        return modelId;
    }

    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }

    public Integer getAggregateType() {
        return aggregateType;
    }

    public void setAggregateType(Integer aggregateType) {
        this.aggregateType = aggregateType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

}
