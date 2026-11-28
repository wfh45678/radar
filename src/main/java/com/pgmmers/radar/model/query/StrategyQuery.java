package com.pgmmers.radar.model.query;


public class StrategyQuery extends PageQuery {

    private Long scenarioId;
    private String name;
    private Integer status;

    public Long getModelId() {
        return scenarioId;
    }

    public void setModelId(Long scenarioId) {
        this.scenarioId = scenarioId;
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
