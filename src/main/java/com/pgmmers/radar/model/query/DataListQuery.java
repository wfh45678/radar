package com.pgmmers.radar.model.query;


public class DataListQuery extends PageQuery {

    private Long scenarioId;
    private String name;
    private String listType;
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

    public String getListType() {
        return listType;
    }

    public void setListType(String listType) {
        this.listType = listType;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

}
