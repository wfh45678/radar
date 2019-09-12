package com.pgmmers.radar.vo.model;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.Serializable;
import java.util.Date;

public class RuleVO implements Serializable{
    static final long serialVersionUID = 324324256L;

    private Long id;


    private Long modelId;


    private Long activationId;


    private String name;

    private String label;

    private String scripts;


    private Integer initScore;


    private Integer baseNum;


    private String operator;


    private String abstractionName;


    private Integer status;


    private Date createTime;


    private Date updateTime;

    private Integer rate;

    @JsonProperty
    private JsonNode ruleDefinition;

    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public Long getModelId() {
        return modelId;
    }


    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }


    public Long getActivationId() {
        return activationId;
    }


    public void setActivationId(Long activationId) {
        this.activationId = activationId;
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public String getScripts() {
        return scripts;
    }


    public void setScripts(String scripts) {
        this.scripts = scripts;
    }


    public Integer getInitScore() {
        return initScore;
    }


    public void setInitScore(Integer initScore) {
        this.initScore = initScore;
    }


    public Integer getBaseNum() {
        return baseNum;
    }


    public void setBaseNum(Integer baseNum) {
        this.baseNum = baseNum;
    }


    public String getOperator() {
        return operator;
    }


    public void setOperator(String operator) {
        this.operator = operator;
    }


    public String getAbstractionName() {
        return abstractionName;
    }


    public void setAbstractionName(String abstractionName) {
        this.abstractionName = abstractionName;
    }


    public Integer getStatus() {
        return status;
    }


    public void setStatus(Integer status) {
        this.status = status;
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


    public Integer getRate() {
        return rate;
    }


    public void setRate(Integer rate) {
        this.rate = rate;
    }


    public JsonNode getRuleDefinition() {
        return ruleDefinition;
    }


    public void setRuleDefinition(JsonNode ruleDefinition) {
        this.ruleDefinition = ruleDefinition;
    }


    public String getLabel() {
        return label;
    }


    public void setLabel(String label) {
        this.label = label;
    }
    
    
}
