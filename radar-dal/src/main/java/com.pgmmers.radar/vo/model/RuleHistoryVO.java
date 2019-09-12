package com.pgmmers.radar.vo.model;
import java.io.Serializable;
import java.util.Date;

public class RuleHistoryVO implements Serializable{
	static final long serialVersionUID = 43500424323L;

	private Long id;
	
	private Long ruleId;
	
	private String merchantCode;
	
	private String label;
	
	private Integer initScore;
	
	private Integer baseNum;
	
	private String operator;
	
	private String abstractionName;
	
	private Integer rate;
	
	private Date updateTime;
	
	private String ruleDefinition;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getRuleId() {
		return ruleId;
	}

	public void setRuleId(Long ruleId) {
		this.ruleId = ruleId;
	}

	public String getMerchantCode() {
		return merchantCode;
	}

	public void setMerchantCode(String merchantCode) {
		this.merchantCode = merchantCode;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
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

	public Integer getRate() {
		return rate;
	}

	public void setRate(Integer rate) {
		this.rate = rate;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getRuleDefinition() {
		return ruleDefinition;
	}

	public void setRuleDefinition(String ruleDefinition) {
		this.ruleDefinition = ruleDefinition;
	}
}
