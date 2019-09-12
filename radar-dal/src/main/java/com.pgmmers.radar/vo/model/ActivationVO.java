package com.pgmmers.radar.vo.model;
import java.io.Serializable;
import java.util.Date;

public class ActivationVO implements Serializable{
    static final long serialVersionUID = 65434232L;

    private Long id;


    private String activationName;


    private Long modelId;


    private String label;

    private String comment;


    private Integer status;


    private Date createTime;


    private Date updateTime;

    private Integer bottom;


    private Integer median;


    private Integer high;
    
    private String ruleOrder;
    
    
    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public String getActivationName() {
        return activationName;
    }


    public void setActivationName(String activationName) {
        this.activationName = activationName;
    }


    public Long getModelId() {
        return modelId;
    }


    public void setModelId(Long modelId) {
        this.modelId = modelId;
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
  

}
