package com.pgmmers.radar.model.query;

import com.pgmmers.radar.model.query.PageQuery;

public class TermQuery extends PageQuery {

    private Long scenarioId;
    
    private String fieldName;
    
    private String fieldValue;
    
    private String beginTime;
    
    private String endTime;

    public Long getModelId() {
        return scenarioId;
    }

    public void setModelId(Long scenarioId) {
        this.scenarioId = scenarioId;
    }

    public String getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(String beginTime) {
        this.beginTime = beginTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(String fieldValue) {
        this.fieldValue = fieldValue;
    }

}
