package com.pgmmers.radar.model.vo.model;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.Serializable;
import java.util.Date;

/**
 * 特征 VO（v4.1）：definition/output 以 JsonNode 形态进出管理端 API；
 * 引擎执行时按 TYPE 解析 DEFINITION。
 */
public class FeatureVO implements Serializable {

    static final long serialVersionUID = 65434240L;

    private Long id;

    private String featureName;

    /** 特征类型：ENRICH/AGGREGATE/DERIVED/RATIO */
    private String type;

    private String label;

    private Long scenarioId;

    /** 阶段内执行顺序 */
    private Integer orderNo;

    private String comment;

    private Integer status;

    /** 类型化定义 */
    private JsonNode definition;

    /** 多列输出列声明 [{column,title,type}] */
    private JsonNode output;

    private Date createTime;

    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFeatureName() {
        return featureName;
    }

    public void setFeatureName(String featureName) {
        this.featureName = featureName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Long getScenarioId() {
        return scenarioId;
    }

    public void setScenarioId(Long scenarioId) {
        this.scenarioId = scenarioId;
    }

    public Integer getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(Integer orderNo) {
        this.orderNo = orderNo;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public JsonNode getDefinition() {
        return definition;
    }

    public void setDefinition(JsonNode definition) {
        this.definition = definition;
    }

    public JsonNode getOutput() {
        return output;
    }

    public void setOutput(JsonNode output) {
        this.output = output;
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
}
