package com.pgmmers.radar.vo.model;

import java.io.Serializable;
import java.util.Date;

public class AbstractionVO2 implements Serializable{
    static final long serialVersionUID = 234345456231L;

    private Long id;

    private String name;
    
    private String label;

    private Long modelId;

    private Integer aggregateType;

    private String searchField;

    private Integer searchIntervalType;

    private Integer searchIntervalValue;

    private String functionField;

    private String ruleScript;

    private Integer status;

    private String comment;

    private Date createTime;

    private Date updateTime;

    private String dataCollectionNames;

    private String  ruleDefinition;
    
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

    public String getSearchField() {
        return searchField;
    }

    public void setSearchField(String searchField) {
        this.searchField = searchField;
    }

    public Integer getSearchIntervalType() {
        return searchIntervalType;
    }

    public void setSearchIntervalType(Integer searchIntervalType) {
        this.searchIntervalType = searchIntervalType;
    }

    public Integer getSearchIntervalValue() {
        return searchIntervalValue;
    }

    public void setSearchIntervalValue(Integer searchIntervalValue) {
        this.searchIntervalValue = searchIntervalValue;
    }

    public String getFunctionField() {
        return functionField;
    }

    public void setFunctionField(String functionField) {
        this.functionField = functionField;
    }

    public String getRuleScript() {
        return ruleScript;
    }

    public void setRuleScript(String ruleScript) {
        this.ruleScript = ruleScript;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getDataCollectionNames() {
        return dataCollectionNames;
    }

    public void setDataCollectionNames(String dataCollectionNames) {
        this.dataCollectionNames = dataCollectionNames;
    }

    public String getRuleDefinition() {
        return ruleDefinition;
    }

    public void setRuleDefinition(String ruleDefinition) {
        this.ruleDefinition = ruleDefinition;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

}
