package com.pgmmers.radar.model.vo.model;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.Serializable;
import java.util.Date;

public class StrategyVO implements Serializable{
    static final long serialVersionUID = 65434232L;

    private Long id;


    private String strategyName;

    /** 策略类型：RULE_SET/SCORECARD/DECISION_TREE（v4.0） */
    private String type;


    private Long scenarioId;


    private String label;

    private String comment;


    private Integer status;


    private Date createTime;


    private Date updateTime;

    private Integer bottom;


    private Integer median;


    private Integer high;

    private String ruleOrder;

    /** 策略类型化定义（评分卡/决策树 JSON；规则集为空） */
    private JsonNode definition;


    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public String getStrategyName() {
        return strategyName;
    }


    public void setStrategyName(String strategyName) {
        this.strategyName = strategyName;
    }


    public String getType() {
        return type;
    }


    public void setType(String type) {
        this.type = type;
    }


    public Long getModelId() {
        return scenarioId;
    }


    public void setModelId(Long scenarioId) {
        this.scenarioId = scenarioId;
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


    public Integer getBottom() {
        return bottom;
    }


    public void setBottom(Integer bottom) {
        this.bottom = bottom;
    }


    public Integer getMedian() {
        return median;
    }


    public void setMedian(Integer median) {
        this.median = median;
    }


    public Integer getHigh() {
        return high;
    }


    public void setHigh(Integer high) {
        this.high = high;
    }


    public String getLabel() {
        return label;
    }


    public void setLabel(String label) {
        this.label = label;
    }

	public String getRuleOrder() {
		return ruleOrder;
	}

	public void setRuleOrder(String ruleOrder) {
		this.ruleOrder = ruleOrder;
	}


	public JsonNode getDefinition() {
		return definition;
	}

	public void setDefinition(JsonNode definition) {
		this.definition = definition;
	}

}
